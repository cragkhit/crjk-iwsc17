package org.jfree.chart.plot.dial;
public interface DialScale extends DialLayer {
    public double valueToAngle ( double value );
    public double angleToValue ( double angle );
}
