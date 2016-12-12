package org.jfree.data.xy;
import org.jfree.chart.util.ParamChecks;
import java.util.Date;
import java.io.Serializable;
public class OHLCDataItem implements Comparable, Serializable {
    private static final long serialVersionUID = 7753817154401169901L;
    private Date date;
    private Number open;
    private Number high;
    private Number low;
    private Number close;
    private Number volume;
    public OHLCDataItem ( final Date date, final double open, final double high, final double low, final double close, final double volume ) {
        ParamChecks.nullNotPermitted ( date, "date" );
        this.date = date;
        this.open = new Double ( open );
        this.high = new Double ( high );
        this.low = new Double ( low );
        this.close = new Double ( close );
        this.volume = new Double ( volume );
    }
    public Date getDate() {
        return this.date;
    }
    public Number getOpen() {
        return this.open;
    }
    public Number getHigh() {
        return this.high;
    }
    public Number getLow() {
        return this.low;
    }
    public Number getClose() {
        return this.close;
    }
    public Number getVolume() {
        return this.volume;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof OHLCDataItem ) ) {
            return false;
        }
        final OHLCDataItem that = ( OHLCDataItem ) obj;
        return this.date.equals ( that.date ) && this.high.equals ( that.high ) && this.low.equals ( that.low ) && this.open.equals ( that.open ) && this.close.equals ( that.close );
    }
    @Override
    public int compareTo ( final Object object ) {
        if ( object instanceof OHLCDataItem ) {
            final OHLCDataItem item = ( OHLCDataItem ) object;
            return this.date.compareTo ( item.date );
        }
        throw new ClassCastException ( "OHLCDataItem.compareTo()." );
    }
}
