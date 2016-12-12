package org.apache.catalina.mbeans;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.IntrospectionUtils;
public class ConnectorMBean extends ClassNameMBean {
    public ConnectorMBean()
    throws MBeanException, RuntimeOperationsException {
        super();
    }
    @Override
    public Object getAttribute ( String name ) throws AttributeNotFoundException,
        MBeanException, ReflectionException {
        if ( name == null )
            throw new RuntimeOperationsException ( new IllegalArgumentException (
                    "Attribute name is null" ), "Attribute name is null" );
        Object result = null;
        try {
            Connector connector = ( Connector ) getManagedResource();
            result = IntrospectionUtils.getProperty ( connector, name );
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( InvalidTargetObjectTypeException e ) {
            throw new MBeanException ( e );
        }
        return result;
    }
    @Override
    public void setAttribute ( Attribute attribute )
    throws AttributeNotFoundException, MBeanException,
        ReflectionException {
        if ( attribute == null )
            throw new RuntimeOperationsException ( new IllegalArgumentException (
                    "Attribute is null" ), "Attribute is null" );
        String name = attribute.getName();
        Object value = attribute.getValue();
        if ( name == null )
            throw new RuntimeOperationsException ( new IllegalArgumentException (
                    "Attribute name is null" ), "Attribute name is null" );
        try {
            Connector connector = ( Connector ) getManagedResource();
            IntrospectionUtils.setProperty ( connector, name, String.valueOf ( value ) );
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( InvalidTargetObjectTypeException e ) {
            throw new MBeanException ( e );
        }
    }
}
