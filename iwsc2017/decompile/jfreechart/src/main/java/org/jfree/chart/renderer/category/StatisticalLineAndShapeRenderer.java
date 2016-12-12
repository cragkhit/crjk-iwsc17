package org.jfree.chart.renderer.category;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.chart.HashUtilities;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ShapeUtilities;
import java.awt.Shape;
import org.jfree.chart.plot.PlotOrientation;
import java.awt.geom.Line2D;
import org.jfree.data.statistics.StatisticalCategoryDataset;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.CategoryPlot;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import java.awt.Stroke;
import java.awt.Paint;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class StatisticalLineAndShapeRenderer extends LineAndShapeRenderer implements Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = -3557517173697777579L;
    private transient Paint errorIndicatorPaint;
    private transient Stroke errorIndicatorStroke;
    public StatisticalLineAndShapeRenderer() {
        this ( true, true );
    }
    public StatisticalLineAndShapeRenderer ( final boolean linesVisible, final boolean shapesVisible ) {
        super ( linesVisible, shapesVisible );
        this.errorIndicatorPaint = null;
        this.errorIndicatorStroke = null;
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
    public Range findRangeBounds ( final CategoryDataset dataset ) {
        return this.findRangeBounds ( dataset, true );
    }
    @Override
    public void drawItem ( final Graphics2D g2, final CategoryItemRendererState state, final Rectangle2D dataArea, final CategoryPlot plot, final CategoryAxis domainAxis, final ValueAxis rangeAxis, final CategoryDataset dataset, final int row, final int column, final int pass ) {
        if ( !this.getItemVisible ( row, column ) ) {
            return;
        }
        if ( ! ( dataset instanceof StatisticalCategoryDataset ) ) {
            super.drawItem ( g2, state, dataArea, plot, domainAxis, rangeAxis, dataset, row, column, pass );
            return;
        }
        final int visibleRow = state.getVisibleSeriesIndex ( row );
        if ( visibleRow < 0 ) {
            return;
        }
        final int visibleRowCount = state.getVisibleSeriesCount();
        final StatisticalCategoryDataset statDataset = ( StatisticalCategoryDataset ) dataset;
        final Number meanValue = statDataset.getMeanValue ( row, column );
        if ( meanValue == null ) {
            return;
        }
        final PlotOrientation orientation = plot.getOrientation();
        double x1;
        if ( this.getUseSeriesOffset() ) {
            x1 = domainAxis.getCategorySeriesMiddle ( column, dataset.getColumnCount(), visibleRow, visibleRowCount, this.getItemMargin(), dataArea, plot.getDomainAxisEdge() );
        } else {
            x1 = domainAxis.getCategoryMiddle ( column, this.getColumnCount(), dataArea, plot.getDomainAxisEdge() );
        }
        final double y1 = rangeAxis.valueToJava2D ( meanValue.doubleValue(), dataArea, plot.getRangeAxisEdge() );
        final Number sdv = statDataset.getStdDevValue ( row, column );
        if ( pass == 1 && sdv != null ) {
            final RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
            final double valueDelta = sdv.doubleValue();
            double highVal;
            if ( meanValue.doubleValue() + valueDelta > rangeAxis.getRange().getUpperBound() ) {
                highVal = rangeAxis.valueToJava2D ( rangeAxis.getRange().getUpperBound(), dataArea, yAxisLocation );
            } else {
                highVal = rangeAxis.valueToJava2D ( meanValue.doubleValue() + valueDelta, dataArea, yAxisLocation );
            }
            double lowVal;
            if ( meanValue.doubleValue() + valueDelta < rangeAxis.getRange().getLowerBound() ) {
                lowVal = rangeAxis.valueToJava2D ( rangeAxis.getRange().getLowerBound(), dataArea, yAxisLocation );
            } else {
                lowVal = rangeAxis.valueToJava2D ( meanValue.doubleValue() - valueDelta, dataArea, yAxisLocation );
            }
            if ( this.errorIndicatorPaint != null ) {
                g2.setPaint ( this.errorIndicatorPaint );
            } else {
                g2.setPaint ( this.getItemPaint ( row, column ) );
            }
            if ( this.errorIndicatorStroke != null ) {
                g2.setStroke ( this.errorIndicatorStroke );
            } else {
                g2.setStroke ( this.getItemOutlineStroke ( row, column ) );
            }
            final Line2D line = new Line2D.Double();
            if ( orientation == PlotOrientation.HORIZONTAL ) {
                line.setLine ( lowVal, x1, highVal, x1 );
                g2.draw ( line );
                line.setLine ( lowVal, x1 - 5.0, lowVal, x1 + 5.0 );
                g2.draw ( line );
                line.setLine ( highVal, x1 - 5.0, highVal, x1 + 5.0 );
                g2.draw ( line );
            } else {
                line.setLine ( x1, lowVal, x1, highVal );
                g2.draw ( line );
                line.setLine ( x1 - 5.0, highVal, x1 + 5.0, highVal );
                g2.draw ( line );
                line.setLine ( x1 - 5.0, lowVal, x1 + 5.0, lowVal );
                g2.draw ( line );
            }
        }
        Shape hotspot = null;
        if ( pass == 1 && this.getItemShapeVisible ( row, column ) ) {
            Shape shape = this.getItemShape ( row, column );
            if ( orientation == PlotOrientation.HORIZONTAL ) {
                shape = ShapeUtilities.createTranslatedShape ( shape, y1, x1 );
            } else if ( orientation == PlotOrientation.VERTICAL ) {
                shape = ShapeUtilities.createTranslatedShape ( shape, x1, y1 );
            }
            hotspot = shape;
            if ( this.getItemShapeFilled ( row, column ) ) {
                if ( this.getUseFillPaint() ) {
                    g2.setPaint ( this.getItemFillPaint ( row, column ) );
                } else {
                    g2.setPaint ( this.getItemPaint ( row, column ) );
                }
                g2.fill ( shape );
            }
            if ( this.getDrawOutlines() ) {
                if ( this.getUseOutlinePaint() ) {
                    g2.setPaint ( this.getItemOutlinePaint ( row, column ) );
                } else {
                    g2.setPaint ( this.getItemPaint ( row, column ) );
                }
                g2.setStroke ( this.getItemOutlineStroke ( row, column ) );
                g2.draw ( shape );
            }
            if ( this.isItemLabelVisible ( row, column ) ) {
                if ( orientation == PlotOrientation.HORIZONTAL ) {
                    this.drawItemLabel ( g2, orientation, dataset, row, column, y1, x1, meanValue.doubleValue() < 0.0 );
                } else if ( orientation == PlotOrientation.VERTICAL ) {
                    this.drawItemLabel ( g2, orientation, dataset, row, column, x1, y1, meanValue.doubleValue() < 0.0 );
                }
            }
        }
        if ( pass == 0 && this.getItemLineVisible ( row, column ) && column != 0 ) {
            final Number previousValue = statDataset.getValue ( row, column - 1 );
            if ( previousValue != null ) {
                final double previous = previousValue.doubleValue();
                double x2;
                if ( this.getUseSeriesOffset() ) {
                    x2 = domainAxis.getCategorySeriesMiddle ( column - 1, dataset.getColumnCount(), visibleRow, visibleRowCount, this.getItemMargin(), dataArea, plot.getDomainAxisEdge() );
                } else {
                    x2 = domainAxis.getCategoryMiddle ( column - 1, this.getColumnCount(), dataArea, plot.getDomainAxisEdge() );
                }
                final double y2 = rangeAxis.valueToJava2D ( previous, dataArea, plot.getRangeAxisEdge() );
                Line2D line2 = null;
                if ( orientation == PlotOrientation.HORIZONTAL ) {
                    line2 = new Line2D.Double ( y2, x2, y1, x1 );
                } else if ( orientation == PlotOrientation.VERTICAL ) {
                    line2 = new Line2D.Double ( x2, y2, x1, y1 );
                }
                g2.setPaint ( this.getItemPaint ( row, column ) );
                g2.setStroke ( this.getItemStroke ( row, column ) );
                g2.draw ( line2 );
            }
        }
        if ( pass == 1 ) {
            final EntityCollection entities = state.getEntityCollection();
            if ( entities != null ) {
                this.addEntity ( entities, hotspot, dataset, row, column, x1, y1 );
            }
        }
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof StatisticalLineAndShapeRenderer ) ) {
            return false;
        }
        final StatisticalLineAndShapeRenderer that = ( StatisticalLineAndShapeRenderer ) obj;
        return PaintUtilities.equal ( this.errorIndicatorPaint, that.errorIndicatorPaint ) && ObjectUtilities.equal ( ( Object ) this.errorIndicatorStroke, ( Object ) that.errorIndicatorStroke ) && super.equals ( obj );
    }
    public int hashCode() {
        int hash = super.hashCode();
        hash = HashUtilities.hashCode ( hash, this.errorIndicatorPaint );
        return hash;
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
