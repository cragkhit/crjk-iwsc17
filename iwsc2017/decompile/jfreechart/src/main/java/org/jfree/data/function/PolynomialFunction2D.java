package org.jfree.data.function;
import org.jfree.chart.HashUtilities;
import java.util.Arrays;
import org.jfree.chart.util.ParamChecks;
import java.io.Serializable;
public class PolynomialFunction2D implements Function2D, Serializable {
    private double[] coefficients;
    public PolynomialFunction2D ( final double[] coefficients ) {
        ParamChecks.nullNotPermitted ( coefficients, "coefficients" );
        this.coefficients = coefficients.clone();
    }
    public double[] getCoefficients() {
        return this.coefficients.clone();
    }
    public int getOrder() {
        return this.coefficients.length - 1;
    }
    @Override
    public double getValue ( final double x ) {
        double y = 0.0;
        for ( int i = 0; i < this.coefficients.length; ++i ) {
            y += this.coefficients[i] * Math.pow ( x, i );
        }
        return y;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( ! ( obj instanceof PolynomialFunction2D ) ) {
            return false;
        }
        final PolynomialFunction2D that = ( PolynomialFunction2D ) obj;
        return Arrays.equals ( this.coefficients, that.coefficients );
    }
    @Override
    public int hashCode() {
        return HashUtilities.hashCodeForDoubleArray ( this.coefficients );
    }
}
