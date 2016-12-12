package org.jfree.chart.renderer.category;
import java.awt.geom.Line2D;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.PlotOrientation;
import java.awt.Graphics2D;
import org.jfree.chart.plot.PlotRenderingInfo;
import java.awt.Paint;
import org.jfree.data.category.CategoryDataset;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.general.Dataset;
import org.jfree.chart.LegendItem;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class CategoryStepRenderer extends AbstractCategoryItemRenderer implements Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = -5121079703118261470L;
    public static final int STAGGER_WIDTH = 5;
    private boolean stagger;
    public CategoryStepRenderer() {
        this ( false );
    }
    public CategoryStepRenderer ( final boolean stagger ) {
        this.stagger = false;
        this.stagger = stagger;
        this.setBaseLegendShape ( new Rectangle2D.Double ( -4.0, -3.0, 8.0, 6.0 ) );
    }
    public boolean getStagger() {
        return this.stagger;
    }
    public void setStagger ( final boolean shouldStagger ) {
        this.stagger = shouldStagger;
        this.fireChangeEvent();
    }
    @Override
    public LegendItem getLegendItem ( final int datasetIndex, final int series ) {
        final CategoryPlot p = this.getPlot();
        if ( p == null ) {
            return null;
        }
        if ( !this.isSeriesVisible ( series ) || !this.isSeriesVisibleInLegend ( series ) ) {
            return null;
        }
        final CategoryDataset dataset = p.getDataset ( datasetIndex );
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
        final LegendItem item = new LegendItem ( label, description, toolTipText, urlText, shape, paint );
        item.setLabelFont ( this.lookupLegendTextFont ( series ) );
        final Paint labelPaint = this.lookupLegendTextPaint ( series );
        if ( labelPaint != null ) {
            item.setLabelPaint ( labelPaint );
        }
        item.setSeriesKey ( dataset.getRowKey ( series ) );
        item.setSeriesIndex ( series );
        item.setDataset ( dataset );
        item.setDatasetIndex ( datasetIndex );
        return item;
    }
    @Override
    protected CategoryItemRendererState createState ( final PlotRenderingInfo info ) {
        return new State ( info );
    }
    protected void drawLine ( final Graphics2D g2, final State state, final PlotOrientation orientation, final double x0, final double y0, final double x1, final double y1 ) {
        if ( orientation == PlotOrientation.VERTICAL ) {
            state.line.setLine ( x0, y0, x1, y1 );
            g2.draw ( state.line );
        } else if ( orientation == PlotOrientation.HORIZONTAL ) {
            state.line.setLine ( y0, x0, y1, x1 );
            g2.draw ( state.line );
        }
    }
    public void drawItem ( final Graphics2D g2, final CategoryItemRendererState state, final Rectangle2D dataArea, final CategoryPlot plot, final CategoryAxis domainAxis, final ValueAxis rangeAxis, final CategoryDataset dataset, final int row, final int column, final int pass ) {
        if ( !this.getItemVisible ( row, column ) ) {
            return;
        }
        final Number value = dataset.getValue ( row, column );
        if ( value == null ) {
            return;
        }
        final PlotOrientation orientation = plot.getOrientation();
        double x1s = domainAxis.getCategoryStart ( column, this.getColumnCount(), dataArea, plot.getDomainAxisEdge() );
        final double x1 = domainAxis.getCategoryMiddle ( column, this.getColumnCount(), dataArea, plot.getDomainAxisEdge() );
        final double x1e = 2.0 * x1 - x1s;
        final double y1 = rangeAxis.valueToJava2D ( value.doubleValue(), dataArea, plot.getRangeAxisEdge() );
        g2.setPaint ( this.getItemPaint ( row, column ) );
        g2.setStroke ( this.getItemStroke ( row, column ) );
        if ( column != 0 ) {
            final Number previousValue = dataset.getValue ( row, column - 1 );
            if ( previousValue != null ) {
                final double previous = previousValue.doubleValue();
                final double x0s = domainAxis.getCategoryStart ( column - 1, this.getColumnCount(), dataArea, plot.getDomainAxisEdge() );
                final double x2 = domainAxis.getCategoryMiddle ( column - 1, this.getColumnCount(), dataArea, plot.getDomainAxisEdge() );
                final double x0e = 2.0 * x2 - x0s;
                final double y2 = rangeAxis.valueToJava2D ( previous, dataArea, plot.getRangeAxisEdge() );
                if ( this.getStagger() ) {
                    int xStagger = row * 5;
                    if ( xStagger > x1s - x0e ) {
                        xStagger = ( int ) ( x1s - x0e );
                    }
                    x1s = x0e + xStagger;
                }
                this.drawLine ( g2, ( State ) state, orientation, x0e, y2, x1s, y2 );
                this.drawLine ( g2, ( State ) state, orientation, x1s, y2, x1s, y1 );
            }
        }
        this.drawLine ( g2, ( State ) state, orientation, x1s, y1, x1e, y1 );
        if ( this.isItemLabelVisible ( row, column ) ) {
            this.drawItemLabel ( g2, orientation, dataset, row, column, x1, y1, value.doubleValue() < 0.0 );
        }
        final EntityCollection entities = state.getEntityCollection();
        if ( entities != null ) {
            final Rectangle2D hotspot = new Rectangle2D.Double();
            if ( orientation == PlotOrientation.VERTICAL ) {
                hotspot.setRect ( x1s, y1, x1e - x1s, 4.0 );
            } else {
                hotspot.setRect ( y1 - 2.0, x1s, 4.0, x1e - x1s );
            }
            this.addItemEntity ( entities, dataset, row, column, hotspot );
        }
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof CategoryStepRenderer ) ) {
            return false;
        }
        final CategoryStepRenderer that = ( CategoryStepRenderer ) obj;
        return this.stagger == that.stagger && super.equals ( obj );
    }
    protected static class State extends CategoryItemRendererState {
        public Line2D line;
        public State ( final PlotRenderingInfo info ) {
            super ( info );
            this.line = new Line2D.Double();
        }
    }
}
