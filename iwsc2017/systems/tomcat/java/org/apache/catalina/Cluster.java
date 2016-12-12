package org.apache.catalina;
public interface Cluster {
    public String getClusterName();
    public void setClusterName ( String clusterName );
    public void setContainer ( Container container );
    public Container getContainer();
    public Manager createManager ( String name );
    public void registerManager ( Manager manager );
    public void removeManager ( Manager manager );
    public void backgroundProcess();
}
