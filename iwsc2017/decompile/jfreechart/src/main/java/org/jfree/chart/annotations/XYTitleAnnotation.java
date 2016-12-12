package org.jfree.chart.annotations;
import org.jfree.chart.HashUtilities;
import org.jfree.util.ObjectUtilities;
import java.awt.geom.Point2D;
import org.jfree.ui.Size2D;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.axis.AxisLocation;
import java.awt.Shape;
import org.jfree.chart.block.EntityBlockResult;
import org.jfree.chart.block.BlockParams;
import org.jfree.chart.block.RectangleConstraint;
import org.jfree.data.Range;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.axis.ValueAxis;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.plot.XYPlot;
import java.awt.Graphics2D;
import org.jfree.chart.util.ParamChecks;
import org.jfree.ui.RectangleAnchor;
import org.jfree.chart.title.Title;
import org.jfree.chart.util.XYCoordinateType;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class XYTitleAnnotation extends AbstractXYAnnotation implements Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = -4364694501921559958L;
    private XYCoordinateType coordinateType;
    private double x;
    private double y;
    private double maxWidth;
    private double maxHeight;
    private Title title;
    private RectangleAnchor anchor;
    public XYTitleAnnotation ( final double x, final double y, final Title title ) {
        this ( x, y, title, RectangleAnchor.CENTER );
    }
    public XYTitleAnnotation ( final double x, final double y, final Title title, final RectangleAnchor anchor ) {
        ParamChecks.nullNotPermitted ( title, "title" );
        ParamChecks.nullNotPermitted ( anchor, "anchor" );
        this.coordinateType = XYCoordinateType.RELATIVE;
        this.x = x;
        this.y = y;
        this.maxWidth = 0.0;
        this.maxHeight = 0.0;
        this.title = title;
        this.anchor = anchor;
    }
    public XYCoordinateType getCoordinateType() {
        return this.coordinateType;
    }
    public double getX() {
        return this.x;
    }
    public double getY() {
        return this.y;
    }
    public Title getTitle() {
        return this.title;
    }
    public RectangleAnchor getTitleAnchor() {
        return this.anchor;
    }
    public double getMaxWidth() {
        return this.maxWidth;
    }
    public void setMaxWidth ( final double max ) {
        this.maxWidth = max;
        this.fireAnnotationChanged();
    }
    public double getMaxHeight() {
        return this.maxHeight;
    }
    public void setMaxHeight ( final double max ) {
        this.maxHeight = max;
        this.fireAnnotationChanged();
    }
    @Override
    public void draw ( final Graphics2D g2, final XYPlot plot, final Rectangle2D dataArea, final ValueAxis domainAxis, final ValueAxis rangeAxis, final int rendererIndex, final PlotRenderingInfo info ) {
        final PlotOrientation orientation = plot.getOrientation();
        final AxisLocation domainAxisLocation = plot.getDomainAxisLocation();
        final AxisLocation rangeAxisLocation = plot.getRangeAxisLocation();
        final RectangleEdge domainEdge = Plot.resolveDomainAxisLocation ( domainAxisLocation, orientation );
        final RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation ( rangeAxisLocation, orientation );
        final Range xRange = domainAxis.getRange();
        final Range yRange = rangeAxis.getRange();
        double anchorX;
        double anchorY;
        if ( this.coordinateType == XYCoordinateType.RELATIVE ) {
            anchorX = xRange.getLowerBound() + this.x * xRange.getLength();
            anchorY = yRange.getLowerBound() + this.y * yRange.getLength();
        } else {
            anchorX = domainAxis.valueToJava2D ( this.x, dataArea, domainEdge );
            anchorY = rangeAxis.valueToJava2D ( this.y, dataArea, rangeEdge );
        }
        final float j2DX = ( float ) domainAxis.valueToJava2D ( anchorX, dataArea, domainEdge );
        final float j2DY = ( float ) rangeAxis.valueToJava2D ( anchorY, dataArea, rangeEdge );
        float xx = 0.0f;
        float yy = 0.0f;
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            xx = j2DY;
            yy = j2DX;
        } else if ( orientation == PlotOrientation.VERTICAL ) {
            xx = j2DX;
            yy = j2DY;
        }
        double maxW = dataArea.getWidth();
        double maxH = dataArea.getHeight();
        if ( this.coordinateType == XYCoordinateType.RELATIVE ) {
            if ( this.maxWidth > 0.0 ) {
                maxW *= this.maxWidth;
            }
            if ( this.maxHeight > 0.0 ) {
                maxH *= this.maxHeight;
            }
        }
        if ( this.coordinateType == XYCoordinateType.DATA ) {
            maxW = this.maxWidth;
            maxH = this.maxHeight;
        }
        final RectangleConstraint rc = new RectangleConstraint ( new Range ( 0.0, maxW ), new Range ( 0.0, maxH ) );
        final Size2D size = this.title.arrange ( g2, rc );
        final Rectangle2D titleRect = new Rectangle2D.Double ( 0.0, 0.0, size.width, size.height );
        final Point2D anchorPoint = RectangleAnchor.coordinates ( titleRect, this.anchor );
        xx -= ( float ) anchorPoint.getX();
        yy -= ( float ) anchorPoint.getY();
        titleRect.setRect ( xx, yy, titleRect.getWidth(), titleRect.getHeight() );
        final BlockParams p = new BlockParams();
        if ( info != null && info.getOwner().getEntityCollection() != null ) {
            p.setGenerateEntities ( true );
        }
        final Object result = this.title.draw ( g2, titleRect, p );
        if ( info != null ) {
            if ( result instanceof EntityBlockResult ) {
                final EntityBlockResult ebr = ( EntityBlockResult ) result;
                info.getOwner().getEntityCollection().addAll ( ebr.getEntityCollection() );
            }
            final String toolTip = this.getToolTipText();
            final String url = this.getURL();
            if ( toolTip != null || url != null ) {
                this.addEntity ( info, new Rectangle2D.Float ( xx, yy, ( float ) size.width, ( float ) size.height ), rendererIndex, toolTip, url );
            }
        }
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof XYTitleAnnotation ) ) {
            return false;
        }
        final XYTitleAnnotation that = ( XYTitleAnnotation ) obj;
        return this.coordinateType == that.coordinateType && this.x == that.x && this.y == that.y && this.maxWidth == that.maxWidth && this.maxHeight == that.maxHeight && ObjectUtilities.equal ( ( Object ) this.title, ( Object ) that.title ) && this.anchor.equals ( ( Object ) that.anchor ) && super.equals ( obj );
    }
    @Override
    public int hashCode() {
        int result = 193;
        result = HashUtilities.hashCode ( result, this.anchor );
        result = HashUtilities.hashCode ( result, this.coordinateType );
        result = HashUtilities.hashCode ( result, this.x );
        result = HashUtilities.hashCode ( result, this.y );
        result = HashUtilities.hashCode ( result, this.maxWidth );
        result = HashUtilities.hashCode ( result, this.maxHeight );
        result = HashUtilities.hashCode ( result, this.title );
        return result;
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
