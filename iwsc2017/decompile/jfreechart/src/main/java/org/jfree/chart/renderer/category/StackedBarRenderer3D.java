package org.jfree.chart.renderer.category;
import org.jfree.chart.HashUtilities;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.entity.EntityCollection;
import java.awt.Paint;
import java.awt.Shape;
import org.jfree.util.BooleanUtilities;
import org.jfree.chart.util.PaintAlpha;
import org.jfree.chart.axis.ValueAxis;
import java.awt.Graphics2D;
import org.jfree.data.Values2D;
import org.jfree.data.DataUtilities;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.PlotOrientation;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.chart.event.RendererChangeEvent;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class StackedBarRenderer3D extends BarRenderer3D implements Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = -5832945916493247123L;
    private boolean renderAsPercentages;
    private boolean ignoreZeroValues;
    public StackedBarRenderer3D() {
        this ( false );
    }
    public StackedBarRenderer3D ( final double xOffset, final double yOffset ) {
        super ( xOffset, yOffset );
    }
    public StackedBarRenderer3D ( final boolean renderAsPercentages ) {
        this.renderAsPercentages = renderAsPercentages;
    }
    public StackedBarRenderer3D ( final double xOffset, final double yOffset, final boolean renderAsPercentages ) {
        super ( xOffset, yOffset );
        this.renderAsPercentages = renderAsPercentages;
    }
    public boolean getRenderAsPercentages() {
        return this.renderAsPercentages;
    }
    public void setRenderAsPercentages ( final boolean asPercentages ) {
        this.renderAsPercentages = asPercentages;
        this.fireChangeEvent();
    }
    public boolean getIgnoreZeroValues() {
        return this.ignoreZeroValues;
    }
    public void setIgnoreZeroValues ( final boolean ignore ) {
        this.ignoreZeroValues = ignore;
        this.notifyListeners ( new RendererChangeEvent ( this ) );
    }
    public Range findRangeBounds ( final CategoryDataset dataset ) {
        if ( dataset == null ) {
            return null;
        }
        if ( this.renderAsPercentages ) {
            return new Range ( 0.0, 1.0 );
        }
        return DatasetUtilities.findStackedRangeBounds ( dataset );
    }
    protected void calculateBarWidth ( final CategoryPlot plot, final Rectangle2D dataArea, final int rendererIndex, final CategoryItemRendererState state ) {
        final CategoryAxis domainAxis = this.getDomainAxis ( plot, rendererIndex );
        final CategoryDataset data = plot.getDataset ( rendererIndex );
        if ( data != null ) {
            final PlotOrientation orientation = plot.getOrientation();
            double space = 0.0;
            if ( orientation == PlotOrientation.HORIZONTAL ) {
                space = dataArea.getHeight();
            } else if ( orientation == PlotOrientation.VERTICAL ) {
                space = dataArea.getWidth();
            }
            final double maxWidth = space * this.getMaximumBarWidth();
            final int columns = data.getColumnCount();
            double categoryMargin = 0.0;
            if ( columns > 1 ) {
                categoryMargin = domainAxis.getCategoryMargin();
            }
            final double used = space * ( 1.0 - domainAxis.getLowerMargin() - domainAxis.getUpperMargin() - categoryMargin );
            if ( columns > 0 ) {
                state.setBarWidth ( Math.min ( used / columns, maxWidth ) );
            } else {
                state.setBarWidth ( Math.min ( used, maxWidth ) );
            }
        }
    }
    protected List createStackedValueList ( final CategoryDataset dataset, final Comparable category, final double base, final boolean asPercentages ) {
        final int[] rows = new int[dataset.getRowCount()];
        for ( int i = 0; i < rows.length; ++i ) {
            rows[i] = i;
        }
        return this.createStackedValueList ( dataset, category, rows, base, asPercentages );
    }
    protected List createStackedValueList ( final CategoryDataset dataset, final Comparable category, final int[] includedRows, final double base, final boolean asPercentages ) {
        final List result = new ArrayList();
        double posBase = base;
        double negBase = base;
        double total = 0.0;
        if ( asPercentages ) {
            total = DataUtilities.calculateColumnTotal ( dataset, dataset.getColumnIndex ( category ), includedRows );
        }
        int baseIndex = -1;
        for ( final int r : includedRows ) {
            final Number n = dataset.getValue ( dataset.getRowKey ( r ), category );
            if ( n != null ) {
                double v = n.doubleValue();
                if ( asPercentages ) {
                    v /= total;
                }
                if ( v > 0.0 || ( !this.ignoreZeroValues && v >= 0.0 ) ) {
                    if ( baseIndex < 0 ) {
                        result.add ( new Object[] { null, new Double ( base ) } );
                        baseIndex = 0;
                    }
                    posBase += v;
                    result.add ( new Object[] { new Integer ( r ), new Double ( posBase ) } );
                } else if ( v < 0.0 ) {
                    if ( baseIndex < 0 ) {
                        result.add ( new Object[] { null, new Double ( base ) } );
                        baseIndex = 0;
                    }
                    negBase += v;
                    result.add ( 0, new Object[] { new Integer ( -r - 1 ), new Double ( negBase ) } );
                    ++baseIndex;
                }
            }
        }
        return result;
    }
    @Override
    public void drawItem ( final Graphics2D g2, final CategoryItemRendererState state, final Rectangle2D dataArea, final CategoryPlot plot, final CategoryAxis domainAxis, final ValueAxis rangeAxis, final CategoryDataset dataset, final int row, final int column, final int pass ) {
        if ( row < dataset.getRowCount() - 1 ) {
            return;
        }
        final Comparable category = dataset.getColumnKey ( column );
        final List values = this.createStackedValueList ( dataset, dataset.getColumnKey ( column ), state.getVisibleSeriesArray(), this.getBase(), this.renderAsPercentages );
        final Rectangle2D adjusted = new Rectangle2D.Double ( dataArea.getX(), dataArea.getY() + this.getYOffset(), dataArea.getWidth() - this.getXOffset(), dataArea.getHeight() - this.getYOffset() );
        final PlotOrientation orientation = plot.getOrientation();
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            this.drawStackHorizontal ( values, category, g2, state, adjusted, plot, domainAxis, rangeAxis, dataset );
        } else {
            this.drawStackVertical ( values, category, g2, state, adjusted, plot, domainAxis, rangeAxis, dataset );
        }
    }
    protected void drawStackHorizontal ( final List values, final Comparable category, final Graphics2D g2, final CategoryItemRendererState state, final Rectangle2D dataArea, final CategoryPlot plot, final CategoryAxis domainAxis, final ValueAxis rangeAxis, final CategoryDataset dataset ) {
        final int column = dataset.getColumnIndex ( category );
        final double barX0 = domainAxis.getCategoryMiddle ( column, dataset.getColumnCount(), dataArea, plot.getDomainAxisEdge() ) - state.getBarWidth() / 2.0;
        final double barW = state.getBarWidth();
        final List itemLabelList = new ArrayList();
        final boolean inverted = rangeAxis.isInverted();
        for ( int blockCount = values.size() - 1, k = 0; k < blockCount; ++k ) {
            final int index = inverted ? ( blockCount - k - 1 ) : k;
            final Object[] prev = values.get ( index );
            final Object[] curr = values.get ( index + 1 );
            int series;
            if ( curr[0] == null ) {
                series = - ( int ) prev[0] - 1;
            } else {
                series = ( int ) curr[0];
                if ( series < 0 ) {
                    series = - ( int ) prev[0] - 1;
                }
            }
            final double v0 = ( double ) prev[1];
            final double vv0 = rangeAxis.valueToJava2D ( v0, dataArea, plot.getRangeAxisEdge() );
            final double v = ( double ) curr[1];
            final double vv = rangeAxis.valueToJava2D ( v, dataArea, plot.getRangeAxisEdge() );
            final Shape[] faces = this.createHorizontalBlock ( barX0, barW, vv0, vv, inverted );
            final Paint fillPaint = this.getItemPaint ( series, column );
            final Paint fillPaintDark = PaintAlpha.darker ( fillPaint );
            final boolean drawOutlines = this.isDrawBarOutline();
            Paint outlinePaint = fillPaint;
            if ( drawOutlines ) {
                outlinePaint = this.getItemOutlinePaint ( series, column );
                g2.setStroke ( this.getItemOutlineStroke ( series, column ) );
            }
            for ( int f = 0; f < 6; ++f ) {
                if ( f == 5 ) {
                    g2.setPaint ( fillPaint );
                } else {
                    g2.setPaint ( fillPaintDark );
                }
                g2.fill ( faces[f] );
                if ( drawOutlines ) {
                    g2.setPaint ( outlinePaint );
                    g2.draw ( faces[f] );
                }
            }
            itemLabelList.add ( new Object[] { new Integer ( series ), faces[5].getBounds2D(), BooleanUtilities.valueOf ( v0 < this.getBase() ) } );
            final EntityCollection entities = state.getEntityCollection();
            if ( entities != null ) {
                this.addItemEntity ( entities, dataset, series, column, faces[5] );
            }
        }
        for ( int i = 0; i < itemLabelList.size(); ++i ) {
            final Object[] record = itemLabelList.get ( i );
            final int series2 = ( int ) record[0];
            final Rectangle2D bar = ( Rectangle2D ) record[1];
            final boolean neg = ( boolean ) record[2];
            final CategoryItemLabelGenerator generator = this.getItemLabelGenerator ( series2, column );
            if ( generator != null && this.isItemLabelVisible ( series2, column ) ) {
                this.drawItemLabel ( g2, dataset, series2, column, plot, generator, bar, neg );
            }
        }
    }
    private Shape[] createHorizontalBlock ( final double x0, final double width, final double y0, final double y1, final boolean inverted ) {
        final Shape[] result = new Shape[6];
        final Point2D p00 = new Point2D.Double ( y0, x0 );
        final Point2D p = new Point2D.Double ( y0, x0 + width );
        final Point2D p2 = new Point2D.Double ( p.getX() + this.getXOffset(), p.getY() - this.getYOffset() );
        final Point2D p3 = new Point2D.Double ( p00.getX() + this.getXOffset(), p00.getY() - this.getYOffset() );
        final Point2D p4 = new Point2D.Double ( y1, x0 );
        final Point2D p5 = new Point2D.Double ( y1, x0 + width );
        final Point2D p6 = new Point2D.Double ( p5.getX() + this.getXOffset(), p5.getY() - this.getYOffset() );
        final Point2D p7 = new Point2D.Double ( p4.getX() + this.getXOffset(), p4.getY() - this.getYOffset() );
        final GeneralPath bottom = new GeneralPath();
        bottom.moveTo ( ( float ) p5.getX(), ( float ) p5.getY() );
        bottom.lineTo ( ( float ) p.getX(), ( float ) p.getY() );
        bottom.lineTo ( ( float ) p2.getX(), ( float ) p2.getY() );
        bottom.lineTo ( ( float ) p6.getX(), ( float ) p6.getY() );
        bottom.closePath();
        final GeneralPath top = new GeneralPath();
        top.moveTo ( ( float ) p4.getX(), ( float ) p4.getY() );
        top.lineTo ( ( float ) p00.getX(), ( float ) p00.getY() );
        top.lineTo ( ( float ) p3.getX(), ( float ) p3.getY() );
        top.lineTo ( ( float ) p7.getX(), ( float ) p7.getY() );
        top.closePath();
        final GeneralPath back = new GeneralPath();
        back.moveTo ( ( float ) p6.getX(), ( float ) p6.getY() );
        back.lineTo ( ( float ) p2.getX(), ( float ) p2.getY() );
        back.lineTo ( ( float ) p3.getX(), ( float ) p3.getY() );
        back.lineTo ( ( float ) p7.getX(), ( float ) p7.getY() );
        back.closePath();
        final GeneralPath front = new GeneralPath();
        front.moveTo ( ( float ) p4.getX(), ( float ) p4.getY() );
        front.lineTo ( ( float ) p5.getX(), ( float ) p5.getY() );
        front.lineTo ( ( float ) p.getX(), ( float ) p.getY() );
        front.lineTo ( ( float ) p00.getX(), ( float ) p00.getY() );
        front.closePath();
        final GeneralPath left = new GeneralPath();
        left.moveTo ( ( float ) p4.getX(), ( float ) p4.getY() );
        left.lineTo ( ( float ) p5.getX(), ( float ) p5.getY() );
        left.lineTo ( ( float ) p6.getX(), ( float ) p6.getY() );
        left.lineTo ( ( float ) p7.getX(), ( float ) p7.getY() );
        left.closePath();
        final GeneralPath right = new GeneralPath();
        right.moveTo ( ( float ) p00.getX(), ( float ) p00.getY() );
        right.lineTo ( ( float ) p.getX(), ( float ) p.getY() );
        right.lineTo ( ( float ) p2.getX(), ( float ) p2.getY() );
        right.lineTo ( ( float ) p3.getX(), ( float ) p3.getY() );
        right.closePath();
        result[0] = bottom;
        result[1] = back;
        if ( inverted ) {
            result[2] = right;
            result[3] = left;
        } else {
            result[2] = left;
            result[3] = right;
        }
        result[4] = top;
        result[5] = front;
        return result;
    }
    protected void drawStackVertical ( final List values, final Comparable category, final Graphics2D g2, final CategoryItemRendererState state, final Rectangle2D dataArea, final CategoryPlot plot, final CategoryAxis domainAxis, final ValueAxis rangeAxis, final CategoryDataset dataset ) {
        final int column = dataset.getColumnIndex ( category );
        final double barX0 = domainAxis.getCategoryMiddle ( column, dataset.getColumnCount(), dataArea, plot.getDomainAxisEdge() ) - state.getBarWidth() / 2.0;
        final double barW = state.getBarWidth();
        final List itemLabelList = new ArrayList();
        final boolean inverted = rangeAxis.isInverted();
        for ( int blockCount = values.size() - 1, k = 0; k < blockCount; ++k ) {
            final int index = inverted ? ( blockCount - k - 1 ) : k;
            final Object[] prev = values.get ( index );
            final Object[] curr = values.get ( index + 1 );
            int series;
            if ( curr[0] == null ) {
                series = - ( int ) prev[0] - 1;
            } else {
                series = ( int ) curr[0];
                if ( series < 0 ) {
                    series = - ( int ) prev[0] - 1;
                }
            }
            final double v0 = ( double ) prev[1];
            final double vv0 = rangeAxis.valueToJava2D ( v0, dataArea, plot.getRangeAxisEdge() );
            final double v = ( double ) curr[1];
            final double vv = rangeAxis.valueToJava2D ( v, dataArea, plot.getRangeAxisEdge() );
            final Shape[] faces = this.createVerticalBlock ( barX0, barW, vv0, vv, inverted );
            final Paint fillPaint = this.getItemPaint ( series, column );
            final Paint fillPaintDark = PaintAlpha.darker ( fillPaint );
            final boolean drawOutlines = this.isDrawBarOutline();
            Paint outlinePaint = fillPaint;
            if ( drawOutlines ) {
                outlinePaint = this.getItemOutlinePaint ( series, column );
                g2.setStroke ( this.getItemOutlineStroke ( series, column ) );
            }
            for ( int f = 0; f < 6; ++f ) {
                if ( f == 5 ) {
                    g2.setPaint ( fillPaint );
                } else {
                    g2.setPaint ( fillPaintDark );
                }
                g2.fill ( faces[f] );
                if ( drawOutlines ) {
                    g2.setPaint ( outlinePaint );
                    g2.draw ( faces[f] );
                }
            }
            itemLabelList.add ( new Object[] { new Integer ( series ), faces[5].getBounds2D(), BooleanUtilities.valueOf ( v0 < this.getBase() ) } );
            final EntityCollection entities = state.getEntityCollection();
            if ( entities != null ) {
                this.addItemEntity ( entities, dataset, series, column, faces[5] );
            }
        }
        for ( int i = 0; i < itemLabelList.size(); ++i ) {
            final Object[] record = itemLabelList.get ( i );
            final int series2 = ( int ) record[0];
            final Rectangle2D bar = ( Rectangle2D ) record[1];
            final boolean neg = ( boolean ) record[2];
            final CategoryItemLabelGenerator generator = this.getItemLabelGenerator ( series2, column );
            if ( generator != null && this.isItemLabelVisible ( series2, column ) ) {
                this.drawItemLabel ( g2, dataset, series2, column, plot, generator, bar, neg );
            }
        }
    }
    private Shape[] createVerticalBlock ( final double x0, final double width, final double y0, final double y1, final boolean inverted ) {
        final Shape[] result = new Shape[6];
        final Point2D p00 = new Point2D.Double ( x0, y0 );
        final Point2D p = new Point2D.Double ( x0 + width, y0 );
        final Point2D p2 = new Point2D.Double ( p.getX() + this.getXOffset(), p.getY() - this.getYOffset() );
        final Point2D p3 = new Point2D.Double ( p00.getX() + this.getXOffset(), p00.getY() - this.getYOffset() );
        final Point2D p4 = new Point2D.Double ( x0, y1 );
        final Point2D p5 = new Point2D.Double ( x0 + width, y1 );
        final Point2D p6 = new Point2D.Double ( p5.getX() + this.getXOffset(), p5.getY() - this.getYOffset() );
        final Point2D p7 = new Point2D.Double ( p4.getX() + this.getXOffset(), p4.getY() - this.getYOffset() );
        final GeneralPath right = new GeneralPath();
        right.moveTo ( ( float ) p5.getX(), ( float ) p5.getY() );
        right.lineTo ( ( float ) p.getX(), ( float ) p.getY() );
        right.lineTo ( ( float ) p2.getX(), ( float ) p2.getY() );
        right.lineTo ( ( float ) p6.getX(), ( float ) p6.getY() );
        right.closePath();
        final GeneralPath left = new GeneralPath();
        left.moveTo ( ( float ) p4.getX(), ( float ) p4.getY() );
        left.lineTo ( ( float ) p00.getX(), ( float ) p00.getY() );
        left.lineTo ( ( float ) p3.getX(), ( float ) p3.getY() );
        left.lineTo ( ( float ) p7.getX(), ( float ) p7.getY() );
        left.closePath();
        final GeneralPath back = new GeneralPath();
        back.moveTo ( ( float ) p6.getX(), ( float ) p6.getY() );
        back.lineTo ( ( float ) p2.getX(), ( float ) p2.getY() );
        back.lineTo ( ( float ) p3.getX(), ( float ) p3.getY() );
        back.lineTo ( ( float ) p7.getX(), ( float ) p7.getY() );
        back.closePath();
        final GeneralPath front = new GeneralPath();
        front.moveTo ( ( float ) p4.getX(), ( float ) p4.getY() );
        front.lineTo ( ( float ) p5.getX(), ( float ) p5.getY() );
        front.lineTo ( ( float ) p.getX(), ( float ) p.getY() );
        front.lineTo ( ( float ) p00.getX(), ( float ) p00.getY() );
        front.closePath();
        final GeneralPath top = new GeneralPath();
        top.moveTo ( ( float ) p4.getX(), ( float ) p4.getY() );
        top.lineTo ( ( float ) p5.getX(), ( float ) p5.getY() );
        top.lineTo ( ( float ) p6.getX(), ( float ) p6.getY() );
        top.lineTo ( ( float ) p7.getX(), ( float ) p7.getY() );
        top.closePath();
        final GeneralPath bottom = new GeneralPath();
        bottom.moveTo ( ( float ) p00.getX(), ( float ) p00.getY() );
        bottom.lineTo ( ( float ) p.getX(), ( float ) p.getY() );
        bottom.lineTo ( ( float ) p2.getX(), ( float ) p2.getY() );
        bottom.lineTo ( ( float ) p3.getX(), ( float ) p3.getY() );
        bottom.closePath();
        result[0] = bottom;
        result[1] = back;
        result[2] = left;
        result[3] = right;
        result[4] = top;
        result[5] = front;
        if ( inverted ) {
            result[0] = top;
            result[4] = bottom;
        }
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof StackedBarRenderer3D ) ) {
            return false;
        }
        final StackedBarRenderer3D that = ( StackedBarRenderer3D ) obj;
        return this.renderAsPercentages == that.getRenderAsPercentages() && this.ignoreZeroValues == that.ignoreZeroValues && super.equals ( obj );
    }
    public int hashCode() {
        int hash = super.hashCode();
        hash = HashUtilities.hashCode ( hash, this.renderAsPercentages );
        hash = HashUtilities.hashCode ( hash, this.ignoreZeroValues );
        return hash;
    }
}
