package org.apache.tomcat.dbcp.pool2;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
private static final class ErodingPerKeyKeyedObjectPool<K, V> extends ErodingKeyedObjectPool<K, V> {
    private final float factor;
    private final Map<K, ErodingFactor> factors;
    public ErodingPerKeyKeyedObjectPool ( final KeyedObjectPool<K, V> keyedPool, final float factor ) {
        super ( keyedPool, null );
        this.factors = Collections.synchronizedMap ( new HashMap<K, ErodingFactor>() );
        this.factor = factor;
    }
    @Override
    protected ErodingFactor getErodingFactor ( final K key ) {
        ErodingFactor eFactor = this.factors.get ( key );
        if ( eFactor == null ) {
            eFactor = new ErodingFactor ( this.factor );
            this.factors.put ( key, eFactor );
        }
        return eFactor;
    }
    @Override
    public String toString() {
        return "ErodingPerKeyKeyedObjectPool{factor=" + this.factor + ", keyedPool=" + this.getKeyedPool() + '}';
    }
}
