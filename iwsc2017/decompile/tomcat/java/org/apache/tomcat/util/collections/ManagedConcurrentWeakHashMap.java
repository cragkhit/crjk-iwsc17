package org.apache.tomcat.util.collections;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.AbstractSet;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.AbstractMap;
public class ManagedConcurrentWeakHashMap<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K, V> {
    private final ConcurrentMap<Key, V> map;
    private final ReferenceQueue<Object> queue;
    public ManagedConcurrentWeakHashMap() {
        this.map = new ConcurrentHashMap<Key, V>();
        this.queue = new ReferenceQueue<Object>();
    }
    public void maintain() {
        Key key;
        while ( ( key = ( Key ) this.queue.poll() ) != null ) {
            if ( key.isDead() ) {
                continue;
            }
            key.ackDeath();
            this.map.remove ( key );
        }
    }
    private Key createStoreKey ( final Object key ) {
        return new Key ( key, this.queue );
    }
    private Key createLookupKey ( final Object key ) {
        return new Key ( key, null );
    }
    @Override
    public int size() {
        return this.map.size();
    }
    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }
    @Override
    public boolean containsValue ( final Object value ) {
        return value != null && this.map.containsValue ( value );
    }
    @Override
    public boolean containsKey ( final Object key ) {
        return key != null && this.map.containsKey ( this.createLookupKey ( key ) );
    }
    @Override
    public V get ( final Object key ) {
        if ( key == null ) {
            return null;
        }
        return this.map.get ( this.createLookupKey ( key ) );
    }
    @Override
    public V put ( final K key, final V value ) {
        Objects.requireNonNull ( value );
        return this.map.put ( this.createStoreKey ( key ), value );
    }
    @Override
    public V remove ( final Object key ) {
        return this.map.remove ( this.createLookupKey ( key ) );
    }
    @Override
    public void clear() {
        this.map.clear();
        this.maintain();
    }
    @Override
    public V putIfAbsent ( final K key, final V value ) {
        Objects.requireNonNull ( value );
        final Key storeKey = this.createStoreKey ( key );
        final V oldValue = this.map.putIfAbsent ( storeKey, value );
        if ( oldValue != null ) {
            storeKey.ackDeath();
        }
        return oldValue;
    }
    @Override
    public boolean remove ( final Object key, final Object value ) {
        return value != null && this.map.remove ( this.createLookupKey ( key ), value );
    }
    @Override
    public boolean replace ( final K key, final V oldValue, final V newValue ) {
        Objects.requireNonNull ( newValue );
        return this.map.replace ( this.createLookupKey ( key ), oldValue, newValue );
    }
    @Override
    public V replace ( final K key, final V value ) {
        Objects.requireNonNull ( value );
        return this.map.replace ( this.createLookupKey ( key ), value );
    }
    @Override
    public Collection<V> values() {
        return this.map.values();
    }
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return new AbstractSet<Map.Entry<K, V>>() {
            @Override
            public boolean isEmpty() {
                return ManagedConcurrentWeakHashMap.this.map.isEmpty();
            }
            @Override
            public int size() {
                return ManagedConcurrentWeakHashMap.this.map.size();
            }
            @Override
            public Iterator<Map.Entry<K, V>> iterator() {
                return new Iterator<Map.Entry<K, V>>() {
                    private final Iterator<Map.Entry<Key, V>> it = ManagedConcurrentWeakHashMap.this.map.entrySet().iterator();
                    @Override
                    public boolean hasNext() {
                        return this.it.hasNext();
                    }
                    @Override
                    public Map.Entry<K, V> next() {
                        return new Map.Entry<K, V>() {
                            private final Map.Entry<Key, V> en = Iterator.this.it.next();
                            @Override
                            public K getKey() {
                                return ( K ) this.en.getKey().get();
                            }
                            @Override
                            public V getValue() {
                                return this.en.getValue();
                            }
                            @Override
                            public V setValue ( final V value ) {
                                Objects.requireNonNull ( value );
                                return this.en.setValue ( value );
                            }
                        };
                    }
                    @Override
                    public void remove() {
                        this.it.remove();
                    }
                };
            }
        };
    }
    private static class Key extends WeakReference<Object> {
        private final int hash;
        private boolean dead;
        public Key ( final Object key, final ReferenceQueue<Object> queue ) {
            super ( key, queue );
            this.hash = key.hashCode();
        }
        @Override
        public int hashCode() {
            return this.hash;
        }
        @Override
        public boolean equals ( final Object obj ) {
            if ( this == obj ) {
                return true;
            }
            if ( this.dead ) {
                return false;
            }
            if ( ! ( obj instanceof Reference ) ) {
                return false;
            }
            final Object oA = this.get();
            final Object oB = ( ( Reference ) obj ).get();
            return oA == oB || ( oA != null && oB != null && oA.equals ( oB ) );
        }
        public void ackDeath() {
            this.dead = true;
        }
        public boolean isDead() {
            return this.dead;
        }
    }
}
