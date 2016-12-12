package org.apache.juli;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
public class VerbatimFormatter extends Formatter {
    @Override
    public String format ( LogRecord record ) {
        StringBuilder sb = new StringBuilder ( record.getMessage() );
        sb.append ( System.lineSeparator() );
        return sb.toString();
    }
}
