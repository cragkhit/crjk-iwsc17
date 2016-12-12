package org.jfree.chart.plot.dial;
public interface DialScale extends DialLayer {
    double valueToAngle ( double p0 );
    double angleToValue ( double p0 );
}
