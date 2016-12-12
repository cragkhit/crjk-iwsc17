package org.apache.tomcat.websocket;
import java.util.concurrent.atomic.AtomicInteger;
import java.security.PrivilegedAction;
import java.security.AccessController;
import java.util.concurrent.ExecutorService;
import java.io.IOException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.BlockingQueue;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.nio.channels.AsynchronousChannelGroup;
import org.apache.tomcat.util.res.StringManager;
public class AsyncChannelGroupUtil {
    private static final StringManager sm;
    private static AsynchronousChannelGroup group;
    private static int usageCount;
    private static final Object lock;
    public static AsynchronousChannelGroup register() {
        synchronized ( AsyncChannelGroupUtil.lock ) {
            if ( AsyncChannelGroupUtil.usageCount == 0 ) {
                AsyncChannelGroupUtil.group = createAsynchronousChannelGroup();
            }
            ++AsyncChannelGroupUtil.usageCount;
            return AsyncChannelGroupUtil.group;
        }
    }
    public static void unregister() {
        synchronized ( AsyncChannelGroupUtil.lock ) {
            --AsyncChannelGroupUtil.usageCount;
            if ( AsyncChannelGroupUtil.usageCount == 0 ) {
                AsyncChannelGroupUtil.group.shutdown();
                AsyncChannelGroupUtil.group = null;
            }
        }
    }
    private static AsynchronousChannelGroup createAsynchronousChannelGroup() {
        final ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader ( AsyncIOThreadFactory.class.getClassLoader() );
            final int initialSize = Runtime.getRuntime().availableProcessors();
            final ExecutorService executorService = new ThreadPoolExecutor ( 0, Integer.MAX_VALUE, Long.MAX_VALUE, TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>(), new AsyncIOThreadFactory() );
            try {
                return AsynchronousChannelGroup.withCachedThreadPool ( executorService, initialSize );
            } catch ( IOException e ) {
                throw new IllegalStateException ( AsyncChannelGroupUtil.sm.getString ( "asyncChannelGroup.createFail" ) );
            }
        } finally {
            Thread.currentThread().setContextClassLoader ( original );
        }
    }
    static {
        sm = StringManager.getManager ( AsyncChannelGroupUtil.class );
        AsyncChannelGroupUtil.group = null;
        AsyncChannelGroupUtil.usageCount = 0;
        lock = new Object();
    }
    private static class AsyncIOThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread ( final Runnable r ) {
            return AccessController.doPrivileged ( ( PrivilegedAction<Thread> ) new NewThreadPrivilegedAction ( r ) );
        }
        static {
            load();
        }
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
    }
}
