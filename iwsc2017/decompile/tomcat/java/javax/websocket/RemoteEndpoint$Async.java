package javax.websocket;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;
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
