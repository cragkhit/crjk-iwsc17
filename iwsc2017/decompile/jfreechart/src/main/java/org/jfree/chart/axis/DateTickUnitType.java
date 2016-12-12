package org.jfree.chart.axis;
import java.io.ObjectStreamException;
import java.io.Serializable;
public class DateTickUnitType implements Serializable {
    public static final DateTickUnitType YEAR;
    public static final DateTickUnitType MONTH;
    public static final DateTickUnitType DAY;
    public static final DateTickUnitType HOUR;
    public static final DateTickUnitType MINUTE;
    public static final DateTickUnitType SECOND;
    public static final DateTickUnitType MILLISECOND;
    private String name;
    private int calendarField;
    private DateTickUnitType ( final String name, final int calendarField ) {
        this.name = name;
        this.calendarField = calendarField;
    }
    public int getCalendarField() {
        return this.calendarField;
    }
    @Override
    public String toString() {
        return this.name;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( ! ( obj instanceof DateTickUnitType ) ) {
            return false;
        }
        final DateTickUnitType t = ( DateTickUnitType ) obj;
        return this.name.equals ( t.toString() );
    }
    private Object readResolve() throws ObjectStreamException {
        if ( this.equals ( DateTickUnitType.YEAR ) ) {
            return DateTickUnitType.YEAR;
        }
        if ( this.equals ( DateTickUnitType.MONTH ) ) {
            return DateTickUnitType.MONTH;
        }
        if ( this.equals ( DateTickUnitType.DAY ) ) {
            return DateTickUnitType.DAY;
        }
        if ( this.equals ( DateTickUnitType.HOUR ) ) {
            return DateTickUnitType.HOUR;
        }
        if ( this.equals ( DateTickUnitType.MINUTE ) ) {
            return DateTickUnitType.MINUTE;
        }
        if ( this.equals ( DateTickUnitType.SECOND ) ) {
            return DateTickUnitType.SECOND;
        }
        if ( this.equals ( DateTickUnitType.MILLISECOND ) ) {
            return DateTickUnitType.MILLISECOND;
        }
        return null;
    }
    static {
        YEAR = new DateTickUnitType ( "DateTickUnitType.YEAR", 1 );
        MONTH = new DateTickUnitType ( "DateTickUnitType.MONTH", 2 );
        DAY = new DateTickUnitType ( "DateTickUnitType.DAY", 5 );
        HOUR = new DateTickUnitType ( "DateTickUnitType.HOUR", 11 );
        MINUTE = new DateTickUnitType ( "DateTickUnitType.MINUTE", 12 );
        SECOND = new DateTickUnitType ( "DateTickUnitType.SECOND", 13 );
        MILLISECOND = new DateTickUnitType ( "DateTickUnitType.MILLISECOND", 14 );
    }
}
