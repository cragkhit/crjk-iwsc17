// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.sql.Connection;
import java.util.concurrent.Future;

protected class ConnectionFuture implements Future<Connection>, Runnable
{
    Future<PooledConnection> pcFuture;
    AtomicBoolean configured;
    CountDownLatch latch;
    volatile Connection result;
    SQLException cause;
    AtomicBoolean cancelled;
    volatile PooledConnection pc;
    
    public ConnectionFuture(final Future<PooledConnection> pcf) {
        this.pcFuture = null;
        this.configured = new AtomicBoolean(false);
        this.latch = new CountDownLatch(1);
        this.result = null;
        this.cause = null;
        this.cancelled = new AtomicBoolean(false);
        this.pc = null;
        this.pcFuture = pcf;
    }
    
    public ConnectionFuture(final PooledConnection pc) throws SQLException {
        this.pcFuture = null;
        this.configured = new AtomicBoolean(false);
        this.latch = new CountDownLatch(1);
        this.result = null;
        this.cause = null;
        this.cancelled = new AtomicBoolean(false);
        this.pc = null;
        this.pc = pc;
        this.result = ConnectionPool.this.setupConnection(pc);
        this.configured.set(true);
    }
    
    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        if (this.pc != null) {
            return false;
        }
        if (!this.cancelled.get() && this.cancelled.compareAndSet(false, true)) {
            ConnectionPool.access$000(ConnectionPool.this).execute(this);
        }
        return true;
    }
    
    @Override
    public Connection get() throws InterruptedException, ExecutionException {
        try {
            return this.get(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        }
        catch (TimeoutException x) {
            throw new ExecutionException(x);
        }
    }
    
    @Override
    public Connection get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        PooledConnection pc = (this.pc != null) ? this.pc : this.pcFuture.get(timeout, unit);
        if (pc == null) {
            return null;
        }
        if (this.result != null) {
            return this.result;
        }
        if (this.configured.compareAndSet(false, true)) {
            try {
                pc = ConnectionPool.this.borrowConnection(System.currentTimeMillis(), pc, null, null);
                this.result = ConnectionPool.this.setupConnection(pc);
            }
            catch (SQLException x) {
                this.cause = x;
            }
            finally {
                this.latch.countDown();
            }
        }
        else {
            this.latch.await(timeout, unit);
        }
        if (this.result == null) {
            throw new ExecutionException(this.cause);
        }
        return this.result;
    }
    
    @Override
    public boolean isCancelled() {
        return this.pc == null && (this.pcFuture.isCancelled() || this.cancelled.get());
    }
    
    @Override
    public boolean isDone() {
        return this.pc != null || this.pcFuture.isDone();
    }
    
    @Override
    public void run() {
        try {
            final Connection con = this.get();
            con.close();
        }
        catch (ExecutionException ex) {}
        catch (Exception x) {
            ConnectionPool.access$100().error((Object)"Unable to cancel ConnectionFuture.", (Throwable)x);
        }
    }
}
