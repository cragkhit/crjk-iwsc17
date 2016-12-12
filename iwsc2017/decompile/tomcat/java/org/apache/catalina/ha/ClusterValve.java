package org.apache.catalina.ha;
import org.apache.catalina.Valve;
public interface ClusterValve extends Valve {
    CatalinaCluster getCluster();
    void setCluster ( CatalinaCluster p0 );
}
