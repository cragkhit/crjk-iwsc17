package org.apache.catalina.loader;
import org.apache.catalina.LifecycleException;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class ParallelWebappClassLoader extends WebappClassLoaderBase {
    private static final Log log = LogFactory.getLog ( ParallelWebappClassLoader.class );
    static {
        boolean result = ClassLoader.registerAsParallelCapable();
        if ( !result ) {
            log.warn ( sm.getString ( "webappClassLoaderParallel.registrationFailed" ) );
        }
    }
    public ParallelWebappClassLoader() {
        super();
    }
    public ParallelWebappClassLoader ( ClassLoader parent ) {
        super ( parent );
    }
    @Override
    public ParallelWebappClassLoader copyWithoutTransformers() {
        ParallelWebappClassLoader result = new ParallelWebappClassLoader ( getParent() );
        super.copyStateWithoutTransformers ( result );
        try {
            result.start();
        } catch ( LifecycleException e ) {
            throw new IllegalStateException ( e );
        }
        return result;
    }
}
