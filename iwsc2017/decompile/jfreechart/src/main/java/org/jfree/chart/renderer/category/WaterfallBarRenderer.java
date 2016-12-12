package org.jfree.chart.renderer.category;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.PaintUtilities;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import java.awt.Stroke;
import java.awt.Shape;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.CategoryPlot;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.GradientPaintTransformer;
import org.jfree.ui.StandardGradientPaintTransformer;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.chart.util.ParamChecks;
import java.awt.GradientPaint;
import java.awt.Color;
import java.awt.Paint;
public class WaterfallBarRenderer extends BarRenderer {
    private static final long serialVersionUID = -2482910643727230911L;
    private transient Paint firstBarPaint;
    private transient Paint lastBarPaint;
    private transient Paint positiveBarPaint;
    private transient Paint negativeBarPaint;
    public WaterfallBarRenderer() {
        this ( new GradientPaint ( 0.0f, 0.0f, new Color ( 34, 34, 255 ), 0.0f, 0.0f, new Color ( 102, 102, 255 ) ), new GradientPaint ( 0.0f, 0.0f, new Color ( 34, 255, 34 ), 0.0f, 0.0f, new Color ( 102, 255, 102 ) ), new GradientPaint ( 0.0f, 0.0f, new Color ( 255, 34, 34 ), 0.0f, 0.0f, new Color ( 255, 102, 102 ) ), new GradientPaint ( 0.0f, 0.0f, new Color ( 255, 255, 34 ), 0.0f, 0.0f, new Color ( 255, 255, 102 ) ) );
    }
    public WaterfallBarRenderer ( final Paint firstBarPaint, final Paint positiveBarPaint, final Paint negativeBarPaint, final Paint lastBarPaint ) {
        ParamChecks.nullNotPermitted ( firstBarPaint, "firstBarPaint" );
        ParamChecks.nullNotPermitted ( positiveBarPaint, "positiveBarPaint" );
        ParamChecks.nullNotPermitted ( negativeBarPaint, "negativeBarPaint" );
        ParamChecks.nullNotPermitted ( lastBarPaint, "lastBarPaint" );
        this.firstBarPaint = firstBarPaint;
        this.lastBarPaint = lastBarPaint;
        this.positiveBarPaint = positiveBarPaint;
        this.negativeBarPaint = negativeBarPaint;
        this.setGradientPaintTransformer ( ( GradientPaintTransformer ) new StandardGradientPaintTransformer ( GradientPaintTransformType.CENTER_VERTICAL ) );
        this.setMinimumBarLength ( 1.0 );
    }
    public Paint getFirstBarPaint() {
        return this.firstBarPaint;
    }
    public void setFirstBarPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.firstBarPaint = paint;
        this.fireChangeEvent();
    }
    public Paint getLastBarPaint() {
        return this.lastBarPaint;
    }
    public void setLastBarPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.lastBarPaint = paint;
        this.fireChangeEvent();
    }
    public Paint getPositiveBarPaint() {
        return this.positiveBarPaint;
    }
    public void setPositiveBarPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.positiveBarPaint = paint;
        this.fireChangeEvent();
    }
    public Paint getNegativeBarPaint() {
        return this.negativeBarPaint;
    }
    public void setNegativeBarPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.negativeBarPaint = paint;
        this.fireChangeEvent();
    }
    @Override
    public Range findRangeBounds ( final CategoryDataset dataset ) {
        if ( dataset == null ) {
            return null;
        }
        boolean allItemsNull = true;
        double minimum = 0.0;
        double maximum = 0.0;
        final int columnCount = dataset.getColumnCount();
        for ( int row = 0; row < dataset.getRowCount(); ++row ) {
            double runningTotal = 0.0;
            for ( int column = 0; column <= columnCount - 1; ++column ) {
                final Number n = dataset.getValue ( row, column );
                if ( n != null ) {
                    allItemsNull = false;
                    final double value = n.doubleValue();
                    if ( column == columnCount - 1 ) {
                        runningTotal = value;
                    } else {
                        runningTotal += value;
                    }
                    minimum = Math.min ( minimum, runningTotal );
                    maximum = Math.max ( maximum, runningTotal );
                }
            }
        }
        if ( !allItemsNull ) {
            return new Range ( minimum, maximum );
        }
        return null;
    }
    @Override
    public void drawItem ( final Graphics2D g2, final CategoryItemRendererState state, final Rectangle2D dataArea, final CategoryPlot plot, final CategoryAxis domainAxis, final ValueAxis rangeAxis, final CategoryDataset dataset, final int row, final int column, final int pass ) {
        double previous = state.getSeriesRunningTotal();
        if ( column == dataset.getColumnCount() - 1 ) {
            previous = 0.0;
        }
        double current = 0.0;
        final Number n = dataset.getValue ( row, column );
        if ( n != null ) {
            current = previous + n.doubleValue();
        }
        state.setSeriesRunningTotal ( current );
        final int categoryCount = this.getColumnCount();
        final PlotOrientation orientation = plot.getOrientation();
        double rectX = 0.0;
        double rectY = 0.0;
        final RectangleEdge rangeAxisLocation = plot.getRangeAxisEdge();
        double j2dy0 = rangeAxis.valueToJava2D ( previous, dataArea, rangeAxisLocation );
        double j2dy = rangeAxis.valueToJava2D ( current, dataArea, rangeAxisLocation );
        final double valDiff = current - previous;
        if ( j2dy < j2dy0 ) {
            final double temp = j2dy;
            j2dy = j2dy0;
            j2dy0 = temp;
        }
        double rectWidth = state.getBarWidth();
        double rectHeight = Math.max ( this.getMinimumBarLength(), Math.abs ( j2dy - j2dy0 ) );
        final Comparable seriesKey = dataset.getRowKey ( row );
        final Comparable categoryKey = dataset.getColumnKey ( column );
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            rectY = domainAxis.getCategorySeriesMiddle ( categoryKey, seriesKey, dataset, this.getItemMargin(), dataArea, RectangleEdge.LEFT );
            rectX = j2dy0;
            rectHeight = state.getBarWidth();
            rectY -= rectHeight / 2.0;
            rectWidth = Math.max ( this.getMinimumBarLength(), Math.abs ( j2dy - j2dy0 ) );
        } else if ( orientation == PlotOrientation.VERTICAL ) {
            rectX = domainAxis.getCategorySeriesMiddle ( categoryKey, seriesKey, dataset, this.getItemMargin(), dataArea, RectangleEdge.TOP );
            rectX -= rectWidth / 2.0;
            rectY = j2dy0;
        }
        final Rectangle2D bar = new Rectangle2D.Double ( rectX, rectY, rectWidth, rectHeight );
        Paint seriesPaint;
        if ( column == 0 ) {
            seriesPaint = this.getFirstBarPaint();
        } else if ( column == categoryCount - 1 ) {
            seriesPaint = this.getLastBarPaint();
        } else if ( valDiff >= 0.0 ) {
            seriesPaint = this.getPositiveBarPaint();
        } else {
            seriesPaint = this.getNegativeBarPaint();
        }
        if ( this.getGradientPaintTransformer() != null && seriesPaint instanceof GradientPaint ) {
            final GradientPaint gp = ( GradientPaint ) seriesPaint;
            seriesPaint = this.getGradientPaintTransformer().transform ( gp, ( Shape ) bar );
        }
        g2.setPaint ( seriesPaint );
        g2.fill ( bar );
        if ( this.isDrawBarOutline() && state.getBarWidth() > 3.0 ) {
            final Stroke stroke = this.getItemOutlineStroke ( row, column );
            final Paint paint = this.getItemOutlinePaint ( row, column );
            if ( stroke != null && paint != null ) {
                g2.setStroke ( stroke );
                g2.setPaint ( paint );
                g2.draw ( bar );
            }
        }
        final CategoryItemLabelGenerator generator = this.getItemLabelGenerator ( row, column );
        if ( generator != null && this.isItemLabelVisible ( row, column ) ) {
            this.drawItemLabel ( g2, dataset, row, column, plot, generator, bar, valDiff < 0.0 );
        }
        final EntityCollection entities = state.getEntityCollection();
        if ( entities != null ) {
            this.addItemEntity ( entities, dataset, row, column, bar );
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
        if ( ! ( obj instanceof WaterfallBarRenderer ) ) {
            return false;
        }
        final WaterfallBarRenderer that = ( WaterfallBarRenderer ) obj;
        return PaintUtilities.equal ( this.firstBarPaint, that.firstBarPaint ) && PaintUtilities.equal ( this.lastBarPaint, that.lastBarPaint ) && PaintUtilities.equal ( this.positiveBarPaint, that.positiveBarPaint ) && PaintUtilities.equal ( this.negativeBarPaint, that.negativeBarPaint );
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.firstBarPaint, stream );
        SerialUtilities.writePaint ( this.lastBarPaint, stream );
        SerialUtilities.writePaint ( this.positiveBarPaint, stream );
        SerialUtilities.writePaint ( this.negativeBarPaint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.firstBarPaint = SerialUtilities.readPaint ( stream );
        this.lastBarPaint = SerialUtilities.readPaint ( stream );
        this.positiveBarPaint = SerialUtilities.readPaint ( stream );
        this.negativeBarPaint = SerialUtilities.readPaint ( stream );
    }
}
