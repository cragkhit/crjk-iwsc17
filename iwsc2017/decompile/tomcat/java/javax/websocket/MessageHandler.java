package javax.websocket;
public interface MessageHandler {
    public interface Whole<T> extends MessageHandler {
        void onMessage ( T p0 );
    }
    public interface Partial<T> extends MessageHandler {
        void onMessage ( T p0, boolean p1 );
    }
}
