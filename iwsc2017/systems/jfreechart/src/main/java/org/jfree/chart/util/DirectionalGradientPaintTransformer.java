package org.jfree.chart.util;
import java.awt.GradientPaint;
import java.awt.geom.Rectangle2D;
import java.awt.Shape;
import org.jfree.ui.GradientPaintTransformer;
public class DirectionalGradientPaintTransformer
    implements GradientPaintTransformer {
    public DirectionalGradientPaintTransformer() {
        super();
    }
    @Override
    public GradientPaint transform ( GradientPaint paint, Shape target ) {
        final double px1 = paint.getPoint1().getX();
        final double py1 = paint.getPoint1().getY();
        final double px2 = paint.getPoint2().getX();
        final double py2 = paint.getPoint2().getY();
        final Rectangle2D bounds = target.getBounds();
        final float bx = ( float ) bounds.getX();
        final float by = ( float ) bounds.getY();
        final float bw = ( float ) bounds.getWidth();
        final float bh = ( float ) bounds.getHeight();
        float rx1, ry1, rx2, ry2;
        if ( px1 == 0 && py1 == 0 ) {
            rx1 = bx;
            ry1 = by;
            if ( px2 != 0.0f && py2 != 0.0f ) {
                float offset = ( paint.isCyclic() ) ? ( bw + bh ) / 4.0f
                               : ( bw + bh ) / 2.0f ;
                rx2 = bx + offset;
                ry2 = by + offset;
            } else {
                rx2 = ( px2 == 0 ) ? rx1 : ( paint.isCyclic() ? ( rx1 + bw / 2.0f )
                                             : ( rx1 + bw ) );
                ry2 = ( py2 == 0 ) ? ry1 : ( paint.isCyclic() ? ( ry1 + bh / 2.0f )
                                             : ( ry1 + bh ) );
            }
        } else {
            rx1 = bx;
            ry1 = by + bh;
            float offset = ( paint.isCyclic() ) ? ( bw + bh ) / 4.0f
                           : ( bw + bh ) / 2.0f;
            rx2 = bx + offset;
            ry2 = by + bh - offset;
        }
        return new GradientPaint ( rx1, ry1, paint.getColor1(), rx2, ry2,
                                   paint.getColor2(), paint.isCyclic() );
    }
}
