package org.apache.catalina.storeconfig;
import java.util.List;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Valve;
import org.apache.catalina.ha.ClusterDeployer;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.ha.ClusterManager;
import org.apache.catalina.ha.ClusterListener;
import java.util.ArrayList;
import org.apache.catalina.ha.tcp.SimpleTcpCluster;
import org.apache.catalina.ha.CatalinaCluster;
import java.io.PrintWriter;
public class CatalinaClusterSF extends StoreFactoryBase {
    @Override
    public void storeChildren ( final PrintWriter aWriter, final int indent, final Object aCluster, final StoreDescription parentDesc ) throws Exception {
        if ( aCluster instanceof CatalinaCluster ) {
            final CatalinaCluster cluster = ( CatalinaCluster ) aCluster;
            if ( cluster instanceof SimpleTcpCluster ) {
                final SimpleTcpCluster tcpCluster = ( SimpleTcpCluster ) cluster;
                final ClusterManager manager = tcpCluster.getManagerTemplate();
                if ( manager != null ) {
                    this.storeElement ( aWriter, indent, manager );
                }
            }
            final Channel channel = cluster.getChannel();
            if ( channel != null ) {
                this.storeElement ( aWriter, indent, channel );
            }
            final ClusterDeployer deployer = cluster.getClusterDeployer();
            if ( deployer != null ) {
                this.storeElement ( aWriter, indent, deployer );
            }
            final Valve[] valves = cluster.getValves();
            this.storeElementArray ( aWriter, indent, valves );
            if ( aCluster instanceof SimpleTcpCluster ) {
                final LifecycleListener[] listeners = ( ( SimpleTcpCluster ) cluster ).findLifecycleListeners();
                this.storeElementArray ( aWriter, indent, listeners );
                final ClusterListener[] mlisteners = ( ( SimpleTcpCluster ) cluster ).findClusterListeners();
                final List<ClusterListener> clusterListeners = new ArrayList<ClusterListener>();
                for ( final ClusterListener clusterListener : mlisteners ) {
                    if ( clusterListener != deployer ) {
                        clusterListeners.add ( clusterListener );
                    }
                }
                this.storeElementArray ( aWriter, indent, clusterListeners.toArray() );
            }
        }
    }
}
