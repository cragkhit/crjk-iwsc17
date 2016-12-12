package org.jfree.chart.renderer.xy;
import java.awt.geom.Rectangle2D;
import java.awt.Color;
import java.awt.Stroke;
import org.jfree.ui.GradientPaintTransformer;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.GradientPaint;
import org.jfree.ui.RectangleEdge;
import java.awt.geom.RectangularShape;
import java.awt.Graphics2D;
import java.io.Serializable;
public class StandardXYBarPainter implements XYBarPainter, Serializable {
    @Override
    public void paintBar ( final Graphics2D g2, final XYBarRenderer renderer, final int row, final int column, final RectangularShape bar, final RectangleEdge base ) {
        Paint itemPaint = renderer.getItemPaint ( row, column );
        final GradientPaintTransformer t = renderer.getGradientPaintTransformer();
        if ( t != null && itemPaint instanceof GradientPaint ) {
            itemPaint = t.transform ( ( GradientPaint ) itemPaint, ( Shape ) bar );
        }
        g2.setPaint ( itemPaint );
        g2.fill ( bar );
        if ( renderer.isDrawBarOutline() ) {
            final Stroke stroke = renderer.getItemOutlineStroke ( row, column );
            final Paint paint = renderer.getItemOutlinePaint ( row, column );
            if ( stroke != null && paint != null ) {
                g2.setStroke ( stroke );
                g2.setPaint ( paint );
                g2.draw ( bar );
            }
        }
    }
    @Override
    public void paintBarShadow ( final Graphics2D g2, final XYBarRenderer renderer, final int row, final int column, final RectangularShape bar, final RectangleEdge base, final boolean pegShadow ) {
        final Paint itemPaint = renderer.getItemPaint ( row, column );
        if ( itemPaint instanceof Color ) {
            final Color c = ( Color ) itemPaint;
            if ( c.getAlpha() == 0 ) {
                return;
            }
        }
        final RectangularShape shadow = this.createShadow ( bar, renderer.getShadowXOffset(), renderer.getShadowYOffset(), base, pegShadow );
        g2.setPaint ( Color.gray );
        g2.fill ( shadow );
    }
    private Rectangle2D createShadow ( final RectangularShape bar, final double xOffset, final double yOffset, final RectangleEdge base, final boolean pegShadow ) {
        double x0 = bar.getMinX();
        double x = bar.getMaxX();
        double y0 = bar.getMinY();
        double y = bar.getMaxY();
        if ( base == RectangleEdge.TOP ) {
            x0 += xOffset;
            x += xOffset;
            if ( !pegShadow ) {
                y0 += yOffset;
            }
            y += yOffset;
        } else if ( base == RectangleEdge.BOTTOM ) {
            x0 += xOffset;
            x += xOffset;
            y0 += yOffset;
            if ( !pegShadow ) {
                y += yOffset;
            }
        } else if ( base == RectangleEdge.LEFT ) {
            if ( !pegShadow ) {
                x0 += xOffset;
            }
            x += xOffset;
            y0 += yOffset;
            y += yOffset;
        } else if ( base == RectangleEdge.RIGHT ) {
            x0 += xOffset;
            if ( !pegShadow ) {
                x += xOffset;
            }
            y0 += yOffset;
            y += yOffset;
        }
        return new Rectangle2D.Double ( x0, y0, x - x0, y - y0 );
    }
    @Override
    public boolean equals ( final Object obj ) {
        return obj == this || obj instanceof StandardXYBarPainter;
    }
    @Override
    public int hashCode() {
        final int hash = 37;
        return hash;
    }
}
