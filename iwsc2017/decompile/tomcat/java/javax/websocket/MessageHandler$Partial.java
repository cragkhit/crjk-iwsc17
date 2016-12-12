package javax.websocket;
public interface Partial<T> extends MessageHandler {
    void onMessage ( T p0, boolean p1 );
}
