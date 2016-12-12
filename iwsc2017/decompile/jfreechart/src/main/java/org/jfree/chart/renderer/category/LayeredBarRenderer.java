package org.jfree.chart.renderer.category;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import java.awt.Stroke;
import org.jfree.ui.GradientPaintTransformer;
import java.awt.Paint;
import org.jfree.ui.RectangleEdge;
import java.awt.Shape;
import java.awt.GradientPaint;
import org.jfree.chart.axis.ValueAxis;
import java.awt.Graphics2D;
import org.jfree.data.category.CategoryDataset;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.PlotOrientation;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.util.ObjectList;
import java.io.Serializable;
public class LayeredBarRenderer extends BarRenderer implements Serializable {
    private static final long serialVersionUID = -8716572894780469487L;
    protected ObjectList seriesBarWidthList;
    public LayeredBarRenderer() {
        this.seriesBarWidthList = new ObjectList();
    }
    public double getSeriesBarWidth ( final int series ) {
        double result = Double.NaN;
        final Number n = ( Number ) this.seriesBarWidthList.get ( series );
        if ( n != null ) {
            result = n.doubleValue();
        }
        return result;
    }
    public void setSeriesBarWidth ( final int series, final double width ) {
        this.seriesBarWidthList.set ( series, ( Object ) new Double ( width ) );
    }
    @Override
    protected void calculateBarWidth ( final CategoryPlot plot, final Rectangle2D dataArea, final int rendererIndex, final CategoryItemRendererState state ) {
        final CategoryAxis domainAxis = this.getDomainAxis ( plot, rendererIndex );
        final CategoryDataset dataset = plot.getDataset ( rendererIndex );
        if ( dataset != null ) {
            final int columns = dataset.getColumnCount();
            final int rows = dataset.getRowCount();
            double space = 0.0;
            final PlotOrientation orientation = plot.getOrientation();
            if ( orientation == PlotOrientation.HORIZONTAL ) {
                space = dataArea.getHeight();
            } else if ( orientation == PlotOrientation.VERTICAL ) {
                space = dataArea.getWidth();
            }
            final double maxWidth = space * this.getMaximumBarWidth();
            double categoryMargin = 0.0;
            if ( columns > 1 ) {
                categoryMargin = domainAxis.getCategoryMargin();
            }
            final double used = space * ( 1.0 - domainAxis.getLowerMargin() - domainAxis.getUpperMargin() - categoryMargin );
            if ( rows * columns > 0 ) {
                state.setBarWidth ( Math.min ( used / dataset.getColumnCount(), maxWidth ) );
            } else {
                state.setBarWidth ( Math.min ( used, maxWidth ) );
            }
        }
    }
    @Override
    public void drawItem ( final Graphics2D g2, final CategoryItemRendererState state, final Rectangle2D dataArea, final CategoryPlot plot, final CategoryAxis domainAxis, final ValueAxis rangeAxis, final CategoryDataset data, final int row, final int column, final int pass ) {
        final PlotOrientation orientation = plot.getOrientation();
        if ( orientation.isHorizontal() ) {
            this.drawHorizontalItem ( g2, state, dataArea, plot, domainAxis, rangeAxis, data, row, column );
        } else if ( orientation.isVertical() ) {
            this.drawVerticalItem ( g2, state, dataArea, plot, domainAxis, rangeAxis, data, row, column );
        }
    }
    protected void drawHorizontalItem ( final Graphics2D g2, final CategoryItemRendererState state, final Rectangle2D dataArea, final CategoryPlot plot, final CategoryAxis domainAxis, final ValueAxis rangeAxis, final CategoryDataset dataset, final int row, final int column ) {
        final Number dataValue = dataset.getValue ( row, column );
        if ( dataValue == null ) {
            return;
        }
        double value = dataValue.doubleValue();
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
            base = lclip;
            if ( value >= uclip ) {
                value = uclip;
            }
        }
        final RectangleEdge edge = plot.getRangeAxisEdge();
        final double transX1 = rangeAxis.valueToJava2D ( base, dataArea, edge );
        final double transX2 = rangeAxis.valueToJava2D ( value, dataArea, edge );
        final double rectX = Math.min ( transX1, transX2 );
        final double rectWidth = Math.abs ( transX2 - transX1 );
        double rectY = domainAxis.getCategoryMiddle ( column, this.getColumnCount(), dataArea, plot.getDomainAxisEdge() ) - state.getBarWidth() / 2.0;
        final int seriesCount = this.getRowCount();
        double shift = 0.0;
        double widthFactor = 1.0;
        final double seriesBarWidth = this.getSeriesBarWidth ( row );
        if ( !Double.isNaN ( seriesBarWidth ) ) {
            widthFactor = seriesBarWidth;
        }
        final double rectHeight = widthFactor * state.getBarWidth();
        rectY += ( 1.0 - widthFactor ) * state.getBarWidth() / 2.0;
        if ( seriesCount > 1 ) {
            shift = rectHeight * 0.2 / ( seriesCount - 1 );
        }
        final Rectangle2D bar = new Rectangle2D.Double ( rectX, rectY + ( seriesCount - 1 - row ) * shift, rectWidth, rectHeight - ( seriesCount - 1 - row ) * shift * 2.0 );
        if ( state.getElementHinting() ) {
            this.beginElementGroup ( g2, dataset.getRowKey ( row ), dataset.getColumnKey ( column ) );
        }
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
        final CategoryItemLabelGenerator generator = this.getItemLabelGenerator ( row, column );
        if ( generator != null && this.isItemLabelVisible ( row, column ) ) {
            this.drawItemLabel ( g2, dataset, row, column, plot, generator, bar, transX1 > transX2 );
        }
        final EntityCollection entities = state.getEntityCollection();
        if ( entities != null ) {
            this.addItemEntity ( entities, dataset, row, column, bar );
        }
    }
    protected void drawVerticalItem ( final Graphics2D g2, final CategoryItemRendererState state, final Rectangle2D dataArea, final CategoryPlot plot, final CategoryAxis domainAxis, final ValueAxis rangeAxis, final CategoryDataset dataset, final int row, final int column ) {
        final Number dataValue = dataset.getValue ( row, column );
        if ( dataValue == null ) {
            return;
        }
        double rectX = domainAxis.getCategoryMiddle ( column, this.getColumnCount(), dataArea, plot.getDomainAxisEdge() ) - state.getBarWidth() / 2.0;
        final int seriesCount = this.getRowCount();
        double value = dataValue.doubleValue();
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
        final RectangleEdge edge = plot.getRangeAxisEdge();
        final double transY1 = rangeAxis.valueToJava2D ( base, dataArea, edge );
        final double transY2 = rangeAxis.valueToJava2D ( value, dataArea, edge );
        final double rectY = Math.min ( transY2, transY1 );
        final double rectHeight = Math.abs ( transY2 - transY1 );
        double shift = 0.0;
        double widthFactor = 1.0;
        final double seriesBarWidth = this.getSeriesBarWidth ( row );
        if ( !Double.isNaN ( seriesBarWidth ) ) {
            widthFactor = seriesBarWidth;
        }
        final double rectWidth = widthFactor * state.getBarWidth();
        rectX += ( 1.0 - widthFactor ) * state.getBarWidth() / 2.0;
        if ( seriesCount > 1 ) {
            shift = rectWidth * 0.2 / ( seriesCount - 1 );
        }
        final Rectangle2D bar = new Rectangle2D.Double ( rectX + ( seriesCount - 1 - row ) * shift, rectY, rectWidth - ( seriesCount - 1 - row ) * shift * 2.0, rectHeight );
        if ( state.getElementHinting() ) {
            this.beginElementGroup ( g2, dataset.getRowKey ( row ), dataset.getColumnKey ( column ) );
        }
        Paint itemPaint = this.getItemPaint ( row, column );
        final GradientPaintTransformer t = this.getGradientPaintTransformer();
        if ( t != null && itemPaint instanceof GradientPaint ) {
            itemPaint = t.transform ( ( GradientPaint ) itemPaint, ( Shape ) bar );
        }
        g2.setPaint ( itemPaint );
        g2.fill ( bar );
        if ( this.isDrawBarOutline() && state.getBarWidth() > 3.0 ) {
            g2.setStroke ( this.getItemOutlineStroke ( row, column ) );
            g2.setPaint ( this.getItemOutlinePaint ( row, column ) );
            g2.draw ( bar );
        }
        if ( state.getElementHinting() ) {
            this.endElementGroup ( g2 );
        }
        final CategoryItemLabelGenerator generator = this.getItemLabelGenerator ( row, column );
        if ( generator != null && this.isItemLabelVisible ( row, column ) ) {
            final double transX1 = rangeAxis.valueToJava2D ( base, dataArea, edge );
            final double transX2 = rangeAxis.valueToJava2D ( value, dataArea, edge );
            this.drawItemLabel ( g2, dataset, row, column, plot, generator, bar, transX1 > transX2 );
        }
        final EntityCollection entities = state.getEntityCollection();
        if ( entities != null ) {
            this.addItemEntity ( entities, dataset, row, column, bar );
        }
    }
}
