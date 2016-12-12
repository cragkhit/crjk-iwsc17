package org.apache.catalina.util;
import java.util.Hashtable;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.TimeZone;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Properties;
public class Strftime {
    protected static final Properties translate;
    protected final SimpleDateFormat simpleDateFormat;
    public Strftime ( final String origFormat, final Locale locale ) {
        final String convertedFormat = this.convertDateFormat ( origFormat );
        this.simpleDateFormat = new SimpleDateFormat ( convertedFormat, locale );
    }
    public String format ( final Date date ) {
        return this.simpleDateFormat.format ( date );
    }
    public TimeZone getTimeZone() {
        return this.simpleDateFormat.getTimeZone();
    }
    public void setTimeZone ( final TimeZone timeZone ) {
        this.simpleDateFormat.setTimeZone ( timeZone );
    }
    protected String convertDateFormat ( final String pattern ) {
        boolean inside = false;
        boolean mark = false;
        boolean modifiedCommand = false;
        final StringBuilder buf = new StringBuilder();
        for ( int i = 0; i < pattern.length(); ++i ) {
            final char c = pattern.charAt ( i );
            if ( c == '%' && !mark ) {
                mark = true;
            } else if ( mark ) {
                if ( modifiedCommand ) {
                    modifiedCommand = false;
                    mark = false;
                } else {
                    inside = this.translateCommand ( buf, pattern, i, inside );
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
        if ( buf.length() > 0 ) {
            final char lastChar = buf.charAt ( buf.length() - 1 );
            if ( lastChar != '\'' && inside ) {
                buf.append ( '\'' );
            }
        }
        return buf.toString();
    }
    protected String quote ( final String str, final boolean insideQuotes ) {
        String retVal = str;
        if ( !insideQuotes ) {
            retVal = '\'' + retVal + '\'';
        }
        return retVal;
    }
    protected boolean translateCommand ( final StringBuilder buf, final String pattern, final int index, final boolean oldInside ) {
        final char firstChar = pattern.charAt ( index );
        boolean newInside = oldInside;
        if ( firstChar == 'O' || firstChar == 'E' ) {
            if ( index + 1 < pattern.length() ) {
                newInside = this.translateCommand ( buf, pattern, index + 1, oldInside );
            } else {
                buf.append ( this.quote ( "%" + firstChar, oldInside ) );
            }
        } else {
            final String command = Strftime.translate.getProperty ( String.valueOf ( firstChar ) );
            if ( command == null ) {
                buf.append ( this.quote ( "%" + firstChar, oldInside ) );
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
    static {
        ( ( Hashtable<String, String> ) ( translate = new Properties() ) ).put ( "a", "EEE" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "A", "EEEE" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "b", "MMM" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "B", "MMMM" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "c", "EEE MMM d HH:mm:ss yyyy" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "d", "dd" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "D", "MM/dd/yy" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "e", "dd" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "F", "yyyy-MM-dd" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "g", "yy" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "G", "yyyy" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "H", "HH" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "h", "MMM" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "I", "hh" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "j", "DDD" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "k", "HH" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "l", "hh" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "m", "MM" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "M", "mm" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "n", "\n" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "p", "a" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "P", "a" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "r", "hh:mm:ss a" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "R", "HH:mm" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "S", "ss" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "t", "\t" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "T", "HH:mm:ss" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "V", "ww" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "X", "HH:mm:ss" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "x", "MM/dd/yy" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "y", "yy" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "Y", "yyyy" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "Z", "z" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "z", "Z" );
        ( ( Hashtable<String, String> ) Strftime.translate ).put ( "%", "%" );
    }
}
