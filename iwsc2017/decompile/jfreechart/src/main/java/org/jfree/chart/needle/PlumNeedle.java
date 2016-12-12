package org.jfree.chart.needle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.io.Serializable;
public class PlumNeedle extends MeterNeedle implements Cloneable, Serializable {
    private static final long serialVersionUID = -3082660488660600718L;
    @Override
    protected void drawNeedle ( final Graphics2D g2, final Rectangle2D plotArea, final Point2D rotate, final double angle ) {
        final Arc2D shape = new Arc2D.Double ( 2 );
        double radius = plotArea.getHeight();
        final double halfX = plotArea.getWidth() / 2.0;
        final double diameter = 2.0 * radius;
        shape.setFrame ( plotArea.getMinX() + halfX - radius, plotArea.getMinY() - radius, diameter, diameter );
        radius = Math.toDegrees ( Math.asin ( halfX / radius ) );
        shape.setAngleStart ( 270.0 - radius );
        shape.setAngleExtent ( 2.0 * radius );
        final Area s = new Area ( shape );
        if ( rotate != null && angle != 0.0 ) {
            this.getTransform().setToRotation ( angle, rotate.getX(), rotate.getY() );
            s.transform ( this.getTransform() );
        }
        this.defaultDisplay ( g2, s );
    }
    @Override
    public boolean equals ( final Object obj ) {
        return obj == this || ( obj instanceof PlumNeedle && super.equals ( obj ) );
    }
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
