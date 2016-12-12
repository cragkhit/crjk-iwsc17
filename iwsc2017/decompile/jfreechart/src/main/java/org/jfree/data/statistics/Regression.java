package org.jfree.data.statistics;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.xy.XYDataset;
public abstract class Regression {
    public static double[] getOLSRegression ( final double[][] data ) {
        final int n = data.length;
        if ( n < 2 ) {
            throw new IllegalArgumentException ( "Not enough data." );
        }
        double sumX = 0.0;
        double sumY = 0.0;
        double sumXX = 0.0;
        double sumXY = 0.0;
        for ( int i = 0; i < n; ++i ) {
            final double x = data[i][0];
            final double y = data[i][1];
            sumX += x;
            sumY += y;
            final double xx = x * x;
            sumXX += xx;
            final double xy = x * y;
            sumXY += xy;
        }
        final double sxx = sumXX - sumX * sumX / n;
        final double sxy = sumXY - sumX * sumY / n;
        final double xbar = sumX / n;
        final double ybar = sumY / n;
        final double[] result = { 0.0, sxy / sxx };
        result[0] = ybar - result[1] * xbar;
        return result;
    }
    public static double[] getOLSRegression ( final XYDataset data, final int series ) {
        final int n = data.getItemCount ( series );
        if ( n < 2 ) {
            throw new IllegalArgumentException ( "Not enough data." );
        }
        double sumX = 0.0;
        double sumY = 0.0;
        double sumXX = 0.0;
        double sumXY = 0.0;
        for ( int i = 0; i < n; ++i ) {
            final double x = data.getXValue ( series, i );
            final double y = data.getYValue ( series, i );
            sumX += x;
            sumY += y;
            final double xx = x * x;
            sumXX += xx;
            final double xy = x * y;
            sumXY += xy;
        }
        final double sxx = sumXX - sumX * sumX / n;
        final double sxy = sumXY - sumX * sumY / n;
        final double xbar = sumX / n;
        final double ybar = sumY / n;
        final double[] result = { 0.0, sxy / sxx };
        result[0] = ybar - result[1] * xbar;
        return result;
    }
    public static double[] getPowerRegression ( final double[][] data ) {
        final int n = data.length;
        if ( n < 2 ) {
            throw new IllegalArgumentException ( "Not enough data." );
        }
        double sumX = 0.0;
        double sumY = 0.0;
        double sumXX = 0.0;
        double sumXY = 0.0;
        for ( int i = 0; i < n; ++i ) {
            final double x = Math.log ( data[i][0] );
            final double y = Math.log ( data[i][1] );
            sumX += x;
            sumY += y;
            final double xx = x * x;
            sumXX += xx;
            final double xy = x * y;
            sumXY += xy;
        }
        final double sxx = sumXX - sumX * sumX / n;
        final double sxy = sumXY - sumX * sumY / n;
        final double xbar = sumX / n;
        final double ybar = sumY / n;
        final double[] result = { 0.0, sxy / sxx };
        result[0] = Math.pow ( Math.exp ( 1.0 ), ybar - result[1] * xbar );
        return result;
    }
    public static double[] getPowerRegression ( final XYDataset data, final int series ) {
        final int n = data.getItemCount ( series );
        if ( n < 2 ) {
            throw new IllegalArgumentException ( "Not enough data." );
        }
        double sumX = 0.0;
        double sumY = 0.0;
        double sumXX = 0.0;
        double sumXY = 0.0;
        for ( int i = 0; i < n; ++i ) {
            final double x = Math.log ( data.getXValue ( series, i ) );
            final double y = Math.log ( data.getYValue ( series, i ) );
            sumX += x;
            sumY += y;
            final double xx = x * x;
            sumXX += xx;
            final double xy = x * y;
            sumXY += xy;
        }
        final double sxx = sumXX - sumX * sumX / n;
        final double sxy = sumXY - sumX * sumY / n;
        final double xbar = sumX / n;
        final double ybar = sumY / n;
        final double[] result = { 0.0, sxy / sxx };
        result[0] = Math.pow ( Math.exp ( 1.0 ), ybar - result[1] * xbar );
        return result;
    }
    public static double[] getPolynomialRegression ( final XYDataset dataset, final int series, final int order ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        final int itemCount = dataset.getItemCount ( series );
        if ( itemCount < order + 1 ) {
            throw new IllegalArgumentException ( "Not enough data." );
        }
        int validItems = 0;
        final double[][] data = new double[2][itemCount];
        for ( int item = 0; item < itemCount; ++item ) {
            final double x = dataset.getXValue ( series, item );
            final double y = dataset.getYValue ( series, item );
            if ( !Double.isNaN ( x ) && !Double.isNaN ( y ) ) {
                data[0][validItems] = x;
                data[1][validItems] = y;
                ++validItems;
            }
        }
        if ( validItems < order + 1 ) {
            throw new IllegalArgumentException ( "Not enough data." );
        }
        final int equations = order + 1;
        final int coefficients = order + 2;
        final double[] result = new double[equations + 1];
        final double[][] matrix = new double[equations][coefficients];
        double sumX = 0.0;
        double sumY = 0.0;
        for ( int item2 = 0; item2 < validItems; ++item2 ) {
            sumX += data[0][item2];
            sumY += data[1][item2];
            for ( int eq = 0; eq < equations; ++eq ) {
                for ( int coe = 0; coe < coefficients - 1; ++coe ) {
                    final double[] array = matrix[eq];
                    final int n = coe;
                    array[n] += Math.pow ( data[0][item2], eq + coe );
                }
                final double[] array2 = matrix[eq];
                final int n2 = coefficients - 1;
                array2[n2] += data[1][item2] * Math.pow ( data[0][item2], eq );
            }
        }
        final double[][] subMatrix = calculateSubMatrix ( matrix );
        for ( int eq = 1; eq < equations; ++eq ) {
            matrix[eq][0] = 0.0;
            for ( int coe = 1; coe < coefficients; ++coe ) {
                matrix[eq][coe] = subMatrix[eq - 1][coe - 1];
            }
        }
        for ( int eq = equations - 1; eq > -1; --eq ) {
            double value = matrix[eq][coefficients - 1];
            for ( int coe2 = eq; coe2 < coefficients - 1; ++coe2 ) {
                value -= matrix[eq][coe2] * result[coe2];
            }
            result[eq] = value / matrix[eq][eq];
        }
        final double meanY = sumY / validItems;
        double yObsSquare = 0.0;
        double yRegSquare = 0.0;
        for ( int item3 = 0; item3 < validItems; ++item3 ) {
            double yCalc = 0.0;
            for ( int eq2 = 0; eq2 < equations; ++eq2 ) {
                yCalc += result[eq2] * Math.pow ( data[0][item3], eq2 );
            }
            yRegSquare += Math.pow ( yCalc - meanY, 2.0 );
            yObsSquare += Math.pow ( data[1][item3] - meanY, 2.0 );
        }
        final double rSquare = yRegSquare / yObsSquare;
        result[equations] = rSquare;
        return result;
    }
    private static double[][] calculateSubMatrix ( final double[][] matrix ) {
        final int equations = matrix.length;
        final int coefficients = matrix[0].length;
        final double[][] result = new double[equations - 1][coefficients - 1];
        for ( int eq = 1; eq < equations; ++eq ) {
            final double factor = matrix[0][0] / matrix[eq][0];
            for ( int coe = 1; coe < coefficients; ++coe ) {
                result[eq - 1][coe - 1] = matrix[0][coe] - matrix[eq][coe] * factor;
            }
        }
        if ( equations == 1 ) {
            return result;
        }
        if ( result[0][0] == 0.0 ) {
            boolean found = false;
            for ( int i = 0; i < result.length; ++i ) {
                if ( result[i][0] != 0.0 ) {
                    found = true;
                    final double[] temp = result[0];
                    System.arraycopy ( result[i], 0, result[0], 0, result[i].length );
                    System.arraycopy ( temp, 0, result[i], 0, temp.length );
                    break;
                }
            }
            if ( !found ) {
                return new double[equations - 1][coefficients - 1];
            }
        }
        final double[][] subMatrix = calculateSubMatrix ( result );
        for ( int eq2 = 1; eq2 < equations - 1; ++eq2 ) {
            result[eq2][0] = 0.0;
            for ( int coe2 = 1; coe2 < coefficients - 1; ++coe2 ) {
                result[eq2][coe2] = subMatrix[eq2 - 1][coe2 - 1];
            }
        }
        return result;
    }
}
