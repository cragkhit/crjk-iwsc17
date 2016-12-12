package org.jfree.chart.axis;
import org.jfree.util.ObjectUtilities;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Date;
import org.jfree.chart.util.ParamChecks;
import java.text.DateFormat;
import java.io.Serializable;
public class DateTickUnit extends TickUnit implements Serializable {
    private static final long serialVersionUID = -7289292157229621901L;
    private DateTickUnitType unitType;
    private int count;
    private DateTickUnitType rollUnitType;
    private int rollCount;
    private DateFormat formatter;
    public static final int YEAR = 0;
    public static final int MONTH = 1;
    public static final int DAY = 2;
    public static final int HOUR = 3;
    public static final int MINUTE = 4;
    public static final int SECOND = 5;
    public static final int MILLISECOND = 6;
    private int unit;
    private int rollUnit;
    public DateTickUnit ( final DateTickUnitType unitType, final int multiple ) {
        this ( unitType, multiple, DateFormat.getDateInstance ( 3 ) );
    }
    public DateTickUnit ( final DateTickUnitType unitType, final int multiple, final DateFormat formatter ) {
        this ( unitType, multiple, unitType, multiple, formatter );
    }
    public DateTickUnit ( final DateTickUnitType unitType, final int multiple, final DateTickUnitType rollUnitType, final int rollMultiple, final DateFormat formatter ) {
        super ( getMillisecondCount ( unitType, multiple ) );
        ParamChecks.nullNotPermitted ( formatter, "formatter" );
        if ( multiple <= 0 ) {
            throw new IllegalArgumentException ( "Requires 'multiple' > 0." );
        }
        if ( rollMultiple <= 0 ) {
            throw new IllegalArgumentException ( "Requires 'rollMultiple' > 0." );
        }
        this.unitType = unitType;
        this.count = multiple;
        this.rollUnitType = rollUnitType;
        this.rollCount = rollMultiple;
        this.formatter = formatter;
        this.unit = unitTypeToInt ( unitType );
        this.rollUnit = unitTypeToInt ( rollUnitType );
    }
    public DateTickUnitType getUnitType() {
        return this.unitType;
    }
    public int getMultiple() {
        return this.count;
    }
    public DateTickUnitType getRollUnitType() {
        return this.rollUnitType;
    }
    public int getRollMultiple() {
        return this.rollCount;
    }
    @Override
    public String valueToString ( final double milliseconds ) {
        return this.formatter.format ( new Date ( ( long ) milliseconds ) );
    }
    public String dateToString ( final Date date ) {
        return this.formatter.format ( date );
    }
    public Date addToDate ( final Date base, final TimeZone zone ) {
        final Calendar calendar = Calendar.getInstance ( zone );
        calendar.setTime ( base );
        calendar.add ( this.unitType.getCalendarField(), this.count );
        return calendar.getTime();
    }
    public Date rollDate ( final Date base ) {
        return this.rollDate ( base, TimeZone.getDefault() );
    }
    public Date rollDate ( final Date base, final TimeZone zone ) {
        final Calendar calendar = Calendar.getInstance ( zone );
        calendar.setTime ( base );
        calendar.add ( this.rollUnitType.getCalendarField(), this.rollCount );
        return calendar.getTime();
    }
    public int getCalendarField() {
        return this.unitType.getCalendarField();
    }
    private static long getMillisecondCount ( final DateTickUnitType unit, final int count ) {
        if ( unit.equals ( DateTickUnitType.YEAR ) ) {
            return 31536000000L * count;
        }
        if ( unit.equals ( DateTickUnitType.MONTH ) ) {
            return 2678400000L * count;
        }
        if ( unit.equals ( DateTickUnitType.DAY ) ) {
            return 86400000L * count;
        }
        if ( unit.equals ( DateTickUnitType.HOUR ) ) {
            return 3600000L * count;
        }
        if ( unit.equals ( DateTickUnitType.MINUTE ) ) {
            return 60000L * count;
        }
        if ( unit.equals ( DateTickUnitType.SECOND ) ) {
            return 1000L * count;
        }
        if ( unit.equals ( DateTickUnitType.MILLISECOND ) ) {
            return count;
        }
        throw new IllegalArgumentException ( "The 'unit' argument has a value that is not recognised." );
    }
    private static DateTickUnitType intToUnitType ( final int unit ) {
        switch ( unit ) {
        case 0: {
            return DateTickUnitType.YEAR;
        }
        case 1: {
            return DateTickUnitType.MONTH;
        }
        case 2: {
            return DateTickUnitType.DAY;
        }
        case 3: {
            return DateTickUnitType.HOUR;
        }
        case 4: {
            return DateTickUnitType.MINUTE;
        }
        case 5: {
            return DateTickUnitType.SECOND;
        }
        case 6: {
            return DateTickUnitType.MILLISECOND;
        }
        default: {
            throw new IllegalArgumentException ( "Unrecognised 'unit' value " + unit + "." );
        }
        }
    }
    private static int unitTypeToInt ( final DateTickUnitType unitType ) {
        ParamChecks.nullNotPermitted ( unitType, "unitType" );
        if ( unitType.equals ( DateTickUnitType.YEAR ) ) {
            return 0;
        }
        if ( unitType.equals ( DateTickUnitType.MONTH ) ) {
            return 1;
        }
        if ( unitType.equals ( DateTickUnitType.DAY ) ) {
            return 2;
        }
        if ( unitType.equals ( DateTickUnitType.HOUR ) ) {
            return 3;
        }
        if ( unitType.equals ( DateTickUnitType.MINUTE ) ) {
            return 4;
        }
        if ( unitType.equals ( DateTickUnitType.SECOND ) ) {
            return 5;
        }
        if ( unitType.equals ( DateTickUnitType.MILLISECOND ) ) {
            return 6;
        }
        throw new IllegalArgumentException ( "The 'unitType' is not recognised" );
    }
    private static DateFormat notNull ( final DateFormat formatter ) {
        if ( formatter == null ) {
            return DateFormat.getDateInstance ( 3 );
        }
        return formatter;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof DateTickUnit ) ) {
            return false;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        final DateTickUnit that = ( DateTickUnit ) obj;
        return this.unitType.equals ( that.unitType ) && this.count == that.count && ObjectUtilities.equal ( ( Object ) this.formatter, ( Object ) that.formatter );
    }
    @Override
    public int hashCode() {
        int result = 19;
        result = 37 * result + this.unitType.hashCode();
        result = 37 * result + this.count;
        result = 37 * result + this.formatter.hashCode();
        return result;
    }
    @Override
    public String toString() {
        return "DateTickUnit[" + this.unitType.toString() + ", " + this.count + "]";
    }
    public DateTickUnit ( final int unit, final int count, final DateFormat formatter ) {
        this ( unit, count, unit, count, formatter );
    }
    public DateTickUnit ( final int unit, final int count ) {
        this ( unit, count, null );
    }
    public DateTickUnit ( final int unit, final int count, final int rollUnit, final int rollCount, final DateFormat formatter ) {
        this ( intToUnitType ( unit ), count, intToUnitType ( rollUnit ), rollCount, notNull ( formatter ) );
    }
    public int getUnit() {
        return this.unit;
    }
    public int getCount() {
        return this.count;
    }
    public int getRollUnit() {
        return this.rollUnit;
    }
    public int getRollCount() {
        return this.rollCount;
    }
    public Date addToDate ( final Date base ) {
        return this.addToDate ( base, TimeZone.getDefault() );
    }
}
