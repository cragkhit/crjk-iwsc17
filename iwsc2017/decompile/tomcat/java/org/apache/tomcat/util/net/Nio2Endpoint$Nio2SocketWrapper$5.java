package org.apache.tomcat.util.net;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import org.apache.tomcat.util.buf.ByteBufferHolder;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
class Nio2Endpoint$Nio2SocketWrapper$5 implements CompletionHandler<Integer, ByteBuffer> {
    final   Nio2Endpoint val$endpoint;
    @Override
    public void completed ( final Integer nBytes, final ByteBuffer attachment ) {
        Nio2SocketWrapper.access$1002 ( Nio2SocketWrapper.this, false );
        synchronized ( Nio2SocketWrapper.access$1100 ( Nio2SocketWrapper.this ) ) {
            if ( nBytes < 0 ) {
                this.failed ( ( Throwable ) new EOFException ( SocketWrapperBase.sm.getString ( "iob.failedwrite" ) ), attachment );
            } else if ( Nio2SocketWrapper.this.bufferedWrites.size() > 0 ) {
                Nio2SocketWrapper.access$1200().get().incrementAndGet();
                final ArrayList<ByteBuffer> arrayList = new ArrayList<ByteBuffer>();
                if ( attachment.hasRemaining() ) {
                    arrayList.add ( attachment );
                }
                for ( final ByteBufferHolder buffer : Nio2SocketWrapper.this.bufferedWrites ) {
                    buffer.flip();
                    arrayList.add ( buffer.getBuf() );
                }
                Nio2SocketWrapper.this.bufferedWrites.clear();
                final ByteBuffer[] array = arrayList.toArray ( new ByteBuffer[arrayList.size()] );
                Nio2SocketWrapper.this.getSocket().write ( array, 0, array.length, Nio2SocketWrapper.access$500 ( Nio2SocketWrapper.this ), TimeUnit.MILLISECONDS, array, Nio2SocketWrapper.access$1300 ( Nio2SocketWrapper.this ) );
                Nio2SocketWrapper.access$1200().get().decrementAndGet();
            } else if ( attachment.hasRemaining() ) {
                Nio2SocketWrapper.access$1200().get().incrementAndGet();
                Nio2SocketWrapper.this.getSocket().write ( attachment, Nio2SocketWrapper.access$500 ( Nio2SocketWrapper.this ), TimeUnit.MILLISECONDS, attachment, Nio2SocketWrapper.access$1100 ( Nio2SocketWrapper.this ) );
                Nio2SocketWrapper.access$1200().get().decrementAndGet();
            } else {
                if ( Nio2SocketWrapper.access$1400 ( Nio2SocketWrapper.this ) ) {
                    Nio2SocketWrapper.access$1402 ( Nio2SocketWrapper.this, false );
                    Nio2SocketWrapper.access$1002 ( Nio2SocketWrapper.this, true );
                }
                Nio2SocketWrapper.access$1500 ( Nio2SocketWrapper.this ).release();
            }
        }
        if ( Nio2SocketWrapper.access$1000 ( Nio2SocketWrapper.this ) && Nio2SocketWrapper.access$1200().get().get() == 0 ) {
            this.val$endpoint.processSocket ( Nio2SocketWrapper.this, SocketEvent.OPEN_WRITE, Nio2Endpoint.isInline() );
        }
    }
    @Override
    public void failed ( final Throwable exc, final ByteBuffer attachment ) {
        IOException ioe;
        if ( exc instanceof IOException ) {
            ioe = ( IOException ) exc;
        } else {
            ioe = new IOException ( exc );
        }
        Nio2SocketWrapper.this.setError ( ioe );
        Nio2SocketWrapper.access$1500 ( Nio2SocketWrapper.this ).release();
        this.val$endpoint.processSocket ( Nio2SocketWrapper.this, SocketEvent.ERROR, true );
    }
}
