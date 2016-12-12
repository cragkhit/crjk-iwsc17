package org.jfree.chart.renderer.xy;
import org.jfree.util.ObjectUtilities;
import java.awt.geom.Point2D;
import org.jfree.chart.labels.ItemLabelPosition;
import java.awt.Font;
import org.jfree.text.TextUtilities;
import java.awt.Stroke;
import java.awt.Paint;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.entity.EntityCollection;
import java.awt.Shape;
import org.jfree.util.ShapeUtilities;
import java.awt.geom.Line2D;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.PlotRenderingInfo;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.labels.XYItemLabelGenerator;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class YIntervalRenderer extends AbstractXYItemRenderer implements XYItemRenderer, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = -2951586537224143260L;
    private XYItemLabelGenerator additionalItemLabelGenerator;
    public YIntervalRenderer() {
        this.additionalItemLabelGenerator = null;
    }
    public XYItemLabelGenerator getAdditionalItemLabelGenerator() {
        return this.additionalItemLabelGenerator;
    }
    public void setAdditionalItemLabelGenerator ( final XYItemLabelGenerator generator ) {
        this.additionalItemLabelGenerator = generator;
        this.fireChangeEvent();
    }
    @Override
    public Range findRangeBounds ( final XYDataset dataset ) {
        return this.findRangeBounds ( dataset, true );
    }
    @Override
    public void drawItem ( final Graphics2D g2, final XYItemRendererState state, final Rectangle2D dataArea, final PlotRenderingInfo info, final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis, final XYDataset dataset, final int series, final int item, final CrosshairState crosshairState, final int pass ) {
        if ( !this.getItemVisible ( series, item ) ) {
            return;
        }
        EntityCollection entities = null;
        if ( info != null ) {
            entities = info.getOwner().getEntityCollection();
        }
        final IntervalXYDataset intervalDataset = ( IntervalXYDataset ) dataset;
        final double x = intervalDataset.getXValue ( series, item );
        final double yLow = intervalDataset.getStartYValue ( series, item );
        final double yHigh = intervalDataset.getEndYValue ( series, item );
        final RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        final RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
        final double xx = domainAxis.valueToJava2D ( x, dataArea, xAxisLocation );
        final double yyLow = rangeAxis.valueToJava2D ( yLow, dataArea, yAxisLocation );
        final double yyHigh = rangeAxis.valueToJava2D ( yHigh, dataArea, yAxisLocation );
        final Paint p = this.getItemPaint ( series, item );
        final Stroke s = this.getItemStroke ( series, item );
        Line2D line = null;
        final Shape shape = this.getItemShape ( series, item );
        Shape top = null;
        Shape bottom = null;
        final PlotOrientation orientation = plot.getOrientation();
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            line = new Line2D.Double ( yyLow, xx, yyHigh, xx );
            top = ShapeUtilities.createTranslatedShape ( shape, yyHigh, xx );
            bottom = ShapeUtilities.createTranslatedShape ( shape, yyLow, xx );
        } else {
            if ( orientation != PlotOrientation.VERTICAL ) {
                throw new IllegalStateException();
            }
            line = new Line2D.Double ( xx, yyLow, xx, yyHigh );
            top = ShapeUtilities.createTranslatedShape ( shape, xx, yyHigh );
            bottom = ShapeUtilities.createTranslatedShape ( shape, xx, yyLow );
        }
        g2.setPaint ( p );
        g2.setStroke ( s );
        g2.draw ( line );
        g2.fill ( top );
        g2.fill ( bottom );
        if ( this.isItemLabelVisible ( series, item ) ) {
            this.drawItemLabel ( g2, orientation, dataset, series, item, xx, yyHigh, false );
            this.drawAdditionalItemLabel ( g2, orientation, dataset, series, item, xx, yyLow );
        }
        final Shape hotspot = ShapeUtilities.createLineRegion ( line, 4.0f );
        if ( entities != null && hotspot.intersects ( dataArea ) ) {
            this.addEntity ( entities, hotspot, dataset, series, item, xx, ( yyHigh + yyLow ) / 2.0 );
        }
    }
    private void drawAdditionalItemLabel ( final Graphics2D g2, final PlotOrientation orientation, final XYDataset dataset, final int series, final int item, final double x, final double y ) {
        if ( this.additionalItemLabelGenerator == null ) {
            return;
        }
        final Font labelFont = this.getItemLabelFont ( series, item );
        final Paint paint = this.getItemLabelPaint ( series, item );
        g2.setFont ( labelFont );
        g2.setPaint ( paint );
        final String label = this.additionalItemLabelGenerator.generateLabel ( dataset, series, item );
        final ItemLabelPosition position = this.getNegativeItemLabelPosition ( series, item );
        final Point2D anchorPoint = this.calculateLabelAnchorPoint ( position.getItemLabelAnchor(), x, y, orientation );
        TextUtilities.drawRotatedString ( label, g2, ( float ) anchorPoint.getX(), ( float ) anchorPoint.getY(), position.getTextAnchor(), position.getAngle(), position.getRotationAnchor() );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof YIntervalRenderer ) ) {
            return false;
        }
        final YIntervalRenderer that = ( YIntervalRenderer ) obj;
        return ObjectUtilities.equal ( ( Object ) this.additionalItemLabelGenerator, ( Object ) that.additionalItemLabelGenerator ) && super.equals ( obj );
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
