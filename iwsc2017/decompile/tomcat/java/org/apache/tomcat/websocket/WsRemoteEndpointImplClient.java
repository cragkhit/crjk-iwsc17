package org.apache.tomcat.websocket;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.websocket.SendResult;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.websocket.SendHandler;
public class WsRemoteEndpointImplClient extends WsRemoteEndpointImplBase {
    private final AsyncChannelWrapper channel;
    public WsRemoteEndpointImplClient ( final AsyncChannelWrapper channel ) {
        this.channel = channel;
    }
    @Override
    protected boolean isMasked() {
        return true;
    }
    @Override
    protected void doWrite ( final SendHandler handler, final long blockingWriteTimeoutExpiry, final ByteBuffer... data ) {
        for ( final ByteBuffer byteBuffer : data ) {
            long timeout;
            if ( blockingWriteTimeoutExpiry == -1L ) {
                timeout = this.getSendTimeout();
                if ( timeout < 1L ) {
                    timeout = Long.MAX_VALUE;
                }
            } else {
                timeout = blockingWriteTimeoutExpiry - System.currentTimeMillis();
                if ( timeout < 0L ) {
                    final SendResult sr = new SendResult ( ( Throwable ) new IOException ( "Blocking write timeout" ) );
                    handler.onResult ( sr );
                }
            }
            try {
                this.channel.write ( byteBuffer ).get ( timeout, TimeUnit.MILLISECONDS );
            } catch ( InterruptedException | ExecutionException | TimeoutException e ) {
                handler.onResult ( new SendResult ( ( Throwable ) e ) );
                return;
            }
        }
        handler.onResult ( WsRemoteEndpointImplClient.SENDRESULT_OK );
    }
    @Override
    protected void doClose() {
        this.channel.close();
    }
}
