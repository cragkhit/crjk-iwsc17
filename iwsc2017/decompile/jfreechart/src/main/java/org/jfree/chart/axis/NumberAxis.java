package org.jfree.chart.axis;
import java.text.DecimalFormat;
import org.jfree.util.ObjectUtilities;
import org.jfree.ui.TextAnchor;
import java.util.ArrayList;
import java.awt.FontMetrics;
import java.awt.font.LineMetrics;
import java.awt.font.FontRenderContext;
import java.awt.Font;
import org.jfree.ui.RectangleInsets;
import java.util.Locale;
import java.util.List;
import org.jfree.chart.plot.PlotRenderingInfo;
import java.awt.Graphics2D;
import org.jfree.ui.RectangleEdge;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.plot.Plot;
import org.jfree.data.Range;
import org.jfree.chart.plot.ValueAxisPlot;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.util.ParamChecks;
import java.text.NumberFormat;
import org.jfree.data.RangeType;
import java.io.Serializable;
public class NumberAxis extends ValueAxis implements Cloneable, Serializable {
    private static final long serialVersionUID = 2805933088476185789L;
    public static final boolean DEFAULT_AUTO_RANGE_INCLUDES_ZERO = true;
    public static final boolean DEFAULT_AUTO_RANGE_STICKY_ZERO = true;
    public static final NumberTickUnit DEFAULT_TICK_UNIT;
    public static final boolean DEFAULT_VERTICAL_TICK_LABELS = false;
    private RangeType rangeType;
    private boolean autoRangeIncludesZero;
    private boolean autoRangeStickyZero;
    private NumberTickUnit tickUnit;
    private NumberFormat numberFormatOverride;
    private MarkerAxisBand markerBand;
    public NumberAxis() {
        this ( null );
    }
    public NumberAxis ( final String label ) {
        super ( label, createStandardTickUnits() );
        this.rangeType = RangeType.FULL;
        this.autoRangeIncludesZero = true;
        this.autoRangeStickyZero = true;
        this.tickUnit = NumberAxis.DEFAULT_TICK_UNIT;
        this.numberFormatOverride = null;
        this.markerBand = null;
    }
    public RangeType getRangeType() {
        return this.rangeType;
    }
    public void setRangeType ( final RangeType rangeType ) {
        ParamChecks.nullNotPermitted ( rangeType, "rangeType" );
        this.rangeType = rangeType;
        this.notifyListeners ( new AxisChangeEvent ( this ) );
    }
    public boolean getAutoRangeIncludesZero() {
        return this.autoRangeIncludesZero;
    }
    public void setAutoRangeIncludesZero ( final boolean flag ) {
        if ( this.autoRangeIncludesZero != flag ) {
            this.autoRangeIncludesZero = flag;
            if ( this.isAutoRange() ) {
                this.autoAdjustRange();
            }
            this.notifyListeners ( new AxisChangeEvent ( this ) );
        }
    }
    public boolean getAutoRangeStickyZero() {
        return this.autoRangeStickyZero;
    }
    public void setAutoRangeStickyZero ( final boolean flag ) {
        if ( this.autoRangeStickyZero != flag ) {
            this.autoRangeStickyZero = flag;
            if ( this.isAutoRange() ) {
                this.autoAdjustRange();
            }
            this.notifyListeners ( new AxisChangeEvent ( this ) );
        }
    }
    public NumberTickUnit getTickUnit() {
        return this.tickUnit;
    }
    public void setTickUnit ( final NumberTickUnit unit ) {
        this.setTickUnit ( unit, true, true );
    }
    public void setTickUnit ( final NumberTickUnit unit, final boolean notify, final boolean turnOffAutoSelect ) {
        ParamChecks.nullNotPermitted ( unit, "unit" );
        this.tickUnit = unit;
        if ( turnOffAutoSelect ) {
            this.setAutoTickUnitSelection ( false, false );
        }
        if ( notify ) {
            this.notifyListeners ( new AxisChangeEvent ( this ) );
        }
    }
    public NumberFormat getNumberFormatOverride() {
        return this.numberFormatOverride;
    }
    public void setNumberFormatOverride ( final NumberFormat formatter ) {
        this.numberFormatOverride = formatter;
        this.notifyListeners ( new AxisChangeEvent ( this ) );
    }
    public MarkerAxisBand getMarkerBand() {
        return this.markerBand;
    }
    public void setMarkerBand ( final MarkerAxisBand band ) {
        this.markerBand = band;
        this.notifyListeners ( new AxisChangeEvent ( this ) );
    }
    @Override
    public void configure() {
        if ( this.isAutoRange() ) {
            this.autoAdjustRange();
        }
    }
    @Override
    protected void autoAdjustRange() {
        final Plot plot = this.getPlot();
        if ( plot == null ) {
            return;
        }
        if ( plot instanceof ValueAxisPlot ) {
            final ValueAxisPlot vap = ( ValueAxisPlot ) plot;
            Range r = vap.getDataRange ( this );
            if ( r == null ) {
                r = this.getDefaultAutoRange();
            }
            double upper = r.getUpperBound();
            double lower = r.getLowerBound();
            if ( this.rangeType == RangeType.POSITIVE ) {
                lower = Math.max ( 0.0, lower );
                upper = Math.max ( 0.0, upper );
            } else if ( this.rangeType == RangeType.NEGATIVE ) {
                lower = Math.min ( 0.0, lower );
                upper = Math.min ( 0.0, upper );
            }
            if ( this.getAutoRangeIncludesZero() ) {
                lower = Math.min ( lower, 0.0 );
                upper = Math.max ( upper, 0.0 );
            }
            final double range = upper - lower;
            final double fixedAutoRange = this.getFixedAutoRange();
            if ( fixedAutoRange > 0.0 ) {
                lower = upper - fixedAutoRange;
            } else {
                final double minRange = this.getAutoRangeMinimumSize();
                if ( range < minRange ) {
                    final double expand = ( minRange - range ) / 2.0;
                    upper += expand;
                    lower -= expand;
                    if ( lower == upper ) {
                        final double adjust = Math.abs ( lower ) / 10.0;
                        lower -= adjust;
                        upper += adjust;
                    }
                    if ( this.rangeType == RangeType.POSITIVE ) {
                        if ( lower < 0.0 ) {
                            upper -= lower;
                            lower = 0.0;
                        }
                    } else if ( this.rangeType == RangeType.NEGATIVE && upper > 0.0 ) {
                        lower -= upper;
                        upper = 0.0;
                    }
                }
                if ( this.getAutoRangeStickyZero() ) {
                    if ( upper <= 0.0 ) {
                        upper = Math.min ( 0.0, upper + this.getUpperMargin() * range );
                    } else {
                        upper += this.getUpperMargin() * range;
                    }
                    if ( lower >= 0.0 ) {
                        lower = Math.max ( 0.0, lower - this.getLowerMargin() * range );
                    } else {
                        lower -= this.getLowerMargin() * range;
                    }
                } else {
                    upper += this.getUpperMargin() * range;
                    lower -= this.getLowerMargin() * range;
                }
            }
            this.setRange ( new Range ( lower, upper ), false, false );
        }
    }
    @Override
    public double valueToJava2D ( final double value, final Rectangle2D area, final RectangleEdge edge ) {
        final Range range = this.getRange();
        final double axisMin = range.getLowerBound();
        final double axisMax = range.getUpperBound();
        double min = 0.0;
        double max = 0.0;
        if ( RectangleEdge.isTopOrBottom ( edge ) ) {
            min = area.getX();
            max = area.getMaxX();
        } else if ( RectangleEdge.isLeftOrRight ( edge ) ) {
            max = area.getMinY();
            min = area.getMaxY();
        }
        if ( this.isInverted() ) {
            return max - ( value - axisMin ) / ( axisMax - axisMin ) * ( max - min );
        }
        return min + ( value - axisMin ) / ( axisMax - axisMin ) * ( max - min );
    }
    @Override
    public double java2DToValue ( final double java2DValue, final Rectangle2D area, final RectangleEdge edge ) {
        final Range range = this.getRange();
        final double axisMin = range.getLowerBound();
        final double axisMax = range.getUpperBound();
        double min = 0.0;
        double max = 0.0;
        if ( RectangleEdge.isTopOrBottom ( edge ) ) {
            min = area.getX();
            max = area.getMaxX();
        } else if ( RectangleEdge.isLeftOrRight ( edge ) ) {
            min = area.getMaxY();
            max = area.getY();
        }
        if ( this.isInverted() ) {
            return axisMax - ( java2DValue - min ) / ( max - min ) * ( axisMax - axisMin );
        }
        return axisMin + ( java2DValue - min ) / ( max - min ) * ( axisMax - axisMin );
    }
    protected double calculateLowestVisibleTickValue() {
        final double unit = this.getTickUnit().getSize();
        final double index = Math.ceil ( this.getRange().getLowerBound() / unit );
        return index * unit;
    }
    protected double calculateHighestVisibleTickValue() {
        final double unit = this.getTickUnit().getSize();
        final double index = Math.floor ( this.getRange().getUpperBound() / unit );
        return index * unit;
    }
    protected int calculateVisibleTickCount() {
        final double unit = this.getTickUnit().getSize();
        final Range range = this.getRange();
        return ( int ) ( Math.floor ( range.getUpperBound() / unit ) - Math.ceil ( range.getLowerBound() / unit ) + 1.0 );
    }
    @Override
    public AxisState draw ( final Graphics2D g2, final double cursor, final Rectangle2D plotArea, final Rectangle2D dataArea, final RectangleEdge edge, final PlotRenderingInfo plotState ) {
        if ( !this.isVisible() ) {
            final AxisState state = new AxisState ( cursor );
            final List ticks = this.refreshTicks ( g2, state, dataArea, edge );
            state.setTicks ( ticks );
            return state;
        }
        AxisState state = this.drawTickMarksAndLabels ( g2, cursor, plotArea, dataArea, edge );
        if ( this.getAttributedLabel() != null ) {
            state = this.drawAttributedLabel ( this.getAttributedLabel(), g2, plotArea, dataArea, edge, state );
        } else {
            state = this.drawLabel ( this.getLabel(), g2, plotArea, dataArea, edge, state );
        }
        this.createAndAddEntity ( cursor, state, dataArea, edge, plotState );
        return state;
    }
    public static TickUnitSource createStandardTickUnits() {
        return new NumberTickUnitSource();
    }
    public static TickUnitSource createIntegerTickUnits() {
        return new NumberTickUnitSource ( true );
    }
    public static TickUnitSource createStandardTickUnits ( final Locale locale ) {
        final NumberFormat numberFormat = NumberFormat.getNumberInstance ( locale );
        return new NumberTickUnitSource ( false, numberFormat );
    }
    public static TickUnitSource createIntegerTickUnits ( final Locale locale ) {
        final NumberFormat numberFormat = NumberFormat.getNumberInstance ( locale );
        return new NumberTickUnitSource ( true, numberFormat );
    }
    protected double estimateMaximumTickLabelHeight ( final Graphics2D g2 ) {
        final RectangleInsets tickLabelInsets = this.getTickLabelInsets();
        double result = tickLabelInsets.getTop() + tickLabelInsets.getBottom();
        final Font tickLabelFont = this.getTickLabelFont();
        final FontRenderContext frc = g2.getFontRenderContext();
        result += tickLabelFont.getLineMetrics ( "123", frc ).getHeight();
        return result;
    }
    protected double estimateMaximumTickLabelWidth ( final Graphics2D g2, final TickUnit unit ) {
        final RectangleInsets tickLabelInsets = this.getTickLabelInsets();
        double result = tickLabelInsets.getLeft() + tickLabelInsets.getRight();
        if ( this.isVerticalTickLabels() ) {
            final FontRenderContext frc = g2.getFontRenderContext();
            final LineMetrics lm = this.getTickLabelFont().getLineMetrics ( "0", frc );
            result += lm.getHeight();
        } else {
            final FontMetrics fm = g2.getFontMetrics ( this.getTickLabelFont() );
            final Range range = this.getRange();
            final double lower = range.getLowerBound();
            final double upper = range.getUpperBound();
            final NumberFormat formatter = this.getNumberFormatOverride();
            String lowerStr;
            String upperStr;
            if ( formatter != null ) {
                lowerStr = formatter.format ( lower );
                upperStr = formatter.format ( upper );
            } else {
                lowerStr = unit.valueToString ( lower );
                upperStr = unit.valueToString ( upper );
            }
            final double w1 = fm.stringWidth ( lowerStr );
            final double w2 = fm.stringWidth ( upperStr );
            result += Math.max ( w1, w2 );
        }
        return result;
    }
    protected void selectAutoTickUnit ( final Graphics2D g2, final Rectangle2D dataArea, final RectangleEdge edge ) {
        if ( RectangleEdge.isTopOrBottom ( edge ) ) {
            this.selectHorizontalAutoTickUnit ( g2, dataArea, edge );
        } else if ( RectangleEdge.isLeftOrRight ( edge ) ) {
            this.selectVerticalAutoTickUnit ( g2, dataArea, edge );
        }
    }
    protected void selectHorizontalAutoTickUnit ( final Graphics2D g2, final Rectangle2D dataArea, final RectangleEdge edge ) {
        TickUnit unit = this.getTickUnit();
        final TickUnitSource tickUnitSource = this.getStandardTickUnits();
        final double length = this.getRange().getLength();
        final int count = ( int ) ( length / unit.getSize() );
        if ( count < 2 || count > 40 ) {
            unit = tickUnitSource.getCeilingTickUnit ( length / 20.0 );
        }
        double tickLabelWidth = this.estimateMaximumTickLabelWidth ( g2, unit );
        final TickUnit unit2 = tickUnitSource.getCeilingTickUnit ( unit );
        final double unit1Width = this.lengthToJava2D ( unit2.getSize(), dataArea, edge );
        final double guess = tickLabelWidth / unit1Width * unit2.getSize();
        NumberTickUnit unit3 = ( NumberTickUnit ) tickUnitSource.getCeilingTickUnit ( guess );
        final double unit2Width = this.lengthToJava2D ( unit3.getSize(), dataArea, edge );
        tickLabelWidth = this.estimateMaximumTickLabelWidth ( g2, unit3 );
        if ( tickLabelWidth > unit2Width ) {
            unit3 = ( NumberTickUnit ) tickUnitSource.getLargerTickUnit ( unit3 );
        }
        this.setTickUnit ( unit3, false, false );
    }
    protected void selectVerticalAutoTickUnit ( final Graphics2D g2, final Rectangle2D dataArea, final RectangleEdge edge ) {
        double tickLabelHeight = this.estimateMaximumTickLabelHeight ( g2 );
        final TickUnitSource tickUnits = this.getStandardTickUnits();
        final TickUnit unit1 = tickUnits.getCeilingTickUnit ( this.getTickUnit() );
        final double unitHeight = this.lengthToJava2D ( unit1.getSize(), dataArea, edge );
        double guess;
        if ( unitHeight > 0.0 ) {
            guess = tickLabelHeight / unitHeight * unit1.getSize();
        } else {
            guess = this.getRange().getLength() / 20.0;
        }
        NumberTickUnit unit2 = ( NumberTickUnit ) tickUnits.getCeilingTickUnit ( guess );
        final double unit2Height = this.lengthToJava2D ( unit2.getSize(), dataArea, edge );
        tickLabelHeight = this.estimateMaximumTickLabelHeight ( g2 );
        if ( tickLabelHeight > unit2Height ) {
            unit2 = ( NumberTickUnit ) tickUnits.getLargerTickUnit ( unit2 );
        }
        this.setTickUnit ( unit2, false, false );
    }
    @Override
    public List refreshTicks ( final Graphics2D g2, final AxisState state, final Rectangle2D dataArea, final RectangleEdge edge ) {
        List result = new ArrayList();
        if ( RectangleEdge.isTopOrBottom ( edge ) ) {
            result = this.refreshTicksHorizontal ( g2, dataArea, edge );
        } else if ( RectangleEdge.isLeftOrRight ( edge ) ) {
            result = this.refreshTicksVertical ( g2, dataArea, edge );
        }
        return result;
    }
    protected List refreshTicksHorizontal ( final Graphics2D g2, final Rectangle2D dataArea, final RectangleEdge edge ) {
        final List result = new ArrayList();
        final Font tickLabelFont = this.getTickLabelFont();
        g2.setFont ( tickLabelFont );
        if ( this.isAutoTickUnitSelection() ) {
            this.selectAutoTickUnit ( g2, dataArea, edge );
        }
        final TickUnit tu = this.getTickUnit();
        final double size = tu.getSize();
        final int count = this.calculateVisibleTickCount();
        final double lowestTickValue = this.calculateLowestVisibleTickValue();
        if ( count <= 500 ) {
            int minorTickSpaces = this.getMinorTickCount();
            if ( minorTickSpaces <= 0 ) {
                minorTickSpaces = tu.getMinorTickCount();
            }
            for ( int minorTick = 1; minorTick < minorTickSpaces; ++minorTick ) {
                final double minorTickValue = lowestTickValue - size * minorTick / minorTickSpaces;
                if ( this.getRange().contains ( minorTickValue ) ) {
                    result.add ( new NumberTick ( TickType.MINOR, minorTickValue, "", TextAnchor.TOP_CENTER, TextAnchor.CENTER, 0.0 ) );
                }
            }
            for ( int i = 0; i < count; ++i ) {
                final double currentTickValue = lowestTickValue + i * size;
                final NumberFormat formatter = this.getNumberFormatOverride();
                String tickLabel;
                if ( formatter != null ) {
                    tickLabel = formatter.format ( currentTickValue );
                } else {
                    tickLabel = this.getTickUnit().valueToString ( currentTickValue );
                }
                double angle = 0.0;
                TextAnchor anchor;
                TextAnchor rotationAnchor;
                if ( this.isVerticalTickLabels() ) {
                    anchor = TextAnchor.CENTER_RIGHT;
                    rotationAnchor = TextAnchor.CENTER_RIGHT;
                    if ( edge == RectangleEdge.TOP ) {
                        angle = 1.5707963267948966;
                    } else {
                        angle = -1.5707963267948966;
                    }
                } else if ( edge == RectangleEdge.TOP ) {
                    anchor = TextAnchor.BOTTOM_CENTER;
                    rotationAnchor = TextAnchor.BOTTOM_CENTER;
                } else {
                    anchor = TextAnchor.TOP_CENTER;
                    rotationAnchor = TextAnchor.TOP_CENTER;
                }
                final Tick tick = new NumberTick ( new Double ( currentTickValue ), tickLabel, anchor, rotationAnchor, angle );
                result.add ( tick );
                final double nextTickValue = lowestTickValue + ( i + 1 ) * size;
                for ( int minorTick2 = 1; minorTick2 < minorTickSpaces; ++minorTick2 ) {
                    final double minorTickValue2 = currentTickValue + ( nextTickValue - currentTickValue ) * minorTick2 / minorTickSpaces;
                    if ( this.getRange().contains ( minorTickValue2 ) ) {
                        result.add ( new NumberTick ( TickType.MINOR, minorTickValue2, "", TextAnchor.TOP_CENTER, TextAnchor.CENTER, 0.0 ) );
                    }
                }
            }
        }
        return result;
    }
    protected List refreshTicksVertical ( final Graphics2D g2, final Rectangle2D dataArea, final RectangleEdge edge ) {
        final List result = new ArrayList();
        result.clear();
        final Font tickLabelFont = this.getTickLabelFont();
        g2.setFont ( tickLabelFont );
        if ( this.isAutoTickUnitSelection() ) {
            this.selectAutoTickUnit ( g2, dataArea, edge );
        }
        final TickUnit tu = this.getTickUnit();
        final double size = tu.getSize();
        final int count = this.calculateVisibleTickCount();
        final double lowestTickValue = this.calculateLowestVisibleTickValue();
        if ( count <= 500 ) {
            int minorTickSpaces = this.getMinorTickCount();
            if ( minorTickSpaces <= 0 ) {
                minorTickSpaces = tu.getMinorTickCount();
            }
            for ( int minorTick = 1; minorTick < minorTickSpaces; ++minorTick ) {
                final double minorTickValue = lowestTickValue - size * minorTick / minorTickSpaces;
                if ( this.getRange().contains ( minorTickValue ) ) {
                    result.add ( new NumberTick ( TickType.MINOR, minorTickValue, "", TextAnchor.TOP_CENTER, TextAnchor.CENTER, 0.0 ) );
                }
            }
            for ( int i = 0; i < count; ++i ) {
                final double currentTickValue = lowestTickValue + i * size;
                final NumberFormat formatter = this.getNumberFormatOverride();
                String tickLabel;
                if ( formatter != null ) {
                    tickLabel = formatter.format ( currentTickValue );
                } else {
                    tickLabel = this.getTickUnit().valueToString ( currentTickValue );
                }
                double angle = 0.0;
                TextAnchor anchor;
                TextAnchor rotationAnchor;
                if ( this.isVerticalTickLabels() ) {
                    if ( edge == RectangleEdge.LEFT ) {
                        anchor = TextAnchor.BOTTOM_CENTER;
                        rotationAnchor = TextAnchor.BOTTOM_CENTER;
                        angle = -1.5707963267948966;
                    } else {
                        anchor = TextAnchor.BOTTOM_CENTER;
                        rotationAnchor = TextAnchor.BOTTOM_CENTER;
                        angle = 1.5707963267948966;
                    }
                } else if ( edge == RectangleEdge.LEFT ) {
                    anchor = TextAnchor.CENTER_RIGHT;
                    rotationAnchor = TextAnchor.CENTER_RIGHT;
                } else {
                    anchor = TextAnchor.CENTER_LEFT;
                    rotationAnchor = TextAnchor.CENTER_LEFT;
                }
                final Tick tick = new NumberTick ( new Double ( currentTickValue ), tickLabel, anchor, rotationAnchor, angle );
                result.add ( tick );
                final double nextTickValue = lowestTickValue + ( i + 1 ) * size;
                for ( int minorTick2 = 1; minorTick2 < minorTickSpaces; ++minorTick2 ) {
                    final double minorTickValue2 = currentTickValue + ( nextTickValue - currentTickValue ) * minorTick2 / minorTickSpaces;
                    if ( this.getRange().contains ( minorTickValue2 ) ) {
                        result.add ( new NumberTick ( TickType.MINOR, minorTickValue2, "", TextAnchor.TOP_CENTER, TextAnchor.CENTER, 0.0 ) );
                    }
                }
            }
        }
        return result;
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final NumberAxis clone = ( NumberAxis ) super.clone();
        if ( this.numberFormatOverride != null ) {
            clone.numberFormatOverride = ( NumberFormat ) this.numberFormatOverride.clone();
        }
        return clone;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof NumberAxis ) ) {
            return false;
        }
        final NumberAxis that = ( NumberAxis ) obj;
        return this.autoRangeIncludesZero == that.autoRangeIncludesZero && this.autoRangeStickyZero == that.autoRangeStickyZero && ObjectUtilities.equal ( ( Object ) this.tickUnit, ( Object ) that.tickUnit ) && ObjectUtilities.equal ( ( Object ) this.numberFormatOverride, ( Object ) that.numberFormatOverride ) && this.rangeType.equals ( that.rangeType ) && super.equals ( obj );
    }
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    static {
        DEFAULT_TICK_UNIT = new NumberTickUnit ( 1.0, new DecimalFormat ( "0" ) );
    }
}
