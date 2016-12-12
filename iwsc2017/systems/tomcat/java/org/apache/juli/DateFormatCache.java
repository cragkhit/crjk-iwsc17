package org.apache.juli;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
public class DateFormatCache {
    private static final String msecPattern = "#";
    private final String format;
    private final int cacheSize;
    private final Cache cache;
    private String tidyFormat ( String format ) {
        boolean escape = false;
        StringBuilder result = new StringBuilder();
        int len = format.length();
        char x;
        for ( int i = 0; i < len; i++ ) {
            x = format.charAt ( i );
            if ( escape || x != 'S' ) {
                result.append ( x );
            } else {
                result.append ( msecPattern );
            }
            if ( x == '\'' ) {
                escape = !escape;
            }
        }
        return result.toString();
    }
    public DateFormatCache ( int size, String format, DateFormatCache parent ) {
        cacheSize = size;
        this.format = tidyFormat ( format );
        Cache parentCache = null;
        if ( parent != null ) {
            synchronized ( parent ) {
                parentCache = parent.cache;
            }
        }
        cache = new Cache ( parentCache );
    }
    public String getFormat ( long time ) {
        return cache.getFormat ( time );
    }
    public String getTimeFormat() {
        return format;
    }
    private class Cache {
        private long previousSeconds = Long.MIN_VALUE;
        private String previousFormat = "";
        private long first = Long.MIN_VALUE;
        private long last = Long.MIN_VALUE;
        private int offset = 0;
        private final Date currentDate = new Date();
        private String cache[];
        private SimpleDateFormat formatter;
        private Cache parent = null;
        private Cache ( Cache parent ) {
            cache = new String[cacheSize];
            formatter = new SimpleDateFormat ( format, Locale.US );
            formatter.setTimeZone ( TimeZone.getDefault() );
            this.parent = parent;
        }
        private String getFormat ( long time ) {
            long seconds = time / 1000;
            if ( seconds == previousSeconds ) {
                return previousFormat;
            }
            previousSeconds = seconds;
            int index = ( offset + ( int ) ( seconds - first ) ) % cacheSize;
            if ( index < 0 ) {
                index += cacheSize;
            }
            if ( seconds >= first && seconds <= last ) {
                if ( cache[index] != null ) {
                    previousFormat = cache[index];
                    return previousFormat;
                }
            } else if ( seconds >= last + cacheSize || seconds <= first - cacheSize ) {
                first = seconds;
                last = first + cacheSize - 1;
                index = 0;
                offset = 0;
                for ( int i = 1; i < cacheSize; i++ ) {
                    cache[i] = null;
                }
            } else if ( seconds > last ) {
                for ( int i = 1; i < seconds - last; i++ ) {
                    cache[ ( index + cacheSize - i ) % cacheSize] = null;
                }
                first = seconds - ( cacheSize - 1 );
                last = seconds;
                offset = ( index + 1 ) % cacheSize;
            } else if ( seconds < first ) {
                for ( int i = 1; i < first - seconds; i++ ) {
                    cache[ ( index + i ) % cacheSize] = null;
                }
                first = seconds;
                last = seconds + ( cacheSize - 1 );
                offset = index;
            }
            if ( parent != null ) {
                synchronized ( parent ) {
                    previousFormat = parent.getFormat ( time );
                }
            } else {
                currentDate.setTime ( time );
                previousFormat = formatter.format ( currentDate );
            }
            cache[index] = previousFormat;
            return previousFormat;
        }
    }
}
