package org.apache.tomcat.util.net;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLEngineResult;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;
private class FutureWrite implements Future<Integer> {
    private final ByteBuffer src;
    private Future<Integer> integer;
    private int written;
    private Throwable t;
    private FutureWrite ( final ByteBuffer src ) {
        this.integer = null;
        this.written = 0;
        this.t = null;
        this.src = src;
        if ( SecureNio2Channel.this.closing || SecureNio2Channel.this.closed ) {
            this.t = new IOException ( SecureNio2Channel.access$300().getString ( "channel.nio.ssl.closing" ) );
        } else {
            this.wrap();
        }
    }
    @Override
    public boolean cancel ( final boolean mayInterruptIfRunning ) {
        return this.integer.cancel ( mayInterruptIfRunning );
    }
    @Override
    public boolean isCancelled() {
        return this.integer.isCancelled();
    }
    @Override
    public boolean isDone() {
        return this.integer.isDone();
    }
    @Override
    public Integer get() throws InterruptedException, ExecutionException {
        if ( this.t != null ) {
            throw new ExecutionException ( this.t );
        }
        if ( this.integer.get() > 0 && this.written == 0 ) {
            this.wrap();
            return this.get();
        }
        if ( SecureNio2Channel.this.netOutBuffer.hasRemaining() ) {
            this.integer = SecureNio2Channel.this.sc.write ( SecureNio2Channel.this.netOutBuffer );
            return this.get();
        }
        return this.written;
    }
    @Override
    public Integer get ( final long timeout, final TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
        if ( this.t != null ) {
            throw new ExecutionException ( this.t );
        }
        if ( this.integer.get ( timeout, unit ) > 0 && this.written == 0 ) {
            this.wrap();
            return this.get ( timeout, unit );
        }
        if ( SecureNio2Channel.this.netOutBuffer.hasRemaining() ) {
            this.integer = SecureNio2Channel.this.sc.write ( SecureNio2Channel.this.netOutBuffer );
            return this.get ( timeout, unit );
        }
        return this.written;
    }
    protected void wrap() {
        try {
            if ( !SecureNio2Channel.this.netOutBuffer.hasRemaining() ) {
                SecureNio2Channel.this.netOutBuffer.clear();
                final SSLEngineResult result = SecureNio2Channel.this.sslEngine.wrap ( this.src, SecureNio2Channel.this.netOutBuffer );
                this.written = result.bytesConsumed();
                SecureNio2Channel.this.netOutBuffer.flip();
                if ( result.getStatus() == SSLEngineResult.Status.OK ) {
                    if ( result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK ) {
                        SecureNio2Channel.this.tasks();
                    }
                } else {
                    this.t = new IOException ( SecureNio2Channel.access$300().getString ( "channel.nio.ssl.wrapFail", result.getStatus() ) );
                }
            }
            this.integer = SecureNio2Channel.this.sc.write ( SecureNio2Channel.this.netOutBuffer );
        } catch ( SSLException e ) {
            this.t = e;
        }
    }
}
