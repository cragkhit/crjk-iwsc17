package org.apache.tomcat.util.net;
import java.util.concurrent.CountDownLatch;
import java.util.Iterator;
import org.apache.tomcat.util.ExceptionUtils;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SocketChannel;
import java.nio.channels.SelectionKey;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.tomcat.util.collections.SynchronizedQueue;
import java.nio.channels.Selector;
protected static class BlockPoller extends Thread {
    protected volatile boolean run;
    protected Selector selector;
    protected final SynchronizedQueue<Runnable> events;
    protected final AtomicInteger wakeupCounter;
    protected BlockPoller() {
        this.run = true;
        this.selector = null;
        this.events = new SynchronizedQueue<Runnable>();
        this.wakeupCounter = new AtomicInteger ( 0 );
    }
    public void disable() {
        this.run = false;
        this.selector.wakeup();
    }
    public void cancelKey ( final SelectionKey key ) {
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                key.cancel();
            }
        };
        this.events.offer ( r );
        this.wakeup();
    }
    public void wakeup() {
        if ( this.wakeupCounter.addAndGet ( 1 ) == 0 ) {
            this.selector.wakeup();
        }
    }
    public void cancel ( final SelectionKey sk, final NioEndpoint.NioSocketWrapper key, final int ops ) {
        if ( sk != null ) {
            sk.cancel();
            sk.attach ( null );
            if ( 0x4 == ( ops & 0x4 ) ) {
                this.countDown ( key.getWriteLatch() );
            }
            if ( 0x1 == ( ops & 0x1 ) ) {
                this.countDown ( key.getReadLatch() );
            }
        }
    }
    public void add ( final NioEndpoint.NioSocketWrapper key, final int ops, final KeyReference ref ) {
        if ( key == null ) {
            return;
        }
        final NioChannel nch = key.getSocket();
        final SocketChannel ch = nch.getIOChannel();
        if ( ch == null ) {
            return;
        }
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                SelectionKey sk = ch.keyFor ( BlockPoller.this.selector );
                try {
                    if ( sk == null ) {
                        sk = ch.register ( BlockPoller.this.selector, ops, key );
                        ref.key = sk;
                    } else if ( !sk.isValid() ) {
                        BlockPoller.this.cancel ( sk, key, ops );
                    } else {
                        sk.interestOps ( sk.interestOps() | ops );
                    }
                } catch ( CancelledKeyException cx ) {
                    BlockPoller.this.cancel ( sk, key, ops );
                } catch ( ClosedChannelException cx2 ) {
                    BlockPoller.this.cancel ( sk, key, ops );
                }
            }
        };
        this.events.offer ( r );
        this.wakeup();
    }
    public void remove ( final NioEndpoint.NioSocketWrapper key, final int ops ) {
        if ( key == null ) {
            return;
        }
        final NioChannel nch = key.getSocket();
        final SocketChannel ch = nch.getIOChannel();
        if ( ch == null ) {
            return;
        }
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                final SelectionKey sk = ch.keyFor ( BlockPoller.this.selector );
                try {
                    if ( sk == null ) {
                        if ( 0x4 == ( ops & 0x4 ) ) {
                            BlockPoller.this.countDown ( key.getWriteLatch() );
                        }
                        if ( 0x1 == ( ops & 0x1 ) ) {
                            BlockPoller.this.countDown ( key.getReadLatch() );
                        }
                    } else if ( sk.isValid() ) {
                        sk.interestOps ( sk.interestOps() & ~ops );
                        if ( 0x4 == ( ops & 0x4 ) ) {
                            BlockPoller.this.countDown ( key.getWriteLatch() );
                        }
                        if ( 0x1 == ( ops & 0x1 ) ) {
                            BlockPoller.this.countDown ( key.getReadLatch() );
                        }
                        if ( sk.interestOps() == 0 ) {
                            sk.cancel();
                            sk.attach ( null );
                        }
                    } else {
                        sk.cancel();
                        sk.attach ( null );
                    }
                } catch ( CancelledKeyException cx ) {
                    if ( sk != null ) {
                        sk.cancel();
                        sk.attach ( null );
                    }
                }
            }
        };
        this.events.offer ( r );
        this.wakeup();
    }
    public boolean events() {
        boolean result = false;
        Runnable r = null;
        result = ( this.events.size() > 0 );
        while ( ( r = this.events.poll() ) != null ) {
            r.run();
            result = true;
        }
        return result;
    }
    @Override
    public void run() {
        while ( this.run ) {
            try {
                this.events();
                int keyCount = 0;
                try {
                    final int i = this.wakeupCounter.get();
                    if ( i > 0 ) {
                        keyCount = this.selector.selectNow();
                    } else {
                        this.wakeupCounter.set ( -1 );
                        keyCount = this.selector.select ( 1000L );
                    }
                    this.wakeupCounter.set ( 0 );
                    if ( !this.run ) {
                        break;
                    }
                } catch ( NullPointerException x ) {
                    if ( this.selector == null ) {
                        throw x;
                    }
                    if ( !NioBlockingSelector.access$000().isDebugEnabled() ) {
                        continue;
                    }
                    NioBlockingSelector.access$000().debug ( "Possibly encountered sun bug 5076772 on windows JDK 1.5", x );
                    continue;
                } catch ( CancelledKeyException x2 ) {
                    if ( !NioBlockingSelector.access$000().isDebugEnabled() ) {
                        continue;
                    }
                    NioBlockingSelector.access$000().debug ( "Possibly encountered sun bug 5076772 on windows JDK 1.5", x2 );
                    continue;
                } catch ( Throwable x3 ) {
                    ExceptionUtils.handleThrowable ( x3 );
                    NioBlockingSelector.access$000().error ( "", x3 );
                    continue;
                }
                final Iterator<SelectionKey> iterator = ( keyCount > 0 ) ? this.selector.selectedKeys().iterator() : null;
                while ( this.run && iterator != null && iterator.hasNext() ) {
                    final SelectionKey sk = iterator.next();
                    final NioEndpoint.NioSocketWrapper attachment = ( NioEndpoint.NioSocketWrapper ) sk.attachment();
                    try {
                        iterator.remove();
                        sk.interestOps ( sk.interestOps() & ~sk.readyOps() );
                        if ( sk.isReadable() ) {
                            this.countDown ( attachment.getReadLatch() );
                        }
                        if ( !sk.isWritable() ) {
                            continue;
                        }
                        this.countDown ( attachment.getWriteLatch() );
                    } catch ( CancelledKeyException ckx ) {
                        sk.cancel();
                        this.countDown ( attachment.getReadLatch() );
                        this.countDown ( attachment.getWriteLatch() );
                    }
                }
            } catch ( Throwable t ) {
                NioBlockingSelector.access$000().error ( "", t );
            }
        }
        this.events.clear();
        try {
            this.selector.selectNow();
        } catch ( Exception ignore ) {
            if ( NioBlockingSelector.access$000().isDebugEnabled() ) {
                NioBlockingSelector.access$000().debug ( "", ignore );
            }
        }
        try {
            this.selector.close();
        } catch ( Exception ignore ) {
            if ( NioBlockingSelector.access$000().isDebugEnabled() ) {
                NioBlockingSelector.access$000().debug ( "", ignore );
            }
        }
    }
    public void countDown ( final CountDownLatch latch ) {
        if ( latch == null ) {
            return;
        }
        latch.countDown();
    }
}
