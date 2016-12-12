package org.apache.catalina.ant.jmx;
import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import org.apache.tools.ant.BuildException;
public class JMXAccessorSetTask extends JMXAccessorTask {
    private String attribute;
    private String value;
    private String type;
    private boolean convert = false ;
    public String getAttribute() {
        return attribute;
    }
    public void setAttribute ( String attribute ) {
        this.attribute = attribute;
    }
    public String getValue() {
        return value;
    }
    public void setValue ( String value ) {
        this.value = value;
    }
    public String getType() {
        return type;
    }
    public void setType ( String valueType ) {
        this.type = valueType;
    }
    public boolean isConvert() {
        return convert;
    }
    public void setConvert ( boolean convert ) {
        this.convert = convert;
    }
    @Override
    public String jmxExecute ( MBeanServerConnection jmxServerConnection )
    throws Exception {
        if ( getName() == null ) {
            throw new BuildException ( "Must specify a 'name'" );
        }
        if ( ( attribute == null || value == null ) ) {
            throw new BuildException (
                "Must specify a 'attribute' and 'value' for set" );
        }
        return  jmxSet ( jmxServerConnection, getName() );
    }
    protected String jmxSet ( MBeanServerConnection jmxServerConnection,
                              String name ) throws Exception {
        Object realValue;
        if ( type != null ) {
            realValue = convertStringToType ( value, type );
        } else {
            if ( isConvert() ) {
                String mType = getMBeanAttributeType ( jmxServerConnection, name,
                                                       attribute );
                realValue = convertStringToType ( value, mType );
            } else {
                realValue = value;
            }
        }
        jmxServerConnection.setAttribute ( new ObjectName ( name ), new Attribute (
                                               attribute, realValue ) );
        return null;
    }
    protected String getMBeanAttributeType (
        MBeanServerConnection jmxServerConnection,
        String name,
        String attribute ) throws Exception {
        ObjectName oname = new ObjectName ( name );
        String mattrType = null;
        MBeanInfo minfo = jmxServerConnection.getMBeanInfo ( oname );
        MBeanAttributeInfo attrs[] = minfo.getAttributes();
        for ( int i = 0; mattrType == null && i < attrs.length; i++ ) {
            if ( attribute.equals ( attrs[i].getName() ) ) {
                mattrType = attrs[i].getType();
            }
        }
        return mattrType;
    }
}
