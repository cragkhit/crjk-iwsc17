package org.jfree.chart.axis;
import org.jfree.util.ObjectUtilities;
import java.util.Iterator;
import java.awt.Paint;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Composite;
import java.awt.AlphaComposite;
import org.jfree.ui.RectangleEdge;
import java.awt.FontMetrics;
import org.jfree.text.TextUtilities;
import java.awt.geom.Rectangle2D;
import java.awt.font.LineMetrics;
import java.awt.Graphics2D;
import org.jfree.chart.plot.IntervalMarker;
import java.util.ArrayList;
import java.util.List;
import java.awt.Font;
import java.io.Serializable;
public class MarkerAxisBand implements Serializable {
    private static final long serialVersionUID = -1729482413886398919L;
    private NumberAxis axis;
    private double topOuterGap;
    private double topInnerGap;
    private double bottomOuterGap;
    private double bottomInnerGap;
    private Font font;
    private List markers;
    public MarkerAxisBand ( final NumberAxis axis, final double topOuterGap, final double topInnerGap, final double bottomOuterGap, final double bottomInnerGap, final Font font ) {
        this.axis = axis;
        this.topOuterGap = topOuterGap;
        this.topInnerGap = topInnerGap;
        this.bottomOuterGap = bottomOuterGap;
        this.bottomInnerGap = bottomInnerGap;
        this.font = font;
        this.markers = new ArrayList();
    }
    public void addMarker ( final IntervalMarker marker ) {
        this.markers.add ( marker );
    }
    public double getHeight ( final Graphics2D g2 ) {
        double result = 0.0;
        if ( this.markers.size() > 0 ) {
            final LineMetrics metrics = this.font.getLineMetrics ( "123g", g2.getFontRenderContext() );
            result = this.topOuterGap + this.topInnerGap + metrics.getHeight() + this.bottomInnerGap + this.bottomOuterGap;
        }
        return result;
    }
    private void drawStringInRect ( final Graphics2D g2, final Rectangle2D bounds, final Font font, final String text ) {
        g2.setFont ( font );
        final FontMetrics fm = g2.getFontMetrics ( font );
        final Rectangle2D r = TextUtilities.getTextBounds ( text, g2, fm );
        double x = bounds.getX();
        if ( r.getWidth() < bounds.getWidth() ) {
            x += ( bounds.getWidth() - r.getWidth() ) / 2.0;
        }
        final LineMetrics metrics = font.getLineMetrics ( text, g2.getFontRenderContext() );
        g2.drawString ( text, ( float ) x, ( float ) ( bounds.getMaxY() - this.bottomInnerGap - metrics.getDescent() ) );
    }
    public void draw ( final Graphics2D g2, final Rectangle2D plotArea, final Rectangle2D dataArea, final double x, final double y ) {
        final double h = this.getHeight ( g2 );
        for ( final IntervalMarker marker : this.markers ) {
            final double start = Math.max ( marker.getStartValue(), this.axis.getRange().getLowerBound() );
            final double end = Math.min ( marker.getEndValue(), this.axis.getRange().getUpperBound() );
            final double s = this.axis.valueToJava2D ( start, dataArea, RectangleEdge.BOTTOM );
            final double e = this.axis.valueToJava2D ( end, dataArea, RectangleEdge.BOTTOM );
            final Rectangle2D r = new Rectangle2D.Double ( s, y + this.topOuterGap, e - s, h - this.topOuterGap - this.bottomOuterGap );
            final Composite originalComposite = g2.getComposite();
            g2.setComposite ( AlphaComposite.getInstance ( 3, marker.getAlpha() ) );
            g2.setPaint ( marker.getPaint() );
            g2.fill ( r );
            g2.setPaint ( marker.getOutlinePaint() );
            g2.draw ( r );
            g2.setComposite ( originalComposite );
            g2.setPaint ( Color.black );
            this.drawStringInRect ( g2, r, this.font, marker.getLabel() );
        }
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof MarkerAxisBand ) ) {
            return false;
        }
        final MarkerAxisBand that = ( MarkerAxisBand ) obj;
        return this.topOuterGap == that.topOuterGap && this.topInnerGap == that.topInnerGap && this.bottomInnerGap == that.bottomInnerGap && this.bottomOuterGap == that.bottomOuterGap && ObjectUtilities.equal ( ( Object ) this.font, ( Object ) that.font ) && ObjectUtilities.equal ( ( Object ) this.markers, ( Object ) that.markers );
    }
    @Override
    public int hashCode() {
        int result = 37;
        result = 19 * result + this.font.hashCode();
        result = 19 * result + this.markers.hashCode();
        return result;
    }
}
