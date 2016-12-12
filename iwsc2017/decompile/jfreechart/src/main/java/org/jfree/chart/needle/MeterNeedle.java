package org.jfree.chart.needle;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.chart.HashUtilities;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.Stroke;
import java.awt.Paint;
import java.io.Serializable;
public abstract class MeterNeedle implements Serializable {
    private static final long serialVersionUID = 5203064851510951052L;
    private transient Paint outlinePaint;
    private transient Stroke outlineStroke;
    private transient Paint fillPaint;
    private transient Paint highlightPaint;
    private int size;
    private double rotateX;
    private double rotateY;
    protected static AffineTransform transform;
    public MeterNeedle() {
        this ( null, null, null );
    }
    public MeterNeedle ( final Paint outline, final Paint fill, final Paint highlight ) {
        this.outlinePaint = Color.black;
        this.outlineStroke = new BasicStroke ( 2.0f );
        this.fillPaint = null;
        this.highlightPaint = null;
        this.size = 5;
        this.rotateX = 0.5;
        this.rotateY = 0.5;
        this.fillPaint = fill;
        this.highlightPaint = highlight;
        this.outlinePaint = outline;
    }
    public Paint getOutlinePaint() {
        return this.outlinePaint;
    }
    public void setOutlinePaint ( final Paint p ) {
        if ( p != null ) {
            this.outlinePaint = p;
        }
    }
    public Stroke getOutlineStroke() {
        return this.outlineStroke;
    }
    public void setOutlineStroke ( final Stroke s ) {
        if ( s != null ) {
            this.outlineStroke = s;
        }
    }
    public Paint getFillPaint() {
        return this.fillPaint;
    }
    public void setFillPaint ( final Paint p ) {
        if ( p != null ) {
            this.fillPaint = p;
        }
    }
    public Paint getHighlightPaint() {
        return this.highlightPaint;
    }
    public void setHighlightPaint ( final Paint p ) {
        if ( p != null ) {
            this.highlightPaint = p;
        }
    }
    public double getRotateX() {
        return this.rotateX;
    }
    public void setRotateX ( final double x ) {
        this.rotateX = x;
    }
    public void setRotateY ( final double y ) {
        this.rotateY = y;
    }
    public double getRotateY() {
        return this.rotateY;
    }
    public void draw ( final Graphics2D g2, final Rectangle2D plotArea ) {
        this.draw ( g2, plotArea, 0.0 );
    }
    public void draw ( final Graphics2D g2, final Rectangle2D plotArea, final double angle ) {
        final Point2D.Double pt = new Point2D.Double();
        pt.setLocation ( plotArea.getMinX() + this.rotateX * plotArea.getWidth(), plotArea.getMinY() + this.rotateY * plotArea.getHeight() );
        this.draw ( g2, plotArea, pt, angle );
    }
    public void draw ( final Graphics2D g2, final Rectangle2D plotArea, final Point2D rotate, final double angle ) {
        final Paint savePaint = g2.getColor();
        final Stroke saveStroke = g2.getStroke();
        this.drawNeedle ( g2, plotArea, rotate, Math.toRadians ( angle ) );
        g2.setStroke ( saveStroke );
        g2.setPaint ( savePaint );
    }
    protected abstract void drawNeedle ( final Graphics2D p0, final Rectangle2D p1, final Point2D p2, final double p3 );
    protected void defaultDisplay ( final Graphics2D g2, final Shape shape ) {
        if ( this.fillPaint != null ) {
            g2.setPaint ( this.fillPaint );
            g2.fill ( shape );
        }
        if ( this.outlinePaint != null ) {
            g2.setStroke ( this.outlineStroke );
            g2.setPaint ( this.outlinePaint );
            g2.draw ( shape );
        }
    }
    public int getSize() {
        return this.size;
    }
    public void setSize ( final int pixels ) {
        this.size = pixels;
    }
    public AffineTransform getTransform() {
        return MeterNeedle.transform;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof MeterNeedle ) ) {
            return false;
        }
        final MeterNeedle that = ( MeterNeedle ) obj;
        return PaintUtilities.equal ( this.outlinePaint, that.outlinePaint ) && ObjectUtilities.equal ( ( Object ) this.outlineStroke, ( Object ) that.outlineStroke ) && PaintUtilities.equal ( this.fillPaint, that.fillPaint ) && PaintUtilities.equal ( this.highlightPaint, that.highlightPaint ) && this.size == that.size && this.rotateX == that.rotateX && this.rotateY == that.rotateY;
    }
    @Override
    public int hashCode() {
        int result = HashUtilities.hashCode ( 193, this.fillPaint );
        result = HashUtilities.hashCode ( result, this.highlightPaint );
        result = HashUtilities.hashCode ( result, this.outlinePaint );
        result = HashUtilities.hashCode ( result, this.outlineStroke );
        result = HashUtilities.hashCode ( result, this.rotateX );
        result = HashUtilities.hashCode ( result, this.rotateY );
        result = HashUtilities.hashCode ( result, this.size );
        return result;
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeStroke ( this.outlineStroke, stream );
        SerialUtilities.writePaint ( this.outlinePaint, stream );
        SerialUtilities.writePaint ( this.fillPaint, stream );
        SerialUtilities.writePaint ( this.highlightPaint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.outlineStroke = SerialUtilities.readStroke ( stream );
        this.outlinePaint = SerialUtilities.readPaint ( stream );
        this.fillPaint = SerialUtilities.readPaint ( stream );
        this.highlightPaint = SerialUtilities.readPaint ( stream );
    }
    static {
        MeterNeedle.transform = new AffineTransform();
    }
}
