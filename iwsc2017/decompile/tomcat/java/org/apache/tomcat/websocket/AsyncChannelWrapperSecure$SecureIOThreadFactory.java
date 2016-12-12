package org.apache.tomcat.websocket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ThreadFactory;
private static class SecureIOThreadFactory implements ThreadFactory {
    private AtomicInteger count;
    private SecureIOThreadFactory() {
        this.count = new AtomicInteger ( 0 );
    }
    @Override
    public Thread newThread ( final Runnable r ) {
        final Thread t = new Thread ( r );
        t.setName ( "WebSocketClient-SecureIO-" + this.count.incrementAndGet() );
        t.setDaemon ( true );
        return t;
    }
}
