package org.apache.catalina.storeconfig;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
public class StoreFactoryBase implements IStoreFactory {
    private static Log log = LogFactory.getLog ( StoreFactoryBase.class );
    private StoreRegistry registry;
    private StoreAppender storeAppender = new StoreAppender();
    protected static final StringManager sm = StringManager
            .getManager ( Constants.Package );
    private static final String info = "org.apache.catalina.config.StoreFactoryBase/1.0";
    public String getInfo() {
        return ( info );
    }
    @Override
    public StoreAppender getStoreAppender() {
        return storeAppender;
    }
    @Override
    public void setStoreAppender ( StoreAppender storeAppender ) {
        this.storeAppender = storeAppender;
    }
    @Override
    public void setRegistry ( StoreRegistry aRegistry ) {
        registry = aRegistry;
    }
    @Override
    public StoreRegistry getRegistry() {
        return registry;
    }
    @Override
    public void storeXMLHead ( PrintWriter aWriter ) {
        aWriter.print ( "<?xml version=\"1.0\" encoding=\"" );
        aWriter.print ( getRegistry().getEncoding() );
        aWriter.println ( "\"?>" );
    }
    @Override
    public void store ( PrintWriter aWriter, int indent, Object aElement )
    throws Exception {
        StoreDescription elementDesc = getRegistry().findDescription (
                                           aElement.getClass() );
        if ( elementDesc != null ) {
            if ( log.isDebugEnabled() )
                log.debug ( sm.getString ( "factory.storeTag",
                                           elementDesc.getTag(), aElement ) );
            getStoreAppender().printIndent ( aWriter, indent + 2 );
            if ( !elementDesc.isChildren() ) {
                getStoreAppender().printTag ( aWriter, indent, aElement,
                                              elementDesc );
            } else {
                getStoreAppender().printOpenTag ( aWriter, indent + 2, aElement,
                                                  elementDesc );
                storeChildren ( aWriter, indent + 2, aElement, elementDesc );
                getStoreAppender().printIndent ( aWriter, indent + 2 );
                getStoreAppender().printCloseTag ( aWriter, elementDesc );
            }
        } else
            log.warn ( sm.getString ( "factory.storeNoDescriptor", aElement
                                      .getClass() ) );
    }
    public void storeChildren ( PrintWriter aWriter, int indent, Object aElement,
                                StoreDescription elementDesc ) throws Exception {
    }
    protected void storeElement ( PrintWriter aWriter, int indent,
                                  Object aTagElement ) throws Exception {
        if ( aTagElement != null ) {
            IStoreFactory elementFactory = getRegistry().findStoreFactory (
                                               aTagElement.getClass() );
            if ( elementFactory != null ) {
                StoreDescription desc = getRegistry().findDescription (
                                            aTagElement.getClass() );
                if ( !desc.isTransientChild ( aTagElement.getClass().getName() ) ) {
                    elementFactory.store ( aWriter, indent, aTagElement );
                }
            } else {
                log.warn ( sm.getString ( "factory.storeNoDescriptor", aTagElement
                                          .getClass() ) );
            }
        }
    }
    protected void storeElementArray ( PrintWriter aWriter, int indent,
                                       Object[] elements ) throws Exception {
        if ( elements != null ) {
            for ( int i = 0; i < elements.length; i++ ) {
                try {
                    storeElement ( aWriter, indent, elements[i] );
                } catch ( IOException ioe ) {
                }
            }
        }
    }
}
