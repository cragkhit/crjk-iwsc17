package javax.websocket;
public interface Text<T> extends Encoder {
    String encode ( T p0 ) throws EncodeException;
}
