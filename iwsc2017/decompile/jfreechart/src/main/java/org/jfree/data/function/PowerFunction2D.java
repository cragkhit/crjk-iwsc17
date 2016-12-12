package org.jfree.data.function;
import org.jfree.chart.HashUtilities;
import java.io.Serializable;
public class PowerFunction2D implements Function2D, Serializable {
    private double a;
    private double b;
    public PowerFunction2D ( final double a, final double b ) {
        this.a = a;
        this.b = b;
    }
    public double getA() {
        return this.a;
    }
    public double getB() {
        return this.b;
    }
    @Override
    public double getValue ( final double x ) {
        return this.a * Math.pow ( x, this.b );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( ! ( obj instanceof PowerFunction2D ) ) {
            return false;
        }
        final PowerFunction2D that = ( PowerFunction2D ) obj;
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
