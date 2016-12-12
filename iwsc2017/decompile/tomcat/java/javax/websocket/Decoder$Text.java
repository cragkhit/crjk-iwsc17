package javax.websocket;
public interface Text<T> extends Decoder {
    T decode ( String p0 ) throws DecodeException;
    boolean willDecode ( String p0 );
}
