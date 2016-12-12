package org.jfree.chart.axis;
import java.awt.Color;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.PaintUtilities;
import java.text.NumberFormat;
import java.awt.Font;
import org.jfree.ui.TextAnchor;
import org.jfree.text.TextUtilities;
import java.util.ArrayList;
import org.jfree.chart.plot.Plot;
import org.jfree.data.Range;
import org.jfree.chart.plot.ValueAxisPlot;
import java.util.Iterator;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.Shape;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.ui.RectangleEdge;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.chart.util.ParamChecks;
import java.util.Arrays;
import java.util.List;
import java.awt.Paint;
import java.io.Serializable;
public class SymbolAxis extends NumberAxis implements Serializable {
    private static final long serialVersionUID = 7216330468770619716L;
    public static final Paint DEFAULT_GRID_BAND_PAINT;
    public static final Paint DEFAULT_GRID_BAND_ALTERNATE_PAINT;
    private List symbols;
    private boolean gridBandsVisible;
    private transient Paint gridBandPaint;
    private transient Paint gridBandAlternatePaint;
    public SymbolAxis ( final String label, final String[] sv ) {
        super ( label );
        this.symbols = Arrays.asList ( sv );
        this.gridBandsVisible = true;
        this.gridBandPaint = SymbolAxis.DEFAULT_GRID_BAND_PAINT;
        this.gridBandAlternatePaint = SymbolAxis.DEFAULT_GRID_BAND_ALTERNATE_PAINT;
        this.setAutoTickUnitSelection ( false, false );
        this.setAutoRangeStickyZero ( false );
    }
    public String[] getSymbols() {
        String[] result = new String[this.symbols.size()];
        result = this.symbols.toArray ( result );
        return result;
    }
    public boolean isGridBandsVisible() {
        return this.gridBandsVisible;
    }
    public void setGridBandsVisible ( final boolean flag ) {
        this.gridBandsVisible = flag;
        this.fireChangeEvent();
    }
    public Paint getGridBandPaint() {
        return this.gridBandPaint;
    }
    public void setGridBandPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.gridBandPaint = paint;
        this.fireChangeEvent();
    }
    public Paint getGridBandAlternatePaint() {
        return this.gridBandAlternatePaint;
    }
    public void setGridBandAlternatePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.gridBandAlternatePaint = paint;
        this.fireChangeEvent();
    }
    @Override
    protected void selectAutoTickUnit ( final Graphics2D g2, final Rectangle2D dataArea, final RectangleEdge edge ) {
        throw new UnsupportedOperationException();
    }
    @Override
    public AxisState draw ( final Graphics2D g2, final double cursor, final Rectangle2D plotArea, final Rectangle2D dataArea, final RectangleEdge edge, final PlotRenderingInfo plotState ) {
        AxisState info = new AxisState ( cursor );
        if ( this.isVisible() ) {
            info = super.draw ( g2, cursor, plotArea, dataArea, edge, plotState );
        }
        if ( this.gridBandsVisible ) {
            this.drawGridBands ( g2, plotArea, dataArea, edge, info.getTicks() );
        }
        return info;
    }
    protected void drawGridBands ( final Graphics2D g2, final Rectangle2D plotArea, final Rectangle2D dataArea, final RectangleEdge edge, final List ticks ) {
        final Shape savedClip = g2.getClip();
        g2.clip ( dataArea );
        if ( RectangleEdge.isTopOrBottom ( edge ) ) {
            this.drawGridBandsHorizontal ( g2, plotArea, dataArea, true, ticks );
        } else if ( RectangleEdge.isLeftOrRight ( edge ) ) {
            this.drawGridBandsVertical ( g2, plotArea, dataArea, true, ticks );
        }
        g2.setClip ( savedClip );
    }
    protected void drawGridBandsHorizontal ( final Graphics2D g2, final Rectangle2D plotArea, final Rectangle2D dataArea, final boolean firstGridBandIsDark, final List ticks ) {
        boolean currentGridBandIsDark = firstGridBandIsDark;
        final double yy = dataArea.getY();
        double outlineStrokeWidth = 1.0;
        final Stroke outlineStroke = this.getPlot().getOutlineStroke();
        if ( outlineStroke != null && outlineStroke instanceof BasicStroke ) {
            outlineStrokeWidth = ( ( BasicStroke ) outlineStroke ).getLineWidth();
        }
        for ( final ValueTick tick : ticks ) {
            final double xx1 = this.valueToJava2D ( tick.getValue() - 0.5, dataArea, RectangleEdge.BOTTOM );
            final double xx2 = this.valueToJava2D ( tick.getValue() + 0.5, dataArea, RectangleEdge.BOTTOM );
            if ( currentGridBandIsDark ) {
                g2.setPaint ( this.gridBandPaint );
            } else {
                g2.setPaint ( this.gridBandAlternatePaint );
            }
            final Rectangle2D band = new Rectangle2D.Double ( Math.min ( xx1, xx2 ), yy + outlineStrokeWidth, Math.abs ( xx2 - xx1 ), dataArea.getMaxY() - yy - outlineStrokeWidth );
            g2.fill ( band );
            currentGridBandIsDark = !currentGridBandIsDark;
        }
    }
    protected void drawGridBandsVertical ( final Graphics2D g2, final Rectangle2D plotArea, final Rectangle2D dataArea, final boolean firstGridBandIsDark, final List ticks ) {
        boolean currentGridBandIsDark = firstGridBandIsDark;
        final double xx = dataArea.getX();
        double outlineStrokeWidth = 1.0;
        final Stroke outlineStroke = this.getPlot().getOutlineStroke();
        if ( outlineStroke != null && outlineStroke instanceof BasicStroke ) {
            outlineStrokeWidth = ( ( BasicStroke ) outlineStroke ).getLineWidth();
        }
        for ( final ValueTick tick : ticks ) {
            final double yy1 = this.valueToJava2D ( tick.getValue() + 0.5, dataArea, RectangleEdge.LEFT );
            final double yy2 = this.valueToJava2D ( tick.getValue() - 0.5, dataArea, RectangleEdge.LEFT );
            if ( currentGridBandIsDark ) {
                g2.setPaint ( this.gridBandPaint );
            } else {
                g2.setPaint ( this.gridBandAlternatePaint );
            }
            final Rectangle2D band = new Rectangle2D.Double ( xx + outlineStrokeWidth, Math.min ( yy1, yy2 ), dataArea.getMaxX() - xx - outlineStrokeWidth, Math.abs ( yy2 - yy1 ) );
            g2.fill ( band );
            currentGridBandIsDark = !currentGridBandIsDark;
        }
    }
    @Override
    protected void autoAdjustRange() {
        final Plot plot = this.getPlot();
        if ( plot == null ) {
            return;
        }
        if ( plot instanceof ValueAxisPlot ) {
            double upper = this.symbols.size() - 1;
            double lower = 0.0;
            final double range = upper - lower;
            final double minRange = this.getAutoRangeMinimumSize();
            if ( range < minRange ) {
                upper = ( upper + lower + minRange ) / 2.0;
                lower = ( upper + lower - minRange ) / 2.0;
            }
            final double upperMargin = 0.5;
            final double lowerMargin = 0.5;
            if ( this.getAutoRangeIncludesZero() ) {
                if ( this.getAutoRangeStickyZero() ) {
                    if ( upper <= 0.0 ) {
                        upper = 0.0;
                    } else {
                        upper += upperMargin;
                    }
                    if ( lower >= 0.0 ) {
                        lower = 0.0;
                    } else {
                        lower -= lowerMargin;
                    }
                } else {
                    upper = Math.max ( 0.0, upper + upperMargin );
                    lower = Math.min ( 0.0, lower - lowerMargin );
                }
            } else if ( this.getAutoRangeStickyZero() ) {
                if ( upper <= 0.0 ) {
                    upper = Math.min ( 0.0, upper + upperMargin );
                } else {
                    upper += upperMargin * range;
                }
                if ( lower >= 0.0 ) {
                    lower = Math.max ( 0.0, lower - lowerMargin );
                } else {
                    lower -= lowerMargin;
                }
            } else {
                upper += upperMargin;
                lower -= lowerMargin;
            }
            this.setRange ( new Range ( lower, upper ), false, false );
        }
    }
    @Override
    public List refreshTicks ( final Graphics2D g2, final AxisState state, final Rectangle2D dataArea, final RectangleEdge edge ) {
        List ticks = null;
        if ( RectangleEdge.isTopOrBottom ( edge ) ) {
            ticks = this.refreshTicksHorizontal ( g2, dataArea, edge );
        } else if ( RectangleEdge.isLeftOrRight ( edge ) ) {
            ticks = this.refreshTicksVertical ( g2, dataArea, edge );
        }
        return ticks;
    }
    @Override
    protected List refreshTicksHorizontal ( final Graphics2D g2, final Rectangle2D dataArea, final RectangleEdge edge ) {
        final List ticks = new ArrayList();
        final Font tickLabelFont = this.getTickLabelFont();
        g2.setFont ( tickLabelFont );
        final double size = this.getTickUnit().getSize();
        final int count = this.calculateVisibleTickCount();
        final double lowestTickValue = this.calculateLowestVisibleTickValue();
        double previousDrawnTickLabelPos = 0.0;
        double previousDrawnTickLabelLength = 0.0;
        if ( count <= 500 ) {
            for ( int i = 0; i < count; ++i ) {
                final double currentTickValue = lowestTickValue + i * size;
                final double xx = this.valueToJava2D ( currentTickValue, dataArea, edge );
                final NumberFormat formatter = this.getNumberFormatOverride();
                String tickLabel;
                if ( formatter != null ) {
                    tickLabel = formatter.format ( currentTickValue );
                } else {
                    tickLabel = this.valueToString ( currentTickValue );
                }
                final Rectangle2D bounds = TextUtilities.getTextBounds ( tickLabel, g2, g2.getFontMetrics() );
                final double tickLabelLength = this.isVerticalTickLabels() ? bounds.getHeight() : bounds.getWidth();
                boolean tickLabelsOverlapping = false;
                if ( i > 0 ) {
                    final double avgTickLabelLength = ( previousDrawnTickLabelLength + tickLabelLength ) / 2.0;
                    if ( Math.abs ( xx - previousDrawnTickLabelPos ) < avgTickLabelLength ) {
                        tickLabelsOverlapping = true;
                    }
                }
                if ( tickLabelsOverlapping ) {
                    tickLabel = "";
                } else {
                    previousDrawnTickLabelPos = xx;
                    previousDrawnTickLabelLength = tickLabelLength;
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
                ticks.add ( tick );
            }
        }
        return ticks;
    }
    @Override
    protected List refreshTicksVertical ( final Graphics2D g2, final Rectangle2D dataArea, final RectangleEdge edge ) {
        final List ticks = new ArrayList();
        final Font tickLabelFont = this.getTickLabelFont();
        g2.setFont ( tickLabelFont );
        final double size = this.getTickUnit().getSize();
        final int count = this.calculateVisibleTickCount();
        final double lowestTickValue = this.calculateLowestVisibleTickValue();
        double previousDrawnTickLabelPos = 0.0;
        double previousDrawnTickLabelLength = 0.0;
        if ( count <= 500 ) {
            for ( int i = 0; i < count; ++i ) {
                final double currentTickValue = lowestTickValue + i * size;
                final double yy = this.valueToJava2D ( currentTickValue, dataArea, edge );
                final NumberFormat formatter = this.getNumberFormatOverride();
                String tickLabel;
                if ( formatter != null ) {
                    tickLabel = formatter.format ( currentTickValue );
                } else {
                    tickLabel = this.valueToString ( currentTickValue );
                }
                final Rectangle2D bounds = TextUtilities.getTextBounds ( tickLabel, g2, g2.getFontMetrics() );
                final double tickLabelLength = this.isVerticalTickLabels() ? bounds.getWidth() : bounds.getHeight();
                boolean tickLabelsOverlapping = false;
                if ( i > 0 ) {
                    final double avgTickLabelLength = ( previousDrawnTickLabelLength + tickLabelLength ) / 2.0;
                    if ( Math.abs ( yy - previousDrawnTickLabelPos ) < avgTickLabelLength ) {
                        tickLabelsOverlapping = true;
                    }
                }
                if ( tickLabelsOverlapping ) {
                    tickLabel = "";
                } else {
                    previousDrawnTickLabelPos = yy;
                    previousDrawnTickLabelLength = tickLabelLength;
                }
                double angle = 0.0;
                TextAnchor anchor;
                TextAnchor rotationAnchor;
                if ( this.isVerticalTickLabels() ) {
                    anchor = TextAnchor.BOTTOM_CENTER;
                    rotationAnchor = TextAnchor.BOTTOM_CENTER;
                    if ( edge == RectangleEdge.LEFT ) {
                        angle = -1.5707963267948966;
                    } else {
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
                ticks.add ( tick );
            }
        }
        return ticks;
    }
    public String valueToString ( final double value ) {
        String strToReturn;
        try {
            strToReturn = this.symbols.get ( ( int ) value );
        } catch ( IndexOutOfBoundsException ex ) {
            strToReturn = "";
        }
        return strToReturn;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof SymbolAxis ) ) {
            return false;
        }
        final SymbolAxis that = ( SymbolAxis ) obj;
        return this.symbols.equals ( that.symbols ) && this.gridBandsVisible == that.gridBandsVisible && PaintUtilities.equal ( this.gridBandPaint, that.gridBandPaint ) && PaintUtilities.equal ( this.gridBandAlternatePaint, that.gridBandAlternatePaint ) && super.equals ( obj );
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.gridBandPaint, stream );
        SerialUtilities.writePaint ( this.gridBandAlternatePaint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.gridBandPaint = SerialUtilities.readPaint ( stream );
        this.gridBandAlternatePaint = SerialUtilities.readPaint ( stream );
    }
    static {
        DEFAULT_GRID_BAND_PAINT = new Color ( 232, 234, 232, 128 );
        DEFAULT_GRID_BAND_ALTERNATE_PAINT = new Color ( 0, 0, 0, 0 );
    }
}
