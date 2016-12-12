package org.apache.coyote.http2;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.coyote.ActionCode;
import org.apache.tomcat.util.net.ApplicationBufferHandler;
import org.apache.juli.logging.LogFactory;
import java.security.PrivilegedActionException;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import org.apache.coyote.Constants;
import java.nio.ByteBuffer;
import java.io.IOException;
import org.apache.coyote.CloseNowException;
import java.util.Iterator;
import org.apache.coyote.OutputBuffer;
import org.apache.coyote.InputBuffer;
import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
class Stream extends AbstractStream implements HpackDecoder.HeaderEmitter {
    private static final Log log;
    private static final StringManager sm;
    private static final int HEADER_STATE_START = 0;
    private static final int HEADER_STATE_PSEUDO = 1;
    private static final int HEADER_STATE_REGULAR = 2;
    private static final int HEADER_STATE_TRAILER = 3;
    private static final Response ACK_RESPONSE;
    private volatile int weight;
    private final Http2UpgradeHandler handler;
    private final StreamStateMachine state;
    private int headerState;
    private String headerStateErrorMsg;
    private final Request coyoteRequest;
    private StringBuilder cookieHeader;
    private final Response coyoteResponse;
    private final StreamInputBuffer inputBuffer;
    private final StreamOutputBuffer outputBuffer;
    Stream ( final Integer identifier, final Http2UpgradeHandler handler ) {
        this ( identifier, handler, null );
    }
    Stream ( final Integer identifier, final Http2UpgradeHandler handler, final Request coyoteRequest ) {
        super ( identifier );
        this.weight = 16;
        this.headerState = 0;
        this.headerStateErrorMsg = null;
        this.cookieHeader = null;
        this.coyoteResponse = new Response();
        this.outputBuffer = new StreamOutputBuffer();
        this.setParentStream ( this.handler = handler );
        this.setWindowSize ( handler.getRemoteSettings().getInitialWindowSize() );
        this.state = new StreamStateMachine ( this );
        if ( coyoteRequest == null ) {
            this.coyoteRequest = new Request();
            this.inputBuffer = new StreamInputBuffer();
            this.coyoteRequest.setInputBuffer ( this.inputBuffer );
        } else {
            this.coyoteRequest = coyoteRequest;
            this.inputBuffer = null;
            this.state.receivedStartOfHeaders();
            this.state.recievedEndOfStream();
        }
        this.coyoteRequest.setSendfile ( false );
        this.coyoteResponse.setOutputBuffer ( this.outputBuffer );
        this.coyoteRequest.setResponse ( this.coyoteResponse );
        this.coyoteRequest.protocol().setString ( "HTTP/2.0" );
    }
    final void rePrioritise ( final AbstractStream parent, final boolean exclusive, final int weight ) {
        if ( Stream.log.isDebugEnabled() ) {
            Stream.log.debug ( Stream.sm.getString ( "stream.reprioritisation.debug", this.getConnectionId(), this.getIdentifier(), Boolean.toString ( exclusive ), parent.getIdentifier(), Integer.toString ( weight ) ) );
        }
        if ( this.isDescendant ( parent ) ) {
            parent.detachFromParent();
            this.getParentStream().addChild ( parent );
        }
        if ( exclusive ) {
            final Iterator<AbstractStream> parentsChildren = parent.getChildStreams().iterator();
            while ( parentsChildren.hasNext() ) {
                final AbstractStream parentsChild = parentsChildren.next();
                parentsChildren.remove();
                this.addChild ( parentsChild );
            }
        }
        parent.addChild ( this );
        this.weight = weight;
    }
    final void receiveReset ( final long errorCode ) {
        if ( Stream.log.isDebugEnabled() ) {
            Stream.log.debug ( Stream.sm.getString ( "stream.reset.receive", this.getConnectionId(), this.getIdentifier(), Long.toString ( errorCode ) ) );
        }
        this.state.receivedReset();
        if ( this.inputBuffer != null ) {
            this.inputBuffer.receiveReset();
        }
        synchronized ( this ) {
            this.notifyAll();
        }
    }
    final void checkState ( final FrameType frameType ) throws Http2Exception {
        this.state.checkFrameType ( frameType );
    }
    @Override
    final synchronized void incrementWindowSize ( final int windowSizeIncrement ) throws Http2Exception {
        final boolean notify = this.getWindowSize() < 1L;
        super.incrementWindowSize ( windowSizeIncrement );
        if ( notify && this.getWindowSize() > 0L ) {
            this.notifyAll();
        }
    }
    private final synchronized int reserveWindowSize ( final int reservation, final boolean block ) throws IOException {
        long windowSize;
        for ( windowSize = this.getWindowSize(); windowSize < 1L; windowSize = this.getWindowSize() ) {
            if ( !this.canWrite() ) {
                throw new CloseNowException ( Stream.sm.getString ( "stream.notWritable", this.getConnectionId(), this.getIdentifier() ) );
            }
            try {
                if ( !block ) {
                    return 0;
                }
                this.wait();
            } catch ( InterruptedException e ) {
                throw new IOException ( e );
            }
        }
        int allocation;
        if ( windowSize < reservation ) {
            allocation = ( int ) windowSize;
        } else {
            allocation = reservation;
        }
        this.decrementWindowSize ( allocation );
        return allocation;
    }
    @Override
    public final void emitHeader ( final String name, final String value ) {
        if ( Stream.log.isDebugEnabled() ) {
            Stream.log.debug ( Stream.sm.getString ( "stream.header.debug", this.getConnectionId(), this.getIdentifier(), name, value ) );
        }
        if ( this.headerStateErrorMsg != null ) {
            return;
        }
        final boolean pseudoHeader = name.charAt ( 0 ) == ':';
        if ( pseudoHeader && this.headerState != 1 ) {
            this.headerStateErrorMsg = Stream.sm.getString ( "stream.header.unexpectedPseudoHeader", this.getConnectionId(), this.getIdentifier(), name );
            return;
        }
        if ( this.headerState == 1 && !pseudoHeader ) {
            this.headerState = 2;
        }
        switch ( name ) {
        case ":method": {
            this.coyoteRequest.method().setString ( value );
            break;
        }
        case ":scheme": {
            this.coyoteRequest.scheme().setString ( value );
            break;
        }
        case ":path": {
            final int queryStart = value.indexOf ( 63 );
            if ( queryStart == -1 ) {
                this.coyoteRequest.requestURI().setString ( value );
                this.coyoteRequest.decodedURI().setString ( this.coyoteRequest.getURLDecoder().convert ( value, false ) );
                break;
            }
            final String uri = value.substring ( 0, queryStart );
            final String query = value.substring ( queryStart + 1 );
            this.coyoteRequest.requestURI().setString ( uri );
            this.coyoteRequest.decodedURI().setString ( this.coyoteRequest.getURLDecoder().convert ( uri, false ) );
            this.coyoteRequest.queryString().setString ( this.coyoteRequest.getURLDecoder().convert ( query, true ) );
            break;
        }
        case ":authority": {
            final int i = value.lastIndexOf ( 58 );
            if ( i > -1 ) {
                this.coyoteRequest.serverName().setString ( value.substring ( 0, i ) );
                this.coyoteRequest.setServerPort ( Integer.parseInt ( value.substring ( i + 1 ) ) );
                break;
            }
            this.coyoteRequest.serverName().setString ( value );
            break;
        }
        case "cookie": {
            if ( this.cookieHeader == null ) {
                this.cookieHeader = new StringBuilder();
            } else {
                this.cookieHeader.append ( "; " );
            }
            this.cookieHeader.append ( value );
            break;
        }
        default: {
            if ( this.headerState == 3 && !this.handler.isTrailerHeaderAllowed ( name ) ) {
                break;
            }
            if ( "expect".equals ( name ) && "100-continue".equals ( value ) ) {
                this.coyoteRequest.setExpectation ( true );
            }
            if ( pseudoHeader ) {
                this.headerStateErrorMsg = Stream.sm.getString ( "stream.header.unknownPseudoHeader", this.getConnectionId(), this.getIdentifier(), name );
            }
            this.coyoteRequest.getMimeHeaders().addValue ( name ).setString ( value );
            break;
        }
        }
    }
    @Override
    public void validateHeaders() throws StreamException {
        if ( this.headerStateErrorMsg == null ) {
            return;
        }
        throw new StreamException ( this.headerStateErrorMsg, Http2Error.PROTOCOL_ERROR, this.getIdentifier() );
    }
    final boolean receivedEndOfHeaders() {
        if ( this.cookieHeader != null ) {
            this.coyoteRequest.getMimeHeaders().addValue ( "cookie" ).setString ( this.cookieHeader.toString() );
        }
        return this.headerState == 2 || this.headerState == 1;
    }
    final void writeHeaders() throws IOException {
        this.handler.writeHeaders ( this, this.coyoteResponse, 1024 );
    }
    final void writeAck() throws IOException {
        this.handler.writeHeaders ( this, Stream.ACK_RESPONSE, 64 );
    }
    final void flushData() throws IOException {
        if ( Stream.log.isDebugEnabled() ) {
            Stream.log.debug ( Stream.sm.getString ( "stream.write", this.getConnectionId(), this.getIdentifier() ) );
        }
        this.outputBuffer.flush ( true );
    }
    @Override
    final String getConnectionId() {
        return this.getParentStream().getConnectionId();
    }
    @Override
    final int getWeight() {
        return this.weight;
    }
    final Request getCoyoteRequest() {
        return this.coyoteRequest;
    }
    final Response getCoyoteResponse() {
        return this.coyoteResponse;
    }
    final ByteBuffer getInputByteBuffer() {
        return this.inputBuffer.getInBuffer();
    }
    final void receivedStartOfHeaders ( final boolean headersEndStream ) throws Http2Exception {
        if ( this.headerState == 0 ) {
            this.headerState = 1;
            this.handler.getHpackDecoder().setMaxHeaderCount ( this.handler.getMaxHeaderCount() );
            this.handler.getHpackDecoder().setMaxHeaderSize ( this.handler.getMaxHeaderSize() );
        } else if ( this.headerState == 1 || this.headerState == 2 ) {
            if ( !headersEndStream ) {
                throw new ConnectionException ( Stream.sm.getString ( "stream.trialerHeader.noEndOfStream", this.getConnectionId(), this.getIdentifier() ), Http2Error.PROTOCOL_ERROR );
            }
            this.headerState = 3;
            this.handler.getHpackDecoder().setMaxHeaderCount ( this.handler.getMaxTrailerCount() );
            this.handler.getHpackDecoder().setMaxHeaderSize ( this.handler.getMaxTrailerSize() );
        }
        this.state.receivedStartOfHeaders();
    }
    final void receivedEndOfStream() {
        synchronized ( this.inputBuffer ) {
            this.inputBuffer.notifyAll();
        }
        this.state.recievedEndOfStream();
    }
    final void sentEndOfStream() {
        this.outputBuffer.endOfStreamSent = true;
        this.state.sentEndOfStream();
    }
    final StreamInputBuffer getInputBuffer() {
        return this.inputBuffer;
    }
    final StreamOutputBuffer getOutputBuffer() {
        return this.outputBuffer;
    }
    final void sentPushPromise() {
        this.state.sentPushPromise();
    }
    final boolean isActive() {
        return this.state.isActive();
    }
    final boolean canWrite() {
        return this.state.canWrite();
    }
    final boolean isClosedFinal() {
        return this.state.isClosedFinal();
    }
    final void closeIfIdle() {
        this.state.closeIfIdle();
    }
    private final boolean isInputFinished() {
        return !this.state.isFrameTypePermitted ( FrameType.DATA );
    }
    final void close ( final Http2Exception http2Exception ) {
        if ( http2Exception instanceof StreamException ) {
            try {
                final StreamException se = ( StreamException ) http2Exception;
                if ( Stream.log.isDebugEnabled() ) {
                    Stream.log.debug ( Stream.sm.getString ( "stream.reset.send", this.getConnectionId(), this.getIdentifier(), Long.toString ( se.getError().getCode() ) ) );
                }
                this.state.sendReset();
                this.handler.sendStreamReset ( se );
            } catch ( IOException ioe ) {
                final ConnectionException ce = new ConnectionException ( Stream.sm.getString ( "stream.reset.fail" ), Http2Error.PROTOCOL_ERROR );
                ce.initCause ( ioe );
                this.handler.closeConnection ( ce );
            }
        } else {
            this.handler.closeConnection ( http2Exception );
        }
    }
    final boolean isPushSupported() {
        return this.handler.getRemoteSettings().getEnablePush();
    }
    final boolean push ( final Request request ) throws IOException {
        if ( !this.isPushSupported() ) {
            return false;
        }
        request.getMimeHeaders().addValue ( ":method" ).duplicate ( request.method() );
        request.getMimeHeaders().addValue ( ":scheme" ).duplicate ( request.scheme() );
        final StringBuilder path = new StringBuilder ( request.requestURI().toString() );
        if ( !request.queryString().isNull() ) {
            path.append ( '?' );
            path.append ( request.queryString().toString() );
        }
        request.getMimeHeaders().addValue ( ":path" ).setString ( path.toString() );
        if ( ( !request.scheme().equals ( "http" ) || request.getServerPort() != 80 ) && ( !request.scheme().equals ( "https" ) || request.getServerPort() != 443 ) ) {
            request.getMimeHeaders().addValue ( ":authority" ).setString ( request.serverName().getString() + ":" + request.getServerPort() );
        } else {
            request.getMimeHeaders().addValue ( ":authority" ).duplicate ( request.serverName() );
        }
        push ( this.handler, request, this );
        return true;
    }
    private static void push ( final Http2UpgradeHandler handler, final Request request, final Stream stream ) throws IOException {
        if ( Constants.IS_SECURITY_ENABLED ) {
            try {
                AccessController.doPrivileged ( ( PrivilegedExceptionAction<Object> ) new PrivilegedExceptionAction<Void>() {
                    @Override
                    public Void run() throws IOException {
                        handler.push ( request, stream );
                        return null;
                    }
                } );
                return;
            } catch ( PrivilegedActionException ex ) {
                final Exception e = ex.getException();
                if ( e instanceof IOException ) {
                    throw ( IOException ) e;
                }
                throw new IOException ( ex );
            }
        }
        handler.push ( request, stream );
    }
    static {
        log = LogFactory.getLog ( Stream.class );
        sm = StringManager.getManager ( Stream.class );
        ( ACK_RESPONSE = new Response() ).setStatus ( 100 );
    }
    class StreamOutputBuffer implements OutputBuffer {
        private final ByteBuffer buffer;
        private volatile long written;
        private volatile boolean closed;
        private volatile boolean endOfStreamSent;
        StreamOutputBuffer() {
            this.buffer = ByteBuffer.allocate ( 8192 );
            this.written = 0L;
            this.closed = false;
            this.endOfStreamSent = false;
        }
        @Override
        public final synchronized int doWrite ( final ByteBuffer chunk ) throws IOException {
            if ( this.closed ) {
                throw new IllegalStateException ( Stream.sm.getString ( "stream.closed", Stream.this.getConnectionId(), Stream.this.getIdentifier() ) );
            }
            if ( !Stream.this.coyoteResponse.isCommitted() ) {
                Stream.this.coyoteResponse.sendHeaders();
            }
            final int chunkLimit = chunk.limit();
            int offset = 0;
            while ( chunk.remaining() > 0 ) {
                final int thisTime = Math.min ( this.buffer.remaining(), chunk.remaining() );
                chunk.limit ( chunk.position() + thisTime );
                this.buffer.put ( chunk );
                chunk.limit ( chunkLimit );
                offset += thisTime;
                if ( chunk.remaining() > 0 && !this.buffer.hasRemaining() && this.flush ( true, Stream.this.coyoteResponse.getWriteListener() == null ) ) {
                    break;
                }
            }
            this.written += offset;
            return offset;
        }
        final synchronized boolean flush ( final boolean block ) throws IOException {
            return this.flush ( false, block );
        }
        private final synchronized boolean flush ( final boolean writeInProgress, final boolean block ) throws IOException {
            if ( Stream.log.isDebugEnabled() ) {
                Stream.log.debug ( Stream.sm.getString ( "stream.outputBuffer.flush.debug", Stream.this.getConnectionId(), Stream.this.getIdentifier(), Integer.toString ( this.buffer.position() ), Boolean.toString ( writeInProgress ), Boolean.toString ( this.closed ) ) );
            }
            if ( this.buffer.position() == 0 ) {
                if ( this.closed && !this.endOfStreamSent ) {
                    Stream.this.handler.writeBody ( Stream.this, this.buffer, 0, true );
                }
                return false;
            }
            this.buffer.flip();
            int left = this.buffer.remaining();
            while ( left > 0 ) {
                int streamReservation = Stream.this.reserveWindowSize ( left, block );
                if ( streamReservation == 0 ) {
                    this.buffer.compact();
                    return true;
                }
                while ( streamReservation > 0 ) {
                    final int connectionReservation = Stream.this.handler.reserveWindowSize ( Stream.this, streamReservation );
                    Stream.this.handler.writeBody ( Stream.this, this.buffer, connectionReservation, !writeInProgress && this.closed && left == connectionReservation );
                    streamReservation -= connectionReservation;
                    left -= connectionReservation;
                }
            }
            this.buffer.clear();
            return false;
        }
        final synchronized boolean isReady() {
            return Stream.this.getWindowSize() > 0L && Stream.this.handler.getWindowSize() > 0L;
        }
        @Override
        public final long getBytesWritten() {
            return this.written;
        }
        final void close() throws IOException {
            this.closed = true;
            Stream.this.flushData();
        }
        final boolean hasNoBody() {
            return this.written == 0L && this.closed;
        }
    }
    class StreamInputBuffer implements InputBuffer {
        private byte[] outBuffer;
        private volatile ByteBuffer inBuffer;
        private volatile boolean readInterest;
        private boolean reset;
        StreamInputBuffer() {
            this.reset = false;
        }
        @Override
        public final int doRead ( final ApplicationBufferHandler applicationBufferHandler ) throws IOException {
            this.ensureBuffersExist();
            int written = -1;
            synchronized ( this.inBuffer ) {
                while ( this.inBuffer.position() == 0 && !Stream.this.isInputFinished() ) {
                    try {
                        if ( Stream.log.isDebugEnabled() ) {
                            Stream.log.debug ( Stream.sm.getString ( "stream.inputBuffer.empty" ) );
                        }
                        this.inBuffer.wait();
                        if ( this.reset ) {
                            throw new IOException ( "HTTP/2 Stream reset" );
                        }
                        continue;
                    } catch ( InterruptedException e ) {
                        throw new IOException ( e );
                    }
                    break;
                }
                if ( this.inBuffer.position() > 0 ) {
                    this.inBuffer.flip();
                    written = this.inBuffer.remaining();
                    if ( Stream.log.isDebugEnabled() ) {
                        Stream.log.debug ( Stream.sm.getString ( "stream.inputBuffer.copy", Integer.toString ( written ) ) );
                    }
                    this.inBuffer.get ( this.outBuffer, 0, written );
                    this.inBuffer.clear();
                } else {
                    if ( Stream.this.isInputFinished() ) {
                        return -1;
                    }
                    throw new IllegalStateException();
                }
            }
            applicationBufferHandler.setByteBuffer ( ByteBuffer.wrap ( this.outBuffer, 0, written ) );
            Stream.this.handler.writeWindowUpdate ( Stream.this, written, true );
            return written;
        }
        final void registerReadInterest() {
            synchronized ( this.inBuffer ) {
                this.readInterest = true;
            }
        }
        final synchronized boolean isRequestBodyFullyRead() {
            return ( this.inBuffer == null || this.inBuffer.position() == 0 ) && Stream.this.isInputFinished();
        }
        final synchronized int available() {
            if ( this.inBuffer == null ) {
                return 0;
            }
            return this.inBuffer.position();
        }
        final synchronized boolean onDataAvailable() {
            if ( this.readInterest ) {
                if ( Stream.log.isDebugEnabled() ) {
                    Stream.log.debug ( Stream.sm.getString ( "stream.inputBuffer.dispatch" ) );
                }
                this.readInterest = false;
                Stream.this.coyoteRequest.action ( ActionCode.DISPATCH_READ, null );
                Stream.this.coyoteRequest.action ( ActionCode.DISPATCH_EXECUTE, null );
                return true;
            }
            if ( Stream.log.isDebugEnabled() ) {
                Stream.log.debug ( Stream.sm.getString ( "stream.inputBuffer.signal" ) );
            }
            synchronized ( this.inBuffer ) {
                this.inBuffer.notifyAll();
            }
            return false;
        }
        private final ByteBuffer getInBuffer() {
            this.ensureBuffersExist();
            return this.inBuffer;
        }
        final synchronized void insertReplayedBody ( final ByteChunk body ) {
            this.inBuffer = ByteBuffer.wrap ( body.getBytes(), body.getOffset(), body.getLength() );
        }
        private final void ensureBuffersExist() {
            if ( this.inBuffer == null ) {
                final int size = Stream.this.handler.getLocalSettings().getInitialWindowSize();
                synchronized ( this ) {
                    if ( this.inBuffer == null ) {
                        this.inBuffer = ByteBuffer.allocate ( size );
                        this.outBuffer = new byte[size];
                    }
                }
            }
        }
        private final void receiveReset() {
            if ( this.inBuffer != null ) {
                synchronized ( this.inBuffer ) {
                    this.reset = true;
                    this.inBuffer.notifyAll();
                }
            }
        }
    }
}
