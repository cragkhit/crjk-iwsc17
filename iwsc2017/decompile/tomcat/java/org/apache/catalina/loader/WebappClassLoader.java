package org.apache.catalina.loader;
import org.apache.catalina.LifecycleException;
public class WebappClassLoader extends WebappClassLoaderBase {
    public WebappClassLoader() {
    }
    public WebappClassLoader ( final ClassLoader parent ) {
        super ( parent );
    }
    @Override
    public WebappClassLoader copyWithoutTransformers() {
        final WebappClassLoader result = new WebappClassLoader ( this.getParent() );
        super.copyStateWithoutTransformers ( result );
        try {
            result.start();
        } catch ( LifecycleException e ) {
            throw new IllegalStateException ( e );
        }
        return result;
    }
    @Override
    protected Object getClassLoadingLock ( final String className ) {
        return this;
    }
}
