

package org.jfree.chart.axis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.AttributedString;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;

import javax.swing.event.EventListenerList;

import org.jfree.chart.entity.AxisEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.util.AttrStringUtils;
import org.jfree.chart.util.ParamChecks;
import org.jfree.io.SerialUtilities;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;
import org.jfree.util.AttributedStringUtilities;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;


public abstract class Axis implements Cloneable, Serializable {


    private static final long serialVersionUID = 7719289504573298271L;


    public static final boolean DEFAULT_AXIS_VISIBLE = true;


    public static final Font DEFAULT_AXIS_LABEL_FONT = new Font (
        "SansSerif", Font.PLAIN, 12 );


    public static final Paint DEFAULT_AXIS_LABEL_PAINT = Color.black;


    public static final RectangleInsets DEFAULT_AXIS_LABEL_INSETS
        = new RectangleInsets ( 3.0, 3.0, 3.0, 3.0 );


    public static final Paint DEFAULT_AXIS_LINE_PAINT = Color.gray;


    public static final Stroke DEFAULT_AXIS_LINE_STROKE = new BasicStroke ( 0.5f );


    public static final boolean DEFAULT_TICK_LABELS_VISIBLE = true;


    public static final Font DEFAULT_TICK_LABEL_FONT = new Font ( "SansSerif",
            Font.PLAIN, 10 );


    public static final Paint DEFAULT_TICK_LABEL_PAINT = Color.black;


    public static final RectangleInsets DEFAULT_TICK_LABEL_INSETS
        = new RectangleInsets ( 2.0, 4.0, 2.0, 4.0 );


    public static final boolean DEFAULT_TICK_MARKS_VISIBLE = true;


    public static final Stroke DEFAULT_TICK_MARK_STROKE = new BasicStroke ( 0.5f );


    public static final Paint DEFAULT_TICK_MARK_PAINT = Color.gray;


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


    protected Axis ( String label ) {

        this.label = label;
        this.visible = DEFAULT_AXIS_VISIBLE;
        this.labelFont = DEFAULT_AXIS_LABEL_FONT;
        this.labelPaint = DEFAULT_AXIS_LABEL_PAINT;
        this.labelInsets = DEFAULT_AXIS_LABEL_INSETS;
        this.labelAngle = 0.0;
        this.labelLocation = AxisLabelLocation.MIDDLE;

        this.axisLineVisible = true;
        this.axisLinePaint = DEFAULT_AXIS_LINE_PAINT;
        this.axisLineStroke = DEFAULT_AXIS_LINE_STROKE;

        this.tickLabelsVisible = DEFAULT_TICK_LABELS_VISIBLE;
        this.tickLabelFont = DEFAULT_TICK_LABEL_FONT;
        this.tickLabelPaint = DEFAULT_TICK_LABEL_PAINT;
        this.tickLabelInsets = DEFAULT_TICK_LABEL_INSETS;

        this.tickMarksVisible = DEFAULT_TICK_MARKS_VISIBLE;
        this.tickMarkStroke = DEFAULT_TICK_MARK_STROKE;
        this.tickMarkPaint = DEFAULT_TICK_MARK_PAINT;
        this.tickMarkInsideLength = DEFAULT_TICK_MARK_INSIDE_LENGTH;
        this.tickMarkOutsideLength = DEFAULT_TICK_MARK_OUTSIDE_LENGTH;

        this.minorTickMarksVisible = false;
        this.minorTickMarkInsideLength = 0.0f;
        this.minorTickMarkOutsideLength = 2.0f;

        this.plot = null;

        this.listenerList = new EventListenerList();
    }


    public boolean isVisible() {
        return this.visible;
    }


    public void setVisible ( boolean flag ) {
        if ( flag != this.visible ) {
            this.visible = flag;
            fireChangeEvent();
        }
    }


    public String getLabel() {
        return this.label;
    }


    public void setLabel ( String label ) {
        this.label = label;
        fireChangeEvent();
    }


    public AttributedString getAttributedLabel() {
        if ( this.attributedLabel != null ) {
            return new AttributedString ( this.attributedLabel.getIterator() );
        } else {
            return null;
        }
    }


    public void setAttributedLabel ( String label ) {
        setAttributedLabel ( createAttributedLabel ( label ) );
    }


    public void setAttributedLabel ( AttributedString label ) {
        if ( label != null ) {
            this.attributedLabel = new AttributedString ( label.getIterator() );
        } else {
            this.attributedLabel = null;
        }
        fireChangeEvent();
    }


    public AttributedString createAttributedLabel ( String label ) {
        if ( label == null ) {
            return null;
        }
        AttributedString s = new AttributedString ( label );
        s.addAttributes ( this.labelFont.getAttributes(), 0, label.length() );
        return s;
    }


    public Font getLabelFont() {
        return this.labelFont;
    }


    public void setLabelFont ( Font font ) {
        ParamChecks.nullNotPermitted ( font, "font" );
        if ( !this.labelFont.equals ( font ) ) {
            this.labelFont = font;
            fireChangeEvent();
        }
    }


    public Paint getLabelPaint() {
        return this.labelPaint;
    }


    public void setLabelPaint ( Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.labelPaint = paint;
        fireChangeEvent();
    }


    public RectangleInsets getLabelInsets() {
        return this.labelInsets;
    }


    public void setLabelInsets ( RectangleInsets insets ) {
        setLabelInsets ( insets, true );
    }


    public void setLabelInsets ( RectangleInsets insets, boolean notify ) {
        ParamChecks.nullNotPermitted ( insets, "insets" );
        if ( !insets.equals ( this.labelInsets ) ) {
            this.labelInsets = insets;
            if ( notify ) {
                fireChangeEvent();
            }
        }
    }


    public double getLabelAngle() {
        return this.labelAngle;
    }


    public void setLabelAngle ( double angle ) {
        this.labelAngle = angle;
        fireChangeEvent();
    }


    public AxisLabelLocation getLabelLocation() {
        return this.labelLocation;
    }


    public void setLabelLocation ( AxisLabelLocation location ) {
        ParamChecks.nullNotPermitted ( location, "location" );
        this.labelLocation = location;
        fireChangeEvent();
    }


    public boolean isAxisLineVisible() {
        return this.axisLineVisible;
    }


    public void setAxisLineVisible ( boolean visible ) {
        this.axisLineVisible = visible;
        fireChangeEvent();
    }


    public Paint getAxisLinePaint() {
        return this.axisLinePaint;
    }


    public void setAxisLinePaint ( Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.axisLinePaint = paint;
        fireChangeEvent();
    }


    public Stroke getAxisLineStroke() {
        return this.axisLineStroke;
    }


    public void setAxisLineStroke ( Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.axisLineStroke = stroke;
        fireChangeEvent();
    }


    public boolean isTickLabelsVisible() {
        return this.tickLabelsVisible;
    }


    public void setTickLabelsVisible ( boolean flag ) {

        if ( flag != this.tickLabelsVisible ) {
            this.tickLabelsVisible = flag;
            fireChangeEvent();
        }

    }


    public boolean isMinorTickMarksVisible() {
        return this.minorTickMarksVisible;
    }


    public void setMinorTickMarksVisible ( boolean flag ) {
        if ( flag != this.minorTickMarksVisible ) {
            this.minorTickMarksVisible = flag;
            fireChangeEvent();
        }
    }


    public Font getTickLabelFont() {
        return this.tickLabelFont;
    }


    public void setTickLabelFont ( Font font ) {
        ParamChecks.nullNotPermitted ( font, "font" );
        if ( !this.tickLabelFont.equals ( font ) ) {
            this.tickLabelFont = font;
            fireChangeEvent();
        }
    }


    public Paint getTickLabelPaint() {
        return this.tickLabelPaint;
    }


    public void setTickLabelPaint ( Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.tickLabelPaint = paint;
        fireChangeEvent();
    }


    public RectangleInsets getTickLabelInsets() {
        return this.tickLabelInsets;
    }


    public void setTickLabelInsets ( RectangleInsets insets ) {
        ParamChecks.nullNotPermitted ( insets, "insets" );
        if ( !this.tickLabelInsets.equals ( insets ) ) {
            this.tickLabelInsets = insets;
            fireChangeEvent();
        }
    }


    public boolean isTickMarksVisible() {
        return this.tickMarksVisible;
    }


    public void setTickMarksVisible ( boolean flag ) {
        if ( flag != this.tickMarksVisible ) {
            this.tickMarksVisible = flag;
            fireChangeEvent();
        }
    }


    public float getTickMarkInsideLength() {
        return this.tickMarkInsideLength;
    }


    public void setTickMarkInsideLength ( float length ) {
        this.tickMarkInsideLength = length;
        fireChangeEvent();
    }


    public float getTickMarkOutsideLength() {
        return this.tickMarkOutsideLength;
    }


    public void setTickMarkOutsideLength ( float length ) {
        this.tickMarkOutsideLength = length;
        fireChangeEvent();
    }


    public Stroke getTickMarkStroke() {
        return this.tickMarkStroke;
    }


    public void setTickMarkStroke ( Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        if ( !this.tickMarkStroke.equals ( stroke ) ) {
            this.tickMarkStroke = stroke;
            fireChangeEvent();
        }
    }


    public Paint getTickMarkPaint() {
        return this.tickMarkPaint;
    }


    public void setTickMarkPaint ( Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.tickMarkPaint = paint;
        fireChangeEvent();
    }


    public float getMinorTickMarkInsideLength() {
        return this.minorTickMarkInsideLength;
    }


    public void setMinorTickMarkInsideLength ( float length ) {
        this.minorTickMarkInsideLength = length;
        fireChangeEvent();
    }


    public float getMinorTickMarkOutsideLength() {
        return this.minorTickMarkOutsideLength;
    }


    public void setMinorTickMarkOutsideLength ( float length ) {
        this.minorTickMarkOutsideLength = length;
        fireChangeEvent();
    }


    public Plot getPlot() {
        return this.plot;
    }


    public void setPlot ( Plot plot ) {
        this.plot = plot;
        configure();
    }


    public double getFixedDimension() {
        return this.fixedDimension;
    }


    public void setFixedDimension ( double dimension ) {
        this.fixedDimension = dimension;
    }


    public abstract void configure();


    public abstract AxisSpace reserveSpace ( Graphics2D g2, Plot plot,
            Rectangle2D plotArea,
            RectangleEdge edge,
            AxisSpace space );


    public abstract AxisState draw ( Graphics2D g2, double cursor,
                                     Rectangle2D plotArea, Rectangle2D dataArea, RectangleEdge edge,
                                     PlotRenderingInfo plotState );


    public abstract List refreshTicks ( Graphics2D g2, AxisState state,
                                        Rectangle2D dataArea, RectangleEdge edge );


    protected void createAndAddEntity ( double cursor, AxisState state,
                                        Rectangle2D dataArea, RectangleEdge edge,
                                        PlotRenderingInfo plotState ) {

        if ( plotState == null || plotState.getOwner() == null ) {
            return;
        }
        Rectangle2D hotspot = null;
        if ( edge.equals ( RectangleEdge.TOP ) ) {
            hotspot = new Rectangle2D.Double ( dataArea.getX(),
                                               state.getCursor(), dataArea.getWidth(),
                                               cursor - state.getCursor() );
        } else if ( edge.equals ( RectangleEdge.BOTTOM ) ) {
            hotspot = new Rectangle2D.Double ( dataArea.getX(), cursor,
                                               dataArea.getWidth(), state.getCursor() - cursor );
        } else if ( edge.equals ( RectangleEdge.LEFT ) ) {
            hotspot = new Rectangle2D.Double ( state.getCursor(),
                                               dataArea.getY(), cursor - state.getCursor(),
                                               dataArea.getHeight() );
        } else if ( edge.equals ( RectangleEdge.RIGHT ) ) {
            hotspot = new Rectangle2D.Double ( cursor, dataArea.getY(),
                                               state.getCursor() - cursor, dataArea.getHeight() );
        }
        EntityCollection e = plotState.getOwner().getEntityCollection();
        if ( e != null ) {
            e.add ( new AxisEntity ( hotspot, this ) );
        }
    }


    public void addChangeListener ( AxisChangeListener listener ) {
        this.listenerList.add ( AxisChangeListener.class, listener );
    }


    public void removeChangeListener ( AxisChangeListener listener ) {
        this.listenerList.remove ( AxisChangeListener.class, listener );
    }


    public boolean hasListener ( EventListener listener ) {
        List list = Arrays.asList ( this.listenerList.getListenerList() );
        return list.contains ( listener );
    }


    protected void notifyListeners ( AxisChangeEvent event ) {
        Object[] listeners = this.listenerList.getListenerList();
        for ( int i = listeners.length - 2; i >= 0; i -= 2 ) {
            if ( listeners[i] == AxisChangeListener.class ) {
                ( ( AxisChangeListener ) listeners[i + 1] ).axisChanged ( event );
            }
        }
    }


    protected void fireChangeEvent() {
        notifyListeners ( new AxisChangeEvent ( this ) );
    }


    protected Rectangle2D getLabelEnclosure ( Graphics2D g2, RectangleEdge edge ) {
        Rectangle2D result = new Rectangle2D.Double();
        Rectangle2D bounds = null;
        if ( this.attributedLabel != null ) {
            TextLayout layout = new TextLayout (
                this.attributedLabel.getIterator(),
                g2.getFontRenderContext() );
            bounds = layout.getBounds();
        } else {
            String axisLabel = getLabel();
            if ( axisLabel != null && !axisLabel.equals ( "" ) ) {
                FontMetrics fm = g2.getFontMetrics ( getLabelFont() );
                bounds = TextUtilities.getTextBounds ( axisLabel, g2, fm );
            }
        }
        if ( bounds != null ) {
            RectangleInsets insets = getLabelInsets();
            bounds = insets.createOutsetRectangle ( bounds );
            double angle = getLabelAngle();
            if ( edge == RectangleEdge.LEFT || edge == RectangleEdge.RIGHT ) {
                angle = angle - Math.PI / 2.0;
            }
            double x = bounds.getCenterX();
            double y = bounds.getCenterY();
            AffineTransform transformer
                = AffineTransform.getRotateInstance ( angle, x, y );
            Shape labelBounds = transformer.createTransformedShape ( bounds );
            result = labelBounds.getBounds2D();
        }
        return result;
    }

    protected double labelLocationX ( AxisLabelLocation location,
                                      Rectangle2D dataArea ) {
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

    protected double labelLocationY ( AxisLabelLocation location,
                                      Rectangle2D dataArea ) {
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

    protected TextAnchor labelAnchorH ( AxisLabelLocation location ) {
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

    protected TextAnchor labelAnchorV ( AxisLabelLocation location ) {
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


    protected AxisState drawLabel ( String label, Graphics2D g2,
                                    Rectangle2D plotArea, Rectangle2D dataArea, RectangleEdge edge,
                                    AxisState state ) {

        ParamChecks.nullNotPermitted ( state, "state" );

        if ( ( label == null ) || ( label.equals ( "" ) ) ) {
            return state;
        }

        Font font = getLabelFont();
        RectangleInsets insets = getLabelInsets();
        g2.setFont ( font );
        g2.setPaint ( getLabelPaint() );
        FontMetrics fm = g2.getFontMetrics();
        Rectangle2D labelBounds = TextUtilities.getTextBounds ( label, g2, fm );

        if ( edge == RectangleEdge.TOP ) {
            AffineTransform t = AffineTransform.getRotateInstance (
                                    getLabelAngle(), labelBounds.getCenterX(),
                                    labelBounds.getCenterY() );
            Shape rotatedLabelBounds = t.createTransformedShape ( labelBounds );
            labelBounds = rotatedLabelBounds.getBounds2D();
            double labelx = labelLocationX ( this.labelLocation, dataArea );
            double labely = state.getCursor() - insets.getBottom()
                            - labelBounds.getHeight() / 2.0;
            TextAnchor anchor = labelAnchorH ( this.labelLocation );
            TextUtilities.drawRotatedString ( label, g2, ( float ) labelx,
                                              ( float ) labely, anchor, getLabelAngle(), TextAnchor.CENTER );
            state.cursorUp ( insets.getTop() + labelBounds.getHeight()
                             + insets.getBottom() );
        } else if ( edge == RectangleEdge.BOTTOM ) {
            AffineTransform t = AffineTransform.getRotateInstance (
                                    getLabelAngle(), labelBounds.getCenterX(),
                                    labelBounds.getCenterY() );
            Shape rotatedLabelBounds = t.createTransformedShape ( labelBounds );
            labelBounds = rotatedLabelBounds.getBounds2D();
            double labelx = labelLocationX ( this.labelLocation, dataArea );
            double labely = state.getCursor()
                            + insets.getTop() + labelBounds.getHeight() / 2.0;
            TextAnchor anchor = labelAnchorH ( this.labelLocation );
            TextUtilities.drawRotatedString ( label, g2, ( float ) labelx,
                                              ( float ) labely, anchor, getLabelAngle(), TextAnchor.CENTER );
            state.cursorDown ( insets.getTop() + labelBounds.getHeight()
                               + insets.getBottom() );
        } else if ( edge == RectangleEdge.LEFT ) {
            AffineTransform t = AffineTransform.getRotateInstance (
                                    getLabelAngle() - Math.PI / 2.0, labelBounds.getCenterX(),
                                    labelBounds.getCenterY() );
            Shape rotatedLabelBounds = t.createTransformedShape ( labelBounds );
            labelBounds = rotatedLabelBounds.getBounds2D();
            double labelx = state.getCursor()
                            - insets.getRight() - labelBounds.getWidth() / 2.0;
            double labely = labelLocationY ( this.labelLocation, dataArea );
            TextAnchor anchor = labelAnchorV ( this.labelLocation );
            TextUtilities.drawRotatedString ( label, g2, ( float ) labelx,
                                              ( float ) labely, anchor, getLabelAngle() - Math.PI / 2.0,
                                              anchor );
            state.cursorLeft ( insets.getLeft() + labelBounds.getWidth()
                               + insets.getRight() );
        } else if ( edge == RectangleEdge.RIGHT ) {
            AffineTransform t = AffineTransform.getRotateInstance (
                                    getLabelAngle() + Math.PI / 2.0,
                                    labelBounds.getCenterX(), labelBounds.getCenterY() );
            Shape rotatedLabelBounds = t.createTransformedShape ( labelBounds );
            labelBounds = rotatedLabelBounds.getBounds2D();
            double labelx = state.getCursor()
                            + insets.getLeft() + labelBounds.getWidth() / 2.0;
            double labely = labelLocationY ( this.labelLocation, dataArea );
            TextAnchor anchor = labelAnchorV ( this.labelLocation );
            TextUtilities.drawRotatedString ( label, g2, ( float ) labelx,
                                              ( float ) labely, anchor, getLabelAngle() + Math.PI / 2.0,
                                              anchor );
            state.cursorRight ( insets.getLeft() + labelBounds.getWidth()
                                + insets.getRight() );
        }

        return state;

    }


    protected AxisState drawAttributedLabel ( AttributedString label,
            Graphics2D g2, Rectangle2D plotArea, Rectangle2D dataArea,
            RectangleEdge edge, AxisState state ) {

        ParamChecks.nullNotPermitted ( state, "state" );

        if ( label == null ) {
            return state;
        }

        RectangleInsets insets = getLabelInsets();
        g2.setFont ( getLabelFont() );
        g2.setPaint ( getLabelPaint() );
        TextLayout layout = new TextLayout ( this.attributedLabel.getIterator(),
                                             g2.getFontRenderContext() );
        Rectangle2D labelBounds = layout.getBounds();

        if ( edge == RectangleEdge.TOP ) {
            AffineTransform t = AffineTransform.getRotateInstance (
                                    getLabelAngle(), labelBounds.getCenterX(),
                                    labelBounds.getCenterY() );
            Shape rotatedLabelBounds = t.createTransformedShape ( labelBounds );
            labelBounds = rotatedLabelBounds.getBounds2D();
            double labelx = labelLocationX ( this.labelLocation, dataArea );
            double labely = state.getCursor() - insets.getBottom()
                            - labelBounds.getHeight() / 2.0;
            TextAnchor anchor = labelAnchorH ( this.labelLocation );
            AttrStringUtils.drawRotatedString ( label, g2, ( float ) labelx,
                                                ( float ) labely, anchor, getLabelAngle(), TextAnchor.CENTER );
            state.cursorUp ( insets.getTop() + labelBounds.getHeight()
                             + insets.getBottom() );
        } else if ( edge == RectangleEdge.BOTTOM ) {
            AffineTransform t = AffineTransform.getRotateInstance (
                                    getLabelAngle(), labelBounds.getCenterX(),
                                    labelBounds.getCenterY() );
            Shape rotatedLabelBounds = t.createTransformedShape ( labelBounds );
            labelBounds = rotatedLabelBounds.getBounds2D();
            double labelx = labelLocationX ( this.labelLocation, dataArea );
            double labely = state.getCursor()
                            + insets.getTop() + labelBounds.getHeight() / 2.0;
            TextAnchor anchor = labelAnchorH ( this.labelLocation );
            AttrStringUtils.drawRotatedString ( label, g2, ( float ) labelx,
                                                ( float ) labely, anchor, getLabelAngle(), TextAnchor.CENTER );
            state.cursorDown ( insets.getTop() + labelBounds.getHeight()
                               + insets.getBottom() );
        } else if ( edge == RectangleEdge.LEFT ) {
            AffineTransform t = AffineTransform.getRotateInstance (
                                    getLabelAngle() - Math.PI / 2.0, labelBounds.getCenterX(),
                                    labelBounds.getCenterY() );
            Shape rotatedLabelBounds = t.createTransformedShape ( labelBounds );
            labelBounds = rotatedLabelBounds.getBounds2D();
            double labelx = state.getCursor()
                            - insets.getRight() - labelBounds.getWidth() / 2.0;
            double labely = labelLocationY ( this.labelLocation, dataArea );
            TextAnchor anchor = labelAnchorV ( this.labelLocation );
            AttrStringUtils.drawRotatedString ( label, g2, ( float ) labelx,
                                                ( float ) labely, anchor, getLabelAngle() - Math.PI / 2.0,
                                                anchor );
            state.cursorLeft ( insets.getLeft() + labelBounds.getWidth()
                               + insets.getRight() );
        } else if ( edge == RectangleEdge.RIGHT ) {
            AffineTransform t = AffineTransform.getRotateInstance (
                                    getLabelAngle() + Math.PI / 2.0,
                                    labelBounds.getCenterX(), labelBounds.getCenterY() );
            Shape rotatedLabelBounds = t.createTransformedShape ( labelBounds );
            labelBounds = rotatedLabelBounds.getBounds2D();
            double labelx = state.getCursor()
                            + insets.getLeft() + labelBounds.getWidth() / 2.0;
            double labely = labelLocationY ( this.labelLocation, dataArea );
            TextAnchor anchor = labelAnchorV ( this.labelLocation );
            AttrStringUtils.drawRotatedString ( label, g2, ( float ) labelx,
                                                ( float ) labely, anchor, getLabelAngle() + Math.PI / 2.0,
                                                anchor );
            state.cursorRight ( insets.getLeft() + labelBounds.getWidth()
                                + insets.getRight() );
        }
        return state;
    }


    protected void drawAxisLine ( Graphics2D g2, double cursor,
                                  Rectangle2D dataArea, RectangleEdge edge ) {
        Line2D axisLine = null;
        double x = dataArea.getX();
        double y = dataArea.getY();
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
        Object saved = g2.getRenderingHint ( RenderingHints.KEY_STROKE_CONTROL );
        g2.setRenderingHint ( RenderingHints.KEY_STROKE_CONTROL,
                              RenderingHints.VALUE_STROKE_NORMALIZE );
        g2.draw ( axisLine );
        g2.setRenderingHint ( RenderingHints.KEY_STROKE_CONTROL, saved );
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        Axis clone = ( Axis ) super.clone();
        clone.plot = null;
        clone.listenerList = new EventListenerList();
        return clone;
    }


    @Override
    public boolean equals ( Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof Axis ) ) {
            return false;
        }
        Axis that = ( Axis ) obj;
        if ( this.visible != that.visible ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.label, that.label ) ) {
            return false;
        }
        if ( !AttributedStringUtilities.equal ( this.attributedLabel,
                                                that.attributedLabel ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.labelFont, that.labelFont ) ) {
            return false;
        }
        if ( !PaintUtilities.equal ( this.labelPaint, that.labelPaint ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.labelInsets, that.labelInsets ) ) {
            return false;
        }
        if ( this.labelAngle != that.labelAngle ) {
            return false;
        }
        if ( !this.labelLocation.equals ( that.labelLocation ) ) {
            return false;
        }
        if ( this.axisLineVisible != that.axisLineVisible ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.axisLineStroke, that.axisLineStroke ) ) {
            return false;
        }
        if ( !PaintUtilities.equal ( this.axisLinePaint, that.axisLinePaint ) ) {
            return false;
        }
        if ( this.tickLabelsVisible != that.tickLabelsVisible ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.tickLabelFont, that.tickLabelFont ) ) {
            return false;
        }
        if ( !PaintUtilities.equal ( this.tickLabelPaint, that.tickLabelPaint ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal (
                    this.tickLabelInsets, that.tickLabelInsets
                ) ) {
            return false;
        }
        if ( this.tickMarksVisible != that.tickMarksVisible ) {
            return false;
        }
        if ( this.tickMarkInsideLength != that.tickMarkInsideLength ) {
            return false;
        }
        if ( this.tickMarkOutsideLength != that.tickMarkOutsideLength ) {
            return false;
        }
        if ( !PaintUtilities.equal ( this.tickMarkPaint, that.tickMarkPaint ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.tickMarkStroke, that.tickMarkStroke ) ) {
            return false;
        }
        if ( this.minorTickMarksVisible != that.minorTickMarksVisible ) {
            return false;
        }
        if ( this.minorTickMarkInsideLength != that.minorTickMarkInsideLength ) {
            return false;
        }
        if ( this.minorTickMarkOutsideLength
                != that.minorTickMarkOutsideLength ) {
            return false;
        }
        if ( this.fixedDimension != that.fixedDimension ) {
            return false;
        }
        return true;
    }


    @Override
    public int hashCode() {
        int hash = 3;
        if ( this.label != null ) {
            hash = 83 * hash + this.label.hashCode();
        }
        return hash;
    }


    private void writeObject ( ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeAttributedString ( this.attributedLabel, stream );
        SerialUtilities.writePaint ( this.labelPaint, stream );
        SerialUtilities.writePaint ( this.tickLabelPaint, stream );
        SerialUtilities.writeStroke ( this.axisLineStroke, stream );
        SerialUtilities.writePaint ( this.axisLinePaint, stream );
        SerialUtilities.writeStroke ( this.tickMarkStroke, stream );
        SerialUtilities.writePaint ( this.tickMarkPaint, stream );
    }


    private void readObject ( ObjectInputStream stream )
    throws IOException, ClassNotFoundException {
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

}
