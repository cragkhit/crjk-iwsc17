package org.jfree.chart.axis;
import java.util.Arrays;
import java.text.ParsePosition;
import java.text.FieldPosition;
import java.util.Date;
import java.text.NumberFormat;
import java.util.GregorianCalendar;
import java.text.DateFormatSymbols;
import org.jfree.chart.util.ParamChecks;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.text.DateFormat;
public class MonthDateFormat extends DateFormat {
    private String[] months;
    private boolean[] showYear;
    private DateFormat yearFormatter;
    public MonthDateFormat() {
        this ( TimeZone.getDefault() );
    }
    public MonthDateFormat ( final TimeZone zone ) {
        this ( zone, Locale.getDefault(), 1, true, false );
    }
    public MonthDateFormat ( final Locale locale ) {
        this ( TimeZone.getDefault(), locale, 1, true, false );
    }
    public MonthDateFormat ( final TimeZone zone, final int chars ) {
        this ( zone, Locale.getDefault(), chars, true, false );
    }
    public MonthDateFormat ( final Locale locale, final int chars ) {
        this ( TimeZone.getDefault(), locale, chars, true, false );
    }
    public MonthDateFormat ( final TimeZone zone, final Locale locale, final int chars, final boolean showYearForJan, final boolean showYearForDec ) {
        this ( zone, locale, chars, new boolean[] { showYearForJan, false, false, false, false, false, false, false, false, false, false, false, showYearForDec }, new SimpleDateFormat ( "yy" ) );
    }
    public MonthDateFormat ( final TimeZone zone, final Locale locale, final int chars, final boolean[] showYear, final DateFormat yearFormatter ) {
        ParamChecks.nullNotPermitted ( locale, "locale" );
        final DateFormatSymbols dfs = new DateFormatSymbols ( locale );
        final String[] monthsFromLocale = dfs.getMonths();
        this.months = new String[12];
        for ( int i = 0; i < 12; ++i ) {
            if ( chars > 0 ) {
                this.months[i] = monthsFromLocale[i].substring ( 0, Math.min ( chars, monthsFromLocale[i].length() ) );
            } else {
                this.months[i] = monthsFromLocale[i];
            }
        }
        this.calendar = new GregorianCalendar ( zone );
        this.showYear = showYear;
        this.yearFormatter = yearFormatter;
        this.numberFormat = NumberFormat.getNumberInstance();
    }
    @Override
    public StringBuffer format ( final Date date, final StringBuffer toAppendTo, final FieldPosition fieldPosition ) {
        this.calendar.setTime ( date );
        final int month = this.calendar.get ( 2 );
        toAppendTo.append ( this.months[month] );
        if ( this.showYear[month] ) {
            toAppendTo.append ( this.yearFormatter.format ( date ) );
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
        if ( ! ( obj instanceof MonthDateFormat ) ) {
            return false;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        final MonthDateFormat that = ( MonthDateFormat ) obj;
        return Arrays.equals ( this.months, that.months ) && Arrays.equals ( this.showYear, that.showYear ) && this.yearFormatter.equals ( that.yearFormatter );
    }
}
