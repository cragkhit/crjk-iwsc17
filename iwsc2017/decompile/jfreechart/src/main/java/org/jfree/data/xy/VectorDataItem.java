package org.jfree.data.xy;
import org.jfree.data.ComparableObjectItem;
public class VectorDataItem extends ComparableObjectItem {
    public VectorDataItem ( final double x, final double y, final double deltaX, final double deltaY ) {
        super ( new XYCoordinate ( x, y ), new Vector ( deltaX, deltaY ) );
    }
    public double getXValue() {
        final XYCoordinate xy = ( XYCoordinate ) this.getComparable();
        return xy.getX();
    }
    public double getYValue() {
        final XYCoordinate xy = ( XYCoordinate ) this.getComparable();
        return xy.getY();
    }
    public Vector getVector() {
        return ( Vector ) this.getObject();
    }
    public double getVectorX() {
        final Vector vi = ( Vector ) this.getObject();
        if ( vi != null ) {
            return vi.getX();
        }
        return Double.NaN;
    }
    public double getVectorY() {
        final Vector vi = ( Vector ) this.getObject();
        if ( vi != null ) {
            return vi.getY();
        }
        return Double.NaN;
    }
}
