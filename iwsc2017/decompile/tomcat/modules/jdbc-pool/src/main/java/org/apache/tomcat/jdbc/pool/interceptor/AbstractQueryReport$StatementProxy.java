// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;

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
                if (AbstractQueryReport.access$000().isWarnEnabled()) {
                    AbstractQueryReport.access$000().warn((Object)"Unable to process slow query", (Throwable)t2);
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
