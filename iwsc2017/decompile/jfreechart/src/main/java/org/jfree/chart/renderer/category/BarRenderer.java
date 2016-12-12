package org.jfree.chart.renderer.category;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.PaintUtilities;
import org.jfree.util.ObjectUtilities;
import org.jfree.chart.labels.ItemLabelAnchor;
import java.awt.geom.Point2D;
import java.awt.Font;
import org.jfree.text.TextUtilities;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import java.awt.geom.RectangularShape;
import org.jfree.data.ItemKey;
import org.jfree.data.KeyedValues2DItemKey;
import org.jfree.ui.RectangleEdge;
import org.jfree.data.general.Dataset;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.geom.Line2D;
import org.jfree.chart.LegendItem;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.CategoryPlot;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import org.jfree.ui.StandardGradientPaintTransformer;
import org.jfree.chart.util.ParamChecks;
import java.awt.Paint;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.ui.GradientPaintTransformer;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class BarRenderer extends AbstractCategoryItemRenderer implements Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 6000649414965887481L;
    public static final double DEFAULT_ITEM_MARGIN = 0.2;
    public static final double BAR_OUTLINE_WIDTH_THRESHOLD = 3.0;
    private static BarPainter defaultBarPainter;
    private static boolean defaultShadowsVisible;
    private double itemMargin;
    private boolean drawBarOutline;
    private double maximumBarWidth;
    private double minimumBarLength;
    private GradientPaintTransformer gradientPaintTransformer;
    private ItemLabelPosition positiveItemLabelPositionFallback;
    private ItemLabelPosition negativeItemLabelPositionFallback;
    private double upperClip;
    private double lowerClip;
    private double base;
    private boolean includeBaseInRange;
    private BarPainter barPainter;
    private boolean shadowsVisible;
    private transient Paint shadowPaint;
    private double shadowXOffset;
    private double shadowYOffset;
    public static BarPainter getDefaultBarPainter() {
        return BarRenderer.defaultBarPainter;
    }
    public static void setDefaultBarPainter ( final BarPainter painter ) {
        ParamChecks.nullNotPermitted ( painter, "painter" );
        BarRenderer.defaultBarPainter = painter;
    }
    public static boolean getDefaultShadowsVisible() {
        return BarRenderer.defaultShadowsVisible;
    }
    public static void setDefaultShadowsVisible ( final boolean visible ) {
        BarRenderer.defaultShadowsVisible = visible;
    }
    public BarRenderer() {
        this.base = 0.0;
        this.includeBaseInRange = true;
        this.itemMargin = 0.2;
        this.drawBarOutline = false;
        this.maximumBarWidth = 1.0;
        this.positiveItemLabelPositionFallback = null;
        this.negativeItemLabelPositionFallback = null;
        this.gradientPaintTransformer = ( GradientPaintTransformer ) new StandardGradientPaintTransformer();
        this.minimumBarLength = 0.0;
        this.setBaseLegendShape ( new Rectangle2D.Double ( -4.0, -4.0, 8.0, 8.0 ) );
        this.barPainter = getDefaultBarPainter();
        this.shadowsVisible = getDefaultShadowsVisible();
        this.shadowPaint = Color.gray;
        this.shadowXOffset = 4.0;
        this.shadowYOffset = 4.0;
    }
    public double getBase() {
        return this.base;
    }
    public void setBase ( final double base ) {
        this.base = base;
        this.fireChangeEvent();
    }
    public double getItemMargin() {
        return this.itemMargin;
    }
    public void setItemMargin ( final double percent ) {
        this.itemMargin = percent;
        this.fireChangeEvent();
    }
    public boolean isDrawBarOutline() {
        return this.drawBarOutline;
    }
    public void setDrawBarOutline ( final boolean draw ) {
        this.drawBarOutline = draw;
        this.fireChangeEvent();
    }
    public double getMaximumBarWidth() {
        return this.maximumBarWidth;
    }
    public void setMaximumBarWidth ( final double percent ) {
        this.maximumBarWidth = percent;
        this.fireChangeEvent();
    }
    public double getMinimumBarLength() {
        return this.minimumBarLength;
    }
    public void setMinimumBarLength ( final double min ) {
        if ( min < 0.0 ) {
            throw new IllegalArgumentException ( "Requires 'min' >= 0.0" );
        }
        this.minimumBarLength = min;
        this.fireChangeEvent();
    }
    public GradientPaintTransformer getGradientPaintTransformer() {
        return this.gradientPaintTransformer;
    }
    public void setGradientPaintTransformer ( final GradientPaintTransformer transformer ) {
        this.gradientPaintTransformer = transformer;
        this.fireChangeEvent();
    }
    public ItemLabelPosition getPositiveItemLabelPositionFallback() {
        return this.positiveItemLabelPositionFallback;
    }
    public void setPositiveItemLabelPositionFallback ( final ItemLabelPosition position ) {
        this.positiveItemLabelPositionFallback = position;
        this.fireChangeEvent();
    }
    public ItemLabelPosition getNegativeItemLabelPositionFallback() {
        return this.negativeItemLabelPositionFallback;
    }
    public void setNegativeItemLabelPositionFallback ( final ItemLabelPosition position ) {
        this.negativeItemLabelPositionFallback = position;
        this.fireChangeEvent();
    }
    public boolean getIncludeBaseInRange() {
        return this.includeBaseInRange;
    }
    public void setIncludeBaseInRange ( final boolean include ) {
        if ( this.includeBaseInRange != include ) {
            this.includeBaseInRange = include;
            this.fireChangeEvent();
        }
    }
    public BarPainter getBarPainter() {
        return this.barPainter;
    }
    public void setBarPainter ( final BarPainter painter ) {
        ParamChecks.nullNotPermitted ( painter, "painter" );
        this.barPainter = painter;
        this.fireChangeEvent();
    }
    public boolean getShadowsVisible() {
        return this.shadowsVisible;
    }
    public void setShadowVisible ( final boolean visible ) {
        this.shadowsVisible = visible;
        this.fireChangeEvent();
    }
    public Paint getShadowPaint() {
        return this.shadowPaint;
    }
    public void setShadowPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.shadowPaint = paint;
        this.fireChangeEvent();
    }
    public double getShadowXOffset() {
        return this.shadowXOffset;
    }
    public void setShadowXOffset ( final double offset ) {
        this.shadowXOffset = offset;
        this.fireChangeEvent();
    }
    public double getShadowYOffset() {
        return this.shadowYOffset;
    }
    public void setShadowYOffset ( final double offset ) {
        this.shadowYOffset = offset;
        this.fireChangeEvent();
    }
    public double getLowerClip() {
        return this.lowerClip;
    }
    public double getUpperClip() {
        return this.upperClip;
    }
    @Override
    public CategoryItemRendererState initialise ( final Graphics2D g2, final Rectangle2D dataArea, final CategoryPlot plot, final int rendererIndex, final PlotRenderingInfo info ) {
        final CategoryItemRendererState state = super.initialise ( g2, dataArea, plot, rendererIndex, info );
        final ValueAxis rangeAxis = plot.getRangeAxisForDataset ( rendererIndex );
        this.lowerClip = rangeAxis.getRange().getLowerBound();
        this.upperClip = rangeAxis.getRange().getUpperBound();
        this.calculateBarWidth ( plot, dataArea, rendererIndex, state );
        return state;
    }
    protected void calculateBarWidth ( final CategoryPlot plot, final Rectangle2D dataArea, final int rendererIndex, final CategoryItemRendererState state ) {
        final CategoryAxis domainAxis = this.getDomainAxis ( plot, rendererIndex );
        final CategoryDataset dataset = plot.getDataset ( rendererIndex );
        if ( dataset != null ) {
            final int columns = dataset.getColumnCount();
            final int rows = ( state.getVisibleSeriesCount() >= 0 ) ? state.getVisibleSeriesCount() : dataset.getRowCount();
            double space = 0.0;
            final PlotOrientation orientation = plot.getOrientation();
            if ( orientation == PlotOrientation.HORIZONTAL ) {
                space = dataArea.getHeight();
            } else if ( orientation == PlotOrientation.VERTICAL ) {
                space = dataArea.getWidth();
            }
            final double maxWidth = space * this.getMaximumBarWidth();
            double categoryMargin = 0.0;
            double currentItemMargin = 0.0;
            if ( columns > 1 ) {
                categoryMargin = domainAxis.getCategoryMargin();
            }
            if ( rows > 1 ) {
                currentItemMargin = this.getItemMargin();
            }
            final double used = space * ( 1.0 - domainAxis.getLowerMargin() - domainAxis.getUpperMargin() - categoryMargin - currentItemMargin );
            if ( rows * columns > 0 ) {
                state.setBarWidth ( Math.min ( used / ( rows * columns ), maxWidth ) );
            } else {
                state.setBarWidth ( Math.min ( used, maxWidth ) );
            }
        }
    }
    protected double calculateBarW0 ( final CategoryPlot plot, final PlotOrientation orientation, final Rectangle2D dataArea, final CategoryAxis domainAxis, final CategoryItemRendererState state, final int row, final int column ) {
        double space;
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            space = dataArea.getHeight();
        } else {
            space = dataArea.getWidth();
        }
        double barW0 = domainAxis.getCategoryStart ( column, this.getColumnCount(), dataArea, plot.getDomainAxisEdge() );
        final int seriesCount = ( state.getVisibleSeriesCount() >= 0 ) ? state.getVisibleSeriesCount() : this.getRowCount();
        final int categoryCount = this.getColumnCount();
        if ( seriesCount > 1 ) {
            final double seriesGap = space * this.getItemMargin() / ( categoryCount * ( seriesCount - 1 ) );
            final double seriesW = this.calculateSeriesWidth ( space, domainAxis, categoryCount, seriesCount );
            barW0 = barW0 + row * ( seriesW + seriesGap ) + seriesW / 2.0 - state.getBarWidth() / 2.0;
        } else {
            barW0 = domainAxis.getCategoryMiddle ( column, this.getColumnCount(), dataArea, plot.getDomainAxisEdge() ) - state.getBarWidth() / 2.0;
        }
        return barW0;
    }
    protected double[] calculateBarL0L1 ( final double value ) {
        final double lclip = this.getLowerClip();
        final double uclip = this.getUpperClip();
        double barLow = Math.min ( this.base, value );
        double barHigh = Math.max ( this.base, value );
        if ( barHigh < lclip ) {
            return null;
        }
        if ( barLow > uclip ) {
            return null;
        }
        barLow = Math.max ( barLow, lclip );
        barHigh = Math.min ( barHigh, uclip );
        return new double[] { barLow, barHigh };
    }
    public Range findRangeBounds ( final CategoryDataset dataset, final boolean includeInterval ) {
        if ( dataset == null ) {
            return null;
        }
        Range result = super.findRangeBounds ( dataset, includeInterval );
        if ( result != null && this.includeBaseInRange ) {
            result = Range.expandToInclude ( result, this.base );
        }
        return result;
    }
    @Override
    public LegendItem getLegendItem ( final int datasetIndex, final int series ) {
        final CategoryPlot cp = this.getPlot();
        if ( cp == null ) {
            return null;
        }
        if ( !this.isSeriesVisible ( series ) || !this.isSeriesVisibleInLegend ( series ) ) {
            return null;
        }
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
        final Paint outlinePaint = this.lookupSeriesOutlinePaint ( series );
        final Stroke outlineStroke = this.lookupSeriesOutlineStroke ( series );
        final LegendItem result = new LegendItem ( label, description, toolTipText, urlText, true, shape, true, paint, this.isDrawBarOutline(), outlinePaint, outlineStroke, false, new Line2D.Float(), new BasicStroke ( 1.0f ), Color.black );
        result.setLabelFont ( this.lookupLegendTextFont ( series ) );
        final Paint labelPaint = this.lookupLegendTextPaint ( series );
        if ( labelPaint != null ) {
            result.setLabelPaint ( labelPaint );
        }
        result.setDataset ( dataset );
        result.setDatasetIndex ( datasetIndex );
        result.setSeriesKey ( dataset.getRowKey ( series ) );
        result.setSeriesIndex ( series );
        if ( this.gradientPaintTransformer != null ) {
            result.setFillPaintTransformer ( this.gradientPaintTransformer );
        }
        return result;
    }
    public void drawItem ( final Graphics2D g2, final CategoryItemRendererState state, final Rectangle2D dataArea, final CategoryPlot plot, final CategoryAxis domainAxis, final ValueAxis rangeAxis, final CategoryDataset dataset, final int row, final int column, final int pass ) {
        final int visibleRow = state.getVisibleSeriesIndex ( row );
        if ( visibleRow < 0 ) {
            return;
        }
        final Number dataValue = dataset.getValue ( row, column );
        if ( dataValue == null ) {
            return;
        }
        final double value = dataValue.doubleValue();
        final PlotOrientation orientation = plot.getOrientation();
        final double barW0 = this.calculateBarW0 ( plot, orientation, dataArea, domainAxis, state, visibleRow, column );
        final double[] barL0L1 = this.calculateBarL0L1 ( value );
        if ( barL0L1 == null ) {
            return;
        }
        final RectangleEdge edge = plot.getRangeAxisEdge();
        final double transL0 = rangeAxis.valueToJava2D ( barL0L1[0], dataArea, edge );
        final double transL = rangeAxis.valueToJava2D ( barL0L1[1], dataArea, edge );
        final boolean positive = value >= this.base;
        final boolean inverted = rangeAxis.isInverted();
        final double barL0 = Math.min ( transL0, transL );
        final double barLength = Math.abs ( transL - transL0 );
        double barLengthAdj = 0.0;
        if ( barLength > 0.0 && barLength < this.getMinimumBarLength() ) {
            barLengthAdj = this.getMinimumBarLength() - barLength;
        }
        double barL0Adj = 0.0;
        RectangleEdge barBase;
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            if ( ( positive && inverted ) || ( !positive && !inverted ) ) {
                barL0Adj = barLengthAdj;
                barBase = RectangleEdge.RIGHT;
            } else {
                barBase = RectangleEdge.LEFT;
            }
        } else if ( ( positive && !inverted ) || ( !positive && inverted ) ) {
            barL0Adj = barLengthAdj;
            barBase = RectangleEdge.BOTTOM;
        } else {
            barBase = RectangleEdge.TOP;
        }
        Rectangle2D bar;
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            bar = new Rectangle2D.Double ( barL0 - barL0Adj, barW0, barLength + barLengthAdj, state.getBarWidth() );
        } else {
            bar = new Rectangle2D.Double ( barW0, barL0 - barL0Adj, state.getBarWidth(), barLength + barLengthAdj );
        }
        if ( state.getElementHinting() ) {
            final KeyedValues2DItemKey key = new KeyedValues2DItemKey ( ( R ) dataset.getRowKey ( row ), ( C ) dataset.getColumnKey ( column ) );
            this.beginElementGroup ( g2, key );
        }
        if ( this.getShadowsVisible() ) {
            this.barPainter.paintBarShadow ( g2, this, row, column, bar, barBase, true );
        }
        this.barPainter.paintBar ( g2, this, row, column, bar, barBase );
        if ( state.getElementHinting() ) {
            this.endElementGroup ( g2 );
        }
        final CategoryItemLabelGenerator generator = this.getItemLabelGenerator ( row, column );
        if ( generator != null && this.isItemLabelVisible ( row, column ) ) {
            this.drawItemLabel ( g2, dataset, row, column, plot, generator, bar, value < 0.0 );
        }
        final int datasetIndex = plot.indexOf ( dataset );
        this.updateCrosshairValues ( state.getCrosshairState(), dataset.getRowKey ( row ), dataset.getColumnKey ( column ), value, datasetIndex, barW0, barL0, orientation );
        final EntityCollection entities = state.getEntityCollection();
        if ( entities != null ) {
            this.addItemEntity ( entities, dataset, row, column, bar );
        }
    }
    protected double calculateSeriesWidth ( final double space, final CategoryAxis axis, final int categories, final int series ) {
        double factor = 1.0 - this.getItemMargin() - axis.getLowerMargin() - axis.getUpperMargin();
        if ( categories > 1 ) {
            factor -= axis.getCategoryMargin();
        }
        return space * factor / ( categories * series );
    }
    protected void drawItemLabel ( final Graphics2D g2, final CategoryDataset data, final int row, final int column, final CategoryPlot plot, final CategoryItemLabelGenerator generator, final Rectangle2D bar, final boolean negative ) {
        final String label = generator.generateLabel ( data, row, column );
        if ( label == null ) {
            return;
        }
        final Font labelFont = this.getItemLabelFont ( row, column );
        g2.setFont ( labelFont );
        final Paint paint = this.getItemLabelPaint ( row, column );
        g2.setPaint ( paint );
        ItemLabelPosition position;
        if ( !negative ) {
            position = this.getPositiveItemLabelPosition ( row, column );
        } else {
            position = this.getNegativeItemLabelPosition ( row, column );
        }
        Point2D anchorPoint = this.calculateLabelAnchorPoint ( position.getItemLabelAnchor(), bar, plot.getOrientation() );
        if ( this.isInternalAnchor ( position.getItemLabelAnchor() ) ) {
            final Shape bounds = TextUtilities.calculateRotatedStringBounds ( label, g2, ( float ) anchorPoint.getX(), ( float ) anchorPoint.getY(), position.getTextAnchor(), position.getAngle(), position.getRotationAnchor() );
            if ( bounds != null && !bar.contains ( bounds.getBounds2D() ) ) {
                if ( !negative ) {
                    position = this.getPositiveItemLabelPositionFallback();
                } else {
                    position = this.getNegativeItemLabelPositionFallback();
                }
                if ( position != null ) {
                    anchorPoint = this.calculateLabelAnchorPoint ( position.getItemLabelAnchor(), bar, plot.getOrientation() );
                }
            }
        }
        if ( position != null ) {
            TextUtilities.drawRotatedString ( label, g2, ( float ) anchorPoint.getX(), ( float ) anchorPoint.getY(), position.getTextAnchor(), position.getAngle(), position.getRotationAnchor() );
        }
    }
    private Point2D calculateLabelAnchorPoint ( final ItemLabelAnchor anchor, final Rectangle2D bar, final PlotOrientation orientation ) {
        Point2D result = null;
        final double offset = this.getItemLabelAnchorOffset();
        final double x0 = bar.getX() - offset;
        final double x = bar.getX();
        final double x2 = bar.getX() + offset;
        final double x3 = bar.getCenterX();
        final double x4 = bar.getMaxX() - offset;
        final double x5 = bar.getMaxX();
        final double x6 = bar.getMaxX() + offset;
        final double y0 = bar.getMaxY() + offset;
        final double y = bar.getMaxY();
        final double y2 = bar.getMaxY() - offset;
        final double y3 = bar.getCenterY();
        final double y4 = bar.getMinY() + offset;
        final double y5 = bar.getMinY();
        final double y6 = bar.getMinY() - offset;
        if ( anchor == ItemLabelAnchor.CENTER ) {
            result = new Point2D.Double ( x3, y3 );
        } else if ( anchor == ItemLabelAnchor.INSIDE1 ) {
            result = new Point2D.Double ( x4, y4 );
        } else if ( anchor == ItemLabelAnchor.INSIDE2 ) {
            result = new Point2D.Double ( x4, y4 );
        } else if ( anchor == ItemLabelAnchor.INSIDE3 ) {
            result = new Point2D.Double ( x4, y3 );
        } else if ( anchor == ItemLabelAnchor.INSIDE4 ) {
            result = new Point2D.Double ( x4, y2 );
        } else if ( anchor == ItemLabelAnchor.INSIDE5 ) {
            result = new Point2D.Double ( x4, y2 );
        } else if ( anchor == ItemLabelAnchor.INSIDE6 ) {
            result = new Point2D.Double ( x3, y2 );
        } else if ( anchor == ItemLabelAnchor.INSIDE7 ) {
            result = new Point2D.Double ( x2, y2 );
        } else if ( anchor == ItemLabelAnchor.INSIDE8 ) {
            result = new Point2D.Double ( x2, y2 );
        } else if ( anchor == ItemLabelAnchor.INSIDE9 ) {
            result = new Point2D.Double ( x2, y3 );
        } else if ( anchor == ItemLabelAnchor.INSIDE10 ) {
            result = new Point2D.Double ( x2, y4 );
        } else if ( anchor == ItemLabelAnchor.INSIDE11 ) {
            result = new Point2D.Double ( x2, y4 );
        } else if ( anchor == ItemLabelAnchor.INSIDE12 ) {
            result = new Point2D.Double ( x3, y4 );
        } else if ( anchor == ItemLabelAnchor.OUTSIDE1 ) {
            result = new Point2D.Double ( x5, y6 );
        } else if ( anchor == ItemLabelAnchor.OUTSIDE2 ) {
            result = new Point2D.Double ( x6, y5 );
        } else if ( anchor == ItemLabelAnchor.OUTSIDE3 ) {
            result = new Point2D.Double ( x6, y3 );
        } else if ( anchor == ItemLabelAnchor.OUTSIDE4 ) {
            result = new Point2D.Double ( x6, y );
        } else if ( anchor == ItemLabelAnchor.OUTSIDE5 ) {
            result = new Point2D.Double ( x5, y0 );
        } else if ( anchor == ItemLabelAnchor.OUTSIDE6 ) {
            result = new Point2D.Double ( x3, y0 );
        } else if ( anchor == ItemLabelAnchor.OUTSIDE7 ) {
            result = new Point2D.Double ( x, y0 );
        } else if ( anchor == ItemLabelAnchor.OUTSIDE8 ) {
            result = new Point2D.Double ( x0, y );
        } else if ( anchor == ItemLabelAnchor.OUTSIDE9 ) {
            result = new Point2D.Double ( x0, y3 );
        } else if ( anchor == ItemLabelAnchor.OUTSIDE10 ) {
            result = new Point2D.Double ( x0, y5 );
        } else if ( anchor == ItemLabelAnchor.OUTSIDE11 ) {
            result = new Point2D.Double ( x, y6 );
        } else if ( anchor == ItemLabelAnchor.OUTSIDE12 ) {
            result = new Point2D.Double ( x3, y6 );
        }
        return result;
    }
    private boolean isInternalAnchor ( final ItemLabelAnchor anchor ) {
        return anchor == ItemLabelAnchor.CENTER || anchor == ItemLabelAnchor.INSIDE1 || anchor == ItemLabelAnchor.INSIDE2 || anchor == ItemLabelAnchor.INSIDE3 || anchor == ItemLabelAnchor.INSIDE4 || anchor == ItemLabelAnchor.INSIDE5 || anchor == ItemLabelAnchor.INSIDE6 || anchor == ItemLabelAnchor.INSIDE7 || anchor == ItemLabelAnchor.INSIDE8 || anchor == ItemLabelAnchor.INSIDE9 || anchor == ItemLabelAnchor.INSIDE10 || anchor == ItemLabelAnchor.INSIDE11 || anchor == ItemLabelAnchor.INSIDE12;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof BarRenderer ) ) {
            return false;
        }
        final BarRenderer that = ( BarRenderer ) obj;
        return this.base == that.base && this.itemMargin == that.itemMargin && this.drawBarOutline == that.drawBarOutline && this.maximumBarWidth == that.maximumBarWidth && this.minimumBarLength == that.minimumBarLength && ObjectUtilities.equal ( ( Object ) this.gradientPaintTransformer, ( Object ) that.gradientPaintTransformer ) && ObjectUtilities.equal ( ( Object ) this.positiveItemLabelPositionFallback, ( Object ) that.positiveItemLabelPositionFallback ) && ObjectUtilities.equal ( ( Object ) this.negativeItemLabelPositionFallback, ( Object ) that.negativeItemLabelPositionFallback ) && this.barPainter.equals ( that.barPainter ) && this.shadowsVisible == that.shadowsVisible && PaintUtilities.equal ( this.shadowPaint, that.shadowPaint ) && this.shadowXOffset == that.shadowXOffset && this.shadowYOffset == that.shadowYOffset && super.equals ( obj );
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.shadowPaint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.shadowPaint = SerialUtilities.readPaint ( stream );
    }
    static {
        BarRenderer.defaultBarPainter = new GradientBarPainter();
        BarRenderer.defaultShadowsVisible = true;
    }
}
