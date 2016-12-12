package org.apache.tomcat.util.descriptor;
import java.util.ArrayList;
import java.util.List;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.res.StringManager;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
public class XmlErrorHandler implements ErrorHandler {
    private static final StringManager sm =
        StringManager.getManager ( Constants.PACKAGE_NAME );
    private final List<SAXParseException> errors = new ArrayList<>();
    private final List<SAXParseException> warnings = new ArrayList<>();
    @Override
    public void error ( SAXParseException exception ) throws SAXException {
        errors.add ( exception );
    }
    @Override
    public void fatalError ( SAXParseException exception ) throws SAXException {
        throw exception;
    }
    @Override
    public void warning ( SAXParseException exception ) throws SAXException {
        warnings.add ( exception );
    }
    public List<SAXParseException> getErrors() {
        return errors;
    }
    public List<SAXParseException> getWarnings() {
        return warnings;
    }
    public void logFindings ( Log log, String source ) {
        for ( SAXParseException e : getWarnings() ) {
            log.warn ( sm.getString (
                           "xmlErrorHandler.warning", e.getMessage(), source ) );
        }
        for ( SAXParseException e : getErrors() ) {
            log.warn ( sm.getString (
                           "xmlErrorHandler.error", e.getMessage(), source ) );
        }
    }
}
