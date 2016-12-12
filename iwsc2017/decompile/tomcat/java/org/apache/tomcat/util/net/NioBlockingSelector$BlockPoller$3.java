package org.apache.tomcat.util.net;
import java.nio.channels.SelectionKey;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SocketChannel;
class NioBlockingSelector$BlockPoller$3 implements Runnable {
    final   SocketChannel val$ch;
    final   int val$ops;
    final   NioEndpoint.NioSocketWrapper val$key;
    @Override
    public void run() {
        final SelectionKey sk = this.val$ch.keyFor ( BlockPoller.this.selector );
        try {
            if ( sk == null ) {
                if ( 0x4 == ( this.val$ops & 0x4 ) ) {
                    BlockPoller.this.countDown ( this.val$key.getWriteLatch() );
                }
                if ( 0x1 == ( this.val$ops & 0x1 ) ) {
                    BlockPoller.this.countDown ( this.val$key.getReadLatch() );
                }
            } else if ( sk.isValid() ) {
                sk.interestOps ( sk.interestOps() & ~this.val$ops );
                if ( 0x4 == ( this.val$ops & 0x4 ) ) {
                    BlockPoller.this.countDown ( this.val$key.getWriteLatch() );
                }
                if ( 0x1 == ( this.val$ops & 0x1 ) ) {
                    BlockPoller.this.countDown ( this.val$key.getReadLatch() );
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
}
