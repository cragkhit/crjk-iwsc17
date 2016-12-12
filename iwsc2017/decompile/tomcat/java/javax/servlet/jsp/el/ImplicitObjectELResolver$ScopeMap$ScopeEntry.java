package javax.servlet.jsp.el;
import java.util.Map;
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
