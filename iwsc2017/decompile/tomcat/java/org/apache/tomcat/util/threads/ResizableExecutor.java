package org.apache.tomcat.util.threads;
import java.util.concurrent.Executor;
public interface ResizableExecutor extends Executor {
    int getPoolSize();
    int getMaxThreads();
    int getActiveCount();
    boolean resizePool ( int p0, int p1 );
    boolean resizeQueue ( int p0 );
}
