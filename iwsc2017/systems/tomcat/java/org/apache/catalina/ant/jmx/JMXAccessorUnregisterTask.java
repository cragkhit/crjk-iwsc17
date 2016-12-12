package org.apache.catalina.ant.jmx;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import org.apache.tools.ant.BuildException;
public class JMXAccessorUnregisterTask extends JMXAccessorTask {
    @Override
    public String jmxExecute ( MBeanServerConnection jmxServerConnection )
    throws Exception {
        if ( getName() == null ) {
            throw new BuildException ( "Must specify a 'name'" );
        }
        return  jmxUuregister ( jmxServerConnection, getName() );
    }
    protected String jmxUuregister ( MBeanServerConnection jmxServerConnection, String name ) throws Exception {
        String error = null;
        if ( isEcho() ) {
            handleOutput ( "Unregister MBean " + name );
        }
        jmxServerConnection.unregisterMBean (
            new ObjectName ( name ) );
        return error;
    }
}
