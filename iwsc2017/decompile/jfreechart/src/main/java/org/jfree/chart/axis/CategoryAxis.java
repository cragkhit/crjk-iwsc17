package org.jfree.chart.axis;
import org.jfree.util.PaintUtilities;
import java.util.Set;
import org.jfree.io.SerialUtilities;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.jfree.util.ObjectUtilities;
import org.jfree.ui.Size2D;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.ShapeUtilities;
import org.jfree.text.TextMeasurer;
import org.jfree.text.TextUtilities;
import org.jfree.text.G2TextMeasurer;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import org.jfree.chart.plot.CategoryPlot;
import java.util.ArrayList;
import org.jfree.chart.entity.EntityCollection;
import java.awt.Shape;
import org.jfree.text.TextBlock;
import java.awt.geom.Point2D;
import java.util.Iterator;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.CategoryLabelEntity;
import org.jfree.ui.RectangleAnchor;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.Plot;
import java.awt.Graphics2D;
import org.jfree.data.category.CategoryDataset;
import java.util.List;
import org.jfree.ui.RectangleEdge;
import java.awt.geom.Rectangle2D;
import java.awt.Paint;
import java.awt.Font;
import org.jfree.chart.util.ParamChecks;
import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;
public class CategoryAxis extends Axis implements Cloneable, Serializable {
    private static final long serialVersionUID = 5886554608114265863L;
    public static final double DEFAULT_AXIS_MARGIN = 0.05;
    public static final double DEFAULT_CATEGORY_MARGIN = 0.2;
    private double lowerMargin;
    private double upperMargin;
    private double categoryMargin;
    private int maximumCategoryLabelLines;
    private float maximumCategoryLabelWidthRatio;
    private int categoryLabelPositionOffset;
    private CategoryLabelPositions categoryLabelPositions;
    private Map tickLabelFontMap;
    private transient Map tickLabelPaintMap;
    private Map categoryLabelToolTips;
    private Map categoryLabelURLs;
    public CategoryAxis() {
        this ( null );
    }
    public CategoryAxis ( final String label ) {
        super ( label );
        this.lowerMargin = 0.05;
        this.upperMargin = 0.05;
        this.categoryMargin = 0.2;
        this.maximumCategoryLabelLines = 1;
        this.maximumCategoryLabelWidthRatio = 0.0f;
        this.categoryLabelPositionOffset = 4;
        this.categoryLabelPositions = CategoryLabelPositions.STANDARD;
        this.tickLabelFontMap = new HashMap();
        this.tickLabelPaintMap = new HashMap();
        this.categoryLabelToolTips = new HashMap();
        this.categoryLabelURLs = new HashMap();
    }
    public double getLowerMargin() {
        return this.lowerMargin;
    }
    public void setLowerMargin ( final double margin ) {
        this.lowerMargin = margin;
        this.fireChangeEvent();
    }
    public double getUpperMargin() {
        return this.upperMargin;
    }
    public void setUpperMargin ( final double margin ) {
        this.upperMargin = margin;
        this.fireChangeEvent();
    }
    public double getCategoryMargin() {
        return this.categoryMargin;
    }
    public void setCategoryMargin ( final double margin ) {
        this.categoryMargin = margin;
        this.fireChangeEvent();
    }
    public int getMaximumCategoryLabelLines() {
        return this.maximumCategoryLabelLines;
    }
    public void setMaximumCategoryLabelLines ( final int lines ) {
        this.maximumCategoryLabelLines = lines;
        this.fireChangeEvent();
    }
    public float getMaximumCategoryLabelWidthRatio() {
        return this.maximumCategoryLabelWidthRatio;
    }
    public void setMaximumCategoryLabelWidthRatio ( final float ratio ) {
        this.maximumCategoryLabelWidthRatio = ratio;
        this.fireChangeEvent();
    }
    public int getCategoryLabelPositionOffset() {
        return this.categoryLabelPositionOffset;
    }
    public void setCategoryLabelPositionOffset ( final int offset ) {
        this.categoryLabelPositionOffset = offset;
        this.fireChangeEvent();
    }
    public CategoryLabelPositions getCategoryLabelPositions() {
        return this.categoryLabelPositions;
    }
    public void setCategoryLabelPositions ( final CategoryLabelPositions positions ) {
        ParamChecks.nullNotPermitted ( positions, "positions" );
        this.categoryLabelPositions = positions;
        this.fireChangeEvent();
    }
    public Font getTickLabelFont ( final Comparable category ) {
        ParamChecks.nullNotPermitted ( category, "category" );
        Font result = this.tickLabelFontMap.get ( category );
        if ( result == null ) {
            result = this.getTickLabelFont();
        }
        return result;
    }
    public void setTickLabelFont ( final Comparable category, final Font font ) {
        ParamChecks.nullNotPermitted ( category, "category" );
        if ( font == null ) {
            this.tickLabelFontMap.remove ( category );
        } else {
            this.tickLabelFontMap.put ( category, font );
        }
        this.fireChangeEvent();
    }
    public Paint getTickLabelPaint ( final Comparable category ) {
        ParamChecks.nullNotPermitted ( category, "category" );
        Paint result = this.tickLabelPaintMap.get ( category );
        if ( result == null ) {
            result = this.getTickLabelPaint();
        }
        return result;
    }
    public void setTickLabelPaint ( final Comparable category, final Paint paint ) {
        ParamChecks.nullNotPermitted ( category, "category" );
        if ( paint == null ) {
            this.tickLabelPaintMap.remove ( category );
        } else {
            this.tickLabelPaintMap.put ( category, paint );
        }
        this.fireChangeEvent();
    }
    public void addCategoryLabelToolTip ( final Comparable category, final String tooltip ) {
        ParamChecks.nullNotPermitted ( category, "category" );
        this.categoryLabelToolTips.put ( category, tooltip );
        this.fireChangeEvent();
    }
    public String getCategoryLabelToolTip ( final Comparable category ) {
        ParamChecks.nullNotPermitted ( category, "category" );
        return this.categoryLabelToolTips.get ( category );
    }
    public void removeCategoryLabelToolTip ( final Comparable category ) {
        ParamChecks.nullNotPermitted ( category, "category" );
        if ( this.categoryLabelToolTips.remove ( category ) != null ) {
            this.fireChangeEvent();
        }
    }
    public void clearCategoryLabelToolTips() {
        this.categoryLabelToolTips.clear();
        this.fireChangeEvent();
    }
    public void addCategoryLabelURL ( final Comparable category, final String url ) {
        ParamChecks.nullNotPermitted ( category, "category" );
        this.categoryLabelURLs.put ( category, url );
        this.fireChangeEvent();
    }
    public String getCategoryLabelURL ( final Comparable category ) {
        ParamChecks.nullNotPermitted ( category, "category" );
        return this.categoryLabelURLs.get ( category );
    }
    public void removeCategoryLabelURL ( final Comparable category ) {
        ParamChecks.nullNotPermitted ( category, "category" );
        if ( this.categoryLabelURLs.remove ( category ) != null ) {
            this.fireChangeEvent();
        }
    }
    public void clearCategoryLabelURLs() {
        this.categoryLabelURLs.clear();
        this.fireChangeEvent();
    }
    public double getCategoryJava2DCoordinate ( final CategoryAnchor anchor, final int category, final int categoryCount, final Rectangle2D area, final RectangleEdge edge ) {
        double result = 0.0;
        if ( anchor == CategoryAnchor.START ) {
            result = this.getCategoryStart ( category, categoryCount, area, edge );
        } else if ( anchor == CategoryAnchor.MIDDLE ) {
            result = this.getCategoryMiddle ( category, categoryCount, area, edge );
        } else if ( anchor == CategoryAnchor.END ) {
            result = this.getCategoryEnd ( category, categoryCount, area, edge );
        }
        return result;
    }
    public double getCategoryStart ( final int category, final int categoryCount, final Rectangle2D area, final RectangleEdge edge ) {
        double result = 0.0;
        if ( edge == RectangleEdge.TOP || edge == RectangleEdge.BOTTOM ) {
            result = area.getX() + area.getWidth() * this.getLowerMargin();
        } else if ( edge == RectangleEdge.LEFT || edge == RectangleEdge.RIGHT ) {
            result = area.getMinY() + area.getHeight() * this.getLowerMargin();
        }
        final double categorySize = this.calculateCategorySize ( categoryCount, area, edge );
        final double categoryGapWidth = this.calculateCategoryGapSize ( categoryCount, area, edge );
        result += category * ( categorySize + categoryGapWidth );
        return result;
    }
    public double getCategoryMiddle ( final int category, final int categoryCount, final Rectangle2D area, final RectangleEdge edge ) {
        if ( category < 0 || category >= categoryCount ) {
            throw new IllegalArgumentException ( "Invalid category index: " + category );
        }
        return this.getCategoryStart ( category, categoryCount, area, edge ) + this.calculateCategorySize ( categoryCount, area, edge ) / 2.0;
    }
    public double getCategoryEnd ( final int category, final int categoryCount, final Rectangle2D area, final RectangleEdge edge ) {
        return this.getCategoryStart ( category, categoryCount, area, edge ) + this.calculateCategorySize ( categoryCount, area, edge );
    }
    public double getCategoryMiddle ( final Comparable category, final List categories, final Rectangle2D area, final RectangleEdge edge ) {
        ParamChecks.nullNotPermitted ( categories, "categories" );
        final int categoryIndex = categories.indexOf ( category );
        final int categoryCount = categories.size();
        return this.getCategoryMiddle ( categoryIndex, categoryCount, area, edge );
    }
    public double getCategorySeriesMiddle ( final Comparable category, final Comparable seriesKey, final CategoryDataset dataset, final double itemMargin, final Rectangle2D area, final RectangleEdge edge ) {
        final int categoryIndex = dataset.getColumnIndex ( category );
        final int categoryCount = dataset.getColumnCount();
        final int seriesIndex = dataset.getRowIndex ( seriesKey );
        final int seriesCount = dataset.getRowCount();
        final double start = this.getCategoryStart ( categoryIndex, categoryCount, area, edge );
        final double end = this.getCategoryEnd ( categoryIndex, categoryCount, area, edge );
        final double width = end - start;
        if ( seriesCount == 1 ) {
            return start + width / 2.0;
        }
        final double gap = width * itemMargin / ( seriesCount - 1 );
        final double ww = width * ( 1.0 - itemMargin ) / seriesCount;
        return start + seriesIndex * ( ww + gap ) + ww / 2.0;
    }
    public double getCategorySeriesMiddle ( final int categoryIndex, final int categoryCount, final int seriesIndex, final int seriesCount, final double itemMargin, final Rectangle2D area, final RectangleEdge edge ) {
        final double start = this.getCategoryStart ( categoryIndex, categoryCount, area, edge );
        final double end = this.getCategoryEnd ( categoryIndex, categoryCount, area, edge );
        final double width = end - start;
        if ( seriesCount == 1 ) {
            return start + width / 2.0;
        }
        final double gap = width * itemMargin / ( seriesCount - 1 );
        final double ww = width * ( 1.0 - itemMargin ) / seriesCount;
        return start + seriesIndex * ( ww + gap ) + ww / 2.0;
    }
    protected double calculateCategorySize ( final int categoryCount, final Rectangle2D area, final RectangleEdge edge ) {
        double available = 0.0;
        if ( edge == RectangleEdge.TOP || edge == RectangleEdge.BOTTOM ) {
            available = area.getWidth();
        } else if ( edge == RectangleEdge.LEFT || edge == RectangleEdge.RIGHT ) {
            available = area.getHeight();
        }
        double result;
        if ( categoryCount > 1 ) {
            result = available * ( 1.0 - this.getLowerMargin() - this.getUpperMargin() - this.getCategoryMargin() );
            result /= categoryCount;
        } else {
            result = available * ( 1.0 - this.getLowerMargin() - this.getUpperMargin() );
        }
        return result;
    }
    protected double calculateCategoryGapSize ( final int categoryCount, final Rectangle2D area, final RectangleEdge edge ) {
        double result = 0.0;
        double available = 0.0;
        if ( edge == RectangleEdge.TOP || edge == RectangleEdge.BOTTOM ) {
            available = area.getWidth();
        } else if ( edge == RectangleEdge.LEFT || edge == RectangleEdge.RIGHT ) {
            available = area.getHeight();
        }
        if ( categoryCount > 1 ) {
            result = available * this.getCategoryMargin() / ( categoryCount - 1 );
        }
        return result;
    }
    @Override
    public AxisSpace reserveSpace ( final Graphics2D g2, final Plot plot, final Rectangle2D plotArea, final RectangleEdge edge, AxisSpace space ) {
        if ( space == null ) {
            space = new AxisSpace();
        }
        if ( !this.isVisible() ) {
            return space;
        }
        double tickLabelHeight = 0.0;
        double tickLabelWidth = 0.0;
        if ( this.isTickLabelsVisible() ) {
            g2.setFont ( this.getTickLabelFont() );
            final AxisState state = new AxisState();
            this.refreshTicks ( g2, state, plotArea, edge );
            if ( edge == RectangleEdge.TOP ) {
                tickLabelHeight = state.getMax();
            } else if ( edge == RectangleEdge.BOTTOM ) {
                tickLabelHeight = state.getMax();
            } else if ( edge == RectangleEdge.LEFT ) {
                tickLabelWidth = state.getMax();
            } else if ( edge == RectangleEdge.RIGHT ) {
                tickLabelWidth = state.getMax();
            }
        }
        final Rectangle2D labelEnclosure = this.getLabelEnclosure ( g2, edge );
        if ( RectangleEdge.isTopOrBottom ( edge ) ) {
            final double labelHeight = labelEnclosure.getHeight();
            space.add ( labelHeight + tickLabelHeight + this.categoryLabelPositionOffset, edge );
        } else if ( RectangleEdge.isLeftOrRight ( edge ) ) {
            final double labelWidth = labelEnclosure.getWidth();
            space.add ( labelWidth + tickLabelWidth + this.categoryLabelPositionOffset, edge );
        }
        return space;
    }
    @Override
    public void configure() {
    }
    @Override
    public AxisState draw ( final Graphics2D g2, final double cursor, final Rectangle2D plotArea, final Rectangle2D dataArea, final RectangleEdge edge, final PlotRenderingInfo plotState ) {
        if ( !this.isVisible() ) {
            return new AxisState ( cursor );
        }
        if ( this.isAxisLineVisible() ) {
            this.drawAxisLine ( g2, cursor, dataArea, edge );
        }
        AxisState state = new AxisState ( cursor );
        if ( this.isTickMarksVisible() ) {
            this.drawTickMarks ( g2, cursor, dataArea, edge, state );
        }
        this.createAndAddEntity ( cursor, state, dataArea, edge, plotState );
        state = this.drawCategoryLabels ( g2, plotArea, dataArea, edge, state, plotState );
        if ( this.getAttributedLabel() != null ) {
            state = this.drawAttributedLabel ( this.getAttributedLabel(), g2, plotArea, dataArea, edge, state );
        } else {
            state = this.drawLabel ( this.getLabel(), g2, plotArea, dataArea, edge, state );
        }
        return state;
    }
    protected AxisState drawCategoryLabels ( final Graphics2D g2, final Rectangle2D plotArea, final Rectangle2D dataArea, final RectangleEdge edge, final AxisState state, final PlotRenderingInfo plotState ) {
        ParamChecks.nullNotPermitted ( state, "state" );
        if ( !this.isTickLabelsVisible() ) {
            return state;
        }
        final List ticks = this.refreshTicks ( g2, state, plotArea, edge );
        state.setTicks ( ticks );
        int categoryIndex = 0;
        for ( final CategoryTick tick : ticks ) {
            g2.setFont ( this.getTickLabelFont ( tick.getCategory() ) );
            g2.setPaint ( this.getTickLabelPaint ( tick.getCategory() ) );
            final CategoryLabelPosition position = this.categoryLabelPositions.getLabelPosition ( edge );
            double x0 = 0.0;
            double x = 0.0;
            double y0 = 0.0;
            double y = 0.0;
            if ( edge == RectangleEdge.TOP ) {
                x0 = this.getCategoryStart ( categoryIndex, ticks.size(), dataArea, edge );
                x = this.getCategoryEnd ( categoryIndex, ticks.size(), dataArea, edge );
                y = state.getCursor() - this.categoryLabelPositionOffset;
                y0 = y - state.getMax();
            } else if ( edge == RectangleEdge.BOTTOM ) {
                x0 = this.getCategoryStart ( categoryIndex, ticks.size(), dataArea, edge );
                x = this.getCategoryEnd ( categoryIndex, ticks.size(), dataArea, edge );
                y0 = state.getCursor() + this.categoryLabelPositionOffset;
                y = y0 + state.getMax();
            } else if ( edge == RectangleEdge.LEFT ) {
                y0 = this.getCategoryStart ( categoryIndex, ticks.size(), dataArea, edge );
                y = this.getCategoryEnd ( categoryIndex, ticks.size(), dataArea, edge );
                x = state.getCursor() - this.categoryLabelPositionOffset;
                x0 = x - state.getMax();
            } else if ( edge == RectangleEdge.RIGHT ) {
                y0 = this.getCategoryStart ( categoryIndex, ticks.size(), dataArea, edge );
                y = this.getCategoryEnd ( categoryIndex, ticks.size(), dataArea, edge );
                x0 = state.getCursor() + this.categoryLabelPositionOffset;
                x = x0 - state.getMax();
            }
            final Rectangle2D area = new Rectangle2D.Double ( x0, y0, x - x0, y - y0 );
            final Point2D anchorPoint = RectangleAnchor.coordinates ( area, position.getCategoryAnchor() );
            final TextBlock block = tick.getLabel();
            block.draw ( g2, ( float ) anchorPoint.getX(), ( float ) anchorPoint.getY(), position.getLabelAnchor(), ( float ) anchorPoint.getX(), ( float ) anchorPoint.getY(), position.getAngle() );
            final Shape bounds = block.calculateBounds ( g2, ( float ) anchorPoint.getX(), ( float ) anchorPoint.getY(), position.getLabelAnchor(), ( float ) anchorPoint.getX(), ( float ) anchorPoint.getY(), position.getAngle() );
            if ( plotState != null && plotState.getOwner() != null ) {
                final EntityCollection entities = plotState.getOwner().getEntityCollection();
                if ( entities != null ) {
                    final String tooltip = this.getCategoryLabelToolTip ( tick.getCategory() );
                    final String url = this.getCategoryLabelURL ( tick.getCategory() );
                    entities.add ( new CategoryLabelEntity ( tick.getCategory(), bounds, tooltip, url ) );
                }
            }
            ++categoryIndex;
        }
        if ( edge.equals ( ( Object ) RectangleEdge.TOP ) ) {
            final double h = state.getMax() + this.categoryLabelPositionOffset;
            state.cursorUp ( h );
        } else if ( edge.equals ( ( Object ) RectangleEdge.BOTTOM ) ) {
            final double h = state.getMax() + this.categoryLabelPositionOffset;
            state.cursorDown ( h );
        } else if ( edge == RectangleEdge.LEFT ) {
            final double w = state.getMax() + this.categoryLabelPositionOffset;
            state.cursorLeft ( w );
        } else if ( edge == RectangleEdge.RIGHT ) {
            final double w = state.getMax() + this.categoryLabelPositionOffset;
            state.cursorRight ( w );
        }
        return state;
    }
    @Override
    public List refreshTicks ( final Graphics2D g2, final AxisState state, final Rectangle2D dataArea, final RectangleEdge edge ) {
        final List ticks = new ArrayList();
        if ( dataArea.getHeight() <= 0.0 || dataArea.getWidth() < 0.0 ) {
            return ticks;
        }
        final CategoryPlot plot = ( CategoryPlot ) this.getPlot();
        final List categories = plot.getCategoriesForAxis ( this );
        double max = 0.0;
        if ( categories != null ) {
            final CategoryLabelPosition position = this.categoryLabelPositions.getLabelPosition ( edge );
            float r = this.maximumCategoryLabelWidthRatio;
            if ( r <= 0.0 ) {
                r = position.getWidthRatio();
            }
            float l;
            if ( position.getWidthType() == CategoryLabelWidthType.CATEGORY ) {
                l = ( float ) this.calculateCategorySize ( categories.size(), dataArea, edge );
            } else if ( RectangleEdge.isLeftOrRight ( edge ) ) {
                l = ( float ) dataArea.getWidth();
            } else {
                l = ( float ) dataArea.getHeight();
            }
            int categoryIndex = 0;
            for ( final Comparable category : categories ) {
                g2.setFont ( this.getTickLabelFont ( category ) );
                final TextBlock label = this.createLabel ( category, l * r, edge, g2 );
                if ( edge == RectangleEdge.TOP || edge == RectangleEdge.BOTTOM ) {
                    max = Math.max ( max, this.calculateTextBlockHeight ( label, position, g2 ) );
                } else if ( edge == RectangleEdge.LEFT || edge == RectangleEdge.RIGHT ) {
                    max = Math.max ( max, this.calculateTextBlockWidth ( label, position, g2 ) );
                }
                final Tick tick = new CategoryTick ( category, label, position.getLabelAnchor(), position.getRotationAnchor(), position.getAngle() );
                ticks.add ( tick );
                ++categoryIndex;
            }
        }
        state.setMax ( max );
        return ticks;
    }
    public void drawTickMarks ( final Graphics2D g2, final double cursor, final Rectangle2D dataArea, final RectangleEdge edge, final AxisState state ) {
        final Plot p = this.getPlot();
        if ( p == null ) {
            return;
        }
        final CategoryPlot plot = ( CategoryPlot ) p;
        final double il = this.getTickMarkInsideLength();
        final double ol = this.getTickMarkOutsideLength();
        final Line2D line = new Line2D.Double();
        final List categories = plot.getCategoriesForAxis ( this );
        g2.setPaint ( this.getTickMarkPaint() );
        g2.setStroke ( this.getTickMarkStroke() );
        final Object saved = g2.getRenderingHint ( RenderingHints.KEY_STROKE_CONTROL );
        g2.setRenderingHint ( RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE );
        if ( edge.equals ( ( Object ) RectangleEdge.TOP ) ) {
            for ( final Comparable key : categories ) {
                final double x = this.getCategoryMiddle ( key, categories, dataArea, edge );
                line.setLine ( x, cursor, x, cursor + il );
                g2.draw ( line );
                line.setLine ( x, cursor, x, cursor - ol );
                g2.draw ( line );
            }
            state.cursorUp ( ol );
        } else if ( edge.equals ( ( Object ) RectangleEdge.BOTTOM ) ) {
            for ( final Comparable key : categories ) {
                final double x = this.getCategoryMiddle ( key, categories, dataArea, edge );
                line.setLine ( x, cursor, x, cursor - il );
                g2.draw ( line );
                line.setLine ( x, cursor, x, cursor + ol );
                g2.draw ( line );
            }
            state.cursorDown ( ol );
        } else if ( edge.equals ( ( Object ) RectangleEdge.LEFT ) ) {
            for ( final Comparable key : categories ) {
                final double y = this.getCategoryMiddle ( key, categories, dataArea, edge );
                line.setLine ( cursor, y, cursor + il, y );
                g2.draw ( line );
                line.setLine ( cursor, y, cursor - ol, y );
                g2.draw ( line );
            }
            state.cursorLeft ( ol );
        } else if ( edge.equals ( ( Object ) RectangleEdge.RIGHT ) ) {
            for ( final Comparable key : categories ) {
                final double y = this.getCategoryMiddle ( key, categories, dataArea, edge );
                line.setLine ( cursor, y, cursor - il, y );
                g2.draw ( line );
                line.setLine ( cursor, y, cursor + ol, y );
                g2.draw ( line );
            }
            state.cursorRight ( ol );
        }
        g2.setRenderingHint ( RenderingHints.KEY_STROKE_CONTROL, saved );
    }
    protected TextBlock createLabel ( final Comparable category, final float width, final RectangleEdge edge, final Graphics2D g2 ) {
        final TextBlock label = TextUtilities.createTextBlock ( category.toString(), this.getTickLabelFont ( category ), this.getTickLabelPaint ( category ), width, this.maximumCategoryLabelLines, ( TextMeasurer ) new G2TextMeasurer ( g2 ) );
        return label;
    }
    protected double calculateTextBlockWidth ( final TextBlock block, final CategoryLabelPosition position, final Graphics2D g2 ) {
        final RectangleInsets insets = this.getTickLabelInsets();
        final Size2D size = block.calculateDimensions ( g2 );
        final Rectangle2D box = new Rectangle2D.Double ( 0.0, 0.0, size.getWidth(), size.getHeight() );
        final Shape rotatedBox = ShapeUtilities.rotateShape ( ( Shape ) box, position.getAngle(), 0.0f, 0.0f );
        final double w = rotatedBox.getBounds2D().getWidth() + insets.getLeft() + insets.getRight();
        return w;
    }
    protected double calculateTextBlockHeight ( final TextBlock block, final CategoryLabelPosition position, final Graphics2D g2 ) {
        final RectangleInsets insets = this.getTickLabelInsets();
        final Size2D size = block.calculateDimensions ( g2 );
        final Rectangle2D box = new Rectangle2D.Double ( 0.0, 0.0, size.getWidth(), size.getHeight() );
        final Shape rotatedBox = ShapeUtilities.rotateShape ( ( Shape ) box, position.getAngle(), 0.0f, 0.0f );
        final double h = rotatedBox.getBounds2D().getHeight() + insets.getTop() + insets.getBottom();
        return h;
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final CategoryAxis clone = ( CategoryAxis ) super.clone();
        clone.tickLabelFontMap = new HashMap ( this.tickLabelFontMap );
        clone.tickLabelPaintMap = new HashMap ( this.tickLabelPaintMap );
        clone.categoryLabelToolTips = new HashMap ( this.categoryLabelToolTips );
        clone.categoryLabelURLs = new HashMap ( this.categoryLabelToolTips );
        return clone;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof CategoryAxis ) ) {
            return false;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        final CategoryAxis that = ( CategoryAxis ) obj;
        return that.lowerMargin == this.lowerMargin && that.upperMargin == this.upperMargin && that.categoryMargin == this.categoryMargin && that.maximumCategoryLabelWidthRatio == this.maximumCategoryLabelWidthRatio && that.categoryLabelPositionOffset == this.categoryLabelPositionOffset && ObjectUtilities.equal ( ( Object ) that.categoryLabelPositions, ( Object ) this.categoryLabelPositions ) && ObjectUtilities.equal ( ( Object ) that.categoryLabelToolTips, ( Object ) this.categoryLabelToolTips ) && ObjectUtilities.equal ( ( Object ) this.categoryLabelURLs, ( Object ) that.categoryLabelURLs ) && ObjectUtilities.equal ( ( Object ) this.tickLabelFontMap, ( Object ) that.tickLabelFontMap ) && this.equalPaintMaps ( this.tickLabelPaintMap, that.tickLabelPaintMap );
    }
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        this.writePaintMap ( this.tickLabelPaintMap, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.tickLabelPaintMap = this.readPaintMap ( stream );
    }
    private Map readPaintMap ( final ObjectInputStream in ) throws IOException, ClassNotFoundException {
        final boolean isNull = in.readBoolean();
        if ( isNull ) {
            return null;
        }
        final Map result = new HashMap();
        for ( int count = in.readInt(), i = 0; i < count; ++i ) {
            final Comparable category = ( Comparable ) in.readObject();
            final Paint paint = SerialUtilities.readPaint ( in );
            result.put ( category, paint );
        }
        return result;
    }
    private void writePaintMap ( final Map map, final ObjectOutputStream out ) throws IOException {
        if ( map == null ) {
            out.writeBoolean ( true );
        } else {
            out.writeBoolean ( false );
            final Set keys = map.keySet();
            final int count = keys.size();
            out.writeInt ( count );
            for ( final Comparable key : keys ) {
                out.writeObject ( key );
                SerialUtilities.writePaint ( ( Paint ) map.get ( key ), out );
            }
        }
    }
    private boolean equalPaintMaps ( final Map map1, final Map map2 ) {
        if ( map1.size() != map2.size() ) {
            return false;
        }
        final Set entries = map1.entrySet();
        for ( final Map.Entry entry : entries ) {
            final Paint p1 = entry.getValue();
            final Paint p2 = map2.get ( entry.getKey() );
            if ( !PaintUtilities.equal ( p1, p2 ) ) {
                return false;
            }
        }
        return true;
    }
    protected AxisState drawCategoryLabels ( final Graphics2D g2, final Rectangle2D dataArea, final RectangleEdge edge, final AxisState state, final PlotRenderingInfo plotState ) {
        return this.drawCategoryLabels ( g2, dataArea, dataArea, edge, state, plotState );
    }
}
