package org.apache.tomcat.websocket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.io.EOFException;
import javax.net.ssl.SSLEngineResult;
import org.apache.juli.logging.LogFactory;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ExecutorService;
import javax.net.ssl.SSLEngine;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.ByteBuffer;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
public class AsyncChannelWrapperSecure implements AsyncChannelWrapper {
    private static final Log log;
    private static final StringManager sm;
    private static final ByteBuffer DUMMY;
    private final AsynchronousSocketChannel socketChannel;
    private final SSLEngine sslEngine;
    private final ByteBuffer socketReadBuffer;
    private final ByteBuffer socketWriteBuffer;
    private final ExecutorService executor;
    private AtomicBoolean writing;
    private AtomicBoolean reading;
    public AsyncChannelWrapperSecure ( final AsynchronousSocketChannel socketChannel, final SSLEngine sslEngine ) {
        this.executor = Executors.newFixedThreadPool ( 2, new SecureIOThreadFactory() );
        this.writing = new AtomicBoolean ( false );
        this.reading = new AtomicBoolean ( false );
        this.socketChannel = socketChannel;
        this.sslEngine = sslEngine;
        final int socketBufferSize = sslEngine.getSession().getPacketBufferSize();
        this.socketReadBuffer = ByteBuffer.allocateDirect ( socketBufferSize );
        this.socketWriteBuffer = ByteBuffer.allocateDirect ( socketBufferSize );
    }
    @Override
    public Future<Integer> read ( final ByteBuffer dst ) {
        final WrapperFuture<Integer, Void> future = new WrapperFuture<Integer, Void>();
        if ( !this.reading.compareAndSet ( false, true ) ) {
            throw new IllegalStateException ( AsyncChannelWrapperSecure.sm.getString ( "asyncChannelWrapperSecure.concurrentRead" ) );
        }
        final ReadTask readTask = new ReadTask ( dst, future );
        this.executor.execute ( readTask );
        return future;
    }
    @Override
    public <B, A extends B> void read ( final ByteBuffer dst, final A attachment, final CompletionHandler<Integer, B> handler ) {
        final WrapperFuture<Integer, B> future = new WrapperFuture<Integer, B> ( handler, attachment );
        if ( !this.reading.compareAndSet ( false, true ) ) {
            throw new IllegalStateException ( AsyncChannelWrapperSecure.sm.getString ( "asyncChannelWrapperSecure.concurrentRead" ) );
        }
        final ReadTask readTask = new ReadTask ( dst, future );
        this.executor.execute ( readTask );
    }
    @Override
    public Future<Integer> write ( final ByteBuffer src ) {
        final WrapperFuture<Long, Void> inner = new WrapperFuture<Long, Void>();
        if ( !this.writing.compareAndSet ( false, true ) ) {
            throw new IllegalStateException ( AsyncChannelWrapperSecure.sm.getString ( "asyncChannelWrapperSecure.concurrentWrite" ) );
        }
        final WriteTask writeTask = new WriteTask ( new ByteBuffer[] { src }, 0, 1, inner );
        this.executor.execute ( writeTask );
        final Future<Integer> future = new LongToIntegerFuture ( inner );
        return future;
    }
    @Override
    public <B, A extends B> void write ( final ByteBuffer[] srcs, final int offset, final int length, final long timeout, final TimeUnit unit, final A attachment, final CompletionHandler<Long, B> handler ) {
        final WrapperFuture<Long, B> future = new WrapperFuture<Long, B> ( handler, attachment );
        if ( !this.writing.compareAndSet ( false, true ) ) {
            throw new IllegalStateException ( AsyncChannelWrapperSecure.sm.getString ( "asyncChannelWrapperSecure.concurrentWrite" ) );
        }
        final WriteTask writeTask = new WriteTask ( srcs, offset, length, future );
        this.executor.execute ( writeTask );
    }
    @Override
    public void close() {
        try {
            this.socketChannel.close();
        } catch ( IOException e ) {
            AsyncChannelWrapperSecure.log.info ( AsyncChannelWrapperSecure.sm.getString ( "asyncChannelWrapperSecure.closeFail" ) );
        }
        this.executor.shutdownNow();
    }
    @Override
    public Future<Void> handshake() throws SSLException {
        final WrapperFuture<Void, Void> wFuture = new WrapperFuture<Void, Void>();
        final Thread t = new WebSocketSslHandshakeThread ( wFuture );
        t.start();
        return wFuture;
    }
    static {
        log = LogFactory.getLog ( AsyncChannelWrapperSecure.class );
        sm = StringManager.getManager ( AsyncChannelWrapperSecure.class );
        DUMMY = ByteBuffer.allocate ( 8192 );
    }
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
                        AsyncChannelWrapperSecure.this.socketWriteBuffer.clear();
                        final SSLEngineResult r = AsyncChannelWrapperSecure.this.sslEngine.wrap ( src, AsyncChannelWrapperSecure.this.socketWriteBuffer );
                        written += r.bytesConsumed();
                        final SSLEngineResult.Status s = r.getStatus();
                        if ( s != SSLEngineResult.Status.OK && s != SSLEngineResult.Status.BUFFER_OVERFLOW ) {
                            throw new IllegalStateException ( AsyncChannelWrapperSecure.sm.getString ( "asyncChannelWrapperSecure.statusWrap" ) );
                        }
                        if ( r.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK ) {
                            for ( Runnable runnable = AsyncChannelWrapperSecure.this.sslEngine.getDelegatedTask(); runnable != null; runnable = AsyncChannelWrapperSecure.this.sslEngine.getDelegatedTask() ) {
                                runnable.run();
                            }
                        }
                        AsyncChannelWrapperSecure.this.socketWriteBuffer.flip();
                        Integer socketWrite;
                        for ( int toWrite = r.bytesProduced(); toWrite > 0; toWrite -= socketWrite ) {
                            final Future<Integer> f = AsyncChannelWrapperSecure.this.socketChannel.write ( AsyncChannelWrapperSecure.this.socketWriteBuffer );
                            socketWrite = f.get();
                        }
                    }
                }
                if ( AsyncChannelWrapperSecure.this.writing.compareAndSet ( true, false ) ) {
                    this.future.complete ( written );
                } else {
                    this.future.fail ( new IllegalStateException ( AsyncChannelWrapperSecure.sm.getString ( "asyncChannelWrapperSecure.wrongStateWrite" ) ) );
                }
            } catch ( Exception e ) {
                this.future.fail ( e );
            }
        }
    }
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
                    AsyncChannelWrapperSecure.this.socketReadBuffer.compact();
                    if ( forceRead ) {
                        forceRead = false;
                        final Future<Integer> f = AsyncChannelWrapperSecure.this.socketChannel.read ( AsyncChannelWrapperSecure.this.socketReadBuffer );
                        final Integer socketRead = f.get();
                        if ( socketRead == -1 ) {
                            throw new EOFException ( AsyncChannelWrapperSecure.sm.getString ( "asyncChannelWrapperSecure.eof" ) );
                        }
                    }
                    AsyncChannelWrapperSecure.this.socketReadBuffer.flip();
                    if ( AsyncChannelWrapperSecure.this.socketReadBuffer.hasRemaining() ) {
                        final SSLEngineResult r = AsyncChannelWrapperSecure.this.sslEngine.unwrap ( AsyncChannelWrapperSecure.this.socketReadBuffer, this.dest );
                        read += r.bytesProduced();
                        final SSLEngineResult.Status s = r.getStatus();
                        if ( s != SSLEngineResult.Status.OK ) {
                            if ( s == SSLEngineResult.Status.BUFFER_UNDERFLOW ) {
                                if ( read == 0 ) {
                                    forceRead = true;
                                }
                            } else {
                                if ( s != SSLEngineResult.Status.BUFFER_OVERFLOW ) {
                                    throw new IllegalStateException ( AsyncChannelWrapperSecure.sm.getString ( "asyncChannelWrapperSecure.statusUnwrap" ) );
                                }
                                if ( AsyncChannelWrapperSecure.this.reading.compareAndSet ( true, false ) ) {
                                    throw new ReadBufferOverflowException ( AsyncChannelWrapperSecure.this.sslEngine.getSession().getApplicationBufferSize() );
                                }
                                this.future.fail ( new IllegalStateException ( AsyncChannelWrapperSecure.sm.getString ( "asyncChannelWrapperSecure.wrongStateRead" ) ) );
                            }
                        }
                        if ( r.getHandshakeStatus() != SSLEngineResult.HandshakeStatus.NEED_TASK ) {
                            continue;
                        }
                        for ( Runnable runnable = AsyncChannelWrapperSecure.this.sslEngine.getDelegatedTask(); runnable != null; runnable = AsyncChannelWrapperSecure.this.sslEngine.getDelegatedTask() ) {
                            runnable.run();
                        }
                    } else {
                        forceRead = true;
                    }
                }
                if ( AsyncChannelWrapperSecure.this.reading.compareAndSet ( true, false ) ) {
                    this.future.complete ( read );
                } else {
                    this.future.fail ( new IllegalStateException ( AsyncChannelWrapperSecure.sm.getString ( "asyncChannelWrapperSecure.wrongStateRead" ) ) );
                }
            } catch ( Exception e ) {
                this.future.fail ( e );
            }
        }
    }
    private class WebSocketSslHandshakeThread extends Thread {
        private final WrapperFuture<Void, Void> hFuture;
        private SSLEngineResult.HandshakeStatus handshakeStatus;
        private SSLEngineResult.Status resultStatus;
        public WebSocketSslHandshakeThread ( final WrapperFuture<Void, Void> hFuture ) {
            this.hFuture = hFuture;
        }
        @Override
        public void run() {
            try {
                AsyncChannelWrapperSecure.this.sslEngine.beginHandshake();
                AsyncChannelWrapperSecure.this.socketReadBuffer.position ( AsyncChannelWrapperSecure.this.socketReadBuffer.limit() );
                this.handshakeStatus = AsyncChannelWrapperSecure.this.sslEngine.getHandshakeStatus();
                this.resultStatus = SSLEngineResult.Status.OK;
                boolean handshaking = true;
                while ( handshaking ) {
                    switch ( this.handshakeStatus ) {
                    case NEED_WRAP: {
                        AsyncChannelWrapperSecure.this.socketWriteBuffer.clear();
                        final SSLEngineResult r = AsyncChannelWrapperSecure.this.sslEngine.wrap ( AsyncChannelWrapperSecure.DUMMY, AsyncChannelWrapperSecure.this.socketWriteBuffer );
                        this.checkResult ( r, true );
                        AsyncChannelWrapperSecure.this.socketWriteBuffer.flip();
                        final Future<Integer> fWrite = AsyncChannelWrapperSecure.this.socketChannel.write ( AsyncChannelWrapperSecure.this.socketWriteBuffer );
                        fWrite.get();
                        continue;
                    }
                    case NEED_UNWRAP: {
                        AsyncChannelWrapperSecure.this.socketReadBuffer.compact();
                        if ( AsyncChannelWrapperSecure.this.socketReadBuffer.position() == 0 || this.resultStatus == SSLEngineResult.Status.BUFFER_UNDERFLOW ) {
                            final Future<Integer> fRead = AsyncChannelWrapperSecure.this.socketChannel.read ( AsyncChannelWrapperSecure.this.socketReadBuffer );
                            fRead.get();
                        }
                        AsyncChannelWrapperSecure.this.socketReadBuffer.flip();
                        final SSLEngineResult r = AsyncChannelWrapperSecure.this.sslEngine.unwrap ( AsyncChannelWrapperSecure.this.socketReadBuffer, AsyncChannelWrapperSecure.DUMMY );
                        this.checkResult ( r, false );
                        continue;
                    }
                    case NEED_TASK: {
                        Runnable r2 = null;
                        while ( ( r2 = AsyncChannelWrapperSecure.this.sslEngine.getDelegatedTask() ) != null ) {
                            r2.run();
                        }
                        this.handshakeStatus = AsyncChannelWrapperSecure.this.sslEngine.getHandshakeStatus();
                        continue;
                    }
                    case FINISHED: {
                        handshaking = false;
                        continue;
                    }
                    case NOT_HANDSHAKING: {
                        throw new SSLException ( AsyncChannelWrapperSecure.sm.getString ( "asyncChannelWrapperSecure.notHandshaking" ) );
                    }
                    }
                }
            } catch ( SSLException | InterruptedException | ExecutionException e ) {
                this.hFuture.fail ( e );
            }
            this.hFuture.complete ( null );
        }
        private void checkResult ( final SSLEngineResult result, final boolean wrap ) throws SSLException {
            this.handshakeStatus = result.getHandshakeStatus();
            this.resultStatus = result.getStatus();
            if ( this.resultStatus != SSLEngineResult.Status.OK && ( wrap || this.resultStatus != SSLEngineResult.Status.BUFFER_UNDERFLOW ) ) {
                throw new SSLException ( AsyncChannelWrapperSecure.sm.getString ( "asyncChannelWrapperSecure.check.notOk", this.resultStatus ) );
            }
            if ( wrap && result.bytesConsumed() != 0 ) {
                throw new SSLException ( AsyncChannelWrapperSecure.sm.getString ( "asyncChannelWrapperSecure.check.wrap" ) );
            }
            if ( !wrap && result.bytesProduced() != 0 ) {
                throw new SSLException ( AsyncChannelWrapperSecure.sm.getString ( "asyncChannelWrapperSecure.check.unwrap" ) );
            }
        }
    }
    private static class WrapperFuture<T, A> implements Future<T> {
        private final CompletionHandler<T, A> handler;
        private final A attachment;
        private volatile T result;
        private volatile Throwable throwable;
        private CountDownLatch completionLatch;
        public WrapperFuture() {
            this ( null, null );
        }
        public WrapperFuture ( final CompletionHandler<T, A> handler, final A attachment ) {
            this.result = null;
            this.throwable = null;
            this.completionLatch = new CountDownLatch ( 1 );
            this.handler = handler;
            this.attachment = attachment;
        }
        public void complete ( final T result ) {
            this.result = result;
            this.completionLatch.countDown();
            if ( this.handler != null ) {
                this.handler.completed ( result, this.attachment );
            }
        }
        public void fail ( final Throwable t ) {
            this.throwable = t;
            this.completionLatch.countDown();
            if ( this.handler != null ) {
                this.handler.failed ( this.throwable, this.attachment );
            }
        }
        @Override
        public final boolean cancel ( final boolean mayInterruptIfRunning ) {
            return false;
        }
        @Override
        public final boolean isCancelled() {
            return false;
        }
        @Override
        public final boolean isDone() {
            return this.completionLatch.getCount() > 0L;
        }
        @Override
        public T get() throws InterruptedException, ExecutionException {
            this.completionLatch.await();
            if ( this.throwable != null ) {
                throw new ExecutionException ( this.throwable );
            }
            return this.result;
        }
        @Override
        public T get ( final long timeout, final TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
            final boolean latchResult = this.completionLatch.await ( timeout, unit );
            if ( !latchResult ) {
                throw new TimeoutException();
            }
            if ( this.throwable != null ) {
                throw new ExecutionException ( this.throwable );
            }
            return this.result;
        }
    }
    private static final class LongToIntegerFuture implements Future<Integer> {
        private final Future<Long> wrapped;
        public LongToIntegerFuture ( final Future<Long> wrapped ) {
            this.wrapped = wrapped;
        }
        @Override
        public boolean cancel ( final boolean mayInterruptIfRunning ) {
            return this.wrapped.cancel ( mayInterruptIfRunning );
        }
        @Override
        public boolean isCancelled() {
            return this.wrapped.isCancelled();
        }
        @Override
        public boolean isDone() {
            return this.wrapped.isDone();
        }
        @Override
        public Integer get() throws InterruptedException, ExecutionException {
            final Long result = this.wrapped.get();
            if ( result > 2147483647L ) {
                throw new ExecutionException ( AsyncChannelWrapperSecure.sm.getString ( "asyncChannelWrapperSecure.tooBig", result ), null );
            }
            return ( int ) ( Object ) result;
        }
        @Override
        public Integer get ( final long timeout, final TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
            final Long result = this.wrapped.get ( timeout, unit );
            if ( result > 2147483647L ) {
                throw new ExecutionException ( AsyncChannelWrapperSecure.sm.getString ( "asyncChannelWrapperSecure.tooBig", result ), null );
            }
            return ( int ) ( Object ) result;
        }
    }
    private static class SecureIOThreadFactory implements ThreadFactory {
        private AtomicInteger count;
        private SecureIOThreadFactory() {
            this.count = new AtomicInteger ( 0 );
        }
        @Override
        public Thread newThread ( final Runnable r ) {
            final Thread t = new Thread ( r );
            t.setName ( "WebSocketClient-SecureIO-" + this.count.incrementAndGet() );
            t.setDaemon ( true );
            return t;
        }
    }
}
