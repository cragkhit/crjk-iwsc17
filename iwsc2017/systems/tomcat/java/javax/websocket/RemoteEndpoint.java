package javax.websocket;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;
public interface RemoteEndpoint {
    interface Async extends RemoteEndpoint {
        long getSendTimeout();
        void setSendTimeout ( long timeout );
        void sendText ( String text, SendHandler completion );
        Future<Void> sendText ( String text );
        Future<Void> sendBinary ( ByteBuffer data );
        void sendBinary ( ByteBuffer data, SendHandler completion );
        Future<Void> sendObject ( Object obj );
        void sendObject ( Object obj, SendHandler completion );
    }
    interface Basic extends RemoteEndpoint {
        void sendText ( String text ) throws IOException;
        void sendBinary ( ByteBuffer data ) throws IOException;
        void sendText ( String fragment, boolean isLast ) throws IOException;
        void sendBinary ( ByteBuffer partialByte, boolean isLast ) throws IOException;
        OutputStream getSendStream() throws IOException;
        Writer getSendWriter() throws IOException;
        void sendObject ( Object data ) throws IOException, EncodeException;
    }
    void setBatchingAllowed ( boolean batchingAllowed ) throws IOException;
    boolean getBatchingAllowed();
    void flushBatch() throws IOException;
    void sendPing ( ByteBuffer applicationData )
    throws IOException, IllegalArgumentException;
    void sendPong ( ByteBuffer applicationData )
    throws IOException, IllegalArgumentException;
}
