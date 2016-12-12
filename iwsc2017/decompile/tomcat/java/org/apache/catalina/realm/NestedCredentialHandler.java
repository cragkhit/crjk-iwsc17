package org.apache.catalina.realm;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import org.apache.catalina.CredentialHandler;
public class NestedCredentialHandler implements CredentialHandler {
    private final List<CredentialHandler> credentialHandlers;
    public NestedCredentialHandler() {
        this.credentialHandlers = new ArrayList<CredentialHandler>();
    }
    @Override
    public boolean matches ( final String inputCredentials, final String storedCredentials ) {
        for ( final CredentialHandler handler : this.credentialHandlers ) {
            if ( handler.matches ( inputCredentials, storedCredentials ) ) {
                return true;
            }
        }
        return false;
    }
    @Override
    public String mutate ( final String inputCredentials ) {
        if ( this.credentialHandlers.isEmpty() ) {
            return null;
        }
        return this.credentialHandlers.get ( 0 ).mutate ( inputCredentials );
    }
    public void addCredentialHandler ( final CredentialHandler handler ) {
        this.credentialHandlers.add ( handler );
    }
    public CredentialHandler[] getCredentialHandlers() {
        return this.credentialHandlers.toArray ( new CredentialHandler[0] );
    }
}
