package org.apache.catalina.authenticator.jaspic;
import java.util.Map;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.config.AuthConfigProvider;
import javax.security.auth.message.config.ClientAuthConfig;
import javax.security.auth.message.config.ServerAuthConfig;
public class SimpleAuthConfigProvider implements AuthConfigProvider {
    private final Map<String, String> properties;
    private volatile ServerAuthConfig serverAuthConfig;
    public SimpleAuthConfigProvider ( Map<String, String> properties, AuthConfigFactory factory ) {
        this.properties = properties;
        if ( factory != null ) {
            factory.registerConfigProvider ( this, null, null, "Automatic registration" );
        }
    }
    @Override
    public ClientAuthConfig getClientAuthConfig ( String layer, String appContext,
            CallbackHandler handler ) throws AuthException {
        return null;
    }
    @Override
    public ServerAuthConfig getServerAuthConfig ( String layer, String appContext,
            CallbackHandler handler ) throws AuthException {
        ServerAuthConfig serverAuthConfig = this.serverAuthConfig;
        if ( serverAuthConfig == null ) {
            synchronized ( this ) {
                if ( this.serverAuthConfig == null ) {
                    this.serverAuthConfig = createServerAuthConfig ( layer, appContext, handler, properties );
                }
                serverAuthConfig = this.serverAuthConfig;
            }
        }
        return serverAuthConfig;
    }
    protected ServerAuthConfig createServerAuthConfig ( String layer, String appContext,
            CallbackHandler handler, Map<String, String> properties ) {
        return new SimpleServerAuthConfig ( layer, appContext, handler, properties );
    }
    @Override
    public void refresh() {
        ServerAuthConfig serverAuthConfig = this.serverAuthConfig;
        if ( serverAuthConfig != null ) {
            serverAuthConfig.refresh();
        }
    }
}
