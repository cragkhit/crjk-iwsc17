package org.apache.catalina.loader;
import java.security.PrivilegedAction;
protected class PrivilegedFindClassByName implements PrivilegedAction<Class<?>> {
    protected final String name;
    PrivilegedFindClassByName ( final String name ) {
        this.name = name;
    }
    @Override
    public Class<?> run() {
        return WebappClassLoaderBase.this.findClassInternal ( this.name );
    }
}
