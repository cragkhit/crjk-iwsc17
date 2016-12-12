package org.jfree.chart.renderer.category;
import org.jfree.util.ObjectUtilities;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.util.ShapeUtilities;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.CategoryAxis;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Paint;
import org.jfree.data.category.CategoryDataset;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.general.Dataset;
import java.awt.Shape;
import java.awt.geom.Line2D;
import org.jfree.chart.LegendItem;
import org.jfree.util.BooleanUtilities;
import org.jfree.util.BooleanList;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class LineAndShapeRenderer extends AbstractCategoryItemRenderer implements Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = -197749519869226398L;
    private Boolean linesVisible;
    private BooleanList seriesLinesVisible;
    private boolean baseLinesVisible;
    private Boolean shapesVisible;
    private BooleanList seriesShapesVisible;
    private boolean baseShapesVisible;
    private Boolean shapesFilled;
    private BooleanList seriesShapesFilled;
    private boolean baseShapesFilled;
    private boolean useFillPaint;
    private boolean drawOutlines;
    private boolean useOutlinePaint;
    private boolean useSeriesOffset;
    private double itemMargin;
    public LineAndShapeRenderer() {
        this ( true, true );
    }
    public LineAndShapeRenderer ( final boolean lines, final boolean shapes ) {
        this.linesVisible = null;
        this.seriesLinesVisible = new BooleanList();
        this.baseLinesVisible = lines;
        this.shapesVisible = null;
        this.seriesShapesVisible = new BooleanList();
        this.baseShapesVisible = shapes;
        this.shapesFilled = null;
        this.seriesShapesFilled = new BooleanList();
        this.baseShapesFilled = true;
        this.useFillPaint = false;
        this.drawOutlines = true;
        this.useOutlinePaint = false;
        this.useSeriesOffset = false;
        this.itemMargin = 0.0;
    }
    public boolean getItemLineVisible ( final int series, final int item ) {
        Boolean flag = this.linesVisible;
        if ( flag == null ) {
            flag = this.getSeriesLinesVisible ( series );
        }
        if ( flag != null ) {
            return flag;
        }
        return this.baseLinesVisible;
    }
    public Boolean getLinesVisible() {
        return this.linesVisible;
    }
    public void setLinesVisible ( final Boolean visible ) {
        this.linesVisible = visible;
        this.fireChangeEvent();
    }
    public void setLinesVisible ( final boolean visible ) {
        this.setLinesVisible ( BooleanUtilities.valueOf ( visible ) );
    }
    public Boolean getSeriesLinesVisible ( final int series ) {
        return this.seriesLinesVisible.getBoolean ( series );
    }
    public void setSeriesLinesVisible ( final int series, final Boolean flag ) {
        this.seriesLinesVisible.setBoolean ( series, flag );
        this.fireChangeEvent();
    }
    public void setSeriesLinesVisible ( final int series, final boolean visible ) {
        this.setSeriesLinesVisible ( series, BooleanUtilities.valueOf ( visible ) );
    }
    public boolean getBaseLinesVisible() {
        return this.baseLinesVisible;
    }
    public void setBaseLinesVisible ( final boolean flag ) {
        this.baseLinesVisible = flag;
        this.fireChangeEvent();
    }
    public boolean getItemShapeVisible ( final int series, final int item ) {
        Boolean flag = this.shapesVisible;
        if ( flag == null ) {
            flag = this.getSeriesShapesVisible ( series );
        }
        if ( flag != null ) {
            return flag;
        }
        return this.baseShapesVisible;
    }
    public Boolean getShapesVisible() {
        return this.shapesVisible;
    }
    public void setShapesVisible ( final Boolean visible ) {
        this.shapesVisible = visible;
        this.fireChangeEvent();
    }
    public void setShapesVisible ( final boolean visible ) {
        this.setShapesVisible ( BooleanUtilities.valueOf ( visible ) );
    }
    public Boolean getSeriesShapesVisible ( final int series ) {
        return this.seriesShapesVisible.getBoolean ( series );
    }
    public void setSeriesShapesVisible ( final int series, final boolean visible ) {
        this.setSeriesShapesVisible ( series, BooleanUtilities.valueOf ( visible ) );
    }
    public void setSeriesShapesVisible ( final int series, final Boolean flag ) {
        this.seriesShapesVisible.setBoolean ( series, flag );
        this.fireChangeEvent();
    }
    public boolean getBaseShapesVisible() {
        return this.baseShapesVisible;
    }
    public void setBaseShapesVisible ( final boolean flag ) {
        this.baseShapesVisible = flag;
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
        if ( this.shapesFilled != null ) {
            return this.shapesFilled;
        }
        final Boolean flag = this.seriesShapesFilled.getBoolean ( series );
        if ( flag != null ) {
            return flag;
        }
        return this.baseShapesFilled;
    }
    public Boolean getShapesFilled() {
        return this.shapesFilled;
    }
    public void setShapesFilled ( final boolean filled ) {
        if ( filled ) {
            this.setShapesFilled ( Boolean.TRUE );
        } else {
            this.setShapesFilled ( Boolean.FALSE );
        }
    }
    public void setShapesFilled ( final Boolean filled ) {
        this.shapesFilled = filled;
        this.fireChangeEvent();
    }
    public void setSeriesShapesFilled ( final int series, final Boolean filled ) {
        this.seriesShapesFilled.setBoolean ( series, filled );
        this.fireChangeEvent();
    }
    public void setSeriesShapesFilled ( final int series, final boolean filled ) {
        this.setSeriesShapesFilled ( series, BooleanUtilities.valueOf ( filled ) );
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
            final boolean lineVisible = this.getItemLineVisible ( series, 0 );
            final boolean shapeVisible = this.getItemShapeVisible ( series, 0 );
            final LegendItem result = new LegendItem ( label, description, toolTipText, urlText, shapeVisible, shape, this.getItemShapeFilled ( series, 0 ), fillPaint, shapeOutlineVisible, outlinePaint, outlineStroke, lineVisible, new Line2D.Double ( -7.0, 0.0, 7.0, 0.0 ), this.getItemStroke ( series, 0 ), this.getItemPaint ( series, 0 ) );
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
    public int getPassCount() {
        return 2;
    }
    public void drawItem ( final Graphics2D g2, final CategoryItemRendererState state, final Rectangle2D dataArea, final CategoryPlot plot, final CategoryAxis domainAxis, final ValueAxis rangeAxis, final CategoryDataset dataset, final int row, final int column, final int pass ) {
        if ( !this.getItemVisible ( row, column ) ) {
            return;
        }
        if ( !this.getItemLineVisible ( row, column ) && !this.getItemShapeVisible ( row, column ) ) {
            return;
        }
        final Number v = dataset.getValue ( row, column );
        if ( v == null ) {
            return;
        }
        final int visibleRow = state.getVisibleSeriesIndex ( row );
        if ( visibleRow < 0 ) {
            return;
        }
        final int visibleRowCount = state.getVisibleSeriesCount();
        final PlotOrientation orientation = plot.getOrientation();
        double x1;
        if ( this.useSeriesOffset ) {
            x1 = domainAxis.getCategorySeriesMiddle ( column, dataset.getColumnCount(), visibleRow, visibleRowCount, this.itemMargin, dataArea, plot.getDomainAxisEdge() );
        } else {
            x1 = domainAxis.getCategoryMiddle ( column, this.getColumnCount(), dataArea, plot.getDomainAxisEdge() );
        }
        final double value = v.doubleValue();
        final double y1 = rangeAxis.valueToJava2D ( value, dataArea, plot.getRangeAxisEdge() );
        if ( pass == 0 && this.getItemLineVisible ( row, column ) && column != 0 ) {
            final Number previousValue = dataset.getValue ( row, column - 1 );
            if ( previousValue != null ) {
                final double previous = previousValue.doubleValue();
                double x2;
                if ( this.useSeriesOffset ) {
                    x2 = domainAxis.getCategorySeriesMiddle ( column - 1, dataset.getColumnCount(), visibleRow, visibleRowCount, this.itemMargin, dataArea, plot.getDomainAxisEdge() );
                } else {
                    x2 = domainAxis.getCategoryMiddle ( column - 1, this.getColumnCount(), dataArea, plot.getDomainAxisEdge() );
                }
                final double y2 = rangeAxis.valueToJava2D ( previous, dataArea, plot.getRangeAxisEdge() );
                Line2D line = null;
                if ( orientation == PlotOrientation.HORIZONTAL ) {
                    line = new Line2D.Double ( y2, x2, y1, x1 );
                } else if ( orientation == PlotOrientation.VERTICAL ) {
                    line = new Line2D.Double ( x2, y2, x1, y1 );
                }
                g2.setPaint ( this.getItemPaint ( row, column ) );
                g2.setStroke ( this.getItemStroke ( row, column ) );
                g2.draw ( line );
            }
        }
        if ( pass == 1 ) {
            Shape shape = this.getItemShape ( row, column );
            if ( orientation == PlotOrientation.HORIZONTAL ) {
                shape = ShapeUtilities.createTranslatedShape ( shape, y1, x1 );
            } else if ( orientation == PlotOrientation.VERTICAL ) {
                shape = ShapeUtilities.createTranslatedShape ( shape, x1, y1 );
            }
            if ( this.getItemShapeVisible ( row, column ) ) {
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
            if ( this.isItemLabelVisible ( row, column ) ) {
                if ( orientation == PlotOrientation.HORIZONTAL ) {
                    this.drawItemLabel ( g2, orientation, dataset, row, column, y1, x1, value < 0.0 );
                } else if ( orientation == PlotOrientation.VERTICAL ) {
                    this.drawItemLabel ( g2, orientation, dataset, row, column, x1, y1, value < 0.0 );
                }
            }
            final int datasetIndex = plot.indexOf ( dataset );
            this.updateCrosshairValues ( state.getCrosshairState(), dataset.getRowKey ( row ), dataset.getColumnKey ( column ), value, datasetIndex, x1, y1, orientation );
            final EntityCollection entities = state.getEntityCollection();
            if ( entities != null ) {
                this.addItemEntity ( entities, dataset, row, column, shape );
            }
        }
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof LineAndShapeRenderer ) ) {
            return false;
        }
        final LineAndShapeRenderer that = ( LineAndShapeRenderer ) obj;
        return this.baseLinesVisible == that.baseLinesVisible && ObjectUtilities.equal ( ( Object ) this.seriesLinesVisible, ( Object ) that.seriesLinesVisible ) && ObjectUtilities.equal ( ( Object ) this.linesVisible, ( Object ) that.linesVisible ) && this.baseShapesVisible == that.baseShapesVisible && ObjectUtilities.equal ( ( Object ) this.seriesShapesVisible, ( Object ) that.seriesShapesVisible ) && ObjectUtilities.equal ( ( Object ) this.shapesVisible, ( Object ) that.shapesVisible ) && ObjectUtilities.equal ( ( Object ) this.shapesFilled, ( Object ) that.shapesFilled ) && ObjectUtilities.equal ( ( Object ) this.seriesShapesFilled, ( Object ) that.seriesShapesFilled ) && this.baseShapesFilled == that.baseShapesFilled && this.useOutlinePaint == that.useOutlinePaint && this.useSeriesOffset == that.useSeriesOffset && this.itemMargin == that.itemMargin && super.equals ( obj );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final LineAndShapeRenderer clone = ( LineAndShapeRenderer ) super.clone();
        clone.seriesLinesVisible = ( BooleanList ) this.seriesLinesVisible.clone();
        clone.seriesShapesVisible = ( BooleanList ) this.seriesShapesVisible.clone();
        clone.seriesShapesFilled = ( BooleanList ) this.seriesShapesFilled.clone();
        return clone;
    }
}
