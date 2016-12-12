package org.apache.catalina.webresources;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.catalina.WebResource;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
public class Cache {
    private static final Log log = LogFactory.getLog ( Cache.class );
    protected static final StringManager sm = StringManager.getManager ( Cache.class );
    private static final long TARGET_FREE_PERCENT_GET = 5;
    private static final long TARGET_FREE_PERCENT_BACKGROUND = 10;
    private static final int OBJECT_MAX_SIZE_FACTOR = 20;
    private final StandardRoot root;
    private final AtomicLong size = new AtomicLong ( 0 );
    private long ttl = 5000;
    private long maxSize = 10 * 1024 * 1024;
    private int objectMaxSize = ( int ) maxSize / OBJECT_MAX_SIZE_FACTOR;
    private AtomicLong lookupCount = new AtomicLong ( 0 );
    private AtomicLong hitCount = new AtomicLong ( 0 );
    private final ConcurrentMap<String, CachedResource> resourceCache =
        new ConcurrentHashMap<>();
    public Cache ( StandardRoot root ) {
        this.root = root;
    }
    protected WebResource getResource ( String path, boolean useClassLoaderResources ) {
        if ( noCache ( path ) ) {
            return root.getResourceInternal ( path, useClassLoaderResources );
        }
        lookupCount.incrementAndGet();
        CachedResource cacheEntry = resourceCache.get ( path );
        if ( cacheEntry != null && !cacheEntry.validateResource ( useClassLoaderResources ) ) {
            removeCacheEntry ( path );
            cacheEntry = null;
        }
        if ( cacheEntry == null ) {
            int objectMaxSizeBytes = getObjectMaxSizeBytes();
            CachedResource newCacheEntry =
                new CachedResource ( this, root, path, getTtl(), objectMaxSizeBytes );
            cacheEntry = resourceCache.putIfAbsent ( path, newCacheEntry );
            if ( cacheEntry == null ) {
                cacheEntry = newCacheEntry;
                cacheEntry.validateResource ( useClassLoaderResources );
                long delta = cacheEntry.getSize();
                size.addAndGet ( delta );
                if ( size.get() > maxSize ) {
                    long targetSize = maxSize * ( 100 - TARGET_FREE_PERCENT_GET ) / 100;
                    long newSize = evict ( targetSize, resourceCache.values().iterator() );
                    if ( newSize > maxSize ) {
                        removeCacheEntry ( path );
                        log.warn ( sm.getString ( "cache.addFail", path, root.getContext().getName() ) );
                    }
                }
            } else {
                cacheEntry.validateResource ( useClassLoaderResources );
            }
        } else {
            hitCount.incrementAndGet();
        }
        return cacheEntry;
    }
    protected WebResource[] getResources ( String path, boolean useClassLoaderResources ) {
        lookupCount.incrementAndGet();
        CachedResource cacheEntry = resourceCache.get ( path );
        if ( cacheEntry != null && !cacheEntry.validateResources ( useClassLoaderResources ) ) {
            removeCacheEntry ( path );
            cacheEntry = null;
        }
        if ( cacheEntry == null ) {
            int objectMaxSizeBytes = getObjectMaxSizeBytes();
            CachedResource newCacheEntry =
                new CachedResource ( this, root, path, getTtl(), objectMaxSizeBytes );
            cacheEntry = resourceCache.putIfAbsent ( path, newCacheEntry );
            if ( cacheEntry == null ) {
                cacheEntry = newCacheEntry;
                cacheEntry.validateResources ( useClassLoaderResources );
                long delta = cacheEntry.getSize();
                size.addAndGet ( delta );
                if ( size.get() > maxSize ) {
                    long targetSize = maxSize * ( 100 - TARGET_FREE_PERCENT_GET ) / 100;
                    long newSize = evict ( targetSize, resourceCache.values().iterator() );
                    if ( newSize > maxSize ) {
                        removeCacheEntry ( path );
                        log.warn ( sm.getString ( "cache.addFail", path ) );
                    }
                }
            } else {
                cacheEntry.validateResources ( useClassLoaderResources );
            }
        } else {
            hitCount.incrementAndGet();
        }
        return cacheEntry.getWebResources();
    }
    protected void backgroundProcess() {
        TreeSet<CachedResource> orderedResources =
            new TreeSet<> ( new EvictionOrder() );
        orderedResources.addAll ( resourceCache.values() );
        Iterator<CachedResource> iter = orderedResources.iterator();
        long targetSize =
            maxSize * ( 100 - TARGET_FREE_PERCENT_BACKGROUND ) / 100;
        long newSize = evict ( targetSize, iter );
        if ( newSize > targetSize ) {
            log.info ( sm.getString ( "cache.backgroundEvictFail",
                                      Long.valueOf ( TARGET_FREE_PERCENT_BACKGROUND ),
                                      root.getContext().getName(),
                                      Long.valueOf ( newSize / 1024 ) ) );
        }
    }
    private boolean noCache ( String path ) {
        if ( ( path.endsWith ( ".class" ) &&
                ( path.startsWith ( "/WEB-INF/classes/" ) || path.startsWith ( "/WEB-INF/lib/" ) ) )
                ||
                ( path.startsWith ( "/WEB-INF/lib/" ) && path.endsWith ( ".jar" ) ) ) {
            return true;
        }
        return false;
    }
    private long evict ( long targetSize, Iterator<CachedResource> iter ) {
        long now = System.currentTimeMillis();
        long newSize = size.get();
        while ( newSize > targetSize && iter.hasNext() ) {
            CachedResource resource = iter.next();
            if ( resource.getNextCheck() > now ) {
                continue;
            }
            removeCacheEntry ( resource.getWebappPath() );
            newSize = size.get();
        }
        return newSize;
    }
    void removeCacheEntry ( String path ) {
        CachedResource cachedResource = resourceCache.remove ( path );
        if ( cachedResource != null ) {
            long delta = cachedResource.getSize();
            size.addAndGet ( -delta );
        }
    }
    public long getTtl() {
        return ttl;
    }
    public void setTtl ( long ttl ) {
        this.ttl = ttl;
    }
    public long getMaxSize() {
        return maxSize / 1024;
    }
    public void setMaxSize ( long maxSize ) {
        this.maxSize = maxSize * 1024;
    }
    public long getLookupCount() {
        return lookupCount.get();
    }
    public long getHitCount() {
        return hitCount.get();
    }
    public void setObjectMaxSize ( int objectMaxSize ) {
        if ( objectMaxSize * 1024L > Integer.MAX_VALUE ) {
            log.warn ( sm.getString ( "cache.objectMaxSizeTooBigBytes", Integer.valueOf ( objectMaxSize ) ) );
            this.objectMaxSize = Integer.MAX_VALUE;
        }
        this.objectMaxSize = objectMaxSize * 1024;
    }
    public int getObjectMaxSize() {
        return objectMaxSize / 1024;
    }
    public int getObjectMaxSizeBytes() {
        return objectMaxSize;
    }
    void enforceObjectMaxSizeLimit() {
        long limit = maxSize / OBJECT_MAX_SIZE_FACTOR;
        if ( limit > Integer.MAX_VALUE ) {
            return;
        }
        if ( objectMaxSize > limit ) {
            log.warn ( sm.getString ( "cache.objectMaxSizeTooBig",
                                      Integer.valueOf ( objectMaxSize / 1024 ), Integer.valueOf ( ( int ) limit / 1024 ) ) );
            objectMaxSize = ( int ) limit;
        }
    }
    public void clear() {
        resourceCache.clear();
        size.set ( 0 );
    }
    public long getSize() {
        return size.get() / 1024;
    }
    private static class EvictionOrder implements Comparator<CachedResource> {
        @Override
        public int compare ( CachedResource cr1, CachedResource cr2 ) {
            long nc1 = cr1.getNextCheck();
            long nc2 = cr2.getNextCheck();
            if ( nc1 == nc2 ) {
                return 0;
            } else if ( nc1 > nc2 ) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}
