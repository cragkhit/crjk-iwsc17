package org.jfree.data.xy;
import java.util.Collection;
import org.jfree.util.ObjectUtilities;
import org.jfree.data.general.SeriesChangeListener;
import org.jfree.chart.util.ParamChecks;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class YIntervalSeriesCollection extends AbstractIntervalXYDataset implements IntervalXYDataset, PublicCloneable, Serializable {
    private List data;
    public YIntervalSeriesCollection() {
        this.data = new ArrayList();
    }
    public void addSeries ( final YIntervalSeries series ) {
        ParamChecks.nullNotPermitted ( series, "series" );
        this.data.add ( series );
        series.addChangeListener ( this );
        this.fireDatasetChanged();
    }
    public int getSeriesCount() {
        return this.data.size();
    }
    public YIntervalSeries getSeries ( final int series ) {
        if ( series < 0 || series >= this.getSeriesCount() ) {
            throw new IllegalArgumentException ( "Series index out of bounds" );
        }
        return this.data.get ( series );
    }
    public Comparable getSeriesKey ( final int series ) {
        return this.getSeries ( series ).getKey();
    }
    public int getItemCount ( final int series ) {
        return this.getSeries ( series ).getItemCount();
    }
    public Number getX ( final int series, final int item ) {
        final YIntervalSeries s = this.data.get ( series );
        return s.getX ( item );
    }
    public double getYValue ( final int series, final int item ) {
        final YIntervalSeries s = this.data.get ( series );
        return s.getYValue ( item );
    }
    @Override
    public double getStartYValue ( final int series, final int item ) {
        final YIntervalSeries s = this.data.get ( series );
        return s.getYLowValue ( item );
    }
    @Override
    public double getEndYValue ( final int series, final int item ) {
        final YIntervalSeries s = this.data.get ( series );
        return s.getYHighValue ( item );
    }
    public Number getY ( final int series, final int item ) {
        final YIntervalSeries s = this.data.get ( series );
        return new Double ( s.getYValue ( item ) );
    }
    @Override
    public Number getStartX ( final int series, final int item ) {
        return this.getX ( series, item );
    }
    @Override
    public Number getEndX ( final int series, final int item ) {
        return this.getX ( series, item );
    }
    @Override
    public Number getStartY ( final int series, final int item ) {
        final YIntervalSeries s = this.data.get ( series );
        return new Double ( s.getYLowValue ( item ) );
    }
    @Override
    public Number getEndY ( final int series, final int item ) {
        final YIntervalSeries s = this.data.get ( series );
        return new Double ( s.getYHighValue ( item ) );
    }
    public void removeSeries ( final int series ) {
        if ( series < 0 || series >= this.getSeriesCount() ) {
            throw new IllegalArgumentException ( "Series index out of bounds." );
        }
        final YIntervalSeries ts = this.data.get ( series );
        ts.removeChangeListener ( this );
        this.data.remove ( series );
        this.fireDatasetChanged();
    }
    public void removeSeries ( final YIntervalSeries series ) {
        ParamChecks.nullNotPermitted ( series, "series" );
        if ( this.data.contains ( series ) ) {
            series.removeChangeListener ( this );
            this.data.remove ( series );
            this.fireDatasetChanged();
        }
    }
    public void removeAllSeries() {
        for ( int i = 0; i < this.data.size(); ++i ) {
            final YIntervalSeries series = this.data.get ( i );
            series.removeChangeListener ( this );
        }
        this.data.clear();
        this.fireDatasetChanged();
    }
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof YIntervalSeriesCollection ) ) {
            return false;
        }
        final YIntervalSeriesCollection that = ( YIntervalSeriesCollection ) obj;
        return ObjectUtilities.equal ( ( Object ) this.data, ( Object ) that.data );
    }
    public Object clone() throws CloneNotSupportedException {
        final YIntervalSeriesCollection clone = ( YIntervalSeriesCollection ) super.clone();
        clone.data = ( List ) ObjectUtilities.deepClone ( ( Collection ) this.data );
        return clone;
    }
}
