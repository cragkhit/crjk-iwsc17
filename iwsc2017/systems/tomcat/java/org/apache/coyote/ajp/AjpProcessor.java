package org.apache.coyote.ajp;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.servlet.http.HttpServletResponse;
import org.apache.coyote.AbstractProcessor;
import org.apache.coyote.ActionCode;
import org.apache.coyote.ErrorState;
import org.apache.coyote.InputBuffer;
import org.apache.coyote.OutputBuffer;
import org.apache.coyote.RequestInfo;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.HexUtils;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.MimeHeaders;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.AbstractEndpoint.Handler.SocketState;
import org.apache.tomcat.util.net.ApplicationBufferHandler;
import org.apache.tomcat.util.net.SSLSupport;
import org.apache.tomcat.util.net.SocketWrapperBase;
import org.apache.tomcat.util.res.StringManager;
public class AjpProcessor extends AbstractProcessor {
    private static final Log log = LogFactory.getLog ( AjpProcessor.class );
    private static final StringManager sm = StringManager.getManager ( AjpProcessor.class );
    private static final byte[] endMessageArray;
    private static final byte[] endAndCloseMessageArray;
    private static final byte[] flushMessageArray;
    private static final byte[] pongMessageArray;
    static {
        AjpMessage endMessage = new AjpMessage ( 16 );
        endMessage.reset();
        endMessage.appendByte ( Constants.JK_AJP13_END_RESPONSE );
        endMessage.appendByte ( 1 );
        endMessage.end();
        endMessageArray = new byte[endMessage.getLen()];
        System.arraycopy ( endMessage.getBuffer(), 0, endMessageArray, 0,
                           endMessage.getLen() );
        AjpMessage endAndCloseMessage = new AjpMessage ( 16 );
        endAndCloseMessage.reset();
        endAndCloseMessage.appendByte ( Constants.JK_AJP13_END_RESPONSE );
        endAndCloseMessage.appendByte ( 0 );
        endAndCloseMessage.end();
        endAndCloseMessageArray = new byte[endAndCloseMessage.getLen()];
        System.arraycopy ( endAndCloseMessage.getBuffer(), 0, endAndCloseMessageArray, 0,
                           endAndCloseMessage.getLen() );
        AjpMessage flushMessage = new AjpMessage ( 16 );
        flushMessage.reset();
        flushMessage.appendByte ( Constants.JK_AJP13_SEND_BODY_CHUNK );
        flushMessage.appendInt ( 0 );
        flushMessage.appendByte ( 0 );
        flushMessage.end();
        flushMessageArray = new byte[flushMessage.getLen()];
        System.arraycopy ( flushMessage.getBuffer(), 0, flushMessageArray, 0,
                           flushMessage.getLen() );
        AjpMessage pongMessage = new AjpMessage ( 16 );
        pongMessage.reset();
        pongMessage.appendByte ( Constants.JK_AJP13_CPONG_REPLY );
        pongMessage.end();
        pongMessageArray = new byte[pongMessage.getLen()];
        System.arraycopy ( pongMessage.getBuffer(), 0, pongMessageArray,
                           0, pongMessage.getLen() );
    }
    private final byte[] getBodyMessageArray;
    private final int outputMaxChunkSize;
    private final AjpMessage requestHeaderMessage;
    private final AjpMessage responseMessage;
    private int responseMsgPos = -1;
    private final AjpMessage bodyMessage;
    private final MessageBytes bodyBytes = MessageBytes.newInstance();
    private char[] hostNameC = new char[0];
    private final MessageBytes tmpMB = MessageBytes.newInstance();
    private final MessageBytes certificates = MessageBytes.newInstance();
    private boolean endOfStream = false;
    private boolean empty = true;
    private boolean first = true;
    private boolean waitingForBodyMessage = false;
    private boolean replay = false;
    private boolean swallowResponse = false;
    private boolean responseFinished = false;
    private long bytesWritten = 0;
    public AjpProcessor ( int packetSize, AbstractEndpoint<?> endpoint ) {
        super ( endpoint );
        this.outputMaxChunkSize =
            Constants.MAX_SEND_SIZE + packetSize - Constants.MAX_PACKET_SIZE;
        request.setInputBuffer ( new SocketInputBuffer() );
        requestHeaderMessage = new AjpMessage ( packetSize );
        responseMessage = new AjpMessage ( packetSize );
        bodyMessage = new AjpMessage ( packetSize );
        AjpMessage getBodyMessage = new AjpMessage ( 16 );
        getBodyMessage.reset();
        getBodyMessage.appendByte ( Constants.JK_AJP13_GET_BODY_CHUNK );
        getBodyMessage.appendInt ( Constants.MAX_READ_SIZE + packetSize -
                                   Constants.MAX_PACKET_SIZE );
        getBodyMessage.end();
        getBodyMessageArray = new byte[getBodyMessage.getLen()];
        System.arraycopy ( getBodyMessage.getBuffer(), 0, getBodyMessageArray,
                           0, getBodyMessage.getLen() );
        response.setOutputBuffer ( new SocketOutputBuffer() );
    }
    protected boolean ajpFlush = true;
    public boolean getAjpFlush() {
        return ajpFlush;
    }
    public void setAjpFlush ( boolean ajpFlush ) {
        this.ajpFlush = ajpFlush;
    }
    private int keepAliveTimeout = -1;
    public int getKeepAliveTimeout() {
        return keepAliveTimeout;
    }
    public void setKeepAliveTimeout ( int timeout ) {
        keepAliveTimeout = timeout;
    }
    private boolean tomcatAuthentication = true;
    public boolean getTomcatAuthentication() {
        return tomcatAuthentication;
    }
    public void setTomcatAuthentication ( boolean tomcatAuthentication ) {
        this.tomcatAuthentication = tomcatAuthentication;
    }
    private boolean tomcatAuthorization = false;
    public boolean getTomcatAuthorization() {
        return tomcatAuthorization;
    }
    public void setTomcatAuthorization ( boolean tomcatAuthorization ) {
        this.tomcatAuthorization = tomcatAuthorization;
    }
    private String requiredSecret = null;
    public void setRequiredSecret ( String requiredSecret ) {
        this.requiredSecret = requiredSecret;
    }
    private String clientCertProvider = null;
    public String getClientCertProvider() {
        return clientCertProvider;
    }
    public void setClientCertProvider ( String clientCertProvider ) {
        this.clientCertProvider = clientCertProvider;
    }
    @Override
    protected boolean flushBufferedWrite() throws IOException {
        if ( hasDataToWrite() ) {
            socketWrapper.flush ( false );
            if ( hasDataToWrite() ) {
                response.checkRegisterForWrite();
                return true;
            }
        }
        return false;
    }
    @Override
    protected void dispatchNonBlockingRead() {
        if ( available ( true ) > 0 ) {
            super.dispatchNonBlockingRead();
        }
    }
    @Override
    protected SocketState dispatchEndRequest() {
        if ( keepAliveTimeout > 0 ) {
            socketWrapper.setReadTimeout ( keepAliveTimeout );
        }
        recycle();
        return SocketState.OPEN;
    }
    @Override
    public SocketState service ( SocketWrapperBase<?> socket ) throws IOException {
        RequestInfo rp = request.getRequestProcessor();
        rp.setStage ( org.apache.coyote.Constants.STAGE_PARSE );
        this.socketWrapper = socket;
        int connectionTimeout = endpoint.getConnectionTimeout();
        boolean cping = false;
        boolean keptAlive = false;
        while ( !getErrorState().isError() && !endpoint.isPaused() ) {
            try {
                if ( !readMessage ( requestHeaderMessage, !keptAlive ) ) {
                    break;
                }
                if ( keepAliveTimeout > 0 ) {
                    socketWrapper.setReadTimeout ( connectionTimeout );
                }
                int type = requestHeaderMessage.getByte();
                if ( type == Constants.JK_AJP13_CPING_REQUEST ) {
                    if ( endpoint.isPaused() ) {
                        recycle();
                        break;
                    }
                    cping = true;
                    try {
                        socketWrapper.write ( true, pongMessageArray, 0, pongMessageArray.length );
                        socketWrapper.flush ( true );
                    } catch ( IOException e ) {
                        setErrorState ( ErrorState.CLOSE_CONNECTION_NOW, e );
                    }
                    recycle();
                    continue;
                } else if ( type != Constants.JK_AJP13_FORWARD_REQUEST ) {
                    if ( getLog().isDebugEnabled() ) {
                        getLog().debug ( "Unexpected message: " + type );
                    }
                    setErrorState ( ErrorState.CLOSE_CONNECTION_NOW, null );
                    break;
                }
                keptAlive = true;
                request.setStartTime ( System.currentTimeMillis() );
            } catch ( IOException e ) {
                setErrorState ( ErrorState.CLOSE_CONNECTION_NOW, e );
                break;
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                getLog().debug ( sm.getString ( "ajpprocessor.header.error" ), t );
                response.setStatus ( 400 );
                setErrorState ( ErrorState.CLOSE_CLEAN, t );
                getAdapter().log ( request, response, 0 );
            }
            if ( !getErrorState().isError() ) {
                rp.setStage ( org.apache.coyote.Constants.STAGE_PREPARE );
                try {
                    prepareRequest();
                } catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                    getLog().debug ( sm.getString ( "ajpprocessor.request.prepare" ), t );
                    response.setStatus ( 500 );
                    setErrorState ( ErrorState.CLOSE_CLEAN, t );
                    getAdapter().log ( request, response, 0 );
                }
            }
            if ( !getErrorState().isError() && !cping && endpoint.isPaused() ) {
                response.setStatus ( 503 );
                setErrorState ( ErrorState.CLOSE_CLEAN, null );
                getAdapter().log ( request, response, 0 );
            }
            cping = false;
            if ( !getErrorState().isError() ) {
                try {
                    rp.setStage ( org.apache.coyote.Constants.STAGE_SERVICE );
                    getAdapter().service ( request, response );
                } catch ( InterruptedIOException e ) {
                    setErrorState ( ErrorState.CLOSE_CONNECTION_NOW, e );
                } catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                    getLog().error ( sm.getString ( "ajpprocessor.request.process" ), t );
                    response.setStatus ( 500 );
                    setErrorState ( ErrorState.CLOSE_CLEAN, t );
                    getAdapter().log ( request, response, 0 );
                }
            }
            if ( isAsync() && !getErrorState().isError() ) {
                break;
            }
            if ( !responseFinished && getErrorState().isIoAllowed() ) {
                try {
                    action ( ActionCode.COMMIT, null );
                    finishResponse();
                } catch ( IOException ioe ) {
                    setErrorState ( ErrorState.CLOSE_CONNECTION_NOW, ioe );
                } catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                    setErrorState ( ErrorState.CLOSE_NOW, t );
                }
            }
            if ( getErrorState().isError() ) {
                response.setStatus ( 500 );
            }
            request.updateCounters();
            rp.setStage ( org.apache.coyote.Constants.STAGE_KEEPALIVE );
            if ( keepAliveTimeout > 0 ) {
                socketWrapper.setReadTimeout ( keepAliveTimeout );
            }
            recycle();
        }
        rp.setStage ( org.apache.coyote.Constants.STAGE_ENDED );
        if ( getErrorState().isError() || endpoint.isPaused() ) {
            return SocketState.CLOSED;
        } else {
            if ( isAsync() ) {
                return SocketState.LONG;
            } else {
                return SocketState.OPEN;
            }
        }
    }
    @Override
    public void recycle() {
        getAdapter().checkRecycled ( request, response );
        super.recycle();
        request.recycle();
        response.recycle();
        first = true;
        endOfStream = false;
        waitingForBodyMessage = false;
        empty = true;
        replay = false;
        responseFinished = false;
        certificates.recycle();
        swallowResponse = false;
        bytesWritten = 0;
    }
    @Override
    public void pause() {
    }
    private boolean receive ( boolean block ) throws IOException {
        bodyMessage.reset();
        if ( !readMessage ( bodyMessage, block ) ) {
            return false;
        }
        waitingForBodyMessage = false;
        if ( bodyMessage.getLen() == 0 ) {
            return false;
        }
        int blen = bodyMessage.peekInt();
        if ( blen == 0 ) {
            return false;
        }
        bodyMessage.getBodyBytes ( bodyBytes );
        empty = false;
        return true;
    }
    private boolean readMessage ( AjpMessage message, boolean block )
    throws IOException {
        byte[] buf = message.getBuffer();
        if ( !read ( buf, 0, Constants.H_SIZE, block ) ) {
            return false;
        }
        int messageLength = message.processHeader ( true );
        if ( messageLength < 0 ) {
            throw new IOException ( sm.getString ( "ajpmessage.invalidLength",
                                                   Integer.valueOf ( messageLength ) ) );
        } else if ( messageLength == 0 ) {
            return true;
        } else {
            if ( messageLength > message.getBuffer().length ) {
                throw new IllegalArgumentException ( sm.getString (
                        "ajpprocessor.header.tooLong",
                        Integer.valueOf ( messageLength ),
                        Integer.valueOf ( buf.length ) ) );
            }
            read ( buf, Constants.H_SIZE, messageLength, true );
            return true;
        }
    }
    protected boolean refillReadBuffer ( boolean block ) throws IOException {
        if ( replay ) {
            endOfStream = true;
        }
        if ( endOfStream ) {
            return false;
        }
        if ( first ) {
            first = false;
            long contentLength = request.getContentLengthLong();
            if ( contentLength > 0 ) {
                waitingForBodyMessage = true;
            } else if ( contentLength == 0 ) {
                endOfStream = true;
                return false;
            }
        }
        if ( !waitingForBodyMessage ) {
            socketWrapper.write ( true, getBodyMessageArray, 0, getBodyMessageArray.length );
            socketWrapper.flush ( true );
            waitingForBodyMessage = true;
        }
        boolean moreData = receive ( block );
        if ( !moreData && !waitingForBodyMessage ) {
            endOfStream = true;
        }
        return moreData;
    }
    private void prepareRequest() {
        byte methodCode = requestHeaderMessage.getByte();
        if ( methodCode != Constants.SC_M_JK_STORED ) {
            String methodName = Constants.getMethodForCode ( methodCode - 1 );
            request.method().setString ( methodName );
        }
        requestHeaderMessage.getBytes ( request.protocol() );
        requestHeaderMessage.getBytes ( request.requestURI() );
        requestHeaderMessage.getBytes ( request.remoteAddr() );
        requestHeaderMessage.getBytes ( request.remoteHost() );
        requestHeaderMessage.getBytes ( request.localName() );
        request.setLocalPort ( requestHeaderMessage.getInt() );
        boolean isSSL = requestHeaderMessage.getByte() != 0;
        if ( isSSL ) {
            request.scheme().setString ( "https" );
        }
        MimeHeaders headers = request.getMimeHeaders();
        headers.setLimit ( endpoint.getMaxHeaderCount() );
        boolean contentLengthSet = false;
        int hCount = requestHeaderMessage.getInt();
        for ( int i = 0 ; i < hCount ; i++ ) {
            String hName = null;
            int isc = requestHeaderMessage.peekInt();
            int hId = isc & 0xFF;
            MessageBytes vMB = null;
            isc &= 0xFF00;
            if ( 0xA000 == isc ) {
                requestHeaderMessage.getInt();
                hName = Constants.getHeaderForCode ( hId - 1 );
                vMB = headers.addValue ( hName );
            } else {
                hId = -1;
                requestHeaderMessage.getBytes ( tmpMB );
                ByteChunk bc = tmpMB.getByteChunk();
                vMB = headers.addValue ( bc.getBuffer(),
                                         bc.getStart(), bc.getLength() );
            }
            requestHeaderMessage.getBytes ( vMB );
            if ( hId == Constants.SC_REQ_CONTENT_LENGTH ||
                    ( hId == -1 && tmpMB.equalsIgnoreCase ( "Content-Length" ) ) ) {
                long cl = vMB.getLong();
                if ( contentLengthSet ) {
                    response.setStatus ( HttpServletResponse.SC_BAD_REQUEST );
                    setErrorState ( ErrorState.CLOSE_CLEAN, null );
                } else {
                    contentLengthSet = true;
                    request.setContentLength ( cl );
                }
            } else if ( hId == Constants.SC_REQ_CONTENT_TYPE ||
                        ( hId == -1 && tmpMB.equalsIgnoreCase ( "Content-Type" ) ) ) {
                ByteChunk bchunk = vMB.getByteChunk();
                request.contentType().setBytes ( bchunk.getBytes(),
                                                 bchunk.getOffset(),
                                                 bchunk.getLength() );
            }
        }
        boolean secret = false;
        byte attributeCode;
        while ( ( attributeCode = requestHeaderMessage.getByte() )
                != Constants.SC_A_ARE_DONE ) {
            switch ( attributeCode ) {
            case Constants.SC_A_REQ_ATTRIBUTE :
                requestHeaderMessage.getBytes ( tmpMB );
                String n = tmpMB.toString();
                requestHeaderMessage.getBytes ( tmpMB );
                String v = tmpMB.toString();
                if ( n.equals ( Constants.SC_A_REQ_LOCAL_ADDR ) ) {
                    request.localAddr().setString ( v );
                } else if ( n.equals ( Constants.SC_A_REQ_REMOTE_PORT ) ) {
                    try {
                        request.setRemotePort ( Integer.parseInt ( v ) );
                    } catch ( NumberFormatException nfe ) {
                    }
                } else if ( n.equals ( Constants.SC_A_SSL_PROTOCOL ) ) {
                    request.setAttribute ( SSLSupport.PROTOCOL_VERSION_KEY, v );
                } else {
                    request.setAttribute ( n, v );
                }
                break;
            case Constants.SC_A_CONTEXT :
                requestHeaderMessage.getBytes ( tmpMB );
                break;
            case Constants.SC_A_SERVLET_PATH :
                requestHeaderMessage.getBytes ( tmpMB );
                break;
            case Constants.SC_A_REMOTE_USER :
                if ( tomcatAuthorization || !tomcatAuthentication ) {
                    requestHeaderMessage.getBytes ( request.getRemoteUser() );
                    request.setRemoteUserNeedsAuthorization ( tomcatAuthorization );
                } else {
                    requestHeaderMessage.getBytes ( tmpMB );
                }
                break;
            case Constants.SC_A_AUTH_TYPE :
                if ( tomcatAuthentication ) {
                    requestHeaderMessage.getBytes ( tmpMB );
                } else {
                    requestHeaderMessage.getBytes ( request.getAuthType() );
                }
                break;
            case Constants.SC_A_QUERY_STRING :
                requestHeaderMessage.getBytes ( request.queryString() );
                break;
            case Constants.SC_A_JVM_ROUTE :
                requestHeaderMessage.getBytes ( tmpMB );
                break;
            case Constants.SC_A_SSL_CERT :
                requestHeaderMessage.getBytes ( certificates );
                break;
            case Constants.SC_A_SSL_CIPHER :
                requestHeaderMessage.getBytes ( tmpMB );
                request.setAttribute ( SSLSupport.CIPHER_SUITE_KEY,
                                       tmpMB.toString() );
                break;
            case Constants.SC_A_SSL_SESSION :
                requestHeaderMessage.getBytes ( tmpMB );
                request.setAttribute ( SSLSupport.SESSION_ID_KEY,
                                       tmpMB.toString() );
                break;
            case Constants.SC_A_SSL_KEY_SIZE :
                request.setAttribute ( SSLSupport.KEY_SIZE_KEY,
                                       Integer.valueOf ( requestHeaderMessage.getInt() ) );
                break;
            case Constants.SC_A_STORED_METHOD:
                requestHeaderMessage.getBytes ( request.method() );
                break;
            case Constants.SC_A_SECRET:
                requestHeaderMessage.getBytes ( tmpMB );
                if ( requiredSecret != null ) {
                    secret = true;
                    if ( !tmpMB.equals ( requiredSecret ) ) {
                        response.setStatus ( 403 );
                        setErrorState ( ErrorState.CLOSE_CLEAN, null );
                    }
                }
                break;
            default:
                break;
            }
        }
        if ( ( requiredSecret != null ) && !secret ) {
            response.setStatus ( 403 );
            setErrorState ( ErrorState.CLOSE_CLEAN, null );
        }
        ByteChunk uriBC = request.requestURI().getByteChunk();
        if ( uriBC.startsWithIgnoreCase ( "http", 0 ) ) {
            int pos = uriBC.indexOf ( "://", 0, 3, 4 );
            int uriBCStart = uriBC.getStart();
            int slashPos = -1;
            if ( pos != -1 ) {
                byte[] uriB = uriBC.getBytes();
                slashPos = uriBC.indexOf ( '/', pos + 3 );
                if ( slashPos == -1 ) {
                    slashPos = uriBC.getLength();
                    request.requestURI().setBytes
                    ( uriB, uriBCStart + pos + 1, 1 );
                } else {
                    request.requestURI().setBytes
                    ( uriB, uriBCStart + slashPos,
                      uriBC.getLength() - slashPos );
                }
                MessageBytes hostMB = headers.setValue ( "host" );
                hostMB.setBytes ( uriB, uriBCStart + pos + 3,
                                  slashPos - pos - 3 );
            }
        }
        MessageBytes valueMB = request.getMimeHeaders().getValue ( "host" );
        parseHost ( valueMB );
        if ( getErrorState().isError() ) {
            getAdapter().log ( request, response, 0 );
        }
    }
    private void parseHost ( MessageBytes valueMB ) {
        if ( valueMB == null || valueMB.isNull() ) {
            request.setServerPort ( request.getLocalPort() );
            try {
                request.serverName().duplicate ( request.localName() );
            } catch ( IOException e ) {
                response.setStatus ( 400 );
                setErrorState ( ErrorState.CLOSE_CLEAN, e );
            }
            return;
        }
        ByteChunk valueBC = valueMB.getByteChunk();
        byte[] valueB = valueBC.getBytes();
        int valueL = valueBC.getLength();
        int valueS = valueBC.getStart();
        int colonPos = -1;
        if ( hostNameC.length < valueL ) {
            hostNameC = new char[valueL];
        }
        boolean ipv6 = ( valueB[valueS] == '[' );
        boolean bracketClosed = false;
        for ( int i = 0; i < valueL; i++ ) {
            char b = ( char ) valueB[i + valueS];
            hostNameC[i] = b;
            if ( b == ']' ) {
                bracketClosed = true;
            } else if ( b == ':' ) {
                if ( !ipv6 || bracketClosed ) {
                    colonPos = i;
                    break;
                }
            }
        }
        if ( colonPos < 0 ) {
            request.serverName().setChars ( hostNameC, 0, valueL );
        } else {
            request.serverName().setChars ( hostNameC, 0, colonPos );
            int port = 0;
            int mult = 1;
            for ( int i = valueL - 1; i > colonPos; i-- ) {
                int charValue = HexUtils.getDec ( valueB[i + valueS] );
                if ( charValue == -1 ) {
                    response.setStatus ( 400 );
                    setErrorState ( ErrorState.CLOSE_CLEAN, null );
                    break;
                }
                port = port + ( charValue * mult );
                mult = 10 * mult;
            }
            request.setServerPort ( port );
        }
    }
    @Override
    protected final void prepareResponse() throws IOException {
        response.setCommitted ( true );
        tmpMB.recycle();
        responseMsgPos = -1;
        responseMessage.reset();
        responseMessage.appendByte ( Constants.JK_AJP13_SEND_HEADERS );
        int statusCode = response.getStatus();
        if ( statusCode < 200 || statusCode == 204 || statusCode == 205 ||
                statusCode == 304 ) {
            swallowResponse = true;
        }
        MessageBytes methodMB = request.method();
        if ( methodMB.equals ( "HEAD" ) ) {
            swallowResponse = true;
        }
        responseMessage.appendInt ( statusCode );
        tmpMB.setString ( Integer.toString ( response.getStatus() ) );
        responseMessage.appendBytes ( tmpMB );
        MimeHeaders headers = response.getMimeHeaders();
        String contentType = response.getContentType();
        if ( contentType != null ) {
            headers.setValue ( "Content-Type" ).setString ( contentType );
        }
        String contentLanguage = response.getContentLanguage();
        if ( contentLanguage != null ) {
            headers.setValue ( "Content-Language" ).setString ( contentLanguage );
        }
        long contentLength = response.getContentLengthLong();
        if ( contentLength >= 0 ) {
            headers.setValue ( "Content-Length" ).setLong ( contentLength );
        }
        int numHeaders = headers.size();
        responseMessage.appendInt ( numHeaders );
        for ( int i = 0; i < numHeaders; i++ ) {
            MessageBytes hN = headers.getName ( i );
            int hC = Constants.getResponseAjpIndex ( hN.toString() );
            if ( hC > 0 ) {
                responseMessage.appendInt ( hC );
            } else {
                responseMessage.appendBytes ( hN );
            }
            MessageBytes hV = headers.getValue ( i );
            responseMessage.appendBytes ( hV );
        }
        responseMessage.end();
        socketWrapper.write ( true, responseMessage.getBuffer(), 0, responseMessage.getLen() );
        socketWrapper.flush ( true );
    }
    @Override
    protected final void flush() throws IOException {
        if ( !responseFinished ) {
            if ( ajpFlush ) {
                socketWrapper.write ( true, flushMessageArray, 0, flushMessageArray.length );
            }
            socketWrapper.flush ( true );
        }
    }
    @Override
    protected final void finishResponse() throws IOException {
        if ( responseFinished ) {
            return;
        }
        responseFinished = true;
        if ( waitingForBodyMessage || first && request.getContentLengthLong() > 0 ) {
            refillReadBuffer ( true );
        }
        if ( getErrorState().isError() ) {
            socketWrapper.write ( true, endAndCloseMessageArray, 0, endAndCloseMessageArray.length );
        } else {
            socketWrapper.write ( true, endMessageArray, 0, endMessageArray.length );
        }
        socketWrapper.flush ( true );
    }
    @Override
    protected final void ack() {
    }
    @Override
    protected final int available ( boolean doRead ) {
        if ( endOfStream ) {
            return 0;
        }
        if ( empty && doRead ) {
            try {
                refillReadBuffer ( false );
            } catch ( IOException timeout ) {
                return 1;
            }
        }
        if ( empty ) {
            return 0;
        } else {
            return bodyBytes.getByteChunk().getLength();
        }
    }
    @Override
    protected final void setRequestBody ( ByteChunk body ) {
        int length = body.getLength();
        bodyBytes.setBytes ( body.getBytes(), body.getStart(), length );
        request.setContentLength ( length );
        first = false;
        empty = false;
        replay = true;
        endOfStream = false;
    }
    @Override
    protected final void setSwallowResponse() {
        swallowResponse = true;
    }
    @Override
    protected final void disableSwallowRequest() {
    }
    @Override
    protected final boolean getPopulateRequestAttributesFromSocket() {
        return false;
    }
    @Override
    protected final void populateRequestAttributeRemoteHost() {
        if ( request.remoteHost().isNull() ) {
            try {
                request.remoteHost().setString ( InetAddress.getByName
                                                 ( request.remoteAddr().toString() ).getHostName() );
            } catch ( IOException iex ) {
            }
        }
    }
    @Override
    protected final void populateSslRequestAttributes() {
        if ( !certificates.isNull() ) {
            ByteChunk certData = certificates.getByteChunk();
            X509Certificate jsseCerts[] = null;
            ByteArrayInputStream bais =
                new ByteArrayInputStream ( certData.getBytes(),
                                           certData.getStart(),
                                           certData.getLength() );
            try {
                CertificateFactory cf;
                String clientCertProvider = getClientCertProvider();
                if ( clientCertProvider == null ) {
                    cf = CertificateFactory.getInstance ( "X.509" );
                } else {
                    cf = CertificateFactory.getInstance ( "X.509",
                                                          clientCertProvider );
                }
                while ( bais.available() > 0 ) {
                    X509Certificate cert = ( X509Certificate )
                                           cf.generateCertificate ( bais );
                    if ( jsseCerts == null ) {
                        jsseCerts = new X509Certificate[1];
                        jsseCerts[0] = cert;
                    } else {
                        X509Certificate [] temp = new X509Certificate[jsseCerts.length + 1];
                        System.arraycopy ( jsseCerts, 0, temp, 0, jsseCerts.length );
                        temp[jsseCerts.length] = cert;
                        jsseCerts = temp;
                    }
                }
            } catch ( java.security.cert.CertificateException e ) {
                getLog().error ( sm.getString ( "ajpprocessor.certs.fail" ), e );
                return;
            } catch ( NoSuchProviderException e ) {
                getLog().error ( sm.getString ( "ajpprocessor.certs.fail" ), e );
                return;
            }
            request.setAttribute ( SSLSupport.CERTIFICATE_KEY, jsseCerts );
        }
    }
    @Override
    protected final boolean isRequestBodyFullyRead() {
        return endOfStream;
    }
    @Override
    protected final void registerReadInterest() {
        socketWrapper.registerReadInterest();
    }
    @Override
    protected final boolean isReady() {
        return responseMsgPos == -1 && socketWrapper.isReadyForWrite();
    }
    @Override
    protected final void executeDispatches ( SocketWrapperBase<?> wrapper ) {
        wrapper.executeNonBlockingDispatches ( getIteratorAndClearDispatches() );
    }
    private boolean read ( byte[] buf, int pos, int n, boolean block ) throws IOException {
        int read = socketWrapper.read ( block, buf, pos, n );
        if ( read > 0 && read < n ) {
            int left = n - read;
            int start = pos + read;
            while ( left > 0 ) {
                read = socketWrapper.read ( true, buf, start, left );
                if ( read == -1 ) {
                    throw new EOFException();
                }
                left = left - read;
                start = start + read;
            }
        } else if ( read == -1 ) {
            throw new EOFException();
        }
        return read > 0;
    }
    private void writeData ( ByteBuffer chunk ) throws IOException {
        boolean blocking = ( response.getWriteListener() == null );
        int len = chunk.remaining();
        int off = 0;
        while ( len > 0 ) {
            int thisTime = Math.min ( len, outputMaxChunkSize );
            responseMessage.reset();
            responseMessage.appendByte ( Constants.JK_AJP13_SEND_BODY_CHUNK );
            chunk.limit ( chunk.position() + thisTime );
            responseMessage.appendBytes ( chunk );
            responseMessage.end();
            socketWrapper.write ( blocking, responseMessage.getBuffer(), 0, responseMessage.getLen() );
            socketWrapper.flush ( blocking );
            len -= thisTime;
            off += thisTime;
        }
        bytesWritten += off;
    }
    private boolean hasDataToWrite() {
        return responseMsgPos != -1 || socketWrapper.hasDataToWrite();
    }
    @Override
    protected Log getLog() {
        return log;
    }
    protected class SocketInputBuffer implements InputBuffer {
        @Override
        public int doRead ( ApplicationBufferHandler handler ) throws IOException {
            if ( endOfStream ) {
                return -1;
            }
            if ( empty ) {
                if ( !refillReadBuffer ( true ) ) {
                    return -1;
                }
            }
            ByteChunk bc = bodyBytes.getByteChunk();
            handler.setByteBuffer ( ByteBuffer.wrap ( bc.getBuffer(), bc.getStart(), bc.getLength() ) );
            empty = true;
            return handler.getByteBuffer().remaining();
        }
    }
    protected class SocketOutputBuffer implements OutputBuffer {
        @Override
        public int doWrite ( ByteBuffer chunk ) throws IOException {
            if ( !response.isCommitted() ) {
                try {
                    prepareResponse();
                } catch ( IOException e ) {
                    setErrorState ( ErrorState.CLOSE_CONNECTION_NOW, e );
                }
            }
            int len = 0;
            if ( !swallowResponse ) {
                try {
                    len = chunk.remaining();
                    writeData ( chunk );
                    len -= chunk.remaining();
                } catch ( IOException ioe ) {
                    setErrorState ( ErrorState.CLOSE_CONNECTION_NOW, ioe );
                    throw ioe;
                }
            }
            return len;
        }
        @Override
        public long getBytesWritten() {
            return bytesWritten;
        }
    }
}
