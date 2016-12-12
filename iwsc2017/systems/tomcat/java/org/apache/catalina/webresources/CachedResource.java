package org.apache.catalina.webresources;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.jar.Manifest;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
public class CachedResource implements WebResource {
    private static final long CACHE_ENTRY_SIZE = 500;
    private final Cache cache;
    private final StandardRoot root;
    private final String webAppPath;
    private final long ttl;
    private final int objectMaxSizeBytes;
    private volatile WebResource webResource;
    private volatile WebResource[] webResources;
    private volatile long nextCheck;
    private volatile Long cachedLastModified = null;
    private volatile String cachedLastModifiedHttp = null;
    private volatile byte[] cachedContent = null;
    private volatile Boolean cachedIsFile = null;
    private volatile Boolean cachedIsDirectory = null;
    private volatile Boolean cachedExists = null;
    private volatile Boolean cachedIsVirtual = null;
    private volatile Long cachedContentLength = null;
    public CachedResource ( Cache cache, StandardRoot root, String path, long ttl,
                            int objectMaxSizeBytes ) {
        this.cache = cache;
        this.root = root;
        this.webAppPath = path;
        this.ttl = ttl;
        this.objectMaxSizeBytes = objectMaxSizeBytes;
    }
    protected boolean validateResource ( boolean useClassLoaderResources ) {
        long now = System.currentTimeMillis();
        if ( webResource == null ) {
            synchronized ( this ) {
                if ( webResource == null ) {
                    webResource = root.getResourceInternal (
                                      webAppPath, useClassLoaderResources );
                    getLastModified();
                    getContentLength();
                    nextCheck = ttl + now;
                    if ( webResource instanceof EmptyResource ) {
                        cachedExists = Boolean.FALSE;
                    } else {
                        cachedExists = Boolean.TRUE;
                    }
                    return true;
                }
            }
        }
        if ( now < nextCheck ) {
            return true;
        }
        WebResource webResourceInternal = root.getResourceInternal (
                                              webAppPath, useClassLoaderResources );
        if ( !webResource.exists() && webResourceInternal.exists() ) {
            return false;
        }
        if ( webResource.getLastModified() != getLastModified() ||
                webResource.getContentLength() != getContentLength() ) {
            return false;
        }
        if ( webResource.getLastModified() != webResourceInternal.getLastModified() ||
                webResource.getContentLength() != webResourceInternal.getContentLength() ) {
            return false;
        }
        nextCheck = ttl + now;
        return true;
    }
    protected boolean validateResources ( boolean useClassLoaderResources ) {
        long now = System.currentTimeMillis();
        if ( webResources == null ) {
            synchronized ( this ) {
                if ( webResources == null ) {
                    webResources = root.getResourcesInternal (
                                       webAppPath, useClassLoaderResources );
                    nextCheck = ttl + now;
                    return true;
                }
            }
        }
        if ( now < nextCheck ) {
            return true;
        }
        return false;
    }
    protected long getNextCheck() {
        return nextCheck;
    }
    @Override
    public long getLastModified() {
        Long cachedLastModified = this.cachedLastModified;
        if ( cachedLastModified == null ) {
            cachedLastModified =
                Long.valueOf ( webResource.getLastModified() );
            this.cachedLastModified = cachedLastModified;
        }
        return cachedLastModified.longValue();
    }
    @Override
    public String getLastModifiedHttp() {
        String cachedLastModifiedHttp = this.cachedLastModifiedHttp;
        if ( cachedLastModifiedHttp == null ) {
            cachedLastModifiedHttp = webResource.getLastModifiedHttp();
            this.cachedLastModifiedHttp = cachedLastModifiedHttp;
        }
        return cachedLastModifiedHttp;
    }
    @Override
    public boolean exists() {
        Boolean cachedExists = this.cachedExists;
        if ( cachedExists == null ) {
            cachedExists = Boolean.valueOf ( webResource.exists() );
            this.cachedExists = cachedExists;
        }
        return cachedExists.booleanValue();
    }
    @Override
    public boolean isVirtual() {
        Boolean cachedIsVirtual = this.cachedIsVirtual;
        if ( cachedIsVirtual == null ) {
            cachedIsVirtual = Boolean.valueOf ( webResource.isVirtual() );
            this.cachedIsVirtual = cachedIsVirtual;
        }
        return cachedIsVirtual.booleanValue();
    }
    @Override
    public boolean isDirectory() {
        Boolean cachedIsDirectory = this.cachedIsDirectory;
        if ( cachedIsDirectory == null ) {
            cachedIsDirectory = Boolean.valueOf ( webResource.isDirectory() );
            this.cachedIsDirectory = cachedIsDirectory;
        }
        return cachedIsDirectory.booleanValue();
    }
    @Override
    public boolean isFile() {
        Boolean cachedIsFile = this.cachedIsFile;
        if ( cachedIsFile == null ) {
            cachedIsFile = Boolean.valueOf ( webResource.isFile() );
            this.cachedIsFile = cachedIsFile;
        }
        return cachedIsFile.booleanValue();
    }
    @Override
    public boolean delete() {
        boolean deleteResult = webResource.delete();
        if ( deleteResult ) {
            cache.removeCacheEntry ( webAppPath );
        }
        return deleteResult;
    }
    @Override
    public String getName() {
        return webResource.getName();
    }
    @Override
    public long getContentLength() {
        Long cachedContentLength = this.cachedContentLength;
        if ( cachedContentLength == null ) {
            long result = 0;
            if ( webResource != null ) {
                result = webResource.getContentLength();
                cachedContentLength = Long.valueOf ( result );
                this.cachedContentLength = cachedContentLength;
            }
            return result;
        }
        return cachedContentLength.longValue();
    }
    @Override
    public String getCanonicalPath() {
        return webResource.getCanonicalPath();
    }
    @Override
    public boolean canRead() {
        return webResource.canRead();
    }
    @Override
    public String getWebappPath() {
        return webAppPath;
    }
    @Override
    public String getETag() {
        return webResource.getETag();
    }
    @Override
    public void setMimeType ( String mimeType ) {
        webResource.setMimeType ( mimeType );
    }
    @Override
    public String getMimeType() {
        return webResource.getMimeType();
    }
    @Override
    public InputStream getInputStream() {
        byte[] content = getContent();
        if ( content == null ) {
            return webResource.getInputStream();
        }
        return new ByteArrayInputStream ( content );
    }
    @Override
    public byte[] getContent() {
        byte[] cachedContent = this.cachedContent;
        if ( cachedContent == null ) {
            if ( getContentLength() > objectMaxSizeBytes ) {
                return null;
            }
            cachedContent = webResource.getContent();
            this.cachedContent = cachedContent;
        }
        return cachedContent;
    }
    @Override
    public long getCreation() {
        return webResource.getCreation();
    }
    @Override
    public URL getURL() {
        return webResource.getURL();
    }
    @Override
    public URL getCodeBase() {
        return webResource.getCodeBase();
    }
    @Override
    public Certificate[] getCertificates() {
        return webResource.getCertificates();
    }
    @Override
    public Manifest getManifest() {
        return webResource.getManifest();
    }
    @Override
    public WebResourceRoot getWebResourceRoot() {
        return webResource.getWebResourceRoot();
    }
    WebResource getWebResource() {
        return webResource;
    }
    WebResource[] getWebResources() {
        return webResources;
    }
    long getSize() {
        long result = CACHE_ENTRY_SIZE;
        if ( getContentLength() <= objectMaxSizeBytes ) {
            result += getContentLength();
        }
        return result;
    }
}
