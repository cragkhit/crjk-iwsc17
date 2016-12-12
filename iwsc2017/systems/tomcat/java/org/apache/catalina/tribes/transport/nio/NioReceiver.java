package org.apache.catalina.tribes.transport.nio;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Deque;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.catalina.tribes.io.ObjectReader;
import org.apache.catalina.tribes.transport.AbstractRxTask;
import org.apache.catalina.tribes.transport.ReceiverBase;
import org.apache.catalina.tribes.transport.RxTaskPool;
import org.apache.catalina.tribes.util.ExceptionUtils;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class NioReceiver extends ReceiverBase implements Runnable {
    private static final Log log = LogFactory.getLog ( NioReceiver.class );
    protected static final StringManager sm = StringManager.getManager ( NioReceiver.class );
    private volatile boolean running = false;
    private AtomicReference<Selector> selector = new AtomicReference<>();
    private ServerSocketChannel serverChannel = null;
    private DatagramChannel datagramChannel = null;
    protected final Deque<Runnable> events = new ConcurrentLinkedDeque<>();
    public NioReceiver() {
    }
    @Override
    public void stop() {
        this.stopListening();
        super.stop();
    }
    @Override
    public void start() throws IOException {
        super.start();
        try {
            setPool ( new RxTaskPool ( getMaxThreads(), getMinThreads(), this ) );
        } catch ( Exception x ) {
            log.fatal ( sm.getString ( "nioReceiver.threadpool.fail" ), x );
            if ( x instanceof IOException ) {
                throw ( IOException ) x;
            } else {
                throw new IOException ( x.getMessage() );
            }
        }
        try {
            getBind();
            bind();
            String channelName = "";
            if ( getChannel().getName() != null ) {
                channelName = "[" + getChannel().getName() + "]";
            }
            Thread t = new Thread ( this, "NioReceiver" + channelName );
            t.setDaemon ( true );
            t.start();
        } catch ( Exception x ) {
            log.fatal ( sm.getString ( "nioReceiver.start.fail" ), x );
            if ( x instanceof IOException ) {
                throw ( IOException ) x;
            } else {
                throw new IOException ( x.getMessage() );
            }
        }
    }
    @Override
    public AbstractRxTask createRxTask() {
        NioReplicationTask thread = new NioReplicationTask ( this, this );
        thread.setUseBufferPool ( this.getUseBufferPool() );
        thread.setRxBufSize ( getRxBufSize() );
        thread.setOptions ( getWorkerThreadOptions() );
        return thread;
    }
    protected void bind() throws IOException {
        serverChannel = ServerSocketChannel.open();
        ServerSocket serverSocket = serverChannel.socket();
        this.selector.set ( Selector.open() );
        bind ( serverSocket, getPort(), getAutoBind() );
        serverChannel.configureBlocking ( false );
        serverChannel.register ( this.selector.get(), SelectionKey.OP_ACCEPT );
        if ( this.getUdpPort() > 0 ) {
            datagramChannel = DatagramChannel.open();
            configureDatagraChannel();
            bindUdp ( datagramChannel.socket(), getUdpPort(), getAutoBind() );
        }
    }
    private void configureDatagraChannel() throws IOException {
        datagramChannel.configureBlocking ( false );
        datagramChannel.socket().setSendBufferSize ( getUdpTxBufSize() );
        datagramChannel.socket().setReceiveBufferSize ( getUdpRxBufSize() );
        datagramChannel.socket().setReuseAddress ( getSoReuseAddress() );
        datagramChannel.socket().setSoTimeout ( getTimeout() );
        datagramChannel.socket().setTrafficClass ( getSoTrafficClass() );
    }
    public void addEvent ( Runnable event ) {
        Selector selector = this.selector.get();
        if ( selector != null ) {
            events.add ( event );
            if ( log.isTraceEnabled() ) {
                log.trace ( "Adding event to selector:" + event );
            }
            if ( isListening() ) {
                selector.wakeup();
            }
        }
    }
    public void events() {
        if ( events.isEmpty() ) {
            return;
        }
        Runnable r = null;
        while ( ( r = events.pollFirst() ) != null ) {
            try {
                if ( log.isTraceEnabled() ) {
                    log.trace ( "Processing event in selector:" + r );
                }
                r.run();
            } catch ( Exception x ) {
                log.error ( "", x );
            }
        }
    }
    public static void cancelledKey ( SelectionKey key ) {
        ObjectReader reader = ( ObjectReader ) key.attachment();
        if ( reader != null ) {
            reader.setCancelled ( true );
            reader.finish();
        }
        key.cancel();
        key.attach ( null );
        if ( key.channel() instanceof SocketChannel )
            try {
                ( ( SocketChannel ) key.channel() ).socket().close();
            } catch ( IOException e ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "", e );
                }
            }
        if ( key.channel() instanceof DatagramChannel )
            try {
                ( ( DatagramChannel ) key.channel() ).socket().close();
            } catch ( Exception e ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "", e );
                }
            }
        try {
            key.channel().close();
        } catch ( IOException e ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( "", e );
            }
        }
    }
    protected long lastCheck = System.currentTimeMillis();
    protected void socketTimeouts() {
        long now = System.currentTimeMillis();
        if ( ( now - lastCheck ) < getSelectorTimeout() ) {
            return;
        }
        Selector tmpsel = this.selector.get();
        Set<SelectionKey> keys = ( isListening() && tmpsel != null ) ? tmpsel.keys() : null;
        if ( keys == null ) {
            return;
        }
        for ( Iterator<SelectionKey> iter = keys.iterator(); iter.hasNext(); ) {
            SelectionKey key = iter.next();
            try {
                if ( key.interestOps() == 0 ) {
                    ObjectReader ka = ( ObjectReader ) key.attachment();
                    if ( ka != null ) {
                        long delta = now - ka.getLastAccess();
                        if ( delta > getTimeout() && ( !ka.isAccessed() ) ) {
                            if ( log.isWarnEnabled() )
                                log.warn ( sm.getString (
                                               "nioReceiver.threadsExhausted",
                                               Integer.valueOf ( getTimeout() ),
                                               Boolean.valueOf ( ka.isCancelled() ),
                                               key,
                                               new java.sql.Timestamp ( ka.getLastAccess() ) ) );
                            ka.setLastAccess ( now );
                        }
                    } else {
                        cancelledKey ( key );
                    }
                }
            } catch ( CancelledKeyException ckx ) {
                cancelledKey ( key );
            }
        }
        lastCheck = System.currentTimeMillis();
    }
    protected void listen() throws Exception {
        if ( doListen() ) {
            log.warn ( sm.getString ( "nioReceiver.alreadyStarted" ) );
            return;
        }
        setListen ( true );
        Selector selector = this.selector.get();
        if ( selector != null && datagramChannel != null ) {
            ObjectReader oreader = new ObjectReader ( MAX_UDP_SIZE );
            registerChannel ( selector, datagramChannel, SelectionKey.OP_READ, oreader );
        }
        while ( doListen() && selector != null ) {
            try {
                events();
                socketTimeouts();
                int n = selector.select ( getSelectorTimeout() );
                if ( n == 0 ) {
                    continue;
                }
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while ( it != null && it.hasNext() ) {
                    SelectionKey key = it.next();
                    if ( key.isAcceptable() ) {
                        ServerSocketChannel server = ( ServerSocketChannel ) key.channel();
                        SocketChannel channel = server.accept();
                        channel.socket().setReceiveBufferSize ( getTxBufSize() );
                        channel.socket().setSendBufferSize ( getTxBufSize() );
                        channel.socket().setTcpNoDelay ( getTcpNoDelay() );
                        channel.socket().setKeepAlive ( getSoKeepAlive() );
                        channel.socket().setOOBInline ( getOoBInline() );
                        channel.socket().setReuseAddress ( getSoReuseAddress() );
                        channel.socket().setSoLinger ( getSoLingerOn(), getSoLingerTime() );
                        channel.socket().setSoTimeout ( getTimeout() );
                        Object attach = new ObjectReader ( channel );
                        registerChannel ( selector,
                                          channel,
                                          SelectionKey.OP_READ,
                                          attach );
                    }
                    if ( key.isReadable() ) {
                        readDataFromSocket ( key );
                    } else {
                        key.interestOps ( key.interestOps() & ( ~SelectionKey.OP_WRITE ) );
                    }
                    it.remove();
                }
            } catch ( java.nio.channels.ClosedSelectorException cse ) {
            } catch ( java.nio.channels.CancelledKeyException nx ) {
                log.warn ( sm.getString ( "nioReceiver.clientDisconnect" ) );
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                log.error ( sm.getString ( "nioReceiver.requestError" ), t );
            }
        }
        serverChannel.close();
        if ( datagramChannel != null ) {
            try {
                datagramChannel.close();
            } catch ( Exception iox ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "Unable to close datagram channel.", iox );
                }
            }
            datagramChannel = null;
        }
        closeSelector();
    }
    protected void stopListening() {
        setListen ( false );
        Selector selector = this.selector.get();
        if ( selector != null ) {
            try {
                selector.wakeup();
                int count = 0;
                while ( running && count < 50 ) {
                    Thread.sleep ( 100 );
                    count ++;
                }
                if ( running ) {
                    log.warn ( sm.getString ( "nioReceiver.stop.threadRunning" ) );
                }
                closeSelector();
            } catch ( Exception x ) {
                log.error ( sm.getString ( "nioReceiver.stop.fail" ), x );
            } finally {
                this.selector.set ( null );
            }
        }
    }
    private void closeSelector() throws IOException {
        Selector selector = this.selector.getAndSet ( null );
        if ( selector == null ) {
            return;
        }
        try {
            Iterator<SelectionKey> it = selector.keys().iterator();
            while ( it.hasNext() ) {
                SelectionKey key = it.next();
                key.channel().close();
                key.attach ( null );
                key.cancel();
            }
        } catch ( IOException ignore ) {
            if ( log.isWarnEnabled() ) {
                log.warn ( sm.getString ( "nioReceiver.cleanup.fail" ), ignore );
            }
        } catch ( ClosedSelectorException ignore ) {
        }
        try {
            selector.selectNow();
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
        }
        selector.close();
    }
    protected void registerChannel ( Selector selector,
                                     SelectableChannel channel,
                                     int ops,
                                     Object attach ) throws Exception {
        if ( channel == null ) {
            return;
        }
        channel.configureBlocking ( false );
        channel.register ( selector, ops, attach );
    }
    @Override
    public void run() {
        running = true;
        try {
            listen();
        } catch ( Exception x ) {
            log.error ( sm.getString ( "nioReceiver.run.fail" ), x );
        } finally {
            running = false;
        }
    }
    protected void readDataFromSocket ( SelectionKey key ) throws Exception {
        NioReplicationTask task = ( NioReplicationTask ) getTaskPool().getRxTask();
        if ( task == null ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( "No TcpReplicationThread available" );
            }
        } else {
            task.serviceChannel ( key );
            getExecutor().execute ( task );
        }
    }
}
