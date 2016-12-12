package javax.servlet.jsp.el;
import java.util.Objects;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Enumeration;
import java.util.AbstractMap;
private abstract static class ScopeMap<V> extends AbstractMap<String, V> {
    protected abstract Enumeration<String> getAttributeNames();
    protected abstract V getAttribute ( final String p0 );
    protected void removeAttribute ( final String name ) {
        throw new UnsupportedOperationException();
    }
    protected void setAttribute ( final String name, final Object value ) {
        throw new UnsupportedOperationException();
    }
    @Override
    public final Set<Map.Entry<String, V>> entrySet() {
        final Enumeration<String> e = this.getAttributeNames();
        final Set<Map.Entry<String, V>> set = new HashSet<Map.Entry<String, V>>();
        if ( e != null ) {
            while ( e.hasMoreElements() ) {
                set.add ( new ScopeEntry ( e.nextElement() ) );
            }
        }
        return set;
    }
    @Override
    public final int size() {
        int size = 0;
        final Enumeration<String> e = this.getAttributeNames();
        if ( e != null ) {
            while ( e.hasMoreElements() ) {
                e.nextElement();
                ++size;
            }
        }
        return size;
    }
    @Override
    public final boolean containsKey ( final Object key ) {
        if ( key == null ) {
            return false;
        }
        final Enumeration<String> e = this.getAttributeNames();
        if ( e != null ) {
            while ( e.hasMoreElements() ) {
                if ( key.equals ( e.nextElement() ) ) {
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    public final V get ( final Object key ) {
        if ( key != null ) {
            return this.getAttribute ( ( String ) key );
        }
        return null;
    }
    @Override
    public final V put ( final String key, final V value ) {
        Objects.requireNonNull ( key );
        if ( value == null ) {
            this.removeAttribute ( key );
        } else {
            this.setAttribute ( key, value );
        }
        return null;
    }
    @Override
    public final V remove ( final Object key ) {
        Objects.requireNonNull ( key );
        this.removeAttribute ( ( String ) key );
        return null;
    }
    private class ScopeEntry implements Map.Entry<String, V> {
        private final String key;
        public ScopeEntry ( final String key ) {
            this.key = key;
        }
        @Override
        public String getKey() {
            return this.key;
        }
        @Override
        public V getValue() {
            return ScopeMap.this.getAttribute ( this.key );
        }
        @Override
        public V setValue ( final Object value ) {
            if ( value == null ) {
                ScopeMap.this.removeAttribute ( this.key );
            } else {
                ScopeMap.this.setAttribute ( this.key, value );
            }
            return null;
        }
        @Override
        public boolean equals ( final Object obj ) {
            return obj != null && this.hashCode() == obj.hashCode();
        }
        @Override
        public int hashCode() {
            return this.key.hashCode();
        }
    }
}
