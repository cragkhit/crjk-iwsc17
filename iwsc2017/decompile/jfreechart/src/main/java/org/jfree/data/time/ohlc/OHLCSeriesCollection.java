package org.jfree.data.time.ohlc;
import java.util.Collection;
import org.jfree.chart.HashUtilities;
import org.jfree.util.ObjectUtilities;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.general.SeriesChangeListener;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.chart.util.ParamChecks;
import java.util.ArrayList;
import org.jfree.data.time.TimePeriodAnchor;
import java.util.List;
import java.io.Serializable;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.AbstractXYDataset;
public class OHLCSeriesCollection extends AbstractXYDataset implements OHLCDataset, Serializable {
    private List data;
    private TimePeriodAnchor xPosition;
    public OHLCSeriesCollection() {
        this.xPosition = TimePeriodAnchor.MIDDLE;
        this.data = new ArrayList();
    }
    public TimePeriodAnchor getXPosition() {
        return this.xPosition;
    }
    public void setXPosition ( final TimePeriodAnchor anchor ) {
        ParamChecks.nullNotPermitted ( anchor, "anchor" );
        this.xPosition = anchor;
        this.notifyListeners ( new DatasetChangeEvent ( this, this ) );
    }
    public void addSeries ( final OHLCSeries series ) {
        ParamChecks.nullNotPermitted ( series, "series" );
        this.data.add ( series );
        series.addChangeListener ( this );
        this.fireDatasetChanged();
    }
    @Override
    public int getSeriesCount() {
        return this.data.size();
    }
    public OHLCSeries getSeries ( final int series ) {
        if ( series < 0 || series >= this.getSeriesCount() ) {
            throw new IllegalArgumentException ( "Series index out of bounds" );
        }
        return this.data.get ( series );
    }
    @Override
    public Comparable getSeriesKey ( final int series ) {
        return this.getSeries ( series ).getKey();
    }
    @Override
    public int getItemCount ( final int series ) {
        return this.getSeries ( series ).getItemCount();
    }
    protected synchronized long getX ( final RegularTimePeriod period ) {
        long result = 0L;
        if ( this.xPosition == TimePeriodAnchor.START ) {
            result = period.getFirstMillisecond();
        } else if ( this.xPosition == TimePeriodAnchor.MIDDLE ) {
            result = period.getMiddleMillisecond();
        } else if ( this.xPosition == TimePeriodAnchor.END ) {
            result = period.getLastMillisecond();
        }
        return result;
    }
    @Override
    public double getXValue ( final int series, final int item ) {
        final OHLCSeries s = this.data.get ( series );
        final OHLCItem di = ( OHLCItem ) s.getDataItem ( item );
        final RegularTimePeriod period = di.getPeriod();
        return this.getX ( period );
    }
    @Override
    public Number getX ( final int series, final int item ) {
        return new Double ( this.getXValue ( series, item ) );
    }
    @Override
    public Number getY ( final int series, final int item ) {
        final OHLCSeries s = this.data.get ( series );
        final OHLCItem di = ( OHLCItem ) s.getDataItem ( item );
        return new Double ( di.getYValue() );
    }
    @Override
    public double getOpenValue ( final int series, final int item ) {
        final OHLCSeries s = this.data.get ( series );
        final OHLCItem di = ( OHLCItem ) s.getDataItem ( item );
        return di.getOpenValue();
    }
    @Override
    public Number getOpen ( final int series, final int item ) {
        return new Double ( this.getOpenValue ( series, item ) );
    }
    @Override
    public double getCloseValue ( final int series, final int item ) {
        final OHLCSeries s = this.data.get ( series );
        final OHLCItem di = ( OHLCItem ) s.getDataItem ( item );
        return di.getCloseValue();
    }
    @Override
    public Number getClose ( final int series, final int item ) {
        return new Double ( this.getCloseValue ( series, item ) );
    }
    @Override
    public double getHighValue ( final int series, final int item ) {
        final OHLCSeries s = this.data.get ( series );
        final OHLCItem di = ( OHLCItem ) s.getDataItem ( item );
        return di.getHighValue();
    }
    @Override
    public Number getHigh ( final int series, final int item ) {
        return new Double ( this.getHighValue ( series, item ) );
    }
    @Override
    public double getLowValue ( final int series, final int item ) {
        final OHLCSeries s = this.data.get ( series );
        final OHLCItem di = ( OHLCItem ) s.getDataItem ( item );
        return di.getLowValue();
    }
    @Override
    public Number getLow ( final int series, final int item ) {
        return new Double ( this.getLowValue ( series, item ) );
    }
    @Override
    public Number getVolume ( final int series, final int item ) {
        return null;
    }
    @Override
    public double getVolumeValue ( final int series, final int item ) {
        return Double.NaN;
    }
    public void removeSeries ( final int index ) {
        final OHLCSeries series = this.getSeries ( index );
        if ( series != null ) {
            this.removeSeries ( series );
        }
    }
    public boolean removeSeries ( final OHLCSeries series ) {
        ParamChecks.nullNotPermitted ( series, "series" );
        final boolean removed = this.data.remove ( series );
        if ( removed ) {
            series.removeChangeListener ( this );
            this.fireDatasetChanged();
        }
        return removed;
    }
    public void removeAllSeries() {
        if ( this.data.isEmpty() ) {
            return;
        }
        for ( int i = 0; i < this.data.size(); ++i ) {
            final OHLCSeries series = this.data.get ( i );
            series.removeChangeListener ( this );
        }
        this.data.clear();
        this.fireDatasetChanged();
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof OHLCSeriesCollection ) ) {
            return false;
        }
        final OHLCSeriesCollection that = ( OHLCSeriesCollection ) obj;
        return this.xPosition.equals ( that.xPosition ) && ObjectUtilities.equal ( ( Object ) this.data, ( Object ) that.data );
    }
    @Override
    public int hashCode() {
        int result = 137;
        result = HashUtilities.hashCode ( result, this.xPosition );
        for ( int i = 0; i < this.data.size(); ++i ) {
            result = HashUtilities.hashCode ( result, this.data.get ( i ) );
        }
        return result;
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final OHLCSeriesCollection clone = ( OHLCSeriesCollection ) super.clone();
        clone.data = ( List ) ObjectUtilities.deepClone ( ( Collection ) this.data );
        return clone;
    }
}
