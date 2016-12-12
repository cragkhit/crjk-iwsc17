package org.jfree.chart.renderer.category;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.jfree.util.ObjectUtilities;
import java.awt.Stroke;
import java.awt.Paint;
import org.jfree.data.general.Dataset;
import java.awt.geom.Line2D;
import org.jfree.chart.LegendItem;
import java.awt.Shape;
import java.util.List;
import org.jfree.util.ShapeUtilities;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.MultiValueCategoryDataset;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.CategoryPlot;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.util.BooleanUtilities;
import org.jfree.util.BooleanList;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class ScatterRenderer extends AbstractCategoryItemRenderer implements Cloneable, PublicCloneable, Serializable {
    private BooleanList seriesShapesFilled;
    private boolean baseShapesFilled;
    private boolean useFillPaint;
    private boolean drawOutlines;
    private boolean useOutlinePaint;
    private boolean useSeriesOffset;
    private double itemMargin;
    public ScatterRenderer() {
        this.seriesShapesFilled = new BooleanList();
        this.baseShapesFilled = true;
        this.useFillPaint = false;
        this.drawOutlines = false;
        this.useOutlinePaint = false;
        this.useSeriesOffset = true;
        this.itemMargin = 0.2;
    }
    public boolean getUseSeriesOffset() {
        return this.useSeriesOffset;
    }
    public void setUseSeriesOffset ( final boolean offset ) {
        this.useSeriesOffset = offset;
        this.fireChangeEvent();
    }
    public double getItemMargin() {
        return this.itemMargin;
    }
    public void setItemMargin ( final double margin ) {
        if ( margin < 0.0 || margin >= 1.0 ) {
            throw new IllegalArgumentException ( "Requires 0.0 <= margin < 1.0." );
        }
        this.itemMargin = margin;
        this.fireChangeEvent();
    }
    public boolean getDrawOutlines() {
        return this.drawOutlines;
    }
    public void setDrawOutlines ( final boolean flag ) {
        this.drawOutlines = flag;
        this.fireChangeEvent();
    }
    public boolean getUseOutlinePaint() {
        return this.useOutlinePaint;
    }
    public void setUseOutlinePaint ( final boolean use ) {
        this.useOutlinePaint = use;
        this.fireChangeEvent();
    }
    public boolean getItemShapeFilled ( final int series, final int item ) {
        return this.getSeriesShapesFilled ( series );
    }
    public boolean getSeriesShapesFilled ( final int series ) {
        final Boolean flag = this.seriesShapesFilled.getBoolean ( series );
        if ( flag != null ) {
            return flag;
        }
        return this.baseShapesFilled;
    }
    public void setSeriesShapesFilled ( final int series, final Boolean filled ) {
        this.seriesShapesFilled.setBoolean ( series, filled );
        this.fireChangeEvent();
    }
    public void setSeriesShapesFilled ( final int series, final boolean filled ) {
        this.seriesShapesFilled.setBoolean ( series, BooleanUtilities.valueOf ( filled ) );
        this.fireChangeEvent();
    }
    public boolean getBaseShapesFilled() {
        return this.baseShapesFilled;
    }
    public void setBaseShapesFilled ( final boolean flag ) {
        this.baseShapesFilled = flag;
        this.fireChangeEvent();
    }
    public boolean getUseFillPaint() {
        return this.useFillPaint;
    }
    public void setUseFillPaint ( final boolean flag ) {
        this.useFillPaint = flag;
        this.fireChangeEvent();
    }
    @Override
    public Range findRangeBounds ( final CategoryDataset dataset ) {
        return this.findRangeBounds ( dataset, true );
    }
    public void drawItem ( final Graphics2D g2, final CategoryItemRendererState state, final Rectangle2D dataArea, final CategoryPlot plot, final CategoryAxis domainAxis, final ValueAxis rangeAxis, final CategoryDataset dataset, final int row, final int column, final int pass ) {
        if ( !this.getItemVisible ( row, column ) ) {
            return;
        }
        final int visibleRow = state.getVisibleSeriesIndex ( row );
        if ( visibleRow < 0 ) {
            return;
        }
        final int visibleRowCount = state.getVisibleSeriesCount();
        final PlotOrientation orientation = plot.getOrientation();
        final MultiValueCategoryDataset d = ( MultiValueCategoryDataset ) dataset;
        final List values = d.getValues ( row, column );
        if ( values == null ) {
            return;
        }
        for ( int valueCount = values.size(), i = 0; i < valueCount; ++i ) {
            double x1;
            if ( this.useSeriesOffset ) {
                x1 = domainAxis.getCategorySeriesMiddle ( column, dataset.getColumnCount(), visibleRow, visibleRowCount, this.itemMargin, dataArea, plot.getDomainAxisEdge() );
            } else {
                x1 = domainAxis.getCategoryMiddle ( column, this.getColumnCount(), dataArea, plot.getDomainAxisEdge() );
            }
            final Number n = values.get ( i );
            final double value = n.doubleValue();
            final double y1 = rangeAxis.valueToJava2D ( value, dataArea, plot.getRangeAxisEdge() );
            Shape shape = this.getItemShape ( row, column );
            if ( orientation == PlotOrientation.HORIZONTAL ) {
                shape = ShapeUtilities.createTranslatedShape ( shape, y1, x1 );
            } else if ( orientation == PlotOrientation.VERTICAL ) {
                shape = ShapeUtilities.createTranslatedShape ( shape, x1, y1 );
            }
            if ( this.getItemShapeFilled ( row, column ) ) {
                if ( this.useFillPaint ) {
                    g2.setPaint ( this.getItemFillPaint ( row, column ) );
                } else {
                    g2.setPaint ( this.getItemPaint ( row, column ) );
                }
                g2.fill ( shape );
            }
            if ( this.drawOutlines ) {
                if ( this.useOutlinePaint ) {
                    g2.setPaint ( this.getItemOutlinePaint ( row, column ) );
                } else {
                    g2.setPaint ( this.getItemPaint ( row, column ) );
                }
                g2.setStroke ( this.getItemOutlineStroke ( row, column ) );
                g2.draw ( shape );
            }
        }
    }
    @Override
    public LegendItem getLegendItem ( final int datasetIndex, final int series ) {
        final CategoryPlot cp = this.getPlot();
        if ( cp == null ) {
            return null;
        }
        if ( this.isSeriesVisible ( series ) && this.isSeriesVisibleInLegend ( series ) ) {
            final CategoryDataset dataset = cp.getDataset ( datasetIndex );
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
            final Paint fillPaint = this.useFillPaint ? this.getItemFillPaint ( series, 0 ) : paint;
            final boolean shapeOutlineVisible = this.drawOutlines;
            final Paint outlinePaint = this.useOutlinePaint ? this.getItemOutlinePaint ( series, 0 ) : paint;
            final Stroke outlineStroke = this.lookupSeriesOutlineStroke ( series );
            final LegendItem result = new LegendItem ( label, description, toolTipText, urlText, true, shape, this.getItemShapeFilled ( series, 0 ), fillPaint, shapeOutlineVisible, outlinePaint, outlineStroke, false, new Line2D.Double ( -7.0, 0.0, 7.0, 0.0 ), this.getItemStroke ( series, 0 ), this.getItemPaint ( series, 0 ) );
            result.setLabelFont ( this.lookupLegendTextFont ( series ) );
            final Paint labelPaint = this.lookupLegendTextPaint ( series );
            if ( labelPaint != null ) {
                result.setLabelPaint ( labelPaint );
            }
            result.setDataset ( dataset );
            result.setDatasetIndex ( datasetIndex );
            result.setSeriesKey ( dataset.getRowKey ( series ) );
            result.setSeriesIndex ( series );
            return result;
        }
        return null;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof ScatterRenderer ) ) {
            return false;
        }
        final ScatterRenderer that = ( ScatterRenderer ) obj;
        return ObjectUtilities.equal ( ( Object ) this.seriesShapesFilled, ( Object ) that.seriesShapesFilled ) && this.baseShapesFilled == that.baseShapesFilled && this.useFillPaint == that.useFillPaint && this.drawOutlines == that.drawOutlines && this.useOutlinePaint == that.useOutlinePaint && this.useSeriesOffset == that.useSeriesOffset && this.itemMargin == that.itemMargin && super.equals ( obj );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final ScatterRenderer clone = ( ScatterRenderer ) super.clone();
        clone.seriesShapesFilled = ( BooleanList ) this.seriesShapesFilled.clone();
        return clone;
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
    }
}
