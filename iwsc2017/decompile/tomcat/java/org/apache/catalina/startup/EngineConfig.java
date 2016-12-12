package org.apache.catalina.startup;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.LifecycleEvent;
import org.apache.tomcat.util.res.StringManager;
import org.apache.catalina.Engine;
import org.apache.juli.logging.Log;
import org.apache.catalina.LifecycleListener;
public class EngineConfig implements LifecycleListener {
    private static final Log log;
    protected Engine engine;
    protected static final StringManager sm;
    public EngineConfig() {
        this.engine = null;
    }
    @Override
    public void lifecycleEvent ( final LifecycleEvent event ) {
        try {
            this.engine = ( Engine ) event.getLifecycle();
        } catch ( ClassCastException e ) {
            EngineConfig.log.error ( EngineConfig.sm.getString ( "engineConfig.cce", event.getLifecycle() ), e );
            return;
        }
        if ( event.getType().equals ( "start" ) ) {
            this.start();
        } else if ( event.getType().equals ( "stop" ) ) {
            this.stop();
        }
    }
    protected void start() {
        if ( this.engine.getLogger().isDebugEnabled() ) {
            this.engine.getLogger().debug ( EngineConfig.sm.getString ( "engineConfig.start" ) );
        }
    }
    protected void stop() {
        if ( this.engine.getLogger().isDebugEnabled() ) {
            this.engine.getLogger().debug ( EngineConfig.sm.getString ( "engineConfig.stop" ) );
        }
    }
    static {
        log = LogFactory.getLog ( EngineConfig.class );
        sm = StringManager.getManager ( "org.apache.catalina.startup" );
    }
}
