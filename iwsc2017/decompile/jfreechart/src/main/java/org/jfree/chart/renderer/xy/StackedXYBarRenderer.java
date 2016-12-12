package org.jfree.chart.renderer.xy;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.entity.EntityCollection;
import java.awt.Shape;
import java.awt.geom.RectangularShape;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.data.Range;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.plot.XYPlot;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.ui.TextAnchor;
import org.jfree.chart.labels.ItemLabelAnchor;
public class StackedXYBarRenderer extends XYBarRenderer {
    private static final long serialVersionUID = -7049101055533436444L;
    private boolean renderAsPercentages;
    public StackedXYBarRenderer() {
        this ( 0.0 );
    }
    public StackedXYBarRenderer ( final double margin ) {
        super ( margin );
        this.renderAsPercentages = false;
        final ItemLabelPosition p = new ItemLabelPosition ( ItemLabelAnchor.CENTER, TextAnchor.CENTER );
        this.setBasePositiveItemLabelPosition ( p );
        this.setBaseNegativeItemLabelPosition ( p );
        this.setPositiveItemLabelPositionFallback ( null );
        this.setNegativeItemLabelPositionFallback ( null );
    }
    public boolean getRenderAsPercentages() {
        return this.renderAsPercentages;
    }
    public void setRenderAsPercentages ( final boolean asPercentages ) {
        this.renderAsPercentages = asPercentages;
        this.fireChangeEvent();
    }
    @Override
    public int getPassCount() {
        return 3;
    }
    @Override
    public XYItemRendererState initialise ( final Graphics2D g2, final Rectangle2D dataArea, final XYPlot plot, final XYDataset data, final PlotRenderingInfo info ) {
        return new XYBarRendererState ( this, info );
    }
    @Override
    public Range findRangeBounds ( final XYDataset dataset ) {
        if ( dataset == null ) {
            return null;
        }
        if ( this.renderAsPercentages ) {
            return new Range ( 0.0, 1.0 );
        }
        return DatasetUtilities.findStackedRangeBounds ( ( TableXYDataset ) dataset );
    }
    @Override
    public void drawItem ( final Graphics2D g2, final XYItemRendererState state, final Rectangle2D dataArea, final PlotRenderingInfo info, final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis, final XYDataset dataset, final int series, final int item, final CrosshairState crosshairState, final int pass ) {
        if ( !this.getItemVisible ( series, item ) ) {
            return;
        }
        if ( ! ( dataset instanceof IntervalXYDataset ) || ! ( dataset instanceof TableXYDataset ) ) {
            String message = "dataset (type " + dataset.getClass().getName() + ") has wrong type:";
            boolean and = false;
            if ( !IntervalXYDataset.class.isAssignableFrom ( dataset.getClass() ) ) {
                message += " it is no IntervalXYDataset";
                and = true;
            }
            if ( !TableXYDataset.class.isAssignableFrom ( dataset.getClass() ) ) {
                if ( and ) {
                    message += " and";
                }
                message += " it is no TableXYDataset";
            }
            throw new IllegalArgumentException ( message );
        }
        final IntervalXYDataset intervalDataset = ( IntervalXYDataset ) dataset;
        double value = intervalDataset.getYValue ( series, item );
        if ( Double.isNaN ( value ) ) {
            return;
        }
        double total = 0.0;
        if ( this.renderAsPercentages ) {
            total = DatasetUtilities.calculateStackTotal ( ( TableXYDataset ) dataset, item );
            value /= total;
        }
        double positiveBase = 0.0;
        double negativeBase = 0.0;
        for ( int i = 0; i < series; ++i ) {
            double v = dataset.getYValue ( i, item );
            if ( !Double.isNaN ( v ) && this.isSeriesVisible ( i ) ) {
                if ( this.renderAsPercentages ) {
                    v /= total;
                }
                if ( v > 0.0 ) {
                    positiveBase += v;
                } else {
                    negativeBase += v;
                }
            }
        }
        final RectangleEdge edgeR = plot.getRangeAxisEdge();
        double translatedBase;
        double translatedValue;
        if ( value > 0.0 ) {
            translatedBase = rangeAxis.valueToJava2D ( positiveBase, dataArea, edgeR );
            translatedValue = rangeAxis.valueToJava2D ( positiveBase + value, dataArea, edgeR );
        } else {
            translatedBase = rangeAxis.valueToJava2D ( negativeBase, dataArea, edgeR );
            translatedValue = rangeAxis.valueToJava2D ( negativeBase + value, dataArea, edgeR );
        }
        final RectangleEdge edgeD = plot.getDomainAxisEdge();
        final double startX = intervalDataset.getStartXValue ( series, item );
        if ( Double.isNaN ( startX ) ) {
            return;
        }
        double translatedStartX = domainAxis.valueToJava2D ( startX, dataArea, edgeD );
        final double endX = intervalDataset.getEndXValue ( series, item );
        if ( Double.isNaN ( endX ) ) {
            return;
        }
        final double translatedEndX = domainAxis.valueToJava2D ( endX, dataArea, edgeD );
        double translatedWidth = Math.max ( 1.0, Math.abs ( translatedEndX - translatedStartX ) );
        final double translatedHeight = Math.abs ( translatedValue - translatedBase );
        if ( this.getMargin() > 0.0 ) {
            final double cut = translatedWidth * this.getMargin();
            translatedWidth -= cut;
            translatedStartX += cut / 2.0;
        }
        Rectangle2D bar = null;
        final PlotOrientation orientation = plot.getOrientation();
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            bar = new Rectangle2D.Double ( Math.min ( translatedBase, translatedValue ), Math.min ( translatedEndX, translatedStartX ), translatedHeight, translatedWidth );
        } else {
            if ( orientation != PlotOrientation.VERTICAL ) {
                throw new IllegalStateException();
            }
            bar = new Rectangle2D.Double ( Math.min ( translatedStartX, translatedEndX ), Math.min ( translatedBase, translatedValue ), translatedWidth, translatedHeight );
        }
        final boolean positive = value > 0.0;
        final boolean inverted = rangeAxis.isInverted();
        RectangleEdge barBase;
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            if ( ( positive && inverted ) || ( !positive && !inverted ) ) {
                barBase = RectangleEdge.RIGHT;
            } else {
                barBase = RectangleEdge.LEFT;
            }
        } else if ( ( positive && !inverted ) || ( !positive && inverted ) ) {
            barBase = RectangleEdge.BOTTOM;
        } else {
            barBase = RectangleEdge.TOP;
        }
        if ( pass == 0 ) {
            if ( this.getShadowsVisible() ) {
                this.getBarPainter().paintBarShadow ( g2, this, series, item, bar, barBase, false );
            }
        } else if ( pass == 1 ) {
            this.getBarPainter().paintBar ( g2, this, series, item, bar, barBase );
            if ( info != null ) {
                final EntityCollection entities = info.getOwner().getEntityCollection();
                if ( entities != null ) {
                    this.addEntity ( entities, bar, dataset, series, item, bar.getCenterX(), bar.getCenterY() );
                }
            }
        } else if ( pass == 2 && this.isItemLabelVisible ( series, item ) ) {
            final XYItemLabelGenerator generator = this.getItemLabelGenerator ( series, item );
            this.drawItemLabel ( g2, dataset, series, item, plot, generator, bar, value < 0.0 );
        }
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof StackedXYBarRenderer ) ) {
            return false;
        }
        final StackedXYBarRenderer that = ( StackedXYBarRenderer ) obj;
        return this.renderAsPercentages == that.renderAsPercentages && super.equals ( obj );
    }
    public int hashCode() {
        int result = super.hashCode();
        result = result * 37 + ( this.renderAsPercentages ? 1 : 0 );
        return result;
    }
}
