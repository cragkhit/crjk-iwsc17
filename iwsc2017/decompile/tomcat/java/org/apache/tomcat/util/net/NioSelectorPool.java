package org.apache.tomcat.util.net;
import org.apache.juli.logging.LogFactory;
import java.nio.channels.SelectionKey;
import java.net.SocketTimeoutException;
import java.io.EOFException;
import java.nio.ByteBuffer;
import java.util.NoSuchElementException;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.nio.channels.Selector;
import org.apache.juli.logging.Log;
public class NioSelectorPool {
    private static final Log log;
    protected static final boolean SHARED;
    protected NioBlockingSelector blockingSelector;
    protected volatile Selector SHARED_SELECTOR;
    protected int maxSelectors;
    protected long sharedSelectorTimeout;
    protected int maxSpareSelectors;
    protected boolean enabled;
    protected AtomicInteger active;
    protected AtomicInteger spare;
    protected ConcurrentLinkedQueue<Selector> selectors;
    public NioSelectorPool() {
        this.maxSelectors = 200;
        this.sharedSelectorTimeout = 30000L;
        this.maxSpareSelectors = -1;
        this.enabled = true;
        this.active = new AtomicInteger ( 0 );
        this.spare = new AtomicInteger ( 0 );
        this.selectors = new ConcurrentLinkedQueue<Selector>();
    }
    protected Selector getSharedSelector() throws IOException {
        if ( NioSelectorPool.SHARED && this.SHARED_SELECTOR == null ) {
            synchronized ( NioSelectorPool.class ) {
                if ( this.SHARED_SELECTOR == null ) {
                    this.SHARED_SELECTOR = Selector.open();
                    NioSelectorPool.log.info ( "Using a shared selector for servlet write/read" );
                }
            }
        }
        return this.SHARED_SELECTOR;
    }
    public Selector get() throws IOException {
        if ( NioSelectorPool.SHARED ) {
            return this.getSharedSelector();
        }
        if ( !this.enabled || this.active.incrementAndGet() >= this.maxSelectors ) {
            if ( this.enabled ) {
                this.active.decrementAndGet();
            }
            return null;
        }
        Selector s = null;
        try {
            s = ( ( this.selectors.size() > 0 ) ? this.selectors.poll() : null );
            if ( s == null ) {
                s = Selector.open();
            } else {
                this.spare.decrementAndGet();
            }
        } catch ( NoSuchElementException x ) {
            try {
                s = Selector.open();
            } catch ( IOException ex ) {}
        } finally {
            if ( s == null ) {
                this.active.decrementAndGet();
            }
        }
        return s;
    }
    public void put ( final Selector s ) throws IOException {
        if ( NioSelectorPool.SHARED ) {
            return;
        }
        if ( this.enabled ) {
            this.active.decrementAndGet();
        }
        if ( this.enabled && ( this.maxSpareSelectors == -1 || this.spare.get() < Math.min ( this.maxSpareSelectors, this.maxSelectors ) ) ) {
            this.spare.incrementAndGet();
            this.selectors.offer ( s );
        } else {
            s.close();
        }
    }
    public void close() throws IOException {
        this.enabled = false;
        Selector s;
        while ( ( s = this.selectors.poll() ) != null ) {
            s.close();
        }
        this.spare.set ( 0 );
        this.active.set ( 0 );
        if ( this.blockingSelector != null ) {
            this.blockingSelector.close();
        }
        if ( NioSelectorPool.SHARED && this.getSharedSelector() != null ) {
            this.getSharedSelector().close();
            this.SHARED_SELECTOR = null;
        }
    }
    public void open() throws IOException {
        this.enabled = true;
        this.getSharedSelector();
        if ( NioSelectorPool.SHARED ) {
            ( this.blockingSelector = new NioBlockingSelector() ).open ( this.getSharedSelector() );
        }
    }
    public int write ( final ByteBuffer buf, final NioChannel socket, final Selector selector, final long writeTimeout, final boolean block ) throws IOException {
        if ( NioSelectorPool.SHARED && block ) {
            return this.blockingSelector.write ( buf, socket, writeTimeout );
        }
        SelectionKey key = null;
        int written = 0;
        boolean timedout = false;
        int keycount = 1;
        long time = System.currentTimeMillis();
        try {
            while ( !timedout && buf.hasRemaining() ) {
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
                    if ( cnt == 0 && !block ) {
                        break;
                    }
                }
                if ( selector != null ) {
                    if ( key == null ) {
                        key = socket.getIOChannel().register ( selector, 4 );
                    } else {
                        key.interestOps ( 4 );
                    }
                    if ( writeTimeout == 0L ) {
                        timedout = buf.hasRemaining();
                    } else if ( writeTimeout < 0L ) {
                        keycount = selector.select();
                    } else {
                        keycount = selector.select ( writeTimeout );
                    }
                }
                if ( writeTimeout > 0L && ( selector == null || keycount == 0 ) ) {
                    timedout = ( System.currentTimeMillis() - time >= writeTimeout );
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
    public int read ( final ByteBuffer buf, final NioChannel socket, final Selector selector, final long readTimeout ) throws IOException {
        return this.read ( buf, socket, selector, readTimeout, true );
    }
    public int read ( final ByteBuffer buf, final NioChannel socket, final Selector selector, final long readTimeout, final boolean block ) throws IOException {
        if ( NioSelectorPool.SHARED && block ) {
            return this.blockingSelector.read ( buf, socket, readTimeout );
        }
        SelectionKey key = null;
        int read = 0;
        boolean timedout = false;
        int keycount = 1;
        final long time = System.currentTimeMillis();
        try {
            while ( !timedout ) {
                int cnt = 0;
                if ( keycount > 0 ) {
                    cnt = socket.read ( buf );
                    if ( cnt == -1 ) {
                        if ( read == 0 ) {
                            read = -1;
                            break;
                        }
                        break;
                    } else {
                        read += cnt;
                        if ( cnt > 0 ) {
                            continue;
                        }
                        if ( cnt == 0 ) {
                            if ( read > 0 ) {
                                break;
                            }
                            if ( !block ) {
                                break;
                            }
                        }
                    }
                }
                if ( selector != null ) {
                    if ( key == null ) {
                        key = socket.getIOChannel().register ( selector, 1 );
                    } else {
                        key.interestOps ( 1 );
                    }
                    if ( readTimeout == 0L ) {
                        timedout = ( read == 0 );
                    } else if ( readTimeout < 0L ) {
                        keycount = selector.select();
                    } else {
                        keycount = selector.select ( readTimeout );
                    }
                }
                if ( readTimeout > 0L && ( selector == null || keycount == 0 ) ) {
                    timedout = ( System.currentTimeMillis() - time >= readTimeout );
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
    public void setMaxSelectors ( final int maxSelectors ) {
        this.maxSelectors = maxSelectors;
    }
    public void setMaxSpareSelectors ( final int maxSpareSelectors ) {
        this.maxSpareSelectors = maxSpareSelectors;
    }
    public void setEnabled ( final boolean enabled ) {
        this.enabled = enabled;
    }
    public void setSharedSelectorTimeout ( final long sharedSelectorTimeout ) {
        this.sharedSelectorTimeout = sharedSelectorTimeout;
    }
    public int getMaxSelectors() {
        return this.maxSelectors;
    }
    public int getMaxSpareSelectors() {
        return this.maxSpareSelectors;
    }
    public boolean isEnabled() {
        return this.enabled;
    }
    public long getSharedSelectorTimeout() {
        return this.sharedSelectorTimeout;
    }
    public ConcurrentLinkedQueue<Selector> getSelectors() {
        return this.selectors;
    }
    public AtomicInteger getSpare() {
        return this.spare;
    }
    static {
        log = LogFactory.getLog ( NioSelectorPool.class );
        SHARED = Boolean.parseBoolean ( System.getProperty ( "org.apache.tomcat.util.net.NioSelectorShared", "true" ) );
    }
}
