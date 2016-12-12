package org.jfree.data.xy;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.DefaultKeyedValues2D;
import org.jfree.util.PublicCloneable;
import org.jfree.data.DomainInfo;
public class CategoryTableXYDataset extends AbstractIntervalXYDataset implements TableXYDataset, IntervalXYDataset, DomainInfo, PublicCloneable {
    private DefaultKeyedValues2D values;
    private IntervalXYDelegate intervalDelegate;
    public CategoryTableXYDataset() {
        this.values = new DefaultKeyedValues2D ( true );
        this.addChangeListener ( this.intervalDelegate = new IntervalXYDelegate ( this ) );
    }
    public void add ( final double x, final double y, final String seriesName ) {
        this.add ( new Double ( x ), new Double ( y ), seriesName, true );
    }
    public void add ( final Number x, final Number y, final String seriesName, final boolean notify ) {
        this.values.addValue ( y, ( Comparable ) x, seriesName );
        if ( notify ) {
            this.fireDatasetChanged();
        }
    }
    public void remove ( final double x, final String seriesName ) {
        this.remove ( new Double ( x ), seriesName, true );
    }
    public void remove ( final Number x, final String seriesName, final boolean notify ) {
        this.values.removeValue ( ( Comparable ) x, seriesName );
        if ( notify ) {
            this.fireDatasetChanged();
        }
    }
    public void clear() {
        this.values.clear();
        this.fireDatasetChanged();
    }
    public int getSeriesCount() {
        return this.values.getColumnCount();
    }
    public Comparable getSeriesKey ( final int series ) {
        return this.values.getColumnKey ( series );
    }
    @Override
    public int getItemCount() {
        return this.values.getRowCount();
    }
    public int getItemCount ( final int series ) {
        return this.getItemCount();
    }
    public Number getX ( final int series, final int item ) {
        return ( Number ) this.values.getRowKey ( item );
    }
    @Override
    public Number getStartX ( final int series, final int item ) {
        return this.intervalDelegate.getStartX ( series, item );
    }
    @Override
    public Number getEndX ( final int series, final int item ) {
        return this.intervalDelegate.getEndX ( series, item );
    }
    public Number getY ( final int series, final int item ) {
        return this.values.getValue ( item, series );
    }
    @Override
    public Number getStartY ( final int series, final int item ) {
        return this.getY ( series, item );
    }
    @Override
    public Number getEndY ( final int series, final int item ) {
        return this.getY ( series, item );
    }
    @Override
    public double getDomainLowerBound ( final boolean includeInterval ) {
        return this.intervalDelegate.getDomainLowerBound ( includeInterval );
    }
    @Override
    public double getDomainUpperBound ( final boolean includeInterval ) {
        return this.intervalDelegate.getDomainUpperBound ( includeInterval );
    }
    @Override
    public Range getDomainBounds ( final boolean includeInterval ) {
        if ( includeInterval ) {
            return this.intervalDelegate.getDomainBounds ( includeInterval );
        }
        return DatasetUtilities.iterateDomainBounds ( this, includeInterval );
    }
    public double getIntervalPositionFactor() {
        return this.intervalDelegate.getIntervalPositionFactor();
    }
    public void setIntervalPositionFactor ( final double d ) {
        this.intervalDelegate.setIntervalPositionFactor ( d );
        this.fireDatasetChanged();
    }
    public double getIntervalWidth() {
        return this.intervalDelegate.getIntervalWidth();
    }
    public void setIntervalWidth ( final double d ) {
        this.intervalDelegate.setFixedIntervalWidth ( d );
        this.fireDatasetChanged();
    }
    public boolean isAutoWidth() {
        return this.intervalDelegate.isAutoWidth();
    }
    public void setAutoWidth ( final boolean b ) {
        this.intervalDelegate.setAutoWidth ( b );
        this.fireDatasetChanged();
    }
    public boolean equals ( final Object obj ) {
        if ( ! ( obj instanceof CategoryTableXYDataset ) ) {
            return false;
        }
        final CategoryTableXYDataset that = ( CategoryTableXYDataset ) obj;
        return this.intervalDelegate.equals ( that.intervalDelegate ) && this.values.equals ( that.values );
    }
    public Object clone() throws CloneNotSupportedException {
        final CategoryTableXYDataset clone = ( CategoryTableXYDataset ) super.clone();
        clone.values = ( DefaultKeyedValues2D ) this.values.clone();
        ( clone.intervalDelegate = new IntervalXYDelegate ( clone ) ).setFixedIntervalWidth ( this.getIntervalWidth() );
        clone.intervalDelegate.setAutoWidth ( this.isAutoWidth() );
        clone.intervalDelegate.setIntervalPositionFactor ( this.getIntervalPositionFactor() );
        return clone;
    }
}
