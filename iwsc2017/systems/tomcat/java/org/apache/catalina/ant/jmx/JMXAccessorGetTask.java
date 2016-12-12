package org.apache.catalina.ant.jmx;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import org.apache.tools.ant.BuildException;
public class JMXAccessorGetTask extends JMXAccessorTask {
    private String attribute;
    public String getAttribute() {
        return attribute;
    }
    public void setAttribute ( String attribute ) {
        this.attribute = attribute;
    }
    @Override
    public String jmxExecute ( MBeanServerConnection jmxServerConnection )
    throws Exception {
        if ( getName() == null ) {
            throw new BuildException ( "Must specify a 'name'" );
        }
        if ( ( attribute == null ) ) {
            throw new BuildException (
                "Must specify a 'attribute' for get" );
        }
        return  jmxGet ( jmxServerConnection, getName() );
    }
    protected String jmxGet ( MBeanServerConnection jmxServerConnection, String name ) throws Exception {
        String error = null;
        if ( isEcho() ) {
            handleOutput ( "MBean " + name + " get attribute " + attribute );
        }
        Object result = jmxServerConnection.getAttribute (
                            new ObjectName ( name ), attribute );
        if ( result != null ) {
            echoResult ( attribute, result );
            createProperty ( result );
        } else {
            error = "Attribute " + attribute + " is empty";
        }
        return error;
    }
}
