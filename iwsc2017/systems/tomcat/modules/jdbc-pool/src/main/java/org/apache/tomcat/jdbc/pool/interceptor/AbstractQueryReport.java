package org.apache.tomcat.jdbc.pool.interceptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.jdbc.pool.JdbcInterceptor;
public abstract class AbstractQueryReport extends AbstractCreateStatementInterceptor {
    private static final Log log = LogFactory.getLog ( AbstractQueryReport.class );
    protected long threshold = 1000;
    protected static final Constructor<?>[] constructors =
        new Constructor[AbstractCreateStatementInterceptor.STATEMENT_TYPE_COUNT];
    public AbstractQueryReport() {
        super();
    }
    protected abstract void prepareStatement ( String sql, long time );
    protected abstract void prepareCall ( String query, long time );
    protected String reportFailedQuery ( String query, Object[] args, final String name, long start, Throwable t ) {
        String sql = ( query == null && args != null &&  args.length > 0 ) ? ( String ) args[0] : query;
        if ( sql == null && compare ( EXECUTE_BATCH, name ) ) {
            sql = "batch";
        }
        return sql;
    }
    protected String reportQuery ( String query, Object[] args, final String name, long start, long delta ) {
        String sql = ( query == null && args != null &&  args.length > 0 ) ? ( String ) args[0] : query;
        if ( sql == null && compare ( EXECUTE_BATCH, name ) ) {
            sql = "batch";
        }
        return sql;
    }
    protected String reportSlowQuery ( String query, Object[] args, final String name, long start, long delta ) {
        String sql = ( query == null && args != null &&  args.length > 0 ) ? ( String ) args[0] : query;
        if ( sql == null && compare ( EXECUTE_BATCH, name ) ) {
            sql = "batch";
        }
        return sql;
    }
    public long getThreshold() {
        return threshold;
    }
    public void setThreshold ( long threshold ) {
        this.threshold = threshold;
    }
    protected Constructor<?> getConstructor ( int idx, Class<?> clazz ) throws NoSuchMethodException {
        if ( constructors[idx] == null ) {
            Class<?> proxyClass = Proxy.getProxyClass ( SlowQueryReport.class.getClassLoader(), new Class[] {clazz} );
            constructors[idx] = proxyClass.getConstructor ( new Class[] { InvocationHandler.class } );
        }
        return constructors[idx];
    }
    @Override
    public Object createStatement ( Object proxy, Method method, Object[] args, Object statement, long time ) {
        try {
            Object result = null;
            String name = method.getName();
            String sql = null;
            Constructor<?> constructor = null;
            if ( compare ( CREATE_STATEMENT, name ) ) {
                constructor = getConstructor ( CREATE_STATEMENT_IDX, Statement.class );
            } else if ( compare ( PREPARE_STATEMENT, name ) ) {
                sql = ( String ) args[0];
                constructor = getConstructor ( PREPARE_STATEMENT_IDX, PreparedStatement.class );
                if ( sql != null ) {
                    prepareStatement ( sql, time );
                }
            } else if ( compare ( PREPARE_CALL, name ) ) {
                sql = ( String ) args[0];
                constructor = getConstructor ( PREPARE_CALL_IDX, CallableStatement.class );
                prepareCall ( sql, time );
            } else {
                return statement;
            }
            result = constructor.newInstance ( new Object[] { new StatementProxy ( statement, sql ) } );
            return result;
        } catch ( Exception x ) {
            log.warn ( "Unable to create statement proxy for slow query report.", x );
        }
        return statement;
    }
    protected class StatementProxy implements InvocationHandler {
        protected boolean closed = false;
        protected Object delegate;
        protected final String query;
        public StatementProxy ( Object parent, String query ) {
            this.delegate = parent;
            this.query = query;
        }
        @Override
        public Object invoke ( Object proxy, Method method, Object[] args ) throws Throwable {
            final String name = method.getName();
            boolean close = compare ( JdbcInterceptor.CLOSE_VAL, name );
            if ( close && closed ) {
                return null;
            }
            if ( compare ( JdbcInterceptor.ISCLOSED_VAL, name ) ) {
                return Boolean.valueOf ( closed );
            }
            if ( closed ) {
                throw new SQLException ( "Statement closed." );
            }
            boolean process = false;
            process = isExecute ( method, process );
            long start = ( process ) ? System.currentTimeMillis() : 0;
            Object result =  null;
            try {
                result =  method.invoke ( delegate, args );
            } catch ( Throwable t ) {
                reportFailedQuery ( query, args, name, start, t );
                if ( t instanceof InvocationTargetException
                        && t.getCause() != null ) {
                    throw t.getCause();
                } else {
                    throw t;
                }
            }
            long delta = ( process ) ? ( System.currentTimeMillis() - start ) : Long.MIN_VALUE;
            if ( delta > threshold ) {
                try {
                    reportSlowQuery ( query, args, name, start, delta );
                } catch ( Exception t ) {
                    if ( log.isWarnEnabled() ) {
                        log.warn ( "Unable to process slow query", t );
                    }
                }
            } else if ( process ) {
                reportQuery ( query, args, name, start, delta );
            }
            if ( close ) {
                closed = true;
                delegate = null;
            }
            return result;
        }
    }
}
