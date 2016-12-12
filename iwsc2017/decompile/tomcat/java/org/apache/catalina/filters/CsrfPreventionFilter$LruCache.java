package org.apache.catalina.filters;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.Serializable;
protected static class LruCache<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Map<T, T> cache;
    public LruCache ( final int cacheSize ) {
        this.cache = new LinkedHashMap<T, T>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected boolean removeEldestEntry ( final Map.Entry<T, T> eldest ) {
                return this.size() > cacheSize;
            }
        };
    }
    public void add ( final T key ) {
        synchronized ( this.cache ) {
            this.cache.put ( key, null );
        }
    }
    public boolean contains ( final T key ) {
        synchronized ( this.cache ) {
            return this.cache.containsKey ( key );
        }
    }
}
