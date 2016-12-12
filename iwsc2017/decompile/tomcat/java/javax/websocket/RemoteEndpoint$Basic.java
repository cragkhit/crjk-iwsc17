package javax.websocket;
import java.io.Writer;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.io.IOException;
public interface Basic extends RemoteEndpoint {
    void sendText ( String p0 ) throws IOException;
    void sendBinary ( ByteBuffer p0 ) throws IOException;
    void sendText ( String p0, boolean p1 ) throws IOException;
    void sendBinary ( ByteBuffer p0, boolean p1 ) throws IOException;
    OutputStream getSendStream() throws IOException;
    Writer getSendWriter() throws IOException;
    void sendObject ( Object p0 ) throws IOException, EncodeException;
}
