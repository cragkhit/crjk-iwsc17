package org.apache.catalina;
public interface Cluster {
    String getClusterName();
    void setClusterName ( String p0 );
    void setContainer ( Container p0 );
    Container getContainer();
    Manager createManager ( String p0 );
    void registerManager ( Manager p0 );
    void removeManager ( Manager p0 );
    void backgroundProcess();
}
