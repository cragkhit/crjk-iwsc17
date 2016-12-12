package org.jfree.data.time;
import org.jfree.date.SerialDate;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.Date;
import java.io.Serializable;
public class Quarter extends RegularTimePeriod implements Serializable {
    private static final long serialVersionUID = 3810061714380888671L;
    public static final int FIRST_QUARTER = 1;
    public static final int LAST_QUARTER = 4;
    public static final int[] FIRST_MONTH_IN_QUARTER;
    public static final int[] LAST_MONTH_IN_QUARTER;
    private short year;
    private byte quarter;
    private long firstMillisecond;
    private long lastMillisecond;
    public Quarter() {
        this ( new Date() );
    }
    public Quarter ( final int quarter, final int year ) {
        if ( quarter < 1 || quarter > 4 ) {
            throw new IllegalArgumentException ( "Quarter outside valid range." );
        }
        this.year = ( short ) year;
        this.quarter = ( byte ) quarter;
        this.peg ( Calendar.getInstance() );
    }
    public Quarter ( final int quarter, final Year year ) {
        if ( quarter < 1 || quarter > 4 ) {
            throw new IllegalArgumentException ( "Quarter outside valid range." );
        }
        this.year = ( short ) year.getYear();
        this.quarter = ( byte ) quarter;
        this.peg ( Calendar.getInstance() );
    }
    public Quarter ( final Date time ) {
        this ( time, TimeZone.getDefault() );
    }
    public Quarter ( final Date time, final TimeZone zone ) {
        this ( time, zone, Locale.getDefault() );
    }
    public Quarter ( final Date time, final TimeZone zone, final Locale locale ) {
        final Calendar calendar = Calendar.getInstance ( zone, locale );
        calendar.setTime ( time );
        final int month = calendar.get ( 2 ) + 1;
        this.quarter = ( byte ) SerialDate.monthCodeToQuarter ( month );
        this.year = ( short ) calendar.get ( 1 );
        this.peg ( calendar );
    }
    public int getQuarter() {
        return this.quarter;
    }
    public Year getYear() {
        return new Year ( this.year );
    }
    public int getYearValue() {
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
        Quarter result;
        if ( this.quarter > 1 ) {
            result = new Quarter ( this.quarter - 1, this.year );
        } else if ( this.year > 1900 ) {
            result = new Quarter ( 4, this.year - 1 );
        } else {
            result = null;
        }
        return result;
    }
    @Override
    public RegularTimePeriod next() {
        Quarter result;
        if ( this.quarter < 4 ) {
            result = new Quarter ( this.quarter + 1, this.year );
        } else if ( this.year < 9999 ) {
            result = new Quarter ( 1, this.year + 1 );
        } else {
            result = null;
        }
        return result;
    }
    @Override
    public long getSerialIndex() {
        return this.year * 4L + this.quarter;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == null ) {
            return false;
        }
        if ( obj instanceof Quarter ) {
            final Quarter target = ( Quarter ) obj;
            return this.quarter == target.getQuarter() && this.year == target.getYearValue();
        }
        return false;
    }
    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + this.quarter;
        result = 37 * result + this.year;
        return result;
    }
    @Override
    public int compareTo ( final Object o1 ) {
        int result;
        if ( o1 instanceof Quarter ) {
            final Quarter q = ( Quarter ) o1;
            result = this.year - q.getYearValue();
            if ( result == 0 ) {
                result = this.quarter - q.getQuarter();
            }
        } else if ( o1 instanceof RegularTimePeriod ) {
            result = 0;
        } else {
            result = 1;
        }
        return result;
    }
    @Override
    public String toString() {
        return "Q" + this.quarter + "/" + this.year;
    }
    @Override
    public long getFirstMillisecond ( final Calendar calendar ) {
        final int month = Quarter.FIRST_MONTH_IN_QUARTER[this.quarter];
        calendar.set ( this.year, month - 1, 1, 0, 0, 0 );
        calendar.set ( 14, 0 );
        return calendar.getTimeInMillis();
    }
    @Override
    public long getLastMillisecond ( final Calendar calendar ) {
        final int month = Quarter.LAST_MONTH_IN_QUARTER[this.quarter];
        final int eom = SerialDate.lastDayOfMonth ( month, ( int ) this.year );
        calendar.set ( this.year, month - 1, eom, 23, 59, 59 );
        calendar.set ( 14, 999 );
        return calendar.getTimeInMillis();
    }
    public static Quarter parseQuarter ( final String s ) {
        final int i = s.indexOf ( "Q" );
        if ( i == -1 ) {
            throw new TimePeriodFormatException ( "Missing Q." );
        }
        if ( i == s.length() - 1 ) {
            throw new TimePeriodFormatException ( "Q found at end of string." );
        }
        final String qstr = s.substring ( i + 1, i + 2 );
        final int quarter = Integer.parseInt ( qstr );
        String remaining = s.substring ( 0, i ) + s.substring ( i + 2, s.length() );
        remaining = remaining.replace ( '/', ' ' );
        remaining = remaining.replace ( ',', ' ' );
        remaining = remaining.replace ( '-', ' ' );
        final Year year = Year.parseYear ( remaining.trim() );
        final Quarter result = new Quarter ( quarter, year );
        return result;
    }
    static {
        FIRST_MONTH_IN_QUARTER = new int[] { 0, 1, 4, 7, 10 };
        LAST_MONTH_IN_QUARTER = new int[] { 0, 3, 6, 9, 12 };
    }
}
