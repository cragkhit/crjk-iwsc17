

package org.jfree.data.general;

import org.jfree.data.xy.AbstractIntervalXYDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;


public class SubSeriesDataset extends AbstractIntervalXYDataset
    implements OHLCDataset, IntervalXYDataset, CombinationDataset {


    private SeriesDataset parent = null;


    private int[] map;


    public SubSeriesDataset ( SeriesDataset parent, int[] map ) {
        this.parent = parent;
        this.map = map;
    }


    public SubSeriesDataset ( SeriesDataset parent, int series ) {
        this ( parent, new int[] {series} );
    }



    @Override
    public Number getHigh ( int series, int item ) {
        return ( ( OHLCDataset ) this.parent ).getHigh ( this.map[series], item );
    }


    @Override
    public double getHighValue ( int series, int item ) {
        double result = Double.NaN;
        Number high = getHigh ( series, item );
        if ( high != null ) {
            result = high.doubleValue();
        }
        return result;
    }


    @Override
    public Number getLow ( int series, int item ) {
        return ( ( OHLCDataset ) this.parent ).getLow ( this.map[series], item );
    }


    @Override
    public double getLowValue ( int series, int item ) {
        double result = Double.NaN;
        Number low = getLow ( series, item );
        if ( low != null ) {
            result = low.doubleValue();
        }
        return result;
    }


    @Override
    public Number getOpen ( int series, int item ) {
        return ( ( OHLCDataset ) this.parent ).getOpen ( this.map[series], item );
    }


    @Override
    public double getOpenValue ( int series, int item ) {
        double result = Double.NaN;
        Number open = getOpen ( series, item );
        if ( open != null ) {
            result = open.doubleValue();
        }
        return result;
    }


    @Override
    public Number getClose ( int series, int item ) {
        return ( ( OHLCDataset ) this.parent ).getClose ( this.map[series], item );
    }


    @Override
    public double getCloseValue ( int series, int item ) {
        double result = Double.NaN;
        Number close = getClose ( series, item );
        if ( close != null ) {
            result = close.doubleValue();
        }
        return result;
    }


    @Override
    public Number getVolume ( int series, int item ) {
        return ( ( OHLCDataset ) this.parent ).getVolume ( this.map[series], item );
    }


    @Override
    public double getVolumeValue ( int series, int item ) {
        double result = Double.NaN;
        Number volume = getVolume ( series, item );
        if ( volume != null ) {
            result = volume.doubleValue();
        }
        return result;
    }



    @Override
    public Number getX ( int series, int item ) {
        return ( ( XYDataset ) this.parent ).getX ( this.map[series], item );
    }


    @Override
    public Number getY ( int series, int item ) {
        return ( ( XYDataset ) this.parent ).getY ( this.map[series], item );
    }


    @Override
    public int getItemCount ( int series ) {
        return ( ( XYDataset ) this.parent ).getItemCount ( this.map[series] );
    }



    @Override
    public int getSeriesCount() {
        return this.map.length;
    }


    @Override
    public Comparable getSeriesKey ( int series ) {
        return this.parent.getSeriesKey ( this.map[series] );
    }



    @Override
    public Number getStartX ( int series, int item ) {
        if ( this.parent instanceof IntervalXYDataset ) {
            return ( ( IntervalXYDataset ) this.parent ).getStartX (
                       this.map[series], item
                   );
        } else {
            return getX ( series, item );
        }
    }


    @Override
    public Number getEndX ( int series, int item ) {
        if ( this.parent instanceof IntervalXYDataset ) {
            return ( ( IntervalXYDataset ) this.parent ).getEndX (
                       this.map[series], item
                   );
        } else {
            return getX ( series, item );
        }
    }


    @Override
    public Number getStartY ( int series, int item ) {
        if ( this.parent instanceof IntervalXYDataset ) {
            return ( ( IntervalXYDataset ) this.parent ).getStartY (
                       this.map[series], item
                   );
        } else {
            return getY ( series, item );
        }
    }


    @Override
    public Number getEndY ( int series,  int item ) {
        if ( this.parent instanceof IntervalXYDataset ) {
            return ( ( IntervalXYDataset ) this.parent ).getEndY (
                       this.map[series], item
                   );
        } else {
            return getY ( series, item );
        }
    }



    @Override
    public SeriesDataset getParent() {
        return this.parent;
    }


    @Override
    public int[] getMap() {
        return ( int[] ) this.map.clone();
    }

}
