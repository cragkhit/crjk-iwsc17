package org.jfree.chart.annotations;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.jfree.util.ObjectUtilities;
import java.awt.geom.Point2D;
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
import org.jfree.chart.util.ParamChecks;
import org.jfree.ui.RectangleAnchor;
import java.awt.Image;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class XYImageAnnotation extends AbstractXYAnnotation implements Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = -4364694501921559958L;
    private double x;
    private double y;
    private transient Image image;
    private RectangleAnchor anchor;
    public XYImageAnnotation ( final double x, final double y, final Image image ) {
        this ( x, y, image, RectangleAnchor.CENTER );
    }
    public XYImageAnnotation ( final double x, final double y, final Image image, final RectangleAnchor anchor ) {
        ParamChecks.nullNotPermitted ( image, "image" );
        ParamChecks.nullNotPermitted ( anchor, "anchor" );
        this.x = x;
        this.y = y;
        this.image = image;
        this.anchor = anchor;
    }
    public double getX() {
        return this.x;
    }
    public double getY() {
        return this.y;
    }
    public Image getImage() {
        return this.image;
    }
    public RectangleAnchor getImageAnchor() {
        return this.anchor;
    }
    @Override
    public void draw ( final Graphics2D g2, final XYPlot plot, final Rectangle2D dataArea, final ValueAxis domainAxis, final ValueAxis rangeAxis, final int rendererIndex, final PlotRenderingInfo info ) {
        final PlotOrientation orientation = plot.getOrientation();
        final AxisLocation domainAxisLocation = plot.getDomainAxisLocation();
        final AxisLocation rangeAxisLocation = plot.getRangeAxisLocation();
        final RectangleEdge domainEdge = Plot.resolveDomainAxisLocation ( domainAxisLocation, orientation );
        final RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation ( rangeAxisLocation, orientation );
        final float j2DX = ( float ) domainAxis.valueToJava2D ( this.x, dataArea, domainEdge );
        final float j2DY = ( float ) rangeAxis.valueToJava2D ( this.y, dataArea, rangeEdge );
        float xx = 0.0f;
        float yy = 0.0f;
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            xx = j2DY;
            yy = j2DX;
        } else if ( orientation == PlotOrientation.VERTICAL ) {
            xx = j2DX;
            yy = j2DY;
        }
        final int w = this.image.getWidth ( null );
        final int h = this.image.getHeight ( null );
        final Rectangle2D imageRect = new Rectangle2D.Double ( 0.0, 0.0, w, h );
        final Point2D anchorPoint = RectangleAnchor.coordinates ( imageRect, this.anchor );
        xx -= ( float ) anchorPoint.getX();
        yy -= ( float ) anchorPoint.getY();
        g2.drawImage ( this.image, ( int ) xx, ( int ) yy, null );
        final String toolTip = this.getToolTipText();
        final String url = this.getURL();
        if ( toolTip != null || url != null ) {
            this.addEntity ( info, new Rectangle2D.Float ( xx, yy, w, h ), rendererIndex, toolTip, url );
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
        if ( ! ( obj instanceof XYImageAnnotation ) ) {
            return false;
        }
        final XYImageAnnotation that = ( XYImageAnnotation ) obj;
        return this.x == that.x && this.y == that.y && ObjectUtilities.equal ( ( Object ) this.image, ( Object ) that.image ) && this.anchor.equals ( ( Object ) that.anchor );
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
