package org.jfree.chart.axis;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.plot.Plot;
import java.util.List;
import org.jfree.chart.Effect3D;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.ui.RectangleEdge;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.io.Serializable;
public class NumberAxis3D extends NumberAxis implements Serializable {
    private static final long serialVersionUID = -1790205852569123512L;
    public NumberAxis3D() {
        this ( null );
    }
    public NumberAxis3D ( final String label ) {
        super ( label );
    }
    @Override
    public AxisState draw ( final Graphics2D g2, final double cursor, final Rectangle2D plotArea, final Rectangle2D dataArea, final RectangleEdge edge, final PlotRenderingInfo plotState ) {
        if ( !this.isVisible() ) {
            final AxisState state = new AxisState ( cursor );
            final List ticks = this.refreshTicks ( g2, state, dataArea, edge );
            state.setTicks ( ticks );
            return state;
        }
        double xOffset = 0.0;
        double yOffset = 0.0;
        final Plot plot = this.getPlot();
        if ( plot instanceof CategoryPlot ) {
            final CategoryPlot cp = ( CategoryPlot ) plot;
            final CategoryItemRenderer r = cp.getRenderer();
            if ( r instanceof Effect3D ) {
                final Effect3D e3D = ( Effect3D ) r;
                xOffset = e3D.getXOffset();
                yOffset = e3D.getYOffset();
            }
        }
        double adjustedX = dataArea.getMinX();
        double adjustedY = dataArea.getMinY();
        final double adjustedW = dataArea.getWidth() - xOffset;
        final double adjustedH = dataArea.getHeight() - yOffset;
        if ( edge == RectangleEdge.LEFT || edge == RectangleEdge.BOTTOM ) {
            adjustedY += yOffset;
        } else if ( edge == RectangleEdge.RIGHT || edge == RectangleEdge.TOP ) {
            adjustedX += xOffset;
        }
        final Rectangle2D adjustedDataArea = new Rectangle2D.Double ( adjustedX, adjustedY, adjustedW, adjustedH );
        AxisState info = this.drawTickMarksAndLabels ( g2, cursor, plotArea, adjustedDataArea, edge );
        if ( this.getAttributedLabel() != null ) {
            info = this.drawAttributedLabel ( this.getAttributedLabel(), g2, plotArea, dataArea, edge, info );
        } else {
            info = this.drawLabel ( this.getLabel(), g2, plotArea, dataArea, edge, info );
        }
        return info;
    }
}
