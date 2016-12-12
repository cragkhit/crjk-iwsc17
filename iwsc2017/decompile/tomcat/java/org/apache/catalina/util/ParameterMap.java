package org.apache.catalina.util;
import java.util.Map;
import org.apache.tomcat.util.res.StringManager;
import java.util.LinkedHashMap;
public final class ParameterMap<K, V> extends LinkedHashMap<K, V> {
    private static final long serialVersionUID = 1L;
    private boolean locked;
    private static final StringManager sm;
    public ParameterMap() {
        this.locked = false;
    }
    public ParameterMap ( final int initialCapacity ) {
        super ( initialCapacity );
        this.locked = false;
    }
    public ParameterMap ( final int initialCapacity, final float loadFactor ) {
        super ( initialCapacity, loadFactor );
        this.locked = false;
    }
    public ParameterMap ( final Map<K, V> map ) {
        super ( map );
        this.locked = false;
    }
    public boolean isLocked() {
        return this.locked;
    }
    public void setLocked ( final boolean locked ) {
        this.locked = locked;
    }
    @Override
    public void clear() {
        if ( this.locked ) {
            throw new IllegalStateException ( ParameterMap.sm.getString ( "parameterMap.locked" ) );
        }
        super.clear();
    }
    @Override
    public V put ( final K key, final V value ) {
        if ( this.locked ) {
            throw new IllegalStateException ( ParameterMap.sm.getString ( "parameterMap.locked" ) );
        }
        return super.put ( key, value );
    }
    @Override
    public void putAll ( final Map<? extends K, ? extends V> map ) {
        if ( this.locked ) {
            throw new IllegalStateException ( ParameterMap.sm.getString ( "parameterMap.locked" ) );
        }
        super.putAll ( map );
    }
    @Override
    public V remove ( final Object key ) {
        if ( this.locked ) {
            throw new IllegalStateException ( ParameterMap.sm.getString ( "parameterMap.locked" ) );
        }
        return super.remove ( key );
    }
    static {
        sm = StringManager.getManager ( "org.apache.catalina.util" );
    }
}
