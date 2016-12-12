package javax.websocket;
public interface Whole<T> extends MessageHandler {
    void onMessage ( T p0 );
}
