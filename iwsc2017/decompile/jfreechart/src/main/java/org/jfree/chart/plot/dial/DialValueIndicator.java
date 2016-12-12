package org.jfree.chart.plot.dial;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.chart.HashUtilities;
import org.jfree.util.PaintUtilities;
import org.jfree.util.ObjectUtilities;
import java.awt.FontMetrics;
import java.awt.geom.Point2D;
import java.awt.Shape;
import org.jfree.ui.Size2D;
import org.jfree.text.TextUtilities;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.chart.util.ParamChecks;
import java.awt.BasicStroke;
import java.awt.Color;
import java.text.DecimalFormat;
import org.jfree.ui.TextAnchor;
import org.jfree.ui.RectangleInsets;
import java.awt.Stroke;
import java.awt.Paint;
import java.awt.Font;
import java.text.NumberFormat;
import org.jfree.ui.RectangleAnchor;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class DialValueIndicator extends AbstractDialLayer implements DialLayer, Cloneable, PublicCloneable, Serializable {
    static final long serialVersionUID = 803094354130942585L;
    private int datasetIndex;
    private double angle;
    private double radius;
    private RectangleAnchor frameAnchor;
    private Number templateValue;
    private Number maxTemplateValue;
    private NumberFormat formatter;
    private Font font;
    private transient Paint paint;
    private transient Paint backgroundPaint;
    private transient Stroke outlineStroke;
    private transient Paint outlinePaint;
    private RectangleInsets insets;
    private RectangleAnchor valueAnchor;
    private TextAnchor textAnchor;
    public DialValueIndicator() {
        this ( 0 );
    }
    public DialValueIndicator ( final int datasetIndex ) {
        this.datasetIndex = datasetIndex;
        this.angle = -90.0;
        this.radius = 0.3;
        this.frameAnchor = RectangleAnchor.CENTER;
        this.templateValue = new Double ( 100.0 );
        this.maxTemplateValue = null;
        this.formatter = new DecimalFormat ( "0.0" );
        this.font = new Font ( "Dialog", 1, 14 );
        this.paint = Color.black;
        this.backgroundPaint = Color.white;
        this.outlineStroke = new BasicStroke ( 1.0f );
        this.outlinePaint = Color.blue;
        this.insets = new RectangleInsets ( 4.0, 4.0, 4.0, 4.0 );
        this.valueAnchor = RectangleAnchor.RIGHT;
        this.textAnchor = TextAnchor.CENTER_RIGHT;
    }
    public int getDatasetIndex() {
        return this.datasetIndex;
    }
    public void setDatasetIndex ( final int index ) {
        this.datasetIndex = index;
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
        this.radius = radius;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public RectangleAnchor getFrameAnchor() {
        return this.frameAnchor;
    }
    public void setFrameAnchor ( final RectangleAnchor anchor ) {
        ParamChecks.nullNotPermitted ( anchor, "anchor" );
        this.frameAnchor = anchor;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public Number getTemplateValue() {
        return this.templateValue;
    }
    public void setTemplateValue ( final Number value ) {
        ParamChecks.nullNotPermitted ( value, "value" );
        this.templateValue = value;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public Number getMaxTemplateValue() {
        return this.maxTemplateValue;
    }
    public void setMaxTemplateValue ( final Number value ) {
        this.maxTemplateValue = value;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public NumberFormat getNumberFormat() {
        return this.formatter;
    }
    public void setNumberFormat ( final NumberFormat formatter ) {
        ParamChecks.nullNotPermitted ( formatter, "formatter" );
        this.formatter = formatter;
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
    public Paint getBackgroundPaint() {
        return this.backgroundPaint;
    }
    public void setBackgroundPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.backgroundPaint = paint;
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
    public Paint getOutlinePaint() {
        return this.outlinePaint;
    }
    public void setOutlinePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.outlinePaint = paint;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public RectangleInsets getInsets() {
        return this.insets;
    }
    public void setInsets ( final RectangleInsets insets ) {
        ParamChecks.nullNotPermitted ( insets, "insets" );
        this.insets = insets;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public RectangleAnchor getValueAnchor() {
        return this.valueAnchor;
    }
    public void setValueAnchor ( final RectangleAnchor anchor ) {
        ParamChecks.nullNotPermitted ( anchor, "anchor" );
        this.valueAnchor = anchor;
        this.notifyListeners ( new DialLayerChangeEvent ( this ) );
    }
    public TextAnchor getTextAnchor() {
        return this.textAnchor;
    }
    public void setTextAnchor ( final TextAnchor anchor ) {
        ParamChecks.nullNotPermitted ( anchor, "anchor" );
        this.textAnchor = anchor;
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
        final FontMetrics fm = g2.getFontMetrics ( this.font );
        final double value = plot.getValue ( this.datasetIndex );
        final String valueStr = this.formatter.format ( value );
        final Rectangle2D valueBounds = TextUtilities.getTextBounds ( valueStr, g2, fm );
        String s = this.formatter.format ( this.templateValue );
        Rectangle2D tb = TextUtilities.getTextBounds ( s, g2, fm );
        final double minW = tb.getWidth();
        final double minH = tb.getHeight();
        double maxW = Double.MAX_VALUE;
        double maxH = Double.MAX_VALUE;
        if ( this.maxTemplateValue != null ) {
            s = this.formatter.format ( this.maxTemplateValue );
            tb = TextUtilities.getTextBounds ( s, g2, fm );
            maxW = Math.max ( tb.getWidth(), minW );
            maxH = Math.max ( tb.getHeight(), minH );
        }
        final double w = this.fixToRange ( valueBounds.getWidth(), minW, maxW );
        final double h = this.fixToRange ( valueBounds.getHeight(), minH, maxH );
        final Rectangle2D bounds = RectangleAnchor.createRectangle ( new Size2D ( w, h ), pt.getX(), pt.getY(), this.frameAnchor );
        final Rectangle2D fb = this.insets.createOutsetRectangle ( bounds );
        g2.setPaint ( this.backgroundPaint );
        g2.fill ( fb );
        g2.setStroke ( this.outlineStroke );
        g2.setPaint ( this.outlinePaint );
        g2.draw ( fb );
        final Shape savedClip = g2.getClip();
        g2.clip ( fb );
        final Point2D pt2 = RectangleAnchor.coordinates ( bounds, this.valueAnchor );
        g2.setPaint ( this.paint );
        g2.setFont ( this.font );
        TextUtilities.drawAlignedString ( valueStr, g2, ( float ) pt2.getX(), ( float ) pt2.getY(), this.textAnchor );
        g2.setClip ( savedClip );
    }
    private double fixToRange ( final double x, final double minX, final double maxX ) {
        if ( minX > maxX ) {
            throw new IllegalArgumentException ( "Requires 'minX' <= 'maxX'." );
        }
        if ( x < minX ) {
            return minX;
        }
        if ( x > maxX ) {
            return maxX;
        }
        return x;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof DialValueIndicator ) ) {
            return false;
        }
        final DialValueIndicator that = ( DialValueIndicator ) obj;
        return this.datasetIndex == that.datasetIndex && this.angle == that.angle && this.radius == that.radius && this.frameAnchor.equals ( ( Object ) that.frameAnchor ) && this.templateValue.equals ( that.templateValue ) && ObjectUtilities.equal ( ( Object ) this.maxTemplateValue, ( Object ) that.maxTemplateValue ) && this.font.equals ( that.font ) && PaintUtilities.equal ( this.paint, that.paint ) && PaintUtilities.equal ( this.backgroundPaint, that.backgroundPaint ) && this.outlineStroke.equals ( that.outlineStroke ) && PaintUtilities.equal ( this.outlinePaint, that.outlinePaint ) && this.insets.equals ( ( Object ) that.insets ) && this.valueAnchor.equals ( ( Object ) that.valueAnchor ) && this.textAnchor.equals ( ( Object ) that.textAnchor ) && super.equals ( obj );
    }
    @Override
    public int hashCode() {
        int result = 193;
        result = 37 * result + HashUtilities.hashCodeForPaint ( this.paint );
        result = 37 * result + HashUtilities.hashCodeForPaint ( this.backgroundPaint );
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
}
