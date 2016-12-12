package org.apache.catalina.realm;
import java.io.IOException;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.Callback;
import org.apache.tomcat.util.res.StringManager;
import javax.security.auth.callback.CallbackHandler;
public class JAASCallbackHandler implements CallbackHandler {
    protected static final StringManager sm;
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
    public JAASCallbackHandler ( final JAASRealm realm, final String username, final String password ) {
        this ( realm, username, password, null, null, null, null, null, null, null );
    }
    public JAASCallbackHandler ( final JAASRealm realm, final String username, final String password, final String nonce, final String nc, final String cnonce, final String qop, final String realmName, final String md5a2, final String authMethod ) {
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
    @Override
    public void handle ( final Callback[] callbacks ) throws IOException, UnsupportedCallbackException {
        for ( int i = 0; i < callbacks.length; ++i ) {
            if ( callbacks[i] instanceof NameCallback ) {
                if ( this.realm.getContainer().getLogger().isTraceEnabled() ) {
                    this.realm.getContainer().getLogger().trace ( JAASCallbackHandler.sm.getString ( "jaasCallback.username", this.username ) );
                }
                ( ( NameCallback ) callbacks[i] ).setName ( this.username );
            } else if ( callbacks[i] instanceof PasswordCallback ) {
                char[] passwordcontents;
                if ( this.password != null ) {
                    passwordcontents = this.password.toCharArray();
                } else {
                    passwordcontents = new char[0];
                }
                ( ( PasswordCallback ) callbacks[i] ).setPassword ( passwordcontents );
            } else {
                if ( ! ( callbacks[i] instanceof TextInputCallback ) ) {
                    throw new UnsupportedCallbackException ( callbacks[i] );
                }
                final TextInputCallback cb = ( TextInputCallback ) callbacks[i];
                if ( cb.getPrompt().equals ( "nonce" ) ) {
                    cb.setText ( this.nonce );
                } else if ( cb.getPrompt().equals ( "nc" ) ) {
                    cb.setText ( this.nc );
                } else if ( cb.getPrompt().equals ( "cnonce" ) ) {
                    cb.setText ( this.cnonce );
                } else if ( cb.getPrompt().equals ( "qop" ) ) {
                    cb.setText ( this.qop );
                } else if ( cb.getPrompt().equals ( "realmName" ) ) {
                    cb.setText ( this.realmName );
                } else if ( cb.getPrompt().equals ( "md5a2" ) ) {
                    cb.setText ( this.md5a2 );
                } else if ( cb.getPrompt().equals ( "authMethod" ) ) {
                    cb.setText ( this.authMethod );
                } else {
                    if ( !cb.getPrompt().equals ( "catalinaBase" ) ) {
                        throw new UnsupportedCallbackException ( callbacks[i] );
                    }
                    cb.setText ( this.realm.getContainer().getCatalinaBase().getAbsolutePath() );
                }
            }
        }
    }
    static {
        sm = StringManager.getManager ( JAASCallbackHandler.class );
    }
}
