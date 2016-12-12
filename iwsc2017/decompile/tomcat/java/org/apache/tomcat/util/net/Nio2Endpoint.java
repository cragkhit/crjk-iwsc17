package org.apache.tomcat.util.net;
import javax.net.ssl.SSLEngine;
import org.apache.tomcat.util.net.jsse.JSSESupport;
import javax.net.ssl.SSLSession;
import java.nio.file.Path;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.nio.file.OpenOption;
import java.io.File;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutionException;
import java.nio.channels.WritePendingException;
import java.nio.channels.ReadPendingException;
import org.apache.tomcat.util.buf.ByteBufferHolder;
import java.util.ArrayList;
import java.nio.channels.AsynchronousCloseException;
import java.io.EOFException;
import java.nio.channels.ClosedChannelException;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.juli.logging.LogFactory;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.TimeUnit;
import java.util.Iterator;
import org.apache.tomcat.util.ExceptionUtils;
import java.util.concurrent.ExecutorService;
import java.net.SocketAddress;
import java.io.IOException;
import java.net.InetSocketAddress;
import org.apache.tomcat.util.collections.SynchronizedStack;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import org.apache.juli.logging.Log;
public class Nio2Endpoint extends AbstractJsseEndpoint<Nio2Channel> {
    private static final Log log;
    private AsynchronousServerSocketChannel serverSock;
    private static ThreadLocal<Boolean> inlineCompletion;
    private AsynchronousChannelGroup threadGroup;
    private volatile boolean allClosed;
    private SynchronizedStack<Nio2Channel> nioChannels;
    public Nio2Endpoint() {
        this.serverSock = null;
        this.threadGroup = null;
        this.setMaxConnections ( -1 );
    }
    public void setSocketProperties ( final SocketProperties socketProperties ) {
        this.socketProperties = socketProperties;
    }
    public boolean getDeferAccept() {
        return false;
    }
    @Override
    public int getLocalPort() {
        final AsynchronousServerSocketChannel ssc = this.serverSock;
        if ( ssc == null ) {
            return -1;
        }
        try {
            final SocketAddress sa = ssc.getLocalAddress();
            if ( sa instanceof InetSocketAddress ) {
                return ( ( InetSocketAddress ) sa ).getPort();
            }
            return -1;
        } catch ( IOException e ) {
            return -1;
        }
    }
    public int getKeepAliveCount() {
        return -1;
    }
    @Override
    public void bind() throws Exception {
        if ( this.getExecutor() == null ) {
            this.createExecutor();
        }
        if ( this.getExecutor() instanceof ExecutorService ) {
            this.threadGroup = AsynchronousChannelGroup.withThreadPool ( ( ExecutorService ) this.getExecutor() );
        }
        if ( !this.internalExecutor ) {
            Nio2Endpoint.log.warn ( Nio2Endpoint.sm.getString ( "endpoint.nio2.exclusiveExecutor" ) );
        }
        this.serverSock = AsynchronousServerSocketChannel.open ( this.threadGroup );
        this.socketProperties.setProperties ( this.serverSock );
        final InetSocketAddress addr = ( this.getAddress() != null ) ? new InetSocketAddress ( this.getAddress(), this.getPort() ) : new InetSocketAddress ( this.getPort() );
        this.serverSock.bind ( addr, this.getAcceptCount() );
        if ( this.acceptorThreadCount != 1 ) {
            this.acceptorThreadCount = 1;
        }
        this.initialiseSsl();
    }
    @Override
    public void startInternal() throws Exception {
        if ( !this.running ) {
            this.allClosed = false;
            this.running = true;
            this.paused = false;
            this.processorCache = new SynchronizedStack<SocketProcessorBase<S>> ( 128, this.socketProperties.getProcessorCache() );
            this.nioChannels = new SynchronizedStack<Nio2Channel> ( 128, this.socketProperties.getBufferPool() );
            if ( this.getExecutor() == null ) {
                this.createExecutor();
            }
            this.initializeConnectionLatch();
            this.startAcceptorThreads();
        }
    }
    @Override
    public void stopInternal() {
        this.releaseConnectionLatch();
        if ( !this.paused ) {
            this.pause();
        }
        if ( this.running ) {
            this.running = false;
            this.unlockAccept();
            this.getExecutor().execute ( new Runnable() {
                @Override
                public void run() {
                    try {
                        for ( final Nio2Channel channel : Nio2Endpoint.this.getHandler().getOpenSockets() ) {
                            Nio2Endpoint.this.closeSocket ( channel.getSocket() );
                        }
                    } catch ( Throwable t ) {
                        ExceptionUtils.handleThrowable ( t );
                    } finally {
                        Nio2Endpoint.this.allClosed = true;
                    }
                }
            } );
            this.nioChannels.clear();
            this.processorCache.clear();
        }
    }
    @Override
    public void unbind() throws Exception {
        if ( this.running ) {
            this.stop();
        }
        this.serverSock.close();
        this.serverSock = null;
        this.destroySsl();
        super.unbind();
        this.shutdownExecutor();
        if ( this.getHandler() != null ) {
            this.getHandler().recycle();
        }
    }
    @Override
    public void shutdownExecutor() {
        if ( this.threadGroup != null && this.internalExecutor ) {
            try {
                long timeout = this.getExecutorTerminationTimeoutMillis();
                while ( timeout > 0L && !this.allClosed ) {
                    timeout -= 100L;
                    Thread.sleep ( 100L );
                }
                this.threadGroup.shutdownNow();
                if ( timeout > 0L ) {
                    this.threadGroup.awaitTermination ( timeout, TimeUnit.MILLISECONDS );
                }
            } catch ( IOException e ) {
                this.getLog().warn ( Nio2Endpoint.sm.getString ( "endpoint.warn.executorShutdown", this.getName() ), e );
            } catch ( InterruptedException ex ) {}
            if ( !this.threadGroup.isTerminated() ) {
                this.getLog().warn ( Nio2Endpoint.sm.getString ( "endpoint.warn.executorShutdown", this.getName() ) );
            }
            this.threadGroup = null;
        }
        super.shutdownExecutor();
    }
    public int getWriteBufSize() {
        return this.socketProperties.getTxBufSize();
    }
    public int getReadBufSize() {
        return this.socketProperties.getRxBufSize();
    }
    @Override
    protected AbstractEndpoint.Acceptor createAcceptor() {
        return new Acceptor();
    }
    protected boolean setSocketOptions ( final AsynchronousSocketChannel socket ) {
        try {
            this.socketProperties.setProperties ( socket );
            Nio2Channel channel = this.nioChannels.pop();
            if ( channel == null ) {
                final SocketBufferHandler bufhandler = new SocketBufferHandler ( this.socketProperties.getAppReadBufSize(), this.socketProperties.getAppWriteBufSize(), this.socketProperties.getDirectBuffer() );
                if ( this.isSSLEnabled() ) {
                    channel = new SecureNio2Channel ( bufhandler, this );
                } else {
                    channel = new Nio2Channel ( bufhandler );
                }
            }
            final Nio2SocketWrapper socketWrapper = new Nio2SocketWrapper ( channel, this );
            channel.reset ( socket, socketWrapper );
            socketWrapper.setReadTimeout ( this.getSocketProperties().getSoTimeout() );
            socketWrapper.setWriteTimeout ( this.getSocketProperties().getSoTimeout() );
            socketWrapper.setKeepAliveLeft ( this.getMaxKeepAliveRequests() );
            socketWrapper.setSecure ( this.isSSLEnabled() );
            socketWrapper.setReadTimeout ( this.getConnectionTimeout() );
            socketWrapper.setWriteTimeout ( this.getConnectionTimeout() );
            return this.processSocket ( socketWrapper, SocketEvent.OPEN_READ, true );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            Nio2Endpoint.log.error ( "", t );
            return false;
        }
    }
    @Override
    protected SocketProcessorBase<Nio2Channel> createSocketProcessor ( final SocketWrapperBase<Nio2Channel> socketWrapper, final SocketEvent event ) {
        return new SocketProcessor ( socketWrapper, event );
    }
    public void closeSocket ( final SocketWrapperBase<Nio2Channel> socket ) {
        if ( Nio2Endpoint.log.isDebugEnabled() ) {
            Nio2Endpoint.log.debug ( "Calling [" + this + "].closeSocket([" + socket + "],[" + socket.getSocket() + "])", new Exception() );
        }
        if ( socket == null ) {
            return;
        }
        try {
            this.getHandler().release ( socket );
        } catch ( Throwable e ) {
            ExceptionUtils.handleThrowable ( e );
            if ( Nio2Endpoint.log.isDebugEnabled() ) {
                Nio2Endpoint.log.error ( "", e );
            }
        }
        try {
            synchronized ( socket.getSocket() ) {
                if ( socket.getSocket().isOpen() ) {
                    this.countDownConnection();
                    socket.getSocket().close ( true );
                }
            }
        } catch ( Throwable e ) {
            ExceptionUtils.handleThrowable ( e );
            if ( Nio2Endpoint.log.isDebugEnabled() ) {
                Nio2Endpoint.log.error ( "", e );
            }
        }
        try {
            final Nio2SocketWrapper nio2Socket = ( Nio2SocketWrapper ) socket;
            if ( nio2Socket.getSendfileData() != null && nio2Socket.getSendfileData().fchannel != null && nio2Socket.getSendfileData().fchannel.isOpen() ) {
                nio2Socket.getSendfileData().fchannel.close();
            }
        } catch ( Throwable e ) {
            ExceptionUtils.handleThrowable ( e );
            if ( Nio2Endpoint.log.isDebugEnabled() ) {
                Nio2Endpoint.log.error ( "", e );
            }
        }
    }
    @Override
    protected Log getLog() {
        return Nio2Endpoint.log;
    }
    public static void startInline() {
        Nio2Endpoint.inlineCompletion.set ( Boolean.TRUE );
    }
    public static void endInline() {
        Nio2Endpoint.inlineCompletion.set ( Boolean.FALSE );
    }
    public static boolean isInline() {
        final Boolean flag = Nio2Endpoint.inlineCompletion.get();
        return flag != null && flag;
    }
    static {
        log = LogFactory.getLog ( Nio2Endpoint.class );
        Nio2Endpoint.inlineCompletion = new ThreadLocal<Boolean>();
    }
    protected class Acceptor extends AbstractEndpoint.Acceptor {
        @Override
        public void run() {
            int errorDelay = 0;
            while ( Nio2Endpoint.this.running ) {
                while ( Nio2Endpoint.this.paused && Nio2Endpoint.this.running ) {
                    this.state = AcceptorState.PAUSED;
                    try {
                        Thread.sleep ( 50L );
                    } catch ( InterruptedException ex ) {}
                }
                if ( !Nio2Endpoint.this.running ) {
                    break;
                }
                this.state = AcceptorState.RUNNING;
                try {
                    Nio2Endpoint.this.countUpOrAwaitConnection();
                    AsynchronousSocketChannel socket = null;
                    try {
                        socket = Nio2Endpoint.this.serverSock.accept().get();
                    } catch ( Exception e ) {
                        Nio2Endpoint.this.countDownConnection();
                        if ( Nio2Endpoint.this.running ) {
                            errorDelay = Nio2Endpoint.this.handleExceptionWithDelay ( errorDelay );
                            throw e;
                        }
                        break;
                    }
                    errorDelay = 0;
                    if ( Nio2Endpoint.this.running && !Nio2Endpoint.this.paused ) {
                        if ( Nio2Endpoint.this.setSocketOptions ( socket ) ) {
                            continue;
                        }
                        Nio2Endpoint.this.countDownConnection();
                        try {
                            socket.close();
                        } catch ( IOException ioe ) {
                            if ( !Nio2Endpoint.log.isDebugEnabled() ) {
                                continue;
                            }
                            Nio2Endpoint.log.debug ( "", ioe );
                        }
                    } else {
                        Nio2Endpoint.this.countDownConnection();
                        try {
                            socket.close();
                        } catch ( IOException ioe ) {
                            if ( !Nio2Endpoint.log.isDebugEnabled() ) {
                                continue;
                            }
                            Nio2Endpoint.log.debug ( "", ioe );
                        }
                    }
                } catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                    Nio2Endpoint.log.error ( AbstractEndpoint.sm.getString ( "endpoint.accept.fail" ), t );
                }
            }
            this.state = AcceptorState.ENDED;
        }
    }
    public static class Nio2SocketWrapper extends SocketWrapperBase<Nio2Channel> {
        private static final ThreadLocal<AtomicInteger> nestedWriteCompletionCount;
        private SendfileData sendfileData;
        private final CompletionHandler<Integer, SocketWrapperBase<Nio2Channel>> readCompletionHandler;
        private final Semaphore readPending;
        private boolean readInterest;
        private final CompletionHandler<Integer, ByteBuffer> writeCompletionHandler;
        private final CompletionHandler<Long, ByteBuffer[]> gatheringWriteCompletionHandler;
        private final Semaphore writePending;
        private boolean writeInterest;
        private boolean writeNotify;
        private CompletionHandler<Integer, SocketWrapperBase<Nio2Channel>> awaitBytesHandler;
        private CompletionHandler<Integer, SendfileData> sendfileHandler;
        public Nio2SocketWrapper ( final Nio2Channel channel, final Nio2Endpoint endpoint ) {
            super ( channel, endpoint );
            this.sendfileData = null;
            this.readPending = new Semaphore ( 1 );
            this.readInterest = false;
            this.writePending = new Semaphore ( 1 );
            this.writeInterest = false;
            this.writeNotify = false;
            this.awaitBytesHandler = new CompletionHandler<Integer, SocketWrapperBase<Nio2Channel>>() {
                @Override
                public void completed ( final Integer nBytes, final SocketWrapperBase<Nio2Channel> attachment ) {
                    if ( nBytes < 0 ) {
                        this.failed ( ( Throwable ) new ClosedChannelException(), attachment );
                        return;
                    }
                    Nio2SocketWrapper.this.getEndpoint().processSocket ( attachment, SocketEvent.OPEN_READ, Nio2Endpoint.isInline() );
                }
                @Override
                public void failed ( final Throwable exc, final SocketWrapperBase<Nio2Channel> attachment ) {
                    Nio2SocketWrapper.this.getEndpoint().processSocket ( attachment, SocketEvent.DISCONNECT, true );
                }
            };
            this.sendfileHandler = new CompletionHandler<Integer, SendfileData>() {
                @Override
                public void completed ( final Integer nWrite, final SendfileData attachment ) {
                    if ( nWrite < 0 ) {
                        this.failed ( ( Throwable ) new EOFException(), attachment );
                        return;
                    }
                    attachment.pos += nWrite;
                    final ByteBuffer buffer = Nio2SocketWrapper.this.getSocket().getBufHandler().getWriteBuffer();
                    if ( !buffer.hasRemaining() ) {
                        if ( attachment.length <= 0L ) {
                            Nio2SocketWrapper.this.setSendfileData ( null );
                            try {
                                attachment.fchannel.close();
                            } catch ( IOException ex ) {}
                            if ( attachment.keepAlive ) {
                                if ( !Nio2Endpoint.isInline() ) {
                                    Nio2SocketWrapper.this.awaitBytes();
                                } else {
                                    attachment.doneInline = true;
                                }
                            } else if ( !Nio2Endpoint.isInline() ) {
                                Nio2SocketWrapper.this.getEndpoint().processSocket ( Nio2SocketWrapper.this, SocketEvent.DISCONNECT, false );
                            } else {
                                attachment.doneInline = true;
                            }
                            return;
                        }
                        Nio2SocketWrapper.this.getSocket().getBufHandler().configureWriteBufferForWrite();
                        int nRead = -1;
                        try {
                            nRead = attachment.fchannel.read ( buffer );
                        } catch ( IOException e ) {
                            this.failed ( ( Throwable ) e, attachment );
                            return;
                        }
                        if ( nRead <= 0 ) {
                            this.failed ( ( Throwable ) new EOFException(), attachment );
                            return;
                        }
                        Nio2SocketWrapper.this.getSocket().getBufHandler().configureWriteBufferForRead();
                        if ( attachment.length < buffer.remaining() ) {
                            buffer.limit ( buffer.limit() - buffer.remaining() + ( int ) attachment.length );
                        }
                        attachment.length -= nRead;
                    }
                    Nio2SocketWrapper.this.getSocket().write ( buffer, Nio2SocketWrapper.this.getNio2WriteTimeout(), TimeUnit.MILLISECONDS, attachment, this );
                }
                @Override
                public void failed ( final Throwable exc, final SendfileData attachment ) {
                    try {
                        attachment.fchannel.close();
                    } catch ( IOException ex ) {}
                    if ( !Nio2Endpoint.isInline() ) {
                        Nio2SocketWrapper.this.getEndpoint().processSocket ( Nio2SocketWrapper.this, SocketEvent.ERROR, false );
                    } else {
                        attachment.doneInline = true;
                        attachment.error = true;
                    }
                }
            };
            this.socketBufferHandler = channel.getBufHandler();
            this.readCompletionHandler = new CompletionHandler<Integer, SocketWrapperBase<Nio2Channel>>() {
                @Override
                public void completed ( final Integer nBytes, final SocketWrapperBase<Nio2Channel> attachment ) {
                    boolean notify = false;
                    if ( Nio2Endpoint.log.isDebugEnabled() ) {
                        Nio2Endpoint.log.debug ( "Socket: [" + attachment + "], Interest: [" + Nio2SocketWrapper.this.readInterest + "]" );
                    }
                    synchronized ( Nio2SocketWrapper.this.readCompletionHandler ) {
                        if ( nBytes < 0 ) {
                            this.failed ( ( Throwable ) new EOFException(), attachment );
                        } else if ( Nio2SocketWrapper.this.readInterest && !Nio2Endpoint.isInline() ) {
                            Nio2SocketWrapper.this.readInterest = false;
                            notify = true;
                        } else {
                            Nio2SocketWrapper.this.readPending.release();
                        }
                    }
                    if ( notify ) {
                        Nio2SocketWrapper.this.getEndpoint().processSocket ( attachment, SocketEvent.OPEN_READ, false );
                    }
                }
                @Override
                public void failed ( final Throwable exc, final SocketWrapperBase<Nio2Channel> attachment ) {
                    IOException ioe;
                    if ( exc instanceof IOException ) {
                        ioe = ( IOException ) exc;
                    } else {
                        ioe = new IOException ( exc );
                    }
                    Nio2SocketWrapper.this.setError ( ioe );
                    if ( exc instanceof AsynchronousCloseException ) {
                        Nio2SocketWrapper.this.readPending.release();
                        return;
                    }
                    Nio2SocketWrapper.this.getEndpoint().processSocket ( attachment, SocketEvent.ERROR, true );
                }
            };
            this.writeCompletionHandler = new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed ( final Integer nBytes, final ByteBuffer attachment ) {
                    Nio2SocketWrapper.this.writeNotify = false;
                    synchronized ( Nio2SocketWrapper.this.writeCompletionHandler ) {
                        if ( nBytes < 0 ) {
                            this.failed ( ( Throwable ) new EOFException ( SocketWrapperBase.sm.getString ( "iob.failedwrite" ) ), attachment );
                        } else if ( Nio2SocketWrapper.this.bufferedWrites.size() > 0 ) {
                            Nio2SocketWrapper.nestedWriteCompletionCount.get().incrementAndGet();
                            final ArrayList<ByteBuffer> arrayList = new ArrayList<ByteBuffer>();
                            if ( attachment.hasRemaining() ) {
                                arrayList.add ( attachment );
                            }
                            for ( final ByteBufferHolder buffer : Nio2SocketWrapper.this.bufferedWrites ) {
                                buffer.flip();
                                arrayList.add ( buffer.getBuf() );
                            }
                            Nio2SocketWrapper.this.bufferedWrites.clear();
                            final ByteBuffer[] array = arrayList.toArray ( new ByteBuffer[arrayList.size()] );
                            Nio2SocketWrapper.this.getSocket().write ( array, 0, array.length, Nio2SocketWrapper.this.getNio2WriteTimeout(), TimeUnit.MILLISECONDS, array, Nio2SocketWrapper.this.gatheringWriteCompletionHandler );
                            Nio2SocketWrapper.nestedWriteCompletionCount.get().decrementAndGet();
                        } else if ( attachment.hasRemaining() ) {
                            Nio2SocketWrapper.nestedWriteCompletionCount.get().incrementAndGet();
                            Nio2SocketWrapper.this.getSocket().write ( attachment, Nio2SocketWrapper.this.getNio2WriteTimeout(), TimeUnit.MILLISECONDS, attachment, Nio2SocketWrapper.this.writeCompletionHandler );
                            Nio2SocketWrapper.nestedWriteCompletionCount.get().decrementAndGet();
                        } else {
                            if ( Nio2SocketWrapper.this.writeInterest ) {
                                Nio2SocketWrapper.this.writeInterest = false;
                                Nio2SocketWrapper.this.writeNotify = true;
                            }
                            Nio2SocketWrapper.this.writePending.release();
                        }
                    }
                    if ( Nio2SocketWrapper.this.writeNotify && Nio2SocketWrapper.nestedWriteCompletionCount.get().get() == 0 ) {
                        endpoint.processSocket ( Nio2SocketWrapper.this, SocketEvent.OPEN_WRITE, Nio2Endpoint.isInline() );
                    }
                }
                @Override
                public void failed ( final Throwable exc, final ByteBuffer attachment ) {
                    IOException ioe;
                    if ( exc instanceof IOException ) {
                        ioe = ( IOException ) exc;
                    } else {
                        ioe = new IOException ( exc );
                    }
                    Nio2SocketWrapper.this.setError ( ioe );
                    Nio2SocketWrapper.this.writePending.release();
                    endpoint.processSocket ( Nio2SocketWrapper.this, SocketEvent.ERROR, true );
                }
            };
            this.gatheringWriteCompletionHandler = new CompletionHandler<Long, ByteBuffer[]>() {
                @Override
                public void completed ( final Long nBytes, final ByteBuffer[] attachment ) {
                    Nio2SocketWrapper.this.writeNotify = false;
                    synchronized ( Nio2SocketWrapper.this.writeCompletionHandler ) {
                        if ( nBytes < 0L ) {
                            this.failed ( ( Throwable ) new EOFException ( SocketWrapperBase.sm.getString ( "iob.failedwrite" ) ), attachment );
                        } else if ( Nio2SocketWrapper.this.bufferedWrites.size() > 0 || arrayHasData ( attachment ) ) {
                            Nio2SocketWrapper.nestedWriteCompletionCount.get().incrementAndGet();
                            final ArrayList<ByteBuffer> arrayList = new ArrayList<ByteBuffer>();
                            for ( final ByteBuffer buffer : attachment ) {
                                if ( buffer.hasRemaining() ) {
                                    arrayList.add ( buffer );
                                }
                            }
                            for ( final ByteBufferHolder buffer2 : Nio2SocketWrapper.this.bufferedWrites ) {
                                buffer2.flip();
                                arrayList.add ( buffer2.getBuf() );
                            }
                            Nio2SocketWrapper.this.bufferedWrites.clear();
                            final ByteBuffer[] array = arrayList.toArray ( new ByteBuffer[arrayList.size()] );
                            Nio2SocketWrapper.this.getSocket().write ( array, 0, array.length, Nio2SocketWrapper.this.getNio2WriteTimeout(), TimeUnit.MILLISECONDS, array, Nio2SocketWrapper.this.gatheringWriteCompletionHandler );
                            Nio2SocketWrapper.nestedWriteCompletionCount.get().decrementAndGet();
                        } else {
                            if ( Nio2SocketWrapper.this.writeInterest ) {
                                Nio2SocketWrapper.this.writeInterest = false;
                                Nio2SocketWrapper.this.writeNotify = true;
                            }
                            Nio2SocketWrapper.this.writePending.release();
                        }
                    }
                    if ( Nio2SocketWrapper.this.writeNotify && Nio2SocketWrapper.nestedWriteCompletionCount.get().get() == 0 ) {
                        endpoint.processSocket ( Nio2SocketWrapper.this, SocketEvent.OPEN_WRITE, Nio2Endpoint.isInline() );
                    }
                }
                @Override
                public void failed ( final Throwable exc, final ByteBuffer[] attachment ) {
                    IOException ioe;
                    if ( exc instanceof IOException ) {
                        ioe = ( IOException ) exc;
                    } else {
                        ioe = new IOException ( exc );
                    }
                    Nio2SocketWrapper.this.setError ( ioe );
                    Nio2SocketWrapper.this.writePending.release();
                    endpoint.processSocket ( Nio2SocketWrapper.this, SocketEvent.ERROR, true );
                }
            };
        }
        private static boolean arrayHasData ( final ByteBuffer[] byteBuffers ) {
            for ( final ByteBuffer byteBuffer : byteBuffers ) {
                if ( byteBuffer.hasRemaining() ) {
                    return true;
                }
            }
            return false;
        }
        public void setSendfileData ( final SendfileData sf ) {
            this.sendfileData = sf;
        }
        public SendfileData getSendfileData() {
            return this.sendfileData;
        }
        @Override
        public boolean isReadyForRead() throws IOException {
            synchronized ( this.readCompletionHandler ) {
                if ( !this.readPending.tryAcquire() ) {
                    this.readInterest = true;
                    return false;
                }
                if ( !this.socketBufferHandler.isReadBufferEmpty() ) {
                    this.readPending.release();
                    return true;
                }
                final int nRead = this.fillReadBuffer ( false );
                final boolean isReady = nRead > 0;
                if ( !isReady ) {
                    this.readInterest = true;
                }
                return isReady;
            }
        }
        @Override
        public int read ( final boolean block, final byte[] b, final int off, final int len ) throws IOException {
            this.checkError();
            if ( Nio2Endpoint.log.isDebugEnabled() ) {
                Nio2Endpoint.log.debug ( "Socket: [" + this + "], block: [" + block + "], length: [" + len + "]" );
            }
            if ( this.socketBufferHandler == null ) {
                throw new IOException ( Nio2SocketWrapper.sm.getString ( "socket.closed" ) );
            }
            Label_0170: {
                if ( block ) {
                    try {
                        this.readPending.acquire();
                        break Label_0170;
                    } catch ( InterruptedException e ) {
                        throw new IOException ( e );
                    }
                }
                if ( !this.readPending.tryAcquire() ) {
                    if ( Nio2Endpoint.log.isDebugEnabled() ) {
                        Nio2Endpoint.log.debug ( "Socket: [" + this + "], Read in progress. Returning [0]" );
                    }
                    return 0;
                }
            }
            int nRead = this.populateReadBuffer ( b, off, len );
            if ( nRead > 0 ) {
                this.readPending.release();
                return nRead;
            }
            synchronized ( this.readCompletionHandler ) {
                nRead = this.fillReadBuffer ( block );
                if ( nRead > 0 ) {
                    this.socketBufferHandler.configureReadBufferForRead();
                    nRead = Math.min ( nRead, len );
                    this.socketBufferHandler.getReadBuffer().get ( b, off, nRead );
                } else if ( nRead == 0 && !block ) {
                    this.readInterest = true;
                }
                if ( Nio2Endpoint.log.isDebugEnabled() ) {
                    Nio2Endpoint.log.debug ( "Socket: [" + this + "], Read: [" + nRead + "]" );
                }
                return nRead;
            }
        }
        @Override
        public int read ( final boolean block, final ByteBuffer to ) throws IOException {
            this.checkError();
            if ( this.socketBufferHandler == null ) {
                throw new IOException ( Nio2SocketWrapper.sm.getString ( "socket.closed" ) );
            }
            Label_0106: {
                if ( block ) {
                    try {
                        this.readPending.acquire();
                        break Label_0106;
                    } catch ( InterruptedException e ) {
                        throw new IOException ( e );
                    }
                }
                if ( !this.readPending.tryAcquire() ) {
                    if ( Nio2Endpoint.log.isDebugEnabled() ) {
                        Nio2Endpoint.log.debug ( "Socket: [" + this + "], Read in progress. Returning [0]" );
                    }
                    return 0;
                }
            }
            int nRead = this.populateReadBuffer ( to );
            if ( nRead > 0 ) {
                this.readPending.release();
                return nRead;
            }
            synchronized ( this.readCompletionHandler ) {
                final int limit = this.socketBufferHandler.getReadBuffer().capacity();
                if ( block && to.remaining() >= limit ) {
                    to.limit ( to.position() + limit );
                    nRead = this.fillReadBuffer ( block, to );
                } else {
                    nRead = this.fillReadBuffer ( block );
                    if ( nRead > 0 ) {
                        nRead = this.populateReadBuffer ( to );
                    } else if ( nRead == 0 && !block ) {
                        this.readInterest = true;
                    }
                }
                return nRead;
            }
        }
        @Override
        public void close() throws IOException {
            this.getSocket().close();
        }
        @Override
        public boolean isClosed() {
            return !this.getSocket().isOpen();
        }
        @Override
        public boolean hasAsyncIO() {
            return false;
        }
        @Override
        public <A> CompletionState read ( final ByteBuffer[] dsts, final int offset, final int length, final boolean block, final long timeout, final TimeUnit unit, final A attachment, final CompletionCheck check, final CompletionHandler<Long, ? super A> handler ) {
            final OperationState<A> state = new OperationState<A> ( dsts, offset, length, timeout, unit, ( Object ) attachment, check, ( CompletionHandler ) handler );
            try {
                if ( ( block || !this.readPending.tryAcquire() ) && ( !block || !this.readPending.tryAcquire ( timeout, unit ) ) ) {
                    throw new ReadPendingException();
                }
                Nio2Endpoint.startInline();
                this.getSocket().read ( dsts, offset, length, timeout, unit, state, ( CompletionHandler<Long, ? super OperationState<A>> ) new ScatterReadCompletionHandler() );
                Nio2Endpoint.endInline();
                if ( block && ( ( OperationState<Object> ) state ).state == CompletionState.PENDING && this.readPending.tryAcquire ( timeout, unit ) ) {
                    this.readPending.release();
                }
            } catch ( InterruptedException e ) {
                handler.failed ( ( Throwable ) e, ( Object ) attachment );
            }
            return ( ( OperationState<Object> ) state ).state;
        }
        @Override
        public boolean isWritePending() {
            synchronized ( this.writeCompletionHandler ) {
                return this.writePending.availablePermits() == 0;
            }
        }
        @Override
        public <A> CompletionState write ( final ByteBuffer[] srcs, final int offset, final int length, final boolean block, final long timeout, final TimeUnit unit, final A attachment, final CompletionCheck check, final CompletionHandler<Long, ? super A> handler ) {
            final OperationState<A> state = new OperationState<A> ( srcs, offset, length, timeout, unit, ( Object ) attachment, check, ( CompletionHandler ) handler );
            try {
                if ( ( block || !this.writePending.tryAcquire() ) && ( !block || !this.writePending.tryAcquire ( timeout, unit ) ) ) {
                    throw new WritePendingException();
                }
                Nio2Endpoint.startInline();
                this.getSocket().write ( srcs, offset, length, timeout, unit, state, ( CompletionHandler<Long, ? super OperationState<A>> ) new GatherWriteCompletionHandler() );
                Nio2Endpoint.endInline();
                if ( block && ( ( OperationState<Object> ) state ).state == CompletionState.PENDING && this.writePending.tryAcquire ( timeout, unit ) ) {
                    this.writePending.release();
                }
            } catch ( InterruptedException e ) {
                handler.failed ( ( Throwable ) e, ( Object ) attachment );
            }
            return ( ( OperationState<Object> ) state ).state;
        }
        private int fillReadBuffer ( final boolean block ) throws IOException {
            this.socketBufferHandler.configureReadBufferForWrite();
            return this.fillReadBuffer ( block, this.socketBufferHandler.getReadBuffer() );
        }
        private int fillReadBuffer ( final boolean block, final ByteBuffer to ) throws IOException {
            int nRead = 0;
            Future<Integer> integer = null;
            if ( block ) {
                try {
                    integer = this.getSocket().read ( to );
                    nRead = integer.get ( this.getNio2ReadTimeout(), TimeUnit.MILLISECONDS );
                } catch ( ExecutionException e ) {
                    if ( e.getCause() instanceof IOException ) {
                        throw ( IOException ) e.getCause();
                    }
                    throw new IOException ( e );
                } catch ( InterruptedException e2 ) {
                    throw new IOException ( e2 );
                } catch ( TimeoutException e3 ) {
                    integer.cancel ( true );
                    throw new SocketTimeoutException();
                } finally {
                    this.readPending.release();
                }
            } else {
                Nio2Endpoint.startInline();
                this.getSocket().read ( to, this.getNio2ReadTimeout(), TimeUnit.MILLISECONDS, this, this.readCompletionHandler );
                Nio2Endpoint.endInline();
                if ( this.readPending.availablePermits() == 1 ) {
                    nRead = to.position();
                }
            }
            return nRead;
        }
        @Override
        protected void writeNonBlocking ( final byte[] buf, int off, int len ) throws IOException {
            synchronized ( this.writeCompletionHandler ) {
                if ( this.writePending.tryAcquire() ) {
                    this.socketBufferHandler.configureWriteBufferForWrite();
                    final int thisTime = SocketWrapperBase.transfer ( buf, off, len, this.socketBufferHandler.getWriteBuffer() );
                    len -= thisTime;
                    off += thisTime;
                    if ( len > 0 ) {
                        this.addToBuffers ( buf, off, len );
                    }
                    this.flushNonBlocking ( true );
                } else {
                    this.addToBuffers ( buf, off, len );
                }
            }
        }
        @Override
        protected void writeNonBlocking ( final ByteBuffer from ) throws IOException {
            synchronized ( this.writeCompletionHandler ) {
                if ( this.writePending.tryAcquire() ) {
                    this.socketBufferHandler.configureWriteBufferForWrite();
                    SocketWrapperBase.transfer ( from, this.socketBufferHandler.getWriteBuffer() );
                    if ( from.remaining() > 0 ) {
                        this.addToBuffers ( from );
                    }
                    this.flushNonBlocking ( true );
                } else {
                    this.addToBuffers ( from );
                }
            }
        }
        @Override
        protected void doWrite ( final boolean block, final ByteBuffer from ) throws IOException {
            Future<Integer> integer = null;
            try {
                do {
                    integer = this.getSocket().write ( from );
                    if ( integer.get ( this.getNio2WriteTimeout(), TimeUnit.MILLISECONDS ) < 0 ) {
                        throw new EOFException ( Nio2SocketWrapper.sm.getString ( "iob.failedwrite" ) );
                    }
                } while ( from.hasRemaining() );
            } catch ( ExecutionException e ) {
                if ( e.getCause() instanceof IOException ) {
                    throw ( IOException ) e.getCause();
                }
                throw new IOException ( e );
            } catch ( InterruptedException e2 ) {
                throw new IOException ( e2 );
            } catch ( TimeoutException e3 ) {
                integer.cancel ( true );
                throw new SocketTimeoutException();
            }
        }
        @Override
        protected void flushBlocking() throws IOException {
            this.checkError();
            try {
                if ( !this.writePending.tryAcquire ( this.getNio2WriteTimeout(), TimeUnit.MILLISECONDS ) ) {
                    throw new SocketTimeoutException();
                }
                this.writePending.release();
            } catch ( InterruptedException ex ) {}
            super.flushBlocking();
        }
        @Override
        protected boolean flushNonBlocking() throws IOException {
            return this.flushNonBlocking ( false );
        }
        private boolean flushNonBlocking ( final boolean hasPermit ) throws IOException {
            this.checkError();
            synchronized ( this.writeCompletionHandler ) {
                if ( hasPermit || this.writePending.tryAcquire() ) {
                    this.socketBufferHandler.configureWriteBufferForRead();
                    if ( this.bufferedWrites.size() > 0 ) {
                        final ArrayList<ByteBuffer> arrayList = new ArrayList<ByteBuffer>();
                        if ( this.socketBufferHandler.getWriteBuffer().hasRemaining() ) {
                            arrayList.add ( this.socketBufferHandler.getWriteBuffer() );
                        }
                        for ( final ByteBufferHolder buffer : this.bufferedWrites ) {
                            buffer.flip();
                            arrayList.add ( buffer.getBuf() );
                        }
                        this.bufferedWrites.clear();
                        final ByteBuffer[] array = arrayList.toArray ( new ByteBuffer[arrayList.size()] );
                        Nio2Endpoint.startInline();
                        this.getSocket().write ( array, 0, array.length, this.getNio2WriteTimeout(), TimeUnit.MILLISECONDS, array, this.gatheringWriteCompletionHandler );
                        Nio2Endpoint.endInline();
                    } else if ( this.socketBufferHandler.getWriteBuffer().hasRemaining() ) {
                        Nio2Endpoint.startInline();
                        this.getSocket().write ( this.socketBufferHandler.getWriteBuffer(), this.getNio2WriteTimeout(), TimeUnit.MILLISECONDS, this.socketBufferHandler.getWriteBuffer(), this.writeCompletionHandler );
                        Nio2Endpoint.endInline();
                    } else if ( !hasPermit ) {
                        this.writePending.release();
                    }
                }
                return this.hasDataToWrite();
            }
        }
        @Override
        public boolean hasDataToWrite() {
            synchronized ( this.writeCompletionHandler ) {
                return !this.socketBufferHandler.isWriteBufferEmpty() || this.bufferedWrites.size() > 0 || this.getError() != null;
            }
        }
        @Override
        public boolean isReadPending() {
            synchronized ( this.readCompletionHandler ) {
                return this.readPending.availablePermits() == 0;
            }
        }
        @Override
        public boolean awaitReadComplete ( final long timeout, final TimeUnit unit ) {
            try {
                if ( this.readPending.tryAcquire ( timeout, unit ) ) {
                    this.readPending.release();
                }
            } catch ( InterruptedException e ) {
                return false;
            }
            return true;
        }
        @Override
        public boolean awaitWriteComplete ( final long timeout, final TimeUnit unit ) {
            try {
                if ( this.writePending.tryAcquire ( timeout, unit ) ) {
                    this.writePending.release();
                }
            } catch ( InterruptedException e ) {
                return false;
            }
            return true;
        }
        void releaseReadPending() {
            synchronized ( this.readCompletionHandler ) {
                if ( this.readPending.availablePermits() == 0 ) {
                    this.readPending.release();
                }
            }
        }
        @Override
        public void registerReadInterest() {
            synchronized ( this.readCompletionHandler ) {
                if ( this.readPending.availablePermits() == 0 ) {
                    this.readInterest = true;
                } else {
                    this.awaitBytes();
                }
            }
        }
        @Override
        public void registerWriteInterest() {
            synchronized ( this.writeCompletionHandler ) {
                if ( this.writePending.availablePermits() == 0 ) {
                    this.writeInterest = true;
                } else {
                    this.getEndpoint().processSocket ( this, SocketEvent.OPEN_WRITE, true );
                }
            }
        }
        public void awaitBytes() {
            if ( this.readPending.tryAcquire() ) {
                this.getSocket().getBufHandler().configureReadBufferForWrite();
                Nio2Endpoint.startInline();
                this.getSocket().read ( this.getSocket().getBufHandler().getReadBuffer(), this.getNio2ReadTimeout(), TimeUnit.MILLISECONDS, this, this.awaitBytesHandler );
                Nio2Endpoint.endInline();
            }
        }
        @Override
        public SendfileDataBase createSendfileData ( final String filename, final long pos, final long length ) {
            return new SendfileData ( filename, pos, length );
        }
        @Override
        public SendfileState processSendfile ( final SendfileDataBase sendfileData ) {
            final SendfileData data = ( SendfileData ) sendfileData;
            this.setSendfileData ( data );
            if ( data.fchannel == null || !data.fchannel.isOpen() ) {
                final Path path = new File ( sendfileData.fileName ).toPath();
                try {
                    data.fchannel = FileChannel.open ( path, StandardOpenOption.READ ).position ( sendfileData.pos );
                } catch ( IOException e ) {
                    return SendfileState.ERROR;
                }
            }
            this.getSocket().getBufHandler().configureWriteBufferForWrite();
            final ByteBuffer buffer = this.getSocket().getBufHandler().getWriteBuffer();
            int nRead = -1;
            try {
                nRead = data.fchannel.read ( buffer );
            } catch ( IOException e2 ) {
                return SendfileState.ERROR;
            }
            if ( nRead < 0 ) {
                return SendfileState.ERROR;
            }
            final SendfileData sendfileData2 = data;
            sendfileData2.length -= nRead;
            this.getSocket().getBufHandler().configureWriteBufferForRead();
            Nio2Endpoint.startInline();
            this.getSocket().write ( buffer, this.getNio2WriteTimeout(), TimeUnit.MILLISECONDS, data, this.sendfileHandler );
            Nio2Endpoint.endInline();
            if ( !data.doneInline ) {
                return SendfileState.PENDING;
            }
            if ( data.error ) {
                return SendfileState.ERROR;
            }
            return SendfileState.DONE;
        }
        private long getNio2ReadTimeout() {
            final long readTimeout = this.getReadTimeout();
            if ( readTimeout > 0L ) {
                return readTimeout;
            }
            return Long.MAX_VALUE;
        }
        private long getNio2WriteTimeout() {
            final long writeTimeout = this.getWriteTimeout();
            if ( writeTimeout > 0L ) {
                return writeTimeout;
            }
            return Long.MAX_VALUE;
        }
        @Override
        protected void populateRemoteAddr() {
            SocketAddress socketAddress = null;
            try {
                socketAddress = this.getSocket().getIOChannel().getRemoteAddress();
            } catch ( IOException ex ) {}
            if ( socketAddress instanceof InetSocketAddress ) {
                this.remoteAddr = ( ( InetSocketAddress ) socketAddress ).getAddress().getHostAddress();
            }
        }
        @Override
        protected void populateRemoteHost() {
            SocketAddress socketAddress = null;
            try {
                socketAddress = this.getSocket().getIOChannel().getRemoteAddress();
            } catch ( IOException e ) {
                Nio2Endpoint.log.warn ( Nio2SocketWrapper.sm.getString ( "endpoint.warn.noRemoteHost", ( ( SocketWrapperBase<Object> ) this ).getSocket() ), e );
            }
            if ( socketAddress instanceof InetSocketAddress ) {
                this.remoteHost = ( ( InetSocketAddress ) socketAddress ).getAddress().getHostName();
                if ( this.remoteAddr == null ) {
                    this.remoteAddr = ( ( InetSocketAddress ) socketAddress ).getAddress().getHostAddress();
                }
            }
        }
        @Override
        protected void populateRemotePort() {
            SocketAddress socketAddress = null;
            try {
                socketAddress = this.getSocket().getIOChannel().getRemoteAddress();
            } catch ( IOException e ) {
                Nio2Endpoint.log.warn ( Nio2SocketWrapper.sm.getString ( "endpoint.warn.noRemotePort", ( ( SocketWrapperBase<Object> ) this ).getSocket() ), e );
            }
            if ( socketAddress instanceof InetSocketAddress ) {
                this.remotePort = ( ( InetSocketAddress ) socketAddress ).getPort();
            }
        }
        @Override
        protected void populateLocalName() {
            SocketAddress socketAddress = null;
            try {
                socketAddress = this.getSocket().getIOChannel().getLocalAddress();
            } catch ( IOException e ) {
                Nio2Endpoint.log.warn ( Nio2SocketWrapper.sm.getString ( "endpoint.warn.noLocalName", ( ( SocketWrapperBase<Object> ) this ).getSocket() ), e );
            }
            if ( socketAddress instanceof InetSocketAddress ) {
                this.localName = ( ( InetSocketAddress ) socketAddress ).getHostName();
            }
        }
        @Override
        protected void populateLocalAddr() {
            SocketAddress socketAddress = null;
            try {
                socketAddress = this.getSocket().getIOChannel().getLocalAddress();
            } catch ( IOException e ) {
                Nio2Endpoint.log.warn ( Nio2SocketWrapper.sm.getString ( "endpoint.warn.noLocalAddr", ( ( SocketWrapperBase<Object> ) this ).getSocket() ), e );
            }
            if ( socketAddress instanceof InetSocketAddress ) {
                this.localAddr = ( ( InetSocketAddress ) socketAddress ).getAddress().getHostAddress();
            }
        }
        @Override
        protected void populateLocalPort() {
            SocketAddress socketAddress = null;
            try {
                socketAddress = this.getSocket().getIOChannel().getLocalAddress();
            } catch ( IOException e ) {
                Nio2Endpoint.log.warn ( Nio2SocketWrapper.sm.getString ( "endpoint.warn.noLocalPort", ( ( SocketWrapperBase<Object> ) this ).getSocket() ), e );
            }
            if ( socketAddress instanceof InetSocketAddress ) {
                this.localPort = ( ( InetSocketAddress ) socketAddress ).getPort();
            }
        }
        @Override
        public SSLSupport getSslSupport ( final String clientCertProvider ) {
            if ( this.getSocket() instanceof SecureNio2Channel ) {
                final SecureNio2Channel ch = ( ( SocketWrapperBase<SecureNio2Channel> ) this ).getSocket();
                final SSLSession session = ch.getSslEngine().getSession();
                return ( ( Nio2Endpoint ) this.getEndpoint() ).getSslImplementation().getSSLSupport ( session );
            }
            return null;
        }
        @Override
        public void doClientAuth ( final SSLSupport sslSupport ) {
            final SecureNio2Channel sslChannel = ( ( SocketWrapperBase<SecureNio2Channel> ) this ).getSocket();
            final SSLEngine engine = sslChannel.getSslEngine();
            if ( !engine.getNeedClientAuth() ) {
                engine.setNeedClientAuth ( true );
                try {
                    sslChannel.rehandshake();
                    ( ( JSSESupport ) sslSupport ).setSession ( engine.getSession() );
                } catch ( IOException ioe ) {
                    Nio2Endpoint.log.warn ( Nio2SocketWrapper.sm.getString ( "socket.sslreneg" ), ioe );
                }
            }
        }
        @Override
        public void setAppReadBufHandler ( final ApplicationBufferHandler handler ) {
            this.getSocket().setAppReadBufHandler ( handler );
        }
        static {
            nestedWriteCompletionCount = new ThreadLocal<AtomicInteger>() {
                @Override
                protected AtomicInteger initialValue() {
                    return new AtomicInteger ( 0 );
                }
            };
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
            private volatile long nBytes;
            private volatile CompletionState state;
            private OperationState ( final ByteBuffer[] buffers, final int offset, final int length, final long timeout, final TimeUnit unit, final A attachment, final CompletionCheck check, final CompletionHandler<Long, ? super A> handler ) {
                this.nBytes = 0L;
                this.state = CompletionState.PENDING;
                this.buffers = buffers;
                this.offset = offset;
                this.length = length;
                this.timeout = timeout;
                this.unit = unit;
                this.attachment = attachment;
                this.check = check;
                this.handler = handler;
            }
        }
        private class ScatterReadCompletionHandler<A> implements CompletionHandler<Long, OperationState<A>> {
            @Override
            public void completed ( final Long nBytes, final OperationState<A> state ) {
                if ( ( int ) ( Object ) nBytes < 0 ) {
                    this.failed ( ( Throwable ) new EOFException(), state );
                } else {
                    ( ( OperationState<Object> ) state ).nBytes += nBytes;
                    final CompletionState currentState = Nio2Endpoint.isInline() ? CompletionState.INLINE : CompletionState.DONE;
                    boolean complete = true;
                    boolean completion = true;
                    if ( ( ( OperationState<Object> ) state ).check != null ) {
                        switch ( ( ( OperationState<Object> ) state ).check.callHandler ( currentState, ( ( OperationState<Object> ) state ).buffers, ( ( OperationState<Object> ) state ).offset, ( ( OperationState<Object> ) state ).length ) ) {
                        case CONTINUE: {
                            complete = false;
                        }
                        case NONE: {
                            completion = false;
                            break;
                        }
                        }
                    }
                    if ( complete ) {
                        Nio2SocketWrapper.this.readPending.release();
                        ( ( OperationState<Object> ) state ).state = currentState;
                        if ( completion && ( ( OperationState<Object> ) state ).handler != null ) {
                            ( ( OperationState<Object> ) state ).handler.completed ( ( ( OperationState<Object> ) state ).nBytes, ( ( OperationState<Object> ) state ).attachment );
                        }
                    } else {
                        Nio2SocketWrapper.this.getSocket().read ( ( ( OperationState<Object> ) state ).buffers, ( ( OperationState<Object> ) state ).offset, ( ( OperationState<Object> ) state ).length, ( ( OperationState<Object> ) state ).timeout, ( ( OperationState<Object> ) state ).unit, state, this );
                    }
                }
            }
            @Override
            public void failed ( final Throwable exc, final OperationState<A> state ) {
                IOException ioe;
                if ( exc instanceof IOException ) {
                    ioe = ( IOException ) exc;
                } else {
                    ioe = new IOException ( exc );
                }
                Nio2SocketWrapper.this.setError ( ioe );
                Nio2SocketWrapper.this.readPending.release();
                if ( exc instanceof AsynchronousCloseException ) {
                    return;
                }
                ( ( OperationState<Object> ) state ).state = ( Nio2Endpoint.isInline() ? CompletionState.ERROR : CompletionState.DONE );
                if ( ( ( OperationState<Object> ) state ).handler != null ) {
                    ( ( OperationState<Object> ) state ).handler.failed ( ioe, ( ( OperationState<Object> ) state ).attachment );
                }
            }
        }
        private class GatherWriteCompletionHandler<A> implements CompletionHandler<Long, OperationState<A>> {
            @Override
            public void completed ( final Long nBytes, final OperationState<A> state ) {
                if ( nBytes < 0L ) {
                    this.failed ( ( Throwable ) new EOFException(), state );
                } else {
                    ( ( OperationState<Object> ) state ).nBytes += nBytes;
                    final CompletionState currentState = Nio2Endpoint.isInline() ? CompletionState.INLINE : CompletionState.DONE;
                    boolean complete = true;
                    boolean completion = true;
                    if ( ( ( OperationState<Object> ) state ).check != null ) {
                        switch ( ( ( OperationState<Object> ) state ).check.callHandler ( currentState, ( ( OperationState<Object> ) state ).buffers, ( ( OperationState<Object> ) state ).offset, ( ( OperationState<Object> ) state ).length ) ) {
                        case CONTINUE: {
                            complete = false;
                        }
                        case NONE: {
                            completion = false;
                            break;
                        }
                        }
                    }
                    if ( complete ) {
                        Nio2SocketWrapper.this.writePending.release();
                        ( ( OperationState<Object> ) state ).state = currentState;
                        if ( completion && ( ( OperationState<Object> ) state ).handler != null ) {
                            ( ( OperationState<Object> ) state ).handler.completed ( ( ( OperationState<Object> ) state ).nBytes, ( ( OperationState<Object> ) state ).attachment );
                        }
                    } else {
                        Nio2SocketWrapper.this.getSocket().write ( ( ( OperationState<Object> ) state ).buffers, ( ( OperationState<Object> ) state ).offset, ( ( OperationState<Object> ) state ).length, ( ( OperationState<Object> ) state ).timeout, ( ( OperationState<Object> ) state ).unit, state, this );
                    }
                }
            }
            @Override
            public void failed ( final Throwable exc, final OperationState<A> state ) {
                IOException ioe;
                if ( exc instanceof IOException ) {
                    ioe = ( IOException ) exc;
                } else {
                    ioe = new IOException ( exc );
                }
                Nio2SocketWrapper.this.setError ( ioe );
                Nio2SocketWrapper.this.writePending.release();
                ( ( OperationState<Object> ) state ).state = ( Nio2Endpoint.isInline() ? CompletionState.ERROR : CompletionState.DONE );
                if ( ( ( OperationState<Object> ) state ).handler != null ) {
                    ( ( OperationState<Object> ) state ).handler.failed ( ioe, ( ( OperationState<Object> ) state ).attachment );
                }
            }
        }
    }
    protected class SocketProcessor extends SocketProcessorBase<Nio2Channel> {
        public SocketProcessor ( final SocketWrapperBase<Nio2Channel> socketWrapper, final SocketEvent event ) {
            super ( socketWrapper, event );
        }
        @Override
        protected void doRun() {
            if ( SocketEvent.OPEN_WRITE != this.event ) {
                ( ( Nio2SocketWrapper ) this.socketWrapper ).releaseReadPending();
            }
            boolean launch = false;
            try {
                int handshake = -1;
                try {
                    if ( ( ( Nio2Channel ) this.socketWrapper.getSocket() ).isHandshakeComplete() ) {
                        handshake = 0;
                    } else if ( this.event == SocketEvent.STOP || this.event == SocketEvent.DISCONNECT || this.event == SocketEvent.ERROR ) {
                        handshake = -1;
                    } else {
                        handshake = ( ( Nio2Channel ) this.socketWrapper.getSocket() ).handshake();
                        this.event = SocketEvent.OPEN_READ;
                    }
                } catch ( IOException x ) {
                    handshake = -1;
                    if ( Nio2Endpoint.log.isDebugEnabled() ) {
                        Nio2Endpoint.log.debug ( AbstractEndpoint.sm.getString ( "endpoint.err.handshake" ), x );
                    }
                }
                if ( handshake == 0 ) {
                    Handler.SocketState state = Handler.SocketState.OPEN;
                    if ( this.event == null ) {
                        state = Nio2Endpoint.this.getHandler().process ( ( SocketWrapperBase<Nio2Channel> ) this.socketWrapper, SocketEvent.OPEN_READ );
                    } else {
                        state = Nio2Endpoint.this.getHandler().process ( ( SocketWrapperBase<Nio2Channel> ) this.socketWrapper, this.event );
                    }
                    if ( state == Handler.SocketState.CLOSED ) {
                        Nio2Endpoint.this.closeSocket ( ( SocketWrapperBase<Nio2Channel> ) this.socketWrapper );
                        if ( Nio2Endpoint.this.running && !Nio2Endpoint.this.paused && !Nio2Endpoint.this.nioChannels.push ( this.socketWrapper.getSocket() ) ) {
                            ( ( Nio2Channel ) this.socketWrapper.getSocket() ).free();
                        }
                    } else if ( state == Handler.SocketState.UPGRADING ) {
                        launch = true;
                    }
                } else if ( handshake == -1 ) {
                    Nio2Endpoint.this.closeSocket ( ( SocketWrapperBase<Nio2Channel> ) this.socketWrapper );
                    if ( Nio2Endpoint.this.running && !Nio2Endpoint.this.paused && !Nio2Endpoint.this.nioChannels.push ( this.socketWrapper.getSocket() ) ) {
                        ( ( Nio2Channel ) this.socketWrapper.getSocket() ).free();
                    }
                }
            } catch ( VirtualMachineError vme ) {
                ExceptionUtils.handleThrowable ( vme );
            } catch ( Throwable t ) {
                Nio2Endpoint.log.error ( AbstractEndpoint.sm.getString ( "endpoint.processing.fail" ), t );
                if ( this.socketWrapper != null ) {
                    Nio2Endpoint.this.closeSocket ( ( SocketWrapperBase<Nio2Channel> ) this.socketWrapper );
                }
            } finally {
                if ( launch ) {
                    try {
                        Nio2Endpoint.this.getExecutor().execute ( new SocketProcessor ( ( SocketWrapperBase<Nio2Channel> ) this.socketWrapper, SocketEvent.OPEN_READ ) );
                    } catch ( NullPointerException npe ) {
                        if ( Nio2Endpoint.this.running ) {
                            Nio2Endpoint.log.error ( AbstractEndpoint.sm.getString ( "endpoint.launch.fail" ), npe );
                        }
                    }
                }
                this.socketWrapper = null;
                this.event = null;
                if ( Nio2Endpoint.this.running && !Nio2Endpoint.this.paused ) {
                    Nio2Endpoint.this.processorCache.push ( ( SocketProcessorBase<S> ) this );
                }
            }
        }
    }
    public static class SendfileData extends SendfileDataBase {
        private FileChannel fchannel;
        private boolean doneInline;
        private boolean error;
        public SendfileData ( final String filename, final long pos, final long length ) {
            super ( filename, pos, length );
            this.doneInline = false;
            this.error = false;
        }
    }
}
