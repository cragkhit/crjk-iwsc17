package org.apache.catalina.storeconfig;
import org.apache.catalina.Engine;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.Executor;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardService;
import java.io.PrintWriter;
public class StandardServiceSF extends StoreFactoryBase {
    @Override
    public void storeChildren ( final PrintWriter aWriter, final int indent, final Object aService, final StoreDescription parentDesc ) throws Exception {
        if ( aService instanceof StandardService ) {
            final StandardService service = ( StandardService ) aService;
            final LifecycleListener[] listeners = service.findLifecycleListeners();
            this.storeElementArray ( aWriter, indent, listeners );
            final Executor[] executors = service.findExecutors();
            this.storeElementArray ( aWriter, indent, executors );
            final Connector[] connectors = service.findConnectors();
            this.storeElementArray ( aWriter, indent, connectors );
            final Engine container = service.getContainer();
            if ( container != null ) {
                final StoreDescription elementDesc = this.getRegistry().findDescription ( container.getClass() );
                if ( elementDesc != null ) {
                    final IStoreFactory factory = elementDesc.getStoreFactory();
                    factory.store ( aWriter, indent, container );
                }
            }
        }
    }
}
