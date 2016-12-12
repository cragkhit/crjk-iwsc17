package org.jfree.chart.renderer.category;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.ui.GradientPaintTransformer;
import org.jfree.ui.RectangleEdge;
import java.awt.geom.Line2D;
import java.awt.Shape;
import java.awt.GradientPaint;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.StatisticalCategoryDataset;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.CategoryPlot;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.Paint;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class StatisticalBarRenderer extends BarRenderer implements CategoryItemRenderer, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = -4986038395414039117L;
    private transient Paint errorIndicatorPaint;
    private transient Stroke errorIndicatorStroke;
    public StatisticalBarRenderer() {
        this.errorIndicatorPaint = Color.gray;
        this.errorIndicatorStroke = new BasicStroke ( 1.0f );
    }
    public Paint getErrorIndicatorPaint() {
        return this.errorIndicatorPaint;
    }
    public void setErrorIndicatorPaint ( final Paint paint ) {
        this.errorIndicatorPaint = paint;
        this.fireChangeEvent();
    }
    public Stroke getErrorIndicatorStroke() {
        return this.errorIndicatorStroke;
    }
    public void setErrorIndicatorStroke ( final Stroke stroke ) {
        this.errorIndicatorStroke = stroke;
        this.fireChangeEvent();
    }
    @Override
    public Range findRangeBounds ( final CategoryDataset dataset ) {
        return this.findRangeBounds ( dataset, true );
    }
    @Override
    public void drawItem ( final Graphics2D g2, final CategoryItemRendererState state, final Rectangle2D dataArea, final CategoryPlot plot, final CategoryAxis domainAxis, final ValueAxis rangeAxis, final CategoryDataset data, final int row, final int column, final int pass ) {
        final int visibleRow = state.getVisibleSeriesIndex ( row );
        if ( visibleRow < 0 ) {
            return;
        }
        if ( ! ( data instanceof StatisticalCategoryDataset ) ) {
            throw new IllegalArgumentException ( "Requires StatisticalCategoryDataset." );
        }
        final StatisticalCategoryDataset statData = ( StatisticalCategoryDataset ) data;
        final PlotOrientation orientation = plot.getOrientation();
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            this.drawHorizontalItem ( g2, state, dataArea, plot, domainAxis, rangeAxis, statData, visibleRow, row, column );
        } else if ( orientation == PlotOrientation.VERTICAL ) {
            this.drawVerticalItem ( g2, state, dataArea, plot, domainAxis, rangeAxis, statData, visibleRow, row, column );
        }
    }
    protected void drawHorizontalItem ( final Graphics2D g2, final CategoryItemRendererState state, final Rectangle2D dataArea, final CategoryPlot plot, final CategoryAxis domainAxis, final ValueAxis rangeAxis, final StatisticalCategoryDataset dataset, final int visibleRow, final int row, final int column ) {
        final double rectY = this.calculateBarW0 ( plot, PlotOrientation.HORIZONTAL, dataArea, domainAxis, state, visibleRow, column );
        final Number meanValue = dataset.getMeanValue ( row, column );
        if ( meanValue == null ) {
            return;
        }
        double value = meanValue.doubleValue();
        double base = 0.0;
        final double lclip = this.getLowerClip();
        final double uclip = this.getUpperClip();
        if ( uclip <= 0.0 ) {
            if ( value >= uclip ) {
                return;
            }
            base = uclip;
            if ( value <= lclip ) {
                value = lclip;
            }
        } else if ( lclip <= 0.0 ) {
            if ( value >= uclip ) {
                value = uclip;
            } else if ( value <= lclip ) {
                value = lclip;
            }
        } else {
            if ( value <= lclip ) {
                return;
            }
            base = this.getLowerClip();
            if ( value >= uclip ) {
                value = uclip;
            }
        }
        final RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
        final double transY1 = rangeAxis.valueToJava2D ( base, dataArea, yAxisLocation );
        final double transY2 = rangeAxis.valueToJava2D ( value, dataArea, yAxisLocation );
        final double rectX = Math.min ( transY2, transY1 );
        final double rectHeight = state.getBarWidth();
        final double rectWidth = Math.abs ( transY2 - transY1 );
        final Rectangle2D bar = new Rectangle2D.Double ( rectX, rectY, rectWidth, rectHeight );
        Paint itemPaint = this.getItemPaint ( row, column );
        final GradientPaintTransformer t = this.getGradientPaintTransformer();
        if ( t != null && itemPaint instanceof GradientPaint ) {
            itemPaint = t.transform ( ( GradientPaint ) itemPaint, ( Shape ) bar );
        }
        g2.setPaint ( itemPaint );
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
        final Number n = dataset.getStdDevValue ( row, column );
        if ( n != null ) {
            final double valueDelta = n.doubleValue();
            final double highVal = rangeAxis.valueToJava2D ( meanValue.doubleValue() + valueDelta, dataArea, yAxisLocation );
            final double lowVal = rangeAxis.valueToJava2D ( meanValue.doubleValue() - valueDelta, dataArea, yAxisLocation );
            if ( this.errorIndicatorPaint != null ) {
                g2.setPaint ( this.errorIndicatorPaint );
            } else {
                g2.setPaint ( this.getItemOutlinePaint ( row, column ) );
            }
            if ( this.errorIndicatorStroke != null ) {
                g2.setStroke ( this.errorIndicatorStroke );
            } else {
                g2.setStroke ( this.getItemOutlineStroke ( row, column ) );
            }
            Line2D line = new Line2D.Double ( lowVal, rectY + rectHeight / 2.0, highVal, rectY + rectHeight / 2.0 );
            g2.draw ( line );
            line = new Line2D.Double ( highVal, rectY + rectHeight * 0.25, highVal, rectY + rectHeight * 0.75 );
            g2.draw ( line );
            line = new Line2D.Double ( lowVal, rectY + rectHeight * 0.25, lowVal, rectY + rectHeight * 0.75 );
            g2.draw ( line );
        }
        final CategoryItemLabelGenerator generator = this.getItemLabelGenerator ( row, column );
        if ( generator != null && this.isItemLabelVisible ( row, column ) ) {
            this.drawItemLabel ( g2, dataset, row, column, plot, generator, bar, value < 0.0 );
        }
        final EntityCollection entities = state.getEntityCollection();
        if ( entities != null ) {
            this.addItemEntity ( entities, dataset, row, column, bar );
        }
    }
    protected void drawVerticalItem ( final Graphics2D g2, final CategoryItemRendererState state, final Rectangle2D dataArea, final CategoryPlot plot, final CategoryAxis domainAxis, final ValueAxis rangeAxis, final StatisticalCategoryDataset dataset, final int visibleRow, final int row, final int column ) {
        final double rectX = this.calculateBarW0 ( plot, PlotOrientation.VERTICAL, dataArea, domainAxis, state, visibleRow, column );
        final Number meanValue = dataset.getMeanValue ( row, column );
        if ( meanValue == null ) {
            return;
        }
        double value = meanValue.doubleValue();
        double base = 0.0;
        final double lclip = this.getLowerClip();
        final double uclip = this.getUpperClip();
        if ( uclip <= 0.0 ) {
            if ( value >= uclip ) {
                return;
            }
            base = uclip;
            if ( value <= lclip ) {
                value = lclip;
            }
        } else if ( lclip <= 0.0 ) {
            if ( value >= uclip ) {
                value = uclip;
            } else if ( value <= lclip ) {
                value = lclip;
            }
        } else {
            if ( value <= lclip ) {
                return;
            }
            base = this.getLowerClip();
            if ( value >= uclip ) {
                value = uclip;
            }
        }
        final RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
        final double transY1 = rangeAxis.valueToJava2D ( base, dataArea, yAxisLocation );
        final double transY2 = rangeAxis.valueToJava2D ( value, dataArea, yAxisLocation );
        final double rectY = Math.min ( transY2, transY1 );
        final double rectWidth = state.getBarWidth();
        final double rectHeight = Math.abs ( transY2 - transY1 );
        final Rectangle2D bar = new Rectangle2D.Double ( rectX, rectY, rectWidth, rectHeight );
        Paint itemPaint = this.getItemPaint ( row, column );
        final GradientPaintTransformer t = this.getGradientPaintTransformer();
        if ( t != null && itemPaint instanceof GradientPaint ) {
            itemPaint = t.transform ( ( GradientPaint ) itemPaint, ( Shape ) bar );
        }
        g2.setPaint ( itemPaint );
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
        final Number n = dataset.getStdDevValue ( row, column );
        if ( n != null ) {
            final double valueDelta = n.doubleValue();
            final double highVal = rangeAxis.valueToJava2D ( meanValue.doubleValue() + valueDelta, dataArea, yAxisLocation );
            final double lowVal = rangeAxis.valueToJava2D ( meanValue.doubleValue() - valueDelta, dataArea, yAxisLocation );
            if ( this.errorIndicatorPaint != null ) {
                g2.setPaint ( this.errorIndicatorPaint );
            } else {
                g2.setPaint ( this.getItemOutlinePaint ( row, column ) );
            }
            if ( this.errorIndicatorStroke != null ) {
                g2.setStroke ( this.errorIndicatorStroke );
            } else {
                g2.setStroke ( this.getItemOutlineStroke ( row, column ) );
            }
            Line2D line = new Line2D.Double ( rectX + rectWidth / 2.0, lowVal, rectX + rectWidth / 2.0, highVal );
            g2.draw ( line );
            line = new Line2D.Double ( rectX + rectWidth / 2.0 - 5.0, highVal, rectX + rectWidth / 2.0 + 5.0, highVal );
            g2.draw ( line );
            line = new Line2D.Double ( rectX + rectWidth / 2.0 - 5.0, lowVal, rectX + rectWidth / 2.0 + 5.0, lowVal );
            g2.draw ( line );
        }
        final CategoryItemLabelGenerator generator = this.getItemLabelGenerator ( row, column );
        if ( generator != null && this.isItemLabelVisible ( row, column ) ) {
            this.drawItemLabel ( g2, dataset, row, column, plot, generator, bar, value < 0.0 );
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
        if ( ! ( obj instanceof StatisticalBarRenderer ) ) {
            return false;
        }
        final StatisticalBarRenderer that = ( StatisticalBarRenderer ) obj;
        return PaintUtilities.equal ( this.errorIndicatorPaint, that.errorIndicatorPaint ) && ObjectUtilities.equal ( ( Object ) this.errorIndicatorStroke, ( Object ) that.errorIndicatorStroke ) && super.equals ( obj );
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.errorIndicatorPaint, stream );
        SerialUtilities.writeStroke ( this.errorIndicatorStroke, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.errorIndicatorPaint = SerialUtilities.readPaint ( stream );
        this.errorIndicatorStroke = SerialUtilities.readStroke ( stream );
    }
}
