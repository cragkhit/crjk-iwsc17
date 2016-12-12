package org.apache.catalina.realm;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.digester.RuleSet;
import org.apache.tomcat.util.digester.Digester;
import java.io.File;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.Callback;
import java.util.Iterator;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.catalina.CredentialHandler;
import java.util.List;
import javax.security.auth.login.LoginException;
import javax.security.auth.Subject;
import java.security.Principal;
import java.util.Map;
import javax.security.auth.callback.CallbackHandler;
import org.apache.juli.logging.Log;
import javax.security.auth.spi.LoginModule;
public class JAASMemoryLoginModule extends MemoryRealm implements LoginModule {
    private static final Log log;
    protected CallbackHandler callbackHandler;
    protected boolean committed;
    protected Map<String, ?> options;
    protected String pathname;
    protected Principal principal;
    protected Map<String, ?> sharedState;
    protected Subject subject;
    public JAASMemoryLoginModule() {
        this.callbackHandler = null;
        this.committed = false;
        this.options = null;
        this.pathname = "conf/tomcat-users.xml";
        this.principal = null;
        this.sharedState = null;
        this.subject = null;
        if ( JAASMemoryLoginModule.log.isDebugEnabled() ) {
            JAASMemoryLoginModule.log.debug ( "MEMORY LOGIN MODULE" );
        }
    }
    @Override
    public boolean abort() throws LoginException {
        if ( this.principal == null ) {
            return false;
        }
        if ( this.committed ) {
            this.logout();
        } else {
            this.committed = false;
            this.principal = null;
        }
        if ( JAASMemoryLoginModule.log.isDebugEnabled() ) {
            JAASMemoryLoginModule.log.debug ( "Abort" );
        }
        return true;
    }
    @Override
    public boolean commit() throws LoginException {
        if ( JAASMemoryLoginModule.log.isDebugEnabled() ) {
            JAASMemoryLoginModule.log.debug ( "commit " + this.principal );
        }
        if ( this.principal == null ) {
            return false;
        }
        if ( !this.subject.getPrincipals().contains ( this.principal ) ) {
            this.subject.getPrincipals().add ( this.principal );
            if ( this.principal instanceof GenericPrincipal ) {
                final String[] roles = ( ( GenericPrincipal ) this.principal ).getRoles();
                for ( int i = 0; i < roles.length; ++i ) {
                    this.subject.getPrincipals().add ( new GenericPrincipal ( roles[i], null, null ) );
                }
            }
        }
        return this.committed = true;
    }
    @Override
    public void initialize ( final Subject subject, final CallbackHandler callbackHandler, final Map<String, ?> sharedState, final Map<String, ?> options ) {
        if ( JAASMemoryLoginModule.log.isDebugEnabled() ) {
            JAASMemoryLoginModule.log.debug ( "Init" );
        }
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
        Object option = options.get ( "pathname" );
        if ( option instanceof String ) {
            this.pathname = ( String ) option;
        }
        CredentialHandler credentialHandler = null;
        option = options.get ( "credentialHandlerClassName" );
        if ( option instanceof String ) {
            try {
                final Class<?> clazz = Class.forName ( ( String ) option );
                credentialHandler = ( CredentialHandler ) clazz.newInstance();
            } catch ( InstantiationException | IllegalAccessException | ClassNotFoundException e ) {
                throw new IllegalArgumentException ( e );
            }
        }
        if ( credentialHandler == null ) {
            credentialHandler = new MessageDigestCredentialHandler();
        }
        for ( final Map.Entry<String, ?> entry : options.entrySet() ) {
            if ( "pathname".equals ( entry.getKey() ) ) {
                continue;
            }
            if ( "credentialHandlerClassName".equals ( entry.getKey() ) ) {
                continue;
            }
            if ( ! ( entry.getValue() instanceof String ) ) {
                continue;
            }
            IntrospectionUtils.setProperty ( credentialHandler, entry.getKey(), ( String ) entry.getValue() );
        }
        this.setCredentialHandler ( credentialHandler );
        this.load();
    }
    @Override
    public boolean login() throws LoginException {
        if ( this.callbackHandler == null ) {
            throw new LoginException ( "No CallbackHandler specified" );
        }
        final Callback[] callbacks = { new NameCallback ( "Username: " ), new PasswordCallback ( "Password: ", false ), new TextInputCallback ( "nonce" ), new TextInputCallback ( "nc" ), new TextInputCallback ( "cnonce" ), new TextInputCallback ( "qop" ), new TextInputCallback ( "realmName" ), new TextInputCallback ( "md5a2" ), new TextInputCallback ( "authMethod" ) };
        String username = null;
        String password = null;
        String nonce = null;
        String nc = null;
        String cnonce = null;
        String qop = null;
        String realmName = null;
        String md5a2 = null;
        String authMethod = null;
        try {
            this.callbackHandler.handle ( callbacks );
            username = ( ( NameCallback ) callbacks[0] ).getName();
            password = new String ( ( ( PasswordCallback ) callbacks[1] ).getPassword() );
            nonce = ( ( TextInputCallback ) callbacks[2] ).getText();
            nc = ( ( TextInputCallback ) callbacks[3] ).getText();
            cnonce = ( ( TextInputCallback ) callbacks[4] ).getText();
            qop = ( ( TextInputCallback ) callbacks[5] ).getText();
            realmName = ( ( TextInputCallback ) callbacks[6] ).getText();
            md5a2 = ( ( TextInputCallback ) callbacks[7] ).getText();
            authMethod = ( ( TextInputCallback ) callbacks[8] ).getText();
        } catch ( IOException | UnsupportedCallbackException e ) {
            throw new LoginException ( e.toString() );
        }
        if ( authMethod == null ) {
            this.principal = super.authenticate ( username, password );
        } else if ( authMethod.equals ( "DIGEST" ) ) {
            this.principal = super.authenticate ( username, password, nonce, nc, cnonce, qop, realmName, md5a2 );
        } else {
            if ( !authMethod.equals ( "CLIENT_CERT" ) ) {
                throw new LoginException ( "Unknown authentication method" );
            }
            this.principal = super.getPrincipal ( username );
        }
        if ( JAASMemoryLoginModule.log.isDebugEnabled() ) {
            JAASMemoryLoginModule.log.debug ( "login " + username + " " + this.principal );
        }
        if ( this.principal != null ) {
            return true;
        }
        throw new FailedLoginException ( "Username or password is incorrect" );
    }
    @Override
    public boolean logout() throws LoginException {
        this.subject.getPrincipals().remove ( this.principal );
        this.committed = false;
        this.principal = null;
        return true;
    }
    protected void load() {
        File file = new File ( this.pathname );
        if ( !file.isAbsolute() ) {
            final String catalinaBase = this.getCatalinaBase();
            if ( catalinaBase == null ) {
                JAASMemoryLoginModule.log.warn ( "Unable to determine Catalina base to load file " + this.pathname );
                return;
            }
            file = new File ( catalinaBase, this.pathname );
        }
        if ( !file.canRead() ) {
            JAASMemoryLoginModule.log.warn ( "Cannot load configuration file " + file.getAbsolutePath() );
            return;
        }
        final Digester digester = new Digester();
        digester.setValidating ( false );
        digester.addRuleSet ( new MemoryRuleSet() );
        try {
            digester.push ( this );
            digester.parse ( file );
        } catch ( Exception e ) {
            JAASMemoryLoginModule.log.warn ( "Error processing configuration file " + file.getAbsolutePath(), e );
        } finally {
            digester.reset();
        }
    }
    private String getCatalinaBase() {
        if ( this.callbackHandler == null ) {
            return null;
        }
        final Callback[] callbacks = { new TextInputCallback ( "catalinaBase" ) };
        String result = null;
        try {
            this.callbackHandler.handle ( callbacks );
            result = ( ( TextInputCallback ) callbacks[0] ).getText();
        } catch ( IOException | UnsupportedCallbackException e ) {
            return null;
        }
        return result;
    }
    static {
        log = LogFactory.getLog ( JAASMemoryLoginModule.class );
    }
}
