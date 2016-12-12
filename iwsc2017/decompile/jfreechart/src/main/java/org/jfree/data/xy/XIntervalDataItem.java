package org.jfree.data.xy;
import org.jfree.data.ComparableObjectItem;
public class XIntervalDataItem extends ComparableObjectItem {
    public XIntervalDataItem ( final double x, final double xLow, final double xHigh, final double y ) {
        super ( new Double ( x ), new YWithXInterval ( y, xLow, xHigh ) );
    }
    public Number getX() {
        return ( Number ) this.getComparable();
    }
    public double getYValue() {
        final YWithXInterval interval = ( YWithXInterval ) this.getObject();
        if ( interval != null ) {
            return interval.getY();
        }
        return Double.NaN;
    }
    public double getXLowValue() {
        final YWithXInterval interval = ( YWithXInterval ) this.getObject();
        if ( interval != null ) {
            return interval.getXLow();
        }
        return Double.NaN;
    }
    public double getXHighValue() {
        final YWithXInterval interval = ( YWithXInterval ) this.getObject();
        if ( interval != null ) {
            return interval.getXHigh();
        }
        return Double.NaN;
    }
}
