package org.apache.catalina.mbeans;
import javax.management.RuntimeOperationsException;
import javax.management.MBeanException;
import org.apache.tomcat.util.modeler.ManagedBean;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.tomcat.util.modeler.BaseModelMBean;
public class RoleMBean extends BaseModelMBean {
    protected final Registry registry;
    protected final ManagedBean managed;
    public RoleMBean() throws MBeanException, RuntimeOperationsException {
        this.registry = MBeanUtils.createRegistry();
        this.managed = this.registry.findManagedBean ( "Role" );
    }
}
