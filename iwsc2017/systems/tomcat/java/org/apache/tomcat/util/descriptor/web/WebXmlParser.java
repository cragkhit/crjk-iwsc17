package org.apache.tomcat.util.descriptor.web;
import java.io.IOException;
import java.net.URL;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.descriptor.DigesterFactory;
import org.apache.tomcat.util.descriptor.InputSourceUtil;
import org.apache.tomcat.util.descriptor.XmlErrorHandler;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.res.StringManager;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
public class WebXmlParser {
    private static final Log log = LogFactory.getLog ( WebXmlParser.class );
    private static final StringManager sm =
        StringManager.getManager ( Constants.PACKAGE_NAME );
    private final Digester webDigester;
    private final WebRuleSet webRuleSet;
    private final Digester webFragmentDigester;
    private final WebRuleSet webFragmentRuleSet;
    public WebXmlParser ( boolean namespaceAware, boolean validation,
                          boolean blockExternal ) {
        webRuleSet = new WebRuleSet ( false );
        webDigester = DigesterFactory.newDigester ( validation,
                      namespaceAware, webRuleSet, blockExternal );
        webDigester.getParser();
        webFragmentRuleSet = new WebRuleSet ( true );
        webFragmentDigester = DigesterFactory.newDigester ( validation,
                              namespaceAware, webFragmentRuleSet, blockExternal );
        webFragmentDigester.getParser();
    }
    public boolean parseWebXml ( URL url, WebXml dest, boolean fragment ) throws IOException {
        if ( url == null ) {
            return true;
        }
        InputSource source = new InputSource ( url.toExternalForm() );
        source.setByteStream ( url.openStream() );
        return parseWebXml ( source, dest, fragment );
    }
    public boolean parseWebXml ( InputSource source, WebXml dest,
                                 boolean fragment ) {
        boolean ok = true;
        if ( source == null ) {
            return ok;
        }
        XmlErrorHandler handler = new XmlErrorHandler();
        Digester digester;
        WebRuleSet ruleSet;
        if ( fragment ) {
            digester = webFragmentDigester;
            ruleSet = webFragmentRuleSet;
        } else {
            digester = webDigester;
            ruleSet = webRuleSet;
        }
        digester.push ( dest );
        digester.setErrorHandler ( handler );
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "webXmlParser.applicationStart",
                                       source.getSystemId() ) );
        }
        try {
            digester.parse ( source );
            if ( handler.getWarnings().size() > 0 ||
                    handler.getErrors().size() > 0 ) {
                ok = false;
                handler.logFindings ( log, source.getSystemId() );
            }
        } catch ( SAXParseException e ) {
            log.error ( sm.getString ( "webXmlParser.applicationParse",
                                       source.getSystemId() ), e );
            log.error ( sm.getString ( "webXmlParser.applicationPosition",
                                       "" + e.getLineNumber(),
                                       "" + e.getColumnNumber() ) );
            ok = false;
        } catch ( Exception e ) {
            log.error ( sm.getString ( "webXmlParser.applicationParse",
                                       source.getSystemId() ), e );
            ok = false;
        } finally {
            InputSourceUtil.close ( source );
            digester.reset();
            ruleSet.recycle();
        }
        return ok;
    }
    public void setClassLoader ( ClassLoader classLoader ) {
        webDigester.setClassLoader ( classLoader );
        webFragmentDigester.setClassLoader ( classLoader );
    }
}
