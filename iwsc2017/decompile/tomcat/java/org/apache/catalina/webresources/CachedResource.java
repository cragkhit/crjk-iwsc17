package org.apache.catalina.webresources;
import org.apache.catalina.WebResourceRoot;
import java.util.jar.Manifest;
import java.security.cert.Certificate;
import java.net.URL;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.apache.catalina.WebResource;
public class CachedResource implements WebResource {
    private static final long CACHE_ENTRY_SIZE = 500L;
    private final Cache cache;
    private final StandardRoot root;
    private final String webAppPath;
    private final long ttl;
    private final int objectMaxSizeBytes;
    private volatile WebResource webResource;
    private volatile WebResource[] webResources;
    private volatile long nextCheck;
    private volatile Long cachedLastModified;
    private volatile String cachedLastModifiedHttp;
    private volatile byte[] cachedContent;
    private volatile Boolean cachedIsFile;
    private volatile Boolean cachedIsDirectory;
    private volatile Boolean cachedExists;
    private volatile Boolean cachedIsVirtual;
    private volatile Long cachedContentLength;
    public CachedResource ( final Cache cache, final StandardRoot root, final String path, final long ttl, final int objectMaxSizeBytes ) {
        this.cachedLastModified = null;
        this.cachedLastModifiedHttp = null;
        this.cachedContent = null;
        this.cachedIsFile = null;
        this.cachedIsDirectory = null;
        this.cachedExists = null;
        this.cachedIsVirtual = null;
        this.cachedContentLength = null;
        this.cache = cache;
        this.root = root;
        this.webAppPath = path;
        this.ttl = ttl;
        this.objectMaxSizeBytes = objectMaxSizeBytes;
    }
    protected boolean validateResource ( final boolean useClassLoaderResources ) {
        final long now = System.currentTimeMillis();
        if ( this.webResource == null ) {
            synchronized ( this ) {
                if ( this.webResource == null ) {
                    this.webResource = this.root.getResourceInternal ( this.webAppPath, useClassLoaderResources );
                    this.getLastModified();
                    this.getContentLength();
                    this.nextCheck = this.ttl + now;
                    if ( this.webResource instanceof EmptyResource ) {
                        this.cachedExists = Boolean.FALSE;
                    } else {
                        this.cachedExists = Boolean.TRUE;
                    }
                    return true;
                }
            }
        }
        if ( now < this.nextCheck ) {
            return true;
        }
        final WebResource webResourceInternal = this.root.getResourceInternal ( this.webAppPath, useClassLoaderResources );
        if ( !this.webResource.exists() && webResourceInternal.exists() ) {
            return false;
        }
        if ( this.webResource.getLastModified() != this.getLastModified() || this.webResource.getContentLength() != this.getContentLength() ) {
            return false;
        }
        if ( this.webResource.getLastModified() != webResourceInternal.getLastModified() || this.webResource.getContentLength() != webResourceInternal.getContentLength() ) {
            return false;
        }
        this.nextCheck = this.ttl + now;
        return true;
    }
    protected boolean validateResources ( final boolean useClassLoaderResources ) {
        final long now = System.currentTimeMillis();
        if ( this.webResources == null ) {
            synchronized ( this ) {
                if ( this.webResources == null ) {
                    this.webResources = this.root.getResourcesInternal ( this.webAppPath, useClassLoaderResources );
                    this.nextCheck = this.ttl + now;
                    return true;
                }
            }
        }
        return now < this.nextCheck;
    }
    protected long getNextCheck() {
        return this.nextCheck;
    }
    @Override
    public long getLastModified() {
        Long cachedLastModified = this.cachedLastModified;
        if ( cachedLastModified == null ) {
            cachedLastModified = this.webResource.getLastModified();
            this.cachedLastModified = cachedLastModified;
        }
        return cachedLastModified;
    }
    @Override
    public String getLastModifiedHttp() {
        String cachedLastModifiedHttp = this.cachedLastModifiedHttp;
        if ( cachedLastModifiedHttp == null ) {
            cachedLastModifiedHttp = this.webResource.getLastModifiedHttp();
            this.cachedLastModifiedHttp = cachedLastModifiedHttp;
        }
        return cachedLastModifiedHttp;
    }
    @Override
    public boolean exists() {
        Boolean cachedExists = this.cachedExists;
        if ( cachedExists == null ) {
            cachedExists = this.webResource.exists();
            this.cachedExists = cachedExists;
        }
        return cachedExists;
    }
    @Override
    public boolean isVirtual() {
        Boolean cachedIsVirtual = this.cachedIsVirtual;
        if ( cachedIsVirtual == null ) {
            cachedIsVirtual = this.webResource.isVirtual();
            this.cachedIsVirtual = cachedIsVirtual;
        }
        return cachedIsVirtual;
    }
    @Override
    public boolean isDirectory() {
        Boolean cachedIsDirectory = this.cachedIsDirectory;
        if ( cachedIsDirectory == null ) {
            cachedIsDirectory = this.webResource.isDirectory();
            this.cachedIsDirectory = cachedIsDirectory;
        }
        return cachedIsDirectory;
    }
    @Override
    public boolean isFile() {
        Boolean cachedIsFile = this.cachedIsFile;
        if ( cachedIsFile == null ) {
            cachedIsFile = this.webResource.isFile();
            this.cachedIsFile = cachedIsFile;
        }
        return cachedIsFile;
    }
    @Override
    public boolean delete() {
        final boolean deleteResult = this.webResource.delete();
        if ( deleteResult ) {
            this.cache.removeCacheEntry ( this.webAppPath );
        }
        return deleteResult;
    }
    @Override
    public String getName() {
        return this.webResource.getName();
    }
    @Override
    public long getContentLength() {
        Long cachedContentLength = this.cachedContentLength;
        if ( cachedContentLength == null ) {
            long result = 0L;
            if ( this.webResource != null ) {
                result = this.webResource.getContentLength();
                cachedContentLength = result;
                this.cachedContentLength = cachedContentLength;
            }
            return result;
        }
        return cachedContentLength;
    }
    @Override
    public String getCanonicalPath() {
        return this.webResource.getCanonicalPath();
    }
    @Override
    public boolean canRead() {
        return this.webResource.canRead();
    }
    @Override
    public String getWebappPath() {
        return this.webAppPath;
    }
    @Override
    public String getETag() {
        return this.webResource.getETag();
    }
    @Override
    public void setMimeType ( final String mimeType ) {
        this.webResource.setMimeType ( mimeType );
    }
    @Override
    public String getMimeType() {
        return this.webResource.getMimeType();
    }
    @Override
    public InputStream getInputStream() {
        final byte[] content = this.getContent();
        if ( content == null ) {
            return this.webResource.getInputStream();
        }
        return new ByteArrayInputStream ( content );
    }
    @Override
    public byte[] getContent() {
        byte[] cachedContent = this.cachedContent;
        if ( cachedContent == null ) {
            if ( this.getContentLength() > this.objectMaxSizeBytes ) {
                return null;
            }
            cachedContent = this.webResource.getContent();
            this.cachedContent = cachedContent;
        }
        return cachedContent;
    }
    @Override
    public long getCreation() {
        return this.webResource.getCreation();
    }
    @Override
    public URL getURL() {
        return this.webResource.getURL();
    }
    @Override
    public URL getCodeBase() {
        return this.webResource.getCodeBase();
    }
    @Override
    public Certificate[] getCertificates() {
        return this.webResource.getCertificates();
    }
    @Override
    public Manifest getManifest() {
        return this.webResource.getManifest();
    }
    @Override
    public WebResourceRoot getWebResourceRoot() {
        return this.webResource.getWebResourceRoot();
    }
    WebResource getWebResource() {
        return this.webResource;
    }
    WebResource[] getWebResources() {
        return this.webResources;
    }
    long getSize() {
        long result = 500L;
        if ( this.getContentLength() <= this.objectMaxSizeBytes ) {
            result += this.getContentLength();
        }
        return result;
    }
}
