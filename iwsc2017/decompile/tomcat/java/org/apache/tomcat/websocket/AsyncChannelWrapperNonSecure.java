package org.apache.tomcat.websocket;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.nio.channels.CompletionHandler;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Future;
public class AsyncChannelWrapperNonSecure implements AsyncChannelWrapper {
    private static final Future<Void> NOOP_FUTURE;
    private final AsynchronousSocketChannel socketChannel;
    public AsyncChannelWrapperNonSecure ( final AsynchronousSocketChannel socketChannel ) {
        this.socketChannel = socketChannel;
    }
    @Override
    public Future<Integer> read ( final ByteBuffer dst ) {
        return this.socketChannel.read ( dst );
    }
    @Override
    public <B, A extends B> void read ( final ByteBuffer dst, final A attachment, final CompletionHandler<Integer, B> handler ) {
        this.socketChannel.read ( dst, attachment, handler );
    }
    @Override
    public Future<Integer> write ( final ByteBuffer src ) {
        return this.socketChannel.write ( src );
    }
    @Override
    public <B, A extends B> void write ( final ByteBuffer[] srcs, final int offset, final int length, final long timeout, final TimeUnit unit, final A attachment, final CompletionHandler<Long, B> handler ) {
        this.socketChannel.write ( srcs, offset, length, timeout, unit, attachment, handler );
    }
    @Override
    public void close() {
        try {
            this.socketChannel.close();
        } catch ( IOException ex ) {}
    }
    @Override
    public Future<Void> handshake() {
        return AsyncChannelWrapperNonSecure.NOOP_FUTURE;
    }
    static {
        NOOP_FUTURE = new NoOpFuture();
    }
    private static final class NoOpFuture implements Future<Void> {
        @Override
        public boolean cancel ( final boolean mayInterruptIfRunning ) {
            return false;
        }
        @Override
        public boolean isCancelled() {
            return false;
        }
        @Override
        public boolean isDone() {
            return true;
        }
        @Override
        public Void get() throws InterruptedException, ExecutionException {
            return null;
        }
        @Override
        public Void get ( final long timeout, final TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }
    }
}
