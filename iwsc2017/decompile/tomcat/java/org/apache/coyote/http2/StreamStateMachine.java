package org.apache.coyote.http2;
import java.util.HashSet;
import java.util.Set;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
class StreamStateMachine {
    private static final Log log;
    private static final StringManager sm;
    private final Stream stream;
    private State state;
    StreamStateMachine ( final Stream stream ) {
        this.stream = stream;
        this.stateChange ( null, State.IDLE );
    }
    final synchronized void sentPushPromise() {
        this.stateChange ( State.IDLE, State.RESERVED_LOCAL );
    }
    final synchronized void receivedStartOfHeaders() {
        this.stateChange ( State.IDLE, State.OPEN );
        this.stateChange ( State.RESERVED_REMOTE, State.HALF_CLOSED_LOCAL );
    }
    final synchronized void sentEndOfStream() {
        this.stateChange ( State.OPEN, State.HALF_CLOSED_LOCAL );
        this.stateChange ( State.HALF_CLOSED_REMOTE, State.CLOSED_TX );
    }
    final synchronized void recievedEndOfStream() {
        this.stateChange ( State.OPEN, State.HALF_CLOSED_REMOTE );
        this.stateChange ( State.HALF_CLOSED_LOCAL, State.CLOSED_RX );
    }
    public synchronized void sendReset() {
        if ( this.state == State.IDLE ) {
            throw new IllegalStateException ( StreamStateMachine.sm.getString ( "streamStateMachine.debug.change", this.stream.getConnectionId(), this.stream.getIdentifier(), this.state ) );
        }
        if ( this.state.canReset() ) {
            this.stateChange ( this.state, State.CLOSED_RST_TX );
        }
    }
    final synchronized void receivedReset() {
        this.stateChange ( this.state, State.CLOSED_RST_RX );
    }
    private void stateChange ( final State oldState, final State newState ) {
        if ( this.state == oldState ) {
            this.state = newState;
            if ( StreamStateMachine.log.isDebugEnabled() ) {
                StreamStateMachine.log.debug ( StreamStateMachine.sm.getString ( "streamStateMachine.debug.change", this.stream.getConnectionId(), this.stream.getIdentifier(), oldState, newState ) );
            }
        }
    }
    final synchronized void checkFrameType ( final FrameType frameType ) throws Http2Exception {
        if ( this.isFrameTypePermitted ( frameType ) ) {
            return;
        }
        if ( this.state.connectionErrorForInvalidFrame ) {
            throw new ConnectionException ( StreamStateMachine.sm.getString ( "streamStateMachine.invalidFrame", this.stream.getConnectionId(), this.stream.getIdentifier(), this.state, frameType ), this.state.errorCodeForInvalidFrame );
        }
        throw new StreamException ( StreamStateMachine.sm.getString ( "streamStateMachine.invalidFrame", this.stream.getConnectionId(), this.stream.getIdentifier(), this.state, frameType ), this.state.errorCodeForInvalidFrame, this.stream.getIdentifier() );
    }
    final synchronized boolean isFrameTypePermitted ( final FrameType frameType ) {
        return this.state.isFrameTypePermitted ( frameType );
    }
    final synchronized boolean isActive() {
        return this.state.isActive();
    }
    final synchronized boolean canWrite() {
        return this.state.canWrite();
    }
    final synchronized boolean isClosedFinal() {
        return this.state == State.CLOSED_FINAL;
    }
    final synchronized void closeIfIdle() {
        this.stateChange ( State.IDLE, State.CLOSED_FINAL );
    }
    static {
        log = LogFactory.getLog ( StreamStateMachine.class );
        sm = StringManager.getManager ( StreamStateMachine.class );
    }
    private enum State {
        IDLE ( false, false, false, true, Http2Error.PROTOCOL_ERROR, new FrameType[] { FrameType.HEADERS, FrameType.PRIORITY } ),
        OPEN ( true, true, true, true, Http2Error.PROTOCOL_ERROR, new FrameType[] { FrameType.DATA, FrameType.HEADERS, FrameType.PRIORITY, FrameType.RST, FrameType.PUSH_PROMISE, FrameType.WINDOW_UPDATE } ),
        RESERVED_LOCAL ( false, false, true, true, Http2Error.PROTOCOL_ERROR, new FrameType[] { FrameType.PRIORITY, FrameType.RST, FrameType.WINDOW_UPDATE } ),
        RESERVED_REMOTE ( false, false, true, true, Http2Error.PROTOCOL_ERROR, new FrameType[] { FrameType.HEADERS, FrameType.PRIORITY, FrameType.RST } ),
        HALF_CLOSED_LOCAL ( true, false, true, true, Http2Error.PROTOCOL_ERROR, new FrameType[] { FrameType.DATA, FrameType.HEADERS, FrameType.PRIORITY, FrameType.RST, FrameType.PUSH_PROMISE, FrameType.WINDOW_UPDATE } ),
        HALF_CLOSED_REMOTE ( false, true, true, true, Http2Error.STREAM_CLOSED, new FrameType[] { FrameType.PRIORITY, FrameType.RST, FrameType.WINDOW_UPDATE } ),
        CLOSED_RX ( false, false, false, true, Http2Error.STREAM_CLOSED, new FrameType[] { FrameType.PRIORITY } ),
        CLOSED_TX ( false, false, false, true, Http2Error.STREAM_CLOSED, new FrameType[] { FrameType.PRIORITY, FrameType.RST, FrameType.WINDOW_UPDATE } ),
        CLOSED_RST_RX ( false, false, false, false, Http2Error.STREAM_CLOSED, new FrameType[] { FrameType.PRIORITY } ),
        CLOSED_RST_TX ( false, false, false, false, Http2Error.STREAM_CLOSED, new FrameType[] { FrameType.DATA, FrameType.HEADERS, FrameType.PRIORITY, FrameType.RST, FrameType.PUSH_PROMISE, FrameType.WINDOW_UPDATE } ),
        CLOSED_FINAL ( false, false, false, true, Http2Error.PROTOCOL_ERROR, new FrameType[] { FrameType.PRIORITY } );
        private final boolean canRead;
        private final boolean canWrite;
        private final boolean canReset;
        private final boolean connectionErrorForInvalidFrame;
        private final Http2Error errorCodeForInvalidFrame;
        private final Set<FrameType> frameTypesPermitted;
        private State ( final boolean canRead, final boolean canWrite, final boolean canReset, final boolean connectionErrorForInvalidFrame, final Http2Error errorCode, final FrameType[] frameTypes ) {
            this.frameTypesPermitted = new HashSet<FrameType>();
            this.canRead = canRead;
            this.canWrite = canWrite;
            this.canReset = canReset;
            this.connectionErrorForInvalidFrame = connectionErrorForInvalidFrame;
            this.errorCodeForInvalidFrame = errorCode;
            for ( final FrameType frameType : frameTypes ) {
                this.frameTypesPermitted.add ( frameType );
            }
        }
        public boolean isActive() {
            return this.canWrite || this.canRead;
        }
        public boolean canWrite() {
            return this.canWrite;
        }
        public boolean canReset() {
            return this.canReset;
        }
        public boolean isFrameTypePermitted ( final FrameType frameType ) {
            return this.frameTypesPermitted.contains ( frameType );
        }
    }
}
