package org.apache.tomcat.util.descriptor.web;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.descriptor.InputSourceUtil;
import org.xml.sax.SAXParseException;
import org.xml.sax.ErrorHandler;
import org.apache.tomcat.util.descriptor.XmlErrorHandler;
import java.io.IOException;
import org.xml.sax.InputSource;
import java.net.URL;
import org.apache.tomcat.util.digester.RuleSet;
import org.apache.tomcat.util.descriptor.DigesterFactory;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
public class WebXmlParser {
    private static final Log log;
    private static final StringManager sm;
    private final Digester webDigester;
    private final WebRuleSet webRuleSet;
    private final Digester webFragmentDigester;
    private final WebRuleSet webFragmentRuleSet;
    public WebXmlParser ( final boolean namespaceAware, final boolean validation, final boolean blockExternal ) {
        this.webRuleSet = new WebRuleSet ( false );
        ( this.webDigester = DigesterFactory.newDigester ( validation, namespaceAware, this.webRuleSet, blockExternal ) ).getParser();
        this.webFragmentRuleSet = new WebRuleSet ( true );
        ( this.webFragmentDigester = DigesterFactory.newDigester ( validation, namespaceAware, this.webFragmentRuleSet, blockExternal ) ).getParser();
    }
    public boolean parseWebXml ( final URL url, final WebXml dest, final boolean fragment ) throws IOException {
        if ( url == null ) {
            return true;
        }
        final InputSource source = new InputSource ( url.toExternalForm() );
        source.setByteStream ( url.openStream() );
        return this.parseWebXml ( source, dest, fragment );
    }
    public boolean parseWebXml ( final InputSource source, final WebXml dest, final boolean fragment ) {
        boolean ok = true;
        if ( source == null ) {
            return ok;
        }
        final XmlErrorHandler handler = new XmlErrorHandler();
        Digester digester;
        WebRuleSet ruleSet;
        if ( fragment ) {
            digester = this.webFragmentDigester;
            ruleSet = this.webFragmentRuleSet;
        } else {
            digester = this.webDigester;
            ruleSet = this.webRuleSet;
        }
        digester.push ( dest );
        digester.setErrorHandler ( handler );
        while ( true ) {
            if ( WebXmlParser.log.isDebugEnabled() ) {
                WebXmlParser.log.debug ( WebXmlParser.sm.getString ( "webXmlParser.applicationStart", source.getSystemId() ) );
                try {
                    digester.parse ( source );
                    if ( handler.getWarnings().size() > 0 || handler.getErrors().size() > 0 ) {
                        ok = false;
                        handler.logFindings ( WebXmlParser.log, source.getSystemId() );
                    }
                } catch ( SAXParseException e ) {
                    WebXmlParser.log.error ( WebXmlParser.sm.getString ( "webXmlParser.applicationParse", source.getSystemId() ), e );
                    WebXmlParser.log.error ( WebXmlParser.sm.getString ( "webXmlParser.applicationPosition", "" + e.getLineNumber(), "" + e.getColumnNumber() ) );
                    ok = false;
                } catch ( Exception e2 ) {
                    WebXmlParser.log.error ( WebXmlParser.sm.getString ( "webXmlParser.applicationParse", source.getSystemId() ), e2 );
                    ok = false;
                } finally {
                    InputSourceUtil.close ( source );
                    digester.reset();
                    ruleSet.recycle();
                }
                return ok;
            }
            continue;
        }
    }
    public void setClassLoader ( final ClassLoader classLoader ) {
        this.webDigester.setClassLoader ( classLoader );
        this.webFragmentDigester.setClassLoader ( classLoader );
    }
    static {
        log = LogFactory.getLog ( WebXmlParser.class );
        sm = StringManager.getManager ( Constants.PACKAGE_NAME );
    }
}
