package org.apache.juli;
import java.util.TimeZone;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;
public class DateFormatCache {
    private static final String msecPattern = "#";
    private final String format;
    private final int cacheSize;
    private final Cache cache;
    private String tidyFormat ( final String format ) {
        boolean escape = false;
        final StringBuilder result = new StringBuilder();
        for ( int len = format.length(), i = 0; i < len; ++i ) {
            final char x = format.charAt ( i );
            if ( escape || x != 'S' ) {
                result.append ( x );
            } else {
                result.append ( "#" );
            }
            if ( x == '\'' ) {
                escape = !escape;
            }
        }
        return result.toString();
    }
    public DateFormatCache ( final int size, final String format, final DateFormatCache parent ) {
        this.cacheSize = size;
        this.format = this.tidyFormat ( format );
        Cache parentCache = null;
        if ( parent != null ) {
            synchronized ( parent ) {
                parentCache = parent.cache;
            }
        }
        this.cache = new Cache ( parentCache );
    }
    public String getFormat ( final long time ) {
        return this.cache.getFormat ( time );
    }
    public String getTimeFormat() {
        return this.format;
    }
    private class Cache {
        private long previousSeconds;
        private String previousFormat;
        private long first;
        private long last;
        private int offset;
        private final Date currentDate;
        private String[] cache;
        private SimpleDateFormat formatter;
        private Cache parent;
        private Cache ( final Cache parent ) {
            this.previousSeconds = Long.MIN_VALUE;
            this.previousFormat = "";
            this.first = Long.MIN_VALUE;
            this.last = Long.MIN_VALUE;
            this.offset = 0;
            this.currentDate = new Date();
            this.parent = null;
            this.cache = new String[DateFormatCache.this.cacheSize];
            ( this.formatter = new SimpleDateFormat ( DateFormatCache.this.format, Locale.US ) ).setTimeZone ( TimeZone.getDefault() );
            this.parent = parent;
        }
        private String getFormat ( final long time ) {
            final long seconds = time / 1000L;
            if ( seconds == this.previousSeconds ) {
                return this.previousFormat;
            }
            this.previousSeconds = seconds;
            int index = ( this.offset + ( int ) ( seconds - this.first ) ) % DateFormatCache.this.cacheSize;
            if ( index < 0 ) {
                index += DateFormatCache.this.cacheSize;
            }
            if ( seconds >= this.first && seconds <= this.last ) {
                if ( this.cache[index] != null ) {
                    return this.previousFormat = this.cache[index];
                }
            } else if ( seconds >= this.last + DateFormatCache.this.cacheSize || seconds <= this.first - DateFormatCache.this.cacheSize ) {
                this.first = seconds;
                this.last = this.first + DateFormatCache.this.cacheSize - 1L;
                index = 0;
                this.offset = 0;
                for ( int i = 1; i < DateFormatCache.this.cacheSize; ++i ) {
                    this.cache[i] = null;
                }
            } else if ( seconds > this.last ) {
                for ( int i = 1; i < seconds - this.last; ++i ) {
                    this.cache[ ( index + DateFormatCache.this.cacheSize - i ) % DateFormatCache.this.cacheSize] = null;
                }
                this.first = seconds - ( DateFormatCache.this.cacheSize - 1 );
                this.last = seconds;
                this.offset = ( index + 1 ) % DateFormatCache.this.cacheSize;
            } else if ( seconds < this.first ) {
                for ( int i = 1; i < this.first - seconds; ++i ) {
                    this.cache[ ( index + i ) % DateFormatCache.this.cacheSize] = null;
                }
                this.first = seconds;
                this.last = seconds + ( DateFormatCache.this.cacheSize - 1 );
                this.offset = index;
            }
            if ( this.parent != null ) {
                synchronized ( this.parent ) {
                    this.previousFormat = this.parent.getFormat ( time );
                }
            } else {
                this.currentDate.setTime ( time );
                this.previousFormat = this.formatter.format ( this.currentDate );
            }
            this.cache[index] = this.previousFormat;
            return this.previousFormat;
        }
    }
}
