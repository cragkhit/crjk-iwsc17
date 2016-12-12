package org.apache.catalina;
public interface Engine extends Container {
    String getDefaultHost();
    void setDefaultHost ( String p0 );
    String getJvmRoute();
    void setJvmRoute ( String p0 );
    Service getService();
    void setService ( Service p0 );
}
