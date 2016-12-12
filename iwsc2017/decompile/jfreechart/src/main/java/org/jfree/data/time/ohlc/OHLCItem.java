package org.jfree.data.time.ohlc;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.ComparableObjectItem;
public class OHLCItem extends ComparableObjectItem {
    public OHLCItem ( final RegularTimePeriod period, final double open, final double high, final double low, final double close ) {
        super ( period, new OHLC ( open, high, low, close ) );
    }
    public RegularTimePeriod getPeriod() {
        return ( RegularTimePeriod ) this.getComparable();
    }
    public double getYValue() {
        return this.getCloseValue();
    }
    public double getOpenValue() {
        final OHLC ohlc = ( OHLC ) this.getObject();
        if ( ohlc != null ) {
            return ohlc.getOpen();
        }
        return Double.NaN;
    }
    public double getHighValue() {
        final OHLC ohlc = ( OHLC ) this.getObject();
        if ( ohlc != null ) {
            return ohlc.getHigh();
        }
        return Double.NaN;
    }
    public double getLowValue() {
        final OHLC ohlc = ( OHLC ) this.getObject();
        if ( ohlc != null ) {
            return ohlc.getLow();
        }
        return Double.NaN;
    }
    public double getCloseValue() {
        final OHLC ohlc = ( OHLC ) this.getObject();
        if ( ohlc != null ) {
            return ohlc.getClose();
        }
        return Double.NaN;
    }
}
