package org.apache.tomcat.util.descriptor.tagplugin;
import org.apache.tomcat.util.digester.RuleSetBase;
import org.apache.juli.logging.LogFactory;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.InputStream;
import org.xml.sax.SAXParseException;
import org.xml.sax.InputSource;
import org.xml.sax.ErrorHandler;
import org.apache.tomcat.util.descriptor.XmlErrorHandler;
import java.net.URL;
import org.apache.tomcat.util.digester.RuleSet;
import org.apache.tomcat.util.descriptor.DigesterFactory;
import java.util.HashMap;
import javax.servlet.ServletContext;
import java.util.Map;
import org.apache.tomcat.util.digester.Digester;
import org.apache.juli.logging.Log;
public class TagPluginParser {
    private static final Log log;
    private static final String PREFIX = "tag-plugins/tag-plugin";
    private final Digester digester;
    private final Map<String, String> plugins;
    public TagPluginParser ( final ServletContext context, final boolean blockExternal ) {
        this.plugins = new HashMap<String, String>();
        ( this.digester = DigesterFactory.newDigester ( false, false, new TagPluginRuleSet(), blockExternal ) ).setClassLoader ( context.getClassLoader() );
    }
    public void parse ( final URL url ) throws IOException, SAXException {
        try ( final InputStream is = url.openStream() ) {
            final XmlErrorHandler handler = new XmlErrorHandler();
            this.digester.setErrorHandler ( handler );
            this.digester.push ( this );
            final InputSource source = new InputSource ( url.toExternalForm() );
            source.setByteStream ( is );
            this.digester.parse ( source );
            if ( !handler.getWarnings().isEmpty() || !handler.getErrors().isEmpty() ) {
                handler.logFindings ( TagPluginParser.log, source.getSystemId() );
                if ( !handler.getErrors().isEmpty() ) {
                    throw handler.getErrors().iterator().next();
                }
            }
        } finally {
            this.digester.reset();
        }
    }
    public void addPlugin ( final String tagClass, final String pluginClass ) {
        this.plugins.put ( tagClass, pluginClass );
    }
    public Map<String, String> getPlugins() {
        return this.plugins;
    }
    static {
        log = LogFactory.getLog ( TagPluginParser.class );
    }
    private static class TagPluginRuleSet extends RuleSetBase {
        @Override
        public void addRuleInstances ( final Digester digester ) {
            digester.addCallMethod ( "tag-plugins/tag-plugin", "addPlugin", 2 );
            digester.addCallParam ( "tag-plugins/tag-plugin/tag-class", 0 );
            digester.addCallParam ( "tag-plugins/tag-plugin/plugin-class", 1 );
        }
    }
}
