package org.apache.tomcat.util.net;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class NioSelectorPool {
    public NioSelectorPool() {
    }
    private static final Log log = LogFactory.getLog ( NioSelectorPool.class );
    protected static final boolean SHARED =
        Boolean.parseBoolean ( System.getProperty ( "org.apache.tomcat.util.net.NioSelectorShared", "true" ) );
    protected NioBlockingSelector blockingSelector;
    protected volatile Selector SHARED_SELECTOR;
    protected int maxSelectors = 200;
    protected long sharedSelectorTimeout = 30000;
    protected int maxSpareSelectors = -1;
    protected boolean enabled = true;
    protected AtomicInteger active = new AtomicInteger ( 0 );
    protected AtomicInteger spare = new AtomicInteger ( 0 );
    protected ConcurrentLinkedQueue<Selector> selectors =
        new ConcurrentLinkedQueue<>();
    protected Selector getSharedSelector() throws IOException {
        if ( SHARED && SHARED_SELECTOR == null ) {
            synchronized ( NioSelectorPool.class ) {
                if ( SHARED_SELECTOR == null )  {
                    SHARED_SELECTOR = Selector.open();
                    log.info ( "Using a shared selector for servlet write/read" );
                }
            }
        }
        return  SHARED_SELECTOR;
    }
    public Selector get() throws IOException {
        if ( SHARED ) {
            return getSharedSelector();
        }
        if ( ( !enabled ) || active.incrementAndGet() >= maxSelectors ) {
            if ( enabled ) {
                active.decrementAndGet();
            }
            return null;
        }
        Selector s = null;
        try {
            s = selectors.size() > 0 ? selectors.poll() : null;
            if ( s == null ) {
                s = Selector.open();
            } else {
                spare.decrementAndGet();
            }
        } catch ( NoSuchElementException x ) {
            try {
                s = Selector.open();
            } catch ( IOException iox ) {
            }
        } finally {
            if ( s == null ) {
                active.decrementAndGet();
            }
        }
        return s;
    }
    public void put ( Selector s ) throws IOException {
        if ( SHARED ) {
            return;
        }
        if ( enabled ) {
            active.decrementAndGet();
        }
        if ( enabled && ( maxSpareSelectors == -1 || spare.get() < Math.min ( maxSpareSelectors, maxSelectors ) ) ) {
            spare.incrementAndGet();
            selectors.offer ( s );
        } else {
            s.close();
        }
    }
    public void close() throws IOException {
        enabled = false;
        Selector s;
        while ( ( s = selectors.poll() ) != null ) {
            s.close();
        }
        spare.set ( 0 );
        active.set ( 0 );
        if ( blockingSelector != null ) {
            blockingSelector.close();
        }
        if ( SHARED && getSharedSelector() != null ) {
            getSharedSelector().close();
            SHARED_SELECTOR = null;
        }
    }
    public void open() throws IOException {
        enabled = true;
        getSharedSelector();
        if ( SHARED ) {
            blockingSelector = new NioBlockingSelector();
            blockingSelector.open ( getSharedSelector() );
        }
    }
    public int write ( ByteBuffer buf, NioChannel socket, Selector selector,
                       long writeTimeout, boolean block ) throws IOException {
        if ( SHARED && block ) {
            return blockingSelector.write ( buf, socket, writeTimeout );
        }
        SelectionKey key = null;
        int written = 0;
        boolean timedout = false;
        int keycount = 1;
        long time = System.currentTimeMillis();
        try {
            while ( ( !timedout ) && buf.hasRemaining() ) {
                int cnt = 0;
                if ( keycount > 0 ) {
                    cnt = socket.write ( buf );
                    if ( cnt == -1 ) {
                        throw new EOFException();
                    }
                    written += cnt;
                    if ( cnt > 0 ) {
                        time = System.currentTimeMillis();
                        continue;
                    }
                    if ( cnt == 0 && ( !block ) ) {
                        break;
                    }
                }
                if ( selector != null ) {
                    if ( key == null ) {
                        key = socket.getIOChannel().register ( selector, SelectionKey.OP_WRITE );
                    } else {
                        key.interestOps ( SelectionKey.OP_WRITE );
                    }
                    if ( writeTimeout == 0 ) {
                        timedout = buf.hasRemaining();
                    } else if ( writeTimeout < 0 ) {
                        keycount = selector.select();
                    } else {
                        keycount = selector.select ( writeTimeout );
                    }
                }
                if ( writeTimeout > 0 && ( selector == null || keycount == 0 ) ) {
                    timedout = ( System.currentTimeMillis() - time ) >= writeTimeout;
                }
            }
            if ( timedout ) {
                throw new SocketTimeoutException();
            }
        } finally {
            if ( key != null ) {
                key.cancel();
                if ( selector != null ) {
                    selector.selectNow();
                }
            }
        }
        return written;
    }
    public int read ( ByteBuffer buf, NioChannel socket, Selector selector, long readTimeout ) throws IOException {
        return read ( buf, socket, selector, readTimeout, true );
    }
    public int read ( ByteBuffer buf, NioChannel socket, Selector selector, long readTimeout, boolean block ) throws IOException {
        if ( SHARED && block ) {
            return blockingSelector.read ( buf, socket, readTimeout );
        }
        SelectionKey key = null;
        int read = 0;
        boolean timedout = false;
        int keycount = 1;
        long time = System.currentTimeMillis();
        try {
            while ( ( !timedout ) ) {
                int cnt = 0;
                if ( keycount > 0 ) {
                    cnt = socket.read ( buf );
                    if ( cnt == -1 ) {
                        if ( read == 0 ) {
                            read = -1;
                        }
                        break;
                    }
                    read += cnt;
                    if ( cnt > 0 ) {
                        continue;
                    }
                    if ( cnt == 0 && ( read > 0 || ( !block ) ) ) {
                        break;
                    }
                }
                if ( selector != null ) {
                    if ( key == null ) {
                        key = socket.getIOChannel().register ( selector, SelectionKey.OP_READ );
                    } else {
                        key.interestOps ( SelectionKey.OP_READ );
                    }
                    if ( readTimeout == 0 ) {
                        timedout = ( read == 0 );
                    } else if ( readTimeout < 0 ) {
                        keycount = selector.select();
                    } else {
                        keycount = selector.select ( readTimeout );
                    }
                }
                if ( readTimeout > 0 && ( selector == null || keycount == 0 ) ) {
                    timedout = ( System.currentTimeMillis() - time ) >= readTimeout;
                }
            }
            if ( timedout ) {
                throw new SocketTimeoutException();
            }
        } finally {
            if ( key != null ) {
                key.cancel();
                if ( selector != null ) {
                    selector.selectNow();
                }
            }
        }
        return read;
    }
    public void setMaxSelectors ( int maxSelectors ) {
        this.maxSelectors = maxSelectors;
    }
    public void setMaxSpareSelectors ( int maxSpareSelectors ) {
        this.maxSpareSelectors = maxSpareSelectors;
    }
    public void setEnabled ( boolean enabled ) {
        this.enabled = enabled;
    }
    public void setSharedSelectorTimeout ( long sharedSelectorTimeout ) {
        this.sharedSelectorTimeout = sharedSelectorTimeout;
    }
    public int getMaxSelectors() {
        return maxSelectors;
    }
    public int getMaxSpareSelectors() {
        return maxSpareSelectors;
    }
    public boolean isEnabled() {
        return enabled;
    }
    public long getSharedSelectorTimeout() {
        return sharedSelectorTimeout;
    }
    public ConcurrentLinkedQueue<Selector> getSelectors() {
        return selectors;
    }
    public AtomicInteger getSpare() {
        return spare;
    }
}
