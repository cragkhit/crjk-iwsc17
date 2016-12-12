package javax.websocket;
import java.util.concurrent.Future;
import java.io.Writer;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.io.IOException;
public interface RemoteEndpoint {
    void setBatchingAllowed ( boolean p0 ) throws IOException;
    boolean getBatchingAllowed();
    void flushBatch() throws IOException;
    void sendPing ( ByteBuffer p0 ) throws IOException, IllegalArgumentException;
    void sendPong ( ByteBuffer p0 ) throws IOException, IllegalArgumentException;
    public interface Basic extends RemoteEndpoint {
        void sendText ( String p0 ) throws IOException;
        void sendBinary ( ByteBuffer p0 ) throws IOException;
        void sendText ( String p0, boolean p1 ) throws IOException;
        void sendBinary ( ByteBuffer p0, boolean p1 ) throws IOException;
        OutputStream getSendStream() throws IOException;
        Writer getSendWriter() throws IOException;
        void sendObject ( Object p0 ) throws IOException, EncodeException;
    }
    public interface Async extends RemoteEndpoint {
        long getSendTimeout();
        void setSendTimeout ( long p0 );
        void sendText ( String p0, SendHandler p1 );
        Future<Void> sendText ( String p0 );
        Future<Void> sendBinary ( ByteBuffer p0 );
        void sendBinary ( ByteBuffer p0, SendHandler p1 );
        Future<Void> sendObject ( Object p0 );
        void sendObject ( Object p0, SendHandler p1 );
    }
}
