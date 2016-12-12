package org.jfree.data.xy;
import org.jfree.chart.HashUtilities;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.Range;
import org.jfree.chart.util.ParamChecks;
import org.jfree.util.PublicCloneable;
import java.io.Serializable;
import org.jfree.data.DomainInfo;
import org.jfree.data.general.DatasetChangeListener;
public class IntervalXYDelegate implements DatasetChangeListener, DomainInfo, Serializable, Cloneable, PublicCloneable {
    private static final long serialVersionUID = -685166711639592857L;
    private XYDataset dataset;
    private boolean autoWidth;
    private double intervalPositionFactor;
    private double fixedIntervalWidth;
    private double autoIntervalWidth;
    public IntervalXYDelegate ( final XYDataset dataset ) {
        this ( dataset, true );
    }
    public IntervalXYDelegate ( final XYDataset dataset, final boolean autoWidth ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        this.dataset = dataset;
        this.autoWidth = autoWidth;
        this.intervalPositionFactor = 0.5;
        this.autoIntervalWidth = Double.POSITIVE_INFINITY;
        this.fixedIntervalWidth = 1.0;
    }
    public boolean isAutoWidth() {
        return this.autoWidth;
    }
    public void setAutoWidth ( final boolean b ) {
        this.autoWidth = b;
        if ( b ) {
            this.autoIntervalWidth = this.recalculateInterval();
        }
    }
    public double getIntervalPositionFactor() {
        return this.intervalPositionFactor;
    }
    public void setIntervalPositionFactor ( final double d ) {
        if ( d < 0.0 || 1.0 < d ) {
            throw new IllegalArgumentException ( "Argument 'd' outside valid range." );
        }
        this.intervalPositionFactor = d;
    }
    public double getFixedIntervalWidth() {
        return this.fixedIntervalWidth;
    }
    public void setFixedIntervalWidth ( final double w ) {
        if ( w < 0.0 ) {
            throw new IllegalArgumentException ( "Negative 'w' argument." );
        }
        this.fixedIntervalWidth = w;
        this.autoWidth = false;
    }
    public double getIntervalWidth() {
        if ( this.isAutoWidth() && !Double.isInfinite ( this.autoIntervalWidth ) ) {
            return this.autoIntervalWidth;
        }
        return this.fixedIntervalWidth;
    }
    public Number getStartX ( final int series, final int item ) {
        Number startX = null;
        final Number x = this.dataset.getX ( series, item );
        if ( x != null ) {
            startX = new Double ( x.doubleValue() - this.getIntervalPositionFactor() * this.getIntervalWidth() );
        }
        return startX;
    }
    public double getStartXValue ( final int series, final int item ) {
        return this.dataset.getXValue ( series, item ) - this.getIntervalPositionFactor() * this.getIntervalWidth();
    }
    public Number getEndX ( final int series, final int item ) {
        Number endX = null;
        final Number x = this.dataset.getX ( series, item );
        if ( x != null ) {
            endX = new Double ( x.doubleValue() + ( 1.0 - this.getIntervalPositionFactor() ) * this.getIntervalWidth() );
        }
        return endX;
    }
    public double getEndXValue ( final int series, final int item ) {
        return this.dataset.getXValue ( series, item ) + ( 1.0 - this.getIntervalPositionFactor() ) * this.getIntervalWidth();
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
        Range range = DatasetUtilities.findDomainBounds ( this.dataset, false );
        if ( includeInterval && range != null ) {
            final double lowerAdj = this.getIntervalWidth() * this.getIntervalPositionFactor();
            final double upperAdj = this.getIntervalWidth() - lowerAdj;
            range = new Range ( range.getLowerBound() - lowerAdj, range.getUpperBound() + upperAdj );
        }
        return range;
    }
    @Override
    public void datasetChanged ( final DatasetChangeEvent e ) {
        if ( this.autoWidth ) {
            this.autoIntervalWidth = this.recalculateInterval();
        }
    }
    private double recalculateInterval() {
        double result = Double.POSITIVE_INFINITY;
        for ( int seriesCount = this.dataset.getSeriesCount(), series = 0; series < seriesCount; ++series ) {
            result = Math.min ( result, this.calculateIntervalForSeries ( series ) );
        }
        return result;
    }
    private double calculateIntervalForSeries ( final int series ) {
        double result = Double.POSITIVE_INFINITY;
        final int itemCount = this.dataset.getItemCount ( series );
        if ( itemCount > 1 ) {
            double prev = this.dataset.getXValue ( series, 0 );
            for ( int item = 1; item < itemCount; ++item ) {
                final double x = this.dataset.getXValue ( series, item );
                result = Math.min ( result, x - prev );
                prev = x;
            }
        }
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof IntervalXYDelegate ) ) {
            return false;
        }
        final IntervalXYDelegate that = ( IntervalXYDelegate ) obj;
        return this.autoWidth == that.autoWidth && this.intervalPositionFactor == that.intervalPositionFactor && this.fixedIntervalWidth == that.fixedIntervalWidth;
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    @Override
    public int hashCode() {
        int hash = 5;
        hash = HashUtilities.hashCode ( hash, this.autoWidth );
        hash = HashUtilities.hashCode ( hash, this.intervalPositionFactor );
        hash = HashUtilities.hashCode ( hash, this.fixedIntervalWidth );
        return hash;
    }
}
