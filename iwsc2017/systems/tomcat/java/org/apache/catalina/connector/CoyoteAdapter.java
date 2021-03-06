package org.apache.catalina.connector;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.ReadListener;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.SessionTrackingMode;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Authenticator;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.Wrapper;
import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.core.AsyncContextImpl;
import org.apache.catalina.util.ServerInfo;
import org.apache.catalina.util.SessionConfig;
import org.apache.catalina.util.URLEncoder;
import org.apache.coyote.ActionCode;
import org.apache.coyote.Adapter;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.buf.B2CConverter;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.CharChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.ServerCookie;
import org.apache.tomcat.util.http.ServerCookies;
import org.apache.tomcat.util.net.SSLSupport;
import org.apache.tomcat.util.net.SocketEvent;
import org.apache.tomcat.util.res.StringManager;
public class CoyoteAdapter implements Adapter {
    private static final Log log = LogFactory.getLog ( CoyoteAdapter.class );
    private static final String POWERED_BY = "Servlet/4.0 JSP/2.3 " +
            "(" + ServerInfo.getServerInfo() + " Java/" +
            System.getProperty ( "java.vm.vendor" ) + "/" +
            System.getProperty ( "java.runtime.version" ) + ")";
    private static final EnumSet<SessionTrackingMode> SSL_ONLY =
        EnumSet.of ( SessionTrackingMode.SSL );
    public static final int ADAPTER_NOTES = 1;
    protected static final boolean ALLOW_BACKSLASH =
        Boolean.parseBoolean ( System.getProperty ( "org.apache.catalina.connector.CoyoteAdapter.ALLOW_BACKSLASH", "false" ) );
    private static final ThreadLocal<String> THREAD_NAME =
    new ThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return Thread.currentThread().getName();
        }
    };
    public CoyoteAdapter ( Connector connector ) {
        super();
        this.connector = connector;
    }
    private final Connector connector;
    protected static final StringManager sm = StringManager.getManager ( CoyoteAdapter.class );
    @Override
    public boolean asyncDispatch ( org.apache.coyote.Request req,
                                   org.apache.coyote.Response res, SocketEvent status ) throws Exception {
        Request request = ( Request ) req.getNote ( ADAPTER_NOTES );
        Response response = ( Response ) res.getNote ( ADAPTER_NOTES );
        if ( request == null ) {
            throw new IllegalStateException (
                "Dispatch may only happen on an existing request." );
        }
        boolean success = true;
        AsyncContextImpl asyncConImpl = request.getAsyncContextInternal();
        req.getRequestProcessor().setWorkerThreadName ( Thread.currentThread().getName() );
        try {
            if ( !request.isAsync() ) {
                Context ctxt = request.getMappingData().context;
                if ( ctxt != null ) {
                    ctxt.fireRequestDestroyEvent ( request );
                }
                response.setSuspended ( false );
            }
            if ( status == SocketEvent.TIMEOUT ) {
                if ( !asyncConImpl.timeout() ) {
                    asyncConImpl.setErrorState ( null, false );
                }
            } else if ( status == SocketEvent.ERROR ) {
                success = false;
                Throwable t = ( Throwable ) req.getAttribute ( RequestDispatcher.ERROR_EXCEPTION );
                req.getAttributes().remove ( RequestDispatcher.ERROR_EXCEPTION );
                ClassLoader oldCL = null;
                try {
                    oldCL = request.getContext().bind ( false, null );
                    if ( req.getReadListener() != null ) {
                        req.getReadListener().onError ( t );
                    }
                    if ( res.getWriteListener() != null ) {
                        res.getWriteListener().onError ( t );
                    }
                } finally {
                    request.getContext().unbind ( false, oldCL );
                }
                if ( t != null ) {
                    asyncConImpl.setErrorState ( t, true );
                }
            }
            if ( !request.isAsyncDispatching() && request.isAsync() ) {
                WriteListener writeListener = res.getWriteListener();
                ReadListener readListener = req.getReadListener();
                if ( writeListener != null && status == SocketEvent.OPEN_WRITE ) {
                    ClassLoader oldCL = null;
                    try {
                        oldCL = request.getContext().bind ( false, null );
                        res.onWritePossible();
                        if ( request.isFinished() && req.sendAllDataReadEvent() &&
                                readListener != null ) {
                            readListener.onAllDataRead();
                        }
                    } catch ( Throwable t ) {
                        ExceptionUtils.handleThrowable ( t );
                        writeListener.onError ( t );
                        success = false;
                    } finally {
                        request.getContext().unbind ( false, oldCL );
                    }
                } else if ( readListener != null && status == SocketEvent.OPEN_READ ) {
                    ClassLoader oldCL = null;
                    try {
                        oldCL = request.getContext().bind ( false, null );
                        if ( !request.isFinished() ) {
                            readListener.onDataAvailable();
                        }
                        if ( request.isFinished() && req.sendAllDataReadEvent() ) {
                            readListener.onAllDataRead();
                        }
                    } catch ( Throwable t ) {
                        ExceptionUtils.handleThrowable ( t );
                        readListener.onError ( t );
                        success = false;
                    } finally {
                        request.getContext().unbind ( false, oldCL );
                    }
                }
            }
            if ( !request.isAsyncDispatching() && request.isAsync() &&
                    response.isErrorReportRequired() ) {
                connector.getService().getContainer().getPipeline().getFirst().invoke ( request, response );
            }
            if ( request.isAsyncDispatching() ) {
                connector.getService().getContainer().getPipeline().getFirst().invoke ( request, response );
                Throwable t = ( Throwable ) request.getAttribute (
                                  RequestDispatcher.ERROR_EXCEPTION );
                if ( t != null ) {
                    asyncConImpl.setErrorState ( t, true );
                }
            }
            if ( !request.isAsync() ) {
                request.finishRequest();
                response.finishResponse();
            }
            AtomicBoolean error = new AtomicBoolean ( false );
            res.action ( ActionCode.IS_ERROR, error );
            if ( error.get() ) {
                if ( request.isAsyncCompleting() ) {
                    res.action ( ActionCode.ASYNC_POST_PROCESS,  null );
                }
                success = false;
            }
        } catch ( IOException e ) {
            success = false;
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            success = false;
            log.error ( sm.getString ( "coyoteAdapter.asyncDispatch" ), t );
        } finally {
            if ( !success ) {
                res.setStatus ( 500 );
            }
            if ( !success || !request.isAsync() ) {
                long time = 0;
                if ( req.getStartTime() != -1 ) {
                    time = System.currentTimeMillis() - req.getStartTime();
                }
                if ( request.getMappingData().context != null ) {
                    request.getMappingData().context.logAccess ( request, response, time, false );
                } else {
                    log ( req, res, time );
                }
            }
            req.getRequestProcessor().setWorkerThreadName ( null );
            if ( !success || !request.isAsync() ) {
                request.recycle();
                response.recycle();
            }
        }
        return success;
    }
    @Override
    public void service ( org.apache.coyote.Request req,
                          org.apache.coyote.Response res )
    throws Exception {
        Request request = ( Request ) req.getNote ( ADAPTER_NOTES );
        Response response = ( Response ) res.getNote ( ADAPTER_NOTES );
        if ( request == null ) {
            request = connector.createRequest();
            request.setCoyoteRequest ( req );
            response = connector.createResponse();
            response.setCoyoteResponse ( res );
            request.setResponse ( response );
            response.setRequest ( request );
            req.setNote ( ADAPTER_NOTES, request );
            res.setNote ( ADAPTER_NOTES, response );
            req.getParameters().setQueryStringEncoding
            ( connector.getURIEncoding() );
        }
        if ( connector.getXpoweredBy() ) {
            response.addHeader ( "X-Powered-By", POWERED_BY );
        }
        boolean async = false;
        boolean postParseSuccess = false;
        try {
            req.getRequestProcessor().setWorkerThreadName ( THREAD_NAME.get() );
            postParseSuccess = postParseRequest ( req, request, res, response );
            if ( postParseSuccess ) {
                request.setAsyncSupported ( connector.getService().getContainer().getPipeline().isAsyncSupported() );
                connector.getService().getContainer().getPipeline().getFirst().invoke ( request, response );
            }
            if ( request.isAsync() ) {
                async = true;
                ReadListener readListener = req.getReadListener();
                if ( readListener != null && request.isFinished() ) {
                    ClassLoader oldCL = null;
                    try {
                        oldCL = request.getContext().bind ( false, null );
                        if ( req.sendAllDataReadEvent() ) {
                            req.getReadListener().onAllDataRead();
                        }
                    } finally {
                        request.getContext().unbind ( false, oldCL );
                    }
                }
                Throwable throwable =
                    ( Throwable ) request.getAttribute ( RequestDispatcher.ERROR_EXCEPTION );
                if ( !request.isAsyncCompleting() && throwable != null ) {
                    request.getAsyncContextInternal().setErrorState ( throwable, true );
                }
            } else {
                request.finishRequest();
                response.finishResponse();
            }
        } catch ( IOException e ) {
        } finally {
            if ( !async && postParseSuccess ) {
                request.getMappingData().context.logAccess ( request, response,
                        System.currentTimeMillis() - req.getStartTime(), false );
            }
            req.getRequestProcessor().setWorkerThreadName ( null );
            AtomicBoolean error = new AtomicBoolean ( false );
            res.action ( ActionCode.IS_ERROR, error );
            if ( !async || error.get() ) {
                request.recycle();
                response.recycle();
            }
        }
    }
    @Override
    public boolean prepare ( org.apache.coyote.Request req, org.apache.coyote.Response res )
    throws IOException, ServletException {
        Request request = ( Request ) req.getNote ( ADAPTER_NOTES );
        Response response = ( Response ) res.getNote ( ADAPTER_NOTES );
        return postParseRequest ( req, request, res, response );
    }
    @Override
    public void log ( org.apache.coyote.Request req,
                      org.apache.coyote.Response res, long time ) {
        Request request = ( Request ) req.getNote ( ADAPTER_NOTES );
        Response response = ( Response ) res.getNote ( ADAPTER_NOTES );
        if ( request == null ) {
            request = connector.createRequest();
            request.setCoyoteRequest ( req );
            response = connector.createResponse();
            response.setCoyoteResponse ( res );
            request.setResponse ( response );
            response.setRequest ( request );
            req.setNote ( ADAPTER_NOTES, request );
            res.setNote ( ADAPTER_NOTES, response );
            req.getParameters().setQueryStringEncoding
            ( connector.getURIEncoding() );
        }
        try {
            boolean logged = false;
            if ( request.mappingData != null ) {
                if ( request.mappingData.context != null ) {
                    logged = true;
                    request.mappingData.context.logAccess (
                        request, response, time, true );
                } else if ( request.mappingData.host != null ) {
                    logged = true;
                    request.mappingData.host.logAccess (
                        request, response, time, true );
                }
            }
            if ( !logged ) {
                connector.getService().getContainer().logAccess (
                    request, response, time, true );
            }
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            log.warn ( sm.getString ( "coyoteAdapter.accesslogFail" ), t );
        } finally {
            request.recycle();
            response.recycle();
        }
    }
    private static class RecycleRequiredException extends Exception {
        private static final long serialVersionUID = 1L;
    }
    @Override
    public void checkRecycled ( org.apache.coyote.Request req,
                                org.apache.coyote.Response res ) {
        Request request = ( Request ) req.getNote ( ADAPTER_NOTES );
        Response response = ( Response ) res.getNote ( ADAPTER_NOTES );
        String messageKey = null;
        if ( request != null && request.getHost() != null ) {
            messageKey = "coyoteAdapter.checkRecycled.request";
        } else if ( response != null && response.getContentWritten() != 0 ) {
            messageKey = "coyoteAdapter.checkRecycled.response";
        }
        if ( messageKey != null ) {
            log ( req, res, 0L );
            if ( connector.getState().isAvailable() ) {
                if ( log.isInfoEnabled() ) {
                    log.info ( sm.getString ( messageKey ),
                               new RecycleRequiredException() );
                }
            } else {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( messageKey ),
                                new RecycleRequiredException() );
                }
            }
        }
    }
    @Override
    public String getDomain() {
        return connector.getDomain();
    }
    protected boolean postParseRequest ( org.apache.coyote.Request req, Request request,
                                         org.apache.coyote.Response res, Response response ) throws IOException, ServletException {
        if ( req.scheme().isNull() ) {
            req.scheme().setString ( connector.getScheme() );
            request.setSecure ( connector.getSecure() );
        } else {
            request.setSecure ( req.scheme().equals ( "https" ) );
        }
        String proxyName = connector.getProxyName();
        int proxyPort = connector.getProxyPort();
        if ( proxyPort != 0 ) {
            req.setServerPort ( proxyPort );
        } else if ( req.getServerPort() == -1 ) {
            if ( req.scheme().equals ( "https" ) ) {
                req.setServerPort ( 443 );
            } else {
                req.setServerPort ( 80 );
            }
        }
        if ( proxyName != null ) {
            req.serverName().setString ( proxyName );
        }
        MessageBytes undecodedURI = req.requestURI();
        if ( undecodedURI.equals ( "*" ) ) {
            if ( req.method().equalsIgnoreCase ( "OPTIONS" ) ) {
                StringBuilder allow = new StringBuilder();
                allow.append ( "GET, HEAD, POST, PUT, DELETE" );
                if ( connector.getAllowTrace() ) {
                    allow.append ( ", TRACE" );
                }
                allow.append ( ", OPTIONS" );
                res.setHeader ( "Allow", allow.toString() );
            } else {
                res.setStatus ( 404 );
                res.setMessage ( "Not found" );
            }
            connector.getService().getContainer().logAccess (
                request, response, 0, true );
            return false;
        }
        MessageBytes decodedURI = req.decodedURI();
        if ( undecodedURI.getType() == MessageBytes.T_BYTES ) {
            decodedURI.duplicate ( undecodedURI );
            parsePathParameters ( req, request );
            try {
                req.getURLDecoder().convert ( decodedURI, false );
            } catch ( IOException ioe ) {
                res.setStatus ( 400 );
                res.setMessage ( "Invalid URI: " + ioe.getMessage() );
                connector.getService().getContainer().logAccess (
                    request, response, 0, true );
                return false;
            }
            if ( !normalize ( req.decodedURI() ) ) {
                res.setStatus ( 400 );
                res.setMessage ( "Invalid URI" );
                connector.getService().getContainer().logAccess (
                    request, response, 0, true );
                return false;
            }
            convertURI ( decodedURI, request );
            if ( !checkNormalize ( req.decodedURI() ) ) {
                res.setStatus ( 400 );
                res.setMessage ( "Invalid URI character encoding" );
                connector.getService().getContainer().logAccess (
                    request, response, 0, true );
                return false;
            }
        } else {
            decodedURI.toChars();
            CharChunk uriCC = decodedURI.getCharChunk();
            int semicolon = uriCC.indexOf ( ';' );
            if ( semicolon > 0 ) {
                decodedURI.setChars
                ( uriCC.getBuffer(), uriCC.getStart(), semicolon );
            }
        }
        MessageBytes serverName;
        if ( connector.getUseIPVHosts() ) {
            serverName = req.localName();
            if ( serverName.isNull() ) {
                res.action ( ActionCode.REQ_LOCAL_NAME_ATTRIBUTE, null );
            }
        } else {
            serverName = req.serverName();
        }
        String version = null;
        Context versionContext = null;
        boolean mapRequired = true;
        while ( mapRequired ) {
            connector.getService().getMapper().map ( serverName, decodedURI,
                    version, request.getMappingData() );
            if ( request.getContext() == null ) {
                res.setStatus ( 404 );
                res.setMessage ( "Not found" );
                Host host = request.getHost();
                if ( host != null ) {
                    host.logAccess ( request, response, 0, true );
                }
                return false;
            }
            String sessionID;
            if ( request.getServletContext().getEffectiveSessionTrackingModes()
                    .contains ( SessionTrackingMode.URL ) ) {
                sessionID = request.getPathParameter (
                                SessionConfig.getSessionUriParamName (
                                    request.getContext() ) );
                if ( sessionID != null ) {
                    request.setRequestedSessionId ( sessionID );
                    request.setRequestedSessionURL ( true );
                }
            }
            parseSessionCookiesId ( request );
            parseSessionSslId ( request );
            sessionID = request.getRequestedSessionId();
            mapRequired = false;
            if ( version != null && request.getContext() == versionContext ) {
            } else {
                version = null;
                versionContext = null;
                Context[] contexts = request.getMappingData().contexts;
                if ( contexts != null && sessionID != null ) {
                    for ( int i = ( contexts.length ); i > 0; i-- ) {
                        Context ctxt = contexts[i - 1];
                        if ( ctxt.getManager().findSession ( sessionID ) != null ) {
                            if ( !ctxt.equals ( request.getMappingData().context ) ) {
                                version = ctxt.getWebappVersion();
                                versionContext = ctxt;
                                request.getMappingData().recycle();
                                mapRequired = true;
                                request.recycleSessionInfo();
                                request.recycleCookieInfo ( true );
                            }
                            break;
                        }
                    }
                }
            }
            if ( !mapRequired && request.getContext().getPaused() ) {
                try {
                    Thread.sleep ( 1000 );
                } catch ( InterruptedException e ) {
                }
                request.getMappingData().recycle();
                mapRequired = true;
            }
        }
        MessageBytes redirectPathMB = request.getMappingData().redirectPath;
        if ( !redirectPathMB.isNull() ) {
            String redirectPath = URLEncoder.DEFAULT.encode ( redirectPathMB.toString(), "UTF-8" );
            String query = request.getQueryString();
            if ( request.isRequestedSessionIdFromURL() ) {
                redirectPath = redirectPath + ";" +
                               SessionConfig.getSessionUriParamName (
                                   request.getContext() ) +
                               "=" + request.getRequestedSessionId();
            }
            if ( query != null ) {
                redirectPath = redirectPath + "?" + query;
            }
            response.sendRedirect ( redirectPath );
            request.getContext().logAccess ( request, response, 0, true );
            return false;
        }
        if ( !connector.getAllowTrace()
                && req.method().equalsIgnoreCase ( "TRACE" ) ) {
            Wrapper wrapper = request.getWrapper();
            String header = null;
            if ( wrapper != null ) {
                String[] methods = wrapper.getServletMethods();
                if ( methods != null ) {
                    for ( int i = 0; i < methods.length; i++ ) {
                        if ( "TRACE".equals ( methods[i] ) ) {
                            continue;
                        }
                        if ( header == null ) {
                            header = methods[i];
                        } else {
                            header += ", " + methods[i];
                        }
                    }
                }
            }
            res.setStatus ( 405 );
            res.addHeader ( "Allow", header );
            res.setMessage ( "TRACE method is not allowed" );
            request.getContext().logAccess ( request, response, 0, true );
            return false;
        }
        doConnectorAuthenticationAuthorization ( req, request );
        return true;
    }
    private void doConnectorAuthenticationAuthorization ( org.apache.coyote.Request req, Request request ) {
        String username = req.getRemoteUser().toString();
        if ( username != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "coyoteAdapter.authenticate", username ) );
            }
            if ( req.getRemoteUserNeedsAuthorization() ) {
                Authenticator authenticator = request.getContext().getAuthenticator();
                if ( authenticator == null ) {
                    request.setUserPrincipal ( new CoyotePrincipal ( username ) );
                } else if ( ! ( authenticator instanceof AuthenticatorBase ) ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug ( sm.getString ( "coyoteAdapter.authorize", username ) );
                    }
                    request.setUserPrincipal (
                        request.getContext().getRealm().authenticate ( username ) );
                }
            } else {
                request.setUserPrincipal ( new CoyotePrincipal ( username ) );
            }
        }
        String authtype = req.getAuthType().toString();
        if ( authtype != null ) {
            request.setAuthType ( authtype );
        }
    }
    protected void parsePathParameters ( org.apache.coyote.Request req,
                                         Request request ) {
        req.decodedURI().toBytes();
        ByteChunk uriBC = req.decodedURI().getByteChunk();
        int semicolon = uriBC.indexOf ( ';', 0 );
        if ( semicolon == -1 ) {
            return;
        }
        String enc = connector.getURIEncodingLower();
        if ( enc == null ) {
            enc = "iso-8859-1";
        }
        Charset charset = null;
        try {
            charset = B2CConverter.getCharsetLower ( enc );
        } catch ( UnsupportedEncodingException e1 ) {
            log.warn ( sm.getString ( "coyoteAdapter.parsePathParam",
                                      enc ) );
        }
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "coyoteAdapter.debug", "uriBC",
                                       uriBC.toString() ) );
            log.debug ( sm.getString ( "coyoteAdapter.debug", "semicolon",
                                       String.valueOf ( semicolon ) ) );
            log.debug ( sm.getString ( "coyoteAdapter.debug", "enc", enc ) );
        }
        while ( semicolon > -1 ) {
            int start = uriBC.getStart();
            int end = uriBC.getEnd();
            int pathParamStart = semicolon + 1;
            int pathParamEnd = ByteChunk.findBytes ( uriBC.getBuffer(),
                               start + pathParamStart, end,
                               new byte[] {';', '/'} );
            String pv = null;
            if ( pathParamEnd >= 0 ) {
                if ( charset != null ) {
                    pv = new String ( uriBC.getBuffer(), start + pathParamStart,
                                      pathParamEnd - pathParamStart, charset );
                }
                byte[] buf = uriBC.getBuffer();
                for ( int i = 0; i < end - start - pathParamEnd; i++ ) {
                    buf[start + semicolon + i]
                        = buf[start + i + pathParamEnd];
                }
                uriBC.setBytes ( buf, start,
                                 end - start - pathParamEnd + semicolon );
            } else {
                if ( charset != null ) {
                    pv = new String ( uriBC.getBuffer(), start + pathParamStart,
                                      ( end - start ) - pathParamStart, charset );
                }
                uriBC.setEnd ( start + semicolon );
            }
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "coyoteAdapter.debug", "pathParamStart",
                                           String.valueOf ( pathParamStart ) ) );
                log.debug ( sm.getString ( "coyoteAdapter.debug", "pathParamEnd",
                                           String.valueOf ( pathParamEnd ) ) );
                log.debug ( sm.getString ( "coyoteAdapter.debug", "pv", pv ) );
            }
            if ( pv != null ) {
                int equals = pv.indexOf ( '=' );
                if ( equals > -1 ) {
                    String name = pv.substring ( 0, equals );
                    String value = pv.substring ( equals + 1 );
                    request.addPathParameter ( name, value );
                    if ( log.isDebugEnabled() ) {
                        log.debug ( sm.getString ( "coyoteAdapter.debug", "equals",
                                                   String.valueOf ( equals ) ) );
                        log.debug ( sm.getString ( "coyoteAdapter.debug", "name",
                                                   name ) );
                        log.debug ( sm.getString ( "coyoteAdapter.debug", "value",
                                                   value ) );
                    }
                }
            }
            semicolon = uriBC.indexOf ( ';', semicolon );
        }
    }
    protected void parseSessionSslId ( Request request ) {
        if ( request.getRequestedSessionId() == null &&
                SSL_ONLY.equals ( request.getServletContext()
                                  .getEffectiveSessionTrackingModes() ) &&
                request.connector.secure ) {
            request.setRequestedSessionId (
                request.getAttribute ( SSLSupport.SESSION_ID_KEY ).toString() );
            request.setRequestedSessionSSL ( true );
        }
    }
    protected void parseSessionCookiesId ( Request request ) {
        Context context = request.getMappingData().context;
        if ( context != null && !context.getServletContext()
                .getEffectiveSessionTrackingModes().contains (
                    SessionTrackingMode.COOKIE ) ) {
            return;
        }
        ServerCookies serverCookies = request.getServerCookies();
        int count = serverCookies.getCookieCount();
        if ( count <= 0 ) {
            return;
        }
        String sessionCookieName = SessionConfig.getSessionCookieName ( context );
        for ( int i = 0; i < count; i++ ) {
            ServerCookie scookie = serverCookies.getCookie ( i );
            if ( scookie.getName().equals ( sessionCookieName ) ) {
                if ( !request.isRequestedSessionIdFromCookie() ) {
                    convertMB ( scookie.getValue() );
                    request.setRequestedSessionId
                    ( scookie.getValue().toString() );
                    request.setRequestedSessionCookie ( true );
                    request.setRequestedSessionURL ( false );
                    if ( log.isDebugEnabled() ) {
                        log.debug ( " Requested cookie session id is " +
                                    request.getRequestedSessionId() );
                    }
                } else {
                    if ( !request.isRequestedSessionIdValid() ) {
                        convertMB ( scookie.getValue() );
                        request.setRequestedSessionId
                        ( scookie.getValue().toString() );
                    }
                }
            }
        }
    }
    protected void convertURI ( MessageBytes uri, Request request ) throws IOException {
        ByteChunk bc = uri.getByteChunk();
        int length = bc.getLength();
        CharChunk cc = uri.getCharChunk();
        cc.allocate ( length, -1 );
        String enc = connector.getURIEncoding();
        if ( enc != null ) {
            B2CConverter conv = request.getURIConverter();
            try {
                if ( conv == null ) {
                    conv = new B2CConverter ( B2CConverter.getCharset ( enc ), true );
                    request.setURIConverter ( conv );
                } else {
                    conv.recycle();
                }
            } catch ( IOException e ) {
                log.error ( sm.getString ( "coyoteAdapter.invalidEncoding" ) );
                connector.setURIEncoding ( null );
            }
            if ( conv != null ) {
                try {
                    conv.convert ( bc, cc, true );
                    uri.setChars ( cc.getBuffer(), cc.getStart(), cc.getLength() );
                    return;
                } catch ( IOException ioe ) {
                    request.getResponse().sendError (
                        HttpServletResponse.SC_BAD_REQUEST );
                }
            }
        }
        byte[] bbuf = bc.getBuffer();
        char[] cbuf = cc.getBuffer();
        int start = bc.getStart();
        for ( int i = 0; i < length; i++ ) {
            cbuf[i] = ( char ) ( bbuf[i + start] & 0xff );
        }
        uri.setChars ( cbuf, 0, length );
    }
    protected void convertMB ( MessageBytes mb ) {
        if ( mb.getType() != MessageBytes.T_BYTES ) {
            return;
        }
        ByteChunk bc = mb.getByteChunk();
        CharChunk cc = mb.getCharChunk();
        int length = bc.getLength();
        cc.allocate ( length, -1 );
        byte[] bbuf = bc.getBuffer();
        char[] cbuf = cc.getBuffer();
        int start = bc.getStart();
        for ( int i = 0; i < length; i++ ) {
            cbuf[i] = ( char ) ( bbuf[i + start] & 0xff );
        }
        mb.setChars ( cbuf, 0, length );
    }
    public static boolean normalize ( MessageBytes uriMB ) {
        ByteChunk uriBC = uriMB.getByteChunk();
        final byte[] b = uriBC.getBytes();
        final int start = uriBC.getStart();
        int end = uriBC.getEnd();
        if ( start == end ) {
            return false;
        }
        if ( ( end - start == 1 ) && b[start] == ( byte ) '*' ) {
            return true;
        }
        int pos = 0;
        int index = 0;
        for ( pos = start; pos < end; pos++ ) {
            if ( b[pos] == ( byte ) '\\' ) {
                if ( ALLOW_BACKSLASH ) {
                    b[pos] = ( byte ) '/';
                } else {
                    return false;
                }
            }
            if ( b[pos] == ( byte ) 0 ) {
                return false;
            }
        }
        if ( b[start] != ( byte ) '/' ) {
            return false;
        }
        for ( pos = start; pos < ( end - 1 ); pos++ ) {
            if ( b[pos] == ( byte ) '/' ) {
                while ( ( pos + 1 < end ) && ( b[pos + 1] == ( byte ) '/' ) ) {
                    copyBytes ( b, pos, pos + 1, end - pos - 1 );
                    end--;
                }
            }
        }
        if ( ( ( end - start ) >= 2 ) && ( b[end - 1] == ( byte ) '.' ) ) {
            if ( ( b[end - 2] == ( byte ) '/' )
                    || ( ( b[end - 2] == ( byte ) '.' )
                         && ( b[end - 3] == ( byte ) '/' ) ) ) {
                b[end] = ( byte ) '/';
                end++;
            }
        }
        uriBC.setEnd ( end );
        index = 0;
        while ( true ) {
            index = uriBC.indexOf ( "/./", 0, 3, index );
            if ( index < 0 ) {
                break;
            }
            copyBytes ( b, start + index, start + index + 2,
                        end - start - index - 2 );
            end = end - 2;
            uriBC.setEnd ( end );
        }
        index = 0;
        while ( true ) {
            index = uriBC.indexOf ( "/../", 0, 4, index );
            if ( index < 0 ) {
                break;
            }
            if ( index == 0 ) {
                return false;
            }
            int index2 = -1;
            for ( pos = start + index - 1; ( pos >= 0 ) && ( index2 < 0 ); pos -- ) {
                if ( b[pos] == ( byte ) '/' ) {
                    index2 = pos;
                }
            }
            copyBytes ( b, start + index2, start + index + 3,
                        end - start - index - 3 );
            end = end + index2 - index - 3;
            uriBC.setEnd ( end );
            index = index2;
        }
        return true;
    }
    public static boolean checkNormalize ( MessageBytes uriMB ) {
        CharChunk uriCC = uriMB.getCharChunk();
        char[] c = uriCC.getChars();
        int start = uriCC.getStart();
        int end = uriCC.getEnd();
        int pos = 0;
        for ( pos = start; pos < end; pos++ ) {
            if ( c[pos] == '\\' ) {
                return false;
            }
            if ( c[pos] == 0 ) {
                return false;
            }
        }
        for ( pos = start; pos < ( end - 1 ); pos++ ) {
            if ( c[pos] == '/' ) {
                if ( c[pos + 1] == '/' ) {
                    return false;
                }
            }
        }
        if ( ( ( end - start ) >= 2 ) && ( c[end - 1] == '.' ) ) {
            if ( ( c[end - 2] == '/' )
                    || ( ( c[end - 2] == '.' )
                         && ( c[end - 3] == '/' ) ) ) {
                return false;
            }
        }
        if ( uriCC.indexOf ( "/./", 0, 3, 0 ) >= 0 ) {
            return false;
        }
        if ( uriCC.indexOf ( "/../", 0, 4, 0 ) >= 0 ) {
            return false;
        }
        return true;
    }
    protected static void copyBytes ( byte[] b, int dest, int src, int len ) {
        for ( int pos = 0; pos < len; pos++ ) {
            b[pos + dest] = b[pos + src];
        }
    }
}
