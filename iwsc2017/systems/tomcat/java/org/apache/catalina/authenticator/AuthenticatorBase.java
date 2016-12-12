package org.apache.catalina.authenticator;
import java.io.IOException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.config.AuthConfigProvider;
import javax.security.auth.message.config.RegistrationListener;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.security.auth.message.config.ServerAuthContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Authenticator;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Manager;
import org.apache.catalina.Realm;
import org.apache.catalina.Session;
import org.apache.catalina.Valve;
import org.apache.catalina.Wrapper;
import org.apache.catalina.authenticator.jaspic.CallbackHandlerImpl;
import org.apache.catalina.authenticator.jaspic.MessageInfoImpl;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.util.SessionIdGeneratorBase;
import org.apache.catalina.util.StandardSessionIdGenerator;
import org.apache.catalina.valves.ValveBase;
import org.apache.coyote.ActionCode;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.apache.tomcat.util.http.FastHttpDateFormat;
import org.apache.tomcat.util.res.StringManager;
public abstract class AuthenticatorBase extends ValveBase
    implements Authenticator, RegistrationListener {
    private static final Log log = LogFactory.getLog ( AuthenticatorBase.class );
    private static final String DATE_ONE =
        ( new SimpleDateFormat ( FastHttpDateFormat.RFC1123_DATE, Locale.US ) ).format ( new Date ( 1 ) );
    protected static final StringManager sm = StringManager.getManager ( AuthenticatorBase.class );
    protected static final String AUTH_HEADER_NAME = "WWW-Authenticate";
    protected static final String REALM_NAME = "Authentication required";
    protected static String getRealmName ( Context context ) {
        if ( context == null ) {
            return REALM_NAME;
        }
        LoginConfig config = context.getLoginConfig();
        if ( config == null ) {
            return REALM_NAME;
        }
        String result = config.getRealmName();
        if ( result == null ) {
            return REALM_NAME;
        }
        return result;
    }
    public AuthenticatorBase() {
        super ( true );
    }
    protected boolean alwaysUseSession = false;
    protected boolean cache = true;
    protected boolean changeSessionIdOnAuthentication = true;
    protected Context context = null;
    protected boolean disableProxyCaching = true;
    protected boolean securePagesWithPragma = false;
    protected String secureRandomClass = null;
    protected String secureRandomAlgorithm = "SHA1PRNG";
    protected String secureRandomProvider = null;
    protected SessionIdGeneratorBase sessionIdGenerator = null;
    protected SingleSignOn sso = null;
    private volatile String jaspicAppContextID = null;
    private volatile AuthConfigProvider jaspicProvider = null;
    public boolean getAlwaysUseSession() {
        return alwaysUseSession;
    }
    public void setAlwaysUseSession ( boolean alwaysUseSession ) {
        this.alwaysUseSession = alwaysUseSession;
    }
    public boolean getCache() {
        return this.cache;
    }
    public void setCache ( boolean cache ) {
        this.cache = cache;
    }
    @Override
    public Container getContainer() {
        return this.context;
    }
    @Override
    public void setContainer ( Container container ) {
        if ( container != null && ! ( container instanceof Context ) ) {
            throw new IllegalArgumentException ( sm.getString ( "authenticator.notContext" ) );
        }
        super.setContainer ( container );
        this.context = ( Context ) container;
    }
    public boolean getDisableProxyCaching() {
        return disableProxyCaching;
    }
    public void setDisableProxyCaching ( boolean nocache ) {
        disableProxyCaching = nocache;
    }
    public boolean getSecurePagesWithPragma() {
        return securePagesWithPragma;
    }
    public void setSecurePagesWithPragma ( boolean securePagesWithPragma ) {
        this.securePagesWithPragma = securePagesWithPragma;
    }
    public boolean getChangeSessionIdOnAuthentication() {
        return changeSessionIdOnAuthentication;
    }
    public void setChangeSessionIdOnAuthentication ( boolean changeSessionIdOnAuthentication ) {
        this.changeSessionIdOnAuthentication = changeSessionIdOnAuthentication;
    }
    public String getSecureRandomClass() {
        return this.secureRandomClass;
    }
    public void setSecureRandomClass ( String secureRandomClass ) {
        this.secureRandomClass = secureRandomClass;
    }
    public String getSecureRandomAlgorithm() {
        return secureRandomAlgorithm;
    }
    public void setSecureRandomAlgorithm ( String secureRandomAlgorithm ) {
        this.secureRandomAlgorithm = secureRandomAlgorithm;
    }
    public String getSecureRandomProvider() {
        return secureRandomProvider;
    }
    public void setSecureRandomProvider ( String secureRandomProvider ) {
        this.secureRandomProvider = secureRandomProvider;
    }
    @Override
    public void invoke ( Request request, Response response ) throws IOException, ServletException {
        if ( log.isDebugEnabled() ) {
            log.debug ( "Security checking request " + request.getMethod() + " " +
                        request.getRequestURI() );
        }
        if ( cache ) {
            Principal principal = request.getUserPrincipal();
            if ( principal == null ) {
                Session session = request.getSessionInternal ( false );
                if ( session != null ) {
                    principal = session.getPrincipal();
                    if ( principal != null ) {
                        if ( log.isDebugEnabled() ) {
                            log.debug ( "We have cached auth type " + session.getAuthType() +
                                        " for principal " + principal );
                        }
                        request.setAuthType ( session.getAuthType() );
                        request.setUserPrincipal ( principal );
                    }
                }
            }
        }
        boolean authRequired = isContinuationRequired ( request );
        Wrapper wrapper = request.getMappingData().wrapper;
        if ( wrapper != null ) {
            wrapper.servletSecurityAnnotationScan();
        }
        Realm realm = this.context.getRealm();
        SecurityConstraint[] constraints = realm.findSecurityConstraints ( request, this.context );
        AuthConfigProvider jaspicProvider = getJaspicProvider();
        if ( jaspicProvider != null ) {
            authRequired = true;
        }
        if ( constraints == null && !context.getPreemptiveAuthentication() && !authRequired ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( " Not subject to any constraint" );
            }
            getNext().invoke ( request, response );
            return;
        }
        if ( constraints != null && disableProxyCaching &&
                !"POST".equalsIgnoreCase ( request.getMethod() ) ) {
            if ( securePagesWithPragma ) {
                response.setHeader ( "Pragma", "No-cache" );
                response.setHeader ( "Cache-Control", "no-cache" );
            } else {
                response.setHeader ( "Cache-Control", "private" );
            }
            response.setHeader ( "Expires", DATE_ONE );
        }
        if ( constraints != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( " Calling hasUserDataPermission()" );
            }
            if ( !realm.hasUserDataPermission ( request, response, constraints ) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( " Failed hasUserDataPermission() test" );
                }
                return;
            }
        }
        boolean hasAuthConstraint = false;
        if ( constraints != null ) {
            hasAuthConstraint = true;
            for ( int i = 0; i < constraints.length && hasAuthConstraint; i++ ) {
                if ( !constraints[i].getAuthConstraint() ) {
                    hasAuthConstraint = false;
                } else if ( !constraints[i].getAllRoles() &&
                            !constraints[i].getAuthenticatedUsers() ) {
                    String[] roles = constraints[i].findAuthRoles();
                    if ( roles == null || roles.length == 0 ) {
                        hasAuthConstraint = false;
                    }
                }
            }
        }
        if ( !authRequired && hasAuthConstraint ) {
            authRequired = true;
        }
        if ( !authRequired && context.getPreemptiveAuthentication() ) {
            authRequired =
                request.getCoyoteRequest().getMimeHeaders().getValue ( "authorization" ) != null;
        }
        if ( !authRequired && context.getPreemptiveAuthentication()
                && HttpServletRequest.CLIENT_CERT_AUTH.equals ( getAuthMethod() ) ) {
            X509Certificate[] certs = getRequestCertificates ( request );
            authRequired = certs != null && certs.length > 0;
        }
        JaspicState jaspicState = null;
        if ( authRequired ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( " Calling authenticate()" );
            }
            if ( jaspicProvider != null ) {
                jaspicState = getJaspicState ( jaspicProvider, request, response, hasAuthConstraint );
                if ( jaspicState == null ) {
                    return;
                }
            }
            if ( jaspicProvider == null && !doAuthenticate ( request, response ) ||
                    jaspicProvider != null &&
                    !authenticateJaspic ( request, response, jaspicState, false ) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( " Failed authenticate() test" );
                }
                return;
            }
        }
        if ( constraints != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( " Calling accessControl()" );
            }
            if ( !realm.hasResourcePermission ( request, response, constraints, this.context ) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( " Failed accessControl() test" );
                }
                return;
            }
        }
        if ( log.isDebugEnabled() ) {
            log.debug ( " Successfully passed all security constraints" );
        }
        getNext().invoke ( request, response );
        if ( jaspicProvider != null ) {
            secureResponseJspic ( request, response, jaspicState );
        }
    }
    @Override
    public boolean authenticate ( Request request, HttpServletResponse httpResponse )
    throws IOException {
        AuthConfigProvider jaspicProvider = getJaspicProvider();
        if ( jaspicProvider == null ) {
            return doAuthenticate ( request, httpResponse );
        } else {
            Response response = request.getResponse();
            JaspicState jaspicState = getJaspicState ( jaspicProvider, request, response, true );
            if ( jaspicState == null ) {
                return false;
            }
            boolean result = authenticateJaspic ( request, response, jaspicState, true );
            secureResponseJspic ( request, response, jaspicState );
            return result;
        }
    }
    private void secureResponseJspic ( Request request, Response response, JaspicState state ) {
        try {
            state.serverAuthContext.secureResponse ( state.messageInfo, null );
            request.setRequest ( ( HttpServletRequest ) state.messageInfo.getRequestMessage() );
            response.setResponse ( ( HttpServletResponse ) state.messageInfo.getResponseMessage() );
        } catch ( AuthException e ) {
            log.warn ( sm.getString ( "authenticator.jaspicSecureResponseFail" ), e );
        }
    }
    private JaspicState getJaspicState ( AuthConfigProvider jaspicProvider, Request request,
                                         Response response, boolean authMandatory ) throws IOException {
        JaspicState jaspicState = new JaspicState();
        jaspicState.messageInfo =
            new MessageInfoImpl ( request.getRequest(), response.getResponse(), authMandatory );
        try {
            ServerAuthConfig serverAuthConfig = jaspicProvider.getServerAuthConfig (
                                                    "HttpServlet", jaspicAppContextID, CallbackHandlerImpl.getInstance() );
            String authContextID = serverAuthConfig.getAuthContextID ( jaspicState.messageInfo );
            jaspicState.serverAuthContext = serverAuthConfig.getAuthContext ( authContextID, null, null );
        } catch ( AuthException e ) {
            log.warn ( sm.getString ( "authenticator.jaspicServerAuthContextFail" ), e );
            response.sendError ( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
            return null;
        }
        return jaspicState;
    }
    protected abstract boolean doAuthenticate ( Request request, HttpServletResponse response )
    throws IOException;
    protected boolean isContinuationRequired ( Request request ) {
        return false;
    }
    protected X509Certificate[] getRequestCertificates ( final Request request )
    throws IllegalStateException {
        X509Certificate certs[] =
            ( X509Certificate[] ) request.getAttribute ( Globals.CERTIFICATES_ATTR );
        if ( ( certs == null ) || ( certs.length < 1 ) ) {
            try {
                request.getCoyoteRequest().action ( ActionCode.REQ_SSL_CERTIFICATE, null );
                certs = ( X509Certificate[] ) request.getAttribute ( Globals.CERTIFICATES_ATTR );
            } catch ( IllegalStateException ise ) {
            }
        }
        return certs;
    }
    protected void associate ( String ssoId, Session session ) {
        if ( sso == null ) {
            return;
        }
        sso.associate ( ssoId, session );
    }
    private boolean authenticateJaspic ( Request request, Response response, JaspicState state,
                                         boolean requirePrincipal ) {
        boolean cachedAuth = checkForCachedAuthentication ( request, response, false );
        Subject client = new Subject();
        AuthStatus authStatus;
        try {
            authStatus = state.serverAuthContext.validateRequest ( state.messageInfo, client, null );
        } catch ( AuthException e ) {
            log.debug ( sm.getString ( "authenticator.loginFail" ), e );
            return false;
        }
        request.setRequest ( ( HttpServletRequest ) state.messageInfo.getRequestMessage() );
        response.setResponse ( ( HttpServletResponse ) state.messageInfo.getResponseMessage() );
        if ( authStatus == AuthStatus.SUCCESS ) {
            GenericPrincipal principal = getPrincipal ( client );
            if ( log.isDebugEnabled() ) {
                log.debug ( "Authenticated user: " + principal );
            }
            if ( principal == null ) {
                request.setUserPrincipal ( null );
                request.setAuthType ( null );
                if ( requirePrincipal ) {
                    return false;
                }
            } else if ( cachedAuth == false ||
                        !principal.getUserPrincipal().equals ( request.getUserPrincipal() ) ) {
                request.setNote ( Constants.REQ_JASPIC_SUBJECT_NOTE, client );
                @SuppressWarnings ( "rawtypes" )
                Map map = state.messageInfo.getMap();
                if ( map != null && map.containsKey ( "javax.servlet.http.registerSession" ) ) {
                    register ( request, response, principal, "JASPIC", null, null, true, true );
                } else {
                    register ( request, response, principal, "JASPIC", null, null );
                }
            }
            return true;
        }
        return false;
    }
    private GenericPrincipal getPrincipal ( Subject subject ) {
        if ( subject == null ) {
            return null;
        }
        Set<GenericPrincipal> principals = subject.getPrivateCredentials ( GenericPrincipal.class );
        if ( principals.isEmpty() ) {
            return null;
        }
        return principals.iterator().next();
    }
    protected boolean checkForCachedAuthentication ( Request request, HttpServletResponse response, boolean useSSO ) {
        Principal principal = request.getUserPrincipal();
        String ssoId = ( String ) request.getNote ( Constants.REQ_SSOID_NOTE );
        if ( principal != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "authenticator.check.found", principal.getName() ) );
            }
            if ( ssoId != null ) {
                associate ( ssoId, request.getSessionInternal ( true ) );
            }
            return true;
        }
        if ( useSSO && ssoId != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "authenticator.check.sso", ssoId ) );
            }
            if ( reauthenticateFromSSO ( ssoId, request ) ) {
                return true;
            }
        }
        if ( request.getCoyoteRequest().getRemoteUserNeedsAuthorization() ) {
            String username = request.getCoyoteRequest().getRemoteUser().toString();
            if ( username != null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "authenticator.check.authorize", username ) );
                }
                Principal authorized = context.getRealm().authenticate ( username );
                if ( authorized == null ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug ( sm.getString ( "authenticator.check.authorizeFail", username ) );
                    }
                    authorized = new GenericPrincipal ( username, null, null );
                }
                String authType = request.getAuthType();
                if ( authType == null || authType.length() == 0 ) {
                    authType = getAuthMethod();
                }
                register ( request, response, authorized, authType, username, null );
                return true;
            }
        }
        return false;
    }
    protected boolean reauthenticateFromSSO ( String ssoId, Request request ) {
        if ( sso == null || ssoId == null ) {
            return false;
        }
        boolean reauthenticated = false;
        Container parent = getContainer();
        if ( parent != null ) {
            Realm realm = parent.getRealm();
            if ( realm != null ) {
                reauthenticated = sso.reauthenticate ( ssoId, realm, request );
            }
        }
        if ( reauthenticated ) {
            associate ( ssoId, request.getSessionInternal ( true ) );
            if ( log.isDebugEnabled() ) {
                log.debug ( " Reauthenticated cached principal '" +
                            request.getUserPrincipal().getName() +
                            "' with auth type '" + request.getAuthType() + "'" );
            }
        }
        return reauthenticated;
    }
    public void register ( Request request, HttpServletResponse response, Principal principal,
                           String authType, String username, String password ) {
        register ( request, response, principal, authType, username, password, alwaysUseSession, cache );
    }
    private void register ( Request request, HttpServletResponse response, Principal principal,
                            String authType, String username, String password, boolean alwaysUseSession,
                            boolean cache ) {
        if ( log.isDebugEnabled() ) {
            String name = ( principal == null ) ? "none" : principal.getName();
            log.debug ( "Authenticated '" + name + "' with type '" + authType + "'" );
        }
        request.setAuthType ( authType );
        request.setUserPrincipal ( principal );
        Session session = request.getSessionInternal ( false );
        if ( session != null ) {
            if ( changeSessionIdOnAuthentication && principal != null ) {
                String oldId = null;
                if ( log.isDebugEnabled() ) {
                    oldId = session.getId();
                }
                Manager manager = request.getContext().getManager();
                manager.changeSessionId ( session );
                request.changeSessionId ( session.getId() );
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "authenticator.changeSessionId",
                                               oldId, session.getId() ) );
                }
            }
        } else if ( alwaysUseSession ) {
            session = request.getSessionInternal ( true );
        }
        if ( cache ) {
            if ( session != null ) {
                session.setAuthType ( authType );
                session.setPrincipal ( principal );
                if ( username != null ) {
                    session.setNote ( Constants.SESS_USERNAME_NOTE, username );
                } else {
                    session.removeNote ( Constants.SESS_USERNAME_NOTE );
                }
                if ( password != null ) {
                    session.setNote ( Constants.SESS_PASSWORD_NOTE, password );
                } else {
                    session.removeNote ( Constants.SESS_PASSWORD_NOTE );
                }
            }
        }
        if ( sso == null ) {
            return;
        }
        String ssoId = ( String ) request.getNote ( Constants.REQ_SSOID_NOTE );
        if ( ssoId == null ) {
            ssoId = sessionIdGenerator.generateSessionId();
            Cookie cookie = new Cookie ( Constants.SINGLE_SIGN_ON_COOKIE, ssoId );
            cookie.setMaxAge ( -1 );
            cookie.setPath ( "/" );
            cookie.setSecure ( request.isSecure() );
            String ssoDomain = sso.getCookieDomain();
            if ( ssoDomain != null ) {
                cookie.setDomain ( ssoDomain );
            }
            if ( request.getServletContext().getSessionCookieConfig().isHttpOnly()
                    || request.getContext().getUseHttpOnly() ) {
                cookie.setHttpOnly ( true );
            }
            response.addCookie ( cookie );
            sso.register ( ssoId, principal, authType, username, password );
            request.setNote ( Constants.REQ_SSOID_NOTE, ssoId );
        } else {
            if ( principal == null ) {
                sso.deregister ( ssoId );
                request.removeNote ( Constants.REQ_SSOID_NOTE );
                return;
            } else {
                sso.update ( ssoId, principal, authType, username, password );
            }
        }
        if ( session == null ) {
            session = request.getSessionInternal ( true );
        }
        sso.associate ( ssoId, session );
    }
    @Override
    public void login ( String username, String password, Request request ) throws ServletException {
        Principal principal = doLogin ( request, username, password );
        register ( request, request.getResponse(), principal, getAuthMethod(), username, password );
    }
    protected abstract String getAuthMethod();
    protected Principal doLogin ( Request request, String username, String password )
    throws ServletException {
        Principal p = context.getRealm().authenticate ( username, password );
        if ( p == null ) {
            throw new ServletException ( sm.getString ( "authenticator.loginFail" ) );
        }
        return p;
    }
    @Override
    public void logout ( Request request ) {
        AuthConfigProvider provider = getJaspicProvider();
        if ( provider != null ) {
            MessageInfo messageInfo = new MessageInfoImpl ( request, request.getResponse(), true );
            Subject client = ( Subject ) request.getNote ( Constants.REQ_JASPIC_SUBJECT_NOTE );
            if ( client == null ) {
                return;
            }
            ServerAuthContext serverAuthContext;
            try {
                ServerAuthConfig serverAuthConfig = provider.getServerAuthConfig ( "HttpServlet",
                                                    jaspicAppContextID, CallbackHandlerImpl.getInstance() );
                String authContextID = serverAuthConfig.getAuthContextID ( messageInfo );
                serverAuthContext = serverAuthConfig.getAuthContext ( authContextID, null, null );
                serverAuthContext.cleanSubject ( messageInfo, client );
            } catch ( AuthException e ) {
                log.debug ( sm.getString ( "authenticator.jaspicCleanSubjectFail" ), e );
            }
        }
        register ( request, request.getResponse(), null, null, null, null );
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        ServletContext servletContext = context.getServletContext();
        jaspicAppContextID = servletContext.getVirtualServerName() + " " +
                             servletContext.getContextPath();
        Container parent = context.getParent();
        while ( ( sso == null ) && ( parent != null ) ) {
            Valve valves[] = parent.getPipeline().getValves();
            for ( int i = 0; i < valves.length; i++ ) {
                if ( valves[i] instanceof SingleSignOn ) {
                    sso = ( SingleSignOn ) valves[i];
                    break;
                }
            }
            if ( sso == null ) {
                parent = parent.getParent();
            }
        }
        if ( log.isDebugEnabled() ) {
            if ( sso != null ) {
                log.debug ( "Found SingleSignOn Valve at " + sso );
            } else {
                log.debug ( "No SingleSignOn Valve is present" );
            }
        }
        sessionIdGenerator = new StandardSessionIdGenerator();
        sessionIdGenerator.setSecureRandomAlgorithm ( getSecureRandomAlgorithm() );
        sessionIdGenerator.setSecureRandomClass ( getSecureRandomClass() );
        sessionIdGenerator.setSecureRandomProvider ( getSecureRandomProvider() );
        super.startInternal();
    }
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        super.stopInternal();
        sso = null;
    }
    private AuthConfigProvider getJaspicProvider() {
        AuthConfigProvider provider = jaspicProvider;
        if ( provider == null ) {
            AuthConfigFactory factory = AuthConfigFactory.getFactory();
            provider = factory.getConfigProvider ( "HttpServlet", jaspicAppContextID, this );
            if ( provider != null ) {
                jaspicProvider = provider;
            }
        }
        return provider;
    }
    @Override
    public void notify ( String layer, String appContext ) {
        AuthConfigFactory factory = AuthConfigFactory.getFactory();
        AuthConfigProvider provider = factory.getConfigProvider ( "HttpServlet", jaspicAppContextID,
                                      this );
        jaspicProvider = provider;
    }
    private static class JaspicState {
        public MessageInfo messageInfo = null;
        public ServerAuthContext serverAuthContext = null;
    }
}
