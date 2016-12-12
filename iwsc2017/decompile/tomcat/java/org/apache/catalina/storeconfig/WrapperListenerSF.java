package org.apache.catalina.storeconfig;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.core.StandardContext;
import java.io.PrintWriter;
import org.apache.juli.logging.Log;
public class WrapperListenerSF extends StoreFactoryBase {
    private static Log log;
    @Override
    public void store ( final PrintWriter aWriter, final int indent, final Object aElement ) throws Exception {
        if ( aElement instanceof StandardContext ) {
            final StoreDescription elementDesc = this.getRegistry().findDescription ( aElement.getClass().getName() + ".[WrapperListener]" );
            final String[] listeners = ( ( StandardContext ) aElement ).findWrapperListeners();
            if ( elementDesc != null ) {
                if ( WrapperListenerSF.log.isDebugEnabled() ) {
                    WrapperListenerSF.log.debug ( "store " + elementDesc.getTag() + "( " + aElement + " )" );
                }
                this.getStoreAppender().printTagArray ( aWriter, "WrapperListener", indent, listeners );
            }
        } else {
            WrapperListenerSF.log.warn ( "Descriptor for element" + aElement.getClass() + ".[WrapperListener] not configured!" );
        }
    }
    static {
        WrapperListenerSF.log = LogFactory.getLog ( WrapperListenerSF.class );
    }
}
