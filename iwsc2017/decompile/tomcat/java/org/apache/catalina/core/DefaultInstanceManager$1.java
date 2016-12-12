package org.apache.catalina.core;
import java.security.PrivilegedExceptionAction;
class DefaultInstanceManager$1 implements PrivilegedExceptionAction<Class<?>> {
    final   String val$className;
    final   ClassLoader val$classLoader;
    @Override
    public Class<?> run() throws Exception {
        return DefaultInstanceManager.this.loadClass ( this.val$className, this.val$classLoader );
    }
}
