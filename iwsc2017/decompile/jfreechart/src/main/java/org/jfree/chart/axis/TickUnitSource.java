package org.jfree.chart.axis;
public interface TickUnitSource {
    TickUnit getLargerTickUnit ( TickUnit p0 );
    TickUnit getCeilingTickUnit ( TickUnit p0 );
    TickUnit getCeilingTickUnit ( double p0 );
}
