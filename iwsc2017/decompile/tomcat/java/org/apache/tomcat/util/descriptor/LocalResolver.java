package org.apache.tomcat.util.descriptor;
import java.net.URISyntaxException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URI;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import java.util.Map;
import org.apache.tomcat.util.res.StringManager;
import org.xml.sax.ext.EntityResolver2;
public class LocalResolver implements EntityResolver2 {
    private static final StringManager sm;
    private static final String[] JAVA_EE_NAMESPACES;
    private final Map<String, String> publicIds;
    private final Map<String, String> systemIds;
    private final boolean blockExternal;
    public LocalResolver ( final Map<String, String> publicIds, final Map<String, String> systemIds, final boolean blockExternal ) {
        this.publicIds = publicIds;
        this.systemIds = systemIds;
        this.blockExternal = blockExternal;
    }
    @Override
    public InputSource resolveEntity ( final String publicId, final String systemId ) throws SAXException, IOException {
        return this.resolveEntity ( null, publicId, null, systemId );
    }
    @Override
    public InputSource resolveEntity ( final String name, final String publicId, final String base, final String systemId ) throws SAXException, IOException {
        String resolved = this.publicIds.get ( publicId );
        if ( resolved != null ) {
            final InputSource is = new InputSource ( resolved );
            is.setPublicId ( publicId );
            return is;
        }
        if ( systemId == null ) {
            throw new FileNotFoundException ( LocalResolver.sm.getString ( "localResolver.unresolvedEntity", name, publicId, systemId, base ) );
        }
        resolved = this.systemIds.get ( systemId );
        if ( resolved != null ) {
            final InputSource is = new InputSource ( resolved );
            is.setPublicId ( publicId );
            return is;
        }
        for ( final String javaEENamespace : LocalResolver.JAVA_EE_NAMESPACES ) {
            final String javaEESystemId = javaEENamespace + '/' + systemId;
            resolved = this.systemIds.get ( javaEESystemId );
            if ( resolved != null ) {
                final InputSource is2 = new InputSource ( resolved );
                is2.setPublicId ( publicId );
                return is2;
            }
        }
        URI systemUri;
        try {
            if ( base == null ) {
                systemUri = new URI ( systemId );
            } else {
                final URI baseUri = new URI ( base );
                systemUri = new URL ( baseUri.toURL(), systemId ).toURI();
            }
            systemUri = systemUri.normalize();
        } catch ( URISyntaxException e ) {
            if ( this.blockExternal ) {
                throw new MalformedURLException ( e.getMessage() );
            }
            return new InputSource ( systemId );
        }
        if ( systemUri.isAbsolute() ) {
            resolved = this.systemIds.get ( systemUri.toString() );
            if ( resolved != null ) {
                final InputSource is3 = new InputSource ( resolved );
                is3.setPublicId ( publicId );
                return is3;
            }
            if ( !this.blockExternal ) {
                final InputSource is3 = new InputSource ( systemUri.toString() );
                is3.setPublicId ( publicId );
                return is3;
            }
        }
        throw new FileNotFoundException ( LocalResolver.sm.getString ( "localResolver.unresolvedEntity", name, publicId, systemId, base ) );
    }
    @Override
    public InputSource getExternalSubset ( final String name, final String baseURI ) throws SAXException, IOException {
        return null;
    }
    static {
        sm = StringManager.getManager ( Constants.PACKAGE_NAME );
        JAVA_EE_NAMESPACES = new String[] { "http://java.sun.com/xml/ns/j2ee", "http://java.sun.com/xml/ns/javaee", "http://xmlns.jcp.org/xml/ns/javaee" };
    }
}
