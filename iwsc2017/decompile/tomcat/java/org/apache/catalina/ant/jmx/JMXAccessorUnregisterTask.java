package org.apache.catalina.ant.jmx;
import javax.management.ObjectName;
import org.apache.tools.ant.BuildException;
import javax.management.MBeanServerConnection;
public class JMXAccessorUnregisterTask extends JMXAccessorTask {
    @Override
    public String jmxExecute ( final MBeanServerConnection jmxServerConnection ) throws Exception {
        if ( this.getName() == null ) {
            throw new BuildException ( "Must specify a 'name'" );
        }
        return this.jmxUuregister ( jmxServerConnection, this.getName() );
    }
    protected String jmxUuregister ( final MBeanServerConnection jmxServerConnection, final String name ) throws Exception {
        final String error = null;
        if ( this.isEcho() ) {
            this.handleOutput ( "Unregister MBean " + name );
        }
        jmxServerConnection.unregisterMBean ( new ObjectName ( name ) );
        return error;
    }
}
