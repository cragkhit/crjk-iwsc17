package org.apache.tomcat.util.descriptor.tld;
import org.apache.juli.logging.LogFactory;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.InputStream;
import org.xml.sax.SAXParseException;
import org.xml.sax.InputSource;
import org.xml.sax.ErrorHandler;
import org.apache.tomcat.util.descriptor.XmlErrorHandler;
import org.apache.tomcat.util.security.PrivilegedSetTccl;
import java.security.PrivilegedAction;
import java.security.AccessController;
import org.apache.tomcat.util.security.PrivilegedGetTccl;
import org.apache.tomcat.util.descriptor.Constants;
import org.apache.tomcat.util.descriptor.DigesterFactory;
import org.apache.tomcat.util.digester.RuleSet;
import org.apache.tomcat.util.digester.Digester;
import org.apache.juli.logging.Log;
public class TldParser {
    private static final Log log;
    private final Digester digester;
    public TldParser ( final boolean namespaceAware, final boolean validation, final boolean blockExternal ) {
        this ( namespaceAware, validation, new TldRuleSet(), blockExternal );
    }
    public TldParser ( final boolean namespaceAware, final boolean validation, final RuleSet ruleSet, final boolean blockExternal ) {
        this.digester = DigesterFactory.newDigester ( validation, namespaceAware, ruleSet, blockExternal );
    }
    public TaglibXml parse ( final TldResourcePath path ) throws IOException, SAXException {
        ClassLoader original;
        if ( Constants.IS_SECURITY_ENABLED ) {
            final PrivilegedGetTccl pa = new PrivilegedGetTccl();
            original = AccessController.doPrivileged ( ( PrivilegedAction<ClassLoader> ) pa );
        } else {
            original = Thread.currentThread().getContextClassLoader();
        }
        try ( final InputStream is = path.openStream() ) {
            if ( Constants.IS_SECURITY_ENABLED ) {
                final PrivilegedSetTccl pa2 = new PrivilegedSetTccl ( TldParser.class.getClassLoader() );
                AccessController.doPrivileged ( ( PrivilegedAction<Object> ) pa2 );
            } else {
                Thread.currentThread().setContextClassLoader ( TldParser.class.getClassLoader() );
            }
            final XmlErrorHandler handler = new XmlErrorHandler();
            this.digester.setErrorHandler ( handler );
            final TaglibXml taglibXml = new TaglibXml();
            this.digester.push ( taglibXml );
            final InputSource source = new InputSource ( path.toExternalForm() );
            source.setByteStream ( is );
            this.digester.parse ( source );
            if ( !handler.getWarnings().isEmpty() || !handler.getErrors().isEmpty() ) {
                handler.logFindings ( TldParser.log, source.getSystemId() );
                if ( !handler.getErrors().isEmpty() ) {
                    throw handler.getErrors().iterator().next();
                }
            }
            return taglibXml;
        } finally {
            this.digester.reset();
            if ( Constants.IS_SECURITY_ENABLED ) {
                final PrivilegedSetTccl pa3 = new PrivilegedSetTccl ( original );
                AccessController.doPrivileged ( ( PrivilegedAction<Object> ) pa3 );
            } else {
                Thread.currentThread().setContextClassLoader ( original );
            }
        }
    }
    public void setClassLoader ( final ClassLoader classLoader ) {
        this.digester.setClassLoader ( classLoader );
    }
    static {
        log = LogFactory.getLog ( TldParser.class );
    }
}
