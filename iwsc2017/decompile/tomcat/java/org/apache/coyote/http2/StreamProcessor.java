package org.apache.coyote.http2;
import org.apache.juli.logging.LogFactory;
import org.apache.coyote.ActionCode;
import org.apache.coyote.PushToken;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.coyote.ErrorState;
import java.io.IOException;
import org.apache.tomcat.util.net.SocketEvent;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.coyote.ContainerThreadMarker;
import org.apache.tomcat.util.net.SocketWrapperBase;
import org.apache.coyote.Adapter;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import org.apache.coyote.AbstractProcessor;
class StreamProcessor extends AbstractProcessor implements Runnable {
    private static final Log log;
    private static final StringManager sm;
    private final Http2UpgradeHandler handler;
    private final Stream stream;
    StreamProcessor ( final Http2UpgradeHandler handler, final Stream stream, final Adapter adapter, final SocketWrapperBase<?> socketWrapper ) {
        super ( stream.getCoyoteRequest(), stream.getCoyoteResponse() );
        this.handler = handler;
        this.stream = stream;
        this.setAdapter ( adapter );
        this.setSocketWrapper ( socketWrapper );
    }
    @Override
    public final void run() {
        try {
            synchronized ( this ) {
                ContainerThreadMarker.set();
                AbstractEndpoint.Handler.SocketState state = AbstractEndpoint.Handler.SocketState.CLOSED;
                try {
                    state = this.process ( this.socketWrapper, SocketEvent.OPEN_READ );
                    if ( state == AbstractEndpoint.Handler.SocketState.CLOSED ) {
                        if ( !this.getErrorState().isConnectionIoAllowed() ) {
                            final ConnectionException ce = new ConnectionException ( StreamProcessor.sm.getString ( "streamProcessor.error.connection", this.stream.getConnectionId(), this.stream.getIdentifier() ), Http2Error.INTERNAL_ERROR );
                            this.stream.close ( ce );
                        } else if ( !this.getErrorState().isIoAllowed() ) {
                            final StreamException se = new StreamException ( StreamProcessor.sm.getString ( "streamProcessor.error.stream", this.stream.getConnectionId(), this.stream.getIdentifier() ), Http2Error.INTERNAL_ERROR, this.stream.getIdentifier() );
                            this.stream.close ( se );
                        }
                    }
                } catch ( Exception e ) {
                    final ConnectionException ce2 = new ConnectionException ( StreamProcessor.sm.getString ( "streamProcessor.error.connection", this.stream.getConnectionId(), this.stream.getIdentifier() ), Http2Error.INTERNAL_ERROR );
                    ce2.initCause ( e );
                    this.stream.close ( ce2 );
                } finally {
                    ContainerThreadMarker.clear();
                }
            }
        } finally {
            this.handler.executeQueuedStream();
        }
    }
    @Override
    protected final void prepareResponse() throws IOException {
        this.response.setCommitted ( true );
        this.stream.writeHeaders();
    }
    @Override
    protected final void finishResponse() throws IOException {
        this.stream.getOutputBuffer().close();
    }
    @Override
    protected final void ack() {
        if ( !this.response.isCommitted() && this.request.hasExpectation() ) {
            try {
                this.stream.writeAck();
            } catch ( IOException ioe ) {
                this.setErrorState ( ErrorState.CLOSE_CONNECTION_NOW, ioe );
            }
        }
    }
    @Override
    protected final void flush() throws IOException {
        this.stream.flushData();
    }
    @Override
    protected final int available ( final boolean doRead ) {
        return this.stream.getInputBuffer().available();
    }
    @Override
    protected final void setRequestBody ( final ByteChunk body ) {
        this.stream.getInputBuffer().insertReplayedBody ( body );
        this.stream.receivedEndOfStream();
    }
    @Override
    protected final void setSwallowResponse() {
    }
    @Override
    protected final void disableSwallowRequest() {
    }
    @Override
    protected final boolean isRequestBodyFullyRead() {
        return this.stream.getInputBuffer().isRequestBodyFullyRead();
    }
    @Override
    protected final void registerReadInterest() {
        this.stream.getInputBuffer().registerReadInterest();
    }
    @Override
    protected final boolean isReady() {
        return this.stream.getOutputBuffer().isReady();
    }
    @Override
    protected final void executeDispatches ( final SocketWrapperBase<?> wrapper ) {
        wrapper.getEndpoint().getExecutor().execute ( this );
    }
    @Override
    protected final boolean isPushSupported() {
        return this.stream.isPushSupported();
    }
    @Override
    protected final void doPush ( final PushToken pushToken ) {
        try {
            pushToken.setResult ( this.stream.push ( pushToken.getPushTarget() ) );
        } catch ( IOException ioe ) {
            this.setErrorState ( ErrorState.CLOSE_CONNECTION_NOW, ioe );
            this.response.setErrorException ( ioe );
        }
    }
    @Override
    public final void recycle() {
        this.setSocketWrapper ( null );
        this.setAdapter ( null );
    }
    @Override
    protected final Log getLog() {
        return StreamProcessor.log;
    }
    @Override
    public final void pause() {
    }
    public final AbstractEndpoint.Handler.SocketState service ( final SocketWrapperBase<?> socket ) throws IOException {
        try {
            this.adapter.service ( this.request, this.response );
        } catch ( Exception e ) {
            if ( StreamProcessor.log.isDebugEnabled() ) {
                StreamProcessor.log.debug ( StreamProcessor.sm.getString ( "streamProcessor.service.error" ), e );
            }
            this.response.setStatus ( 500 );
            this.setErrorState ( ErrorState.CLOSE_NOW, e );
        }
        if ( this.getErrorState().isError() ) {
            this.action ( ActionCode.CLOSE, null );
            this.request.updateCounters();
            return AbstractEndpoint.Handler.SocketState.CLOSED;
        }
        if ( this.isAsync() ) {
            return AbstractEndpoint.Handler.SocketState.LONG;
        }
        this.action ( ActionCode.CLOSE, null );
        this.request.updateCounters();
        return AbstractEndpoint.Handler.SocketState.CLOSED;
    }
    @Override
    protected final boolean flushBufferedWrite() throws IOException {
        if ( !this.stream.getOutputBuffer().flush ( false ) ) {
            return false;
        }
        if ( this.stream.getOutputBuffer().isReady() ) {
            throw new IllegalStateException();
        }
        return true;
    }
    @Override
    protected final AbstractEndpoint.Handler.SocketState dispatchEndRequest() {
        return AbstractEndpoint.Handler.SocketState.CLOSED;
    }
    static {
        log = LogFactory.getLog ( StreamProcessor.class );
        sm = StringManager.getManager ( StreamProcessor.class );
    }
}
