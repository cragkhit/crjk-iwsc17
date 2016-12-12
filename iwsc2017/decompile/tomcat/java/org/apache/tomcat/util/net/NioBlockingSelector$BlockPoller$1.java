package org.apache.tomcat.util.net;
import java.nio.channels.SelectionKey;
class NioBlockingSelector$BlockPoller$1 implements Runnable {
    final   SelectionKey val$key;
    @Override
    public void run() {
        this.val$key.cancel();
    }
}
