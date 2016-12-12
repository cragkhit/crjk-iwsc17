package org.jfree.data.xy;
import org.jfree.data.ComparableObjectItem;
import org.jfree.data.ComparableObjectSeries;
public class XYIntervalSeries extends ComparableObjectSeries {
    public XYIntervalSeries ( final Comparable key ) {
        this ( key, true, true );
    }
    public XYIntervalSeries ( final Comparable key, final boolean autoSort, final boolean allowDuplicateXValues ) {
        super ( key, autoSort, allowDuplicateXValues );
    }
    public void add ( final double x, final double xLow, final double xHigh, final double y, final double yLow, final double yHigh ) {
        this.add ( new XYIntervalDataItem ( x, xLow, xHigh, y, yLow, yHigh ), true );
    }
    public void add ( final XYIntervalDataItem item, final boolean notify ) {
        super.add ( item, notify );
    }
    public Number getX ( final int index ) {
        final XYIntervalDataItem item = ( XYIntervalDataItem ) this.getDataItem ( index );
        return item.getX();
    }
    public double getXLowValue ( final int index ) {
        final XYIntervalDataItem item = ( XYIntervalDataItem ) this.getDataItem ( index );
        return item.getXLowValue();
    }
    public double getXHighValue ( final int index ) {
        final XYIntervalDataItem item = ( XYIntervalDataItem ) this.getDataItem ( index );
        return item.getXHighValue();
    }
    public double getYValue ( final int index ) {
        final XYIntervalDataItem item = ( XYIntervalDataItem ) this.getDataItem ( index );
        return item.getYValue();
    }
    public double getYLowValue ( final int index ) {
        final XYIntervalDataItem item = ( XYIntervalDataItem ) this.getDataItem ( index );
        return item.getYLowValue();
    }
    public double getYHighValue ( final int index ) {
        final XYIntervalDataItem item = ( XYIntervalDataItem ) this.getDataItem ( index );
        return item.getYHighValue();
    }
    public ComparableObjectItem getDataItem ( final int index ) {
        return super.getDataItem ( index );
    }
}
