package org.apache.catalina.valves;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
protected static class DateFormatCache {
    private int cacheSize;
    private final Locale cacheDefaultLocale;
    private final DateFormatCache parent;
    protected final Cache cLFCache;
    private final HashMap<String, Cache> formatCache;
    protected DateFormatCache ( final int size, final Locale loc, final DateFormatCache parent ) {
        this.cacheSize = 0;
        this.formatCache = new HashMap<String, Cache>();
        this.cacheSize = size;
        this.cacheDefaultLocale = loc;
        this.parent = parent;
        Cache parentCache = null;
        if ( parent != null ) {
            synchronized ( parent ) {
                parentCache = parent.getCache ( null, null );
            }
        }
        this.cLFCache = new Cache ( parentCache );
    }
    private Cache getCache ( final String format, final Locale loc ) {
        Cache cache;
        if ( format == null ) {
            cache = this.cLFCache;
        } else {
            cache = this.formatCache.get ( format );
            if ( cache == null ) {
                Cache parentCache = null;
                if ( this.parent != null ) {
                    synchronized ( this.parent ) {
                        parentCache = this.parent.getCache ( format, loc );
                    }
                }
                cache = new Cache ( format, loc, parentCache );
                this.formatCache.put ( format, cache );
            }
        }
        return cache;
    }
    public String getFormat ( final long time ) {
        return this.cLFCache.getFormatInternal ( time );
    }
    public String getFormat ( final String format, final Locale loc, final long time ) {
        return this.getCache ( format, loc ).getFormatInternal ( time );
    }
    protected class Cache {
        private static final String cLFFormat = "dd/MMM/yyyy:HH:mm:ss Z";
        private long previousSeconds;
        private String previousFormat;
        private long first;
        private long last;
        private int offset;
        private final Date currentDate;
        protected final String[] cache;
        private SimpleDateFormat formatter;
        private boolean isCLF;
        private Cache parent;
        private Cache ( final DateFormatCache this$0, final Cache parent ) {
            this ( this$0, null, parent );
        }
        private Cache ( final DateFormatCache this$0, final String format, final Cache parent ) {
            this ( this$0, format, null, parent );
        }
        private Cache ( String format, Locale loc, final Cache parent ) {
            this.previousSeconds = Long.MIN_VALUE;
            this.previousFormat = "";
            this.first = Long.MIN_VALUE;
            this.last = Long.MIN_VALUE;
            this.offset = 0;
            this.currentDate = new Date();
            this.isCLF = false;
            this.parent = null;
            this.cache = new String[DateFormatCache.this.cacheSize];
            for ( int i = 0; i < DateFormatCache.this.cacheSize; ++i ) {
                this.cache[i] = null;
            }
            if ( loc == null ) {
                loc = DateFormatCache.this.cacheDefaultLocale;
            }
            if ( format == null ) {
                this.isCLF = true;
                format = "dd/MMM/yyyy:HH:mm:ss Z";
                this.formatter = new SimpleDateFormat ( format, Locale.US );
            } else {
                this.formatter = new SimpleDateFormat ( format, loc );
            }
            this.formatter.setTimeZone ( TimeZone.getDefault() );
            this.parent = parent;
        }
        private String getFormatInternal ( final long time ) {
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
                    this.previousFormat = this.parent.getFormatInternal ( time );
                }
            } else {
                this.currentDate.setTime ( time );
                this.previousFormat = this.formatter.format ( this.currentDate );
                if ( this.isCLF ) {
                    final StringBuilder current = new StringBuilder ( 32 );
                    current.append ( '[' );
                    current.append ( this.previousFormat );
                    current.append ( ']' );
                    this.previousFormat = current.toString();
                }
            }
            this.cache[index] = this.previousFormat;
            return this.previousFormat;
        }
    }
}
