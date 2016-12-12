package org.apache.tomcat.util.net;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.collections.SynchronizedQueue;
import org.apache.tomcat.util.collections.SynchronizedStack;
import org.apache.tomcat.util.net.NioEndpoint.NioSocketWrapper;
public class NioBlockingSelector {
    private static final Log log = LogFactory.getLog ( NioBlockingSelector.class );
    private static int threadCounter = 0;
    private final SynchronizedStack<KeyReference> keyReferenceStack =
        new SynchronizedStack<>();
    protected Selector sharedSelector;
    protected BlockPoller poller;
    public NioBlockingSelector() {
    }
    public void open ( Selector selector ) {
        sharedSelector = selector;
        poller = new BlockPoller();
        poller.selector = sharedSelector;
        poller.setDaemon ( true );
        poller.setName ( "NioBlockingSelector.BlockPoller-" + ( ++threadCounter ) );
        poller.start();
    }
    public void close() {
        if ( poller != null ) {
            poller.disable();
            poller.interrupt();
            poller = null;
        }
    }
    public int write ( ByteBuffer buf, NioChannel socket, long writeTimeout )
    throws IOException {
        SelectionKey key = socket.getIOChannel().keyFor ( socket.getPoller().getSelector() );
        if ( key == null ) {
            throw new IOException ( "Key no longer registered" );
        }
        KeyReference reference = keyReferenceStack.pop();
        if ( reference == null ) {
            reference = new KeyReference();
        }
        NioSocketWrapper att = ( NioSocketWrapper ) key.attachment();
        int written = 0;
        boolean timedout = false;
        int keycount = 1;
        long time = System.currentTimeMillis();
        try {
            while ( ( !timedout ) && buf.hasRemaining() ) {
                if ( keycount > 0 ) {
                    int cnt = socket.write ( buf );
                    if ( cnt == -1 ) {
                        throw new EOFException();
                    }
                    written += cnt;
                    if ( cnt > 0 ) {
                        time = System.currentTimeMillis();
                        continue;
                    }
                }
                try {
                    if ( att.getWriteLatch() == null || att.getWriteLatch().getCount() == 0 ) {
                        att.startWriteLatch ( 1 );
                    }
                    poller.add ( att, SelectionKey.OP_WRITE, reference );
                    if ( writeTimeout < 0 ) {
                        att.awaitWriteLatch ( Long.MAX_VALUE, TimeUnit.MILLISECONDS );
                    } else {
                        att.awaitWriteLatch ( writeTimeout, TimeUnit.MILLISECONDS );
                    }
                } catch ( InterruptedException ignore ) {
                }
                if ( att.getWriteLatch() != null && att.getWriteLatch().getCount() > 0 ) {
                    keycount = 0;
                } else {
                    keycount = 1;
                    att.resetWriteLatch();
                }
                if ( writeTimeout > 0 && ( keycount == 0 ) ) {
                    timedout = ( System.currentTimeMillis() - time ) >= writeTimeout;
                }
            }
            if ( timedout ) {
                throw new SocketTimeoutException();
            }
        } finally {
            poller.remove ( att, SelectionKey.OP_WRITE );
            if ( timedout && reference.key != null ) {
                poller.cancelKey ( reference.key );
            }
            reference.key = null;
            keyReferenceStack.push ( reference );
        }
        return written;
    }
    public int read ( ByteBuffer buf, NioChannel socket, long readTimeout ) throws IOException {
        SelectionKey key = socket.getIOChannel().keyFor ( socket.getPoller().getSelector() );
        if ( key == null ) {
            throw new IOException ( "Key no longer registered" );
        }
        KeyReference reference = keyReferenceStack.pop();
        if ( reference == null ) {
            reference = new KeyReference();
        }
        NioSocketWrapper att = ( NioSocketWrapper ) key.attachment();
        int read = 0;
        boolean timedout = false;
        int keycount = 1;
        long time = System.currentTimeMillis();
        try {
            while ( !timedout ) {
                if ( keycount > 0 ) {
                    read = socket.read ( buf );
                    if ( read != 0 ) {
                        break;
                    }
                }
                try {
                    if ( att.getReadLatch() == null || att.getReadLatch().getCount() == 0 ) {
                        att.startReadLatch ( 1 );
                    }
                    poller.add ( att, SelectionKey.OP_READ, reference );
                    if ( readTimeout < 0 ) {
                        att.awaitReadLatch ( Long.MAX_VALUE, TimeUnit.MILLISECONDS );
                    } else {
                        att.awaitReadLatch ( readTimeout, TimeUnit.MILLISECONDS );
                    }
                } catch ( InterruptedException ignore ) {
                }
                if ( att.getReadLatch() != null && att.getReadLatch().getCount() > 0 ) {
                    keycount = 0;
                } else {
                    keycount = 1;
                    att.resetReadLatch();
                }
                if ( readTimeout >= 0 && ( keycount == 0 ) ) {
                    timedout = ( System.currentTimeMillis() - time ) >= readTimeout;
                }
            }
            if ( timedout ) {
                throw new SocketTimeoutException();
            }
        } finally {
            poller.remove ( att, SelectionKey.OP_READ );
            if ( timedout && reference.key != null ) {
                poller.cancelKey ( reference.key );
            }
            reference.key = null;
            keyReferenceStack.push ( reference );
        }
        return read;
    }
    protected static class BlockPoller extends Thread {
        protected volatile boolean run = true;
        protected Selector selector = null;
        protected final SynchronizedQueue<Runnable> events =
            new SynchronizedQueue<>();
        public void disable() {
            run = false;
            selector.wakeup();
        }
        protected final AtomicInteger wakeupCounter = new AtomicInteger ( 0 );
        public void cancelKey ( final SelectionKey key ) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    key.cancel();
                }
            };
            events.offer ( r );
            wakeup();
        }
        public void wakeup() {
            if ( wakeupCounter.addAndGet ( 1 ) == 0 ) {
                selector.wakeup();
            }
        }
        public void cancel ( SelectionKey sk, NioSocketWrapper key, int ops ) {
            if ( sk != null ) {
                sk.cancel();
                sk.attach ( null );
                if ( SelectionKey.OP_WRITE == ( ops & SelectionKey.OP_WRITE ) ) {
                    countDown ( key.getWriteLatch() );
                }
                if ( SelectionKey.OP_READ == ( ops & SelectionKey.OP_READ ) ) {
                    countDown ( key.getReadLatch() );
                }
            }
        }
        public void add ( final NioSocketWrapper key, final int ops, final KeyReference ref ) {
            if ( key == null ) {
                return;
            }
            NioChannel nch = key.getSocket();
            final SocketChannel ch = nch.getIOChannel();
            if ( ch == null ) {
                return;
            }
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    SelectionKey sk = ch.keyFor ( selector );
                    try {
                        if ( sk == null ) {
                            sk = ch.register ( selector, ops, key );
                            ref.key = sk;
                        } else if ( !sk.isValid() ) {
                            cancel ( sk, key, ops );
                        } else {
                            sk.interestOps ( sk.interestOps() | ops );
                        }
                    } catch ( CancelledKeyException cx ) {
                        cancel ( sk, key, ops );
                    } catch ( ClosedChannelException cx ) {
                        cancel ( sk, key, ops );
                    }
                }
            };
            events.offer ( r );
            wakeup();
        }
        public void remove ( final NioSocketWrapper key, final int ops ) {
            if ( key == null ) {
                return;
            }
            NioChannel nch = key.getSocket();
            final SocketChannel ch = nch.getIOChannel();
            if ( ch == null ) {
                return;
            }
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    SelectionKey sk = ch.keyFor ( selector );
                    try {
                        if ( sk == null ) {
                            if ( SelectionKey.OP_WRITE == ( ops & SelectionKey.OP_WRITE ) ) {
                                countDown ( key.getWriteLatch() );
                            }
                            if ( SelectionKey.OP_READ == ( ops & SelectionKey.OP_READ ) ) {
                                countDown ( key.getReadLatch() );
                            }
                        } else {
                            if ( sk.isValid() ) {
                                sk.interestOps ( sk.interestOps() & ( ~ops ) );
                                if ( SelectionKey.OP_WRITE == ( ops & SelectionKey.OP_WRITE ) ) {
                                    countDown ( key.getWriteLatch() );
                                }
                                if ( SelectionKey.OP_READ == ( ops & SelectionKey.OP_READ ) ) {
                                    countDown ( key.getReadLatch() );
                                }
                                if ( sk.interestOps() == 0 ) {
                                    sk.cancel();
                                    sk.attach ( null );
                                }
                            } else {
                                sk.cancel();
                                sk.attach ( null );
                            }
                        }
                    } catch ( CancelledKeyException cx ) {
                        if ( sk != null ) {
                            sk.cancel();
                            sk.attach ( null );
                        }
                    }
                }
            };
            events.offer ( r );
            wakeup();
        }
        public boolean events() {
            boolean result = false;
            Runnable r = null;
            result = ( events.size() > 0 );
            while ( ( r = events.poll() ) != null ) {
                r.run();
                result = true;
            }
            return result;
        }
        @Override
        public void run() {
            while ( run ) {
                try {
                    events();
                    int keyCount = 0;
                    try {
                        int i = wakeupCounter.get();
                        if ( i > 0 ) {
                            keyCount = selector.selectNow();
                        } else {
                            wakeupCounter.set ( -1 );
                            keyCount = selector.select ( 1000 );
                        }
                        wakeupCounter.set ( 0 );
                        if ( !run ) {
                            break;
                        }
                    } catch ( NullPointerException x ) {
                        if ( selector == null ) {
                            throw x;
                        }
                        if ( log.isDebugEnabled() ) {
                            log.debug ( "Possibly encountered sun bug 5076772 on windows JDK 1.5", x );
                        }
                        continue;
                    } catch ( CancelledKeyException x ) {
                        if ( log.isDebugEnabled() ) {
                            log.debug ( "Possibly encountered sun bug 5076772 on windows JDK 1.5", x );
                        }
                        continue;
                    } catch ( Throwable x ) {
                        ExceptionUtils.handleThrowable ( x );
                        log.error ( "", x );
                        continue;
                    }
                    Iterator<SelectionKey> iterator = keyCount > 0 ? selector.selectedKeys().iterator() : null;
                    while ( run && iterator != null && iterator.hasNext() ) {
                        SelectionKey sk = iterator.next();
                        NioSocketWrapper attachment = ( NioSocketWrapper ) sk.attachment();
                        try {
                            iterator.remove();
                            sk.interestOps ( sk.interestOps() & ( ~sk.readyOps() ) );
                            if ( sk.isReadable() ) {
                                countDown ( attachment.getReadLatch() );
                            }
                            if ( sk.isWritable() ) {
                                countDown ( attachment.getWriteLatch() );
                            }
                        } catch ( CancelledKeyException ckx ) {
                            sk.cancel();
                            countDown ( attachment.getReadLatch() );
                            countDown ( attachment.getWriteLatch() );
                        }
                    }
                } catch ( Throwable t ) {
                    log.error ( "", t );
                }
            }
            events.clear();
            try {
                selector.selectNow();
            } catch ( Exception ignore ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "", ignore );
                }
            }
            try {
                selector.close();
            } catch ( Exception ignore ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "", ignore );
                }
            }
        }
        public void countDown ( CountDownLatch latch ) {
            if ( latch == null ) {
                return;
            }
            latch.countDown();
        }
    }
    public static class KeyReference {
        SelectionKey key = null;
        @Override
        public void finalize() {
            if ( key != null && key.isValid() ) {
                log.warn ( "Possible key leak, cancelling key in the finalizer." );
                try {
                    key.cancel();
                } catch ( Exception ignore ) {}
            }
            key = null;
        }
    }
}
