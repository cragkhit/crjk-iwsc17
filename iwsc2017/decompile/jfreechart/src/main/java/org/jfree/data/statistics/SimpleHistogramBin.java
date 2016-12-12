package org.jfree.data.statistics;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class SimpleHistogramBin implements Comparable, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 3480862537505941742L;
    private double lowerBound;
    private double upperBound;
    private boolean includeLowerBound;
    private boolean includeUpperBound;
    private int itemCount;
    public SimpleHistogramBin ( final double lowerBound, final double upperBound ) {
        this ( lowerBound, upperBound, true, true );
    }
    public SimpleHistogramBin ( final double lowerBound, final double upperBound, final boolean includeLowerBound, final boolean includeUpperBound ) {
        if ( lowerBound >= upperBound ) {
            throw new IllegalArgumentException ( "Invalid bounds" );
        }
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.includeLowerBound = includeLowerBound;
        this.includeUpperBound = includeUpperBound;
        this.itemCount = 0;
    }
    public double getLowerBound() {
        return this.lowerBound;
    }
    public double getUpperBound() {
        return this.upperBound;
    }
    public int getItemCount() {
        return this.itemCount;
    }
    public void setItemCount ( final int count ) {
        this.itemCount = count;
    }
    public boolean accepts ( final double value ) {
        if ( Double.isNaN ( value ) ) {
            return false;
        }
        if ( value < this.lowerBound ) {
            return false;
        }
        if ( value > this.upperBound ) {
            return false;
        }
        if ( value == this.lowerBound ) {
            return this.includeLowerBound;
        }
        return value != this.upperBound || this.includeUpperBound;
    }
    public boolean overlapsWith ( final SimpleHistogramBin bin ) {
        if ( this.upperBound < bin.lowerBound ) {
            return false;
        }
        if ( this.lowerBound > bin.upperBound ) {
            return false;
        }
        if ( this.upperBound == bin.lowerBound ) {
            return this.includeUpperBound && bin.includeLowerBound;
        }
        return this.lowerBound != bin.upperBound || ( this.includeLowerBound && bin.includeUpperBound );
    }
    @Override
    public int compareTo ( final Object obj ) {
        if ( ! ( obj instanceof SimpleHistogramBin ) ) {
            return 0;
        }
        final SimpleHistogramBin bin = ( SimpleHistogramBin ) obj;
        if ( this.lowerBound < bin.lowerBound ) {
            return -1;
        }
        if ( this.lowerBound > bin.lowerBound ) {
            return 1;
        }
        if ( this.upperBound < bin.upperBound ) {
            return -1;
        }
        if ( this.upperBound > bin.upperBound ) {
            return 1;
        }
        return 0;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( ! ( obj instanceof SimpleHistogramBin ) ) {
            return false;
        }
        final SimpleHistogramBin that = ( SimpleHistogramBin ) obj;
        return this.lowerBound == that.lowerBound && this.upperBound == that.upperBound && this.includeLowerBound == that.includeLowerBound && this.includeUpperBound == that.includeUpperBound && this.itemCount == that.itemCount;
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
