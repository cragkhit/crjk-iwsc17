package org.apache.catalina.storeconfig;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.deploy.NamingResourcesImpl;
import java.io.PrintWriter;
import org.apache.juli.logging.Log;
public class GlobalNamingResourcesSF extends StoreFactoryBase {
    private static Log log;
    @Override
    public void store ( final PrintWriter aWriter, final int indent, final Object aElement ) throws Exception {
        if ( aElement instanceof NamingResourcesImpl ) {
            final StoreDescription elementDesc = this.getRegistry().findDescription ( NamingResourcesImpl.class.getName() + ".[GlobalNamingResources]" );
            if ( elementDesc != null ) {
                this.getStoreAppender().printIndent ( aWriter, indent + 2 );
                this.getStoreAppender().printOpenTag ( aWriter, indent + 2, aElement, elementDesc );
                final NamingResourcesImpl resources = ( NamingResourcesImpl ) aElement;
                final StoreDescription resourcesdesc = this.getRegistry().findDescription ( NamingResourcesImpl.class.getName() );
                if ( resourcesdesc != null ) {
                    resourcesdesc.getStoreFactory().store ( aWriter, indent + 2, resources );
                } else if ( GlobalNamingResourcesSF.log.isWarnEnabled() ) {
                    GlobalNamingResourcesSF.log.warn ( "Can't find NamingResources Store Factory!" );
                }
                this.getStoreAppender().printIndent ( aWriter, indent + 2 );
                this.getStoreAppender().printCloseTag ( aWriter, elementDesc );
            } else if ( GlobalNamingResourcesSF.log.isWarnEnabled() ) {
                GlobalNamingResourcesSF.log.warn ( "Descriptor for element" + aElement.getClass() + " not configured!" );
            }
        } else if ( GlobalNamingResourcesSF.log.isWarnEnabled() ) {
            GlobalNamingResourcesSF.log.warn ( "wrong element " + aElement.getClass() );
        }
    }
    static {
        GlobalNamingResourcesSF.log = LogFactory.getLog ( GlobalNamingResourcesSF.class );
    }
}
