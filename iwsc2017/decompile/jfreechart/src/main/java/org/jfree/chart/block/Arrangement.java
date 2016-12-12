package org.jfree.chart.block;
import org.jfree.ui.Size2D;
import java.awt.Graphics2D;
public interface Arrangement {
    void add ( Block p0, Object p1 );
    Size2D arrange ( BlockContainer p0, Graphics2D p1, RectangleConstraint p2 );
    void clear();
}
