package org.apache.tomcat.util.collections;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
public class ManagedConcurrentWeakHashMap<K, V> extends AbstractMap<K, V> implements
    ConcurrentMap<K, V> {
    private final ConcurrentMap<Key, V> map = new ConcurrentHashMap<>();
    private final ReferenceQueue<Object> queue = new ReferenceQueue<>();
    public void maintain() {
        Key key;
        while ( ( key = ( Key ) queue.poll() ) != null ) {
            if ( key.isDead() ) {
                continue;
            }
            key.ackDeath();
            map.remove ( key );
        }
    }
    private static class Key extends WeakReference<Object> {
        private final int hash;
        private boolean dead;
        public Key ( Object key, ReferenceQueue<Object> queue ) {
            super ( key, queue );
            hash = key.hashCode();
        }
        @Override
        public int hashCode() {
            return hash;
        }
        @Override
        public boolean equals ( Object obj ) {
            if ( this == obj ) {
                return true;
            }
            if ( dead ) {
                return false;
            }
            if ( ! ( obj instanceof Reference<?> ) ) {
                return false;
            }
            Object oA = get();
            Object oB = ( ( Reference<?> ) obj ).get();
            if ( oA == oB ) {
                return true;
            }
            if ( oA == null || oB == null ) {
                return false;
            }
            return oA.equals ( oB );
        }
        public void ackDeath() {
            this.dead = true;
        }
        public boolean isDead() {
            return dead;
        }
    }
    private Key createStoreKey ( Object key ) {
        return new Key ( key, queue );
    }
    private Key createLookupKey ( Object key ) {
        return new Key ( key, null );
    }
    @Override
    public int size() {
        return map.size();
    }
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }
    @Override
    public boolean containsValue ( Object value ) {
        if ( value == null ) {
            return false;
        }
        return map.containsValue ( value );
    }
    @Override
    public boolean containsKey ( Object key ) {
        if ( key == null ) {
            return false;
        }
        return map.containsKey ( createLookupKey ( key ) );
    }
    @Override
    public V get ( Object key ) {
        if ( key == null ) {
            return null;
        }
        return map.get ( createLookupKey ( key ) );
    }
    @Override
    public V put ( K key, V value ) {
        Objects.requireNonNull ( value );
        return map.put ( createStoreKey ( key ), value );
    }
    @Override
    public V remove ( Object key ) {
        return map.remove ( createLookupKey ( key ) );
    }
    @Override
    public void clear() {
        map.clear();
        maintain();
    }
    @Override
    public V putIfAbsent ( K key, V value ) {
        Objects.requireNonNull ( value );
        Key storeKey = createStoreKey ( key );
        V oldValue = map.putIfAbsent ( storeKey, value );
        if ( oldValue != null ) {
            storeKey.ackDeath();
        }
        return oldValue;
    }
    @Override
    public boolean remove ( Object key, Object value ) {
        if ( value == null ) {
            return false;
        }
        return map.remove ( createLookupKey ( key ), value );
    }
    @Override
    public boolean replace ( K key, V oldValue, V newValue ) {
        Objects.requireNonNull ( newValue );
        return map.replace ( createLookupKey ( key ), oldValue, newValue );
    }
    @Override
    public V replace ( K key, V value ) {
        Objects.requireNonNull ( value );
        return map.replace ( createLookupKey ( key ), value );
    }
    @Override
    public Collection<V> values() {
        return map.values();
    }
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return new AbstractSet<Map.Entry<K, V>>() {
            @Override
            public boolean isEmpty() {
                return map.isEmpty();
            }
            @Override
            public int size() {
                return map.size();
            }
            @Override
            public Iterator<Map.Entry<K, V>> iterator() {
                return new Iterator<Map.Entry<K, V>>() {
                    private final Iterator<Map.Entry<Key, V>> it = map
                            .entrySet().iterator();
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }
                    @Override
                    public Map.Entry<K, V> next() {
                        return new Map.Entry<K, V>() {
                            private final Map.Entry<Key, V> en = it.next();
                            @SuppressWarnings ( "unchecked" )
                            @Override
                            public K getKey() {
                                return ( K ) en.getKey().get();
                            }
                            @Override
                            public V getValue() {
                                return en.getValue();
                            }
                            @Override
                            public V setValue ( V value ) {
                                Objects.requireNonNull ( value );
                                return en.setValue ( value );
                            }
                        };
                    }
                    @Override
                    public void remove() {
                        it.remove();
                    }
                };
            }
        };
    }
}
