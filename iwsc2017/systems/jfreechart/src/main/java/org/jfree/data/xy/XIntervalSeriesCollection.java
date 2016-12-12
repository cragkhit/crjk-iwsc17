

package org.jfree.data.xy;

import java.io.Serializable;
import java.util.List;
import org.jfree.chart.util.ParamChecks;

import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PublicCloneable;


public class XIntervalSeriesCollection extends AbstractIntervalXYDataset
    implements IntervalXYDataset, PublicCloneable, Serializable {


    private List data;


    public XIntervalSeriesCollection() {
        this.data = new java.util.ArrayList();
    }


    public void addSeries ( XIntervalSeries series ) {
        ParamChecks.nullNotPermitted ( series, "series" );
        this.data.add ( series );
        series.addChangeListener ( this );
        fireDatasetChanged();
    }


    @Override
    public int getSeriesCount() {
        return this.data.size();
    }


    public XIntervalSeries getSeries ( int series ) {
        if ( ( series < 0 ) || ( series >= getSeriesCount() ) ) {
            throw new IllegalArgumentException ( "Series index out of bounds" );
        }
        return ( XIntervalSeries ) this.data.get ( series );
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
        XIntervalSeries s = ( XIntervalSeries ) this.data.get ( series );
        XIntervalDataItem di = ( XIntervalDataItem ) s.getDataItem ( item );
        return di.getX();
    }


    @Override
    public double getStartXValue ( int series, int item ) {
        XIntervalSeries s = ( XIntervalSeries ) this.data.get ( series );
        return s.getXLowValue ( item );
    }


    @Override
    public double getEndXValue ( int series, int item ) {
        XIntervalSeries s = ( XIntervalSeries ) this.data.get ( series );
        return s.getXHighValue ( item );
    }


    @Override
    public double getYValue ( int series, int item ) {
        XIntervalSeries s = ( XIntervalSeries ) this.data.get ( series );
        return s.getYValue ( item );
    }


    @Override
    public Number getY ( int series, int item ) {
        XIntervalSeries s = ( XIntervalSeries ) this.data.get ( series );
        XIntervalDataItem di = ( XIntervalDataItem ) s.getDataItem ( item );
        return new Double ( di.getYValue() );
    }


    @Override
    public Number getStartX ( int series, int item ) {
        XIntervalSeries s = ( XIntervalSeries ) this.data.get ( series );
        XIntervalDataItem di = ( XIntervalDataItem ) s.getDataItem ( item );
        return new Double ( di.getXLowValue() );
    }


    @Override
    public Number getEndX ( int series, int item ) {
        XIntervalSeries s = ( XIntervalSeries ) this.data.get ( series );
        XIntervalDataItem di = ( XIntervalDataItem ) s.getDataItem ( item );
        return new Double ( di.getXHighValue() );
    }


    @Override
    public Number getStartY ( int series, int item ) {
        return getY ( series, item );
    }


    @Override
    public Number getEndY ( int series, int item ) {
        return getY ( series, item );
    }


    public void removeSeries ( int series ) {
        if ( ( series < 0 ) || ( series >= getSeriesCount() ) ) {
            throw new IllegalArgumentException ( "Series index out of bounds." );
        }
        XIntervalSeries ts = ( XIntervalSeries ) this.data.get ( series );
        ts.removeChangeListener ( this );
        this.data.remove ( series );
        fireDatasetChanged();
    }


    public void removeSeries ( XIntervalSeries series ) {
        ParamChecks.nullNotPermitted ( series, "series" );
        if ( this.data.contains ( series ) ) {
            series.removeChangeListener ( this );
            this.data.remove ( series );
            fireDatasetChanged();
        }
    }


    public void removeAllSeries() {
        for ( int i = 0; i < this.data.size(); i++ ) {
            XIntervalSeries series = ( XIntervalSeries ) this.data.get ( i );
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
        if ( ! ( obj instanceof XIntervalSeriesCollection ) ) {
            return false;
        }
        XIntervalSeriesCollection that = ( XIntervalSeriesCollection ) obj;
        return ObjectUtilities.equal ( this.data, that.data );
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        XIntervalSeriesCollection clone
            = ( XIntervalSeriesCollection ) super.clone();
        clone.data = ( List ) ObjectUtilities.deepClone ( this.data );
        return clone;
    }

}
