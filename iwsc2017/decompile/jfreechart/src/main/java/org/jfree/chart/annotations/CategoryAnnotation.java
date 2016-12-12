package org.jfree.chart.annotations;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.CategoryAxis;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.plot.CategoryPlot;
import java.awt.Graphics2D;
public interface CategoryAnnotation extends Annotation {
    void draw ( Graphics2D p0, CategoryPlot p1, Rectangle2D p2, CategoryAxis p3, ValueAxis p4 );
}
