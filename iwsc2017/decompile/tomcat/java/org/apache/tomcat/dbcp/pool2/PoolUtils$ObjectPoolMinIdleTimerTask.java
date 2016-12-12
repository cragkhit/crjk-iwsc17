package org.apache.tomcat.dbcp.pool2;
import java.util.TimerTask;
private static final class ObjectPoolMinIdleTimerTask<T> extends TimerTask {
    private final int minIdle;
    private final ObjectPool<T> pool;
    ObjectPoolMinIdleTimerTask ( final ObjectPool<T> pool, final int minIdle ) throws IllegalArgumentException {
        if ( pool == null ) {
            throw new IllegalArgumentException ( "pool must not be null." );
        }
        this.pool = pool;
        this.minIdle = minIdle;
    }
    @Override
    public void run() {
        boolean success = false;
        try {
            if ( this.pool.getNumIdle() < this.minIdle ) {
                this.pool.addObject();
            }
            success = true;
        } catch ( Exception e ) {
            this.cancel();
        } finally {
            if ( !success ) {
                this.cancel();
            }
        }
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append ( "ObjectPoolMinIdleTimerTask" );
        sb.append ( "{minIdle=" ).append ( this.minIdle );
        sb.append ( ", pool=" ).append ( this.pool );
        sb.append ( '}' );
        return sb.toString();
    }
}
