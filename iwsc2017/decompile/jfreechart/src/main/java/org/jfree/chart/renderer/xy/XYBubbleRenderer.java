package org.jfree.chart.renderer.xy;
import java.awt.Stroke;
import java.awt.Paint;
import org.jfree.data.general.Dataset;
import org.jfree.chart.LegendItem;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYZDataset;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.PlotRenderingInfo;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import org.jfree.util.PublicCloneable;
public class XYBubbleRenderer extends AbstractXYItemRenderer implements XYItemRenderer, PublicCloneable {
    public static final long serialVersionUID = -5221991598674249125L;
    public static final int SCALE_ON_BOTH_AXES = 0;
    public static final int SCALE_ON_DOMAIN_AXIS = 1;
    public static final int SCALE_ON_RANGE_AXIS = 2;
    private int scaleType;
    public XYBubbleRenderer() {
        this ( 0 );
    }
    public XYBubbleRenderer ( final int scaleType ) {
        if ( scaleType < 0 || scaleType > 2 ) {
            throw new IllegalArgumentException ( "Invalid 'scaleType'." );
        }
        this.scaleType = scaleType;
        this.setBaseLegendShape ( new Ellipse2D.Double ( -4.0, -4.0, 8.0, 8.0 ) );
    }
    public int getScaleType() {
        return this.scaleType;
    }
    @Override
    public void drawItem ( final Graphics2D g2, final XYItemRendererState state, final Rectangle2D dataArea, final PlotRenderingInfo info, final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis, final XYDataset dataset, final int series, final int item, final CrosshairState crosshairState, final int pass ) {
        if ( !this.getItemVisible ( series, item ) ) {
            return;
        }
        final PlotOrientation orientation = plot.getOrientation();
        final double x = dataset.getXValue ( series, item );
        final double y = dataset.getYValue ( series, item );
        double z = Double.NaN;
        if ( dataset instanceof XYZDataset ) {
            final XYZDataset xyzData = ( XYZDataset ) dataset;
            z = xyzData.getZValue ( series, item );
        }
        if ( !Double.isNaN ( z ) ) {
            final RectangleEdge domainAxisLocation = plot.getDomainAxisEdge();
            final RectangleEdge rangeAxisLocation = plot.getRangeAxisEdge();
            final double transX = domainAxis.valueToJava2D ( x, dataArea, domainAxisLocation );
            final double transY = rangeAxis.valueToJava2D ( y, dataArea, rangeAxisLocation );
            double transRange = 0.0;
            double transDomain = 0.0;
            switch ( this.getScaleType() ) {
            case 1: {
                final double zero = domainAxis.valueToJava2D ( 0.0, dataArea, domainAxisLocation );
                transDomain = ( transRange = domainAxis.valueToJava2D ( z, dataArea, domainAxisLocation ) - zero );
                break;
            }
            case 2: {
                final double zero = rangeAxis.valueToJava2D ( 0.0, dataArea, rangeAxisLocation );
                transRange = ( transDomain = zero - rangeAxis.valueToJava2D ( z, dataArea, rangeAxisLocation ) );
                break;
            }
            default: {
                final double zero2 = domainAxis.valueToJava2D ( 0.0, dataArea, domainAxisLocation );
                final double zero3 = rangeAxis.valueToJava2D ( 0.0, dataArea, rangeAxisLocation );
                transDomain = domainAxis.valueToJava2D ( z, dataArea, domainAxisLocation ) - zero2;
                transRange = zero3 - rangeAxis.valueToJava2D ( z, dataArea, rangeAxisLocation );
                break;
            }
            }
            transDomain = Math.abs ( transDomain );
            transRange = Math.abs ( transRange );
            Ellipse2D circle = null;
            if ( orientation == PlotOrientation.VERTICAL ) {
                circle = new Ellipse2D.Double ( transX - transDomain / 2.0, transY - transRange / 2.0, transDomain, transRange );
            } else {
                if ( orientation != PlotOrientation.HORIZONTAL ) {
                    throw new IllegalStateException();
                }
                circle = new Ellipse2D.Double ( transY - transRange / 2.0, transX - transDomain / 2.0, transRange, transDomain );
            }
            g2.setPaint ( this.getItemPaint ( series, item ) );
            g2.fill ( circle );
            g2.setStroke ( this.getItemOutlineStroke ( series, item ) );
            g2.setPaint ( this.getItemOutlinePaint ( series, item ) );
            g2.draw ( circle );
            if ( this.isItemLabelVisible ( series, item ) ) {
                if ( orientation == PlotOrientation.VERTICAL ) {
                    this.drawItemLabel ( g2, orientation, dataset, series, item, transX, transY, false );
                } else if ( orientation == PlotOrientation.HORIZONTAL ) {
                    this.drawItemLabel ( g2, orientation, dataset, series, item, transY, transX, false );
                }
            }
            if ( info != null ) {
                final EntityCollection entities = info.getOwner().getEntityCollection();
                if ( entities != null && circle.intersects ( dataArea ) ) {
                    this.addEntity ( entities, circle, dataset, series, item, circle.getCenterX(), circle.getCenterY() );
                }
            }
            final int domainAxisIndex = plot.getDomainAxisIndex ( domainAxis );
            final int rangeAxisIndex = plot.getRangeAxisIndex ( rangeAxis );
            this.updateCrosshairValues ( crosshairState, x, y, domainAxisIndex, rangeAxisIndex, transX, transY, orientation );
        }
    }
    @Override
    public LegendItem getLegendItem ( final int datasetIndex, final int series ) {
        LegendItem result = null;
        final XYPlot plot = this.getPlot();
        if ( plot == null ) {
            return null;
        }
        final XYDataset dataset = plot.getDataset ( datasetIndex );
        if ( dataset != null && this.getItemVisible ( series, 0 ) ) {
            final String description;
            final String label = description = this.getLegendItemLabelGenerator().generateLabel ( dataset, series );
            String toolTipText = null;
            if ( this.getLegendItemToolTipGenerator() != null ) {
                toolTipText = this.getLegendItemToolTipGenerator().generateLabel ( dataset, series );
            }
            String urlText = null;
            if ( this.getLegendItemURLGenerator() != null ) {
                urlText = this.getLegendItemURLGenerator().generateLabel ( dataset, series );
            }
            final Shape shape = this.lookupLegendShape ( series );
            final Paint paint = this.lookupSeriesPaint ( series );
            final Paint outlinePaint = this.lookupSeriesOutlinePaint ( series );
            final Stroke outlineStroke = this.lookupSeriesOutlineStroke ( series );
            result = new LegendItem ( label, description, toolTipText, urlText, shape, paint, outlineStroke, outlinePaint );
            result.setLabelFont ( this.lookupLegendTextFont ( series ) );
            final Paint labelPaint = this.lookupLegendTextPaint ( series );
            if ( labelPaint != null ) {
                result.setLabelPaint ( labelPaint );
            }
            result.setDataset ( dataset );
            result.setDatasetIndex ( datasetIndex );
            result.setSeriesKey ( dataset.getSeriesKey ( series ) );
            result.setSeriesIndex ( series );
        }
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof XYBubbleRenderer ) ) {
            return false;
        }
        final XYBubbleRenderer that = ( XYBubbleRenderer ) obj;
        return this.scaleType == that.scaleType && super.equals ( obj );
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
