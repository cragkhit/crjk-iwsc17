// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool;

import java.sql.SQLException;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;

public class DisposableConnectionFacade extends JdbcInterceptor
{
    protected DisposableConnectionFacade(final JdbcInterceptor interceptor) {
        this.setUseEquals(interceptor.isUseEquals());
        this.setNext(interceptor);
    }
    
    @Override
    public void reset(final ConnectionPool parent, final PooledConnection con) {
    }
    
    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
    
    @Override
    public boolean equals(final Object obj) {
        return this == obj;
    }
    
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (this.compare("equals", method)) {
            return this.equals(Proxy.getInvocationHandler(args[0]));
        }
        if (this.compare("hashCode", method)) {
            return this.hashCode();
        }
        if (this.getNext() == null) {
            if (this.compare("isClosed", method)) {
                return Boolean.TRUE;
            }
            if (this.compare("close", method)) {
                return null;
            }
            if (this.compare("isValid", method)) {
                return Boolean.FALSE;
            }
        }
        try {
            return super.invoke(proxy, method, args);
        }
        catch (NullPointerException e) {
            if (this.getNext() != null) {
                throw e;
            }
            if (this.compare("toString", method)) {
                return "DisposableConnectionFacade[null]";
            }
            throw new SQLException("PooledConnection has already been closed.");
        }
        finally {
            if (this.compare("close", method)) {
                this.setNext(null);
            }
        }
    }
}
