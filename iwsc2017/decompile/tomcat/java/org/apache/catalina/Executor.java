package org.apache.catalina;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executor;
public interface Executor extends java.util.concurrent.Executor, Lifecycle {
    String getName();
    void execute ( Runnable p0, long p1, TimeUnit p2 );
}
