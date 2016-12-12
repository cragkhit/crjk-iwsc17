package org.apache.catalina.startup;
import org.apache.catalina.Valve;
import org.apache.catalina.authenticator.NonLoginAuthenticator;
import org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
public static class FixContextListener implements LifecycleListener {
    @Override
    public void lifecycleEvent ( final LifecycleEvent event ) {
        try {
            final Context context = ( Context ) event.getLifecycle();
            if ( event.getType().equals ( "configure_start" ) ) {
                context.setConfigured ( true );
            }
            if ( context.getLoginConfig() == null ) {
                context.setLoginConfig ( new LoginConfig ( "NONE", null, null, null ) );
                context.getPipeline().addValve ( new NonLoginAuthenticator() );
            }
        } catch ( ClassCastException e ) {}
    }
}
