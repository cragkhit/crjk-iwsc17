package javax.websocket;
import java.io.IOException;
import java.io.Writer;
public interface TextStream<T> extends Encoder {
    void encode ( T p0, Writer p1 ) throws EncodeException, IOException;
}
