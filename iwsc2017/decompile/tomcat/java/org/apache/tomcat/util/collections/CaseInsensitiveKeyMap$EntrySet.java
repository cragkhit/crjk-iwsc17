package org.apache.tomcat.util.collections;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.util.AbstractSet;
private static class EntrySet<V> extends AbstractSet<Map.Entry<String, V>> {
    private final Set<Map.Entry<Key, V>> entrySet;
    public EntrySet ( final Set<Map.Entry<Key, V>> entrySet ) {
        this.entrySet = entrySet;
    }
    @Override
    public Iterator<Map.Entry<String, V>> iterator() {
        return new EntryIterator<V> ( this.entrySet.iterator() );
    }
    @Override
    public int size() {
        return this.entrySet.size();
    }
}
