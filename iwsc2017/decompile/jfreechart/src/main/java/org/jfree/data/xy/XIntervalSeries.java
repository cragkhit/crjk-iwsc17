package org.jfree.data.xy;
import org.jfree.data.ComparableObjectItem;
import org.jfree.data.ComparableObjectSeries;
public class XIntervalSeries extends ComparableObjectSeries {
    public XIntervalSeries ( final Comparable key ) {
        this ( key, true, true );
    }
    public XIntervalSeries ( final Comparable key, final boolean autoSort, final boolean allowDuplicateXValues ) {
        super ( key, autoSort, allowDuplicateXValues );
    }
    public void add ( final double x, final double xLow, final double xHigh, final double y ) {
        this.add ( new XIntervalDataItem ( x, xLow, xHigh, y ), true );
    }
    public void add ( final XIntervalDataItem item, final boolean notify ) {
        super.add ( item, notify );
    }
    public Number getX ( final int index ) {
        final XIntervalDataItem item = ( XIntervalDataItem ) this.getDataItem ( index );
        return item.getX();
    }
    public double getXLowValue ( final int index ) {
        final XIntervalDataItem item = ( XIntervalDataItem ) this.getDataItem ( index );
        return item.getXLowValue();
    }
    public double getXHighValue ( final int index ) {
        final XIntervalDataItem item = ( XIntervalDataItem ) this.getDataItem ( index );
        return item.getXHighValue();
    }
    public double getYValue ( final int index ) {
        final XIntervalDataItem item = ( XIntervalDataItem ) this.getDataItem ( index );
        return item.getYValue();
    }
    public ComparableObjectItem getDataItem ( final int index ) {
        return super.getDataItem ( index );
    }
}
