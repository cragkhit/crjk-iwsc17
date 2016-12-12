package org.jfree.chart.annotations;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.chart.HashUtilities;
import org.jfree.util.ObjectUtilities;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.text.TextUtilities;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.GeneralPath;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.CategoryAxis;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.plot.CategoryPlot;
import java.awt.Graphics2D;
import org.jfree.chart.util.ParamChecks;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class CategoryPointerAnnotation extends CategoryTextAnnotation implements Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = -4031161445009858551L;
    public static final double DEFAULT_TIP_RADIUS = 10.0;
    public static final double DEFAULT_BASE_RADIUS = 30.0;
    public static final double DEFAULT_LABEL_OFFSET = 3.0;
    public static final double DEFAULT_ARROW_LENGTH = 5.0;
    public static final double DEFAULT_ARROW_WIDTH = 3.0;
    private double angle;
    private double tipRadius;
    private double baseRadius;
    private double arrowLength;
    private double arrowWidth;
    private transient Stroke arrowStroke;
    private transient Paint arrowPaint;
    private double labelOffset;
    public CategoryPointerAnnotation ( final String label, final Comparable key, final double value, final double angle ) {
        super ( label, key, value );
        this.angle = angle;
        this.tipRadius = 10.0;
        this.baseRadius = 30.0;
        this.arrowLength = 5.0;
        this.arrowWidth = 3.0;
        this.labelOffset = 3.0;
        this.arrowStroke = new BasicStroke ( 1.0f );
        this.arrowPaint = Color.black;
    }
    public double getAngle() {
        return this.angle;
    }
    public void setAngle ( final double angle ) {
        this.angle = angle;
        this.fireAnnotationChanged();
    }
    public double getTipRadius() {
        return this.tipRadius;
    }
    public void setTipRadius ( final double radius ) {
        this.tipRadius = radius;
        this.fireAnnotationChanged();
    }
    public double getBaseRadius() {
        return this.baseRadius;
    }
    public void setBaseRadius ( final double radius ) {
        this.baseRadius = radius;
        this.fireAnnotationChanged();
    }
    public double getLabelOffset() {
        return this.labelOffset;
    }
    public void setLabelOffset ( final double offset ) {
        this.labelOffset = offset;
        this.fireAnnotationChanged();
    }
    public double getArrowLength() {
        return this.arrowLength;
    }
    public void setArrowLength ( final double length ) {
        this.arrowLength = length;
        this.fireAnnotationChanged();
    }
    public double getArrowWidth() {
        return this.arrowWidth;
    }
    public void setArrowWidth ( final double width ) {
        this.arrowWidth = width;
        this.fireAnnotationChanged();
    }
    public Stroke getArrowStroke() {
        return this.arrowStroke;
    }
    public void setArrowStroke ( final Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.arrowStroke = stroke;
        this.fireAnnotationChanged();
    }
    public Paint getArrowPaint() {
        return this.arrowPaint;
    }
    public void setArrowPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.arrowPaint = paint;
        this.fireAnnotationChanged();
    }
    @Override
    public void draw ( final Graphics2D g2, final CategoryPlot plot, final Rectangle2D dataArea, final CategoryAxis domainAxis, final ValueAxis rangeAxis ) {
        final PlotOrientation orientation = plot.getOrientation();
        final RectangleEdge domainEdge = Plot.resolveDomainAxisLocation ( plot.getDomainAxisLocation(), orientation );
        final RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation ( plot.getRangeAxisLocation(), orientation );
        final CategoryDataset dataset = plot.getDataset();
        final int catIndex = dataset.getColumnIndex ( this.getCategory() );
        final int catCount = dataset.getColumnCount();
        double j2DX = domainAxis.getCategoryMiddle ( catIndex, catCount, dataArea, domainEdge );
        double j2DY = rangeAxis.valueToJava2D ( this.getValue(), dataArea, rangeEdge );
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            final double temp = j2DX;
            j2DX = j2DY;
            j2DY = temp;
        }
        final double startX = j2DX + Math.cos ( this.angle ) * this.baseRadius;
        final double startY = j2DY + Math.sin ( this.angle ) * this.baseRadius;
        final double endX = j2DX + Math.cos ( this.angle ) * this.tipRadius;
        final double endY = j2DY + Math.sin ( this.angle ) * this.tipRadius;
        final double arrowBaseX = endX + Math.cos ( this.angle ) * this.arrowLength;
        final double arrowBaseY = endY + Math.sin ( this.angle ) * this.arrowLength;
        final double arrowLeftX = arrowBaseX + Math.cos ( this.angle + 1.5707963267948966 ) * this.arrowWidth;
        final double arrowLeftY = arrowBaseY + Math.sin ( this.angle + 1.5707963267948966 ) * this.arrowWidth;
        final double arrowRightX = arrowBaseX - Math.cos ( this.angle + 1.5707963267948966 ) * this.arrowWidth;
        final double arrowRightY = arrowBaseY - Math.sin ( this.angle + 1.5707963267948966 ) * this.arrowWidth;
        final GeneralPath arrow = new GeneralPath();
        arrow.moveTo ( ( float ) endX, ( float ) endY );
        arrow.lineTo ( ( float ) arrowLeftX, ( float ) arrowLeftY );
        arrow.lineTo ( ( float ) arrowRightX, ( float ) arrowRightY );
        arrow.closePath();
        g2.setStroke ( this.arrowStroke );
        g2.setPaint ( this.arrowPaint );
        final Line2D line = new Line2D.Double ( startX, startY, arrowBaseX, arrowBaseY );
        g2.draw ( line );
        g2.fill ( arrow );
        g2.setFont ( this.getFont() );
        g2.setPaint ( this.getPaint() );
        final double labelX = j2DX + Math.cos ( this.angle ) * ( this.baseRadius + this.labelOffset );
        final double labelY = j2DY + Math.sin ( this.angle ) * ( this.baseRadius + this.labelOffset );
        TextUtilities.drawAlignedString ( this.getText(), g2, ( float ) labelX, ( float ) labelY, this.getTextAnchor() );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof CategoryPointerAnnotation ) ) {
            return false;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        final CategoryPointerAnnotation that = ( CategoryPointerAnnotation ) obj;
        return this.angle == that.angle && this.tipRadius == that.tipRadius && this.baseRadius == that.baseRadius && this.arrowLength == that.arrowLength && this.arrowWidth == that.arrowWidth && this.arrowPaint.equals ( that.arrowPaint ) && ObjectUtilities.equal ( ( Object ) this.arrowStroke, ( Object ) that.arrowStroke ) && this.labelOffset == that.labelOffset;
    }
    @Override
    public int hashCode() {
        int result = 193;
        long temp = Double.doubleToLongBits ( this.angle );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        temp = Double.doubleToLongBits ( this.tipRadius );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        temp = Double.doubleToLongBits ( this.baseRadius );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        temp = Double.doubleToLongBits ( this.arrowLength );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        temp = Double.doubleToLongBits ( this.arrowWidth );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        result = 37 * result + HashUtilities.hashCodeForPaint ( this.arrowPaint );
        result = 37 * result + this.arrowStroke.hashCode();
        temp = Double.doubleToLongBits ( this.labelOffset );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        return result;
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.arrowPaint, stream );
        SerialUtilities.writeStroke ( this.arrowStroke, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.arrowPaint = SerialUtilities.readPaint ( stream );
        this.arrowStroke = SerialUtilities.readStroke ( stream );
    }
}
