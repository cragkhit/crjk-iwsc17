package org.apache.tomcat.websocket;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;
import javax.websocket.SendHandler;
import javax.websocket.RemoteEndpoint;
public class WsRemoteEndpointAsync extends WsRemoteEndpointBase implements RemoteEndpoint.Async {
    WsRemoteEndpointAsync ( final WsRemoteEndpointImplBase base ) {
        super ( base );
    }
    public long getSendTimeout() {
        return this.base.getSendTimeout();
    }
    public void setSendTimeout ( final long timeout ) {
        this.base.setSendTimeout ( timeout );
    }
    public void sendText ( final String text, final SendHandler completion ) {
        this.base.sendStringByCompletion ( text, completion );
    }
    public Future<Void> sendText ( final String text ) {
        return this.base.sendStringByFuture ( text );
    }
    public Future<Void> sendBinary ( final ByteBuffer data ) {
        return this.base.sendBytesByFuture ( data );
    }
    public void sendBinary ( final ByteBuffer data, final SendHandler completion ) {
        this.base.sendBytesByCompletion ( data, completion );
    }
    public Future<Void> sendObject ( final Object obj ) {
        return this.base.sendObjectByFuture ( obj );
    }
    public void sendObject ( final Object obj, final SendHandler completion ) {
        this.base.sendObjectByCompletion ( obj, completion );
    }
}
