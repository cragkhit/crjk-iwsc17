package org.apache.catalina.loader;
import java.security.PrivilegedAction;
protected static final class PrivilegedGetClassLoader implements PrivilegedAction<ClassLoader> {
    public final Class<?> clazz;
    public PrivilegedGetClassLoader ( final Class<?> clazz ) {
        this.clazz = clazz;
    }
    @Override
    public ClassLoader run() {
        return this.clazz.getClassLoader();
    }
}
