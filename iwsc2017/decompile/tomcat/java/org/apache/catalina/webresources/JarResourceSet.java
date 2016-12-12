package org.apache.catalina.webresources;
import org.apache.catalina.WebResource;
import java.util.jar.Manifest;
import java.util.jar.JarEntry;
import org.apache.catalina.WebResourceRoot;
public class JarResourceSet extends AbstractSingleArchiveResourceSet {
    public JarResourceSet() {
    }
    public JarResourceSet ( final WebResourceRoot root, final String webAppMount, final String base, final String internalPath ) throws IllegalArgumentException {
        super ( root, webAppMount, base, internalPath );
    }
    @Override
    protected WebResource createArchiveResource ( final JarEntry jarEntry, final String webAppPath, final Manifest manifest ) {
        return new JarResource ( this, webAppPath, this.getBaseUrlString(), jarEntry );
    }
}
