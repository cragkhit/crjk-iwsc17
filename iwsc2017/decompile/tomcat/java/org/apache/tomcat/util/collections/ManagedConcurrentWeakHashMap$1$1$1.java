package org.apache.tomcat.util.collections;
import java.util.Objects;
import java.util.Map;
class ManagedConcurrentWeakHashMap$1$1$1 implements Map.Entry<K, V> {
    private final Map.Entry<Key, V> en = ManagedConcurrentWeakHashMap$1$1.access$100 ( Iterator.this ).next();
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
}
