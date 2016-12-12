package javax.el;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
private static final class ConcurrentCache<K, V> {
    private final int size;
    private final Map<K, V> eden;
    private final Map<K, V> longterm;
    public ConcurrentCache ( final int size ) {
        this.size = size;
        this.eden = new ConcurrentHashMap<K, V> ( size );
        this.longterm = new WeakHashMap<K, V> ( size );
    }
    public V get ( final K key ) {
        V value = this.eden.get ( key );
        if ( value == null ) {
            synchronized ( this.longterm ) {
                value = this.longterm.get ( key );
            }
            if ( value != null ) {
                this.eden.put ( key, value );
            }
        }
        return value;
    }
    public void put ( final K key, final V value ) {
        if ( this.eden.size() >= this.size ) {
            synchronized ( this.longterm ) {
                this.longterm.putAll ( ( Map<? extends K, ? extends V> ) this.eden );
            }
            this.eden.clear();
        }
        this.eden.put ( key, value );
    }
}
