package org.apache.catalina.realm;
import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.Map.Entry;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextInputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.servlet.http.HttpServletRequest;
import org.apache.catalina.CredentialHandler;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.digester.Digester;
public class JAASMemoryLoginModule extends MemoryRealm implements LoginModule {
    private static final Log log = LogFactory.getLog ( JAASMemoryLoginModule.class );
    protected CallbackHandler callbackHandler = null;
    protected boolean committed = false;
    protected Map<String, ?> options = null;
    protected String pathname = "conf/tomcat-users.xml";
    protected Principal principal = null;
    protected Map<String, ?> sharedState = null;
    protected Subject subject = null;
    public JAASMemoryLoginModule() {
        if ( log.isDebugEnabled() ) {
            log.debug ( "MEMORY LOGIN MODULE" );
        }
    }
    @Override
    public boolean abort() throws LoginException {
        if ( principal == null ) {
            return false;
        }
        if ( committed ) {
            logout();
        } else {
            committed = false;
            principal = null;
        }
        if ( log.isDebugEnabled() ) {
            log.debug ( "Abort" );
        }
        return true;
    }
    @Override
    public boolean commit() throws LoginException {
        if ( log.isDebugEnabled() ) {
            log.debug ( "commit " + principal );
        }
        if ( principal == null ) {
            return false;
        }
        if ( !subject.getPrincipals().contains ( principal ) ) {
            subject.getPrincipals().add ( principal );
            if ( principal instanceof GenericPrincipal ) {
                String roles[] = ( ( GenericPrincipal ) principal ).getRoles();
                for ( int i = 0; i < roles.length; i++ ) {
                    subject.getPrincipals().add ( new GenericPrincipal ( roles[i], null, null ) );
                }
            }
        }
        committed = true;
        return true;
    }
    @Override
    public void initialize ( Subject subject, CallbackHandler callbackHandler,
                             Map<String, ?> sharedState, Map<String, ?> options ) {
        if ( log.isDebugEnabled() ) {
            log.debug ( "Init" );
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
                Class<?> clazz = Class.forName ( ( String ) option );
                credentialHandler = ( CredentialHandler ) clazz.newInstance();
            } catch ( InstantiationException | IllegalAccessException | ClassNotFoundException e ) {
                throw new IllegalArgumentException ( e );
            }
        }
        if ( credentialHandler == null ) {
            credentialHandler = new MessageDigestCredentialHandler();
        }
        for ( Entry<String, ?> entry : options.entrySet() ) {
            if ( "pathname".equals ( entry.getKey() ) ) {
                continue;
            }
            if ( "credentialHandlerClassName".equals ( entry.getKey() ) ) {
                continue;
            }
            if ( entry.getValue() instanceof String ) {
                IntrospectionUtils.setProperty ( credentialHandler, entry.getKey(),
                                                 ( String ) entry.getValue() );
            }
        }
        setCredentialHandler ( credentialHandler );
        load();
    }
    @Override
    public boolean login() throws LoginException {
        if ( callbackHandler == null ) {
            throw new LoginException ( "No CallbackHandler specified" );
        }
        Callback callbacks[] = new Callback[9];
        callbacks[0] = new NameCallback ( "Username: " );
        callbacks[1] = new PasswordCallback ( "Password: ", false );
        callbacks[2] = new TextInputCallback ( "nonce" );
        callbacks[3] = new TextInputCallback ( "nc" );
        callbacks[4] = new TextInputCallback ( "cnonce" );
        callbacks[5] = new TextInputCallback ( "qop" );
        callbacks[6] = new TextInputCallback ( "realmName" );
        callbacks[7] = new TextInputCallback ( "md5a2" );
        callbacks[8] = new TextInputCallback ( "authMethod" );
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
            callbackHandler.handle ( callbacks );
            username = ( ( NameCallback ) callbacks[0] ).getName();
            password =
                new String ( ( ( PasswordCallback ) callbacks[1] ).getPassword() );
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
            principal = super.authenticate ( username, password );
        } else if ( authMethod.equals ( HttpServletRequest.DIGEST_AUTH ) ) {
            principal = super.authenticate ( username, password, nonce, nc,
                                             cnonce, qop, realmName, md5a2 );
        } else if ( authMethod.equals ( HttpServletRequest.CLIENT_CERT_AUTH ) ) {
            principal = super.getPrincipal ( username );
        } else {
            throw new LoginException ( "Unknown authentication method" );
        }
        if ( log.isDebugEnabled() ) {
            log.debug ( "login " + username + " " + principal );
        }
        if ( principal != null ) {
            return true;
        } else {
            throw new FailedLoginException ( "Username or password is incorrect" );
        }
    }
    @Override
    public boolean logout() throws LoginException {
        subject.getPrincipals().remove ( principal );
        committed = false;
        principal = null;
        return true;
    }
    protected void load() {
        File file = new File ( pathname );
        if ( !file.isAbsolute() ) {
            String catalinaBase = getCatalinaBase();
            if ( catalinaBase == null ) {
                log.warn ( "Unable to determine Catalina base to load file " + pathname );
                return;
            } else {
                file = new File ( catalinaBase, pathname );
            }
        }
        if ( !file.canRead() ) {
            log.warn ( "Cannot load configuration file " + file.getAbsolutePath() );
            return;
        }
        Digester digester = new Digester();
        digester.setValidating ( false );
        digester.addRuleSet ( new MemoryRuleSet() );
        try {
            digester.push ( this );
            digester.parse ( file );
        } catch ( Exception e ) {
            log.warn ( "Error processing configuration file " + file.getAbsolutePath(), e );
            return;
        } finally {
            digester.reset();
        }
    }
    private String getCatalinaBase() {
        if ( callbackHandler == null ) {
            return null;
        }
        Callback callbacks[] = new Callback[1];
        callbacks[0] = new TextInputCallback ( "catalinaBase" );
        String result = null;
        try {
            callbackHandler.handle ( callbacks );
            result = ( ( TextInputCallback ) callbacks[0] ).getText();
        } catch ( IOException | UnsupportedCallbackException e ) {
            return null;
        }
        return result;
    }
}
