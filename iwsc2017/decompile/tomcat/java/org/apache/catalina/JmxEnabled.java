package org.apache.catalina;
import javax.management.ObjectName;
import javax.management.MBeanRegistration;
public interface JmxEnabled extends MBeanRegistration {
    String getDomain();
    void setDomain ( String p0 );
    ObjectName getObjectName();
}
