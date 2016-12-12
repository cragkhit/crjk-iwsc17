package org.jfree.data.xy;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.Range;
import org.jfree.util.ObjectUtilities;
import org.jfree.data.general.SeriesChangeEvent;
import java.util.Iterator;
import org.jfree.data.general.SeriesChangeListener;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.general.DatasetChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.jfree.util.PublicCloneable;
import org.jfree.data.DomainInfo;
public class DefaultTableXYDataset extends AbstractIntervalXYDataset implements TableXYDataset, IntervalXYDataset, DomainInfo, PublicCloneable {
    private List data;
    private HashSet xPoints;
    private boolean propagateEvents;
    private boolean autoPrune;
    private IntervalXYDelegate intervalDelegate;
    public DefaultTableXYDataset() {
        this ( false );
    }
    public DefaultTableXYDataset ( final boolean autoPrune ) {
        this.data = null;
        this.xPoints = null;
        this.propagateEvents = true;
        this.autoPrune = false;
        this.autoPrune = autoPrune;
        this.data = new ArrayList();
        this.xPoints = new HashSet();
        this.addChangeListener ( this.intervalDelegate = new IntervalXYDelegate ( this, false ) );
    }
    public boolean isAutoPrune() {
        return this.autoPrune;
    }
    public void addSeries ( final XYSeries series ) {
        ParamChecks.nullNotPermitted ( series, "series" );
        if ( series.getAllowDuplicateXValues() ) {
            throw new IllegalArgumentException ( "Cannot accept XYSeries that allow duplicate values. Use XYSeries(seriesName, <sort>, false) constructor." );
        }
        this.updateXPoints ( series );
        this.data.add ( series );
        series.addChangeListener ( this );
        this.fireDatasetChanged();
    }
    private void updateXPoints ( final XYSeries series ) {
        ParamChecks.nullNotPermitted ( series, "series" );
        final HashSet seriesXPoints = new HashSet();
        final boolean savedState = this.propagateEvents;
        this.propagateEvents = false;
        for ( int itemNo = 0; itemNo < series.getItemCount(); ++itemNo ) {
            final Number xValue = series.getX ( itemNo );
            seriesXPoints.add ( xValue );
            if ( !this.xPoints.contains ( xValue ) ) {
                this.xPoints.add ( xValue );
                for ( int seriesCount = this.data.size(), seriesNo = 0; seriesNo < seriesCount; ++seriesNo ) {
                    final XYSeries dataSeries = this.data.get ( seriesNo );
                    if ( !dataSeries.equals ( series ) ) {
                        dataSeries.add ( xValue, null );
                    }
                }
            }
        }
        for ( final Number xPoint : this.xPoints ) {
            if ( !seriesXPoints.contains ( xPoint ) ) {
                series.add ( xPoint, null );
            }
        }
        this.propagateEvents = savedState;
    }
    public void updateXPoints() {
        this.propagateEvents = false;
        for ( int s = 0; s < this.data.size(); ++s ) {
            this.updateXPoints ( this.data.get ( s ) );
        }
        if ( this.autoPrune ) {
            this.prune();
        }
        this.propagateEvents = true;
    }
    public int getSeriesCount() {
        return this.data.size();
    }
    @Override
    public int getItemCount() {
        if ( this.xPoints == null ) {
            return 0;
        }
        return this.xPoints.size();
    }
    public XYSeries getSeries ( final int series ) {
        if ( series < 0 || series >= this.getSeriesCount() ) {
            throw new IllegalArgumentException ( "Index outside valid range." );
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
        final XYSeries s = this.data.get ( series );
        return s.getX ( item );
    }
    @Override
    public Number getStartX ( final int series, final int item ) {
        return this.intervalDelegate.getStartX ( series, item );
    }
    @Override
    public Number getEndX ( final int series, final int item ) {
        return this.intervalDelegate.getEndX ( series, item );
    }
    public Number getY ( final int series, final int index ) {
        final XYSeries s = this.data.get ( series );
        return s.getY ( index );
    }
    @Override
    public Number getStartY ( final int series, final int item ) {
        return this.getY ( series, item );
    }
    @Override
    public Number getEndY ( final int series, final int item ) {
        return this.getY ( series, item );
    }
    public void removeAllSeries() {
        for ( int i = 0; i < this.data.size(); ++i ) {
            final XYSeries series = this.data.get ( i );
            series.removeChangeListener ( this );
        }
        this.data.clear();
        this.xPoints.clear();
        this.fireDatasetChanged();
    }
    public void removeSeries ( final XYSeries series ) {
        ParamChecks.nullNotPermitted ( series, "series" );
        if ( this.data.contains ( series ) ) {
            series.removeChangeListener ( this );
            this.data.remove ( series );
            if ( this.data.isEmpty() ) {
                this.xPoints.clear();
            }
            this.fireDatasetChanged();
        }
    }
    public void removeSeries ( final int series ) {
        if ( series < 0 || series > this.getSeriesCount() ) {
            throw new IllegalArgumentException ( "Index outside valid range." );
        }
        final XYSeries s = this.data.get ( series );
        s.removeChangeListener ( this );
        this.data.remove ( series );
        if ( this.data.isEmpty() ) {
            this.xPoints.clear();
        } else if ( this.autoPrune ) {
            this.prune();
        }
        this.fireDatasetChanged();
    }
    public void removeAllValuesForX ( final Number x ) {
        ParamChecks.nullNotPermitted ( x, "x" );
        final boolean savedState = this.propagateEvents;
        this.propagateEvents = false;
        for ( int s = 0; s < this.data.size(); ++s ) {
            final XYSeries series = this.data.get ( s );
            series.remove ( x );
        }
        this.propagateEvents = savedState;
        this.xPoints.remove ( x );
        this.fireDatasetChanged();
    }
    protected boolean canPrune ( final Number x ) {
        for ( int s = 0; s < this.data.size(); ++s ) {
            final XYSeries series = this.data.get ( s );
            if ( series.getY ( series.indexOf ( x ) ) != null ) {
                return false;
            }
        }
        return true;
    }
    public void prune() {
        final HashSet hs = ( HashSet ) this.xPoints.clone();
        for ( final Number x : hs ) {
            if ( this.canPrune ( x ) ) {
                this.removeAllValuesForX ( x );
            }
        }
    }
    public void seriesChanged ( final SeriesChangeEvent event ) {
        if ( this.propagateEvents ) {
            this.updateXPoints();
            this.fireDatasetChanged();
        }
    }
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof DefaultTableXYDataset ) ) {
            return false;
        }
        final DefaultTableXYDataset that = ( DefaultTableXYDataset ) obj;
        return this.autoPrune == that.autoPrune && this.propagateEvents == that.propagateEvents && this.intervalDelegate.equals ( that.intervalDelegate ) && ObjectUtilities.equal ( ( Object ) this.data, ( Object ) that.data );
    }
    public int hashCode() {
        int result = ( this.data != null ) ? this.data.hashCode() : 0;
        result = 29 * result + ( ( this.xPoints != null ) ? this.xPoints.hashCode() : 0 );
        result = 29 * result + ( this.propagateEvents ? 1 : 0 );
        result = 29 * result + ( this.autoPrune ? 1 : 0 );
        return result;
    }
    public Object clone() throws CloneNotSupportedException {
        final DefaultTableXYDataset clone = ( DefaultTableXYDataset ) super.clone();
        final int seriesCount = this.data.size();
        clone.data = new ArrayList ( seriesCount );
        for ( int i = 0; i < seriesCount; ++i ) {
            final XYSeries series = this.data.get ( i );
            clone.data.add ( series.clone() );
        }
        ( clone.intervalDelegate = new IntervalXYDelegate ( clone ) ).setFixedIntervalWidth ( this.getIntervalWidth() );
        clone.intervalDelegate.setAutoWidth ( this.isAutoWidth() );
        clone.intervalDelegate.setIntervalPositionFactor ( this.getIntervalPositionFactor() );
        clone.updateXPoints();
        return clone;
    }
    @Override
    public double getDomainLowerBound ( final boolean includeInterval ) {
        return this.intervalDelegate.getDomainLowerBound ( includeInterval );
    }
    @Override
    public double getDomainUpperBound ( final boolean includeInterval ) {
        return this.intervalDelegate.getDomainUpperBound ( includeInterval );
    }
    @Override
    public Range getDomainBounds ( final boolean includeInterval ) {
        if ( includeInterval ) {
            return this.intervalDelegate.getDomainBounds ( includeInterval );
        }
        return DatasetUtilities.iterateDomainBounds ( this, includeInterval );
    }
    public double getIntervalPositionFactor() {
        return this.intervalDelegate.getIntervalPositionFactor();
    }
    public void setIntervalPositionFactor ( final double d ) {
        this.intervalDelegate.setIntervalPositionFactor ( d );
        this.fireDatasetChanged();
    }
    public double getIntervalWidth() {
        return this.intervalDelegate.getIntervalWidth();
    }
    public void setIntervalWidth ( final double d ) {
        this.intervalDelegate.setFixedIntervalWidth ( d );
        this.fireDatasetChanged();
    }
    public boolean isAutoWidth() {
        return this.intervalDelegate.isAutoWidth();
    }
    public void setAutoWidth ( final boolean b ) {
        this.intervalDelegate.setAutoWidth ( b );
        this.fireDatasetChanged();
    }
}
