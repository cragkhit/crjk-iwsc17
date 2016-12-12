package org.apache.catalina.core;
import java.lang.reflect.Method;
import java.security.PrivilegedExceptionAction;
private static class PrivilegedExecuteMethod implements PrivilegedExceptionAction<Object> {
    private final Method method;
    private final ApplicationContext context;
    private final Object[] params;
    public PrivilegedExecuteMethod ( final Method method, final ApplicationContext context, final Object[] params ) {
        this.method = method;
        this.context = context;
        this.params = params;
    }
    @Override
    public Object run() throws Exception {
        return this.method.invoke ( this.context, this.params );
    }
}
