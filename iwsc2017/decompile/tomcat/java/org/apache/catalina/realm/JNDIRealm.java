package org.apache.catalina.realm;
import java.util.Collections;
import java.net.URISyntaxException;
import java.net.URI;
import javax.naming.CompositeName;
import org.apache.catalina.LifecycleException;
import javax.net.ssl.SSLSession;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.ExtendedRequest;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.SecureRandom;
import javax.net.ssl.TrustManager;
import javax.net.ssl.KeyManager;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;
import javax.security.auth.login.LoginContext;
import org.ietf.jgss.GSSCredential;
import java.io.IOException;
import javax.naming.directory.Attribute;
import java.util.Iterator;
import java.util.Set;
import javax.naming.Name;
import javax.naming.NameParser;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import javax.naming.AuthenticationException;
import javax.naming.NamingEnumeration;
import javax.naming.directory.SearchResult;
import javax.naming.PartialResultException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.Attributes;
import javax.naming.NameNotFoundException;
import java.util.ArrayList;
import java.util.List;
import javax.naming.InvalidNameException;
import javax.naming.NamingException;
import javax.naming.ServiceUnavailableException;
import javax.naming.CommunicationException;
import java.security.Principal;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import java.util.Arrays;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.naming.ldap.StartTlsResponse;
import java.text.MessageFormat;
import javax.naming.directory.DirContext;
public class JNDIRealm extends RealmBase {
    protected String authentication;
    protected String connectionName;
    protected String connectionPassword;
    protected String connectionURL;
    protected DirContext context;
    protected String contextFactory;
    protected String derefAliases;
    public static final String DEREF_ALIASES = "java.naming.ldap.derefAliases";
    protected static final String name = "JNDIRealm";
    protected String protocol;
    protected boolean adCompat;
    protected String referrals;
    protected String userBase;
    protected String userSearch;
    private boolean userSearchAsUser;
    protected MessageFormat userSearchFormat;
    protected boolean userSubtree;
    protected String userPassword;
    protected String userRoleAttribute;
    protected String[] userPatternArray;
    protected String userPattern;
    protected MessageFormat[] userPatternFormatArray;
    protected String roleBase;
    protected MessageFormat roleBaseFormat;
    protected MessageFormat roleFormat;
    protected String userRoleName;
    protected String roleName;
    protected String roleSearch;
    protected boolean roleSubtree;
    protected boolean roleNested;
    protected boolean roleSearchAsUser;
    protected String alternateURL;
    protected int connectionAttempt;
    protected String commonRole;
    protected String connectionTimeout;
    protected long sizeLimit;
    protected int timeLimit;
    protected boolean useDelegatedCredential;
    protected String spnegoDelegationQop;
    private boolean useStartTls;
    private StartTlsResponse tls;
    private String[] cipherSuitesArray;
    private HostnameVerifier hostnameVerifier;
    private SSLSocketFactory sslSocketFactory;
    private String sslSocketFactoryClassName;
    private String cipherSuites;
    private String hostNameVerifierClassName;
    private String sslProtocol;
    public JNDIRealm() {
        this.authentication = null;
        this.connectionName = null;
        this.connectionPassword = null;
        this.connectionURL = null;
        this.context = null;
        this.contextFactory = "com.sun.jndi.ldap.LdapCtxFactory";
        this.derefAliases = null;
        this.protocol = null;
        this.adCompat = false;
        this.referrals = null;
        this.userBase = "";
        this.userSearch = null;
        this.userSearchAsUser = false;
        this.userSearchFormat = null;
        this.userSubtree = false;
        this.userPassword = null;
        this.userRoleAttribute = null;
        this.userPatternArray = null;
        this.userPattern = null;
        this.userPatternFormatArray = null;
        this.roleBase = "";
        this.roleBaseFormat = null;
        this.roleFormat = null;
        this.userRoleName = null;
        this.roleName = null;
        this.roleSearch = null;
        this.roleSubtree = false;
        this.roleNested = false;
        this.roleSearchAsUser = false;
        this.connectionAttempt = 0;
        this.commonRole = null;
        this.connectionTimeout = "5000";
        this.sizeLimit = 0L;
        this.timeLimit = 0;
        this.useDelegatedCredential = true;
        this.spnegoDelegationQop = "auth-conf";
        this.useStartTls = false;
        this.tls = null;
        this.cipherSuitesArray = null;
        this.hostnameVerifier = null;
        this.sslSocketFactory = null;
    }
    public String getAuthentication() {
        return this.authentication;
    }
    public void setAuthentication ( final String authentication ) {
        this.authentication = authentication;
    }
    public String getConnectionName() {
        return this.connectionName;
    }
    public void setConnectionName ( final String connectionName ) {
        this.connectionName = connectionName;
    }
    public String getConnectionPassword() {
        return this.connectionPassword;
    }
    public void setConnectionPassword ( final String connectionPassword ) {
        this.connectionPassword = connectionPassword;
    }
    public String getConnectionURL() {
        return this.connectionURL;
    }
    public void setConnectionURL ( final String connectionURL ) {
        this.connectionURL = connectionURL;
    }
    public String getContextFactory() {
        return this.contextFactory;
    }
    public void setContextFactory ( final String contextFactory ) {
        this.contextFactory = contextFactory;
    }
    public String getDerefAliases() {
        return this.derefAliases;
    }
    public void setDerefAliases ( final String derefAliases ) {
        this.derefAliases = derefAliases;
    }
    public String getProtocol() {
        return this.protocol;
    }
    public void setProtocol ( final String protocol ) {
        this.protocol = protocol;
    }
    public boolean getAdCompat() {
        return this.adCompat;
    }
    public void setAdCompat ( final boolean adCompat ) {
        this.adCompat = adCompat;
    }
    public String getReferrals() {
        return this.referrals;
    }
    public void setReferrals ( final String referrals ) {
        this.referrals = referrals;
    }
    public String getUserBase() {
        return this.userBase;
    }
    public void setUserBase ( final String userBase ) {
        this.userBase = userBase;
    }
    public String getUserSearch() {
        return this.userSearch;
    }
    public void setUserSearch ( final String userSearch ) {
        this.userSearch = userSearch;
        if ( userSearch == null ) {
            this.userSearchFormat = null;
        } else {
            this.userSearchFormat = new MessageFormat ( userSearch );
        }
    }
    public boolean isUserSearchAsUser() {
        return this.userSearchAsUser;
    }
    public void setUserSearchAsUser ( final boolean userSearchAsUser ) {
        this.userSearchAsUser = userSearchAsUser;
    }
    public boolean getUserSubtree() {
        return this.userSubtree;
    }
    public void setUserSubtree ( final boolean userSubtree ) {
        this.userSubtree = userSubtree;
    }
    public String getUserRoleName() {
        return this.userRoleName;
    }
    public void setUserRoleName ( final String userRoleName ) {
        this.userRoleName = userRoleName;
    }
    public String getRoleBase() {
        return this.roleBase;
    }
    public void setRoleBase ( final String roleBase ) {
        this.roleBase = roleBase;
        if ( roleBase == null ) {
            this.roleBaseFormat = null;
        } else {
            this.roleBaseFormat = new MessageFormat ( roleBase );
        }
    }
    public String getRoleName() {
        return this.roleName;
    }
    public void setRoleName ( final String roleName ) {
        this.roleName = roleName;
    }
    public String getRoleSearch() {
        return this.roleSearch;
    }
    public void setRoleSearch ( final String roleSearch ) {
        this.roleSearch = roleSearch;
        if ( roleSearch == null ) {
            this.roleFormat = null;
        } else {
            this.roleFormat = new MessageFormat ( roleSearch );
        }
    }
    public boolean isRoleSearchAsUser() {
        return this.roleSearchAsUser;
    }
    public void setRoleSearchAsUser ( final boolean roleSearchAsUser ) {
        this.roleSearchAsUser = roleSearchAsUser;
    }
    public boolean getRoleSubtree() {
        return this.roleSubtree;
    }
    public void setRoleSubtree ( final boolean roleSubtree ) {
        this.roleSubtree = roleSubtree;
    }
    public boolean getRoleNested() {
        return this.roleNested;
    }
    public void setRoleNested ( final boolean roleNested ) {
        this.roleNested = roleNested;
    }
    public String getUserPassword() {
        return this.userPassword;
    }
    public void setUserPassword ( final String userPassword ) {
        this.userPassword = userPassword;
    }
    public String getUserRoleAttribute() {
        return this.userRoleAttribute;
    }
    public void setUserRoleAttribute ( final String userRoleAttribute ) {
        this.userRoleAttribute = userRoleAttribute;
    }
    public String getUserPattern() {
        return this.userPattern;
    }
    public void setUserPattern ( final String userPattern ) {
        this.userPattern = userPattern;
        if ( userPattern == null ) {
            this.userPatternArray = null;
        } else {
            this.userPatternArray = this.parseUserPatternString ( userPattern );
            final int len = this.userPatternArray.length;
            this.userPatternFormatArray = new MessageFormat[len];
            for ( int i = 0; i < len; ++i ) {
                this.userPatternFormatArray[i] = new MessageFormat ( this.userPatternArray[i] );
            }
        }
    }
    public String getAlternateURL() {
        return this.alternateURL;
    }
    public void setAlternateURL ( final String alternateURL ) {
        this.alternateURL = alternateURL;
    }
    public String getCommonRole() {
        return this.commonRole;
    }
    public void setCommonRole ( final String commonRole ) {
        this.commonRole = commonRole;
    }
    public String getConnectionTimeout() {
        return this.connectionTimeout;
    }
    public void setConnectionTimeout ( final String timeout ) {
        this.connectionTimeout = timeout;
    }
    public long getSizeLimit() {
        return this.sizeLimit;
    }
    public void setSizeLimit ( final long sizeLimit ) {
        this.sizeLimit = sizeLimit;
    }
    public int getTimeLimit() {
        return this.timeLimit;
    }
    public void setTimeLimit ( final int timeLimit ) {
        this.timeLimit = timeLimit;
    }
    public boolean isUseDelegatedCredential() {
        return this.useDelegatedCredential;
    }
    public void setUseDelegatedCredential ( final boolean useDelegatedCredential ) {
        this.useDelegatedCredential = useDelegatedCredential;
    }
    public String getSpnegoDelegationQop() {
        return this.spnegoDelegationQop;
    }
    public void setSpnegoDelegationQop ( final String spnegoDelegationQop ) {
        this.spnegoDelegationQop = spnegoDelegationQop;
    }
    public boolean getUseStartTls() {
        return this.useStartTls;
    }
    public void setUseStartTls ( final boolean useStartTls ) {
        this.useStartTls = useStartTls;
    }
    private String[] getCipherSuitesArray() {
        if ( this.cipherSuites == null || this.cipherSuitesArray != null ) {
            return this.cipherSuitesArray;
        }
        if ( this.cipherSuites.trim().isEmpty() ) {
            this.containerLog.warn ( JNDIRealm.sm.getString ( "jndiRealm.emptyCipherSuites" ) );
            this.cipherSuitesArray = null;
        } else {
            this.cipherSuitesArray = this.cipherSuites.trim().split ( "\\s*,\\s*" );
            this.containerLog.debug ( JNDIRealm.sm.getString ( "jndiRealm.cipherSuites", Arrays.toString ( this.cipherSuitesArray ) ) );
        }
        return this.cipherSuitesArray;
    }
    public void setCipherSuites ( final String suites ) {
        this.cipherSuites = suites;
    }
    public String getHostnameVerifierClassName() {
        if ( this.hostnameVerifier == null ) {
            return "";
        }
        return this.hostnameVerifier.getClass().getCanonicalName();
    }
    public void setHostnameVerifierClassName ( final String verifierClassName ) {
        if ( verifierClassName != null ) {
            this.hostNameVerifierClassName = verifierClassName.trim();
        } else {
            this.hostNameVerifierClassName = null;
        }
    }
    public HostnameVerifier getHostnameVerifier() {
        if ( this.hostnameVerifier != null ) {
            return this.hostnameVerifier;
        }
        if ( this.hostNameVerifierClassName == null || this.hostNameVerifierClassName.equals ( "" ) ) {
            return null;
        }
        try {
            final Object o = this.constructInstance ( this.hostNameVerifierClassName );
            if ( o instanceof HostnameVerifier ) {
                return this.hostnameVerifier = ( HostnameVerifier ) o;
            }
            try {
                throw new IllegalArgumentException ( JNDIRealm.sm.getString ( "jndiRealm.invalidHostnameVerifier", this.hostNameVerifierClassName ) );
            } catch ( SecurityException | InstantiationException e ) {
                throw new IllegalArgumentException ( JNDIRealm.sm.getString ( "jndiRealm.invalidHostnameVerifier", this.hostNameVerifierClassName ), e );
            }
        } catch ( ClassNotFoundException ) {}
        catch ( SecurityException ) {}
        catch ( InstantiationException ) {}
        catch ( IllegalAccessException ) {}
    }
    public void setSslSocketFactoryClassName ( final String factoryClassName ) {
        this.sslSocketFactoryClassName = factoryClassName;
    }
    public void setSslProtocol ( final String protocol ) {
        this.sslProtocol = protocol;
    }
    private String[] getSupportedSslProtocols() {
        try {
            final SSLContext sslContext = SSLContext.getDefault();
            return sslContext.getSupportedSSLParameters().getProtocols();
        } catch ( NoSuchAlgorithmException e ) {
            throw new RuntimeException ( JNDIRealm.sm.getString ( "jndiRealm.exception" ), e );
        }
    }
    private Object constructInstance ( final String className ) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        final Class<?> clazz = Class.forName ( className );
        return clazz.newInstance();
    }
    @Override
    public Principal authenticate ( final String username, final String credentials ) {
        DirContext context = null;
        Principal principal = null;
        try {
            context = this.open();
            try {
                principal = this.authenticate ( context, username, credentials );
            } catch ( NullPointerException | CommunicationException | ServiceUnavailableException e ) {
                this.containerLog.info ( JNDIRealm.sm.getString ( "jndiRealm.exception.retry" ), e );
                if ( context != null ) {
                    this.close ( context );
                }
                context = this.open();
                principal = this.authenticate ( context, username, credentials );
            }
            this.release ( context );
            return principal;
        } catch ( NamingException e2 ) {
            this.containerLog.error ( JNDIRealm.sm.getString ( "jndiRealm.exception" ), e2 );
            if ( context != null ) {
                this.close ( context );
            }
            if ( this.containerLog.isDebugEnabled() ) {
                this.containerLog.debug ( "Returning null principal." );
            }
            return null;
        }
    }
    public synchronized Principal authenticate ( final DirContext context, final String username, final String credentials ) throws NamingException {
        if ( username == null || username.equals ( "" ) || credentials == null || credentials.equals ( "" ) ) {
            if ( this.containerLog.isDebugEnabled() ) {
                this.containerLog.debug ( "username null or empty: returning null principal." );
            }
            return null;
        }
        if ( this.userPatternArray != null ) {
            for ( int curUserPattern = 0; curUserPattern < this.userPatternFormatArray.length; ++curUserPattern ) {
                final User user = this.getUser ( context, username, credentials, curUserPattern );
                if ( user != null ) {
                    try {
                        if ( this.checkCredentials ( context, user, credentials ) ) {
                            final List<String> roles = this.getRoles ( context, user );
                            if ( this.containerLog.isDebugEnabled() ) {
                                this.containerLog.debug ( "Found roles: " + roles.toString() );
                            }
                            return new GenericPrincipal ( username, credentials, roles );
                        }
                    } catch ( InvalidNameException ine ) {
                        this.containerLog.warn ( JNDIRealm.sm.getString ( "jndiRealm.exception" ), ine );
                    }
                }
            }
            return null;
        }
        final User user2 = this.getUser ( context, username, credentials );
        if ( user2 == null ) {
            return null;
        }
        if ( !this.checkCredentials ( context, user2, credentials ) ) {
            return null;
        }
        final List<String> roles2 = this.getRoles ( context, user2 );
        if ( this.containerLog.isDebugEnabled() ) {
            this.containerLog.debug ( "Found roles: " + roles2.toString() );
        }
        return new GenericPrincipal ( username, credentials, roles2 );
    }
    protected User getUser ( final DirContext context, final String username ) throws NamingException {
        return this.getUser ( context, username, null, -1 );
    }
    protected User getUser ( final DirContext context, final String username, final String credentials ) throws NamingException {
        return this.getUser ( context, username, credentials, -1 );
    }
    protected User getUser ( final DirContext context, final String username, final String credentials, final int curUserPattern ) throws NamingException {
        User user = null;
        final ArrayList<String> list = new ArrayList<String>();
        if ( this.userPassword != null ) {
            list.add ( this.userPassword );
        }
        if ( this.userRoleName != null ) {
            list.add ( this.userRoleName );
        }
        if ( this.userRoleAttribute != null ) {
            list.add ( this.userRoleAttribute );
        }
        final String[] attrIds = new String[list.size()];
        list.toArray ( attrIds );
        if ( this.userPatternFormatArray != null && curUserPattern >= 0 ) {
            user = this.getUserByPattern ( context, username, credentials, attrIds, curUserPattern );
            if ( this.containerLog.isDebugEnabled() ) {
                this.containerLog.debug ( "Found user by pattern [" + user + "]" );
            }
        } else {
            final boolean thisUserSearchAsUser = this.isUserSearchAsUser();
            try {
                if ( thisUserSearchAsUser ) {
                    this.userCredentialsAdd ( context, username, credentials );
                }
                user = this.getUserBySearch ( context, username, attrIds );
            } finally {
                if ( thisUserSearchAsUser ) {
                    this.userCredentialsRemove ( context );
                }
            }
            if ( this.containerLog.isDebugEnabled() ) {
                this.containerLog.debug ( "Found user by search [" + user + "]" );
            }
        }
        if ( this.userPassword == null && credentials != null && user != null ) {
            return new User ( user.getUserName(), user.getDN(), credentials, user.getRoles(), user.getUserRoleId() );
        }
        return user;
    }
    protected User getUserByPattern ( final DirContext context, final String username, final String[] attrIds, final String dn ) throws NamingException {
        if ( attrIds == null || attrIds.length == 0 ) {
            return new User ( username, dn, null, null, null );
        }
        Attributes attrs = null;
        try {
            attrs = context.getAttributes ( dn, attrIds );
        } catch ( NameNotFoundException e ) {
            return null;
        }
        if ( attrs == null ) {
            return null;
        }
        String password = null;
        if ( this.userPassword != null ) {
            password = this.getAttributeValue ( this.userPassword, attrs );
        }
        String userRoleAttrValue = null;
        if ( this.userRoleAttribute != null ) {
            userRoleAttrValue = this.getAttributeValue ( this.userRoleAttribute, attrs );
        }
        ArrayList<String> roles = null;
        if ( this.userRoleName != null ) {
            roles = this.addAttributeValues ( this.userRoleName, attrs, roles );
        }
        return new User ( username, dn, password, roles, userRoleAttrValue );
    }
    protected User getUserByPattern ( final DirContext context, final String username, final String credentials, final String[] attrIds, final int curUserPattern ) throws NamingException {
        User user = null;
        if ( username == null || this.userPatternFormatArray[curUserPattern] == null ) {
            return null;
        }
        final String dn = this.userPatternFormatArray[curUserPattern].format ( new String[] { username } );
        try {
            user = this.getUserByPattern ( context, username, attrIds, dn );
        } catch ( NameNotFoundException e ) {
            return null;
        } catch ( NamingException e2 ) {
            try {
                this.userCredentialsAdd ( context, dn, credentials );
                user = this.getUserByPattern ( context, username, attrIds, dn );
            } finally {
                this.userCredentialsRemove ( context );
            }
        }
        return user;
    }
    protected User getUserBySearch ( final DirContext context, final String username, String[] attrIds ) throws NamingException {
        if ( username == null || this.userSearchFormat == null ) {
            return null;
        }
        final String filter = this.userSearchFormat.format ( new String[] { username } );
        final SearchControls constraints = new SearchControls();
        if ( this.userSubtree ) {
            constraints.setSearchScope ( 2 );
        } else {
            constraints.setSearchScope ( 1 );
        }
        constraints.setCountLimit ( this.sizeLimit );
        constraints.setTimeLimit ( this.timeLimit );
        if ( attrIds == null ) {
            attrIds = new String[0];
        }
        constraints.setReturningAttributes ( attrIds );
        final NamingEnumeration<SearchResult> results = context.search ( this.userBase, filter, constraints );
        try {
            try {
                if ( results == null || !results.hasMore() ) {
                    return null;
                }
            } catch ( PartialResultException ex ) {
                if ( !this.adCompat ) {
                    throw ex;
                }
                return null;
            }
            final SearchResult result = results.next();
            try {
                if ( results.hasMore() ) {
                    if ( this.containerLog.isInfoEnabled() ) {
                        this.containerLog.info ( "username " + username + " has multiple entries" );
                    }
                    return null;
                }
            } catch ( PartialResultException ex2 ) {
                if ( !this.adCompat ) {
                    throw ex2;
                }
            }
            final String dn = this.getDistinguishedName ( context, this.userBase, result );
            if ( this.containerLog.isTraceEnabled() ) {
                this.containerLog.trace ( "  entry found for " + username + " with dn " + dn );
            }
            final Attributes attrs = result.getAttributes();
            if ( attrs == null ) {
                return null;
            }
            String password = null;
            if ( this.userPassword != null ) {
                password = this.getAttributeValue ( this.userPassword, attrs );
            }
            String userRoleAttrValue = null;
            if ( this.userRoleAttribute != null ) {
                userRoleAttrValue = this.getAttributeValue ( this.userRoleAttribute, attrs );
            }
            ArrayList<String> roles = null;
            if ( this.userRoleName != null ) {
                roles = this.addAttributeValues ( this.userRoleName, attrs, roles );
            }
            return new User ( username, dn, password, roles, userRoleAttrValue );
        } finally {
            if ( results != null ) {
                results.close();
            }
        }
    }
    protected boolean checkCredentials ( final DirContext context, final User user, final String credentials ) throws NamingException {
        boolean validated = false;
        if ( this.userPassword == null ) {
            validated = this.bindAsUser ( context, user, credentials );
        } else {
            validated = this.compareCredentials ( context, user, credentials );
        }
        if ( this.containerLog.isTraceEnabled() ) {
            if ( validated ) {
                this.containerLog.trace ( JNDIRealm.sm.getString ( "jndiRealm.authenticateSuccess", user.getUserName() ) );
            } else {
                this.containerLog.trace ( JNDIRealm.sm.getString ( "jndiRealm.authenticateFailure", user.getUserName() ) );
            }
        }
        return validated;
    }
    protected boolean compareCredentials ( final DirContext context, final User info, final String credentials ) throws NamingException {
        if ( this.containerLog.isTraceEnabled() ) {
            this.containerLog.trace ( "  validating credentials" );
        }
        if ( info == null || credentials == null ) {
            return false;
        }
        final String password = info.getPassword();
        return this.getCredentialHandler().matches ( credentials, password );
    }
    protected boolean bindAsUser ( final DirContext context, final User user, final String credentials ) throws NamingException {
        if ( credentials == null || user == null ) {
            return false;
        }
        final String dn = user.getDN();
        if ( dn == null ) {
            return false;
        }
        if ( this.containerLog.isTraceEnabled() ) {
            this.containerLog.trace ( "  validating credentials by binding as the user" );
        }
        this.userCredentialsAdd ( context, dn, credentials );
        boolean validated = false;
        try {
            if ( this.containerLog.isTraceEnabled() ) {
                this.containerLog.trace ( "  binding as " + dn );
            }
            context.getAttributes ( "", null );
            validated = true;
        } catch ( AuthenticationException e ) {
            if ( this.containerLog.isTraceEnabled() ) {
                this.containerLog.trace ( "  bind attempt failed" );
            }
        }
        this.userCredentialsRemove ( context );
        return validated;
    }
    private void userCredentialsAdd ( final DirContext context, final String dn, final String credentials ) throws NamingException {
        context.addToEnvironment ( "java.naming.security.principal", dn );
        context.addToEnvironment ( "java.naming.security.credentials", credentials );
    }
    private void userCredentialsRemove ( final DirContext context ) throws NamingException {
        if ( this.connectionName != null ) {
            context.addToEnvironment ( "java.naming.security.principal", this.connectionName );
        } else {
            context.removeFromEnvironment ( "java.naming.security.principal" );
        }
        if ( this.connectionPassword != null ) {
            context.addToEnvironment ( "java.naming.security.credentials", this.connectionPassword );
        } else {
            context.removeFromEnvironment ( "java.naming.security.credentials" );
        }
    }
    protected List<String> getRoles ( final DirContext context, final User user ) throws NamingException {
        if ( user == null ) {
            return null;
        }
        final String dn = user.getDN();
        final String username = user.getUserName();
        final String userRoleId = user.getUserRoleId();
        if ( dn == null || username == null ) {
            return null;
        }
        if ( this.containerLog.isTraceEnabled() ) {
            this.containerLog.trace ( "  getRoles(" + dn + ")" );
        }
        final List<String> list = new ArrayList<String>();
        final List<String> userRoles = user.getRoles();
        if ( userRoles != null ) {
            list.addAll ( userRoles );
        }
        if ( this.commonRole != null ) {
            list.add ( this.commonRole );
        }
        if ( this.containerLog.isTraceEnabled() ) {
            this.containerLog.trace ( "  Found " + list.size() + " user internal roles" );
            this.containerLog.trace ( "  Found user internal roles " + list.toString() );
        }
        if ( this.roleFormat == null || this.roleName == null ) {
            return list;
        }
        String filter = this.roleFormat.format ( new String[] { this.doRFC2254Encoding ( dn ), username, userRoleId } );
        final SearchControls controls = new SearchControls();
        if ( this.roleSubtree ) {
            controls.setSearchScope ( 2 );
        } else {
            controls.setSearchScope ( 1 );
        }
        controls.setReturningAttributes ( new String[] { this.roleName } );
        String base = null;
        if ( this.roleBaseFormat != null ) {
            final NameParser np = context.getNameParser ( "" );
            final Name name = np.parse ( dn );
            final String[] nameParts = new String[name.size()];
            for ( int i = 0; i < name.size(); ++i ) {
                nameParts[i] = name.get ( i );
            }
            base = this.roleBaseFormat.format ( nameParts );
        } else {
            base = "";
        }
        NamingEnumeration<SearchResult> results = this.searchAsUser ( context, user, base, filter, controls, this.isRoleSearchAsUser() );
        if ( results == null ) {
            return list;
        }
        final HashMap<String, String> groupMap = new HashMap<String, String>();
        try {
            while ( results.hasMore() ) {
                final SearchResult result = results.next();
                final Attributes attrs = result.getAttributes();
                if ( attrs == null ) {
                    continue;
                }
                final String dname = this.getDistinguishedName ( context, this.roleBase, result );
                final String name2 = this.getAttributeValue ( this.roleName, attrs );
                if ( name2 == null || dname == null ) {
                    continue;
                }
                groupMap.put ( dname, name2 );
            }
        } catch ( PartialResultException ex ) {
            if ( !this.adCompat ) {
                throw ex;
            }
        } finally {
            results.close();
        }
        if ( this.containerLog.isTraceEnabled() ) {
            final Set<Map.Entry<String, String>> entries = groupMap.entrySet();
            this.containerLog.trace ( "  Found " + entries.size() + " direct roles" );
            for ( final Map.Entry<String, String> entry : entries ) {
                this.containerLog.trace ( "  Found direct role " + entry.getKey() + " -> " + entry.getValue() );
            }
        }
        if ( this.getRoleNested() ) {
            Map<String, String> newThisRound;
            for ( Map<String, String> newGroups = new HashMap<String, String> ( groupMap ); !newGroups.isEmpty(); newGroups = newThisRound ) {
                newThisRound = new HashMap<String, String>();
                for ( final Map.Entry<String, String> group : newGroups.entrySet() ) {
                    filter = this.roleFormat.format ( new String[] { group.getKey(), group.getValue(), group.getValue() } );
                    if ( this.containerLog.isTraceEnabled() ) {
                        this.containerLog.trace ( "Perform a nested group search with base " + this.roleBase + " and filter " + filter );
                    }
                    results = this.searchAsUser ( context, user, this.roleBase, filter, controls, this.isRoleSearchAsUser() );
                    try {
                        while ( results.hasMore() ) {
                            final SearchResult result2 = results.next();
                            final Attributes attrs2 = result2.getAttributes();
                            if ( attrs2 == null ) {
                                continue;
                            }
                            final String dname2 = this.getDistinguishedName ( context, this.roleBase, result2 );
                            final String name3 = this.getAttributeValue ( this.roleName, attrs2 );
                            if ( name3 == null || dname2 == null || groupMap.keySet().contains ( dname2 ) ) {
                                continue;
                            }
                            groupMap.put ( dname2, name3 );
                            newThisRound.put ( dname2, name3 );
                            if ( !this.containerLog.isTraceEnabled() ) {
                                continue;
                            }
                            this.containerLog.trace ( "  Found nested role " + dname2 + " -> " + name3 );
                        }
                    } catch ( PartialResultException ex2 ) {
                        if ( !this.adCompat ) {
                            throw ex2;
                        }
                        continue;
                    } finally {
                        results.close();
                    }
                }
            }
        }
        list.addAll ( groupMap.values() );
        return list;
    }
    private NamingEnumeration<SearchResult> searchAsUser ( final DirContext context, final User user, final String base, final String filter, final SearchControls controls, final boolean searchAsUser ) throws NamingException {
        NamingEnumeration<SearchResult> results;
        try {
            if ( searchAsUser ) {
                this.userCredentialsAdd ( context, user.getDN(), user.getPassword() );
            }
            results = context.search ( base, filter, controls );
        } finally {
            if ( searchAsUser ) {
                this.userCredentialsRemove ( context );
            }
        }
        return results;
    }
    private String getAttributeValue ( final String attrId, final Attributes attrs ) throws NamingException {
        if ( this.containerLog.isTraceEnabled() ) {
            this.containerLog.trace ( "  retrieving attribute " + attrId );
        }
        if ( attrId == null || attrs == null ) {
            return null;
        }
        final Attribute attr = attrs.get ( attrId );
        if ( attr == null ) {
            return null;
        }
        final Object value = attr.get();
        if ( value == null ) {
            return null;
        }
        String valueString = null;
        if ( value instanceof byte[] ) {
            valueString = new String ( ( byte[] ) value );
        } else {
            valueString = value.toString();
        }
        return valueString;
    }
    private ArrayList<String> addAttributeValues ( final String attrId, final Attributes attrs, ArrayList<String> values ) throws NamingException {
        if ( this.containerLog.isTraceEnabled() ) {
            this.containerLog.trace ( "  retrieving values for attribute " + attrId );
        }
        if ( attrId == null || attrs == null ) {
            return values;
        }
        if ( values == null ) {
            values = new ArrayList<String>();
        }
        final Attribute attr = attrs.get ( attrId );
        if ( attr == null ) {
            return values;
        }
        final NamingEnumeration<?> e = attr.getAll();
        try {
            while ( e.hasMore() ) {
                final String value = ( String ) e.next();
                values.add ( value );
            }
        } catch ( PartialResultException ex ) {
            if ( !this.adCompat ) {
                throw ex;
            }
        } finally {
            e.close();
        }
        return values;
    }
    protected void close ( final DirContext context ) {
        if ( context == null ) {
            return;
        }
        if ( this.tls != null ) {
            try {
                this.tls.close();
            } catch ( IOException e ) {
                this.containerLog.error ( JNDIRealm.sm.getString ( "jndiRealm.tlsClose" ), e );
            }
        }
        try {
            if ( this.containerLog.isDebugEnabled() ) {
                this.containerLog.debug ( "Closing directory context" );
            }
            context.close();
        } catch ( NamingException e2 ) {
            this.containerLog.error ( JNDIRealm.sm.getString ( "jndiRealm.close" ), e2 );
        }
        this.context = null;
    }
    @Override
    protected String getName() {
        return "JNDIRealm";
    }
    @Override
    protected String getPassword ( final String username ) {
        final String userPassword = this.getUserPassword();
        if ( userPassword == null || userPassword.isEmpty() ) {
            return null;
        }
        try {
            final User user = this.getUser ( this.open(), username, null );
            if ( user == null ) {
                return null;
            }
            return user.getPassword();
        } catch ( NamingException e ) {
            return null;
        }
    }
    @Override
    protected Principal getPrincipal ( final String username ) {
        return this.getPrincipal ( username, null );
    }
    @Override
    protected Principal getPrincipal ( final String username, final GSSCredential gssCredential ) {
        DirContext context = null;
        Principal principal = null;
        try {
            context = this.open();
            try {
                principal = this.getPrincipal ( context, username, gssCredential );
            } catch ( CommunicationException | ServiceUnavailableException e ) {
                this.containerLog.info ( JNDIRealm.sm.getString ( "jndiRealm.exception.retry" ), e );
                if ( context != null ) {
                    this.close ( context );
                }
                context = this.open();
                principal = this.getPrincipal ( context, username, gssCredential );
            }
            this.release ( context );
            return principal;
        } catch ( NamingException e ) {
            this.containerLog.error ( JNDIRealm.sm.getString ( "jndiRealm.exception" ), e );
            if ( context != null ) {
                this.close ( context );
            }
            return null;
        }
    }
    protected synchronized Principal getPrincipal ( final DirContext context, final String username, final GSSCredential gssCredential ) throws NamingException {
        User user = null;
        List<String> roles = null;
        Hashtable<?, ?> preservedEnvironment = null;
        try {
            if ( gssCredential != null && this.isUseDelegatedCredential() ) {
                preservedEnvironment = context.getEnvironment();
                context.addToEnvironment ( "java.naming.security.authentication", "GSSAPI" );
                context.addToEnvironment ( "javax.security.sasl.server.authentication", "true" );
                context.addToEnvironment ( "javax.security.sasl.qop", this.spnegoDelegationQop );
            }
            user = this.getUser ( context, username );
            if ( user != null ) {
                roles = this.getRoles ( context, user );
            }
        } finally {
            this.restoreEnvironmentParameter ( context, "java.naming.security.authentication", preservedEnvironment );
            this.restoreEnvironmentParameter ( context, "javax.security.sasl.server.authentication", preservedEnvironment );
            this.restoreEnvironmentParameter ( context, "javax.security.sasl.qop", preservedEnvironment );
        }
        if ( user != null ) {
            return new GenericPrincipal ( user.getUserName(), user.getPassword(), roles, null, null, gssCredential );
        }
        return null;
    }
    private void restoreEnvironmentParameter ( final DirContext context, final String parameterName, final Hashtable<?, ?> preservedEnvironment ) {
        try {
            context.removeFromEnvironment ( parameterName );
            if ( preservedEnvironment != null && preservedEnvironment.containsKey ( parameterName ) ) {
                context.addToEnvironment ( parameterName, preservedEnvironment.get ( parameterName ) );
            }
        } catch ( NamingException ex ) {}
    }
    protected DirContext open() throws NamingException {
        if ( this.context != null ) {
            return this.context;
        }
        try {
            this.context = this.createDirContext ( this.getDirectoryContextEnvironment() );
        } catch ( Exception e ) {
            this.connectionAttempt = 1;
            this.containerLog.info ( JNDIRealm.sm.getString ( "jndiRealm.exception.retry" ), e );
            this.context = this.createDirContext ( this.getDirectoryContextEnvironment() );
        } finally {
            this.connectionAttempt = 0;
        }
        return this.context;
    }
    @Override
    public boolean isAvailable() {
        return this.context != null;
    }
    private DirContext createDirContext ( final Hashtable<String, String> env ) throws NamingException {
        if ( this.useStartTls ) {
            return this.createTlsDirContext ( env );
        }
        return new InitialDirContext ( env );
    }
    private SSLSocketFactory getSSLSocketFactory() {
        if ( this.sslSocketFactory != null ) {
            return this.sslSocketFactory;
        }
        SSLSocketFactory result;
        if ( this.sslSocketFactoryClassName != null && !this.sslSocketFactoryClassName.trim().equals ( "" ) ) {
            result = this.createSSLSocketFactoryFromClassName ( this.sslSocketFactoryClassName );
        } else {
            result = this.createSSLContextFactoryFromProtocol ( this.sslProtocol );
        }
        return this.sslSocketFactory = result;
    }
    private SSLSocketFactory createSSLSocketFactoryFromClassName ( final String className ) {
        try {
            final Object o = this.constructInstance ( className );
            if ( o instanceof SSLSocketFactory ) {
                return this.sslSocketFactory;
            }
            try {
                throw new IllegalArgumentException ( JNDIRealm.sm.getString ( "jndiRealm.invalidSslSocketFactory", className ) );
            } catch ( SecurityException | InstantiationException e ) {
                throw new IllegalArgumentException ( JNDIRealm.sm.getString ( "jndiRealm.invalidSslSocketFactory", className ), e );
            }
        } catch ( ClassNotFoundException ) {}
        catch ( SecurityException ) {}
        catch ( InstantiationException ) {}
        catch ( IllegalAccessException ) {}
    }
    private SSLSocketFactory createSSLContextFactoryFromProtocol ( final String protocol ) {
        try {
            SSLContext sslContext;
            if ( protocol != null ) {
                sslContext = SSLContext.getInstance ( protocol );
                sslContext.init ( null, null, null );
            } else {
                sslContext = SSLContext.getDefault();
            }
            return sslContext.getSocketFactory();
        } catch ( NoSuchAlgorithmException | KeyManagementException e ) {
            final List<String> allowedProtocols = Arrays.asList ( this.getSupportedSslProtocols() );
            throw new IllegalArgumentException ( JNDIRealm.sm.getString ( "jndiRealm.invalidSslProtocol", protocol, allowedProtocols ), e );
        }
    }
    private DirContext createTlsDirContext ( final Hashtable<String, String> env ) throws NamingException {
        final Map<String, Object> savedEnv = new HashMap<String, Object>();
        for ( final String key : Arrays.asList ( "java.naming.security.authentication", "java.naming.security.credentials", "java.naming.security.principal", "java.naming.security.protocol" ) ) {
            final Object entry = env.remove ( key );
            if ( entry != null ) {
                savedEnv.put ( key, entry );
            }
        }
        LdapContext result = null;
        try {
            result = new InitialLdapContext ( env, null );
            this.tls = ( StartTlsResponse ) result.extendedOperation ( new StartTlsRequest() );
            if ( this.getHostnameVerifier() != null ) {
                this.tls.setHostnameVerifier ( this.getHostnameVerifier() );
            }
            if ( this.getCipherSuitesArray() != null ) {
                this.tls.setEnabledCipherSuites ( this.getCipherSuitesArray() );
            }
            try {
                final SSLSession negotiate = this.tls.negotiate ( this.getSSLSocketFactory() );
                this.containerLog.debug ( JNDIRealm.sm.getString ( "jndiRealm.negotiatedTls", negotiate.getProtocol() ) );
            } catch ( IOException e ) {
                throw new NamingException ( e.getMessage() );
            }
        } finally {
            if ( result != null ) {
                for ( final Map.Entry<String, Object> savedEntry : savedEnv.entrySet() ) {
                    result.addToEnvironment ( savedEntry.getKey(), savedEntry.getValue() );
                }
            }
        }
        return result;
    }
    protected Hashtable<String, String> getDirectoryContextEnvironment() {
        final Hashtable<String, String> env = new Hashtable<String, String>();
        if ( this.containerLog.isDebugEnabled() && this.connectionAttempt == 0 ) {
            this.containerLog.debug ( "Connecting to URL " + this.connectionURL );
        } else if ( this.containerLog.isDebugEnabled() && this.connectionAttempt > 0 ) {
            this.containerLog.debug ( "Connecting to URL " + this.alternateURL );
        }
        env.put ( "java.naming.factory.initial", this.contextFactory );
        if ( this.connectionName != null ) {
            env.put ( "java.naming.security.principal", this.connectionName );
        }
        if ( this.connectionPassword != null ) {
            env.put ( "java.naming.security.credentials", this.connectionPassword );
        }
        if ( this.connectionURL != null && this.connectionAttempt == 0 ) {
            env.put ( "java.naming.provider.url", this.connectionURL );
        } else if ( this.alternateURL != null && this.connectionAttempt > 0 ) {
            env.put ( "java.naming.provider.url", this.alternateURL );
        }
        if ( this.authentication != null ) {
            env.put ( "java.naming.security.authentication", this.authentication );
        }
        if ( this.protocol != null ) {
            env.put ( "java.naming.security.protocol", this.protocol );
        }
        if ( this.referrals != null ) {
            env.put ( "java.naming.referral", this.referrals );
        }
        if ( this.derefAliases != null ) {
            env.put ( "java.naming.ldap.derefAliases", this.derefAliases );
        }
        if ( this.connectionTimeout != null ) {
            env.put ( "com.sun.jndi.ldap.connect.timeout", this.connectionTimeout );
        }
        return env;
    }
    protected void release ( final DirContext context ) {
    }
    @Override
    protected void startInternal() throws LifecycleException {
        try {
            this.open();
        } catch ( NamingException e ) {
            this.containerLog.error ( JNDIRealm.sm.getString ( "jndiRealm.open" ), e );
        }
        super.startInternal();
    }
    @Override
    protected void stopInternal() throws LifecycleException {
        super.stopInternal();
        this.close ( this.context );
    }
    protected String[] parseUserPatternString ( final String userPatternString ) {
        if ( userPatternString == null ) {
            return null;
        }
        final ArrayList<String> pathList = new ArrayList<String>();
        int startParenLoc = userPatternString.indexOf ( 40 );
        if ( startParenLoc == -1 ) {
            return new String[] { userPatternString };
        }
        for ( int startingPoint = 0; startParenLoc > -1; startParenLoc = userPatternString.indexOf ( 40, startingPoint ) ) {
            int endParenLoc = 0;
            while ( userPatternString.charAt ( startParenLoc + 1 ) == '|' || ( startParenLoc != 0 && userPatternString.charAt ( startParenLoc - 1 ) == '\\' ) ) {
                startParenLoc = userPatternString.indexOf ( 40, startParenLoc + 1 );
            }
            for ( endParenLoc = userPatternString.indexOf ( 41, startParenLoc + 1 ); userPatternString.charAt ( endParenLoc - 1 ) == '\\'; endParenLoc = userPatternString.indexOf ( 41, endParenLoc + 1 ) ) {}
            final String nextPathPart = userPatternString.substring ( startParenLoc + 1, endParenLoc );
            pathList.add ( nextPathPart );
            startingPoint = endParenLoc + 1;
        }
        return pathList.toArray ( new String[0] );
    }
    protected String doRFC2254Encoding ( final String inString ) {
        final StringBuilder buf = new StringBuilder ( inString.length() );
        for ( int i = 0; i < inString.length(); ++i ) {
            final char c = inString.charAt ( i );
            switch ( c ) {
            case '\\': {
                buf.append ( "\\5c" );
                break;
            }
            case '*': {
                buf.append ( "\\2a" );
                break;
            }
            case '(': {
                buf.append ( "\\28" );
                break;
            }
            case ')': {
                buf.append ( "\\29" );
                break;
            }
            case '\0': {
                buf.append ( "\\00" );
                break;
            }
            default: {
                buf.append ( c );
                break;
            }
            }
        }
        return buf.toString();
    }
    protected String getDistinguishedName ( final DirContext context, final String base, final SearchResult result ) throws NamingException {
        final String resultName = result.getName();
        if ( result.isRelative() ) {
            if ( this.containerLog.isTraceEnabled() ) {
                this.containerLog.trace ( "  search returned relative name: " + resultName );
            }
            final NameParser parser = context.getNameParser ( "" );
            final Name contextName = parser.parse ( context.getNameInNamespace() );
            final Name baseName = parser.parse ( base );
            final Name entryName = parser.parse ( new CompositeName ( resultName ).get ( 0 ) );
            Name name = contextName.addAll ( baseName );
            name = name.addAll ( entryName );
            return name.toString();
        }
        if ( this.containerLog.isTraceEnabled() ) {
            this.containerLog.trace ( "  search returned absolute name: " + resultName );
        }
        try {
            final NameParser parser = context.getNameParser ( "" );
            final URI userNameUri = new URI ( resultName );
            final String pathComponent = userNameUri.getPath();
            if ( pathComponent.length() < 1 ) {
                throw new InvalidNameException ( "Search returned unparseable absolute name: " + resultName );
            }
            final Name name2 = parser.parse ( pathComponent.substring ( 1 ) );
            return name2.toString();
        } catch ( URISyntaxException e ) {
            throw new InvalidNameException ( "Search returned unparseable absolute name: " + resultName );
        }
    }
    protected static class User {
        private final String username;
        private final String dn;
        private final String password;
        private final List<String> roles;
        private final String userRoleId;
        public User ( final String username, final String dn, final String password, final List<String> roles, final String userRoleId ) {
            this.username = username;
            this.dn = dn;
            this.password = password;
            if ( roles == null ) {
                this.roles = Collections.emptyList();
            } else {
                this.roles = Collections.unmodifiableList ( ( List<? extends String> ) roles );
            }
            this.userRoleId = userRoleId;
        }
        public String getUserName() {
            return this.username;
        }
        public String getDN() {
            return this.dn;
        }
        public String getPassword() {
            return this.password;
        }
        public List<String> getRoles() {
            return this.roles;
        }
        public String getUserRoleId() {
            return this.userRoleId;
        }
    }
}
