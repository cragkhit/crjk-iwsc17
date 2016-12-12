package org.apache.coyote.http2;
import java.io.IOException;
import java.nio.ByteBuffer;
interface Output {
    HpackDecoder getHpackDecoder();
    ByteBuffer startRequestBodyFrame ( int p0, int p1 ) throws Http2Exception;
    void endRequestBodyFrame ( int p0 ) throws Http2Exception;
    void receivedEndOfStream ( int p0 ) throws ConnectionException;
    void swallowedPadding ( int p0, int p1 ) throws ConnectionException, IOException;
    HpackDecoder.HeaderEmitter headersStart ( int p0, boolean p1 ) throws Http2Exception;
    void headersEnd ( int p0 ) throws ConnectionException;
    void reprioritise ( int p0, int p1, boolean p2, int p3 ) throws Http2Exception;
    void reset ( int p0, long p1 ) throws Http2Exception;
    void setting ( Setting p0, long p1 ) throws ConnectionException;
    void settingsEnd ( boolean p0 ) throws IOException;
    void pingReceive ( byte[] p0, boolean p1 ) throws IOException;
    void goaway ( int p0, long p1, String p2 );
    void incrementWindowSize ( int p0, int p1 ) throws Http2Exception;
    void swallowed ( int p0, FrameType p1, int p2, int p3 ) throws IOException;
}
