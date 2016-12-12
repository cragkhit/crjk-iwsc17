package org.jfree.data.xy;
import java.io.Serializable;
public class Vector implements Serializable {
    private double x;
    private double y;
    public Vector ( final double x, final double y ) {
        this.x = x;
        this.y = y;
    }
    public double getX() {
        return this.x;
    }
    public double getY() {
        return this.y;
    }
    public double getLength() {
        return Math.sqrt ( this.x * this.x + this.y * this.y );
    }
    public double getAngle() {
        return Math.atan2 ( this.y, this.x );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof Vector ) ) {
            return false;
        }
        final Vector that = ( Vector ) obj;
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
}
