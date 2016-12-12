package org.apache.catalina.ant.jmx;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.Attribute;
import javax.management.ObjectName;
import org.apache.tools.ant.BuildException;
import javax.management.MBeanServerConnection;
public class JMXAccessorSetTask extends JMXAccessorTask {
    private String attribute;
    private String value;
    private String type;
    private boolean convert;
    public JMXAccessorSetTask() {
        this.convert = false;
    }
    public String getAttribute() {
        return this.attribute;
    }
    public void setAttribute ( final String attribute ) {
        this.attribute = attribute;
    }
    public String getValue() {
        return this.value;
    }
    public void setValue ( final String value ) {
        this.value = value;
    }
    public String getType() {
        return this.type;
    }
    public void setType ( final String valueType ) {
        this.type = valueType;
    }
    public boolean isConvert() {
        return this.convert;
    }
    public void setConvert ( final boolean convert ) {
        this.convert = convert;
    }
    @Override
    public String jmxExecute ( final MBeanServerConnection jmxServerConnection ) throws Exception {
        if ( this.getName() == null ) {
            throw new BuildException ( "Must specify a 'name'" );
        }
        if ( this.attribute == null || this.value == null ) {
            throw new BuildException ( "Must specify a 'attribute' and 'value' for set" );
        }
        return this.jmxSet ( jmxServerConnection, this.getName() );
    }
    protected String jmxSet ( final MBeanServerConnection jmxServerConnection, final String name ) throws Exception {
        Object realValue;
        if ( this.type != null ) {
            realValue = this.convertStringToType ( this.value, this.type );
        } else if ( this.isConvert() ) {
            final String mType = this.getMBeanAttributeType ( jmxServerConnection, name, this.attribute );
            realValue = this.convertStringToType ( this.value, mType );
        } else {
            realValue = this.value;
        }
        jmxServerConnection.setAttribute ( new ObjectName ( name ), new Attribute ( this.attribute, realValue ) );
        return null;
    }
    protected String getMBeanAttributeType ( final MBeanServerConnection jmxServerConnection, final String name, final String attribute ) throws Exception {
        final ObjectName oname = new ObjectName ( name );
        String mattrType = null;
        final MBeanInfo minfo = jmxServerConnection.getMBeanInfo ( oname );
        final MBeanAttributeInfo[] attrs = minfo.getAttributes();
        for ( int i = 0; mattrType == null && i < attrs.length; ++i ) {
            if ( attribute.equals ( attrs[i].getName() ) ) {
                mattrType = attrs[i].getType();
            }
        }
        return mattrType;
    }
}
