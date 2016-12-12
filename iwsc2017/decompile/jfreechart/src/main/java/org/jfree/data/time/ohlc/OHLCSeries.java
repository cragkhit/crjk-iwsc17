package org.jfree.data.time.ohlc;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.ComparableObjectItem;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.ComparableObjectSeries;
public class OHLCSeries extends ComparableObjectSeries {
    public OHLCSeries ( final Comparable key ) {
        super ( key, true, false );
    }
    public RegularTimePeriod getPeriod ( final int index ) {
        final OHLCItem item = ( OHLCItem ) this.getDataItem ( index );
        return item.getPeriod();
    }
    public ComparableObjectItem getDataItem ( final int index ) {
        return super.getDataItem ( index );
    }
    public void add ( final RegularTimePeriod period, final double open, final double high, final double low, final double close ) {
        if ( this.getItemCount() > 0 ) {
            final OHLCItem item0 = ( OHLCItem ) this.getDataItem ( 0 );
            if ( !period.getClass().equals ( item0.getPeriod().getClass() ) ) {
                throw new IllegalArgumentException ( "Can't mix RegularTimePeriod class types." );
            }
        }
        super.add ( new OHLCItem ( period, open, high, low, close ), true );
    }
    public void add ( final OHLCItem item ) {
        ParamChecks.nullNotPermitted ( item, "item" );
        this.add ( item.getPeriod(), item.getOpenValue(), item.getHighValue(), item.getLowValue(), item.getCloseValue() );
    }
    public ComparableObjectItem remove ( final int index ) {
        return super.remove ( index );
    }
}
