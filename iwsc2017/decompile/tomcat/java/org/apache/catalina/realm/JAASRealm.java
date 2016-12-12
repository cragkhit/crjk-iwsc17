package org.apache.catalina.realm;
import org.apache.juli.logging.LogFactory;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URI;
import org.apache.catalina.LifecycleException;
import java.util.Iterator;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.AccountExpiredException;
import org.apache.tomcat.util.ExceptionUtils;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.callback.CallbackHandler;
import java.security.Principal;
import org.apache.catalina.Container;
import java.util.ArrayList;
import javax.security.auth.login.Configuration;
import java.util.List;
import org.apache.juli.logging.Log;
public class JAASRealm extends RealmBase {
    private static final Log log;
    protected String appName;
    protected static final String name = "JAASRealm";
    protected final List<String> roleClasses;
    protected final List<String> userClasses;
    protected boolean useContextClassLoader;
    protected String configFile;
    protected Configuration jaasConfiguration;
    protected volatile boolean jaasConfigurationLoaded;
    protected String roleClassNames;
    protected String userClassNames;
    public JAASRealm() {
        this.appName = null;
        this.roleClasses = new ArrayList<String>();
        this.userClasses = new ArrayList<String>();
        this.useContextClassLoader = true;
        this.jaasConfigurationLoaded = false;
        this.roleClassNames = null;
        this.userClassNames = null;
    }
    public String getConfigFile() {
        return this.configFile;
    }
    public void setConfigFile ( final String configFile ) {
        this.configFile = configFile;
    }
    public void setAppName ( final String name ) {
        this.appName = name;
    }
    public String getAppName() {
        return this.appName;
    }
    public void setUseContextClassLoader ( final boolean useContext ) {
        this.useContextClassLoader = useContext;
        JAASRealm.log.info ( "Setting useContextClassLoader = " + useContext );
    }
    public boolean isUseContextClassLoader() {
        return this.useContextClassLoader;
    }
    @Override
    public void setContainer ( final Container container ) {
        super.setContainer ( container );
        if ( this.appName == null ) {
            String name = container.getName();
            if ( !name.startsWith ( "/" ) ) {
                name = "/" + name;
            }
            name = this.makeLegalForJAAS ( name );
            this.appName = name;
            JAASRealm.log.info ( "Set JAAS app name " + this.appName );
        }
    }
    public String getRoleClassNames() {
        return this.roleClassNames;
    }
    public void setRoleClassNames ( final String roleClassNames ) {
        this.roleClassNames = roleClassNames;
    }
    protected void parseClassNames ( final String classNamesString, final List<String> classNamesList ) {
        classNamesList.clear();
        if ( classNamesString == null ) {
            return;
        }
        ClassLoader loader = this.getClass().getClassLoader();
        if ( this.isUseContextClassLoader() ) {
            loader = Thread.currentThread().getContextClassLoader();
        }
        final String[] classNames = classNamesString.split ( "[ ]*,[ ]*" );
        for ( int i = 0; i < classNames.length; ++i ) {
            if ( classNames[i].length() != 0 ) {
                try {
                    final Class<?> principalClass = Class.forName ( classNames[i], false, loader );
                    if ( Principal.class.isAssignableFrom ( principalClass ) ) {
                        classNamesList.add ( classNames[i] );
                    } else {
                        JAASRealm.log.error ( "Class " + classNames[i] + " is not implementing java.security.Principal! Class not added." );
                    }
                } catch ( ClassNotFoundException e ) {
                    JAASRealm.log.error ( "Class " + classNames[i] + " not found! Class not added." );
                }
            }
        }
    }
    public String getUserClassNames() {
        return this.userClassNames;
    }
    public void setUserClassNames ( final String userClassNames ) {
        this.userClassNames = userClassNames;
    }
    @Override
    public Principal authenticate ( final String username, final String credentials ) {
        return this.authenticate ( username, new JAASCallbackHandler ( this, username, credentials ) );
    }
    @Override
    public Principal authenticate ( final String username, final String clientDigest, final String nonce, final String nc, final String cnonce, final String qop, final String realmName, final String md5a2 ) {
        return this.authenticate ( username, new JAASCallbackHandler ( this, username, clientDigest, nonce, nc, cnonce, qop, realmName, md5a2, "DIGEST" ) );
    }
    protected Principal authenticate ( final String username, final CallbackHandler callbackHandler ) {
        try {
            LoginContext loginContext = null;
            if ( this.appName == null ) {
                this.appName = "Tomcat";
            }
            if ( JAASRealm.log.isDebugEnabled() ) {
                JAASRealm.log.debug ( JAASRealm.sm.getString ( "jaasRealm.beginLogin", username, this.appName ) );
            }
            ClassLoader ocl = null;
            if ( !this.isUseContextClassLoader() ) {
                ocl = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader ( this.getClass().getClassLoader() );
            }
            try {
                final Configuration config = this.getConfig();
                loginContext = new LoginContext ( this.appName, null, callbackHandler, config );
            } catch ( Throwable e ) {
                ExceptionUtils.handleThrowable ( e );
                JAASRealm.log.error ( JAASRealm.sm.getString ( "jaasRealm.unexpectedError" ), e );
                return null;
            } finally {
                if ( !this.isUseContextClassLoader() ) {
                    Thread.currentThread().setContextClassLoader ( ocl );
                }
            }
            if ( JAASRealm.log.isDebugEnabled() ) {
                JAASRealm.log.debug ( "Login context created " + username );
            }
            Subject subject = null;
            try {
                loginContext.login();
                subject = loginContext.getSubject();
                if ( subject == null ) {
                    if ( JAASRealm.log.isDebugEnabled() ) {
                        JAASRealm.log.debug ( JAASRealm.sm.getString ( "jaasRealm.failedLogin", username ) );
                    }
                    return null;
                }
            } catch ( AccountExpiredException e4 ) {
                if ( JAASRealm.log.isDebugEnabled() ) {
                    JAASRealm.log.debug ( JAASRealm.sm.getString ( "jaasRealm.accountExpired", username ) );
                }
                return null;
            } catch ( CredentialExpiredException e5 ) {
                if ( JAASRealm.log.isDebugEnabled() ) {
                    JAASRealm.log.debug ( JAASRealm.sm.getString ( "jaasRealm.credentialExpired", username ) );
                }
                return null;
            } catch ( FailedLoginException e6 ) {
                if ( JAASRealm.log.isDebugEnabled() ) {
                    JAASRealm.log.debug ( JAASRealm.sm.getString ( "jaasRealm.failedLogin", username ) );
                }
                return null;
            } catch ( LoginException e2 ) {
                JAASRealm.log.warn ( JAASRealm.sm.getString ( "jaasRealm.loginException", username ), e2 );
                return null;
            } catch ( Throwable e3 ) {
                ExceptionUtils.handleThrowable ( e3 );
                JAASRealm.log.error ( JAASRealm.sm.getString ( "jaasRealm.unexpectedError" ), e3 );
                return null;
            }
            if ( JAASRealm.log.isDebugEnabled() ) {
                JAASRealm.log.debug ( JAASRealm.sm.getString ( "jaasRealm.loginContextCreated", username ) );
            }
            final Principal principal = this.createPrincipal ( username, subject, loginContext );
            if ( principal == null ) {
                JAASRealm.log.debug ( JAASRealm.sm.getString ( "jaasRealm.authenticateFailure", username ) );
                return null;
            }
            if ( JAASRealm.log.isDebugEnabled() ) {
                JAASRealm.log.debug ( JAASRealm.sm.getString ( "jaasRealm.authenticateSuccess", username ) );
            }
            return principal;
        } catch ( Throwable t ) {
            JAASRealm.log.error ( "error ", t );
            return null;
        }
    }
    @Override
    protected String getName() {
        return "JAASRealm";
    }
    @Override
    protected String getPassword ( final String username ) {
        return null;
    }
    @Override
    protected Principal getPrincipal ( final String username ) {
        return this.authenticate ( username, new JAASCallbackHandler ( this, username, null, null, null, null, null, null, null, "CLIENT_CERT" ) );
    }
    protected Principal createPrincipal ( final String username, final Subject subject, final LoginContext loginContext ) {
        final List<String> roles = new ArrayList<String>();
        Principal userPrincipal = null;
        for ( final Principal principal : subject.getPrincipals() ) {
            final String principalClass = principal.getClass().getName();
            if ( JAASRealm.log.isDebugEnabled() ) {
                JAASRealm.log.debug ( JAASRealm.sm.getString ( "jaasRealm.checkPrincipal", principal, principalClass ) );
            }
            if ( userPrincipal == null && this.userClasses.contains ( principalClass ) ) {
                userPrincipal = principal;
                if ( JAASRealm.log.isDebugEnabled() ) {
                    JAASRealm.log.debug ( JAASRealm.sm.getString ( "jaasRealm.userPrincipalSuccess", principal.getName() ) );
                }
            }
            if ( this.roleClasses.contains ( principalClass ) ) {
                roles.add ( principal.getName() );
                if ( !JAASRealm.log.isDebugEnabled() ) {
                    continue;
                }
                JAASRealm.log.debug ( JAASRealm.sm.getString ( "jaasRealm.rolePrincipalAdd", principal.getName() ) );
            }
        }
        if ( userPrincipal == null ) {
            if ( JAASRealm.log.isDebugEnabled() ) {
                JAASRealm.log.debug ( JAASRealm.sm.getString ( "jaasRealm.userPrincipalFailure" ) );
                JAASRealm.log.debug ( JAASRealm.sm.getString ( "jaasRealm.rolePrincipalFailure" ) );
            }
        } else if ( roles.size() == 0 && JAASRealm.log.isDebugEnabled() ) {
            JAASRealm.log.debug ( JAASRealm.sm.getString ( "jaasRealm.rolePrincipalFailure" ) );
        }
        return new GenericPrincipal ( username, null, roles, userPrincipal, loginContext );
    }
    protected String makeLegalForJAAS ( final String src ) {
        String result = src;
        if ( result == null ) {
            result = "other";
        }
        if ( result.startsWith ( "/" ) ) {
            result = result.substring ( 1 );
        }
        return result;
    }
    @Override
    protected void startInternal() throws LifecycleException {
        this.parseClassNames ( this.userClassNames, this.userClasses );
        this.parseClassNames ( this.roleClassNames, this.roleClasses );
        super.startInternal();
    }
    protected Configuration getConfig() {
        try {
            if ( this.jaasConfigurationLoaded ) {
                return this.jaasConfiguration;
            }
            synchronized ( this ) {
                if ( this.configFile == null ) {
                    this.jaasConfigurationLoaded = true;
                    return null;
                }
                final URL resource = Thread.currentThread().getContextClassLoader().getResource ( this.configFile );
                final URI uri = resource.toURI();
                final Class<Configuration> sunConfigFile = ( Class<Configuration> ) Class.forName ( "com.sun.security.auth.login.ConfigFile" );
                final Constructor<Configuration> constructor = sunConfigFile.getConstructor ( URI.class );
                final Configuration config = constructor.newInstance ( uri );
                this.jaasConfiguration = config;
                this.jaasConfigurationLoaded = true;
                return this.jaasConfiguration;
            }
        } catch ( URISyntaxException ex ) {
            throw new RuntimeException ( ex );
        } catch ( NoSuchMethodException ex2 ) {
            throw new RuntimeException ( ex2 );
        } catch ( SecurityException ex3 ) {
            throw new RuntimeException ( ex3 );
        } catch ( InstantiationException ex4 ) {
            throw new RuntimeException ( ex4 );
        } catch ( IllegalAccessException ex5 ) {
            throw new RuntimeException ( ex5 );
        } catch ( IllegalArgumentException ex6 ) {
            throw new RuntimeException ( ex6 );
        } catch ( InvocationTargetException ex7 ) {
            throw new RuntimeException ( ex7.getCause() );
        } catch ( ClassNotFoundException ex8 ) {
            throw new RuntimeException ( ex8 );
        }
    }
    static {
        log = LogFactory.getLog ( JAASRealm.class );
    }
}
