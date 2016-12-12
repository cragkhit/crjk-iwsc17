package org.jfree.chart.renderer.xy;
import java.io.ObjectOutputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectInputStream;
import org.jfree.util.PublicCloneable;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.util.ShapeUtilities;
import java.awt.geom.Line2D;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.PlotRenderingInfo;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.data.xy.XYZDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.util.ParamChecks;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.BasicStroke;
import java.awt.Color;
import org.jfree.chart.renderer.LookupPaintScale;
import java.awt.Stroke;
import java.awt.Paint;
import org.jfree.chart.renderer.PaintScale;
import java.io.Serializable;
public class XYShapeRenderer extends AbstractXYItemRenderer implements XYItemRenderer, Cloneable, Serializable {
    private static final long serialVersionUID = 8320552104211173221L;
    private PaintScale paintScale;
    private boolean drawOutlines;
    private boolean useOutlinePaint;
    private boolean useFillPaint;
    private boolean guideLinesVisible;
    private transient Paint guideLinePaint;
    private transient Stroke guideLineStroke;
    public XYShapeRenderer() {
        this.paintScale = new LookupPaintScale();
        this.useFillPaint = false;
        this.drawOutlines = false;
        this.useOutlinePaint = true;
        this.guideLinesVisible = false;
        this.guideLinePaint = Color.darkGray;
        this.guideLineStroke = new BasicStroke();
        this.setBaseShape ( new Ellipse2D.Double ( -5.0, -5.0, 10.0, 10.0 ) );
        this.setAutoPopulateSeriesShape ( false );
    }
    public PaintScale getPaintScale() {
        return this.paintScale;
    }
    public void setPaintScale ( final PaintScale scale ) {
        ParamChecks.nullNotPermitted ( scale, "scale" );
        this.paintScale = scale;
        this.notifyListeners ( new RendererChangeEvent ( this ) );
    }
    public boolean getDrawOutlines() {
        return this.drawOutlines;
    }
    public void setDrawOutlines ( final boolean flag ) {
        this.drawOutlines = flag;
        this.fireChangeEvent();
    }
    public boolean getUseFillPaint() {
        return this.useFillPaint;
    }
    public void setUseFillPaint ( final boolean flag ) {
        this.useFillPaint = flag;
        this.fireChangeEvent();
    }
    public boolean getUseOutlinePaint() {
        return this.useOutlinePaint;
    }
    public void setUseOutlinePaint ( final boolean use ) {
        this.useOutlinePaint = use;
        this.fireChangeEvent();
    }
    public boolean isGuideLinesVisible() {
        return this.guideLinesVisible;
    }
    public void setGuideLinesVisible ( final boolean visible ) {
        this.guideLinesVisible = visible;
        this.fireChangeEvent();
    }
    public Paint getGuideLinePaint() {
        return this.guideLinePaint;
    }
    public void setGuideLinePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.guideLinePaint = paint;
        this.fireChangeEvent();
    }
    public Stroke getGuideLineStroke() {
        return this.guideLineStroke;
    }
    public void setGuideLineStroke ( final Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.guideLineStroke = stroke;
        this.fireChangeEvent();
    }
    @Override
    public Range findDomainBounds ( final XYDataset dataset ) {
        if ( dataset == null ) {
            return null;
        }
        final Range r = DatasetUtilities.findDomainBounds ( dataset, false );
        if ( r == null ) {
            return null;
        }
        final double offset = 0.0;
        return new Range ( r.getLowerBound() + offset, r.getUpperBound() + offset );
    }
    @Override
    public Range findRangeBounds ( final XYDataset dataset ) {
        if ( dataset == null ) {
            return null;
        }
        final Range r = DatasetUtilities.findRangeBounds ( dataset, false );
        if ( r == null ) {
            return null;
        }
        final double offset = 0.0;
        return new Range ( r.getLowerBound() + offset, r.getUpperBound() + offset );
    }
    public Range findZBounds ( final XYZDataset dataset ) {
        if ( dataset != null ) {
            return DatasetUtilities.findZBounds ( dataset );
        }
        return null;
    }
    @Override
    public int getPassCount() {
        return 2;
    }
    @Override
    public void drawItem ( final Graphics2D g2, final XYItemRendererState state, final Rectangle2D dataArea, final PlotRenderingInfo info, final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis, final XYDataset dataset, final int series, final int item, final CrosshairState crosshairState, final int pass ) {
        EntityCollection entities = null;
        if ( info != null ) {
            entities = info.getOwner().getEntityCollection();
        }
        final double x = dataset.getXValue ( series, item );
        final double y = dataset.getYValue ( series, item );
        if ( Double.isNaN ( x ) || Double.isNaN ( y ) ) {
            return;
        }
        final double transX = domainAxis.valueToJava2D ( x, dataArea, plot.getDomainAxisEdge() );
        final double transY = rangeAxis.valueToJava2D ( y, dataArea, plot.getRangeAxisEdge() );
        final PlotOrientation orientation = plot.getOrientation();
        if ( pass == 0 && this.guideLinesVisible ) {
            g2.setStroke ( this.guideLineStroke );
            g2.setPaint ( this.guideLinePaint );
            if ( orientation == PlotOrientation.HORIZONTAL ) {
                g2.draw ( new Line2D.Double ( transY, dataArea.getMinY(), transY, dataArea.getMaxY() ) );
                g2.draw ( new Line2D.Double ( dataArea.getMinX(), transX, dataArea.getMaxX(), transX ) );
            } else {
                g2.draw ( new Line2D.Double ( transX, dataArea.getMinY(), transX, dataArea.getMaxY() ) );
                g2.draw ( new Line2D.Double ( dataArea.getMinX(), transY, dataArea.getMaxX(), transY ) );
            }
        } else if ( pass == 1 ) {
            Shape shape = this.getItemShape ( series, item );
            if ( orientation == PlotOrientation.HORIZONTAL ) {
                shape = ShapeUtilities.createTranslatedShape ( shape, transY, transX );
            } else if ( orientation == PlotOrientation.VERTICAL ) {
                shape = ShapeUtilities.createTranslatedShape ( shape, transX, transY );
            }
            final Shape hotspot = shape;
            if ( shape.intersects ( dataArea ) ) {
                g2.setPaint ( this.getPaint ( dataset, series, item ) );
                g2.fill ( shape );
                if ( this.drawOutlines ) {
                    if ( this.getUseOutlinePaint() ) {
                        g2.setPaint ( this.getItemOutlinePaint ( series, item ) );
                    } else {
                        g2.setPaint ( this.getItemPaint ( series, item ) );
                    }
                    g2.setStroke ( this.getItemOutlineStroke ( series, item ) );
                    g2.draw ( shape );
                }
            }
            final int domainAxisIndex = plot.getDomainAxisIndex ( domainAxis );
            final int rangeAxisIndex = plot.getRangeAxisIndex ( rangeAxis );
            this.updateCrosshairValues ( crosshairState, x, y, domainAxisIndex, rangeAxisIndex, transX, transY, orientation );
            if ( entities != null ) {
                this.addEntity ( entities, hotspot, dataset, series, item, transX, transY );
            }
        }
    }
    protected Paint getPaint ( final XYDataset dataset, final int series, final int item ) {
        Paint p;
        if ( dataset instanceof XYZDataset ) {
            final double z = ( ( XYZDataset ) dataset ).getZValue ( series, item );
            p = this.paintScale.getPaint ( z );
        } else if ( this.useFillPaint ) {
            p = this.getItemFillPaint ( series, item );
        } else {
            p = this.getItemPaint ( series, item );
        }
        return p;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof XYShapeRenderer ) ) {
            return false;
        }
        final XYShapeRenderer that = ( XYShapeRenderer ) obj;
        return this.paintScale.equals ( that.paintScale ) && this.drawOutlines == that.drawOutlines && this.useOutlinePaint == that.useOutlinePaint && this.useFillPaint == that.useFillPaint && this.guideLinesVisible == that.guideLinesVisible && this.guideLinePaint.equals ( that.guideLinePaint ) && this.guideLineStroke.equals ( that.guideLineStroke ) && super.equals ( obj );
    }
    public Object clone() throws CloneNotSupportedException {
        final XYShapeRenderer clone = ( XYShapeRenderer ) super.clone();
        if ( this.paintScale instanceof PublicCloneable ) {
            final PublicCloneable pc = ( PublicCloneable ) this.paintScale;
            clone.paintScale = ( PaintScale ) pc.clone();
        }
        return clone;
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.guideLinePaint = SerialUtilities.readPaint ( stream );
        this.guideLineStroke = SerialUtilities.readStroke ( stream );
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.guideLinePaint, stream );
        SerialUtilities.writeStroke ( this.guideLineStroke, stream );
    }
}
