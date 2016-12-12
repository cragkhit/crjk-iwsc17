

package org.jfree.data.xy;

import java.io.Serializable;
import java.util.List;
import org.jfree.chart.util.ParamChecks;

import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PublicCloneable;


public class YIntervalSeriesCollection extends AbstractIntervalXYDataset
    implements IntervalXYDataset, PublicCloneable, Serializable {


    private List data;


    public YIntervalSeriesCollection() {
        this.data = new java.util.ArrayList();
    }


    public void addSeries ( YIntervalSeries series ) {
        ParamChecks.nullNotPermitted ( series, "series" );
        this.data.add ( series );
        series.addChangeListener ( this );
        fireDatasetChanged();
    }


    @Override
    public int getSeriesCount() {
        return this.data.size();
    }


    public YIntervalSeries getSeries ( int series ) {
        if ( ( series < 0 ) || ( series >= getSeriesCount() ) ) {
            throw new IllegalArgumentException ( "Series index out of bounds" );
        }
        return ( YIntervalSeries ) this.data.get ( series );
    }


    @Override
    public Comparable getSeriesKey ( int series ) {
        return getSeries ( series ).getKey();
    }


    @Override
    public int getItemCount ( int series ) {
        return getSeries ( series ).getItemCount();
    }


    @Override
    public Number getX ( int series, int item ) {
        YIntervalSeries s = ( YIntervalSeries ) this.data.get ( series );
        return s.getX ( item );
    }


    @Override
    public double getYValue ( int series, int item ) {
        YIntervalSeries s = ( YIntervalSeries ) this.data.get ( series );
        return s.getYValue ( item );
    }


    @Override
    public double getStartYValue ( int series, int item ) {
        YIntervalSeries s = ( YIntervalSeries ) this.data.get ( series );
        return s.getYLowValue ( item );
    }


    @Override
    public double getEndYValue ( int series, int item ) {
        YIntervalSeries s = ( YIntervalSeries ) this.data.get ( series );
        return s.getYHighValue ( item );
    }


    @Override
    public Number getY ( int series, int item ) {
        YIntervalSeries s = ( YIntervalSeries ) this.data.get ( series );
        return new Double ( s.getYValue ( item ) );
    }


    @Override
    public Number getStartX ( int series, int item ) {
        return getX ( series, item );
    }


    @Override
    public Number getEndX ( int series, int item ) {
        return getX ( series, item );
    }


    @Override
    public Number getStartY ( int series, int item ) {
        YIntervalSeries s = ( YIntervalSeries ) this.data.get ( series );
        return new Double ( s.getYLowValue ( item ) );
    }


    @Override
    public Number getEndY ( int series, int item ) {
        YIntervalSeries s = ( YIntervalSeries ) this.data.get ( series );
        return new Double ( s.getYHighValue ( item ) );
    }


    public void removeSeries ( int series ) {
        if ( ( series < 0 ) || ( series >= getSeriesCount() ) ) {
            throw new IllegalArgumentException ( "Series index out of bounds." );
        }
        YIntervalSeries ts = ( YIntervalSeries ) this.data.get ( series );
        ts.removeChangeListener ( this );
        this.data.remove ( series );
        fireDatasetChanged();
    }


    public void removeSeries ( YIntervalSeries series ) {
        ParamChecks.nullNotPermitted ( series, "series" );
        if ( this.data.contains ( series ) ) {
            series.removeChangeListener ( this );
            this.data.remove ( series );
            fireDatasetChanged();
        }
    }


    public void removeAllSeries() {
        for ( int i = 0; i < this.data.size(); i++ ) {
            YIntervalSeries series = ( YIntervalSeries ) this.data.get ( i );
            series.removeChangeListener ( this );
        }
        this.data.clear();
        fireDatasetChanged();
    }


    @Override
    public boolean equals ( Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof YIntervalSeriesCollection ) ) {
            return false;
        }
        YIntervalSeriesCollection that = ( YIntervalSeriesCollection ) obj;
        return ObjectUtilities.equal ( this.data, that.data );
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        YIntervalSeriesCollection clone
            = ( YIntervalSeriesCollection ) super.clone();
        clone.data = ( List ) ObjectUtilities.deepClone ( this.data );
        return clone;
    }

}
