package org.jfree.chart.needle;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.io.Serializable;
public class ShipNeedle extends MeterNeedle implements Cloneable, Serializable {
    private static final long serialVersionUID = 149554868169435612L;
    @Override
    protected void drawNeedle ( final Graphics2D g2, final Rectangle2D plotArea, final Point2D rotate, final double angle ) {
        final GeneralPath shape = new GeneralPath();
        shape.append ( new Arc2D.Double ( -9.0, -7.0, 10.0, 14.0, 0.0, 25.5, 0 ), true );
        shape.append ( new Arc2D.Double ( 0.0, -7.0, 10.0, 14.0, 154.5, 25.5, 0 ), true );
        shape.closePath();
        this.getTransform().setToTranslation ( plotArea.getMinX(), plotArea.getMaxY() );
        this.getTransform().scale ( plotArea.getWidth(), plotArea.getHeight() / 3.0 );
        shape.transform ( this.getTransform() );
        if ( rotate != null && angle != 0.0 ) {
            this.getTransform().setToRotation ( angle, rotate.getX(), rotate.getY() );
            shape.transform ( this.getTransform() );
        }
        this.defaultDisplay ( g2, shape );
    }
    @Override
    public boolean equals ( final Object object ) {
        return object != null && ( object == this || ( super.equals ( object ) && object instanceof ShipNeedle ) );
    }
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
