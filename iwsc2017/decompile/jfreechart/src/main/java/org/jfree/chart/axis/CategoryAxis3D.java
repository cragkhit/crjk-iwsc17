package org.jfree.chart.axis;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.Effect3D;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.ui.RectangleEdge;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.io.Serializable;
public class CategoryAxis3D extends CategoryAxis implements Cloneable, Serializable {
    private static final long serialVersionUID = 4114732251353700972L;
    public CategoryAxis3D() {
        this ( null );
    }
    public CategoryAxis3D ( final String label ) {
        super ( label );
    }
    @Override
    public AxisState draw ( final Graphics2D g2, final double cursor, final Rectangle2D plotArea, final Rectangle2D dataArea, final RectangleEdge edge, final PlotRenderingInfo plotState ) {
        if ( !this.isVisible() ) {
            return new AxisState ( cursor );
        }
        final CategoryPlot plot = ( CategoryPlot ) this.getPlot();
        final Rectangle2D adjustedDataArea = new Rectangle2D.Double();
        if ( plot.getRenderer() instanceof Effect3D ) {
            final Effect3D e3D = ( Effect3D ) plot.getRenderer();
            double adjustedX = dataArea.getMinX();
            double adjustedY = dataArea.getMinY();
            final double adjustedW = dataArea.getWidth() - e3D.getXOffset();
            final double adjustedH = dataArea.getHeight() - e3D.getYOffset();
            if ( edge == RectangleEdge.LEFT || edge == RectangleEdge.BOTTOM ) {
                adjustedY += e3D.getYOffset();
            } else if ( edge == RectangleEdge.RIGHT || edge == RectangleEdge.TOP ) {
                adjustedX += e3D.getXOffset();
            }
            adjustedDataArea.setRect ( adjustedX, adjustedY, adjustedW, adjustedH );
        } else {
            adjustedDataArea.setRect ( dataArea );
        }
        if ( this.isAxisLineVisible() ) {
            this.drawAxisLine ( g2, cursor, adjustedDataArea, edge );
        }
        AxisState state = new AxisState ( cursor );
        if ( this.isTickMarksVisible() ) {
            this.drawTickMarks ( g2, cursor, adjustedDataArea, edge, state );
        }
        state = this.drawCategoryLabels ( g2, plotArea, adjustedDataArea, edge, state, plotState );
        if ( this.getAttributedLabel() != null ) {
            state = this.drawAttributedLabel ( this.getAttributedLabel(), g2, plotArea, dataArea, edge, state );
        } else {
            state = this.drawLabel ( this.getLabel(), g2, plotArea, dataArea, edge, state );
        }
        return state;
    }
    @Override
    public double getCategoryJava2DCoordinate ( final CategoryAnchor anchor, final int category, final int categoryCount, final Rectangle2D area, final RectangleEdge edge ) {
        double result = 0.0;
        Rectangle2D adjustedArea = area;
        final CategoryPlot plot = ( CategoryPlot ) this.getPlot();
        final CategoryItemRenderer renderer = plot.getRenderer();
        if ( renderer instanceof Effect3D ) {
            final Effect3D e3D = ( Effect3D ) renderer;
            double adjustedX = area.getMinX();
            double adjustedY = area.getMinY();
            final double adjustedW = area.getWidth() - e3D.getXOffset();
            final double adjustedH = area.getHeight() - e3D.getYOffset();
            if ( edge == RectangleEdge.LEFT || edge == RectangleEdge.BOTTOM ) {
                adjustedY += e3D.getYOffset();
            } else if ( edge == RectangleEdge.RIGHT || edge == RectangleEdge.TOP ) {
                adjustedX += e3D.getXOffset();
            }
            adjustedArea = new Rectangle2D.Double ( adjustedX, adjustedY, adjustedW, adjustedH );
        }
        if ( anchor == CategoryAnchor.START ) {
            result = this.getCategoryStart ( category, categoryCount, adjustedArea, edge );
        } else if ( anchor == CategoryAnchor.MIDDLE ) {
            result = this.getCategoryMiddle ( category, categoryCount, adjustedArea, edge );
        } else if ( anchor == CategoryAnchor.END ) {
            result = this.getCategoryEnd ( category, categoryCount, adjustedArea, edge );
        }
        return result;
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
