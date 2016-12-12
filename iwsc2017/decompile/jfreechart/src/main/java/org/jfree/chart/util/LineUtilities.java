package org.jfree.chart.util;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
public class LineUtilities {
    public static boolean clipLine ( final Line2D line, final Rectangle2D rect ) {
        double x1 = line.getX1();
        double y1 = line.getY1();
        double x2 = line.getX2();
        double y2 = line.getY2();
        final double minX = rect.getMinX();
        final double maxX = rect.getMaxX();
        final double minY = rect.getMinY();
        final double maxY = rect.getMaxY();
        int f1 = rect.outcode ( x1, y1 );
        int f2 = rect.outcode ( x2, y2 );
        while ( ( f1 | f2 ) != 0x0 ) {
            if ( ( f1 & f2 ) != 0x0 ) {
                return false;
            }
            final double dx = x2 - x1;
            final double dy = y2 - y1;
            if ( f1 != 0 ) {
                if ( ( f1 & 0x1 ) == 0x1 && dx != 0.0 ) {
                    y1 += ( minX - x1 ) * dy / dx;
                    x1 = minX;
                } else if ( ( f1 & 0x4 ) == 0x4 && dx != 0.0 ) {
                    y1 += ( maxX - x1 ) * dy / dx;
                    x1 = maxX;
                } else if ( ( f1 & 0x8 ) == 0x8 && dy != 0.0 ) {
                    x1 += ( maxY - y1 ) * dx / dy;
                    y1 = maxY;
                } else if ( ( f1 & 0x2 ) == 0x2 && dy != 0.0 ) {
                    x1 += ( minY - y1 ) * dx / dy;
                    y1 = minY;
                }
                f1 = rect.outcode ( x1, y1 );
            } else {
                if ( f2 == 0 ) {
                    continue;
                }
                if ( ( f2 & 0x1 ) == 0x1 && dx != 0.0 ) {
                    y2 += ( minX - x2 ) * dy / dx;
                    x2 = minX;
                } else if ( ( f2 & 0x4 ) == 0x4 && dx != 0.0 ) {
                    y2 += ( maxX - x2 ) * dy / dx;
                    x2 = maxX;
                } else if ( ( f2 & 0x8 ) == 0x8 && dy != 0.0 ) {
                    x2 += ( maxY - y2 ) * dx / dy;
                    y2 = maxY;
                } else if ( ( f2 & 0x2 ) == 0x2 && dy != 0.0 ) {
                    x2 += ( minY - y2 ) * dx / dy;
                    y2 = minY;
                }
                f2 = rect.outcode ( x2, y2 );
            }
        }
        line.setLine ( x1, y1, x2, y2 );
        return true;
    }
    public static Line2D extendLine ( final Line2D line, final double startPercent, final double endPercent ) {
        ParamChecks.nullNotPermitted ( line, "line" );
        double x1 = line.getX1();
        double x2 = line.getX2();
        final double deltaX = x2 - x1;
        double y1 = line.getY1();
        double y2 = line.getY2();
        final double deltaY = y2 - y1;
        x1 -= startPercent * deltaX;
        y1 -= startPercent * deltaY;
        x2 += endPercent * deltaX;
        y2 += endPercent * deltaY;
        return new Line2D.Double ( x1, y1, x2, y2 );
    }
}
