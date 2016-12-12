package org.apache.tomcat.websocket;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CountDownLatch;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Future;
private static class WrapperFuture<T, A> implements Future<T> {
    private final CompletionHandler<T, A> handler;
    private final A attachment;
    private volatile T result;
    private volatile Throwable throwable;
    private CountDownLatch completionLatch;
    public WrapperFuture() {
        this ( null, null );
    }
    public WrapperFuture ( final CompletionHandler<T, A> handler, final A attachment ) {
        this.result = null;
        this.throwable = null;
        this.completionLatch = new CountDownLatch ( 1 );
        this.handler = handler;
        this.attachment = attachment;
    }
    public void complete ( final T result ) {
        this.result = result;
        this.completionLatch.countDown();
        if ( this.handler != null ) {
            this.handler.completed ( result, this.attachment );
        }
    }
    public void fail ( final Throwable t ) {
        this.throwable = t;
        this.completionLatch.countDown();
        if ( this.handler != null ) {
            this.handler.failed ( this.throwable, this.attachment );
        }
    }
    @Override
    public final boolean cancel ( final boolean mayInterruptIfRunning ) {
        return false;
    }
    @Override
    public final boolean isCancelled() {
        return false;
    }
    @Override
    public final boolean isDone() {
        return this.completionLatch.getCount() > 0L;
    }
    @Override
    public T get() throws InterruptedException, ExecutionException {
        this.completionLatch.await();
        if ( this.throwable != null ) {
            throw new ExecutionException ( this.throwable );
        }
        return this.result;
    }
    @Override
    public T get ( final long timeout, final TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
        final boolean latchResult = this.completionLatch.await ( timeout, unit );
        if ( !latchResult ) {
            throw new TimeoutException();
        }
        if ( this.throwable != null ) {
            throw new ExecutionException ( this.throwable );
        }
        return this.result;
    }
}
