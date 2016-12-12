package org.jfree.data.time;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;
import org.jfree.date.MonthConstants;
public abstract class RegularTimePeriod implements TimePeriod, Comparable, MonthConstants {
    public static final TimeZone DEFAULT_TIME_ZONE;
    public static final Calendar WORKING_CALENDAR;
    public static RegularTimePeriod createInstance ( final Class c, final Date millisecond, final TimeZone zone ) {
        RegularTimePeriod result = null;
        try {
            final Constructor constructor = c.getDeclaredConstructor ( Date.class, TimeZone.class );
            result = constructor.newInstance ( millisecond, zone );
        } catch ( Exception ex ) {}
        return result;
    }
    public static Class downsize ( final Class c ) {
        if ( c.equals ( Year.class ) ) {
            return Quarter.class;
        }
        if ( c.equals ( Quarter.class ) ) {
            return Month.class;
        }
        if ( c.equals ( Month.class ) ) {
            return Day.class;
        }
        if ( c.equals ( Day.class ) ) {
            return Hour.class;
        }
        if ( c.equals ( Hour.class ) ) {
            return Minute.class;
        }
        if ( c.equals ( Minute.class ) ) {
            return Second.class;
        }
        if ( c.equals ( Second.class ) ) {
            return Millisecond.class;
        }
        return Millisecond.class;
    }
    public abstract RegularTimePeriod previous();
    public abstract RegularTimePeriod next();
    public abstract long getSerialIndex();
    public abstract void peg ( final Calendar p0 );
    @Override
    public Date getStart() {
        return new Date ( this.getFirstMillisecond() );
    }
    @Override
    public Date getEnd() {
        return new Date ( this.getLastMillisecond() );
    }
    public abstract long getFirstMillisecond();
    public long getFirstMillisecond ( final TimeZone zone ) {
        final Calendar calendar = Calendar.getInstance ( zone );
        return this.getFirstMillisecond ( calendar );
    }
    public abstract long getFirstMillisecond ( final Calendar p0 );
    public abstract long getLastMillisecond();
    public long getLastMillisecond ( final TimeZone zone ) {
        final Calendar calendar = Calendar.getInstance ( zone );
        return this.getLastMillisecond ( calendar );
    }
    public abstract long getLastMillisecond ( final Calendar p0 );
    public long getMiddleMillisecond() {
        final long m1 = this.getFirstMillisecond();
        final long m2 = this.getLastMillisecond();
        return m1 + ( m2 - m1 ) / 2L;
    }
    public long getMiddleMillisecond ( final TimeZone zone ) {
        final Calendar calendar = Calendar.getInstance ( zone );
        final long m1 = this.getFirstMillisecond ( calendar );
        final long m2 = this.getLastMillisecond ( calendar );
        return m1 + ( m2 - m1 ) / 2L;
    }
    public long getMiddleMillisecond ( final Calendar calendar ) {
        final long m1 = this.getFirstMillisecond ( calendar );
        final long m2 = this.getLastMillisecond ( calendar );
        return m1 + ( m2 - m1 ) / 2L;
    }
    public long getMillisecond ( final TimePeriodAnchor anchor, final Calendar calendar ) {
        if ( anchor.equals ( TimePeriodAnchor.START ) ) {
            return this.getFirstMillisecond ( calendar );
        }
        if ( anchor.equals ( TimePeriodAnchor.MIDDLE ) ) {
            return this.getMiddleMillisecond ( calendar );
        }
        if ( anchor.equals ( TimePeriodAnchor.END ) ) {
            return this.getLastMillisecond ( calendar );
        }
        throw new IllegalStateException ( "Unrecognised anchor: " + anchor );
    }
    @Override
    public String toString() {
        return String.valueOf ( this.getStart() );
    }
    static {
        DEFAULT_TIME_ZONE = TimeZone.getDefault();
        WORKING_CALENDAR = Calendar.getInstance ( RegularTimePeriod.DEFAULT_TIME_ZONE );
    }
}
