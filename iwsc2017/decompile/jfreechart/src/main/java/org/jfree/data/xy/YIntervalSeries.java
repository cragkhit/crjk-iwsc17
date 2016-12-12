package org.jfree.data.xy;
import org.jfree.data.ComparableObjectItem;
import org.jfree.data.ComparableObjectSeries;
public class YIntervalSeries extends ComparableObjectSeries {
    public YIntervalSeries ( final Comparable key ) {
        this ( key, true, true );
    }
    public YIntervalSeries ( final Comparable key, final boolean autoSort, final boolean allowDuplicateXValues ) {
        super ( key, autoSort, allowDuplicateXValues );
    }
    public void add ( final double x, final double y, final double yLow, final double yHigh ) {
        this.add ( new YIntervalDataItem ( x, y, yLow, yHigh ), true );
    }
    public void add ( final YIntervalDataItem item, final boolean notify ) {
        super.add ( item, notify );
    }
    public Number getX ( final int index ) {
        final YIntervalDataItem item = ( YIntervalDataItem ) this.getDataItem ( index );
        return item.getX();
    }
    public double getYValue ( final int index ) {
        final YIntervalDataItem item = ( YIntervalDataItem ) this.getDataItem ( index );
        return item.getYValue();
    }
    public double getYLowValue ( final int index ) {
        final YIntervalDataItem item = ( YIntervalDataItem ) this.getDataItem ( index );
        return item.getYLowValue();
    }
    public double getYHighValue ( final int index ) {
        final YIntervalDataItem item = ( YIntervalDataItem ) this.getDataItem ( index );
        return item.getYHighValue();
    }
    public ComparableObjectItem getDataItem ( final int index ) {
        return super.getDataItem ( index );
    }
}
