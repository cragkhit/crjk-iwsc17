package org.apache.coyote;
import java.io.IOException;
import org.apache.tomcat.util.net.ApplicationBufferHandler;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.servlet.ReadListener;
import java.util.HashMap;
import org.apache.tomcat.util.http.Parameters;
import org.apache.tomcat.util.http.ServerCookies;
import org.apache.tomcat.util.buf.UDecoder;
import java.util.Map;
import org.apache.tomcat.util.http.MimeHeaders;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.res.StringManager;
public final class Request {
    private static final StringManager sm;
    private static final int INITIAL_COOKIE_SIZE = 4;
    private int serverPort;
    private final MessageBytes serverNameMB;
    private int remotePort;
    private int localPort;
    private final MessageBytes schemeMB;
    private final MessageBytes methodMB;
    private final MessageBytes uriMB;
    private final MessageBytes decodedUriMB;
    private final MessageBytes queryMB;
    private final MessageBytes protoMB;
    private final MessageBytes remoteAddrMB;
    private final MessageBytes localNameMB;
    private final MessageBytes remoteHostMB;
    private final MessageBytes localAddrMB;
    private final MimeHeaders headers;
    private final Map<String, String> pathParameters;
    private final Object[] notes;
    private InputBuffer inputBuffer;
    private final UDecoder urlDecoder;
    private long contentLength;
    private MessageBytes contentTypeMB;
    private String charEncoding;
    private boolean expectation;
    private final ServerCookies serverCookies;
    private final Parameters parameters;
    private final MessageBytes remoteUser;
    private boolean remoteUserNeedsAuthorization;
    private final MessageBytes authType;
    private final HashMap<String, Object> attributes;
    private Response response;
    private volatile ActionHook hook;
    private long bytesRead;
    private long startTime;
    private int available;
    private final RequestInfo reqProcessorMX;
    private boolean sendfile;
    volatile ReadListener listener;
    private final AtomicBoolean allDataReadEventSent;
    public Request() {
        this.serverPort = -1;
        this.serverNameMB = MessageBytes.newInstance();
        this.schemeMB = MessageBytes.newInstance();
        this.methodMB = MessageBytes.newInstance();
        this.uriMB = MessageBytes.newInstance();
        this.decodedUriMB = MessageBytes.newInstance();
        this.queryMB = MessageBytes.newInstance();
        this.protoMB = MessageBytes.newInstance();
        this.remoteAddrMB = MessageBytes.newInstance();
        this.localNameMB = MessageBytes.newInstance();
        this.remoteHostMB = MessageBytes.newInstance();
        this.localAddrMB = MessageBytes.newInstance();
        this.headers = new MimeHeaders();
        this.pathParameters = new HashMap<String, String>();
        this.notes = new Object[32];
        this.inputBuffer = null;
        this.urlDecoder = new UDecoder();
        this.contentLength = -1L;
        this.contentTypeMB = null;
        this.charEncoding = null;
        this.expectation = false;
        this.serverCookies = new ServerCookies ( 4 );
        this.parameters = new Parameters();
        this.remoteUser = MessageBytes.newInstance();
        this.remoteUserNeedsAuthorization = false;
        this.authType = MessageBytes.newInstance();
        this.attributes = new HashMap<String, Object>();
        this.bytesRead = 0L;
        this.startTime = -1L;
        this.available = 0;
        this.reqProcessorMX = new RequestInfo ( this );
        this.sendfile = true;
        this.allDataReadEventSent = new AtomicBoolean ( false );
        this.parameters.setQuery ( this.queryMB );
        this.parameters.setURLDecoder ( this.urlDecoder );
    }
    public ReadListener getReadListener() {
        return this.listener;
    }
    public void setReadListener ( final ReadListener listener ) {
        if ( listener == null ) {
            throw new NullPointerException ( Request.sm.getString ( "request.nullReadListener" ) );
        }
        if ( this.getReadListener() != null ) {
            throw new IllegalStateException ( Request.sm.getString ( "request.readListenerSet" ) );
        }
        final AtomicBoolean result = new AtomicBoolean ( false );
        this.action ( ActionCode.ASYNC_IS_ASYNC, result );
        if ( !result.get() ) {
            throw new IllegalStateException ( Request.sm.getString ( "request.notAsync" ) );
        }
        this.listener = listener;
    }
    public boolean sendAllDataReadEvent() {
        return this.allDataReadEventSent.compareAndSet ( false, true );
    }
    public MimeHeaders getMimeHeaders() {
        return this.headers;
    }
    public UDecoder getURLDecoder() {
        return this.urlDecoder;
    }
    public MessageBytes scheme() {
        return this.schemeMB;
    }
    public MessageBytes method() {
        return this.methodMB;
    }
    public MessageBytes requestURI() {
        return this.uriMB;
    }
    public MessageBytes decodedURI() {
        return this.decodedUriMB;
    }
    public MessageBytes queryString() {
        return this.queryMB;
    }
    public MessageBytes protocol() {
        return this.protoMB;
    }
    public MessageBytes serverName() {
        return this.serverNameMB;
    }
    public int getServerPort() {
        return this.serverPort;
    }
    public void setServerPort ( final int serverPort ) {
        this.serverPort = serverPort;
    }
    public MessageBytes remoteAddr() {
        return this.remoteAddrMB;
    }
    public MessageBytes remoteHost() {
        return this.remoteHostMB;
    }
    public MessageBytes localName() {
        return this.localNameMB;
    }
    public MessageBytes localAddr() {
        return this.localAddrMB;
    }
    public int getRemotePort() {
        return this.remotePort;
    }
    public void setRemotePort ( final int port ) {
        this.remotePort = port;
    }
    public int getLocalPort() {
        return this.localPort;
    }
    public void setLocalPort ( final int port ) {
        this.localPort = port;
    }
    public String getCharacterEncoding() {
        if ( this.charEncoding != null ) {
            return this.charEncoding;
        }
        return this.charEncoding = getCharsetFromContentType ( this.getContentType() );
    }
    public void setCharacterEncoding ( final String enc ) {
        this.charEncoding = enc;
    }
    public void setContentLength ( final long len ) {
        this.contentLength = len;
    }
    public int getContentLength() {
        final long length = this.getContentLengthLong();
        if ( length < 2147483647L ) {
            return ( int ) length;
        }
        return -1;
    }
    public long getContentLengthLong() {
        if ( this.contentLength > -1L ) {
            return this.contentLength;
        }
        final MessageBytes clB = this.headers.getUniqueValue ( "content-length" );
        return this.contentLength = ( ( clB == null || clB.isNull() ) ? -1L : clB.getLong() );
    }
    public String getContentType() {
        this.contentType();
        if ( this.contentTypeMB == null || this.contentTypeMB.isNull() ) {
            return null;
        }
        return this.contentTypeMB.toString();
    }
    public void setContentType ( final String type ) {
        this.contentTypeMB.setString ( type );
    }
    public MessageBytes contentType() {
        if ( this.contentTypeMB == null ) {
            this.contentTypeMB = this.headers.getValue ( "content-type" );
        }
        return this.contentTypeMB;
    }
    public void setContentType ( final MessageBytes mb ) {
        this.contentTypeMB = mb;
    }
    public String getHeader ( final String name ) {
        return this.headers.getHeader ( name );
    }
    public void setExpectation ( final boolean expectation ) {
        this.expectation = expectation;
    }
    public boolean hasExpectation() {
        return this.expectation;
    }
    public Response getResponse() {
        return this.response;
    }
    public void setResponse ( final Response response ) {
        ( this.response = response ).setRequest ( this );
    }
    protected void setHook ( final ActionHook hook ) {
        this.hook = hook;
    }
    public void action ( final ActionCode actionCode, final Object param ) {
        if ( this.hook != null ) {
            if ( param == null ) {
                this.hook.action ( actionCode, this );
            } else {
                this.hook.action ( actionCode, param );
            }
        }
    }
    public ServerCookies getCookies() {
        return this.serverCookies;
    }
    public Parameters getParameters() {
        return this.parameters;
    }
    public void addPathParameter ( final String name, final String value ) {
        this.pathParameters.put ( name, value );
    }
    public String getPathParameter ( final String name ) {
        return this.pathParameters.get ( name );
    }
    public void setAttribute ( final String name, final Object o ) {
        this.attributes.put ( name, o );
    }
    public HashMap<String, Object> getAttributes() {
        return this.attributes;
    }
    public Object getAttribute ( final String name ) {
        return this.attributes.get ( name );
    }
    public MessageBytes getRemoteUser() {
        return this.remoteUser;
    }
    public boolean getRemoteUserNeedsAuthorization() {
        return this.remoteUserNeedsAuthorization;
    }
    public void setRemoteUserNeedsAuthorization ( final boolean remoteUserNeedsAuthorization ) {
        this.remoteUserNeedsAuthorization = remoteUserNeedsAuthorization;
    }
    public MessageBytes getAuthType() {
        return this.authType;
    }
    public int getAvailable() {
        return this.available;
    }
    public void setAvailable ( final int available ) {
        this.available = available;
    }
    public boolean getSendfile() {
        return this.sendfile;
    }
    public void setSendfile ( final boolean sendfile ) {
        this.sendfile = sendfile;
    }
    public boolean isFinished() {
        final AtomicBoolean result = new AtomicBoolean ( false );
        this.action ( ActionCode.REQUEST_BODY_FULLY_READ, result );
        return result.get();
    }
    public boolean getSupportsRelativeRedirects() {
        return !this.protocol().equals ( "" ) && !this.protocol().equals ( "HTTP/1.0" );
    }
    public InputBuffer getInputBuffer() {
        return this.inputBuffer;
    }
    public void setInputBuffer ( final InputBuffer inputBuffer ) {
        this.inputBuffer = inputBuffer;
    }
    public int doRead ( final ApplicationBufferHandler handler ) throws IOException {
        final int n = this.inputBuffer.doRead ( handler );
        if ( n > 0 ) {
            this.bytesRead += n;
        }
        return n;
    }
    @Override
    public String toString() {
        return "R( " + this.requestURI().toString() + ")";
    }
    public long getStartTime() {
        return this.startTime;
    }
    public void setStartTime ( final long startTime ) {
        this.startTime = startTime;
    }
    public final void setNote ( final int pos, final Object value ) {
        this.notes[pos] = value;
    }
    public final Object getNote ( final int pos ) {
        return this.notes[pos];
    }
    public void recycle() {
        this.bytesRead = 0L;
        this.contentLength = -1L;
        this.contentTypeMB = null;
        this.charEncoding = null;
        this.expectation = false;
        this.headers.recycle();
        this.serverNameMB.recycle();
        this.serverPort = -1;
        this.localAddrMB.recycle();
        this.localNameMB.recycle();
        this.localPort = -1;
        this.remoteAddrMB.recycle();
        this.remoteHostMB.recycle();
        this.remotePort = -1;
        this.available = 0;
        this.sendfile = true;
        this.serverCookies.recycle();
        this.parameters.recycle();
        this.pathParameters.clear();
        this.uriMB.recycle();
        this.decodedUriMB.recycle();
        this.queryMB.recycle();
        this.methodMB.recycle();
        this.protoMB.recycle();
        this.schemeMB.recycle();
        this.remoteUser.recycle();
        this.remoteUserNeedsAuthorization = false;
        this.authType.recycle();
        this.attributes.clear();
        this.listener = null;
        this.allDataReadEventSent.set ( false );
        this.startTime = -1L;
    }
    public void updateCounters() {
        this.reqProcessorMX.updateCounters();
    }
    public RequestInfo getRequestProcessor() {
        return this.reqProcessorMX;
    }
    public long getBytesRead() {
        return this.bytesRead;
    }
    public boolean isProcessing() {
        return this.reqProcessorMX.getStage() == 3;
    }
    private static String getCharsetFromContentType ( final String contentType ) {
        if ( contentType == null ) {
            return null;
        }
        final int start = contentType.indexOf ( "charset=" );
        if ( start < 0 ) {
            return null;
        }
        String encoding = contentType.substring ( start + 8 );
        final int end = encoding.indexOf ( 59 );
        if ( end >= 0 ) {
            encoding = encoding.substring ( 0, end );
        }
        encoding = encoding.trim();
        if ( encoding.length() > 2 && encoding.startsWith ( "\"" ) && encoding.endsWith ( "\"" ) ) {
            encoding = encoding.substring ( 1, encoding.length() - 1 );
        }
        return encoding.trim();
    }
    static {
        sm = StringManager.getManager ( Request.class );
    }
}
