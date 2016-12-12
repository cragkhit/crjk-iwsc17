

package org.jfree.data.xy;

import java.io.Serializable;
import java.util.List;
import org.jfree.chart.util.ParamChecks;

import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PublicCloneable;


public class XYIntervalSeriesCollection extends AbstractIntervalXYDataset
    implements IntervalXYDataset, PublicCloneable, Serializable {


    private List data;


    public XYIntervalSeriesCollection() {
        this.data = new java.util.ArrayList();
    }


    public void addSeries ( XYIntervalSeries series ) {
        ParamChecks.nullNotPermitted ( series, "series" );
        this.data.add ( series );
        series.addChangeListener ( this );
        fireDatasetChanged();
    }


    @Override
    public int getSeriesCount() {
        return this.data.size();
    }


    public XYIntervalSeries getSeries ( int series ) {
        if ( ( series < 0 ) || ( series >= getSeriesCount() ) ) {
            throw new IllegalArgumentException ( "Series index out of bounds" );
        }
        return ( XYIntervalSeries ) this.data.get ( series );
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
        XYIntervalSeries s = ( XYIntervalSeries ) this.data.get ( series );
        return s.getX ( item );
    }


    @Override
    public double getStartXValue ( int series, int item ) {
        XYIntervalSeries s = ( XYIntervalSeries ) this.data.get ( series );
        return s.getXLowValue ( item );
    }


    @Override
    public double getEndXValue ( int series, int item ) {
        XYIntervalSeries s = ( XYIntervalSeries ) this.data.get ( series );
        return s.getXHighValue ( item );
    }


    @Override
    public double getYValue ( int series, int item ) {
        XYIntervalSeries s = ( XYIntervalSeries ) this.data.get ( series );
        return s.getYValue ( item );
    }


    @Override
    public double getStartYValue ( int series, int item ) {
        XYIntervalSeries s = ( XYIntervalSeries ) this.data.get ( series );
        return s.getYLowValue ( item );
    }


    @Override
    public double getEndYValue ( int series, int item ) {
        XYIntervalSeries s = ( XYIntervalSeries ) this.data.get ( series );
        return s.getYHighValue ( item );
    }


    @Override
    public Number getY ( int series, int item ) {
        return new Double ( getYValue ( series, item ) );
    }


    @Override
    public Number getStartX ( int series, int item ) {
        return new Double ( getStartXValue ( series, item ) );
    }


    @Override
    public Number getEndX ( int series, int item ) {
        return new Double ( getEndXValue ( series, item ) );
    }


    @Override
    public Number getStartY ( int series, int item ) {
        return new Double ( getStartYValue ( series, item ) );
    }


    @Override
    public Number getEndY ( int series, int item ) {
        return new Double ( getEndYValue ( series, item ) );
    }


    public void removeSeries ( int series ) {
        if ( ( series < 0 ) || ( series >= getSeriesCount() ) ) {
            throw new IllegalArgumentException ( "Series index out of bounds." );
        }
        XYIntervalSeries ts = ( XYIntervalSeries ) this.data.get ( series );
        ts.removeChangeListener ( this );
        this.data.remove ( series );
        fireDatasetChanged();
    }


    public void removeSeries ( XYIntervalSeries series ) {
        ParamChecks.nullNotPermitted ( series, "series" );
        if ( this.data.contains ( series ) ) {
            series.removeChangeListener ( this );
            this.data.remove ( series );
            fireDatasetChanged();
        }
    }


    public void removeAllSeries() {
        for ( int i = 0; i < this.data.size(); i++ ) {
            XYIntervalSeries series = ( XYIntervalSeries ) this.data.get ( i );
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
        if ( ! ( obj instanceof XYIntervalSeriesCollection ) ) {
            return false;
        }
        XYIntervalSeriesCollection that = ( XYIntervalSeriesCollection ) obj;
        return ObjectUtilities.equal ( this.data, that.data );
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        XYIntervalSeriesCollection clone
            = ( XYIntervalSeriesCollection ) super.clone();
        int seriesCount = getSeriesCount();
        clone.data = new java.util.ArrayList ( seriesCount );
        for ( int i = 0; i < this.data.size(); i++ ) {
            clone.data.set ( i, getSeries ( i ).clone() );
        }
        return clone;
    }

}
