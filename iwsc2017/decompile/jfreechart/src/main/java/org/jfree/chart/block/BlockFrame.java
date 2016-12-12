package org.jfree.chart.block;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.ui.RectangleInsets;
public interface BlockFrame {
    RectangleInsets getInsets();
    void draw ( Graphics2D p0, Rectangle2D p1 );
}
