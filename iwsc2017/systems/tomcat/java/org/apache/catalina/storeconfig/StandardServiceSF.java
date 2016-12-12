package org.apache.catalina.storeconfig;
import java.io.PrintWriter;
import org.apache.catalina.Engine;
import org.apache.catalina.Executor;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardService;
public class StandardServiceSF extends StoreFactoryBase {
    @Override
    public void storeChildren ( PrintWriter aWriter, int indent, Object aService,
                                StoreDescription parentDesc ) throws Exception {
        if ( aService instanceof StandardService ) {
            StandardService service = ( StandardService ) aService;
            LifecycleListener listeners[] = ( ( Lifecycle ) service )
                                            .findLifecycleListeners();
            storeElementArray ( aWriter, indent, listeners );
            Executor[] executors = service.findExecutors();
            storeElementArray ( aWriter, indent, executors );
            Connector connectors[] = service.findConnectors();
            storeElementArray ( aWriter, indent, connectors );
            Engine container = service.getContainer();
            if ( container != null ) {
                StoreDescription elementDesc = getRegistry().findDescription ( container.getClass() );
                if ( elementDesc != null ) {
                    IStoreFactory factory = elementDesc.getStoreFactory();
                    factory.store ( aWriter, indent, container );
                }
            }
        }
    }
}
