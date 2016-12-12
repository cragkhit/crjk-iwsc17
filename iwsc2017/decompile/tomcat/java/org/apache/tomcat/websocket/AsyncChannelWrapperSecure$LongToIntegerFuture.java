package org.apache.tomcat.websocket;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
private static final class LongToIntegerFuture implements Future<Integer> {
    private final Future<Long> wrapped;
    public LongToIntegerFuture ( final Future<Long> wrapped ) {
        this.wrapped = wrapped;
    }
    @Override
    public boolean cancel ( final boolean mayInterruptIfRunning ) {
        return this.wrapped.cancel ( mayInterruptIfRunning );
    }
    @Override
    public boolean isCancelled() {
        return this.wrapped.isCancelled();
    }
    @Override
    public boolean isDone() {
        return this.wrapped.isDone();
    }
    @Override
    public Integer get() throws InterruptedException, ExecutionException {
        final Long result = this.wrapped.get();
        if ( result > 2147483647L ) {
            throw new ExecutionException ( AsyncChannelWrapperSecure.access$300().getString ( "asyncChannelWrapperSecure.tooBig", result ), null );
        }
        return ( int ) ( Object ) result;
    }
    @Override
    public Integer get ( final long timeout, final TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
        final Long result = this.wrapped.get ( timeout, unit );
        if ( result > 2147483647L ) {
            throw new ExecutionException ( AsyncChannelWrapperSecure.access$300().getString ( "asyncChannelWrapperSecure.tooBig", result ), null );
        }
        return ( int ) ( Object ) result;
    }
}
