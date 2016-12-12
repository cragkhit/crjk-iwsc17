package org.apache.tomcat.util.net;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadPendingException;
import java.nio.channels.WritePendingException;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.buf.ByteBufferHolder;
import org.apache.tomcat.util.collections.SynchronizedStack;
import org.apache.tomcat.util.net.AbstractEndpoint.Handler.SocketState;
import org.apache.tomcat.util.net.jsse.JSSESupport;
public class Nio2Endpoint extends AbstractJsseEndpoint<Nio2Channel> {
    private static final Log log = LogFactory.getLog ( Nio2Endpoint.class );
    private AsynchronousServerSocketChannel serverSock = null;
    private static ThreadLocal<Boolean> inlineCompletion = new ThreadLocal<>();
    private AsynchronousChannelGroup threadGroup = null;
    private volatile boolean allClosed;
    private SynchronizedStack<Nio2Channel> nioChannels;
    public Nio2Endpoint() {
        setMaxConnections ( -1 );
    }
    public void setSocketProperties ( SocketProperties socketProperties ) {
        this.socketProperties = socketProperties;
    }
    @Override
    public boolean getDeferAccept() {
        return false;
    }
    @Override
    public int getLocalPort() {
        AsynchronousServerSocketChannel ssc = serverSock;
        if ( ssc == null ) {
            return -1;
        } else {
            try {
                SocketAddress sa = ssc.getLocalAddress();
                if ( sa instanceof InetSocketAddress ) {
                    return ( ( InetSocketAddress ) sa ).getPort();
                } else {
                    return -1;
                }
            } catch ( IOException e ) {
                return -1;
            }
        }
    }
    public int getKeepAliveCount() {
        return -1;
    }
    @Override
    public void bind() throws Exception {
        if ( getExecutor() == null ) {
            createExecutor();
        }
        if ( getExecutor() instanceof ExecutorService ) {
            threadGroup = AsynchronousChannelGroup.withThreadPool ( ( ExecutorService ) getExecutor() );
        }
        if ( !internalExecutor ) {
            log.warn ( sm.getString ( "endpoint.nio2.exclusiveExecutor" ) );
        }
        serverSock = AsynchronousServerSocketChannel.open ( threadGroup );
        socketProperties.setProperties ( serverSock );
        InetSocketAddress addr = ( getAddress() != null ? new InetSocketAddress ( getAddress(), getPort() ) : new InetSocketAddress ( getPort() ) );
        serverSock.bind ( addr, getAcceptCount() );
        if ( acceptorThreadCount != 1 ) {
            acceptorThreadCount = 1;
        }
        initialiseSsl();
    }
    @Override
    public void startInternal() throws Exception {
        if ( !running ) {
            allClosed = false;
            running = true;
            paused = false;
            processorCache = new SynchronizedStack<> ( SynchronizedStack.DEFAULT_SIZE,
                    socketProperties.getProcessorCache() );
            nioChannels = new SynchronizedStack<> ( SynchronizedStack.DEFAULT_SIZE,
                                                    socketProperties.getBufferPool() );
            if ( getExecutor() == null ) {
                createExecutor();
            }
            initializeConnectionLatch();
            startAcceptorThreads();
        }
    }
    @Override
    public void stopInternal() {
        releaseConnectionLatch();
        if ( !paused ) {
            pause();
        }
        if ( running ) {
            running = false;
            unlockAccept();
            getExecutor().execute ( new Runnable() {
                @Override
                public void run() {
                    try {
                        for ( Nio2Channel channel : getHandler().getOpenSockets() ) {
                            closeSocket ( channel.getSocket() );
                        }
                    } catch ( Throwable t ) {
                        ExceptionUtils.handleThrowable ( t );
                    } finally {
                        allClosed = true;
                    }
                }
            } );
            nioChannels.clear();
            processorCache.clear();
        }
    }
    @Override
    public void unbind() throws Exception {
        if ( running ) {
            stop();
        }
        serverSock.close();
        serverSock = null;
        destroySsl();
        super.unbind();
        shutdownExecutor();
        if ( getHandler() != null ) {
            getHandler().recycle();
        }
    }
    @Override
    public void shutdownExecutor() {
        if ( threadGroup != null && internalExecutor ) {
            try {
                long timeout = getExecutorTerminationTimeoutMillis();
                while ( timeout > 0 && !allClosed ) {
                    timeout -= 100;
                    Thread.sleep ( 100 );
                }
                threadGroup.shutdownNow();
                if ( timeout > 0 ) {
                    threadGroup.awaitTermination ( timeout, TimeUnit.MILLISECONDS );
                }
            } catch ( IOException e ) {
                getLog().warn ( sm.getString ( "endpoint.warn.executorShutdown", getName() ), e );
            } catch ( InterruptedException e ) {
            }
            if ( !threadGroup.isTerminated() ) {
                getLog().warn ( sm.getString ( "endpoint.warn.executorShutdown", getName() ) );
            }
            threadGroup = null;
        }
        super.shutdownExecutor();
    }
    public int getWriteBufSize() {
        return socketProperties.getTxBufSize();
    }
    public int getReadBufSize() {
        return socketProperties.getRxBufSize();
    }
    @Override
    protected AbstractEndpoint.Acceptor createAcceptor() {
        return new Acceptor();
    }
    protected boolean setSocketOptions ( AsynchronousSocketChannel socket ) {
        try {
            socketProperties.setProperties ( socket );
            Nio2Channel channel = nioChannels.pop();
            if ( channel == null ) {
                SocketBufferHandler bufhandler = new SocketBufferHandler (
                    socketProperties.getAppReadBufSize(),
                    socketProperties.getAppWriteBufSize(),
                    socketProperties.getDirectBuffer() );
                if ( isSSLEnabled() ) {
                    channel = new SecureNio2Channel ( bufhandler, this );
                } else {
                    channel = new Nio2Channel ( bufhandler );
                }
            }
            Nio2SocketWrapper socketWrapper = new Nio2SocketWrapper ( channel, this );
            channel.reset ( socket, socketWrapper );
            socketWrapper.setReadTimeout ( getSocketProperties().getSoTimeout() );
            socketWrapper.setWriteTimeout ( getSocketProperties().getSoTimeout() );
            socketWrapper.setKeepAliveLeft ( Nio2Endpoint.this.getMaxKeepAliveRequests() );
            socketWrapper.setSecure ( isSSLEnabled() );
            socketWrapper.setReadTimeout ( getConnectionTimeout() );
            socketWrapper.setWriteTimeout ( getConnectionTimeout() );
            return processSocket ( socketWrapper, SocketEvent.OPEN_READ, true );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            log.error ( "", t );
        }
        return false;
    }
    @Override
    protected SocketProcessorBase<Nio2Channel> createSocketProcessor (
        SocketWrapperBase<Nio2Channel> socketWrapper, SocketEvent event ) {
        return new SocketProcessor ( socketWrapper, event );
    }
    public void closeSocket ( SocketWrapperBase<Nio2Channel> socket ) {
        if ( log.isDebugEnabled() ) {
            log.debug ( "Calling [" + this + "].closeSocket([" + socket + "],[" + socket.getSocket() + "])",
                        new Exception() );
        }
        if ( socket == null ) {
            return;
        }
        try {
            getHandler().release ( socket );
        } catch ( Throwable e ) {
            ExceptionUtils.handleThrowable ( e );
            if ( log.isDebugEnabled() ) {
                log.error ( "", e );
            }
        }
        try {
            synchronized ( socket.getSocket() ) {
                if ( socket.getSocket().isOpen() ) {
                    countDownConnection();
                    socket.getSocket().close ( true );
                }
            }
        } catch ( Throwable e ) {
            ExceptionUtils.handleThrowable ( e );
            if ( log.isDebugEnabled() ) {
                log.error ( "", e );
            }
        }
        try {
            Nio2SocketWrapper nio2Socket = ( Nio2SocketWrapper ) socket;
            if ( nio2Socket.getSendfileData() != null
                    && nio2Socket.getSendfileData().fchannel != null
                    && nio2Socket.getSendfileData().fchannel.isOpen() ) {
                nio2Socket.getSendfileData().fchannel.close();
            }
        } catch ( Throwable e ) {
            ExceptionUtils.handleThrowable ( e );
            if ( log.isDebugEnabled() ) {
                log.error ( "", e );
            }
        }
    }
    @Override
    protected Log getLog() {
        return log;
    }
    protected class Acceptor extends AbstractEndpoint.Acceptor {
        @Override
        public void run() {
            int errorDelay = 0;
            while ( running ) {
                while ( paused && running ) {
                    state = AcceptorState.PAUSED;
                    try {
                        Thread.sleep ( 50 );
                    } catch ( InterruptedException e ) {
                    }
                }
                if ( !running ) {
                    break;
                }
                state = AcceptorState.RUNNING;
                try {
                    countUpOrAwaitConnection();
                    AsynchronousSocketChannel socket = null;
                    try {
                        socket = serverSock.accept().get();
                    } catch ( Exception e ) {
                        countDownConnection();
                        if ( running ) {
                            errorDelay = handleExceptionWithDelay ( errorDelay );
                            throw e;
                        } else {
                            break;
                        }
                    }
                    errorDelay = 0;
                    if ( running && !paused ) {
                        if ( !setSocketOptions ( socket ) ) {
                            countDownConnection();
                            try {
                                socket.close();
                            } catch ( IOException ioe ) {
                                if ( log.isDebugEnabled() ) {
                                    log.debug ( "", ioe );
                                }
                            }
                        }
                    } else {
                        countDownConnection();
                        try {
                            socket.close();
                        } catch ( IOException ioe ) {
                            if ( log.isDebugEnabled() ) {
                                log.debug ( "", ioe );
                            }
                        }
                    }
                } catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                    log.error ( sm.getString ( "endpoint.accept.fail" ), t );
                }
            }
            state = AcceptorState.ENDED;
        }
    }
    public static class Nio2SocketWrapper extends SocketWrapperBase<Nio2Channel> {
        private static final ThreadLocal<AtomicInteger> nestedWriteCompletionCount =
        new ThreadLocal<AtomicInteger>() {
            @Override
            protected AtomicInteger initialValue() {
                return new AtomicInteger ( 0 );
            }
        };
        private SendfileData sendfileData = null;
        private final CompletionHandler<Integer, SocketWrapperBase<Nio2Channel>> readCompletionHandler;
        private final Semaphore readPending = new Semaphore ( 1 );
        private boolean readInterest = false;
        private final CompletionHandler<Integer, ByteBuffer> writeCompletionHandler;
        private final CompletionHandler<Long, ByteBuffer[]> gatheringWriteCompletionHandler;
        private final Semaphore writePending = new Semaphore ( 1 );
        private boolean writeInterest = false;
        private boolean writeNotify = false;
        private CompletionHandler<Integer, SocketWrapperBase<Nio2Channel>> awaitBytesHandler
        = new CompletionHandler<Integer, SocketWrapperBase<Nio2Channel>>() {
            @Override
            public void completed ( Integer nBytes, SocketWrapperBase<Nio2Channel> attachment ) {
                if ( nBytes.intValue() < 0 ) {
                    failed ( new ClosedChannelException(), attachment );
                    return;
                }
                getEndpoint().processSocket ( attachment, SocketEvent.OPEN_READ, Nio2Endpoint.isInline() );
            }
            @Override
            public void failed ( Throwable exc, SocketWrapperBase<Nio2Channel> attachment ) {
                getEndpoint().processSocket ( attachment, SocketEvent.DISCONNECT, true );
            }
        };
        private CompletionHandler<Integer, SendfileData> sendfileHandler
        = new CompletionHandler<Integer, SendfileData>() {
            @Override
            public void completed ( Integer nWrite, SendfileData attachment ) {
                if ( nWrite.intValue() < 0 ) {
                    failed ( new EOFException(), attachment );
                    return;
                }
                attachment.pos += nWrite.intValue();
                ByteBuffer buffer = getSocket().getBufHandler().getWriteBuffer();
                if ( !buffer.hasRemaining() ) {
                    if ( attachment.length <= 0 ) {
                        setSendfileData ( null );
                        try {
                            attachment.fchannel.close();
                        } catch ( IOException e ) {
                        }
                        if ( attachment.keepAlive ) {
                            if ( !isInline() ) {
                                awaitBytes();
                            } else {
                                attachment.doneInline = true;
                            }
                        } else {
                            if ( !isInline() ) {
                                getEndpoint().processSocket ( Nio2SocketWrapper.this, SocketEvent.DISCONNECT, false );
                            } else {
                                attachment.doneInline = true;
                            }
                        }
                        return;
                    } else {
                        getSocket().getBufHandler().configureWriteBufferForWrite();
                        int nRead = -1;
                        try {
                            nRead = attachment.fchannel.read ( buffer );
                        } catch ( IOException e ) {
                            failed ( e, attachment );
                            return;
                        }
                        if ( nRead > 0 ) {
                            getSocket().getBufHandler().configureWriteBufferForRead();
                            if ( attachment.length < buffer.remaining() ) {
                                buffer.limit ( buffer.limit() - buffer.remaining() + ( int ) attachment.length );
                            }
                            attachment.length -= nRead;
                        } else {
                            failed ( new EOFException(), attachment );
                            return;
                        }
                    }
                }
                getSocket().write ( buffer, getNio2WriteTimeout(), TimeUnit.MILLISECONDS, attachment, this );
            }
            @Override
            public void failed ( Throwable exc, SendfileData attachment ) {
                try {
                    attachment.fchannel.close();
                } catch ( IOException e ) {
                }
                if ( !isInline() ) {
                    getEndpoint().processSocket ( Nio2SocketWrapper.this, SocketEvent.ERROR, false );
                } else {
                    attachment.doneInline = true;
                    attachment.error = true;
                }
            }
        };
        public Nio2SocketWrapper ( Nio2Channel channel, final Nio2Endpoint endpoint ) {
            super ( channel, endpoint );
            socketBufferHandler = channel.getBufHandler();
            this.readCompletionHandler = new CompletionHandler<Integer, SocketWrapperBase<Nio2Channel>>() {
                @Override
                public void completed ( Integer nBytes, SocketWrapperBase<Nio2Channel> attachment ) {
                    boolean notify = false;
                    if ( log.isDebugEnabled() ) {
                        log.debug ( "Socket: [" + attachment + "], Interest: [" + readInterest + "]" );
                    }
                    synchronized ( readCompletionHandler ) {
                        if ( nBytes.intValue() < 0 ) {
                            failed ( new EOFException(), attachment );
                        } else {
                            if ( readInterest && !Nio2Endpoint.isInline() ) {
                                readInterest = false;
                                notify = true;
                            } else {
                                readPending.release();
                            }
                        }
                    }
                    if ( notify ) {
                        getEndpoint().processSocket ( attachment, SocketEvent.OPEN_READ, false );
                    }
                }
                @Override
                public void failed ( Throwable exc, SocketWrapperBase<Nio2Channel> attachment ) {
                    IOException ioe;
                    if ( exc instanceof IOException ) {
                        ioe = ( IOException ) exc;
                    } else {
                        ioe = new IOException ( exc );
                    }
                    setError ( ioe );
                    if ( exc instanceof AsynchronousCloseException ) {
                        readPending.release();
                        return;
                    }
                    getEndpoint().processSocket ( attachment, SocketEvent.ERROR, true );
                }
            };
            this.writeCompletionHandler = new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed ( Integer nBytes, ByteBuffer attachment ) {
                    writeNotify = false;
                    synchronized ( writeCompletionHandler ) {
                        if ( nBytes.intValue() < 0 ) {
                            failed ( new EOFException ( sm.getString ( "iob.failedwrite" ) ), attachment );
                        } else if ( bufferedWrites.size() > 0 ) {
                            nestedWriteCompletionCount.get().incrementAndGet();
                            ArrayList<ByteBuffer> arrayList = new ArrayList<>();
                            if ( attachment.hasRemaining() ) {
                                arrayList.add ( attachment );
                            }
                            for ( ByteBufferHolder buffer : bufferedWrites ) {
                                buffer.flip();
                                arrayList.add ( buffer.getBuf() );
                            }
                            bufferedWrites.clear();
                            ByteBuffer[] array = arrayList.toArray ( new ByteBuffer[arrayList.size()] );
                            getSocket().write ( array, 0, array.length,
                                                getNio2WriteTimeout(), TimeUnit.MILLISECONDS,
                                                array, gatheringWriteCompletionHandler );
                            nestedWriteCompletionCount.get().decrementAndGet();
                        } else if ( attachment.hasRemaining() ) {
                            nestedWriteCompletionCount.get().incrementAndGet();
                            getSocket().write ( attachment, getNio2WriteTimeout(),
                                                TimeUnit.MILLISECONDS, attachment, writeCompletionHandler );
                            nestedWriteCompletionCount.get().decrementAndGet();
                        } else {
                            if ( writeInterest ) {
                                writeInterest = false;
                                writeNotify = true;
                            }
                            writePending.release();
                        }
                    }
                    if ( writeNotify && nestedWriteCompletionCount.get().get() == 0 ) {
                        endpoint.processSocket ( Nio2SocketWrapper.this, SocketEvent.OPEN_WRITE, Nio2Endpoint.isInline() );
                    }
                }
                @Override
                public void failed ( Throwable exc, ByteBuffer attachment ) {
                    IOException ioe;
                    if ( exc instanceof IOException ) {
                        ioe = ( IOException ) exc;
                    } else {
                        ioe = new IOException ( exc );
                    }
                    setError ( ioe );
                    writePending.release();
                    endpoint.processSocket ( Nio2SocketWrapper.this, SocketEvent.ERROR, true );
                }
            };
            gatheringWriteCompletionHandler = new CompletionHandler<Long, ByteBuffer[]>() {
                @Override
                public void completed ( Long nBytes, ByteBuffer[] attachment ) {
                    writeNotify = false;
                    synchronized ( writeCompletionHandler ) {
                        if ( nBytes.longValue() < 0 ) {
                            failed ( new EOFException ( sm.getString ( "iob.failedwrite" ) ), attachment );
                        } else if ( bufferedWrites.size() > 0 || arrayHasData ( attachment ) ) {
                            nestedWriteCompletionCount.get().incrementAndGet();
                            ArrayList<ByteBuffer> arrayList = new ArrayList<>();
                            for ( ByteBuffer buffer : attachment ) {
                                if ( buffer.hasRemaining() ) {
                                    arrayList.add ( buffer );
                                }
                            }
                            for ( ByteBufferHolder buffer : bufferedWrites ) {
                                buffer.flip();
                                arrayList.add ( buffer.getBuf() );
                            }
                            bufferedWrites.clear();
                            ByteBuffer[] array = arrayList.toArray ( new ByteBuffer[arrayList.size()] );
                            getSocket().write ( array, 0, array.length,
                                                getNio2WriteTimeout(), TimeUnit.MILLISECONDS,
                                                array, gatheringWriteCompletionHandler );
                            nestedWriteCompletionCount.get().decrementAndGet();
                        } else {
                            if ( writeInterest ) {
                                writeInterest = false;
                                writeNotify = true;
                            }
                            writePending.release();
                        }
                    }
                    if ( writeNotify && nestedWriteCompletionCount.get().get() == 0 ) {
                        endpoint.processSocket ( Nio2SocketWrapper.this, SocketEvent.OPEN_WRITE, Nio2Endpoint.isInline() );
                    }
                }
                @Override
                public void failed ( Throwable exc, ByteBuffer[] attachment ) {
                    IOException ioe;
                    if ( exc instanceof IOException ) {
                        ioe = ( IOException ) exc;
                    } else {
                        ioe = new IOException ( exc );
                    }
                    setError ( ioe );
                    writePending.release();
                    endpoint.processSocket ( Nio2SocketWrapper.this, SocketEvent.ERROR, true );
                }
            };
        }
        private static boolean arrayHasData ( ByteBuffer[] byteBuffers ) {
            for ( ByteBuffer byteBuffer : byteBuffers ) {
                if ( byteBuffer.hasRemaining() ) {
                    return true;
                }
            }
            return false;
        }
        public void setSendfileData ( SendfileData sf ) {
            this.sendfileData = sf;
        }
        public SendfileData getSendfileData() {
            return this.sendfileData;
        }
        @Override
        public boolean isReadyForRead() throws IOException {
            synchronized ( readCompletionHandler ) {
                if ( !readPending.tryAcquire() ) {
                    readInterest = true;
                    return false;
                }
                if ( !socketBufferHandler.isReadBufferEmpty() ) {
                    readPending.release();
                    return true;
                }
                int nRead = fillReadBuffer ( false );
                boolean isReady = nRead > 0;
                if ( !isReady ) {
                    readInterest = true;
                }
                return isReady;
            }
        }
        @Override
        public int read ( boolean block, byte[] b, int off, int len ) throws IOException {
            checkError();
            if ( log.isDebugEnabled() ) {
                log.debug ( "Socket: [" + this + "], block: [" + block + "], length: [" + len + "]" );
            }
            if ( socketBufferHandler == null ) {
                throw new IOException ( sm.getString ( "socket.closed" ) );
            }
            if ( block ) {
                try {
                    readPending.acquire();
                } catch ( InterruptedException e ) {
                    throw new IOException ( e );
                }
            } else {
                if ( !readPending.tryAcquire() ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug ( "Socket: [" + this + "], Read in progress. Returning [0]" );
                    }
                    return 0;
                }
            }
            int nRead = populateReadBuffer ( b, off, len );
            if ( nRead > 0 ) {
                readPending.release();
                return nRead;
            }
            synchronized ( readCompletionHandler ) {
                nRead = fillReadBuffer ( block );
                if ( nRead > 0 ) {
                    socketBufferHandler.configureReadBufferForRead();
                    nRead = Math.min ( nRead, len );
                    socketBufferHandler.getReadBuffer().get ( b, off, nRead );
                } else if ( nRead == 0 && !block ) {
                    readInterest = true;
                }
                if ( log.isDebugEnabled() ) {
                    log.debug ( "Socket: [" + this + "], Read: [" + nRead + "]" );
                }
                return nRead;
            }
        }
        @Override
        public int read ( boolean block, ByteBuffer to ) throws IOException {
            checkError();
            if ( socketBufferHandler == null ) {
                throw new IOException ( sm.getString ( "socket.closed" ) );
            }
            if ( block ) {
                try {
                    readPending.acquire();
                } catch ( InterruptedException e ) {
                    throw new IOException ( e );
                }
            } else {
                if ( !readPending.tryAcquire() ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug ( "Socket: [" + this + "], Read in progress. Returning [0]" );
                    }
                    return 0;
                }
            }
            int nRead = populateReadBuffer ( to );
            if ( nRead > 0 ) {
                readPending.release();
                return nRead;
            }
            synchronized ( readCompletionHandler ) {
                int limit = socketBufferHandler.getReadBuffer().capacity();
                if ( block && to.remaining() >= limit ) {
                    to.limit ( to.position() + limit );
                    nRead = fillReadBuffer ( block, to );
                } else {
                    nRead = fillReadBuffer ( block );
                    if ( nRead > 0 ) {
                        nRead = populateReadBuffer ( to );
                    } else if ( nRead == 0 && !block ) {
                        readInterest = true;
                    }
                }
                return nRead;
            }
        }
        @Override
        public void close() throws IOException {
            getSocket().close();
        }
        @Override
        public boolean isClosed() {
            return !getSocket().isOpen();
        }
        @Override
        public boolean hasAsyncIO() {
            return false;
        }
        private static class OperationState<A> {
            private final ByteBuffer[] buffers;
            private final int offset;
            private final int length;
            private final A attachment;
            private final long timeout;
            private final TimeUnit unit;
            private final CompletionCheck check;
            private final CompletionHandler<Long, ? super A> handler;
            private OperationState ( ByteBuffer[] buffers, int offset, int length,
                                     long timeout, TimeUnit unit, A attachment, CompletionCheck check,
                                     CompletionHandler<Long, ? super A> handler ) {
                this.buffers = buffers;
                this.offset = offset;
                this.length = length;
                this.timeout = timeout;
                this.unit = unit;
                this.attachment = attachment;
                this.check = check;
                this.handler = handler;
            }
            private volatile long nBytes = 0;
            private volatile CompletionState state = CompletionState.PENDING;
        }
        private class ScatterReadCompletionHandler<A> implements CompletionHandler<Long, OperationState<A>> {
            @Override
            public void completed ( Long nBytes, OperationState<A> state ) {
                if ( nBytes.intValue() < 0 ) {
                    failed ( new EOFException(), state );
                } else {
                    state.nBytes += nBytes.longValue();
                    CompletionState currentState = Nio2Endpoint.isInline() ? CompletionState.INLINE : CompletionState.DONE;
                    boolean complete = true;
                    boolean completion = true;
                    if ( state.check != null ) {
                        switch ( state.check.callHandler ( currentState, state.buffers, state.offset, state.length ) ) {
                        case CONTINUE:
                            complete = false;
                            break;
                        case DONE:
                            break;
                        case NONE:
                            completion = false;
                            break;
                        }
                    }
                    if ( complete ) {
                        readPending.release();
                        state.state = currentState;
                        if ( completion && state.handler != null ) {
                            state.handler.completed ( Long.valueOf ( state.nBytes ), state.attachment );
                        }
                    } else {
                        getSocket().read ( state.buffers, state.offset, state.length,
                                           state.timeout, state.unit, state, this );
                    }
                }
            }
            @Override
            public void failed ( Throwable exc, OperationState<A> state ) {
                IOException ioe;
                if ( exc instanceof IOException ) {
                    ioe = ( IOException ) exc;
                } else {
                    ioe = new IOException ( exc );
                }
                setError ( ioe );
                readPending.release();
                if ( exc instanceof AsynchronousCloseException ) {
                    return;
                }
                state.state = Nio2Endpoint.isInline() ? CompletionState.ERROR : CompletionState.DONE;
                if ( state.handler != null ) {
                    state.handler.failed ( ioe, state.attachment );
                }
            }
        }
        private class GatherWriteCompletionHandler<A> implements CompletionHandler<Long, OperationState<A>> {
            @Override
            public void completed ( Long nBytes, OperationState<A> state ) {
                if ( nBytes.longValue() < 0 ) {
                    failed ( new EOFException(), state );
                } else {
                    state.nBytes += nBytes.longValue();
                    CompletionState currentState = Nio2Endpoint.isInline() ? CompletionState.INLINE : CompletionState.DONE;
                    boolean complete = true;
                    boolean completion = true;
                    if ( state.check != null ) {
                        switch ( state.check.callHandler ( currentState, state.buffers, state.offset, state.length ) ) {
                        case CONTINUE:
                            complete = false;
                            break;
                        case DONE:
                            break;
                        case NONE:
                            completion = false;
                            break;
                        }
                    }
                    if ( complete ) {
                        writePending.release();
                        state.state = currentState;
                        if ( completion && state.handler != null ) {
                            state.handler.completed ( Long.valueOf ( state.nBytes ), state.attachment );
                        }
                    } else {
                        getSocket().write ( state.buffers, state.offset, state.length,
                                            state.timeout, state.unit, state, this );
                    }
                }
            }
            @Override
            public void failed ( Throwable exc, OperationState<A> state ) {
                IOException ioe;
                if ( exc instanceof IOException ) {
                    ioe = ( IOException ) exc;
                } else {
                    ioe = new IOException ( exc );
                }
                setError ( ioe );
                writePending.release();
                state.state = Nio2Endpoint.isInline() ? CompletionState.ERROR : CompletionState.DONE;
                if ( state.handler != null ) {
                    state.handler.failed ( ioe, state.attachment );
                }
            }
        }
        @Override
        public <A> CompletionState read ( ByteBuffer[] dsts, int offset, int length,
                                          boolean block, long timeout, TimeUnit unit, A attachment,
                                          CompletionCheck check, CompletionHandler<Long, ? super A> handler ) {
            OperationState<A> state = new OperationState<> ( dsts, offset, length, timeout, unit, attachment, check, handler );
            try {
                if ( ( !block && readPending.tryAcquire() ) || ( block && readPending.tryAcquire ( timeout, unit ) ) ) {
                    Nio2Endpoint.startInline();
                    getSocket().read ( dsts, offset, length, timeout, unit, state, new ScatterReadCompletionHandler<>() );
                    Nio2Endpoint.endInline();
                } else {
                    throw new ReadPendingException();
                }
                if ( block && state.state == CompletionState.PENDING && readPending.tryAcquire ( timeout, unit ) ) {
                    readPending.release();
                }
            } catch ( InterruptedException e ) {
                handler.failed ( e, attachment );
            }
            return state.state;
        }
        @Override
        public boolean isWritePending() {
            synchronized ( writeCompletionHandler ) {
                return writePending.availablePermits() == 0;
            }
        }
        @Override
        public <A> CompletionState write ( ByteBuffer[] srcs, int offset, int length,
                                           boolean block, long timeout, TimeUnit unit, A attachment,
                                           CompletionCheck check, CompletionHandler<Long, ? super A> handler ) {
            OperationState<A> state = new OperationState<> ( srcs, offset, length, timeout, unit, attachment, check, handler );
            try {
                if ( ( !block && writePending.tryAcquire() ) || ( block && writePending.tryAcquire ( timeout, unit ) ) ) {
                    Nio2Endpoint.startInline();
                    getSocket().write ( srcs, offset, length, timeout, unit, state, new GatherWriteCompletionHandler<>() );
                    Nio2Endpoint.endInline();
                } else {
                    throw new WritePendingException();
                }
                if ( block && state.state == CompletionState.PENDING && writePending.tryAcquire ( timeout, unit ) ) {
                    writePending.release();
                }
            } catch ( InterruptedException e ) {
                handler.failed ( e, attachment );
            }
            return state.state;
        }
        private int fillReadBuffer ( boolean block ) throws IOException {
            socketBufferHandler.configureReadBufferForWrite();
            return fillReadBuffer ( block, socketBufferHandler.getReadBuffer() );
        }
        private int fillReadBuffer ( boolean block, ByteBuffer to ) throws IOException {
            int nRead = 0;
            Future<Integer> integer = null;
            if ( block ) {
                try {
                    integer = getSocket().read ( to );
                    nRead = integer.get ( getNio2ReadTimeout(), TimeUnit.MILLISECONDS ).intValue();
                } catch ( ExecutionException e ) {
                    if ( e.getCause() instanceof IOException ) {
                        throw ( IOException ) e.getCause();
                    } else {
                        throw new IOException ( e );
                    }
                } catch ( InterruptedException e ) {
                    throw new IOException ( e );
                } catch ( TimeoutException e ) {
                    integer.cancel ( true );
                    throw new SocketTimeoutException();
                } finally {
                    readPending.release();
                }
            } else {
                Nio2Endpoint.startInline();
                getSocket().read ( to, getNio2ReadTimeout(), TimeUnit.MILLISECONDS, this,
                                   readCompletionHandler );
                Nio2Endpoint.endInline();
                if ( readPending.availablePermits() == 1 ) {
                    nRead = to.position();
                }
            }
            return nRead;
        }
        @Override
        protected void writeNonBlocking ( byte[] buf, int off, int len ) throws IOException {
            synchronized ( writeCompletionHandler ) {
                if ( writePending.tryAcquire() ) {
                    socketBufferHandler.configureWriteBufferForWrite();
                    int thisTime = transfer ( buf, off, len, socketBufferHandler.getWriteBuffer() );
                    len = len - thisTime;
                    off = off + thisTime;
                    if ( len > 0 ) {
                        addToBuffers ( buf, off, len );
                    }
                    flushNonBlocking ( true );
                } else {
                    addToBuffers ( buf, off, len );
                }
            }
        }
        @Override
        protected void writeNonBlocking ( ByteBuffer from ) throws IOException {
            synchronized ( writeCompletionHandler ) {
                if ( writePending.tryAcquire() ) {
                    socketBufferHandler.configureWriteBufferForWrite();
                    transfer ( from, socketBufferHandler.getWriteBuffer() );
                    if ( from.remaining() > 0 ) {
                        addToBuffers ( from );
                    }
                    flushNonBlocking ( true );
                } else {
                    addToBuffers ( from );
                }
            }
        }
        @Override
        protected void doWrite ( boolean block, ByteBuffer from ) throws IOException {
            Future<Integer> integer = null;
            try {
                do {
                    integer = getSocket().write ( from );
                    if ( integer.get ( getNio2WriteTimeout(), TimeUnit.MILLISECONDS ).intValue() < 0 ) {
                        throw new EOFException ( sm.getString ( "iob.failedwrite" ) );
                    }
                } while ( from.hasRemaining() );
            } catch ( ExecutionException e ) {
                if ( e.getCause() instanceof IOException ) {
                    throw ( IOException ) e.getCause();
                } else {
                    throw new IOException ( e );
                }
            } catch ( InterruptedException e ) {
                throw new IOException ( e );
            } catch ( TimeoutException e ) {
                integer.cancel ( true );
                throw new SocketTimeoutException();
            }
        }
        @Override
        protected void flushBlocking() throws IOException {
            checkError();
            try {
                if ( writePending.tryAcquire ( getNio2WriteTimeout(), TimeUnit.MILLISECONDS ) ) {
                    writePending.release();
                } else {
                    throw new SocketTimeoutException();
                }
            } catch ( InterruptedException e ) {
            }
            super.flushBlocking();
        }
        @Override
        protected boolean flushNonBlocking() throws IOException {
            return flushNonBlocking ( false );
        }
        private boolean flushNonBlocking ( boolean hasPermit ) throws IOException {
            checkError();
            synchronized ( writeCompletionHandler ) {
                if ( hasPermit || writePending.tryAcquire() ) {
                    socketBufferHandler.configureWriteBufferForRead();
                    if ( bufferedWrites.size() > 0 ) {
                        ArrayList<ByteBuffer> arrayList = new ArrayList<>();
                        if ( socketBufferHandler.getWriteBuffer().hasRemaining() ) {
                            arrayList.add ( socketBufferHandler.getWriteBuffer() );
                        }
                        for ( ByteBufferHolder buffer : bufferedWrites ) {
                            buffer.flip();
                            arrayList.add ( buffer.getBuf() );
                        }
                        bufferedWrites.clear();
                        ByteBuffer[] array = arrayList.toArray ( new ByteBuffer[arrayList.size()] );
                        Nio2Endpoint.startInline();
                        getSocket().write ( array, 0, array.length, getNio2WriteTimeout(),
                                            TimeUnit.MILLISECONDS, array, gatheringWriteCompletionHandler );
                        Nio2Endpoint.endInline();
                    } else if ( socketBufferHandler.getWriteBuffer().hasRemaining() ) {
                        Nio2Endpoint.startInline();
                        getSocket().write ( socketBufferHandler.getWriteBuffer(), getNio2WriteTimeout(),
                                            TimeUnit.MILLISECONDS, socketBufferHandler.getWriteBuffer(),
                                            writeCompletionHandler );
                        Nio2Endpoint.endInline();
                    } else {
                        if ( !hasPermit ) {
                            writePending.release();
                        }
                    }
                }
                return hasDataToWrite();
            }
        }
        @Override
        public boolean hasDataToWrite() {
            synchronized ( writeCompletionHandler ) {
                return !socketBufferHandler.isWriteBufferEmpty() ||
                       bufferedWrites.size() > 0 || getError() != null;
            }
        }
        @Override
        public boolean isReadPending() {
            synchronized ( readCompletionHandler ) {
                return readPending.availablePermits() == 0;
            }
        }
        @Override
        public boolean awaitReadComplete ( long timeout, TimeUnit unit ) {
            try {
                if ( readPending.tryAcquire ( timeout, unit ) ) {
                    readPending.release();
                }
            } catch ( InterruptedException e ) {
                return false;
            }
            return true;
        }
        @Override
        public boolean awaitWriteComplete ( long timeout, TimeUnit unit ) {
            try {
                if ( writePending.tryAcquire ( timeout, unit ) ) {
                    writePending.release();
                }
            } catch ( InterruptedException e ) {
                return false;
            }
            return true;
        }
        void releaseReadPending() {
            synchronized ( readCompletionHandler ) {
                if ( readPending.availablePermits() == 0 ) {
                    readPending.release();
                }
            }
        }
        @Override
        public void registerReadInterest() {
            synchronized ( readCompletionHandler ) {
                if ( readPending.availablePermits() == 0 ) {
                    readInterest = true;
                } else {
                    awaitBytes();
                }
            }
        }
        @Override
        public void registerWriteInterest() {
            synchronized ( writeCompletionHandler ) {
                if ( writePending.availablePermits() == 0 ) {
                    writeInterest = true;
                } else {
                    getEndpoint().processSocket ( this, SocketEvent.OPEN_WRITE, true );
                }
            }
        }
        public void awaitBytes() {
            if ( readPending.tryAcquire() ) {
                getSocket().getBufHandler().configureReadBufferForWrite();
                Nio2Endpoint.startInline();
                getSocket().read ( getSocket().getBufHandler().getReadBuffer(),
                                   getNio2ReadTimeout(), TimeUnit.MILLISECONDS, this, awaitBytesHandler );
                Nio2Endpoint.endInline();
            }
        }
        @Override
        public SendfileDataBase createSendfileData ( String filename, long pos, long length ) {
            return new SendfileData ( filename, pos, length );
        }
        @Override
        public SendfileState processSendfile ( SendfileDataBase sendfileData ) {
            SendfileData data = ( SendfileData ) sendfileData;
            setSendfileData ( data );
            if ( data.fchannel == null || !data.fchannel.isOpen() ) {
                java.nio.file.Path path = new File ( sendfileData.fileName ).toPath();
                try {
                    data.fchannel = java.nio.channels.FileChannel
                                    .open ( path, StandardOpenOption.READ ).position ( sendfileData.pos );
                } catch ( IOException e ) {
                    return SendfileState.ERROR;
                }
            }
            getSocket().getBufHandler().configureWriteBufferForWrite();
            ByteBuffer buffer = getSocket().getBufHandler().getWriteBuffer();
            int nRead = -1;
            try {
                nRead = data.fchannel.read ( buffer );
            } catch ( IOException e1 ) {
                return SendfileState.ERROR;
            }
            if ( nRead >= 0 ) {
                data.length -= nRead;
                getSocket().getBufHandler().configureWriteBufferForRead();
                Nio2Endpoint.startInline();
                getSocket().write ( buffer, getNio2WriteTimeout(), TimeUnit.MILLISECONDS,
                                    data, sendfileHandler );
                Nio2Endpoint.endInline();
                if ( data.doneInline ) {
                    if ( data.error ) {
                        return SendfileState.ERROR;
                    } else {
                        return SendfileState.DONE;
                    }
                } else {
                    return SendfileState.PENDING;
                }
            } else {
                return SendfileState.ERROR;
            }
        }
        private long getNio2ReadTimeout() {
            long readTimeout = getReadTimeout();
            if ( readTimeout > 0 ) {
                return readTimeout;
            }
            return Long.MAX_VALUE;
        }
        private long getNio2WriteTimeout() {
            long writeTimeout = getWriteTimeout();
            if ( writeTimeout > 0 ) {
                return writeTimeout;
            }
            return Long.MAX_VALUE;
        }
        @Override
        protected void populateRemoteAddr() {
            SocketAddress socketAddress = null;
            try {
                socketAddress = getSocket().getIOChannel().getRemoteAddress();
            } catch ( IOException e ) {
            }
            if ( socketAddress instanceof InetSocketAddress ) {
                remoteAddr = ( ( InetSocketAddress ) socketAddress ).getAddress().getHostAddress();
            }
        }
        @Override
        protected void populateRemoteHost() {
            SocketAddress socketAddress = null;
            try {
                socketAddress = getSocket().getIOChannel().getRemoteAddress();
            } catch ( IOException e ) {
                log.warn ( sm.getString ( "endpoint.warn.noRemoteHost", getSocket() ), e );
            }
            if ( socketAddress instanceof InetSocketAddress ) {
                remoteHost = ( ( InetSocketAddress ) socketAddress ).getAddress().getHostName();
                if ( remoteAddr == null ) {
                    remoteAddr = ( ( InetSocketAddress ) socketAddress ).getAddress().getHostAddress();
                }
            }
        }
        @Override
        protected void populateRemotePort() {
            SocketAddress socketAddress = null;
            try {
                socketAddress = getSocket().getIOChannel().getRemoteAddress();
            } catch ( IOException e ) {
                log.warn ( sm.getString ( "endpoint.warn.noRemotePort", getSocket() ), e );
            }
            if ( socketAddress instanceof InetSocketAddress ) {
                remotePort = ( ( InetSocketAddress ) socketAddress ).getPort();
            }
        }
        @Override
        protected void populateLocalName() {
            SocketAddress socketAddress = null;
            try {
                socketAddress = getSocket().getIOChannel().getLocalAddress();
            } catch ( IOException e ) {
                log.warn ( sm.getString ( "endpoint.warn.noLocalName", getSocket() ), e );
            }
            if ( socketAddress instanceof InetSocketAddress ) {
                localName = ( ( InetSocketAddress ) socketAddress ).getHostName();
            }
        }
        @Override
        protected void populateLocalAddr() {
            SocketAddress socketAddress = null;
            try {
                socketAddress = getSocket().getIOChannel().getLocalAddress();
            } catch ( IOException e ) {
                log.warn ( sm.getString ( "endpoint.warn.noLocalAddr", getSocket() ), e );
            }
            if ( socketAddress instanceof InetSocketAddress ) {
                localAddr = ( ( InetSocketAddress ) socketAddress ).getAddress().getHostAddress();
            }
        }
        @Override
        protected void populateLocalPort() {
            SocketAddress socketAddress = null;
            try {
                socketAddress = getSocket().getIOChannel().getLocalAddress();
            } catch ( IOException e ) {
                log.warn ( sm.getString ( "endpoint.warn.noLocalPort", getSocket() ), e );
            }
            if ( socketAddress instanceof InetSocketAddress ) {
                localPort = ( ( InetSocketAddress ) socketAddress ).getPort();
            }
        }
        @Override
        public SSLSupport getSslSupport ( String clientCertProvider ) {
            if ( getSocket() instanceof SecureNio2Channel ) {
                SecureNio2Channel ch = ( SecureNio2Channel ) getSocket();
                SSLSession session = ch.getSslEngine().getSession();
                return ( ( Nio2Endpoint ) getEndpoint() ).getSslImplementation().getSSLSupport ( session );
            } else {
                return null;
            }
        }
        @Override
        public void doClientAuth ( SSLSupport sslSupport ) {
            SecureNio2Channel sslChannel = ( SecureNio2Channel ) getSocket();
            SSLEngine engine = sslChannel.getSslEngine();
            if ( !engine.getNeedClientAuth() ) {
                engine.setNeedClientAuth ( true );
                try {
                    sslChannel.rehandshake();
                    ( ( JSSESupport ) sslSupport ).setSession ( engine.getSession() );
                } catch ( IOException ioe ) {
                    log.warn ( sm.getString ( "socket.sslreneg" ), ioe );
                }
            }
        }
        @Override
        public void setAppReadBufHandler ( ApplicationBufferHandler handler ) {
            getSocket().setAppReadBufHandler ( handler );
        }
    }
    public static void startInline() {
        inlineCompletion.set ( Boolean.TRUE );
    }
    public static void endInline() {
        inlineCompletion.set ( Boolean.FALSE );
    }
    public static boolean isInline() {
        Boolean flag = inlineCompletion.get();
        if ( flag == null ) {
            return false;
        } else {
            return flag.booleanValue();
        }
    }
    protected class SocketProcessor extends SocketProcessorBase<Nio2Channel> {
        public SocketProcessor ( SocketWrapperBase<Nio2Channel> socketWrapper, SocketEvent event ) {
            super ( socketWrapper, event );
        }
        @Override
        protected void doRun() {
            if ( SocketEvent.OPEN_WRITE != event ) {
                ( ( Nio2SocketWrapper ) socketWrapper ).releaseReadPending();
            }
            boolean launch = false;
            try {
                int handshake = -1;
                try {
                    if ( socketWrapper.getSocket().isHandshakeComplete() ) {
                        handshake = 0;
                    } else if ( event == SocketEvent.STOP || event == SocketEvent.DISCONNECT ||
                                event == SocketEvent.ERROR ) {
                        handshake = -1;
                    } else {
                        handshake = socketWrapper.getSocket().handshake();
                        event = SocketEvent.OPEN_READ;
                    }
                } catch ( IOException x ) {
                    handshake = -1;
                    if ( log.isDebugEnabled() ) {
                        log.debug ( sm.getString ( "endpoint.err.handshake" ), x );
                    }
                }
                if ( handshake == 0 ) {
                    SocketState state = SocketState.OPEN;
                    if ( event == null ) {
                        state = getHandler().process ( socketWrapper, SocketEvent.OPEN_READ );
                    } else {
                        state = getHandler().process ( socketWrapper, event );
                    }
                    if ( state == SocketState.CLOSED ) {
                        closeSocket ( socketWrapper );
                        if ( running && !paused ) {
                            if ( !nioChannels.push ( socketWrapper.getSocket() ) ) {
                                socketWrapper.getSocket().free();
                            }
                        }
                    } else if ( state == SocketState.UPGRADING ) {
                        launch = true;
                    }
                } else if ( handshake == -1 ) {
                    closeSocket ( socketWrapper );
                    if ( running && !paused ) {
                        if ( !nioChannels.push ( socketWrapper.getSocket() ) ) {
                            socketWrapper.getSocket().free();
                        }
                    }
                }
            } catch ( VirtualMachineError vme ) {
                ExceptionUtils.handleThrowable ( vme );
            } catch ( Throwable t ) {
                log.error ( sm.getString ( "endpoint.processing.fail" ), t );
                if ( socketWrapper != null ) {
                    closeSocket ( socketWrapper );
                }
            } finally {
                if ( launch ) {
                    try {
                        getExecutor().execute ( new SocketProcessor ( socketWrapper, SocketEvent.OPEN_READ ) );
                    } catch ( NullPointerException npe ) {
                        if ( running ) {
                            log.error ( sm.getString ( "endpoint.launch.fail" ),
                                        npe );
                        }
                    }
                }
                socketWrapper = null;
                event = null;
                if ( running && !paused ) {
                    processorCache.push ( this );
                }
            }
        }
    }
    public static class SendfileData extends SendfileDataBase {
        private FileChannel fchannel;
        private boolean doneInline = false;
        private boolean error = false;
        public SendfileData ( String filename, long pos, long length ) {
            super ( filename, pos, length );
        }
    }
}
