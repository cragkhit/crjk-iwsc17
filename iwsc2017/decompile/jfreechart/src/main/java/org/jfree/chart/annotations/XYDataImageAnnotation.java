package org.jfree.chart.annotations;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.jfree.util.ObjectUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.axis.AxisLocation;
import java.awt.Shape;
import java.awt.image.ImageObserver;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.axis.ValueAxis;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.plot.XYPlot;
import java.awt.Graphics2D;
import org.jfree.data.Range;
import org.jfree.chart.util.ParamChecks;
import java.awt.Image;
import org.jfree.util.PublicCloneable;
public class XYDataImageAnnotation extends AbstractXYAnnotation implements Cloneable, PublicCloneable, XYAnnotationBoundsInfo {
    private transient Image image;
    private double x;
    private double y;
    private double w;
    private double h;
    private boolean includeInDataBounds;
    public XYDataImageAnnotation ( final Image image, final double x, final double y, final double w, final double h ) {
        this ( image, x, y, w, h, false );
    }
    public XYDataImageAnnotation ( final Image image, final double x, final double y, final double w, final double h, final boolean includeInDataBounds ) {
        ParamChecks.nullNotPermitted ( image, "image" );
        this.image = image;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.includeInDataBounds = includeInDataBounds;
    }
    public Image getImage() {
        return this.image;
    }
    public double getX() {
        return this.x;
    }
    public double getY() {
        return this.y;
    }
    public double getWidth() {
        return this.w;
    }
    public double getHeight() {
        return this.h;
    }
    public boolean getIncludeInDataBounds() {
        return this.includeInDataBounds;
    }
    public Range getXRange() {
        return new Range ( this.x, this.x + this.w );
    }
    public Range getYRange() {
        return new Range ( this.y, this.y + this.h );
    }
    @Override
    public void draw ( final Graphics2D g2, final XYPlot plot, final Rectangle2D dataArea, final ValueAxis domainAxis, final ValueAxis rangeAxis, final int rendererIndex, final PlotRenderingInfo info ) {
        final PlotOrientation orientation = plot.getOrientation();
        final AxisLocation xAxisLocation = plot.getDomainAxisLocation();
        final AxisLocation yAxisLocation = plot.getRangeAxisLocation();
        final RectangleEdge xEdge = Plot.resolveDomainAxisLocation ( xAxisLocation, orientation );
        final RectangleEdge yEdge = Plot.resolveRangeAxisLocation ( yAxisLocation, orientation );
        final float j2DX0 = ( float ) domainAxis.valueToJava2D ( this.x, dataArea, xEdge );
        final float j2DY0 = ( float ) rangeAxis.valueToJava2D ( this.y, dataArea, yEdge );
        final float j2DX = ( float ) domainAxis.valueToJava2D ( this.x + this.w, dataArea, xEdge );
        final float j2DY = ( float ) rangeAxis.valueToJava2D ( this.y + this.h, dataArea, yEdge );
        float xx0 = 0.0f;
        float yy0 = 0.0f;
        float xx = 0.0f;
        float yy = 0.0f;
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            xx0 = j2DY0;
            xx = j2DY;
            yy0 = j2DX0;
            yy = j2DX;
        } else if ( orientation == PlotOrientation.VERTICAL ) {
            xx0 = j2DX0;
            xx = j2DX;
            yy0 = j2DY0;
            yy = j2DY;
        }
        g2.drawImage ( this.image, ( int ) xx0, ( int ) Math.min ( yy0, yy ), ( int ) ( xx - xx0 ), ( int ) Math.abs ( yy - yy0 ), null );
        final String toolTip = this.getToolTipText();
        final String url = this.getURL();
        if ( toolTip != null || url != null ) {
            this.addEntity ( info, new Rectangle2D.Float ( xx0, yy0, xx - xx0, yy - yy0 ), rendererIndex, toolTip, url );
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
        if ( ! ( obj instanceof XYDataImageAnnotation ) ) {
            return false;
        }
        final XYDataImageAnnotation that = ( XYDataImageAnnotation ) obj;
        return this.x == that.x && this.y == that.y && this.w == that.w && this.h == that.h && this.includeInDataBounds == that.includeInDataBounds && ObjectUtilities.equal ( ( Object ) this.image, ( Object ) that.image );
    }
    @Override
    public int hashCode() {
        return this.image.hashCode();
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
    }
}
