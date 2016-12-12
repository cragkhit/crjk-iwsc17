package javax.websocket;
import java.nio.ByteBuffer;
public interface Binary<T> extends Encoder {
    ByteBuffer encode ( T p0 ) throws EncodeException;
}
