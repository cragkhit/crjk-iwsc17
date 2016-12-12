package org.apache.catalina.mbeans;
import javax.management.ReflectionException;
import javax.management.AttributeNotFoundException;
import org.apache.tomcat.util.descriptor.web.NamingResources;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.InstanceNotFoundException;
import org.apache.tomcat.util.descriptor.web.ContextEnvironment;
import javax.management.Attribute;
import javax.management.RuntimeOperationsException;
import javax.management.MBeanException;
import org.apache.tomcat.util.modeler.BaseModelMBean;
public class ContextEnvironmentMBean extends BaseModelMBean {
    @Override
    public void setAttribute ( final Attribute attribute ) throws AttributeNotFoundException, MBeanException, ReflectionException {
        super.setAttribute ( attribute );
        ContextEnvironment ce = null;
        try {
            ce = ( ContextEnvironment ) this.getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( InvalidTargetObjectTypeException e2 ) {
            throw new MBeanException ( e2 );
        }
        final NamingResources nr = ce.getNamingResources();
        nr.removeEnvironment ( ce.getName() );
        nr.addEnvironment ( ce );
    }
}
