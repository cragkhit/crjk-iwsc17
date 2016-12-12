package org.apache.catalina.mbeans;
import javax.management.MBeanException;
import javax.management.RuntimeOperationsException;
import org.apache.tomcat.util.modeler.BaseModelMBean;
import org.apache.tomcat.util.modeler.ManagedBean;
import org.apache.tomcat.util.modeler.Registry;
public class RoleMBean extends BaseModelMBean {
    public RoleMBean()
    throws MBeanException, RuntimeOperationsException {
        super();
    }
    protected final Registry registry = MBeanUtils.createRegistry();
    protected final ManagedBean managed = registry.findManagedBean ( "Role" );
}
