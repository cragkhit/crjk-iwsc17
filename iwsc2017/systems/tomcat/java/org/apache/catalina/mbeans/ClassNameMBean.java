package org.apache.catalina.mbeans;
import javax.management.MBeanException;
import javax.management.RuntimeOperationsException;
import org.apache.tomcat.util.modeler.BaseModelMBean;
public class ClassNameMBean extends BaseModelMBean {
    public ClassNameMBean()
    throws MBeanException, RuntimeOperationsException {
        super();
    }
    @Override
    public String getClassName() {
        return ( this.resource.getClass().getName() );
    }
}
