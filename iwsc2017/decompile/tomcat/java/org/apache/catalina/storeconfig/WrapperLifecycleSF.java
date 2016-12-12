package org.apache.catalina.storeconfig;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.core.StandardContext;
import java.io.PrintWriter;
import org.apache.juli.logging.Log;
public class WrapperLifecycleSF extends StoreFactoryBase {
    private static Log log;
    @Override
    public void store ( final PrintWriter aWriter, final int indent, final Object aElement ) throws Exception {
        if ( aElement instanceof StandardContext ) {
            final StoreDescription elementDesc = this.getRegistry().findDescription ( aElement.getClass().getName() + ".[WrapperLifecycle]" );
            final String[] listeners = ( ( StandardContext ) aElement ).findWrapperLifecycles();
            if ( elementDesc != null ) {
                if ( WrapperLifecycleSF.log.isDebugEnabled() ) {
                    WrapperLifecycleSF.log.debug ( "store " + elementDesc.getTag() + "( " + aElement + " )" );
                }
                this.getStoreAppender().printTagArray ( aWriter, "WrapperLifecycle", indent, listeners );
            }
        } else {
            WrapperLifecycleSF.log.warn ( "Descriptor for element" + aElement.getClass() + ".[WrapperLifecycle] not configured!" );
        }
    }
    static {
        WrapperLifecycleSF.log = LogFactory.getLog ( WrapperLifecycleSF.class );
    }
}
