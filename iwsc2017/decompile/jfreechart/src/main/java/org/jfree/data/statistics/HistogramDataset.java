package org.jfree.data.statistics;
import org.jfree.util.ObjectUtilities;
import java.util.Map;
import java.util.HashMap;
import org.jfree.chart.util.ParamChecks;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.AbstractIntervalXYDataset;
public class HistogramDataset extends AbstractIntervalXYDataset implements IntervalXYDataset, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = -6341668077370231153L;
    private List list;
    private HistogramType type;
    public HistogramDataset() {
        this.list = new ArrayList();
        this.type = HistogramType.FREQUENCY;
    }
    public HistogramType getType() {
        return this.type;
    }
    public void setType ( final HistogramType type ) {
        ParamChecks.nullNotPermitted ( type, "type" );
        this.type = type;
        this.fireDatasetChanged();
    }
    public void addSeries ( final Comparable key, final double[] values, final int bins ) {
        final double minimum = this.getMinimum ( values );
        final double maximum = this.getMaximum ( values );
        this.addSeries ( key, values, bins, minimum, maximum );
    }
    public void addSeries ( final Comparable key, final double[] values, final int bins, final double minimum, final double maximum ) {
        ParamChecks.nullNotPermitted ( key, "key" );
        ParamChecks.nullNotPermitted ( values, "values" );
        if ( bins < 1 ) {
            throw new IllegalArgumentException ( "The 'bins' value must be at least 1." );
        }
        final double binWidth = ( maximum - minimum ) / bins;
        double lower = minimum;
        final List binList = new ArrayList ( bins );
        for ( int i = 0; i < bins; ++i ) {
            HistogramBin bin;
            if ( i == bins - 1 ) {
                bin = new HistogramBin ( lower, maximum );
            } else {
                final double upper = minimum + ( i + 1 ) * binWidth;
                bin = new HistogramBin ( lower, upper );
                lower = upper;
            }
            binList.add ( bin );
        }
        for ( int i = 0; i < values.length; ++i ) {
            int binIndex = bins - 1;
            if ( values[i] < maximum ) {
                double fraction = ( values[i] - minimum ) / ( maximum - minimum );
                if ( fraction < 0.0 ) {
                    fraction = 0.0;
                }
                binIndex = ( int ) ( fraction * bins );
                if ( binIndex >= bins ) {
                    binIndex = bins - 1;
                }
            }
            final HistogramBin bin2 = binList.get ( binIndex );
            bin2.incrementCount();
        }
        final Map map = new HashMap();
        map.put ( "key", key );
        map.put ( "bins", binList );
        map.put ( "values.length", new Integer ( values.length ) );
        map.put ( "bin width", new Double ( binWidth ) );
        this.list.add ( map );
        this.fireDatasetChanged();
    }
    private double getMinimum ( final double[] values ) {
        if ( values == null || values.length < 1 ) {
            throw new IllegalArgumentException ( "Null or zero length 'values' argument." );
        }
        double min = Double.MAX_VALUE;
        for ( int i = 0; i < values.length; ++i ) {
            if ( values[i] < min ) {
                min = values[i];
            }
        }
        return min;
    }
    private double getMaximum ( final double[] values ) {
        if ( values == null || values.length < 1 ) {
            throw new IllegalArgumentException ( "Null or zero length 'values' argument." );
        }
        double max = -1.7976931348623157E308;
        for ( int i = 0; i < values.length; ++i ) {
            if ( values[i] > max ) {
                max = values[i];
            }
        }
        return max;
    }
    List getBins ( final int series ) {
        final Map map = this.list.get ( series );
        return map.get ( "bins" );
    }
    private int getTotal ( final int series ) {
        final Map map = this.list.get ( series );
        return map.get ( "values.length" );
    }
    private double getBinWidth ( final int series ) {
        final Map map = this.list.get ( series );
        return map.get ( "bin width" );
    }
    public int getSeriesCount() {
        return this.list.size();
    }
    public Comparable getSeriesKey ( final int series ) {
        final Map map = this.list.get ( series );
        return map.get ( "key" );
    }
    public int getItemCount ( final int series ) {
        return this.getBins ( series ).size();
    }
    public Number getX ( final int series, final int item ) {
        final List bins = this.getBins ( series );
        final HistogramBin bin = bins.get ( item );
        final double x = ( bin.getStartBoundary() + bin.getEndBoundary() ) / 2.0;
        return new Double ( x );
    }
    public Number getY ( final int series, final int item ) {
        final List bins = this.getBins ( series );
        final HistogramBin bin = bins.get ( item );
        final double total = this.getTotal ( series );
        final double binWidth = this.getBinWidth ( series );
        if ( this.type == HistogramType.FREQUENCY ) {
            return new Double ( bin.getCount() );
        }
        if ( this.type == HistogramType.RELATIVE_FREQUENCY ) {
            return new Double ( bin.getCount() / total );
        }
        if ( this.type == HistogramType.SCALE_AREA_TO_1 ) {
            return new Double ( bin.getCount() / ( binWidth * total ) );
        }
        throw new IllegalStateException();
    }
    @Override
    public Number getStartX ( final int series, final int item ) {
        final List bins = this.getBins ( series );
        final HistogramBin bin = bins.get ( item );
        return new Double ( bin.getStartBoundary() );
    }
    @Override
    public Number getEndX ( final int series, final int item ) {
        final List bins = this.getBins ( series );
        final HistogramBin bin = bins.get ( item );
        return new Double ( bin.getEndBoundary() );
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
        if ( ! ( obj instanceof HistogramDataset ) ) {
            return false;
        }
        final HistogramDataset that = ( HistogramDataset ) obj;
        return ObjectUtilities.equal ( ( Object ) this.type, ( Object ) that.type ) && ObjectUtilities.equal ( ( Object ) this.list, ( Object ) that.list );
    }
    public Object clone() throws CloneNotSupportedException {
        final HistogramDataset clone = ( HistogramDataset ) super.clone();
        final int seriesCount = this.getSeriesCount();
        clone.list = new ArrayList ( seriesCount );
        for ( int i = 0; i < seriesCount; ++i ) {
            clone.list.add ( new HashMap ( this.list.get ( i ) ) );
        }
        return clone;
    }
}
