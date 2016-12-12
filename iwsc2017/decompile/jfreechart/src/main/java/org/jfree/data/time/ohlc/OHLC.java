package org.jfree.data.time.ohlc;
import org.jfree.chart.HashUtilities;
import java.io.Serializable;
public class OHLC implements Serializable {
    private double open;
    private double close;
    private double high;
    private double low;
    public OHLC ( final double open, final double high, final double low, final double close ) {
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
    }
    public double getOpen() {
        return this.open;
    }
    public double getClose() {
        return this.close;
    }
    public double getHigh() {
        return this.high;
    }
    public double getLow() {
        return this.low;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof OHLC ) ) {
            return false;
        }
        final OHLC that = ( OHLC ) obj;
        return this.open == that.open && this.close == that.close && this.high == that.high && this.low == that.low;
    }
    @Override
    public int hashCode() {
        int result = 193;
        result = HashUtilities.hashCode ( result, this.open );
        result = HashUtilities.hashCode ( result, this.high );
        result = HashUtilities.hashCode ( result, this.low );
        result = HashUtilities.hashCode ( result, this.close );
        return result;
    }
}
