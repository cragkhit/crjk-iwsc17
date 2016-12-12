package org.apache.catalina.webresources;
import org.apache.juli.logging.LogFactory;
import java.util.Iterator;
import java.util.Comparator;
import java.util.TreeSet;
import org.apache.catalina.WebResource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
public class Cache {
    private static final Log log;
    protected static final StringManager sm;
    private static final long TARGET_FREE_PERCENT_GET = 5L;
    private static final long TARGET_FREE_PERCENT_BACKGROUND = 10L;
    private static final int OBJECT_MAX_SIZE_FACTOR = 20;
    private final StandardRoot root;
    private final AtomicLong size;
    private long ttl;
    private long maxSize;
    private int objectMaxSize;
    private AtomicLong lookupCount;
    private AtomicLong hitCount;
    private final ConcurrentMap<String, CachedResource> resourceCache;
    public Cache ( final StandardRoot root ) {
        this.size = new AtomicLong ( 0L );
        this.ttl = 5000L;
        this.maxSize = 10485760L;
        this.objectMaxSize = ( int ) this.maxSize / 20;
        this.lookupCount = new AtomicLong ( 0L );
        this.hitCount = new AtomicLong ( 0L );
        this.resourceCache = new ConcurrentHashMap<String, CachedResource>();
        this.root = root;
    }
    protected WebResource getResource ( final String path, final boolean useClassLoaderResources ) {
        if ( this.noCache ( path ) ) {
            return this.root.getResourceInternal ( path, useClassLoaderResources );
        }
        this.lookupCount.incrementAndGet();
        CachedResource cacheEntry = this.resourceCache.get ( path );
        if ( cacheEntry != null && !cacheEntry.validateResource ( useClassLoaderResources ) ) {
            this.removeCacheEntry ( path );
            cacheEntry = null;
        }
        if ( cacheEntry == null ) {
            final int objectMaxSizeBytes = this.getObjectMaxSizeBytes();
            final CachedResource newCacheEntry = new CachedResource ( this, this.root, path, this.getTtl(), objectMaxSizeBytes );
            cacheEntry = this.resourceCache.putIfAbsent ( path, newCacheEntry );
            if ( cacheEntry == null ) {
                cacheEntry = newCacheEntry;
                cacheEntry.validateResource ( useClassLoaderResources );
                final long delta = cacheEntry.getSize();
                this.size.addAndGet ( delta );
                if ( this.size.get() > this.maxSize ) {
                    final long targetSize = this.maxSize * 95L / 100L;
                    final long newSize = this.evict ( targetSize, this.resourceCache.values().iterator() );
                    if ( newSize > this.maxSize ) {
                        this.removeCacheEntry ( path );
                        Cache.log.warn ( Cache.sm.getString ( "cache.addFail", path, this.root.getContext().getName() ) );
                    }
                }
            } else {
                cacheEntry.validateResource ( useClassLoaderResources );
            }
        } else {
            this.hitCount.incrementAndGet();
        }
        return cacheEntry;
    }
    protected WebResource[] getResources ( final String path, final boolean useClassLoaderResources ) {
        this.lookupCount.incrementAndGet();
        CachedResource cacheEntry = this.resourceCache.get ( path );
        if ( cacheEntry != null && !cacheEntry.validateResources ( useClassLoaderResources ) ) {
            this.removeCacheEntry ( path );
            cacheEntry = null;
        }
        if ( cacheEntry == null ) {
            final int objectMaxSizeBytes = this.getObjectMaxSizeBytes();
            final CachedResource newCacheEntry = new CachedResource ( this, this.root, path, this.getTtl(), objectMaxSizeBytes );
            cacheEntry = this.resourceCache.putIfAbsent ( path, newCacheEntry );
            if ( cacheEntry == null ) {
                cacheEntry = newCacheEntry;
                cacheEntry.validateResources ( useClassLoaderResources );
                final long delta = cacheEntry.getSize();
                this.size.addAndGet ( delta );
                if ( this.size.get() > this.maxSize ) {
                    final long targetSize = this.maxSize * 95L / 100L;
                    final long newSize = this.evict ( targetSize, this.resourceCache.values().iterator() );
                    if ( newSize > this.maxSize ) {
                        this.removeCacheEntry ( path );
                        Cache.log.warn ( Cache.sm.getString ( "cache.addFail", path ) );
                    }
                }
            } else {
                cacheEntry.validateResources ( useClassLoaderResources );
            }
        } else {
            this.hitCount.incrementAndGet();
        }
        return cacheEntry.getWebResources();
    }
    protected void backgroundProcess() {
        final TreeSet<CachedResource> orderedResources = new TreeSet<CachedResource> ( new EvictionOrder() );
        orderedResources.addAll ( this.resourceCache.values() );
        final Iterator<CachedResource> iter = orderedResources.iterator();
        final long targetSize = this.maxSize * 90L / 100L;
        final long newSize = this.evict ( targetSize, iter );
        if ( newSize > targetSize ) {
            Cache.log.info ( Cache.sm.getString ( "cache.backgroundEvictFail", 10L, this.root.getContext().getName(), newSize / 1024L ) );
        }
    }
    private boolean noCache ( final String path ) {
        return ( path.endsWith ( ".class" ) && ( path.startsWith ( "/WEB-INF/classes/" ) || path.startsWith ( "/WEB-INF/lib/" ) ) ) || ( path.startsWith ( "/WEB-INF/lib/" ) && path.endsWith ( ".jar" ) );
    }
    private long evict ( final long targetSize, final Iterator<CachedResource> iter ) {
        final long now = System.currentTimeMillis();
        long newSize = this.size.get();
        while ( newSize > targetSize && iter.hasNext() ) {
            final CachedResource resource = iter.next();
            if ( resource.getNextCheck() > now ) {
                continue;
            }
            this.removeCacheEntry ( resource.getWebappPath() );
            newSize = this.size.get();
        }
        return newSize;
    }
    void removeCacheEntry ( final String path ) {
        final CachedResource cachedResource = this.resourceCache.remove ( path );
        if ( cachedResource != null ) {
            final long delta = cachedResource.getSize();
            this.size.addAndGet ( -delta );
        }
    }
    public long getTtl() {
        return this.ttl;
    }
    public void setTtl ( final long ttl ) {
        this.ttl = ttl;
    }
    public long getMaxSize() {
        return this.maxSize / 1024L;
    }
    public void setMaxSize ( final long maxSize ) {
        this.maxSize = maxSize * 1024L;
    }
    public long getLookupCount() {
        return this.lookupCount.get();
    }
    public long getHitCount() {
        return this.hitCount.get();
    }
    public void setObjectMaxSize ( final int objectMaxSize ) {
        if ( objectMaxSize * 1024L > 2147483647L ) {
            Cache.log.warn ( Cache.sm.getString ( "cache.objectMaxSizeTooBigBytes", objectMaxSize ) );
            this.objectMaxSize = Integer.MAX_VALUE;
        }
        this.objectMaxSize = objectMaxSize * 1024;
    }
    public int getObjectMaxSize() {
        return this.objectMaxSize / 1024;
    }
    public int getObjectMaxSizeBytes() {
        return this.objectMaxSize;
    }
    void enforceObjectMaxSizeLimit() {
        final long limit = this.maxSize / 20L;
        if ( limit > 2147483647L ) {
            return;
        }
        if ( this.objectMaxSize > limit ) {
            Cache.log.warn ( Cache.sm.getString ( "cache.objectMaxSizeTooBig", this.objectMaxSize / 1024, ( int ) limit / 1024 ) );
            this.objectMaxSize = ( int ) limit;
        }
    }
    public void clear() {
        this.resourceCache.clear();
        this.size.set ( 0L );
    }
    public long getSize() {
        return this.size.get() / 1024L;
    }
    static {
        log = LogFactory.getLog ( Cache.class );
        sm = StringManager.getManager ( Cache.class );
    }
    private static class EvictionOrder implements Comparator<CachedResource> {
        @Override
        public int compare ( final CachedResource cr1, final CachedResource cr2 ) {
            final long nc1 = cr1.getNextCheck();
            final long nc2 = cr2.getNextCheck();
            if ( nc1 == nc2 ) {
                return 0;
            }
            if ( nc1 > nc2 ) {
                return -1;
            }
            return 1;
        }
    }
}
