package org.apache.catalina.loader;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.LifecycleException;
import org.apache.juli.logging.Log;
public class ParallelWebappClassLoader extends WebappClassLoaderBase {
    private static final Log log;
    public ParallelWebappClassLoader() {
    }
    public ParallelWebappClassLoader ( final ClassLoader parent ) {
        super ( parent );
    }
    @Override
    public ParallelWebappClassLoader copyWithoutTransformers() {
        final ParallelWebappClassLoader result = new ParallelWebappClassLoader ( this.getParent() );
        super.copyStateWithoutTransformers ( result );
        try {
            result.start();
        } catch ( LifecycleException e ) {
            throw new IllegalStateException ( e );
        }
        return result;
    }
    static {
        log = LogFactory.getLog ( ParallelWebappClassLoader.class );
        final boolean result = ClassLoader.registerAsParallelCapable();
        if ( !result ) {
            ParallelWebappClassLoader.log.warn ( ParallelWebappClassLoader.sm.getString ( "webappClassLoaderParallel.registrationFailed" ) );
        }
    }
}
