package org.apache.catalina.webresources;
import org.apache.catalina.WebResource;
import java.util.jar.Manifest;
import java.util.jar.JarEntry;
import org.apache.catalina.WebResourceRoot;
public class WarResourceSet extends AbstractSingleArchiveResourceSet {
    public WarResourceSet() {
    }
    public WarResourceSet ( final WebResourceRoot root, final String webAppMount, final String base ) throws IllegalArgumentException {
        super ( root, webAppMount, base, "/" );
    }
    @Override
    protected WebResource createArchiveResource ( final JarEntry jarEntry, final String webAppPath, final Manifest manifest ) {
        return new WarResource ( this, webAppPath, this.getBaseUrlString(), jarEntry );
    }
}
