package org.apache.catalina.storeconfig;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Valve;
import org.apache.catalina.ha.CatalinaCluster;
import org.apache.catalina.ha.ClusterDeployer;
import org.apache.catalina.ha.ClusterListener;
import org.apache.catalina.ha.ClusterManager;
import org.apache.catalina.ha.tcp.SimpleTcpCluster;
import org.apache.catalina.tribes.Channel;
public class CatalinaClusterSF extends StoreFactoryBase {
    @Override
    public void storeChildren ( PrintWriter aWriter, int indent, Object aCluster,
                                StoreDescription parentDesc ) throws Exception {
        if ( aCluster instanceof CatalinaCluster ) {
            CatalinaCluster cluster = ( CatalinaCluster ) aCluster;
            if ( cluster instanceof SimpleTcpCluster ) {
                SimpleTcpCluster tcpCluster = ( SimpleTcpCluster ) cluster;
                ClusterManager manager = tcpCluster.getManagerTemplate();
                if ( manager != null ) {
                    storeElement ( aWriter, indent, manager );
                }
            }
            Channel channel = cluster.getChannel();
            if ( channel != null ) {
                storeElement ( aWriter, indent, channel );
            }
            ClusterDeployer deployer = cluster.getClusterDeployer();
            if ( deployer != null ) {
                storeElement ( aWriter, indent, deployer );
            }
            Valve valves[] = cluster.getValves();
            storeElementArray ( aWriter, indent, valves );
            if ( aCluster instanceof SimpleTcpCluster ) {
                LifecycleListener listeners[] = ( ( SimpleTcpCluster ) cluster ).findLifecycleListeners();
                storeElementArray ( aWriter, indent, listeners );
                ClusterListener mlisteners[] = ( ( SimpleTcpCluster ) cluster ).findClusterListeners();
                List<ClusterListener> clusterListeners = new ArrayList<>();
                for ( ClusterListener clusterListener : mlisteners ) {
                    if ( clusterListener != deployer ) {
                        clusterListeners.add ( clusterListener );
                    }
                }
                storeElementArray ( aWriter, indent, clusterListeners.toArray() );
            }
        }
    }
}
