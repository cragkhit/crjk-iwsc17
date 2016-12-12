package org.apache.catalina.mbeans;
import javax.management.Attribute;
import javax.management.ReflectionException;
import javax.management.AttributeNotFoundException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.InstanceNotFoundException;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.catalina.connector.Connector;
import javax.management.RuntimeOperationsException;
import javax.management.MBeanException;
public class ConnectorMBean extends ClassNameMBean {
    @Override
    public Object getAttribute ( final String name ) throws AttributeNotFoundException, MBeanException, ReflectionException {
        if ( name == null ) {
            throw new RuntimeOperationsException ( new IllegalArgumentException ( "Attribute name is null" ), "Attribute name is null" );
        }
        Object result = null;
        try {
            final Connector connector = ( Connector ) this.getManagedResource();
            result = IntrospectionUtils.getProperty ( connector, name );
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( InvalidTargetObjectTypeException e2 ) {
            throw new MBeanException ( e2 );
        }
        return result;
    }
    @Override
    public void setAttribute ( final Attribute attribute ) throws AttributeNotFoundException, MBeanException, ReflectionException {
        if ( attribute == null ) {
            throw new RuntimeOperationsException ( new IllegalArgumentException ( "Attribute is null" ), "Attribute is null" );
        }
        final String name = attribute.getName();
        final Object value = attribute.getValue();
        if ( name == null ) {
            throw new RuntimeOperationsException ( new IllegalArgumentException ( "Attribute name is null" ), "Attribute name is null" );
        }
        try {
            final Connector connector = ( Connector ) this.getManagedResource();
            IntrospectionUtils.setProperty ( connector, name, String.valueOf ( value ) );
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( InvalidTargetObjectTypeException e2 ) {
            throw new MBeanException ( e2 );
        }
    }
}
