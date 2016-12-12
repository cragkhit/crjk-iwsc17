package org.jfree.chart.plot;
import org.jfree.chart.util.ResourceBundleWrapper;
import java.awt.BasicStroke;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.PaintUtilities;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.ArrayUtilities;
import java.util.Iterator;
import java.awt.geom.Line2D;
import org.jfree.chart.axis.ValueTick;
import java.awt.RenderingHints;
import java.util.List;
import org.jfree.chart.axis.AxisState;
import org.jfree.ui.RectangleInsets;
import java.awt.Composite;
import java.awt.AlphaComposite;
import java.awt.Shape;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.axis.AxisSpace;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.awt.Color;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.util.ParamChecks;
import org.jfree.chart.axis.NumberAxis;
import java.util.ResourceBundle;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.Range;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.Serializable;
public class FastScatterPlot extends Plot implements ValueAxisPlot, Pannable, Zoomable, Cloneable, Serializable {
    private static final long serialVersionUID = 7871545897358563521L;
    public static final Stroke DEFAULT_GRIDLINE_STROKE;
    public static final Paint DEFAULT_GRIDLINE_PAINT;
    private float[][] data;
    private Range xDataRange;
    private Range yDataRange;
    private ValueAxis domainAxis;
    private ValueAxis rangeAxis;
    private transient Paint paint;
    private boolean domainGridlinesVisible;
    private transient Stroke domainGridlineStroke;
    private transient Paint domainGridlinePaint;
    private boolean rangeGridlinesVisible;
    private transient Stroke rangeGridlineStroke;
    private transient Paint rangeGridlinePaint;
    private boolean domainPannable;
    private boolean rangePannable;
    protected static ResourceBundle localizationResources;
    public FastScatterPlot() {
        this ( null, new NumberAxis ( "X" ), new NumberAxis ( "Y" ) );
    }
    public FastScatterPlot ( final float[][] data, final ValueAxis domainAxis, final ValueAxis rangeAxis ) {
        ParamChecks.nullNotPermitted ( domainAxis, "domainAxis" );
        ParamChecks.nullNotPermitted ( rangeAxis, "rangeAxis" );
        this.data = data;
        this.xDataRange = this.calculateXDataRange ( data );
        this.yDataRange = this.calculateYDataRange ( data );
        ( this.domainAxis = domainAxis ).setPlot ( this );
        this.domainAxis.addChangeListener ( this );
        ( this.rangeAxis = rangeAxis ).setPlot ( this );
        this.rangeAxis.addChangeListener ( this );
        this.paint = Color.red;
        this.domainGridlinesVisible = true;
        this.domainGridlinePaint = FastScatterPlot.DEFAULT_GRIDLINE_PAINT;
        this.domainGridlineStroke = FastScatterPlot.DEFAULT_GRIDLINE_STROKE;
        this.rangeGridlinesVisible = true;
        this.rangeGridlinePaint = FastScatterPlot.DEFAULT_GRIDLINE_PAINT;
        this.rangeGridlineStroke = FastScatterPlot.DEFAULT_GRIDLINE_STROKE;
    }
    @Override
    public String getPlotType() {
        return FastScatterPlot.localizationResources.getString ( "Fast_Scatter_Plot" );
    }
    public float[][] getData() {
        return this.data;
    }
    public void setData ( final float[][] data ) {
        this.data = data;
        this.fireChangeEvent();
    }
    @Override
    public PlotOrientation getOrientation() {
        return PlotOrientation.VERTICAL;
    }
    public ValueAxis getDomainAxis() {
        return this.domainAxis;
    }
    public void setDomainAxis ( final ValueAxis axis ) {
        ParamChecks.nullNotPermitted ( axis, "axis" );
        this.domainAxis = axis;
        this.fireChangeEvent();
    }
    public ValueAxis getRangeAxis() {
        return this.rangeAxis;
    }
    public void setRangeAxis ( final ValueAxis axis ) {
        ParamChecks.nullNotPermitted ( axis, "axis" );
        this.rangeAxis = axis;
        this.fireChangeEvent();
    }
    public Paint getPaint() {
        return this.paint;
    }
    public void setPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.paint = paint;
        this.fireChangeEvent();
    }
    public boolean isDomainGridlinesVisible() {
        return this.domainGridlinesVisible;
    }
    public void setDomainGridlinesVisible ( final boolean visible ) {
        if ( this.domainGridlinesVisible != visible ) {
            this.domainGridlinesVisible = visible;
            this.fireChangeEvent();
        }
    }
    public Stroke getDomainGridlineStroke() {
        return this.domainGridlineStroke;
    }
    public void setDomainGridlineStroke ( final Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.domainGridlineStroke = stroke;
        this.fireChangeEvent();
    }
    public Paint getDomainGridlinePaint() {
        return this.domainGridlinePaint;
    }
    public void setDomainGridlinePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.domainGridlinePaint = paint;
        this.fireChangeEvent();
    }
    public boolean isRangeGridlinesVisible() {
        return this.rangeGridlinesVisible;
    }
    public void setRangeGridlinesVisible ( final boolean visible ) {
        if ( this.rangeGridlinesVisible != visible ) {
            this.rangeGridlinesVisible = visible;
            this.fireChangeEvent();
        }
    }
    public Stroke getRangeGridlineStroke() {
        return this.rangeGridlineStroke;
    }
    public void setRangeGridlineStroke ( final Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.rangeGridlineStroke = stroke;
        this.fireChangeEvent();
    }
    public Paint getRangeGridlinePaint() {
        return this.rangeGridlinePaint;
    }
    public void setRangeGridlinePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.rangeGridlinePaint = paint;
        this.fireChangeEvent();
    }
    @Override
    public void draw ( final Graphics2D g2, final Rectangle2D area, final Point2D anchor, final PlotState parentState, final PlotRenderingInfo info ) {
        if ( info != null ) {
            info.setPlotArea ( area );
        }
        final RectangleInsets insets = this.getInsets();
        insets.trim ( area );
        AxisSpace space = new AxisSpace();
        space = this.domainAxis.reserveSpace ( g2, this, area, RectangleEdge.BOTTOM, space );
        space = this.rangeAxis.reserveSpace ( g2, this, area, RectangleEdge.LEFT, space );
        final Rectangle2D dataArea = space.shrink ( area, null );
        if ( info != null ) {
            info.setDataArea ( dataArea );
        }
        this.drawBackground ( g2, dataArea );
        final AxisState domainAxisState = this.domainAxis.draw ( g2, dataArea.getMaxY(), area, dataArea, RectangleEdge.BOTTOM, info );
        final AxisState rangeAxisState = this.rangeAxis.draw ( g2, dataArea.getMinX(), area, dataArea, RectangleEdge.LEFT, info );
        this.drawDomainGridlines ( g2, dataArea, domainAxisState.getTicks() );
        this.drawRangeGridlines ( g2, dataArea, rangeAxisState.getTicks() );
        final Shape originalClip = g2.getClip();
        final Composite originalComposite = g2.getComposite();
        g2.clip ( dataArea );
        g2.setComposite ( AlphaComposite.getInstance ( 3, this.getForegroundAlpha() ) );
        this.render ( g2, dataArea, info, null );
        g2.setClip ( originalClip );
        g2.setComposite ( originalComposite );
        this.drawOutline ( g2, dataArea );
    }
    public void render ( final Graphics2D g2, final Rectangle2D dataArea, final PlotRenderingInfo info, final CrosshairState crosshairState ) {
        g2.setPaint ( this.paint );
        if ( this.data != null ) {
            for ( int i = 0; i < this.data[0].length; ++i ) {
                final float x = this.data[0][i];
                final float y = this.data[1][i];
                final int transX = ( int ) this.domainAxis.valueToJava2D ( x, dataArea, RectangleEdge.BOTTOM );
                final int transY = ( int ) this.rangeAxis.valueToJava2D ( y, dataArea, RectangleEdge.LEFT );
                g2.fillRect ( transX, transY, 1, 1 );
            }
        }
    }
    protected void drawDomainGridlines ( final Graphics2D g2, final Rectangle2D dataArea, final List ticks ) {
        if ( !this.isDomainGridlinesVisible() ) {
            return;
        }
        final Object saved = g2.getRenderingHint ( RenderingHints.KEY_STROKE_CONTROL );
        g2.setRenderingHint ( RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE );
        for ( final ValueTick tick : ticks ) {
            final double v = this.domainAxis.valueToJava2D ( tick.getValue(), dataArea, RectangleEdge.BOTTOM );
            final Line2D line = new Line2D.Double ( v, dataArea.getMinY(), v, dataArea.getMaxY() );
            g2.setPaint ( this.getDomainGridlinePaint() );
            g2.setStroke ( this.getDomainGridlineStroke() );
            g2.draw ( line );
        }
        g2.setRenderingHint ( RenderingHints.KEY_STROKE_CONTROL, saved );
    }
    protected void drawRangeGridlines ( final Graphics2D g2, final Rectangle2D dataArea, final List ticks ) {
        if ( !this.isRangeGridlinesVisible() ) {
            return;
        }
        final Object saved = g2.getRenderingHint ( RenderingHints.KEY_STROKE_CONTROL );
        g2.setRenderingHint ( RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE );
        for ( final ValueTick tick : ticks ) {
            final double v = this.rangeAxis.valueToJava2D ( tick.getValue(), dataArea, RectangleEdge.LEFT );
            final Line2D line = new Line2D.Double ( dataArea.getMinX(), v, dataArea.getMaxX(), v );
            g2.setPaint ( this.getRangeGridlinePaint() );
            g2.setStroke ( this.getRangeGridlineStroke() );
            g2.draw ( line );
        }
        g2.setRenderingHint ( RenderingHints.KEY_STROKE_CONTROL, saved );
    }
    @Override
    public Range getDataRange ( final ValueAxis axis ) {
        Range result = null;
        if ( axis == this.domainAxis ) {
            result = this.xDataRange;
        } else if ( axis == this.rangeAxis ) {
            result = this.yDataRange;
        }
        return result;
    }
    private Range calculateXDataRange ( final float[][] data ) {
        Range result = null;
        if ( data != null ) {
            float lowest = Float.POSITIVE_INFINITY;
            float highest = Float.NEGATIVE_INFINITY;
            for ( int i = 0; i < data[0].length; ++i ) {
                final float v = data[0][i];
                if ( v < lowest ) {
                    lowest = v;
                }
                if ( v > highest ) {
                    highest = v;
                }
            }
            if ( lowest <= highest ) {
                result = new Range ( lowest, highest );
            }
        }
        return result;
    }
    private Range calculateYDataRange ( final float[][] data ) {
        Range result = null;
        if ( data != null ) {
            float lowest = Float.POSITIVE_INFINITY;
            float highest = Float.NEGATIVE_INFINITY;
            for ( int i = 0; i < data[0].length; ++i ) {
                final float v = data[1][i];
                if ( v < lowest ) {
                    lowest = v;
                }
                if ( v > highest ) {
                    highest = v;
                }
            }
            if ( lowest <= highest ) {
                result = new Range ( lowest, highest );
            }
        }
        return result;
    }
    @Override
    public void zoomDomainAxes ( final double factor, final PlotRenderingInfo info, final Point2D source ) {
        this.domainAxis.resizeRange ( factor );
    }
    @Override
    public void zoomDomainAxes ( final double factor, final PlotRenderingInfo info, final Point2D source, final boolean useAnchor ) {
        if ( useAnchor ) {
            final double sourceX = source.getX();
            final double anchorX = this.domainAxis.java2DToValue ( sourceX, info.getDataArea(), RectangleEdge.BOTTOM );
            this.domainAxis.resizeRange2 ( factor, anchorX );
        } else {
            this.domainAxis.resizeRange ( factor );
        }
    }
    @Override
    public void zoomDomainAxes ( final double lowerPercent, final double upperPercent, final PlotRenderingInfo info, final Point2D source ) {
        this.domainAxis.zoomRange ( lowerPercent, upperPercent );
    }
    @Override
    public void zoomRangeAxes ( final double factor, final PlotRenderingInfo info, final Point2D source ) {
        this.rangeAxis.resizeRange ( factor );
    }
    @Override
    public void zoomRangeAxes ( final double factor, final PlotRenderingInfo info, final Point2D source, final boolean useAnchor ) {
        if ( useAnchor ) {
            final double sourceY = source.getY();
            final double anchorY = this.rangeAxis.java2DToValue ( sourceY, info.getDataArea(), RectangleEdge.LEFT );
            this.rangeAxis.resizeRange2 ( factor, anchorY );
        } else {
            this.rangeAxis.resizeRange ( factor );
        }
    }
    @Override
    public void zoomRangeAxes ( final double lowerPercent, final double upperPercent, final PlotRenderingInfo info, final Point2D source ) {
        this.rangeAxis.zoomRange ( lowerPercent, upperPercent );
    }
    @Override
    public boolean isDomainZoomable() {
        return true;
    }
    @Override
    public boolean isRangeZoomable() {
        return true;
    }
    @Override
    public boolean isDomainPannable() {
        return this.domainPannable;
    }
    public void setDomainPannable ( final boolean pannable ) {
        this.domainPannable = pannable;
    }
    @Override
    public boolean isRangePannable() {
        return this.rangePannable;
    }
    public void setRangePannable ( final boolean pannable ) {
        this.rangePannable = pannable;
    }
    @Override
    public void panDomainAxes ( final double percent, final PlotRenderingInfo info, final Point2D source ) {
        if ( !this.isDomainPannable() || this.domainAxis == null ) {
            return;
        }
        final double length = this.domainAxis.getRange().getLength();
        double adj = percent * length;
        if ( this.domainAxis.isInverted() ) {
            adj = -adj;
        }
        this.domainAxis.setRange ( this.domainAxis.getLowerBound() + adj, this.domainAxis.getUpperBound() + adj );
    }
    @Override
    public void panRangeAxes ( final double percent, final PlotRenderingInfo info, final Point2D source ) {
        if ( !this.isRangePannable() || this.rangeAxis == null ) {
            return;
        }
        final double length = this.rangeAxis.getRange().getLength();
        double adj = percent * length;
        if ( this.rangeAxis.isInverted() ) {
            adj = -adj;
        }
        this.rangeAxis.setRange ( this.rangeAxis.getLowerBound() + adj, this.rangeAxis.getUpperBound() + adj );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        if ( ! ( obj instanceof FastScatterPlot ) ) {
            return false;
        }
        final FastScatterPlot that = ( FastScatterPlot ) obj;
        return this.domainPannable == that.domainPannable && this.rangePannable == that.rangePannable && ArrayUtilities.equal ( this.data, that.data ) && ObjectUtilities.equal ( ( Object ) this.domainAxis, ( Object ) that.domainAxis ) && ObjectUtilities.equal ( ( Object ) this.rangeAxis, ( Object ) that.rangeAxis ) && PaintUtilities.equal ( this.paint, that.paint ) && this.domainGridlinesVisible == that.domainGridlinesVisible && PaintUtilities.equal ( this.domainGridlinePaint, that.domainGridlinePaint ) && ObjectUtilities.equal ( ( Object ) this.domainGridlineStroke, ( Object ) that.domainGridlineStroke ) && !this.rangeGridlinesVisible != that.rangeGridlinesVisible && PaintUtilities.equal ( this.rangeGridlinePaint, that.rangeGridlinePaint ) && ObjectUtilities.equal ( ( Object ) this.rangeGridlineStroke, ( Object ) that.rangeGridlineStroke );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final FastScatterPlot clone = ( FastScatterPlot ) super.clone();
        if ( this.data != null ) {
            clone.data = ArrayUtilities.clone ( this.data );
        }
        if ( this.domainAxis != null ) {
            ( clone.domainAxis = ( ValueAxis ) this.domainAxis.clone() ).setPlot ( clone );
            clone.domainAxis.addChangeListener ( clone );
        }
        if ( this.rangeAxis != null ) {
            ( clone.rangeAxis = ( ValueAxis ) this.rangeAxis.clone() ).setPlot ( clone );
            clone.rangeAxis.addChangeListener ( clone );
        }
        return clone;
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.paint, stream );
        SerialUtilities.writeStroke ( this.domainGridlineStroke, stream );
        SerialUtilities.writePaint ( this.domainGridlinePaint, stream );
        SerialUtilities.writeStroke ( this.rangeGridlineStroke, stream );
        SerialUtilities.writePaint ( this.rangeGridlinePaint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.paint = SerialUtilities.readPaint ( stream );
        this.domainGridlineStroke = SerialUtilities.readStroke ( stream );
        this.domainGridlinePaint = SerialUtilities.readPaint ( stream );
        this.rangeGridlineStroke = SerialUtilities.readStroke ( stream );
        this.rangeGridlinePaint = SerialUtilities.readPaint ( stream );
        if ( this.domainAxis != null ) {
            this.domainAxis.addChangeListener ( this );
        }
        if ( this.rangeAxis != null ) {
            this.rangeAxis.addChangeListener ( this );
        }
    }
    static {
        DEFAULT_GRIDLINE_STROKE = new BasicStroke ( 0.5f, 0, 2, 0.0f, new float[] { 2.0f, 2.0f }, 0.0f );
        DEFAULT_GRIDLINE_PAINT = Color.lightGray;
        FastScatterPlot.localizationResources = ResourceBundleWrapper.getBundle ( "org.jfree.chart.plot.LocalizationBundle" );
    }
}
