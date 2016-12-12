package org.jfree.chart.axis;
import java.awt.BasicStroke;
import java.awt.Color;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.PaintUtilities;
import org.jfree.util.AttributedStringUtilities;
import org.jfree.util.ObjectUtilities;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import org.jfree.chart.util.AttrStringUtils;
import org.jfree.ui.TextAnchor;
import java.awt.FontMetrics;
import java.awt.geom.AffineTransform;
import org.jfree.text.TextUtilities;
import java.awt.font.TextLayout;
import org.jfree.chart.event.AxisChangeEvent;
import java.util.Arrays;
import java.util.EventListener;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.ChartEntity;
import java.awt.Shape;
import org.jfree.chart.entity.AxisEntity;
import java.util.List;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.ui.RectangleEdge;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.chart.util.ParamChecks;
import java.text.AttributedCharacterIterator;
import java.util.Map;
import javax.swing.event.EventListenerList;
import org.jfree.chart.plot.Plot;
import java.text.AttributedString;
import java.awt.Stroke;
import org.jfree.ui.RectangleInsets;
import java.awt.Paint;
import java.awt.Font;
import java.io.Serializable;
public abstract class Axis implements Cloneable, Serializable {
    private static final long serialVersionUID = 7719289504573298271L;
    public static final boolean DEFAULT_AXIS_VISIBLE = true;
    public static final Font DEFAULT_AXIS_LABEL_FONT;
    public static final Paint DEFAULT_AXIS_LABEL_PAINT;
    public static final RectangleInsets DEFAULT_AXIS_LABEL_INSETS;
    public static final Paint DEFAULT_AXIS_LINE_PAINT;
    public static final Stroke DEFAULT_AXIS_LINE_STROKE;
    public static final boolean DEFAULT_TICK_LABELS_VISIBLE = true;
    public static final Font DEFAULT_TICK_LABEL_FONT;
    public static final Paint DEFAULT_TICK_LABEL_PAINT;
    public static final RectangleInsets DEFAULT_TICK_LABEL_INSETS;
    public static final boolean DEFAULT_TICK_MARKS_VISIBLE = true;
    public static final Stroke DEFAULT_TICK_MARK_STROKE;
    public static final Paint DEFAULT_TICK_MARK_PAINT;
    public static final float DEFAULT_TICK_MARK_INSIDE_LENGTH = 0.0f;
    public static final float DEFAULT_TICK_MARK_OUTSIDE_LENGTH = 2.0f;
    private boolean visible;
    private String label;
    private transient AttributedString attributedLabel;
    private Font labelFont;
    private transient Paint labelPaint;
    private RectangleInsets labelInsets;
    private double labelAngle;
    private AxisLabelLocation labelLocation;
    private boolean axisLineVisible;
    private transient Stroke axisLineStroke;
    private transient Paint axisLinePaint;
    private boolean tickLabelsVisible;
    private Font tickLabelFont;
    private transient Paint tickLabelPaint;
    private RectangleInsets tickLabelInsets;
    private boolean tickMarksVisible;
    private float tickMarkInsideLength;
    private float tickMarkOutsideLength;
    private boolean minorTickMarksVisible;
    private float minorTickMarkInsideLength;
    private float minorTickMarkOutsideLength;
    private transient Stroke tickMarkStroke;
    private transient Paint tickMarkPaint;
    private double fixedDimension;
    private transient Plot plot;
    private transient EventListenerList listenerList;
    protected Axis ( final String label ) {
        this.label = label;
        this.visible = true;
        this.labelFont = Axis.DEFAULT_AXIS_LABEL_FONT;
        this.labelPaint = Axis.DEFAULT_AXIS_LABEL_PAINT;
        this.labelInsets = Axis.DEFAULT_AXIS_LABEL_INSETS;
        this.labelAngle = 0.0;
        this.labelLocation = AxisLabelLocation.MIDDLE;
        this.axisLineVisible = true;
        this.axisLinePaint = Axis.DEFAULT_AXIS_LINE_PAINT;
        this.axisLineStroke = Axis.DEFAULT_AXIS_LINE_STROKE;
        this.tickLabelsVisible = true;
        this.tickLabelFont = Axis.DEFAULT_TICK_LABEL_FONT;
        this.tickLabelPaint = Axis.DEFAULT_TICK_LABEL_PAINT;
        this.tickLabelInsets = Axis.DEFAULT_TICK_LABEL_INSETS;
        this.tickMarksVisible = true;
        this.tickMarkStroke = Axis.DEFAULT_TICK_MARK_STROKE;
        this.tickMarkPaint = Axis.DEFAULT_TICK_MARK_PAINT;
        this.tickMarkInsideLength = 0.0f;
        this.tickMarkOutsideLength = 2.0f;
        this.minorTickMarksVisible = false;
        this.minorTickMarkInsideLength = 0.0f;
        this.minorTickMarkOutsideLength = 2.0f;
        this.plot = null;
        this.listenerList = new EventListenerList();
    }
    public boolean isVisible() {
        return this.visible;
    }
    public void setVisible ( final boolean flag ) {
        if ( flag != this.visible ) {
            this.visible = flag;
            this.fireChangeEvent();
        }
    }
    public String getLabel() {
        return this.label;
    }
    public void setLabel ( final String label ) {
        this.label = label;
        this.fireChangeEvent();
    }
    public AttributedString getAttributedLabel() {
        if ( this.attributedLabel != null ) {
            return new AttributedString ( this.attributedLabel.getIterator() );
        }
        return null;
    }
    public void setAttributedLabel ( final String label ) {
        this.setAttributedLabel ( this.createAttributedLabel ( label ) );
    }
    public void setAttributedLabel ( final AttributedString label ) {
        if ( label != null ) {
            this.attributedLabel = new AttributedString ( label.getIterator() );
        } else {
            this.attributedLabel = null;
        }
        this.fireChangeEvent();
    }
    public AttributedString createAttributedLabel ( final String label ) {
        if ( label == null ) {
            return null;
        }
        final AttributedString s = new AttributedString ( label );
        s.addAttributes ( this.labelFont.getAttributes(), 0, label.length() );
        return s;
    }
    public Font getLabelFont() {
        return this.labelFont;
    }
    public void setLabelFont ( final Font font ) {
        ParamChecks.nullNotPermitted ( font, "font" );
        if ( !this.labelFont.equals ( font ) ) {
            this.labelFont = font;
            this.fireChangeEvent();
        }
    }
    public Paint getLabelPaint() {
        return this.labelPaint;
    }
    public void setLabelPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.labelPaint = paint;
        this.fireChangeEvent();
    }
    public RectangleInsets getLabelInsets() {
        return this.labelInsets;
    }
    public void setLabelInsets ( final RectangleInsets insets ) {
        this.setLabelInsets ( insets, true );
    }
    public void setLabelInsets ( final RectangleInsets insets, final boolean notify ) {
        ParamChecks.nullNotPermitted ( insets, "insets" );
        if ( !insets.equals ( ( Object ) this.labelInsets ) ) {
            this.labelInsets = insets;
            if ( notify ) {
                this.fireChangeEvent();
            }
        }
    }
    public double getLabelAngle() {
        return this.labelAngle;
    }
    public void setLabelAngle ( final double angle ) {
        this.labelAngle = angle;
        this.fireChangeEvent();
    }
    public AxisLabelLocation getLabelLocation() {
        return this.labelLocation;
    }
    public void setLabelLocation ( final AxisLabelLocation location ) {
        ParamChecks.nullNotPermitted ( location, "location" );
        this.labelLocation = location;
        this.fireChangeEvent();
    }
    public boolean isAxisLineVisible() {
        return this.axisLineVisible;
    }
    public void setAxisLineVisible ( final boolean visible ) {
        this.axisLineVisible = visible;
        this.fireChangeEvent();
    }
    public Paint getAxisLinePaint() {
        return this.axisLinePaint;
    }
    public void setAxisLinePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.axisLinePaint = paint;
        this.fireChangeEvent();
    }
    public Stroke getAxisLineStroke() {
        return this.axisLineStroke;
    }
    public void setAxisLineStroke ( final Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.axisLineStroke = stroke;
        this.fireChangeEvent();
    }
    public boolean isTickLabelsVisible() {
        return this.tickLabelsVisible;
    }
    public void setTickLabelsVisible ( final boolean flag ) {
        if ( flag != this.tickLabelsVisible ) {
            this.tickLabelsVisible = flag;
            this.fireChangeEvent();
        }
    }
    public boolean isMinorTickMarksVisible() {
        return this.minorTickMarksVisible;
    }
    public void setMinorTickMarksVisible ( final boolean flag ) {
        if ( flag != this.minorTickMarksVisible ) {
            this.minorTickMarksVisible = flag;
            this.fireChangeEvent();
        }
    }
    public Font getTickLabelFont() {
        return this.tickLabelFont;
    }
    public void setTickLabelFont ( final Font font ) {
        ParamChecks.nullNotPermitted ( font, "font" );
        if ( !this.tickLabelFont.equals ( font ) ) {
            this.tickLabelFont = font;
            this.fireChangeEvent();
        }
    }
    public Paint getTickLabelPaint() {
        return this.tickLabelPaint;
    }
    public void setTickLabelPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.tickLabelPaint = paint;
        this.fireChangeEvent();
    }
    public RectangleInsets getTickLabelInsets() {
        return this.tickLabelInsets;
    }
    public void setTickLabelInsets ( final RectangleInsets insets ) {
        ParamChecks.nullNotPermitted ( insets, "insets" );
        if ( !this.tickLabelInsets.equals ( ( Object ) insets ) ) {
            this.tickLabelInsets = insets;
            this.fireChangeEvent();
        }
    }
    public boolean isTickMarksVisible() {
        return this.tickMarksVisible;
    }
    public void setTickMarksVisible ( final boolean flag ) {
        if ( flag != this.tickMarksVisible ) {
            this.tickMarksVisible = flag;
            this.fireChangeEvent();
        }
    }
    public float getTickMarkInsideLength() {
        return this.tickMarkInsideLength;
    }
    public void setTickMarkInsideLength ( final float length ) {
        this.tickMarkInsideLength = length;
        this.fireChangeEvent();
    }
    public float getTickMarkOutsideLength() {
        return this.tickMarkOutsideLength;
    }
    public void setTickMarkOutsideLength ( final float length ) {
        this.tickMarkOutsideLength = length;
        this.fireChangeEvent();
    }
    public Stroke getTickMarkStroke() {
        return this.tickMarkStroke;
    }
    public void setTickMarkStroke ( final Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        if ( !this.tickMarkStroke.equals ( stroke ) ) {
            this.tickMarkStroke = stroke;
            this.fireChangeEvent();
        }
    }
    public Paint getTickMarkPaint() {
        return this.tickMarkPaint;
    }
    public void setTickMarkPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.tickMarkPaint = paint;
        this.fireChangeEvent();
    }
    public float getMinorTickMarkInsideLength() {
        return this.minorTickMarkInsideLength;
    }
    public void setMinorTickMarkInsideLength ( final float length ) {
        this.minorTickMarkInsideLength = length;
        this.fireChangeEvent();
    }
    public float getMinorTickMarkOutsideLength() {
        return this.minorTickMarkOutsideLength;
    }
    public void setMinorTickMarkOutsideLength ( final float length ) {
        this.minorTickMarkOutsideLength = length;
        this.fireChangeEvent();
    }
    public Plot getPlot() {
        return this.plot;
    }
    public void setPlot ( final Plot plot ) {
        this.plot = plot;
        this.configure();
    }
    public double getFixedDimension() {
        return this.fixedDimension;
    }
    public void setFixedDimension ( final double dimension ) {
        this.fixedDimension = dimension;
    }
    public abstract void configure();
    public abstract AxisSpace reserveSpace ( final Graphics2D p0, final Plot p1, final Rectangle2D p2, final RectangleEdge p3, final AxisSpace p4 );
    public abstract AxisState draw ( final Graphics2D p0, final double p1, final Rectangle2D p2, final Rectangle2D p3, final RectangleEdge p4, final PlotRenderingInfo p5 );
    public abstract List refreshTicks ( final Graphics2D p0, final AxisState p1, final Rectangle2D p2, final RectangleEdge p3 );
    protected void createAndAddEntity ( final double cursor, final AxisState state, final Rectangle2D dataArea, final RectangleEdge edge, final PlotRenderingInfo plotState ) {
        if ( plotState == null || plotState.getOwner() == null ) {
            return;
        }
        Rectangle2D hotspot = null;
        if ( edge.equals ( ( Object ) RectangleEdge.TOP ) ) {
            hotspot = new Rectangle2D.Double ( dataArea.getX(), state.getCursor(), dataArea.getWidth(), cursor - state.getCursor() );
        } else if ( edge.equals ( ( Object ) RectangleEdge.BOTTOM ) ) {
            hotspot = new Rectangle2D.Double ( dataArea.getX(), cursor, dataArea.getWidth(), state.getCursor() - cursor );
        } else if ( edge.equals ( ( Object ) RectangleEdge.LEFT ) ) {
            hotspot = new Rectangle2D.Double ( state.getCursor(), dataArea.getY(), cursor - state.getCursor(), dataArea.getHeight() );
        } else if ( edge.equals ( ( Object ) RectangleEdge.RIGHT ) ) {
            hotspot = new Rectangle2D.Double ( cursor, dataArea.getY(), state.getCursor() - cursor, dataArea.getHeight() );
        }
        final EntityCollection e = plotState.getOwner().getEntityCollection();
        if ( e != null ) {
            e.add ( new AxisEntity ( hotspot, this ) );
        }
    }
    public void addChangeListener ( final AxisChangeListener listener ) {
        this.listenerList.add ( AxisChangeListener.class, listener );
    }
    public void removeChangeListener ( final AxisChangeListener listener ) {
        this.listenerList.remove ( AxisChangeListener.class, listener );
    }
    public boolean hasListener ( final EventListener listener ) {
        final List list = Arrays.asList ( this.listenerList.getListenerList() );
        return list.contains ( listener );
    }
    protected void notifyListeners ( final AxisChangeEvent event ) {
        final Object[] listeners = this.listenerList.getListenerList();
        for ( int i = listeners.length - 2; i >= 0; i -= 2 ) {
            if ( listeners[i] == AxisChangeListener.class ) {
                ( ( AxisChangeListener ) listeners[i + 1] ).axisChanged ( event );
            }
        }
    }
    protected void fireChangeEvent() {
        this.notifyListeners ( new AxisChangeEvent ( this ) );
    }
    protected Rectangle2D getLabelEnclosure ( final Graphics2D g2, final RectangleEdge edge ) {
        Rectangle2D result = new Rectangle2D.Double();
        Rectangle2D bounds = null;
        if ( this.attributedLabel != null ) {
            final TextLayout layout = new TextLayout ( this.attributedLabel.getIterator(), g2.getFontRenderContext() );
            bounds = layout.getBounds();
        } else {
            final String axisLabel = this.getLabel();
            if ( axisLabel != null && !axisLabel.equals ( "" ) ) {
                final FontMetrics fm = g2.getFontMetrics ( this.getLabelFont() );
                bounds = TextUtilities.getTextBounds ( axisLabel, g2, fm );
            }
        }
        if ( bounds != null ) {
            final RectangleInsets insets = this.getLabelInsets();
            bounds = insets.createOutsetRectangle ( bounds );
            double angle = this.getLabelAngle();
            if ( edge == RectangleEdge.LEFT || edge == RectangleEdge.RIGHT ) {
                angle -= 1.5707963267948966;
            }
            final double x = bounds.getCenterX();
            final double y = bounds.getCenterY();
            final AffineTransform transformer = AffineTransform.getRotateInstance ( angle, x, y );
            final Shape labelBounds = transformer.createTransformedShape ( bounds );
            result = labelBounds.getBounds2D();
        }
        return result;
    }
    protected double labelLocationX ( final AxisLabelLocation location, final Rectangle2D dataArea ) {
        if ( location.equals ( AxisLabelLocation.HIGH_END ) ) {
            return dataArea.getMaxX();
        }
        if ( location.equals ( AxisLabelLocation.MIDDLE ) ) {
            return dataArea.getCenterX();
        }
        if ( location.equals ( AxisLabelLocation.LOW_END ) ) {
            return dataArea.getMinX();
        }
        throw new RuntimeException ( "Unexpected AxisLabelLocation: " + location );
    }
    protected double labelLocationY ( final AxisLabelLocation location, final Rectangle2D dataArea ) {
        if ( location.equals ( AxisLabelLocation.HIGH_END ) ) {
            return dataArea.getMinY();
        }
        if ( location.equals ( AxisLabelLocation.MIDDLE ) ) {
            return dataArea.getCenterY();
        }
        if ( location.equals ( AxisLabelLocation.LOW_END ) ) {
            return dataArea.getMaxY();
        }
        throw new RuntimeException ( "Unexpected AxisLabelLocation: " + location );
    }
    protected TextAnchor labelAnchorH ( final AxisLabelLocation location ) {
        if ( location.equals ( AxisLabelLocation.HIGH_END ) ) {
            return TextAnchor.CENTER_RIGHT;
        }
        if ( location.equals ( AxisLabelLocation.MIDDLE ) ) {
            return TextAnchor.CENTER;
        }
        if ( location.equals ( AxisLabelLocation.LOW_END ) ) {
            return TextAnchor.CENTER_LEFT;
        }
        throw new RuntimeException ( "Unexpected AxisLabelLocation: " + location );
    }
    protected TextAnchor labelAnchorV ( final AxisLabelLocation location ) {
        if ( location.equals ( AxisLabelLocation.HIGH_END ) ) {
            return TextAnchor.CENTER_RIGHT;
        }
        if ( location.equals ( AxisLabelLocation.MIDDLE ) ) {
            return TextAnchor.CENTER;
        }
        if ( location.equals ( AxisLabelLocation.LOW_END ) ) {
            return TextAnchor.CENTER_LEFT;
        }
        throw new RuntimeException ( "Unexpected AxisLabelLocation: " + location );
    }
    protected AxisState drawLabel ( final String label, final Graphics2D g2, final Rectangle2D plotArea, final Rectangle2D dataArea, final RectangleEdge edge, final AxisState state ) {
        ParamChecks.nullNotPermitted ( state, "state" );
        if ( label == null || label.equals ( "" ) ) {
            return state;
        }
        final Font font = this.getLabelFont();
        final RectangleInsets insets = this.getLabelInsets();
        g2.setFont ( font );
        g2.setPaint ( this.getLabelPaint() );
        final FontMetrics fm = g2.getFontMetrics();
        Rectangle2D labelBounds = TextUtilities.getTextBounds ( label, g2, fm );
        if ( edge == RectangleEdge.TOP ) {
            final AffineTransform t = AffineTransform.getRotateInstance ( this.getLabelAngle(), labelBounds.getCenterX(), labelBounds.getCenterY() );
            final Shape rotatedLabelBounds = t.createTransformedShape ( labelBounds );
            labelBounds = rotatedLabelBounds.getBounds2D();
            final double labelx = this.labelLocationX ( this.labelLocation, dataArea );
            final double labely = state.getCursor() - insets.getBottom() - labelBounds.getHeight() / 2.0;
            final TextAnchor anchor = this.labelAnchorH ( this.labelLocation );
            TextUtilities.drawRotatedString ( label, g2, ( float ) labelx, ( float ) labely, anchor, this.getLabelAngle(), TextAnchor.CENTER );
            state.cursorUp ( insets.getTop() + labelBounds.getHeight() + insets.getBottom() );
        } else if ( edge == RectangleEdge.BOTTOM ) {
            final AffineTransform t = AffineTransform.getRotateInstance ( this.getLabelAngle(), labelBounds.getCenterX(), labelBounds.getCenterY() );
            final Shape rotatedLabelBounds = t.createTransformedShape ( labelBounds );
            labelBounds = rotatedLabelBounds.getBounds2D();
            final double labelx = this.labelLocationX ( this.labelLocation, dataArea );
            final double labely = state.getCursor() + insets.getTop() + labelBounds.getHeight() / 2.0;
            final TextAnchor anchor = this.labelAnchorH ( this.labelLocation );
            TextUtilities.drawRotatedString ( label, g2, ( float ) labelx, ( float ) labely, anchor, this.getLabelAngle(), TextAnchor.CENTER );
            state.cursorDown ( insets.getTop() + labelBounds.getHeight() + insets.getBottom() );
        } else if ( edge == RectangleEdge.LEFT ) {
            final AffineTransform t = AffineTransform.getRotateInstance ( this.getLabelAngle() - 1.5707963267948966, labelBounds.getCenterX(), labelBounds.getCenterY() );
            final Shape rotatedLabelBounds = t.createTransformedShape ( labelBounds );
            labelBounds = rotatedLabelBounds.getBounds2D();
            final double labelx = state.getCursor() - insets.getRight() - labelBounds.getWidth() / 2.0;
            final double labely = this.labelLocationY ( this.labelLocation, dataArea );
            final TextAnchor anchor = this.labelAnchorV ( this.labelLocation );
            TextUtilities.drawRotatedString ( label, g2, ( float ) labelx, ( float ) labely, anchor, this.getLabelAngle() - 1.5707963267948966, anchor );
            state.cursorLeft ( insets.getLeft() + labelBounds.getWidth() + insets.getRight() );
        } else if ( edge == RectangleEdge.RIGHT ) {
            final AffineTransform t = AffineTransform.getRotateInstance ( this.getLabelAngle() + 1.5707963267948966, labelBounds.getCenterX(), labelBounds.getCenterY() );
            final Shape rotatedLabelBounds = t.createTransformedShape ( labelBounds );
            labelBounds = rotatedLabelBounds.getBounds2D();
            final double labelx = state.getCursor() + insets.getLeft() + labelBounds.getWidth() / 2.0;
            final double labely = this.labelLocationY ( this.labelLocation, dataArea );
            final TextAnchor anchor = this.labelAnchorV ( this.labelLocation );
            TextUtilities.drawRotatedString ( label, g2, ( float ) labelx, ( float ) labely, anchor, this.getLabelAngle() + 1.5707963267948966, anchor );
            state.cursorRight ( insets.getLeft() + labelBounds.getWidth() + insets.getRight() );
        }
        return state;
    }
    protected AxisState drawAttributedLabel ( final AttributedString label, final Graphics2D g2, final Rectangle2D plotArea, final Rectangle2D dataArea, final RectangleEdge edge, final AxisState state ) {
        ParamChecks.nullNotPermitted ( state, "state" );
        if ( label == null ) {
            return state;
        }
        final RectangleInsets insets = this.getLabelInsets();
        g2.setFont ( this.getLabelFont() );
        g2.setPaint ( this.getLabelPaint() );
        final TextLayout layout = new TextLayout ( this.attributedLabel.getIterator(), g2.getFontRenderContext() );
        Rectangle2D labelBounds = layout.getBounds();
        if ( edge == RectangleEdge.TOP ) {
            final AffineTransform t = AffineTransform.getRotateInstance ( this.getLabelAngle(), labelBounds.getCenterX(), labelBounds.getCenterY() );
            final Shape rotatedLabelBounds = t.createTransformedShape ( labelBounds );
            labelBounds = rotatedLabelBounds.getBounds2D();
            final double labelx = this.labelLocationX ( this.labelLocation, dataArea );
            final double labely = state.getCursor() - insets.getBottom() - labelBounds.getHeight() / 2.0;
            final TextAnchor anchor = this.labelAnchorH ( this.labelLocation );
            AttrStringUtils.drawRotatedString ( label, g2, ( float ) labelx, ( float ) labely, anchor, this.getLabelAngle(), TextAnchor.CENTER );
            state.cursorUp ( insets.getTop() + labelBounds.getHeight() + insets.getBottom() );
        } else if ( edge == RectangleEdge.BOTTOM ) {
            final AffineTransform t = AffineTransform.getRotateInstance ( this.getLabelAngle(), labelBounds.getCenterX(), labelBounds.getCenterY() );
            final Shape rotatedLabelBounds = t.createTransformedShape ( labelBounds );
            labelBounds = rotatedLabelBounds.getBounds2D();
            final double labelx = this.labelLocationX ( this.labelLocation, dataArea );
            final double labely = state.getCursor() + insets.getTop() + labelBounds.getHeight() / 2.0;
            final TextAnchor anchor = this.labelAnchorH ( this.labelLocation );
            AttrStringUtils.drawRotatedString ( label, g2, ( float ) labelx, ( float ) labely, anchor, this.getLabelAngle(), TextAnchor.CENTER );
            state.cursorDown ( insets.getTop() + labelBounds.getHeight() + insets.getBottom() );
        } else if ( edge == RectangleEdge.LEFT ) {
            final AffineTransform t = AffineTransform.getRotateInstance ( this.getLabelAngle() - 1.5707963267948966, labelBounds.getCenterX(), labelBounds.getCenterY() );
            final Shape rotatedLabelBounds = t.createTransformedShape ( labelBounds );
            labelBounds = rotatedLabelBounds.getBounds2D();
            final double labelx = state.getCursor() - insets.getRight() - labelBounds.getWidth() / 2.0;
            final double labely = this.labelLocationY ( this.labelLocation, dataArea );
            final TextAnchor anchor = this.labelAnchorV ( this.labelLocation );
            AttrStringUtils.drawRotatedString ( label, g2, ( float ) labelx, ( float ) labely, anchor, this.getLabelAngle() - 1.5707963267948966, anchor );
            state.cursorLeft ( insets.getLeft() + labelBounds.getWidth() + insets.getRight() );
        } else if ( edge == RectangleEdge.RIGHT ) {
            final AffineTransform t = AffineTransform.getRotateInstance ( this.getLabelAngle() + 1.5707963267948966, labelBounds.getCenterX(), labelBounds.getCenterY() );
            final Shape rotatedLabelBounds = t.createTransformedShape ( labelBounds );
            labelBounds = rotatedLabelBounds.getBounds2D();
            final double labelx = state.getCursor() + insets.getLeft() + labelBounds.getWidth() / 2.0;
            final double labely = this.labelLocationY ( this.labelLocation, dataArea );
            final TextAnchor anchor = this.labelAnchorV ( this.labelLocation );
            AttrStringUtils.drawRotatedString ( label, g2, ( float ) labelx, ( float ) labely, anchor, this.getLabelAngle() + 1.5707963267948966, anchor );
            state.cursorRight ( insets.getLeft() + labelBounds.getWidth() + insets.getRight() );
        }
        return state;
    }
    protected void drawAxisLine ( final Graphics2D g2, final double cursor, final Rectangle2D dataArea, final RectangleEdge edge ) {
        Line2D axisLine = null;
        final double x = dataArea.getX();
        final double y = dataArea.getY();
        if ( edge == RectangleEdge.TOP ) {
            axisLine = new Line2D.Double ( x, cursor, dataArea.getMaxX(), cursor );
        } else if ( edge == RectangleEdge.BOTTOM ) {
            axisLine = new Line2D.Double ( x, cursor, dataArea.getMaxX(), cursor );
        } else if ( edge == RectangleEdge.LEFT ) {
            axisLine = new Line2D.Double ( cursor, y, cursor, dataArea.getMaxY() );
        } else if ( edge == RectangleEdge.RIGHT ) {
            axisLine = new Line2D.Double ( cursor, y, cursor, dataArea.getMaxY() );
        }
        g2.setPaint ( this.axisLinePaint );
        g2.setStroke ( this.axisLineStroke );
        final Object saved = g2.getRenderingHint ( RenderingHints.KEY_STROKE_CONTROL );
        g2.setRenderingHint ( RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE );
        g2.draw ( axisLine );
        g2.setRenderingHint ( RenderingHints.KEY_STROKE_CONTROL, saved );
    }
    public Object clone() throws CloneNotSupportedException {
        final Axis clone = ( Axis ) super.clone();
        clone.plot = null;
        clone.listenerList = new EventListenerList();
        return clone;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof Axis ) ) {
            return false;
        }
        final Axis that = ( Axis ) obj;
        return this.visible == that.visible && ObjectUtilities.equal ( ( Object ) this.label, ( Object ) that.label ) && AttributedStringUtilities.equal ( this.attributedLabel, that.attributedLabel ) && ObjectUtilities.equal ( ( Object ) this.labelFont, ( Object ) that.labelFont ) && PaintUtilities.equal ( this.labelPaint, that.labelPaint ) && ObjectUtilities.equal ( ( Object ) this.labelInsets, ( Object ) that.labelInsets ) && this.labelAngle == that.labelAngle && this.labelLocation.equals ( that.labelLocation ) && this.axisLineVisible == that.axisLineVisible && ObjectUtilities.equal ( ( Object ) this.axisLineStroke, ( Object ) that.axisLineStroke ) && PaintUtilities.equal ( this.axisLinePaint, that.axisLinePaint ) && this.tickLabelsVisible == that.tickLabelsVisible && ObjectUtilities.equal ( ( Object ) this.tickLabelFont, ( Object ) that.tickLabelFont ) && PaintUtilities.equal ( this.tickLabelPaint, that.tickLabelPaint ) && ObjectUtilities.equal ( ( Object ) this.tickLabelInsets, ( Object ) that.tickLabelInsets ) && this.tickMarksVisible == that.tickMarksVisible && this.tickMarkInsideLength == that.tickMarkInsideLength && this.tickMarkOutsideLength == that.tickMarkOutsideLength && PaintUtilities.equal ( this.tickMarkPaint, that.tickMarkPaint ) && ObjectUtilities.equal ( ( Object ) this.tickMarkStroke, ( Object ) that.tickMarkStroke ) && this.minorTickMarksVisible == that.minorTickMarksVisible && this.minorTickMarkInsideLength == that.minorTickMarkInsideLength && this.minorTickMarkOutsideLength == that.minorTickMarkOutsideLength && this.fixedDimension == that.fixedDimension;
    }
    @Override
    public int hashCode() {
        int hash = 3;
        if ( this.label != null ) {
            hash = 83 * hash + this.label.hashCode();
        }
        return hash;
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeAttributedString ( this.attributedLabel, stream );
        SerialUtilities.writePaint ( this.labelPaint, stream );
        SerialUtilities.writePaint ( this.tickLabelPaint, stream );
        SerialUtilities.writeStroke ( this.axisLineStroke, stream );
        SerialUtilities.writePaint ( this.axisLinePaint, stream );
        SerialUtilities.writeStroke ( this.tickMarkStroke, stream );
        SerialUtilities.writePaint ( this.tickMarkPaint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.attributedLabel = SerialUtilities.readAttributedString ( stream );
        this.labelPaint = SerialUtilities.readPaint ( stream );
        this.tickLabelPaint = SerialUtilities.readPaint ( stream );
        this.axisLineStroke = SerialUtilities.readStroke ( stream );
        this.axisLinePaint = SerialUtilities.readPaint ( stream );
        this.tickMarkStroke = SerialUtilities.readStroke ( stream );
        this.tickMarkPaint = SerialUtilities.readPaint ( stream );
        this.listenerList = new EventListenerList();
    }
    static {
        DEFAULT_AXIS_LABEL_FONT = new Font ( "SansSerif", 0, 12 );
        DEFAULT_AXIS_LABEL_PAINT = Color.black;
        DEFAULT_AXIS_LABEL_INSETS = new RectangleInsets ( 3.0, 3.0, 3.0, 3.0 );
        DEFAULT_AXIS_LINE_PAINT = Color.gray;
        DEFAULT_AXIS_LINE_STROKE = new BasicStroke ( 0.5f );
        DEFAULT_TICK_LABEL_FONT = new Font ( "SansSerif", 0, 10 );
        DEFAULT_TICK_LABEL_PAINT = Color.black;
        DEFAULT_TICK_LABEL_INSETS = new RectangleInsets ( 2.0, 4.0, 2.0, 4.0 );
        DEFAULT_TICK_MARK_STROKE = new BasicStroke ( 0.5f );
        DEFAULT_TICK_MARK_PAINT = Color.gray;
    }
}
