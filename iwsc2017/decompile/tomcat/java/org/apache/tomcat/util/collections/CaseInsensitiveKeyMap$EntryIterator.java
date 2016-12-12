package org.apache.tomcat.util.collections;
import java.util.Map;
import java.util.Iterator;
private static class EntryIterator<V> implements Iterator<Map.Entry<String, V>> {
    private final Iterator<Map.Entry<Key, V>> iterator;
    public EntryIterator ( final Iterator<Map.Entry<Key, V>> iterator ) {
        this.iterator = iterator;
    }
    @Override
    public boolean hasNext() {
        return this.iterator.hasNext();
    }
    @Override
    public Map.Entry<String, V> next() {
        final Map.Entry<Key, V> entry = this.iterator.next();
        return new EntryImpl<V> ( entry.getKey().getKey(), entry.getValue() );
    }
    @Override
    public void remove() {
        this.iterator.remove();
    }
}
