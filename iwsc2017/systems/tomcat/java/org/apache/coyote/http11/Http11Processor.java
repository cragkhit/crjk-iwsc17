package org.apache.coyote.http11;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletResponse;
import org.apache.coyote.AbstractProcessor;
import org.apache.coyote.ActionCode;
import org.apache.coyote.ErrorState;
import org.apache.coyote.Request;
import org.apache.coyote.RequestInfo;
import org.apache.coyote.UpgradeProtocol;
import org.apache.coyote.UpgradeToken;
import org.apache.coyote.http11.filters.BufferedInputFilter;
import org.apache.coyote.http11.filters.ChunkedInputFilter;
import org.apache.coyote.http11.filters.ChunkedOutputFilter;
import org.apache.coyote.http11.filters.GzipOutputFilter;
import org.apache.coyote.http11.filters.IdentityInputFilter;
import org.apache.coyote.http11.filters.IdentityOutputFilter;
import org.apache.coyote.http11.filters.SavedRequestInputFilter;
import org.apache.coyote.http11.filters.VoidInputFilter;
import org.apache.coyote.http11.filters.VoidOutputFilter;
import org.apache.coyote.http11.upgrade.InternalHttpUpgradeHandler;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.buf.Ascii;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.HexUtils;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.FastHttpDateFormat;
import org.apache.tomcat.util.http.MimeHeaders;
import org.apache.tomcat.util.log.UserDataHelper;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.AbstractEndpoint.Handler.SocketState;
import org.apache.tomcat.util.net.SSLSupport;
import org.apache.tomcat.util.net.SendfileDataBase;
import org.apache.tomcat.util.net.SocketWrapperBase;
import org.apache.tomcat.util.res.StringManager;
public class Http11Processor extends AbstractProcessor {
    private static final Log log = LogFactory.getLog ( Http11Processor.class );
    private static final StringManager sm = StringManager.getManager ( Http11Processor.class );
    private final UserDataHelper userDataHelper;
    protected final Http11InputBuffer inputBuffer;
    protected final Http11OutputBuffer outputBuffer;
    private int pluggableFilterIndex = Integer.MAX_VALUE;
    protected volatile boolean keepAlive = true;
    protected boolean openSocket = false;
    protected boolean readComplete = true;
    protected boolean http11 = true;
    protected boolean http09 = false;
    protected boolean contentDelimitation = true;
    protected Pattern restrictedUserAgents = null;
    protected int maxKeepAliveRequests = -1;
    protected int connectionUploadTimeout = 300000;
    protected boolean disableUploadTimeout = false;
    protected int compressionLevel = 0;
    protected int compressionMinSize = 2048;
    protected int maxSavePostSize = 4 * 1024;
    protected Pattern noCompressionUserAgents = null;
    protected String[] compressableMimeTypes;
    protected char[] hostNameC = new char[0];
    private String server = null;
    private boolean serverRemoveAppProvidedValues = false;
    protected UpgradeToken upgradeToken = null;
    protected SendfileDataBase sendfileData = null;
    private final Map<String, UpgradeProtocol> httpUpgradeProtocols;
    public Http11Processor ( int maxHttpHeaderSize, AbstractEndpoint<?> endpoint, int maxTrailerSize,
                             Set<String> allowedTrailerHeaders, int maxExtensionSize, int maxSwallowSize,
                             Map<String, UpgradeProtocol> httpUpgradeProtocols ) {
        super ( endpoint );
        userDataHelper = new UserDataHelper ( log );
        inputBuffer = new Http11InputBuffer ( request, maxHttpHeaderSize );
        request.setInputBuffer ( inputBuffer );
        outputBuffer = new Http11OutputBuffer ( response, maxHttpHeaderSize );
        response.setOutputBuffer ( outputBuffer );
        inputBuffer.addFilter ( new IdentityInputFilter ( maxSwallowSize ) );
        outputBuffer.addFilter ( new IdentityOutputFilter() );
        inputBuffer.addFilter ( new ChunkedInputFilter ( maxTrailerSize, allowedTrailerHeaders,
                                maxExtensionSize, maxSwallowSize ) );
        outputBuffer.addFilter ( new ChunkedOutputFilter() );
        inputBuffer.addFilter ( new VoidInputFilter() );
        outputBuffer.addFilter ( new VoidOutputFilter() );
        inputBuffer.addFilter ( new BufferedInputFilter() );
        outputBuffer.addFilter ( new GzipOutputFilter() );
        pluggableFilterIndex = inputBuffer.getFilters().length;
        this.httpUpgradeProtocols = httpUpgradeProtocols;
    }
    public void setCompression ( String compression ) {
        if ( compression.equals ( "on" ) ) {
            this.compressionLevel = 1;
        } else if ( compression.equals ( "force" ) ) {
            this.compressionLevel = 2;
        } else if ( compression.equals ( "off" ) ) {
            this.compressionLevel = 0;
        } else {
            try {
                compressionMinSize = Integer.parseInt ( compression );
                this.compressionLevel = 1;
            } catch ( Exception e ) {
                this.compressionLevel = 0;
            }
        }
    }
    public void setCompressionMinSize ( int compressionMinSize ) {
        this.compressionMinSize = compressionMinSize;
    }
    public void setNoCompressionUserAgents ( String noCompressionUserAgents ) {
        if ( noCompressionUserAgents == null || noCompressionUserAgents.length() == 0 ) {
            this.noCompressionUserAgents = null;
        } else {
            this.noCompressionUserAgents =
                Pattern.compile ( noCompressionUserAgents );
        }
    }
    public void setCompressableMimeTypes ( String[] compressableMimeTypes ) {
        this.compressableMimeTypes = compressableMimeTypes;
    }
    public String getCompression() {
        switch ( compressionLevel ) {
        case 0:
            return "off";
        case 1:
            return "on";
        case 2:
            return "force";
        }
        return "off";
    }
    private static boolean startsWithStringArray ( String sArray[], String value ) {
        if ( value == null ) {
            return false;
        }
        for ( int i = 0; i < sArray.length; i++ ) {
            if ( value.startsWith ( sArray[i] ) ) {
                return true;
            }
        }
        return false;
    }
    public void setRestrictedUserAgents ( String restrictedUserAgents ) {
        if ( restrictedUserAgents == null ||
                restrictedUserAgents.length() == 0 ) {
            this.restrictedUserAgents = null;
        } else {
            this.restrictedUserAgents = Pattern.compile ( restrictedUserAgents );
        }
    }
    public void setMaxKeepAliveRequests ( int mkar ) {
        maxKeepAliveRequests = mkar;
    }
    public int getMaxKeepAliveRequests() {
        return maxKeepAliveRequests;
    }
    public void setMaxSavePostSize ( int msps ) {
        maxSavePostSize = msps;
    }
    public int getMaxSavePostSize() {
        return maxSavePostSize;
    }
    public void setDisableUploadTimeout ( boolean isDisabled ) {
        disableUploadTimeout = isDisabled;
    }
    public boolean getDisableUploadTimeout() {
        return disableUploadTimeout;
    }
    public void setConnectionUploadTimeout ( int timeout ) {
        connectionUploadTimeout = timeout ;
    }
    public int getConnectionUploadTimeout() {
        return connectionUploadTimeout;
    }
    public void setServer ( String server ) {
        if ( server == null || server.equals ( "" ) ) {
            this.server = null;
        } else {
            this.server = server;
        }
    }
    public void setServerRemoveAppProvidedValues ( boolean serverRemoveAppProvidedValues ) {
        this.serverRemoveAppProvidedValues = serverRemoveAppProvidedValues;
    }
    private boolean isCompressable() {
        MessageBytes contentEncodingMB =
            response.getMimeHeaders().getValue ( "Content-Encoding" );
        if ( ( contentEncodingMB != null )
                && ( contentEncodingMB.indexOf ( "gzip" ) != -1 ) ) {
            return false;
        }
        if ( compressionLevel == 2 ) {
            return true;
        }
        long contentLength = response.getContentLengthLong();
        if ( ( contentLength == -1 )
                || ( contentLength > compressionMinSize ) ) {
            if ( compressableMimeTypes != null ) {
                return ( startsWithStringArray ( compressableMimeTypes,
                                                 response.getContentType() ) );
            }
        }
        return false;
    }
    private boolean useCompression() {
        MessageBytes acceptEncodingMB =
            request.getMimeHeaders().getValue ( "accept-encoding" );
        if ( ( acceptEncodingMB == null )
                || ( acceptEncodingMB.indexOf ( "gzip" ) == -1 ) ) {
            return false;
        }
        if ( compressionLevel == 2 ) {
            return true;
        }
        if ( noCompressionUserAgents != null ) {
            MessageBytes userAgentValueMB =
                request.getMimeHeaders().getValue ( "user-agent" );
            if ( userAgentValueMB != null ) {
                String userAgentValue = userAgentValueMB.toString();
                if ( noCompressionUserAgents.matcher ( userAgentValue ).matches() ) {
                    return false;
                }
            }
        }
        return true;
    }
    private static int findBytes ( ByteChunk bc, byte[] b ) {
        byte first = b[0];
        byte[] buff = bc.getBuffer();
        int start = bc.getStart();
        int end = bc.getEnd();
        int srcEnd = b.length;
        for ( int i = start; i <= ( end - srcEnd ); i++ ) {
            if ( Ascii.toLower ( buff[i] ) != first ) {
                continue;
            }
            int myPos = i + 1;
            for ( int srcPos = 1; srcPos < srcEnd; ) {
                if ( Ascii.toLower ( buff[myPos++] ) != b[srcPos++] ) {
                    break;
                }
                if ( srcPos == srcEnd ) {
                    return i - start;
                }
            }
        }
        return -1;
    }
    private static boolean statusDropsConnection ( int status ) {
        return status == 400   ||
               status == 408   ||
               status == 411   ||
               status == 413   ||
               status == 414   ||
               status == 500   ||
               status == 503   ||
               status == 501  ;
    }
    private void addInputFilter ( InputFilter[] inputFilters, String encodingName ) {
        encodingName = encodingName.trim().toLowerCase ( Locale.ENGLISH );
        if ( encodingName.equals ( "identity" ) ) {
        } else if ( encodingName.equals ( "chunked" ) ) {
            inputBuffer.addActiveFilter
            ( inputFilters[Constants.CHUNKED_FILTER] );
            contentDelimitation = true;
        } else {
            for ( int i = pluggableFilterIndex; i < inputFilters.length; i++ ) {
                if ( inputFilters[i].getEncodingName().toString().equals ( encodingName ) ) {
                    inputBuffer.addActiveFilter ( inputFilters[i] );
                    return;
                }
            }
            response.setStatus ( 501 );
            setErrorState ( ErrorState.CLOSE_CLEAN, null );
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "http11processor.request.prepare" ) +
                            " Unsupported transfer encoding [" + encodingName + "]" );
            }
        }
    }
    @Override
    public SocketState service ( SocketWrapperBase<?> socketWrapper )
    throws IOException {
        RequestInfo rp = request.getRequestProcessor();
        rp.setStage ( org.apache.coyote.Constants.STAGE_PARSE );
        setSocketWrapper ( socketWrapper );
        inputBuffer.init ( socketWrapper );
        outputBuffer.init ( socketWrapper );
        keepAlive = true;
        openSocket = false;
        readComplete = true;
        boolean keptAlive = false;
        while ( !getErrorState().isError() && keepAlive && !isAsync() &&
                upgradeToken == null && !endpoint.isPaused() ) {
            try {
                if ( !inputBuffer.parseRequestLine ( keptAlive ) ) {
                    if ( inputBuffer.getParsingRequestLinePhase() == -1 ) {
                        return SocketState.UPGRADING;
                    } else if ( handleIncompleteRequestLineRead() ) {
                        break;
                    }
                }
                if ( endpoint.isPaused() ) {
                    response.setStatus ( 503 );
                    setErrorState ( ErrorState.CLOSE_CLEAN, null );
                } else {
                    keptAlive = true;
                    request.getMimeHeaders().setLimit ( endpoint.getMaxHeaderCount() );
                    if ( !inputBuffer.parseHeaders() ) {
                        openSocket = true;
                        readComplete = false;
                        break;
                    }
                    if ( !disableUploadTimeout ) {
                        socketWrapper.setReadTimeout ( connectionUploadTimeout );
                    }
                }
            } catch ( IOException e ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "http11processor.header.parse" ), e );
                }
                setErrorState ( ErrorState.CLOSE_CONNECTION_NOW, e );
                break;
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                UserDataHelper.Mode logMode = userDataHelper.getNextMode();
                if ( logMode != null ) {
                    String message = sm.getString ( "http11processor.header.parse" );
                    switch ( logMode ) {
                    case INFO_THEN_DEBUG:
                        message += sm.getString ( "http11processor.fallToDebug" );
                    case INFO:
                        log.info ( message, t );
                        break;
                    case DEBUG:
                        log.debug ( message, t );
                    }
                }
                response.setStatus ( 400 );
                setErrorState ( ErrorState.CLOSE_CLEAN, t );
                getAdapter().log ( request, response, 0 );
            }
            Enumeration<String> connectionValues = request.getMimeHeaders().values ( "Connection" );
            boolean foundUpgrade = false;
            while ( connectionValues.hasMoreElements() && !foundUpgrade ) {
                foundUpgrade = connectionValues.nextElement().toLowerCase (
                                   Locale.ENGLISH ).contains ( "upgrade" );
            }
            if ( foundUpgrade ) {
                String requestedProtocol = request.getHeader ( "Upgrade" );
                UpgradeProtocol upgradeProtocol = httpUpgradeProtocols.get ( requestedProtocol );
                if ( upgradeProtocol != null ) {
                    if ( upgradeProtocol.accept ( request ) ) {
                        response.setStatus ( HttpServletResponse.SC_SWITCHING_PROTOCOLS );
                        response.setHeader ( "Connection", "Upgrade" );
                        response.setHeader ( "Upgrade", requestedProtocol );
                        action ( ActionCode.CLOSE,  null );
                        getAdapter().log ( request, response, 0 );
                        InternalHttpUpgradeHandler upgradeHandler =
                            upgradeProtocol.getInternalUpgradeHandler (
                                getAdapter(), cloneRequest ( request ) );
                        UpgradeToken upgradeToken = new UpgradeToken ( upgradeHandler, null, null );
                        action ( ActionCode.UPGRADE, upgradeToken );
                        return SocketState.UPGRADING;
                    }
                }
            }
            if ( !getErrorState().isError() ) {
                rp.setStage ( org.apache.coyote.Constants.STAGE_PREPARE );
                try {
                    prepareRequest();
                } catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                    if ( log.isDebugEnabled() ) {
                        log.debug ( sm.getString ( "http11processor.request.prepare" ), t );
                    }
                    response.setStatus ( 500 );
                    setErrorState ( ErrorState.CLOSE_CLEAN, t );
                    getAdapter().log ( request, response, 0 );
                }
            }
            if ( maxKeepAliveRequests == 1 ) {
                keepAlive = false;
            } else if ( maxKeepAliveRequests > 0 &&
                        socketWrapper.decrementKeepAlive() <= 0 ) {
                keepAlive = false;
            }
            if ( !getErrorState().isError() ) {
                try {
                    rp.setStage ( org.apache.coyote.Constants.STAGE_SERVICE );
                    getAdapter().service ( request, response );
                    if ( keepAlive && !getErrorState().isError() && !isAsync() &&
                            statusDropsConnection ( response.getStatus() ) ) {
                        setErrorState ( ErrorState.CLOSE_CLEAN, null );
                    }
                } catch ( InterruptedIOException e ) {
                    setErrorState ( ErrorState.CLOSE_CONNECTION_NOW, e );
                } catch ( HeadersTooLargeException e ) {
                    log.error ( sm.getString ( "http11processor.request.process" ), e );
                    if ( response.isCommitted() ) {
                        setErrorState ( ErrorState.CLOSE_NOW, e );
                    } else {
                        response.reset();
                        response.setStatus ( 500 );
                        setErrorState ( ErrorState.CLOSE_CLEAN, e );
                        response.setHeader ( "Connection", "close" );
                    }
                } catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                    log.error ( sm.getString ( "http11processor.request.process" ), t );
                    response.setStatus ( 500 );
                    setErrorState ( ErrorState.CLOSE_CLEAN, t );
                    getAdapter().log ( request, response, 0 );
                }
            }
            rp.setStage ( org.apache.coyote.Constants.STAGE_ENDINPUT );
            if ( !isAsync() ) {
                endRequest();
            }
            rp.setStage ( org.apache.coyote.Constants.STAGE_ENDOUTPUT );
            if ( getErrorState().isError() ) {
                response.setStatus ( 500 );
            }
            if ( !isAsync() || getErrorState().isError() ) {
                request.updateCounters();
                if ( getErrorState().isIoAllowed() ) {
                    inputBuffer.nextRequest();
                    outputBuffer.nextRequest();
                }
            }
            if ( !disableUploadTimeout ) {
                int connectionTimeout = endpoint.getConnectionTimeout();
                if ( connectionTimeout > 0 ) {
                    socketWrapper.setReadTimeout ( connectionTimeout );
                } else {
                    socketWrapper.setReadTimeout ( 0 );
                }
            }
            rp.setStage ( org.apache.coyote.Constants.STAGE_KEEPALIVE );
            if ( breakKeepAliveLoop ( socketWrapper ) ) {
                break;
            }
        }
        rp.setStage ( org.apache.coyote.Constants.STAGE_ENDED );
        if ( getErrorState().isError() || endpoint.isPaused() ) {
            return SocketState.CLOSED;
        } else if ( isAsync() ) {
            return SocketState.LONG;
        } else if ( isUpgrade() ) {
            return SocketState.UPGRADING;
        } else {
            if ( sendfileData != null ) {
                return SocketState.SENDFILE;
            } else {
                if ( openSocket ) {
                    if ( readComplete ) {
                        return SocketState.OPEN;
                    } else {
                        return SocketState.LONG;
                    }
                } else {
                    return SocketState.CLOSED;
                }
            }
        }
    }
    private Request cloneRequest ( Request source ) throws IOException {
        Request dest = new Request();
        dest.decodedURI().duplicate ( source.decodedURI() );
        dest.method().duplicate ( source.method() );
        dest.getMimeHeaders().duplicate ( source.getMimeHeaders() );
        dest.requestURI().duplicate ( source.requestURI() );
        return dest;
    }
    private boolean handleIncompleteRequestLineRead() {
        openSocket = true;
        if ( inputBuffer.getParsingRequestLinePhase() > 1 ) {
            if ( endpoint.isPaused() ) {
                response.setStatus ( 503 );
                setErrorState ( ErrorState.CLOSE_CLEAN, null );
                getAdapter().log ( request, response, 0 );
                return false;
            } else {
                readComplete = false;
            }
        }
        return true;
    }
    private void checkExpectationAndResponseStatus() {
        if ( request.hasExpectation() &&
                ( response.getStatus() < 200 || response.getStatus() > 299 ) ) {
            inputBuffer.setSwallowInput ( false );
            keepAlive = false;
        }
    }
    private void prepareRequest() {
        http11 = true;
        http09 = false;
        contentDelimitation = false;
        sendfileData = null;
        if ( endpoint.isSSLEnabled() ) {
            request.scheme().setString ( "https" );
        }
        MessageBytes protocolMB = request.protocol();
        if ( protocolMB.equals ( Constants.HTTP_11 ) ) {
            http11 = true;
            protocolMB.setString ( Constants.HTTP_11 );
        } else if ( protocolMB.equals ( Constants.HTTP_10 ) ) {
            http11 = false;
            keepAlive = false;
            protocolMB.setString ( Constants.HTTP_10 );
        } else if ( protocolMB.equals ( "" ) ) {
            http09 = true;
            http11 = false;
            keepAlive = false;
        } else {
            http11 = false;
            response.setStatus ( 505 );
            setErrorState ( ErrorState.CLOSE_CLEAN, null );
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "http11processor.request.prepare" ) +
                            " Unsupported HTTP version \"" + protocolMB + "\"" );
            }
        }
        MimeHeaders headers = request.getMimeHeaders();
        MessageBytes connectionValueMB = headers.getValue ( Constants.CONNECTION );
        if ( connectionValueMB != null ) {
            ByteChunk connectionValueBC = connectionValueMB.getByteChunk();
            if ( findBytes ( connectionValueBC, Constants.CLOSE_BYTES ) != -1 ) {
                keepAlive = false;
            } else if ( findBytes ( connectionValueBC,
                                    Constants.KEEPALIVE_BYTES ) != -1 ) {
                keepAlive = true;
            }
        }
        if ( http11 ) {
            MessageBytes expectMB = headers.getValue ( "expect" );
            if ( expectMB != null ) {
                if ( expectMB.indexOfIgnoreCase ( "100-continue", 0 ) != -1 ) {
                    inputBuffer.setSwallowInput ( false );
                    request.setExpectation ( true );
                } else {
                    response.setStatus ( HttpServletResponse.SC_EXPECTATION_FAILED );
                    setErrorState ( ErrorState.CLOSE_CLEAN, null );
                }
            }
        }
        if ( restrictedUserAgents != null && ( http11 || keepAlive ) ) {
            MessageBytes userAgentValueMB = headers.getValue ( "user-agent" );
            if ( userAgentValueMB != null ) {
                String userAgentValue = userAgentValueMB.toString();
                if ( restrictedUserAgents != null &&
                        restrictedUserAgents.matcher ( userAgentValue ).matches() ) {
                    http11 = false;
                    keepAlive = false;
                }
            }
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
        InputFilter[] inputFilters = inputBuffer.getFilters();
        if ( http11 ) {
            MessageBytes transferEncodingValueMB = headers.getValue ( "transfer-encoding" );
            if ( transferEncodingValueMB != null ) {
                String transferEncodingValue = transferEncodingValueMB.toString();
                int startPos = 0;
                int commaPos = transferEncodingValue.indexOf ( ',' );
                String encodingName = null;
                while ( commaPos != -1 ) {
                    encodingName = transferEncodingValue.substring ( startPos, commaPos );
                    addInputFilter ( inputFilters, encodingName );
                    startPos = commaPos + 1;
                    commaPos = transferEncodingValue.indexOf ( ',', startPos );
                }
                encodingName = transferEncodingValue.substring ( startPos );
                addInputFilter ( inputFilters, encodingName );
            }
        }
        long contentLength = request.getContentLengthLong();
        if ( contentLength >= 0 ) {
            if ( contentDelimitation ) {
                headers.removeHeader ( "content-length" );
                request.setContentLength ( -1 );
            } else {
                inputBuffer.addActiveFilter
                ( inputFilters[Constants.IDENTITY_FILTER] );
                contentDelimitation = true;
            }
        }
        MessageBytes valueMB = headers.getValue ( "host" );
        if ( http11 && ( valueMB == null ) ) {
            response.setStatus ( 400 );
            setErrorState ( ErrorState.CLOSE_CLEAN, null );
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "http11processor.request.prepare" ) +
                            " host header missing" );
            }
        }
        parseHost ( valueMB );
        if ( !contentDelimitation ) {
            inputBuffer.addActiveFilter
            ( inputFilters[Constants.VOID_FILTER] );
            contentDelimitation = true;
        }
        if ( getErrorState().isError() ) {
            getAdapter().log ( request, response, 0 );
        }
    }
    @Override
    protected final void prepareResponse() throws IOException {
        boolean entityBody = true;
        contentDelimitation = false;
        OutputFilter[] outputFilters = outputBuffer.getFilters();
        if ( http09 == true ) {
            outputBuffer.addActiveFilter ( outputFilters[Constants.IDENTITY_FILTER] );
            outputBuffer.commit();
            return;
        }
        int statusCode = response.getStatus();
        if ( statusCode < 200 || statusCode == 204 || statusCode == 205 ||
                statusCode == 304 ) {
            outputBuffer.addActiveFilter
            ( outputFilters[Constants.VOID_FILTER] );
            entityBody = false;
            contentDelimitation = true;
        }
        MessageBytes methodMB = request.method();
        if ( methodMB.equals ( "HEAD" ) ) {
            outputBuffer.addActiveFilter
            ( outputFilters[Constants.VOID_FILTER] );
            contentDelimitation = true;
        }
        boolean sendingWithSendfile = false;
        if ( endpoint.getUseSendfile() ) {
            sendingWithSendfile = prepareSendfile ( outputFilters );
        }
        boolean isCompressable = false;
        boolean useCompression = false;
        if ( entityBody && ( compressionLevel > 0 ) && !sendingWithSendfile ) {
            isCompressable = isCompressable();
            if ( isCompressable ) {
                useCompression = useCompression();
            }
            if ( useCompression ) {
                response.setContentLength ( -1 );
            }
        }
        MimeHeaders headers = response.getMimeHeaders();
        if ( !entityBody ) {
            response.setContentLength ( -1 );
        }
        if ( entityBody || statusCode == HttpServletResponse.SC_NO_CONTENT ) {
            String contentType = response.getContentType();
            if ( contentType != null ) {
                headers.setValue ( "Content-Type" ).setString ( contentType );
            }
            String contentLanguage = response.getContentLanguage();
            if ( contentLanguage != null ) {
                headers.setValue ( "Content-Language" )
                .setString ( contentLanguage );
            }
        }
        long contentLength = response.getContentLengthLong();
        boolean connectionClosePresent = false;
        if ( contentLength != -1 ) {
            headers.setValue ( "Content-Length" ).setLong ( contentLength );
            outputBuffer.addActiveFilter
            ( outputFilters[Constants.IDENTITY_FILTER] );
            contentDelimitation = true;
        } else {
            connectionClosePresent = isConnectionClose ( headers );
            if ( entityBody && http11 && !connectionClosePresent ) {
                outputBuffer.addActiveFilter
                ( outputFilters[Constants.CHUNKED_FILTER] );
                contentDelimitation = true;
                headers.addValue ( Constants.TRANSFERENCODING ).setString ( Constants.CHUNKED );
            } else {
                outputBuffer.addActiveFilter
                ( outputFilters[Constants.IDENTITY_FILTER] );
            }
        }
        if ( useCompression ) {
            outputBuffer.addActiveFilter ( outputFilters[Constants.GZIP_FILTER] );
            headers.setValue ( "Content-Encoding" ).setString ( "gzip" );
        }
        if ( isCompressable ) {
            MessageBytes vary = headers.getValue ( "Vary" );
            if ( vary == null ) {
                headers.setValue ( "Vary" ).setString ( "Accept-Encoding" );
            } else if ( vary.equals ( "*" ) ) {
            } else {
                headers.setValue ( "Vary" ).setString (
                    vary.getString() + ",Accept-Encoding" );
            }
        }
        if ( headers.getValue ( "Date" ) == null ) {
            headers.addValue ( "Date" ).setString (
                FastHttpDateFormat.getCurrentDate() );
        }
        if ( ( entityBody ) && ( !contentDelimitation ) ) {
            keepAlive = false;
        }
        checkExpectationAndResponseStatus();
        if ( keepAlive && statusDropsConnection ( statusCode ) ) {
            keepAlive = false;
        }
        if ( !keepAlive ) {
            if ( !connectionClosePresent ) {
                headers.addValue ( Constants.CONNECTION ).setString (
                    Constants.CLOSE );
            }
        } else if ( !http11 && !getErrorState().isError() ) {
            headers.addValue ( Constants.CONNECTION ).setString ( Constants.KEEPALIVE );
        }
        if ( server == null ) {
            if ( serverRemoveAppProvidedValues ) {
                headers.removeHeader ( "server" );
            }
        } else {
            headers.setValue ( "Server" ).setString ( server );
        }
        try {
            outputBuffer.sendStatus();
            int size = headers.size();
            for ( int i = 0; i < size; i++ ) {
                outputBuffer.sendHeader ( headers.getName ( i ), headers.getValue ( i ) );
            }
            outputBuffer.endHeaders();
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            outputBuffer.resetHeaderBuffer();
            throw t;
        }
        outputBuffer.commit();
    }
    private static boolean isConnectionClose ( MimeHeaders headers ) {
        MessageBytes connection = headers.getValue ( Constants.CONNECTION );
        if ( connection == null ) {
            return false;
        }
        return connection.equals ( Constants.CLOSE );
    }
    private boolean prepareSendfile ( OutputFilter[] outputFilters ) {
        String fileName = ( String ) request.getAttribute (
                              org.apache.coyote.Constants.SENDFILE_FILENAME_ATTR );
        if ( fileName != null ) {
            outputBuffer.addActiveFilter ( outputFilters[Constants.VOID_FILTER] );
            contentDelimitation = true;
            long pos = ( ( Long ) request.getAttribute (
                             org.apache.coyote.Constants.SENDFILE_FILE_START_ATTR ) ).longValue();
            long end = ( ( Long ) request.getAttribute (
                             org.apache.coyote.Constants.SENDFILE_FILE_END_ATTR ) ).longValue();
            sendfileData = socketWrapper.createSendfileData ( fileName, pos, end - pos );
            return true;
        }
        return false;
    }
    private void parseHost ( MessageBytes valueMB ) {
        if ( valueMB == null || valueMB.isNull() ) {
            request.setServerPort ( endpoint.getPort() );
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
                if ( charValue == -1 || charValue > 9 ) {
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
    protected boolean flushBufferedWrite() throws IOException {
        if ( outputBuffer.hasDataToWrite() ) {
            if ( outputBuffer.flushBuffer ( false ) ) {
                outputBuffer.registerWriteInterest();
                return true;
            }
        }
        return false;
    }
    @Override
    protected SocketState dispatchEndRequest() {
        if ( !keepAlive ) {
            return SocketState.CLOSED;
        } else {
            endRequest();
            inputBuffer.nextRequest();
            outputBuffer.nextRequest();
            if ( socketWrapper.isReadPending() ) {
                return SocketState.LONG;
            } else {
                return SocketState.OPEN;
            }
        }
    }
    @Override
    protected Log getLog() {
        return log;
    }
    private void endRequest() {
        if ( getErrorState().isError() ) {
            inputBuffer.setSwallowInput ( false );
        } else {
            checkExpectationAndResponseStatus();
        }
        if ( getErrorState().isIoAllowed() ) {
            try {
                inputBuffer.endRequest();
            } catch ( IOException e ) {
                setErrorState ( ErrorState.CLOSE_CONNECTION_NOW, e );
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                response.setStatus ( 500 );
                setErrorState ( ErrorState.CLOSE_NOW, t );
                log.error ( sm.getString ( "http11processor.request.finish" ), t );
            }
        }
        if ( getErrorState().isIoAllowed() ) {
            try {
                action ( ActionCode.COMMIT, null );
                outputBuffer.finishResponse();
            } catch ( IOException e ) {
                setErrorState ( ErrorState.CLOSE_CONNECTION_NOW, e );
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                setErrorState ( ErrorState.CLOSE_NOW, t );
                log.error ( sm.getString ( "http11processor.response.finish" ), t );
            }
        }
    }
    @Override
    protected final void finishResponse() throws IOException {
        outputBuffer.finishResponse();
    }
    @Override
    protected final void ack() {
        if ( !response.isCommitted() && request.hasExpectation() ) {
            inputBuffer.setSwallowInput ( true );
            try {
                outputBuffer.sendAck();
            } catch ( IOException e ) {
                setErrorState ( ErrorState.CLOSE_CONNECTION_NOW, e );
            }
        }
    }
    @Override
    protected final void flush() throws IOException {
        outputBuffer.flush();
    }
    @Override
    protected final int available ( boolean doRead ) {
        return inputBuffer.available ( doRead );
    }
    @Override
    protected final void setRequestBody ( ByteChunk body ) {
        InputFilter savedBody = new SavedRequestInputFilter ( body );
        savedBody.setRequest ( request );
        Http11InputBuffer internalBuffer = ( Http11InputBuffer ) request.getInputBuffer();
        internalBuffer.addActiveFilter ( savedBody );
    }
    @Override
    protected final void setSwallowResponse() {
        outputBuffer.responseFinished = true;
    }
    @Override
    protected final void disableSwallowRequest() {
        inputBuffer.setSwallowInput ( false );
    }
    @Override
    protected final void sslReHandShake() {
        if ( sslSupport != null ) {
            InputFilter[] inputFilters = inputBuffer.getFilters();
            ( ( BufferedInputFilter ) inputFilters[Constants.BUFFERED_FILTER] ).setLimit (
                maxSavePostSize );
            inputBuffer.addActiveFilter ( inputFilters[Constants.BUFFERED_FILTER] );
            try {
                socketWrapper.doClientAuth ( sslSupport );
                Object sslO = sslSupport.getPeerCertificateChain();
                if ( sslO != null ) {
                    request.setAttribute ( SSLSupport.CERTIFICATE_KEY, sslO );
                }
            } catch ( IOException ioe ) {
                log.warn ( sm.getString ( "http11processor.socket.ssl" ), ioe );
            }
        }
    }
    @Override
    protected final boolean isRequestBodyFullyRead() {
        return inputBuffer.isFinished();
    }
    @Override
    protected final void registerReadInterest() {
        socketWrapper.registerReadInterest();
    }
    @Override
    protected final boolean isReady() {
        return outputBuffer.isReady();
    }
    @Override
    protected final void executeDispatches ( SocketWrapperBase<?> wrapper ) {
        wrapper.executeNonBlockingDispatches ( getIteratorAndClearDispatches() );
    }
    @Override
    public UpgradeToken getUpgradeToken() {
        return upgradeToken;
    }
    @Override
    protected final void doHttpUpgrade ( UpgradeToken upgradeToken ) {
        this.upgradeToken = upgradeToken;
        outputBuffer.responseFinished = true;
    }
    @Override
    public ByteBuffer getLeftoverInput() {
        return inputBuffer.getLeftover();
    }
    @Override
    public boolean isUpgrade() {
        return upgradeToken != null;
    }
    private boolean breakKeepAliveLoop ( SocketWrapperBase<?> socketWrapper ) {
        openSocket = keepAlive;
        if ( sendfileData != null && !getErrorState().isError() ) {
            sendfileData.keepAlive = keepAlive;
            switch ( socketWrapper.processSendfile ( sendfileData ) ) {
            case DONE:
                sendfileData = null;
                return false;
            case PENDING:
                return true;
            case ERROR:
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "http11processor.sendfile.error" ) );
                }
                setErrorState ( ErrorState.CLOSE_CONNECTION_NOW, null );
                return true;
            }
        }
        return false;
    }
    @Override
    public final void recycle() {
        getAdapter().checkRecycled ( request, response );
        super.recycle();
        inputBuffer.recycle();
        outputBuffer.recycle();
        upgradeToken = null;
        socketWrapper = null;
        sendfileData = null;
    }
    @Override
    public void pause() {
    }
}
