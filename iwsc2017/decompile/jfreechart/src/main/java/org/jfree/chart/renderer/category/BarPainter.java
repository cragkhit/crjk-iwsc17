package org.jfree.chart.renderer.category;
import org.jfree.ui.RectangleEdge;
import java.awt.geom.RectangularShape;
import java.awt.Graphics2D;
public interface BarPainter {
    void paintBar ( Graphics2D p0, BarRenderer p1, int p2, int p3, RectangularShape p4, RectangleEdge p5 );
    void paintBarShadow ( Graphics2D p0, BarRenderer p1, int p2, int p3, RectangularShape p4, RectangleEdge p5, boolean p6 );
}
