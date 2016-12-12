package javax.websocket;
import java.nio.ByteBuffer;
public interface Binary<T> extends Decoder {
    T decode ( ByteBuffer p0 ) throws DecodeException;
    boolean willDecode ( ByteBuffer p0 );
}
