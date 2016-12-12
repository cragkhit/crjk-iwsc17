package org.jfree.chart.plot;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Paint;
public interface DrawingSupplier {
    Paint getNextPaint();
    Paint getNextOutlinePaint();
    Paint getNextFillPaint();
    Stroke getNextStroke();
    Stroke getNextOutlineStroke();
    Shape getNextShape();
}
