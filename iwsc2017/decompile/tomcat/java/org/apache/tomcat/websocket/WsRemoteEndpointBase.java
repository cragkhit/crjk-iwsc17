package org.apache.tomcat.websocket;
import java.nio.ByteBuffer;
import java.io.IOException;
import javax.websocket.RemoteEndpoint;
public abstract class WsRemoteEndpointBase implements RemoteEndpoint {
    protected final WsRemoteEndpointImplBase base;
    WsRemoteEndpointBase ( final WsRemoteEndpointImplBase base ) {
        this.base = base;
    }
    public final void setBatchingAllowed ( final boolean batchingAllowed ) throws IOException {
        this.base.setBatchingAllowed ( batchingAllowed );
    }
    public final boolean getBatchingAllowed() {
        return this.base.getBatchingAllowed();
    }
    public final void flushBatch() throws IOException {
        this.base.flushBatch();
    }
    public final void sendPing ( final ByteBuffer applicationData ) throws IOException, IllegalArgumentException {
        this.base.sendPing ( applicationData );
    }
    public final void sendPong ( final ByteBuffer applicationData ) throws IOException, IllegalArgumentException {
        this.base.sendPong ( applicationData );
    }
}
