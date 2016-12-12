package org.apache.tomcat.util.net;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
static final class Nio2Channel$1 implements Future<Boolean> {
    @Override
    public boolean cancel ( final boolean mayInterruptIfRunning ) {
        return false;
    }
    @Override
    public boolean isCancelled() {
        return false;
    }
    @Override
    public boolean isDone() {
        return true;
    }
    @Override
    public Boolean get() throws InterruptedException, ExecutionException {
        return Boolean.TRUE;
    }
    @Override
    public Boolean get ( final long timeout, final TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
        return Boolean.TRUE;
    }
}
