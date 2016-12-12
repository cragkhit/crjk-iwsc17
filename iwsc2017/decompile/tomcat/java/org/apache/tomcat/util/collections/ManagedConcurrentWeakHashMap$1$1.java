package org.apache.tomcat.util.collections;
import java.util.Objects;
import java.util.Map;
import java.util.Iterator;
class ManagedConcurrentWeakHashMap$1$1 implements Iterator<Map.Entry<K, V>> {
    private final Iterator<Map.Entry<Key, V>> it = ManagedConcurrentWeakHashMap.access$000 ( AbstractSet.this.this$0 ).entrySet().iterator();
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
}
