package org.apache.coyote.http2;
import java.util.function.IntPredicate;
import org.apache.tomcat.util.res.StringManager;
enum FrameType {
    DATA ( 0, false, true, ( IntPredicate ) null, false ),
    HEADERS ( 1, false, true, ( IntPredicate ) null, true ),
    PRIORITY ( 2, false, true, x -> x == 5, false ),
    RST ( 3, false, true, x -> x == 4, false ),
    SETTINGS ( 4, true, false, x -> x % 6 == 0, true ),
    PUSH_PROMISE ( 5, false, true, x -> x >= 4, true ),
    PING ( 6, true, false, x -> x == 8, false ),
    GOAWAY ( 7, true, false, x -> x >= 8, false ),
    WINDOW_UPDATE ( 8, true, true, x -> x == 4, true ),
    CONTINUATION ( 9, false, true, ( IntPredicate ) null, true ),
    UNKNOWN ( 256, true, true, ( IntPredicate ) null, false );
    private static final StringManager sm;
    private final int id;
    private final boolean streamZero;
    private final boolean streamNonZero;
    private final IntPredicate payloadSizeValidator;
    private final boolean payloadErrorFatal;
    private FrameType ( final int id, final boolean streamZero, final boolean streamNonZero, final IntPredicate payloadSizeValidator, final boolean payloadErrorFatal ) {
        this.id = id;
        this.streamZero = streamZero;
        this.streamNonZero = streamNonZero;
        this.payloadSizeValidator = payloadSizeValidator;
        this.payloadErrorFatal = payloadErrorFatal;
    }
    byte getIdByte() {
        return ( byte ) this.id;
    }
    void check ( final int streamId, final int payloadSize ) throws Http2Exception {
        if ( ( streamId == 0 && !this.streamZero ) || ( streamId != 0 && !this.streamNonZero ) ) {
            throw new ConnectionException ( FrameType.sm.getString ( "frameType.checkStream", this ), Http2Error.PROTOCOL_ERROR );
        }
        if ( this.payloadSizeValidator == null || this.payloadSizeValidator.test ( payloadSize ) ) {
            return;
        }
        if ( this.payloadErrorFatal || streamId == 0 ) {
            throw new ConnectionException ( FrameType.sm.getString ( "frameType.checkPayloadSize", Integer.toString ( payloadSize ), this ), Http2Error.FRAME_SIZE_ERROR );
        }
        throw new StreamException ( FrameType.sm.getString ( "frameType.checkPayloadSize", Integer.toString ( payloadSize ), this ), Http2Error.FRAME_SIZE_ERROR, streamId );
    }
    static FrameType valueOf ( final int i ) {
        switch ( i ) {
        case 0: {
            return FrameType.DATA;
        }
        case 1: {
            return FrameType.HEADERS;
        }
        case 2: {
            return FrameType.PRIORITY;
        }
        case 3: {
            return FrameType.RST;
        }
        case 4: {
            return FrameType.SETTINGS;
        }
        case 5: {
            return FrameType.PUSH_PROMISE;
        }
        case 6: {
            return FrameType.PING;
        }
        case 7: {
            return FrameType.GOAWAY;
        }
        case 8: {
            return FrameType.WINDOW_UPDATE;
        }
        case 9: {
            return FrameType.CONTINUATION;
        }
        default: {
            return FrameType.UNKNOWN;
        }
        }
    }
    static {
        sm = StringManager.getManager ( FrameType.class );
    }
}
