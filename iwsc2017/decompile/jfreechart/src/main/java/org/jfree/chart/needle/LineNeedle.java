package org.jfree.chart.needle;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.io.Serializable;
public class LineNeedle extends MeterNeedle implements Cloneable, Serializable {
    private static final long serialVersionUID = 6215321387896748945L;
    @Override
    protected void drawNeedle ( final Graphics2D g2, final Rectangle2D plotArea, final Point2D rotate, final double angle ) {
        final Line2D shape = new Line2D.Double();
        final double x = plotArea.getMinX() + plotArea.getWidth() / 2.0;
        shape.setLine ( x, plotArea.getMinY(), x, plotArea.getMaxY() );
        Shape s = shape;
        if ( rotate != null && angle != 0.0 ) {
            this.getTransform().setToRotation ( angle, rotate.getX(), rotate.getY() );
            s = this.getTransform().createTransformedShape ( s );
        }
        this.defaultDisplay ( g2, s );
    }
    @Override
    public boolean equals ( final Object obj ) {
        return obj == this || ( obj instanceof LineNeedle && super.equals ( obj ) );
    }
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
