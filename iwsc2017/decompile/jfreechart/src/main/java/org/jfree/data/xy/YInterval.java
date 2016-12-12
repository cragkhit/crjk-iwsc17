package org.jfree.data.xy;
import java.io.Serializable;
public class YInterval implements Serializable {
    private double y;
    private double yLow;
    private double yHigh;
    public YInterval ( final double y, final double yLow, final double yHigh ) {
        this.y = y;
        this.yLow = yLow;
        this.yHigh = yHigh;
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
        if ( ! ( obj instanceof YInterval ) ) {
            return false;
        }
        final YInterval that = ( YInterval ) obj;
        return this.y == that.y && this.yLow == that.yLow && this.yHigh == that.yHigh;
    }
}
