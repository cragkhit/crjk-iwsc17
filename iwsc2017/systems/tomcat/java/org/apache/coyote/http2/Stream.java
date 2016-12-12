package org.apache.coyote.http2;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Iterator;
import org.apache.coyote.ActionCode;
import org.apache.coyote.CloseNowException;
import org.apache.coyote.InputBuffer;
import org.apache.coyote.OutputBuffer;
import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.apache.coyote.http2.HpackDecoder.HeaderEmitter;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.net.ApplicationBufferHandler;
import org.apache.tomcat.util.res.StringManager;
class Stream extends AbstractStream implements HeaderEmitter {
    private static final Log log = LogFactory.getLog ( Stream.class );
    private static final StringManager sm = StringManager.getManager ( Stream.class );
    private static final int HEADER_STATE_START = 0;
    private static final int HEADER_STATE_PSEUDO = 1;
    private static final int HEADER_STATE_REGULAR = 2;
    private static final int HEADER_STATE_TRAILER = 3;
    private static final Response ACK_RESPONSE = new Response();
    static {
        ACK_RESPONSE.setStatus ( 100 );
    }
    private volatile int weight = Constants.DEFAULT_WEIGHT;
    private final Http2UpgradeHandler handler;
    private final StreamStateMachine state;
    private int headerState = HEADER_STATE_START;
    private String headerStateErrorMsg = null;
    private final Request coyoteRequest;
    private StringBuilder cookieHeader = null;
    private final Response coyoteResponse = new Response();
    private final StreamInputBuffer inputBuffer;
    private final StreamOutputBuffer outputBuffer = new StreamOutputBuffer();
    Stream ( Integer identifier, Http2UpgradeHandler handler ) {
        this ( identifier, handler, null );
    }
    Stream ( Integer identifier, Http2UpgradeHandler handler, Request coyoteRequest ) {
        super ( identifier );
        this.handler = handler;
        setParentStream ( handler );
        setWindowSize ( handler.getRemoteSettings().getInitialWindowSize() );
        state = new StreamStateMachine ( this );
        if ( coyoteRequest == null ) {
            this.coyoteRequest = new Request();
            this.inputBuffer = new StreamInputBuffer();
            this.coyoteRequest.setInputBuffer ( inputBuffer );
        } else {
            this.coyoteRequest = coyoteRequest;
            this.inputBuffer = null;
            state.receivedStartOfHeaders();
            state.recievedEndOfStream();
        }
        this.coyoteRequest.setSendfile ( false );
        this.coyoteResponse.setOutputBuffer ( outputBuffer );
        this.coyoteRequest.setResponse ( coyoteResponse );
        this.coyoteRequest.protocol().setString ( "HTTP/2.0" );
    }
    final void rePrioritise ( AbstractStream parent, boolean exclusive, int weight ) {
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "stream.reprioritisation.debug",
                                       getConnectionId(), getIdentifier(), Boolean.toString ( exclusive ),
                                       parent.getIdentifier(), Integer.toString ( weight ) ) );
        }
        if ( isDescendant ( parent ) ) {
            parent.detachFromParent();
            getParentStream().addChild ( parent );
        }
        if ( exclusive ) {
            Iterator<AbstractStream> parentsChildren = parent.getChildStreams().iterator();
            while ( parentsChildren.hasNext() ) {
                AbstractStream parentsChild = parentsChildren.next();
                parentsChildren.remove();
                this.addChild ( parentsChild );
            }
        }
        parent.addChild ( this );
        this.weight = weight;
    }
    final void receiveReset ( long errorCode ) {
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "stream.reset.receive", getConnectionId(), getIdentifier(),
                                       Long.toString ( errorCode ) ) );
        }
        state.receivedReset();
        if ( inputBuffer != null ) {
            inputBuffer.receiveReset();
        }
        synchronized ( this ) {
            this.notifyAll();
        }
    }
    final void checkState ( FrameType frameType ) throws Http2Exception {
        state.checkFrameType ( frameType );
    }
    @Override
    final synchronized void incrementWindowSize ( int windowSizeIncrement ) throws Http2Exception {
        boolean notify = getWindowSize() < 1;
        super.incrementWindowSize ( windowSizeIncrement );
        if ( notify && getWindowSize() > 0 ) {
            notifyAll();
        }
    }
    private final synchronized int reserveWindowSize ( int reservation, boolean block )
    throws IOException {
        long windowSize = getWindowSize();
        while ( windowSize < 1 ) {
            if ( !canWrite() ) {
                throw new CloseNowException ( sm.getString ( "stream.notWritable",
                                              getConnectionId(), getIdentifier() ) );
            }
            try {
                if ( block ) {
                    wait();
                } else {
                    return 0;
                }
            } catch ( InterruptedException e ) {
                throw new IOException ( e );
            }
            windowSize = getWindowSize();
        }
        int allocation;
        if ( windowSize < reservation ) {
            allocation = ( int ) windowSize;
        } else {
            allocation = reservation;
        }
        decrementWindowSize ( allocation );
        return allocation;
    }
    @Override
    public final void emitHeader ( String name, String value ) {
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "stream.header.debug", getConnectionId(), getIdentifier(),
                                       name, value ) );
        }
        if ( headerStateErrorMsg != null ) {
            return;
        }
        boolean pseudoHeader = name.charAt ( 0 ) == ':';
        if ( pseudoHeader && headerState != HEADER_STATE_PSEUDO ) {
            headerStateErrorMsg = sm.getString ( "stream.header.unexpectedPseudoHeader",
                                                 getConnectionId(), getIdentifier(), name );
            return;
        }
        if ( headerState == HEADER_STATE_PSEUDO && !pseudoHeader ) {
            headerState = HEADER_STATE_REGULAR;
        }
        switch ( name ) {
        case ":method": {
            coyoteRequest.method().setString ( value );
            break;
        }
        case ":scheme": {
            coyoteRequest.scheme().setString ( value );
            break;
        }
        case ":path": {
            int queryStart = value.indexOf ( '?' );
            if ( queryStart == -1 ) {
                coyoteRequest.requestURI().setString ( value );
                coyoteRequest.decodedURI().setString ( coyoteRequest.getURLDecoder().convert ( value, false ) );
            } else {
                String uri = value.substring ( 0, queryStart );
                String query = value.substring ( queryStart + 1 );
                coyoteRequest.requestURI().setString ( uri );
                coyoteRequest.decodedURI().setString ( coyoteRequest.getURLDecoder().convert ( uri, false ) );
                coyoteRequest.queryString().setString ( coyoteRequest.getURLDecoder().convert ( query, true ) );
            }
            break;
        }
        case ":authority": {
            int i = value.lastIndexOf ( ':' );
            if ( i > -1 ) {
                coyoteRequest.serverName().setString ( value.substring ( 0, i ) );
                coyoteRequest.setServerPort ( Integer.parseInt ( value.substring ( i + 1 ) ) );
            } else {
                coyoteRequest.serverName().setString ( value );
            }
            break;
        }
        case "cookie": {
            if ( cookieHeader == null ) {
                cookieHeader = new StringBuilder();
            } else {
                cookieHeader.append ( "; " );
            }
            cookieHeader.append ( value );
            break;
        }
        default: {
            if ( headerState == HEADER_STATE_TRAILER && !handler.isTrailerHeaderAllowed ( name ) ) {
                break;
            }
            if ( "expect".equals ( name ) && "100-continue".equals ( value ) ) {
                coyoteRequest.setExpectation ( true );
            }
            if ( pseudoHeader ) {
                headerStateErrorMsg = sm.getString ( "stream.header.unknownPseudoHeader",
                                                     getConnectionId(), getIdentifier(), name );
            }
            coyoteRequest.getMimeHeaders().addValue ( name ).setString ( value );
        }
        }
    }
    @Override
    public void validateHeaders() throws StreamException {
        if ( headerStateErrorMsg == null ) {
            return;
        }
        throw new StreamException ( headerStateErrorMsg, Http2Error.PROTOCOL_ERROR,
                                    getIdentifier().intValue() );
    }
    final boolean receivedEndOfHeaders() {
        if ( cookieHeader != null ) {
            coyoteRequest.getMimeHeaders().addValue ( "cookie" ).setString ( cookieHeader.toString() );
        }
        return headerState == HEADER_STATE_REGULAR || headerState == HEADER_STATE_PSEUDO;
    }
    final void writeHeaders() throws IOException {
        handler.writeHeaders ( this, coyoteResponse, 1024 );
    }
    final void writeAck() throws IOException {
        handler.writeHeaders ( this, ACK_RESPONSE, 64 );
    }
    final void flushData() throws IOException {
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "stream.write", getConnectionId(), getIdentifier() ) );
        }
        outputBuffer.flush ( true );
    }
    @Override
    final String getConnectionId() {
        return getParentStream().getConnectionId();
    }
    @Override
    final int getWeight() {
        return weight;
    }
    final Request getCoyoteRequest() {
        return coyoteRequest;
    }
    final Response getCoyoteResponse() {
        return coyoteResponse;
    }
    final ByteBuffer getInputByteBuffer() {
        return inputBuffer.getInBuffer();
    }
    final void receivedStartOfHeaders ( boolean headersEndStream ) throws Http2Exception {
        if ( headerState == HEADER_STATE_START ) {
            headerState = HEADER_STATE_PSEUDO;
            handler.getHpackDecoder().setMaxHeaderCount ( handler.getMaxHeaderCount() );
            handler.getHpackDecoder().setMaxHeaderSize ( handler.getMaxHeaderSize() );
        } else if ( headerState == HEADER_STATE_PSEUDO || headerState == HEADER_STATE_REGULAR ) {
            if ( headersEndStream ) {
                headerState = HEADER_STATE_TRAILER;
                handler.getHpackDecoder().setMaxHeaderCount ( handler.getMaxTrailerCount() );
                handler.getHpackDecoder().setMaxHeaderSize ( handler.getMaxTrailerSize() );
            } else {
                throw new ConnectionException ( sm.getString ( "stream.trialerHeader.noEndOfStream",
                                                getConnectionId(), getIdentifier() ), Http2Error.PROTOCOL_ERROR );
            }
        }
        state.receivedStartOfHeaders();
    }
    final void receivedEndOfStream() {
        synchronized ( inputBuffer ) {
            inputBuffer.notifyAll();
        }
        state.recievedEndOfStream();
    }
    final void sentEndOfStream() {
        outputBuffer.endOfStreamSent = true;
        state.sentEndOfStream();
    }
    final StreamInputBuffer getInputBuffer() {
        return inputBuffer;
    }
    final StreamOutputBuffer getOutputBuffer() {
        return outputBuffer;
    }
    final void sentPushPromise() {
        state.sentPushPromise();
    }
    final boolean isActive() {
        return state.isActive();
    }
    final boolean canWrite() {
        return state.canWrite();
    }
    final boolean isClosedFinal() {
        return state.isClosedFinal();
    }
    final void closeIfIdle() {
        state.closeIfIdle();
    }
    private final boolean isInputFinished() {
        return !state.isFrameTypePermitted ( FrameType.DATA );
    }
    final void close ( Http2Exception http2Exception ) {
        if ( http2Exception instanceof StreamException ) {
            try {
                StreamException se = ( StreamException ) http2Exception;
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "stream.reset.send", getConnectionId(), getIdentifier(),
                                               Long.toString ( se.getError().getCode() ) ) );
                }
                state.sendReset();
                handler.sendStreamReset ( se );
            } catch ( IOException ioe ) {
                ConnectionException ce = new ConnectionException (
                    sm.getString ( "stream.reset.fail" ), Http2Error.PROTOCOL_ERROR );
                ce.initCause ( ioe );
                handler.closeConnection ( ce );
            }
        } else {
            handler.closeConnection ( http2Exception );
        }
    }
    final boolean isPushSupported() {
        return handler.getRemoteSettings().getEnablePush();
    }
    final boolean push ( Request request ) throws IOException {
        if ( !isPushSupported() ) {
            return false;
        }
        request.getMimeHeaders().addValue ( ":method" ).duplicate ( request.method() );
        request.getMimeHeaders().addValue ( ":scheme" ).duplicate ( request.scheme() );
        StringBuilder path = new StringBuilder ( request.requestURI().toString() );
        if ( !request.queryString().isNull() ) {
            path.append ( '?' );
            path.append ( request.queryString().toString() );
        }
        request.getMimeHeaders().addValue ( ":path" ).setString ( path.toString() );
        if ( ! ( request.scheme().equals ( "http" ) && request.getServerPort() == 80 ) &&
                ! ( request.scheme().equals ( "https" ) && request.getServerPort() == 443 ) ) {
            request.getMimeHeaders().addValue ( ":authority" ).setString (
                request.serverName().getString() + ":" + request.getServerPort() );
        } else {
            request.getMimeHeaders().addValue ( ":authority" ).duplicate ( request.serverName() );
        }
        push ( handler, request, this );
        return true;
    }
    private static void push ( final Http2UpgradeHandler handler, final Request request,
                               final Stream stream ) throws IOException {
        if ( org.apache.coyote.Constants.IS_SECURITY_ENABLED ) {
            try {
                AccessController.doPrivileged (
                new PrivilegedExceptionAction<Void>() {
                    @Override
                    public Void run() throws IOException {
                        handler.push ( request, stream );
                        return null;
                    }
                } );
            } catch ( PrivilegedActionException ex ) {
                Exception e = ex.getException();
                if ( e instanceof IOException ) {
                    throw ( IOException ) e;
                } else {
                    throw new IOException ( ex );
                }
            }
        } else {
            handler.push ( request, stream );
        }
    }
    class StreamOutputBuffer implements OutputBuffer {
        private final ByteBuffer buffer = ByteBuffer.allocate ( 8 * 1024 );
        private volatile long written = 0;
        private volatile boolean closed = false;
        private volatile boolean endOfStreamSent = false;
        @Override
        public final synchronized int doWrite ( ByteBuffer chunk ) throws IOException {
            if ( closed ) {
                throw new IllegalStateException (
                    sm.getString ( "stream.closed", getConnectionId(), getIdentifier() ) );
            }
            if ( !coyoteResponse.isCommitted() ) {
                coyoteResponse.sendHeaders();
            }
            int chunkLimit = chunk.limit();
            int offset = 0;
            while ( chunk.remaining() > 0 ) {
                int thisTime = Math.min ( buffer.remaining(), chunk.remaining() );
                chunk.limit ( chunk.position() + thisTime );
                buffer.put ( chunk );
                chunk.limit ( chunkLimit );
                offset += thisTime;
                if ( chunk.remaining() > 0 && !buffer.hasRemaining() ) {
                    if ( flush ( true, coyoteResponse.getWriteListener() == null ) ) {
                        break;
                    }
                }
            }
            written += offset;
            return offset;
        }
        final synchronized boolean flush ( boolean block ) throws IOException {
            return flush ( false, block );
        }
        private final synchronized boolean flush ( boolean writeInProgress, boolean block )
        throws IOException {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "stream.outputBuffer.flush.debug", getConnectionId(),
                                           getIdentifier(), Integer.toString ( buffer.position() ),
                                           Boolean.toString ( writeInProgress ), Boolean.toString ( closed ) ) );
            }
            if ( buffer.position() == 0 ) {
                if ( closed && !endOfStreamSent ) {
                    handler.writeBody ( Stream.this, buffer, 0, true );
                }
                return false;
            }
            buffer.flip();
            int left = buffer.remaining();
            while ( left > 0 ) {
                int streamReservation  = reserveWindowSize ( left, block );
                if ( streamReservation == 0 ) {
                    buffer.compact();
                    return true;
                }
                while ( streamReservation > 0 ) {
                    int connectionReservation =
                        handler.reserveWindowSize ( Stream.this, streamReservation );
                    handler.writeBody ( Stream.this, buffer, connectionReservation,
                                        !writeInProgress && closed && left == connectionReservation );
                    streamReservation -= connectionReservation;
                    left -= connectionReservation;
                }
            }
            buffer.clear();
            return false;
        }
        final synchronized boolean isReady() {
            if ( getWindowSize() > 0 && handler.getWindowSize() > 0 ) {
                return true;
            } else {
                return false;
            }
        }
        @Override
        public final long getBytesWritten() {
            return written;
        }
        final void close() throws IOException {
            closed = true;
            flushData();
        }
        final boolean hasNoBody() {
            return ( ( written == 0 ) && closed );
        }
    }
    class StreamInputBuffer implements InputBuffer {
        private byte[] outBuffer;
        private volatile ByteBuffer inBuffer;
        private volatile boolean readInterest;
        private boolean reset = false;
        @Override
        public final int doRead ( ApplicationBufferHandler applicationBufferHandler )
        throws IOException {
            ensureBuffersExist();
            int written = -1;
            synchronized ( inBuffer ) {
                while ( inBuffer.position() == 0 && !isInputFinished() ) {
                    try {
                        if ( log.isDebugEnabled() ) {
                            log.debug ( sm.getString ( "stream.inputBuffer.empty" ) );
                        }
                        inBuffer.wait();
                        if ( reset ) {
                            throw new IOException ( "HTTP/2 Stream reset" );
                        }
                    } catch ( InterruptedException e ) {
                        throw new IOException ( e );
                    }
                }
                if ( inBuffer.position() > 0 ) {
                    inBuffer.flip();
                    written = inBuffer.remaining();
                    if ( log.isDebugEnabled() ) {
                        log.debug ( sm.getString ( "stream.inputBuffer.copy",
                                                   Integer.toString ( written ) ) );
                    }
                    inBuffer.get ( outBuffer, 0, written );
                    inBuffer.clear();
                } else if ( isInputFinished() ) {
                    return -1;
                } else {
                    throw new IllegalStateException();
                }
            }
            applicationBufferHandler.setByteBuffer ( ByteBuffer.wrap ( outBuffer, 0,  written ) );
            handler.writeWindowUpdate ( Stream.this, written, true );
            return written;
        }
        final void registerReadInterest() {
            synchronized ( inBuffer ) {
                readInterest = true;
            }
        }
        final synchronized boolean isRequestBodyFullyRead() {
            return ( inBuffer == null || inBuffer.position() == 0 ) && isInputFinished();
        }
        final synchronized int available() {
            if ( inBuffer == null ) {
                return 0;
            }
            return inBuffer.position();
        }
        final synchronized boolean onDataAvailable() {
            if ( readInterest ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "stream.inputBuffer.dispatch" ) );
                }
                readInterest = false;
                coyoteRequest.action ( ActionCode.DISPATCH_READ, null );
                coyoteRequest.action ( ActionCode.DISPATCH_EXECUTE, null );
                return true;
            } else {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "stream.inputBuffer.signal" ) );
                }
                synchronized ( inBuffer ) {
                    inBuffer.notifyAll();
                }
                return false;
            }
        }
        private final ByteBuffer getInBuffer() {
            ensureBuffersExist();
            return inBuffer;
        }
        final synchronized void insertReplayedBody ( ByteChunk body ) {
            inBuffer = ByteBuffer.wrap ( body.getBytes(),  body.getOffset(),  body.getLength() );
        }
        private final void ensureBuffersExist() {
            if ( inBuffer == null ) {
                int size = handler.getLocalSettings().getInitialWindowSize();
                synchronized ( this ) {
                    if ( inBuffer == null ) {
                        inBuffer = ByteBuffer.allocate ( size );
                        outBuffer = new byte[size];
                    }
                }
            }
        }
        private final void receiveReset() {
            if ( inBuffer != null ) {
                synchronized ( inBuffer ) {
                    reset = true;
                    inBuffer.notifyAll();
                }
            }
        }
    }
}
