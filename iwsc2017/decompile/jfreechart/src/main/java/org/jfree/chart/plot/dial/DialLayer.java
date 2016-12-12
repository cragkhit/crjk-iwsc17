package org.jfree.chart.plot.dial;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.util.EventListener;
public interface DialLayer {
    boolean isVisible();
    void addChangeListener ( DialLayerChangeListener p0 );
    void removeChangeListener ( DialLayerChangeListener p0 );
    boolean hasListener ( EventListener p0 );
    boolean isClippedToWindow();
    void draw ( Graphics2D p0, DialPlot p1, Rectangle2D p2, Rectangle2D p3 );
}
