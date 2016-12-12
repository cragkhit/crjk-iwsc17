package org.jfree.data.xy;
import org.jfree.data.ComparableObjectItem;
public class XYIntervalDataItem extends ComparableObjectItem {
    public XYIntervalDataItem ( final double x, final double xLow, final double xHigh, final double y, final double yLow, final double yHigh ) {
        super ( new Double ( x ), new XYInterval ( xLow, xHigh, y, yLow, yHigh ) );
    }
    public Double getX() {
        return ( Double ) this.getComparable();
    }
    public double getYValue() {
        final XYInterval interval = ( XYInterval ) this.getObject();
        if ( interval != null ) {
            return interval.getY();
        }
        return Double.NaN;
    }
    public double getXLowValue() {
        final XYInterval interval = ( XYInterval ) this.getObject();
        if ( interval != null ) {
            return interval.getXLow();
        }
        return Double.NaN;
    }
    public double getXHighValue() {
        final XYInterval interval = ( XYInterval ) this.getObject();
        if ( interval != null ) {
            return interval.getXHigh();
        }
        return Double.NaN;
    }
    public double getYLowValue() {
        final XYInterval interval = ( XYInterval ) this.getObject();
        if ( interval != null ) {
            return interval.getYLow();
        }
        return Double.NaN;
    }
    public double getYHighValue() {
        final XYInterval interval = ( XYInterval ) this.getObject();
        if ( interval != null ) {
            return interval.getYHigh();
        }
        return Double.NaN;
    }
}
