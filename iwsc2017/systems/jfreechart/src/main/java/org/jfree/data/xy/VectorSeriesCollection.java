

package org.jfree.data.xy;

import java.io.Serializable;
import java.util.List;
import org.jfree.chart.util.ParamChecks;

import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PublicCloneable;


public class VectorSeriesCollection extends AbstractXYDataset
    implements VectorXYDataset, PublicCloneable, Serializable {


    private List data;


    public VectorSeriesCollection() {
        this.data = new java.util.ArrayList();
    }


    public void addSeries ( VectorSeries series ) {
        ParamChecks.nullNotPermitted ( series, "series" );
        this.data.add ( series );
        series.addChangeListener ( this );
        fireDatasetChanged();
    }


    public boolean removeSeries ( VectorSeries series ) {
        ParamChecks.nullNotPermitted ( series, "series" );
        boolean removed = this.data.remove ( series );
        if ( removed ) {
            series.removeChangeListener ( this );
            fireDatasetChanged();
        }
        return removed;
    }


    public void removeAllSeries() {

        for ( int i = 0; i < this.data.size(); i++ ) {
            VectorSeries series = ( VectorSeries ) this.data.get ( i );
            series.removeChangeListener ( this );
        }

        this.data.clear();
        fireDatasetChanged();

    }


    @Override
    public int getSeriesCount() {
        return this.data.size();
    }


    public VectorSeries getSeries ( int series ) {
        if ( ( series < 0 ) || ( series >= getSeriesCount() ) ) {
            throw new IllegalArgumentException ( "Series index out of bounds" );
        }
        return ( VectorSeries ) this.data.get ( series );
    }


    @Override
    public Comparable getSeriesKey ( int series ) {
        return getSeries ( series ).getKey();
    }


    public int indexOf ( VectorSeries series ) {
        ParamChecks.nullNotPermitted ( series, "series" );
        return this.data.indexOf ( series );
    }


    @Override
    public int getItemCount ( int series ) {
        return getSeries ( series ).getItemCount();
    }


    @Override
    public double getXValue ( int series, int item ) {
        VectorSeries s = ( VectorSeries ) this.data.get ( series );
        VectorDataItem di = ( VectorDataItem ) s.getDataItem ( item );
        return di.getXValue();
    }


    @Override
    public Number getX ( int series, int item ) {
        return new Double ( getXValue ( series, item ) );
    }


    @Override
    public double getYValue ( int series, int item ) {
        VectorSeries s = ( VectorSeries ) this.data.get ( series );
        VectorDataItem di = ( VectorDataItem ) s.getDataItem ( item );
        return di.getYValue();
    }


    @Override
    public Number getY ( int series, int item ) {
        return new Double ( getYValue ( series, item ) );
    }


    @Override
    public Vector getVector ( int series, int item ) {
        VectorSeries s = ( VectorSeries ) this.data.get ( series );
        VectorDataItem di = ( VectorDataItem ) s.getDataItem ( item );
        return di.getVector();
    }


    @Override
    public double getVectorXValue ( int series, int item ) {
        VectorSeries s = ( VectorSeries ) this.data.get ( series );
        VectorDataItem di = ( VectorDataItem ) s.getDataItem ( item );
        return di.getVectorX();
    }


    @Override
    public double getVectorYValue ( int series, int item ) {
        VectorSeries s = ( VectorSeries ) this.data.get ( series );
        VectorDataItem di = ( VectorDataItem ) s.getDataItem ( item );
        return di.getVectorY();
    }


    @Override
    public boolean equals ( Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof VectorSeriesCollection ) ) {
            return false;
        }
        VectorSeriesCollection that = ( VectorSeriesCollection ) obj;
        return ObjectUtilities.equal ( this.data, that.data );
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        VectorSeriesCollection clone
            = ( VectorSeriesCollection ) super.clone();
        clone.data = ( List ) ObjectUtilities.deepClone ( this.data );
        return clone;
    }

}
