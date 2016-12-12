package org.apache.tomcat.util.descriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import org.apache.tomcat.util.res.StringManager;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;
public class LocalResolver implements EntityResolver2 {
    private static final StringManager sm =
        StringManager.getManager ( Constants.PACKAGE_NAME );
    private static final String[] JAVA_EE_NAMESPACES = {
        XmlIdentifiers.JAVAEE_1_4_NS,
        XmlIdentifiers.JAVAEE_5_NS,
        XmlIdentifiers.JAVAEE_7_NS
    };
    private final Map<String, String> publicIds;
    private final Map<String, String> systemIds;
    private final boolean blockExternal;
    public LocalResolver ( Map<String, String> publicIds,
                           Map<String, String> systemIds, boolean blockExternal ) {
        this.publicIds = publicIds;
        this.systemIds = systemIds;
        this.blockExternal = blockExternal;
    }
    @Override
    public InputSource resolveEntity ( String publicId, String systemId )
    throws SAXException, IOException {
        return resolveEntity ( null, publicId, null, systemId );
    }
    @Override
    public InputSource resolveEntity ( String name, String publicId,
                                       String base, String systemId ) throws SAXException, IOException {
        String resolved = publicIds.get ( publicId );
        if ( resolved != null ) {
            InputSource is = new InputSource ( resolved );
            is.setPublicId ( publicId );
            return is;
        }
        if ( systemId == null ) {
            throw new FileNotFoundException ( sm.getString ( "localResolver.unresolvedEntity",
                                              name, publicId, systemId, base ) );
        }
        resolved = systemIds.get ( systemId );
        if ( resolved != null ) {
            InputSource is = new InputSource ( resolved );
            is.setPublicId ( publicId );
            return is;
        }
        for ( String javaEENamespace : JAVA_EE_NAMESPACES ) {
            String javaEESystemId = javaEENamespace + '/' + systemId;
            resolved = systemIds.get ( javaEESystemId );
            if ( resolved != null ) {
                InputSource is = new InputSource ( resolved );
                is.setPublicId ( publicId );
                return is;
            }
        }
        URI systemUri;
        try {
            if ( base == null ) {
                systemUri = new URI ( systemId );
            } else {
                URI baseUri = new URI ( base );
                systemUri = new URL ( baseUri.toURL(), systemId ).toURI();
            }
            systemUri = systemUri.normalize();
        } catch ( URISyntaxException e ) {
            if ( blockExternal ) {
                throw new MalformedURLException ( e.getMessage() );
            } else {
                return new InputSource ( systemId );
            }
        }
        if ( systemUri.isAbsolute() ) {
            resolved = systemIds.get ( systemUri.toString() );
            if ( resolved != null ) {
                InputSource is = new InputSource ( resolved );
                is.setPublicId ( publicId );
                return is;
            }
            if ( !blockExternal ) {
                InputSource is = new InputSource ( systemUri.toString() );
                is.setPublicId ( publicId );
                return is;
            }
        }
        throw new FileNotFoundException ( sm.getString ( "localResolver.unresolvedEntity",
                                          name, publicId, systemId, base ) );
    }
    @Override
    public InputSource getExternalSubset ( String name, String baseURI )
    throws SAXException, IOException {
        return null;
    }
}
