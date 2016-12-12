package org.apache.tomcat.dbcp.pool2;
import java.util.TimerTask;
private static final class KeyedObjectPoolMinIdleTimerTask<K, V> extends TimerTask {
    private final int minIdle;
    private final K key;
    private final KeyedObjectPool<K, V> keyedPool;
    KeyedObjectPoolMinIdleTimerTask ( final KeyedObjectPool<K, V> keyedPool, final K key, final int minIdle ) throws IllegalArgumentException {
        if ( keyedPool == null ) {
            throw new IllegalArgumentException ( "keyedPool must not be null." );
        }
        this.keyedPool = keyedPool;
        this.key = key;
        this.minIdle = minIdle;
    }
    @Override
    public void run() {
        boolean success = false;
        try {
            if ( this.keyedPool.getNumIdle ( this.key ) < this.minIdle ) {
                this.keyedPool.addObject ( this.key );
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
        sb.append ( "KeyedObjectPoolMinIdleTimerTask" );
        sb.append ( "{minIdle=" ).append ( this.minIdle );
        sb.append ( ", key=" ).append ( this.key );
        sb.append ( ", keyedPool=" ).append ( this.keyedPool );
        sb.append ( '}' );
        return sb.toString();
    }
}
