package org.apache.tomcat.util.net;
import java.io.IOException;
import javax.net.ssl.SSLEngineResult;
import java.io.EOFException;
import java.util.concurrent.TimeUnit;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
class SecureNio2Channel$1 implements CompletionHandler<Integer, A> {
    final   ByteBuffer val$dst;
    final   long val$timeout;
    final   TimeUnit val$unit;
    final   Object val$attachment;
    final   CompletionHandler val$handler;
    @Override
    public void completed ( final Integer nBytes, final A attach ) {
        if ( nBytes < 0 ) {
            this.failed ( new EOFException(), attach );
        } else {
            try {
                ByteBuffer dst2 = this.val$dst;
                int read = 0;
                do {
                    SecureNio2Channel.this.netInBuffer.flip();
                    final SSLEngineResult unwrap = SecureNio2Channel.this.sslEngine.unwrap ( SecureNio2Channel.this.netInBuffer, dst2 );
                    SecureNio2Channel.this.netInBuffer.compact();
                    if ( unwrap.getStatus() == SSLEngineResult.Status.OK || unwrap.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW ) {
                        read += unwrap.bytesProduced();
                        if ( unwrap.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK ) {
                            SecureNio2Channel.this.tasks();
                        }
                        if ( unwrap.getStatus() != SSLEngineResult.Status.BUFFER_UNDERFLOW ) {
                            continue;
                        }
                        if ( read == 0 ) {
                            SecureNio2Channel.this.sc.read ( SecureNio2Channel.this.netInBuffer, this.val$timeout, this.val$unit, this.val$attachment, ( CompletionHandler<Integer, ? super Object> ) this );
                            return;
                        }
                        break;
                    } else {
                        if ( unwrap.getStatus() != SSLEngineResult.Status.BUFFER_OVERFLOW ) {
                            throw new IOException ( SecureNio2Channel.access$300().getString ( "channel.nio.ssl.unwrapFail", unwrap.getStatus() ) );
                        }
                        if ( read > 0 ) {
                            break;
                        }
                        if ( dst2 != SecureNio2Channel.this.getBufHandler().getReadBuffer() ) {
                            throw new IOException ( SecureNio2Channel.access$300().getString ( "channel.nio.ssl.unwrapFailResize", unwrap.getStatus() ) );
                        }
                        SecureNio2Channel.this.getBufHandler().expand ( SecureNio2Channel.this.sslEngine.getSession().getApplicationBufferSize() );
                        dst2 = SecureNio2Channel.this.getBufHandler().getReadBuffer();
                    }
                } while ( SecureNio2Channel.this.netInBuffer.position() != 0 );
                if ( !dst2.hasRemaining() ) {
                    SecureNio2Channel.access$202 ( SecureNio2Channel.this, true );
                } else {
                    SecureNio2Channel.access$202 ( SecureNio2Channel.this, false );
                }
                this.val$handler.completed ( read, attach );
            } catch ( Exception e ) {
                this.failed ( e, attach );
            }
        }
    }
    @Override
    public void failed ( final Throwable exc, final A attach ) {
        this.val$handler.failed ( exc, attach );
    }
}
