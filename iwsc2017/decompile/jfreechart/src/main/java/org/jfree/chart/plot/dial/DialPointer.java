package org.jfree.chart.plot.dial;
import java.awt.geom.GeneralPath;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
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
import org.jfree.chart.HashUtilities;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public abstract class DialPointer extends AbstractDialLayer implements DialLayer, Cloneable, PublicCloneable, Serializable {
    double radius;
    int datasetIndex;
    protected DialPointer() {
        this ( 0 );
    }
    protected DialPointer ( final int datasetIndex ) {
        this.radius = 0.9;
        this.datasetIndex = datasetIndex;
    }
    public int getDatasetIndex() {
        return this.datasetIndex;
    }
    public void setDatasetIndex ( final int index ) {
        this.datasetIndex = index;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public double getRadius() {
        return this.radius;
    }
    public void setRadius ( final double radius ) {
        this.radius = radius;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    @Override
    public boolean isClippedToWindow() {
        return true;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof DialPointer ) ) {
            return false;
        }
        final DialPointer that = ( DialPointer ) obj;
        return this.datasetIndex == that.datasetIndex && this.radius == that.radius && super.equals ( obj );
    }
    @Override
    public int hashCode() {
        int result = 23;
        result = HashUtilities.hashCode ( result, this.radius );
        return result;
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
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
    public static class Pointer extends DialPointer {
        static final long serialVersionUID = -4180500011963176960L;
        private double widthRadius;
        private transient Paint fillPaint;
        private transient Paint outlinePaint;
        public Pointer() {
            this ( 0 );
        }
        public Pointer ( final int datasetIndex ) {
            super ( datasetIndex );
            this.widthRadius = 0.05;
            this.fillPaint = Color.gray;
            this.outlinePaint = Color.black;
        }
        public double getWidthRadius() {
            return this.widthRadius;
        }
        public void setWidthRadius ( final double radius ) {
            this.widthRadius = radius;
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
        @Override
        public void draw ( final Graphics2D g2, final DialPlot plot, final Rectangle2D frame, final Rectangle2D view ) {
            g2.setPaint ( Color.blue );
            g2.setStroke ( new BasicStroke ( 1.0f ) );
            final Rectangle2D lengthRect = DialPlot.rectangleByRadius ( frame, this.radius, this.radius );
            final Rectangle2D widthRect = DialPlot.rectangleByRadius ( frame, this.widthRadius, this.widthRadius );
            final double value = plot.getValue ( this.datasetIndex );
            final DialScale scale = plot.getScaleForDataset ( this.datasetIndex );
            final double angle = scale.valueToAngle ( value );
            final Arc2D arc1 = new Arc2D.Double ( lengthRect, angle, 0.0, 0 );
            final Point2D pt1 = arc1.getEndPoint();
            final Arc2D arc2 = new Arc2D.Double ( widthRect, angle - 90.0, 180.0, 0 );
            final Point2D pt2 = arc2.getStartPoint();
            final Point2D pt3 = arc2.getEndPoint();
            final Arc2D arc3 = new Arc2D.Double ( widthRect, angle - 180.0, 0.0, 0 );
            final Point2D pt4 = arc3.getStartPoint();
            final GeneralPath gp = new GeneralPath();
            gp.moveTo ( ( float ) pt1.getX(), ( float ) pt1.getY() );
            gp.lineTo ( ( float ) pt2.getX(), ( float ) pt2.getY() );
            gp.lineTo ( ( float ) pt4.getX(), ( float ) pt4.getY() );
            gp.lineTo ( ( float ) pt3.getX(), ( float ) pt3.getY() );
            gp.closePath();
            g2.setPaint ( this.fillPaint );
            g2.fill ( gp );
            g2.setPaint ( this.outlinePaint );
            final Line2D line = new Line2D.Double ( frame.getCenterX(), frame.getCenterY(), pt1.getX(), pt1.getY() );
            g2.draw ( line );
            line.setLine ( pt2, pt3 );
            g2.draw ( line );
            line.setLine ( pt3, pt1 );
            g2.draw ( line );
            line.setLine ( pt2, pt1 );
            g2.draw ( line );
            line.setLine ( pt2, pt4 );
            g2.draw ( line );
            line.setLine ( pt3, pt4 );
            g2.draw ( line );
        }
        @Override
        public boolean equals ( final Object obj ) {
            if ( obj == this ) {
                return true;
            }
            if ( ! ( obj instanceof Pointer ) ) {
                return false;
            }
            final Pointer that = ( Pointer ) obj;
            return this.widthRadius == that.widthRadius && PaintUtilities.equal ( this.fillPaint, that.fillPaint ) && PaintUtilities.equal ( this.outlinePaint, that.outlinePaint ) && super.equals ( obj );
        }
        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = HashUtilities.hashCode ( result, this.widthRadius );
            result = HashUtilities.hashCode ( result, this.fillPaint );
            result = HashUtilities.hashCode ( result, this.outlinePaint );
            return result;
        }
        private void writeObject ( final ObjectOutputStream stream ) throws IOException {
            stream.defaultWriteObject();
            SerialUtilities.writePaint ( this.fillPaint, stream );
            SerialUtilities.writePaint ( this.outlinePaint, stream );
        }
        private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
            stream.defaultReadObject();
            this.fillPaint = SerialUtilities.readPaint ( stream );
            this.outlinePaint = SerialUtilities.readPaint ( stream );
        }
    }
}
