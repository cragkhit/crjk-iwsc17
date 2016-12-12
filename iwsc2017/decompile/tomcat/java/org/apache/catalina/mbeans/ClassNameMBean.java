package org.apache.catalina.mbeans;
import javax.management.RuntimeOperationsException;
import javax.management.MBeanException;
import org.apache.tomcat.util.modeler.BaseModelMBean;
public class ClassNameMBean extends BaseModelMBean {
    @Override
    public String getClassName() {
        return this.resource.getClass().getName();
    }
}
