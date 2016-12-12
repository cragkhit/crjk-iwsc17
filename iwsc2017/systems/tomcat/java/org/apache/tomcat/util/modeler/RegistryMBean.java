package org.apache.tomcat.util.modeler;
import java.util.List;
import javax.management.ObjectName;
public interface RegistryMBean {
    public void invoke ( List<ObjectName> mbeans, String operation, boolean failFirst )
    throws Exception;
    public void registerComponent ( Object bean, String oname, String type )
    throws Exception;
    public void unregisterComponent ( String oname );
    public int getId ( String domain, String name );
    public void stop();
}
