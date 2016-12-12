package org.apache.juli;
import java.util.TimeZone;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;
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
        this.cache = new String[DateFormatCache.access$200 ( DateFormatCache.this )];
        ( this.formatter = new SimpleDateFormat ( DateFormatCache.access$300 ( DateFormatCache.this ), Locale.US ) ).setTimeZone ( TimeZone.getDefault() );
        this.parent = parent;
    }
    private String getFormat ( final long time ) {
        final long seconds = time / 1000L;
        if ( seconds == this.previousSeconds ) {
            return this.previousFormat;
        }
        this.previousSeconds = seconds;
        int index = ( this.offset + ( int ) ( seconds - this.first ) ) % DateFormatCache.access$200 ( DateFormatCache.this );
        if ( index < 0 ) {
            index += DateFormatCache.access$200 ( DateFormatCache.this );
        }
        if ( seconds >= this.first && seconds <= this.last ) {
            if ( this.cache[index] != null ) {
                return this.previousFormat = this.cache[index];
            }
        } else if ( seconds >= this.last + DateFormatCache.access$200 ( DateFormatCache.this ) || seconds <= this.first - DateFormatCache.access$200 ( DateFormatCache.this ) ) {
            this.first = seconds;
            this.last = this.first + DateFormatCache.access$200 ( DateFormatCache.this ) - 1L;
            index = 0;
            this.offset = 0;
            for ( int i = 1; i < DateFormatCache.access$200 ( DateFormatCache.this ); ++i ) {
                this.cache[i] = null;
            }
        } else if ( seconds > this.last ) {
            for ( int i = 1; i < seconds - this.last; ++i ) {
                this.cache[ ( index + DateFormatCache.access$200 ( DateFormatCache.this ) - i ) % DateFormatCache.access$200 ( DateFormatCache.this )] = null;
            }
            this.first = seconds - ( DateFormatCache.access$200 ( DateFormatCache.this ) - 1 );
            this.last = seconds;
            this.offset = ( index + 1 ) % DateFormatCache.access$200 ( DateFormatCache.this );
        } else if ( seconds < this.first ) {
            for ( int i = 1; i < this.first - seconds; ++i ) {
                this.cache[ ( index + i ) % DateFormatCache.access$200 ( DateFormatCache.this )] = null;
            }
            this.first = seconds;
            this.last = seconds + ( DateFormatCache.access$200 ( DateFormatCache.this ) - 1 );
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
