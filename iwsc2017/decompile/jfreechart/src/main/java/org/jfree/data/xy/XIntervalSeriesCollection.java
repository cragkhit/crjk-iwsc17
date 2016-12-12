package org.jfree.data.xy;
import java.util.Collection;
import org.jfree.util.ObjectUtilities;
import org.jfree.data.general.SeriesChangeListener;
import org.jfree.chart.util.ParamChecks;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class XIntervalSeriesCollection extends AbstractIntervalXYDataset implements IntervalXYDataset, PublicCloneable, Serializable {
    private List data;
    public XIntervalSeriesCollection() {
        this.data = new ArrayList();
    }
    public void addSeries ( final XIntervalSeries series ) {
        ParamChecks.nullNotPermitted ( series, "series" );
        this.data.add ( series );
        series.addChangeListener ( this );
        this.fireDatasetChanged();
    }
    public int getSeriesCount() {
        return this.data.size();
    }
    public XIntervalSeries getSeries ( final int series ) {
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
        final XIntervalSeries s = this.data.get ( series );
        final XIntervalDataItem di = ( XIntervalDataItem ) s.getDataItem ( item );
        return di.getX();
    }
    @Override
    public double getStartXValue ( final int series, final int item ) {
        final XIntervalSeries s = this.data.get ( series );
        return s.getXLowValue ( item );
    }
    @Override
    public double getEndXValue ( final int series, final int item ) {
        final XIntervalSeries s = this.data.get ( series );
        return s.getXHighValue ( item );
    }
    public double getYValue ( final int series, final int item ) {
        final XIntervalSeries s = this.data.get ( series );
        return s.getYValue ( item );
    }
    public Number getY ( final int series, final int item ) {
        final XIntervalSeries s = this.data.get ( series );
        final XIntervalDataItem di = ( XIntervalDataItem ) s.getDataItem ( item );
        return new Double ( di.getYValue() );
    }
    @Override
    public Number getStartX ( final int series, final int item ) {
        final XIntervalSeries s = this.data.get ( series );
        final XIntervalDataItem di = ( XIntervalDataItem ) s.getDataItem ( item );
        return new Double ( di.getXLowValue() );
    }
    @Override
    public Number getEndX ( final int series, final int item ) {
        final XIntervalSeries s = this.data.get ( series );
        final XIntervalDataItem di = ( XIntervalDataItem ) s.getDataItem ( item );
        return new Double ( di.getXHighValue() );
    }
    @Override
    public Number getStartY ( final int series, final int item ) {
        return this.getY ( series, item );
    }
    @Override
    public Number getEndY ( final int series, final int item ) {
        return this.getY ( series, item );
    }
    public void removeSeries ( final int series ) {
        if ( series < 0 || series >= this.getSeriesCount() ) {
            throw new IllegalArgumentException ( "Series index out of bounds." );
        }
        final XIntervalSeries ts = this.data.get ( series );
        ts.removeChangeListener ( this );
        this.data.remove ( series );
        this.fireDatasetChanged();
    }
    public void removeSeries ( final XIntervalSeries series ) {
        ParamChecks.nullNotPermitted ( series, "series" );
        if ( this.data.contains ( series ) ) {
            series.removeChangeListener ( this );
            this.data.remove ( series );
            this.fireDatasetChanged();
        }
    }
    public void removeAllSeries() {
        for ( int i = 0; i < this.data.size(); ++i ) {
            final XIntervalSeries series = this.data.get ( i );
            series.removeChangeListener ( this );
        }
        this.data.clear();
        this.fireDatasetChanged();
    }
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof XIntervalSeriesCollection ) ) {
            return false;
        }
        final XIntervalSeriesCollection that = ( XIntervalSeriesCollection ) obj;
        return ObjectUtilities.equal ( ( Object ) this.data, ( Object ) that.data );
    }
    public Object clone() throws CloneNotSupportedException {
        final XIntervalSeriesCollection clone = ( XIntervalSeriesCollection ) super.clone();
        clone.data = ( List ) ObjectUtilities.deepClone ( ( Collection ) this.data );
        return clone;
    }
}
