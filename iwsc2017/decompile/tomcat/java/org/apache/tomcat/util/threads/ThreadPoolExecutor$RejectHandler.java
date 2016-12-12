package org.apache.tomcat.util.threads;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.RejectedExecutionHandler;
private static class RejectHandler implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution ( final Runnable r, final java.util.concurrent.ThreadPoolExecutor executor ) {
        throw new RejectedExecutionException();
    }
}
