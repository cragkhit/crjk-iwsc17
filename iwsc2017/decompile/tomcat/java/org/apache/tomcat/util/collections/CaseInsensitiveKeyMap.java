package org.apache.tomcat.util.collections;
import java.util.Locale;
import java.util.Iterator;
import java.util.AbstractSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import org.apache.tomcat.util.res.StringManager;
import java.util.AbstractMap;
public class CaseInsensitiveKeyMap<V> extends AbstractMap<String, V> {
    private static final StringManager sm;
    private final Map<Key, V> map;
    public CaseInsensitiveKeyMap() {
        this.map = new HashMap<Key, V>();
    }
    @Override
    public V get ( final Object key ) {
        return this.map.get ( Key.getInstance ( key ) );
    }
    @Override
    public V put ( final String key, final V value ) {
        final Key caseInsensitiveKey = Key.getInstance ( key );
        if ( caseInsensitiveKey == null ) {
            throw new NullPointerException ( CaseInsensitiveKeyMap.sm.getString ( "caseInsensitiveKeyMap.nullKey" ) );
        }
        return this.map.put ( caseInsensitiveKey, value );
    }
    @Override
    public void putAll ( final Map<? extends String, ? extends V> m ) {
        super.putAll ( m );
    }
    @Override
    public boolean containsKey ( final Object key ) {
        return this.map.containsKey ( Key.getInstance ( key ) );
    }
    @Override
    public V remove ( final Object key ) {
        return this.map.remove ( Key.getInstance ( key ) );
    }
    @Override
    public Set<Map.Entry<String, V>> entrySet() {
        return ( Set<Map.Entry<String, V>> ) new EntrySet ( ( Set<Map.Entry<Key, Object>> ) this.map.entrySet() );
    }
    static {
        sm = StringManager.getManager ( CaseInsensitiveKeyMap.class );
    }
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
    private static class Key {
        private final String key;
        private final String lcKey;
        private Key ( final String key ) {
            this.key = key;
            this.lcKey = key.toLowerCase ( Locale.ENGLISH );
        }
        public String getKey() {
            return this.key;
        }
        @Override
        public int hashCode() {
            return this.lcKey.hashCode();
        }
        @Override
        public boolean equals ( final Object obj ) {
            if ( this == obj ) {
                return true;
            }
            if ( obj == null ) {
                return false;
            }
            if ( this.getClass() != obj.getClass() ) {
                return false;
            }
            final Key other = ( Key ) obj;
            return this.lcKey.equals ( other.lcKey );
        }
        public static Key getInstance ( final Object o ) {
            if ( o instanceof String ) {
                return new Key ( ( String ) o );
            }
            return null;
        }
    }
}
