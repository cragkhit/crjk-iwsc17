package org.jfree.chart.axis;
import org.jfree.chart.util.LogFormat;
import java.util.Locale;
import org.jfree.util.ObjectUtilities;
import java.awt.font.LineMetrics;
import org.jfree.chart.util.AttrStringUtils;
import java.awt.font.FontRenderContext;
import org.jfree.ui.RectangleInsets;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.util.Map;
import java.awt.Font;
import java.text.AttributedString;
import org.jfree.ui.TextAnchor;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.plot.PlotRenderingInfo;
import java.awt.Graphics2D;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.ValueAxisPlot;
import org.jfree.ui.RectangleEdge;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.Range;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.Format;
public class LogAxis extends ValueAxis {
    private double base;
    private double baseLog;
    private String baseSymbol;
    private Format baseFormatter;
    private double smallestValue;
    private NumberTickUnit tickUnit;
    private NumberFormat numberFormatOverride;
    public LogAxis() {
        this ( null );
    }
    public LogAxis ( final String label ) {
        super ( label, new NumberTickUnitSource() );
        this.base = 10.0;
        this.baseLog = Math.log ( 10.0 );
        this.baseSymbol = null;
        this.baseFormatter = new DecimalFormat ( "0" );
        this.smallestValue = 1.0E-100;
        this.setDefaultAutoRange ( new Range ( 0.01, 1.0 ) );
        this.tickUnit = new NumberTickUnit ( 1.0, new DecimalFormat ( "0.#" ), 10 );
    }
    public double getBase() {
        return this.base;
    }
    public void setBase ( final double base ) {
        if ( base <= 1.0 ) {
            throw new IllegalArgumentException ( "Requires 'base' > 1.0." );
        }
        this.base = base;
        this.baseLog = Math.log ( base );
        this.fireChangeEvent();
    }
    public String getBaseSymbol() {
        return this.baseSymbol;
    }
    public void setBaseSymbol ( final String symbol ) {
        this.baseSymbol = symbol;
        this.fireChangeEvent();
    }
    public Format getBaseFormatter() {
        return this.baseFormatter;
    }
    public void setBaseFormatter ( final Format formatter ) {
        ParamChecks.nullNotPermitted ( formatter, "formatter" );
        this.baseFormatter = formatter;
        this.fireChangeEvent();
    }
    public double getSmallestValue() {
        return this.smallestValue;
    }
    public void setSmallestValue ( final double value ) {
        if ( value <= 0.0 ) {
            throw new IllegalArgumentException ( "Requires 'value' > 0.0." );
        }
        this.smallestValue = value;
        this.fireChangeEvent();
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
            this.fireChangeEvent();
        }
    }
    public NumberFormat getNumberFormatOverride() {
        return this.numberFormatOverride;
    }
    public void setNumberFormatOverride ( final NumberFormat formatter ) {
        this.numberFormatOverride = formatter;
        this.fireChangeEvent();
    }
    public double calculateLog ( final double value ) {
        return Math.log ( value ) / this.baseLog;
    }
    public double calculateValue ( final double log ) {
        return Math.pow ( this.base, log );
    }
    private double calculateValueNoINF ( final double log ) {
        double result = this.calculateValue ( log );
        if ( Double.isInfinite ( result ) ) {
            result = Double.MAX_VALUE;
        }
        if ( result <= 0.0 ) {
            result = Double.MIN_VALUE;
        }
        return result;
    }
    @Override
    public double java2DToValue ( final double java2DValue, final Rectangle2D area, final RectangleEdge edge ) {
        final Range range = this.getRange();
        final double axisMin = this.calculateLog ( Math.max ( this.smallestValue, range.getLowerBound() ) );
        final double axisMax = this.calculateLog ( range.getUpperBound() );
        double min = 0.0;
        double max = 0.0;
        if ( RectangleEdge.isTopOrBottom ( edge ) ) {
            min = area.getX();
            max = area.getMaxX();
        } else if ( RectangleEdge.isLeftOrRight ( edge ) ) {
            min = area.getMaxY();
            max = area.getY();
        }
        double log;
        if ( this.isInverted() ) {
            log = axisMax - ( java2DValue - min ) / ( max - min ) * ( axisMax - axisMin );
        } else {
            log = axisMin + ( java2DValue - min ) / ( max - min ) * ( axisMax - axisMin );
        }
        return this.calculateValue ( log );
    }
    @Override
    public double valueToJava2D ( double value, final Rectangle2D area, final RectangleEdge edge ) {
        final Range range = this.getRange();
        final double axisMin = this.calculateLog ( range.getLowerBound() );
        final double axisMax = this.calculateLog ( range.getUpperBound() );
        value = this.calculateLog ( value );
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
            double lower = Math.max ( r.getLowerBound(), this.smallestValue );
            final double range = upper - lower;
            final double fixedAutoRange = this.getFixedAutoRange();
            if ( fixedAutoRange > 0.0 ) {
                lower = Math.max ( upper - fixedAutoRange, this.smallestValue );
            } else {
                final double minRange = this.getAutoRangeMinimumSize();
                if ( range < minRange ) {
                    final double expand = ( minRange - range ) / 2.0;
                    upper += expand;
                    lower -= expand;
                }
                double logUpper = this.calculateLog ( upper );
                double logLower = this.calculateLog ( lower );
                final double logRange = logUpper - logLower;
                logUpper += this.getUpperMargin() * logRange;
                logLower -= this.getLowerMargin() * logRange;
                upper = this.calculateValueNoINF ( logUpper );
                lower = this.calculateValueNoINF ( logLower );
            }
            this.setRange ( new Range ( lower, upper ), false, false );
        }
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
        final Range range = this.getRange();
        final List ticks = new ArrayList();
        final Font tickLabelFont = this.getTickLabelFont();
        g2.setFont ( tickLabelFont );
        TextAnchor textAnchor;
        if ( edge == RectangleEdge.TOP ) {
            textAnchor = TextAnchor.BOTTOM_CENTER;
        } else {
            textAnchor = TextAnchor.TOP_CENTER;
        }
        if ( this.isAutoTickUnitSelection() ) {
            this.selectAutoTickUnit ( g2, dataArea, edge );
        }
        final int minorTickCount = this.tickUnit.getMinorTickCount();
        final double unit = this.getTickUnit().getSize();
        final double index = Math.ceil ( this.calculateLog ( this.getRange().getLowerBound() ) / unit );
        final double start = index * unit;
        final double end = this.calculateLog ( this.getUpperBound() );
        double current = start;
        for ( boolean hasTicks = this.tickUnit.getSize() > 0.0 && !Double.isInfinite ( start ); hasTicks && current <= end; current += this.tickUnit.getSize() ) {
            final double v = this.calculateValueNoINF ( current );
            if ( range.contains ( v ) ) {
                ticks.add ( new LogTick ( TickType.MAJOR, v, this.createTickLabel ( v ), textAnchor ) );
            }
            final double next = Math.pow ( this.base, current + this.tickUnit.getSize() );
            for ( int i = 1; i < minorTickCount; ++i ) {
                final double minorV = v + i * ( ( next - v ) / minorTickCount );
                if ( range.contains ( minorV ) ) {
                    ticks.add ( new LogTick ( TickType.MINOR, minorV, null, textAnchor ) );
                }
            }
        }
        return ticks;
    }
    protected List refreshTicksVertical ( final Graphics2D g2, final Rectangle2D dataArea, final RectangleEdge edge ) {
        final Range range = this.getRange();
        final List ticks = new ArrayList();
        final Font tickLabelFont = this.getTickLabelFont();
        g2.setFont ( tickLabelFont );
        TextAnchor textAnchor;
        if ( edge == RectangleEdge.RIGHT ) {
            textAnchor = TextAnchor.CENTER_LEFT;
        } else {
            textAnchor = TextAnchor.CENTER_RIGHT;
        }
        if ( this.isAutoTickUnitSelection() ) {
            this.selectAutoTickUnit ( g2, dataArea, edge );
        }
        final int minorTickCount = this.tickUnit.getMinorTickCount();
        final double unit = this.getTickUnit().getSize();
        final double index = Math.ceil ( this.calculateLog ( this.getRange().getLowerBound() ) / unit );
        final double start = index * unit;
        final double end = this.calculateLog ( this.getUpperBound() );
        double current = start;
        for ( boolean hasTicks = this.tickUnit.getSize() > 0.0 && !Double.isInfinite ( start ); hasTicks && current <= end; current += this.tickUnit.getSize() ) {
            final double v = this.calculateValueNoINF ( current );
            if ( range.contains ( v ) ) {
                ticks.add ( new LogTick ( TickType.MAJOR, v, this.createTickLabel ( v ), textAnchor ) );
            }
            final double next = Math.pow ( this.base, current + this.tickUnit.getSize() );
            for ( int i = 1; i < minorTickCount; ++i ) {
                final double minorV = v + i * ( ( next - v ) / minorTickCount );
                if ( range.contains ( minorV ) ) {
                    ticks.add ( new LogTick ( TickType.MINOR, minorV, null, textAnchor ) );
                }
            }
        }
        return ticks;
    }
    protected void selectAutoTickUnit ( final Graphics2D g2, final Rectangle2D dataArea, final RectangleEdge edge ) {
        if ( RectangleEdge.isTopOrBottom ( edge ) ) {
            this.selectHorizontalAutoTickUnit ( g2, dataArea, edge );
        } else if ( RectangleEdge.isLeftOrRight ( edge ) ) {
            this.selectVerticalAutoTickUnit ( g2, dataArea, edge );
        }
    }
    protected void selectHorizontalAutoTickUnit ( final Graphics2D g2, final Rectangle2D dataArea, final RectangleEdge edge ) {
        final Range range = this.getRange();
        final double logAxisMin = this.calculateLog ( Math.max ( this.smallestValue, range.getLowerBound() ) );
        final double logAxisMax = this.calculateLog ( range.getUpperBound() );
        final double size = ( logAxisMax - logAxisMin ) / 50.0;
        final TickUnitSource tickUnits = this.getStandardTickUnits();
        TickUnit prevCandidate;
        TickUnit candidate = prevCandidate = tickUnits.getCeilingTickUnit ( size );
        boolean found = false;
        while ( !found ) {
            this.tickUnit = ( NumberTickUnit ) candidate;
            final double tickLabelWidth = this.estimateMaximumTickLabelWidth ( g2, candidate );
            final double candidateWidth = this.exponentLengthToJava2D ( candidate.getSize(), dataArea, edge );
            if ( tickLabelWidth < candidateWidth ) {
                found = true;
            } else if ( Double.isNaN ( candidateWidth ) ) {
                candidate = prevCandidate;
                found = true;
            } else {
                prevCandidate = candidate;
                candidate = tickUnits.getLargerTickUnit ( prevCandidate );
                if ( !candidate.equals ( prevCandidate ) ) {
                    continue;
                }
                found = true;
            }
        }
        this.setTickUnit ( ( NumberTickUnit ) candidate, false, false );
    }
    public double exponentLengthToJava2D ( final double length, final Rectangle2D area, final RectangleEdge edge ) {
        final double one = this.valueToJava2D ( this.calculateValueNoINF ( 1.0 ), area, edge );
        final double l = this.valueToJava2D ( this.calculateValueNoINF ( length + 1.0 ), area, edge );
        return Math.abs ( l - one );
    }
    protected void selectVerticalAutoTickUnit ( final Graphics2D g2, final Rectangle2D dataArea, final RectangleEdge edge ) {
        final Range range = this.getRange();
        final double logAxisMin = this.calculateLog ( Math.max ( this.smallestValue, range.getLowerBound() ) );
        final double logAxisMax = this.calculateLog ( range.getUpperBound() );
        final double size = ( logAxisMax - logAxisMin ) / 50.0;
        final TickUnitSource tickUnits = this.getStandardTickUnits();
        TickUnit prevCandidate;
        TickUnit candidate = prevCandidate = tickUnits.getCeilingTickUnit ( size );
        boolean found = false;
        while ( !found ) {
            this.tickUnit = ( NumberTickUnit ) candidate;
            final double tickLabelHeight = this.estimateMaximumTickLabelHeight ( g2 );
            final double candidateHeight = this.exponentLengthToJava2D ( candidate.getSize(), dataArea, edge );
            if ( tickLabelHeight < candidateHeight ) {
                found = true;
            } else if ( Double.isNaN ( candidateHeight ) ) {
                candidate = prevCandidate;
                found = true;
            } else {
                prevCandidate = candidate;
                candidate = tickUnits.getLargerTickUnit ( prevCandidate );
                if ( !candidate.equals ( prevCandidate ) ) {
                    continue;
                }
                found = true;
            }
        }
        this.setTickUnit ( ( NumberTickUnit ) candidate, false, false );
    }
    protected AttributedString createTickLabel ( final double value ) {
        if ( this.numberFormatOverride != null ) {
            return new AttributedString ( this.numberFormatOverride.format ( value ) );
        }
        String baseStr = this.baseSymbol;
        if ( baseStr == null ) {
            baseStr = this.baseFormatter.format ( this.base );
        }
        final double logy = this.calculateLog ( value );
        final String exponentStr = this.getTickUnit().valueToString ( logy );
        final AttributedString as = new AttributedString ( baseStr + exponentStr );
        as.addAttributes ( this.getTickLabelFont().getAttributes(), 0, ( baseStr + exponentStr ).length() );
        as.addAttribute ( TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUPER, baseStr.length(), baseStr.length() + exponentStr.length() );
        return as;
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
            final Range range = this.getRange();
            final double lower = range.getLowerBound();
            final double upper = range.getUpperBound();
            final AttributedString lowerStr = this.createTickLabel ( lower );
            final AttributedString upperStr = this.createTickLabel ( upper );
            final double w1 = AttrStringUtils.getTextBounds ( lowerStr, g2 ).getWidth();
            final double w2 = AttrStringUtils.getTextBounds ( upperStr, g2 ).getWidth();
            result += Math.max ( w1, w2 );
        }
        return result;
    }
    @Override
    public void zoomRange ( final double lowerPercent, final double upperPercent ) {
        final Range range = this.getRange();
        final double start = range.getLowerBound();
        final double end = range.getUpperBound();
        final double log1 = this.calculateLog ( start );
        final double log2 = this.calculateLog ( end );
        final double length = log2 - log1;
        Range adjusted;
        if ( this.isInverted() ) {
            final double logA = log1 + length * ( 1.0 - upperPercent );
            final double logB = log1 + length * ( 1.0 - lowerPercent );
            adjusted = new Range ( this.calculateValueNoINF ( logA ), this.calculateValueNoINF ( logB ) );
        } else {
            final double logA = log1 + length * lowerPercent;
            final double logB = log1 + length * upperPercent;
            adjusted = new Range ( this.calculateValueNoINF ( logA ), this.calculateValueNoINF ( logB ) );
        }
        this.setRange ( adjusted );
    }
    @Override
    public void pan ( final double percent ) {
        final Range range = this.getRange();
        final double lower = range.getLowerBound();
        final double upper = range.getUpperBound();
        double log1 = this.calculateLog ( lower );
        double log2 = this.calculateLog ( upper );
        final double length = log2 - log1;
        final double adj = length * percent;
        log1 += adj;
        log2 += adj;
        this.setRange ( this.calculateValueNoINF ( log1 ), this.calculateValueNoINF ( log2 ) );
    }
    @Override
    public void resizeRange ( final double percent ) {
        final Range range = this.getRange();
        final double logMin = this.calculateLog ( range.getLowerBound() );
        final double logMax = this.calculateLog ( range.getUpperBound() );
        final double centralValue = this.calculateValueNoINF ( ( logMin + logMax ) / 2.0 );
        this.resizeRange ( percent, centralValue );
    }
    @Override
    public void resizeRange ( final double percent, final double anchorValue ) {
        this.resizeRange2 ( percent, anchorValue );
    }
    @Override
    public void resizeRange2 ( final double percent, final double anchorValue ) {
        if ( percent > 0.0 ) {
            final double logAnchorValue = this.calculateLog ( anchorValue );
            final Range range = this.getRange();
            final double logAxisMin = this.calculateLog ( range.getLowerBound() );
            final double logAxisMax = this.calculateLog ( range.getUpperBound() );
            final double left = percent * ( logAnchorValue - logAxisMin );
            final double right = percent * ( logAxisMax - logAnchorValue );
            final double upperBound = this.calculateValueNoINF ( logAnchorValue + right );
            final Range adjusted = new Range ( this.calculateValueNoINF ( logAnchorValue - left ), upperBound );
            this.setRange ( adjusted );
        } else {
            this.setAutoRange ( true );
        }
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof LogAxis ) ) {
            return false;
        }
        final LogAxis that = ( LogAxis ) obj;
        return this.base == that.base && ObjectUtilities.equal ( ( Object ) this.baseSymbol, ( Object ) that.baseSymbol ) && this.baseFormatter.equals ( that.baseFormatter ) && this.smallestValue == that.smallestValue && ObjectUtilities.equal ( ( Object ) this.numberFormatOverride, ( Object ) that.numberFormatOverride ) && super.equals ( obj );
    }
    @Override
    public int hashCode() {
        int result = 193;
        long temp = Double.doubleToLongBits ( this.base );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        temp = Double.doubleToLongBits ( this.smallestValue );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        if ( this.numberFormatOverride != null ) {
            result = 37 * result + this.numberFormatOverride.hashCode();
        }
        result = 37 * result + this.tickUnit.hashCode();
        return result;
    }
    public static TickUnitSource createLogTickUnits ( final Locale locale ) {
        final TickUnits units = new TickUnits();
        final NumberFormat numberFormat = new LogFormat();
        units.add ( new NumberTickUnit ( 0.05, numberFormat, 2 ) );
        units.add ( new NumberTickUnit ( 0.1, numberFormat, 10 ) );
        units.add ( new NumberTickUnit ( 0.2, numberFormat, 2 ) );
        units.add ( new NumberTickUnit ( 0.5, numberFormat, 5 ) );
        units.add ( new NumberTickUnit ( 1.0, numberFormat, 10 ) );
        units.add ( new NumberTickUnit ( 2.0, numberFormat, 10 ) );
        units.add ( new NumberTickUnit ( 3.0, numberFormat, 15 ) );
        units.add ( new NumberTickUnit ( 4.0, numberFormat, 20 ) );
        units.add ( new NumberTickUnit ( 5.0, numberFormat, 25 ) );
        units.add ( new NumberTickUnit ( 6.0, numberFormat ) );
        units.add ( new NumberTickUnit ( 7.0, numberFormat ) );
        units.add ( new NumberTickUnit ( 8.0, numberFormat ) );
        units.add ( new NumberTickUnit ( 9.0, numberFormat ) );
        units.add ( new NumberTickUnit ( 10.0, numberFormat ) );
        return units;
    }
}
