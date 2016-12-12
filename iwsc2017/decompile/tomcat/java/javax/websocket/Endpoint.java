package javax.websocket;
public abstract class Endpoint {
    public abstract void onOpen ( final Session p0, final EndpointConfig p1 );
    public void onClose ( final Session session, final CloseReason closeReason ) {
    }
    public void onError ( final Session session, final Throwable throwable ) {
    }
}
