package org.jfree.chart.plot;
import java.awt.geom.Point2D;
public interface Zoomable {
    boolean isDomainZoomable();
    boolean isRangeZoomable();
    PlotOrientation getOrientation();
    void zoomDomainAxes ( double p0, PlotRenderingInfo p1, Point2D p2 );
    void zoomDomainAxes ( double p0, PlotRenderingInfo p1, Point2D p2, boolean p3 );
    void zoomDomainAxes ( double p0, double p1, PlotRenderingInfo p2, Point2D p3 );
    void zoomRangeAxes ( double p0, PlotRenderingInfo p1, Point2D p2 );
    void zoomRangeAxes ( double p0, PlotRenderingInfo p1, Point2D p2, boolean p3 );
    void zoomRangeAxes ( double p0, double p1, PlotRenderingInfo p2, Point2D p3 );
}
