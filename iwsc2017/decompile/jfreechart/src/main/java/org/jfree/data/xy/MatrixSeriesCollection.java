package org.jfree.data.xy;
import java.util.Collection;
import org.jfree.util.ObjectUtilities;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.general.SeriesChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class MatrixSeriesCollection extends AbstractXYZDataset implements XYZDataset, PublicCloneable, Serializable {
    private static final long serialVersionUID = -3197705779242543945L;
    private List seriesList;
    public MatrixSeriesCollection() {
        this ( null );
    }
    public MatrixSeriesCollection ( final MatrixSeries series ) {
        this.seriesList = new ArrayList();
        if ( series != null ) {
            this.seriesList.add ( series );
            series.addChangeListener ( this );
        }
    }
    public int getItemCount ( final int seriesIndex ) {
        return this.getSeries ( seriesIndex ).getItemCount();
    }
    public MatrixSeries getSeries ( final int seriesIndex ) {
        if ( seriesIndex < 0 || seriesIndex > this.getSeriesCount() ) {
            throw new IllegalArgumentException ( "Index outside valid range." );
        }
        final MatrixSeries series = this.seriesList.get ( seriesIndex );
        return series;
    }
    public int getSeriesCount() {
        return this.seriesList.size();
    }
    public Comparable getSeriesKey ( final int seriesIndex ) {
        return this.getSeries ( seriesIndex ).getKey();
    }
    public Number getX ( final int seriesIndex, final int itemIndex ) {
        final MatrixSeries series = this.seriesList.get ( seriesIndex );
        final int x = series.getItemColumn ( itemIndex );
        return new Integer ( x );
    }
    public Number getY ( final int seriesIndex, final int itemIndex ) {
        final MatrixSeries series = this.seriesList.get ( seriesIndex );
        final int y = series.getItemRow ( itemIndex );
        return new Integer ( y );
    }
    @Override
    public Number getZ ( final int seriesIndex, final int itemIndex ) {
        final MatrixSeries series = this.seriesList.get ( seriesIndex );
        final Number z = series.getItem ( itemIndex );
        return z;
    }
    public void addSeries ( final MatrixSeries series ) {
        ParamChecks.nullNotPermitted ( series, "series" );
        this.seriesList.add ( series );
        series.addChangeListener ( this );
        this.fireDatasetChanged();
    }
    public boolean equals ( final Object obj ) {
        if ( obj == null ) {
            return false;
        }
        if ( obj == this ) {
            return true;
        }
        if ( obj instanceof MatrixSeriesCollection ) {
            final MatrixSeriesCollection c = ( MatrixSeriesCollection ) obj;
            return ObjectUtilities.equal ( ( Object ) this.seriesList, ( Object ) c.seriesList );
        }
        return false;
    }
    public int hashCode() {
        return ( this.seriesList != null ) ? this.seriesList.hashCode() : 0;
    }
    public Object clone() throws CloneNotSupportedException {
        final MatrixSeriesCollection clone = ( MatrixSeriesCollection ) super.clone();
        clone.seriesList = ( List ) ObjectUtilities.deepClone ( ( Collection ) this.seriesList );
        return clone;
    }
    public void removeAllSeries() {
        for ( int i = 0; i < this.seriesList.size(); ++i ) {
            final MatrixSeries series = this.seriesList.get ( i );
            series.removeChangeListener ( this );
        }
        this.seriesList.clear();
        this.fireDatasetChanged();
    }
    public void removeSeries ( final MatrixSeries series ) {
        ParamChecks.nullNotPermitted ( series, "series" );
        if ( this.seriesList.contains ( series ) ) {
            series.removeChangeListener ( this );
            this.seriesList.remove ( series );
            this.fireDatasetChanged();
        }
    }
    public void removeSeries ( final int seriesIndex ) {
        if ( seriesIndex < 0 || seriesIndex > this.getSeriesCount() ) {
            throw new IllegalArgumentException ( "Index outside valid range." );
        }
        final MatrixSeries series = this.seriesList.get ( seriesIndex );
        series.removeChangeListener ( this );
        this.seriesList.remove ( seriesIndex );
        this.fireDatasetChanged();
    }
}
