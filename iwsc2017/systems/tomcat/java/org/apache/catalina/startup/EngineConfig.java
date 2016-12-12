package org.apache.catalina.startup;
import org.apache.catalina.Engine;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
public class EngineConfig
    implements LifecycleListener {
    private static final Log log = LogFactory.getLog ( EngineConfig.class );
    protected Engine engine = null;
    protected static final StringManager sm =
        StringManager.getManager ( Constants.Package );
    @Override
    public void lifecycleEvent ( LifecycleEvent event ) {
        try {
            engine = ( Engine ) event.getLifecycle();
        } catch ( ClassCastException e ) {
            log.error ( sm.getString ( "engineConfig.cce", event.getLifecycle() ), e );
            return;
        }
        if ( event.getType().equals ( Lifecycle.START_EVENT ) ) {
            start();
        } else if ( event.getType().equals ( Lifecycle.STOP_EVENT ) ) {
            stop();
        }
    }
    protected void start() {
        if ( engine.getLogger().isDebugEnabled() ) {
            engine.getLogger().debug ( sm.getString ( "engineConfig.start" ) );
        }
    }
    protected void stop() {
        if ( engine.getLogger().isDebugEnabled() ) {
            engine.getLogger().debug ( sm.getString ( "engineConfig.stop" ) );
        }
    }
}
