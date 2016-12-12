package org.jfree.chart.plot.dial;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.PaintUtilities;
import java.awt.geom.Point2D;
import org.jfree.text.TextUtilities;
import org.jfree.ui.TextAnchor;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.chart.util.ParamChecks;
import java.awt.BasicStroke;
import java.awt.Color;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.Paint;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class StandardDialScale extends AbstractDialLayer implements DialScale, Cloneable, PublicCloneable, Serializable {
    static final long serialVersionUID = 3715644629665918516L;
    private double lowerBound;
    private double upperBound;
    private double startAngle;
    private double extent;
    private double tickRadius;
    private double majorTickIncrement;
    private double majorTickLength;
    private transient Paint majorTickPaint;
    private transient Stroke majorTickStroke;
    private int minorTickCount;
    private double minorTickLength;
    private transient Paint minorTickPaint;
    private transient Stroke minorTickStroke;
    private double tickLabelOffset;
    private Font tickLabelFont;
    private boolean tickLabelsVisible;
    private NumberFormat tickLabelFormatter;
    private boolean firstTickLabelVisible;
    private transient Paint tickLabelPaint;
    public StandardDialScale() {
        this ( 0.0, 100.0, 175.0, -170.0, 10.0, 4 );
    }
    public StandardDialScale ( final double lowerBound, final double upperBound, final double startAngle, final double extent, final double majorTickIncrement, final int minorTickCount ) {
        if ( majorTickIncrement <= 0.0 ) {
            throw new IllegalArgumentException ( "Requires 'majorTickIncrement' > 0." );
        }
        this.startAngle = startAngle;
        this.extent = extent;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.tickRadius = 0.7;
        this.tickLabelsVisible = true;
        this.tickLabelFormatter = new DecimalFormat ( "0.0" );
        this.firstTickLabelVisible = true;
        this.tickLabelFont = new Font ( "Dialog", 1, 16 );
        this.tickLabelPaint = Color.blue;
        this.tickLabelOffset = 0.1;
        this.majorTickIncrement = majorTickIncrement;
        this.majorTickLength = 0.04;
        this.majorTickPaint = Color.black;
        this.majorTickStroke = new BasicStroke ( 3.0f );
        this.minorTickCount = minorTickCount;
        this.minorTickLength = 0.02;
        this.minorTickPaint = Color.black;
        this.minorTickStroke = new BasicStroke ( 1.0f );
    }
    public double getLowerBound() {
        return this.lowerBound;
    }
    public void setLowerBound ( final double lower ) {
        this.lowerBound = lower;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public double getUpperBound() {
        return this.upperBound;
    }
    public void setUpperBound ( final double upper ) {
        this.upperBound = upper;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public double getStartAngle() {
        return this.startAngle;
    }
    public void setStartAngle ( final double angle ) {
        this.startAngle = angle;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public double getExtent() {
        return this.extent;
    }
    public void setExtent ( final double extent ) {
        this.extent = extent;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public double getTickRadius() {
        return this.tickRadius;
    }
    public void setTickRadius ( final double radius ) {
        if ( radius <= 0.0 ) {
            throw new IllegalArgumentException ( "The 'radius' must be positive." );
        }
        this.tickRadius = radius;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public double getMajorTickIncrement() {
        return this.majorTickIncrement;
    }
    public void setMajorTickIncrement ( final double increment ) {
        if ( increment <= 0.0 ) {
            throw new IllegalArgumentException ( "The 'increment' must be positive." );
        }
        this.majorTickIncrement = increment;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public double getMajorTickLength() {
        return this.majorTickLength;
    }
    public void setMajorTickLength ( final double length ) {
        if ( length < 0.0 ) {
            throw new IllegalArgumentException ( "Negative 'length' argument." );
        }
        this.majorTickLength = length;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public Paint getMajorTickPaint() {
        return this.majorTickPaint;
    }
    public void setMajorTickPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.majorTickPaint = paint;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public Stroke getMajorTickStroke() {
        return this.majorTickStroke;
    }
    public void setMajorTickStroke ( final Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.majorTickStroke = stroke;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public int getMinorTickCount() {
        return this.minorTickCount;
    }
    public void setMinorTickCount ( final int count ) {
        if ( count < 0 ) {
            throw new IllegalArgumentException ( "The 'count' cannot be negative." );
        }
        this.minorTickCount = count;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public double getMinorTickLength() {
        return this.minorTickLength;
    }
    public void setMinorTickLength ( final double length ) {
        if ( length < 0.0 ) {
            throw new IllegalArgumentException ( "Negative 'length' argument." );
        }
        this.minorTickLength = length;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public Paint getMinorTickPaint() {
        return this.minorTickPaint;
    }
    public void setMinorTickPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.minorTickPaint = paint;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public Stroke getMinorTickStroke() {
        return this.minorTickStroke;
    }
    public void setMinorTickStroke ( final Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.minorTickStroke = stroke;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public double getTickLabelOffset() {
        return this.tickLabelOffset;
    }
    public void setTickLabelOffset ( final double offset ) {
        this.tickLabelOffset = offset;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public Font getTickLabelFont() {
        return this.tickLabelFont;
    }
    public void setTickLabelFont ( final Font font ) {
        ParamChecks.nullNotPermitted ( font, "font" );
        this.tickLabelFont = font;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public Paint getTickLabelPaint() {
        return this.tickLabelPaint;
    }
    public void setTickLabelPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.tickLabelPaint = paint;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public boolean getTickLabelsVisible() {
        return this.tickLabelsVisible;
    }
    public void setTickLabelsVisible ( final boolean visible ) {
        this.tickLabelsVisible = visible;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public NumberFormat getTickLabelFormatter() {
        return this.tickLabelFormatter;
    }
    public void setTickLabelFormatter ( final NumberFormat formatter ) {
        ParamChecks.nullNotPermitted ( formatter, "formatter" );
        this.tickLabelFormatter = formatter;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public boolean getFirstTickLabelVisible() {
        return this.firstTickLabelVisible;
    }
    public void setFirstTickLabelVisible ( final boolean visible ) {
        this.firstTickLabelVisible = visible;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public boolean isClippedToWindow() {
        return true;
    }
    public void draw ( final Graphics2D g2, final DialPlot plot, final Rectangle2D frame, final Rectangle2D view ) {
        final Rectangle2D arcRect = DialPlot.rectangleByRadius ( frame, this.tickRadius, this.tickRadius );
        final Rectangle2D arcRectMajor = DialPlot.rectangleByRadius ( frame, this.tickRadius - this.majorTickLength, this.tickRadius - this.majorTickLength );
        Rectangle2D arcRectMinor = arcRect;
        if ( this.minorTickCount > 0 && this.minorTickLength > 0.0 ) {
            arcRectMinor = DialPlot.rectangleByRadius ( frame, this.tickRadius - this.minorTickLength, this.tickRadius - this.minorTickLength );
        }
        final Rectangle2D arcRectForLabels = DialPlot.rectangleByRadius ( frame, this.tickRadius - this.tickLabelOffset, this.tickRadius - this.tickLabelOffset );
        boolean firstLabel = true;
        final Arc2D arc = new Arc2D.Double();
        final Line2D workingLine = new Line2D.Double();
        for ( double v = this.lowerBound; v <= this.upperBound; v += this.majorTickIncrement ) {
            arc.setArc ( arcRect, this.startAngle, this.valueToAngle ( v ) - this.startAngle, 0 );
            Point2D pt0 = arc.getEndPoint();
            arc.setArc ( arcRectMajor, this.startAngle, this.valueToAngle ( v ) - this.startAngle, 0 );
            final Point2D pt = arc.getEndPoint();
            g2.setPaint ( this.majorTickPaint );
            g2.setStroke ( this.majorTickStroke );
            workingLine.setLine ( pt0, pt );
            g2.draw ( workingLine );
            arc.setArc ( arcRectForLabels, this.startAngle, this.valueToAngle ( v ) - this.startAngle, 0 );
            final Point2D pt2 = arc.getEndPoint();
            if ( this.tickLabelsVisible && ( !firstLabel || this.firstTickLabelVisible ) ) {
                g2.setFont ( this.tickLabelFont );
                g2.setPaint ( this.tickLabelPaint );
                TextUtilities.drawAlignedString ( this.tickLabelFormatter.format ( v ), g2, ( float ) pt2.getX(), ( float ) pt2.getY(), TextAnchor.CENTER );
            }
            firstLabel = false;
            if ( this.minorTickCount > 0 && this.minorTickLength > 0.0 ) {
                final double minorTickIncrement = this.majorTickIncrement / ( this.minorTickCount + 1 );
                for ( int i = 0; i < this.minorTickCount; ++i ) {
                    final double vv = v + ( i + 1 ) * minorTickIncrement;
                    if ( vv >= this.upperBound ) {
                        break;
                    }
                    final double angle = this.valueToAngle ( vv );
                    arc.setArc ( arcRect, this.startAngle, angle - this.startAngle, 0 );
                    pt0 = arc.getEndPoint();
                    arc.setArc ( arcRectMinor, this.startAngle, angle - this.startAngle, 0 );
                    final Point2D pt3 = arc.getEndPoint();
                    g2.setStroke ( this.minorTickStroke );
                    g2.setPaint ( this.minorTickPaint );
                    workingLine.setLine ( pt0, pt3 );
                    g2.draw ( workingLine );
                }
            }
        }
    }
    @Override
    public double valueToAngle ( final double value ) {
        final double range = this.upperBound - this.lowerBound;
        final double unit = this.extent / range;
        return this.startAngle + unit * ( value - this.lowerBound );
    }
    @Override
    public double angleToValue ( final double angle ) {
        final double range = this.upperBound - this.lowerBound;
        final double unit = range / this.extent;
        return ( angle - this.startAngle ) * unit;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof StandardDialScale ) ) {
            return false;
        }
        final StandardDialScale that = ( StandardDialScale ) obj;
        return this.lowerBound == that.lowerBound && this.upperBound == that.upperBound && this.startAngle == that.startAngle && this.extent == that.extent && this.tickRadius == that.tickRadius && this.majorTickIncrement == that.majorTickIncrement && this.majorTickLength == that.majorTickLength && PaintUtilities.equal ( this.majorTickPaint, that.majorTickPaint ) && this.majorTickStroke.equals ( that.majorTickStroke ) && this.minorTickCount == that.minorTickCount && this.minorTickLength == that.minorTickLength && PaintUtilities.equal ( this.minorTickPaint, that.minorTickPaint ) && this.minorTickStroke.equals ( that.minorTickStroke ) && this.tickLabelsVisible == that.tickLabelsVisible && this.tickLabelOffset == that.tickLabelOffset && this.tickLabelFont.equals ( that.tickLabelFont ) && PaintUtilities.equal ( this.tickLabelPaint, that.tickLabelPaint ) && super.equals ( obj );
    }
    @Override
    public int hashCode() {
        int result = 193;
        long temp = Double.doubleToLongBits ( this.lowerBound );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        temp = Double.doubleToLongBits ( this.upperBound );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        temp = Double.doubleToLongBits ( this.startAngle );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        temp = Double.doubleToLongBits ( this.extent );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        temp = Double.doubleToLongBits ( this.tickRadius );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        return result;
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.majorTickPaint, stream );
        SerialUtilities.writeStroke ( this.majorTickStroke, stream );
        SerialUtilities.writePaint ( this.minorTickPaint, stream );
        SerialUtilities.writeStroke ( this.minorTickStroke, stream );
        SerialUtilities.writePaint ( this.tickLabelPaint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.majorTickPaint = SerialUtilities.readPaint ( stream );
        this.majorTickStroke = SerialUtilities.readStroke ( stream );
        this.minorTickPaint = SerialUtilities.readPaint ( stream );
        this.minorTickStroke = SerialUtilities.readStroke ( stream );
        this.tickLabelPaint = SerialUtilities.readPaint ( stream );
    }
}
