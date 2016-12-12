package org.apache.catalina.servlets;
import java.io.Reader;
import java.io.StringReader;
import org.xml.sax.InputSource;
import javax.servlet.ServletContext;
import org.xml.sax.EntityResolver;
private static class WebdavResolver implements EntityResolver {
    private ServletContext context;
    public WebdavResolver ( final ServletContext theContext ) {
        this.context = theContext;
    }
    @Override
    public InputSource resolveEntity ( final String publicId, final String systemId ) {
        this.context.log ( DefaultServlet.sm.getString ( "webdavservlet.enternalEntityIgnored", publicId, systemId ) );
        return new InputSource ( new StringReader ( "Ignored external entity" ) );
    }
}
