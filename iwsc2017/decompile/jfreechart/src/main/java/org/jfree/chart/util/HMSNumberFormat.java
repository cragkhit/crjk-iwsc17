package org.jfree.chart.util;
import java.text.ParsePosition;
import java.text.FieldPosition;
import java.text.DecimalFormat;
import java.text.NumberFormat;
public class HMSNumberFormat extends NumberFormat {
    private NumberFormat formatter;
    public HMSNumberFormat() {
        this.formatter = new DecimalFormat ( "00" );
    }
    @Override
    public StringBuffer format ( final double number, final StringBuffer toAppendTo, final FieldPosition pos ) {
        return this.format ( ( long ) number, toAppendTo, pos );
    }
    @Override
    public StringBuffer format ( final long number, final StringBuffer toAppendTo, final FieldPosition pos ) {
        final StringBuffer sb = new StringBuffer();
        final long hours = number / 3600L;
        sb.append ( this.formatter.format ( hours ) ).append ( ":" );
        final long remaining = number - hours * 3600L;
        final long minutes = remaining / 60L;
        sb.append ( this.formatter.format ( minutes ) ).append ( ":" );
        final long seconds = remaining - minutes * 60L;
        sb.append ( this.formatter.format ( seconds ) );
        return sb;
    }
    @Override
    public Number parse ( final String source, final ParsePosition parsePosition ) {
        return null;
    }
}
