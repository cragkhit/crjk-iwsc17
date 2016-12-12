package org.apache.tomcat.jdbc.pool.interceptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class StatementDecoratorInterceptor extends AbstractCreateStatementInterceptor {
    private static final Log logger = LogFactory.getLog ( StatementDecoratorInterceptor.class );
    protected static final String EXECUTE_QUERY  = "executeQuery";
    protected static final String GET_GENERATED_KEYS = "getGeneratedKeys";
    protected static final String GET_RESULTSET  = "getResultSet";
    protected static final String[] RESULTSET_TYPES = {EXECUTE_QUERY, GET_GENERATED_KEYS, GET_RESULTSET};
    protected static final Constructor<?>[] constructors = new Constructor[AbstractCreateStatementInterceptor.STATEMENT_TYPE_COUNT];
    protected static Constructor<?> resultSetConstructor = null;
    @Override
    public void closeInvoked() {
    }
    protected Constructor<?> getConstructor ( int idx, Class<?> clazz ) throws NoSuchMethodException {
        if ( constructors[idx] == null ) {
            Class<?> proxyClass = Proxy.getProxyClass ( StatementDecoratorInterceptor.class.getClassLoader(),
                                  new Class[] { clazz } );
            constructors[idx] = proxyClass.getConstructor ( new Class[] { InvocationHandler.class } );
        }
        return constructors[idx];
    }
    protected Constructor<?> getResultSetConstructor() throws NoSuchMethodException {
        if ( resultSetConstructor == null ) {
            Class<?> proxyClass = Proxy.getProxyClass ( StatementDecoratorInterceptor.class.getClassLoader(),
                                  new Class[] { ResultSet.class } );
            resultSetConstructor = proxyClass.getConstructor ( new Class[] { InvocationHandler.class } );
        }
        return resultSetConstructor;
    }
    @Override
    public Object createStatement ( Object proxy, Method method, Object[] args, Object statement, long time ) {
        try {
            String name = method.getName();
            Constructor<?> constructor = null;
            String sql = null;
            if ( compare ( CREATE_STATEMENT, name ) ) {
                constructor = getConstructor ( CREATE_STATEMENT_IDX, Statement.class );
            } else if ( compare ( PREPARE_STATEMENT, name ) ) {
                constructor = getConstructor ( PREPARE_STATEMENT_IDX, PreparedStatement.class );
                sql = ( String ) args[0];
            } else if ( compare ( PREPARE_CALL, name ) ) {
                constructor = getConstructor ( PREPARE_CALL_IDX, CallableStatement.class );
                sql = ( String ) args[0];
            } else {
                return statement;
            }
            return createDecorator ( proxy, method, args, statement, constructor, sql );
        } catch ( Exception x ) {
            if ( x instanceof InvocationTargetException ) {
                Throwable cause = x.getCause();
                if ( cause instanceof ThreadDeath ) {
                    throw ( ThreadDeath ) cause;
                }
                if ( cause instanceof VirtualMachineError ) {
                    throw ( VirtualMachineError ) cause;
                }
            }
            logger.warn ( "Unable to create statement proxy for slow query report.", x );
        }
        return statement;
    }
    protected Object createDecorator ( Object proxy, Method method, Object[] args,
                                       Object statement, Constructor<?> constructor, String sql )
    throws InstantiationException, IllegalAccessException, InvocationTargetException {
        Object result = null;
        StatementProxy<Statement> statementProxy =
            new StatementProxy<> ( ( Statement ) statement, sql );
        result = constructor.newInstance ( new Object[] { statementProxy } );
        statementProxy.setActualProxy ( result );
        statementProxy.setConnection ( proxy );
        statementProxy.setConstructor ( constructor );
        return result;
    }
    protected boolean isExecuteQuery ( String methodName ) {
        return EXECUTE_QUERY.equals ( methodName );
    }
    protected boolean isExecuteQuery ( Method method ) {
        return isExecuteQuery ( method.getName() );
    }
    protected boolean isResultSet ( Method method, boolean process ) {
        return process ( RESULTSET_TYPES, method, process );
    }
    protected class StatementProxy<T extends java.sql.Statement> implements InvocationHandler {
        protected boolean closed = false;
        protected T delegate;
        private Object actualProxy;
        private Object connection;
        private String sql;
        private Constructor<?> constructor;
        public StatementProxy ( T delegate, String sql ) {
            this.delegate = delegate;
            this.sql = sql;
        }
        public T getDelegate() {
            return this.delegate;
        }
        public String getSql() {
            return sql;
        }
        public void setConnection ( Object proxy ) {
            this.connection = proxy;
        }
        public Object getConnection() {
            return this.connection;
        }
        public void setActualProxy ( Object proxy ) {
            this.actualProxy = proxy;
        }
        public Object getActualProxy() {
            return this.actualProxy;
        }
        public Constructor<?> getConstructor() {
            return constructor;
        }
        public void setConstructor ( Constructor<?> constructor ) {
            this.constructor = constructor;
        }
        public void closeInvoked() {
            if ( getDelegate() != null ) {
                try {
                    getDelegate().close();
                } catch ( SQLException ignore ) {
                }
            }
            closed = true;
            delegate = null;
        }
        @Override
        public Object invoke ( Object proxy, Method method, Object[] args ) throws Throwable {
            if ( compare ( TOSTRING_VAL, method ) ) {
                return toString();
            }
            boolean close = compare ( CLOSE_VAL, method );
            if ( close && closed ) {
                return null;
            }
            if ( compare ( ISCLOSED_VAL, method ) ) {
                return Boolean.valueOf ( closed );
            }
            if ( closed ) {
                throw new SQLException ( "Statement closed." );
            }
            if ( compare ( GETCONNECTION_VAL, method ) ) {
                return connection;
            }
            boolean process = false;
            process = isResultSet ( method, process );
            Object result = null;
            try {
                if ( close ) {
                    closeInvoked();
                } else {
                    result = method.invoke ( delegate, args );
                }
            } catch ( Throwable t ) {
                if ( t instanceof InvocationTargetException
                        && t.getCause() != null ) {
                    throw t.getCause();
                } else {
                    throw t;
                }
            }
            if ( process && result != null ) {
                Constructor<?> cons = getResultSetConstructor();
                result = cons.newInstance ( new Object[] {new ResultSetProxy ( actualProxy, result ) } );
            }
            return result;
        }
        @Override
        public String toString() {
            StringBuffer buf = new StringBuffer ( StatementProxy.class.getName() );
            buf.append ( "[Proxy=" );
            buf.append ( System.identityHashCode ( this ) );
            buf.append ( "; Sql=" );
            buf.append ( getSql() );
            buf.append ( "; Delegate=" );
            buf.append ( getDelegate() );
            buf.append ( "; Connection=" );
            buf.append ( getConnection() );
            buf.append ( "]" );
            return buf.toString();
        }
    }
    protected class ResultSetProxy implements InvocationHandler {
        private Object st;
        private Object delegate;
        public ResultSetProxy ( Object st, Object delegate ) {
            this.st = st;
            this.delegate = delegate;
        }
        @Override
        public Object invoke ( Object proxy, Method method, Object[] args ) throws Throwable {
            if ( method.getName().equals ( "getStatement" ) ) {
                return this.st;
            } else {
                try {
                    return method.invoke ( this.delegate, args );
                } catch ( Throwable t ) {
                    if ( t instanceof InvocationTargetException
                            && t.getCause() != null ) {
                        throw t.getCause();
                    } else {
                        throw t;
                    }
                }
            }
        }
    }
}
