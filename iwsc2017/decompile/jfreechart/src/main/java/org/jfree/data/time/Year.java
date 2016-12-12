package org.jfree.data.time;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.Date;
import java.io.Serializable;
public class Year extends RegularTimePeriod implements Serializable {
    public static final int MINIMUM_YEAR = -9999;
    public static final int MAXIMUM_YEAR = 9999;
    private static final long serialVersionUID = -7659990929736074836L;
    private short year;
    private long firstMillisecond;
    private long lastMillisecond;
    public Year() {
        this ( new Date() );
    }
    public Year ( final int year ) {
        if ( year < -9999 || year > 9999 ) {
            throw new IllegalArgumentException ( "Year constructor: year (" + year + ") outside valid range." );
        }
        this.year = ( short ) year;
        this.peg ( Calendar.getInstance() );
    }
    public Year ( final Date time ) {
        this ( time, TimeZone.getDefault() );
    }
    public Year ( final Date time, final TimeZone zone ) {
        this ( time, zone, Locale.getDefault() );
    }
    public Year ( final Date time, final TimeZone zone, final Locale locale ) {
        final Calendar calendar = Calendar.getInstance ( zone, locale );
        calendar.setTime ( time );
        this.year = ( short ) calendar.get ( 1 );
        this.peg ( calendar );
    }
    public int getYear() {
        return this.year;
    }
    @Override
    public long getFirstMillisecond() {
        return this.firstMillisecond;
    }
    @Override
    public long getLastMillisecond() {
        return this.lastMillisecond;
    }
    @Override
    public void peg ( final Calendar calendar ) {
        this.firstMillisecond = this.getFirstMillisecond ( calendar );
        this.lastMillisecond = this.getLastMillisecond ( calendar );
    }
    @Override
    public RegularTimePeriod previous() {
        if ( this.year > -9999 ) {
            return new Year ( this.year - 1 );
        }
        return null;
    }
    @Override
    public RegularTimePeriod next() {
        if ( this.year < 9999 ) {
            return new Year ( this.year + 1 );
        }
        return null;
    }
    @Override
    public long getSerialIndex() {
        return this.year;
    }
    @Override
    public long getFirstMillisecond ( final Calendar calendar ) {
        calendar.set ( this.year, 0, 1, 0, 0, 0 );
        calendar.set ( 14, 0 );
        return calendar.getTimeInMillis();
    }
    @Override
    public long getLastMillisecond ( final Calendar calendar ) {
        calendar.set ( this.year, 11, 31, 23, 59, 59 );
        calendar.set ( 14, 999 );
        return calendar.getTimeInMillis();
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof Year ) ) {
            return false;
        }
        final Year that = ( Year ) obj;
        return this.year == that.year;
    }
    @Override
    public int hashCode() {
        int result = 17;
        final int c = this.year;
        result = 37 * result + c;
        return result;
    }
    @Override
    public int compareTo ( final Object o1 ) {
        int result;
        if ( o1 instanceof Year ) {
            final Year y = ( Year ) o1;
            result = this.year - y.getYear();
        } else if ( o1 instanceof RegularTimePeriod ) {
            result = 0;
        } else {
            result = 1;
        }
        return result;
    }
    @Override
    public String toString() {
        return Integer.toString ( this.year );
    }
    public static Year parseYear ( final String s ) {
        int y;
        try {
            y = Integer.parseInt ( s.trim() );
        } catch ( NumberFormatException e ) {
            throw new TimePeriodFormatException ( "Cannot parse string." );
        }
        try {
            return new Year ( y );
        } catch ( IllegalArgumentException e2 ) {
            throw new TimePeriodFormatException ( "Year outside valid range." );
        }
    }
}
