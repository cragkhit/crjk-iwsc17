package org.apache.tomcat.util.net;
import java.util.ConcurrentModificationException;
import java.net.SocketTimeoutException;
import java.nio.channels.WritableByteChannel;
import java.io.FileInputStream;
import java.io.File;
import java.nio.channels.CancelledKeyException;
import java.util.Iterator;
import org.apache.tomcat.util.ExceptionUtils;
import java.nio.channels.SelectionKey;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.tomcat.util.collections.SynchronizedQueue;
import java.nio.channels.Selector;
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
        PollerEvent r = NioEndpoint.access$300 ( NioEndpoint.this ).pop();
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
                NioEndpoint.access$300 ( NioEndpoint.this ).push ( pe );
            } catch ( Throwable x ) {
                NioEndpoint.access$200().error ( "", x );
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
        PollerEvent r = NioEndpoint.access$300 ( NioEndpoint.this ).pop();
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
                    if ( NioEndpoint.access$200().isDebugEnabled() ) {
                        NioEndpoint.access$200().debug ( AbstractEndpoint.sm.getString ( "endpoint.debug.channelCloseFail" ), e );
                    }
                }
            }
            try {
                if ( ka != null ) {
                    ka.getSocket().close ( true );
                }
            } catch ( Exception e ) {
                if ( NioEndpoint.access$200().isDebugEnabled() ) {
                    NioEndpoint.access$200().debug ( AbstractEndpoint.sm.getString ( "endpoint.debug.socketCloseFail" ), e );
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
            if ( NioEndpoint.access$200().isDebugEnabled() ) {
                NioEndpoint.access$200().error ( "", e2 );
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
                        this.keyCount = this.selector.select ( NioEndpoint.access$400 ( NioEndpoint.this ) );
                    }
                    this.wakeupCounter.set ( 0L );
                }
                if ( this.close ) {
                    this.events();
                    this.timeout ( 0, false );
                    try {
                        this.selector.close();
                    } catch ( IOException ioe ) {
                        NioEndpoint.access$200().error ( AbstractEndpoint.sm.getString ( "endpoint.nio.selectorCloseFail" ), ioe );
                    }
                    break;
                }
            } catch ( Throwable x ) {
                ExceptionUtils.handleThrowable ( x );
                NioEndpoint.access$200().error ( "", x );
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
        NioEndpoint.access$500 ( NioEndpoint.this ).countDown();
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
            NioEndpoint.access$200().error ( "", t );
        }
    }
    public SendfileState processSendfile ( final SelectionKey sk, final NioSocketWrapper socketWrapper, final boolean calledByProcessor ) {
        NioChannel sc = null;
        try {
            this.unreg ( sk, socketWrapper, sk.readyOps() );
            final SendfileData sd = socketWrapper.getSendfileData();
            if ( NioEndpoint.access$200().isTraceEnabled() ) {
                NioEndpoint.access$200().trace ( "Processing send file for: " + sd.fileName );
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
                if ( NioEndpoint.access$200().isDebugEnabled() ) {
                    NioEndpoint.access$200().debug ( "Send file complete for: " + sd.fileName );
                }
                socketWrapper.setSendfileData ( null );
                try {
                    sd.fchannel.close();
                } catch ( Exception ex ) {}
                if ( !calledByProcessor ) {
                    if ( sd.keepAlive ) {
                        if ( NioEndpoint.access$200().isDebugEnabled() ) {
                            NioEndpoint.access$200().debug ( "Connection is keep alive, registering back for OP_READ" );
                        }
                        this.reg ( sk, socketWrapper, 1 );
                    } else {
                        if ( NioEndpoint.access$200().isDebugEnabled() ) {
                            NioEndpoint.access$200().debug ( "Send file connection is being closed" );
                        }
                        NioEndpoint.access$600 ( NioEndpoint.this, sc, sk );
                    }
                }
                return SendfileState.DONE;
            }
            if ( NioEndpoint.access$200().isDebugEnabled() ) {
                NioEndpoint.access$200().debug ( "OP_WRITE for sendfile: " + sd.fileName );
            }
            if ( calledByProcessor ) {
                this.add ( socketWrapper.getSocket(), 4 );
            } else {
                this.reg ( sk, socketWrapper, 4 );
            }
            return SendfileState.PENDING;
        } catch ( IOException x ) {
            if ( NioEndpoint.access$200().isDebugEnabled() ) {
                NioEndpoint.access$200().debug ( "Unable to complete sendfile request:", x );
            }
            if ( !calledByProcessor && sc != null ) {
                NioEndpoint.access$600 ( NioEndpoint.this, sc, sk );
            } else {
                this.cancelledKey ( sk );
            }
            return SendfileState.ERROR;
        } catch ( Throwable t ) {
            NioEndpoint.access$200().error ( "", t );
            if ( !calledByProcessor && sc != null ) {
                NioEndpoint.access$600 ( NioEndpoint.this, sc, sk );
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
            NioEndpoint.access$200().warn ( AbstractEndpoint.sm.getString ( "endpoint.nio.timeoutCme" ), cme );
        }
        final long prevExp = this.nextExpiration;
        this.nextExpiration = System.currentTimeMillis() + NioEndpoint.this.socketProperties.getTimeoutInterval();
        if ( NioEndpoint.access$200().isTraceEnabled() ) {
            NioEndpoint.access$200().trace ( "timeout completed: keys processed=" + keycount + "; now=" + now + "; nextExpiration=" + prevExp + "; keyCount=" + keyCount + "; hasEvents=" + hasEvents + "; eval=" + ( now < prevExp && ( keyCount > 0 || hasEvents ) && !this.close ) );
        }
    }
}
