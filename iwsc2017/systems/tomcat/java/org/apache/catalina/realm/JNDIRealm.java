package org.apache.catalina.realm;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.PartialResultException;
import javax.naming.ServiceUnavailableException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import org.apache.catalina.LifecycleException;
import org.ietf.jgss.GSSCredential;
public class JNDIRealm extends RealmBase {
    protected String authentication = null;
    protected String connectionName = null;
    protected String connectionPassword = null;
    protected String connectionURL = null;
    protected DirContext context = null;
    protected String contextFactory = "com.sun.jndi.ldap.LdapCtxFactory";
    protected String derefAliases = null;
    public static final String DEREF_ALIASES = "java.naming.ldap.derefAliases";
    protected static final String name = "JNDIRealm";
    protected String protocol = null;
    protected boolean adCompat = false;
    protected String referrals = null;
    protected String userBase = "";
    protected String userSearch = null;
    private boolean userSearchAsUser = false;
    protected MessageFormat userSearchFormat = null;
    protected boolean userSubtree = false;
    protected String userPassword = null;
    protected String userRoleAttribute = null;
    protected String[] userPatternArray = null;
    protected String userPattern = null;
    protected MessageFormat[] userPatternFormatArray = null;
    protected String roleBase = "";
    protected MessageFormat roleBaseFormat = null;
    protected MessageFormat roleFormat = null;
    protected String userRoleName = null;
    protected String roleName = null;
    protected String roleSearch = null;
    protected boolean roleSubtree = false;
    protected boolean roleNested = false;
    protected boolean roleSearchAsUser = false;
    protected String alternateURL;
    protected int connectionAttempt = 0;
    protected String commonRole = null;
    protected String connectionTimeout = "5000";
    protected long sizeLimit = 0;
    protected int timeLimit = 0;
    protected boolean useDelegatedCredential = true;
    protected String spnegoDelegationQop = "auth-conf";
    private boolean useStartTls = false;
    private StartTlsResponse tls = null;
    private String[] cipherSuitesArray = null;
    private HostnameVerifier hostnameVerifier = null;
    private SSLSocketFactory sslSocketFactory = null;
    private String sslSocketFactoryClassName;
    private String cipherSuites;
    private String hostNameVerifierClassName;
    private String sslProtocol;
    public String getAuthentication() {
        return authentication;
    }
    public void setAuthentication ( String authentication ) {
        this.authentication = authentication;
    }
    public String getConnectionName() {
        return this.connectionName;
    }
    public void setConnectionName ( String connectionName ) {
        this.connectionName = connectionName;
    }
    public String getConnectionPassword() {
        return this.connectionPassword;
    }
    public void setConnectionPassword ( String connectionPassword ) {
        this.connectionPassword = connectionPassword;
    }
    public String getConnectionURL() {
        return this.connectionURL;
    }
    public void setConnectionURL ( String connectionURL ) {
        this.connectionURL = connectionURL;
    }
    public String getContextFactory() {
        return this.contextFactory;
    }
    public void setContextFactory ( String contextFactory ) {
        this.contextFactory = contextFactory;
    }
    public java.lang.String getDerefAliases() {
        return derefAliases;
    }
    public void setDerefAliases ( java.lang.String derefAliases ) {
        this.derefAliases = derefAliases;
    }
    public String getProtocol() {
        return protocol;
    }
    public void setProtocol ( String protocol ) {
        this.protocol = protocol;
    }
    public boolean getAdCompat () {
        return adCompat;
    }
    public void setAdCompat ( boolean adCompat ) {
        this.adCompat = adCompat;
    }
    public String getReferrals () {
        return referrals;
    }
    public void setReferrals ( String referrals ) {
        this.referrals = referrals;
    }
    public String getUserBase() {
        return this.userBase;
    }
    public void setUserBase ( String userBase ) {
        this.userBase = userBase;
    }
    public String getUserSearch() {
        return this.userSearch;
    }
    public void setUserSearch ( String userSearch ) {
        this.userSearch = userSearch;
        if ( userSearch == null ) {
            userSearchFormat = null;
        } else {
            userSearchFormat = new MessageFormat ( userSearch );
        }
    }
    public boolean isUserSearchAsUser() {
        return userSearchAsUser;
    }
    public void setUserSearchAsUser ( boolean userSearchAsUser ) {
        this.userSearchAsUser = userSearchAsUser;
    }
    public boolean getUserSubtree() {
        return this.userSubtree;
    }
    public void setUserSubtree ( boolean userSubtree ) {
        this.userSubtree = userSubtree;
    }
    public String getUserRoleName() {
        return userRoleName;
    }
    public void setUserRoleName ( String userRoleName ) {
        this.userRoleName = userRoleName;
    }
    public String getRoleBase() {
        return this.roleBase;
    }
    public void setRoleBase ( String roleBase ) {
        this.roleBase = roleBase;
        if ( roleBase == null ) {
            roleBaseFormat = null;
        } else {
            roleBaseFormat = new MessageFormat ( roleBase );
        }
    }
    public String getRoleName() {
        return this.roleName;
    }
    public void setRoleName ( String roleName ) {
        this.roleName = roleName;
    }
    public String getRoleSearch() {
        return this.roleSearch;
    }
    public void setRoleSearch ( String roleSearch ) {
        this.roleSearch = roleSearch;
        if ( roleSearch == null ) {
            roleFormat = null;
        } else {
            roleFormat = new MessageFormat ( roleSearch );
        }
    }
    public boolean isRoleSearchAsUser() {
        return roleSearchAsUser;
    }
    public void setRoleSearchAsUser ( boolean roleSearchAsUser ) {
        this.roleSearchAsUser = roleSearchAsUser;
    }
    public boolean getRoleSubtree() {
        return this.roleSubtree;
    }
    public void setRoleSubtree ( boolean roleSubtree ) {
        this.roleSubtree = roleSubtree;
    }
    public boolean getRoleNested() {
        return this.roleNested;
    }
    public void setRoleNested ( boolean roleNested ) {
        this.roleNested = roleNested;
    }
    public String getUserPassword() {
        return this.userPassword;
    }
    public void setUserPassword ( String userPassword ) {
        this.userPassword = userPassword;
    }
    public String getUserRoleAttribute() {
        return userRoleAttribute;
    }
    public void setUserRoleAttribute ( String userRoleAttribute ) {
        this.userRoleAttribute = userRoleAttribute;
    }
    public String getUserPattern() {
        return this.userPattern;
    }
    public void setUserPattern ( String userPattern ) {
        this.userPattern = userPattern;
        if ( userPattern == null ) {
            userPatternArray = null;
        } else {
            userPatternArray = parseUserPatternString ( userPattern );
            int len = this.userPatternArray.length;
            userPatternFormatArray = new MessageFormat[len];
            for ( int i = 0; i < len; i++ ) {
                userPatternFormatArray[i] =
                    new MessageFormat ( userPatternArray[i] );
            }
        }
    }
    public String getAlternateURL() {
        return this.alternateURL;
    }
    public void setAlternateURL ( String alternateURL ) {
        this.alternateURL = alternateURL;
    }
    public String getCommonRole() {
        return commonRole;
    }
    public void setCommonRole ( String commonRole ) {
        this.commonRole = commonRole;
    }
    public String getConnectionTimeout() {
        return connectionTimeout;
    }
    public void setConnectionTimeout ( String timeout ) {
        this.connectionTimeout = timeout;
    }
    public long getSizeLimit() {
        return sizeLimit;
    }
    public void setSizeLimit ( long sizeLimit ) {
        this.sizeLimit = sizeLimit;
    }
    public int getTimeLimit() {
        return timeLimit;
    }
    public void setTimeLimit ( int timeLimit ) {
        this.timeLimit = timeLimit;
    }
    public boolean isUseDelegatedCredential() {
        return useDelegatedCredential;
    }
    public void setUseDelegatedCredential ( boolean useDelegatedCredential ) {
        this.useDelegatedCredential = useDelegatedCredential;
    }
    public String getSpnegoDelegationQop() {
        return spnegoDelegationQop;
    }
    public void setSpnegoDelegationQop ( String spnegoDelegationQop ) {
        this.spnegoDelegationQop = spnegoDelegationQop;
    }
    public boolean getUseStartTls() {
        return useStartTls;
    }
    public void setUseStartTls ( boolean useStartTls ) {
        this.useStartTls = useStartTls;
    }
    private String[] getCipherSuitesArray() {
        if ( cipherSuites == null || cipherSuitesArray != null ) {
            return cipherSuitesArray;
        }
        if ( this.cipherSuites.trim().isEmpty() ) {
            containerLog.warn ( sm.getString ( "jndiRealm.emptyCipherSuites" ) );
            this.cipherSuitesArray = null;
        } else {
            this.cipherSuitesArray = cipherSuites.trim().split ( "\\s*,\\s*" );
            containerLog.debug ( sm.getString ( "jndiRealm.cipherSuites",
                                                Arrays.toString ( this.cipherSuitesArray ) ) );
        }
        return this.cipherSuitesArray;
    }
    public void setCipherSuites ( String suites ) {
        this.cipherSuites = suites;
    }
    public String getHostnameVerifierClassName() {
        if ( this.hostnameVerifier == null ) {
            return "";
        }
        return this.hostnameVerifier.getClass().getCanonicalName();
    }
    public void setHostnameVerifierClassName ( String verifierClassName ) {
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
        if ( this.hostNameVerifierClassName == null
                || hostNameVerifierClassName.equals ( "" ) ) {
            return null;
        }
        try {
            Object o = constructInstance ( hostNameVerifierClassName );
            if ( o instanceof HostnameVerifier ) {
                this.hostnameVerifier = ( HostnameVerifier ) o;
                return this.hostnameVerifier;
            } else {
                throw new IllegalArgumentException ( sm.getString (
                        "jndiRealm.invalidHostnameVerifier",
                        hostNameVerifierClassName ) );
            }
        } catch ( ClassNotFoundException | SecurityException
                      | InstantiationException | IllegalAccessException e ) {
            throw new IllegalArgumentException ( sm.getString (
                    "jndiRealm.invalidHostnameVerifier",
                    hostNameVerifierClassName ), e );
        }
    }
    public void setSslSocketFactoryClassName ( String factoryClassName ) {
        this.sslSocketFactoryClassName = factoryClassName;
    }
    public void setSslProtocol ( String protocol ) {
        this.sslProtocol = protocol;
    }
    private String[] getSupportedSslProtocols() {
        try {
            SSLContext sslContext = SSLContext.getDefault();
            return sslContext.getSupportedSSLParameters().getProtocols();
        } catch ( NoSuchAlgorithmException e ) {
            throw new RuntimeException ( sm.getString ( "jndiRealm.exception" ), e );
        }
    }
    private Object constructInstance ( String className )
    throws ClassNotFoundException, InstantiationException,
        IllegalAccessException {
        Class<?> clazz = Class.forName ( className );
        return clazz.newInstance();
    }
    @Override
    public Principal authenticate ( String username, String credentials ) {
        DirContext context = null;
        Principal principal = null;
        try {
            context = open();
            try {
                principal = authenticate ( context, username, credentials );
            } catch ( NullPointerException | CommunicationException
                          | ServiceUnavailableException e ) {
                containerLog.info ( sm.getString ( "jndiRealm.exception.retry" ), e );
                if ( context != null ) {
                    close ( context );
                }
                context = open();
                principal = authenticate ( context, username, credentials );
            }
            release ( context );
            return principal;
        } catch ( NamingException e ) {
            containerLog.error ( sm.getString ( "jndiRealm.exception" ), e );
            if ( context != null ) {
                close ( context );
            }
            if ( containerLog.isDebugEnabled() ) {
                containerLog.debug ( "Returning null principal." );
            }
            return null;
        }
    }
    public synchronized Principal authenticate ( DirContext context,
            String username,
            String credentials )
    throws NamingException {
        if ( username == null || username.equals ( "" )
                || credentials == null || credentials.equals ( "" ) ) {
            if ( containerLog.isDebugEnabled() ) {
                containerLog.debug ( "username null or empty: returning null principal." );
            }
            return null;
        }
        if ( userPatternArray != null ) {
            for ( int curUserPattern = 0;
                    curUserPattern < userPatternFormatArray.length;
                    curUserPattern++ ) {
                User user = getUser ( context, username, credentials, curUserPattern );
                if ( user != null ) {
                    try {
                        if ( checkCredentials ( context, user, credentials ) ) {
                            List<String> roles = getRoles ( context, user );
                            if ( containerLog.isDebugEnabled() ) {
                                containerLog.debug ( "Found roles: " + roles.toString() );
                            }
                            return ( new GenericPrincipal ( username, credentials, roles ) );
                        }
                    } catch ( InvalidNameException ine ) {
                        containerLog.warn ( sm.getString ( "jndiRealm.exception" ), ine );
                    }
                }
            }
            return null;
        } else {
            User user = getUser ( context, username, credentials );
            if ( user == null ) {
                return null;
            }
            if ( !checkCredentials ( context, user, credentials ) ) {
                return null;
            }
            List<String> roles = getRoles ( context, user );
            if ( containerLog.isDebugEnabled() ) {
                containerLog.debug ( "Found roles: " + roles.toString() );
            }
            return new GenericPrincipal ( username, credentials, roles );
        }
    }
    protected User getUser ( DirContext context, String username )
    throws NamingException {
        return getUser ( context, username, null, -1 );
    }
    protected User getUser ( DirContext context, String username, String credentials )
    throws NamingException {
        return getUser ( context, username, credentials, -1 );
    }
    protected User getUser ( DirContext context, String username,
                             String credentials, int curUserPattern )
    throws NamingException {
        User user = null;
        ArrayList<String> list = new ArrayList<>();
        if ( userPassword != null ) {
            list.add ( userPassword );
        }
        if ( userRoleName != null ) {
            list.add ( userRoleName );
        }
        if ( userRoleAttribute != null ) {
            list.add ( userRoleAttribute );
        }
        String[] attrIds = new String[list.size()];
        list.toArray ( attrIds );
        if ( userPatternFormatArray != null && curUserPattern >= 0 ) {
            user = getUserByPattern ( context, username, credentials, attrIds, curUserPattern );
            if ( containerLog.isDebugEnabled() ) {
                containerLog.debug ( "Found user by pattern [" + user + "]" );
            }
        } else {
            boolean thisUserSearchAsUser = isUserSearchAsUser();
            try {
                if ( thisUserSearchAsUser ) {
                    userCredentialsAdd ( context, username, credentials );
                }
                user = getUserBySearch ( context, username, attrIds );
            } finally {
                if ( thisUserSearchAsUser ) {
                    userCredentialsRemove ( context );
                }
            }
            if ( containerLog.isDebugEnabled() ) {
                containerLog.debug ( "Found user by search [" + user + "]" );
            }
        }
        if ( userPassword == null && credentials != null && user != null ) {
            return new User ( user.getUserName(), user.getDN(), credentials,
                              user.getRoles(), user.getUserRoleId() );
        }
        return user;
    }
    protected User getUserByPattern ( DirContext context,
                                      String username,
                                      String[] attrIds,
                                      String dn )
    throws NamingException {
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
        if ( userPassword != null ) {
            password = getAttributeValue ( userPassword, attrs );
        }
        String userRoleAttrValue = null;
        if ( userRoleAttribute != null ) {
            userRoleAttrValue = getAttributeValue ( userRoleAttribute, attrs );
        }
        ArrayList<String> roles = null;
        if ( userRoleName != null ) {
            roles = addAttributeValues ( userRoleName, attrs, roles );
        }
        return new User ( username, dn, password, roles, userRoleAttrValue );
    }
    protected User getUserByPattern ( DirContext context,
                                      String username,
                                      String credentials,
                                      String[] attrIds,
                                      int curUserPattern )
    throws NamingException {
        User user = null;
        if ( username == null || userPatternFormatArray[curUserPattern] == null ) {
            return null;
        }
        String dn = userPatternFormatArray[curUserPattern].format ( new String[] { username } );
        try {
            user = getUserByPattern ( context, username, attrIds, dn );
        } catch ( NameNotFoundException e ) {
            return null;
        } catch ( NamingException e ) {
            try {
                userCredentialsAdd ( context, dn, credentials );
                user = getUserByPattern ( context, username, attrIds, dn );
            } finally {
                userCredentialsRemove ( context );
            }
        }
        return user;
    }
    protected User getUserBySearch ( DirContext context,
                                     String username,
                                     String[] attrIds )
    throws NamingException {
        if ( username == null || userSearchFormat == null ) {
            return null;
        }
        String filter = userSearchFormat.format ( new String[] { username } );
        SearchControls constraints = new SearchControls();
        if ( userSubtree ) {
            constraints.setSearchScope ( SearchControls.SUBTREE_SCOPE );
        } else {
            constraints.setSearchScope ( SearchControls.ONELEVEL_SCOPE );
        }
        constraints.setCountLimit ( sizeLimit );
        constraints.setTimeLimit ( timeLimit );
        if ( attrIds == null ) {
            attrIds = new String[0];
        }
        constraints.setReturningAttributes ( attrIds );
        NamingEnumeration<SearchResult> results =
            context.search ( userBase, filter, constraints );
        try {
            try {
                if ( results == null || !results.hasMore() ) {
                    return null;
                }
            } catch ( PartialResultException ex ) {
                if ( !adCompat ) {
                    throw ex;
                } else {
                    return null;
                }
            }
            SearchResult result = results.next();
            try {
                if ( results.hasMore() ) {
                    if ( containerLog.isInfoEnabled() ) {
                        containerLog.info ( "username " + username + " has multiple entries" );
                    }
                    return null;
                }
            } catch ( PartialResultException ex ) {
                if ( !adCompat ) {
                    throw ex;
                }
            }
            String dn = getDistinguishedName ( context, userBase, result );
            if ( containerLog.isTraceEnabled() ) {
                containerLog.trace ( "  entry found for " + username + " with dn " + dn );
            }
            Attributes attrs = result.getAttributes();
            if ( attrs == null ) {
                return null;
            }
            String password = null;
            if ( userPassword != null ) {
                password = getAttributeValue ( userPassword, attrs );
            }
            String userRoleAttrValue = null;
            if ( userRoleAttribute != null ) {
                userRoleAttrValue = getAttributeValue ( userRoleAttribute, attrs );
            }
            ArrayList<String> roles = null;
            if ( userRoleName != null ) {
                roles = addAttributeValues ( userRoleName, attrs, roles );
            }
            return new User ( username, dn, password, roles, userRoleAttrValue );
        } finally {
            if ( results != null ) {
                results.close();
            }
        }
    }
    protected boolean checkCredentials ( DirContext context,
                                         User user,
                                         String credentials )
    throws NamingException {
        boolean validated = false;
        if ( userPassword == null ) {
            validated = bindAsUser ( context, user, credentials );
        } else {
            validated = compareCredentials ( context, user, credentials );
        }
        if ( containerLog.isTraceEnabled() ) {
            if ( validated ) {
                containerLog.trace ( sm.getString ( "jndiRealm.authenticateSuccess",
                                                    user.getUserName() ) );
            } else {
                containerLog.trace ( sm.getString ( "jndiRealm.authenticateFailure",
                                                    user.getUserName() ) );
            }
        }
        return validated;
    }
    protected boolean compareCredentials ( DirContext context,
                                           User info,
                                           String credentials )
    throws NamingException {
        if ( containerLog.isTraceEnabled() ) {
            containerLog.trace ( "  validating credentials" );
        }
        if ( info == null || credentials == null ) {
            return false;
        }
        String password = info.getPassword();
        return getCredentialHandler().matches ( credentials, password );
    }
    protected boolean bindAsUser ( DirContext context,
                                   User user,
                                   String credentials )
    throws NamingException {
        if ( credentials == null || user == null ) {
            return false;
        }
        String dn = user.getDN();
        if ( dn == null ) {
            return false;
        }
        if ( containerLog.isTraceEnabled() ) {
            containerLog.trace ( "  validating credentials by binding as the user" );
        }
        userCredentialsAdd ( context, dn, credentials );
        boolean validated = false;
        try {
            if ( containerLog.isTraceEnabled() ) {
                containerLog.trace ( "  binding as "  + dn );
            }
            context.getAttributes ( "", null );
            validated = true;
        } catch ( AuthenticationException e ) {
            if ( containerLog.isTraceEnabled() ) {
                containerLog.trace ( "  bind attempt failed" );
            }
        }
        userCredentialsRemove ( context );
        return validated;
    }
    private void userCredentialsAdd ( DirContext context, String dn,
                                      String credentials ) throws NamingException {
        context.addToEnvironment ( Context.SECURITY_PRINCIPAL, dn );
        context.addToEnvironment ( Context.SECURITY_CREDENTIALS, credentials );
    }
    private void userCredentialsRemove ( DirContext context )
    throws NamingException {
        if ( connectionName != null ) {
            context.addToEnvironment ( Context.SECURITY_PRINCIPAL,
                                       connectionName );
        } else {
            context.removeFromEnvironment ( Context.SECURITY_PRINCIPAL );
        }
        if ( connectionPassword != null ) {
            context.addToEnvironment ( Context.SECURITY_CREDENTIALS,
                                       connectionPassword );
        } else {
            context.removeFromEnvironment ( Context.SECURITY_CREDENTIALS );
        }
    }
    protected List<String> getRoles ( DirContext context, User user )
    throws NamingException {
        if ( user == null ) {
            return null;
        }
        String dn = user.getDN();
        String username = user.getUserName();
        String userRoleId = user.getUserRoleId();
        if ( dn == null || username == null ) {
            return null;
        }
        if ( containerLog.isTraceEnabled() ) {
            containerLog.trace ( "  getRoles(" + dn + ")" );
        }
        List<String> list = new ArrayList<>();
        List<String> userRoles = user.getRoles();
        if ( userRoles != null ) {
            list.addAll ( userRoles );
        }
        if ( commonRole != null ) {
            list.add ( commonRole );
        }
        if ( containerLog.isTraceEnabled() ) {
            containerLog.trace ( "  Found " + list.size() + " user internal roles" );
            containerLog.trace ( "  Found user internal roles " + list.toString() );
        }
        if ( ( roleFormat == null ) || ( roleName == null ) ) {
            return list;
        }
        String filter = roleFormat.format ( new String[] { doRFC2254Encoding ( dn ), username, userRoleId } );
        SearchControls controls = new SearchControls();
        if ( roleSubtree ) {
            controls.setSearchScope ( SearchControls.SUBTREE_SCOPE );
        } else {
            controls.setSearchScope ( SearchControls.ONELEVEL_SCOPE );
        }
        controls.setReturningAttributes ( new String[] {roleName} );
        String base = null;
        if ( roleBaseFormat != null ) {
            NameParser np = context.getNameParser ( "" );
            Name name = np.parse ( dn );
            String nameParts[] = new String[name.size()];
            for ( int i = 0; i < name.size(); i++ ) {
                nameParts[i] = name.get ( i );
            }
            base = roleBaseFormat.format ( nameParts );
        } else {
            base = "";
        }
        NamingEnumeration<SearchResult> results = searchAsUser ( context, user, base, filter, controls,
                isRoleSearchAsUser() );
        if ( results == null ) {
            return list;
        }
        HashMap<String, String> groupMap = new HashMap<>();
        try {
            while ( results.hasMore() ) {
                SearchResult result = results.next();
                Attributes attrs = result.getAttributes();
                if ( attrs == null ) {
                    continue;
                }
                String dname = getDistinguishedName ( context, roleBase, result );
                String name = getAttributeValue ( roleName, attrs );
                if ( name != null && dname != null ) {
                    groupMap.put ( dname, name );
                }
            }
        } catch ( PartialResultException ex ) {
            if ( !adCompat ) {
                throw ex;
            }
        } finally {
            results.close();
        }
        if ( containerLog.isTraceEnabled() ) {
            Set<Entry<String, String>> entries = groupMap.entrySet();
            containerLog.trace ( "  Found " + entries.size() + " direct roles" );
            for ( Entry<String, String> entry : entries ) {
                containerLog.trace ( "  Found direct role " + entry.getKey() + " -> " + entry.getValue() );
            }
        }
        if ( getRoleNested() ) {
            Map<String, String> newGroups = new HashMap<> ( groupMap );
            while ( !newGroups.isEmpty() ) {
                Map<String, String> newThisRound = new HashMap<>();
                for ( Entry<String, String> group : newGroups.entrySet() ) {
                    filter = roleFormat.format ( new String[] { group.getKey(), group.getValue(), group.getValue() } );
                    if ( containerLog.isTraceEnabled() ) {
                        containerLog.trace ( "Perform a nested group search with base " + roleBase + " and filter " + filter );
                    }
                    results = searchAsUser ( context, user, roleBase, filter, controls,
                                             isRoleSearchAsUser() );
                    try {
                        while ( results.hasMore() ) {
                            SearchResult result = results.next();
                            Attributes attrs = result.getAttributes();
                            if ( attrs == null ) {
                                continue;
                            }
                            String dname = getDistinguishedName ( context, roleBase, result );
                            String name = getAttributeValue ( roleName, attrs );
                            if ( name != null && dname != null && !groupMap.keySet().contains ( dname ) ) {
                                groupMap.put ( dname, name );
                                newThisRound.put ( dname, name );
                                if ( containerLog.isTraceEnabled() ) {
                                    containerLog.trace ( "  Found nested role " + dname + " -> " + name );
                                }
                            }
                        }
                    } catch ( PartialResultException ex ) {
                        if ( !adCompat ) {
                            throw ex;
                        }
                    } finally {
                        results.close();
                    }
                }
                newGroups = newThisRound;
            }
        }
        list.addAll ( groupMap.values() );
        return list;
    }
    private NamingEnumeration<SearchResult> searchAsUser ( DirContext context,
            User user, String base, String filter,
            SearchControls controls, boolean searchAsUser ) throws NamingException {
        NamingEnumeration<SearchResult> results;
        try {
            if ( searchAsUser ) {
                userCredentialsAdd ( context, user.getDN(), user.getPassword() );
            }
            results = context.search ( base, filter, controls );
        } finally {
            if ( searchAsUser ) {
                userCredentialsRemove ( context );
            }
        }
        return results;
    }
    private String getAttributeValue ( String attrId, Attributes attrs )
    throws NamingException {
        if ( containerLog.isTraceEnabled() ) {
            containerLog.trace ( "  retrieving attribute " + attrId );
        }
        if ( attrId == null || attrs == null ) {
            return null;
        }
        Attribute attr = attrs.get ( attrId );
        if ( attr == null ) {
            return null;
        }
        Object value = attr.get();
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
    private ArrayList<String> addAttributeValues ( String attrId,
            Attributes attrs,
            ArrayList<String> values )
    throws NamingException {
        if ( containerLog.isTraceEnabled() ) {
            containerLog.trace ( "  retrieving values for attribute " + attrId );
        }
        if ( attrId == null || attrs == null ) {
            return values;
        }
        if ( values == null ) {
            values = new ArrayList<>();
        }
        Attribute attr = attrs.get ( attrId );
        if ( attr == null ) {
            return values;
        }
        NamingEnumeration<?> e = attr.getAll();
        try {
            while ( e.hasMore() ) {
                String value = ( String ) e.next();
                values.add ( value );
            }
        } catch ( PartialResultException ex ) {
            if ( !adCompat ) {
                throw ex;
            }
        } finally {
            e.close();
        }
        return values;
    }
    protected void close ( DirContext context ) {
        if ( context == null ) {
            return;
        }
        if ( tls != null ) {
            try {
                tls.close();
            } catch ( IOException e ) {
                containerLog.error ( sm.getString ( "jndiRealm.tlsClose" ), e );
            }
        }
        try {
            if ( containerLog.isDebugEnabled() ) {
                containerLog.debug ( "Closing directory context" );
            }
            context.close();
        } catch ( NamingException e ) {
            containerLog.error ( sm.getString ( "jndiRealm.close" ), e );
        }
        this.context = null;
    }
    @Override
    protected String getName() {
        return name;
    }
    @Override
    protected String getPassword ( String username ) {
        String userPassword = getUserPassword();
        if ( userPassword == null || userPassword.isEmpty() ) {
            return null;
        }
        try {
            User user = getUser ( open(), username, null );
            if ( user == null ) {
                return null;
            } else {
                return user.getPassword();
            }
        } catch ( NamingException e ) {
            return null;
        }
    }
    @Override
    protected Principal getPrincipal ( String username ) {
        return getPrincipal ( username, null );
    }
    @Override
    protected Principal getPrincipal ( String username,
                                       GSSCredential gssCredential ) {
        DirContext context = null;
        Principal principal = null;
        try {
            context = open();
            try {
                principal = getPrincipal ( context, username, gssCredential );
            } catch ( CommunicationException | ServiceUnavailableException e ) {
                containerLog.info ( sm.getString ( "jndiRealm.exception.retry" ), e );
                if ( context != null ) {
                    close ( context );
                }
                context = open();
                principal = getPrincipal ( context, username, gssCredential );
            }
            release ( context );
            return principal;
        } catch ( NamingException e ) {
            containerLog.error ( sm.getString ( "jndiRealm.exception" ), e );
            if ( context != null ) {
                close ( context );
            }
            return null;
        }
    }
    protected synchronized Principal getPrincipal ( DirContext context,
            String username, GSSCredential gssCredential )
    throws NamingException {
        User user = null;
        List<String> roles = null;
        Hashtable<?, ?> preservedEnvironment = null;
        try {
            if ( gssCredential != null && isUseDelegatedCredential() ) {
                preservedEnvironment = context.getEnvironment();
                context.addToEnvironment (
                    Context.SECURITY_AUTHENTICATION, "GSSAPI" );
                context.addToEnvironment (
                    "javax.security.sasl.server.authentication", "true" );
                context.addToEnvironment (
                    "javax.security.sasl.qop", spnegoDelegationQop );
            }
            user = getUser ( context, username );
            if ( user != null ) {
                roles = getRoles ( context, user );
            }
        } finally {
            restoreEnvironmentParameter ( context,
                                          Context.SECURITY_AUTHENTICATION, preservedEnvironment );
            restoreEnvironmentParameter ( context,
                                          "javax.security.sasl.server.authentication", preservedEnvironment );
            restoreEnvironmentParameter ( context, "javax.security.sasl.qop",
                                          preservedEnvironment );
        }
        if ( user != null ) {
            return new GenericPrincipal ( user.getUserName(), user.getPassword(),
                                          roles, null, null, gssCredential );
        }
        return null;
    }
    private void restoreEnvironmentParameter ( DirContext context,
            String parameterName, Hashtable<?, ?> preservedEnvironment ) {
        try {
            context.removeFromEnvironment ( parameterName );
            if ( preservedEnvironment != null && preservedEnvironment.containsKey ( parameterName ) ) {
                context.addToEnvironment ( parameterName,
                                           preservedEnvironment.get ( parameterName ) );
            }
        } catch ( NamingException e ) {
        }
    }
    protected DirContext open() throws NamingException {
        if ( context != null ) {
            return context;
        }
        try {
            context = createDirContext ( getDirectoryContextEnvironment() );
        } catch ( Exception e ) {
            connectionAttempt = 1;
            containerLog.info ( sm.getString ( "jndiRealm.exception.retry" ), e );
            context = createDirContext ( getDirectoryContextEnvironment() );
        } finally {
            connectionAttempt = 0;
        }
        return context;
    }
    @Override
    public boolean isAvailable() {
        return ( context != null );
    }
    private DirContext createDirContext ( Hashtable<String, String> env ) throws NamingException {
        if ( useStartTls ) {
            return createTlsDirContext ( env );
        } else {
            return new InitialDirContext ( env );
        }
    }
    private SSLSocketFactory getSSLSocketFactory() {
        if ( sslSocketFactory != null ) {
            return sslSocketFactory;
        }
        final SSLSocketFactory result;
        if ( this.sslSocketFactoryClassName != null
                && !sslSocketFactoryClassName.trim().equals ( "" ) ) {
            result = createSSLSocketFactoryFromClassName ( this.sslSocketFactoryClassName );
        } else {
            result = createSSLContextFactoryFromProtocol ( sslProtocol );
        }
        this.sslSocketFactory = result;
        return result;
    }
    private SSLSocketFactory createSSLSocketFactoryFromClassName ( String className ) {
        try {
            Object o = constructInstance ( className );
            if ( o instanceof SSLSocketFactory ) {
                return sslSocketFactory;
            } else {
                throw new IllegalArgumentException ( sm.getString (
                        "jndiRealm.invalidSslSocketFactory",
                        className ) );
            }
        } catch ( ClassNotFoundException | SecurityException
                      | InstantiationException | IllegalAccessException e ) {
            throw new IllegalArgumentException ( sm.getString (
                    "jndiRealm.invalidSslSocketFactory",
                    className ), e );
        }
    }
    private SSLSocketFactory createSSLContextFactoryFromProtocol ( String protocol ) {
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
            List<String> allowedProtocols = Arrays
                                            .asList ( getSupportedSslProtocols() );
            throw new IllegalArgumentException (
                sm.getString ( "jndiRealm.invalidSslProtocol", protocol,
                               allowedProtocols ), e );
        }
    }
    private DirContext createTlsDirContext (
        Hashtable<String, String> env ) throws NamingException {
        Map<String, Object> savedEnv = new HashMap<>();
        for ( String key : Arrays.asList ( Context.SECURITY_AUTHENTICATION,
                                           Context.SECURITY_CREDENTIALS, Context.SECURITY_PRINCIPAL,
                                           Context.SECURITY_PROTOCOL ) ) {
            Object entry = env.remove ( key );
            if ( entry != null ) {
                savedEnv.put ( key, entry );
            }
        }
        LdapContext result = null;
        try {
            result = new InitialLdapContext ( env, null );
            tls = ( StartTlsResponse ) result
                  .extendedOperation ( new StartTlsRequest() );
            if ( getHostnameVerifier() != null ) {
                tls.setHostnameVerifier ( getHostnameVerifier() );
            }
            if ( getCipherSuitesArray() != null ) {
                tls.setEnabledCipherSuites ( getCipherSuitesArray() );
            }
            try {
                SSLSession negotiate = tls.negotiate ( getSSLSocketFactory() );
                containerLog.debug ( sm.getString ( "jndiRealm.negotiatedTls",
                                                    negotiate.getProtocol() ) );
            } catch ( IOException e ) {
                throw new NamingException ( e.getMessage() );
            }
        } finally {
            if ( result != null ) {
                for ( Map.Entry<String, Object> savedEntry : savedEnv.entrySet() ) {
                    result.addToEnvironment ( savedEntry.getKey(),
                                              savedEntry.getValue() );
                }
            }
        }
        return result;
    }
    protected Hashtable<String, String> getDirectoryContextEnvironment() {
        Hashtable<String, String> env = new Hashtable<>();
        if ( containerLog.isDebugEnabled() && connectionAttempt == 0 ) {
            containerLog.debug ( "Connecting to URL " + connectionURL );
        } else if ( containerLog.isDebugEnabled() && connectionAttempt > 0 ) {
            containerLog.debug ( "Connecting to URL " + alternateURL );
        }
        env.put ( Context.INITIAL_CONTEXT_FACTORY, contextFactory );
        if ( connectionName != null ) {
            env.put ( Context.SECURITY_PRINCIPAL, connectionName );
        }
        if ( connectionPassword != null ) {
            env.put ( Context.SECURITY_CREDENTIALS, connectionPassword );
        }
        if ( connectionURL != null && connectionAttempt == 0 ) {
            env.put ( Context.PROVIDER_URL, connectionURL );
        } else if ( alternateURL != null && connectionAttempt > 0 ) {
            env.put ( Context.PROVIDER_URL, alternateURL );
        }
        if ( authentication != null ) {
            env.put ( Context.SECURITY_AUTHENTICATION, authentication );
        }
        if ( protocol != null ) {
            env.put ( Context.SECURITY_PROTOCOL, protocol );
        }
        if ( referrals != null ) {
            env.put ( Context.REFERRAL, referrals );
        }
        if ( derefAliases != null ) {
            env.put ( JNDIRealm.DEREF_ALIASES, derefAliases );
        }
        if ( connectionTimeout != null ) {
            env.put ( "com.sun.jndi.ldap.connect.timeout", connectionTimeout );
        }
        return env;
    }
    protected void release ( DirContext context ) {
    }
    @Override
    protected void startInternal() throws LifecycleException {
        try {
            open();
        } catch ( NamingException e ) {
            containerLog.error ( sm.getString ( "jndiRealm.open" ), e );
        }
        super.startInternal();
    }
    @Override
    protected void stopInternal() throws LifecycleException {
        super.stopInternal();
        close ( this.context );
    }
    protected String[] parseUserPatternString ( String userPatternString ) {
        if ( userPatternString != null ) {
            ArrayList<String> pathList = new ArrayList<>();
            int startParenLoc = userPatternString.indexOf ( '(' );
            if ( startParenLoc == -1 ) {
                return new String[] {userPatternString};
            }
            int startingPoint = 0;
            while ( startParenLoc > -1 ) {
                int endParenLoc = 0;
                while ( ( userPatternString.charAt ( startParenLoc + 1 ) == '|' ) ||
                        ( startParenLoc != 0 && userPatternString.charAt ( startParenLoc - 1 ) == '\\' ) ) {
                    startParenLoc = userPatternString.indexOf ( '(', startParenLoc + 1 );
                }
                endParenLoc = userPatternString.indexOf ( ')', startParenLoc + 1 );
                while ( userPatternString.charAt ( endParenLoc - 1 ) == '\\' ) {
                    endParenLoc = userPatternString.indexOf ( ')', endParenLoc + 1 );
                }
                String nextPathPart = userPatternString.substring
                                      ( startParenLoc + 1, endParenLoc );
                pathList.add ( nextPathPart );
                startingPoint = endParenLoc + 1;
                startParenLoc = userPatternString.indexOf ( '(', startingPoint );
            }
            return pathList.toArray ( new String[] {} );
        }
        return null;
    }
    protected String doRFC2254Encoding ( String inString ) {
        StringBuilder buf = new StringBuilder ( inString.length() );
        for ( int i = 0; i < inString.length(); i++ ) {
            char c = inString.charAt ( i );
            switch ( c ) {
            case '\\':
                buf.append ( "\\5c" );
                break;
            case '*':
                buf.append ( "\\2a" );
                break;
            case '(':
                buf.append ( "\\28" );
                break;
            case ')':
                buf.append ( "\\29" );
                break;
            case '\0':
                buf.append ( "\\00" );
                break;
            default:
                buf.append ( c );
                break;
            }
        }
        return buf.toString();
    }
    protected String getDistinguishedName ( DirContext context, String base,
                                            SearchResult result ) throws NamingException {
        String resultName = result.getName();
        if ( result.isRelative() ) {
            if ( containerLog.isTraceEnabled() ) {
                containerLog.trace ( "  search returned relative name: " + resultName );
            }
            NameParser parser = context.getNameParser ( "" );
            Name contextName = parser.parse ( context.getNameInNamespace() );
            Name baseName = parser.parse ( base );
            Name entryName = parser.parse ( new CompositeName ( resultName ).get ( 0 ) );
            Name name = contextName.addAll ( baseName );
            name = name.addAll ( entryName );
            return name.toString();
        } else {
            if ( containerLog.isTraceEnabled() ) {
                containerLog.trace ( "  search returned absolute name: " + resultName );
            }
            try {
                NameParser parser = context.getNameParser ( "" );
                URI userNameUri = new URI ( resultName );
                String pathComponent = userNameUri.getPath();
                if ( pathComponent.length() < 1 ) {
                    throw new InvalidNameException (
                        "Search returned unparseable absolute name: " +
                        resultName );
                }
                Name name = parser.parse ( pathComponent.substring ( 1 ) );
                return name.toString();
            } catch ( URISyntaxException e ) {
                throw new InvalidNameException (
                    "Search returned unparseable absolute name: " +
                    resultName );
            }
        }
    }
    protected static class User {
        private final String username;
        private final String dn;
        private final String password;
        private final List<String> roles;
        private final String userRoleId;
        public User ( String username, String dn, String password,
                      List<String> roles, String userRoleId ) {
            this.username = username;
            this.dn = dn;
            this.password = password;
            if ( roles == null ) {
                this.roles = Collections.emptyList();
            } else {
                this.roles = Collections.unmodifiableList ( roles );
            }
            this.userRoleId = userRoleId;
        }
        public String getUserName() {
            return username;
        }
        public String getDN() {
            return dn;
        }
        public String getPassword() {
            return password;
        }
        public List<String> getRoles() {
            return roles;
        }
        public String getUserRoleId() {
            return userRoleId;
        }
    }
}
