package org.jfree.data.general;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.AbstractIntervalXYDataset;
public class SubSeriesDataset extends AbstractIntervalXYDataset implements OHLCDataset, IntervalXYDataset, CombinationDataset {
    private SeriesDataset parent;
    private int[] map;
    public SubSeriesDataset ( final SeriesDataset parent, final int[] map ) {
        this.parent = null;
        this.parent = parent;
        this.map = map;
    }
    public SubSeriesDataset ( final SeriesDataset parent, final int series ) {
        this ( parent, new int[] { series } );
    }
    @Override
    public Number getHigh ( final int series, final int item ) {
        return ( ( OHLCDataset ) this.parent ).getHigh ( this.map[series], item );
    }
    @Override
    public double getHighValue ( final int series, final int item ) {
        double result = Double.NaN;
        final Number high = this.getHigh ( series, item );
        if ( high != null ) {
            result = high.doubleValue();
        }
        return result;
    }
    @Override
    public Number getLow ( final int series, final int item ) {
        return ( ( OHLCDataset ) this.parent ).getLow ( this.map[series], item );
    }
    @Override
    public double getLowValue ( final int series, final int item ) {
        double result = Double.NaN;
        final Number low = this.getLow ( series, item );
        if ( low != null ) {
            result = low.doubleValue();
        }
        return result;
    }
    @Override
    public Number getOpen ( final int series, final int item ) {
        return ( ( OHLCDataset ) this.parent ).getOpen ( this.map[series], item );
    }
    @Override
    public double getOpenValue ( final int series, final int item ) {
        double result = Double.NaN;
        final Number open = this.getOpen ( series, item );
        if ( open != null ) {
            result = open.doubleValue();
        }
        return result;
    }
    @Override
    public Number getClose ( final int series, final int item ) {
        return ( ( OHLCDataset ) this.parent ).getClose ( this.map[series], item );
    }
    @Override
    public double getCloseValue ( final int series, final int item ) {
        double result = Double.NaN;
        final Number close = this.getClose ( series, item );
        if ( close != null ) {
            result = close.doubleValue();
        }
        return result;
    }
    @Override
    public Number getVolume ( final int series, final int item ) {
        return ( ( OHLCDataset ) this.parent ).getVolume ( this.map[series], item );
    }
    @Override
    public double getVolumeValue ( final int series, final int item ) {
        double result = Double.NaN;
        final Number volume = this.getVolume ( series, item );
        if ( volume != null ) {
            result = volume.doubleValue();
        }
        return result;
    }
    @Override
    public Number getX ( final int series, final int item ) {
        return ( ( XYDataset ) this.parent ).getX ( this.map[series], item );
    }
    @Override
    public Number getY ( final int series, final int item ) {
        return ( ( XYDataset ) this.parent ).getY ( this.map[series], item );
    }
    @Override
    public int getItemCount ( final int series ) {
        return ( ( XYDataset ) this.parent ).getItemCount ( this.map[series] );
    }
    @Override
    public int getSeriesCount() {
        return this.map.length;
    }
    @Override
    public Comparable getSeriesKey ( final int series ) {
        return this.parent.getSeriesKey ( this.map[series] );
    }
    @Override
    public Number getStartX ( final int series, final int item ) {
        if ( this.parent instanceof IntervalXYDataset ) {
            return ( ( IntervalXYDataset ) this.parent ).getStartX ( this.map[series], item );
        }
        return this.getX ( series, item );
    }
    @Override
    public Number getEndX ( final int series, final int item ) {
        if ( this.parent instanceof IntervalXYDataset ) {
            return ( ( IntervalXYDataset ) this.parent ).getEndX ( this.map[series], item );
        }
        return this.getX ( series, item );
    }
    @Override
    public Number getStartY ( final int series, final int item ) {
        if ( this.parent instanceof IntervalXYDataset ) {
            return ( ( IntervalXYDataset ) this.parent ).getStartY ( this.map[series], item );
        }
        return this.getY ( series, item );
    }
    @Override
    public Number getEndY ( final int series, final int item ) {
        if ( this.parent instanceof IntervalXYDataset ) {
            return ( ( IntervalXYDataset ) this.parent ).getEndY ( this.map[series], item );
        }
        return this.getY ( series, item );
    }
    @Override
    public SeriesDataset getParent() {
        return this.parent;
    }
    @Override
    public int[] getMap() {
        return this.map.clone();
    }
}
