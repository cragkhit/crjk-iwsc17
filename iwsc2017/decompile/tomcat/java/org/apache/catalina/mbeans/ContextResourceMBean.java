package org.apache.catalina.mbeans;
import org.apache.tomcat.util.descriptor.web.NamingResources;
import javax.management.Attribute;
import javax.management.ReflectionException;
import javax.management.AttributeNotFoundException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.InstanceNotFoundException;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import javax.management.RuntimeOperationsException;
import javax.management.MBeanException;
import org.apache.tomcat.util.modeler.BaseModelMBean;
public class ContextResourceMBean extends BaseModelMBean {
    @Override
    public Object getAttribute ( final String name ) throws AttributeNotFoundException, MBeanException, ReflectionException {
        if ( name == null ) {
            throw new RuntimeOperationsException ( new IllegalArgumentException ( "Attribute name is null" ), "Attribute name is null" );
        }
        ContextResource cr = null;
        try {
            cr = ( ContextResource ) this.getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( InvalidTargetObjectTypeException e2 ) {
            throw new MBeanException ( e2 );
        }
        String value = null;
        if ( "auth".equals ( name ) ) {
            return cr.getAuth();
        }
        if ( "description".equals ( name ) ) {
            return cr.getDescription();
        }
        if ( "name".equals ( name ) ) {
            return cr.getName();
        }
        if ( "scope".equals ( name ) ) {
            return cr.getScope();
        }
        if ( "type".equals ( name ) ) {
            return cr.getType();
        }
        value = ( String ) cr.getProperty ( name );
        if ( value == null ) {
            throw new AttributeNotFoundException ( "Cannot find attribute " + name );
        }
        return value;
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
        ContextResource cr = null;
        try {
            cr = ( ContextResource ) this.getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( InvalidTargetObjectTypeException e2 ) {
            throw new MBeanException ( e2 );
        }
        if ( "auth".equals ( name ) ) {
            cr.setAuth ( ( String ) value );
        } else if ( "description".equals ( name ) ) {
            cr.setDescription ( ( String ) value );
        } else if ( "name".equals ( name ) ) {
            cr.setName ( ( String ) value );
        } else if ( "scope".equals ( name ) ) {
            cr.setScope ( ( String ) value );
        } else if ( "type".equals ( name ) ) {
            cr.setType ( ( String ) value );
        } else {
            cr.setProperty ( name, "" + value );
        }
        final NamingResources nr = cr.getNamingResources();
        nr.removeResource ( cr.getName() );
        nr.addResource ( cr );
    }
}
