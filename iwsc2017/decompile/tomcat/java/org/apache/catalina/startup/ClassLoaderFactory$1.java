package org.apache.catalina.startup;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.PrivilegedAction;
static final class ClassLoaderFactory$1 implements PrivilegedAction<URLClassLoader> {
    final   ClassLoader val$parent;
    final   URL[] val$array;
    @Override
    public URLClassLoader run() {
        if ( this.val$parent == null ) {
            return new URLClassLoader ( this.val$array );
        }
        return new URLClassLoader ( this.val$array, this.val$parent );
    }
}
