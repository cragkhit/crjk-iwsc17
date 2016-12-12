package org.apache.tomcat.util.net;
import java.nio.channels.SelectionKey;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SocketChannel;
class NioBlockingSelector$BlockPoller$2 implements Runnable {
    final   SocketChannel val$ch;
    final   int val$ops;
    final   NioEndpoint.NioSocketWrapper val$key;
    final   KeyReference val$ref;
    @Override
    public void run() {
        SelectionKey sk = this.val$ch.keyFor ( BlockPoller.this.selector );
        try {
            if ( sk == null ) {
                sk = this.val$ch.register ( BlockPoller.this.selector, this.val$ops, this.val$key );
                this.val$ref.key = sk;
            } else if ( !sk.isValid() ) {
                BlockPoller.this.cancel ( sk, this.val$key, this.val$ops );
            } else {
                sk.interestOps ( sk.interestOps() | this.val$ops );
            }
        } catch ( CancelledKeyException cx ) {
            BlockPoller.this.cancel ( sk, this.val$key, this.val$ops );
        } catch ( ClosedChannelException cx2 ) {
            BlockPoller.this.cancel ( sk, this.val$key, this.val$ops );
        }
    }
}
