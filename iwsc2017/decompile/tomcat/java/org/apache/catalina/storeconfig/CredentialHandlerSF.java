package org.apache.catalina.storeconfig;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.CredentialHandler;
import org.apache.catalina.realm.NestedCredentialHandler;
import java.io.PrintWriter;
import org.apache.juli.logging.Log;
public class CredentialHandlerSF extends StoreFactoryBase {
    private static Log log;
    @Override
    public void store ( final PrintWriter aWriter, final int indent, final Object aElement ) throws Exception {
        if ( aElement instanceof NestedCredentialHandler ) {
            final StoreDescription elementDesc = this.getRegistry().findDescription ( aElement.getClass() );
            if ( elementDesc != null ) {
                if ( CredentialHandlerSF.log.isDebugEnabled() ) {
                    CredentialHandlerSF.log.debug ( CredentialHandlerSF.sm.getString ( "factory.storeTag", elementDesc.getTag(), aElement ) );
                }
                this.getStoreAppender().printIndent ( aWriter, indent + 2 );
                this.getStoreAppender().printOpenTag ( aWriter, indent + 2, aElement, elementDesc );
                this.storeChildren ( aWriter, indent + 2, aElement, elementDesc );
                this.getStoreAppender().printIndent ( aWriter, indent + 2 );
                this.getStoreAppender().printCloseTag ( aWriter, elementDesc );
            } else if ( CredentialHandlerSF.log.isWarnEnabled() ) {
                CredentialHandlerSF.log.warn ( CredentialHandlerSF.sm.getString ( "factory.storeNoDescriptor", aElement.getClass() ) );
            }
        } else {
            super.store ( aWriter, indent, aElement );
        }
    }
    @Override
    public void storeChildren ( final PrintWriter aWriter, final int indent, final Object aCredentialHandler, final StoreDescription parentDesc ) throws Exception {
        if ( aCredentialHandler instanceof NestedCredentialHandler ) {
            final NestedCredentialHandler nestedCredentialHandler = ( NestedCredentialHandler ) aCredentialHandler;
            final CredentialHandler[] credentialHandlers = nestedCredentialHandler.getCredentialHandlers();
            this.storeElementArray ( aWriter, indent, credentialHandlers );
        }
    }
    static {
        CredentialHandlerSF.log = LogFactory.getLog ( CredentialHandlerSF.class );
    }
}
