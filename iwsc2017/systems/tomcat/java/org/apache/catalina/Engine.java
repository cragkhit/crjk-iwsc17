package org.apache.catalina;
public interface Engine extends Container {
    public String getDefaultHost();
    public void setDefaultHost ( String defaultHost );
    public String getJvmRoute();
    public void setJvmRoute ( String jvmRouteId );
    public Service getService();
    public void setService ( Service service );
}
