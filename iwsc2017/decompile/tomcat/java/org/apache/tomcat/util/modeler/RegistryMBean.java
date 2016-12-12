package org.apache.tomcat.util.modeler;
import javax.management.ObjectName;
import java.util.List;
public interface RegistryMBean {
    void invoke ( List<ObjectName> p0, String p1, boolean p2 ) throws Exception;
    void registerComponent ( Object p0, String p1, String p2 ) throws Exception;
    void unregisterComponent ( String p0 );
    int getId ( String p0, String p1 );
    void stop();
}
