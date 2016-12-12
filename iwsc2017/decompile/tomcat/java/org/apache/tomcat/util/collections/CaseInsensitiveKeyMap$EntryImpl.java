package org.apache.tomcat.util.collections;
import java.util.Map;
private static class EntryImpl<V> implements Map.Entry<String, V> {
    private final String key;
    private final V value;
    public EntryImpl ( final String key, final V value ) {
        this.key = key;
        this.value = value;
    }
    @Override
    public String getKey() {
        return this.key;
    }
    @Override
    public V getValue() {
        return this.value;
    }
    @Override
    public V setValue ( final V value ) {
        throw new UnsupportedOperationException();
    }
}
