package org.apache.catalina.filters;
import java.util.Map;
import java.util.LinkedHashMap;
class CsrfPreventionFilter$LruCache$1 extends LinkedHashMap<T, T> {
    private static final long serialVersionUID = 1L;
    final   int val$cacheSize;
    @Override
    protected boolean removeEldestEntry ( final Map.Entry<T, T> eldest ) {
        return this.size() > this.val$cacheSize;
    }
}
