package org.jfree.chart.util;
import java.text.ParsePosition;
import java.text.FieldPosition;
import java.text.DecimalFormat;
import java.util.GregorianCalendar;
import java.util.Date;
import java.text.NumberFormat;
import java.text.DateFormat;
public class RelativeDateFormat extends DateFormat {
    private long baseMillis;
    private boolean showZeroDays;
    private boolean showZeroHours;
    private NumberFormat dayFormatter;
    private String positivePrefix;
    private String daySuffix;
    private NumberFormat hourFormatter;
    private String hourSuffix;
    private NumberFormat minuteFormatter;
    private String minuteSuffix;
    private NumberFormat secondFormatter;
    private String secondSuffix;
    private static final long MILLISECONDS_IN_ONE_HOUR = 3600000L;
    private static final long MILLISECONDS_IN_ONE_DAY = 86400000L;
    public RelativeDateFormat() {
        this ( 0L );
    }
    public RelativeDateFormat ( final Date time ) {
        this ( time.getTime() );
    }
    public RelativeDateFormat ( final long baseMillis ) {
        this.baseMillis = baseMillis;
        this.showZeroDays = false;
        this.showZeroHours = true;
        this.positivePrefix = "";
        this.dayFormatter = NumberFormat.getNumberInstance();
        this.daySuffix = "d";
        this.hourFormatter = NumberFormat.getNumberInstance();
        this.hourSuffix = "h";
        this.minuteFormatter = NumberFormat.getNumberInstance();
        this.minuteSuffix = "m";
        ( this.secondFormatter = NumberFormat.getNumberInstance() ).setMaximumFractionDigits ( 3 );
        this.secondFormatter.setMinimumFractionDigits ( 3 );
        this.secondSuffix = "s";
        this.calendar = new GregorianCalendar();
        this.numberFormat = new DecimalFormat ( "0" );
    }
    public long getBaseMillis() {
        return this.baseMillis;
    }
    public void setBaseMillis ( final long baseMillis ) {
        this.baseMillis = baseMillis;
    }
    public boolean getShowZeroDays() {
        return this.showZeroDays;
    }
    public void setShowZeroDays ( final boolean show ) {
        this.showZeroDays = show;
    }
    public boolean getShowZeroHours() {
        return this.showZeroHours;
    }
    public void setShowZeroHours ( final boolean show ) {
        this.showZeroHours = show;
    }
    public String getPositivePrefix() {
        return this.positivePrefix;
    }
    public void setPositivePrefix ( final String prefix ) {
        ParamChecks.nullNotPermitted ( prefix, "prefix" );
        this.positivePrefix = prefix;
    }
    public void setDayFormatter ( final NumberFormat formatter ) {
        ParamChecks.nullNotPermitted ( formatter, "formatter" );
        this.dayFormatter = formatter;
    }
    public String getDaySuffix() {
        return this.daySuffix;
    }
    public void setDaySuffix ( final String suffix ) {
        ParamChecks.nullNotPermitted ( suffix, "suffix" );
        this.daySuffix = suffix;
    }
    public void setHourFormatter ( final NumberFormat formatter ) {
        ParamChecks.nullNotPermitted ( formatter, "formatter" );
        this.hourFormatter = formatter;
    }
    public String getHourSuffix() {
        return this.hourSuffix;
    }
    public void setHourSuffix ( final String suffix ) {
        ParamChecks.nullNotPermitted ( suffix, "suffix" );
        this.hourSuffix = suffix;
    }
    public void setMinuteFormatter ( final NumberFormat formatter ) {
        ParamChecks.nullNotPermitted ( formatter, "formatter" );
        this.minuteFormatter = formatter;
    }
    public String getMinuteSuffix() {
        return this.minuteSuffix;
    }
    public void setMinuteSuffix ( final String suffix ) {
        ParamChecks.nullNotPermitted ( suffix, "suffix" );
        this.minuteSuffix = suffix;
    }
    public String getSecondSuffix() {
        return this.secondSuffix;
    }
    public void setSecondSuffix ( final String suffix ) {
        ParamChecks.nullNotPermitted ( suffix, "suffix" );
        this.secondSuffix = suffix;
    }
    public void setSecondFormatter ( final NumberFormat formatter ) {
        ParamChecks.nullNotPermitted ( formatter, "formatter" );
        this.secondFormatter = formatter;
    }
    @Override
    public StringBuffer format ( final Date date, final StringBuffer toAppendTo, final FieldPosition fieldPosition ) {
        final long currentMillis = date.getTime();
        long elapsed = currentMillis - this.baseMillis;
        String signPrefix;
        if ( elapsed < 0L ) {
            elapsed *= -1L;
            signPrefix = "-";
        } else {
            signPrefix = this.positivePrefix;
        }
        final long days = elapsed / 86400000L;
        elapsed -= days * 86400000L;
        final long hours = elapsed / 3600000L;
        elapsed -= hours * 3600000L;
        final long minutes = elapsed / 60000L;
        elapsed -= minutes * 60000L;
        final double seconds = elapsed / 1000.0;
        toAppendTo.append ( signPrefix );
        if ( days != 0L || this.showZeroDays ) {
            toAppendTo.append ( this.dayFormatter.format ( days ) ).append ( this.getDaySuffix() );
        }
        if ( hours != 0L || this.showZeroHours ) {
            toAppendTo.append ( this.hourFormatter.format ( hours ) ).append ( this.getHourSuffix() );
        }
        toAppendTo.append ( this.minuteFormatter.format ( minutes ) ).append ( this.getMinuteSuffix() );
        toAppendTo.append ( this.secondFormatter.format ( seconds ) ).append ( this.getSecondSuffix() );
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
        if ( ! ( obj instanceof RelativeDateFormat ) ) {
            return false;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        final RelativeDateFormat that = ( RelativeDateFormat ) obj;
        return this.baseMillis == that.baseMillis && this.showZeroDays == that.showZeroDays && this.showZeroHours == that.showZeroHours && this.positivePrefix.equals ( that.positivePrefix ) && this.daySuffix.equals ( that.daySuffix ) && this.hourSuffix.equals ( that.hourSuffix ) && this.minuteSuffix.equals ( that.minuteSuffix ) && this.secondSuffix.equals ( that.secondSuffix ) && this.dayFormatter.equals ( that.dayFormatter ) && this.hourFormatter.equals ( that.hourFormatter ) && this.minuteFormatter.equals ( that.minuteFormatter ) && this.secondFormatter.equals ( that.secondFormatter );
    }
    @Override
    public int hashCode() {
        int result = 193;
        result = 37 * result + ( int ) ( this.baseMillis ^ this.baseMillis >>> 32 );
        result = 37 * result + this.positivePrefix.hashCode();
        result = 37 * result + this.daySuffix.hashCode();
        result = 37 * result + this.hourSuffix.hashCode();
        result = 37 * result + this.minuteSuffix.hashCode();
        result = 37 * result + this.secondSuffix.hashCode();
        result = 37 * result + this.secondFormatter.hashCode();
        return result;
    }
    @Override
    public Object clone() {
        final RelativeDateFormat clone = ( RelativeDateFormat ) super.clone();
        clone.dayFormatter = ( NumberFormat ) this.dayFormatter.clone();
        clone.secondFormatter = ( NumberFormat ) this.secondFormatter.clone();
        return clone;
    }
}
