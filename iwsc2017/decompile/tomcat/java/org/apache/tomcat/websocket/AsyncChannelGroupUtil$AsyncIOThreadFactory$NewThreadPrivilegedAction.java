package org.apache.tomcat.websocket;
import java.util.concurrent.atomic.AtomicInteger;
import java.security.PrivilegedAction;
private static class NewThreadPrivilegedAction implements PrivilegedAction<Thread> {
    private static AtomicInteger count;
    private final Runnable r;
    public NewThreadPrivilegedAction ( final Runnable r ) {
        this.r = r;
    }
    @Override
    public Thread run() {
        final Thread t = new Thread ( this.r );
        t.setName ( "WebSocketClient-AsyncIO-" + NewThreadPrivilegedAction.count.incrementAndGet() );
        t.setContextClassLoader ( this.getClass().getClassLoader() );
        t.setDaemon ( true );
        return t;
    }
    private static void load() {
    }
    static {
        NewThreadPrivilegedAction.count = new AtomicInteger ( 0 );
    }
}
