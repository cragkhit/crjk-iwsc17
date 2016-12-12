package org.jfree.chart.annotations;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.chart.HashUtilities;
import org.jfree.util.PaintUtilities;
import java.awt.Shape;
import org.jfree.ui.RectangleEdge;
import org.jfree.text.TextUtilities;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.axis.ValueAxis;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.plot.XYPlot;
import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Color;
import org.jfree.chart.util.ParamChecks;
import java.awt.Stroke;
import org.jfree.ui.TextAnchor;
import java.awt.Paint;
import java.awt.Font;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class XYTextAnnotation extends AbstractXYAnnotation implements Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = -2946063342782506328L;
    public static final Font DEFAULT_FONT;
    public static final Paint DEFAULT_PAINT;
    public static final TextAnchor DEFAULT_TEXT_ANCHOR;
    public static final TextAnchor DEFAULT_ROTATION_ANCHOR;
    public static final double DEFAULT_ROTATION_ANGLE = 0.0;
    private String text;
    private Font font;
    private transient Paint paint;
    private double x;
    private double y;
    private TextAnchor textAnchor;
    private TextAnchor rotationAnchor;
    private double rotationAngle;
    private transient Paint backgroundPaint;
    private boolean outlineVisible;
    private transient Paint outlinePaint;
    private transient Stroke outlineStroke;
    public XYTextAnnotation ( final String text, final double x, final double y ) {
        ParamChecks.nullNotPermitted ( text, "text" );
        this.text = text;
        this.font = XYTextAnnotation.DEFAULT_FONT;
        this.paint = XYTextAnnotation.DEFAULT_PAINT;
        this.x = x;
        this.y = y;
        this.textAnchor = XYTextAnnotation.DEFAULT_TEXT_ANCHOR;
        this.rotationAnchor = XYTextAnnotation.DEFAULT_ROTATION_ANCHOR;
        this.rotationAngle = 0.0;
        this.backgroundPaint = null;
        this.outlineVisible = false;
        this.outlinePaint = Color.black;
        this.outlineStroke = new BasicStroke ( 0.5f );
    }
    public String getText() {
        return this.text;
    }
    public void setText ( final String text ) {
        ParamChecks.nullNotPermitted ( text, "text" );
        this.text = text;
        this.fireAnnotationChanged();
    }
    public Font getFont() {
        return this.font;
    }
    public void setFont ( final Font font ) {
        ParamChecks.nullNotPermitted ( font, "font" );
        this.font = font;
        this.fireAnnotationChanged();
    }
    public Paint getPaint() {
        return this.paint;
    }
    public void setPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.paint = paint;
        this.fireAnnotationChanged();
    }
    public TextAnchor getTextAnchor() {
        return this.textAnchor;
    }
    public void setTextAnchor ( final TextAnchor anchor ) {
        ParamChecks.nullNotPermitted ( anchor, "anchor" );
        this.textAnchor = anchor;
        this.fireAnnotationChanged();
    }
    public TextAnchor getRotationAnchor() {
        return this.rotationAnchor;
    }
    public void setRotationAnchor ( final TextAnchor anchor ) {
        ParamChecks.nullNotPermitted ( anchor, "anchor" );
        this.rotationAnchor = anchor;
        this.fireAnnotationChanged();
    }
    public double getRotationAngle() {
        return this.rotationAngle;
    }
    public void setRotationAngle ( final double angle ) {
        this.rotationAngle = angle;
        this.fireAnnotationChanged();
    }
    public double getX() {
        return this.x;
    }
    public void setX ( final double x ) {
        this.x = x;
        this.fireAnnotationChanged();
    }
    public double getY() {
        return this.y;
    }
    public void setY ( final double y ) {
        this.y = y;
        this.fireAnnotationChanged();
    }
    public Paint getBackgroundPaint() {
        return this.backgroundPaint;
    }
    public void setBackgroundPaint ( final Paint paint ) {
        this.backgroundPaint = paint;
        this.fireAnnotationChanged();
    }
    public Paint getOutlinePaint() {
        return this.outlinePaint;
    }
    public void setOutlinePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.outlinePaint = paint;
        this.fireAnnotationChanged();
    }
    public Stroke getOutlineStroke() {
        return this.outlineStroke;
    }
    public void setOutlineStroke ( final Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.outlineStroke = stroke;
        this.fireAnnotationChanged();
    }
    public boolean isOutlineVisible() {
        return this.outlineVisible;
    }
    public void setOutlineVisible ( final boolean visible ) {
        this.outlineVisible = visible;
        this.fireAnnotationChanged();
    }
    @Override
    public void draw ( final Graphics2D g2, final XYPlot plot, final Rectangle2D dataArea, final ValueAxis domainAxis, final ValueAxis rangeAxis, final int rendererIndex, final PlotRenderingInfo info ) {
        final PlotOrientation orientation = plot.getOrientation();
        final RectangleEdge domainEdge = Plot.resolveDomainAxisLocation ( plot.getDomainAxisLocation(), orientation );
        final RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation ( plot.getRangeAxisLocation(), orientation );
        float anchorX = ( float ) domainAxis.valueToJava2D ( this.x, dataArea, domainEdge );
        float anchorY = ( float ) rangeAxis.valueToJava2D ( this.y, dataArea, rangeEdge );
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            final float tempAnchor = anchorX;
            anchorX = anchorY;
            anchorY = tempAnchor;
        }
        g2.setFont ( this.getFont() );
        final Shape hotspot = TextUtilities.calculateRotatedStringBounds ( this.getText(), g2, anchorX, anchorY, this.getTextAnchor(), this.getRotationAngle(), this.getRotationAnchor() );
        if ( this.backgroundPaint != null ) {
            g2.setPaint ( this.backgroundPaint );
            g2.fill ( hotspot );
        }
        g2.setPaint ( this.getPaint() );
        TextUtilities.drawRotatedString ( this.getText(), g2, anchorX, anchorY, this.getTextAnchor(), this.getRotationAngle(), this.getRotationAnchor() );
        if ( this.outlineVisible ) {
            g2.setStroke ( this.outlineStroke );
            g2.setPaint ( this.outlinePaint );
            g2.draw ( hotspot );
        }
        final String toolTip = this.getToolTipText();
        final String url = this.getURL();
        if ( toolTip != null || url != null ) {
            this.addEntity ( info, hotspot, rendererIndex, toolTip, url );
        }
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof XYTextAnnotation ) ) {
            return false;
        }
        final XYTextAnnotation that = ( XYTextAnnotation ) obj;
        return this.text.equals ( that.text ) && this.x == that.x && this.y == that.y && this.font.equals ( that.font ) && PaintUtilities.equal ( this.paint, that.paint ) && this.rotationAnchor.equals ( ( Object ) that.rotationAnchor ) && this.rotationAngle == that.rotationAngle && this.textAnchor.equals ( ( Object ) that.textAnchor ) && this.outlineVisible == that.outlineVisible && PaintUtilities.equal ( this.backgroundPaint, that.backgroundPaint ) && PaintUtilities.equal ( this.outlinePaint, that.outlinePaint ) && this.outlineStroke.equals ( that.outlineStroke ) && super.equals ( obj );
    }
    @Override
    public int hashCode() {
        int result = 193;
        result = 37 * result + this.text.hashCode();
        result = 37 * result + this.font.hashCode();
        result = 37 * result + HashUtilities.hashCodeForPaint ( this.paint );
        long temp = Double.doubleToLongBits ( this.x );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        temp = Double.doubleToLongBits ( this.y );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        result = 37 * result + this.textAnchor.hashCode();
        result = 37 * result + this.rotationAnchor.hashCode();
        temp = Double.doubleToLongBits ( this.rotationAngle );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        return result;
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.paint, stream );
        SerialUtilities.writePaint ( this.backgroundPaint, stream );
        SerialUtilities.writePaint ( this.outlinePaint, stream );
        SerialUtilities.writeStroke ( this.outlineStroke, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.paint = SerialUtilities.readPaint ( stream );
        this.backgroundPaint = SerialUtilities.readPaint ( stream );
        this.outlinePaint = SerialUtilities.readPaint ( stream );
        this.outlineStroke = SerialUtilities.readStroke ( stream );
    }
    static {
        DEFAULT_FONT = new Font ( "SansSerif", 0, 10 );
        DEFAULT_PAINT = Color.black;
        DEFAULT_TEXT_ANCHOR = TextAnchor.CENTER;
        DEFAULT_ROTATION_ANCHOR = TextAnchor.CENTER;
    }
}
