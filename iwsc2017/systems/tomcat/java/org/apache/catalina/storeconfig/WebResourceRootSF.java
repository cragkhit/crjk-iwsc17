package org.apache.catalina.storeconfig;
import java.io.PrintWriter;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.WebResourceSet;
public class WebResourceRootSF extends StoreFactoryBase {
    @Override
    public void storeChildren ( PrintWriter aWriter, int indent, Object aResourceRoot,
                                StoreDescription parentDesc ) throws Exception {
        if ( aResourceRoot instanceof WebResourceRoot ) {
            WebResourceRoot resourceRoot = ( WebResourceRoot ) aResourceRoot;
            WebResourceSet[] preResourcesArray = resourceRoot.getPreResources();
            StoreDescription preResourcesElementDesc = getRegistry().findDescription (
                        WebResourceSet.class.getName()
                        + ".[PreResources]" );
            if ( preResourcesElementDesc != null ) {
                for ( WebResourceSet preResources : preResourcesArray ) {
                    preResourcesElementDesc.getStoreFactory().store ( aWriter, indent,
                            preResources );
                }
            }
            WebResourceSet[] jarResourcesArray = resourceRoot.getJarResources();
            StoreDescription jarResourcesElementDesc = getRegistry().findDescription (
                        WebResourceSet.class.getName()
                        + ".[JarResources]" );
            if ( jarResourcesElementDesc != null ) {
                for ( WebResourceSet jarResources : jarResourcesArray ) {
                    jarResourcesElementDesc.getStoreFactory().store ( aWriter, indent,
                            jarResources );
                }
            }
            WebResourceSet[] postResourcesArray = resourceRoot.getPostResources();
            StoreDescription postResourcesElementDesc = getRegistry().findDescription (
                        WebResourceSet.class.getName()
                        + ".[PostResources]" );
            if ( postResourcesElementDesc != null ) {
                for ( WebResourceSet postResources : postResourcesArray ) {
                    postResourcesElementDesc.getStoreFactory().store ( aWriter, indent,
                            postResources );
                }
            }
        }
    }
}
