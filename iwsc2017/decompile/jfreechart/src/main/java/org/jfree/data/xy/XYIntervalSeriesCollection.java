package org.jfree.data.xy;
import org.jfree.util.ObjectUtilities;
import org.jfree.data.general.SeriesChangeListener;
import org.jfree.chart.util.ParamChecks;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class XYIntervalSeriesCollection extends AbstractIntervalXYDataset implements IntervalXYDataset, PublicCloneable, Serializable {
    private List data;
    public XYIntervalSeriesCollection() {
        this.data = new ArrayList();
    }
    public void addSeries ( final XYIntervalSeries series ) {
        ParamChecks.nullNotPermitted ( series, "series" );
        this.data.add ( series );
        series.addChangeListener ( this );
        this.fireDatasetChanged();
    }
    public int getSeriesCount() {
        return this.data.size();
    }
    public XYIntervalSeries getSeries ( final int series ) {
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
        final XYIntervalSeries s = this.data.get ( series );
        return s.getX ( item );
    }
    @Override
    public double getStartXValue ( final int series, final int item ) {
        final XYIntervalSeries s = this.data.get ( series );
        return s.getXLowValue ( item );
    }
    @Override
    public double getEndXValue ( final int series, final int item ) {
        final XYIntervalSeries s = this.data.get ( series );
        return s.getXHighValue ( item );
    }
    public double getYValue ( final int series, final int item ) {
        final XYIntervalSeries s = this.data.get ( series );
        return s.getYValue ( item );
    }
    @Override
    public double getStartYValue ( final int series, final int item ) {
        final XYIntervalSeries s = this.data.get ( series );
        return s.getYLowValue ( item );
    }
    @Override
    public double getEndYValue ( final int series, final int item ) {
        final XYIntervalSeries s = this.data.get ( series );
        return s.getYHighValue ( item );
    }
    public Number getY ( final int series, final int item ) {
        return new Double ( this.getYValue ( series, item ) );
    }
    @Override
    public Number getStartX ( final int series, final int item ) {
        return new Double ( this.getStartXValue ( series, item ) );
    }
    @Override
    public Number getEndX ( final int series, final int item ) {
        return new Double ( this.getEndXValue ( series, item ) );
    }
    @Override
    public Number getStartY ( final int series, final int item ) {
        return new Double ( this.getStartYValue ( series, item ) );
    }
    @Override
    public Number getEndY ( final int series, final int item ) {
        return new Double ( this.getEndYValue ( series, item ) );
    }
    public void removeSeries ( final int series ) {
        if ( series < 0 || series >= this.getSeriesCount() ) {
            throw new IllegalArgumentException ( "Series index out of bounds." );
        }
        final XYIntervalSeries ts = this.data.get ( series );
        ts.removeChangeListener ( this );
        this.data.remove ( series );
        this.fireDatasetChanged();
    }
    public void removeSeries ( final XYIntervalSeries series ) {
        ParamChecks.nullNotPermitted ( series, "series" );
        if ( this.data.contains ( series ) ) {
            series.removeChangeListener ( this );
            this.data.remove ( series );
            this.fireDatasetChanged();
        }
    }
    public void removeAllSeries() {
        for ( int i = 0; i < this.data.size(); ++i ) {
            final XYIntervalSeries series = this.data.get ( i );
            series.removeChangeListener ( this );
        }
        this.data.clear();
        this.fireDatasetChanged();
    }
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof XYIntervalSeriesCollection ) ) {
            return false;
        }
        final XYIntervalSeriesCollection that = ( XYIntervalSeriesCollection ) obj;
        return ObjectUtilities.equal ( ( Object ) this.data, ( Object ) that.data );
    }
    public Object clone() throws CloneNotSupportedException {
        final XYIntervalSeriesCollection clone = ( XYIntervalSeriesCollection ) super.clone();
        final int seriesCount = this.getSeriesCount();
        clone.data = new ArrayList ( seriesCount );
        for ( int i = 0; i < this.data.size(); ++i ) {
            clone.data.set ( i, this.getSeries ( i ).clone() );
        }
        return clone;
    }
}
