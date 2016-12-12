package org.apache.catalina.tribes.transport.nio;
import java.nio.channels.SelectionKey;
class NioReplicationTask$2 implements Runnable {
    final   SelectionKey val$key;
    @Override
    public void run() {
        if ( NioReplicationTask.access$000().isTraceEnabled() ) {
            NioReplicationTask.access$000().trace ( "Cancelling key:" + this.val$key );
        }
        NioReceiver.cancelledKey ( this.val$key );
    }
}
