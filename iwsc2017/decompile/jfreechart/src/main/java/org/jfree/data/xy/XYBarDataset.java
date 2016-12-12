package org.jfree.data.xy;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.util.PublicCloneable;
import org.jfree.data.general.DatasetChangeListener;
public class XYBarDataset extends AbstractIntervalXYDataset implements IntervalXYDataset, DatasetChangeListener, PublicCloneable {
    private XYDataset underlying;
    private double barWidth;
    public XYBarDataset ( final XYDataset underlying, final double barWidth ) {
        ( this.underlying = underlying ).addChangeListener ( this );
        this.barWidth = barWidth;
    }
    public XYDataset getUnderlyingDataset() {
        return this.underlying;
    }
    public double getBarWidth() {
        return this.barWidth;
    }
    public void setBarWidth ( final double barWidth ) {
        this.barWidth = barWidth;
        this.notifyListeners ( new DatasetChangeEvent ( this, this ) );
    }
    public int getSeriesCount() {
        return this.underlying.getSeriesCount();
    }
    public Comparable getSeriesKey ( final int series ) {
        return this.underlying.getSeriesKey ( series );
    }
    public int getItemCount ( final int series ) {
        return this.underlying.getItemCount ( series );
    }
    public Number getX ( final int series, final int item ) {
        return this.underlying.getX ( series, item );
    }
    public double getXValue ( final int series, final int item ) {
        return this.underlying.getXValue ( series, item );
    }
    public Number getY ( final int series, final int item ) {
        return this.underlying.getY ( series, item );
    }
    public double getYValue ( final int series, final int item ) {
        return this.underlying.getYValue ( series, item );
    }
    @Override
    public Number getStartX ( final int series, final int item ) {
        Number result = null;
        final Number xnum = this.underlying.getX ( series, item );
        if ( xnum != null ) {
            result = new Double ( xnum.doubleValue() - this.barWidth / 2.0 );
        }
        return result;
    }
    @Override
    public double getStartXValue ( final int series, final int item ) {
        return this.getXValue ( series, item ) - this.barWidth / 2.0;
    }
    @Override
    public Number getEndX ( final int series, final int item ) {
        Number result = null;
        final Number xnum = this.underlying.getX ( series, item );
        if ( xnum != null ) {
            result = new Double ( xnum.doubleValue() + this.barWidth / 2.0 );
        }
        return result;
    }
    @Override
    public double getEndXValue ( final int series, final int item ) {
        return this.getXValue ( series, item ) + this.barWidth / 2.0;
    }
    @Override
    public Number getStartY ( final int series, final int item ) {
        return this.underlying.getY ( series, item );
    }
    @Override
    public double getStartYValue ( final int series, final int item ) {
        return this.getYValue ( series, item );
    }
    @Override
    public Number getEndY ( final int series, final int item ) {
        return this.underlying.getY ( series, item );
    }
    @Override
    public double getEndYValue ( final int series, final int item ) {
        return this.getYValue ( series, item );
    }
    @Override
    public void datasetChanged ( final DatasetChangeEvent event ) {
        this.notifyListeners ( event );
    }
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof XYBarDataset ) ) {
            return false;
        }
        final XYBarDataset that = ( XYBarDataset ) obj;
        return this.underlying.equals ( that.underlying ) && this.barWidth == that.barWidth;
    }
    public Object clone() throws CloneNotSupportedException {
        final XYBarDataset clone = ( XYBarDataset ) super.clone();
        if ( this.underlying instanceof PublicCloneable ) {
            final PublicCloneable pc = ( PublicCloneable ) this.underlying;
            clone.underlying = ( XYDataset ) pc.clone();
        }
        return clone;
    }
}
