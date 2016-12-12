package org.jfree.chart.renderer.category;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import java.awt.Shape;
import java.awt.geom.RectangularShape;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.ui.RectangleEdge;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.CategoryPlot;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
public class IntervalBarRenderer extends BarRenderer {
    private static final long serialVersionUID = -5068857361615528725L;
    @Override
    public Range findRangeBounds ( final CategoryDataset dataset ) {
        return this.findRangeBounds ( dataset, true );
    }
    @Override
    public void drawItem ( final Graphics2D g2, final CategoryItemRendererState state, final Rectangle2D dataArea, final CategoryPlot plot, final CategoryAxis domainAxis, final ValueAxis rangeAxis, final CategoryDataset dataset, final int row, final int column, final int pass ) {
        if ( dataset instanceof IntervalCategoryDataset ) {
            final IntervalCategoryDataset d = ( IntervalCategoryDataset ) dataset;
            this.drawInterval ( g2, state, dataArea, plot, domainAxis, rangeAxis, d, row, column );
        } else {
            super.drawItem ( g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, row, column, pass );
        }
    }
    protected void drawInterval ( final Graphics2D g2, final CategoryItemRendererState state, final Rectangle2D dataArea, final CategoryPlot plot, final CategoryAxis domainAxis, final ValueAxis rangeAxis, final IntervalCategoryDataset dataset, final int row, final int column ) {
        final int visibleRow = state.getVisibleSeriesIndex ( row );
        if ( visibleRow < 0 ) {
            return;
        }
        final PlotOrientation orientation = plot.getOrientation();
        double rectX = 0.0;
        double rectY = 0.0;
        final RectangleEdge rangeAxisLocation = plot.getRangeAxisEdge();
        final Number value0 = dataset.getEndValue ( row, column );
        if ( value0 == null ) {
            return;
        }
        double java2dValue0 = rangeAxis.valueToJava2D ( value0.doubleValue(), dataArea, rangeAxisLocation );
        final Number value = dataset.getStartValue ( row, column );
        if ( value == null ) {
            return;
        }
        double java2dValue = rangeAxis.valueToJava2D ( value.doubleValue(), dataArea, rangeAxisLocation );
        if ( java2dValue < java2dValue0 ) {
            final double temp = java2dValue;
            java2dValue = java2dValue0;
            java2dValue0 = temp;
        }
        double rectWidth = state.getBarWidth();
        double rectHeight = Math.abs ( java2dValue - java2dValue0 );
        RectangleEdge barBase = RectangleEdge.LEFT;
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            rectX = java2dValue0;
            rectY = this.calculateBarW0 ( this.getPlot(), orientation, dataArea, domainAxis, state, visibleRow, column );
            rectHeight = state.getBarWidth();
            rectWidth = Math.abs ( java2dValue - java2dValue0 );
            barBase = RectangleEdge.LEFT;
        } else if ( orientation.isVertical() ) {
            rectX = this.calculateBarW0 ( this.getPlot(), orientation, dataArea, domainAxis, state, visibleRow, column );
            rectY = java2dValue0;
            barBase = RectangleEdge.BOTTOM;
        }
        final Rectangle2D bar = new Rectangle2D.Double ( rectX, rectY, rectWidth, rectHeight );
        final BarPainter painter = this.getBarPainter();
        if ( state.getElementHinting() ) {
            this.beginElementGroup ( g2, dataset.getRowKey ( row ), dataset.getColumnKey ( column ) );
        }
        if ( this.getShadowsVisible() ) {
            painter.paintBarShadow ( g2, this, row, column, bar, barBase, false );
        }
        this.getBarPainter().paintBar ( g2, this, row, column, bar, barBase );
        if ( state.getElementHinting() ) {
            this.endElementGroup ( g2 );
        }
        final CategoryItemLabelGenerator generator = this.getItemLabelGenerator ( row, column );
        if ( generator != null && this.isItemLabelVisible ( row, column ) ) {
            this.drawItemLabel ( g2, dataset, row, column, plot, generator, bar, false );
        }
        final EntityCollection entities = state.getEntityCollection();
        if ( entities != null ) {
            this.addItemEntity ( entities, dataset, row, column, bar );
        }
    }
    @Override
    public boolean equals ( final Object obj ) {
        return obj == this || ( obj instanceof IntervalBarRenderer && super.equals ( obj ) );
    }
}
