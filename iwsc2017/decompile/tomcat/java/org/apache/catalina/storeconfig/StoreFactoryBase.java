package org.apache.catalina.storeconfig;
import org.apache.juli.logging.LogFactory;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
public class StoreFactoryBase implements IStoreFactory {
    private static Log log;
    private StoreRegistry registry;
    private StoreAppender storeAppender;
    protected static final StringManager sm;
    private static final String info = "org.apache.catalina.config.StoreFactoryBase/1.0";
    public StoreFactoryBase() {
        this.storeAppender = new StoreAppender();
    }
    public String getInfo() {
        return "org.apache.catalina.config.StoreFactoryBase/1.0";
    }
    @Override
    public StoreAppender getStoreAppender() {
        return this.storeAppender;
    }
    @Override
    public void setStoreAppender ( final StoreAppender storeAppender ) {
        this.storeAppender = storeAppender;
    }
    @Override
    public void setRegistry ( final StoreRegistry aRegistry ) {
        this.registry = aRegistry;
    }
    @Override
    public StoreRegistry getRegistry() {
        return this.registry;
    }
    @Override
    public void storeXMLHead ( final PrintWriter aWriter ) {
        aWriter.print ( "<?xml version=\"1.0\" encoding=\"" );
        aWriter.print ( this.getRegistry().getEncoding() );
        aWriter.println ( "\"?>" );
    }
    @Override
    public void store ( final PrintWriter aWriter, final int indent, final Object aElement ) throws Exception {
        final StoreDescription elementDesc = this.getRegistry().findDescription ( aElement.getClass() );
        if ( elementDesc != null ) {
            if ( StoreFactoryBase.log.isDebugEnabled() ) {
                StoreFactoryBase.log.debug ( StoreFactoryBase.sm.getString ( "factory.storeTag", elementDesc.getTag(), aElement ) );
            }
            this.getStoreAppender().printIndent ( aWriter, indent + 2 );
            if ( !elementDesc.isChildren() ) {
                this.getStoreAppender().printTag ( aWriter, indent, aElement, elementDesc );
            } else {
                this.getStoreAppender().printOpenTag ( aWriter, indent + 2, aElement, elementDesc );
                this.storeChildren ( aWriter, indent + 2, aElement, elementDesc );
                this.getStoreAppender().printIndent ( aWriter, indent + 2 );
                this.getStoreAppender().printCloseTag ( aWriter, elementDesc );
            }
        } else {
            StoreFactoryBase.log.warn ( StoreFactoryBase.sm.getString ( "factory.storeNoDescriptor", aElement.getClass() ) );
        }
    }
    public void storeChildren ( final PrintWriter aWriter, final int indent, final Object aElement, final StoreDescription elementDesc ) throws Exception {
    }
    protected void storeElement ( final PrintWriter aWriter, final int indent, final Object aTagElement ) throws Exception {
        if ( aTagElement != null ) {
            final IStoreFactory elementFactory = this.getRegistry().findStoreFactory ( aTagElement.getClass() );
            if ( elementFactory != null ) {
                final StoreDescription desc = this.getRegistry().findDescription ( aTagElement.getClass() );
                if ( !desc.isTransientChild ( aTagElement.getClass().getName() ) ) {
                    elementFactory.store ( aWriter, indent, aTagElement );
                }
            } else {
                StoreFactoryBase.log.warn ( StoreFactoryBase.sm.getString ( "factory.storeNoDescriptor", aTagElement.getClass() ) );
            }
        }
    }
    protected void storeElementArray ( final PrintWriter aWriter, final int indent, final Object[] elements ) throws Exception {
        if ( elements != null ) {
            for ( int i = 0; i < elements.length; ++i ) {
                try {
                    this.storeElement ( aWriter, indent, elements[i] );
                } catch ( IOException ex ) {}
            }
        }
    }
    static {
        StoreFactoryBase.log = LogFactory.getLog ( StoreFactoryBase.class );
        sm = StringManager.getManager ( "org.apache.catalina.storeconfig" );
    }
}
