package org.apache.catalina.ha;
import org.apache.catalina.Valve;
public interface ClusterValve extends Valve {
    public CatalinaCluster getCluster();
    public void setCluster ( CatalinaCluster cluster );
}
