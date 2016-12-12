package org.jfree.chart.needle;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.io.Serializable;
public class PointerNeedle extends MeterNeedle implements Cloneable, Serializable {
    private static final long serialVersionUID = -4744677345334729606L;
    @Override
    protected void drawNeedle ( final Graphics2D g2, final Rectangle2D plotArea, final Point2D rotate, final double angle ) {
        final GeneralPath shape1 = new GeneralPath();
        final GeneralPath shape2 = new GeneralPath();
        final float minX = ( float ) plotArea.getMinX();
        final float minY = ( float ) plotArea.getMinY();
        final float maxX = ( float ) plotArea.getMaxX();
        final float maxY = ( float ) plotArea.getMaxY();
        final float midX = ( float ) ( minX + plotArea.getWidth() / 2.0 );
        final float midY = ( float ) ( minY + plotArea.getHeight() / 2.0 );
        shape1.moveTo ( minX, midY );
        shape1.lineTo ( midX, minY );
        shape1.lineTo ( maxX, midY );
        shape1.closePath();
        shape2.moveTo ( minX, midY );
        shape2.lineTo ( midX, maxY );
        shape2.lineTo ( maxX, midY );
        shape2.closePath();
        if ( rotate != null && angle != 0.0 ) {
            this.getTransform().setToRotation ( angle, rotate.getX(), rotate.getY() );
            shape1.transform ( this.getTransform() );
            shape2.transform ( this.getTransform() );
        }
        if ( this.getFillPaint() != null ) {
            g2.setPaint ( this.getFillPaint() );
            g2.fill ( shape1 );
        }
        if ( this.getHighlightPaint() != null ) {
            g2.setPaint ( this.getHighlightPaint() );
            g2.fill ( shape2 );
        }
        if ( this.getOutlinePaint() != null ) {
            g2.setStroke ( this.getOutlineStroke() );
            g2.setPaint ( this.getOutlinePaint() );
            g2.draw ( shape1 );
            g2.draw ( shape2 );
        }
    }
    @Override
    public boolean equals ( final Object obj ) {
        return obj == this || ( obj instanceof PointerNeedle && super.equals ( obj ) );
    }
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
