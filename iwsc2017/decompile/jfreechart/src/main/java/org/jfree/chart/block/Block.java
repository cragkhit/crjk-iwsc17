package org.jfree.chart.block;
import java.awt.geom.Rectangle2D;
import org.jfree.ui.Size2D;
import java.awt.Graphics2D;
import org.jfree.ui.Drawable;
public interface Block extends Drawable {
    String getID();
    void setID ( String p0 );
    Size2D arrange ( Graphics2D p0 );
    Size2D arrange ( Graphics2D p0, RectangleConstraint p1 );
    Rectangle2D getBounds();
    void setBounds ( Rectangle2D p0 );
    Object draw ( Graphics2D p0, Rectangle2D p1, Object p2 );
}
