// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

protected class ItemFuture<T> implements Future<T>
{
    protected volatile T item;
    protected volatile ExchangeCountDownLatch<T> latch;
    protected volatile boolean canceled;
    
    public ItemFuture(final T item) {
        this.item = null;
        this.latch = null;
        this.canceled = false;
        this.item = item;
    }
    
    public ItemFuture(final ExchangeCountDownLatch<T> latch) {
        this.item = null;
        this.latch = null;
        this.canceled = false;
        this.latch = latch;
    }
    
    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        return false;
    }
    
    @Override
    public T get() throws InterruptedException, ExecutionException {
        if (this.item != null) {
            return this.item;
        }
        if (this.latch != null) {
            this.latch.await();
            return this.latch.getItem();
        }
        throw new ExecutionException("ItemFuture incorrectly instantiated. Bug in the code?", new Exception());
    }
    
    @Override
    public T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (this.item != null) {
            return this.item;
        }
        if (this.latch == null) {
            throw new ExecutionException("ItemFuture incorrectly instantiated. Bug in the code?", new Exception());
        }
        final boolean timedout = !this.latch.await(timeout, unit);
        if (timedout) {
            throw new TimeoutException();
        }
        return this.latch.getItem();
    }
    
    @Override
    public boolean isCancelled() {
        return false;
    }
    
    @Override
    public boolean isDone() {
        return this.item != null || this.latch.getItem() != null;
    }
}
