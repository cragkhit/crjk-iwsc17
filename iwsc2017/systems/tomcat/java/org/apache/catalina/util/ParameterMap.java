package org.apache.catalina.util;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.tomcat.util.res.StringManager;
public final class ParameterMap<K, V> extends LinkedHashMap<K, V> {
    private static final long serialVersionUID = 1L;
    public ParameterMap() {
        super();
    }
    public ParameterMap ( int initialCapacity ) {
        super ( initialCapacity );
    }
    public ParameterMap ( int initialCapacity, float loadFactor ) {
        super ( initialCapacity, loadFactor );
    }
    public ParameterMap ( Map<K, V> map ) {
        super ( map );
    }
    private boolean locked = false;
    public boolean isLocked() {
        return ( this.locked );
    }
    public void setLocked ( boolean locked ) {
        this.locked = locked;
    }
    private static final StringManager sm =
        StringManager.getManager ( "org.apache.catalina.util" );
    @Override
    public void clear() {
        if ( locked )
            throw new IllegalStateException
            ( sm.getString ( "parameterMap.locked" ) );
        super.clear();
    }
    @Override
    public V put ( K key, V value ) {
        if ( locked )
            throw new IllegalStateException
            ( sm.getString ( "parameterMap.locked" ) );
        return ( super.put ( key, value ) );
    }
    @Override
    public void putAll ( Map<? extends K, ? extends V> map ) {
        if ( locked )
            throw new IllegalStateException
            ( sm.getString ( "parameterMap.locked" ) );
        super.putAll ( map );
    }
    @Override
    public V remove ( Object key ) {
        if ( locked )
            throw new IllegalStateException
            ( sm.getString ( "parameterMap.locked" ) );
        return ( super.remove ( key ) );
    }
}
