package javax.websocket;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.io.OutputStream;
public interface Encoder {
    void init ( EndpointConfig p0 );
    void destroy();
    public interface BinaryStream<T> extends Encoder {
        void encode ( T p0, OutputStream p1 ) throws EncodeException, IOException;
    }
    public interface Binary<T> extends Encoder {
        ByteBuffer encode ( T p0 ) throws EncodeException;
    }
    public interface TextStream<T> extends Encoder {
        void encode ( T p0, Writer p1 ) throws EncodeException, IOException;
    }
    public interface Text<T> extends Encoder {
        String encode ( T p0 ) throws EncodeException;
    }
}
