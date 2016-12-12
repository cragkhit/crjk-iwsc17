package org.apache.tomcat.websocket;
import javax.websocket.EncodeException;
import java.io.Writer;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.io.IOException;
import javax.websocket.RemoteEndpoint;
public class WsRemoteEndpointBasic extends WsRemoteEndpointBase implements RemoteEndpoint.Basic {
    WsRemoteEndpointBasic ( final WsRemoteEndpointImplBase base ) {
        super ( base );
    }
    public void sendText ( final String text ) throws IOException {
        this.base.sendString ( text );
    }
    public void sendBinary ( final ByteBuffer data ) throws IOException {
        this.base.sendBytes ( data );
    }
    public void sendText ( final String fragment, final boolean isLast ) throws IOException {
        this.base.sendPartialString ( fragment, isLast );
    }
    public void sendBinary ( final ByteBuffer partialByte, final boolean isLast ) throws IOException {
        this.base.sendPartialBytes ( partialByte, isLast );
    }
    public OutputStream getSendStream() throws IOException {
        return this.base.getSendStream();
    }
    public Writer getSendWriter() throws IOException {
        return this.base.getSendWriter();
    }
    public void sendObject ( final Object o ) throws IOException, EncodeException {
        this.base.sendObject ( o );
    }
}
