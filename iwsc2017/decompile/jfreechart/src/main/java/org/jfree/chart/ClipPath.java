package org.jfree.chart;
import org.jfree.ui.RectangleEdge;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import org.jfree.chart.axis.ValueAxis;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Stroke;
import java.awt.Paint;
public class ClipPath implements Cloneable {
    private double[] xValue;
    private double[] yValue;
    private boolean clip;
    private boolean drawPath;
    private boolean fillPath;
    private Paint fillPaint;
    private Paint drawPaint;
    private Stroke drawStroke;
    private Composite composite;
    public ClipPath() {
        this.xValue = null;
        this.yValue = null;
        this.clip = true;
        this.drawPath = false;
        this.fillPath = false;
        this.fillPaint = null;
        this.drawPaint = null;
        this.drawStroke = null;
        this.composite = null;
    }
    public ClipPath ( final double[] xValue, final double[] yValue ) {
        this ( xValue, yValue, true, false, true );
    }
    public ClipPath ( final double[] xValue, final double[] yValue, final boolean clip, final boolean fillPath, final boolean drawPath ) {
        this.xValue = null;
        this.yValue = null;
        this.clip = true;
        this.drawPath = false;
        this.fillPath = false;
        this.fillPaint = null;
        this.drawPaint = null;
        this.drawStroke = null;
        this.composite = null;
        this.xValue = xValue;
        this.yValue = yValue;
        this.clip = clip;
        this.fillPath = fillPath;
        this.drawPath = drawPath;
        this.fillPaint = Color.gray;
        this.drawPaint = Color.blue;
        this.drawStroke = new BasicStroke ( 1.0f );
        this.composite = AlphaComposite.Src;
    }
    public ClipPath ( final double[] xValue, final double[] yValue, final boolean fillPath, final boolean drawPath, final Paint fillPaint, final Paint drawPaint, final Stroke drawStroke, final Composite composite ) {
        this.xValue = null;
        this.yValue = null;
        this.clip = true;
        this.drawPath = false;
        this.fillPath = false;
        this.fillPaint = null;
        this.drawPaint = null;
        this.drawStroke = null;
        this.composite = null;
        this.xValue = xValue;
        this.yValue = yValue;
        this.fillPath = fillPath;
        this.drawPath = drawPath;
        this.fillPaint = fillPaint;
        this.drawPaint = drawPaint;
        this.drawStroke = drawStroke;
        this.composite = composite;
    }
    public GeneralPath draw ( final Graphics2D g2, final Rectangle2D dataArea, final ValueAxis horizontalAxis, final ValueAxis verticalAxis ) {
        final GeneralPath generalPath = this.generateClipPath ( dataArea, horizontalAxis, verticalAxis );
        if ( this.fillPath || this.drawPath ) {
            final Composite saveComposite = g2.getComposite();
            final Paint savePaint = g2.getPaint();
            final Stroke saveStroke = g2.getStroke();
            if ( this.fillPaint != null ) {
                g2.setPaint ( this.fillPaint );
            }
            if ( this.composite != null ) {
                g2.setComposite ( this.composite );
            }
            if ( this.fillPath ) {
                g2.fill ( generalPath );
            }
            if ( this.drawStroke != null ) {
                g2.setStroke ( this.drawStroke );
            }
            if ( this.drawPath ) {
                g2.draw ( generalPath );
            }
            g2.setPaint ( savePaint );
            g2.setComposite ( saveComposite );
            g2.setStroke ( saveStroke );
        }
        return generalPath;
    }
    public GeneralPath generateClipPath ( final Rectangle2D dataArea, final ValueAxis horizontalAxis, final ValueAxis verticalAxis ) {
        final GeneralPath generalPath = new GeneralPath();
        double transX = horizontalAxis.valueToJava2D ( this.xValue[0], dataArea, RectangleEdge.BOTTOM );
        double transY = verticalAxis.valueToJava2D ( this.yValue[0], dataArea, RectangleEdge.LEFT );
        generalPath.moveTo ( ( float ) transX, ( float ) transY );
        for ( int k = 0; k < this.yValue.length; ++k ) {
            transX = horizontalAxis.valueToJava2D ( this.xValue[k], dataArea, RectangleEdge.BOTTOM );
            transY = verticalAxis.valueToJava2D ( this.yValue[k], dataArea, RectangleEdge.LEFT );
            generalPath.lineTo ( ( float ) transX, ( float ) transY );
        }
        generalPath.closePath();
        return generalPath;
    }
    public Composite getComposite() {
        return this.composite;
    }
    public Paint getDrawPaint() {
        return this.drawPaint;
    }
    public boolean isDrawPath() {
        return this.drawPath;
    }
    public Stroke getDrawStroke() {
        return this.drawStroke;
    }
    public Paint getFillPaint() {
        return this.fillPaint;
    }
    public boolean isFillPath() {
        return this.fillPath;
    }
    public double[] getXValue() {
        return this.xValue;
    }
    public double[] getYValue() {
        return this.yValue;
    }
    public void setComposite ( final Composite composite ) {
        this.composite = composite;
    }
    public void setDrawPaint ( final Paint drawPaint ) {
        this.drawPaint = drawPaint;
    }
    public void setDrawPath ( final boolean drawPath ) {
        this.drawPath = drawPath;
    }
    public void setDrawStroke ( final Stroke drawStroke ) {
        this.drawStroke = drawStroke;
    }
    public void setFillPaint ( final Paint fillPaint ) {
        this.fillPaint = fillPaint;
    }
    public void setFillPath ( final boolean fillPath ) {
        this.fillPath = fillPath;
    }
    public void setXValue ( final double[] xValue ) {
        this.xValue = xValue;
    }
    public void setYValue ( final double[] yValue ) {
        this.yValue = yValue;
    }
    public boolean isClip() {
        return this.clip;
    }
    public void setClip ( final boolean clip ) {
        this.clip = clip;
    }
    public Object clone() throws CloneNotSupportedException {
        final ClipPath clone = ( ClipPath ) super.clone();
        clone.xValue = this.xValue.clone();
        clone.yValue = this.yValue.clone();
        return clone;
    }
}
