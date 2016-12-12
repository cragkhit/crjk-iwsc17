package org.jfree.chart.renderer.category;
import org.jfree.chart.HashUtilities;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.Shape;
import java.awt.Paint;
import java.awt.GradientPaint;
import java.awt.Color;
import org.jfree.ui.RectangleEdge;
import java.awt.geom.RectangularShape;
import java.awt.Graphics2D;
import java.io.Serializable;
public class GradientBarPainter implements BarPainter, Serializable {
    private double g1;
    private double g2;
    private double g3;
    public GradientBarPainter() {
        this ( 0.1, 0.2, 0.8 );
    }
    public GradientBarPainter ( final double g1, final double g2, final double g3 ) {
        this.g1 = g1;
        this.g2 = g2;
        this.g3 = g3;
    }
    @Override
    public void paintBar ( final Graphics2D g2, final BarRenderer renderer, final int row, final int column, final RectangularShape bar, final RectangleEdge base ) {
        final Paint itemPaint = renderer.getItemPaint ( row, column );
        Color c0;
        Color c;
        if ( itemPaint instanceof Color ) {
            c0 = ( Color ) itemPaint;
            c = c0.brighter();
        } else if ( itemPaint instanceof GradientPaint ) {
            final GradientPaint gp = ( GradientPaint ) itemPaint;
            c0 = gp.getColor1();
            c = gp.getColor2();
        } else {
            c0 = Color.BLUE;
            c = Color.BLUE.brighter();
        }
        if ( c0.getAlpha() == 0 ) {
            return;
        }
        if ( base == RectangleEdge.TOP || base == RectangleEdge.BOTTOM ) {
            final Rectangle2D[] regions = this.splitVerticalBar ( bar, this.g1, this.g2, this.g3 );
            GradientPaint gp2 = new GradientPaint ( ( float ) regions[0].getMinX(), 0.0f, c0, ( float ) regions[0].getMaxX(), 0.0f, Color.WHITE );
            g2.setPaint ( gp2 );
            g2.fill ( regions[0] );
            gp2 = new GradientPaint ( ( float ) regions[1].getMinX(), 0.0f, Color.WHITE, ( float ) regions[1].getMaxX(), 0.0f, c0 );
            g2.setPaint ( gp2 );
            g2.fill ( regions[1] );
            gp2 = new GradientPaint ( ( float ) regions[2].getMinX(), 0.0f, c0, ( float ) regions[2].getMaxX(), 0.0f, c );
            g2.setPaint ( gp2 );
            g2.fill ( regions[2] );
            gp2 = new GradientPaint ( ( float ) regions[3].getMinX(), 0.0f, c, ( float ) regions[3].getMaxX(), 0.0f, c0 );
            g2.setPaint ( gp2 );
            g2.fill ( regions[3] );
        } else if ( base == RectangleEdge.LEFT || base == RectangleEdge.RIGHT ) {
            final Rectangle2D[] regions = this.splitHorizontalBar ( bar, this.g1, this.g2, this.g3 );
            GradientPaint gp2 = new GradientPaint ( 0.0f, ( float ) regions[0].getMinY(), c0, 0.0f, ( float ) regions[0].getMaxY(), Color.WHITE );
            g2.setPaint ( gp2 );
            g2.fill ( regions[0] );
            gp2 = new GradientPaint ( 0.0f, ( float ) regions[1].getMinY(), Color.WHITE, 0.0f, ( float ) regions[1].getMaxY(), c0 );
            g2.setPaint ( gp2 );
            g2.fill ( regions[1] );
            gp2 = new GradientPaint ( 0.0f, ( float ) regions[2].getMinY(), c0, 0.0f, ( float ) regions[2].getMaxY(), c );
            g2.setPaint ( gp2 );
            g2.fill ( regions[2] );
            gp2 = new GradientPaint ( 0.0f, ( float ) regions[3].getMinY(), c, 0.0f, ( float ) regions[3].getMaxY(), c0 );
            g2.setPaint ( gp2 );
            g2.fill ( regions[3] );
        }
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
    public void paintBarShadow ( final Graphics2D g2, final BarRenderer renderer, final int row, final int column, final RectangularShape bar, final RectangleEdge base, final boolean pegShadow ) {
        final Paint itemPaint = renderer.getItemPaint ( row, column );
        if ( itemPaint instanceof Color ) {
            final Color c = ( Color ) itemPaint;
            if ( c.getAlpha() == 0 ) {
                return;
            }
        }
        final RectangularShape shadow = this.createShadow ( bar, renderer.getShadowXOffset(), renderer.getShadowYOffset(), base, pegShadow );
        g2.setPaint ( renderer.getShadowPaint() );
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
    private Rectangle2D[] splitVerticalBar ( final RectangularShape bar, final double a, final double b, final double c ) {
        final Rectangle2D[] result = new Rectangle2D[4];
        final double x0 = bar.getMinX();
        final double x = Math.rint ( x0 + bar.getWidth() * a );
        final double x2 = Math.rint ( x0 + bar.getWidth() * b );
        final double x3 = Math.rint ( x0 + bar.getWidth() * c );
        result[0] = new Rectangle2D.Double ( bar.getMinX(), bar.getMinY(), x - x0, bar.getHeight() );
        result[1] = new Rectangle2D.Double ( x, bar.getMinY(), x2 - x, bar.getHeight() );
        result[2] = new Rectangle2D.Double ( x2, bar.getMinY(), x3 - x2, bar.getHeight() );
        result[3] = new Rectangle2D.Double ( x3, bar.getMinY(), bar.getMaxX() - x3, bar.getHeight() );
        return result;
    }
    private Rectangle2D[] splitHorizontalBar ( final RectangularShape bar, final double a, final double b, final double c ) {
        final Rectangle2D[] result = new Rectangle2D[4];
        final double y0 = bar.getMinY();
        final double y = Math.rint ( y0 + bar.getHeight() * a );
        final double y2 = Math.rint ( y0 + bar.getHeight() * b );
        final double y3 = Math.rint ( y0 + bar.getHeight() * c );
        result[0] = new Rectangle2D.Double ( bar.getMinX(), bar.getMinY(), bar.getWidth(), y - y0 );
        result[1] = new Rectangle2D.Double ( bar.getMinX(), y, bar.getWidth(), y2 - y );
        result[2] = new Rectangle2D.Double ( bar.getMinX(), y2, bar.getWidth(), y3 - y2 );
        result[3] = new Rectangle2D.Double ( bar.getMinX(), y3, bar.getWidth(), bar.getMaxY() - y3 );
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof GradientBarPainter ) ) {
            return false;
        }
        final GradientBarPainter that = ( GradientBarPainter ) obj;
        return this.g1 == that.g1 && this.g2 == that.g2 && this.g3 == that.g3;
    }
    @Override
    public int hashCode() {
        int hash = 37;
        hash = HashUtilities.hashCode ( hash, this.g1 );
        hash = HashUtilities.hashCode ( hash, this.g2 );
        hash = HashUtilities.hashCode ( hash, this.g3 );
        return hash;
    }
}
