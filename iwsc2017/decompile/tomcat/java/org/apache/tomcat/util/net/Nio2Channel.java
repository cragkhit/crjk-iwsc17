package org.apache.tomcat.util.net;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.nio.channels.CompletionHandler;
import java.io.IOException;
import java.util.concurrent.Future;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
public class Nio2Channel implements AsynchronousByteChannel {
    protected static final ByteBuffer emptyBuf;
    protected AsynchronousSocketChannel sc;
    protected SocketWrapperBase<Nio2Channel> socket;
    protected final SocketBufferHandler bufHandler;
    private static final Future<Boolean> DONE;
    private ApplicationBufferHandler appReadBufHandler;
    public Nio2Channel ( final SocketBufferHandler bufHandler ) {
        this.sc = null;
        this.socket = null;
        this.bufHandler = bufHandler;
    }
    public void reset ( final AsynchronousSocketChannel channel, final SocketWrapperBase<Nio2Channel> socket ) throws IOException {
        this.sc = channel;
        this.socket = socket;
        this.bufHandler.reset();
    }
    public void free() {
        this.bufHandler.free();
    }
    public SocketWrapperBase<Nio2Channel> getSocket() {
        return this.socket;
    }
    @Override
    public void close() throws IOException {
        this.sc.close();
    }
    public void close ( final boolean force ) throws IOException {
        if ( this.isOpen() || force ) {
            this.close();
        }
    }
    @Override
    public boolean isOpen() {
        return this.sc.isOpen();
    }
    public SocketBufferHandler getBufHandler() {
        return this.bufHandler;
    }
    public AsynchronousSocketChannel getIOChannel() {
        return this.sc;
    }
    public boolean isClosing() {
        return false;
    }
    public boolean isHandshakeComplete() {
        return true;
    }
    public int handshake() throws IOException {
        return 0;
    }
    @Override
    public String toString() {
        return super.toString() + ":" + this.sc.toString();
    }
    @Override
    public Future<Integer> read ( final ByteBuffer dst ) {
        return this.sc.read ( dst );
    }
    @Override
    public <A> void read ( final ByteBuffer dst, final A attachment, final CompletionHandler<Integer, ? super A> handler ) {
        this.read ( dst, 2147483647L, TimeUnit.MILLISECONDS, attachment, handler );
    }
    public <A> void read ( final ByteBuffer dst, final long timeout, final TimeUnit unit, final A attachment, final CompletionHandler<Integer, ? super A> handler ) {
        this.sc.read ( dst, timeout, unit, attachment, handler );
    }
    public <A> void read ( final ByteBuffer[] dsts, final int offset, final int length, final long timeout, final TimeUnit unit, final A attachment, final CompletionHandler<Long, ? super A> handler ) {
        this.sc.read ( dsts, offset, length, timeout, unit, attachment, handler );
    }
    @Override
    public Future<Integer> write ( final ByteBuffer src ) {
        return this.sc.write ( src );
    }
    @Override
    public <A> void write ( final ByteBuffer src, final A attachment, final CompletionHandler<Integer, ? super A> handler ) {
        this.write ( src, 2147483647L, TimeUnit.MILLISECONDS, attachment, handler );
    }
    public <A> void write ( final ByteBuffer src, final long timeout, final TimeUnit unit, final A attachment, final CompletionHandler<Integer, ? super A> handler ) {
        this.sc.write ( src, timeout, unit, attachment, handler );
    }
    public <A> void write ( final ByteBuffer[] srcs, final int offset, final int length, final long timeout, final TimeUnit unit, final A attachment, final CompletionHandler<Long, ? super A> handler ) {
        this.sc.write ( srcs, offset, length, timeout, unit, attachment, handler );
    }
    public Future<Boolean> flush() {
        return Nio2Channel.DONE;
    }
    public void setAppReadBufHandler ( final ApplicationBufferHandler handler ) {
        this.appReadBufHandler = handler;
    }
    protected ApplicationBufferHandler getAppReadBufHandler() {
        return this.appReadBufHandler;
    }
    static {
        emptyBuf = ByteBuffer.allocate ( 0 );
        DONE = new Future<Boolean>() {
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
            public Boolean get() throws InterruptedException, ExecutionException {
                return Boolean.TRUE;
            }
            @Override
            public Boolean get ( final long timeout, final TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
                return Boolean.TRUE;
            }
        };
    }
}
