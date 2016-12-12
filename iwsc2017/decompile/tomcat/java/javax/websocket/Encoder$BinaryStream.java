package javax.websocket;
import java.io.IOException;
import java.io.OutputStream;
public interface BinaryStream<T> extends Encoder {
    void encode ( T p0, OutputStream p1 ) throws EncodeException, IOException;
}
