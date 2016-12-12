package org.apache.tomcat.websocket;
import java.util.concurrent.Future;
import javax.net.ssl.SSLEngineResult;
import java.io.EOFException;
import java.nio.ByteBuffer;
private class ReadTask implements Runnable {
    private final ByteBuffer dest;
    private final WrapperFuture<Integer, ?> future;
    public ReadTask ( final ByteBuffer dest, final WrapperFuture<Integer, ?> future ) {
        this.dest = dest;
        this.future = future;
    }
    @Override
    public void run() {
        int read = 0;
        boolean forceRead = false;
        try {
            while ( read == 0 ) {
                AsyncChannelWrapperSecure.access$600 ( AsyncChannelWrapperSecure.this ).compact();
                if ( forceRead ) {
                    forceRead = false;
                    final Future<Integer> f = AsyncChannelWrapperSecure.access$400 ( AsyncChannelWrapperSecure.this ).read ( AsyncChannelWrapperSecure.access$600 ( AsyncChannelWrapperSecure.this ) );
                    final Integer socketRead = f.get();
                    if ( socketRead == -1 ) {
                        throw new EOFException ( AsyncChannelWrapperSecure.access$300().getString ( "asyncChannelWrapperSecure.eof" ) );
                    }
                }
                AsyncChannelWrapperSecure.access$600 ( AsyncChannelWrapperSecure.this ).flip();
                if ( AsyncChannelWrapperSecure.access$600 ( AsyncChannelWrapperSecure.this ).hasRemaining() ) {
                    final SSLEngineResult r = AsyncChannelWrapperSecure.access$200 ( AsyncChannelWrapperSecure.this ).unwrap ( AsyncChannelWrapperSecure.access$600 ( AsyncChannelWrapperSecure.this ), this.dest );
                    read += r.bytesProduced();
                    final SSLEngineResult.Status s = r.getStatus();
                    if ( s != SSLEngineResult.Status.OK ) {
                        if ( s == SSLEngineResult.Status.BUFFER_UNDERFLOW ) {
                            if ( read == 0 ) {
                                forceRead = true;
                            }
                        } else {
                            if ( s != SSLEngineResult.Status.BUFFER_OVERFLOW ) {
                                throw new IllegalStateException ( AsyncChannelWrapperSecure.access$300().getString ( "asyncChannelWrapperSecure.statusUnwrap" ) );
                            }
                            if ( AsyncChannelWrapperSecure.access$700 ( AsyncChannelWrapperSecure.this ).compareAndSet ( true, false ) ) {
                                throw new ReadBufferOverflowException ( AsyncChannelWrapperSecure.access$200 ( AsyncChannelWrapperSecure.this ).getSession().getApplicationBufferSize() );
                            }
                            this.future.fail ( new IllegalStateException ( AsyncChannelWrapperSecure.access$300().getString ( "asyncChannelWrapperSecure.wrongStateRead" ) ) );
                        }
                    }
                    if ( r.getHandshakeStatus() != SSLEngineResult.HandshakeStatus.NEED_TASK ) {
                        continue;
                    }
                    for ( Runnable runnable = AsyncChannelWrapperSecure.access$200 ( AsyncChannelWrapperSecure.this ).getDelegatedTask(); runnable != null; runnable = AsyncChannelWrapperSecure.access$200 ( AsyncChannelWrapperSecure.this ).getDelegatedTask() ) {
                        runnable.run();
                    }
                } else {
                    forceRead = true;
                }
            }
            if ( AsyncChannelWrapperSecure.access$700 ( AsyncChannelWrapperSecure.this ).compareAndSet ( true, false ) ) {
                this.future.complete ( read );
            } else {
                this.future.fail ( new IllegalStateException ( AsyncChannelWrapperSecure.access$300().getString ( "asyncChannelWrapperSecure.wrongStateRead" ) ) );
            }
        } catch ( Exception e ) {
            this.future.fail ( e );
        }
    }
}
