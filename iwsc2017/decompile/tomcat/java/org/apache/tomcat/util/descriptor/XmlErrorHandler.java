package org.apache.tomcat.util.descriptor;
import java.util.Iterator;
import org.apache.juli.logging.Log;
import org.xml.sax.SAXException;
import java.util.ArrayList;
import org.xml.sax.SAXParseException;
import java.util.List;
import org.apache.tomcat.util.res.StringManager;
import org.xml.sax.ErrorHandler;
public class XmlErrorHandler implements ErrorHandler {
    private static final StringManager sm;
    private final List<SAXParseException> errors;
    private final List<SAXParseException> warnings;
    public XmlErrorHandler() {
        this.errors = new ArrayList<SAXParseException>();
        this.warnings = new ArrayList<SAXParseException>();
    }
    @Override
    public void error ( final SAXParseException exception ) throws SAXException {
        this.errors.add ( exception );
    }
    @Override
    public void fatalError ( final SAXParseException exception ) throws SAXException {
        throw exception;
    }
    @Override
    public void warning ( final SAXParseException exception ) throws SAXException {
        this.warnings.add ( exception );
    }
    public List<SAXParseException> getErrors() {
        return this.errors;
    }
    public List<SAXParseException> getWarnings() {
        return this.warnings;
    }
    public void logFindings ( final Log log, final String source ) {
        for ( final SAXParseException e : this.getWarnings() ) {
            log.warn ( XmlErrorHandler.sm.getString ( "xmlErrorHandler.warning", e.getMessage(), source ) );
        }
        for ( final SAXParseException e : this.getErrors() ) {
            log.warn ( XmlErrorHandler.sm.getString ( "xmlErrorHandler.error", e.getMessage(), source ) );
        }
    }
    static {
        sm = StringManager.getManager ( Constants.PACKAGE_NAME );
    }
}
