package org.apache.tomcat.util.http;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
public final class FastHttpDateFormat {
    private static final int CACHE_SIZE =
        Integer.parseInt ( System.getProperty ( "org.apache.tomcat.util.http.FastHttpDateFormat.CACHE_SIZE", "1000" ) );
    public static final String RFC1123_DATE =
        "EEE, dd MMM yyyy HH:mm:ss zzz";
    private static final SimpleDateFormat format =
        new SimpleDateFormat ( RFC1123_DATE, Locale.US );
    private static final TimeZone gmtZone = TimeZone.getTimeZone ( "GMT" );
    static {
        format.setTimeZone ( gmtZone );
    }
    private static volatile long currentDateGenerated = 0L;
    private static String currentDate = null;
    private static final Map<Long, String> formatCache = new ConcurrentHashMap<> ( CACHE_SIZE );
    private static final Map<String, Long> parseCache = new ConcurrentHashMap<> ( CACHE_SIZE );
    public static final String getCurrentDate() {
        long now = System.currentTimeMillis();
        if ( ( now - currentDateGenerated ) > 1000 ) {
            synchronized ( format ) {
                if ( ( now - currentDateGenerated ) > 1000 ) {
                    currentDate = format.format ( new Date ( now ) );
                    currentDateGenerated = now;
                }
            }
        }
        return currentDate;
    }
    public static final String formatDate
    ( long value, DateFormat threadLocalformat ) {
        Long longValue = Long.valueOf ( value );
        String cachedDate = formatCache.get ( longValue );
        if ( cachedDate != null ) {
            return cachedDate;
        }
        String newDate = null;
        Date dateValue = new Date ( value );
        if ( threadLocalformat != null ) {
            newDate = threadLocalformat.format ( dateValue );
            updateFormatCache ( longValue, newDate );
        } else {
            synchronized ( format ) {
                newDate = format.format ( dateValue );
            }
            updateFormatCache ( longValue, newDate );
        }
        return newDate;
    }
    public static final long parseDate ( String value,
                                         DateFormat[] threadLocalformats ) {
        Long cachedDate = parseCache.get ( value );
        if ( cachedDate != null ) {
            return cachedDate.longValue();
        }
        Long date = null;
        if ( threadLocalformats != null ) {
            date = internalParseDate ( value, threadLocalformats );
            updateParseCache ( value, date );
        } else {
            throw new IllegalArgumentException();
        }
        if ( date == null ) {
            return ( -1L );
        }
        return date.longValue();
    }
    private static final Long internalParseDate
    ( String value, DateFormat[] formats ) {
        Date date = null;
        for ( int i = 0; ( date == null ) && ( i < formats.length ); i++ ) {
            try {
                date = formats[i].parse ( value );
            } catch ( ParseException e ) {
            }
        }
        if ( date == null ) {
            return null;
        }
        return Long.valueOf ( date.getTime() );
    }
    private static void updateFormatCache ( Long key, String value ) {
        if ( value == null ) {
            return;
        }
        if ( formatCache.size() > CACHE_SIZE ) {
            formatCache.clear();
        }
        formatCache.put ( key, value );
    }
    private static void updateParseCache ( String key, Long value ) {
        if ( value == null ) {
            return;
        }
        if ( parseCache.size() > CACHE_SIZE ) {
            parseCache.clear();
        }
        parseCache.put ( key, value );
    }
}
