package org.apache.catalina.startup;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
public static class DefaultWebXmlListener implements LifecycleListener {
    @Override
    public void lifecycleEvent ( final LifecycleEvent event ) {
        if ( "before_start".equals ( event.getType() ) ) {
            Tomcat.initWebappDefaults ( ( Context ) event.getLifecycle() );
        }
    }
}
