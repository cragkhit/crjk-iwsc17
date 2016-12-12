package org.apache.catalina;
import javax.management.MBeanRegistration;
import javax.management.ObjectName;
public interface JmxEnabled extends MBeanRegistration {
    String getDomain();
    void setDomain ( String domain );
    ObjectName getObjectName();
}
