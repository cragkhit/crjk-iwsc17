package org.apache.catalina.storeconfig;
import org.apache.catalina.WebResourceSet;
import org.apache.catalina.WebResourceRoot;
import java.io.PrintWriter;
public class WebResourceRootSF extends StoreFactoryBase {
    @Override
    public void storeChildren ( final PrintWriter aWriter, final int indent, final Object aResourceRoot, final StoreDescription parentDesc ) throws Exception {
        if ( aResourceRoot instanceof WebResourceRoot ) {
            final WebResourceRoot resourceRoot = ( WebResourceRoot ) aResourceRoot;
            final WebResourceSet[] preResourcesArray = resourceRoot.getPreResources();
            final StoreDescription preResourcesElementDesc = this.getRegistry().findDescription ( WebResourceSet.class.getName() + ".[PreResources]" );
            if ( preResourcesElementDesc != null ) {
                for ( final WebResourceSet preResources : preResourcesArray ) {
                    preResourcesElementDesc.getStoreFactory().store ( aWriter, indent, preResources );
                }
            }
            final WebResourceSet[] jarResourcesArray = resourceRoot.getJarResources();
            final StoreDescription jarResourcesElementDesc = this.getRegistry().findDescription ( WebResourceSet.class.getName() + ".[JarResources]" );
            if ( jarResourcesElementDesc != null ) {
                for ( final WebResourceSet jarResources : jarResourcesArray ) {
                    jarResourcesElementDesc.getStoreFactory().store ( aWriter, indent, jarResources );
                }
            }
            final WebResourceSet[] postResourcesArray = resourceRoot.getPostResources();
            final StoreDescription postResourcesElementDesc = this.getRegistry().findDescription ( WebResourceSet.class.getName() + ".[PostResources]" );
            if ( postResourcesElementDesc != null ) {
                for ( final WebResourceSet postResources : postResourcesArray ) {
                    postResourcesElementDesc.getStoreFactory().store ( aWriter, indent, postResources );
                }
            }
        }
    }
}
