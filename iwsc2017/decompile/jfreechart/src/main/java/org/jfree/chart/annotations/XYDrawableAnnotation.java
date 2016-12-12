package org.jfree.chart.annotations;
import org.jfree.util.ObjectUtilities;
import java.awt.geom.AffineTransform;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.plot.PlotOrientation;
import java.awt.Shape;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.axis.ValueAxis;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.plot.XYPlot;
import java.awt.Graphics2D;
import org.jfree.chart.util.ParamChecks;
import org.jfree.ui.Drawable;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class XYDrawableAnnotation extends AbstractXYAnnotation implements Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = -6540812859722691020L;
    private double drawScaleFactor;
    private double x;
    private double y;
    private double displayWidth;
    private double displayHeight;
    private Drawable drawable;
    public XYDrawableAnnotation ( final double x, final double y, final double width, final double height, final Drawable drawable ) {
        this ( x, y, width, height, 1.0, drawable );
    }
    public XYDrawableAnnotation ( final double x, final double y, final double displayWidth, final double displayHeight, final double drawScaleFactor, final Drawable drawable ) {
        ParamChecks.nullNotPermitted ( drawable, "drawable" );
        this.x = x;
        this.y = y;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.drawScaleFactor = drawScaleFactor;
        this.drawable = drawable;
    }
    @Override
    public void draw ( final Graphics2D g2, final XYPlot plot, final Rectangle2D dataArea, final ValueAxis domainAxis, final ValueAxis rangeAxis, final int rendererIndex, final PlotRenderingInfo info ) {
        final PlotOrientation orientation = plot.getOrientation();
        final RectangleEdge domainEdge = Plot.resolveDomainAxisLocation ( plot.getDomainAxisLocation(), orientation );
        final RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation ( plot.getRangeAxisLocation(), orientation );
        final float j2DX = ( float ) domainAxis.valueToJava2D ( this.x, dataArea, domainEdge );
        final float j2DY = ( float ) rangeAxis.valueToJava2D ( this.y, dataArea, rangeEdge );
        final Rectangle2D displayArea = new Rectangle2D.Double ( j2DX - this.displayWidth / 2.0, j2DY - this.displayHeight / 2.0, this.displayWidth, this.displayHeight );
        final AffineTransform savedTransform = g2.getTransform();
        final Rectangle2D drawArea = new Rectangle2D.Double ( 0.0, 0.0, this.displayWidth * this.drawScaleFactor, this.displayHeight * this.drawScaleFactor );
        g2.scale ( 1.0 / this.drawScaleFactor, 1.0 / this.drawScaleFactor );
        g2.translate ( ( j2DX - this.displayWidth / 2.0 ) * this.drawScaleFactor, ( j2DY - this.displayHeight / 2.0 ) * this.drawScaleFactor );
        this.drawable.draw ( g2, drawArea );
        g2.setTransform ( savedTransform );
        final String toolTip = this.getToolTipText();
        final String url = this.getURL();
        if ( toolTip != null || url != null ) {
            this.addEntity ( info, displayArea, rendererIndex, toolTip, url );
        }
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        if ( ! ( obj instanceof XYDrawableAnnotation ) ) {
            return false;
        }
        final XYDrawableAnnotation that = ( XYDrawableAnnotation ) obj;
        return this.x == that.x && this.y == that.y && this.displayWidth == that.displayWidth && this.displayHeight == that.displayHeight && this.drawScaleFactor == that.drawScaleFactor && ObjectUtilities.equal ( ( Object ) this.drawable, ( Object ) that.drawable );
    }
    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits ( this.x );
        int result = ( int ) ( temp ^ temp >>> 32 );
        temp = Double.doubleToLongBits ( this.y );
        result = 29 * result + ( int ) ( temp ^ temp >>> 32 );
        temp = Double.doubleToLongBits ( this.displayWidth );
        result = 29 * result + ( int ) ( temp ^ temp >>> 32 );
        temp = Double.doubleToLongBits ( this.displayHeight );
        result = 29 * result + ( int ) ( temp ^ temp >>> 32 );
        return result;
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
