package org.apache.catalina.authenticator.jaspic;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.config.ClientAuthConfig;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.config.ServerAuthConfig;
import java.util.Map;
import javax.security.auth.message.config.AuthConfigProvider;
public class SimpleAuthConfigProvider implements AuthConfigProvider {
    private final Map<String, String> properties;
    private volatile ServerAuthConfig serverAuthConfig;
    public SimpleAuthConfigProvider ( final Map<String, String> properties, final AuthConfigFactory factory ) {
        this.properties = properties;
        if ( factory != null ) {
            factory.registerConfigProvider ( ( AuthConfigProvider ) this, ( String ) null, ( String ) null, "Automatic registration" );
        }
    }
    public ClientAuthConfig getClientAuthConfig ( final String layer, final String appContext, final CallbackHandler handler ) throws AuthException {
        return null;
    }
    public ServerAuthConfig getServerAuthConfig ( final String layer, final String appContext, final CallbackHandler handler ) throws AuthException {
        ServerAuthConfig serverAuthConfig = this.serverAuthConfig;
        if ( serverAuthConfig == null ) {
            synchronized ( this ) {
                if ( this.serverAuthConfig == null ) {
                    this.serverAuthConfig = this.createServerAuthConfig ( layer, appContext, handler, this.properties );
                }
                serverAuthConfig = this.serverAuthConfig;
            }
        }
        return serverAuthConfig;
    }
    protected ServerAuthConfig createServerAuthConfig ( final String layer, final String appContext, final CallbackHandler handler, final Map<String, String> properties ) {
        return ( ServerAuthConfig ) new SimpleServerAuthConfig ( layer, appContext, handler, properties );
    }
    public void refresh() {
        final ServerAuthConfig serverAuthConfig = this.serverAuthConfig;
        if ( serverAuthConfig != null ) {
            serverAuthConfig.refresh();
        }
    }
}
