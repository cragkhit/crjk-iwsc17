package org.jfree.chart.renderer.category;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Component;
import java.awt.geom.GeneralPath;
import javax.swing.Icon;
class MinMaxCategoryRenderer$2 implements Icon {
    final   GeneralPath val$path;
    final   boolean val$fill;
    final   boolean val$outline;
    final   int val$width;
    final   int val$height;
    @Override
    public void paintIcon ( final Component c, final Graphics g, final int x, final int y ) {
        final Graphics2D g2 = ( Graphics2D ) g;
        this.val$path.transform ( AffineTransform.getTranslateInstance ( x, y ) );
        if ( this.val$fill ) {
            g2.fill ( this.val$path );
        }
        if ( this.val$outline ) {
            g2.draw ( this.val$path );
        }
        this.val$path.transform ( AffineTransform.getTranslateInstance ( -x, -y ) );
    }
    @Override
    public int getIconWidth() {
        return this.val$width;
    }
    @Override
    public int getIconHeight() {
        return this.val$height;
    }
}
