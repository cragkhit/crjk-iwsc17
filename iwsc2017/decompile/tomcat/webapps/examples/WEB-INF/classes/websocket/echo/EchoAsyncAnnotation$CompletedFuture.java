// 
// Decompiled by Procyon v0.5.29
// 

package websocket.echo;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

private static class CompletedFuture implements Future<Void>
{
    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
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
    public Void get() throws InterruptedException, ExecutionException {
        return null;
    }
    
    @Override
    public Void get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }
}
