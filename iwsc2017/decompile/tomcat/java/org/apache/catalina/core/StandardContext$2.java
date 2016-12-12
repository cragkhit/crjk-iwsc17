package org.apache.catalina.core;
import org.apache.catalina.CredentialHandler;
class StandardContext$2 implements CredentialHandler {
    @Override
    public boolean matches ( final String inputCredentials, final String storedCredentials ) {
        return StandardContext.this.getRealmInternal().getCredentialHandler().matches ( inputCredentials, storedCredentials );
    }
    @Override
    public String mutate ( final String inputCredentials ) {
        return StandardContext.this.getRealmInternal().getCredentialHandler().mutate ( inputCredentials );
    }
}
