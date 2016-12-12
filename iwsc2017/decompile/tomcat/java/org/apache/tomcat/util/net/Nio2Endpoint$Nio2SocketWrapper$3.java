package org.apache.tomcat.util.net;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.io.EOFException;
import java.nio.channels.CompletionHandler;
class Nio2Endpoint$Nio2SocketWrapper$3 implements CompletionHandler<Integer, SendfileData> {
    @Override
    public void completed ( final Integer nWrite, final SendfileData attachment ) {
        if ( nWrite < 0 ) {
            this.failed ( ( Throwable ) new EOFException(), attachment );
            return;
        }
        attachment.pos += nWrite;
        final ByteBuffer buffer = Nio2SocketWrapper.this.getSocket().getBufHandler().getWriteBuffer();
        if ( !buffer.hasRemaining() ) {
            if ( attachment.length <= 0L ) {
                Nio2SocketWrapper.this.setSendfileData ( null );
                try {
                    attachment.fchannel.close();
                } catch ( IOException ex ) {}
                if ( attachment.keepAlive ) {
                    if ( !Nio2Endpoint.isInline() ) {
                        Nio2SocketWrapper.this.awaitBytes();
                    } else {
                        attachment.doneInline = true;
                    }
                } else if ( !Nio2Endpoint.isInline() ) {
                    Nio2SocketWrapper.this.getEndpoint().processSocket ( Nio2SocketWrapper.this, SocketEvent.DISCONNECT, false );
                } else {
                    attachment.doneInline = true;
                }
                return;
            }
            Nio2SocketWrapper.this.getSocket().getBufHandler().configureWriteBufferForWrite();
            int nRead = -1;
            try {
                nRead = attachment.fchannel.read ( buffer );
            } catch ( IOException e ) {
                this.failed ( ( Throwable ) e, attachment );
                return;
            }
            if ( nRead <= 0 ) {
                this.failed ( ( Throwable ) new EOFException(), attachment );
                return;
            }
            Nio2SocketWrapper.this.getSocket().getBufHandler().configureWriteBufferForRead();
            if ( attachment.length < buffer.remaining() ) {
                buffer.limit ( buffer.limit() - buffer.remaining() + ( int ) attachment.length );
            }
            attachment.length -= nRead;
        }
        Nio2SocketWrapper.this.getSocket().write ( buffer, Nio2SocketWrapper.access$500 ( Nio2SocketWrapper.this ), TimeUnit.MILLISECONDS, attachment, this );
    }
    @Override
    public void failed ( final Throwable exc, final SendfileData attachment ) {
        try {
            attachment.fchannel.close();
        } catch ( IOException ex ) {}
        if ( !Nio2Endpoint.isInline() ) {
            Nio2SocketWrapper.this.getEndpoint().processSocket ( Nio2SocketWrapper.this, SocketEvent.ERROR, false );
        } else {
            attachment.doneInline = true;
            attachment.error = true;
        }
    }
}
