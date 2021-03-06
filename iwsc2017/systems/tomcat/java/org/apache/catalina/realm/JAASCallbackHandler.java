package org.apache.catalina.realm;
import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.apache.tomcat.util.res.StringManager;
public class JAASCallbackHandler implements CallbackHandler {
    public JAASCallbackHandler ( JAASRealm realm, String username,
                                 String password ) {
        this ( realm, username, password, null, null, null, null, null, null,
               null );
    }
    public JAASCallbackHandler ( JAASRealm realm, String username,
                                 String password, String nonce, String nc,
                                 String cnonce, String qop, String realmName,
                                 String md5a2, String authMethod ) {
        this.realm = realm;
        this.username = username;
        if ( realm.hasMessageDigest() ) {
            this.password = realm.getCredentialHandler().mutate ( password );
        } else {
            this.password = password;
        }
        this.nonce = nonce;
        this.nc = nc;
        this.cnonce = cnonce;
        this.qop = qop;
        this.realmName = realmName;
        this.md5a2 = md5a2;
        this.authMethod = authMethod;
    }
    protected static final StringManager sm = StringManager.getManager ( JAASCallbackHandler.class );
    protected final String password;
    protected final JAASRealm realm;
    protected final String username;
    protected final String nonce;
    protected final String nc;
    protected final String cnonce;
    protected final String qop;
    protected final String realmName;
    protected final String md5a2;
    protected final String authMethod;
    @Override
    public void handle ( Callback callbacks[] )
    throws IOException, UnsupportedCallbackException {
        for ( int i = 0; i < callbacks.length; i++ ) {
            if ( callbacks[i] instanceof NameCallback ) {
                if ( realm.getContainer().getLogger().isTraceEnabled() ) {
                    realm.getContainer().getLogger().trace ( sm.getString ( "jaasCallback.username", username ) );
                }
                ( ( NameCallback ) callbacks[i] ).setName ( username );
            } else if ( callbacks[i] instanceof PasswordCallback ) {
                final char[] passwordcontents;
                if ( password != null ) {
                    passwordcontents = password.toCharArray();
                } else {
                    passwordcontents = new char[0];
                }
                ( ( PasswordCallback ) callbacks[i] ).setPassword
                ( passwordcontents );
            } else if ( callbacks[i] instanceof TextInputCallback ) {
                TextInputCallback cb = ( ( TextInputCallback ) callbacks[i] );
                if ( cb.getPrompt().equals ( "nonce" ) ) {
                    cb.setText ( nonce );
                } else if ( cb.getPrompt().equals ( "nc" ) ) {
                    cb.setText ( nc );
                } else if ( cb.getPrompt().equals ( "cnonce" ) ) {
                    cb.setText ( cnonce );
                } else if ( cb.getPrompt().equals ( "qop" ) ) {
                    cb.setText ( qop );
                } else if ( cb.getPrompt().equals ( "realmName" ) ) {
                    cb.setText ( realmName );
                } else if ( cb.getPrompt().equals ( "md5a2" ) ) {
                    cb.setText ( md5a2 );
                } else if ( cb.getPrompt().equals ( "authMethod" ) ) {
                    cb.setText ( authMethod );
                } else if ( cb.getPrompt().equals ( "catalinaBase" ) ) {
                    cb.setText ( realm.getContainer().getCatalinaBase().getAbsolutePath() );
                } else {
                    throw new UnsupportedCallbackException ( callbacks[i] );
                }
            } else {
                throw new UnsupportedCallbackException ( callbacks[i] );
            }
        }
    }
}
