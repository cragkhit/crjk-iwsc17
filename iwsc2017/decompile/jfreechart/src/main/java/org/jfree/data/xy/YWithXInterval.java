package org.jfree.data.xy;
import java.io.Serializable;
public class YWithXInterval implements Serializable {
    private double y;
    private double xLow;
    private double xHigh;
    public YWithXInterval ( final double y, final double xLow, final double xHigh ) {
        this.y = y;
        this.xLow = xLow;
        this.xHigh = xHigh;
    }
    public double getY() {
        return this.y;
    }
    public double getXLow() {
        return this.xLow;
    }
    public double getXHigh() {
        return this.xHigh;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof YWithXInterval ) ) {
            return false;
        }
        final YWithXInterval that = ( YWithXInterval ) obj;
        return this.y == that.y && this.xLow == that.xLow && this.xHigh == that.xHigh;
    }
}
