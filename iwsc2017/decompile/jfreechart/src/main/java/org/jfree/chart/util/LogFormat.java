package org.jfree.chart.util;
import java.text.ParsePosition;
import java.text.FieldPosition;
import java.text.DecimalFormat;
import java.text.NumberFormat;
public class LogFormat extends NumberFormat {
    private double base;
    private double baseLog;
    private String baseLabel;
    private String powerLabel;
    private boolean showBase;
    private NumberFormat formatter;
    public LogFormat() {
        this ( 10.0, "10", true );
    }
    public LogFormat ( final double base, final String baseLabel, final boolean showBase ) {
        this ( base, baseLabel, "^", showBase );
    }
    public LogFormat ( final double base, final String baseLabel, final String powerLabel, final boolean showBase ) {
        this.formatter = new DecimalFormat ( "0.0#" );
        ParamChecks.nullNotPermitted ( baseLabel, "baseLabel" );
        ParamChecks.nullNotPermitted ( powerLabel, "powerLabel" );
        this.base = base;
        this.baseLog = Math.log ( this.base );
        this.baseLabel = baseLabel;
        this.showBase = showBase;
        this.powerLabel = powerLabel;
    }
    public NumberFormat getExponentFormat() {
        return ( NumberFormat ) this.formatter.clone();
    }
    public void setExponentFormat ( final NumberFormat format ) {
        ParamChecks.nullNotPermitted ( format, "format" );
        this.formatter = format;
    }
    private double calculateLog ( final double value ) {
        return Math.log ( value ) / this.baseLog;
    }
    @Override
    public StringBuffer format ( final double number, final StringBuffer toAppendTo, final FieldPosition pos ) {
        final StringBuffer result = new StringBuffer();
        if ( this.showBase ) {
            result.append ( this.baseLabel );
            result.append ( this.powerLabel );
        }
        result.append ( this.formatter.format ( this.calculateLog ( number ) ) );
        return result;
    }
    @Override
    public StringBuffer format ( final long number, final StringBuffer toAppendTo, final FieldPosition pos ) {
        final StringBuffer result = new StringBuffer();
        if ( this.showBase ) {
            result.append ( this.baseLabel );
            result.append ( this.powerLabel );
        }
        result.append ( this.formatter.format ( this.calculateLog ( number ) ) );
        return result;
    }
    @Override
    public Number parse ( final String source, final ParsePosition parsePosition ) {
        return null;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof LogFormat ) ) {
            return false;
        }
        final LogFormat that = ( LogFormat ) obj;
        return this.base == that.base && this.baseLabel.equals ( that.baseLabel ) && this.baseLog == that.baseLog && this.showBase == that.showBase && this.formatter.equals ( that.formatter ) && super.equals ( obj );
    }
    @Override
    public Object clone() {
        final LogFormat clone = ( LogFormat ) super.clone();
        clone.formatter = ( NumberFormat ) this.formatter.clone();
        return clone;
    }
}
