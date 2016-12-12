package org.apache.catalina.storeconfig;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.Loader;
import java.io.PrintWriter;
import org.apache.juli.logging.Log;
public class LoaderSF extends StoreFactoryBase {
    private static Log log;
    @Override
    public void store ( final PrintWriter aWriter, final int indent, final Object aElement ) throws Exception {
        final StoreDescription elementDesc = this.getRegistry().findDescription ( aElement.getClass() );
        if ( elementDesc != null ) {
            final Loader loader = ( Loader ) aElement;
            if ( !this.isDefaultLoader ( loader ) ) {
                if ( LoaderSF.log.isDebugEnabled() ) {
                    LoaderSF.log.debug ( "store " + elementDesc.getTag() + "( " + aElement + " )" );
                }
                this.getStoreAppender().printIndent ( aWriter, indent + 2 );
                this.getStoreAppender().printTag ( aWriter, indent + 2, loader, elementDesc );
            }
        } else if ( LoaderSF.log.isWarnEnabled() ) {
            LoaderSF.log.warn ( "Descriptor for element" + aElement.getClass() + " not configured or element class not StandardManager!" );
        }
    }
    protected boolean isDefaultLoader ( final Loader loader ) {
        if ( ! ( loader instanceof WebappLoader ) ) {
            return false;
        }
        final WebappLoader wloader = ( WebappLoader ) loader;
        return !wloader.getDelegate() && wloader.getLoaderClass().equals ( "org.apache.catalina.loader.WebappClassLoader" );
    }
    static {
        LoaderSF.log = LogFactory.getLog ( LoaderSF.class );
    }
}
