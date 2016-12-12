package org.jfree.data.xy;
import java.io.Serializable;
public class XYInterval implements Serializable {
    private double xLow;
    private double xHigh;
    private double y;
    private double yLow;
    private double yHigh;
    public XYInterval ( final double xLow, final double xHigh, final double y, final double yLow, final double yHigh ) {
        this.xLow = xLow;
        this.xHigh = xHigh;
        this.y = y;
        this.yLow = yLow;
        this.yHigh = yHigh;
    }
    public double getXLow() {
        return this.xLow;
    }
    public double getXHigh() {
        return this.xHigh;
    }
    public double getY() {
        return this.y;
    }
    public double getYLow() {
        return this.yLow;
    }
    public double getYHigh() {
        return this.yHigh;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof XYInterval ) ) {
            return false;
        }
        final XYInterval that = ( XYInterval ) obj;
        return this.xLow == that.xLow && this.xHigh == that.xHigh && this.y == that.y && this.yLow == that.yLow && this.yHigh == that.yHigh;
    }
}
