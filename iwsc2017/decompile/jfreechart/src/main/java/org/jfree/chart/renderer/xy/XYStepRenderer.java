package org.jfree.chart.renderer.xy;
import org.jfree.chart.HashUtilities;
import org.jfree.chart.util.LineUtilities;
import java.awt.geom.Line2D;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.ui.RectangleEdge;
import java.awt.Stroke;
import java.awt.Paint;
import java.awt.Shape;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.PlotRenderingInfo;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class XYStepRenderer extends XYLineAndShapeRenderer implements XYItemRenderer, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = -8918141928884796108L;
    private double stepPoint;
    public XYStepRenderer() {
        this ( null, null );
    }
    public XYStepRenderer ( final XYToolTipGenerator toolTipGenerator, final XYURLGenerator urlGenerator ) {
        this.stepPoint = 1.0;
        this.setBaseToolTipGenerator ( toolTipGenerator );
        this.setURLGenerator ( urlGenerator );
        this.setBaseShapesVisible ( false );
    }
    public double getStepPoint() {
        return this.stepPoint;
    }
    public void setStepPoint ( final double stepPoint ) {
        if ( stepPoint < 0.0 || stepPoint > 1.0 ) {
            throw new IllegalArgumentException ( "Requires stepPoint in [0.0;1.0]" );
        }
        this.stepPoint = stepPoint;
        this.fireChangeEvent();
    }
    @Override
    public void drawItem ( final Graphics2D g2, final XYItemRendererState state, final Rectangle2D dataArea, final PlotRenderingInfo info, final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis, final XYDataset dataset, final int series, final int item, final CrosshairState crosshairState, final int pass ) {
        if ( !this.getItemVisible ( series, item ) ) {
            return;
        }
        final PlotOrientation orientation = plot.getOrientation();
        final Paint seriesPaint = this.getItemPaint ( series, item );
        final Stroke seriesStroke = this.getItemStroke ( series, item );
        g2.setPaint ( seriesPaint );
        g2.setStroke ( seriesStroke );
        final double x1 = dataset.getXValue ( series, item );
        final double y1 = dataset.getYValue ( series, item );
        final RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        final RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
        final double transX1 = domainAxis.valueToJava2D ( x1, dataArea, xAxisLocation );
        final double transY1 = Double.isNaN ( y1 ) ? Double.NaN : rangeAxis.valueToJava2D ( y1, dataArea, yAxisLocation );
        if ( pass == 0 && item > 0 ) {
            final double x2 = dataset.getXValue ( series, item - 1 );
            final double y2 = dataset.getYValue ( series, item - 1 );
            final double transX2 = domainAxis.valueToJava2D ( x2, dataArea, xAxisLocation );
            final double transY2 = Double.isNaN ( y2 ) ? Double.NaN : rangeAxis.valueToJava2D ( y2, dataArea, yAxisLocation );
            if ( orientation == PlotOrientation.HORIZONTAL ) {
                if ( transY2 == transY1 ) {
                    this.drawLine ( g2, state.workingLine, transY2, transX2, transY1, transX1, dataArea );
                } else {
                    final double transXs = transX2 + this.getStepPoint() * ( transX1 - transX2 );
                    this.drawLine ( g2, state.workingLine, transY2, transX2, transY2, transXs, dataArea );
                    this.drawLine ( g2, state.workingLine, transY2, transXs, transY1, transXs, dataArea );
                    this.drawLine ( g2, state.workingLine, transY1, transXs, transY1, transX1, dataArea );
                }
            } else if ( orientation == PlotOrientation.VERTICAL ) {
                if ( transY2 == transY1 ) {
                    this.drawLine ( g2, state.workingLine, transX2, transY2, transX1, transY1, dataArea );
                } else {
                    final double transXs = transX2 + this.getStepPoint() * ( transX1 - transX2 );
                    this.drawLine ( g2, state.workingLine, transX2, transY2, transXs, transY2, dataArea );
                    this.drawLine ( g2, state.workingLine, transXs, transY2, transXs, transY1, dataArea );
                    this.drawLine ( g2, state.workingLine, transXs, transY1, transX1, transY1, dataArea );
                }
            }
            final int domainAxisIndex = plot.getDomainAxisIndex ( domainAxis );
            final int rangeAxisIndex = plot.getRangeAxisIndex ( rangeAxis );
            this.updateCrosshairValues ( crosshairState, x1, y1, domainAxisIndex, rangeAxisIndex, transX1, transY1, orientation );
            final EntityCollection entities = state.getEntityCollection();
            if ( entities != null ) {
                this.addEntity ( entities, null, dataset, series, item, transX1, transY1 );
            }
        }
        if ( pass == 1 && this.isItemLabelVisible ( series, item ) ) {
            double xx = transX1;
            double yy = transY1;
            if ( orientation == PlotOrientation.HORIZONTAL ) {
                xx = transY1;
                yy = transX1;
            }
            this.drawItemLabel ( g2, orientation, dataset, series, item, xx, yy, y1 < 0.0 );
        }
    }
    private void drawLine ( final Graphics2D g2, final Line2D line, final double x0, final double y0, final double x1, final double y1, final Rectangle2D dataArea ) {
        if ( Double.isNaN ( x0 ) || Double.isNaN ( x1 ) || Double.isNaN ( y0 ) || Double.isNaN ( y1 ) ) {
            return;
        }
        line.setLine ( x0, y0, x1, y1 );
        final boolean visible = LineUtilities.clipLine ( line, dataArea );
        if ( visible ) {
            g2.draw ( line );
        }
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof XYLineAndShapeRenderer ) ) {
            return false;
        }
        final XYStepRenderer that = ( XYStepRenderer ) obj;
        return this.stepPoint == that.stepPoint && super.equals ( obj );
    }
    public int hashCode() {
        return HashUtilities.hashCode ( super.hashCode(), this.stepPoint );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
