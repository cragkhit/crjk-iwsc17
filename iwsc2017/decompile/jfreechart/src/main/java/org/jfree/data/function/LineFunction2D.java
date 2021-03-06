package org.jfree.data.function;
import org.jfree.chart.HashUtilities;
import java.io.Serializable;
public class LineFunction2D implements Function2D, Serializable {
    private double a;
    private double b;
    public LineFunction2D ( final double a, final double b ) {
        this.a = a;
        this.b = b;
    }
    public double getIntercept() {
        return this.a;
    }
    public double getSlope() {
        return this.b;
    }
    @Override
    public double getValue ( final double x ) {
        return this.a + this.b * x;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( ! ( obj instanceof LineFunction2D ) ) {
            return false;
        }
        final LineFunction2D that = ( LineFunction2D ) obj;
        return this.a == that.a && this.b == that.b;
    }
    @Override
    public int hashCode() {
        int result = 29;
        result = HashUtilities.hashCode ( result, this.a );
        result = HashUtilities.hashCode ( result, this.b );
        return result;
    }
}
