package org.jfree.data.time;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Calendar;
import org.jfree.chart.util.ParamChecks;
import java.util.Date;
import java.io.Serializable;
public class Minute extends RegularTimePeriod implements Serializable {
    private static final long serialVersionUID = 2144572840034842871L;
    public static final int FIRST_MINUTE_IN_HOUR = 0;
    public static final int LAST_MINUTE_IN_HOUR = 59;
    private Day day;
    private byte hour;
    private byte minute;
    private long firstMillisecond;
    private long lastMillisecond;
    public Minute() {
        this ( new Date() );
    }
    public Minute ( final int minute, final Hour hour ) {
        ParamChecks.nullNotPermitted ( hour, "hour" );
        this.minute = ( byte ) minute;
        this.hour = ( byte ) hour.getHour();
        this.day = hour.getDay();
        this.peg ( Calendar.getInstance() );
    }
    public Minute ( final Date time ) {
        this ( time, TimeZone.getDefault(), Locale.getDefault() );
    }
    public Minute ( final Date time, final TimeZone zone ) {
        this ( time, zone, Locale.getDefault() );
    }
    public Minute ( final Date time, final TimeZone zone, final Locale locale ) {
        ParamChecks.nullNotPermitted ( time, "time" );
        ParamChecks.nullNotPermitted ( zone, "zone" );
        ParamChecks.nullNotPermitted ( locale, "locale" );
        final Calendar calendar = Calendar.getInstance ( zone, locale );
        calendar.setTime ( time );
        final int min = calendar.get ( 12 );
        this.minute = ( byte ) min;
        this.hour = ( byte ) calendar.get ( 11 );
        this.day = new Day ( time, zone, locale );
        this.peg ( calendar );
    }
    public Minute ( final int minute, final int hour, final int day, final int month, final int year ) {
        this ( minute, new Hour ( hour, new Day ( day, month, year ) ) );
    }
    public Day getDay() {
        return this.day;
    }
    public Hour getHour() {
        return new Hour ( this.hour, this.day );
    }
    public int getHourValue() {
        return this.hour;
    }
    public int getMinute() {
        return this.minute;
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
        Minute result;
        if ( this.minute != 0 ) {
            result = new Minute ( this.minute - 1, this.getHour() );
        } else {
            final Hour h = ( Hour ) this.getHour().previous();
            if ( h != null ) {
                result = new Minute ( 59, h );
            } else {
                result = null;
            }
        }
        return result;
    }
    @Override
    public RegularTimePeriod next() {
        Minute result;
        if ( this.minute != 59 ) {
            result = new Minute ( this.minute + 1, this.getHour() );
        } else {
            final Hour nextHour = ( Hour ) this.getHour().next();
            if ( nextHour != null ) {
                result = new Minute ( 0, nextHour );
            } else {
                result = null;
            }
        }
        return result;
    }
    @Override
    public long getSerialIndex() {
        final long hourIndex = this.day.getSerialIndex() * 24L + this.hour;
        return hourIndex * 60L + this.minute;
    }
    @Override
    public long getFirstMillisecond ( final Calendar calendar ) {
        final int year = this.day.getYear();
        final int month = this.day.getMonth() - 1;
        final int d = this.day.getDayOfMonth();
        calendar.clear();
        calendar.set ( year, month, d, this.hour, this.minute, 0 );
        calendar.set ( 14, 0 );
        return calendar.getTimeInMillis();
    }
    @Override
    public long getLastMillisecond ( final Calendar calendar ) {
        final int year = this.day.getYear();
        final int month = this.day.getMonth() - 1;
        final int d = this.day.getDayOfMonth();
        calendar.clear();
        calendar.set ( year, month, d, this.hour, this.minute, 59 );
        calendar.set ( 14, 999 );
        return calendar.getTimeInMillis();
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof Minute ) ) {
            return false;
        }
        final Minute that = ( Minute ) obj;
        return this.minute == that.minute && this.hour == that.hour;
    }
    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + this.minute;
        result = 37 * result + this.hour;
        result = 37 * result + this.day.hashCode();
        return result;
    }
    @Override
    public int compareTo ( final Object o1 ) {
        int result;
        if ( o1 instanceof Minute ) {
            final Minute m = ( Minute ) o1;
            result = this.getHour().compareTo ( m.getHour() );
            if ( result == 0 ) {
                result = this.minute - m.getMinute();
            }
        } else if ( o1 instanceof RegularTimePeriod ) {
            result = 0;
        } else {
            result = 1;
        }
        return result;
    }
    public static Minute parseMinute ( String s ) {
        Minute result = null;
        s = s.trim();
        final String daystr = s.substring ( 0, Math.min ( 10, s.length() ) );
        final Day day = Day.parseDay ( daystr );
        if ( day != null ) {
            String hmstr = s.substring ( Math.min ( daystr.length() + 1, s.length() ), s.length() );
            hmstr = hmstr.trim();
            final String hourstr = hmstr.substring ( 0, Math.min ( 2, hmstr.length() ) );
            final int hour = Integer.parseInt ( hourstr );
            if ( hour >= 0 && hour <= 23 ) {
                final String minstr = hmstr.substring ( Math.min ( hourstr.length() + 1, hmstr.length() ), hmstr.length() );
                final int minute = Integer.parseInt ( minstr );
                if ( minute >= 0 && minute <= 59 ) {
                    result = new Minute ( minute, new Hour ( hour, day ) );
                }
            }
        }
        return result;
    }
}
