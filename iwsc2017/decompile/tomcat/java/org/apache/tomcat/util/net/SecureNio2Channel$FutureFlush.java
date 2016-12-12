package org.apache.tomcat.util.net;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
private class FutureFlush implements Future<Boolean> {
    private Future<Integer> integer;
    protected FutureFlush() {
        this.integer = SecureNio2Channel.this.sc.write ( SecureNio2Channel.this.netOutBuffer );
    }
    @Override
    public boolean cancel ( final boolean mayInterruptIfRunning ) {
        return this.integer.cancel ( mayInterruptIfRunning );
    }
    @Override
    public boolean isCancelled() {
        return this.integer.isCancelled();
    }
    @Override
    public boolean isDone() {
        return this.integer.isDone();
    }
    @Override
    public Boolean get() throws InterruptedException, ExecutionException {
        return this.integer.get() >= 0;
    }
    @Override
    public Boolean get ( final long timeout, final TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
        return this.integer.get ( timeout, unit ) >= 0;
    }
}
