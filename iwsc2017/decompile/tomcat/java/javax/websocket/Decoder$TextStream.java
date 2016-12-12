package javax.websocket;
import java.io.IOException;
import java.io.Reader;
public interface TextStream<T> extends Decoder {
    T decode ( Reader p0 ) throws DecodeException, IOException;
}
