package org.apache.catalina.storeconfig;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import org.apache.catalina.Cluster;
import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Realm;
import org.apache.catalina.Valve;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.ha.ClusterValve;
public class StandardEngineSF extends StoreFactoryBase {
    @Override
    public void storeChildren ( PrintWriter aWriter, int indent, Object aEngine,
                                StoreDescription parentDesc ) throws Exception {
        if ( aEngine instanceof StandardEngine ) {
            StandardEngine engine = ( StandardEngine ) aEngine;
            LifecycleListener listeners[] = ( ( Lifecycle ) engine )
                                            .findLifecycleListeners();
            storeElementArray ( aWriter, indent, listeners );
            Realm realm = engine.getRealm();
            Realm parentRealm = null;
            if ( engine.getParent() != null ) {
                parentRealm = engine.getParent().getRealm();
            }
            if ( realm != parentRealm ) {
                storeElement ( aWriter, indent, realm );
            }
            Valve valves[] = engine.getPipeline().getValves();
            if ( valves != null && valves.length > 0 ) {
                List<Valve> engineValves = new ArrayList<>() ;
                for ( int i = 0 ; i < valves.length ; i++ ) {
                    if ( ! ( valves[i] instanceof ClusterValve ) ) {
                        engineValves.add ( valves[i] );
                    }
                }
                storeElementArray ( aWriter, indent, engineValves.toArray() );
            }
            Cluster cluster = engine.getCluster();
            if ( cluster != null ) {
                storeElement ( aWriter, indent, cluster );
            }
            Container children[] = engine.findChildren();
            storeElementArray ( aWriter, indent, children );
        }
    }
}
