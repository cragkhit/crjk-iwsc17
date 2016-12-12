package org.apache.tomcat.util.net;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
class Nio2Endpoint$Nio2SocketWrapper$2 implements CompletionHandler<Integer, SocketWrapperBase<Nio2Channel>> {
    @Override
    public void completed ( final Integer nBytes, final SocketWrapperBase<Nio2Channel> attachment ) {
        if ( nBytes < 0 ) {
            this.failed ( ( Throwable ) new ClosedChannelException(), attachment );
            return;
        }
        Nio2SocketWrapper.this.getEndpoint().processSocket ( attachment, SocketEvent.OPEN_READ, Nio2Endpoint.isInline() );
    }
    @Override
    public void failed ( final Throwable exc, final SocketWrapperBase<Nio2Channel> attachment ) {
        Nio2SocketWrapper.this.getEndpoint().processSocket ( attachment, SocketEvent.DISCONNECT, true );
    }
}
