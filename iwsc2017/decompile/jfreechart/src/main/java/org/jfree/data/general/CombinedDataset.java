package org.jfree.data.general;
import java.util.ArrayList;
import java.util.List;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.AbstractIntervalXYDataset;
public class CombinedDataset extends AbstractIntervalXYDataset implements XYDataset, OHLCDataset, IntervalXYDataset, CombinationDataset {
    private List datasetInfo;
    public CombinedDataset() {
        this.datasetInfo = new ArrayList();
    }
    public CombinedDataset ( final SeriesDataset[] data ) {
        this.datasetInfo = new ArrayList();
        this.add ( data );
    }
    public void add ( final SeriesDataset data ) {
        this.fastAdd ( data );
        final DatasetChangeEvent event = new DatasetChangeEvent ( this, this );
        this.notifyListeners ( event );
    }
    public void add ( final SeriesDataset[] data ) {
        for ( int i = 0; i < data.length; ++i ) {
            this.fastAdd ( data[i] );
        }
        final DatasetChangeEvent event = new DatasetChangeEvent ( this, this );
        this.notifyListeners ( event );
    }
    public void add ( final SeriesDataset data, final int series ) {
        this.add ( new SubSeriesDataset ( data, series ) );
    }
    private void fastAdd ( final SeriesDataset data ) {
        for ( int i = 0; i < data.getSeriesCount(); ++i ) {
            this.datasetInfo.add ( new DatasetInfo ( data, i ) );
        }
    }
    @Override
    public int getSeriesCount() {
        return this.datasetInfo.size();
    }
    @Override
    public Comparable getSeriesKey ( final int series ) {
        final DatasetInfo di = this.getDatasetInfo ( series );
        return di.data.getSeriesKey ( di.series );
    }
    @Override
    public Number getX ( final int series, final int item ) {
        final DatasetInfo di = this.getDatasetInfo ( series );
        return ( ( XYDataset ) di.data ).getX ( di.series, item );
    }
    @Override
    public Number getY ( final int series, final int item ) {
        final DatasetInfo di = this.getDatasetInfo ( series );
        return ( ( XYDataset ) di.data ).getY ( di.series, item );
    }
    @Override
    public int getItemCount ( final int series ) {
        final DatasetInfo di = this.getDatasetInfo ( series );
        return ( ( XYDataset ) di.data ).getItemCount ( di.series );
    }
    @Override
    public Number getHigh ( final int series, final int item ) {
        final DatasetInfo di = this.getDatasetInfo ( series );
        return ( ( OHLCDataset ) di.data ).getHigh ( di.series, item );
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
        final DatasetInfo di = this.getDatasetInfo ( series );
        return ( ( OHLCDataset ) di.data ).getLow ( di.series, item );
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
        final DatasetInfo di = this.getDatasetInfo ( series );
        return ( ( OHLCDataset ) di.data ).getOpen ( di.series, item );
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
        final DatasetInfo di = this.getDatasetInfo ( series );
        return ( ( OHLCDataset ) di.data ).getClose ( di.series, item );
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
        final DatasetInfo di = this.getDatasetInfo ( series );
        return ( ( OHLCDataset ) di.data ).getVolume ( di.series, item );
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
    public Number getStartX ( final int series, final int item ) {
        final DatasetInfo di = this.getDatasetInfo ( series );
        if ( di.data instanceof IntervalXYDataset ) {
            return ( ( IntervalXYDataset ) di.data ).getStartX ( di.series, item );
        }
        return this.getX ( series, item );
    }
    @Override
    public Number getEndX ( final int series, final int item ) {
        final DatasetInfo di = this.getDatasetInfo ( series );
        if ( di.data instanceof IntervalXYDataset ) {
            return ( ( IntervalXYDataset ) di.data ).getEndX ( di.series, item );
        }
        return this.getX ( series, item );
    }
    @Override
    public Number getStartY ( final int series, final int item ) {
        final DatasetInfo di = this.getDatasetInfo ( series );
        if ( di.data instanceof IntervalXYDataset ) {
            return ( ( IntervalXYDataset ) di.data ).getStartY ( di.series, item );
        }
        return this.getY ( series, item );
    }
    @Override
    public Number getEndY ( final int series, final int item ) {
        final DatasetInfo di = this.getDatasetInfo ( series );
        if ( di.data instanceof IntervalXYDataset ) {
            return ( ( IntervalXYDataset ) di.data ).getEndY ( di.series, item );
        }
        return this.getY ( series, item );
    }
    @Override
    public SeriesDataset getParent() {
        SeriesDataset parent = null;
        for ( int i = 0; i < this.datasetInfo.size(); ++i ) {
            final SeriesDataset child = this.getDatasetInfo ( i ).data;
            if ( ! ( child instanceof CombinationDataset ) ) {
                return null;
            }
            final SeriesDataset childParent = ( ( CombinationDataset ) child ).getParent();
            if ( parent == null ) {
                parent = childParent;
            } else if ( parent != childParent ) {
                return null;
            }
        }
        return parent;
    }
    @Override
    public int[] getMap() {
        int[] map = null;
        for ( int i = 0; i < this.datasetInfo.size(); ++i ) {
            final SeriesDataset child = this.getDatasetInfo ( i ).data;
            if ( ! ( child instanceof CombinationDataset ) ) {
                return null;
            }
            final int[] childMap = ( ( CombinationDataset ) child ).getMap();
            if ( childMap == null ) {
                return null;
            }
            map = this.joinMap ( map, childMap );
        }
        return map;
    }
    public int getChildPosition ( final Dataset child ) {
        int n = 0;
        for ( int i = 0; i < this.datasetInfo.size(); ++i ) {
            final SeriesDataset childDataset = this.getDatasetInfo ( i ).data;
            if ( childDataset instanceof CombinedDataset ) {
                final int m = ( ( CombinedDataset ) childDataset ).getChildPosition ( child );
                if ( m >= 0 ) {
                    return n + m;
                }
                ++n;
            } else {
                if ( child == childDataset ) {
                    return n;
                }
                ++n;
            }
        }
        return -1;
    }
    private DatasetInfo getDatasetInfo ( final int series ) {
        return this.datasetInfo.get ( series );
    }
    private int[] joinMap ( final int[] a, final int[] b ) {
        if ( a == null ) {
            return b;
        }
        if ( b == null ) {
            return a;
        }
        final int[] result = new int[a.length + b.length];
        System.arraycopy ( a, 0, result, 0, a.length );
        System.arraycopy ( b, 0, result, a.length, b.length );
        return result;
    }
    private class DatasetInfo {
        private SeriesDataset data;
        private int series;
        DatasetInfo ( final SeriesDataset data, final int series ) {
            this.data = data;
            this.series = series;
        }
    }
}
