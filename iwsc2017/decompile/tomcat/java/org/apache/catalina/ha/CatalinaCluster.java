package org.apache.catalina.ha;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.Manager;
import java.util.Map;
import org.apache.catalina.Valve;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.Cluster;
public interface CatalinaCluster extends Cluster {
    void send ( ClusterMessage p0 );
    void send ( ClusterMessage p0, Member p1 );
    boolean hasMembers();
    Member[] getMembers();
    Member getLocalMember();
    void addValve ( Valve p0 );
    void addClusterListener ( ClusterListener p0 );
    void removeClusterListener ( ClusterListener p0 );
    void setClusterDeployer ( ClusterDeployer p0 );
    ClusterDeployer getClusterDeployer();
    Map<String, ClusterManager> getManagers();
    Manager getManager ( String p0 );
    String getManagerName ( String p0, Manager p1 );
    Valve[] getValves();
    void setChannel ( Channel p0 );
    Channel getChannel();
}
