package org.apache.catalina.manager;
import java.text.NumberFormat;
import java.text.DateFormat;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import org.apache.catalina.manager.util.SessionUtils;
import org.apache.catalina.Session;
public class JspHelper {
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final int HIGHEST_SPECIAL = 62;
    private static final char[][] specialCharactersRepresentation;
    public static String guessDisplayLocaleFromSession ( final Session in_session ) {
        return localeToString ( SessionUtils.guessLocaleFromSession ( in_session ) );
    }
    private static String localeToString ( final Locale locale ) {
        if ( locale != null ) {
            return escapeXml ( locale.toString() );
        }
        return "";
    }
    public static String guessDisplayUserFromSession ( final Session in_session ) {
        final Object user = SessionUtils.guessUserFromSession ( in_session );
        return escapeXml ( user );
    }
    public static String getDisplayCreationTimeForSession ( final Session in_session ) {
        try {
            if ( in_session.getCreationTime() == 0L ) {
                return "";
            }
            final DateFormat formatter = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss" );
            return formatter.format ( new Date ( in_session.getCreationTime() ) );
        } catch ( IllegalStateException ise ) {
            return "";
        }
    }
    public static String getDisplayLastAccessedTimeForSession ( final Session in_session ) {
        try {
            if ( in_session.getLastAccessedTime() == 0L ) {
                return "";
            }
            final DateFormat formatter = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss" );
            return formatter.format ( new Date ( in_session.getLastAccessedTime() ) );
        } catch ( IllegalStateException ise ) {
            return "";
        }
    }
    public static String getDisplayUsedTimeForSession ( final Session in_session ) {
        try {
            if ( in_session.getCreationTime() == 0L ) {
                return "";
            }
        } catch ( IllegalStateException ise ) {
            return "";
        }
        return secondsToTimeString ( SessionUtils.getUsedTimeForSession ( in_session ) / 1000L );
    }
    public static String getDisplayTTLForSession ( final Session in_session ) {
        try {
            if ( in_session.getCreationTime() == 0L ) {
                return "";
            }
        } catch ( IllegalStateException ise ) {
            return "";
        }
        return secondsToTimeString ( SessionUtils.getTTLForSession ( in_session ) / 1000L );
    }
    public static String getDisplayInactiveTimeForSession ( final Session in_session ) {
        try {
            if ( in_session.getCreationTime() == 0L ) {
                return "";
            }
        } catch ( IllegalStateException ise ) {
            return "";
        }
        return secondsToTimeString ( SessionUtils.getInactiveTimeForSession ( in_session ) / 1000L );
    }
    public static String secondsToTimeString ( long in_seconds ) {
        final StringBuilder buff = new StringBuilder ( 9 );
        if ( in_seconds < 0L ) {
            buff.append ( '-' );
            in_seconds = -in_seconds;
        }
        long rest = in_seconds;
        final long hour = rest / 3600L;
        rest %= 3600L;
        final long minute = rest / 60L;
        final long second;
        rest = ( second = rest % 60L );
        if ( hour < 10L ) {
            buff.append ( '0' );
        }
        buff.append ( hour );
        buff.append ( ':' );
        if ( minute < 10L ) {
            buff.append ( '0' );
        }
        buff.append ( minute );
        buff.append ( ':' );
        if ( second < 10L ) {
            buff.append ( '0' );
        }
        buff.append ( second );
        return buff.toString();
    }
    public static String escapeXml ( final Object obj ) {
        String value = null;
        try {
            value = ( ( obj == null ) ? null : obj.toString() );
        } catch ( Exception ex ) {}
        return escapeXml ( value );
    }
    public static String escapeXml ( final String buffer ) {
        if ( buffer == null ) {
            return "";
        }
        int start = 0;
        final int length = buffer.length();
        final char[] arrayBuffer = buffer.toCharArray();
        StringBuilder escapedBuffer = null;
        for ( int i = 0; i < length; ++i ) {
            final char c = arrayBuffer[i];
            if ( c <= '>' ) {
                final char[] escaped = JspHelper.specialCharactersRepresentation[c];
                if ( escaped != null ) {
                    if ( start == 0 ) {
                        escapedBuffer = new StringBuilder ( length + 5 );
                    }
                    if ( start < i ) {
                        escapedBuffer.append ( arrayBuffer, start, i - start );
                    }
                    start = i + 1;
                    escapedBuffer.append ( escaped );
                }
            }
        }
        if ( start == 0 ) {
            return buffer;
        }
        if ( start < length ) {
            escapedBuffer.append ( arrayBuffer, start, length - start );
        }
        return escapedBuffer.toString();
    }
    public static String formatNumber ( final long number ) {
        return NumberFormat.getNumberInstance().format ( number );
    }
    static {
        ( specialCharactersRepresentation = new char[63][] ) [38] = "&amp;".toCharArray();
        JspHelper.specialCharactersRepresentation[60] = "&lt;".toCharArray();
        JspHelper.specialCharactersRepresentation[62] = "&gt;".toCharArray();
        JspHelper.specialCharactersRepresentation[34] = "&#034;".toCharArray();
        JspHelper.specialCharactersRepresentation[39] = "&#039;".toCharArray();
    }
}
