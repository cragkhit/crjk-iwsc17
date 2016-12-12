package org.apache.tomcat.util.net;
import java.nio.channels.AsynchronousCloseException;
import java.io.IOException;
import java.io.EOFException;
import java.nio.channels.CompletionHandler;
class Nio2Endpoint$Nio2SocketWrapper$4 implements CompletionHandler<Integer, SocketWrapperBase<Nio2Channel>> {
    @Override
    public void completed ( final Integer nBytes, final SocketWrapperBase<Nio2Channel> attachment ) {
        boolean notify = false;
        if ( Nio2Endpoint.access$300().isDebugEnabled() ) {
            Nio2Endpoint.access$300().debug ( "Socket: [" + attachment + "], Interest: [" + Nio2SocketWrapper.access$700 ( Nio2SocketWrapper.this ) + "]" );
        }
        synchronized ( Nio2SocketWrapper.access$800 ( Nio2SocketWrapper.this ) ) {
            if ( nBytes < 0 ) {
                this.failed ( ( Throwable ) new EOFException(), attachment );
            } else if ( Nio2SocketWrapper.access$700 ( Nio2SocketWrapper.this ) && !Nio2Endpoint.isInline() ) {
                Nio2SocketWrapper.access$702 ( Nio2SocketWrapper.this, false );
                notify = true;
            } else {
                Nio2SocketWrapper.access$900 ( Nio2SocketWrapper.this ).release();
            }
        }
        if ( notify ) {
            Nio2SocketWrapper.this.getEndpoint().processSocket ( attachment, SocketEvent.OPEN_READ, false );
        }
    }
    @Override
    public void failed ( final Throwable exc, final SocketWrapperBase<Nio2Channel> attachment ) {
        IOException ioe;
        if ( exc instanceof IOException ) {
            ioe = ( IOException ) exc;
        } else {
            ioe = new IOException ( exc );
        }
        Nio2SocketWrapper.this.setError ( ioe );
        if ( exc instanceof AsynchronousCloseException ) {
            Nio2SocketWrapper.access$900 ( Nio2SocketWrapper.this ).release();
            return;
        }
        Nio2SocketWrapper.this.getEndpoint().processSocket ( attachment, SocketEvent.ERROR, true );
    }
}
