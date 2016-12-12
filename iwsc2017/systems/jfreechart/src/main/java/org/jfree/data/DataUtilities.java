

package org.jfree.data;

import java.util.Arrays;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.general.DatasetUtilities;


public abstract class DataUtilities {


    public static boolean equal ( double[][] a, double[][] b ) {
        if ( a == null ) {
            return ( b == null );
        }
        if ( b == null ) {
            return false;
        }
        if ( a.length != b.length ) {
            return false;
        }
        for ( int i = 0; i < a.length; i++ ) {
            if ( !Arrays.equals ( a[i], b[i] ) ) {
                return false;
            }
        }
        return true;
    }


    public static double[][] clone ( double[][] source ) {
        ParamChecks.nullNotPermitted ( source, "source" );
        double[][] clone = new double[source.length][];
        for ( int i = 0; i < source.length; i++ ) {
            if ( source[i] != null ) {
                double[] row = new double[source[i].length];
                System.arraycopy ( source[i], 0, row, 0, source[i].length );
                clone[i] = row;
            }
        }
        return clone;
    }


    public static double calculateColumnTotal ( Values2D data, int column ) {
        ParamChecks.nullNotPermitted ( data, "data" );
        double total = 0.0;
        int rowCount = data.getRowCount();
        for ( int r = 0; r < rowCount; r++ ) {
            Number n = data.getValue ( r, column );
            if ( n != null ) {
                total += n.doubleValue();
            }
        }
        return total;
    }


    public static double calculateColumnTotal ( Values2D data, int column,
            int[] validRows ) {
        ParamChecks.nullNotPermitted ( data, "data" );
        double total = 0.0;
        int rowCount = data.getRowCount();
        for ( int v = 0; v < validRows.length; v++ ) {
            int row = validRows[v];
            if ( row < rowCount ) {
                Number n = data.getValue ( row, column );
                if ( n != null ) {
                    total += n.doubleValue();
                }
            }
        }
        return total;
    }


    public static double calculateRowTotal ( Values2D data, int row ) {
        ParamChecks.nullNotPermitted ( data, "data" );
        double total = 0.0;
        int columnCount = data.getColumnCount();
        for ( int c = 0; c < columnCount; c++ ) {
            Number n = data.getValue ( row, c );
            if ( n != null ) {
                total += n.doubleValue();
            }
        }
        return total;
    }


    public static double calculateRowTotal ( Values2D data, int row,
            int[] validCols ) {
        ParamChecks.nullNotPermitted ( data, "data" );
        double total = 0.0;
        int colCount = data.getColumnCount();
        for ( int v = 0; v < validCols.length; v++ ) {
            int col = validCols[v];
            if ( col < colCount ) {
                Number n = data.getValue ( row, col );
                if ( n != null ) {
                    total += n.doubleValue();
                }
            }
        }
        return total;
    }


    public static Number[] createNumberArray ( double[] data ) {
        ParamChecks.nullNotPermitted ( data, "data" );
        Number[] result = new Number[data.length];
        for ( int i = 0; i < data.length; i++ ) {
            result[i] = new Double ( data[i] );
        }
        return result;
    }


    public static Number[][] createNumberArray2D ( double[][] data ) {
        ParamChecks.nullNotPermitted ( data, "data" );
        int l1 = data.length;
        Number[][] result = new Number[l1][];
        for ( int i = 0; i < l1; i++ ) {
            result[i] = createNumberArray ( data[i] );
        }
        return result;
    }


    public static KeyedValues getCumulativePercentages ( KeyedValues data ) {
        ParamChecks.nullNotPermitted ( data, "data" );
        DefaultKeyedValues result = new DefaultKeyedValues();
        double total = 0.0;
        for ( int i = 0; i < data.getItemCount(); i++ ) {
            Number v = data.getValue ( i );
            if ( v != null ) {
                total = total + v.doubleValue();
            }
        }
        double runningTotal = 0.0;
        for ( int i = 0; i < data.getItemCount(); i++ ) {
            Number v = data.getValue ( i );
            if ( v != null ) {
                runningTotal = runningTotal + v.doubleValue();
            }
            result.addValue ( data.getKey ( i ), new Double ( runningTotal / total ) );
        }
        return result;
    }

}
