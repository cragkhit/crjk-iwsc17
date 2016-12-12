package javax.websocket;
import java.io.IOException;
import java.io.InputStream;
public interface BinaryStream<T> extends Decoder {
    T decode ( InputStream p0 ) throws DecodeException, IOException;
}
