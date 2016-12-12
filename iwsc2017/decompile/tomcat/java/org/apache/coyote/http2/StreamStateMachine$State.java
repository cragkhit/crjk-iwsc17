package org.apache.coyote.http2;
import java.util.HashSet;
import java.util.Set;
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
