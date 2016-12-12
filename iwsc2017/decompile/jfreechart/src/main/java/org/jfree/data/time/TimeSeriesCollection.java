package org.jfree.data.time;
import java.util.Collection;
import org.jfree.util.ObjectUtilities;
import java.beans.PropertyVetoException;
import org.jfree.data.general.Series;
import java.beans.PropertyChangeEvent;
import org.jfree.data.Range;
import java.util.Iterator;
import java.util.Collections;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.SeriesChangeListener;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.List;
import java.io.Serializable;
import java.beans.VetoableChangeListener;
import org.jfree.data.xy.XYRangeInfo;
import org.jfree.data.xy.XYDomainInfo;
import org.jfree.data.DomainInfo;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.AbstractIntervalXYDataset;
public class TimeSeriesCollection extends AbstractIntervalXYDataset implements XYDataset, IntervalXYDataset, DomainInfo, XYDomainInfo, XYRangeInfo, VetoableChangeListener, Serializable {
    private static final long serialVersionUID = 834149929022371137L;
    private List data;
    private Calendar workingCalendar;
    private TimePeriodAnchor xPosition;
    private boolean domainIsPointsInTime;
    public TimeSeriesCollection() {
        this ( null, TimeZone.getDefault() );
    }
    public TimeSeriesCollection ( final TimeZone zone ) {
        this ( null, zone );
    }
    public TimeSeriesCollection ( final TimeSeries series ) {
        this ( series, TimeZone.getDefault() );
    }
    public TimeSeriesCollection ( final TimeSeries series, TimeZone zone ) {
        if ( zone == null ) {
            zone = TimeZone.getDefault();
        }
        this.workingCalendar = Calendar.getInstance ( zone );
        this.data = new ArrayList();
        if ( series != null ) {
            this.data.add ( series );
            series.addChangeListener ( this );
        }
        this.xPosition = TimePeriodAnchor.START;
        this.domainIsPointsInTime = true;
    }
    public boolean getDomainIsPointsInTime() {
        return this.domainIsPointsInTime;
    }
    public void setDomainIsPointsInTime ( final boolean flag ) {
        this.domainIsPointsInTime = flag;
        this.notifyListeners ( new DatasetChangeEvent ( this, this ) );
    }
    @Override
    public DomainOrder getDomainOrder() {
        return DomainOrder.ASCENDING;
    }
    public TimePeriodAnchor getXPosition() {
        return this.xPosition;
    }
    public void setXPosition ( final TimePeriodAnchor anchor ) {
        ParamChecks.nullNotPermitted ( anchor, "anchor" );
        this.xPosition = anchor;
        this.notifyListeners ( new DatasetChangeEvent ( this, this ) );
    }
    public List getSeries() {
        return Collections.unmodifiableList ( ( List<?> ) this.data );
    }
    @Override
    public int getSeriesCount() {
        return this.data.size();
    }
    public int indexOf ( final TimeSeries series ) {
        ParamChecks.nullNotPermitted ( series, "series" );
        return this.data.indexOf ( series );
    }
    public TimeSeries getSeries ( final int series ) {
        if ( series < 0 || series >= this.getSeriesCount() ) {
            throw new IllegalArgumentException ( "The 'series' argument is out of bounds (" + series + ")." );
        }
        return this.data.get ( series );
    }
    public TimeSeries getSeries ( final Comparable key ) {
        TimeSeries result = null;
        for ( final TimeSeries series : this.data ) {
            final Comparable k = series.getKey();
            if ( k != null && k.equals ( key ) ) {
                result = series;
            }
        }
        return result;
    }
    @Override
    public Comparable getSeriesKey ( final int series ) {
        return this.getSeries ( series ).getKey();
    }
    public int getSeriesIndex ( final Comparable key ) {
        ParamChecks.nullNotPermitted ( key, "key" );
        for ( int seriesCount = this.getSeriesCount(), i = 0; i < seriesCount; ++i ) {
            final TimeSeries series = this.data.get ( i );
            if ( key.equals ( series.getKey() ) ) {
                return i;
            }
        }
        return -1;
    }
    public void addSeries ( final TimeSeries series ) {
        ParamChecks.nullNotPermitted ( series, "series" );
        this.data.add ( series );
        series.addChangeListener ( this );
        series.addVetoableChangeListener ( this );
        this.fireDatasetChanged();
    }
    public void removeSeries ( final TimeSeries series ) {
        ParamChecks.nullNotPermitted ( series, "series" );
        this.data.remove ( series );
        series.removeChangeListener ( this );
        series.removeVetoableChangeListener ( this );
        this.fireDatasetChanged();
    }
    public void removeSeries ( final int index ) {
        final TimeSeries series = this.getSeries ( index );
        if ( series != null ) {
            this.removeSeries ( series );
        }
    }
    public void removeAllSeries() {
        for ( int i = 0; i < this.data.size(); ++i ) {
            final TimeSeries series = this.data.get ( i );
            series.removeChangeListener ( this );
            series.removeVetoableChangeListener ( this );
        }
        this.data.clear();
        this.fireDatasetChanged();
    }
    @Override
    public int getItemCount ( final int series ) {
        return this.getSeries ( series ).getItemCount();
    }
    @Override
    public double getXValue ( final int series, final int item ) {
        final TimeSeries s = this.data.get ( series );
        final RegularTimePeriod period = s.getTimePeriod ( item );
        return this.getX ( period );
    }
    @Override
    public Number getX ( final int series, final int item ) {
        final TimeSeries ts = this.data.get ( series );
        final RegularTimePeriod period = ts.getTimePeriod ( item );
        return new Long ( this.getX ( period ) );
    }
    protected synchronized long getX ( final RegularTimePeriod period ) {
        long result = 0L;
        if ( this.xPosition == TimePeriodAnchor.START ) {
            result = period.getFirstMillisecond ( this.workingCalendar );
        } else if ( this.xPosition == TimePeriodAnchor.MIDDLE ) {
            result = period.getMiddleMillisecond ( this.workingCalendar );
        } else if ( this.xPosition == TimePeriodAnchor.END ) {
            result = period.getLastMillisecond ( this.workingCalendar );
        }
        return result;
    }
    @Override
    public synchronized Number getStartX ( final int series, final int item ) {
        final TimeSeries ts = this.data.get ( series );
        return new Long ( ts.getTimePeriod ( item ).getFirstMillisecond ( this.workingCalendar ) );
    }
    @Override
    public synchronized Number getEndX ( final int series, final int item ) {
        final TimeSeries ts = this.data.get ( series );
        return new Long ( ts.getTimePeriod ( item ).getLastMillisecond ( this.workingCalendar ) );
    }
    @Override
    public Number getY ( final int series, final int item ) {
        final TimeSeries ts = this.data.get ( series );
        return ts.getValue ( item );
    }
    @Override
    public Number getStartY ( final int series, final int item ) {
        return this.getY ( series, item );
    }
    @Override
    public Number getEndY ( final int series, final int item ) {
        return this.getY ( series, item );
    }
    public int[] getSurroundingItems ( final int series, final long milliseconds ) {
        final int[] result = { -1, -1 };
        final TimeSeries timeSeries = this.getSeries ( series );
        for ( int i = 0; i < timeSeries.getItemCount(); ++i ) {
            final Number x = this.getX ( series, i );
            final long m = x.longValue();
            if ( m <= milliseconds ) {
                result[0] = i;
            }
            if ( m >= milliseconds ) {
                result[1] = i;
                break;
            }
        }
        return result;
    }
    @Override
    public double getDomainLowerBound ( final boolean includeInterval ) {
        double result = Double.NaN;
        final Range r = this.getDomainBounds ( includeInterval );
        if ( r != null ) {
            result = r.getLowerBound();
        }
        return result;
    }
    @Override
    public double getDomainUpperBound ( final boolean includeInterval ) {
        double result = Double.NaN;
        final Range r = this.getDomainBounds ( includeInterval );
        if ( r != null ) {
            result = r.getUpperBound();
        }
        return result;
    }
    @Override
    public Range getDomainBounds ( final boolean includeInterval ) {
        Range result = null;
        for ( final TimeSeries series : this.data ) {
            final int count = series.getItemCount();
            if ( count > 0 ) {
                final RegularTimePeriod start = series.getTimePeriod ( 0 );
                final RegularTimePeriod end = series.getTimePeriod ( count - 1 );
                Range temp;
                if ( !includeInterval ) {
                    temp = new Range ( this.getX ( start ), this.getX ( end ) );
                } else {
                    temp = new Range ( start.getFirstMillisecond ( this.workingCalendar ), end.getLastMillisecond ( this.workingCalendar ) );
                }
                result = Range.combine ( result, temp );
            }
        }
        return result;
    }
    @Override
    public Range getDomainBounds ( final List visibleSeriesKeys, final boolean includeInterval ) {
        Range result = null;
        for ( final Comparable seriesKey : visibleSeriesKeys ) {
            final TimeSeries series = this.getSeries ( seriesKey );
            final int count = series.getItemCount();
            if ( count > 0 ) {
                final RegularTimePeriod start = series.getTimePeriod ( 0 );
                final RegularTimePeriod end = series.getTimePeriod ( count - 1 );
                Range temp;
                if ( !includeInterval ) {
                    temp = new Range ( this.getX ( start ), this.getX ( end ) );
                } else {
                    temp = new Range ( start.getFirstMillisecond ( this.workingCalendar ), end.getLastMillisecond ( this.workingCalendar ) );
                }
                result = Range.combine ( result, temp );
            }
        }
        return result;
    }
    public Range getRangeBounds ( final boolean includeInterval ) {
        Range result = null;
        for ( final TimeSeries series : this.data ) {
            final Range r = new Range ( series.getMinY(), series.getMaxY() );
            result = Range.combineIgnoringNaN ( result, r );
        }
        return result;
    }
    @Override
    public Range getRangeBounds ( final List visibleSeriesKeys, final Range xRange, final boolean includeInterval ) {
        Range result = null;
        for ( final Comparable seriesKey : visibleSeriesKeys ) {
            final TimeSeries series = this.getSeries ( seriesKey );
            final Range r = series.findValueRange ( xRange, this.xPosition, this.workingCalendar.getTimeZone() );
            result = Range.combineIgnoringNaN ( result, r );
        }
        return result;
    }
    @Override
    public void vetoableChange ( final PropertyChangeEvent e ) throws PropertyVetoException {
        if ( !"Key".equals ( e.getPropertyName() ) ) {
            return;
        }
        final Series s = ( Series ) e.getSource();
        if ( this.getSeriesIndex ( s.getKey() ) == -1 ) {
            throw new IllegalStateException ( "Receiving events from a series that does not belong to this collection." );
        }
        final Comparable key = ( Comparable ) e.getNewValue();
        if ( this.getSeriesIndex ( key ) >= 0 ) {
            throw new PropertyVetoException ( "Duplicate key2", e );
        }
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof TimeSeriesCollection ) ) {
            return false;
        }
        final TimeSeriesCollection that = ( TimeSeriesCollection ) obj;
        return this.xPosition == that.xPosition && this.domainIsPointsInTime == that.domainIsPointsInTime && ObjectUtilities.equal ( ( Object ) this.data, ( Object ) that.data );
    }
    @Override
    public int hashCode() {
        int result = this.data.hashCode();
        result = 29 * result + ( ( this.workingCalendar != null ) ? this.workingCalendar.hashCode() : 0 );
        result = 29 * result + ( ( this.xPosition != null ) ? this.xPosition.hashCode() : 0 );
        result = 29 * result + ( this.domainIsPointsInTime ? 1 : 0 );
        return result;
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final TimeSeriesCollection clone = ( TimeSeriesCollection ) super.clone();
        clone.data = ( List ) ObjectUtilities.deepClone ( ( Collection ) this.data );
        clone.workingCalendar = ( Calendar ) this.workingCalendar.clone();
        return clone;
    }
}
