// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.sql.Statement;

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
