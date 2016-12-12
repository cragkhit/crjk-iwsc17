package org.apache.tomcat.util.net;
import java.io.EOFException;
import java.nio.channels.CompletionHandler;
private class HandshakeWriteCompletionHandler implements CompletionHandler<Integer, SocketWrapperBase<Nio2Channel>> {
    @Override
    public void completed ( final Integer result, final SocketWrapperBase<Nio2Channel> attachment ) {
        if ( result < 0 ) {
            this.failed ( ( Throwable ) new EOFException(), attachment );
        } else {
            SecureNio2Channel.this.endpoint.processSocket ( attachment, SocketEvent.OPEN_WRITE, false );
        }
    }
    @Override
    public void failed ( final Throwable exc, final SocketWrapperBase<Nio2Channel> attachment ) {
        SecureNio2Channel.this.endpoint.processSocket ( attachment, SocketEvent.ERROR, false );
    }
}
