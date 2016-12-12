package org.apache.catalina.storeconfig;
import org.apache.catalina.Container;
import org.apache.catalina.Cluster;
import java.util.List;
import org.apache.catalina.Realm;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.ha.ClusterValve;
import org.apache.catalina.Valve;
import java.util.ArrayList;
import org.apache.catalina.core.StandardEngine;
import java.io.PrintWriter;
public class StandardEngineSF extends StoreFactoryBase {
    @Override
    public void storeChildren ( final PrintWriter aWriter, final int indent, final Object aEngine, final StoreDescription parentDesc ) throws Exception {
        if ( aEngine instanceof StandardEngine ) {
            final StandardEngine engine = ( StandardEngine ) aEngine;
            final LifecycleListener[] listeners = engine.findLifecycleListeners();
            this.storeElementArray ( aWriter, indent, listeners );
            final Realm realm = engine.getRealm();
            Realm parentRealm = null;
            if ( engine.getParent() != null ) {
                parentRealm = engine.getParent().getRealm();
            }
            if ( realm != parentRealm ) {
                this.storeElement ( aWriter, indent, realm );
            }
            final Valve[] valves = engine.getPipeline().getValves();
            if ( valves != null && valves.length > 0 ) {
                final List<Valve> engineValves = new ArrayList<Valve>();
                for ( int i = 0; i < valves.length; ++i ) {
                    if ( ! ( valves[i] instanceof ClusterValve ) ) {
                        engineValves.add ( valves[i] );
                    }
                }
                this.storeElementArray ( aWriter, indent, engineValves.toArray() );
            }
            final Cluster cluster = engine.getCluster();
            if ( cluster != null ) {
                this.storeElement ( aWriter, indent, cluster );
            }
            final Container[] children = engine.findChildren();
            this.storeElementArray ( aWriter, indent, children );
        }
    }
}
