package org.apache.catalina.realm;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.servlet.annotation.ServletSecurity.TransportGuarantee;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.CredentialHandler;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Realm;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.util.LifecycleMBeanBase;
import org.apache.catalina.util.SessionConfig;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.buf.B2CConverter;
import org.apache.tomcat.util.buf.HexUtils;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.security.ConcurrentMessageDigest;
import org.apache.tomcat.util.security.MD5Encoder;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSName;
public abstract class RealmBase extends LifecycleMBeanBase implements Realm {
    private static final Log log = LogFactory.getLog ( RealmBase.class );
    private static final List<Class<? extends DigestCredentialHandlerBase>> credentialHandlerClasses =
        new ArrayList<>();
    static {
        credentialHandlerClasses.add ( MessageDigestCredentialHandler.class );
        credentialHandlerClasses.add ( SecretKeyCredentialHandler.class );
    }
    protected Container container = null;
    protected Log containerLog = null;
    private CredentialHandler credentialHandler;
    protected static final StringManager sm = StringManager.getManager ( RealmBase.class );
    protected final PropertyChangeSupport support = new PropertyChangeSupport ( this );
    protected boolean validate = true;
    protected String x509UsernameRetrieverClassName;
    protected X509UsernameRetriever x509UsernameRetriever;
    protected AllRolesMode allRolesMode = AllRolesMode.STRICT_MODE;
    protected boolean stripRealmForGss = true;
    private int transportGuaranteeRedirectStatus = HttpServletResponse.SC_FOUND;
    public int getTransportGuaranteeRedirectStatus() {
        return transportGuaranteeRedirectStatus;
    }
    public void setTransportGuaranteeRedirectStatus ( int transportGuaranteeRedirectStatus ) {
        this.transportGuaranteeRedirectStatus = transportGuaranteeRedirectStatus;
    }
    @Override
    public CredentialHandler getCredentialHandler() {
        return credentialHandler;
    }
    @Override
    public void setCredentialHandler ( CredentialHandler credentialHandler ) {
        this.credentialHandler = credentialHandler;
    }
    @Override
    public Container getContainer() {
        return ( container );
    }
    @Override
    public void setContainer ( Container container ) {
        Container oldContainer = this.container;
        this.container = container;
        support.firePropertyChange ( "container", oldContainer, this.container );
    }
    public String getAllRolesMode() {
        return allRolesMode.toString();
    }
    public void setAllRolesMode ( String allRolesMode ) {
        this.allRolesMode = AllRolesMode.toMode ( allRolesMode );
    }
    public boolean getValidate() {
        return validate;
    }
    public void setValidate ( boolean validate ) {
        this.validate = validate;
    }
    public String getX509UsernameRetrieverClassName() {
        return x509UsernameRetrieverClassName;
    }
    public void setX509UsernameRetrieverClassName ( String className ) {
        this.x509UsernameRetrieverClassName = className;
    }
    public boolean isStripRealmForGss() {
        return stripRealmForGss;
    }
    public void setStripRealmForGss ( boolean stripRealmForGss ) {
        this.stripRealmForGss = stripRealmForGss;
    }
    @Override
    public void addPropertyChangeListener ( PropertyChangeListener listener ) {
        support.addPropertyChangeListener ( listener );
    }
    @Override
    public Principal authenticate ( String username ) {
        if ( username == null ) {
            return null;
        }
        if ( containerLog.isTraceEnabled() ) {
            containerLog.trace ( sm.getString ( "realmBase.authenticateSuccess", username ) );
        }
        return getPrincipal ( username );
    }
    @Override
    public Principal authenticate ( String username, String credentials ) {
        if ( username == null || credentials == null ) {
            if ( containerLog.isTraceEnabled() ) {
                containerLog.trace ( sm.getString ( "realmBase.authenticateFailure",
                                                    username ) );
            }
            return null;
        }
        String serverCredentials = getPassword ( username );
        if ( serverCredentials == null ) {
            getCredentialHandler().mutate ( credentials );
            if ( containerLog.isTraceEnabled() ) {
                containerLog.trace ( sm.getString ( "realmBase.authenticateFailure",
                                                    username ) );
            }
            return null;
        }
        boolean validated = getCredentialHandler().matches ( credentials, serverCredentials );
        if ( validated ) {
            if ( containerLog.isTraceEnabled() ) {
                containerLog.trace ( sm.getString ( "realmBase.authenticateSuccess",
                                                    username ) );
            }
            return getPrincipal ( username );
        } else {
            if ( containerLog.isTraceEnabled() ) {
                containerLog.trace ( sm.getString ( "realmBase.authenticateFailure",
                                                    username ) );
            }
            return null;
        }
    }
    @Override
    public Principal authenticate ( String username, String clientDigest,
                                    String nonce, String nc, String cnonce,
                                    String qop, String realm,
                                    String md5a2 ) {
        String md5a1 = getDigest ( username, realm );
        if ( md5a1 == null ) {
            return null;
        }
        md5a1 = md5a1.toLowerCase ( Locale.ENGLISH );
        String serverDigestValue;
        if ( qop == null ) {
            serverDigestValue = md5a1 + ":" + nonce + ":" + md5a2;
        } else {
            serverDigestValue = md5a1 + ":" + nonce + ":" + nc + ":" +
                                cnonce + ":" + qop + ":" + md5a2;
        }
        byte[] valueBytes = null;
        try {
            valueBytes = serverDigestValue.getBytes ( getDigestCharset() );
        } catch ( UnsupportedEncodingException uee ) {
            log.error ( "Illegal digestEncoding: " + getDigestEncoding(), uee );
            throw new IllegalArgumentException ( uee.getMessage() );
        }
        String serverDigest = MD5Encoder.encode ( ConcurrentMessageDigest.digestMD5 ( valueBytes ) );
        if ( log.isDebugEnabled() ) {
            log.debug ( "Digest : " + clientDigest + " Username:" + username
                        + " ClientSigest:" + clientDigest + " nonce:" + nonce
                        + " nc:" + nc + " cnonce:" + cnonce + " qop:" + qop
                        + " realm:" + realm + "md5a2:" + md5a2
                        + " Server digest:" + serverDigest );
        }
        if ( serverDigest.equals ( clientDigest ) ) {
            return getPrincipal ( username );
        }
        return null;
    }
    @Override
    public Principal authenticate ( X509Certificate certs[] ) {
        if ( ( certs == null ) || ( certs.length < 1 ) ) {
            return ( null );
        }
        if ( log.isDebugEnabled() ) {
            log.debug ( "Authenticating client certificate chain" );
        }
        if ( validate ) {
            for ( int i = 0; i < certs.length; i++ ) {
                if ( log.isDebugEnabled() )
                    log.debug ( " Checking validity for '" +
                                certs[i].getSubjectDN().getName() + "'" );
                try {
                    certs[i].checkValidity();
                } catch ( Exception e ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug ( "  Validity exception", e );
                    }
                    return ( null );
                }
            }
        }
        return ( getPrincipal ( certs[0] ) );
    }
    @Override
    public Principal authenticate ( GSSContext gssContext, boolean storeCred ) {
        if ( gssContext.isEstablished() ) {
            GSSName gssName = null;
            try {
                gssName = gssContext.getSrcName();
            } catch ( GSSException e ) {
                log.warn ( sm.getString ( "realmBase.gssNameFail" ), e );
            }
            if ( gssName != null ) {
                String name = gssName.toString();
                if ( isStripRealmForGss() ) {
                    int i = name.indexOf ( '@' );
                    if ( i > 0 ) {
                        name = name.substring ( 0, i );
                    }
                }
                GSSCredential gssCredential = null;
                if ( storeCred && gssContext.getCredDelegState() ) {
                    try {
                        gssCredential = gssContext.getDelegCred();
                    } catch ( GSSException e ) {
                        if ( log.isDebugEnabled() ) {
                            log.debug ( sm.getString (
                                            "realmBase.delegatedCredentialFail", name ),
                                        e );
                        }
                    }
                }
                return getPrincipal ( name, gssCredential );
            }
        }
        return null;
    }
    @Override
    public void backgroundProcess() {
    }
    @Override
    public SecurityConstraint [] findSecurityConstraints ( Request request,
            Context context ) {
        ArrayList<SecurityConstraint> results = null;
        SecurityConstraint constraints[] = context.findConstraints();
        if ( ( constraints == null ) || ( constraints.length == 0 ) ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( "  No applicable constraints defined" );
            }
            return ( null );
        }
        String uri = request.getRequestPathMB().toString();
        if ( uri == null ) {
            uri = "/";
        }
        String method = request.getMethod();
        int i;
        boolean found = false;
        for ( i = 0; i < constraints.length; i++ ) {
            SecurityCollection [] collection = constraints[i].findCollections();
            if ( collection == null ) {
                continue;
            }
            if ( log.isDebugEnabled() ) {
                log.debug ( "  Checking constraint '" + constraints[i] +
                            "' against " + method + " " + uri + " --> " +
                            constraints[i].included ( uri, method ) );
            }
            for ( int j = 0; j < collection.length; j++ ) {
                String [] patterns = collection[j].findPatterns();
                if ( patterns == null ) {
                    continue;
                }
                for ( int k = 0; k < patterns.length; k++ ) {
                    if ( uri.equals ( patterns[k] ) ) {
                        found = true;
                        if ( collection[j].findMethod ( method ) ) {
                            if ( results == null ) {
                                results = new ArrayList<>();
                            }
                            results.add ( constraints[i] );
                        }
                    }
                }
            }
        }
        if ( found ) {
            return resultsToArray ( results );
        }
        int longest = -1;
        for ( i = 0; i < constraints.length; i++ ) {
            SecurityCollection [] collection = constraints[i].findCollections();
            if ( collection == null ) {
                continue;
            }
            if ( log.isDebugEnabled() ) {
                log.debug ( "  Checking constraint '" + constraints[i] +
                            "' against " + method + " " + uri + " --> " +
                            constraints[i].included ( uri, method ) );
            }
            for ( int j = 0; j < collection.length; j++ ) {
                String [] patterns = collection[j].findPatterns();
                if ( patterns == null ) {
                    continue;
                }
                boolean matched = false;
                int length = -1;
                for ( int k = 0; k < patterns.length; k++ ) {
                    String pattern = patterns[k];
                    if ( pattern.startsWith ( "/" ) && pattern.endsWith ( "/*" ) &&
                            pattern.length() >= longest ) {
                        if ( pattern.length() == 2 ) {
                            matched = true;
                            length = pattern.length();
                        } else if ( pattern.regionMatches ( 0, uri, 0,
                                                            pattern.length() - 1 ) ||
                                    ( pattern.length() - 2 == uri.length() &&
                                      pattern.regionMatches ( 0, uri, 0,
                                                              pattern.length() - 2 ) ) ) {
                            matched = true;
                            length = pattern.length();
                        }
                    }
                }
                if ( matched ) {
                    if ( length > longest ) {
                        found = false;
                        if ( results != null ) {
                            results.clear();
                        }
                        longest = length;
                    }
                    if ( collection[j].findMethod ( method ) ) {
                        found = true;
                        if ( results == null ) {
                            results = new ArrayList<>();
                        }
                        results.add ( constraints[i] );
                    }
                }
            }
        }
        if ( found ) {
            return  resultsToArray ( results );
        }
        for ( i = 0; i < constraints.length; i++ ) {
            SecurityCollection [] collection = constraints[i].findCollections();
            if ( collection == null ) {
                continue;
            }
            if ( log.isDebugEnabled() ) {
                log.debug ( "  Checking constraint '" + constraints[i] +
                            "' against " + method + " " + uri + " --> " +
                            constraints[i].included ( uri, method ) );
            }
            boolean matched = false;
            int pos = -1;
            for ( int j = 0; j < collection.length; j++ ) {
                String [] patterns = collection[j].findPatterns();
                if ( patterns == null ) {
                    continue;
                }
                for ( int k = 0; k < patterns.length && !matched; k++ ) {
                    String pattern = patterns[k];
                    if ( pattern.startsWith ( "*." ) ) {
                        int slash = uri.lastIndexOf ( '/' );
                        int dot = uri.lastIndexOf ( '.' );
                        if ( slash >= 0 && dot > slash &&
                                dot != uri.length() - 1 &&
                                uri.length() - dot == pattern.length() - 1 ) {
                            if ( pattern.regionMatches ( 1, uri, dot, uri.length() - dot ) ) {
                                matched = true;
                                pos = j;
                            }
                        }
                    }
                }
            }
            if ( matched ) {
                found = true;
                if ( collection[pos].findMethod ( method ) ) {
                    if ( results == null ) {
                        results = new ArrayList<>();
                    }
                    results.add ( constraints[i] );
                }
            }
        }
        if ( found ) {
            return resultsToArray ( results );
        }
        for ( i = 0; i < constraints.length; i++ ) {
            SecurityCollection [] collection = constraints[i].findCollections();
            if ( collection == null ) {
                continue;
            }
            if ( log.isDebugEnabled() ) {
                log.debug ( "  Checking constraint '" + constraints[i] +
                            "' against " + method + " " + uri + " --> " +
                            constraints[i].included ( uri, method ) );
            }
            for ( int j = 0; j < collection.length; j++ ) {
                String [] patterns = collection[j].findPatterns();
                if ( patterns == null ) {
                    continue;
                }
                boolean matched = false;
                for ( int k = 0; k < patterns.length && !matched; k++ ) {
                    String pattern = patterns[k];
                    if ( pattern.equals ( "/" ) ) {
                        matched = true;
                    }
                }
                if ( matched ) {
                    if ( results == null ) {
                        results = new ArrayList<>();
                    }
                    results.add ( constraints[i] );
                }
            }
        }
        if ( results == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( "  No applicable constraint located" );
            }
        }
        return resultsToArray ( results );
    }
    private SecurityConstraint [] resultsToArray (
        ArrayList<SecurityConstraint> results ) {
        if ( results == null || results.size() == 0 ) {
            return null;
        }
        SecurityConstraint [] array = new SecurityConstraint[results.size()];
        results.toArray ( array );
        return array;
    }
    @Override
    public boolean hasResourcePermission ( Request request,
                                           Response response,
                                           SecurityConstraint []constraints,
                                           Context context )
    throws IOException {
        if ( constraints == null || constraints.length == 0 ) {
            return true;
        }
        Principal principal = request.getPrincipal();
        boolean status = false;
        boolean denyfromall = false;
        for ( int i = 0; i < constraints.length; i++ ) {
            SecurityConstraint constraint = constraints[i];
            String roles[];
            if ( constraint.getAllRoles() ) {
                roles = request.getContext().findSecurityRoles();
            } else {
                roles = constraint.findAuthRoles();
            }
            if ( roles == null ) {
                roles = new String[0];
            }
            if ( log.isDebugEnabled() ) {
                log.debug ( "  Checking roles " + principal );
            }
            if ( constraint.getAuthenticatedUsers() && principal != null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "Passing all authenticated users" );
                }
                status = true;
            } else if ( roles.length == 0 && !constraint.getAllRoles() &&
                        !constraint.getAuthenticatedUsers() ) {
                if ( constraint.getAuthConstraint() ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug ( "No roles" );
                    }
                    status = false;
                    denyfromall = true;
                    break;
                }
                if ( log.isDebugEnabled() ) {
                    log.debug ( "Passing all access" );
                }
                status = true;
            } else if ( principal == null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "  No user authenticated, cannot grant access" );
                }
            } else {
                for ( int j = 0; j < roles.length; j++ ) {
                    if ( hasRole ( null, principal, roles[j] ) ) {
                        status = true;
                        if ( log.isDebugEnabled() ) {
                            log.debug ( "Role found:  " + roles[j] );
                        }
                    } else if ( log.isDebugEnabled() ) {
                        log.debug ( "No role found:  " + roles[j] );
                    }
                }
            }
        }
        if ( !denyfromall && allRolesMode != AllRolesMode.STRICT_MODE &&
                !status && principal != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( "Checking for all roles mode: " + allRolesMode );
            }
            for ( int i = 0; i < constraints.length; i++ ) {
                SecurityConstraint constraint = constraints[i];
                String roles[];
                if ( constraint.getAllRoles() ) {
                    if ( allRolesMode == AllRolesMode.AUTH_ONLY_MODE ) {
                        if ( log.isDebugEnabled() ) {
                            log.debug ( "Granting access for role-name=*, auth-only" );
                        }
                        status = true;
                        break;
                    }
                    roles = request.getContext().findSecurityRoles();
                    if ( roles.length == 0 && allRolesMode == AllRolesMode.STRICT_AUTH_ONLY_MODE ) {
                        if ( log.isDebugEnabled() ) {
                            log.debug ( "Granting access for role-name=*, strict auth-only" );
                        }
                        status = true;
                        break;
                    }
                }
            }
        }
        if ( !status ) {
            response.sendError
            ( HttpServletResponse.SC_FORBIDDEN,
              sm.getString ( "realmBase.forbidden" ) );
        }
        return status;
    }
    @Override
    public boolean hasRole ( Wrapper wrapper, Principal principal, String role ) {
        if ( wrapper != null ) {
            String realRole = wrapper.findSecurityReference ( role );
            if ( realRole != null ) {
                role = realRole;
            }
        }
        if ( ( principal == null ) || ( role == null ) ||
                ! ( principal instanceof GenericPrincipal ) ) {
            return false;
        }
        GenericPrincipal gp = ( GenericPrincipal ) principal;
        boolean result = gp.hasRole ( role );
        if ( log.isDebugEnabled() ) {
            String name = principal.getName();
            if ( result ) {
                log.debug ( sm.getString ( "realmBase.hasRoleSuccess", name, role ) );
            } else {
                log.debug ( sm.getString ( "realmBase.hasRoleFailure", name, role ) );
            }
        }
        return ( result );
    }
    @Override
    public boolean hasUserDataPermission ( Request request,
                                           Response response,
                                           SecurityConstraint []constraints )
    throws IOException {
        if ( constraints == null || constraints.length == 0 ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( "  No applicable security constraint defined" );
            }
            return true;
        }
        for ( int i = 0; i < constraints.length; i++ ) {
            SecurityConstraint constraint = constraints[i];
            String userConstraint = constraint.getUserConstraint();
            if ( userConstraint == null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "  No applicable user data constraint defined" );
                }
                return true;
            }
            if ( userConstraint.equals ( TransportGuarantee.NONE.name() ) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "  User data constraint has no restrictions" );
                }
                return true;
            }
        }
        if ( request.getRequest().isSecure() ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( "  User data constraint already satisfied" );
            }
            return true;
        }
        int redirectPort = request.getConnector().getRedirectPort();
        if ( redirectPort <= 0 ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( "  SSL redirect is disabled" );
            }
            response.sendError
            ( HttpServletResponse.SC_FORBIDDEN,
              request.getRequestURI() );
            return false;
        }
        StringBuilder file = new StringBuilder();
        String protocol = "https";
        String host = request.getServerName();
        file.append ( protocol ).append ( "://" ).append ( host );
        if ( redirectPort != 443 ) {
            file.append ( ":" ).append ( redirectPort );
        }
        file.append ( request.getRequestURI() );
        String requestedSessionId = request.getRequestedSessionId();
        if ( ( requestedSessionId != null ) &&
                request.isRequestedSessionIdFromURL() ) {
            file.append ( ";" );
            file.append ( SessionConfig.getSessionUriParamName (
                              request.getContext() ) );
            file.append ( "=" );
            file.append ( requestedSessionId );
        }
        String queryString = request.getQueryString();
        if ( queryString != null ) {
            file.append ( '?' );
            file.append ( queryString );
        }
        if ( log.isDebugEnabled() ) {
            log.debug ( "  Redirecting to " + file.toString() );
        }
        response.sendRedirect ( file.toString(), transportGuaranteeRedirectStatus );
        return false;
    }
    @Override
    public void removePropertyChangeListener ( PropertyChangeListener listener ) {
        support.removePropertyChangeListener ( listener );
    }
    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        if ( container != null ) {
            this.containerLog = container.getLogger();
        }
        x509UsernameRetriever = createUsernameRetriever ( x509UsernameRetrieverClassName );
    }
    @Override
    protected void startInternal() throws LifecycleException {
        if ( credentialHandler == null ) {
            credentialHandler = new MessageDigestCredentialHandler();
        }
        setState ( LifecycleState.STARTING );
    }
    @Override
    protected void stopInternal() throws LifecycleException {
        setState ( LifecycleState.STOPPING );
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "Realm[" );
        sb.append ( getName() );
        sb.append ( ']' );
        return sb.toString();
    }
    protected boolean hasMessageDigest() {
        CredentialHandler ch = credentialHandler;
        if ( ch instanceof MessageDigestCredentialHandler ) {
            return ( ( MessageDigestCredentialHandler ) ch ).getAlgorithm() != null;
        }
        return false;
    }
    protected String getDigest ( String username, String realmName ) {
        if ( hasMessageDigest() ) {
            return getPassword ( username );
        }
        String digestValue = username + ":" + realmName + ":"
                             + getPassword ( username );
        byte[] valueBytes = null;
        try {
            valueBytes = digestValue.getBytes ( getDigestCharset() );
        } catch ( UnsupportedEncodingException uee ) {
            log.error ( "Illegal digestEncoding: " + getDigestEncoding(), uee );
            throw new IllegalArgumentException ( uee.getMessage() );
        }
        return MD5Encoder.encode ( ConcurrentMessageDigest.digestMD5 ( valueBytes ) );
    }
    private String getDigestEncoding() {
        CredentialHandler ch = credentialHandler;
        if ( ch instanceof MessageDigestCredentialHandler ) {
            return ( ( MessageDigestCredentialHandler ) ch ).getEncoding();
        }
        return null;
    }
    private Charset getDigestCharset() throws UnsupportedEncodingException {
        String charset = getDigestEncoding();
        if ( charset == null ) {
            return StandardCharsets.ISO_8859_1;
        } else {
            return B2CConverter.getCharset ( charset );
        }
    }
    protected abstract String getName();
    protected abstract String getPassword ( String username );
    protected Principal getPrincipal ( X509Certificate usercert ) {
        String username = x509UsernameRetriever.getUsername ( usercert );
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "realmBase.gotX509Username", username ) );
        }
        return ( getPrincipal ( username ) );
    }
    protected abstract Principal getPrincipal ( String username );
    protected Principal getPrincipal ( String username,
                                       GSSCredential gssCredential ) {
        Principal p = getPrincipal ( username );
        if ( p instanceof GenericPrincipal ) {
            ( ( GenericPrincipal ) p ).setGssCredential ( gssCredential );
        }
        return p;
    }
    protected Server getServer() {
        Container c = container;
        if ( c instanceof Context ) {
            c = c.getParent();
        }
        if ( c instanceof Host ) {
            c = c.getParent();
        }
        if ( c instanceof Engine ) {
            Service s = ( ( Engine ) c ).getService();
            if ( s != null ) {
                return s.getServer();
            }
        }
        return null;
    }
    public static final String Digest ( String credentials, String algorithm,
                                        String encoding ) {
        try {
            MessageDigest md =
                ( MessageDigest ) MessageDigest.getInstance ( algorithm ).clone();
            if ( encoding == null ) {
                md.update ( credentials.getBytes() );
            } else {
                md.update ( credentials.getBytes ( encoding ) );
            }
            return ( HexUtils.toHexString ( md.digest() ) );
        } catch ( Exception ex ) {
            log.error ( ex );
            return credentials;
        }
    }
    public static void main ( String args[] ) {
        int saltLength = -1;
        int iterations = -1;
        int keyLength = -1;
        String encoding = Charset.defaultCharset().name();
        String algorithm = null;
        String handlerClassName = null;
        if ( args.length == 0 ) {
            usage();
            return;
        }
        int argIndex = 0;
        while ( args.length > argIndex + 2 && args[argIndex].length() == 2 &&
                args[argIndex].charAt ( 0 ) == '-' ) {
            switch ( args[argIndex].charAt ( 1 ) ) {
            case 'a': {
                algorithm = args[argIndex + 1];
                break;
            }
            case 'e': {
                encoding = args[argIndex + 1];
                break;
            }
            case 'i': {
                iterations = Integer.parseInt ( args[argIndex + 1] );
                break;
            }
            case 's': {
                saltLength = Integer.parseInt ( args[argIndex + 1] );
                break;
            }
            case 'k': {
                keyLength = Integer.parseInt ( args[argIndex + 1] );
                break;
            }
            case 'h': {
                handlerClassName = args[argIndex + 1];
                break;
            }
            default: {
                usage();
                return;
            }
            }
            argIndex += 2;
        }
        if ( algorithm == null && handlerClassName == null ) {
            algorithm = "SHA-512";
        }
        CredentialHandler handler = null;
        if ( handlerClassName == null ) {
            for ( Class<? extends DigestCredentialHandlerBase> clazz : credentialHandlerClasses ) {
                try {
                    handler = clazz.newInstance();
                    if ( IntrospectionUtils.setProperty ( handler, "algorithm", algorithm ) ) {
                        break;
                    }
                } catch ( InstantiationException | IllegalAccessException e ) {
                    throw new RuntimeException ( e );
                }
            }
        } else {
            try {
                Class<?> clazz = Class.forName ( handlerClassName );
                handler = ( DigestCredentialHandlerBase ) clazz.newInstance();
                IntrospectionUtils.setProperty ( handler, "algorithm", algorithm );
            } catch ( InstantiationException | IllegalAccessException
                          | ClassNotFoundException e ) {
                throw new RuntimeException ( e );
            }
        }
        if ( handler == null ) {
            throw new RuntimeException ( new NoSuchAlgorithmException ( algorithm ) );
        }
        IntrospectionUtils.setProperty ( handler, "encoding", encoding );
        if ( iterations > 0 ) {
            IntrospectionUtils.setProperty ( handler, "iterations", Integer.toString ( iterations ) );
        }
        if ( saltLength > -1 ) {
            IntrospectionUtils.setProperty ( handler, "saltLength", Integer.toString ( saltLength ) );
        }
        if ( keyLength > 0 ) {
            IntrospectionUtils.setProperty ( handler, "keyLength", Integer.toString ( keyLength ) );
        }
        for ( ; argIndex < args.length; argIndex++ ) {
            String credential = args[argIndex];
            System.out.print ( credential + ":" );
            System.out.println ( handler.mutate ( credential ) );
        }
    }
    private static void usage() {
        System.out.println ( "Usage: RealmBase [-a <algorithm>] [-e <encoding>] " +
                             "[-i <iterations>] [-s <salt-length>] [-k <key-length>] " +
                             "[-h <handler-class-name>] <credentials>" );
    }
    @Override
    public String getObjectNameKeyProperties() {
        StringBuilder keyProperties = new StringBuilder ( "type=Realm" );
        keyProperties.append ( getRealmSuffix() );
        keyProperties.append ( container.getMBeanKeyProperties() );
        return keyProperties.toString();
    }
    @Override
    public String getDomainInternal() {
        return container.getDomain();
    }
    protected String realmPath = "/realm0";
    public String getRealmPath() {
        return realmPath;
    }
    public void setRealmPath ( String theRealmPath ) {
        realmPath = theRealmPath;
    }
    protected String getRealmSuffix() {
        return ",realmPath=" + getRealmPath();
    }
    protected static class AllRolesMode {
        private final String name;
        public static final AllRolesMode STRICT_MODE = new AllRolesMode ( "strict" );
        public static final AllRolesMode AUTH_ONLY_MODE = new AllRolesMode ( "authOnly" );
        public static final AllRolesMode STRICT_AUTH_ONLY_MODE = new AllRolesMode ( "strictAuthOnly" );
        static AllRolesMode toMode ( String name ) {
            AllRolesMode mode;
            if ( name.equalsIgnoreCase ( STRICT_MODE.name ) ) {
                mode = STRICT_MODE;
            } else if ( name.equalsIgnoreCase ( AUTH_ONLY_MODE.name ) ) {
                mode = AUTH_ONLY_MODE;
            } else if ( name.equalsIgnoreCase ( STRICT_AUTH_ONLY_MODE.name ) ) {
                mode = STRICT_AUTH_ONLY_MODE;
            } else {
                throw new IllegalStateException ( "Unknown mode, must be one of: strict, authOnly, strictAuthOnly" );
            }
            return mode;
        }
        private AllRolesMode ( String name ) {
            this.name = name;
        }
        @Override
        public boolean equals ( Object o ) {
            boolean equals = false;
            if ( o instanceof AllRolesMode ) {
                AllRolesMode mode = ( AllRolesMode ) o;
                equals = name.equals ( mode.name );
            }
            return equals;
        }
        @Override
        public int hashCode() {
            return name.hashCode();
        }
        @Override
        public String toString() {
            return name;
        }
    }
    private static X509UsernameRetriever createUsernameRetriever ( String className )
    throws LifecycleException {
        if ( null == className || "".equals ( className.trim() ) ) {
            return new X509SubjectDnRetriever();
        }
        try {
            @SuppressWarnings ( "unchecked" )
            Class<? extends X509UsernameRetriever> clazz = ( Class<? extends X509UsernameRetriever> ) Class.forName ( className );
            return clazz.newInstance();
        } catch ( ClassNotFoundException e ) {
            throw new LifecycleException ( sm.getString ( "realmBase.createUsernameRetriever.ClassNotFoundException", className ), e );
        } catch ( InstantiationException e ) {
            throw new LifecycleException ( sm.getString ( "realmBase.createUsernameRetriever.InstantiationException", className ), e );
        } catch ( IllegalAccessException e ) {
            throw new LifecycleException ( sm.getString ( "realmBase.createUsernameRetriever.IllegalAccessException", className ), e );
        } catch ( ClassCastException e ) {
            throw new LifecycleException ( sm.getString ( "realmBase.createUsernameRetriever.ClassCastException", className ), e );
        }
    }
    @Override
    public String[] getRoles ( Principal principal ) {
        if ( principal instanceof GenericPrincipal ) {
            return ( ( GenericPrincipal ) principal ).getRoles();
        }
        String className = principal.getClass().getSimpleName();
        throw new IllegalStateException ( sm.getString ( "realmBase.cannotGetRoles", className ) );
    }
}
