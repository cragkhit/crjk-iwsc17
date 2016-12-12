package org.apache.catalina.authenticator;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Locale;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Manager;
import org.apache.catalina.Realm;
import org.apache.catalina.Session;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.coyote.ActionCode;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.tomcat.util.http.MimeHeaders;
public class FormAuthenticator
    extends AuthenticatorBase {
    private static final Log log = LogFactory.getLog ( FormAuthenticator.class );
    protected String characterEncoding = null;
    protected String landingPage = null;
    public String getCharacterEncoding() {
        return characterEncoding;
    }
    public void setCharacterEncoding ( String encoding ) {
        characterEncoding = encoding;
    }
    public String getLandingPage() {
        return landingPage;
    }
    public void setLandingPage ( String landingPage ) {
        this.landingPage = landingPage;
    }
    @Override
    protected boolean doAuthenticate ( Request request, HttpServletResponse response )
    throws IOException {
        if ( checkForCachedAuthentication ( request, response, true ) ) {
            return true;
        }
        Session session = null;
        Principal principal = null;
        if ( !cache ) {
            session = request.getSessionInternal ( true );
            if ( log.isDebugEnabled() ) {
                log.debug ( "Checking for reauthenticate in session " + session );
            }
            String username =
                ( String ) session.getNote ( Constants.SESS_USERNAME_NOTE );
            String password =
                ( String ) session.getNote ( Constants.SESS_PASSWORD_NOTE );
            if ( ( username != null ) && ( password != null ) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "Reauthenticating username '" + username + "'" );
                }
                principal =
                    context.getRealm().authenticate ( username, password );
                if ( principal != null ) {
                    session.setNote ( Constants.FORM_PRINCIPAL_NOTE, principal );
                    if ( !matchRequest ( request ) ) {
                        register ( request, response, principal,
                                   HttpServletRequest.FORM_AUTH,
                                   username, password );
                        return true;
                    }
                }
                if ( log.isDebugEnabled() ) {
                    log.debug ( "Reauthentication failed, proceed normally" );
                }
            }
        }
        if ( matchRequest ( request ) ) {
            session = request.getSessionInternal ( true );
            if ( log.isDebugEnabled() ) {
                log.debug ( "Restore request from session '"
                            + session.getIdInternal()
                            + "'" );
            }
            principal = ( Principal )
                        session.getNote ( Constants.FORM_PRINCIPAL_NOTE );
            register ( request, response, principal, HttpServletRequest.FORM_AUTH,
                       ( String ) session.getNote ( Constants.SESS_USERNAME_NOTE ),
                       ( String ) session.getNote ( Constants.SESS_PASSWORD_NOTE ) );
            if ( cache ) {
                session.removeNote ( Constants.SESS_USERNAME_NOTE );
                session.removeNote ( Constants.SESS_PASSWORD_NOTE );
            }
            if ( restoreRequest ( request, session ) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "Proceed to restored request" );
                }
                return true;
            } else {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "Restore of original request failed" );
                }
                response.sendError ( HttpServletResponse.SC_BAD_REQUEST );
                return false;
            }
        }
        String contextPath = request.getContextPath();
        String requestURI = request.getDecodedRequestURI();
        boolean loginAction =
            requestURI.startsWith ( contextPath ) &&
            requestURI.endsWith ( Constants.FORM_ACTION );
        LoginConfig config = context.getLoginConfig();
        if ( !loginAction ) {
            if ( request.getServletPath().length() == 0 && request.getPathInfo() == null ) {
                StringBuilder location = new StringBuilder ( requestURI );
                location.append ( '/' );
                if ( request.getQueryString() != null ) {
                    location.append ( '?' );
                    location.append ( request.getQueryString() );
                }
                response.sendRedirect ( response.encodeRedirectURL ( location.toString() ) );
                return false;
            }
            session = request.getSessionInternal ( true );
            if ( log.isDebugEnabled() ) {
                log.debug ( "Save request in session '" + session.getIdInternal() + "'" );
            }
            try {
                saveRequest ( request, session );
            } catch ( IOException ioe ) {
                log.debug ( "Request body too big to save during authentication" );
                response.sendError ( HttpServletResponse.SC_FORBIDDEN,
                                     sm.getString ( "authenticator.requestBodyTooBig" ) );
                return false;
            }
            forwardToLoginPage ( request, response, config );
            return false;
        }
        request.getResponse().sendAcknowledgement();
        Realm realm = context.getRealm();
        if ( characterEncoding != null ) {
            request.setCharacterEncoding ( characterEncoding );
        }
        String username = request.getParameter ( Constants.FORM_USERNAME );
        String password = request.getParameter ( Constants.FORM_PASSWORD );
        if ( log.isDebugEnabled() ) {
            log.debug ( "Authenticating username '" + username + "'" );
        }
        principal = realm.authenticate ( username, password );
        if ( principal == null ) {
            forwardToErrorPage ( request, response, config );
            return false;
        }
        if ( log.isDebugEnabled() ) {
            log.debug ( "Authentication of '" + username + "' was successful" );
        }
        if ( session == null ) {
            session = request.getSessionInternal ( false );
        }
        if ( session == null ) {
            if ( containerLog.isDebugEnabled() ) {
                containerLog.debug
                ( "User took so long to log on the session expired" );
            }
            if ( landingPage == null ) {
                response.sendError ( HttpServletResponse.SC_REQUEST_TIMEOUT,
                                     sm.getString ( "authenticator.sessionExpired" ) );
            } else {
                String uri = request.getContextPath() + landingPage;
                SavedRequest saved = new SavedRequest();
                saved.setMethod ( "GET" );
                saved.setRequestURI ( uri );
                saved.setDecodedRequestURI ( uri );
                request.getSessionInternal ( true ).setNote (
                    Constants.FORM_REQUEST_NOTE, saved );
                response.sendRedirect ( response.encodeRedirectURL ( uri ) );
            }
            return false;
        }
        session.setNote ( Constants.FORM_PRINCIPAL_NOTE, principal );
        session.setNote ( Constants.SESS_USERNAME_NOTE, username );
        session.setNote ( Constants.SESS_PASSWORD_NOTE, password );
        requestURI = savedRequestURL ( session );
        if ( log.isDebugEnabled() ) {
            log.debug ( "Redirecting to original '" + requestURI + "'" );
        }
        if ( requestURI == null ) {
            if ( landingPage == null ) {
                response.sendError ( HttpServletResponse.SC_BAD_REQUEST,
                                     sm.getString ( "authenticator.formlogin" ) );
            } else {
                String uri = request.getContextPath() + landingPage;
                SavedRequest saved = new SavedRequest();
                saved.setMethod ( "GET" );
                saved.setRequestURI ( uri );
                saved.setDecodedRequestURI ( uri );
                session.setNote ( Constants.FORM_REQUEST_NOTE, saved );
                response.sendRedirect ( response.encodeRedirectURL ( uri ) );
            }
        } else {
            Response internalResponse = request.getResponse();
            String location = response.encodeRedirectURL ( requestURI );
            if ( "HTTP/1.1".equals ( request.getProtocol() ) ) {
                internalResponse.sendRedirect ( location,
                                                HttpServletResponse.SC_SEE_OTHER );
            } else {
                internalResponse.sendRedirect ( location,
                                                HttpServletResponse.SC_FOUND );
            }
        }
        return false;
    }
    @Override
    protected boolean isContinuationRequired ( Request request ) {
        String contextPath = this.context.getPath();
        String decodedRequestURI = request.getDecodedRequestURI();
        if ( decodedRequestURI.startsWith ( contextPath ) &&
                decodedRequestURI.endsWith ( Constants.FORM_ACTION ) ) {
            return true;
        }
        Session session = request.getSessionInternal ( false );
        if ( session != null ) {
            SavedRequest savedRequest = ( SavedRequest ) session.getNote ( Constants.FORM_REQUEST_NOTE );
            if ( savedRequest != null &&
                    decodedRequestURI.equals ( savedRequest.getDecodedRequestURI() ) ) {
                return true;
            }
        }
        return false;
    }
    @Override
    protected String getAuthMethod() {
        return HttpServletRequest.FORM_AUTH;
    }
    protected void forwardToLoginPage ( Request request,
                                        HttpServletResponse response, LoginConfig config )
    throws IOException {
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "formAuthenticator.forwardLogin",
                                       request.getRequestURI(), request.getMethod(),
                                       config.getLoginPage(), context.getName() ) );
        }
        String loginPage = config.getLoginPage();
        if ( loginPage == null || loginPage.length() == 0 ) {
            String msg = sm.getString ( "formAuthenticator.noLoginPage",
                                        context.getName() );
            log.warn ( msg );
            response.sendError ( HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                 msg );
            return;
        }
        if ( getChangeSessionIdOnAuthentication() ) {
            Session session = request.getSessionInternal ( false );
            if ( session != null ) {
                Manager manager = request.getContext().getManager();
                manager.changeSessionId ( session );
                request.changeSessionId ( session.getId() );
            }
        }
        String oldMethod = request.getMethod();
        request.getCoyoteRequest().method().setString ( "GET" );
        RequestDispatcher disp =
            context.getServletContext().getRequestDispatcher ( loginPage );
        try {
            if ( context.fireRequestInitEvent ( request ) ) {
                disp.forward ( request.getRequest(), response );
                context.fireRequestDestroyEvent ( request );
            }
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            String msg = sm.getString ( "formAuthenticator.forwardLoginFail" );
            log.warn ( msg, t );
            request.setAttribute ( RequestDispatcher.ERROR_EXCEPTION, t );
            response.sendError ( HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                 msg );
        } finally {
            request.getCoyoteRequest().method().setString ( oldMethod );
        }
    }
    protected void forwardToErrorPage ( Request request,
                                        HttpServletResponse response, LoginConfig config )
    throws IOException {
        String errorPage = config.getErrorPage();
        if ( errorPage == null || errorPage.length() == 0 ) {
            String msg = sm.getString ( "formAuthenticator.noErrorPage",
                                        context.getName() );
            log.warn ( msg );
            response.sendError ( HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                 msg );
            return;
        }
        RequestDispatcher disp =
            context.getServletContext().getRequestDispatcher
            ( config.getErrorPage() );
        try {
            if ( context.fireRequestInitEvent ( request ) ) {
                disp.forward ( request.getRequest(), response );
                context.fireRequestDestroyEvent ( request );
            }
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            String msg = sm.getString ( "formAuthenticator.forwardErrorFail" );
            log.warn ( msg, t );
            request.setAttribute ( RequestDispatcher.ERROR_EXCEPTION, t );
            response.sendError ( HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                 msg );
        }
    }
    protected boolean matchRequest ( Request request ) {
        Session session = request.getSessionInternal ( false );
        if ( session == null ) {
            return false;
        }
        SavedRequest sreq =
            ( SavedRequest ) session.getNote ( Constants.FORM_REQUEST_NOTE );
        if ( sreq == null ) {
            return false;
        }
        if ( session.getNote ( Constants.FORM_PRINCIPAL_NOTE ) == null ) {
            return false;
        }
        String decodedRequestURI = request.getDecodedRequestURI();
        if ( decodedRequestURI == null ) {
            return false;
        }
        return decodedRequestURI.equals ( sreq.getDecodedRequestURI() );
    }
    protected boolean restoreRequest ( Request request, Session session )
    throws IOException {
        SavedRequest saved = ( SavedRequest )
                             session.getNote ( Constants.FORM_REQUEST_NOTE );
        session.removeNote ( Constants.FORM_REQUEST_NOTE );
        session.removeNote ( Constants.FORM_PRINCIPAL_NOTE );
        if ( saved == null ) {
            return false;
        }
        byte[] buffer = new byte[4096];
        InputStream is = request.createInputStream();
        while ( is.read ( buffer ) >= 0 ) {
        }
        request.clearCookies();
        Iterator<Cookie> cookies = saved.getCookies();
        while ( cookies.hasNext() ) {
            request.addCookie ( cookies.next() );
        }
        String method = saved.getMethod();
        MimeHeaders rmh = request.getCoyoteRequest().getMimeHeaders();
        rmh.recycle();
        boolean cachable = "GET".equalsIgnoreCase ( method ) ||
                           "HEAD".equalsIgnoreCase ( method );
        Iterator<String> names = saved.getHeaderNames();
        while ( names.hasNext() ) {
            String name = names.next();
            if ( ! ( "If-Modified-Since".equalsIgnoreCase ( name ) ||
                     ( cachable && "If-None-Match".equalsIgnoreCase ( name ) ) ) ) {
                Iterator<String> values = saved.getHeaderValues ( name );
                while ( values.hasNext() ) {
                    rmh.addValue ( name ).setString ( values.next() );
                }
            }
        }
        request.clearLocales();
        Iterator<Locale> locales = saved.getLocales();
        while ( locales.hasNext() ) {
            request.addLocale ( locales.next() );
        }
        request.getCoyoteRequest().getParameters().recycle();
        request.getCoyoteRequest().getParameters().setQueryStringEncoding (
            request.getConnector().getURIEncoding() );
        ByteChunk body = saved.getBody();
        if ( body != null ) {
            request.getCoyoteRequest().action
            ( ActionCode.REQ_SET_BODY_REPLAY, body );
            MessageBytes contentType = MessageBytes.newInstance();
            String savedContentType = saved.getContentType();
            if ( savedContentType == null && "POST".equalsIgnoreCase ( method ) ) {
                savedContentType = "application/x-www-form-urlencoded";
            }
            contentType.setString ( savedContentType );
            request.getCoyoteRequest().setContentType ( contentType );
        }
        request.getCoyoteRequest().method().setString ( method );
        return true;
    }
    protected void saveRequest ( Request request, Session session )
    throws IOException {
        SavedRequest saved = new SavedRequest();
        Cookie cookies[] = request.getCookies();
        if ( cookies != null ) {
            for ( int i = 0; i < cookies.length; i++ ) {
                saved.addCookie ( cookies[i] );
            }
        }
        Enumeration<String> names = request.getHeaderNames();
        while ( names.hasMoreElements() ) {
            String name = names.nextElement();
            Enumeration<String> values = request.getHeaders ( name );
            while ( values.hasMoreElements() ) {
                String value = values.nextElement();
                saved.addHeader ( name, value );
            }
        }
        Enumeration<Locale> locales = request.getLocales();
        while ( locales.hasMoreElements() ) {
            Locale locale = locales.nextElement();
            saved.addLocale ( locale );
        }
        request.getResponse().sendAcknowledgement();
        ByteChunk body = new ByteChunk();
        body.setLimit ( request.getConnector().getMaxSavePostSize() );
        byte[] buffer = new byte[4096];
        int bytesRead;
        InputStream is = request.getInputStream();
        while ( ( bytesRead = is.read ( buffer ) ) >= 0 ) {
            body.append ( buffer, 0, bytesRead );
        }
        if ( body.getLength() > 0 ) {
            saved.setContentType ( request.getContentType() );
            saved.setBody ( body );
        }
        saved.setMethod ( request.getMethod() );
        saved.setQueryString ( request.getQueryString() );
        saved.setRequestURI ( request.getRequestURI() );
        saved.setDecodedRequestURI ( request.getDecodedRequestURI() );
        session.setNote ( Constants.FORM_REQUEST_NOTE, saved );
    }
    protected String savedRequestURL ( Session session ) {
        SavedRequest saved =
            ( SavedRequest ) session.getNote ( Constants.FORM_REQUEST_NOTE );
        if ( saved == null ) {
            return ( null );
        }
        StringBuilder sb = new StringBuilder ( saved.getRequestURI() );
        if ( saved.getQueryString() != null ) {
            sb.append ( '?' );
            sb.append ( saved.getQueryString() );
        }
        return ( sb.toString() );
    }
}
