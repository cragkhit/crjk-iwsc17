// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool.interceptor;

import java.sql.SQLException;
import org.apache.juli.logging.LogFactory;
import java.lang.reflect.InvocationTargetException;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.Constructor;
import org.apache.juli.logging.Log;

public class StatementDecoratorInterceptor extends AbstractCreateStatementInterceptor
{
    private static final Log logger;
    protected static final String EXECUTE_QUERY = "executeQuery";
    protected static final String GET_GENERATED_KEYS = "getGeneratedKeys";
    protected static final String GET_RESULTSET = "getResultSet";
    protected static final String[] RESULTSET_TYPES;
    protected static final Constructor<?>[] constructors;
    protected static Constructor<?> resultSetConstructor;
    
    @Override
    public void closeInvoked() {
    }
    
    protected Constructor<?> getConstructor(final int idx, final Class<?> clazz) throws NoSuchMethodException {
        if (StatementDecoratorInterceptor.constructors[idx] == null) {
            final Class<?> proxyClass = Proxy.getProxyClass(StatementDecoratorInterceptor.class.getClassLoader(), clazz);
            StatementDecoratorInterceptor.constructors[idx] = proxyClass.getConstructor(InvocationHandler.class);
        }
        return StatementDecoratorInterceptor.constructors[idx];
    }
    
    protected Constructor<?> getResultSetConstructor() throws NoSuchMethodException {
        if (StatementDecoratorInterceptor.resultSetConstructor == null) {
            final Class<?> proxyClass = Proxy.getProxyClass(StatementDecoratorInterceptor.class.getClassLoader(), ResultSet.class);
            StatementDecoratorInterceptor.resultSetConstructor = proxyClass.getConstructor(InvocationHandler.class);
        }
        return StatementDecoratorInterceptor.resultSetConstructor;
    }
    
    @Override
    public Object createStatement(final Object proxy, final Method method, final Object[] args, final Object statement, final long time) {
        try {
            final String name = method.getName();
            Constructor<?> constructor = null;
            String sql = null;
            if (this.compare("createStatement", name)) {
                constructor = this.getConstructor(0, Statement.class);
            }
            else if (this.compare("prepareStatement", name)) {
                constructor = this.getConstructor(1, PreparedStatement.class);
                sql = (String)args[0];
            }
            else {
                if (!this.compare("prepareCall", name)) {
                    return statement;
                }
                constructor = this.getConstructor(2, CallableStatement.class);
                sql = (String)args[0];
            }
            return this.createDecorator(proxy, method, args, statement, constructor, sql);
        }
        catch (Exception x) {
            if (x instanceof InvocationTargetException) {
                final Throwable cause = x.getCause();
                if (cause instanceof ThreadDeath) {
                    throw (ThreadDeath)cause;
                }
                if (cause instanceof VirtualMachineError) {
                    throw (VirtualMachineError)cause;
                }
            }
            StatementDecoratorInterceptor.logger.warn((Object)"Unable to create statement proxy for slow query report.", (Throwable)x);
            return statement;
        }
    }
    
    protected Object createDecorator(final Object proxy, final Method method, final Object[] args, final Object statement, final Constructor<?> constructor, final String sql) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        Object result = null;
        final StatementProxy<Statement> statementProxy = new StatementProxy<Statement>((Statement)statement, sql);
        result = constructor.newInstance(statementProxy);
        statementProxy.setActualProxy(result);
        statementProxy.setConnection(proxy);
        statementProxy.setConstructor(constructor);
        return result;
    }
    
    protected boolean isExecuteQuery(final String methodName) {
        return "executeQuery".equals(methodName);
    }
    
    protected boolean isExecuteQuery(final Method method) {
        return this.isExecuteQuery(method.getName());
    }
    
    protected boolean isResultSet(final Method method, final boolean process) {
        return this.process(StatementDecoratorInterceptor.RESULTSET_TYPES, method, process);
    }
    
    static {
        logger = LogFactory.getLog((Class)StatementDecoratorInterceptor.class);
        RESULTSET_TYPES = new String[] { "executeQuery", "getGeneratedKeys", "getResultSet" };
        constructors = new Constructor[AbstractCreateStatementInterceptor.STATEMENT_TYPE_COUNT];
        StatementDecoratorInterceptor.resultSetConstructor = null;
    }
    
    protected class StatementProxy<T extends Statement> implements InvocationHandler
    {
        protected boolean closed;
        protected T delegate;
        private Object actualProxy;
        private Object connection;
        private String sql;
        private Constructor<?> constructor;
        
        public StatementProxy(final T delegate, final String sql) {
            this.closed = false;
            this.delegate = delegate;
            this.sql = sql;
        }
        
        public T getDelegate() {
            return this.delegate;
        }
        
        public String getSql() {
            return this.sql;
        }
        
        public void setConnection(final Object proxy) {
            this.connection = proxy;
        }
        
        public Object getConnection() {
            return this.connection;
        }
        
        public void setActualProxy(final Object proxy) {
            this.actualProxy = proxy;
        }
        
        public Object getActualProxy() {
            return this.actualProxy;
        }
        
        public Constructor<?> getConstructor() {
            return this.constructor;
        }
        
        public void setConstructor(final Constructor<?> constructor) {
            this.constructor = constructor;
        }
        
        public void closeInvoked() {
            if (this.getDelegate() != null) {
                try {
                    this.getDelegate().close();
                }
                catch (SQLException ex) {}
            }
            this.closed = true;
            this.delegate = null;
        }
        
        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            if (StatementDecoratorInterceptor.this.compare("toString", method)) {
                return this.toString();
            }
            final boolean close = StatementDecoratorInterceptor.this.compare("close", method);
            if (close && this.closed) {
                return null;
            }
            if (StatementDecoratorInterceptor.this.compare("isClosed", method)) {
                return this.closed;
            }
            if (this.closed) {
                throw new SQLException("Statement closed.");
            }
            if (StatementDecoratorInterceptor.this.compare("getConnection", method)) {
                return this.connection;
            }
            boolean process = false;
            process = StatementDecoratorInterceptor.this.isResultSet(method, process);
            Object result = null;
            try {
                if (close) {
                    this.closeInvoked();
                }
                else {
                    result = method.invoke(this.delegate, args);
                }
            }
            catch (Throwable t) {
                if (t instanceof InvocationTargetException && t.getCause() != null) {
                    throw t.getCause();
                }
                throw t;
            }
            if (process && result != null) {
                final Constructor<?> cons = StatementDecoratorInterceptor.this.getResultSetConstructor();
                result = cons.newInstance(new ResultSetProxy(this.actualProxy, result));
            }
            return result;
        }
        
        @Override
        public String toString() {
            final StringBuffer buf = new StringBuffer(StatementProxy.class.getName());
            buf.append("[Proxy=");
            buf.append(System.identityHashCode(this));
            buf.append("; Sql=");
            buf.append(this.getSql());
            buf.append("; Delegate=");
            buf.append(this.getDelegate());
            buf.append("; Connection=");
            buf.append(this.getConnection());
            buf.append("]");
            return buf.toString();
        }
    }
    
    protected class ResultSetProxy implements InvocationHandler
    {
        private Object st;
        private Object delegate;
        
        public ResultSetProxy(final Object st, final Object delegate) {
            this.st = st;
            this.delegate = delegate;
        }
        
        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            if (method.getName().equals("getStatement")) {
                return this.st;
            }
            try {
                return method.invoke(this.delegate, args);
            }
            catch (Throwable t) {
                if (t instanceof InvocationTargetException && t.getCause() != null) {
                    throw t.getCause();
                }
                throw t;
            }
        }
    }
}
