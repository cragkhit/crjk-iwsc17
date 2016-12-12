package org.jfree.chart.axis;
import java.util.Arrays;
import java.text.ParsePosition;
import java.text.FieldPosition;
import java.util.Date;
import java.text.NumberFormat;
import java.util.GregorianCalendar;
import org.jfree.chart.util.ParamChecks;
import java.util.TimeZone;
import java.io.Serializable;
import java.text.DateFormat;
public class QuarterDateFormat extends DateFormat implements Cloneable, Serializable {
    private static final long serialVersionUID = -6738465248529797176L;
    public static final String[] REGULAR_QUARTERS;
    public static final String[] ROMAN_QUARTERS;
    public static final String[] GREEK_QUARTERS;
    private String[] quarters;
    private boolean quarterFirst;
    public QuarterDateFormat() {
        this ( TimeZone.getDefault() );
    }
    public QuarterDateFormat ( final TimeZone zone ) {
        this ( zone, QuarterDateFormat.REGULAR_QUARTERS );
    }
    public QuarterDateFormat ( final TimeZone zone, final String[] quarterSymbols ) {
        this ( zone, quarterSymbols, false );
    }
    public QuarterDateFormat ( final TimeZone zone, final String[] quarterSymbols, final boolean quarterFirst ) {
        this.quarters = QuarterDateFormat.REGULAR_QUARTERS;
        ParamChecks.nullNotPermitted ( zone, "zone" );
        this.calendar = new GregorianCalendar ( zone );
        this.quarters = quarterSymbols;
        this.quarterFirst = quarterFirst;
        this.numberFormat = NumberFormat.getNumberInstance();
    }
    @Override
    public StringBuffer format ( final Date date, final StringBuffer toAppendTo, final FieldPosition fieldPosition ) {
        this.calendar.setTime ( date );
        final int year = this.calendar.get ( 1 );
        final int month = this.calendar.get ( 2 );
        final int quarter = month / 3;
        if ( this.quarterFirst ) {
            toAppendTo.append ( this.quarters[quarter] );
            toAppendTo.append ( " " );
            toAppendTo.append ( year );
        } else {
            toAppendTo.append ( year );
            toAppendTo.append ( " " );
            toAppendTo.append ( this.quarters[quarter] );
        }
        return toAppendTo;
    }
    @Override
    public Date parse ( final String source, final ParsePosition pos ) {
        return null;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof QuarterDateFormat ) ) {
            return false;
        }
        final QuarterDateFormat that = ( QuarterDateFormat ) obj;
        return Arrays.equals ( this.quarters, that.quarters ) && this.quarterFirst == that.quarterFirst && super.equals ( obj );
    }
    static {
        REGULAR_QUARTERS = new String[] { "1", "2", "3", "4" };
        ROMAN_QUARTERS = new String[] { "I", "II", "III", "IV" };
        GREEK_QUARTERS = new String[] { "\u0391", "\u0392", "\u0393", "\u0394" };
    }
}
