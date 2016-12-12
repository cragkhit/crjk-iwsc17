package org.jfree.chart.axis;
import java.io.Serializable;
public abstract class TickUnit implements Comparable, Serializable {
    private static final long serialVersionUID = 510179855057013974L;
    private double size;
    private int minorTickCount;
    public TickUnit ( final double size ) {
        this.size = size;
    }
    public TickUnit ( final double size, final int minorTickCount ) {
        this.size = size;
        this.minorTickCount = minorTickCount;
    }
    public double getSize() {
        return this.size;
    }
    public int getMinorTickCount() {
        return this.minorTickCount;
    }
    public String valueToString ( final double value ) {
        return String.valueOf ( value );
    }
    @Override
    public int compareTo ( final Object object ) {
        if ( ! ( object instanceof TickUnit ) ) {
            return -1;
        }
        final TickUnit other = ( TickUnit ) object;
        if ( this.size > other.getSize() ) {
            return 1;
        }
        if ( this.size < other.getSize() ) {
            return -1;
        }
        return 0;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof TickUnit ) ) {
            return false;
        }
        final TickUnit that = ( TickUnit ) obj;
        return this.size == that.size && this.minorTickCount == that.minorTickCount;
    }
    @Override
    public int hashCode() {
        final long temp = ( this.size != 0.0 ) ? Double.doubleToLongBits ( this.size ) : 0L;
        return ( int ) ( temp ^ temp >>> 32 );
    }
}
