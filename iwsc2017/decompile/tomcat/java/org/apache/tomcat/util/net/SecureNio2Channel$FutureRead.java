package org.apache.tomcat.util.net;
import java.io.IOException;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;
private class FutureRead implements Future<Integer> {
    private ByteBuffer dst;
    private Future<Integer> integer;
    private FutureRead ( final ByteBuffer dst ) {
        this.dst = dst;
        if ( SecureNio2Channel.access$200 ( SecureNio2Channel.this ) || SecureNio2Channel.this.netInBuffer.position() > 0 ) {
            this.integer = null;
        } else {
            this.integer = SecureNio2Channel.this.sc.read ( SecureNio2Channel.this.netInBuffer );
        }
    }
    @Override
    public boolean cancel ( final boolean mayInterruptIfRunning ) {
        return this.integer != null && this.integer.cancel ( mayInterruptIfRunning );
    }
    @Override
    public boolean isCancelled() {
        return this.integer != null && this.integer.isCancelled();
    }
    @Override
    public boolean isDone() {
        return this.integer == null || this.integer.isDone();
    }
    @Override
    public Integer get() throws InterruptedException, ExecutionException {
        try {
            return ( this.integer == null ) ? this.unwrap ( SecureNio2Channel.this.netInBuffer.position(), -1L, TimeUnit.MILLISECONDS ) : this.unwrap ( this.integer.get(), -1L, TimeUnit.MILLISECONDS );
        } catch ( TimeoutException e ) {
            throw new ExecutionException ( e );
        }
    }
    @Override
    public Integer get ( final long timeout, final TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
        return ( this.integer == null ) ? this.unwrap ( SecureNio2Channel.this.netInBuffer.position(), timeout, unit ) : this.unwrap ( this.integer.get ( timeout, unit ), timeout, unit );
    }
    private Integer unwrap ( final int nRead, final long timeout, final TimeUnit unit ) throws ExecutionException, TimeoutException, InterruptedException {
        if ( SecureNio2Channel.this.closing || SecureNio2Channel.this.closed ) {
            return -1;
        }
        if ( nRead < 0 ) {
            return -1;
        }
        int read = 0;
        do {
            SecureNio2Channel.this.netInBuffer.flip();
            SSLEngineResult unwrap;
            try {
                unwrap = SecureNio2Channel.this.sslEngine.unwrap ( SecureNio2Channel.this.netInBuffer, this.dst );
            } catch ( SSLException e ) {
                throw new ExecutionException ( e );
            }
            SecureNio2Channel.this.netInBuffer.compact();
            if ( unwrap.getStatus() == SSLEngineResult.Status.OK || unwrap.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW ) {
                read += unwrap.bytesProduced();
                if ( unwrap.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK ) {
                    SecureNio2Channel.this.tasks();
                }
                if ( unwrap.getStatus() != SSLEngineResult.Status.BUFFER_UNDERFLOW ) {
                    continue;
                }
                if ( read != 0 ) {
                    break;
                }
                this.integer = SecureNio2Channel.this.sc.read ( SecureNio2Channel.this.netInBuffer );
                if ( timeout > 0L ) {
                    return this.unwrap ( this.integer.get ( timeout, unit ), timeout, unit );
                }
                return this.unwrap ( this.integer.get(), -1L, TimeUnit.MILLISECONDS );
            } else {
                if ( unwrap.getStatus() != SSLEngineResult.Status.BUFFER_OVERFLOW ) {
                    throw new ExecutionException ( new IOException ( SecureNio2Channel.access$300().getString ( "channel.nio.ssl.unwrapFail", unwrap.getStatus() ) ) );
                }
                if ( read > 0 ) {
                    break;
                }
                if ( this.dst == SecureNio2Channel.this.getBufHandler().getReadBuffer() ) {
                    SecureNio2Channel.this.getBufHandler().expand ( SecureNio2Channel.this.sslEngine.getSession().getApplicationBufferSize() );
                    this.dst = SecureNio2Channel.this.getBufHandler().getReadBuffer();
                } else {
                    if ( this.dst != SecureNio2Channel.this.getAppReadBufHandler().getByteBuffer() ) {
                        throw new ExecutionException ( new IOException ( SecureNio2Channel.access$300().getString ( "channel.nio.ssl.unwrapFailResize", unwrap.getStatus() ) ) );
                    }
                    SecureNio2Channel.this.getAppReadBufHandler().expand ( SecureNio2Channel.this.sslEngine.getSession().getApplicationBufferSize() );
                    this.dst = SecureNio2Channel.this.getAppReadBufHandler().getByteBuffer();
                }
            }
        } while ( SecureNio2Channel.this.netInBuffer.position() != 0 );
        if ( !this.dst.hasRemaining() ) {
            SecureNio2Channel.access$202 ( SecureNio2Channel.this, true );
        } else {
            SecureNio2Channel.access$202 ( SecureNio2Channel.this, false );
        }
        return read;
    }
}
