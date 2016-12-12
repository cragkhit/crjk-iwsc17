package org.jfree.data.time;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.Date;
import java.io.Serializable;
public class Millisecond extends RegularTimePeriod implements Serializable {
    static final long serialVersionUID = -5316836467277638485L;
    public static final int FIRST_MILLISECOND_IN_SECOND = 0;
    public static final int LAST_MILLISECOND_IN_SECOND = 999;
    private Day day;
    private byte hour;
    private byte minute;
    private byte second;
    private int millisecond;
    private long firstMillisecond;
    public Millisecond() {
        this ( new Date() );
    }
    public Millisecond ( final int millisecond, final Second second ) {
        this.millisecond = millisecond;
        this.second = ( byte ) second.getSecond();
        this.minute = ( byte ) second.getMinute().getMinute();
        this.hour = ( byte ) second.getMinute().getHourValue();
        this.day = second.getMinute().getDay();
        this.peg ( Calendar.getInstance() );
    }
    public Millisecond ( final int millisecond, final int second, final int minute, final int hour, final int day, final int month, final int year ) {
        this ( millisecond, new Second ( second, minute, hour, day, month, year ) );
    }
    public Millisecond ( final Date time ) {
        this ( time, TimeZone.getDefault(), Locale.getDefault() );
    }
    public Millisecond ( final Date time, final TimeZone zone ) {
        this ( time, zone, Locale.getDefault() );
    }
    public Millisecond ( final Date time, final TimeZone zone, final Locale locale ) {
        final Calendar calendar = Calendar.getInstance ( zone, locale );
        calendar.setTime ( time );
        this.millisecond = calendar.get ( 14 );
        this.second = ( byte ) calendar.get ( 13 );
        this.minute = ( byte ) calendar.get ( 12 );
        this.hour = ( byte ) calendar.get ( 11 );
        this.day = new Day ( time, zone, locale );
        this.peg ( calendar );
    }
    public Second getSecond() {
        return new Second ( this.second, this.minute, this.hour, this.day.getDayOfMonth(), this.day.getMonth(), this.day.getYear() );
    }
    public long getMillisecond() {
        return this.millisecond;
    }
    @Override
    public long getFirstMillisecond() {
        return this.firstMillisecond;
    }
    @Override
    public long getLastMillisecond() {
        return this.firstMillisecond;
    }
    @Override
    public void peg ( final Calendar calendar ) {
        this.firstMillisecond = this.getFirstMillisecond ( calendar );
    }
    @Override
    public RegularTimePeriod previous() {
        RegularTimePeriod result = null;
        if ( this.millisecond != 0 ) {
            result = new Millisecond ( this.millisecond - 1, this.getSecond() );
        } else {
            final Second previous = ( Second ) this.getSecond().previous();
            if ( previous != null ) {
                result = new Millisecond ( 999, previous );
            }
        }
        return result;
    }
    @Override
    public RegularTimePeriod next() {
        RegularTimePeriod result = null;
        if ( this.millisecond != 999 ) {
            result = new Millisecond ( this.millisecond + 1, this.getSecond() );
        } else {
            final Second next = ( Second ) this.getSecond().next();
            if ( next != null ) {
                result = new Millisecond ( 0, next );
            }
        }
        return result;
    }
    @Override
    public long getSerialIndex() {
        final long hourIndex = this.day.getSerialIndex() * 24L + this.hour;
        final long minuteIndex = hourIndex * 60L + this.minute;
        final long secondIndex = minuteIndex * 60L + this.second;
        return secondIndex * 1000L + this.millisecond;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof Millisecond ) ) {
            return false;
        }
        final Millisecond that = ( Millisecond ) obj;
        return this.millisecond == that.millisecond && this.second == that.second && this.minute == that.minute && this.hour == that.hour && this.day.equals ( that.day );
    }
    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + this.millisecond;
        result = 37 * result + this.getSecond().hashCode();
        return result;
    }
    @Override
    public int compareTo ( final Object obj ) {
        int result;
        if ( obj instanceof Millisecond ) {
            final Millisecond ms = ( Millisecond ) obj;
            final long difference = this.getFirstMillisecond() - ms.getFirstMillisecond();
            if ( difference > 0L ) {
                result = 1;
            } else if ( difference < 0L ) {
                result = -1;
            } else {
                result = 0;
            }
        } else if ( obj instanceof RegularTimePeriod ) {
            final RegularTimePeriod rtp = ( RegularTimePeriod ) obj;
            final long thisVal = this.getFirstMillisecond();
            final long anotherVal = rtp.getFirstMillisecond();
            result = ( ( thisVal < anotherVal ) ? -1 : ( ( thisVal == anotherVal ) ? 0 : 1 ) );
        } else {
            result = 1;
        }
        return result;
    }
    @Override
    public long getFirstMillisecond ( final Calendar calendar ) {
        final int year = this.day.getYear();
        final int month = this.day.getMonth() - 1;
        final int d = this.day.getDayOfMonth();
        calendar.clear();
        calendar.set ( year, month, d, this.hour, this.minute, this.second );
        calendar.set ( 14, this.millisecond );
        return calendar.getTimeInMillis();
    }
    @Override
    public long getLastMillisecond ( final Calendar calendar ) {
        return this.getFirstMillisecond ( calendar );
    }
}
