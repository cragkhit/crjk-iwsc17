package org.apache.coyote.http2;
import java.util.function.IntPredicate;
import org.apache.tomcat.util.res.StringManager;
enum FrameType {
    DATA ( 0,   false,  true, null,              false ),
    HEADERS ( 1,   false,  true, null,               true ),
    PRIORITY ( 2,   false,  true, ( x ) -> x == 5,     false ),
    RST ( 3,   false,  true, ( x ) -> x == 4,     false ),
    SETTINGS ( 4,    true, false, ( x ) -> x % 6 == 0,  true ),
    PUSH_PROMISE ( 5,   false,  true, ( x ) -> x >= 4,      true ),
    PING ( 6,    true, false, ( x ) -> x == 8,     false ),
    GOAWAY ( 7,    true, false, ( x ) -> x >= 8,     false ),
    WINDOW_UPDATE ( 8,    true,  true, ( x ) -> x == 4,      true ),
    CONTINUATION ( 9,   false,  true, null,               true ),
    UNKNOWN ( 256,  true,  true, null,              false );
    private static final StringManager sm = StringManager.getManager ( FrameType.class );
    private final int id;
    private final boolean streamZero;
    private final boolean streamNonZero;
    private final IntPredicate payloadSizeValidator;
    private final boolean payloadErrorFatal;
    private FrameType ( int id, boolean streamZero, boolean streamNonZero,
    IntPredicate payloadSizeValidator,  boolean payloadErrorFatal ) {
        this.id = id;
        this.streamZero = streamZero;
        this.streamNonZero = streamNonZero;
        this.payloadSizeValidator =  payloadSizeValidator;
        this.payloadErrorFatal = payloadErrorFatal;
    }
    byte getIdByte() {
        return ( byte ) id;
    }
    void check ( int streamId, int payloadSize ) throws Http2Exception {
        if ( streamId == 0 && !streamZero || streamId != 0 && !streamNonZero ) {
            throw new ConnectionException ( sm.getString ( "frameType.checkStream", this ),
            Http2Error.PROTOCOL_ERROR );
        }
        if ( payloadSizeValidator != null && !payloadSizeValidator.test ( payloadSize ) ) {
            if ( payloadErrorFatal || streamId == 0 ) {
                throw new ConnectionException ( sm.getString ( "frameType.checkPayloadSize",
                Integer.toString ( payloadSize ), this ),
                Http2Error.FRAME_SIZE_ERROR );
            } else {
                throw new StreamException ( sm.getString ( "frameType.checkPayloadSize",
                                            Integer.toString ( payloadSize ), this ),
                                            Http2Error.FRAME_SIZE_ERROR, streamId );
            }
        }
    }
    static FrameType valueOf ( int i ) {
        switch ( i ) {
        case 0:
            return DATA;
        case 1:
            return HEADERS;
        case 2:
            return PRIORITY;
        case 3:
            return RST;
        case 4:
            return SETTINGS;
        case 5:
            return PUSH_PROMISE;
        case 6:
            return PING;
        case 7:
            return GOAWAY;
        case 8:
            return WINDOW_UPDATE;
        case 9:
            return CONTINUATION;
        default:
            return UNKNOWN;
        }
    }
}
