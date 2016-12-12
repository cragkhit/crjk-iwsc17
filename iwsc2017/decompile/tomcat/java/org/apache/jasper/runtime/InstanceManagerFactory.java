package org.apache.jasper.runtime;
import org.apache.tomcat.InstanceManager;
import javax.servlet.ServletConfig;
public class InstanceManagerFactory {
    public static InstanceManager getInstanceManager ( final ServletConfig config ) {
        final InstanceManager instanceManager = ( InstanceManager ) config.getServletContext().getAttribute ( InstanceManager.class.getName() );
        if ( instanceManager == null ) {
            throw new IllegalStateException ( "No org.apache.tomcat.InstanceManager set in ServletContext" );
        }
        return instanceManager;
    }
}
