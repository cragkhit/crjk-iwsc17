package org.apache.catalina.storeconfig;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.core.StandardContext;
import java.io.PrintWriter;
import org.apache.juli.logging.Log;
public class WatchedResourceSF extends StoreFactoryBase {
    private static Log log;
    @Override
    public void store ( final PrintWriter aWriter, final int indent, final Object aElement ) throws Exception {
        if ( aElement instanceof StandardContext ) {
            final StoreDescription elementDesc = this.getRegistry().findDescription ( aElement.getClass().getName() + ".[WatchedResource]" );
            final String[] resources = ( ( StandardContext ) aElement ).findWatchedResources();
            if ( elementDesc != null ) {
                if ( WatchedResourceSF.log.isDebugEnabled() ) {
                    WatchedResourceSF.log.debug ( "store " + elementDesc.getTag() + "( " + aElement + " )" );
                }
                this.getStoreAppender().printTagArray ( aWriter, "WatchedResource", indent, resources );
            }
        } else {
            WatchedResourceSF.log.warn ( "Descriptor for element" + aElement.getClass() + ".[WatchedResource] not configured!" );
        }
    }
    static {
        WatchedResourceSF.log = LogFactory.getLog ( WatchedResourceSF.class );
    }
}
