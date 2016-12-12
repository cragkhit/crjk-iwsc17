package org.jfree.chart.renderer.xy;
import java.io.ObjectOutputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectInputStream;
import org.jfree.util.ShapeUtilities;
import java.awt.geom.PathIterator;
import org.jfree.ui.RectangleEdge;
import java.awt.geom.AffineTransform;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.axis.ValueAxis;
import java.awt.geom.GeneralPath;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.plot.XYPlot;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.chart.util.ParamChecks;
import java.awt.geom.Line2D;
import java.awt.Shape;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class SamplingXYLineRenderer extends AbstractXYItemRenderer implements XYItemRenderer, Cloneable, PublicCloneable, Serializable {
    private transient Shape legendLine;
    public SamplingXYLineRenderer() {
        this.setBaseLegendShape ( this.legendLine = new Line2D.Double ( -7.0, 0.0, 7.0, 0.0 ) );
        this.setTreatLegendShapeAsLine ( true );
    }
    public Shape getLegendLine() {
        return this.legendLine;
    }
    public void setLegendLine ( final Shape line ) {
        ParamChecks.nullNotPermitted ( line, "line" );
        this.legendLine = line;
        this.fireChangeEvent();
    }
    @Override
    public int getPassCount() {
        return 1;
    }
    @Override
    public XYItemRendererState initialise ( final Graphics2D g2, final Rectangle2D dataArea, final XYPlot plot, final XYDataset data, final PlotRenderingInfo info ) {
        final double dpi = 72.0;
        final State state = new State ( info );
        state.seriesPath = new GeneralPath();
        state.intervalPath = new GeneralPath();
        state.dX = 72.0 / dpi;
        return state;
    }
    @Override
    public void drawItem ( final Graphics2D g2, final XYItemRendererState state, final Rectangle2D dataArea, final PlotRenderingInfo info, final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis, final XYDataset dataset, final int series, final int item, final CrosshairState crosshairState, final int pass ) {
        if ( !this.getItemVisible ( series, item ) ) {
            return;
        }
        final RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        final RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
        final double x1 = dataset.getXValue ( series, item );
        final double y1 = dataset.getYValue ( series, item );
        final double transX1 = domainAxis.valueToJava2D ( x1, dataArea, xAxisLocation );
        final double transY1 = rangeAxis.valueToJava2D ( y1, dataArea, yAxisLocation );
        final State s = ( State ) state;
        if ( !Double.isNaN ( transX1 ) && !Double.isNaN ( transY1 ) ) {
            float x2 = ( float ) transX1;
            float y2 = ( float ) transY1;
            final PlotOrientation orientation = plot.getOrientation();
            if ( orientation == PlotOrientation.HORIZONTAL ) {
                x2 = ( float ) transY1;
                y2 = ( float ) transX1;
            }
            if ( s.lastPointGood ) {
                if ( Math.abs ( x2 - s.lastX ) > s.dX ) {
                    s.seriesPath.lineTo ( x2, y2 );
                    if ( s.lowY < s.highY ) {
                        s.intervalPath.moveTo ( ( float ) s.lastX, ( float ) s.lowY );
                        s.intervalPath.lineTo ( ( float ) s.lastX, ( float ) s.highY );
                    }
                    s.lastX = x2;
                    s.openY = y2;
                    s.highY = y2;
                    s.lowY = y2;
                    s.closeY = y2;
                } else {
                    s.highY = Math.max ( s.highY, y2 );
                    s.lowY = Math.min ( s.lowY, y2 );
                    s.closeY = y2;
                }
            } else {
                s.seriesPath.moveTo ( x2, y2 );
                s.lastX = x2;
                s.openY = y2;
                s.highY = y2;
                s.lowY = y2;
                s.closeY = y2;
            }
            s.lastPointGood = true;
        } else {
            s.lastPointGood = false;
        }
        if ( item == s.getLastItemIndex() ) {
            final PathIterator pi = s.seriesPath.getPathIterator ( null );
            int count = 0;
            while ( !pi.isDone() ) {
                ++count;
                pi.next();
            }
            g2.setStroke ( this.getItemStroke ( series, item ) );
            g2.setPaint ( this.getItemPaint ( series, item ) );
            g2.draw ( s.seriesPath );
            g2.draw ( s.intervalPath );
        }
    }
    public Object clone() throws CloneNotSupportedException {
        final SamplingXYLineRenderer clone = ( SamplingXYLineRenderer ) super.clone();
        if ( this.legendLine != null ) {
            clone.legendLine = ShapeUtilities.clone ( this.legendLine );
        }
        return clone;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof SamplingXYLineRenderer ) ) {
            return false;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        final SamplingXYLineRenderer that = ( SamplingXYLineRenderer ) obj;
        return ShapeUtilities.equal ( this.legendLine, that.legendLine );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.legendLine = SerialUtilities.readShape ( stream );
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeShape ( this.legendLine, stream );
    }
    public static class State extends XYItemRendererState {
        GeneralPath seriesPath;
        GeneralPath intervalPath;
        double dX;
        double lastX;
        double openY;
        double highY;
        double lowY;
        double closeY;
        boolean lastPointGood;
        public State ( final PlotRenderingInfo info ) {
            super ( info );
            this.dX = 1.0;
            this.openY = 0.0;
            this.highY = 0.0;
            this.lowY = 0.0;
            this.closeY = 0.0;
        }
        @Override
        public void startSeriesPass ( final XYDataset dataset, final int series, final int firstItem, final int lastItem, final int pass, final int passCount ) {
            this.seriesPath.reset();
            this.intervalPath.reset();
            this.lastPointGood = false;
            super.startSeriesPass ( dataset, series, firstItem, lastItem, pass, passCount );
        }
    }
}
