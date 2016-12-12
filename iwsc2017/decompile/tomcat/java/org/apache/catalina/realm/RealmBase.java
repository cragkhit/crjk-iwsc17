package org.apache.catalina.realm;
import org.apache.juli.logging.LogFactory;
import java.util.Iterator;
import java.security.NoSuchAlgorithmException;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.buf.HexUtils;
import java.security.MessageDigest;
import org.apache.catalina.Service;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Server;
import org.apache.tomcat.util.buf.B2CConverter;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.util.SessionConfig;
import javax.servlet.annotation.ServletSecurity;
import java.io.IOException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Response;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import java.util.ArrayList;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Request;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSContext;
import java.security.cert.X509Certificate;
import org.apache.tomcat.util.security.MD5Encoder;
import org.apache.tomcat.util.security.ConcurrentMessageDigest;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.security.Principal;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.apache.tomcat.util.res.StringManager;
import org.apache.catalina.CredentialHandler;
import org.apache.catalina.Container;
import java.util.List;
import org.apache.juli.logging.Log;
import org.apache.catalina.Realm;
import org.apache.catalina.util.LifecycleMBeanBase;
public abstract class RealmBase extends LifecycleMBeanBase implements Realm {
    private static final Log log;
    private static final List<Class<? extends DigestCredentialHandlerBase>> credentialHandlerClasses;
    protected Container container;
    protected Log containerLog;
    private CredentialHandler credentialHandler;
    protected static final StringManager sm;
    protected final PropertyChangeSupport support;
    protected boolean validate;
    protected String x509UsernameRetrieverClassName;
    protected X509UsernameRetriever x509UsernameRetriever;
    protected AllRolesMode allRolesMode;
    protected boolean stripRealmForGss;
    private int transportGuaranteeRedirectStatus;
    protected String realmPath;
    public RealmBase() {
        this.container = null;
        this.containerLog = null;
        this.support = new PropertyChangeSupport ( this );
        this.validate = true;
        this.allRolesMode = AllRolesMode.STRICT_MODE;
        this.stripRealmForGss = true;
        this.transportGuaranteeRedirectStatus = 302;
        this.realmPath = "/realm0";
    }
    public int getTransportGuaranteeRedirectStatus() {
        return this.transportGuaranteeRedirectStatus;
    }
    public void setTransportGuaranteeRedirectStatus ( final int transportGuaranteeRedirectStatus ) {
        this.transportGuaranteeRedirectStatus = transportGuaranteeRedirectStatus;
    }
    @Override
    public CredentialHandler getCredentialHandler() {
        return this.credentialHandler;
    }
    @Override
    public void setCredentialHandler ( final CredentialHandler credentialHandler ) {
        this.credentialHandler = credentialHandler;
    }
    @Override
    public Container getContainer() {
        return this.container;
    }
    @Override
    public void setContainer ( final Container container ) {
        final Container oldContainer = this.container;
        this.container = container;
        this.support.firePropertyChange ( "container", oldContainer, this.container );
    }
    public String getAllRolesMode() {
        return this.allRolesMode.toString();
    }
    public void setAllRolesMode ( final String allRolesMode ) {
        this.allRolesMode = AllRolesMode.toMode ( allRolesMode );
    }
    public boolean getValidate() {
        return this.validate;
    }
    public void setValidate ( final boolean validate ) {
        this.validate = validate;
    }
    public String getX509UsernameRetrieverClassName() {
        return this.x509UsernameRetrieverClassName;
    }
    public void setX509UsernameRetrieverClassName ( final String className ) {
        this.x509UsernameRetrieverClassName = className;
    }
    public boolean isStripRealmForGss() {
        return this.stripRealmForGss;
    }
    public void setStripRealmForGss ( final boolean stripRealmForGss ) {
        this.stripRealmForGss = stripRealmForGss;
    }
    @Override
    public void addPropertyChangeListener ( final PropertyChangeListener listener ) {
        this.support.addPropertyChangeListener ( listener );
    }
    @Override
    public Principal authenticate ( final String username ) {
        if ( username == null ) {
            return null;
        }
        if ( this.containerLog.isTraceEnabled() ) {
            this.containerLog.trace ( RealmBase.sm.getString ( "realmBase.authenticateSuccess", username ) );
        }
        return this.getPrincipal ( username );
    }
    @Override
    public Principal authenticate ( final String username, final String credentials ) {
        if ( username == null || credentials == null ) {
            if ( this.containerLog.isTraceEnabled() ) {
                this.containerLog.trace ( RealmBase.sm.getString ( "realmBase.authenticateFailure", username ) );
            }
            return null;
        }
        final String serverCredentials = this.getPassword ( username );
        if ( serverCredentials == null ) {
            this.getCredentialHandler().mutate ( credentials );
            if ( this.containerLog.isTraceEnabled() ) {
                this.containerLog.trace ( RealmBase.sm.getString ( "realmBase.authenticateFailure", username ) );
            }
            return null;
        }
        final boolean validated = this.getCredentialHandler().matches ( credentials, serverCredentials );
        if ( validated ) {
            if ( this.containerLog.isTraceEnabled() ) {
                this.containerLog.trace ( RealmBase.sm.getString ( "realmBase.authenticateSuccess", username ) );
            }
            return this.getPrincipal ( username );
        }
        if ( this.containerLog.isTraceEnabled() ) {
            this.containerLog.trace ( RealmBase.sm.getString ( "realmBase.authenticateFailure", username ) );
        }
        return null;
    }
    @Override
    public Principal authenticate ( final String username, final String clientDigest, final String nonce, final String nc, final String cnonce, final String qop, final String realm, final String md5a2 ) {
        String md5a3 = this.getDigest ( username, realm );
        if ( md5a3 == null ) {
            return null;
        }
        md5a3 = md5a3.toLowerCase ( Locale.ENGLISH );
        String serverDigestValue;
        if ( qop == null ) {
            serverDigestValue = md5a3 + ":" + nonce + ":" + md5a2;
        } else {
            serverDigestValue = md5a3 + ":" + nonce + ":" + nc + ":" + cnonce + ":" + qop + ":" + md5a2;
        }
        byte[] valueBytes = null;
        try {
            valueBytes = serverDigestValue.getBytes ( this.getDigestCharset() );
        } catch ( UnsupportedEncodingException uee ) {
            RealmBase.log.error ( "Illegal digestEncoding: " + this.getDigestEncoding(), uee );
            throw new IllegalArgumentException ( uee.getMessage() );
        }
        final String serverDigest = MD5Encoder.encode ( ConcurrentMessageDigest.digestMD5 ( new byte[][] { valueBytes } ) );
        if ( RealmBase.log.isDebugEnabled() ) {
            RealmBase.log.debug ( "Digest : " + clientDigest + " Username:" + username + " ClientSigest:" + clientDigest + " nonce:" + nonce + " nc:" + nc + " cnonce:" + cnonce + " qop:" + qop + " realm:" + realm + "md5a2:" + md5a2 + " Server digest:" + serverDigest );
        }
        if ( serverDigest.equals ( clientDigest ) ) {
            return this.getPrincipal ( username );
        }
        return null;
    }
    @Override
    public Principal authenticate ( final X509Certificate[] certs ) {
        if ( certs == null || certs.length < 1 ) {
            return null;
        }
        if ( RealmBase.log.isDebugEnabled() ) {
            RealmBase.log.debug ( "Authenticating client certificate chain" );
        }
        if ( this.validate ) {
            for ( int i = 0; i < certs.length; ++i ) {
                if ( RealmBase.log.isDebugEnabled() ) {
                    RealmBase.log.debug ( " Checking validity for '" + certs[i].getSubjectDN().getName() + "'" );
                }
                try {
                    certs[i].checkValidity();
                } catch ( Exception e ) {
                    if ( RealmBase.log.isDebugEnabled() ) {
                        RealmBase.log.debug ( "  Validity exception", e );
                    }
                    return null;
                }
            }
        }
        return this.getPrincipal ( certs[0] );
    }
    @Override
    public Principal authenticate ( final GSSContext gssContext, final boolean storeCred ) {
        if ( gssContext.isEstablished() ) {
            GSSName gssName = null;
            try {
                gssName = gssContext.getSrcName();
            } catch ( GSSException e ) {
                RealmBase.log.warn ( RealmBase.sm.getString ( "realmBase.gssNameFail" ), e );
            }
            if ( gssName != null ) {
                String name = gssName.toString();
                if ( this.isStripRealmForGss() ) {
                    final int i = name.indexOf ( 64 );
                    if ( i > 0 ) {
                        name = name.substring ( 0, i );
                    }
                }
                GSSCredential gssCredential = null;
                if ( storeCred && gssContext.getCredDelegState() ) {
                    try {
                        gssCredential = gssContext.getDelegCred();
                    } catch ( GSSException e2 ) {
                        if ( RealmBase.log.isDebugEnabled() ) {
                            RealmBase.log.debug ( RealmBase.sm.getString ( "realmBase.delegatedCredentialFail", name ), e2 );
                        }
                    }
                }
                return this.getPrincipal ( name, gssCredential );
            }
        }
        return null;
    }
    @Override
    public void backgroundProcess() {
    }
    @Override
    public SecurityConstraint[] findSecurityConstraints ( final Request request, final Context context ) {
        ArrayList<SecurityConstraint> results = null;
        final SecurityConstraint[] constraints = context.findConstraints();
        if ( constraints == null || constraints.length == 0 ) {
            if ( RealmBase.log.isDebugEnabled() ) {
                RealmBase.log.debug ( "  No applicable constraints defined" );
            }
            return null;
        }
        String uri = request.getRequestPathMB().toString();
        if ( uri == null ) {
            uri = "/";
        }
        final String method = request.getMethod();
        boolean found = false;
        for ( int i = 0; i < constraints.length; ++i ) {
            final SecurityCollection[] collection = constraints[i].findCollections();
            if ( collection != null ) {
                if ( RealmBase.log.isDebugEnabled() ) {
                    RealmBase.log.debug ( "  Checking constraint '" + constraints[i] + "' against " + method + " " + uri + " --> " + constraints[i].included ( uri, method ) );
                }
                for ( int j = 0; j < collection.length; ++j ) {
                    final String[] patterns = collection[j].findPatterns();
                    if ( patterns != null ) {
                        for ( int k = 0; k < patterns.length; ++k ) {
                            if ( uri.equals ( patterns[k] ) ) {
                                found = true;
                                if ( collection[j].findMethod ( method ) ) {
                                    if ( results == null ) {
                                        results = new ArrayList<SecurityConstraint>();
                                    }
                                    results.add ( constraints[i] );
                                }
                            }
                        }
                    }
                }
            }
        }
        if ( found ) {
            return this.resultsToArray ( results );
        }
        int longest = -1;
        for ( int i = 0; i < constraints.length; ++i ) {
            final SecurityCollection[] collection2 = constraints[i].findCollections();
            if ( collection2 != null ) {
                if ( RealmBase.log.isDebugEnabled() ) {
                    RealmBase.log.debug ( "  Checking constraint '" + constraints[i] + "' against " + method + " " + uri + " --> " + constraints[i].included ( uri, method ) );
                }
                for ( int l = 0; l < collection2.length; ++l ) {
                    final String[] patterns2 = collection2[l].findPatterns();
                    if ( patterns2 != null ) {
                        boolean matched = false;
                        int length = -1;
                        for ( int m = 0; m < patterns2.length; ++m ) {
                            final String pattern = patterns2[m];
                            if ( pattern.startsWith ( "/" ) && pattern.endsWith ( "/*" ) && pattern.length() >= longest ) {
                                if ( pattern.length() == 2 ) {
                                    matched = true;
                                    length = pattern.length();
                                } else if ( pattern.regionMatches ( 0, uri, 0, pattern.length() - 1 ) || ( pattern.length() - 2 == uri.length() && pattern.regionMatches ( 0, uri, 0, pattern.length() - 2 ) ) ) {
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
                            if ( collection2[l].findMethod ( method ) ) {
                                found = true;
                                if ( results == null ) {
                                    results = new ArrayList<SecurityConstraint>();
                                }
                                results.add ( constraints[i] );
                            }
                        }
                    }
                }
            }
        }
        if ( found ) {
            return this.resultsToArray ( results );
        }
        for ( int i = 0; i < constraints.length; ++i ) {
            final SecurityCollection[] collection2 = constraints[i].findCollections();
            if ( collection2 != null ) {
                if ( RealmBase.log.isDebugEnabled() ) {
                    RealmBase.log.debug ( "  Checking constraint '" + constraints[i] + "' against " + method + " " + uri + " --> " + constraints[i].included ( uri, method ) );
                }
                boolean matched2 = false;
                int pos = -1;
                for ( int j2 = 0; j2 < collection2.length; ++j2 ) {
                    final String[] patterns3 = collection2[j2].findPatterns();
                    if ( patterns3 != null ) {
                        for ( int m = 0; m < patterns3.length && !matched2; ++m ) {
                            final String pattern = patterns3[m];
                            if ( pattern.startsWith ( "*." ) ) {
                                final int slash = uri.lastIndexOf ( 47 );
                                final int dot = uri.lastIndexOf ( 46 );
                                if ( slash >= 0 && dot > slash && dot != uri.length() - 1 && uri.length() - dot == pattern.length() - 1 && pattern.regionMatches ( 1, uri, dot, uri.length() - dot ) ) {
                                    matched2 = true;
                                    pos = j2;
                                }
                            }
                        }
                    }
                }
                if ( matched2 ) {
                    found = true;
                    if ( collection2[pos].findMethod ( method ) ) {
                        if ( results == null ) {
                            results = new ArrayList<SecurityConstraint>();
                        }
                        results.add ( constraints[i] );
                    }
                }
            }
        }
        if ( found ) {
            return this.resultsToArray ( results );
        }
        for ( int i = 0; i < constraints.length; ++i ) {
            final SecurityCollection[] collection2 = constraints[i].findCollections();
            if ( collection2 != null ) {
                if ( RealmBase.log.isDebugEnabled() ) {
                    RealmBase.log.debug ( "  Checking constraint '" + constraints[i] + "' against " + method + " " + uri + " --> " + constraints[i].included ( uri, method ) );
                }
                for ( int l = 0; l < collection2.length; ++l ) {
                    final String[] patterns2 = collection2[l].findPatterns();
                    if ( patterns2 != null ) {
                        boolean matched = false;
                        for ( int k2 = 0; k2 < patterns2.length && !matched; ++k2 ) {
                            final String pattern2 = patterns2[k2];
                            if ( pattern2.equals ( "/" ) ) {
                                matched = true;
                            }
                        }
                        if ( matched ) {
                            if ( results == null ) {
                                results = new ArrayList<SecurityConstraint>();
                            }
                            results.add ( constraints[i] );
                        }
                    }
                }
            }
        }
        if ( results == null && RealmBase.log.isDebugEnabled() ) {
            RealmBase.log.debug ( "  No applicable constraint located" );
        }
        return this.resultsToArray ( results );
    }
    private SecurityConstraint[] resultsToArray ( final ArrayList<SecurityConstraint> results ) {
        if ( results == null || results.size() == 0 ) {
            return null;
        }
        final SecurityConstraint[] array = new SecurityConstraint[results.size()];
        results.toArray ( array );
        return array;
    }
    @Override
    public boolean hasResourcePermission ( final Request request, final Response response, final SecurityConstraint[] constraints, final Context context ) throws IOException {
        if ( constraints == null || constraints.length == 0 ) {
            return true;
        }
        final Principal principal = request.getPrincipal();
        boolean status = false;
        boolean denyfromall = false;
        for ( int i = 0; i < constraints.length; ++i ) {
            final SecurityConstraint constraint = constraints[i];
            String[] roles;
            if ( constraint.getAllRoles() ) {
                roles = request.getContext().findSecurityRoles();
            } else {
                roles = constraint.findAuthRoles();
            }
            if ( roles == null ) {
                roles = new String[0];
            }
            if ( RealmBase.log.isDebugEnabled() ) {
                RealmBase.log.debug ( "  Checking roles " + principal );
            }
            if ( constraint.getAuthenticatedUsers() && principal != null ) {
                if ( RealmBase.log.isDebugEnabled() ) {
                    RealmBase.log.debug ( "Passing all authenticated users" );
                }
                status = true;
            } else if ( roles.length == 0 && !constraint.getAllRoles() && !constraint.getAuthenticatedUsers() ) {
                if ( constraint.getAuthConstraint() ) {
                    if ( RealmBase.log.isDebugEnabled() ) {
                        RealmBase.log.debug ( "No roles" );
                    }
                    status = false;
                    denyfromall = true;
                    break;
                }
                if ( RealmBase.log.isDebugEnabled() ) {
                    RealmBase.log.debug ( "Passing all access" );
                }
                status = true;
            } else if ( principal == null ) {
                if ( RealmBase.log.isDebugEnabled() ) {
                    RealmBase.log.debug ( "  No user authenticated, cannot grant access" );
                }
            } else {
                for ( int j = 0; j < roles.length; ++j ) {
                    if ( this.hasRole ( null, principal, roles[j] ) ) {
                        status = true;
                        if ( RealmBase.log.isDebugEnabled() ) {
                            RealmBase.log.debug ( "Role found:  " + roles[j] );
                        }
                    } else if ( RealmBase.log.isDebugEnabled() ) {
                        RealmBase.log.debug ( "No role found:  " + roles[j] );
                    }
                }
            }
        }
        if ( !denyfromall && this.allRolesMode != AllRolesMode.STRICT_MODE && !status && principal != null ) {
            if ( RealmBase.log.isDebugEnabled() ) {
                RealmBase.log.debug ( "Checking for all roles mode: " + this.allRolesMode );
            }
            for ( int i = 0; i < constraints.length; ++i ) {
                final SecurityConstraint constraint = constraints[i];
                if ( constraint.getAllRoles() ) {
                    if ( this.allRolesMode == AllRolesMode.AUTH_ONLY_MODE ) {
                        if ( RealmBase.log.isDebugEnabled() ) {
                            RealmBase.log.debug ( "Granting access for role-name=*, auth-only" );
                        }
                        status = true;
                        break;
                    }
                    final String[] roles = request.getContext().findSecurityRoles();
                    if ( roles.length == 0 && this.allRolesMode == AllRolesMode.STRICT_AUTH_ONLY_MODE ) {
                        if ( RealmBase.log.isDebugEnabled() ) {
                            RealmBase.log.debug ( "Granting access for role-name=*, strict auth-only" );
                        }
                        status = true;
                        break;
                    }
                }
            }
        }
        if ( !status ) {
            response.sendError ( 403, RealmBase.sm.getString ( "realmBase.forbidden" ) );
        }
        return status;
    }
    @Override
    public boolean hasRole ( final Wrapper wrapper, final Principal principal, String role ) {
        if ( wrapper != null ) {
            final String realRole = wrapper.findSecurityReference ( role );
            if ( realRole != null ) {
                role = realRole;
            }
        }
        if ( principal == null || role == null || ! ( principal instanceof GenericPrincipal ) ) {
            return false;
        }
        final GenericPrincipal gp = ( GenericPrincipal ) principal;
        final boolean result = gp.hasRole ( role );
        if ( RealmBase.log.isDebugEnabled() ) {
            final String name = principal.getName();
            if ( result ) {
                RealmBase.log.debug ( RealmBase.sm.getString ( "realmBase.hasRoleSuccess", name, role ) );
            } else {
                RealmBase.log.debug ( RealmBase.sm.getString ( "realmBase.hasRoleFailure", name, role ) );
            }
        }
        return result;
    }
    @Override
    public boolean hasUserDataPermission ( final Request request, final Response response, final SecurityConstraint[] constraints ) throws IOException {
        if ( constraints == null || constraints.length == 0 ) {
            if ( RealmBase.log.isDebugEnabled() ) {
                RealmBase.log.debug ( "  No applicable security constraint defined" );
            }
            return true;
        }
        for ( int i = 0; i < constraints.length; ++i ) {
            final SecurityConstraint constraint = constraints[i];
            final String userConstraint = constraint.getUserConstraint();
            if ( userConstraint == null ) {
                if ( RealmBase.log.isDebugEnabled() ) {
                    RealmBase.log.debug ( "  No applicable user data constraint defined" );
                }
                return true;
            }
            if ( userConstraint.equals ( ServletSecurity.TransportGuarantee.NONE.name() ) ) {
                if ( RealmBase.log.isDebugEnabled() ) {
                    RealmBase.log.debug ( "  User data constraint has no restrictions" );
                }
                return true;
            }
        }
        if ( request.getRequest().isSecure() ) {
            if ( RealmBase.log.isDebugEnabled() ) {
                RealmBase.log.debug ( "  User data constraint already satisfied" );
            }
            return true;
        }
        final int redirectPort = request.getConnector().getRedirectPort();
        if ( redirectPort <= 0 ) {
            if ( RealmBase.log.isDebugEnabled() ) {
                RealmBase.log.debug ( "  SSL redirect is disabled" );
            }
            response.sendError ( 403, request.getRequestURI() );
            return false;
        }
        final StringBuilder file = new StringBuilder();
        final String protocol = "https";
        final String host = request.getServerName();
        file.append ( protocol ).append ( "://" ).append ( host );
        if ( redirectPort != 443 ) {
            file.append ( ":" ).append ( redirectPort );
        }
        file.append ( request.getRequestURI() );
        final String requestedSessionId = request.getRequestedSessionId();
        if ( requestedSessionId != null && request.isRequestedSessionIdFromURL() ) {
            file.append ( ";" );
            file.append ( SessionConfig.getSessionUriParamName ( request.getContext() ) );
            file.append ( "=" );
            file.append ( requestedSessionId );
        }
        final String queryString = request.getQueryString();
        if ( queryString != null ) {
            file.append ( '?' );
            file.append ( queryString );
        }
        if ( RealmBase.log.isDebugEnabled() ) {
            RealmBase.log.debug ( "  Redirecting to " + file.toString() );
        }
        response.sendRedirect ( file.toString(), this.transportGuaranteeRedirectStatus );
        return false;
    }
    @Override
    public void removePropertyChangeListener ( final PropertyChangeListener listener ) {
        this.support.removePropertyChangeListener ( listener );
    }
    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        if ( this.container != null ) {
            this.containerLog = this.container.getLogger();
        }
        this.x509UsernameRetriever = createUsernameRetriever ( this.x509UsernameRetrieverClassName );
    }
    @Override
    protected void startInternal() throws LifecycleException {
        if ( this.credentialHandler == null ) {
            this.credentialHandler = new MessageDigestCredentialHandler();
        }
        this.setState ( LifecycleState.STARTING );
    }
    @Override
    protected void stopInternal() throws LifecycleException {
        this.setState ( LifecycleState.STOPPING );
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "Realm[" );
        sb.append ( this.getName() );
        sb.append ( ']' );
        return sb.toString();
    }
    protected boolean hasMessageDigest() {
        final CredentialHandler ch = this.credentialHandler;
        return ch instanceof MessageDigestCredentialHandler && ( ( MessageDigestCredentialHandler ) ch ).getAlgorithm() != null;
    }
    protected String getDigest ( final String username, final String realmName ) {
        if ( this.hasMessageDigest() ) {
            return this.getPassword ( username );
        }
        final String digestValue = username + ":" + realmName + ":" + this.getPassword ( username );
        byte[] valueBytes = null;
        try {
            valueBytes = digestValue.getBytes ( this.getDigestCharset() );
        } catch ( UnsupportedEncodingException uee ) {
            RealmBase.log.error ( "Illegal digestEncoding: " + this.getDigestEncoding(), uee );
            throw new IllegalArgumentException ( uee.getMessage() );
        }
        return MD5Encoder.encode ( ConcurrentMessageDigest.digestMD5 ( new byte[][] { valueBytes } ) );
    }
    private String getDigestEncoding() {
        final CredentialHandler ch = this.credentialHandler;
        if ( ch instanceof MessageDigestCredentialHandler ) {
            return ( ( MessageDigestCredentialHandler ) ch ).getEncoding();
        }
        return null;
    }
    private Charset getDigestCharset() throws UnsupportedEncodingException {
        final String charset = this.getDigestEncoding();
        if ( charset == null ) {
            return StandardCharsets.ISO_8859_1;
        }
        return B2CConverter.getCharset ( charset );
    }
    protected abstract String getName();
    protected abstract String getPassword ( final String p0 );
    protected Principal getPrincipal ( final X509Certificate usercert ) {
        final String username = this.x509UsernameRetriever.getUsername ( usercert );
        if ( RealmBase.log.isDebugEnabled() ) {
            RealmBase.log.debug ( RealmBase.sm.getString ( "realmBase.gotX509Username", username ) );
        }
        return this.getPrincipal ( username );
    }
    protected abstract Principal getPrincipal ( final String p0 );
    protected Principal getPrincipal ( final String username, final GSSCredential gssCredential ) {
        final Principal p = this.getPrincipal ( username );
        if ( p instanceof GenericPrincipal ) {
            ( ( GenericPrincipal ) p ).setGssCredential ( gssCredential );
        }
        return p;
    }
    protected Server getServer() {
        Container c = this.container;
        if ( c instanceof Context ) {
            c = c.getParent();
        }
        if ( c instanceof Host ) {
            c = c.getParent();
        }
        if ( c instanceof Engine ) {
            final Service s = ( ( Engine ) c ).getService();
            if ( s != null ) {
                return s.getServer();
            }
        }
        return null;
    }
    public static final String Digest ( final String credentials, final String algorithm, final String encoding ) {
        try {
            final MessageDigest md = ( MessageDigest ) MessageDigest.getInstance ( algorithm ).clone();
            if ( encoding == null ) {
                md.update ( credentials.getBytes() );
            } else {
                md.update ( credentials.getBytes ( encoding ) );
            }
            return HexUtils.toHexString ( md.digest() );
        } catch ( Exception ex ) {
            RealmBase.log.error ( ex );
            return credentials;
        }
    }
    public static void main ( final String[] args ) {
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
        int argIndex;
        for ( argIndex = 0; args.length > argIndex + 2 && args[argIndex].length() == 2 && args[argIndex].charAt ( 0 ) == '-'; argIndex += 2 ) {
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
        }
        if ( algorithm == null && handlerClassName == null ) {
            algorithm = "SHA-512";
        }
        CredentialHandler handler = null;
        if ( handlerClassName == null ) {
            for ( final Class<? extends DigestCredentialHandlerBase> clazz : RealmBase.credentialHandlerClasses ) {
                try {
                    handler = ( CredentialHandler ) clazz.newInstance();
                    if ( IntrospectionUtils.setProperty ( handler, "algorithm", algorithm ) ) {
                        break;
                    }
                    continue;
                } catch ( InstantiationException | IllegalAccessException e ) {
                    throw new RuntimeException ( e );
                }
            }
        } else {
            try {
                final Class<?> clazz2 = Class.forName ( handlerClassName );
                handler = ( DigestCredentialHandlerBase ) clazz2.newInstance();
                IntrospectionUtils.setProperty ( handler, "algorithm", algorithm );
            } catch ( InstantiationException | IllegalAccessException | ClassNotFoundException e2 ) {
                throw new RuntimeException ( e2 );
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
        while ( argIndex < args.length ) {
            final String credential = args[argIndex];
            System.out.print ( credential + ":" );
            System.out.println ( handler.mutate ( credential ) );
            ++argIndex;
        }
    }
    private static void usage() {
        System.out.println ( "Usage: RealmBase [-a <algorithm>] [-e <encoding>] [-i <iterations>] [-s <salt-length>] [-k <key-length>] [-h <handler-class-name>] <credentials>" );
    }
    public String getObjectNameKeyProperties() {
        final StringBuilder keyProperties = new StringBuilder ( "type=Realm" );
        keyProperties.append ( this.getRealmSuffix() );
        keyProperties.append ( this.container.getMBeanKeyProperties() );
        return keyProperties.toString();
    }
    public String getDomainInternal() {
        return this.container.getDomain();
    }
    public String getRealmPath() {
        return this.realmPath;
    }
    public void setRealmPath ( final String theRealmPath ) {
        this.realmPath = theRealmPath;
    }
    protected String getRealmSuffix() {
        return ",realmPath=" + this.getRealmPath();
    }
    private static X509UsernameRetriever createUsernameRetriever ( final String className ) throws LifecycleException {
        if ( null == className || "".equals ( className.trim() ) ) {
            return new X509SubjectDnRetriever();
        }
        try {
            final Class<? extends X509UsernameRetriever> clazz = ( Class<? extends X509UsernameRetriever> ) Class.forName ( className );
            return ( X509UsernameRetriever ) clazz.newInstance();
        } catch ( ClassNotFoundException e ) {
            throw new LifecycleException ( RealmBase.sm.getString ( "realmBase.createUsernameRetriever.ClassNotFoundException", className ), e );
        } catch ( InstantiationException e2 ) {
            throw new LifecycleException ( RealmBase.sm.getString ( "realmBase.createUsernameRetriever.InstantiationException", className ), e2 );
        } catch ( IllegalAccessException e3 ) {
            throw new LifecycleException ( RealmBase.sm.getString ( "realmBase.createUsernameRetriever.IllegalAccessException", className ), e3 );
        } catch ( ClassCastException e4 ) {
            throw new LifecycleException ( RealmBase.sm.getString ( "realmBase.createUsernameRetriever.ClassCastException", className ), e4 );
        }
    }
    @Override
    public String[] getRoles ( final Principal principal ) {
        if ( principal instanceof GenericPrincipal ) {
            return ( ( GenericPrincipal ) principal ).getRoles();
        }
        final String className = principal.getClass().getSimpleName();
        throw new IllegalStateException ( RealmBase.sm.getString ( "realmBase.cannotGetRoles", className ) );
    }
    static {
        log = LogFactory.getLog ( RealmBase.class );
        ( credentialHandlerClasses = new ArrayList<Class<? extends DigestCredentialHandlerBase>>() ).add ( MessageDigestCredentialHandler.class );
        RealmBase.credentialHandlerClasses.add ( SecretKeyCredentialHandler.class );
        sm = StringManager.getManager ( RealmBase.class );
    }
    protected static class AllRolesMode {
        private final String name;
        public static final AllRolesMode STRICT_MODE;
        public static final AllRolesMode AUTH_ONLY_MODE;
        public static final AllRolesMode STRICT_AUTH_ONLY_MODE;
        static AllRolesMode toMode ( final String name ) {
            AllRolesMode mode;
            if ( name.equalsIgnoreCase ( AllRolesMode.STRICT_MODE.name ) ) {
                mode = AllRolesMode.STRICT_MODE;
            } else if ( name.equalsIgnoreCase ( AllRolesMode.AUTH_ONLY_MODE.name ) ) {
                mode = AllRolesMode.AUTH_ONLY_MODE;
            } else {
                if ( !name.equalsIgnoreCase ( AllRolesMode.STRICT_AUTH_ONLY_MODE.name ) ) {
                    throw new IllegalStateException ( "Unknown mode, must be one of: strict, authOnly, strictAuthOnly" );
                }
                mode = AllRolesMode.STRICT_AUTH_ONLY_MODE;
            }
            return mode;
        }
        private AllRolesMode ( final String name ) {
            this.name = name;
        }
        @Override
        public boolean equals ( final Object o ) {
            boolean equals = false;
            if ( o instanceof AllRolesMode ) {
                final AllRolesMode mode = ( AllRolesMode ) o;
                equals = this.name.equals ( mode.name );
            }
            return equals;
        }
        @Override
        public int hashCode() {
            return this.name.hashCode();
        }
        @Override
        public String toString() {
            return this.name;
        }
        static {
            STRICT_MODE = new AllRolesMode ( "strict" );
            AUTH_ONLY_MODE = new AllRolesMode ( "authOnly" );
            STRICT_AUTH_ONLY_MODE = new AllRolesMode ( "strictAuthOnly" );
        }
    }
}
