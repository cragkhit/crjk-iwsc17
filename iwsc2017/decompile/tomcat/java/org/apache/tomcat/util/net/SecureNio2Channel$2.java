package org.apache.tomcat.util.net;
import java.io.IOException;
import javax.net.ssl.SSLEngineResult;
import java.io.EOFException;
import java.util.concurrent.TimeUnit;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
class SecureNio2Channel$2 implements CompletionHandler<Integer, A> {
    final   ByteBuffer[] val$dsts;
    final   int val$offset;
    final   int val$length;
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
                long read = 0L;
                do {
                    SecureNio2Channel.this.netInBuffer.flip();
                    final SSLEngineResult unwrap = SecureNio2Channel.this.sslEngine.unwrap ( SecureNio2Channel.this.netInBuffer, this.val$dsts, this.val$offset, this.val$length );
                    SecureNio2Channel.this.netInBuffer.compact();
                    if ( unwrap.getStatus() == SSLEngineResult.Status.OK || unwrap.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW ) {
                        read += unwrap.bytesProduced();
                        if ( unwrap.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK ) {
                            SecureNio2Channel.this.tasks();
                        }
                        if ( unwrap.getStatus() != SSLEngineResult.Status.BUFFER_UNDERFLOW ) {
                            continue;
                        }
                        if ( read == 0L ) {
                            SecureNio2Channel.this.sc.read ( SecureNio2Channel.this.netInBuffer, this.val$timeout, this.val$unit, this.val$attachment, ( CompletionHandler<Integer, ? super Object> ) this );
                            return;
                        }
                        break;
                    } else {
                        if ( unwrap.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW && read > 0L ) {
                            break;
                        }
                        throw new IOException ( SecureNio2Channel.access$300().getString ( "channel.nio.ssl.unwrapFail", unwrap.getStatus() ) );
                    }
                } while ( SecureNio2Channel.this.netInBuffer.position() != 0 );
                int capacity = 0;
                for ( int endOffset = this.val$offset + this.val$length, i = this.val$offset; i < endOffset; ++i ) {
                    capacity += this.val$dsts[i].remaining();
                }
                if ( capacity == 0 ) {
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
