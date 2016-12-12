package org.jfree.chart.renderer.category;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.PaintUtilities;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import java.awt.Stroke;
import org.jfree.chart.entity.EntityCollection;
import java.awt.Shape;
import java.awt.geom.RectangularShape;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.gantt.GanttCategoryDataset;
import org.jfree.data.category.CategoryDataset;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.CategoryPlot;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.chart.util.ParamChecks;
import java.awt.Color;
import java.awt.Paint;
import java.io.Serializable;
public class GanttRenderer extends IntervalBarRenderer implements Serializable {
    private static final long serialVersionUID = -4010349116350119512L;
    private transient Paint completePaint;
    private transient Paint incompletePaint;
    private double startPercent;
    private double endPercent;
    public GanttRenderer() {
        this.setIncludeBaseInRange ( false );
        this.completePaint = Color.green;
        this.incompletePaint = Color.red;
        this.startPercent = 0.35;
        this.endPercent = 0.65;
    }
    public Paint getCompletePaint() {
        return this.completePaint;
    }
    public void setCompletePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.completePaint = paint;
        this.fireChangeEvent();
    }
    public Paint getIncompletePaint() {
        return this.incompletePaint;
    }
    public void setIncompletePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.incompletePaint = paint;
        this.fireChangeEvent();
    }
    public double getStartPercent() {
        return this.startPercent;
    }
    public void setStartPercent ( final double percent ) {
        this.startPercent = percent;
        this.fireChangeEvent();
    }
    public double getEndPercent() {
        return this.endPercent;
    }
    public void setEndPercent ( final double percent ) {
        this.endPercent = percent;
        this.fireChangeEvent();
    }
    @Override
    public void drawItem ( final Graphics2D g2, final CategoryItemRendererState state, final Rectangle2D dataArea, final CategoryPlot plot, final CategoryAxis domainAxis, final ValueAxis rangeAxis, final CategoryDataset dataset, final int row, final int column, final int pass ) {
        if ( dataset instanceof GanttCategoryDataset ) {
            final GanttCategoryDataset gcd = ( GanttCategoryDataset ) dataset;
            this.drawTasks ( g2, state, dataArea, plot, domainAxis, rangeAxis, gcd, row, column );
        } else {
            super.drawItem ( g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, row, column, pass );
        }
    }
    protected void drawTasks ( final Graphics2D g2, final CategoryItemRendererState state, final Rectangle2D dataArea, final CategoryPlot plot, final CategoryAxis domainAxis, final ValueAxis rangeAxis, final GanttCategoryDataset dataset, final int row, final int column ) {
        final int count = dataset.getSubIntervalCount ( row, column );
        if ( count == 0 ) {
            this.drawTask ( g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, row, column );
        }
        final PlotOrientation orientation = plot.getOrientation();
        for ( int subinterval = 0; subinterval < count; ++subinterval ) {
            final RectangleEdge rangeAxisLocation = plot.getRangeAxisEdge();
            final Number value0 = dataset.getStartValue ( row, column, subinterval );
            if ( value0 == null ) {
                return;
            }
            double translatedValue0 = rangeAxis.valueToJava2D ( value0.doubleValue(), dataArea, rangeAxisLocation );
            final Number value = dataset.getEndValue ( row, column, subinterval );
            if ( value == null ) {
                return;
            }
            double translatedValue = rangeAxis.valueToJava2D ( value.doubleValue(), dataArea, rangeAxisLocation );
            if ( translatedValue < translatedValue0 ) {
                final double temp = translatedValue;
                translatedValue = translatedValue0;
                translatedValue0 = temp;
            }
            final double rectStart = this.calculateBarW0 ( plot, plot.getOrientation(), dataArea, domainAxis, state, row, column );
            final double rectLength = Math.abs ( translatedValue - translatedValue0 );
            final double rectBreadth = state.getBarWidth();
            Rectangle2D bar = null;
            RectangleEdge barBase = null;
            if ( plot.getOrientation() == PlotOrientation.HORIZONTAL ) {
                bar = new Rectangle2D.Double ( translatedValue0, rectStart, rectLength, rectBreadth );
                barBase = RectangleEdge.LEFT;
            } else if ( plot.getOrientation() == PlotOrientation.VERTICAL ) {
                bar = new Rectangle2D.Double ( rectStart, translatedValue0, rectBreadth, rectLength );
                barBase = RectangleEdge.BOTTOM;
            }
            Rectangle2D completeBar = null;
            Rectangle2D incompleteBar = null;
            final Number percent = dataset.getPercentComplete ( row, column, subinterval );
            final double start = this.getStartPercent();
            final double end = this.getEndPercent();
            if ( percent != null ) {
                final double p = percent.doubleValue();
                if ( orientation == PlotOrientation.HORIZONTAL ) {
                    completeBar = new Rectangle2D.Double ( translatedValue0, rectStart + start * rectBreadth, rectLength * p, rectBreadth * ( end - start ) );
                    incompleteBar = new Rectangle2D.Double ( translatedValue0 + rectLength * p, rectStart + start * rectBreadth, rectLength * ( 1.0 - p ), rectBreadth * ( end - start ) );
                } else if ( orientation == PlotOrientation.VERTICAL ) {
                    completeBar = new Rectangle2D.Double ( rectStart + start * rectBreadth, translatedValue0 + rectLength * ( 1.0 - p ), rectBreadth * ( end - start ), rectLength * p );
                    incompleteBar = new Rectangle2D.Double ( rectStart + start * rectBreadth, translatedValue0, rectBreadth * ( end - start ), rectLength * ( 1.0 - p ) );
                }
            }
            if ( this.getShadowsVisible() ) {
                this.getBarPainter().paintBarShadow ( g2, this, row, column, bar, barBase, true );
            }
            this.getBarPainter().paintBar ( g2, this, row, column, bar, barBase );
            if ( completeBar != null ) {
                g2.setPaint ( this.getCompletePaint() );
                g2.fill ( completeBar );
            }
            if ( incompleteBar != null ) {
                g2.setPaint ( this.getIncompletePaint() );
                g2.fill ( incompleteBar );
            }
            if ( this.isDrawBarOutline() && state.getBarWidth() > 3.0 ) {
                g2.setStroke ( this.getItemStroke ( row, column ) );
                g2.setPaint ( this.getItemOutlinePaint ( row, column ) );
                g2.draw ( bar );
            }
            if ( subinterval == count - 1 ) {
                final int datasetIndex = plot.indexOf ( dataset );
                final Comparable columnKey = dataset.getColumnKey ( column );
                final Comparable rowKey = dataset.getRowKey ( row );
                final double xx = domainAxis.getCategorySeriesMiddle ( columnKey, rowKey, dataset, this.getItemMargin(), dataArea, plot.getDomainAxisEdge() );
                this.updateCrosshairValues ( state.getCrosshairState(), dataset.getRowKey ( row ), dataset.getColumnKey ( column ), value.doubleValue(), datasetIndex, xx, translatedValue, orientation );
            }
            if ( state.getInfo() != null ) {
                final EntityCollection entities = state.getEntityCollection();
                if ( entities != null ) {
                    this.addItemEntity ( entities, dataset, row, column, bar );
                }
            }
        }
    }
    protected void drawTask ( final Graphics2D g2, final CategoryItemRendererState state, final Rectangle2D dataArea, final CategoryPlot plot, final CategoryAxis domainAxis, final ValueAxis rangeAxis, final GanttCategoryDataset dataset, final int row, final int column ) {
        final PlotOrientation orientation = plot.getOrientation();
        final RectangleEdge rangeAxisLocation = plot.getRangeAxisEdge();
        final Number value0 = dataset.getEndValue ( row, column );
        if ( value0 == null ) {
            return;
        }
        double java2dValue0 = rangeAxis.valueToJava2D ( value0.doubleValue(), dataArea, rangeAxisLocation );
        Number value = dataset.getStartValue ( row, column );
        if ( value == null ) {
            return;
        }
        double java2dValue = rangeAxis.valueToJava2D ( value.doubleValue(), dataArea, rangeAxisLocation );
        if ( java2dValue < java2dValue0 ) {
            final double temp = java2dValue;
            java2dValue = java2dValue0;
            java2dValue0 = temp;
            value = value0;
        }
        final double rectStart = this.calculateBarW0 ( plot, orientation, dataArea, domainAxis, state, row, column );
        final double rectBreadth = state.getBarWidth();
        final double rectLength = Math.abs ( java2dValue - java2dValue0 );
        Rectangle2D bar = null;
        RectangleEdge barBase = null;
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            bar = new Rectangle2D.Double ( java2dValue0, rectStart, rectLength, rectBreadth );
            barBase = RectangleEdge.LEFT;
        } else if ( orientation == PlotOrientation.VERTICAL ) {
            bar = new Rectangle2D.Double ( rectStart, java2dValue, rectBreadth, rectLength );
            barBase = RectangleEdge.BOTTOM;
        }
        Rectangle2D completeBar = null;
        Rectangle2D incompleteBar = null;
        final Number percent = dataset.getPercentComplete ( row, column );
        final double start = this.getStartPercent();
        final double end = this.getEndPercent();
        if ( percent != null ) {
            final double p = percent.doubleValue();
            if ( plot.getOrientation() == PlotOrientation.HORIZONTAL ) {
                completeBar = new Rectangle2D.Double ( java2dValue0, rectStart + start * rectBreadth, rectLength * p, rectBreadth * ( end - start ) );
                incompleteBar = new Rectangle2D.Double ( java2dValue0 + rectLength * p, rectStart + start * rectBreadth, rectLength * ( 1.0 - p ), rectBreadth * ( end - start ) );
            } else if ( plot.getOrientation() == PlotOrientation.VERTICAL ) {
                completeBar = new Rectangle2D.Double ( rectStart + start * rectBreadth, java2dValue + rectLength * ( 1.0 - p ), rectBreadth * ( end - start ), rectLength * p );
                incompleteBar = new Rectangle2D.Double ( rectStart + start * rectBreadth, java2dValue, rectBreadth * ( end - start ), rectLength * ( 1.0 - p ) );
            }
        }
        if ( this.getShadowsVisible() ) {
            this.getBarPainter().paintBarShadow ( g2, this, row, column, bar, barBase, true );
        }
        this.getBarPainter().paintBar ( g2, this, row, column, bar, barBase );
        if ( completeBar != null ) {
            g2.setPaint ( this.getCompletePaint() );
            g2.fill ( completeBar );
        }
        if ( incompleteBar != null ) {
            g2.setPaint ( this.getIncompletePaint() );
            g2.fill ( incompleteBar );
        }
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
            this.drawItemLabel ( g2, dataset, row, column, plot, generator, bar, false );
        }
        final int datasetIndex = plot.indexOf ( dataset );
        final Comparable columnKey = dataset.getColumnKey ( column );
        final Comparable rowKey = dataset.getRowKey ( row );
        final double xx = domainAxis.getCategorySeriesMiddle ( columnKey, rowKey, dataset, this.getItemMargin(), dataArea, plot.getDomainAxisEdge() );
        this.updateCrosshairValues ( state.getCrosshairState(), dataset.getRowKey ( row ), dataset.getColumnKey ( column ), value.doubleValue(), datasetIndex, xx, java2dValue, orientation );
        final EntityCollection entities = state.getEntityCollection();
        if ( entities != null ) {
            this.addItemEntity ( entities, dataset, row, column, bar );
        }
    }
    @Override
    public double getItemMiddle ( final Comparable rowKey, final Comparable columnKey, final CategoryDataset dataset, final CategoryAxis axis, final Rectangle2D area, final RectangleEdge edge ) {
        return axis.getCategorySeriesMiddle ( columnKey, rowKey, dataset, this.getItemMargin(), area, edge );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof GanttRenderer ) ) {
            return false;
        }
        final GanttRenderer that = ( GanttRenderer ) obj;
        return PaintUtilities.equal ( this.completePaint, that.completePaint ) && PaintUtilities.equal ( this.incompletePaint, that.incompletePaint ) && this.startPercent == that.startPercent && this.endPercent == that.endPercent && super.equals ( obj );
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.completePaint, stream );
        SerialUtilities.writePaint ( this.incompletePaint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.completePaint = SerialUtilities.readPaint ( stream );
        this.incompletePaint = SerialUtilities.readPaint ( stream );
    }
}
