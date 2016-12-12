// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool;

import java.sql.SQLException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TrapException extends JdbcInterceptor
{
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        try {
            return super.invoke(proxy, method, args);
        }
        catch (Exception t) {
            Throwable exception = t;
            if (t instanceof InvocationTargetException && t.getCause() != null) {
                exception = t.getCause();
                if (exception instanceof Error) {
                    throw exception;
                }
            }
            final Class<?> exceptionClass = exception.getClass();
            if (!this.isDeclaredException(method, exceptionClass)) {
                if (this.isDeclaredException(method, SQLException.class)) {
                    final SQLException sqlx = new SQLException("Uncaught underlying exception.");
                    sqlx.initCause(exception);
                    exception = sqlx;
                }
                else {
                    final RuntimeException rx = new RuntimeException("Uncaught underlying exception.");
                    rx.initCause(exception);
                    exception = rx;
                }
            }
            throw exception;
        }
    }
    
    public boolean isDeclaredException(final Method m, final Class<?> clazz) {
        for (final Class<?> cl : m.getExceptionTypes()) {
            if (cl.equals(clazz) || cl.isAssignableFrom(clazz)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void reset(final ConnectionPool parent, final PooledConnection con) {
    }
}
