package org.apache.juli;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
public class JdkLoggerFormatter extends Formatter {
    public static final int LOG_LEVEL_TRACE  = 400;
    public static final int LOG_LEVEL_DEBUG  = 500;
    public static final int LOG_LEVEL_INFO   = 800;
    public static final int LOG_LEVEL_WARN   = 900;
    public static final int LOG_LEVEL_ERROR  = 1000;
    public static final int LOG_LEVEL_FATAL  = 1000;
    @Override
    public String format ( LogRecord record ) {
        Throwable t = record.getThrown();
        int level = record.getLevel().intValue();
        String name = record.getLoggerName();
        long time = record.getMillis();
        String message = formatMessage ( record );
        if ( name.indexOf ( '.' ) >= 0 ) {
            name = name.substring ( name.lastIndexOf ( '.' ) + 1 );
        }
        StringBuilder buf = new StringBuilder();
        buf.append ( time );
        for ( int i = 0; i < 8 - buf.length(); i++ ) {
            buf.append ( " " );
        }
        switch ( level ) {
        case LOG_LEVEL_TRACE:
            buf.append ( " T " );
            break;
        case LOG_LEVEL_DEBUG:
            buf.append ( " D " );
            break;
        case LOG_LEVEL_INFO:
            buf.append ( " I " );
            break;
        case LOG_LEVEL_WARN:
            buf.append ( " W " );
            break;
        case LOG_LEVEL_ERROR:
            buf.append ( " E " );
            break;
        default:
            buf.append ( "   " );
        }
        buf.append ( name );
        buf.append ( " " );
        for ( int i = 0; i < 8 - buf.length(); i++ ) {
            buf.append ( " " );
        }
        buf.append ( message );
        if ( t != null ) {
            buf.append ( System.lineSeparator() );
            java.io.StringWriter sw = new java.io.StringWriter ( 1024 );
            java.io.PrintWriter pw = new java.io.PrintWriter ( sw );
            t.printStackTrace ( pw );
            pw.close();
            buf.append ( sw.toString() );
        }
        buf.append ( System.lineSeparator() );
        return buf.toString();
    }
}
