package org.apache.tomcat.util.net;
import org.apache.tomcat.jni.SSLSocket;
import org.apache.tomcat.jni.Sockaddr;
import org.apache.tomcat.jni.Address;
import org.apache.tomcat.jni.Status;
import org.apache.tomcat.util.buf.ByteBufferUtils;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;
import java.io.EOFException;
import org.apache.tomcat.jni.Socket;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import org.apache.tomcat.jni.Error;
import org.apache.tomcat.util.ExceptionUtils;
import java.io.IOException;
import java.net.SocketTimeoutException;
import org.apache.tomcat.jni.Poll;
import org.apache.tomcat.jni.OS;
import org.apache.tomcat.jni.Pool;
import java.util.concurrent.atomic.AtomicInteger;
public class Poller implements Runnable {
    private long[] pollers;
    private int actualPollerSize;
    private int[] pollerSpace;
    private int pollerCount;
    private int pollerTime;
    private int nextPollerTime;
    private long pool;
    private long[] desc;
    private SocketList addList;
    private SocketList closeList;
    private SocketTimeouts timeouts;
    private long lastMaintain;
    private AtomicInteger connectionCount;
    private volatile boolean pollerRunning;
    public Poller() {
        this.pollers = null;
        this.actualPollerSize = 0;
        this.pollerSpace = null;
        this.pool = 0L;
        this.addList = null;
        this.closeList = null;
        this.timeouts = null;
        this.lastMaintain = System.currentTimeMillis();
        this.connectionCount = new AtomicInteger ( 0 );
        this.pollerRunning = true;
    }
    public int getConnectionCount() {
        return this.connectionCount.get();
    }
    protected synchronized void init() {
        this.pool = Pool.create ( AprEndpoint.this.serverSockPool );
        final int defaultPollerSize = AprEndpoint.this.getMaxConnections();
        if ( ( OS.IS_WIN32 || OS.IS_WIN64 ) && defaultPollerSize > 1024 ) {
            this.actualPollerSize = 1024;
        } else {
            this.actualPollerSize = defaultPollerSize;
        }
        this.timeouts = new SocketTimeouts ( defaultPollerSize );
        long pollset = AprEndpoint.this.allocatePoller ( this.actualPollerSize, this.pool, -1 );
        if ( pollset == 0L && this.actualPollerSize > 1024 ) {
            this.actualPollerSize = 1024;
            pollset = AprEndpoint.this.allocatePoller ( this.actualPollerSize, this.pool, -1 );
        }
        if ( pollset == 0L ) {
            this.actualPollerSize = 62;
            pollset = AprEndpoint.this.allocatePoller ( this.actualPollerSize, this.pool, -1 );
        }
        this.pollerCount = defaultPollerSize / this.actualPollerSize;
        this.pollerTime = AprEndpoint.this.pollTime / this.pollerCount;
        this.nextPollerTime = this.pollerTime;
        ( this.pollers = new long[this.pollerCount] ) [0] = pollset;
        for ( int i = 1; i < this.pollerCount; ++i ) {
            this.pollers[i] = AprEndpoint.this.allocatePoller ( this.actualPollerSize, this.pool, -1 );
        }
        this.pollerSpace = new int[this.pollerCount];
        for ( int i = 0; i < this.pollerCount; ++i ) {
            this.pollerSpace[i] = this.actualPollerSize;
        }
        this.desc = new long[this.actualPollerSize * 4];
        this.connectionCount.set ( 0 );
        this.addList = new SocketList ( defaultPollerSize );
        this.closeList = new SocketList ( defaultPollerSize );
    }
    protected synchronized void stop() {
        this.pollerRunning = false;
    }
    protected synchronized void destroy() {
        try {
            this.notify();
            this.wait ( this.pollerCount * AprEndpoint.this.pollTime / 1000 );
        } catch ( InterruptedException ex ) {}
        for ( SocketInfo info = this.closeList.get(); info != null; info = this.closeList.get() ) {
            this.addList.remove ( info.socket );
            this.removeFromPoller ( info.socket );
            AprEndpoint.access$100 ( AprEndpoint.this, info.socket );
        }
        this.closeList.clear();
        for ( SocketInfo info = this.addList.get(); info != null; info = this.addList.get() ) {
            this.removeFromPoller ( info.socket );
            AprEndpoint.access$100 ( AprEndpoint.this, info.socket );
        }
        this.addList.clear();
        for ( int i = 0; i < this.pollerCount; ++i ) {
            final int rv = Poll.pollset ( this.pollers[i], this.desc );
            if ( rv > 0 ) {
                for ( int n = 0; n < rv; ++n ) {
                    AprEndpoint.access$100 ( AprEndpoint.this, this.desc[n * 2 + 1] );
                }
            }
        }
        Pool.destroy ( this.pool );
        this.connectionCount.set ( 0 );
    }
    private void add ( final long socket, long timeout, final int flags ) {
        if ( AprEndpoint.access$200().isDebugEnabled() ) {
            final String msg = AbstractEndpoint.sm.getString ( "endpoint.debug.pollerAdd", socket, timeout, flags );
            if ( AprEndpoint.access$200().isTraceEnabled() ) {
                AprEndpoint.access$200().trace ( msg, new Exception() );
            } else {
                AprEndpoint.access$200().debug ( msg );
            }
        }
        if ( timeout <= 0L ) {
            timeout = 2147483647L;
        }
        synchronized ( this ) {
            if ( this.addList.add ( socket, timeout, flags ) ) {
                this.notify();
            }
        }
    }
    private boolean addToPoller ( final long socket, final int events ) {
        int rv = -1;
        for ( int i = 0; i < this.pollers.length; ++i ) {
            if ( this.pollerSpace[i] > 0 ) {
                rv = Poll.add ( this.pollers[i], socket, events );
                if ( rv == 0 ) {
                    final int[] pollerSpace = this.pollerSpace;
                    final int n = i;
                    --pollerSpace[n];
                    this.connectionCount.incrementAndGet();
                    return true;
                }
            }
        }
        return false;
    }
    private synchronized void close ( final long socket ) {
        this.closeList.add ( socket, 0L, 0 );
        this.notify();
    }
    private void removeFromPoller ( final long socket ) {
        if ( AprEndpoint.access$200().isDebugEnabled() ) {
            AprEndpoint.access$200().debug ( AbstractEndpoint.sm.getString ( "endpoint.debug.pollerRemove", socket ) );
        }
        int rv = -1;
        for ( int i = 0; i < this.pollers.length; ++i ) {
            if ( this.pollerSpace[i] < this.actualPollerSize ) {
                rv = Poll.remove ( this.pollers[i], socket );
                if ( rv != 70015 ) {
                    final int[] pollerSpace = this.pollerSpace;
                    final int n = i;
                    ++pollerSpace[n];
                    this.connectionCount.decrementAndGet();
                    if ( AprEndpoint.access$200().isDebugEnabled() ) {
                        AprEndpoint.access$200().debug ( AbstractEndpoint.sm.getString ( "endpoint.debug.pollerRemoved", socket ) );
                        break;
                    }
                    break;
                }
            }
        }
        this.timeouts.remove ( socket );
    }
    private synchronized void maintain() {
        final long date = System.currentTimeMillis();
        if ( date - this.lastMaintain < 1000L ) {
            return;
        }
        this.lastMaintain = date;
        for ( long socket = this.timeouts.check ( date ); socket != 0L; socket = this.timeouts.check ( date ) ) {
            if ( AprEndpoint.access$200().isDebugEnabled() ) {
                AprEndpoint.access$200().debug ( AbstractEndpoint.sm.getString ( "endpoint.debug.socketTimeout", socket ) );
            }
            final SocketWrapperBase<Long> socketWrapper = AprEndpoint.access$300 ( AprEndpoint.this ).get ( socket );
            socketWrapper.setError ( new SocketTimeoutException() );
            AprEndpoint.this.processSocket ( socketWrapper, SocketEvent.ERROR, true );
        }
    }
    @Override
    public String toString() {
        final StringBuffer buf = new StringBuffer();
        buf.append ( "Poller" );
        final long[] res = new long[this.actualPollerSize * 2];
        for ( int i = 0; i < this.pollers.length; ++i ) {
            final int count = Poll.pollset ( this.pollers[i], res );
            buf.append ( " [ " );
            for ( int j = 0; j < count; ++j ) {
                buf.append ( this.desc[2 * j + 1] ).append ( " " );
            }
            buf.append ( "]" );
        }
        return buf.toString();
    }
    @Override
    public void run() {
        final SocketList localAddList = new SocketList ( AprEndpoint.this.getMaxConnections() );
        final SocketList localCloseList = new SocketList ( AprEndpoint.this.getMaxConnections() );
        while ( this.pollerRunning ) {
            while ( this.pollerRunning && this.connectionCount.get() < 1 && this.addList.size() < 1 && this.closeList.size() < 1 ) {
                try {
                    if ( AprEndpoint.this.getConnectionTimeout() > 0 && this.pollerRunning ) {
                        this.maintain();
                    }
                    synchronized ( this ) {
                        if ( this.addList.size() >= 1 || this.closeList.size() >= 1 ) {
                            continue;
                        }
                        this.wait ( 10000L );
                    }
                } catch ( InterruptedException ex ) {}
                catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                    AprEndpoint.this.getLog().warn ( AbstractEndpoint.sm.getString ( "endpoint.timeout.err" ) );
                }
            }
            if ( !this.pollerRunning ) {
                break;
            }
            try {
                synchronized ( this ) {
                    if ( this.closeList.size() > 0 ) {
                        this.closeList.duplicate ( localCloseList );
                        this.closeList.clear();
                    } else {
                        localCloseList.clear();
                    }
                }
                synchronized ( this ) {
                    if ( this.addList.size() > 0 ) {
                        this.addList.duplicate ( localAddList );
                        this.addList.clear();
                    } else {
                        localAddList.clear();
                    }
                }
                if ( localCloseList.size() > 0 ) {
                    for ( SocketInfo info = localCloseList.get(); info != null; info = localCloseList.get() ) {
                        localAddList.remove ( info.socket );
                        this.removeFromPoller ( info.socket );
                        AprEndpoint.access$100 ( AprEndpoint.this, info.socket );
                    }
                }
                if ( localAddList.size() > 0 ) {
                    SocketInfo info = localAddList.get();
                    while ( info != null ) {
                        if ( AprEndpoint.access$200().isDebugEnabled() ) {
                            AprEndpoint.access$200().debug ( AbstractEndpoint.sm.getString ( "endpoint.debug.pollerAddDo", info.socket ) );
                        }
                        this.timeouts.remove ( info.socket );
                        final AprSocketWrapper wrapper = AprEndpoint.access$300 ( AprEndpoint.this ).get ( info.socket );
                        if ( wrapper == null ) {
                            continue;
                        }
                        if ( info.read() || info.write() ) {
                            wrapper.pollerFlags = ( wrapper.pollerFlags | ( info.read() ? 1 : 0 ) | ( info.write() ? 4 : 0 ) );
                            this.removeFromPoller ( info.socket );
                            if ( !this.addToPoller ( info.socket, wrapper.pollerFlags ) ) {
                                AprEndpoint.access$000 ( AprEndpoint.this, info.socket );
                            } else {
                                this.timeouts.add ( info.socket, System.currentTimeMillis() + info.timeout );
                            }
                        } else {
                            AprEndpoint.access$000 ( AprEndpoint.this, info.socket );
                            AprEndpoint.this.getLog().warn ( AbstractEndpoint.sm.getString ( "endpoint.apr.pollAddInvalid", info ) );
                        }
                        info = localAddList.get();
                    }
                }
                for ( int i = 0; i < this.pollers.length; ++i ) {
                    boolean reset = false;
                    int rv = 0;
                    if ( this.pollerSpace[i] < this.actualPollerSize ) {
                        rv = Poll.poll ( this.pollers[i], this.nextPollerTime, this.desc, true );
                        this.nextPollerTime = this.pollerTime;
                    } else {
                        this.nextPollerTime += this.pollerTime;
                    }
                    if ( rv > 0 ) {
                        rv = this.mergeDescriptors ( this.desc, rv );
                        final int[] pollerSpace = this.pollerSpace;
                        final int n2 = i;
                        pollerSpace[n2] += rv;
                        this.connectionCount.addAndGet ( -rv );
                        for ( int n = 0; n < rv; ++n ) {
                            long timeout = this.timeouts.remove ( this.desc[n * 2 + 1] );
                            final AprSocketWrapper wrapper2 = AprEndpoint.access$300 ( AprEndpoint.this ).get ( this.desc[n * 2 + 1] );
                            if ( AprEndpoint.this.getLog().isDebugEnabled() ) {
                                AprEndpoint.access$200().debug ( AbstractEndpoint.sm.getString ( "endpoint.debug.pollerProcess", this.desc[n * 2 + 1], this.desc[n * 2] ) );
                            }
                            wrapper2.pollerFlags &= ~ ( int ) this.desc[n * 2];
                            if ( ( this.desc[n * 2] & 0x20L ) == 0x20L || ( this.desc[n * 2] & 0x10L ) == 0x10L || ( this.desc[n * 2] & 0x40L ) == 0x40L ) {
                                if ( ( this.desc[n * 2] & 0x1L ) == 0x1L ) {
                                    if ( !AprEndpoint.this.processSocket ( this.desc[n * 2 + 1], SocketEvent.OPEN_READ ) ) {
                                        AprEndpoint.access$000 ( AprEndpoint.this, this.desc[n * 2 + 1] );
                                    }
                                } else if ( ( this.desc[n * 2] & 0x4L ) == 0x4L ) {
                                    if ( !AprEndpoint.this.processSocket ( this.desc[n * 2 + 1], SocketEvent.OPEN_WRITE ) ) {
                                        AprEndpoint.access$000 ( AprEndpoint.this, this.desc[n * 2 + 1] );
                                    }
                                } else if ( ( wrapper2.pollerFlags & 0x1 ) == 0x1 ) {
                                    if ( !AprEndpoint.this.processSocket ( this.desc[n * 2 + 1], SocketEvent.OPEN_READ ) ) {
                                        AprEndpoint.access$000 ( AprEndpoint.this, this.desc[n * 2 + 1] );
                                    }
                                } else if ( ( wrapper2.pollerFlags & 0x4 ) == 0x4 ) {
                                    if ( !AprEndpoint.this.processSocket ( this.desc[n * 2 + 1], SocketEvent.OPEN_WRITE ) ) {
                                        AprEndpoint.access$000 ( AprEndpoint.this, this.desc[n * 2 + 1] );
                                    }
                                } else {
                                    AprEndpoint.access$000 ( AprEndpoint.this, this.desc[n * 2 + 1] );
                                }
                            } else if ( ( this.desc[n * 2] & 0x1L ) == 0x1L || ( this.desc[n * 2] & 0x4L ) == 0x4L ) {
                                boolean error = false;
                                if ( ( this.desc[n * 2] & 0x1L ) == 0x1L && !AprEndpoint.this.processSocket ( this.desc[n * 2 + 1], SocketEvent.OPEN_READ ) ) {
                                    error = true;
                                    AprEndpoint.access$000 ( AprEndpoint.this, this.desc[n * 2 + 1] );
                                }
                                if ( !error && ( this.desc[n * 2] & 0x4L ) == 0x4L && !AprEndpoint.this.processSocket ( this.desc[n * 2 + 1], SocketEvent.OPEN_WRITE ) ) {
                                    error = true;
                                    AprEndpoint.access$000 ( AprEndpoint.this, this.desc[n * 2 + 1] );
                                }
                                if ( !error && wrapper2.pollerFlags != 0 ) {
                                    if ( timeout > 0L ) {
                                        timeout -= System.currentTimeMillis();
                                    }
                                    if ( timeout <= 0L ) {
                                        timeout = 1L;
                                    }
                                    if ( timeout > 2147483647L ) {
                                        timeout = 2147483647L;
                                    }
                                    this.add ( this.desc[n * 2 + 1], ( int ) timeout, wrapper2.pollerFlags );
                                }
                            } else {
                                AprEndpoint.this.getLog().warn ( AbstractEndpoint.sm.getString ( "endpoint.apr.pollUnknownEvent", this.desc[n * 2] ) );
                                AprEndpoint.access$000 ( AprEndpoint.this, this.desc[n * 2 + 1] );
                            }
                        }
                    } else if ( rv < 0 ) {
                        int errn = -rv;
                        if ( errn != 120001 && errn != 120003 ) {
                            if ( errn > 120000 ) {
                                errn -= 120000;
                            }
                            AprEndpoint.this.getLog().error ( AbstractEndpoint.sm.getString ( "endpoint.apr.pollError", errn, Error.strerror ( errn ) ) );
                            reset = true;
                        }
                    }
                    if ( reset && this.pollerRunning ) {
                        final int count = Poll.pollset ( this.pollers[i], this.desc );
                        final long newPoller = AprEndpoint.this.allocatePoller ( this.actualPollerSize, this.pool, -1 );
                        this.pollerSpace[i] = this.actualPollerSize;
                        this.connectionCount.addAndGet ( -count );
                        Poll.destroy ( this.pollers[i] );
                        this.pollers[i] = newPoller;
                    }
                }
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                AprEndpoint.this.getLog().warn ( AbstractEndpoint.sm.getString ( "endpoint.poll.error" ), t );
            }
            try {
                if ( AprEndpoint.this.getConnectionTimeout() <= 0 || !this.pollerRunning ) {
                    continue;
                }
                this.maintain();
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                AprEndpoint.this.getLog().warn ( AbstractEndpoint.sm.getString ( "endpoint.timeout.err" ), t );
            }
        }
        synchronized ( this ) {
            this.notifyAll();
        }
    }
    private int mergeDescriptors ( final long[] desc, final int startCount ) {
        final HashMap<Long, Long> merged = new HashMap<Long, Long> ( startCount );
        for ( int n = 0; n < startCount; ++n ) {
            final Long newValue = merged.merge ( desc[2 * n + 1], desc[2 * n], ( v1, v2 ) -> v1 | v2 );
            if ( AprEndpoint.access$200().isDebugEnabled() && newValue != desc[2 * n] ) {
                AprEndpoint.access$200().debug ( AbstractEndpoint.sm.getString ( "endpoint.apr.pollMergeEvents", desc[2 * n + 1], desc[2 * n], newValue ) );
            }
        }
        int i = 0;
        for ( final Map.Entry<Long, Long> entry : merged.entrySet() ) {
            desc[i++] = entry.getValue();
            desc[i++] = entry.getKey();
        }
        return merged.size();
    }
}
