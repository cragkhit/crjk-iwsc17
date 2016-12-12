package org.apache.catalina.util;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
public class Strftime {
    protected static final Properties translate;
    protected final SimpleDateFormat simpleDateFormat;
    static {
        translate = new Properties();
        translate.put ( "a", "EEE" );
        translate.put ( "A", "EEEE" );
        translate.put ( "b", "MMM" );
        translate.put ( "B", "MMMM" );
        translate.put ( "c", "EEE MMM d HH:mm:ss yyyy" );
        translate.put ( "d", "dd" );
        translate.put ( "D", "MM/dd/yy" );
        translate.put ( "e", "dd" );
        translate.put ( "F", "yyyy-MM-dd" );
        translate.put ( "g", "yy" );
        translate.put ( "G", "yyyy" );
        translate.put ( "H", "HH" );
        translate.put ( "h", "MMM" );
        translate.put ( "I", "hh" );
        translate.put ( "j", "DDD" );
        translate.put ( "k", "HH" );
        translate.put ( "l", "hh" );
        translate.put ( "m", "MM" );
        translate.put ( "M", "mm" );
        translate.put ( "n", "\n" );
        translate.put ( "p", "a" );
        translate.put ( "P", "a" );
        translate.put ( "r", "hh:mm:ss a" );
        translate.put ( "R", "HH:mm" );
        translate.put ( "S", "ss" );
        translate.put ( "t", "\t" );
        translate.put ( "T", "HH:mm:ss" );
        translate.put ( "V", "ww" );
        translate.put ( "X", "HH:mm:ss" );
        translate.put ( "x", "MM/dd/yy" );
        translate.put ( "y", "yy" );
        translate.put ( "Y", "yyyy" );
        translate.put ( "Z", "z" );
        translate.put ( "z", "Z" );
        translate.put ( "%", "%" );
    }
    public Strftime ( String origFormat, Locale locale ) {
        String convertedFormat = convertDateFormat ( origFormat );
        simpleDateFormat = new SimpleDateFormat ( convertedFormat, locale );
    }
    public String format ( Date date ) {
        return simpleDateFormat.format ( date );
    }
    public TimeZone getTimeZone() {
        return simpleDateFormat.getTimeZone();
    }
    public void setTimeZone ( TimeZone timeZone ) {
        simpleDateFormat.setTimeZone ( timeZone );
    }
    protected String convertDateFormat ( String pattern ) {
        boolean inside = false;
        boolean mark = false;
        boolean modifiedCommand = false;
        StringBuilder buf = new StringBuilder();
        for ( int i = 0; i < pattern.length(); i++ ) {
            char c = pattern.charAt ( i );
            if ( c == '%' && !mark ) {
                mark = true;
            } else {
                if ( mark ) {
                    if ( modifiedCommand ) {
                        modifiedCommand = false;
                        mark = false;
                    } else {
                        inside = translateCommand ( buf, pattern, i, inside );
                        if ( c == 'O' || c == 'E' ) {
                            modifiedCommand = true;
                        } else {
                            mark = false;
                        }
                    }
                } else {
                    if ( !inside && c != ' ' ) {
                        buf.append ( "'" );
                        inside = true;
                    }
                    buf.append ( c );
                }
            }
        }
        if ( buf.length() > 0 ) {
            char lastChar = buf.charAt ( buf.length() - 1 );
            if ( lastChar != '\'' && inside ) {
                buf.append ( '\'' );
            }
        }
        return buf.toString();
    }
    protected String quote ( String str, boolean insideQuotes ) {
        String retVal = str;
        if ( !insideQuotes ) {
            retVal = '\'' + retVal + '\'';
        }
        return retVal;
    }
    protected boolean translateCommand ( StringBuilder buf, String pattern, int index, boolean oldInside ) {
        char firstChar = pattern.charAt ( index );
        boolean newInside = oldInside;
        if ( firstChar == 'O' || firstChar == 'E' ) {
            if ( index + 1 < pattern.length() ) {
                newInside = translateCommand ( buf, pattern, index + 1, oldInside );
            } else {
                buf.append ( quote ( "%" + firstChar, oldInside ) );
            }
        } else {
            String command = translate.getProperty ( String.valueOf ( firstChar ) );
            if ( command == null ) {
                buf.append ( quote ( "%" + firstChar, oldInside ) );
            } else {
                if ( oldInside ) {
                    buf.append ( '\'' );
                }
                buf.append ( command );
                newInside = false;
            }
        }
        return newInside;
    }
}
