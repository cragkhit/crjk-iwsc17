package org.apache.catalina.webresources;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
public class JarResourceSet extends AbstractSingleArchiveResourceSet {
    public JarResourceSet() {
    }
    public JarResourceSet ( WebResourceRoot root, String webAppMount, String base,
                            String internalPath ) throws IllegalArgumentException {
        super ( root, webAppMount, base, internalPath );
    }
    @Override
    protected WebResource createArchiveResource ( JarEntry jarEntry,
            String webAppPath, Manifest manifest ) {
        return new JarResource ( this, webAppPath, getBaseUrlString(), jarEntry );
    }
}
