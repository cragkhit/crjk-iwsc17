package org.jfree.data.xy;
public abstract class AbstractIntervalXYDataset extends AbstractXYDataset implements IntervalXYDataset {
    @Override
    public double getStartXValue ( final int series, final int item ) {
        double result = Double.NaN;
        final Number x = this.getStartX ( series, item );
        if ( x != null ) {
            result = x.doubleValue();
        }
        return result;
    }
    @Override
    public double getEndXValue ( final int series, final int item ) {
        double result = Double.NaN;
        final Number x = this.getEndX ( series, item );
        if ( x != null ) {
            result = x.doubleValue();
        }
        return result;
    }
    @Override
    public double getStartYValue ( final int series, final int item ) {
        double result = Double.NaN;
        final Number y = this.getStartY ( series, item );
        if ( y != null ) {
            result = y.doubleValue();
        }
        return result;
    }
    @Override
    public double getEndYValue ( final int series, final int item ) {
        double result = Double.NaN;
        final Number y = this.getEndY ( series, item );
        if ( y != null ) {
            result = y.doubleValue();
        }
        return result;
    }
}
