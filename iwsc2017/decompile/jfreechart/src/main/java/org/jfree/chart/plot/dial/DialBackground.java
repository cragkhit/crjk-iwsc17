package org.jfree.chart.plot.dial;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.chart.HashUtilities;
import org.jfree.util.PaintUtilities;
import java.awt.Shape;
import java.awt.GradientPaint;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.ui.StandardGradientPaintTransformer;
import org.jfree.chart.util.ParamChecks;
import java.awt.Color;
import org.jfree.ui.GradientPaintTransformer;
import java.awt.Paint;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class DialBackground extends AbstractDialLayer implements DialLayer, Cloneable, PublicCloneable, Serializable {
    static final long serialVersionUID = -9019069533317612375L;
    private transient Paint paint;
    private GradientPaintTransformer gradientPaintTransformer;
    public DialBackground() {
        this ( Color.white );
    }
    public DialBackground ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.paint = paint;
        this.gradientPaintTransformer = ( GradientPaintTransformer ) new StandardGradientPaintTransformer();
    }
    public Paint getPaint() {
        return this.paint;
    }
    public void setPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.paint = paint;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public GradientPaintTransformer getGradientPaintTransformer() {
        return this.gradientPaintTransformer;
    }
    public void setGradientPaintTransformer ( final GradientPaintTransformer t ) {
        ParamChecks.nullNotPermitted ( t, "t" );
        this.gradientPaintTransformer = t;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    @Override
    public boolean isClippedToWindow() {
        return true;
    }
    @Override
    public void draw ( final Graphics2D g2, final DialPlot plot, final Rectangle2D frame, final Rectangle2D view ) {
        Paint p = this.paint;
        if ( p instanceof GradientPaint ) {
            p = this.gradientPaintTransformer.transform ( ( GradientPaint ) p, ( Shape ) view );
        }
        g2.setPaint ( p );
        g2.fill ( view );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof DialBackground ) ) {
            return false;
        }
        final DialBackground that = ( DialBackground ) obj;
        return PaintUtilities.equal ( this.paint, that.paint ) && this.gradientPaintTransformer.equals ( that.gradientPaintTransformer ) && super.equals ( obj );
    }
    @Override
    public int hashCode() {
        int result = 193;
        result = 37 * result + HashUtilities.hashCodeForPaint ( this.paint );
        result = 37 * result + this.gradientPaintTransformer.hashCode();
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
