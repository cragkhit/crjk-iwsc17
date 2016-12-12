package org.apache.catalina.authenticator.jaspic;
import java.util.List;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.module.ServerAuthModule;
import java.util.ArrayList;
import java.util.HashMap;
import javax.security.auth.Subject;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.config.ServerAuthContext;
import java.util.Map;
import javax.security.auth.callback.CallbackHandler;
import org.apache.tomcat.util.res.StringManager;
import javax.security.auth.message.config.ServerAuthConfig;
public class SimpleServerAuthConfig implements ServerAuthConfig {
    private static StringManager sm;
    private static final String SERVER_AUTH_MODULE_KEY_PREFIX = "org.apache.catalina.authenticator.jaspic.ServerAuthModule.";
    private final String layer;
    private final String appContext;
    private final CallbackHandler handler;
    private final Map<String, String> properties;
    private volatile ServerAuthContext serverAuthContext;
    public SimpleServerAuthConfig ( final String layer, final String appContext, final CallbackHandler handler, final Map<String, String> properties ) {
        this.layer = layer;
        this.appContext = appContext;
        this.handler = handler;
        this.properties = properties;
    }
    public String getMessageLayer() {
        return this.layer;
    }
    public String getAppContext() {
        return this.appContext;
    }
    public String getAuthContextID ( final MessageInfo messageInfo ) {
        return messageInfo.toString();
    }
    public void refresh() {
        this.serverAuthContext = null;
    }
    public boolean isProtected() {
        return false;
    }
    public ServerAuthContext getAuthContext ( final String authContextID, final Subject serviceSubject, final Map properties ) throws AuthException {
        ServerAuthContext serverAuthContext = this.serverAuthContext;
        if ( serverAuthContext == null ) {
            synchronized ( this ) {
                if ( this.serverAuthContext == null ) {
                    final Map<String, String> mergedProperties = new HashMap<String, String>();
                    if ( this.properties != null ) {
                        mergedProperties.putAll ( this.properties );
                    }
                    if ( properties != null ) {
                        mergedProperties.putAll ( properties );
                    }
                    final List<ServerAuthModule> modules = new ArrayList<ServerAuthModule>();
                    int moduleIndex = 1;
                    for ( String key = "org.apache.catalina.authenticator.jaspic.ServerAuthModule." + moduleIndex, moduleClassName = mergedProperties.get ( key ); moduleClassName != null; moduleClassName = mergedProperties.get ( key ) ) {
                        try {
                            final Class<?> clazz = Class.forName ( moduleClassName );
                            final ServerAuthModule module = ( ServerAuthModule ) clazz.newInstance();
                            module.initialize ( ( MessagePolicy ) null, ( MessagePolicy ) null, this.handler, ( Map ) mergedProperties );
                            modules.add ( module );
                        } catch ( ClassNotFoundException | InstantiationException | IllegalAccessException e ) {
                            final AuthException ae = new AuthException();
                            ae.initCause ( ( Throwable ) e );
                            throw ae;
                        }
                        ++moduleIndex;
                        key = "org.apache.catalina.authenticator.jaspic.ServerAuthModule." + moduleIndex;
                    }
                    if ( modules.size() == 0 ) {
                        throw new AuthException ( SimpleServerAuthConfig.sm.getString ( "simpleServerAuthConfig.noModules" ) );
                    }
                    this.serverAuthContext = this.createServerAuthContext ( modules );
                }
                serverAuthContext = this.serverAuthContext;
            }
        }
        return serverAuthContext;
    }
    protected ServerAuthContext createServerAuthContext ( final List<ServerAuthModule> modules ) {
        return ( ServerAuthContext ) new SimpleServerAuthContext ( modules );
    }
    static {
        SimpleServerAuthConfig.sm = StringManager.getManager ( SimpleServerAuthConfig.class );
    }
}
