package javax.websocket;
import java.nio.ByteBuffer;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
public interface Decoder {
    void init ( EndpointConfig p0 );
    void destroy();
    public interface TextStream<T> extends Decoder {
        T decode ( Reader p0 ) throws DecodeException, IOException;
    }
    public interface Text<T> extends Decoder {
        T decode ( String p0 ) throws DecodeException;
        boolean willDecode ( String p0 );
    }
    public interface BinaryStream<T> extends Decoder {
        T decode ( InputStream p0 ) throws DecodeException, IOException;
    }
    public interface Binary<T> extends Decoder {
        T decode ( ByteBuffer p0 ) throws DecodeException;
        boolean willDecode ( ByteBuffer p0 );
    }
}
