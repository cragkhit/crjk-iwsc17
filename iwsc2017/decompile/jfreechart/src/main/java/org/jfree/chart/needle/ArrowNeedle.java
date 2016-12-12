package org.jfree.chart.needle;
import org.jfree.chart.HashUtilities;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.io.Serializable;
public class ArrowNeedle extends MeterNeedle implements Cloneable, Serializable {
    private static final long serialVersionUID = -5334056511213782357L;
    private boolean isArrowAtTop;
    public ArrowNeedle ( final boolean isArrowAtTop ) {
        this.isArrowAtTop = true;
        this.isArrowAtTop = isArrowAtTop;
    }
    @Override
    protected void drawNeedle ( final Graphics2D g2, final Rectangle2D plotArea, final Point2D rotate, final double angle ) {
        final Line2D shape = new Line2D.Float();
        final float x = ( float ) ( plotArea.getMinX() + plotArea.getWidth() / 2.0 );
        float minY = ( float ) plotArea.getMinY();
        final float maxY = ( float ) plotArea.getMaxY();
        shape.setLine ( x, minY, x, maxY );
        final GeneralPath shape2 = new GeneralPath();
        if ( this.isArrowAtTop ) {
            shape2.moveTo ( x, minY );
            minY += 4 * this.getSize();
        } else {
            shape2.moveTo ( x, maxY );
            minY = maxY - 4 * this.getSize();
        }
        shape2.lineTo ( x + this.getSize(), minY );
        shape2.lineTo ( x - this.getSize(), minY );
        shape2.closePath();
        Shape d;
        if ( rotate != null && angle != 0.0 ) {
            this.getTransform().setToRotation ( angle, rotate.getX(), rotate.getY() );
            d = this.getTransform().createTransformedShape ( shape );
        } else {
            d = shape;
        }
        this.defaultDisplay ( g2, d );
        if ( rotate != null && angle != 0.0 ) {
            d = this.getTransform().createTransformedShape ( shape2 );
        } else {
            d = shape2;
        }
        this.defaultDisplay ( g2, d );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof ArrowNeedle ) ) {
            return false;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        final ArrowNeedle that = ( ArrowNeedle ) obj;
        return this.isArrowAtTop == that.isArrowAtTop;
    }
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = HashUtilities.hashCode ( result, this.isArrowAtTop );
        return result;
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
