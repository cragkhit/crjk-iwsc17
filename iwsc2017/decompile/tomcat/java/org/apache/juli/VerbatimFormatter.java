package org.apache.juli;
import java.util.logging.LogRecord;
import java.util.logging.Formatter;
public class VerbatimFormatter extends Formatter {
    @Override
    public String format ( final LogRecord record ) {
        final StringBuilder sb = new StringBuilder ( record.getMessage() );
        sb.append ( System.lineSeparator() );
        return sb.toString();
    }
}
