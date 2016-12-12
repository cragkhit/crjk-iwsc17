package org.jfree.chart.plot.dial;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.chart.HashUtilities;
import org.jfree.util.PaintUtilities;
import java.awt.geom.Point2D;
import org.jfree.text.TextUtilities;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.awt.Color;
import org.jfree.chart.util.ParamChecks;
import org.jfree.ui.TextAnchor;
import java.awt.Paint;
import java.awt.Font;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class DialTextAnnotation extends AbstractDialLayer implements DialLayer, Cloneable, PublicCloneable, Serializable {
    static final long serialVersionUID = 3065267524054428071L;
    private String label;
    private Font font;
    private transient Paint paint;
    private double angle;
    private double radius;
    private TextAnchor anchor;
    public DialTextAnnotation ( final String label ) {
        ParamChecks.nullNotPermitted ( label, "label" );
        this.angle = -90.0;
        this.radius = 0.3;
        this.font = new Font ( "Dialog", 1, 14 );
        this.paint = Color.black;
        this.label = label;
        this.anchor = TextAnchor.TOP_CENTER;
    }
    public String getLabel() {
        return this.label;
    }
    public void setLabel ( final String label ) {
        ParamChecks.nullNotPermitted ( label, "label" );
        this.label = label;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public Font getFont() {
        return this.font;
    }
    public void setFont ( final Font font ) {
        ParamChecks.nullNotPermitted ( font, "font" );
        this.font = font;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public Paint getPaint() {
        return this.paint;
    }
    public void setPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.paint = paint;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public double getAngle() {
        return this.angle;
    }
    public void setAngle ( final double angle ) {
        this.angle = angle;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public double getRadius() {
        return this.radius;
    }
    public void setRadius ( final double radius ) {
        if ( radius < 0.0 ) {
            throw new IllegalArgumentException ( "The 'radius' cannot be negative." );
        }
        this.radius = radius;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public TextAnchor getAnchor() {
        return this.anchor;
    }
    public void setAnchor ( final TextAnchor anchor ) {
        ParamChecks.nullNotPermitted ( anchor, "anchor" );
        this.anchor = anchor;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    @Override
    public boolean isClippedToWindow() {
        return true;
    }
    @Override
    public void draw ( final Graphics2D g2, final DialPlot plot, final Rectangle2D frame, final Rectangle2D view ) {
        final Rectangle2D f = DialPlot.rectangleByRadius ( frame, this.radius, this.radius );
        final Arc2D arc = new Arc2D.Double ( f, this.angle, 0.0, 0 );
        final Point2D pt = arc.getStartPoint();
        g2.setPaint ( this.paint );
        g2.setFont ( this.font );
        TextUtilities.drawAlignedString ( this.label, g2, ( float ) pt.getX(), ( float ) pt.getY(), this.anchor );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof DialTextAnnotation ) ) {
            return false;
        }
        final DialTextAnnotation that = ( DialTextAnnotation ) obj;
        return this.label.equals ( that.label ) && this.font.equals ( that.font ) && PaintUtilities.equal ( this.paint, that.paint ) && this.radius == that.radius && this.angle == that.angle && this.anchor.equals ( ( Object ) that.anchor ) && super.equals ( obj );
    }
    @Override
    public int hashCode() {
        int result = 193;
        result = 37 * result + HashUtilities.hashCodeForPaint ( this.paint );
        result = 37 * result + this.font.hashCode();
        result = 37 * result + this.label.hashCode();
        result = 37 * result + this.anchor.hashCode();
        long temp = Double.doubleToLongBits ( this.angle );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        temp = Double.doubleToLongBits ( this.radius );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        return result;
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.paint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.paint = SerialUtilities.readPaint ( stream );
    }
}
