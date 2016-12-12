package org.apache.catalina.ant.jmx;
import javax.management.ObjectName;
import org.apache.tools.ant.BuildException;
import javax.management.MBeanServerConnection;
public class JMXAccessorGetTask extends JMXAccessorTask {
    private String attribute;
    public String getAttribute() {
        return this.attribute;
    }
    public void setAttribute ( final String attribute ) {
        this.attribute = attribute;
    }
    @Override
    public String jmxExecute ( final MBeanServerConnection jmxServerConnection ) throws Exception {
        if ( this.getName() == null ) {
            throw new BuildException ( "Must specify a 'name'" );
        }
        if ( this.attribute == null ) {
            throw new BuildException ( "Must specify a 'attribute' for get" );
        }
        return this.jmxGet ( jmxServerConnection, this.getName() );
    }
    protected String jmxGet ( final MBeanServerConnection jmxServerConnection, final String name ) throws Exception {
        String error = null;
        if ( this.isEcho() ) {
            this.handleOutput ( "MBean " + name + " get attribute " + this.attribute );
        }
        final Object result = jmxServerConnection.getAttribute ( new ObjectName ( name ), this.attribute );
        if ( result != null ) {
            this.echoResult ( this.attribute, result );
            this.createProperty ( result );
        } else {
            error = "Attribute " + this.attribute + " is empty";
        }
        return error;
    }
}
