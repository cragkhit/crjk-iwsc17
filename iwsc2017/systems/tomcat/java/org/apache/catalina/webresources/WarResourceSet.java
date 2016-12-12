package org.apache.catalina.webresources;
import java.util.jar.JarEntry;
import java.util.jar.Manifest;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
public class WarResourceSet extends AbstractSingleArchiveResourceSet {
    public WarResourceSet() {
    }
    public WarResourceSet ( WebResourceRoot root, String webAppMount, String base )
    throws IllegalArgumentException {
        super ( root, webAppMount, base, "/" );
    }
    @Override
    protected WebResource createArchiveResource ( JarEntry jarEntry,
            String webAppPath, Manifest manifest ) {
        return new WarResource ( this, webAppPath, getBaseUrlString(), jarEntry );
    }
}
