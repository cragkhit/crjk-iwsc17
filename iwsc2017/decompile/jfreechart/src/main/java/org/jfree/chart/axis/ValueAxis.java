package org.jfree.chart.axis;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.ObjectUtilities;
import java.awt.font.LineMetrics;
import java.awt.FontMetrics;
import java.awt.Font;
import org.jfree.chart.plot.Plot;
import java.util.Iterator;
import java.util.List;
import org.jfree.text.TextUtilities;
import org.jfree.chart.util.AttrStringUtils;
import org.jfree.ui.RectangleInsets;
import java.awt.geom.AffineTransform;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import org.jfree.ui.RectangleEdge;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.chart.util.ParamChecks;
import java.awt.Polygon;
import java.awt.Shape;
import org.jfree.data.Range;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public abstract class ValueAxis extends Axis implements Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 3698345477322391456L;
    public static final Range DEFAULT_RANGE;
    public static final boolean DEFAULT_AUTO_RANGE = true;
    public static final boolean DEFAULT_INVERTED = false;
    public static final double DEFAULT_AUTO_RANGE_MINIMUM_SIZE = 1.0E-8;
    public static final double DEFAULT_LOWER_MARGIN = 0.05;
    public static final double DEFAULT_UPPER_MARGIN = 0.05;
    public static final double DEFAULT_LOWER_BOUND = 0.0;
    public static final double DEFAULT_UPPER_BOUND = 1.0;
    public static final boolean DEFAULT_AUTO_TICK_UNIT_SELECTION = true;
    public static final int MAXIMUM_TICK_COUNT = 500;
    private boolean positiveArrowVisible;
    private boolean negativeArrowVisible;
    private transient Shape upArrow;
    private transient Shape downArrow;
    private transient Shape leftArrow;
    private transient Shape rightArrow;
    private boolean inverted;
    private Range range;
    private boolean autoRange;
    private double autoRangeMinimumSize;
    private Range defaultAutoRange;
    private double upperMargin;
    private double lowerMargin;
    private double fixedAutoRange;
    private boolean autoTickUnitSelection;
    private TickUnitSource standardTickUnits;
    private int autoTickIndex;
    private int minorTickCount;
    private boolean verticalTickLabels;
    protected ValueAxis ( final String label, final TickUnitSource standardTickUnits ) {
        super ( label );
        this.positiveArrowVisible = false;
        this.negativeArrowVisible = false;
        this.range = ValueAxis.DEFAULT_RANGE;
        this.autoRange = true;
        this.defaultAutoRange = ValueAxis.DEFAULT_RANGE;
        this.inverted = false;
        this.autoRangeMinimumSize = 1.0E-8;
        this.lowerMargin = 0.05;
        this.upperMargin = 0.05;
        this.fixedAutoRange = 0.0;
        this.autoTickUnitSelection = true;
        this.standardTickUnits = standardTickUnits;
        final Polygon p1 = new Polygon();
        p1.addPoint ( 0, 0 );
        p1.addPoint ( -2, 2 );
        p1.addPoint ( 2, 2 );
        this.upArrow = p1;
        final Polygon p2 = new Polygon();
        p2.addPoint ( 0, 0 );
        p2.addPoint ( -2, -2 );
        p2.addPoint ( 2, -2 );
        this.downArrow = p2;
        final Polygon p3 = new Polygon();
        p3.addPoint ( 0, 0 );
        p3.addPoint ( -2, -2 );
        p3.addPoint ( -2, 2 );
        this.rightArrow = p3;
        final Polygon p4 = new Polygon();
        p4.addPoint ( 0, 0 );
        p4.addPoint ( 2, -2 );
        p4.addPoint ( 2, 2 );
        this.leftArrow = p4;
        this.verticalTickLabels = false;
        this.minorTickCount = 0;
    }
    public boolean isVerticalTickLabels() {
        return this.verticalTickLabels;
    }
    public void setVerticalTickLabels ( final boolean flag ) {
        if ( this.verticalTickLabels != flag ) {
            this.verticalTickLabels = flag;
            this.fireChangeEvent();
        }
    }
    public boolean isPositiveArrowVisible() {
        return this.positiveArrowVisible;
    }
    public void setPositiveArrowVisible ( final boolean visible ) {
        this.positiveArrowVisible = visible;
        this.fireChangeEvent();
    }
    public boolean isNegativeArrowVisible() {
        return this.negativeArrowVisible;
    }
    public void setNegativeArrowVisible ( final boolean visible ) {
        this.negativeArrowVisible = visible;
        this.fireChangeEvent();
    }
    public Shape getUpArrow() {
        return this.upArrow;
    }
    public void setUpArrow ( final Shape arrow ) {
        ParamChecks.nullNotPermitted ( arrow, "arrow" );
        this.upArrow = arrow;
        this.fireChangeEvent();
    }
    public Shape getDownArrow() {
        return this.downArrow;
    }
    public void setDownArrow ( final Shape arrow ) {
        ParamChecks.nullNotPermitted ( arrow, "arrow" );
        this.downArrow = arrow;
        this.fireChangeEvent();
    }
    public Shape getLeftArrow() {
        return this.leftArrow;
    }
    public void setLeftArrow ( final Shape arrow ) {
        ParamChecks.nullNotPermitted ( arrow, "arrow" );
        this.leftArrow = arrow;
        this.fireChangeEvent();
    }
    public Shape getRightArrow() {
        return this.rightArrow;
    }
    public void setRightArrow ( final Shape arrow ) {
        ParamChecks.nullNotPermitted ( arrow, "arrow" );
        this.rightArrow = arrow;
        this.fireChangeEvent();
    }
    @Override
    protected void drawAxisLine ( final Graphics2D g2, final double cursor, final Rectangle2D dataArea, final RectangleEdge edge ) {
        Line2D axisLine = null;
        if ( edge == RectangleEdge.TOP ) {
            axisLine = new Line2D.Double ( dataArea.getX(), cursor, dataArea.getMaxX(), cursor );
        } else if ( edge == RectangleEdge.BOTTOM ) {
            axisLine = new Line2D.Double ( dataArea.getX(), cursor, dataArea.getMaxX(), cursor );
        } else if ( edge == RectangleEdge.LEFT ) {
            axisLine = new Line2D.Double ( cursor, dataArea.getY(), cursor, dataArea.getMaxY() );
        } else if ( edge == RectangleEdge.RIGHT ) {
            axisLine = new Line2D.Double ( cursor, dataArea.getY(), cursor, dataArea.getMaxY() );
        }
        g2.setPaint ( this.getAxisLinePaint() );
        g2.setStroke ( this.getAxisLineStroke() );
        final Object saved = g2.getRenderingHint ( RenderingHints.KEY_STROKE_CONTROL );
        g2.setRenderingHint ( RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE );
        g2.draw ( axisLine );
        g2.setRenderingHint ( RenderingHints.KEY_STROKE_CONTROL, saved );
        boolean drawUpOrRight = false;
        boolean drawDownOrLeft = false;
        if ( this.positiveArrowVisible ) {
            if ( this.inverted ) {
                drawDownOrLeft = true;
            } else {
                drawUpOrRight = true;
            }
        }
        if ( this.negativeArrowVisible ) {
            if ( this.inverted ) {
                drawUpOrRight = true;
            } else {
                drawDownOrLeft = true;
            }
        }
        if ( drawUpOrRight ) {
            double x = 0.0;
            double y = 0.0;
            Shape arrow = null;
            if ( edge == RectangleEdge.TOP || edge == RectangleEdge.BOTTOM ) {
                x = dataArea.getMaxX();
                y = cursor;
                arrow = this.rightArrow;
            } else if ( edge == RectangleEdge.LEFT || edge == RectangleEdge.RIGHT ) {
                x = cursor;
                y = dataArea.getMinY();
                arrow = this.upArrow;
            }
            final AffineTransform transformer = new AffineTransform();
            transformer.setToTranslation ( x, y );
            final Shape shape = transformer.createTransformedShape ( arrow );
            g2.fill ( shape );
            g2.draw ( shape );
        }
        if ( drawDownOrLeft ) {
            double x = 0.0;
            double y = 0.0;
            Shape arrow = null;
            if ( edge == RectangleEdge.TOP || edge == RectangleEdge.BOTTOM ) {
                x = dataArea.getMinX();
                y = cursor;
                arrow = this.leftArrow;
            } else if ( edge == RectangleEdge.LEFT || edge == RectangleEdge.RIGHT ) {
                x = cursor;
                y = dataArea.getMaxY();
                arrow = this.downArrow;
            }
            final AffineTransform transformer = new AffineTransform();
            transformer.setToTranslation ( x, y );
            final Shape shape = transformer.createTransformedShape ( arrow );
            g2.fill ( shape );
            g2.draw ( shape );
        }
    }
    protected float[] calculateAnchorPoint ( final ValueTick tick, final double cursor, final Rectangle2D dataArea, final RectangleEdge edge ) {
        final RectangleInsets insets = this.getTickLabelInsets();
        final float[] result = new float[2];
        if ( edge == RectangleEdge.TOP ) {
            result[0] = ( float ) this.valueToJava2D ( tick.getValue(), dataArea, edge );
            result[1] = ( float ) ( cursor - insets.getBottom() - 2.0 );
        } else if ( edge == RectangleEdge.BOTTOM ) {
            result[0] = ( float ) this.valueToJava2D ( tick.getValue(), dataArea, edge );
            result[1] = ( float ) ( cursor + insets.getTop() + 2.0 );
        } else if ( edge == RectangleEdge.LEFT ) {
            result[0] = ( float ) ( cursor - insets.getLeft() - 2.0 );
            result[1] = ( float ) this.valueToJava2D ( tick.getValue(), dataArea, edge );
        } else if ( edge == RectangleEdge.RIGHT ) {
            result[0] = ( float ) ( cursor + insets.getRight() + 2.0 );
            result[1] = ( float ) this.valueToJava2D ( tick.getValue(), dataArea, edge );
        }
        return result;
    }
    protected AxisState drawTickMarksAndLabels ( final Graphics2D g2, final double cursor, final Rectangle2D plotArea, final Rectangle2D dataArea, final RectangleEdge edge ) {
        final AxisState state = new AxisState ( cursor );
        if ( this.isAxisLineVisible() ) {
            this.drawAxisLine ( g2, cursor, dataArea, edge );
        }
        final List ticks = this.refreshTicks ( g2, state, dataArea, edge );
        state.setTicks ( ticks );
        g2.setFont ( this.getTickLabelFont() );
        final Object saved = g2.getRenderingHint ( RenderingHints.KEY_STROKE_CONTROL );
        g2.setRenderingHint ( RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE );
        for ( final ValueTick tick : ticks ) {
            if ( this.isTickLabelsVisible() ) {
                g2.setPaint ( this.getTickLabelPaint() );
                final float[] anchorPoint = this.calculateAnchorPoint ( tick, cursor, dataArea, edge );
                if ( tick instanceof LogTick ) {
                    final LogTick lt = ( LogTick ) tick;
                    if ( lt.getAttributedLabel() == null ) {
                        continue;
                    }
                    AttrStringUtils.drawRotatedString ( lt.getAttributedLabel(), g2, anchorPoint[0], anchorPoint[1], tick.getTextAnchor(), tick.getAngle(), tick.getRotationAnchor() );
                } else {
                    if ( tick.getText() == null ) {
                        continue;
                    }
                    TextUtilities.drawRotatedString ( tick.getText(), g2, anchorPoint[0], anchorPoint[1], tick.getTextAnchor(), tick.getAngle(), tick.getRotationAnchor() );
                }
            }
            if ( ( this.isTickMarksVisible() && tick.getTickType().equals ( TickType.MAJOR ) ) || ( this.isMinorTickMarksVisible() && tick.getTickType().equals ( TickType.MINOR ) ) ) {
                final double ol = tick.getTickType().equals ( TickType.MINOR ) ? this.getMinorTickMarkOutsideLength() : ( ( double ) this.getTickMarkOutsideLength() );
                final double il = tick.getTickType().equals ( TickType.MINOR ) ? this.getMinorTickMarkInsideLength() : ( ( double ) this.getTickMarkInsideLength() );
                final float xx = ( float ) this.valueToJava2D ( tick.getValue(), dataArea, edge );
                Line2D mark = null;
                g2.setStroke ( this.getTickMarkStroke() );
                g2.setPaint ( this.getTickMarkPaint() );
                if ( edge == RectangleEdge.LEFT ) {
                    mark = new Line2D.Double ( cursor - ol, xx, cursor + il, xx );
                } else if ( edge == RectangleEdge.RIGHT ) {
                    mark = new Line2D.Double ( cursor + ol, xx, cursor - il, xx );
                } else if ( edge == RectangleEdge.TOP ) {
                    mark = new Line2D.Double ( xx, cursor - ol, xx, cursor + il );
                } else if ( edge == RectangleEdge.BOTTOM ) {
                    mark = new Line2D.Double ( xx, cursor + ol, xx, cursor - il );
                }
                g2.draw ( mark );
            }
        }
        g2.setRenderingHint ( RenderingHints.KEY_STROKE_CONTROL, saved );
        double used = 0.0;
        if ( this.isTickLabelsVisible() ) {
            if ( edge == RectangleEdge.LEFT ) {
                used += this.findMaximumTickLabelWidth ( ticks, g2, plotArea, this.isVerticalTickLabels() );
                state.cursorLeft ( used );
            } else if ( edge == RectangleEdge.RIGHT ) {
                used = this.findMaximumTickLabelWidth ( ticks, g2, plotArea, this.isVerticalTickLabels() );
                state.cursorRight ( used );
            } else if ( edge == RectangleEdge.TOP ) {
                used = this.findMaximumTickLabelHeight ( ticks, g2, plotArea, this.isVerticalTickLabels() );
                state.cursorUp ( used );
            } else if ( edge == RectangleEdge.BOTTOM ) {
                used = this.findMaximumTickLabelHeight ( ticks, g2, plotArea, this.isVerticalTickLabels() );
                state.cursorDown ( used );
            }
        }
        return state;
    }
    @Override
    public AxisSpace reserveSpace ( final Graphics2D g2, final Plot plot, final Rectangle2D plotArea, final RectangleEdge edge, AxisSpace space ) {
        if ( space == null ) {
            space = new AxisSpace();
        }
        if ( !this.isVisible() ) {
            return space;
        }
        final double dimension = this.getFixedDimension();
        if ( dimension > 0.0 ) {
            space.add ( dimension, edge );
            return space;
        }
        double tickLabelHeight = 0.0;
        double tickLabelWidth = 0.0;
        if ( this.isTickLabelsVisible() ) {
            g2.setFont ( this.getTickLabelFont() );
            final List ticks = this.refreshTicks ( g2, new AxisState(), plotArea, edge );
            if ( RectangleEdge.isTopOrBottom ( edge ) ) {
                tickLabelHeight = this.findMaximumTickLabelHeight ( ticks, g2, plotArea, this.isVerticalTickLabels() );
            } else if ( RectangleEdge.isLeftOrRight ( edge ) ) {
                tickLabelWidth = this.findMaximumTickLabelWidth ( ticks, g2, plotArea, this.isVerticalTickLabels() );
            }
        }
        final Rectangle2D labelEnclosure = this.getLabelEnclosure ( g2, edge );
        if ( RectangleEdge.isTopOrBottom ( edge ) ) {
            final double labelHeight = labelEnclosure.getHeight();
            space.add ( labelHeight + tickLabelHeight, edge );
        } else if ( RectangleEdge.isLeftOrRight ( edge ) ) {
            final double labelWidth = labelEnclosure.getWidth();
            space.add ( labelWidth + tickLabelWidth, edge );
        }
        return space;
    }
    protected double findMaximumTickLabelHeight ( final List ticks, final Graphics2D g2, final Rectangle2D drawArea, final boolean vertical ) {
        final RectangleInsets insets = this.getTickLabelInsets();
        final Font font = this.getTickLabelFont();
        g2.setFont ( font );
        double maxHeight = 0.0;
        if ( vertical ) {
            final FontMetrics fm = g2.getFontMetrics ( font );
            for ( final Tick tick : ticks ) {
                Rectangle2D labelBounds = null;
                if ( tick instanceof LogTick ) {
                    final LogTick lt = ( LogTick ) tick;
                    if ( lt.getAttributedLabel() != null ) {
                        labelBounds = AttrStringUtils.getTextBounds ( lt.getAttributedLabel(), g2 );
                    }
                } else if ( tick.getText() != null ) {
                    labelBounds = TextUtilities.getTextBounds ( tick.getText(), g2, fm );
                }
                if ( labelBounds != null && labelBounds.getWidth() + insets.getTop() + insets.getBottom() > maxHeight ) {
                    maxHeight = labelBounds.getWidth() + insets.getTop() + insets.getBottom();
                }
            }
        } else {
            final LineMetrics metrics = font.getLineMetrics ( "ABCxyz", g2.getFontRenderContext() );
            maxHeight = metrics.getHeight() + insets.getTop() + insets.getBottom();
        }
        return maxHeight;
    }
    protected double findMaximumTickLabelWidth ( final List ticks, final Graphics2D g2, final Rectangle2D drawArea, final boolean vertical ) {
        final RectangleInsets insets = this.getTickLabelInsets();
        final Font font = this.getTickLabelFont();
        double maxWidth = 0.0;
        if ( !vertical ) {
            final FontMetrics fm = g2.getFontMetrics ( font );
            for ( final Tick tick : ticks ) {
                Rectangle2D labelBounds = null;
                if ( tick instanceof LogTick ) {
                    final LogTick lt = ( LogTick ) tick;
                    if ( lt.getAttributedLabel() != null ) {
                        labelBounds = AttrStringUtils.getTextBounds ( lt.getAttributedLabel(), g2 );
                    }
                } else if ( tick.getText() != null ) {
                    labelBounds = TextUtilities.getTextBounds ( tick.getText(), g2, fm );
                }
                if ( labelBounds != null && labelBounds.getWidth() + insets.getLeft() + insets.getRight() > maxWidth ) {
                    maxWidth = labelBounds.getWidth() + insets.getLeft() + insets.getRight();
                }
            }
        } else {
            final LineMetrics metrics = font.getLineMetrics ( "ABCxyz", g2.getFontRenderContext() );
            maxWidth = metrics.getHeight() + insets.getTop() + insets.getBottom();
        }
        return maxWidth;
    }
    public boolean isInverted() {
        return this.inverted;
    }
    public void setInverted ( final boolean flag ) {
        if ( this.inverted != flag ) {
            this.inverted = flag;
            this.fireChangeEvent();
        }
    }
    public boolean isAutoRange() {
        return this.autoRange;
    }
    public void setAutoRange ( final boolean auto ) {
        this.setAutoRange ( auto, true );
    }
    protected void setAutoRange ( final boolean auto, final boolean notify ) {
        if ( this.autoRange != auto ) {
            this.autoRange = auto;
            if ( this.autoRange ) {
                this.autoAdjustRange();
            }
            if ( notify ) {
                this.fireChangeEvent();
            }
        }
    }
    public double getAutoRangeMinimumSize() {
        return this.autoRangeMinimumSize;
    }
    public void setAutoRangeMinimumSize ( final double size ) {
        this.setAutoRangeMinimumSize ( size, true );
    }
    public void setAutoRangeMinimumSize ( final double size, final boolean notify ) {
        if ( size <= 0.0 ) {
            throw new IllegalArgumentException ( "NumberAxis.setAutoRangeMinimumSize(double): must be > 0.0." );
        }
        if ( this.autoRangeMinimumSize != size ) {
            this.autoRangeMinimumSize = size;
            if ( this.autoRange ) {
                this.autoAdjustRange();
            }
            if ( notify ) {
                this.fireChangeEvent();
            }
        }
    }
    public Range getDefaultAutoRange() {
        return this.defaultAutoRange;
    }
    public void setDefaultAutoRange ( final Range range ) {
        ParamChecks.nullNotPermitted ( range, "range" );
        this.defaultAutoRange = range;
        this.fireChangeEvent();
    }
    public double getLowerMargin() {
        return this.lowerMargin;
    }
    public void setLowerMargin ( final double margin ) {
        this.lowerMargin = margin;
        if ( this.isAutoRange() ) {
            this.autoAdjustRange();
        }
        this.fireChangeEvent();
    }
    public double getUpperMargin() {
        return this.upperMargin;
    }
    public void setUpperMargin ( final double margin ) {
        this.upperMargin = margin;
        if ( this.isAutoRange() ) {
            this.autoAdjustRange();
        }
        this.fireChangeEvent();
    }
    public double getFixedAutoRange() {
        return this.fixedAutoRange;
    }
    public void setFixedAutoRange ( final double length ) {
        this.fixedAutoRange = length;
        if ( this.isAutoRange() ) {
            this.autoAdjustRange();
        }
        this.fireChangeEvent();
    }
    public double getLowerBound() {
        return this.range.getLowerBound();
    }
    public void setLowerBound ( final double min ) {
        if ( this.range.getUpperBound() > min ) {
            this.setRange ( new Range ( min, this.range.getUpperBound() ) );
        } else {
            this.setRange ( new Range ( min, min + 1.0 ) );
        }
    }
    public double getUpperBound() {
        return this.range.getUpperBound();
    }
    public void setUpperBound ( final double max ) {
        if ( this.range.getLowerBound() < max ) {
            this.setRange ( new Range ( this.range.getLowerBound(), max ) );
        } else {
            this.setRange ( max - 1.0, max );
        }
    }
    public Range getRange() {
        return this.range;
    }
    public void setRange ( final Range range ) {
        this.setRange ( range, true, true );
    }
    public void setRange ( final Range range, final boolean turnOffAutoRange, final boolean notify ) {
        ParamChecks.nullNotPermitted ( range, "range" );
        if ( range.getLength() <= 0.0 ) {
            throw new IllegalArgumentException ( "A positive range length is required: " + range );
        }
        if ( turnOffAutoRange ) {
            this.autoRange = false;
        }
        this.range = range;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public void setRange ( final double lower, final double upper ) {
        this.setRange ( new Range ( lower, upper ) );
    }
    public void setRangeWithMargins ( final Range range ) {
        this.setRangeWithMargins ( range, true, true );
    }
    public void setRangeWithMargins ( final Range range, final boolean turnOffAutoRange, final boolean notify ) {
        ParamChecks.nullNotPermitted ( range, "range" );
        this.setRange ( Range.expand ( range, this.getLowerMargin(), this.getUpperMargin() ), turnOffAutoRange, notify );
    }
    public void setRangeWithMargins ( final double lower, final double upper ) {
        this.setRangeWithMargins ( new Range ( lower, upper ) );
    }
    public void setRangeAboutValue ( final double value, final double length ) {
        this.setRange ( new Range ( value - length / 2.0, value + length / 2.0 ) );
    }
    public boolean isAutoTickUnitSelection() {
        return this.autoTickUnitSelection;
    }
    public void setAutoTickUnitSelection ( final boolean flag ) {
        this.setAutoTickUnitSelection ( flag, true );
    }
    public void setAutoTickUnitSelection ( final boolean flag, final boolean notify ) {
        if ( this.autoTickUnitSelection != flag ) {
            this.autoTickUnitSelection = flag;
            if ( notify ) {
                this.fireChangeEvent();
            }
        }
    }
    public TickUnitSource getStandardTickUnits() {
        return this.standardTickUnits;
    }
    public void setStandardTickUnits ( final TickUnitSource source ) {
        this.standardTickUnits = source;
        this.fireChangeEvent();
    }
    public int getMinorTickCount() {
        return this.minorTickCount;
    }
    public void setMinorTickCount ( final int count ) {
        this.minorTickCount = count;
        this.fireChangeEvent();
    }
    public abstract double valueToJava2D ( final double p0, final Rectangle2D p1, final RectangleEdge p2 );
    public double lengthToJava2D ( final double length, final Rectangle2D area, final RectangleEdge edge ) {
        final double zero = this.valueToJava2D ( 0.0, area, edge );
        final double l = this.valueToJava2D ( length, area, edge );
        return Math.abs ( l - zero );
    }
    public abstract double java2DToValue ( final double p0, final Rectangle2D p1, final RectangleEdge p2 );
    protected abstract void autoAdjustRange();
    public void centerRange ( final double value ) {
        final double central = this.range.getCentralValue();
        final Range adjusted = new Range ( this.range.getLowerBound() + value - central, this.range.getUpperBound() + value - central );
        this.setRange ( adjusted );
    }
    public void resizeRange ( final double percent ) {
        this.resizeRange ( percent, this.range.getCentralValue() );
    }
    public void resizeRange ( final double percent, final double anchorValue ) {
        if ( percent > 0.0 ) {
            final double halfLength = this.range.getLength() * percent / 2.0;
            final Range adjusted = new Range ( anchorValue - halfLength, anchorValue + halfLength );
            this.setRange ( adjusted );
        } else {
            this.setAutoRange ( true );
        }
    }
    public void resizeRange2 ( final double percent, final double anchorValue ) {
        if ( percent > 0.0 ) {
            final double left = anchorValue - this.getLowerBound();
            final double right = this.getUpperBound() - anchorValue;
            final Range adjusted = new Range ( anchorValue - left * percent, anchorValue + right * percent );
            this.setRange ( adjusted );
        } else {
            this.setAutoRange ( true );
        }
    }
    public void zoomRange ( final double lowerPercent, final double upperPercent ) {
        final double start = this.range.getLowerBound();
        final double length = this.range.getLength();
        double r0;
        double r;
        if ( this.isInverted() ) {
            r0 = start + length * ( 1.0 - upperPercent );
            r = start + length * ( 1.0 - lowerPercent );
        } else {
            r0 = start + length * lowerPercent;
            r = start + length * upperPercent;
        }
        if ( r > r0 && !Double.isInfinite ( r - r0 ) ) {
            this.setRange ( new Range ( r0, r ) );
        }
    }
    public void pan ( final double percent ) {
        final Range r = this.getRange();
        final double length = this.range.getLength();
        final double adj = length * percent;
        final double lower = r.getLowerBound() + adj;
        final double upper = r.getUpperBound() + adj;
        this.setRange ( lower, upper );
    }
    protected int getAutoTickIndex() {
        return this.autoTickIndex;
    }
    protected void setAutoTickIndex ( final int index ) {
        this.autoTickIndex = index;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof ValueAxis ) ) {
            return false;
        }
        final ValueAxis that = ( ValueAxis ) obj;
        return this.positiveArrowVisible == that.positiveArrowVisible && this.negativeArrowVisible == that.negativeArrowVisible && this.inverted == that.inverted && ( this.autoRange || ObjectUtilities.equal ( ( Object ) this.range, ( Object ) that.range ) ) && this.autoRange == that.autoRange && this.autoRangeMinimumSize == that.autoRangeMinimumSize && this.defaultAutoRange.equals ( that.defaultAutoRange ) && this.upperMargin == that.upperMargin && this.lowerMargin == that.lowerMargin && this.fixedAutoRange == that.fixedAutoRange && this.autoTickUnitSelection == that.autoTickUnitSelection && ObjectUtilities.equal ( ( Object ) this.standardTickUnits, ( Object ) that.standardTickUnits ) && this.verticalTickLabels == that.verticalTickLabels && this.minorTickCount == that.minorTickCount && super.equals ( obj );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final ValueAxis clone = ( ValueAxis ) super.clone();
        return clone;
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeShape ( this.upArrow, stream );
        SerialUtilities.writeShape ( this.downArrow, stream );
        SerialUtilities.writeShape ( this.leftArrow, stream );
        SerialUtilities.writeShape ( this.rightArrow, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.upArrow = SerialUtilities.readShape ( stream );
        this.downArrow = SerialUtilities.readShape ( stream );
        this.leftArrow = SerialUtilities.readShape ( stream );
        this.rightArrow = SerialUtilities.readShape ( stream );
    }
    static {
        DEFAULT_RANGE = new Range ( 0.0, 1.0 );
    }
}
