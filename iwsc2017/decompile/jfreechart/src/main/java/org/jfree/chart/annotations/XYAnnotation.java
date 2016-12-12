package org.jfree.chart.annotations;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.axis.ValueAxis;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.plot.XYPlot;
import java.awt.Graphics2D;
public interface XYAnnotation extends Annotation {
    void draw ( Graphics2D p0, XYPlot p1, Rectangle2D p2, ValueAxis p3, ValueAxis p4, int p5, PlotRenderingInfo p6 );
}
