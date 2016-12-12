package org.apache.catalina.storeconfig;
import java.io.PrintWriter;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Service;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.deploy.NamingResourcesImpl;
public class StandardServerSF extends StoreFactoryBase {
    @Override
    public void store ( PrintWriter aWriter, int indent, Object aServer )
    throws Exception {
        storeXMLHead ( aWriter );
        super.store ( aWriter, indent, aServer );
    }
    @Override
    public void storeChildren ( PrintWriter aWriter, int indent, Object aObject,
                                StoreDescription parentDesc ) throws Exception {
        if ( aObject instanceof StandardServer ) {
            StandardServer server = ( StandardServer ) aObject;
            LifecycleListener listeners[] = ( ( Lifecycle ) server )
                                            .findLifecycleListeners();
            storeElementArray ( aWriter, indent, listeners );
            NamingResourcesImpl globalNamingResources = server
                    .getGlobalNamingResources();
            StoreDescription elementDesc = getRegistry().findDescription (
                                               NamingResourcesImpl.class.getName()
                                               + ".[GlobalNamingResources]" );
            if ( elementDesc != null ) {
                elementDesc.getStoreFactory().store ( aWriter, indent,
                                                      globalNamingResources );
            }
            Service services[] = server.findServices();
            storeElementArray ( aWriter, indent, services );
        }
    }
}
