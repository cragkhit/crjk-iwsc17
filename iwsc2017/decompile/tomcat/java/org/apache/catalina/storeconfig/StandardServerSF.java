package org.apache.catalina.storeconfig;
import org.apache.catalina.Service;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.catalina.core.StandardServer;
import java.io.PrintWriter;
public class StandardServerSF extends StoreFactoryBase {
    @Override
    public void store ( final PrintWriter aWriter, final int indent, final Object aServer ) throws Exception {
        this.storeXMLHead ( aWriter );
        super.store ( aWriter, indent, aServer );
    }
    @Override
    public void storeChildren ( final PrintWriter aWriter, final int indent, final Object aObject, final StoreDescription parentDesc ) throws Exception {
        if ( aObject instanceof StandardServer ) {
            final StandardServer server = ( StandardServer ) aObject;
            final LifecycleListener[] listeners = server.findLifecycleListeners();
            this.storeElementArray ( aWriter, indent, listeners );
            final NamingResourcesImpl globalNamingResources = server.getGlobalNamingResources();
            final StoreDescription elementDesc = this.getRegistry().findDescription ( NamingResourcesImpl.class.getName() + ".[GlobalNamingResources]" );
            if ( elementDesc != null ) {
                elementDesc.getStoreFactory().store ( aWriter, indent, globalNamingResources );
            }
            final Service[] services = server.findServices();
            this.storeElementArray ( aWriter, indent, services );
        }
    }
}
