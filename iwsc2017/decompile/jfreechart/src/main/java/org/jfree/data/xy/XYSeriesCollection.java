package org.jfree.data.xy;
import java.beans.PropertyVetoException;
import org.jfree.data.general.Series;
import java.beans.PropertyChangeEvent;
import org.jfree.data.Range;
import org.jfree.chart.HashUtilities;
import java.util.Collection;
import org.jfree.util.ObjectUtilities;
import java.util.Iterator;
import org.jfree.data.UnknownKeyException;
import java.util.Collections;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.SeriesChangeListener;
import org.jfree.data.general.DatasetChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
import java.beans.VetoableChangeListener;
import org.jfree.data.RangeInfo;
import org.jfree.data.DomainInfo;
public class XYSeriesCollection extends AbstractIntervalXYDataset implements IntervalXYDataset, DomainInfo, RangeInfo, VetoableChangeListener, PublicCloneable, Serializable {
    private static final long serialVersionUID = -7590013825931496766L;
    private List data;
    private IntervalXYDelegate intervalDelegate;
    public XYSeriesCollection() {
        this ( null );
    }
    public XYSeriesCollection ( final XYSeries series ) {
        this.data = new ArrayList();
        this.addChangeListener ( this.intervalDelegate = new IntervalXYDelegate ( this, false ) );
        if ( series != null ) {
            this.data.add ( series );
            series.addChangeListener ( this );
            series.addVetoableChangeListener ( this );
        }
    }
    public DomainOrder getDomainOrder() {
        for ( int seriesCount = this.getSeriesCount(), i = 0; i < seriesCount; ++i ) {
            final XYSeries s = this.getSeries ( i );
            if ( !s.getAutoSort() ) {
                return DomainOrder.NONE;
            }
        }
        return DomainOrder.ASCENDING;
    }
    public void addSeries ( final XYSeries series ) {
        ParamChecks.nullNotPermitted ( series, "series" );
        if ( this.getSeriesIndex ( series.getKey() ) >= 0 ) {
            throw new IllegalArgumentException ( "This dataset already contains a series with the key " + series.getKey() );
        }
        this.data.add ( series );
        series.addChangeListener ( this );
        series.addVetoableChangeListener ( this );
        this.fireDatasetChanged();
    }
    public void removeSeries ( final int series ) {
        if ( series < 0 || series >= this.getSeriesCount() ) {
            throw new IllegalArgumentException ( "Series index out of bounds." );
        }
        final XYSeries s = this.data.get ( series );
        if ( s != null ) {
            this.removeSeries ( s );
        }
    }
    public void removeSeries ( final XYSeries series ) {
        ParamChecks.nullNotPermitted ( series, "series" );
        if ( this.data.contains ( series ) ) {
            series.removeChangeListener ( this );
            series.removeVetoableChangeListener ( this );
            this.data.remove ( series );
            this.fireDatasetChanged();
        }
    }
    public void removeAllSeries() {
        for ( int i = 0; i < this.data.size(); ++i ) {
            final XYSeries series = this.data.get ( i );
            series.removeChangeListener ( this );
            series.removeVetoableChangeListener ( this );
        }
        this.data.clear();
        this.fireDatasetChanged();
    }
    public int getSeriesCount() {
        return this.data.size();
    }
    public List getSeries() {
        return Collections.unmodifiableList ( ( List<?> ) this.data );
    }
    public int indexOf ( final XYSeries series ) {
        ParamChecks.nullNotPermitted ( series, "series" );
        return this.data.indexOf ( series );
    }
    public XYSeries getSeries ( final int series ) {
        if ( series < 0 || series >= this.getSeriesCount() ) {
            throw new IllegalArgumentException ( "Series index out of bounds" );
        }
        return this.data.get ( series );
    }
    public XYSeries getSeries ( final Comparable key ) {
        ParamChecks.nullNotPermitted ( key, "key" );
        for ( final XYSeries series : this.data ) {
            if ( key.equals ( series.getKey() ) ) {
                return series;
            }
        }
        throw new UnknownKeyException ( "Key not found: " + key );
    }
    public Comparable getSeriesKey ( final int series ) {
        return this.getSeries ( series ).getKey();
    }
    public int getSeriesIndex ( final Comparable key ) {
        ParamChecks.nullNotPermitted ( key, "key" );
        for ( int seriesCount = this.getSeriesCount(), i = 0; i < seriesCount; ++i ) {
            final XYSeries series = this.data.get ( i );
            if ( key.equals ( series.getKey() ) ) {
                return i;
            }
        }
        return -1;
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
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof XYSeriesCollection ) ) {
            return false;
        }
        final XYSeriesCollection that = ( XYSeriesCollection ) obj;
        return this.intervalDelegate.equals ( that.intervalDelegate ) && ObjectUtilities.equal ( ( Object ) this.data, ( Object ) that.data );
    }
    public Object clone() throws CloneNotSupportedException {
        final XYSeriesCollection clone = ( XYSeriesCollection ) super.clone();
        clone.data = ( List ) ObjectUtilities.deepClone ( ( Collection ) this.data );
        clone.intervalDelegate = ( IntervalXYDelegate ) this.intervalDelegate.clone();
        return clone;
    }
    public int hashCode() {
        int hash = 5;
        hash = HashUtilities.hashCode ( hash, this.intervalDelegate );
        hash = HashUtilities.hashCode ( hash, this.data );
        return hash;
    }
    @Override
    public double getDomainLowerBound ( final boolean includeInterval ) {
        if ( includeInterval ) {
            return this.intervalDelegate.getDomainLowerBound ( includeInterval );
        }
        double result = Double.NaN;
        for ( int seriesCount = this.getSeriesCount(), s = 0; s < seriesCount; ++s ) {
            final XYSeries series = this.getSeries ( s );
            final double lowX = series.getMinX();
            if ( Double.isNaN ( result ) ) {
                result = lowX;
            } else if ( !Double.isNaN ( lowX ) ) {
                result = Math.min ( result, lowX );
            }
        }
        return result;
    }
    @Override
    public double getDomainUpperBound ( final boolean includeInterval ) {
        if ( includeInterval ) {
            return this.intervalDelegate.getDomainUpperBound ( includeInterval );
        }
        double result = Double.NaN;
        for ( int seriesCount = this.getSeriesCount(), s = 0; s < seriesCount; ++s ) {
            final XYSeries series = this.getSeries ( s );
            final double hiX = series.getMaxX();
            if ( Double.isNaN ( result ) ) {
                result = hiX;
            } else if ( !Double.isNaN ( hiX ) ) {
                result = Math.max ( result, hiX );
            }
        }
        return result;
    }
    @Override
    public Range getDomainBounds ( final boolean includeInterval ) {
        if ( includeInterval ) {
            return this.intervalDelegate.getDomainBounds ( includeInterval );
        }
        double lower = Double.POSITIVE_INFINITY;
        double upper = Double.NEGATIVE_INFINITY;
        for ( int seriesCount = this.getSeriesCount(), s = 0; s < seriesCount; ++s ) {
            final XYSeries series = this.getSeries ( s );
            final double minX = series.getMinX();
            if ( !Double.isNaN ( minX ) ) {
                lower = Math.min ( lower, minX );
            }
            final double maxX = series.getMaxX();
            if ( !Double.isNaN ( maxX ) ) {
                upper = Math.max ( upper, maxX );
            }
        }
        if ( lower > upper ) {
            return null;
        }
        return new Range ( lower, upper );
    }
    public double getIntervalWidth() {
        return this.intervalDelegate.getIntervalWidth();
    }
    public void setIntervalWidth ( final double width ) {
        if ( width < 0.0 ) {
            throw new IllegalArgumentException ( "Negative 'width' argument." );
        }
        this.intervalDelegate.setFixedIntervalWidth ( width );
        this.fireDatasetChanged();
    }
    public double getIntervalPositionFactor() {
        return this.intervalDelegate.getIntervalPositionFactor();
    }
    public void setIntervalPositionFactor ( final double factor ) {
        this.intervalDelegate.setIntervalPositionFactor ( factor );
        this.fireDatasetChanged();
    }
    public boolean isAutoWidth() {
        return this.intervalDelegate.isAutoWidth();
    }
    public void setAutoWidth ( final boolean b ) {
        this.intervalDelegate.setAutoWidth ( b );
        this.fireDatasetChanged();
    }
    @Override
    public Range getRangeBounds ( final boolean includeInterval ) {
        double lower = Double.POSITIVE_INFINITY;
        double upper = Double.NEGATIVE_INFINITY;
        for ( int seriesCount = this.getSeriesCount(), s = 0; s < seriesCount; ++s ) {
            final XYSeries series = this.getSeries ( s );
            final double minY = series.getMinY();
            if ( !Double.isNaN ( minY ) ) {
                lower = Math.min ( lower, minY );
            }
            final double maxY = series.getMaxY();
            if ( !Double.isNaN ( maxY ) ) {
                upper = Math.max ( upper, maxY );
            }
        }
        if ( lower > upper ) {
            return null;
        }
        return new Range ( lower, upper );
    }
    @Override
    public double getRangeLowerBound ( final boolean includeInterval ) {
        double result = Double.NaN;
        for ( int seriesCount = this.getSeriesCount(), s = 0; s < seriesCount; ++s ) {
            final XYSeries series = this.getSeries ( s );
            final double lowY = series.getMinY();
            if ( Double.isNaN ( result ) ) {
                result = lowY;
            } else if ( !Double.isNaN ( lowY ) ) {
                result = Math.min ( result, lowY );
            }
        }
        return result;
    }
    @Override
    public double getRangeUpperBound ( final boolean includeInterval ) {
        double result = Double.NaN;
        for ( int seriesCount = this.getSeriesCount(), s = 0; s < seriesCount; ++s ) {
            final XYSeries series = this.getSeries ( s );
            final double hiY = series.getMaxY();
            if ( Double.isNaN ( result ) ) {
                result = hiY;
            } else if ( !Double.isNaN ( hiY ) ) {
                result = Math.max ( result, hiY );
            }
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
}
