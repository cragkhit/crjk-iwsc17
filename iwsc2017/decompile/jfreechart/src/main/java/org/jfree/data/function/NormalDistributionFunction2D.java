package org.jfree.data.function;
import org.jfree.chart.HashUtilities;
import java.io.Serializable;
public class NormalDistributionFunction2D implements Function2D, Serializable {
    private double mean;
    private double std;
    private double factor;
    private double denominator;
    public NormalDistributionFunction2D ( final double mean, final double std ) {
        if ( std <= 0.0 ) {
            throw new IllegalArgumentException ( "Requires 'std' > 0." );
        }
        this.mean = mean;
        this.std = std;
        this.factor = 1.0 / ( std * Math.sqrt ( 6.283185307179586 ) );
        this.denominator = 2.0 * std * std;
    }
    public double getMean() {
        return this.mean;
    }
    public double getStandardDeviation() {
        return this.std;
    }
    @Override
    public double getValue ( final double x ) {
        final double z = x - this.mean;
        return this.factor * Math.exp ( -z * z / this.denominator );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( ! ( obj instanceof NormalDistributionFunction2D ) ) {
            return false;
        }
        final NormalDistributionFunction2D that = ( NormalDistributionFunction2D ) obj;
        return this.mean == that.mean && this.std == that.std;
    }
    @Override
    public int hashCode() {
        int result = 29;
        result = HashUtilities.hashCode ( result, this.mean );
        result = HashUtilities.hashCode ( result, this.std );
        return result;
    }
}
