package org.jfree.chart.renderer.xy;
import java.io.ObjectOutputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectInputStream;
import org.jfree.util.ShapeUtilities;
import java.awt.Paint;
import org.jfree.data.general.Dataset;
import org.jfree.chart.LegendItem;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.PlotRenderingInfo;
import java.awt.Graphics2D;
import org.jfree.chart.util.ParamChecks;
import java.awt.geom.Rectangle2D;
import java.awt.Shape;
import org.jfree.util.PublicCloneable;
public class XYDotRenderer extends AbstractXYItemRenderer implements XYItemRenderer, PublicCloneable {
    private static final long serialVersionUID = -2764344339073566425L;
    private int dotWidth;
    private int dotHeight;
    private transient Shape legendShape;
    public XYDotRenderer() {
        this.dotWidth = 1;
        this.dotHeight = 1;
        this.legendShape = new Rectangle2D.Double ( -3.0, -3.0, 6.0, 6.0 );
    }
    public int getDotWidth() {
        return this.dotWidth;
    }
    public void setDotWidth ( final int w ) {
        if ( w < 1 ) {
            throw new IllegalArgumentException ( "Requires w > 0." );
        }
        this.dotWidth = w;
        this.fireChangeEvent();
    }
    public int getDotHeight() {
        return this.dotHeight;
    }
    public void setDotHeight ( final int h ) {
        if ( h < 1 ) {
            throw new IllegalArgumentException ( "Requires h > 0." );
        }
        this.dotHeight = h;
        this.fireChangeEvent();
    }
    public Shape getLegendShape() {
        return this.legendShape;
    }
    public void setLegendShape ( final Shape shape ) {
        ParamChecks.nullNotPermitted ( shape, "shape" );
        this.legendShape = shape;
        this.fireChangeEvent();
    }
    @Override
    public void drawItem ( final Graphics2D g2, final XYItemRendererState state, final Rectangle2D dataArea, final PlotRenderingInfo info, final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis, final XYDataset dataset, final int series, final int item, final CrosshairState crosshairState, final int pass ) {
        if ( !this.getItemVisible ( series, item ) ) {
            return;
        }
        final double x = dataset.getXValue ( series, item );
        final double y = dataset.getYValue ( series, item );
        final double adjx = ( this.dotWidth - 1 ) / 2.0;
        final double adjy = ( this.dotHeight - 1 ) / 2.0;
        if ( !Double.isNaN ( y ) ) {
            final RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
            final RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
            final double transX = domainAxis.valueToJava2D ( x, dataArea, xAxisLocation ) - adjx;
            final double transY = rangeAxis.valueToJava2D ( y, dataArea, yAxisLocation ) - adjy;
            g2.setPaint ( this.getItemPaint ( series, item ) );
            final PlotOrientation orientation = plot.getOrientation();
            if ( orientation == PlotOrientation.HORIZONTAL ) {
                g2.fillRect ( ( int ) transY, ( int ) transX, this.dotHeight, this.dotWidth );
            } else if ( orientation == PlotOrientation.VERTICAL ) {
                g2.fillRect ( ( int ) transX, ( int ) transY, this.dotWidth, this.dotHeight );
            }
            final int domainAxisIndex = plot.getDomainAxisIndex ( domainAxis );
            final int rangeAxisIndex = plot.getRangeAxisIndex ( rangeAxis );
            this.updateCrosshairValues ( crosshairState, x, y, domainAxisIndex, rangeAxisIndex, transX, transY, orientation );
        }
    }
    @Override
    public LegendItem getLegendItem ( final int datasetIndex, final int series ) {
        final XYPlot plot = this.getPlot();
        if ( plot == null ) {
            return null;
        }
        final XYDataset dataset = plot.getDataset ( datasetIndex );
        if ( dataset == null ) {
            return null;
        }
        LegendItem result = null;
        if ( this.getItemVisible ( series, 0 ) ) {
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
            final Paint fillPaint = this.lookupSeriesPaint ( series );
            result = new LegendItem ( label, description, toolTipText, urlText, this.getLegendShape(), fillPaint );
            result.setLabelFont ( this.lookupLegendTextFont ( series ) );
            final Paint labelPaint = this.lookupLegendTextPaint ( series );
            if ( labelPaint != null ) {
                result.setLabelPaint ( labelPaint );
            }
            result.setSeriesKey ( dataset.getSeriesKey ( series ) );
            result.setSeriesIndex ( series );
            result.setDataset ( dataset );
            result.setDatasetIndex ( datasetIndex );
        }
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof XYDotRenderer ) ) {
            return false;
        }
        final XYDotRenderer that = ( XYDotRenderer ) obj;
        return this.dotWidth == that.dotWidth && this.dotHeight == that.dotHeight && ShapeUtilities.equal ( this.legendShape, that.legendShape ) && super.equals ( obj );
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.legendShape = SerialUtilities.readShape ( stream );
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeShape ( this.legendShape, stream );
    }
}
