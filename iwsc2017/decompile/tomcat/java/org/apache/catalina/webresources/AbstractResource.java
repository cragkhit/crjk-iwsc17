package org.apache.catalina.webresources;
import org.apache.juli.logging.Log;
import java.io.InputStream;
import org.apache.catalina.util.ConcurrentDateFormat;
import java.util.Date;
import org.apache.catalina.WebResourceRoot;
import org.apache.tomcat.util.res.StringManager;
import org.apache.catalina.WebResource;
public abstract class AbstractResource implements WebResource {
    protected static final StringManager sm;
    private final WebResourceRoot root;
    private final String webAppPath;
    private String mimeType;
    private volatile String weakETag;
    protected AbstractResource ( final WebResourceRoot root, final String webAppPath ) {
        this.mimeType = null;
        this.root = root;
        this.webAppPath = webAppPath;
    }
    @Override
    public final WebResourceRoot getWebResourceRoot() {
        return this.root;
    }
    @Override
    public final String getWebappPath() {
        return this.webAppPath;
    }
    @Override
    public final String getLastModifiedHttp() {
        return ConcurrentDateFormat.formatRfc1123 ( new Date ( this.getLastModified() ) );
    }
    @Override
    public final String getETag() {
        if ( this.weakETag == null ) {
            synchronized ( this ) {
                if ( this.weakETag == null ) {
                    final long contentLength = this.getContentLength();
                    final long lastModified = this.getLastModified();
                    if ( contentLength >= 0L || lastModified >= 0L ) {
                        this.weakETag = "W/\"" + contentLength + "-" + lastModified + "\"";
                    }
                }
            }
        }
        return this.weakETag;
    }
    @Override
    public final void setMimeType ( final String mimeType ) {
        this.mimeType = mimeType;
    }
    @Override
    public final String getMimeType() {
        return this.mimeType;
    }
    @Override
    public final InputStream getInputStream() {
        final InputStream is = this.doGetInputStream();
        if ( is == null || !this.root.getTrackLockedFiles() ) {
            return is;
        }
        return new TrackedInputStream ( this.root, this.getName(), is );
    }
    protected abstract InputStream doGetInputStream();
    protected abstract Log getLog();
    static {
        sm = StringManager.getManager ( AbstractResource.class );
    }
}
