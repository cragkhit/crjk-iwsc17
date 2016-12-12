package org.jfree.chart.plot.dial;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.chart.HashUtilities;
import org.jfree.util.PaintUtilities;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.chart.util.ParamChecks;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.Paint;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class DialCap extends AbstractDialLayer implements DialLayer, Cloneable, PublicCloneable, Serializable {
    static final long serialVersionUID = -2929484264982524463L;
    private double radius;
    private transient Paint fillPaint;
    private transient Paint outlinePaint;
    private transient Stroke outlineStroke;
    public DialCap() {
        this.radius = 0.05;
        this.fillPaint = Color.white;
        this.outlinePaint = Color.black;
        this.outlineStroke = new BasicStroke ( 2.0f );
    }
    public double getRadius() {
        return this.radius;
    }
    public void setRadius ( final double radius ) {
        if ( radius <= 0.0 ) {
            throw new IllegalArgumentException ( "Requires radius > 0.0." );
        }
        this.radius = radius;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public Paint getFillPaint() {
        return this.fillPaint;
    }
    public void setFillPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.fillPaint = paint;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public Paint getOutlinePaint() {
        return this.outlinePaint;
    }
    public void setOutlinePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.outlinePaint = paint;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public Stroke getOutlineStroke() {
        return this.outlineStroke;
    }
    public void setOutlineStroke ( final Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.outlineStroke = stroke;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    @Override
    public boolean isClippedToWindow() {
        return true;
    }
    @Override
    public void draw ( final Graphics2D g2, final DialPlot plot, final Rectangle2D frame, final Rectangle2D view ) {
        g2.setPaint ( this.fillPaint );
        final Rectangle2D f = DialPlot.rectangleByRadius ( frame, this.radius, this.radius );
        final Ellipse2D e = new Ellipse2D.Double ( f.getX(), f.getY(), f.getWidth(), f.getHeight() );
        g2.fill ( e );
        g2.setPaint ( this.outlinePaint );
        g2.setStroke ( this.outlineStroke );
        g2.draw ( e );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof DialCap ) ) {
            return false;
        }
        final DialCap that = ( DialCap ) obj;
        return this.radius == that.radius && PaintUtilities.equal ( this.fillPaint, that.fillPaint ) && PaintUtilities.equal ( this.outlinePaint, that.outlinePaint ) && this.outlineStroke.equals ( that.outlineStroke ) && super.equals ( obj );
    }
    @Override
    public int hashCode() {
        int result = 193;
        result = 37 * result + HashUtilities.hashCodeForPaint ( this.fillPaint );
        result = 37 * result + HashUtilities.hashCodeForPaint ( this.outlinePaint );
        result = 37 * result + this.outlineStroke.hashCode();
        return result;
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.fillPaint, stream );
        SerialUtilities.writePaint ( this.outlinePaint, stream );
        SerialUtilities.writeStroke ( this.outlineStroke, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.fillPaint = SerialUtilities.readPaint ( stream );
        this.outlinePaint = SerialUtilities.readPaint ( stream );
        this.outlineStroke = SerialUtilities.readStroke ( stream );
    }
}
