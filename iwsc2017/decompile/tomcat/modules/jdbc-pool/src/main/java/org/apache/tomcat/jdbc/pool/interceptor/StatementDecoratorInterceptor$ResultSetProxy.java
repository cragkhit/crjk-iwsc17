// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationHandler;

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
