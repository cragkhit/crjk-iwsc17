package org.jfree.chart.axis;
import java.awt.Color;
import java.awt.BasicStroke;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.text.TextUtilities;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import java.awt.FontMetrics;
import java.awt.Shape;
import java.awt.geom.Line2D;
import org.jfree.data.Range;
import java.text.NumberFormat;
import java.awt.Font;
import org.jfree.ui.TextAnchor;
import java.util.ArrayList;
import java.util.List;
import org.jfree.ui.RectangleEdge;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.chart.util.ParamChecks;
import java.awt.Paint;
import java.awt.Stroke;
public class CyclicNumberAxis extends NumberAxis {
    static final long serialVersionUID = -7514160997164582554L;
    public static Stroke DEFAULT_ADVANCE_LINE_STROKE;
    public static final Paint DEFAULT_ADVANCE_LINE_PAINT;
    protected double offset;
    protected double period;
    protected boolean boundMappedToLastCycle;
    protected boolean advanceLineVisible;
    protected transient Stroke advanceLineStroke;
    protected transient Paint advanceLinePaint;
    private transient boolean internalMarkerWhenTicksOverlap;
    private transient Tick internalMarkerCycleBoundTick;
    public CyclicNumberAxis ( final double period ) {
        this ( period, 0.0 );
    }
    public CyclicNumberAxis ( final double period, final double offset ) {
        this ( period, offset, null );
    }
    public CyclicNumberAxis ( final double period, final String label ) {
        this ( 0.0, period, label );
    }
    public CyclicNumberAxis ( final double period, final double offset, final String label ) {
        super ( label );
        this.advanceLineStroke = CyclicNumberAxis.DEFAULT_ADVANCE_LINE_STROKE;
        this.period = period;
        this.offset = offset;
        this.setFixedAutoRange ( period );
        this.advanceLineVisible = true;
        this.advanceLinePaint = CyclicNumberAxis.DEFAULT_ADVANCE_LINE_PAINT;
    }
    public boolean isAdvanceLineVisible() {
        return this.advanceLineVisible;
    }
    public void setAdvanceLineVisible ( final boolean visible ) {
        this.advanceLineVisible = visible;
    }
    public Paint getAdvanceLinePaint() {
        return this.advanceLinePaint;
    }
    public void setAdvanceLinePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.advanceLinePaint = paint;
    }
    public Stroke getAdvanceLineStroke() {
        return this.advanceLineStroke;
    }
    public void setAdvanceLineStroke ( final Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.advanceLineStroke = stroke;
    }
    public boolean isBoundMappedToLastCycle() {
        return this.boundMappedToLastCycle;
    }
    public void setBoundMappedToLastCycle ( final boolean boundMappedToLastCycle ) {
        this.boundMappedToLastCycle = boundMappedToLastCycle;
    }
    protected void selectHorizontalAutoTickUnit ( final Graphics2D g2, final Rectangle2D drawArea, final Rectangle2D dataArea, final RectangleEdge edge ) {
        final double tickLabelWidth = this.estimateMaximumTickLabelWidth ( g2, this.getTickUnit() );
        final double n = this.getRange().getLength() * tickLabelWidth / dataArea.getWidth();
        this.setTickUnit ( ( NumberTickUnit ) this.getStandardTickUnits().getCeilingTickUnit ( n ), false, false );
    }
    protected void selectVerticalAutoTickUnit ( final Graphics2D g2, final Rectangle2D drawArea, final Rectangle2D dataArea, final RectangleEdge edge ) {
        final double tickLabelWidth = this.estimateMaximumTickLabelWidth ( g2, this.getTickUnit() );
        final double n = this.getRange().getLength() * tickLabelWidth / dataArea.getHeight();
        this.setTickUnit ( ( NumberTickUnit ) this.getStandardTickUnits().getCeilingTickUnit ( n ), false, false );
    }
    @Override
    protected float[] calculateAnchorPoint ( final ValueTick tick, final double cursor, final Rectangle2D dataArea, final RectangleEdge edge ) {
        if ( tick instanceof CycleBoundTick ) {
            final boolean mapsav = this.boundMappedToLastCycle;
            this.boundMappedToLastCycle = ( ( CycleBoundTick ) tick ).mapToLastCycle;
            final float[] ret = super.calculateAnchorPoint ( tick, cursor, dataArea, edge );
            this.boundMappedToLastCycle = mapsav;
            return ret;
        }
        return super.calculateAnchorPoint ( tick, cursor, dataArea, edge );
    }
    @Override
    protected List refreshTicksHorizontal ( final Graphics2D g2, final Rectangle2D dataArea, final RectangleEdge edge ) {
        final List result = new ArrayList();
        final Font tickLabelFont = this.getTickLabelFont();
        g2.setFont ( tickLabelFont );
        if ( this.isAutoTickUnitSelection() ) {
            this.selectAutoTickUnit ( g2, dataArea, edge );
        }
        final double unit = this.getTickUnit().getSize();
        final double cycleBound = this.getCycleBound();
        double currentTickValue = Math.ceil ( cycleBound / unit ) * unit;
        double upperValue = this.getRange().getUpperBound();
        boolean cycled = false;
        final boolean boundMapping = this.boundMappedToLastCycle;
        this.boundMappedToLastCycle = false;
        CycleBoundTick lastTick = null;
        float lastX = 0.0f;
        if ( upperValue == cycleBound ) {
            currentTickValue = this.calculateLowestVisibleTickValue();
            cycled = true;
            this.boundMappedToLastCycle = true;
        }
        while ( currentTickValue <= upperValue ) {
            boolean cyclenow = false;
            if ( currentTickValue + unit > upperValue && !cycled ) {
                cyclenow = true;
            }
            final double xx = this.valueToJava2D ( currentTickValue, dataArea, edge );
            final NumberFormat formatter = this.getNumberFormatOverride();
            String tickLabel;
            if ( formatter != null ) {
                tickLabel = formatter.format ( currentTickValue );
            } else {
                tickLabel = this.getTickUnit().valueToString ( currentTickValue );
            }
            final float x = ( float ) xx;
            double angle = 0.0;
            TextAnchor anchor;
            TextAnchor rotationAnchor;
            if ( this.isVerticalTickLabels() ) {
                if ( edge == RectangleEdge.TOP ) {
                    angle = 1.5707963267948966;
                } else {
                    angle = -1.5707963267948966;
                }
                anchor = TextAnchor.CENTER_RIGHT;
                if ( lastTick != null && lastX == x && currentTickValue != cycleBound ) {
                    anchor = ( this.isInverted() ? TextAnchor.TOP_RIGHT : TextAnchor.BOTTOM_RIGHT );
                    result.remove ( result.size() - 1 );
                    result.add ( new CycleBoundTick ( this.boundMappedToLastCycle, lastTick.getNumber(), lastTick.getText(), anchor, anchor, lastTick.getAngle() ) );
                    this.internalMarkerWhenTicksOverlap = true;
                    anchor = ( this.isInverted() ? TextAnchor.BOTTOM_RIGHT : TextAnchor.TOP_RIGHT );
                }
                rotationAnchor = anchor;
            } else if ( edge == RectangleEdge.TOP ) {
                anchor = TextAnchor.BOTTOM_CENTER;
                if ( lastTick != null && lastX == x && currentTickValue != cycleBound ) {
                    anchor = ( this.isInverted() ? TextAnchor.BOTTOM_LEFT : TextAnchor.BOTTOM_RIGHT );
                    result.remove ( result.size() - 1 );
                    result.add ( new CycleBoundTick ( this.boundMappedToLastCycle, lastTick.getNumber(), lastTick.getText(), anchor, anchor, lastTick.getAngle() ) );
                    this.internalMarkerWhenTicksOverlap = true;
                    anchor = ( this.isInverted() ? TextAnchor.BOTTOM_RIGHT : TextAnchor.BOTTOM_LEFT );
                }
                rotationAnchor = anchor;
            } else {
                anchor = TextAnchor.TOP_CENTER;
                if ( lastTick != null && lastX == x && currentTickValue != cycleBound ) {
                    anchor = ( this.isInverted() ? TextAnchor.TOP_LEFT : TextAnchor.TOP_RIGHT );
                    result.remove ( result.size() - 1 );
                    result.add ( new CycleBoundTick ( this.boundMappedToLastCycle, lastTick.getNumber(), lastTick.getText(), anchor, anchor, lastTick.getAngle() ) );
                    this.internalMarkerWhenTicksOverlap = true;
                    anchor = ( this.isInverted() ? TextAnchor.TOP_RIGHT : TextAnchor.TOP_LEFT );
                }
                rotationAnchor = anchor;
            }
            final CycleBoundTick tick = new CycleBoundTick ( this.boundMappedToLastCycle, new Double ( currentTickValue ), tickLabel, anchor, rotationAnchor, angle );
            if ( currentTickValue == cycleBound ) {
                this.internalMarkerCycleBoundTick = tick;
            }
            result.add ( tick );
            lastTick = tick;
            lastX = x;
            currentTickValue += unit;
            if ( cyclenow ) {
                currentTickValue = this.calculateLowestVisibleTickValue();
                upperValue = cycleBound;
                cycled = true;
                this.boundMappedToLastCycle = true;
            }
        }
        this.boundMappedToLastCycle = boundMapping;
        return result;
    }
    protected List refreshVerticalTicks ( final Graphics2D g2, final Rectangle2D dataArea, final RectangleEdge edge ) {
        final List result = new ArrayList();
        result.clear();
        final Font tickLabelFont = this.getTickLabelFont();
        g2.setFont ( tickLabelFont );
        if ( this.isAutoTickUnitSelection() ) {
            this.selectAutoTickUnit ( g2, dataArea, edge );
        }
        final double unit = this.getTickUnit().getSize();
        final double cycleBound = this.getCycleBound();
        double currentTickValue = Math.ceil ( cycleBound / unit ) * unit;
        double upperValue = this.getRange().getUpperBound();
        boolean cycled = false;
        final boolean boundMapping = this.boundMappedToLastCycle;
        this.boundMappedToLastCycle = true;
        NumberTick lastTick = null;
        float lastY = 0.0f;
        if ( upperValue == cycleBound ) {
            currentTickValue = this.calculateLowestVisibleTickValue();
            cycled = true;
            this.boundMappedToLastCycle = true;
        }
        while ( currentTickValue <= upperValue ) {
            boolean cyclenow = false;
            if ( currentTickValue + unit > upperValue && !cycled ) {
                cyclenow = true;
            }
            final double yy = this.valueToJava2D ( currentTickValue, dataArea, edge );
            final NumberFormat formatter = this.getNumberFormatOverride();
            String tickLabel;
            if ( formatter != null ) {
                tickLabel = formatter.format ( currentTickValue );
            } else {
                tickLabel = this.getTickUnit().valueToString ( currentTickValue );
            }
            final float y = ( float ) yy;
            double angle = 0.0;
            TextAnchor anchor;
            TextAnchor rotationAnchor;
            if ( this.isVerticalTickLabels() ) {
                if ( edge == RectangleEdge.LEFT ) {
                    anchor = TextAnchor.BOTTOM_CENTER;
                    if ( lastTick != null && lastY == y && currentTickValue != cycleBound ) {
                        anchor = ( this.isInverted() ? TextAnchor.BOTTOM_LEFT : TextAnchor.BOTTOM_RIGHT );
                        result.remove ( result.size() - 1 );
                        result.add ( new CycleBoundTick ( this.boundMappedToLastCycle, lastTick.getNumber(), lastTick.getText(), anchor, anchor, lastTick.getAngle() ) );
                        this.internalMarkerWhenTicksOverlap = true;
                        anchor = ( this.isInverted() ? TextAnchor.BOTTOM_RIGHT : TextAnchor.BOTTOM_LEFT );
                    }
                    rotationAnchor = anchor;
                    angle = -1.5707963267948966;
                } else {
                    anchor = TextAnchor.BOTTOM_CENTER;
                    if ( lastTick != null && lastY == y && currentTickValue != cycleBound ) {
                        anchor = ( this.isInverted() ? TextAnchor.BOTTOM_RIGHT : TextAnchor.BOTTOM_LEFT );
                        result.remove ( result.size() - 1 );
                        result.add ( new CycleBoundTick ( this.boundMappedToLastCycle, lastTick.getNumber(), lastTick.getText(), anchor, anchor, lastTick.getAngle() ) );
                        this.internalMarkerWhenTicksOverlap = true;
                        anchor = ( this.isInverted() ? TextAnchor.BOTTOM_LEFT : TextAnchor.BOTTOM_RIGHT );
                    }
                    rotationAnchor = anchor;
                    angle = 1.5707963267948966;
                }
            } else if ( edge == RectangleEdge.LEFT ) {
                anchor = TextAnchor.CENTER_RIGHT;
                if ( lastTick != null && lastY == y && currentTickValue != cycleBound ) {
                    anchor = ( this.isInverted() ? TextAnchor.BOTTOM_RIGHT : TextAnchor.TOP_RIGHT );
                    result.remove ( result.size() - 1 );
                    result.add ( new CycleBoundTick ( this.boundMappedToLastCycle, lastTick.getNumber(), lastTick.getText(), anchor, anchor, lastTick.getAngle() ) );
                    this.internalMarkerWhenTicksOverlap = true;
                    anchor = ( this.isInverted() ? TextAnchor.TOP_RIGHT : TextAnchor.BOTTOM_RIGHT );
                }
                rotationAnchor = anchor;
            } else {
                anchor = TextAnchor.CENTER_LEFT;
                if ( lastTick != null && lastY == y && currentTickValue != cycleBound ) {
                    anchor = ( this.isInverted() ? TextAnchor.BOTTOM_LEFT : TextAnchor.TOP_LEFT );
                    result.remove ( result.size() - 1 );
                    result.add ( new CycleBoundTick ( this.boundMappedToLastCycle, lastTick.getNumber(), lastTick.getText(), anchor, anchor, lastTick.getAngle() ) );
                    this.internalMarkerWhenTicksOverlap = true;
                    anchor = ( this.isInverted() ? TextAnchor.TOP_LEFT : TextAnchor.BOTTOM_LEFT );
                }
                rotationAnchor = anchor;
            }
            final CycleBoundTick tick = new CycleBoundTick ( this.boundMappedToLastCycle, new Double ( currentTickValue ), tickLabel, anchor, rotationAnchor, angle );
            if ( currentTickValue == cycleBound ) {
                this.internalMarkerCycleBoundTick = tick;
            }
            result.add ( tick );
            lastTick = tick;
            lastY = y;
            if ( currentTickValue == cycleBound ) {
                this.internalMarkerCycleBoundTick = tick;
            }
            currentTickValue += unit;
            if ( cyclenow ) {
                currentTickValue = this.calculateLowestVisibleTickValue();
                upperValue = cycleBound;
                cycled = true;
                this.boundMappedToLastCycle = false;
            }
        }
        this.boundMappedToLastCycle = boundMapping;
        return result;
    }
    @Override
    public double java2DToValue ( final double java2DValue, final Rectangle2D dataArea, final RectangleEdge edge ) {
        final Range range = this.getRange();
        final double vmax = range.getUpperBound();
        final double vp = this.getCycleBound();
        double jmin = 0.0;
        double jmax = 0.0;
        if ( RectangleEdge.isTopOrBottom ( edge ) ) {
            jmin = dataArea.getMinX();
            jmax = dataArea.getMaxX();
        } else if ( RectangleEdge.isLeftOrRight ( edge ) ) {
            jmin = dataArea.getMaxY();
            jmax = dataArea.getMinY();
        }
        if ( this.isInverted() ) {
            final double jbreak = jmax - ( vmax - vp ) * ( jmax - jmin ) / this.period;
            if ( java2DValue >= jbreak ) {
                return vp + ( jmax - java2DValue ) * this.period / ( jmax - jmin );
            }
            return vp - ( java2DValue - jmin ) * this.period / ( jmax - jmin );
        } else {
            final double jbreak = ( vmax - vp ) * ( jmax - jmin ) / this.period + jmin;
            if ( java2DValue <= jbreak ) {
                return vp + ( java2DValue - jmin ) * this.period / ( jmax - jmin );
            }
            return vp - ( jmax - java2DValue ) * this.period / ( jmax - jmin );
        }
    }
    @Override
    public double valueToJava2D ( final double value, final Rectangle2D dataArea, final RectangleEdge edge ) {
        final Range range = this.getRange();
        final double vmin = range.getLowerBound();
        final double vmax = range.getUpperBound();
        final double vp = this.getCycleBound();
        if ( value < vmin || value > vmax ) {
            return Double.NaN;
        }
        double jmin = 0.0;
        double jmax = 0.0;
        if ( RectangleEdge.isTopOrBottom ( edge ) ) {
            jmin = dataArea.getMinX();
            jmax = dataArea.getMaxX();
        } else if ( RectangleEdge.isLeftOrRight ( edge ) ) {
            jmax = dataArea.getMinY();
            jmin = dataArea.getMaxY();
        }
        if ( this.isInverted() ) {
            if ( value == vp ) {
                return this.boundMappedToLastCycle ? jmin : jmax;
            }
            if ( value > vp ) {
                return jmax - ( value - vp ) * ( jmax - jmin ) / this.period;
            }
            return jmin + ( vp - value ) * ( jmax - jmin ) / this.period;
        } else {
            if ( value == vp ) {
                return this.boundMappedToLastCycle ? jmax : jmin;
            }
            if ( value >= vp ) {
                return jmin + ( value - vp ) * ( jmax - jmin ) / this.period;
            }
            return jmax - ( vp - value ) * ( jmax - jmin ) / this.period;
        }
    }
    @Override
    public void centerRange ( final double value ) {
        this.setRange ( value - this.period / 2.0, value + this.period / 2.0 );
    }
    @Override
    public void setAutoRangeMinimumSize ( final double size, final boolean notify ) {
        if ( size > this.period ) {
            this.period = size;
        }
        super.setAutoRangeMinimumSize ( size, notify );
    }
    @Override
    public void setFixedAutoRange ( final double length ) {
        super.setFixedAutoRange ( this.period = length );
    }
    @Override
    public void setRange ( final Range range, final boolean turnOffAutoRange, final boolean notify ) {
        final double size = range.getUpperBound() - range.getLowerBound();
        if ( size > this.period ) {
            this.period = size;
        }
        super.setRange ( range, turnOffAutoRange, notify );
    }
    public double getCycleBound() {
        return Math.floor ( ( this.getRange().getUpperBound() - this.offset ) / this.period ) * this.period + this.offset;
    }
    public double getOffset() {
        return this.offset;
    }
    public void setOffset ( final double offset ) {
        this.offset = offset;
    }
    public double getPeriod() {
        return this.period;
    }
    public void setPeriod ( final double period ) {
        this.period = period;
    }
    @Override
    protected AxisState drawTickMarksAndLabels ( final Graphics2D g2, final double cursor, final Rectangle2D plotArea, final Rectangle2D dataArea, final RectangleEdge edge ) {
        this.internalMarkerWhenTicksOverlap = false;
        final AxisState ret = super.drawTickMarksAndLabels ( g2, cursor, plotArea, dataArea, edge );
        if ( !this.internalMarkerWhenTicksOverlap ) {
            return ret;
        }
        final FontMetrics fm = g2.getFontMetrics ( this.getTickLabelFont() );
        double ol;
        if ( this.isVerticalTickLabels() ) {
            ol = fm.getMaxAdvance();
        } else {
            ol = fm.getHeight();
        }
        final double il = 0.0;
        if ( this.isTickMarksVisible() ) {
            final float xx = ( float ) this.valueToJava2D ( this.getRange().getUpperBound(), dataArea, edge );
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
        return ret;
    }
    @Override
    public AxisState draw ( final Graphics2D g2, final double cursor, final Rectangle2D plotArea, final Rectangle2D dataArea, final RectangleEdge edge, final PlotRenderingInfo plotState ) {
        final AxisState ret = super.draw ( g2, cursor, plotArea, dataArea, edge, plotState );
        if ( this.isAdvanceLineVisible() ) {
            final double xx = this.valueToJava2D ( this.getRange().getUpperBound(), dataArea, edge );
            Line2D mark = null;
            g2.setStroke ( this.getAdvanceLineStroke() );
            g2.setPaint ( this.getAdvanceLinePaint() );
            if ( edge == RectangleEdge.LEFT ) {
                mark = new Line2D.Double ( cursor, xx, cursor + dataArea.getWidth(), xx );
            } else if ( edge == RectangleEdge.RIGHT ) {
                mark = new Line2D.Double ( cursor - dataArea.getWidth(), xx, cursor, xx );
            } else if ( edge == RectangleEdge.TOP ) {
                mark = new Line2D.Double ( xx, cursor + dataArea.getHeight(), xx, cursor );
            } else if ( edge == RectangleEdge.BOTTOM ) {
                mark = new Line2D.Double ( xx, cursor, xx, cursor - dataArea.getHeight() );
            }
            g2.draw ( mark );
        }
        return ret;
    }
    @Override
    public AxisSpace reserveSpace ( final Graphics2D g2, final Plot plot, final Rectangle2D plotArea, final RectangleEdge edge, final AxisSpace space ) {
        this.internalMarkerCycleBoundTick = null;
        final AxisSpace ret = super.reserveSpace ( g2, plot, plotArea, edge, space );
        if ( this.internalMarkerCycleBoundTick == null ) {
            return ret;
        }
        final FontMetrics fm = g2.getFontMetrics ( this.getTickLabelFont() );
        final Rectangle2D r = TextUtilities.getTextBounds ( this.internalMarkerCycleBoundTick.getText(), g2, fm );
        if ( RectangleEdge.isTopOrBottom ( edge ) ) {
            if ( this.isVerticalTickLabels() ) {
                space.add ( r.getHeight() / 2.0, RectangleEdge.RIGHT );
            } else {
                space.add ( r.getWidth() / 2.0, RectangleEdge.RIGHT );
            }
        } else if ( RectangleEdge.isLeftOrRight ( edge ) ) {
            if ( this.isVerticalTickLabels() ) {
                space.add ( r.getWidth() / 2.0, RectangleEdge.TOP );
            } else {
                space.add ( r.getHeight() / 2.0, RectangleEdge.TOP );
            }
        }
        return ret;
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.advanceLinePaint, stream );
        SerialUtilities.writeStroke ( this.advanceLineStroke, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.advanceLinePaint = SerialUtilities.readPaint ( stream );
        this.advanceLineStroke = SerialUtilities.readStroke ( stream );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof CyclicNumberAxis ) ) {
            return false;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        final CyclicNumberAxis that = ( CyclicNumberAxis ) obj;
        return this.period == that.period && this.offset == that.offset && PaintUtilities.equal ( this.advanceLinePaint, that.advanceLinePaint ) && ObjectUtilities.equal ( ( Object ) this.advanceLineStroke, ( Object ) that.advanceLineStroke ) && this.advanceLineVisible == that.advanceLineVisible && this.boundMappedToLastCycle == that.boundMappedToLastCycle;
    }
    static {
        CyclicNumberAxis.DEFAULT_ADVANCE_LINE_STROKE = new BasicStroke ( 1.0f );
        DEFAULT_ADVANCE_LINE_PAINT = Color.gray;
    }
    protected static class CycleBoundTick extends NumberTick {
        public boolean mapToLastCycle;
        public CycleBoundTick ( final boolean mapToLastCycle, final Number number, final String label, final TextAnchor textAnchor, final TextAnchor rotationAnchor, final double angle ) {
            super ( number, label, textAnchor, rotationAnchor, angle );
            this.mapToLastCycle = mapToLastCycle;
        }
    }
}
