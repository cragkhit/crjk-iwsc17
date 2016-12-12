package org.apache.catalina.ant.jmx;
import org.apache.tools.ant.BuildException;
public class JMXAccessorEqualsCondition extends JMXAccessorConditionBase {
    public boolean eval() {
        final String value = this.getValue();
        if ( value == null ) {
            throw new BuildException ( "value attribute is not set" );
        }
        if ( this.getName() == null || this.getAttribute() == null ) {
            throw new BuildException ( "Must specify an MBean name and attribute for equals condition" );
        }
        final String jmxValue = this.accessJMXValue();
        return jmxValue != null && jmxValue.equals ( value );
    }
}
