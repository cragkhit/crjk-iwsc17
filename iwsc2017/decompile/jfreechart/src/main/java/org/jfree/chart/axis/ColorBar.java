package org.jfree.chart.axis;
import org.jfree.chart.plot.Plot;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.RenderingHints;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.ui.RectangleEdge;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.chart.plot.ContourPlot;
import org.jfree.chart.plot.RainbowPalette;
import org.jfree.chart.plot.ColorPalette;
import java.io.Serializable;
public class ColorBar implements Cloneable, Serializable {
    private static final long serialVersionUID = -2101776212647268103L;
    public static final int DEFAULT_COLORBAR_THICKNESS = 0;
    public static final double DEFAULT_COLORBAR_THICKNESS_PERCENT = 0.1;
    public static final int DEFAULT_OUTERGAP = 2;
    private ValueAxis axis;
    private int colorBarThickness;
    private double colorBarThicknessPercent;
    private ColorPalette colorPalette;
    private int colorBarLength;
    private int outerGap;
    public ColorBar ( final String label ) {
        this.colorBarThickness = 0;
        this.colorBarThicknessPercent = 0.1;
        this.colorPalette = null;
        this.colorBarLength = 0;
        final NumberAxis a = new NumberAxis ( label );
        a.setAutoRangeIncludesZero ( false );
        ( this.axis = a ).setLowerMargin ( 0.0 );
        this.axis.setUpperMargin ( 0.0 );
        this.colorPalette = new RainbowPalette();
        this.colorBarThickness = 0;
        this.colorBarThicknessPercent = 0.1;
        this.outerGap = 2;
        this.colorPalette.setMinZ ( this.axis.getRange().getLowerBound() );
        this.colorPalette.setMaxZ ( this.axis.getRange().getUpperBound() );
    }
    public void configure ( final ContourPlot plot ) {
        final double minZ = plot.getDataset().getMinZValue();
        final double maxZ = plot.getDataset().getMaxZValue();
        this.setMinimumValue ( minZ );
        this.setMaximumValue ( maxZ );
    }
    public ValueAxis getAxis() {
        return this.axis;
    }
    public void setAxis ( final ValueAxis axis ) {
        this.axis = axis;
    }
    public void autoAdjustRange() {
        this.axis.autoAdjustRange();
        this.colorPalette.setMinZ ( this.axis.getLowerBound() );
        this.colorPalette.setMaxZ ( this.axis.getUpperBound() );
    }
    public double draw ( final Graphics2D g2, double cursor, final Rectangle2D plotArea, final Rectangle2D dataArea, final Rectangle2D reservedArea, final RectangleEdge edge ) {
        Rectangle2D colorBarArea = null;
        double thickness = this.calculateBarThickness ( dataArea, edge );
        if ( this.colorBarThickness > 0 ) {
            thickness = this.colorBarThickness;
        }
        double length;
        if ( RectangleEdge.isLeftOrRight ( edge ) ) {
            length = dataArea.getHeight();
        } else {
            length = dataArea.getWidth();
        }
        if ( this.colorBarLength > 0 ) {
            length = this.colorBarLength;
        }
        if ( edge == RectangleEdge.BOTTOM ) {
            colorBarArea = new Rectangle2D.Double ( dataArea.getX(), plotArea.getMaxY() + this.outerGap, length, thickness );
        } else if ( edge == RectangleEdge.TOP ) {
            colorBarArea = new Rectangle2D.Double ( dataArea.getX(), reservedArea.getMinY() + this.outerGap, length, thickness );
        } else if ( edge == RectangleEdge.LEFT ) {
            colorBarArea = new Rectangle2D.Double ( plotArea.getX() - thickness - this.outerGap, dataArea.getMinY(), thickness, length );
        } else if ( edge == RectangleEdge.RIGHT ) {
            colorBarArea = new Rectangle2D.Double ( plotArea.getMaxX() + this.outerGap, dataArea.getMinY(), thickness, length );
        }
        this.axis.refreshTicks ( g2, new AxisState(), colorBarArea, edge );
        this.drawColorBar ( g2, colorBarArea, edge );
        AxisState state = null;
        assert colorBarArea != null;
        if ( edge == RectangleEdge.TOP ) {
            cursor = colorBarArea.getMinY();
            state = this.axis.draw ( g2, cursor, reservedArea, colorBarArea, RectangleEdge.TOP, null );
        } else if ( edge == RectangleEdge.BOTTOM ) {
            cursor = colorBarArea.getMaxY();
            state = this.axis.draw ( g2, cursor, reservedArea, colorBarArea, RectangleEdge.BOTTOM, null );
        } else if ( edge == RectangleEdge.LEFT ) {
            cursor = colorBarArea.getMinX();
            state = this.axis.draw ( g2, cursor, reservedArea, colorBarArea, RectangleEdge.LEFT, null );
        } else if ( edge == RectangleEdge.RIGHT ) {
            cursor = colorBarArea.getMaxX();
            state = this.axis.draw ( g2, cursor, reservedArea, colorBarArea, RectangleEdge.RIGHT, null );
        }
        assert state != null;
        return state.getCursor();
    }
    public void drawColorBar ( final Graphics2D g2, final Rectangle2D colorBarArea, final RectangleEdge edge ) {
        final Object antiAlias = g2.getRenderingHint ( RenderingHints.KEY_ANTIALIASING );
        g2.setRenderingHint ( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
        final Stroke strokeSaved = g2.getStroke();
        g2.setStroke ( new BasicStroke ( 1.0f ) );
        if ( RectangleEdge.isTopOrBottom ( edge ) ) {
            final double y1 = colorBarArea.getY();
            final double y2 = colorBarArea.getMaxY();
            double xx = colorBarArea.getX();
            final Line2D line = new Line2D.Double();
            while ( xx <= colorBarArea.getMaxX() ) {
                final double value = this.axis.java2DToValue ( xx, colorBarArea, edge );
                line.setLine ( xx, y1, xx, y2 );
                g2.setPaint ( this.getPaint ( value ) );
                g2.draw ( line );
                ++xx;
            }
        } else {
            final double y1 = colorBarArea.getX();
            final double y2 = colorBarArea.getMaxX();
            double xx = colorBarArea.getY();
            final Line2D line = new Line2D.Double();
            while ( xx <= colorBarArea.getMaxY() ) {
                final double value = this.axis.java2DToValue ( xx, colorBarArea, edge );
                line.setLine ( y1, xx, y2, xx );
                g2.setPaint ( this.getPaint ( value ) );
                g2.draw ( line );
                ++xx;
            }
        }
        g2.setRenderingHint ( RenderingHints.KEY_ANTIALIASING, antiAlias );
        g2.setStroke ( strokeSaved );
    }
    public ColorPalette getColorPalette() {
        return this.colorPalette;
    }
    public Paint getPaint ( final double value ) {
        return this.colorPalette.getPaint ( value );
    }
    public void setColorPalette ( final ColorPalette palette ) {
        this.colorPalette = palette;
    }
    public void setMaximumValue ( final double value ) {
        this.colorPalette.setMaxZ ( value );
        this.axis.setUpperBound ( value );
    }
    public void setMinimumValue ( final double value ) {
        this.colorPalette.setMinZ ( value );
        this.axis.setLowerBound ( value );
    }
    public AxisSpace reserveSpace ( final Graphics2D g2, final Plot plot, final Rectangle2D plotArea, final Rectangle2D dataArea, final RectangleEdge edge, final AxisSpace space ) {
        final AxisSpace result = this.axis.reserveSpace ( g2, plot, plotArea, edge, space );
        final double thickness = this.calculateBarThickness ( dataArea, edge );
        result.add ( thickness + 2 * this.outerGap, edge );
        return result;
    }
    private double calculateBarThickness ( final Rectangle2D plotArea, final RectangleEdge edge ) {
        double result;
        if ( RectangleEdge.isLeftOrRight ( edge ) ) {
            result = plotArea.getWidth() * this.colorBarThicknessPercent;
        } else {
            result = plotArea.getHeight() * this.colorBarThicknessPercent;
        }
        return result;
    }
    public Object clone() throws CloneNotSupportedException {
        final ColorBar clone = ( ColorBar ) super.clone();
        clone.axis = ( ValueAxis ) this.axis.clone();
        return clone;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof ColorBar ) ) {
            return false;
        }
        final ColorBar that = ( ColorBar ) obj;
        return this.axis.equals ( that.axis ) && this.colorBarThickness == that.colorBarThickness && this.colorBarThicknessPercent == that.colorBarThicknessPercent && this.colorPalette.equals ( that.colorPalette ) && this.colorBarLength == that.colorBarLength && this.outerGap == that.outerGap;
    }
    @Override
    public int hashCode() {
        return this.axis.hashCode();
    }
}
