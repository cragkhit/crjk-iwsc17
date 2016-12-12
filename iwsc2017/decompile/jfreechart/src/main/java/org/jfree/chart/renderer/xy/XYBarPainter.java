package org.jfree.chart.renderer.xy;
import org.jfree.ui.RectangleEdge;
import java.awt.geom.RectangularShape;
import java.awt.Graphics2D;
public interface XYBarPainter {
    void paintBar ( Graphics2D p0, XYBarRenderer p1, int p2, int p3, RectangularShape p4, RectangleEdge p5 );
    void paintBarShadow ( Graphics2D p0, XYBarRenderer p1, int p2, int p3, RectangularShape p4, RectangleEdge p5, boolean p6 );
}
