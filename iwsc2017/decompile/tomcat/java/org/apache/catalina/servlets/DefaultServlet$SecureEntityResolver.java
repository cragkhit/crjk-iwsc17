package org.apache.catalina.servlets;
import java.io.IOException;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.ext.EntityResolver2;
private static class SecureEntityResolver implements EntityResolver2 {
    @Override
    public InputSource resolveEntity ( final String publicId, final String systemId ) throws SAXException, IOException {
        throw new SAXException ( DefaultServlet.sm.getString ( "defaultServlet.blockExternalEntity", publicId, systemId ) );
    }
    @Override
    public InputSource getExternalSubset ( final String name, final String baseURI ) throws SAXException, IOException {
        throw new SAXException ( DefaultServlet.sm.getString ( "defaultServlet.blockExternalSubset", name, baseURI ) );
    }
    @Override
    public InputSource resolveEntity ( final String name, final String publicId, final String baseURI, final String systemId ) throws SAXException, IOException {
        throw new SAXException ( DefaultServlet.sm.getString ( "defaultServlet.blockExternalEntity2", name, publicId, baseURI, systemId ) );
    }
}
