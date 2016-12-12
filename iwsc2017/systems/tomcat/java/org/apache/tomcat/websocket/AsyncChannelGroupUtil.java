package org.apache.tomcat.websocket;
import java.io.IOException;
import java.nio.channels.AsynchronousChannelGroup;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
public class AsyncChannelGroupUtil {
    private static final StringManager sm =
        StringManager.getManager ( AsyncChannelGroupUtil.class );
    private static AsynchronousChannelGroup group = null;
    private static int usageCount = 0;
    private static final Object lock = new Object();
    private AsyncChannelGroupUtil() {
    }
    public static AsynchronousChannelGroup register() {
        synchronized ( lock ) {
            if ( usageCount == 0 ) {
                group = createAsynchronousChannelGroup();
            }
            usageCount++;
            return group;
        }
    }
    public static void unregister() {
        synchronized ( lock ) {
            usageCount--;
            if ( usageCount == 0 ) {
                group.shutdown();
                group = null;
            }
        }
    }
    private static AsynchronousChannelGroup createAsynchronousChannelGroup() {
        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader (
                AsyncIOThreadFactory.class.getClassLoader() );
            int initialSize = Runtime.getRuntime().availableProcessors();
            ExecutorService executorService = new ThreadPoolExecutor (
                0,
                Integer.MAX_VALUE,
                Long.MAX_VALUE, TimeUnit.MILLISECONDS,
                new SynchronousQueue<Runnable>(),
                new AsyncIOThreadFactory() );
            try {
                return AsynchronousChannelGroup.withCachedThreadPool (
                           executorService, initialSize );
            } catch ( IOException e ) {
                throw new IllegalStateException ( sm.getString ( "asyncChannelGroup.createFail" ) );
            }
        } finally {
            Thread.currentThread().setContextClassLoader ( original );
        }
    }
    private static class AsyncIOThreadFactory implements ThreadFactory {
        static {
            NewThreadPrivilegedAction.load();
        }
        @Override
        public Thread newThread ( final Runnable r ) {
            return AccessController.doPrivileged ( new NewThreadPrivilegedAction ( r ) );
        }
        private static class NewThreadPrivilegedAction implements PrivilegedAction<Thread> {
            private static AtomicInteger count = new AtomicInteger ( 0 );
            private final Runnable r;
            public NewThreadPrivilegedAction ( Runnable r ) {
                this.r = r;
            }
            @Override
            public Thread run() {
                Thread t = new Thread ( r );
                t.setName ( "WebSocketClient-AsyncIO-" + count.incrementAndGet() );
                t.setContextClassLoader ( this.getClass().getClassLoader() );
                t.setDaemon ( true );
                return t;
            }
            private static void load() {
            }
        }
    }
}
