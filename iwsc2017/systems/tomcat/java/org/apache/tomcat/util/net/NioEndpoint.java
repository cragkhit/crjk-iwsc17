package org.apache.tomcat.util.net;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.collections.SynchronizedQueue;
import org.apache.tomcat.util.collections.SynchronizedStack;
import org.apache.tomcat.util.net.AbstractEndpoint.Handler.SocketState;
import org.apache.tomcat.util.net.jsse.JSSESupport;
public class NioEndpoint extends AbstractJsseEndpoint<NioChannel> {
    private static final Log log = LogFactory.getLog ( NioEndpoint.class );
    public static final int OP_REGISTER = 0x100;
    private NioSelectorPool selectorPool = new NioSelectorPool();
    private ServerSocketChannel serverSock = null;
    private volatile CountDownLatch stopLatch = null;
    private SynchronizedStack<PollerEvent> eventCache;
    private SynchronizedStack<NioChannel> nioChannels;
    @Override
    public boolean setProperty ( String name, String value ) {
        final String selectorPoolName = "selectorPool.";
        try {
            if ( name.startsWith ( selectorPoolName ) ) {
                return IntrospectionUtils.setProperty ( selectorPool, name.substring ( selectorPoolName.length() ), value );
            } else {
                return super.setProperty ( name, value );
            }
        } catch ( Exception x ) {
            log.error ( "Unable to set attribute \"" + name + "\" to \"" + value + "\"", x );
            return false;
        }
    }
    private int pollerThreadPriority = Thread.NORM_PRIORITY;
    public void setPollerThreadPriority ( int pollerThreadPriority ) {
        this.pollerThreadPriority = pollerThreadPriority;
    }
    public int getPollerThreadPriority() {
        return pollerThreadPriority;
    }
    private int pollerThreadCount = Math.min ( 2, Runtime.getRuntime().availableProcessors() );
    public void setPollerThreadCount ( int pollerThreadCount ) {
        this.pollerThreadCount = pollerThreadCount;
    }
    public int getPollerThreadCount() {
        return pollerThreadCount;
    }
    private long selectorTimeout = 1000;
    public void setSelectorTimeout ( long timeout ) {
        this.selectorTimeout = timeout;
    }
    public long getSelectorTimeout() {
        return this.selectorTimeout;
    }
    private Poller[] pollers = null;
    private AtomicInteger pollerRotater = new AtomicInteger ( 0 );
    public Poller getPoller0() {
        int idx = Math.abs ( pollerRotater.incrementAndGet() ) % pollers.length;
        return pollers[idx];
    }
    public void setSelectorPool ( NioSelectorPool selectorPool ) {
        this.selectorPool = selectorPool;
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
        ServerSocketChannel ssc = serverSock;
        if ( ssc == null ) {
            return -1;
        } else {
            ServerSocket s = ssc.socket();
            if ( s == null ) {
                return -1;
            } else {
                return s.getLocalPort();
            }
        }
    }
    public int getKeepAliveCount() {
        if ( pollers == null ) {
            return 0;
        } else {
            int sum = 0;
            for ( int i = 0; i < pollers.length; i++ ) {
                sum += pollers[i].getKeyCount();
            }
            return sum;
        }
    }
    @Override
    public void bind() throws Exception {
        serverSock = ServerSocketChannel.open();
        socketProperties.setProperties ( serverSock.socket() );
        InetSocketAddress addr = ( getAddress() != null ? new InetSocketAddress ( getAddress(), getPort() ) : new InetSocketAddress ( getPort() ) );
        serverSock.socket().bind ( addr, getAcceptCount() );
        serverSock.configureBlocking ( true );
        serverSock.socket().setSoTimeout ( getSocketProperties().getSoTimeout() );
        if ( acceptorThreadCount == 0 ) {
            acceptorThreadCount = 1;
        }
        if ( pollerThreadCount <= 0 ) {
            pollerThreadCount = 1;
        }
        stopLatch = new CountDownLatch ( pollerThreadCount );
        initialiseSsl();
        selectorPool.open();
    }
    @Override
    public void startInternal() throws Exception {
        if ( !running ) {
            running = true;
            paused = false;
            processorCache = new SynchronizedStack<> ( SynchronizedStack.DEFAULT_SIZE,
                    socketProperties.getProcessorCache() );
            eventCache = new SynchronizedStack<> ( SynchronizedStack.DEFAULT_SIZE,
                                                   socketProperties.getEventCache() );
            nioChannels = new SynchronizedStack<> ( SynchronizedStack.DEFAULT_SIZE,
                                                    socketProperties.getBufferPool() );
            if ( getExecutor() == null ) {
                createExecutor();
            }
            initializeConnectionLatch();
            pollers = new Poller[getPollerThreadCount()];
            for ( int i = 0; i < pollers.length; i++ ) {
                pollers[i] = new Poller();
                Thread pollerThread = new Thread ( pollers[i], getName() + "-ClientPoller-" + i );
                pollerThread.setPriority ( threadPriority );
                pollerThread.setDaemon ( true );
                pollerThread.start();
            }
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
            for ( int i = 0; pollers != null && i < pollers.length; i++ ) {
                if ( pollers[i] == null ) {
                    continue;
                }
                pollers[i].destroy();
                pollers[i] = null;
            }
            try {
                stopLatch.await ( selectorTimeout + 100, TimeUnit.MILLISECONDS );
            } catch ( InterruptedException ignore ) {
            }
            shutdownExecutor();
            eventCache.clear();
            nioChannels.clear();
            processorCache.clear();
        }
    }
    @Override
    public void unbind() throws Exception {
        if ( log.isDebugEnabled() ) {
            log.debug ( "Destroy initiated for " + new InetSocketAddress ( getAddress(), getPort() ) );
        }
        if ( running ) {
            stop();
        }
        serverSock.socket().close();
        serverSock.close();
        serverSock = null;
        destroySsl();
        super.unbind();
        if ( getHandler() != null ) {
            getHandler().recycle();
        }
        selectorPool.close();
        if ( log.isDebugEnabled() ) {
            log.debug ( "Destroy completed for " + new InetSocketAddress ( getAddress(), getPort() ) );
        }
    }
    public int getWriteBufSize() {
        return socketProperties.getTxBufSize();
    }
    public int getReadBufSize() {
        return socketProperties.getRxBufSize();
    }
    public NioSelectorPool getSelectorPool() {
        return selectorPool;
    }
    @Override
    protected AbstractEndpoint.Acceptor createAcceptor() {
        return new Acceptor();
    }
    protected boolean setSocketOptions ( SocketChannel socket ) {
        try {
            socket.configureBlocking ( false );
            Socket sock = socket.socket();
            socketProperties.setProperties ( sock );
            NioChannel channel = nioChannels.pop();
            if ( channel == null ) {
                SocketBufferHandler bufhandler = new SocketBufferHandler (
                    socketProperties.getAppReadBufSize(),
                    socketProperties.getAppWriteBufSize(),
                    socketProperties.getDirectBuffer() );
                if ( isSSLEnabled() ) {
                    channel = new SecureNioChannel ( socket, bufhandler, selectorPool, this );
                } else {
                    channel = new NioChannel ( socket, bufhandler );
                }
            } else {
                channel.setIOChannel ( socket );
                channel.reset();
            }
            getPoller0().register ( channel );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            try {
                log.error ( "", t );
            } catch ( Throwable tt ) {
                ExceptionUtils.handleThrowable ( tt );
            }
            return false;
        }
        return true;
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
                    SocketChannel socket = null;
                    try {
                        socket = serverSock.accept();
                    } catch ( IOException ioe ) {
                        countDownConnection();
                        errorDelay = handleExceptionWithDelay ( errorDelay );
                        throw ioe;
                    }
                    errorDelay = 0;
                    if ( running && !paused ) {
                        if ( !setSocketOptions ( socket ) ) {
                            countDownConnection();
                            closeSocket ( socket );
                        }
                    } else {
                        countDownConnection();
                        closeSocket ( socket );
                    }
                } catch ( SocketTimeoutException sx ) {
                } catch ( IOException x ) {
                    if ( running ) {
                        log.error ( sm.getString ( "endpoint.accept.fail" ), x );
                    }
                } catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                    log.error ( sm.getString ( "endpoint.accept.fail" ), t );
                }
            }
            state = AcceptorState.ENDED;
        }
    }
    @Override
    protected SocketProcessorBase<NioChannel> createSocketProcessor (
        SocketWrapperBase<NioChannel> socketWrapper, SocketEvent event ) {
        return new SocketProcessor ( socketWrapper, event );
    }
    private void close ( NioChannel socket, SelectionKey key ) {
        try {
            if ( socket.getPoller().cancelledKey ( key ) != null ) {
                if ( running && !paused ) {
                    if ( !nioChannels.push ( socket ) ) {
                        socket.free();
                    }
                }
            }
        } catch ( Exception x ) {
            log.error ( "", x );
        }
    }
    private void closeSocket ( SocketChannel socket ) {
        try {
            socket.socket().close();
        } catch ( IOException ioe )  {
            if ( log.isDebugEnabled() ) {
                log.debug ( "", ioe );
            }
        }
        try {
            socket.close();
        } catch ( IOException ioe ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( "", ioe );
            }
        }
    }
    public static class PollerEvent implements Runnable {
        private NioChannel socket;
        private int interestOps;
        private NioSocketWrapper socketWrapper;
        public PollerEvent ( NioChannel ch, NioSocketWrapper w, int intOps ) {
            reset ( ch, w, intOps );
        }
        public void reset ( NioChannel ch, NioSocketWrapper w, int intOps ) {
            socket = ch;
            interestOps = intOps;
            socketWrapper = w;
        }
        public void reset() {
            reset ( null, null, 0 );
        }
        @Override
        public void run() {
            if ( interestOps == OP_REGISTER ) {
                try {
                    socket.getIOChannel().register (
                        socket.getPoller().getSelector(), SelectionKey.OP_READ, socketWrapper );
                } catch ( Exception x ) {
                    log.error ( sm.getString ( "endpoint.nio.registerFail" ), x );
                }
            } else {
                final SelectionKey key = socket.getIOChannel().keyFor ( socket.getPoller().getSelector() );
                try {
                    if ( key == null ) {
                        socket.socketWrapper.getEndpoint().countDownConnection();
                    } else {
                        final NioSocketWrapper socketWrapper = ( NioSocketWrapper ) key.attachment();
                        if ( socketWrapper != null ) {
                            int ops = key.interestOps() | interestOps;
                            socketWrapper.interestOps ( ops );
                            key.interestOps ( ops );
                        } else {
                            socket.getPoller().cancelledKey ( key );
                        }
                    }
                } catch ( CancelledKeyException ckx ) {
                    try {
                        socket.getPoller().cancelledKey ( key );
                    } catch ( Exception ignore ) {}
                }
            }
        }
        @Override
        public String toString() {
            return "Poller event: socket [" + socket + "], socketWrapper [" + socketWrapper +
                   "], interstOps [" + interestOps + "]";
        }
    }
    public class Poller implements Runnable {
        private Selector selector;
        private final SynchronizedQueue<PollerEvent> events =
            new SynchronizedQueue<>();
        private volatile boolean close = false;
        private long nextExpiration = 0;
        private AtomicLong wakeupCounter = new AtomicLong ( 0 );
        private volatile int keyCount = 0;
        public Poller() throws IOException {
            this.selector = Selector.open();
        }
        public int getKeyCount() {
            return keyCount;
        }
        public Selector getSelector() {
            return selector;
        }
        protected void destroy() {
            close = true;
            selector.wakeup();
        }
        private void addEvent ( PollerEvent event ) {
            events.offer ( event );
            if ( wakeupCounter.incrementAndGet() == 0 ) {
                selector.wakeup();
            }
        }
        public void add ( final NioChannel socket, final int interestOps ) {
            PollerEvent r = eventCache.pop();
            if ( r == null ) {
                r = new PollerEvent ( socket, null, interestOps );
            } else {
                r.reset ( socket, null, interestOps );
            }
            addEvent ( r );
            if ( close ) {
                NioEndpoint.NioSocketWrapper ka = ( NioEndpoint.NioSocketWrapper ) socket.getAttachment();
                processSocket ( ka, SocketEvent.STOP, false );
            }
        }
        public boolean events() {
            boolean result = false;
            PollerEvent pe = null;
            while ( ( pe = events.poll() ) != null ) {
                result = true;
                try {
                    pe.run();
                    pe.reset();
                    if ( running && !paused ) {
                        eventCache.push ( pe );
                    }
                } catch ( Throwable x ) {
                    log.error ( "", x );
                }
            }
            return result;
        }
        public void register ( final NioChannel socket ) {
            socket.setPoller ( this );
            NioSocketWrapper ka = new NioSocketWrapper ( socket, NioEndpoint.this );
            socket.setSocketWrapper ( ka );
            ka.setPoller ( this );
            ka.setReadTimeout ( getSocketProperties().getSoTimeout() );
            ka.setWriteTimeout ( getSocketProperties().getSoTimeout() );
            ka.setKeepAliveLeft ( NioEndpoint.this.getMaxKeepAliveRequests() );
            ka.setSecure ( isSSLEnabled() );
            ka.setReadTimeout ( getConnectionTimeout() );
            ka.setWriteTimeout ( getConnectionTimeout() );
            PollerEvent r = eventCache.pop();
            ka.interestOps ( SelectionKey.OP_READ );
            if ( r == null ) {
                r = new PollerEvent ( socket, ka, OP_REGISTER );
            } else {
                r.reset ( socket, ka, OP_REGISTER );
            }
            addEvent ( r );
        }
        public NioSocketWrapper cancelledKey ( SelectionKey key ) {
            NioSocketWrapper ka = null;
            try {
                if ( key == null ) {
                    return null;
                }
                ka = ( NioSocketWrapper ) key.attach ( null );
                if ( ka != null ) {
                    getHandler().release ( ka );
                }
                if ( key.isValid() ) {
                    key.cancel();
                }
                if ( key.channel().isOpen() ) {
                    try {
                        key.channel().close();
                    } catch ( Exception e ) {
                        if ( log.isDebugEnabled() ) {
                            log.debug ( sm.getString (
                                            "endpoint.debug.channelCloseFail" ), e );
                        }
                    }
                }
                try {
                    if ( ka != null ) {
                        ka.getSocket().close ( true );
                    }
                } catch ( Exception e ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug ( sm.getString (
                                        "endpoint.debug.socketCloseFail" ), e );
                    }
                }
                try {
                    if ( ka != null && ka.getSendfileData() != null
                            && ka.getSendfileData().fchannel != null
                            && ka.getSendfileData().fchannel.isOpen() ) {
                        ka.getSendfileData().fchannel.close();
                    }
                } catch ( Exception ignore ) {
                }
                if ( ka != null ) {
                    countDownConnection();
                }
            } catch ( Throwable e ) {
                ExceptionUtils.handleThrowable ( e );
                if ( log.isDebugEnabled() ) {
                    log.error ( "", e );
                }
            }
            return ka;
        }
        @Override
        public void run() {
            while ( true ) {
                boolean hasEvents = false;
                try {
                    if ( !close ) {
                        hasEvents = events();
                        if ( wakeupCounter.getAndSet ( -1 ) > 0 ) {
                            keyCount = selector.selectNow();
                        } else {
                            keyCount = selector.select ( selectorTimeout );
                        }
                        wakeupCounter.set ( 0 );
                    }
                    if ( close ) {
                        events();
                        timeout ( 0, false );
                        try {
                            selector.close();
                        } catch ( IOException ioe ) {
                            log.error ( sm.getString ( "endpoint.nio.selectorCloseFail" ), ioe );
                        }
                        break;
                    }
                } catch ( Throwable x ) {
                    ExceptionUtils.handleThrowable ( x );
                    log.error ( "", x );
                    continue;
                }
                if ( keyCount == 0 ) {
                    hasEvents = ( hasEvents | events() );
                }
                Iterator<SelectionKey> iterator =
                    keyCount > 0 ? selector.selectedKeys().iterator() : null;
                while ( iterator != null && iterator.hasNext() ) {
                    SelectionKey sk = iterator.next();
                    NioSocketWrapper attachment = ( NioSocketWrapper ) sk.attachment();
                    if ( attachment == null ) {
                        iterator.remove();
                    } else {
                        iterator.remove();
                        processKey ( sk, attachment );
                    }
                }
                timeout ( keyCount, hasEvents );
            }
            stopLatch.countDown();
        }
        protected void processKey ( SelectionKey sk, NioSocketWrapper attachment ) {
            try {
                if ( close ) {
                    cancelledKey ( sk );
                } else if ( sk.isValid() && attachment != null ) {
                    if ( sk.isReadable() || sk.isWritable() ) {
                        if ( attachment.getSendfileData() != null ) {
                            processSendfile ( sk, attachment, false );
                        } else {
                            unreg ( sk, attachment, sk.readyOps() );
                            boolean closeSocket = false;
                            if ( sk.isReadable() ) {
                                if ( !processSocket ( attachment, SocketEvent.OPEN_READ, true ) ) {
                                    closeSocket = true;
                                }
                            }
                            if ( !closeSocket && sk.isWritable() ) {
                                if ( !processSocket ( attachment, SocketEvent.OPEN_WRITE, true ) ) {
                                    closeSocket = true;
                                }
                            }
                            if ( closeSocket ) {
                                cancelledKey ( sk );
                            }
                        }
                    }
                } else {
                    cancelledKey ( sk );
                }
            } catch ( CancelledKeyException ckx ) {
                cancelledKey ( sk );
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                log.error ( "", t );
            }
        }
        public SendfileState processSendfile ( SelectionKey sk, NioSocketWrapper socketWrapper,
                                               boolean calledByProcessor ) {
            NioChannel sc = null;
            try {
                unreg ( sk, socketWrapper, sk.readyOps() );
                SendfileData sd = socketWrapper.getSendfileData();
                if ( log.isTraceEnabled() ) {
                    log.trace ( "Processing send file for: " + sd.fileName );
                }
                if ( sd.fchannel == null ) {
                    File f = new File ( sd.fileName );
                    if ( !f.exists() ) {
                        cancelledKey ( sk );
                        return SendfileState.ERROR;
                    }
                    @SuppressWarnings ( "resource" )
                    FileInputStream fis = new FileInputStream ( f );
                    sd.fchannel = fis.getChannel();
                }
                sc = socketWrapper.getSocket();
                WritableByteChannel wc = ( ( sc instanceof SecureNioChannel ) ? sc : sc.getIOChannel() );
                if ( sc.getOutboundRemaining() > 0 ) {
                    if ( sc.flushOutbound() ) {
                        socketWrapper.updateLastWrite();
                    }
                } else {
                    long written = sd.fchannel.transferTo ( sd.pos, sd.length, wc );
                    if ( written > 0 ) {
                        sd.pos += written;
                        sd.length -= written;
                        socketWrapper.updateLastWrite();
                    } else {
                        if ( sd.fchannel.size() <= sd.pos ) {
                            throw new IOException ( "Sendfile configured to " +
                                                    "send more data than was available" );
                        }
                    }
                }
                if ( sd.length <= 0 && sc.getOutboundRemaining() <= 0 ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug ( "Send file complete for: " + sd.fileName );
                    }
                    socketWrapper.setSendfileData ( null );
                    try {
                        sd.fchannel.close();
                    } catch ( Exception ignore ) {
                    }
                    if ( !calledByProcessor ) {
                        if ( sd.keepAlive ) {
                            if ( log.isDebugEnabled() ) {
                                log.debug ( "Connection is keep alive, registering back for OP_READ" );
                            }
                            reg ( sk, socketWrapper, SelectionKey.OP_READ );
                        } else {
                            if ( log.isDebugEnabled() ) {
                                log.debug ( "Send file connection is being closed" );
                            }
                            close ( sc, sk );
                        }
                    }
                    return SendfileState.DONE;
                } else {
                    if ( log.isDebugEnabled() ) {
                        log.debug ( "OP_WRITE for sendfile: " + sd.fileName );
                    }
                    if ( calledByProcessor ) {
                        add ( socketWrapper.getSocket(), SelectionKey.OP_WRITE );
                    } else {
                        reg ( sk, socketWrapper, SelectionKey.OP_WRITE );
                    }
                    return SendfileState.PENDING;
                }
            } catch ( IOException x ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "Unable to complete sendfile request:", x );
                }
                if ( !calledByProcessor && sc != null ) {
                    close ( sc, sk );
                } else {
                    cancelledKey ( sk );
                }
                return SendfileState.ERROR;
            } catch ( Throwable t ) {
                log.error ( "", t );
                if ( !calledByProcessor && sc != null ) {
                    close ( sc, sk );
                } else {
                    cancelledKey ( sk );
                }
                return SendfileState.ERROR;
            }
        }
        protected void unreg ( SelectionKey sk, NioSocketWrapper attachment, int readyOps ) {
            reg ( sk, attachment, sk.interestOps() & ( ~readyOps ) );
        }
        protected void reg ( SelectionKey sk, NioSocketWrapper attachment, int intops ) {
            sk.interestOps ( intops );
            attachment.interestOps ( intops );
        }
        protected void timeout ( int keyCount, boolean hasEvents ) {
            long now = System.currentTimeMillis();
            if ( nextExpiration > 0 && ( keyCount > 0 || hasEvents ) && ( now < nextExpiration ) && !close ) {
                return;
            }
            int keycount = 0;
            try {
                for ( SelectionKey key : selector.keys() ) {
                    keycount++;
                    try {
                        NioSocketWrapper ka = ( NioSocketWrapper ) key.attachment();
                        if ( ka == null ) {
                            cancelledKey ( key );
                        } else if ( close ) {
                            key.interestOps ( 0 );
                            ka.interestOps ( 0 );
                            processKey ( key, ka );
                        } else if ( ( ka.interestOps() & SelectionKey.OP_READ ) == SelectionKey.OP_READ ||
                                    ( ka.interestOps() & SelectionKey.OP_WRITE ) == SelectionKey.OP_WRITE ) {
                            boolean isTimedOut = false;
                            if ( ( ka.interestOps() & SelectionKey.OP_READ ) == SelectionKey.OP_READ ) {
                                long delta = now - ka.getLastRead();
                                long timeout = ka.getReadTimeout();
                                isTimedOut = timeout > 0 && delta > timeout;
                            }
                            if ( !isTimedOut && ( ka.interestOps() & SelectionKey.OP_WRITE ) == SelectionKey.OP_WRITE ) {
                                long delta = now - ka.getLastWrite();
                                long timeout = ka.getWriteTimeout();
                                isTimedOut = timeout > 0 && delta > timeout;
                            }
                            if ( isTimedOut ) {
                                key.interestOps ( 0 );
                                ka.interestOps ( 0 );
                                ka.setError ( new SocketTimeoutException() );
                                if ( !processSocket ( ka, SocketEvent.ERROR, true ) ) {
                                    cancelledKey ( key );
                                }
                            }
                        }
                    } catch ( CancelledKeyException ckx ) {
                        cancelledKey ( key );
                    }
                }
            } catch ( ConcurrentModificationException cme ) {
                log.warn ( sm.getString ( "endpoint.nio.timeoutCme" ), cme );
            }
            long prevExp = nextExpiration;
            nextExpiration = System.currentTimeMillis() +
                             socketProperties.getTimeoutInterval();
            if ( log.isTraceEnabled() ) {
                log.trace ( "timeout completed: keys processed=" + keycount +
                            "; now=" + now + "; nextExpiration=" + prevExp +
                            "; keyCount=" + keyCount + "; hasEvents=" + hasEvents +
                            "; eval=" + ( ( now < prevExp ) && ( keyCount > 0 || hasEvents ) && ( !close ) ) );
            }
        }
    }
    public static class NioSocketWrapper extends SocketWrapperBase<NioChannel> {
        private final NioSelectorPool pool;
        private Poller poller = null;
        private int interestOps = 0;
        private CountDownLatch readLatch = null;
        private CountDownLatch writeLatch = null;
        private volatile SendfileData sendfileData = null;
        private volatile long lastRead = System.currentTimeMillis();
        private volatile long lastWrite = lastRead;
        public NioSocketWrapper ( NioChannel channel, NioEndpoint endpoint ) {
            super ( channel, endpoint );
            pool = endpoint.getSelectorPool();
            socketBufferHandler = channel.getBufHandler();
        }
        public Poller getPoller() {
            return poller;
        }
        public void setPoller ( Poller poller ) {
            this.poller = poller;
        }
        public int interestOps() {
            return interestOps;
        }
        public int interestOps ( int ops ) {
            this.interestOps  = ops;
            return ops;
        }
        public CountDownLatch getReadLatch() {
            return readLatch;
        }
        public CountDownLatch getWriteLatch() {
            return writeLatch;
        }
        protected CountDownLatch resetLatch ( CountDownLatch latch ) {
            if ( latch == null || latch.getCount() == 0 ) {
                return null;
            } else {
                throw new IllegalStateException ( "Latch must be at count 0" );
            }
        }
        public void resetReadLatch() {
            readLatch = resetLatch ( readLatch );
        }
        public void resetWriteLatch() {
            writeLatch = resetLatch ( writeLatch );
        }
        protected CountDownLatch startLatch ( CountDownLatch latch, int cnt ) {
            if ( latch == null || latch.getCount() == 0 ) {
                return new CountDownLatch ( cnt );
            } else {
                throw new IllegalStateException ( "Latch must be at count 0 or null." );
            }
        }
        public void startReadLatch ( int cnt ) {
            readLatch = startLatch ( readLatch, cnt );
        }
        public void startWriteLatch ( int cnt ) {
            writeLatch = startLatch ( writeLatch, cnt );
        }
        protected void awaitLatch ( CountDownLatch latch, long timeout, TimeUnit unit ) throws InterruptedException {
            if ( latch == null ) {
                throw new IllegalStateException ( "Latch cannot be null" );
            }
            latch.await ( timeout, unit );
        }
        public void awaitReadLatch ( long timeout, TimeUnit unit ) throws InterruptedException {
            awaitLatch ( readLatch, timeout, unit );
        }
        public void awaitWriteLatch ( long timeout, TimeUnit unit ) throws InterruptedException {
            awaitLatch ( writeLatch, timeout, unit );
        }
        public void setSendfileData ( SendfileData sf ) {
            this.sendfileData = sf;
        }
        public SendfileData getSendfileData() {
            return this.sendfileData;
        }
        public void updateLastWrite() {
            lastWrite = System.currentTimeMillis();
        }
        public long getLastWrite() {
            return lastWrite;
        }
        public void updateLastRead() {
            lastRead = System.currentTimeMillis();
        }
        public long getLastRead() {
            return lastRead;
        }
        @Override
        public boolean isReadyForRead() throws IOException {
            socketBufferHandler.configureReadBufferForRead();
            if ( socketBufferHandler.getReadBuffer().remaining() > 0 ) {
                return true;
            }
            fillReadBuffer ( false );
            boolean isReady = socketBufferHandler.getReadBuffer().position() > 0;
            return isReady;
        }
        @Override
        public int read ( boolean block, byte[] b, int off, int len ) throws IOException {
            int nRead = populateReadBuffer ( b, off, len );
            if ( nRead > 0 ) {
                return nRead;
            }
            nRead = fillReadBuffer ( block );
            updateLastRead();
            if ( nRead > 0 ) {
                socketBufferHandler.configureReadBufferForRead();
                nRead = Math.min ( nRead, len );
                socketBufferHandler.getReadBuffer().get ( b, off, nRead );
            }
            return nRead;
        }
        @Override
        public int read ( boolean block, ByteBuffer to ) throws IOException {
            int nRead = populateReadBuffer ( to );
            if ( nRead > 0 ) {
                return nRead;
            }
            int limit = socketBufferHandler.getReadBuffer().capacity();
            if ( to.remaining() >= limit ) {
                to.limit ( to.position() + limit );
                nRead = fillReadBuffer ( block, to );
                updateLastRead();
            } else {
                nRead = fillReadBuffer ( block );
                updateLastRead();
                if ( nRead > 0 ) {
                    nRead = populateReadBuffer ( to );
                }
            }
            return nRead;
        }
        @Override
        public void close() throws IOException {
            getSocket().close();
        }
        @Override
        public boolean isClosed() {
            return !getSocket().isOpen();
        }
        private int fillReadBuffer ( boolean block ) throws IOException {
            socketBufferHandler.configureReadBufferForWrite();
            return fillReadBuffer ( block, socketBufferHandler.getReadBuffer() );
        }
        private int fillReadBuffer ( boolean block, ByteBuffer to ) throws IOException {
            int nRead;
            NioChannel channel = getSocket();
            if ( block ) {
                Selector selector = null;
                try {
                    selector = pool.get();
                } catch ( IOException x ) {
                }
                try {
                    NioEndpoint.NioSocketWrapper att = ( NioEndpoint.NioSocketWrapper ) channel
                                                       .getAttachment();
                    if ( att == null ) {
                        throw new IOException ( "Key must be cancelled." );
                    }
                    nRead = pool.read ( to, channel, selector, att.getReadTimeout() );
                } finally {
                    if ( selector != null ) {
                        pool.put ( selector );
                    }
                }
            } else {
                nRead = channel.read ( to );
                if ( nRead == -1 ) {
                    throw new EOFException();
                }
            }
            return nRead;
        }
        @Override
        protected void doWrite ( boolean block, ByteBuffer from ) throws IOException {
            long writeTimeout = getWriteTimeout();
            Selector selector = null;
            try {
                selector = pool.get();
            } catch ( IOException x ) {
            }
            try {
                pool.write ( from, getSocket(), selector, writeTimeout, block );
                if ( block ) {
                    do {
                        if ( getSocket().flush ( true, selector, writeTimeout ) ) {
                            break;
                        }
                    } while ( true );
                }
                updateLastWrite();
            } finally {
                if ( selector != null ) {
                    pool.put ( selector );
                }
            }
        }
        @Override
        public void registerReadInterest() {
            getPoller().add ( getSocket(), SelectionKey.OP_READ );
        }
        @Override
        public void registerWriteInterest() {
            getPoller().add ( getSocket(), SelectionKey.OP_WRITE );
        }
        @Override
        public SendfileDataBase createSendfileData ( String filename, long pos, long length ) {
            return new SendfileData ( filename, pos, length );
        }
        @Override
        public SendfileState processSendfile ( SendfileDataBase sendfileData ) {
            setSendfileData ( ( SendfileData ) sendfileData );
            SelectionKey key = getSocket().getIOChannel().keyFor (
                                   getSocket().getPoller().getSelector() );
            return getSocket().getPoller().processSendfile ( key, this, true );
        }
        @Override
        protected void populateRemoteAddr() {
            InetAddress inetAddr = getSocket().getIOChannel().socket().getInetAddress();
            if ( inetAddr != null ) {
                remoteAddr = inetAddr.getHostAddress();
            }
        }
        @Override
        protected void populateRemoteHost() {
            InetAddress inetAddr = getSocket().getIOChannel().socket().getInetAddress();
            if ( inetAddr != null ) {
                remoteHost = inetAddr.getHostName();
                if ( remoteAddr == null ) {
                    remoteAddr = inetAddr.getHostAddress();
                }
            }
        }
        @Override
        protected void populateRemotePort() {
            remotePort = getSocket().getIOChannel().socket().getPort();
        }
        @Override
        protected void populateLocalName() {
            InetAddress inetAddr = getSocket().getIOChannel().socket().getLocalAddress();
            if ( inetAddr != null ) {
                localName = inetAddr.getHostName();
            }
        }
        @Override
        protected void populateLocalAddr() {
            InetAddress inetAddr = getSocket().getIOChannel().socket().getLocalAddress();
            if ( inetAddr != null ) {
                localAddr = inetAddr.getHostAddress();
            }
        }
        @Override
        protected void populateLocalPort() {
            localPort = getSocket().getIOChannel().socket().getLocalPort();
        }
        @Override
        public SSLSupport getSslSupport ( String clientCertProvider ) {
            if ( getSocket() instanceof SecureNioChannel ) {
                SecureNioChannel ch = ( SecureNioChannel ) getSocket();
                SSLSession session = ch.getSslEngine().getSession();
                return ( ( NioEndpoint ) getEndpoint() ).getSslImplementation().getSSLSupport ( session );
            } else {
                return null;
            }
        }
        @Override
        public void doClientAuth ( SSLSupport sslSupport ) {
            SecureNioChannel sslChannel = ( SecureNioChannel ) getSocket();
            SSLEngine engine = sslChannel.getSslEngine();
            if ( !engine.getNeedClientAuth() ) {
                engine.setNeedClientAuth ( true );
                try {
                    sslChannel.rehandshake ( getEndpoint().getConnectionTimeout() );
                    ( ( JSSESupport ) sslSupport ).setSession ( engine.getSession() );
                } catch ( IOException ioe ) {
                    log.warn ( sm.getString ( "socket.sslreneg", ioe ) );
                }
            }
        }
        @Override
        public void setAppReadBufHandler ( ApplicationBufferHandler handler ) {
            getSocket().setAppReadBufHandler ( handler );
        }
    }
    protected class SocketProcessor extends SocketProcessorBase<NioChannel> {
        public SocketProcessor ( SocketWrapperBase<NioChannel> socketWrapper, SocketEvent event ) {
            super ( socketWrapper, event );
        }
        @Override
        protected void doRun() {
            NioChannel socket = socketWrapper.getSocket();
            SelectionKey key = socket.getIOChannel().keyFor ( socket.getPoller().getSelector() );
            try {
                int handshake = -1;
                try {
                    if ( key != null ) {
                        if ( socket.isHandshakeComplete() ) {
                            handshake = 0;
                        } else if ( event == SocketEvent.STOP || event == SocketEvent.DISCONNECT ||
                                    event == SocketEvent.ERROR ) {
                            handshake = -1;
                        } else {
                            handshake = socket.handshake ( key.isReadable(), key.isWritable() );
                            event = SocketEvent.OPEN_READ;
                        }
                    }
                } catch ( IOException x ) {
                    handshake = -1;
                    if ( log.isDebugEnabled() ) {
                        log.debug ( "Error during SSL handshake", x );
                    }
                } catch ( CancelledKeyException ckx ) {
                    handshake = -1;
                }
                if ( handshake == 0 ) {
                    SocketState state = SocketState.OPEN;
                    if ( event == null ) {
                        state = getHandler().process ( socketWrapper, SocketEvent.OPEN_READ );
                    } else {
                        state = getHandler().process ( socketWrapper, event );
                    }
                    if ( state == SocketState.CLOSED ) {
                        close ( socket, key );
                    }
                } else if ( handshake == -1 ) {
                    close ( socket, key );
                } else if ( handshake == SelectionKey.OP_READ ) {
                    socketWrapper.registerReadInterest();
                } else if ( handshake == SelectionKey.OP_WRITE ) {
                    socketWrapper.registerWriteInterest();
                }
            } catch ( CancelledKeyException cx ) {
                socket.getPoller().cancelledKey ( key );
            } catch ( VirtualMachineError vme ) {
                ExceptionUtils.handleThrowable ( vme );
            } catch ( Throwable t ) {
                log.error ( "", t );
                socket.getPoller().cancelledKey ( key );
            } finally {
                socketWrapper = null;
                event = null;
                if ( running && !paused ) {
                    processorCache.push ( this );
                }
            }
        }
    }
    public static class SendfileData extends SendfileDataBase {
        public SendfileData ( String filename, long pos, long length ) {
            super ( filename, pos, length );
        }
        protected volatile FileChannel fchannel;
    }
}
