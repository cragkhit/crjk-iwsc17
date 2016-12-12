package org.jfree.chart.axis;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.io.Serializable;
public class StandardTickUnitSource implements TickUnitSource, Serializable {
    private static final double LOG_10_VALUE;
    @Override
    public TickUnit getLargerTickUnit ( final TickUnit unit ) {
        final double x = unit.getSize();
        final double log = Math.log ( x ) / StandardTickUnitSource.LOG_10_VALUE;
        final double higher = Math.ceil ( log );
        return new NumberTickUnit ( Math.pow ( 10.0, higher ), new DecimalFormat ( "0.0E0" ) );
    }
    @Override
    public TickUnit getCeilingTickUnit ( final TickUnit unit ) {
        return this.getLargerTickUnit ( unit );
    }
    @Override
    public TickUnit getCeilingTickUnit ( final double size ) {
        final double log = Math.log ( size ) / StandardTickUnitSource.LOG_10_VALUE;
        final double higher = Math.ceil ( log );
        return new NumberTickUnit ( Math.pow ( 10.0, higher ), new DecimalFormat ( "0.0E0" ) );
    }
    @Override
    public boolean equals ( final Object obj ) {
        return obj == this || obj instanceof StandardTickUnitSource;
    }
    @Override
    public int hashCode() {
        return 0;
    }
    static {
        LOG_10_VALUE = Math.log ( 10.0 );
    }
}
