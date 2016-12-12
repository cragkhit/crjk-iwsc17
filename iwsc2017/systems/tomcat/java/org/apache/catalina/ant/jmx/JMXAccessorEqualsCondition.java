package org.apache.catalina.ant.jmx;
import org.apache.tools.ant.BuildException;
public class JMXAccessorEqualsCondition extends JMXAccessorConditionBase {
    @Override
    public boolean eval() {
        String value = getValue();
        if ( value == null ) {
            throw new BuildException ( "value attribute is not set" );
        }
        if ( getName() == null || getAttribute() == null ) {
            throw new BuildException (
                "Must specify an MBean name and attribute for equals condition" );
        }
        String jmxValue = accessJMXValue();
        if ( jmxValue != null ) {
            return jmxValue.equals ( value );
        }
        return false;
    }
}
