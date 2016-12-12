package org.jfree.data.xy;
import java.io.Serializable;
public class XYCoordinate implements Comparable, Serializable {
    private double x;
    private double y;
    public XYCoordinate() {
        this ( 0.0, 0.0 );
    }
    public XYCoordinate ( final double x, final double y ) {
        this.x = x;
        this.y = y;
    }
    public double getX() {
        return this.x;
    }
    public double getY() {
        return this.y;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof XYCoordinate ) ) {
            return false;
        }
        final XYCoordinate that = ( XYCoordinate ) obj;
        return this.x == that.x && this.y == that.y;
    }
    @Override
    public int hashCode() {
        int result = 193;
        long temp = Double.doubleToLongBits ( this.x );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        temp = Double.doubleToLongBits ( this.y );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        return result;
    }
    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ")";
    }
    @Override
    public int compareTo ( final Object obj ) {
        if ( ! ( obj instanceof XYCoordinate ) ) {
            throw new IllegalArgumentException ( "Incomparable object." );
        }
        final XYCoordinate that = ( XYCoordinate ) obj;
        if ( this.x > that.x ) {
            return 1;
        }
        if ( this.x < that.x ) {
            return -1;
        }
        if ( this.y > that.y ) {
            return 1;
        }
        if ( this.y < that.y ) {
            return -1;
        }
        return 0;
    }
}
