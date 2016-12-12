package org.apache.catalina.mbeans;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import org.apache.tomcat.util.descriptor.web.ContextEnvironment;
import org.apache.tomcat.util.descriptor.web.NamingResources;
import org.apache.tomcat.util.modeler.BaseModelMBean;
public class ContextEnvironmentMBean extends BaseModelMBean {
    public ContextEnvironmentMBean()
    throws MBeanException, RuntimeOperationsException {
        super();
    }
    @Override
    public void setAttribute ( Attribute attribute )
    throws AttributeNotFoundException, MBeanException,
        ReflectionException {
        super.setAttribute ( attribute );
        ContextEnvironment ce = null;
        try {
            ce = ( ContextEnvironment ) getManagedResource();
        } catch ( InstanceNotFoundException e ) {
            throw new MBeanException ( e );
        } catch ( InvalidTargetObjectTypeException e ) {
            throw new MBeanException ( e );
        }
        NamingResources nr = ce.getNamingResources();
        nr.removeEnvironment ( ce.getName() );
        nr.addEnvironment ( ce );
    }
}
