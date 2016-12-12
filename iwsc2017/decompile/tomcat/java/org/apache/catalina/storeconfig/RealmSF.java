package org.apache.catalina.storeconfig;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.CredentialHandler;
import org.apache.catalina.Realm;
import org.apache.catalina.realm.CombinedRealm;
import java.io.PrintWriter;
import org.apache.juli.logging.Log;
public class RealmSF extends StoreFactoryBase {
    private static Log log;
    @Override
    public void store ( final PrintWriter aWriter, final int indent, final Object aElement ) throws Exception {
        if ( aElement instanceof CombinedRealm ) {
            final StoreDescription elementDesc = this.getRegistry().findDescription ( aElement.getClass() );
            if ( elementDesc != null ) {
                if ( RealmSF.log.isDebugEnabled() ) {
                    RealmSF.log.debug ( RealmSF.sm.getString ( "factory.storeTag", elementDesc.getTag(), aElement ) );
                }
                this.getStoreAppender().printIndent ( aWriter, indent + 2 );
                this.getStoreAppender().printOpenTag ( aWriter, indent + 2, aElement, elementDesc );
                this.storeChildren ( aWriter, indent + 2, aElement, elementDesc );
                this.getStoreAppender().printIndent ( aWriter, indent + 2 );
                this.getStoreAppender().printCloseTag ( aWriter, elementDesc );
            } else if ( RealmSF.log.isWarnEnabled() ) {
                RealmSF.log.warn ( RealmSF.sm.getString ( "factory.storeNoDescriptor", aElement.getClass() ) );
            }
        } else {
            super.store ( aWriter, indent, aElement );
        }
    }
    @Override
    public void storeChildren ( final PrintWriter aWriter, final int indent, final Object aRealm, final StoreDescription parentDesc ) throws Exception {
        if ( aRealm instanceof CombinedRealm ) {
            final CombinedRealm combinedRealm = ( CombinedRealm ) aRealm;
            final Realm[] realms = combinedRealm.getNestedRealms();
            this.storeElementArray ( aWriter, indent, realms );
        }
        final CredentialHandler credentialHandler = ( ( Realm ) aRealm ).getCredentialHandler();
        if ( credentialHandler != null ) {
            this.storeElement ( aWriter, indent, credentialHandler );
        }
    }
    static {
        RealmSF.log = LogFactory.getLog ( RealmSF.class );
    }
}
