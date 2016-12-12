package org.jfree.data.statistics;
import java.util.Collection;
import org.jfree.util.ObjectUtilities;
import java.util.Iterator;
import java.util.Collections;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetChangeEvent;
import java.util.ArrayList;
import org.jfree.chart.util.ParamChecks;
import java.util.List;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.AbstractIntervalXYDataset;
public class SimpleHistogramDataset extends AbstractIntervalXYDataset implements IntervalXYDataset, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 7997996479768018443L;
    private Comparable key;
    private List bins;
    private boolean adjustForBinSize;
    public SimpleHistogramDataset ( final Comparable key ) {
        ParamChecks.nullNotPermitted ( key, "key" );
        this.key = key;
        this.bins = new ArrayList();
        this.adjustForBinSize = true;
    }
    public boolean getAdjustForBinSize() {
        return this.adjustForBinSize;
    }
    public void setAdjustForBinSize ( final boolean adjust ) {
        this.adjustForBinSize = adjust;
        this.notifyListeners ( new DatasetChangeEvent ( this, this ) );
    }
    public int getSeriesCount() {
        return 1;
    }
    public Comparable getSeriesKey ( final int series ) {
        return this.key;
    }
    public DomainOrder getDomainOrder() {
        return DomainOrder.ASCENDING;
    }
    public int getItemCount ( final int series ) {
        return this.bins.size();
    }
    public void addBin ( final SimpleHistogramBin bin ) {
        for ( final SimpleHistogramBin existingBin : this.bins ) {
            if ( bin.overlapsWith ( existingBin ) ) {
                throw new RuntimeException ( "Overlapping bin" );
            }
        }
        this.bins.add ( bin );
        Collections.sort ( ( List<Comparable> ) this.bins );
    }
    public void addObservation ( final double value ) {
        this.addObservation ( value, true );
    }
    public void addObservation ( final double value, final boolean notify ) {
        boolean placed = false;
        for ( Iterator iterator = this.bins.iterator(); iterator.hasNext() && !placed; placed = true ) {
            final SimpleHistogramBin bin = iterator.next();
            if ( bin.accepts ( value ) ) {
                bin.setItemCount ( bin.getItemCount() + 1 );
            }
        }
        if ( !placed ) {
            throw new RuntimeException ( "No bin." );
        }
        if ( notify ) {
            this.notifyListeners ( new DatasetChangeEvent ( this, this ) );
        }
    }
    public void addObservations ( final double[] values ) {
        for ( int i = 0; i < values.length; ++i ) {
            this.addObservation ( values[i], false );
        }
        this.notifyListeners ( new DatasetChangeEvent ( this, this ) );
    }
    public void clearObservations() {
        for ( final SimpleHistogramBin bin : this.bins ) {
            bin.setItemCount ( 0 );
        }
        this.notifyListeners ( new DatasetChangeEvent ( this, this ) );
    }
    public void removeAllBins() {
        this.bins = new ArrayList();
        this.notifyListeners ( new DatasetChangeEvent ( this, this ) );
    }
    public Number getX ( final int series, final int item ) {
        return new Double ( this.getXValue ( series, item ) );
    }
    public double getXValue ( final int series, final int item ) {
        final SimpleHistogramBin bin = this.bins.get ( item );
        return ( bin.getLowerBound() + bin.getUpperBound() ) / 2.0;
    }
    public Number getY ( final int series, final int item ) {
        return new Double ( this.getYValue ( series, item ) );
    }
    public double getYValue ( final int series, final int item ) {
        final SimpleHistogramBin bin = this.bins.get ( item );
        if ( this.adjustForBinSize ) {
            return bin.getItemCount() / ( bin.getUpperBound() - bin.getLowerBound() );
        }
        return bin.getItemCount();
    }
    @Override
    public Number getStartX ( final int series, final int item ) {
        return new Double ( this.getStartXValue ( series, item ) );
    }
    @Override
    public double getStartXValue ( final int series, final int item ) {
        final SimpleHistogramBin bin = this.bins.get ( item );
        return bin.getLowerBound();
    }
    @Override
    public Number getEndX ( final int series, final int item ) {
        return new Double ( this.getEndXValue ( series, item ) );
    }
    @Override
    public double getEndXValue ( final int series, final int item ) {
        final SimpleHistogramBin bin = this.bins.get ( item );
        return bin.getUpperBound();
    }
    @Override
    public Number getStartY ( final int series, final int item ) {
        return this.getY ( series, item );
    }
    @Override
    public double getStartYValue ( final int series, final int item ) {
        return this.getYValue ( series, item );
    }
    @Override
    public Number getEndY ( final int series, final int item ) {
        return this.getY ( series, item );
    }
    @Override
    public double getEndYValue ( final int series, final int item ) {
        return this.getYValue ( series, item );
    }
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof SimpleHistogramDataset ) ) {
            return false;
        }
        final SimpleHistogramDataset that = ( SimpleHistogramDataset ) obj;
        return this.key.equals ( that.key ) && this.adjustForBinSize == that.adjustForBinSize && this.bins.equals ( that.bins );
    }
    public Object clone() throws CloneNotSupportedException {
        final SimpleHistogramDataset clone = ( SimpleHistogramDataset ) super.clone();
        clone.bins = ( List ) ObjectUtilities.deepClone ( ( Collection ) this.bins );
        return clone;
    }
}
