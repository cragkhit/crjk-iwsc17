// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool;

import java.lang.ref.WeakReference;
import java.util.TimerTask;

protected static class PoolCleaner extends TimerTask
{
    protected WeakReference<ConnectionPool> pool;
    protected long sleepTime;
    
    PoolCleaner(final ConnectionPool pool, final long sleepTime) {
        this.pool = new WeakReference<ConnectionPool>(pool);
        this.sleepTime = sleepTime;
        if (sleepTime <= 0L) {
            ConnectionPool.access$100().warn((Object)"Database connection pool evicter thread interval is set to 0, defaulting to 30 seconds");
            this.sleepTime = 30000L;
        }
        else if (sleepTime < 1000L) {
            ConnectionPool.access$100().warn((Object)"Database connection pool evicter thread interval is set to lower than 1 second.");
        }
    }
    
    @Override
    public void run() {
        final ConnectionPool pool = this.pool.get();
        if (pool == null) {
            this.stopRunning();
        }
        else if (!pool.isClosed()) {
            try {
                if (pool.getPoolProperties().isRemoveAbandoned() || pool.getPoolProperties().getSuspectTimeout() > 0) {
                    pool.checkAbandoned();
                }
                if (pool.getPoolProperties().getMinIdle() < ConnectionPool.access$300(pool).size()) {
                    pool.checkIdle();
                }
                if (pool.getPoolProperties().isTestWhileIdle()) {
                    pool.testAllIdle();
                }
            }
            catch (Exception x) {
                ConnectionPool.access$100().error((Object)"", (Throwable)x);
            }
        }
    }
    
    public void start() {
        ConnectionPool.access$400(this);
    }
    
    public void stopRunning() {
        ConnectionPool.access$500(this);
    }
}
