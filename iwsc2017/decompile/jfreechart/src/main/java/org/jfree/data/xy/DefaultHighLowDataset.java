package org.jfree.data.xy;
import java.util.Arrays;
import org.jfree.chart.util.ParamChecks;
import java.util.Date;
import org.jfree.util.PublicCloneable;
public class DefaultHighLowDataset extends AbstractXYDataset implements OHLCDataset, PublicCloneable {
    private Comparable seriesKey;
    private Date[] date;
    private Number[] high;
    private Number[] low;
    private Number[] open;
    private Number[] close;
    private Number[] volume;
    public DefaultHighLowDataset ( final Comparable seriesKey, final Date[] date, final double[] high, final double[] low, final double[] open, final double[] close, final double[] volume ) {
        ParamChecks.nullNotPermitted ( seriesKey, "seriesKey" );
        ParamChecks.nullNotPermitted ( date, "date" );
        this.seriesKey = seriesKey;
        this.date = date;
        this.high = createNumberArray ( high );
        this.low = createNumberArray ( low );
        this.open = createNumberArray ( open );
        this.close = createNumberArray ( close );
        this.volume = createNumberArray ( volume );
    }
    public Comparable getSeriesKey ( final int series ) {
        return this.seriesKey;
    }
    public Number getX ( final int series, final int item ) {
        return new Long ( this.date[item].getTime() );
    }
    public Date getXDate ( final int series, final int item ) {
        return this.date[item];
    }
    public Number getY ( final int series, final int item ) {
        return this.getClose ( series, item );
    }
    @Override
    public Number getHigh ( final int series, final int item ) {
        return this.high[item];
    }
    @Override
    public double getHighValue ( final int series, final int item ) {
        double result = Double.NaN;
        final Number h = this.getHigh ( series, item );
        if ( h != null ) {
            result = h.doubleValue();
        }
        return result;
    }
    @Override
    public Number getLow ( final int series, final int item ) {
        return this.low[item];
    }
    @Override
    public double getLowValue ( final int series, final int item ) {
        double result = Double.NaN;
        final Number l = this.getLow ( series, item );
        if ( l != null ) {
            result = l.doubleValue();
        }
        return result;
    }
    @Override
    public Number getOpen ( final int series, final int item ) {
        return this.open[item];
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
        return this.close[item];
    }
    @Override
    public double getCloseValue ( final int series, final int item ) {
        double result = Double.NaN;
        final Number c = this.getClose ( series, item );
        if ( c != null ) {
            result = c.doubleValue();
        }
        return result;
    }
    @Override
    public Number getVolume ( final int series, final int item ) {
        return this.volume[item];
    }
    @Override
    public double getVolumeValue ( final int series, final int item ) {
        double result = Double.NaN;
        final Number v = this.getVolume ( series, item );
        if ( v != null ) {
            result = v.doubleValue();
        }
        return result;
    }
    public int getSeriesCount() {
        return 1;
    }
    public int getItemCount ( final int series ) {
        return this.date.length;
    }
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof DefaultHighLowDataset ) ) {
            return false;
        }
        final DefaultHighLowDataset that = ( DefaultHighLowDataset ) obj;
        return this.seriesKey.equals ( that.seriesKey ) && Arrays.equals ( this.date, that.date ) && Arrays.equals ( this.open, that.open ) && Arrays.equals ( this.high, that.high ) && Arrays.equals ( this.low, that.low ) && Arrays.equals ( this.close, that.close ) && Arrays.equals ( this.volume, that.volume );
    }
    public static Number[] createNumberArray ( final double[] data ) {
        final Number[] result = new Number[data.length];
        for ( int i = 0; i < data.length; ++i ) {
            result[i] = new Double ( data[i] );
        }
        return result;
    }
}
