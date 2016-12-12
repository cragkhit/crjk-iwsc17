package org.apache.jasper.compiler;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
private static class EnableDTDValidationException extends SAXParseException {
    private static final long serialVersionUID = 1L;
    EnableDTDValidationException ( final String message, final Locator loc ) {
        super ( message, loc );
    }
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
