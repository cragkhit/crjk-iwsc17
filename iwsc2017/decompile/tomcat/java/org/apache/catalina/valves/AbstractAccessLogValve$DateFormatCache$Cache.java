package org.apache.catalina.valves;
import java.util.TimeZone;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        this.cache = new String[DateFormatCache.access$000 ( DateFormatCache.this )];
        for ( int i = 0; i < DateFormatCache.access$000 ( DateFormatCache.this ); ++i ) {
            this.cache[i] = null;
        }
        if ( loc == null ) {
            loc = DateFormatCache.access$100 ( DateFormatCache.this );
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
        int index = ( this.offset + ( int ) ( seconds - this.first ) ) % DateFormatCache.access$000 ( DateFormatCache.this );
        if ( index < 0 ) {
            index += DateFormatCache.access$000 ( DateFormatCache.this );
        }
        if ( seconds >= this.first && seconds <= this.last ) {
            if ( this.cache[index] != null ) {
                return this.previousFormat = this.cache[index];
            }
        } else if ( seconds >= this.last + DateFormatCache.access$000 ( DateFormatCache.this ) || seconds <= this.first - DateFormatCache.access$000 ( DateFormatCache.this ) ) {
            this.first = seconds;
            this.last = this.first + DateFormatCache.access$000 ( DateFormatCache.this ) - 1L;
            index = 0;
            this.offset = 0;
            for ( int i = 1; i < DateFormatCache.access$000 ( DateFormatCache.this ); ++i ) {
                this.cache[i] = null;
            }
        } else if ( seconds > this.last ) {
            for ( int i = 1; i < seconds - this.last; ++i ) {
                this.cache[ ( index + DateFormatCache.access$000 ( DateFormatCache.this ) - i ) % DateFormatCache.access$000 ( DateFormatCache.this )] = null;
            }
            this.first = seconds - ( DateFormatCache.access$000 ( DateFormatCache.this ) - 1 );
            this.last = seconds;
            this.offset = ( index + 1 ) % DateFormatCache.access$000 ( DateFormatCache.this );
        } else if ( seconds < this.first ) {
            for ( int i = 1; i < this.first - seconds; ++i ) {
                this.cache[ ( index + i ) % DateFormatCache.access$000 ( DateFormatCache.this )] = null;
            }
            this.first = seconds;
            this.last = seconds + ( DateFormatCache.access$000 ( DateFormatCache.this ) - 1 );
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
