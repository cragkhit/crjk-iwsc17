package org.apache.tomcat.dbcp.pool2;
public abstract class BaseKeyedPooledObjectFactory<K, V> extends BaseObject
    implements KeyedPooledObjectFactory<K, V> {
    public abstract V create ( K key )
    throws Exception;
    public abstract PooledObject<V> wrap ( V value );
    @Override
    public PooledObject<V> makeObject ( final K key ) throws Exception {
        return wrap ( create ( key ) );
    }
    @Override
    public void destroyObject ( final K key, final PooledObject<V> p )
    throws Exception {
    }
    @Override
    public boolean validateObject ( final K key, final PooledObject<V> p ) {
        return true;
    }
    @Override
    public void activateObject ( final K key, final PooledObject<V> p )
    throws Exception {
    }
    @Override
    public void passivateObject ( final K key, final PooledObject<V> p )
    throws Exception {
    }
}
