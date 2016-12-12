package org.apache.catalina;
import java.util.concurrent.TimeUnit;
public interface Executor extends java.util.concurrent.Executor, Lifecycle {
    public String getName();
    void execute ( Runnable command, long timeout, TimeUnit unit );
}
