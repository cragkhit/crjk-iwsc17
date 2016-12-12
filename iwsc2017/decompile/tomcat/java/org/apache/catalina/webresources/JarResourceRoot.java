package org.apache.catalina.webresources;
import org.apache.juli.logging.LogFactory;
import java.util.jar.Manifest;
import java.security.cert.Certificate;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.InputStream;
import org.apache.catalina.WebResourceRoot;
import java.io.File;
import org.apache.juli.logging.Log;
public class JarResourceRoot extends AbstractResource {
    private static final Log log;
    private final File base;
    private final String baseUrl;
    private final String name;
    public JarResourceRoot ( final WebResourceRoot root, final File base, final String baseUrl, final String webAppPath ) {
        super ( root, webAppPath );
        if ( !webAppPath.endsWith ( "/" ) ) {
            throw new IllegalArgumentException ( JarResourceRoot.sm.getString ( "jarResourceRoot.invalidWebAppPath", webAppPath ) );
        }
        this.base = base;
        this.baseUrl = "jar:" + baseUrl;
        String resourceName = webAppPath.substring ( 0, webAppPath.length() - 1 );
        final int i = resourceName.lastIndexOf ( 47 );
        if ( i > -1 ) {
            resourceName = resourceName.substring ( i + 1 );
        }
        this.name = resourceName;
    }
    @Override
    public long getLastModified() {
        return this.base.lastModified();
    }
    @Override
    public boolean exists() {
        return true;
    }
    @Override
    public boolean isVirtual() {
        return false;
    }
    @Override
    public boolean isDirectory() {
        return true;
    }
    @Override
    public boolean isFile() {
        return false;
    }
    @Override
    public boolean delete() {
        return false;
    }
    @Override
    public String getName() {
        return this.name;
    }
    @Override
    public long getContentLength() {
        return -1L;
    }
    @Override
    public String getCanonicalPath() {
        return null;
    }
    @Override
    public boolean canRead() {
        return true;
    }
    @Override
    protected InputStream doGetInputStream() {
        return null;
    }
    @Override
    public byte[] getContent() {
        return null;
    }
    @Override
    public long getCreation() {
        return this.base.lastModified();
    }
    @Override
    public URL getURL() {
        final String url = this.baseUrl + "!/";
        try {
            return new URL ( url );
        } catch ( MalformedURLException e ) {
            if ( JarResourceRoot.log.isDebugEnabled() ) {
                JarResourceRoot.log.debug ( JarResourceRoot.sm.getString ( "fileResource.getUrlFail", url ), e );
            }
            return null;
        }
    }
    @Override
    public URL getCodeBase() {
        try {
            return new URL ( this.baseUrl );
        } catch ( MalformedURLException e ) {
            if ( this.getLog().isDebugEnabled() ) {
                this.getLog().debug ( JarResourceRoot.sm.getString ( "fileResource.getUrlFail", this.baseUrl ), e );
            }
            return null;
        }
    }
    @Override
    protected Log getLog() {
        return JarResourceRoot.log;
    }
    @Override
    public Certificate[] getCertificates() {
        return null;
    }
    @Override
    public Manifest getManifest() {
        return null;
    }
    static {
        log = LogFactory.getLog ( JarResourceRoot.class );
    }
}
