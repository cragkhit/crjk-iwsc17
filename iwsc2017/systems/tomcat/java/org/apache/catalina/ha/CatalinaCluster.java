package org.apache.catalina.ha;
import java.util.Map;
import org.apache.catalina.Cluster;
import org.apache.catalina.Manager;
import org.apache.catalina.Valve;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.Member;
public interface CatalinaCluster extends Cluster {
    public void send ( ClusterMessage msg );
    public void send ( ClusterMessage msg, Member dest );
    public boolean hasMembers();
    public Member[] getMembers();
    public Member getLocalMember();
    public void addValve ( Valve valve );
    public void addClusterListener ( ClusterListener listener );
    public void removeClusterListener ( ClusterListener listener );
    public void setClusterDeployer ( ClusterDeployer deployer );
    public ClusterDeployer getClusterDeployer();
    public Map<String, ClusterManager> getManagers();
    public Manager getManager ( String name );
    public String getManagerName ( String name, Manager manager );
    public Valve[] getValves();
    public void setChannel ( Channel channel );
    public Channel getChannel();
}
