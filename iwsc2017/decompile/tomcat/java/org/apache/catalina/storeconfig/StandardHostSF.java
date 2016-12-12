package org.apache.catalina.storeconfig;
import org.apache.catalina.Container;
import org.apache.catalina.Cluster;
import java.util.List;
import org.apache.catalina.Realm;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.ha.ClusterValve;
import org.apache.catalina.Valve;
import java.util.ArrayList;
import org.apache.catalina.core.StandardHost;
import java.io.PrintWriter;
public class StandardHostSF extends StoreFactoryBase {
    @Override
    public void storeChildren ( final PrintWriter aWriter, final int indent, final Object aHost, final StoreDescription parentDesc ) throws Exception {
        if ( aHost instanceof StandardHost ) {
            final StandardHost host = ( StandardHost ) aHost;
            final LifecycleListener[] listeners = host.findLifecycleListeners();
            this.storeElementArray ( aWriter, indent, listeners );
            final String[] aliases = host.findAliases();
            this.getStoreAppender().printTagArray ( aWriter, "Alias", indent + 2, aliases );
            final Realm realm = host.getRealm();
            if ( realm != null ) {
                Realm parentRealm = null;
                if ( host.getParent() != null ) {
                    parentRealm = host.getParent().getRealm();
                }
                if ( realm != parentRealm ) {
                    this.storeElement ( aWriter, indent, realm );
                }
            }
            final Valve[] valves = host.getPipeline().getValves();
            if ( valves != null && valves.length > 0 ) {
                final List<Valve> hostValves = new ArrayList<Valve>();
                for ( int i = 0; i < valves.length; ++i ) {
                    if ( ! ( valves[i] instanceof ClusterValve ) ) {
                        hostValves.add ( valves[i] );
                    }
                }
                this.storeElementArray ( aWriter, indent, hostValves.toArray() );
            }
            final Cluster cluster = host.getCluster();
            if ( cluster != null ) {
                Cluster parentCluster = null;
                if ( host.getParent() != null ) {
                    parentCluster = host.getParent().getCluster();
                }
                if ( cluster != parentCluster ) {
                    this.storeElement ( aWriter, indent, cluster );
                }
            }
            final Container[] children = host.findChildren();
            this.storeElementArray ( aWriter, indent, children );
        }
    }
}
