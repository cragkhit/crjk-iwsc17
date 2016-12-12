package org.apache.tomcat.util.modeler.modules;
import java.util.List;
import javax.management.ObjectName;
import org.apache.tomcat.util.modeler.Registry;
public abstract class ModelerSource {
    protected Object source;
    public abstract List<ObjectName> loadDescriptors ( Registry registry,
            String type, Object source ) throws Exception;
}
