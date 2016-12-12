package org.jfree.chart.renderer.xy;
import java.io.ObjectOutputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectInputStream;
import org.jfree.util.ShapeUtilities;
import org.jfree.util.ObjectUtilities;
import org.jfree.data.Range;
import org.jfree.chart.labels.ItemLabelAnchor;
import java.awt.geom.Point2D;
import java.awt.Font;
import org.jfree.text.TextUtilities;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import java.awt.geom.RectangularShape;
import org.jfree.ui.RectangleEdge;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.chart.plot.CrosshairState;
import java.awt.Stroke;
import java.awt.Paint;
import org.jfree.chart.labels.XYSeriesLabelGenerator;
import org.jfree.data.general.Dataset;
import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.plot.XYPlot;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import org.jfree.ui.StandardGradientPaintTransformer;
import org.jfree.chart.util.ParamChecks;
import org.jfree.chart.labels.ItemLabelPosition;
import java.awt.Shape;
import org.jfree.ui.GradientPaintTransformer;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class XYBarRenderer extends AbstractXYItemRenderer implements XYItemRenderer, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 770559577251370036L;
    private static XYBarPainter defaultBarPainter;
    private static boolean defaultShadowsVisible;
    private double base;
    private boolean useYInterval;
    private double margin;
    private boolean drawBarOutline;
    private GradientPaintTransformer gradientPaintTransformer;
    private transient Shape legendBar;
    private ItemLabelPosition positiveItemLabelPositionFallback;
    private ItemLabelPosition negativeItemLabelPositionFallback;
    private XYBarPainter barPainter;
    private boolean shadowsVisible;
    private double shadowXOffset;
    private double shadowYOffset;
    private double barAlignmentFactor;
    public static XYBarPainter getDefaultBarPainter() {
        return XYBarRenderer.defaultBarPainter;
    }
    public static void setDefaultBarPainter ( final XYBarPainter painter ) {
        ParamChecks.nullNotPermitted ( painter, "painter" );
        XYBarRenderer.defaultBarPainter = painter;
    }
    public static boolean getDefaultShadowsVisible() {
        return XYBarRenderer.defaultShadowsVisible;
    }
    public static void setDefaultShadowsVisible ( final boolean visible ) {
        XYBarRenderer.defaultShadowsVisible = visible;
    }
    public XYBarRenderer() {
        this ( 0.0 );
    }
    public XYBarRenderer ( final double margin ) {
        this.margin = margin;
        this.base = 0.0;
        this.useYInterval = false;
        this.gradientPaintTransformer = ( GradientPaintTransformer ) new StandardGradientPaintTransformer();
        this.drawBarOutline = false;
        this.legendBar = new Rectangle2D.Double ( -3.0, -5.0, 6.0, 10.0 );
        this.barPainter = getDefaultBarPainter();
        this.shadowsVisible = getDefaultShadowsVisible();
        this.shadowXOffset = 4.0;
        this.shadowYOffset = 4.0;
        this.barAlignmentFactor = -1.0;
    }
    public double getBase() {
        return this.base;
    }
    public void setBase ( final double base ) {
        this.base = base;
        this.fireChangeEvent();
    }
    public boolean getUseYInterval() {
        return this.useYInterval;
    }
    public void setUseYInterval ( final boolean use ) {
        if ( this.useYInterval != use ) {
            this.useYInterval = use;
            this.fireChangeEvent();
        }
    }
    public double getMargin() {
        return this.margin;
    }
    public void setMargin ( final double margin ) {
        this.margin = margin;
        this.fireChangeEvent();
    }
    public boolean isDrawBarOutline() {
        return this.drawBarOutline;
    }
    public void setDrawBarOutline ( final boolean draw ) {
        this.drawBarOutline = draw;
        this.fireChangeEvent();
    }
    public GradientPaintTransformer getGradientPaintTransformer() {
        return this.gradientPaintTransformer;
    }
    public void setGradientPaintTransformer ( final GradientPaintTransformer transformer ) {
        this.gradientPaintTransformer = transformer;
        this.fireChangeEvent();
    }
    public Shape getLegendBar() {
        return this.legendBar;
    }
    public void setLegendBar ( final Shape bar ) {
        ParamChecks.nullNotPermitted ( bar, "bar" );
        this.legendBar = bar;
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
    public XYBarPainter getBarPainter() {
        return this.barPainter;
    }
    public void setBarPainter ( final XYBarPainter painter ) {
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
    public double getBarAlignmentFactor() {
        return this.barAlignmentFactor;
    }
    public void setBarAlignmentFactor ( final double factor ) {
        this.barAlignmentFactor = factor;
        this.fireChangeEvent();
    }
    @Override
    public XYItemRendererState initialise ( final Graphics2D g2, final Rectangle2D dataArea, final XYPlot plot, final XYDataset dataset, final PlotRenderingInfo info ) {
        final XYBarRendererState state = new XYBarRendererState ( info );
        final ValueAxis rangeAxis = plot.getRangeAxisForDataset ( plot.indexOf ( dataset ) );
        state.setG2Base ( rangeAxis.valueToJava2D ( this.base, dataArea, plot.getRangeAxisEdge() ) );
        return state;
    }
    @Override
    public LegendItem getLegendItem ( final int datasetIndex, final int series ) {
        final XYPlot xyplot = this.getPlot();
        if ( xyplot == null ) {
            return null;
        }
        final XYDataset dataset = xyplot.getDataset ( datasetIndex );
        if ( dataset == null ) {
            return null;
        }
        final XYSeriesLabelGenerator lg = this.getLegendItemLabelGenerator();
        final String description;
        final String label = description = lg.generateLabel ( dataset, series );
        String toolTipText = null;
        if ( this.getLegendItemToolTipGenerator() != null ) {
            toolTipText = this.getLegendItemToolTipGenerator().generateLabel ( dataset, series );
        }
        String urlText = null;
        if ( this.getLegendItemURLGenerator() != null ) {
            urlText = this.getLegendItemURLGenerator().generateLabel ( dataset, series );
        }
        final Shape shape = this.legendBar;
        final Paint paint = this.lookupSeriesPaint ( series );
        final Paint outlinePaint = this.lookupSeriesOutlinePaint ( series );
        final Stroke outlineStroke = this.lookupSeriesOutlineStroke ( series );
        LegendItem result;
        if ( this.drawBarOutline ) {
            result = new LegendItem ( label, description, toolTipText, urlText, shape, paint, outlineStroke, outlinePaint );
        } else {
            result = new LegendItem ( label, description, toolTipText, urlText, shape, paint );
        }
        result.setLabelFont ( this.lookupLegendTextFont ( series ) );
        final Paint labelPaint = this.lookupLegendTextPaint ( series );
        if ( labelPaint != null ) {
            result.setLabelPaint ( labelPaint );
        }
        result.setDataset ( dataset );
        result.setDatasetIndex ( datasetIndex );
        result.setSeriesKey ( dataset.getSeriesKey ( series ) );
        result.setSeriesIndex ( series );
        if ( this.getGradientPaintTransformer() != null ) {
            result.setFillPaintTransformer ( this.getGradientPaintTransformer() );
        }
        return result;
    }
    @Override
    public void drawItem ( final Graphics2D g2, final XYItemRendererState state, final Rectangle2D dataArea, final PlotRenderingInfo info, final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis, final XYDataset dataset, final int series, final int item, final CrosshairState crosshairState, final int pass ) {
        if ( !this.getItemVisible ( series, item ) ) {
            return;
        }
        final IntervalXYDataset intervalDataset = ( IntervalXYDataset ) dataset;
        double value0;
        double value;
        if ( this.useYInterval ) {
            value0 = intervalDataset.getStartYValue ( series, item );
            value = intervalDataset.getEndYValue ( series, item );
        } else {
            value0 = this.base;
            value = intervalDataset.getYValue ( series, item );
        }
        if ( Double.isNaN ( value0 ) || Double.isNaN ( value ) ) {
            return;
        }
        if ( value0 <= value ) {
            if ( !rangeAxis.getRange().intersects ( value0, value ) ) {
                return;
            }
        } else if ( !rangeAxis.getRange().intersects ( value, value0 ) ) {
            return;
        }
        final double translatedValue0 = rangeAxis.valueToJava2D ( value0, dataArea, plot.getRangeAxisEdge() );
        final double translatedValue = rangeAxis.valueToJava2D ( value, dataArea, plot.getRangeAxisEdge() );
        double bottom = Math.min ( translatedValue0, translatedValue );
        double top = Math.max ( translatedValue0, translatedValue );
        double startX = intervalDataset.getStartXValue ( series, item );
        if ( Double.isNaN ( startX ) ) {
            return;
        }
        double endX = intervalDataset.getEndXValue ( series, item );
        if ( Double.isNaN ( endX ) ) {
            return;
        }
        if ( startX <= endX ) {
            if ( !domainAxis.getRange().intersects ( startX, endX ) ) {
                return;
            }
        } else if ( !domainAxis.getRange().intersects ( endX, startX ) ) {
            return;
        }
        if ( this.barAlignmentFactor >= 0.0 && this.barAlignmentFactor <= 1.0 ) {
            final double x = intervalDataset.getXValue ( series, item );
            final double interval = endX - startX;
            startX = x - interval * this.barAlignmentFactor;
            endX = startX + interval;
        }
        final RectangleEdge location = plot.getDomainAxisEdge();
        final double translatedStartX = domainAxis.valueToJava2D ( startX, dataArea, location );
        final double translatedEndX = domainAxis.valueToJava2D ( endX, dataArea, location );
        double translatedWidth = Math.max ( 1.0, Math.abs ( translatedEndX - translatedStartX ) );
        double left = Math.min ( translatedStartX, translatedEndX );
        if ( this.getMargin() > 0.0 ) {
            final double cut = translatedWidth * this.getMargin();
            translatedWidth -= cut;
            left += cut / 2.0;
        }
        Rectangle2D bar = null;
        final PlotOrientation orientation = plot.getOrientation();
        if ( orientation.isHorizontal() ) {
            bottom = Math.max ( bottom, dataArea.getMinX() );
            top = Math.min ( top, dataArea.getMaxX() );
            bar = new Rectangle2D.Double ( bottom, left, top - bottom, translatedWidth );
        } else if ( orientation.isVertical() ) {
            bottom = Math.max ( bottom, dataArea.getMinY() );
            top = Math.min ( top, dataArea.getMaxY() );
            bar = new Rectangle2D.Double ( left, bottom, translatedWidth, top - bottom );
        }
        final boolean positive = value > 0.0;
        final boolean inverted = rangeAxis.isInverted();
        RectangleEdge barBase;
        if ( orientation.isHorizontal() ) {
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
        if ( state.getElementHinting() ) {
            this.beginElementGroup ( g2, dataset.getSeriesKey ( series ), item );
        }
        if ( this.getShadowsVisible() ) {
            this.barPainter.paintBarShadow ( g2, this, series, item, bar, barBase, !this.useYInterval );
        }
        this.barPainter.paintBar ( g2, this, series, item, bar, barBase );
        if ( state.getElementHinting() ) {
            this.endElementGroup ( g2 );
        }
        if ( this.isItemLabelVisible ( series, item ) ) {
            final XYItemLabelGenerator generator = this.getItemLabelGenerator ( series, item );
            this.drawItemLabel ( g2, dataset, series, item, plot, generator, bar, value < 0.0 );
        }
        final double x2 = ( startX + endX ) / 2.0;
        final double y1 = dataset.getYValue ( series, item );
        final double transX1 = domainAxis.valueToJava2D ( x2, dataArea, location );
        final double transY1 = rangeAxis.valueToJava2D ( y1, dataArea, plot.getRangeAxisEdge() );
        final int domainAxisIndex = plot.getDomainAxisIndex ( domainAxis );
        final int rangeAxisIndex = plot.getRangeAxisIndex ( rangeAxis );
        this.updateCrosshairValues ( crosshairState, x2, y1, domainAxisIndex, rangeAxisIndex, transX1, transY1, plot.getOrientation() );
        final EntityCollection entities = state.getEntityCollection();
        if ( entities != null ) {
            this.addEntity ( entities, bar, dataset, series, item, 0.0, 0.0 );
        }
    }
    protected void drawItemLabel ( final Graphics2D g2, final XYDataset dataset, final int series, final int item, final XYPlot plot, final XYItemLabelGenerator generator, final Rectangle2D bar, final boolean negative ) {
        if ( generator == null ) {
            return;
        }
        final String label = generator.generateLabel ( dataset, series, item );
        if ( label == null ) {
            return;
        }
        final Font labelFont = this.getItemLabelFont ( series, item );
        g2.setFont ( labelFont );
        final Paint paint = this.getItemLabelPaint ( series, item );
        g2.setPaint ( paint );
        ItemLabelPosition position;
        if ( !negative ) {
            position = this.getPositiveItemLabelPosition ( series, item );
        } else {
            position = this.getNegativeItemLabelPosition ( series, item );
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
    public Range findDomainBounds ( final XYDataset dataset ) {
        return this.findDomainBounds ( dataset, true );
    }
    @Override
    public Range findRangeBounds ( final XYDataset dataset ) {
        return this.findRangeBounds ( dataset, this.useYInterval );
    }
    public Object clone() throws CloneNotSupportedException {
        final XYBarRenderer result = ( XYBarRenderer ) super.clone();
        if ( this.gradientPaintTransformer != null ) {
            result.gradientPaintTransformer = ( GradientPaintTransformer ) ObjectUtilities.clone ( ( Object ) this.gradientPaintTransformer );
        }
        result.legendBar = ShapeUtilities.clone ( this.legendBar );
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof XYBarRenderer ) ) {
            return false;
        }
        final XYBarRenderer that = ( XYBarRenderer ) obj;
        return this.base == that.base && this.drawBarOutline == that.drawBarOutline && this.margin == that.margin && this.useYInterval == that.useYInterval && ObjectUtilities.equal ( ( Object ) this.gradientPaintTransformer, ( Object ) that.gradientPaintTransformer ) && ShapeUtilities.equal ( this.legendBar, that.legendBar ) && ObjectUtilities.equal ( ( Object ) this.positiveItemLabelPositionFallback, ( Object ) that.positiveItemLabelPositionFallback ) && ObjectUtilities.equal ( ( Object ) this.negativeItemLabelPositionFallback, ( Object ) that.negativeItemLabelPositionFallback ) && this.barPainter.equals ( that.barPainter ) && this.shadowsVisible == that.shadowsVisible && this.shadowXOffset == that.shadowXOffset && this.shadowYOffset == that.shadowYOffset && this.barAlignmentFactor == that.barAlignmentFactor && super.equals ( obj );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.legendBar = SerialUtilities.readShape ( stream );
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeShape ( this.legendBar, stream );
    }
    static {
        XYBarRenderer.defaultBarPainter = new GradientXYBarPainter();
        XYBarRenderer.defaultShadowsVisible = true;
    }
    protected class XYBarRendererState extends XYItemRendererState {
        private double g2Base;
        public XYBarRendererState ( final PlotRenderingInfo info ) {
            super ( info );
        }
        public double getG2Base() {
            return this.g2Base;
        }
        public void setG2Base ( final double value ) {
            this.g2Base = value;
        }
    }
}
