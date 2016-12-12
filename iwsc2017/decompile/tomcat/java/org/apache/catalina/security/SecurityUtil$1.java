package org.apache.catalina.security;
import java.lang.reflect.Method;
import java.security.PrivilegedExceptionAction;
static final class SecurityUtil$1 implements PrivilegedExceptionAction<Void> {
    final   Method val$method;
    final   Object val$targetObject;
    final   Object[] val$targetArguments;
    @Override
    public Void run() throws Exception {
        this.val$method.invoke ( this.val$targetObject, this.val$targetArguments );
        return null;
    }
}
