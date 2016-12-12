// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import org.apache.juli.logging.LogFactory;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.Constructor;
import org.apache.juli.logging.Log;

public abstract class AbstractQueryReport extends AbstractCreateStatementInterceptor
{
    private static final Log log;
    protected long threshold;
    protected static final Constructor<?>[] constructors;
    
    public AbstractQueryReport() {
        this.threshold = 1000L;
    }
    
    protected abstract void prepareStatement(final String p0, final long p1);
    
    protected abstract void prepareCall(final String p0, final long p1);
    
    protected String reportFailedQuery(final String query, final Object[] args, final String name, final long start, final Throwable t) {
        String sql = (String)((query == null && args != null && args.length > 0) ? args[0] : query);
        if (sql == null && this.compare("executeBatch", name)) {
            sql = "batch";
        }
        return sql;
    }
    
    protected String reportQuery(final String query, final Object[] args, final String name, final long start, final long delta) {
        String sql = (String)((query == null && args != null && args.length > 0) ? args[0] : query);
        if (sql == null && this.compare("executeBatch", name)) {
            sql = "batch";
        }
        return sql;
    }
    
    protected String reportSlowQuery(final String query, final Object[] args, final String name, final long start, final long delta) {
        String sql = (String)((query == null && args != null && args.length > 0) ? args[0] : query);
        if (sql == null && this.compare("executeBatch", name)) {
            sql = "batch";
        }
        return sql;
    }
    
    public long getThreshold() {
        return this.threshold;
    }
    
    public void setThreshold(final long threshold) {
        this.threshold = threshold;
    }
    
    protected Constructor<?> getConstructor(final int idx, final Class<?> clazz) throws NoSuchMethodException {
        if (AbstractQueryReport.constructors[idx] == null) {
            final Class<?> proxyClass = Proxy.getProxyClass(SlowQueryReport.class.getClassLoader(), clazz);
            AbstractQueryReport.constructors[idx] = proxyClass.getConstructor(InvocationHandler.class);
        }
        return AbstractQueryReport.constructors[idx];
    }
    
    @Override
    public Object createStatement(final Object proxy, final Method method, final Object[] args, final Object statement, final long time) {
        try {
            Object result = null;
            final String name = method.getName();
            String sql = null;
            Constructor<?> constructor = null;
            if (this.compare("createStatement", name)) {
                constructor = this.getConstructor(0, Statement.class);
            }
            else if (this.compare("prepareStatement", name)) {
                sql = (String)args[0];
                constructor = this.getConstructor(1, PreparedStatement.class);
                if (sql != null) {
                    this.prepareStatement(sql, time);
                }
            }
            else {
                if (!this.compare("prepareCall", name)) {
                    return statement;
                }
                sql = (String)args[0];
                constructor = this.getConstructor(2, CallableStatement.class);
                this.prepareCall(sql, time);
            }
            result = constructor.newInstance(new StatementProxy(statement, sql));
            return result;
        }
        catch (Exception x) {
            AbstractQueryReport.log.warn((Object)"Unable to create statement proxy for slow query report.", (Throwable)x);
            return statement;
        }
    }
    
    static {
        log = LogFactory.getLog((Class)AbstractQueryReport.class);
        constructors = new Constructor[AbstractCreateStatementInterceptor.STATEMENT_TYPE_COUNT];
    }
    
    protected class StatementProxy implements InvocationHandler
    {
        protected boolean closed;
        protected Object delegate;
        protected final String query;
        
        public StatementProxy(final Object parent, final String query) {
            this.closed = false;
            this.delegate = parent;
            this.query = query;
        }
        
        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            final String name = method.getName();
            final boolean close = AbstractQueryReport.this.compare("close", name);
            if (close && this.closed) {
                return null;
            }
            if (AbstractQueryReport.this.compare("isClosed", name)) {
                return this.closed;
            }
            if (this.closed) {
                throw new SQLException("Statement closed.");
            }
            boolean process = false;
            process = AbstractQueryReport.this.isExecute(method, process);
            final long start = process ? System.currentTimeMillis() : 0L;
            Object result = null;
            try {
                result = method.invoke(this.delegate, args);
            }
            catch (Throwable t) {
                AbstractQueryReport.this.reportFailedQuery(this.query, args, name, start, t);
                if (t instanceof InvocationTargetException && t.getCause() != null) {
                    throw t.getCause();
                }
                throw t;
            }
            final long delta = process ? (System.currentTimeMillis() - start) : Long.MIN_VALUE;
            if (delta > AbstractQueryReport.this.threshold) {
                try {
                    AbstractQueryReport.this.reportSlowQuery(this.query, args, name, start, delta);
                }
                catch (Exception t2) {
                    if (AbstractQueryReport.log.isWarnEnabled()) {
                        AbstractQueryReport.log.warn((Object)"Unable to process slow query", (Throwable)t2);
                    }
                }
            }
            else if (process) {
                AbstractQueryReport.this.reportQuery(this.query, args, name, start, delta);
            }
            if (close) {
                this.closed = true;
                this.delegate = null;
            }
            return result;
        }
    }
}
