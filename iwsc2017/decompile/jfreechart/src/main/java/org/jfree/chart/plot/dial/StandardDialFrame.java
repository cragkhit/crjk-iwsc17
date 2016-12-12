package org.jfree.chart.plot.dial;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.chart.HashUtilities;
import org.jfree.util.PaintUtilities;
import java.awt.geom.Area;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.util.ParamChecks;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.Paint;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class StandardDialFrame extends AbstractDialLayer implements DialFrame, Cloneable, PublicCloneable, Serializable {
    static final long serialVersionUID = 1016585407507121596L;
    private double radius;
    private transient Paint backgroundPaint;
    private transient Paint foregroundPaint;
    private transient Stroke stroke;
    public StandardDialFrame() {
        this.backgroundPaint = Color.gray;
        this.foregroundPaint = Color.black;
        this.stroke = new BasicStroke ( 2.0f );
        this.radius = 0.95;
    }
    public double getRadius() {
        return this.radius;
    }
    public void setRadius ( final double radius ) {
        if ( radius <= 0.0 ) {
            throw new IllegalArgumentException ( "The 'radius' must be positive." );
        }
        this.radius = radius;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public Paint getBackgroundPaint() {
        return this.backgroundPaint;
    }
    public void setBackgroundPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.backgroundPaint = paint;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public Paint getForegroundPaint() {
        return this.foregroundPaint;
    }
    public void setForegroundPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.foregroundPaint = paint;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public Stroke getStroke() {
        return this.stroke;
    }
    public void setStroke ( final Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.stroke = stroke;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    @Override
    public Shape getWindow ( final Rectangle2D frame ) {
        final Rectangle2D f = DialPlot.rectangleByRadius ( frame, this.radius, this.radius );
        return new Ellipse2D.Double ( f.getX(), f.getY(), f.getWidth(), f.getHeight() );
    }
    public boolean isClippedToWindow() {
        return false;
    }
    public void draw ( final Graphics2D g2, final DialPlot plot, final Rectangle2D frame, final Rectangle2D view ) {
        final Shape window = this.getWindow ( frame );
        final Rectangle2D f = DialPlot.rectangleByRadius ( frame, this.radius + 0.02, this.radius + 0.02 );
        final Ellipse2D e = new Ellipse2D.Double ( f.getX(), f.getY(), f.getWidth(), f.getHeight() );
        final Area area = new Area ( e );
        final Area area2 = new Area ( window );
        area.subtract ( area2 );
        g2.setPaint ( this.backgroundPaint );
        g2.fill ( area );
        g2.setStroke ( this.stroke );
        g2.setPaint ( this.foregroundPaint );
        g2.draw ( window );
        g2.draw ( e );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof StandardDialFrame ) ) {
            return false;
        }
        final StandardDialFrame that = ( StandardDialFrame ) obj;
        return PaintUtilities.equal ( this.backgroundPaint, that.backgroundPaint ) && PaintUtilities.equal ( this.foregroundPaint, that.foregroundPaint ) && this.radius == that.radius && this.stroke.equals ( that.stroke ) && super.equals ( obj );
    }
    @Override
    public int hashCode() {
        int result = 193;
        final long temp = Double.doubleToLongBits ( this.radius );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        result = 37 * result + HashUtilities.hashCodeForPaint ( this.backgroundPaint );
        result = 37 * result + HashUtilities.hashCodeForPaint ( this.foregroundPaint );
        result = 37 * result + this.stroke.hashCode();
        return result;
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.backgroundPaint, stream );
        SerialUtilities.writePaint ( this.foregroundPaint, stream );
        SerialUtilities.writeStroke ( this.stroke, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.backgroundPaint = SerialUtilities.readPaint ( stream );
        this.foregroundPaint = SerialUtilities.readPaint ( stream );
        this.stroke = SerialUtilities.readStroke ( stream );
    }
}
