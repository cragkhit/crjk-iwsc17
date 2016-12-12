package org.jfree.data.xy;
import java.util.Arrays;
import java.util.Date;
import org.jfree.util.PublicCloneable;
public class DefaultOHLCDataset extends AbstractXYDataset implements OHLCDataset, PublicCloneable {
    private Comparable key;
    private OHLCDataItem[] data;
    public DefaultOHLCDataset ( final Comparable key, final OHLCDataItem[] data ) {
        this.key = key;
        this.data = data;
    }
    public Comparable getSeriesKey ( final int series ) {
        return this.key;
    }
    public Number getX ( final int series, final int item ) {
        return new Long ( this.data[item].getDate().getTime() );
    }
    public Date getXDate ( final int series, final int item ) {
        return this.data[item].getDate();
    }
    public Number getY ( final int series, final int item ) {
        return this.getClose ( series, item );
    }
    @Override
    public Number getHigh ( final int series, final int item ) {
        return this.data[item].getHigh();
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
        return this.data[item].getLow();
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
        return this.data[item].getOpen();
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
        return this.data[item].getClose();
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
        return this.data[item].getVolume();
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
    public int getSeriesCount() {
        return 1;
    }
    public int getItemCount ( final int series ) {
        return this.data.length;
    }
    public void sortDataByDate() {
        Arrays.sort ( this.data );
    }
    public boolean equals ( final Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( ! ( obj instanceof DefaultOHLCDataset ) ) {
            return false;
        }
        final DefaultOHLCDataset that = ( DefaultOHLCDataset ) obj;
        return this.key.equals ( that.key ) && Arrays.equals ( this.data, that.data );
    }
    public Object clone() throws CloneNotSupportedException {
        final DefaultOHLCDataset clone = ( DefaultOHLCDataset ) super.clone();
        clone.data = new OHLCDataItem[this.data.length];
        System.arraycopy ( this.data, 0, clone.data, 0, this.data.length );
        return clone;
    }
}
