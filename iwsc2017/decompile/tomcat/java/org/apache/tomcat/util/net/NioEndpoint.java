package org.apache.tomcat.util.net;
import java.nio.channels.FileChannel;
import javax.net.ssl.SSLEngine;
import org.apache.tomcat.util.net.jsse.JSSESupport;
import javax.net.ssl.SSLSession;
import java.net.InetAddress;
import java.io.EOFException;
import java.nio.ByteBuffer;
import java.util.ConcurrentModificationException;
import java.nio.channels.WritableByteChannel;
import java.io.FileInputStream;
import java.io.File;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.tomcat.util.collections.SynchronizedQueue;
import java.nio.channels.Selector;
import java.nio.channels.CancelledKeyException;
import java.net.SocketTimeoutException;
import org.apache.juli.logging.LogFactory;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.net.Socket;
import org.apache.tomcat.util.ExceptionUtils;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import org.apache.tomcat.util.IntrospectionUtils;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.tomcat.util.collections.SynchronizedStack;
import java.util.concurrent.CountDownLatch;
import java.nio.channels.ServerSocketChannel;
import org.apache.juli.logging.Log;
public class NioEndpoint extends AbstractJsseEndpoint<NioChannel> {
    private static final Log log;
    public static final int OP_REGISTER = 256;
    private NioSelectorPool selectorPool;
    private ServerSocketChannel serverSock;
    private volatile CountDownLatch stopLatch;
    private SynchronizedStack<PollerEvent> eventCache;
    private SynchronizedStack<NioChannel> nioChannels;
    private int pollerThreadPriority;
    private int pollerThreadCount;
    private long selectorTimeout;
    private Poller[] pollers;
    private AtomicInteger pollerRotater;
    public NioEndpoint() {
        this.selectorPool = new NioSelectorPool();
        this.serverSock = null;
        this.stopLatch = null;
        this.pollerThreadPriority = 5;
        this.pollerThreadCount = Math.min ( 2, Runtime.getRuntime().availableProcessors() );
        this.selectorTimeout = 1000L;
        this.pollers = null;
        this.pollerRotater = new AtomicInteger ( 0 );
    }
    @Override
    public boolean setProperty ( final String name, final String value ) {
        final String selectorPoolName = "selectorPool.";
        try {
            if ( name.startsWith ( "selectorPool." ) ) {
                return IntrospectionUtils.setProperty ( this.selectorPool, name.substring ( "selectorPool.".length() ), value );
            }
            return super.setProperty ( name, value );
        } catch ( Exception x ) {
            NioEndpoint.log.error ( "Unable to set attribute \"" + name + "\" to \"" + value + "\"", x );
            return false;
        }
    }
    public void setPollerThreadPriority ( final int pollerThreadPriority ) {
        this.pollerThreadPriority = pollerThreadPriority;
    }
    public int getPollerThreadPriority() {
        return this.pollerThreadPriority;
    }
    public void setPollerThreadCount ( final int pollerThreadCount ) {
        this.pollerThreadCount = pollerThreadCount;
    }
    public int getPollerThreadCount() {
        return this.pollerThreadCount;
    }
    public void setSelectorTimeout ( final long timeout ) {
        this.selectorTimeout = timeout;
    }
    public long getSelectorTimeout() {
        return this.selectorTimeout;
    }
    public Poller getPoller0() {
        final int idx = Math.abs ( this.pollerRotater.incrementAndGet() ) % this.pollers.length;
        return this.pollers[idx];
    }
    public void setSelectorPool ( final NioSelectorPool selectorPool ) {
        this.selectorPool = selectorPool;
    }
    public void setSocketProperties ( final SocketProperties socketProperties ) {
        this.socketProperties = socketProperties;
    }
    public boolean getDeferAccept() {
        return false;
    }
    @Override
    public int getLocalPort() {
        final ServerSocketChannel ssc = this.serverSock;
        if ( ssc == null ) {
            return -1;
        }
        final ServerSocket s = ssc.socket();
        if ( s == null ) {
            return -1;
        }
        return s.getLocalPort();
    }
    public int getKeepAliveCount() {
        if ( this.pollers == null ) {
            return 0;
        }
        int sum = 0;
        for ( int i = 0; i < this.pollers.length; ++i ) {
            sum += this.pollers[i].getKeyCount();
        }
        return sum;
    }
    @Override
    public void bind() throws Exception {
        this.serverSock = ServerSocketChannel.open();
        this.socketProperties.setProperties ( this.serverSock.socket() );
        final InetSocketAddress addr = ( this.getAddress() != null ) ? new InetSocketAddress ( this.getAddress(), this.getPort() ) : new InetSocketAddress ( this.getPort() );
        this.serverSock.socket().bind ( addr, this.getAcceptCount() );
        this.serverSock.configureBlocking ( true );
        this.serverSock.socket().setSoTimeout ( this.getSocketProperties().getSoTimeout() );
        if ( this.acceptorThreadCount == 0 ) {
            this.acceptorThreadCount = 1;
        }
        if ( this.pollerThreadCount <= 0 ) {
            this.pollerThreadCount = 1;
        }
        this.stopLatch = new CountDownLatch ( this.pollerThreadCount );
        this.initialiseSsl();
        this.selectorPool.open();
    }
    @Override
    public void startInternal() throws Exception {
        if ( !this.running ) {
            this.running = true;
            this.paused = false;
            this.processorCache = new SynchronizedStack<SocketProcessorBase<S>> ( 128, this.socketProperties.getProcessorCache() );
            this.eventCache = new SynchronizedStack<PollerEvent> ( 128, this.socketProperties.getEventCache() );
            this.nioChannels = new SynchronizedStack<NioChannel> ( 128, this.socketProperties.getBufferPool() );
            if ( this.getExecutor() == null ) {
                this.createExecutor();
            }
            this.initializeConnectionLatch();
            this.pollers = new Poller[this.getPollerThreadCount()];
            for ( int i = 0; i < this.pollers.length; ++i ) {
                this.pollers[i] = new Poller();
                final Thread pollerThread = new Thread ( this.pollers[i], this.getName() + "-ClientPoller-" + i );
                pollerThread.setPriority ( this.threadPriority );
                pollerThread.setDaemon ( true );
                pollerThread.start();
            }
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
            for ( int i = 0; this.pollers != null && i < this.pollers.length; ++i ) {
                if ( this.pollers[i] != null ) {
                    this.pollers[i].destroy();
                    this.pollers[i] = null;
                }
            }
            try {
                this.stopLatch.await ( this.selectorTimeout + 100L, TimeUnit.MILLISECONDS );
            } catch ( InterruptedException ex ) {}
            this.shutdownExecutor();
            this.eventCache.clear();
            this.nioChannels.clear();
            this.processorCache.clear();
        }
    }
    @Override
    public void unbind() throws Exception {
        if ( NioEndpoint.log.isDebugEnabled() ) {
            NioEndpoint.log.debug ( "Destroy initiated for " + new InetSocketAddress ( this.getAddress(), this.getPort() ) );
        }
        if ( this.running ) {
            this.stop();
        }
        this.serverSock.socket().close();
        this.serverSock.close();
        this.serverSock = null;
        this.destroySsl();
        super.unbind();
        if ( this.getHandler() != null ) {
            this.getHandler().recycle();
        }
        this.selectorPool.close();
        if ( NioEndpoint.log.isDebugEnabled() ) {
            NioEndpoint.log.debug ( "Destroy completed for " + new InetSocketAddress ( this.getAddress(), this.getPort() ) );
        }
    }
    public int getWriteBufSize() {
        return this.socketProperties.getTxBufSize();
    }
    public int getReadBufSize() {
        return this.socketProperties.getRxBufSize();
    }
    public NioSelectorPool getSelectorPool() {
        return this.selectorPool;
    }
    @Override
    protected AbstractEndpoint.Acceptor createAcceptor() {
        return new Acceptor();
    }
    protected boolean setSocketOptions ( final SocketChannel socket ) {
        try {
            socket.configureBlocking ( false );
            final Socket sock = socket.socket();
            this.socketProperties.setProperties ( sock );
            NioChannel channel = this.nioChannels.pop();
            if ( channel == null ) {
                final SocketBufferHandler bufhandler = new SocketBufferHandler ( this.socketProperties.getAppReadBufSize(), this.socketProperties.getAppWriteBufSize(), this.socketProperties.getDirectBuffer() );
                if ( this.isSSLEnabled() ) {
                    channel = new SecureNioChannel ( socket, bufhandler, this.selectorPool, this );
                } else {
                    channel = new NioChannel ( socket, bufhandler );
                }
            } else {
                channel.setIOChannel ( socket );
                channel.reset();
            }
            this.getPoller0().register ( channel );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            try {
                NioEndpoint.log.error ( "", t );
            } catch ( Throwable tt ) {
                ExceptionUtils.handleThrowable ( tt );
            }
            return false;
        }
        return true;
    }
    @Override
    protected Log getLog() {
        return NioEndpoint.log;
    }
    @Override
    protected SocketProcessorBase<NioChannel> createSocketProcessor ( final SocketWrapperBase<NioChannel> socketWrapper, final SocketEvent event ) {
        return new SocketProcessor ( socketWrapper, event );
    }
    private void close ( final NioChannel socket, final SelectionKey key ) {
        try {
            if ( socket.getPoller().cancelledKey ( key ) != null && this.running && !this.paused && !this.nioChannels.push ( socket ) ) {
                socket.free();
            }
        } catch ( Exception x ) {
            NioEndpoint.log.error ( "", x );
        }
    }
    private void closeSocket ( final SocketChannel socket ) {
        try {
            socket.socket().close();
        } catch ( IOException ioe ) {
            if ( NioEndpoint.log.isDebugEnabled() ) {
                NioEndpoint.log.debug ( "", ioe );
            }
        }
        try {
            socket.close();
        } catch ( IOException ioe ) {
            if ( NioEndpoint.log.isDebugEnabled() ) {
                NioEndpoint.log.debug ( "", ioe );
            }
        }
    }
    static {
        log = LogFactory.getLog ( NioEndpoint.class );
    }
    protected class Acceptor extends AbstractEndpoint.Acceptor {
        @Override
        public void run() {
            int errorDelay = 0;
            while ( NioEndpoint.this.running ) {
                while ( NioEndpoint.this.paused && NioEndpoint.this.running ) {
                    this.state = AcceptorState.PAUSED;
                    try {
                        Thread.sleep ( 50L );
                    } catch ( InterruptedException ex ) {}
                }
                if ( !NioEndpoint.this.running ) {
                    break;
                }
                this.state = AcceptorState.RUNNING;
                try {
                    NioEndpoint.this.countUpOrAwaitConnection();
                    SocketChannel socket = null;
                    try {
                        socket = NioEndpoint.this.serverSock.accept();
                    } catch ( IOException ioe ) {
                        NioEndpoint.this.countDownConnection();
                        errorDelay = NioEndpoint.this.handleExceptionWithDelay ( errorDelay );
                        throw ioe;
                    }
                    errorDelay = 0;
                    if ( NioEndpoint.this.running && !NioEndpoint.this.paused ) {
                        if ( NioEndpoint.this.setSocketOptions ( socket ) ) {
                            continue;
                        }
                        NioEndpoint.this.countDownConnection();
                        NioEndpoint.this.closeSocket ( socket );
                    } else {
                        NioEndpoint.this.countDownConnection();
                        NioEndpoint.this.closeSocket ( socket );
                    }
                } catch ( SocketTimeoutException ex2 ) {}
                catch ( IOException x ) {
                    if ( !NioEndpoint.this.running ) {
                        continue;
                    }
                    NioEndpoint.log.error ( AbstractEndpoint.sm.getString ( "endpoint.accept.fail" ), x );
                } catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                    NioEndpoint.log.error ( AbstractEndpoint.sm.getString ( "endpoint.accept.fail" ), t );
                }
            }
            this.state = AcceptorState.ENDED;
        }
    }
    public static class PollerEvent implements Runnable {
        private NioChannel socket;
        private int interestOps;
        private NioSocketWrapper socketWrapper;
        public PollerEvent ( final NioChannel ch, final NioSocketWrapper w, final int intOps ) {
            this.reset ( ch, w, intOps );
        }
        public void reset ( final NioChannel ch, final NioSocketWrapper w, final int intOps ) {
            this.socket = ch;
            this.interestOps = intOps;
            this.socketWrapper = w;
        }
        public void reset() {
            this.reset ( null, null, 0 );
        }
        @Override
        public void run() {
            if ( this.interestOps == 256 ) {
                try {
                    this.socket.getIOChannel().register ( this.socket.getPoller().getSelector(), 1, this.socketWrapper );
                } catch ( Exception x ) {
                    NioEndpoint.log.error ( AbstractEndpoint.sm.getString ( "endpoint.nio.registerFail" ), x );
                }
            } else {
                final SelectionKey key = this.socket.getIOChannel().keyFor ( this.socket.getPoller().getSelector() );
                try {
                    if ( key == null ) {
                        this.socket.socketWrapper.getEndpoint().countDownConnection();
                    } else {
                        final NioSocketWrapper socketWrapper = ( NioSocketWrapper ) key.attachment();
                        if ( socketWrapper != null ) {
                            final int ops = key.interestOps() | this.interestOps;
                            socketWrapper.interestOps ( ops );
                            key.interestOps ( ops );
                        } else {
                            this.socket.getPoller().cancelledKey ( key );
                        }
                    }
                } catch ( CancelledKeyException ckx ) {
                    try {
                        this.socket.getPoller().cancelledKey ( key );
                    } catch ( Exception ex ) {}
                }
            }
        }
        @Override
        public String toString() {
            return "Poller event: socket [" + this.socket + "], socketWrapper [" + this.socketWrapper + "], interstOps [" + this.interestOps + "]";
        }
    }
    public class Poller implements Runnable {
        private Selector selector;
        private final SynchronizedQueue<PollerEvent> events;
        private volatile boolean close;
        private long nextExpiration;
        private AtomicLong wakeupCounter;
        private volatile int keyCount;
        public Poller() throws IOException {
            this.events = new SynchronizedQueue<PollerEvent>();
            this.close = false;
            this.nextExpiration = 0L;
            this.wakeupCounter = new AtomicLong ( 0L );
            this.keyCount = 0;
            this.selector = Selector.open();
        }
        public int getKeyCount() {
            return this.keyCount;
        }
        public Selector getSelector() {
            return this.selector;
        }
        protected void destroy() {
            this.close = true;
            this.selector.wakeup();
        }
        private void addEvent ( final PollerEvent event ) {
            this.events.offer ( event );
            if ( this.wakeupCounter.incrementAndGet() == 0L ) {
                this.selector.wakeup();
            }
        }
        public void add ( final NioChannel socket, final int interestOps ) {
            PollerEvent r = NioEndpoint.this.eventCache.pop();
            if ( r == null ) {
                r = new PollerEvent ( socket, null, interestOps );
            } else {
                r.reset ( socket, null, interestOps );
            }
            this.addEvent ( r );
            if ( this.close ) {
                final NioSocketWrapper ka = ( NioSocketWrapper ) socket.getAttachment();
                NioEndpoint.this.processSocket ( ka, SocketEvent.STOP, false );
            }
        }
        public boolean events() {
            boolean result = false;
            PollerEvent pe = null;
            while ( ( pe = this.events.poll() ) != null ) {
                result = true;
                try {
                    pe.run();
                    pe.reset();
                    if ( !NioEndpoint.this.running || NioEndpoint.this.paused ) {
                        continue;
                    }
                    NioEndpoint.this.eventCache.push ( pe );
                } catch ( Throwable x ) {
                    NioEndpoint.log.error ( "", x );
                }
            }
            return result;
        }
        public void register ( final NioChannel socket ) {
            socket.setPoller ( this );
            final NioSocketWrapper ka = new NioSocketWrapper ( socket, NioEndpoint.this );
            socket.setSocketWrapper ( ka );
            ka.setPoller ( this );
            ka.setReadTimeout ( NioEndpoint.this.getSocketProperties().getSoTimeout() );
            ka.setWriteTimeout ( NioEndpoint.this.getSocketProperties().getSoTimeout() );
            ka.setKeepAliveLeft ( NioEndpoint.this.getMaxKeepAliveRequests() );
            ka.setSecure ( NioEndpoint.this.isSSLEnabled() );
            ka.setReadTimeout ( NioEndpoint.this.getConnectionTimeout() );
            ka.setWriteTimeout ( NioEndpoint.this.getConnectionTimeout() );
            PollerEvent r = NioEndpoint.this.eventCache.pop();
            ka.interestOps ( 1 );
            if ( r == null ) {
                r = new PollerEvent ( socket, ka, 256 );
            } else {
                r.reset ( socket, ka, 256 );
            }
            this.addEvent ( r );
        }
        public NioSocketWrapper cancelledKey ( final SelectionKey key ) {
            NioSocketWrapper ka = null;
            try {
                if ( key == null ) {
                    return null;
                }
                ka = ( NioSocketWrapper ) key.attach ( null );
                if ( ka != null ) {
                    NioEndpoint.this.getHandler().release ( ka );
                }
                if ( key.isValid() ) {
                    key.cancel();
                }
                if ( key.channel().isOpen() ) {
                    try {
                        key.channel().close();
                    } catch ( Exception e ) {
                        if ( NioEndpoint.log.isDebugEnabled() ) {
                            NioEndpoint.log.debug ( AbstractEndpoint.sm.getString ( "endpoint.debug.channelCloseFail" ), e );
                        }
                    }
                }
                try {
                    if ( ka != null ) {
                        ka.getSocket().close ( true );
                    }
                } catch ( Exception e ) {
                    if ( NioEndpoint.log.isDebugEnabled() ) {
                        NioEndpoint.log.debug ( AbstractEndpoint.sm.getString ( "endpoint.debug.socketCloseFail" ), e );
                    }
                }
                try {
                    if ( ka != null && ka.getSendfileData() != null && ka.getSendfileData().fchannel != null && ka.getSendfileData().fchannel.isOpen() ) {
                        ka.getSendfileData().fchannel.close();
                    }
                } catch ( Exception ex ) {}
                if ( ka != null ) {
                    NioEndpoint.this.countDownConnection();
                }
            } catch ( Throwable e2 ) {
                ExceptionUtils.handleThrowable ( e2 );
                if ( NioEndpoint.log.isDebugEnabled() ) {
                    NioEndpoint.log.error ( "", e2 );
                }
            }
            return ka;
        }
        @Override
        public void run() {
            while ( true ) {
                boolean hasEvents = false;
                try {
                    if ( !this.close ) {
                        hasEvents = this.events();
                        if ( this.wakeupCounter.getAndSet ( -1L ) > 0L ) {
                            this.keyCount = this.selector.selectNow();
                        } else {
                            this.keyCount = this.selector.select ( NioEndpoint.this.selectorTimeout );
                        }
                        this.wakeupCounter.set ( 0L );
                    }
                    if ( this.close ) {
                        this.events();
                        this.timeout ( 0, false );
                        try {
                            this.selector.close();
                        } catch ( IOException ioe ) {
                            NioEndpoint.log.error ( AbstractEndpoint.sm.getString ( "endpoint.nio.selectorCloseFail" ), ioe );
                        }
                        break;
                    }
                } catch ( Throwable x ) {
                    ExceptionUtils.handleThrowable ( x );
                    NioEndpoint.log.error ( "", x );
                    continue;
                }
                if ( this.keyCount == 0 ) {
                    hasEvents |= this.events();
                }
                final Iterator<SelectionKey> iterator = ( this.keyCount > 0 ) ? this.selector.selectedKeys().iterator() : null;
                while ( iterator != null && iterator.hasNext() ) {
                    final SelectionKey sk = iterator.next();
                    final NioSocketWrapper attachment = ( NioSocketWrapper ) sk.attachment();
                    if ( attachment == null ) {
                        iterator.remove();
                    } else {
                        iterator.remove();
                        this.processKey ( sk, attachment );
                    }
                }
                this.timeout ( this.keyCount, hasEvents );
            }
            NioEndpoint.this.stopLatch.countDown();
        }
        protected void processKey ( final SelectionKey sk, final NioSocketWrapper attachment ) {
            try {
                if ( this.close ) {
                    this.cancelledKey ( sk );
                } else if ( sk.isValid() && attachment != null ) {
                    if ( sk.isReadable() || sk.isWritable() ) {
                        if ( attachment.getSendfileData() != null ) {
                            this.processSendfile ( sk, attachment, false );
                        } else {
                            this.unreg ( sk, attachment, sk.readyOps() );
                            boolean closeSocket = false;
                            if ( sk.isReadable() && !NioEndpoint.this.processSocket ( attachment, SocketEvent.OPEN_READ, true ) ) {
                                closeSocket = true;
                            }
                            if ( !closeSocket && sk.isWritable() && !NioEndpoint.this.processSocket ( attachment, SocketEvent.OPEN_WRITE, true ) ) {
                                closeSocket = true;
                            }
                            if ( closeSocket ) {
                                this.cancelledKey ( sk );
                            }
                        }
                    }
                } else {
                    this.cancelledKey ( sk );
                }
            } catch ( CancelledKeyException ckx ) {
                this.cancelledKey ( sk );
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                NioEndpoint.log.error ( "", t );
            }
        }
        public SendfileState processSendfile ( final SelectionKey sk, final NioSocketWrapper socketWrapper, final boolean calledByProcessor ) {
            NioChannel sc = null;
            try {
                this.unreg ( sk, socketWrapper, sk.readyOps() );
                final SendfileData sd = socketWrapper.getSendfileData();
                if ( NioEndpoint.log.isTraceEnabled() ) {
                    NioEndpoint.log.trace ( "Processing send file for: " + sd.fileName );
                }
                if ( sd.fchannel == null ) {
                    final File f = new File ( sd.fileName );
                    if ( !f.exists() ) {
                        this.cancelledKey ( sk );
                        return SendfileState.ERROR;
                    }
                    final FileInputStream fis = new FileInputStream ( f );
                    sd.fchannel = fis.getChannel();
                }
                sc = socketWrapper.getSocket();
                final WritableByteChannel wc = ( sc instanceof SecureNioChannel ) ? sc : sc.getIOChannel();
                if ( sc.getOutboundRemaining() > 0 ) {
                    if ( sc.flushOutbound() ) {
                        socketWrapper.updateLastWrite();
                    }
                } else {
                    final long written = sd.fchannel.transferTo ( sd.pos, sd.length, wc );
                    if ( written > 0L ) {
                        final SendfileData sendfileData = sd;
                        sendfileData.pos += written;
                        final SendfileData sendfileData2 = sd;
                        sendfileData2.length -= written;
                        socketWrapper.updateLastWrite();
                    } else if ( sd.fchannel.size() <= sd.pos ) {
                        throw new IOException ( "Sendfile configured to send more data than was available" );
                    }
                }
                if ( sd.length <= 0L && sc.getOutboundRemaining() <= 0 ) {
                    if ( NioEndpoint.log.isDebugEnabled() ) {
                        NioEndpoint.log.debug ( "Send file complete for: " + sd.fileName );
                    }
                    socketWrapper.setSendfileData ( null );
                    try {
                        sd.fchannel.close();
                    } catch ( Exception ex ) {}
                    if ( !calledByProcessor ) {
                        if ( sd.keepAlive ) {
                            if ( NioEndpoint.log.isDebugEnabled() ) {
                                NioEndpoint.log.debug ( "Connection is keep alive, registering back for OP_READ" );
                            }
                            this.reg ( sk, socketWrapper, 1 );
                        } else {
                            if ( NioEndpoint.log.isDebugEnabled() ) {
                                NioEndpoint.log.debug ( "Send file connection is being closed" );
                            }
                            NioEndpoint.this.close ( sc, sk );
                        }
                    }
                    return SendfileState.DONE;
                }
                if ( NioEndpoint.log.isDebugEnabled() ) {
                    NioEndpoint.log.debug ( "OP_WRITE for sendfile: " + sd.fileName );
                }
                if ( calledByProcessor ) {
                    this.add ( socketWrapper.getSocket(), 4 );
                } else {
                    this.reg ( sk, socketWrapper, 4 );
                }
                return SendfileState.PENDING;
            } catch ( IOException x ) {
                if ( NioEndpoint.log.isDebugEnabled() ) {
                    NioEndpoint.log.debug ( "Unable to complete sendfile request:", x );
                }
                if ( !calledByProcessor && sc != null ) {
                    NioEndpoint.this.close ( sc, sk );
                } else {
                    this.cancelledKey ( sk );
                }
                return SendfileState.ERROR;
            } catch ( Throwable t ) {
                NioEndpoint.log.error ( "", t );
                if ( !calledByProcessor && sc != null ) {
                    NioEndpoint.this.close ( sc, sk );
                } else {
                    this.cancelledKey ( sk );
                }
                return SendfileState.ERROR;
            }
        }
        protected void unreg ( final SelectionKey sk, final NioSocketWrapper attachment, final int readyOps ) {
            this.reg ( sk, attachment, sk.interestOps() & ~readyOps );
        }
        protected void reg ( final SelectionKey sk, final NioSocketWrapper attachment, final int intops ) {
            sk.interestOps ( intops );
            attachment.interestOps ( intops );
        }
        protected void timeout ( final int keyCount, final boolean hasEvents ) {
            final long now = System.currentTimeMillis();
            if ( this.nextExpiration > 0L && ( keyCount > 0 || hasEvents ) && now < this.nextExpiration && !this.close ) {
                return;
            }
            int keycount = 0;
            try {
                for ( final SelectionKey key : this.selector.keys() ) {
                    ++keycount;
                    try {
                        final NioSocketWrapper ka = ( NioSocketWrapper ) key.attachment();
                        if ( ka == null ) {
                            this.cancelledKey ( key );
                        } else if ( this.close ) {
                            key.interestOps ( 0 );
                            ka.interestOps ( 0 );
                            this.processKey ( key, ka );
                        } else {
                            if ( ( ka.interestOps() & 0x1 ) != 0x1 && ( ka.interestOps() & 0x4 ) != 0x4 ) {
                                continue;
                            }
                            boolean isTimedOut = false;
                            if ( ( ka.interestOps() & 0x1 ) == 0x1 ) {
                                final long delta = now - ka.getLastRead();
                                final long timeout = ka.getReadTimeout();
                                isTimedOut = ( timeout > 0L && delta > timeout );
                            }
                            if ( !isTimedOut && ( ka.interestOps() & 0x4 ) == 0x4 ) {
                                final long delta = now - ka.getLastWrite();
                                final long timeout = ka.getWriteTimeout();
                                isTimedOut = ( timeout > 0L && delta > timeout );
                            }
                            if ( !isTimedOut ) {
                                continue;
                            }
                            key.interestOps ( 0 );
                            ka.interestOps ( 0 );
                            ka.setError ( new SocketTimeoutException() );
                            if ( NioEndpoint.this.processSocket ( ka, SocketEvent.ERROR, true ) ) {
                                continue;
                            }
                            this.cancelledKey ( key );
                        }
                    } catch ( CancelledKeyException ckx ) {
                        this.cancelledKey ( key );
                    }
                }
            } catch ( ConcurrentModificationException cme ) {
                NioEndpoint.log.warn ( AbstractEndpoint.sm.getString ( "endpoint.nio.timeoutCme" ), cme );
            }
            final long prevExp = this.nextExpiration;
            this.nextExpiration = System.currentTimeMillis() + NioEndpoint.this.socketProperties.getTimeoutInterval();
            if ( NioEndpoint.log.isTraceEnabled() ) {
                NioEndpoint.log.trace ( "timeout completed: keys processed=" + keycount + "; now=" + now + "; nextExpiration=" + prevExp + "; keyCount=" + keyCount + "; hasEvents=" + hasEvents + "; eval=" + ( now < prevExp && ( keyCount > 0 || hasEvents ) && !this.close ) );
            }
        }
    }
    public static class NioSocketWrapper extends SocketWrapperBase<NioChannel> {
        private final NioSelectorPool pool;
        private Poller poller;
        private int interestOps;
        private CountDownLatch readLatch;
        private CountDownLatch writeLatch;
        private volatile SendfileData sendfileData;
        private volatile long lastRead;
        private volatile long lastWrite;
        public NioSocketWrapper ( final NioChannel channel, final NioEndpoint endpoint ) {
            super ( channel, endpoint );
            this.poller = null;
            this.interestOps = 0;
            this.readLatch = null;
            this.writeLatch = null;
            this.sendfileData = null;
            this.lastRead = System.currentTimeMillis();
            this.lastWrite = this.lastRead;
            this.pool = endpoint.getSelectorPool();
            this.socketBufferHandler = channel.getBufHandler();
        }
        public Poller getPoller() {
            return this.poller;
        }
        public void setPoller ( final Poller poller ) {
            this.poller = poller;
        }
        public int interestOps() {
            return this.interestOps;
        }
        public int interestOps ( final int ops ) {
            return this.interestOps = ops;
        }
        public CountDownLatch getReadLatch() {
            return this.readLatch;
        }
        public CountDownLatch getWriteLatch() {
            return this.writeLatch;
        }
        protected CountDownLatch resetLatch ( final CountDownLatch latch ) {
            if ( latch == null || latch.getCount() == 0L ) {
                return null;
            }
            throw new IllegalStateException ( "Latch must be at count 0" );
        }
        public void resetReadLatch() {
            this.readLatch = this.resetLatch ( this.readLatch );
        }
        public void resetWriteLatch() {
            this.writeLatch = this.resetLatch ( this.writeLatch );
        }
        protected CountDownLatch startLatch ( final CountDownLatch latch, final int cnt ) {
            if ( latch == null || latch.getCount() == 0L ) {
                return new CountDownLatch ( cnt );
            }
            throw new IllegalStateException ( "Latch must be at count 0 or null." );
        }
        public void startReadLatch ( final int cnt ) {
            this.readLatch = this.startLatch ( this.readLatch, cnt );
        }
        public void startWriteLatch ( final int cnt ) {
            this.writeLatch = this.startLatch ( this.writeLatch, cnt );
        }
        protected void awaitLatch ( final CountDownLatch latch, final long timeout, final TimeUnit unit ) throws InterruptedException {
            if ( latch == null ) {
                throw new IllegalStateException ( "Latch cannot be null" );
            }
            latch.await ( timeout, unit );
        }
        public void awaitReadLatch ( final long timeout, final TimeUnit unit ) throws InterruptedException {
            this.awaitLatch ( this.readLatch, timeout, unit );
        }
        public void awaitWriteLatch ( final long timeout, final TimeUnit unit ) throws InterruptedException {
            this.awaitLatch ( this.writeLatch, timeout, unit );
        }
        public void setSendfileData ( final SendfileData sf ) {
            this.sendfileData = sf;
        }
        public SendfileData getSendfileData() {
            return this.sendfileData;
        }
        public void updateLastWrite() {
            this.lastWrite = System.currentTimeMillis();
        }
        public long getLastWrite() {
            return this.lastWrite;
        }
        public void updateLastRead() {
            this.lastRead = System.currentTimeMillis();
        }
        public long getLastRead() {
            return this.lastRead;
        }
        @Override
        public boolean isReadyForRead() throws IOException {
            this.socketBufferHandler.configureReadBufferForRead();
            if ( this.socketBufferHandler.getReadBuffer().remaining() > 0 ) {
                return true;
            }
            this.fillReadBuffer ( false );
            final boolean isReady = this.socketBufferHandler.getReadBuffer().position() > 0;
            return isReady;
        }
        @Override
        public int read ( final boolean block, final byte[] b, final int off, final int len ) throws IOException {
            int nRead = this.populateReadBuffer ( b, off, len );
            if ( nRead > 0 ) {
                return nRead;
            }
            nRead = this.fillReadBuffer ( block );
            this.updateLastRead();
            if ( nRead > 0 ) {
                this.socketBufferHandler.configureReadBufferForRead();
                nRead = Math.min ( nRead, len );
                this.socketBufferHandler.getReadBuffer().get ( b, off, nRead );
            }
            return nRead;
        }
        @Override
        public int read ( final boolean block, final ByteBuffer to ) throws IOException {
            int nRead = this.populateReadBuffer ( to );
            if ( nRead > 0 ) {
                return nRead;
            }
            final int limit = this.socketBufferHandler.getReadBuffer().capacity();
            if ( to.remaining() >= limit ) {
                to.limit ( to.position() + limit );
                nRead = this.fillReadBuffer ( block, to );
                this.updateLastRead();
            } else {
                nRead = this.fillReadBuffer ( block );
                this.updateLastRead();
                if ( nRead > 0 ) {
                    nRead = this.populateReadBuffer ( to );
                }
            }
            return nRead;
        }
        @Override
        public void close() throws IOException {
            this.getSocket().close();
        }
        @Override
        public boolean isClosed() {
            return !this.getSocket().isOpen();
        }
        private int fillReadBuffer ( final boolean block ) throws IOException {
            this.socketBufferHandler.configureReadBufferForWrite();
            return this.fillReadBuffer ( block, this.socketBufferHandler.getReadBuffer() );
        }
        private int fillReadBuffer ( final boolean block, final ByteBuffer to ) throws IOException {
            final NioChannel channel = this.getSocket();
            int nRead;
            if ( block ) {
                Selector selector = null;
                try {
                    selector = this.pool.get();
                } catch ( IOException ex ) {}
                try {
                    final NioSocketWrapper att = ( NioSocketWrapper ) channel.getAttachment();
                    if ( att == null ) {
                        throw new IOException ( "Key must be cancelled." );
                    }
                    nRead = this.pool.read ( to, channel, selector, att.getReadTimeout() );
                } finally {
                    if ( selector != null ) {
                        this.pool.put ( selector );
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
        protected void doWrite ( final boolean block, final ByteBuffer from ) throws IOException {
            final long writeTimeout = this.getWriteTimeout();
            Selector selector = null;
            try {
                selector = this.pool.get();
            } catch ( IOException ex ) {}
            try {
                this.pool.write ( from, this.getSocket(), selector, writeTimeout, block );
                if ( block ) {
                    while ( !this.getSocket().flush ( true, selector, writeTimeout ) ) {}
                }
                this.updateLastWrite();
            } finally {
                if ( selector != null ) {
                    this.pool.put ( selector );
                }
            }
        }
        @Override
        public void registerReadInterest() {
            this.getPoller().add ( this.getSocket(), 1 );
        }
        @Override
        public void registerWriteInterest() {
            this.getPoller().add ( this.getSocket(), 4 );
        }
        @Override
        public SendfileDataBase createSendfileData ( final String filename, final long pos, final long length ) {
            return new SendfileData ( filename, pos, length );
        }
        @Override
        public SendfileState processSendfile ( final SendfileDataBase sendfileData ) {
            this.setSendfileData ( ( SendfileData ) sendfileData );
            final SelectionKey key = this.getSocket().getIOChannel().keyFor ( this.getSocket().getPoller().getSelector() );
            return this.getSocket().getPoller().processSendfile ( key, this, true );
        }
        @Override
        protected void populateRemoteAddr() {
            final InetAddress inetAddr = this.getSocket().getIOChannel().socket().getInetAddress();
            if ( inetAddr != null ) {
                this.remoteAddr = inetAddr.getHostAddress();
            }
        }
        @Override
        protected void populateRemoteHost() {
            final InetAddress inetAddr = this.getSocket().getIOChannel().socket().getInetAddress();
            if ( inetAddr != null ) {
                this.remoteHost = inetAddr.getHostName();
                if ( this.remoteAddr == null ) {
                    this.remoteAddr = inetAddr.getHostAddress();
                }
            }
        }
        @Override
        protected void populateRemotePort() {
            this.remotePort = this.getSocket().getIOChannel().socket().getPort();
        }
        @Override
        protected void populateLocalName() {
            final InetAddress inetAddr = this.getSocket().getIOChannel().socket().getLocalAddress();
            if ( inetAddr != null ) {
                this.localName = inetAddr.getHostName();
            }
        }
        @Override
        protected void populateLocalAddr() {
            final InetAddress inetAddr = this.getSocket().getIOChannel().socket().getLocalAddress();
            if ( inetAddr != null ) {
                this.localAddr = inetAddr.getHostAddress();
            }
        }
        @Override
        protected void populateLocalPort() {
            this.localPort = this.getSocket().getIOChannel().socket().getLocalPort();
        }
        @Override
        public SSLSupport getSslSupport ( final String clientCertProvider ) {
            if ( this.getSocket() instanceof SecureNioChannel ) {
                final SecureNioChannel ch = ( ( SocketWrapperBase<SecureNioChannel> ) this ).getSocket();
                final SSLSession session = ch.getSslEngine().getSession();
                return ( ( NioEndpoint ) this.getEndpoint() ).getSslImplementation().getSSLSupport ( session );
            }
            return null;
        }
        @Override
        public void doClientAuth ( final SSLSupport sslSupport ) {
            final SecureNioChannel sslChannel = ( ( SocketWrapperBase<SecureNioChannel> ) this ).getSocket();
            final SSLEngine engine = sslChannel.getSslEngine();
            if ( !engine.getNeedClientAuth() ) {
                engine.setNeedClientAuth ( true );
                try {
                    sslChannel.rehandshake ( this.getEndpoint().getConnectionTimeout() );
                    ( ( JSSESupport ) sslSupport ).setSession ( engine.getSession() );
                } catch ( IOException ioe ) {
                    NioEndpoint.log.warn ( NioSocketWrapper.sm.getString ( "socket.sslreneg", ioe ) );
                }
            }
        }
        @Override
        public void setAppReadBufHandler ( final ApplicationBufferHandler handler ) {
            this.getSocket().setAppReadBufHandler ( handler );
        }
    }
    protected class SocketProcessor extends SocketProcessorBase<NioChannel> {
        public SocketProcessor ( final SocketWrapperBase<NioChannel> socketWrapper, final SocketEvent event ) {
            super ( socketWrapper, event );
        }
        @Override
        protected void doRun() {
            final NioChannel socket = ( NioChannel ) this.socketWrapper.getSocket();
            final SelectionKey key = socket.getIOChannel().keyFor ( socket.getPoller().getSelector() );
            try {
                int handshake = -1;
                try {
                    if ( key != null ) {
                        if ( socket.isHandshakeComplete() ) {
                            handshake = 0;
                        } else if ( this.event == SocketEvent.STOP || this.event == SocketEvent.DISCONNECT || this.event == SocketEvent.ERROR ) {
                            handshake = -1;
                        } else {
                            handshake = socket.handshake ( key.isReadable(), key.isWritable() );
                            this.event = SocketEvent.OPEN_READ;
                        }
                    }
                } catch ( IOException x ) {
                    handshake = -1;
                    if ( NioEndpoint.log.isDebugEnabled() ) {
                        NioEndpoint.log.debug ( "Error during SSL handshake", x );
                    }
                } catch ( CancelledKeyException ckx ) {
                    handshake = -1;
                }
                if ( handshake == 0 ) {
                    Handler.SocketState state = Handler.SocketState.OPEN;
                    if ( this.event == null ) {
                        state = NioEndpoint.this.getHandler().process ( ( SocketWrapperBase<NioChannel> ) this.socketWrapper, SocketEvent.OPEN_READ );
                    } else {
                        state = NioEndpoint.this.getHandler().process ( ( SocketWrapperBase<NioChannel> ) this.socketWrapper, this.event );
                    }
                    if ( state == Handler.SocketState.CLOSED ) {
                        NioEndpoint.this.close ( socket, key );
                    }
                } else if ( handshake == -1 ) {
                    NioEndpoint.this.close ( socket, key );
                } else if ( handshake == 1 ) {
                    this.socketWrapper.registerReadInterest();
                } else if ( handshake == 4 ) {
                    this.socketWrapper.registerWriteInterest();
                }
            } catch ( CancelledKeyException cx ) {
                socket.getPoller().cancelledKey ( key );
            } catch ( VirtualMachineError vme ) {
                ExceptionUtils.handleThrowable ( vme );
            } catch ( Throwable t ) {
                NioEndpoint.log.error ( "", t );
                socket.getPoller().cancelledKey ( key );
            } finally {
                this.socketWrapper = null;
                this.event = null;
                if ( NioEndpoint.this.running && !NioEndpoint.this.paused ) {
                    NioEndpoint.this.processorCache.push ( ( SocketProcessorBase<S> ) this );
                }
            }
        }
    }
    public static class SendfileData extends SendfileDataBase {
        protected volatile FileChannel fchannel;
        public SendfileData ( final String filename, final long pos, final long length ) {
            super ( filename, pos, length );
        }
    }
}
