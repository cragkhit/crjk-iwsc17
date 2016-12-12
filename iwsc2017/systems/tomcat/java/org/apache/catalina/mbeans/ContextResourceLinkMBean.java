package org.apache.catalina.mbeans;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import org.apache.tomcat.util.descriptor.web.ContextResourceLink;
import org.apache.tomcat.util.descriptor.web.NamingResources;
import org.apache.tomcat.util.modeler.BaseModelMBean;
public class ContextResourceLinkMBean extends BaseModelMBean {
    public ContextResourceLinkMBean()
    throws MBeanException, RuntimeOperationsException {
        super();
    }
    @Override
    public Object getAttribute ( String name )
    throws AttributeNotFoundException, MBeanException,
        ReflectionException {
        if ( name == null )
            throw new RuntimeOperationsException
            ( new IllegalArgumentException ( "Attribute name is null" ),
              "Attribute name is null" );
        ContextResourceLink cl = null;
        try {
            cl = ( ContextResourceLink ) getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( InvalidTargetObjectTypeException e ) {
            throw new MBeanException ( e );
        }
        String value = null;
        if ( "global".equals ( name ) ) {
            return ( cl.getGlobal() );
        } else if ( "description".equals ( name ) ) {
            return ( cl.getDescription() );
        } else if ( "name".equals ( name ) ) {
            return ( cl.getName() );
        } else if ( "type".equals ( name ) ) {
            return ( cl.getType() );
        } else {
            value = ( String ) cl.getProperty ( name );
            if ( value == null ) {
                throw new AttributeNotFoundException
                ( "Cannot find attribute " + name );
            }
        }
        return value;
    }
    @Override
    public void setAttribute ( Attribute attribute )
    throws AttributeNotFoundException, MBeanException,
        ReflectionException {
        if ( attribute == null )
            throw new RuntimeOperationsException
            ( new IllegalArgumentException ( "Attribute is null" ),
              "Attribute is null" );
        String name = attribute.getName();
        Object value = attribute.getValue();
        if ( name == null )
            throw new RuntimeOperationsException
            ( new IllegalArgumentException ( "Attribute name is null" ),
              "Attribute name is null" );
        ContextResourceLink crl = null;
        try {
            crl = ( ContextResourceLink ) getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( InvalidTargetObjectTypeException e ) {
            throw new MBeanException ( e );
        }
        if ( "global".equals ( name ) ) {
            crl.setGlobal ( ( String ) value );
        } else if ( "description".equals ( name ) ) {
            crl.setDescription ( ( String ) value );
        } else if ( "name".equals ( name ) ) {
            crl.setName ( ( String ) value );
        } else if ( "type".equals ( name ) ) {
            crl.setType ( ( String ) value );
        } else {
            crl.setProperty ( name, "" + value );
        }
        NamingResources nr = crl.getNamingResources();
        nr.removeResourceLink ( crl.getName() );
        nr.addResourceLink ( crl );
    }
}
