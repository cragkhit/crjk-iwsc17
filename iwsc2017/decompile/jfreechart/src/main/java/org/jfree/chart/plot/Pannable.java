package org.jfree.chart.plot;
import java.awt.geom.Point2D;
public interface Pannable {
    PlotOrientation getOrientation();
    boolean isDomainPannable();
    boolean isRangePannable();
    void panDomainAxes ( double p0, PlotRenderingInfo p1, Point2D p2 );
    void panRangeAxes ( double p0, PlotRenderingInfo p1, Point2D p2 );
}
