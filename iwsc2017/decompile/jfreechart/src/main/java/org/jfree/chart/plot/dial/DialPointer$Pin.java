package org.jfree.chart.plot.dial;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.chart.HashUtilities;
import org.jfree.util.PaintUtilities;
import java.awt.geom.Point2D;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.chart.util.ParamChecks;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.Paint;
public static class Pin extends DialPointer {
    static final long serialVersionUID = -8445860485367689750L;
    private transient Paint paint;
    private transient Stroke stroke;
    public Pin() {
        this ( 0 );
    }
    public Pin ( final int datasetIndex ) {
        super ( datasetIndex );
        this.paint = Color.red;
        this.stroke = new BasicStroke ( 3.0f, 1, 2 );
    }
    public Paint getPaint() {
        return this.paint;
    }
    public void setPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.paint = paint;
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
    public void draw ( final Graphics2D g2, final DialPlot plot, final Rectangle2D frame, final Rectangle2D view ) {
        g2.setPaint ( this.paint );
        g2.setStroke ( this.stroke );
        final Rectangle2D arcRect = DialPlot.rectangleByRadius ( frame, this.radius, this.radius );
        final double value = plot.getValue ( this.datasetIndex );
        final DialScale scale = plot.getScaleForDataset ( this.datasetIndex );
        final double angle = scale.valueToAngle ( value );
        final Arc2D arc = new Arc2D.Double ( arcRect, angle, 0.0, 0 );
        final Point2D pt = arc.getEndPoint();
        final Line2D line = new Line2D.Double ( frame.getCenterX(), frame.getCenterY(), pt.getX(), pt.getY() );
        g2.draw ( line );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof Pin ) ) {
            return false;
        }
        final Pin that = ( Pin ) obj;
        return PaintUtilities.equal ( this.paint, that.paint ) && this.stroke.equals ( that.stroke ) && super.equals ( obj );
    }
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = HashUtilities.hashCode ( result, this.paint );
        result = HashUtilities.hashCode ( result, this.stroke );
        return result;
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.paint, stream );
        SerialUtilities.writeStroke ( this.stroke, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.paint = SerialUtilities.readPaint ( stream );
        this.stroke = SerialUtilities.readStroke ( stream );
    }
}
