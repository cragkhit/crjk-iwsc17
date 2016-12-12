package org.apache.catalina.tribes.transport.nio;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
class NioReplicationTask$1 implements Runnable {
    final   SelectionKey val$key;
    @Override
    public void run() {
        try {
            if ( this.val$key.isValid() ) {
                this.val$key.selector().wakeup();
                final int resumeOps = this.val$key.interestOps() | 0x1;
                this.val$key.interestOps ( resumeOps );
                if ( NioReplicationTask.access$000().isTraceEnabled() ) {
                    NioReplicationTask.access$000().trace ( "Registering key for read:" + this.val$key );
                }
            }
        } catch ( CancelledKeyException ckx ) {
            NioReceiver.cancelledKey ( this.val$key );
            if ( NioReplicationTask.access$000().isTraceEnabled() ) {
                NioReplicationTask.access$000().trace ( "CKX Cancelling key:" + this.val$key );
            }
        } catch ( Exception x ) {
            NioReplicationTask.access$000().error ( NioReplicationTask.sm.getString ( "nioReplicationTask.error.register.key", this.val$key ), x );
        }
    }
}
