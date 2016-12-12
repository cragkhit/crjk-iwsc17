package org.jfree.data.xy;
import org.jfree.data.ComparableObjectItem;
public class YIntervalDataItem extends ComparableObjectItem {
    public YIntervalDataItem ( final double x, final double y, final double yLow, final double yHigh ) {
        super ( new Double ( x ), new YInterval ( y, yLow, yHigh ) );
    }
    public Double getX() {
        return ( Double ) this.getComparable();
    }
    public double getYValue() {
        final YInterval interval = ( YInterval ) this.getObject();
        if ( interval != null ) {
            return interval.getY();
        }
        return Double.NaN;
    }
    public double getYLowValue() {
        final YInterval interval = ( YInterval ) this.getObject();
        if ( interval != null ) {
            return interval.getYLow();
        }
        return Double.NaN;
    }
    public double getYHighValue() {
        final YInterval interval = ( YInterval ) this.getObject();
        if ( interval != null ) {
            return interval.getYHigh();
        }
        return Double.NaN;
    }
}
