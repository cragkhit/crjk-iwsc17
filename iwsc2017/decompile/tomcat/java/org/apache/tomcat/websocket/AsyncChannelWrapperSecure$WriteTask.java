package org.apache.tomcat.websocket;
import java.util.concurrent.Future;
import javax.net.ssl.SSLEngineResult;
import java.nio.ByteBuffer;
private class WriteTask implements Runnable {
    private final ByteBuffer[] srcs;
    private final int offset;
    private final int length;
    private final WrapperFuture<Long, ?> future;
    public WriteTask ( final ByteBuffer[] srcs, final int offset, final int length, final WrapperFuture<Long, ?> future ) {
        this.srcs = srcs;
        this.future = future;
        this.offset = offset;
        this.length = length;
    }
    @Override
    public void run() {
        long written = 0L;
        try {
            for ( int i = this.offset; i < this.offset + this.length; ++i ) {
                final ByteBuffer src = this.srcs[i];
                while ( src.hasRemaining() ) {
                    AsyncChannelWrapperSecure.access$100 ( AsyncChannelWrapperSecure.this ).clear();
                    final SSLEngineResult r = AsyncChannelWrapperSecure.access$200 ( AsyncChannelWrapperSecure.this ).wrap ( src, AsyncChannelWrapperSecure.access$100 ( AsyncChannelWrapperSecure.this ) );
                    written += r.bytesConsumed();
                    final SSLEngineResult.Status s = r.getStatus();
                    if ( s != SSLEngineResult.Status.OK && s != SSLEngineResult.Status.BUFFER_OVERFLOW ) {
                        throw new IllegalStateException ( AsyncChannelWrapperSecure.access$300().getString ( "asyncChannelWrapperSecure.statusWrap" ) );
                    }
                    if ( r.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK ) {
                        for ( Runnable runnable = AsyncChannelWrapperSecure.access$200 ( AsyncChannelWrapperSecure.this ).getDelegatedTask(); runnable != null; runnable = AsyncChannelWrapperSecure.access$200 ( AsyncChannelWrapperSecure.this ).getDelegatedTask() ) {
                            runnable.run();
                        }
                    }
                    AsyncChannelWrapperSecure.access$100 ( AsyncChannelWrapperSecure.this ).flip();
                    Integer socketWrite;
                    for ( int toWrite = r.bytesProduced(); toWrite > 0; toWrite -= socketWrite ) {
                        final Future<Integer> f = AsyncChannelWrapperSecure.access$400 ( AsyncChannelWrapperSecure.this ).write ( AsyncChannelWrapperSecure.access$100 ( AsyncChannelWrapperSecure.this ) );
                        socketWrite = f.get();
                    }
                }
            }
            if ( AsyncChannelWrapperSecure.access$500 ( AsyncChannelWrapperSecure.this ).compareAndSet ( true, false ) ) {
                this.future.complete ( written );
            } else {
                this.future.fail ( new IllegalStateException ( AsyncChannelWrapperSecure.access$300().getString ( "asyncChannelWrapperSecure.wrongStateWrite" ) ) );
            }
        } catch ( Exception e ) {
            this.future.fail ( e );
        }
    }
}
