package org.jfree.data;
import org.jfree.chart.util.ParamChecks;
import java.util.Arrays;
public abstract class DataUtilities {
    public static boolean equal ( final double[][] a, final double[][] b ) {
        if ( a == null ) {
            return b == null;
        }
        if ( b == null ) {
            return false;
        }
        if ( a.length != b.length ) {
            return false;
        }
        for ( int i = 0; i < a.length; ++i ) {
            if ( !Arrays.equals ( a[i], b[i] ) ) {
                return false;
            }
        }
        return true;
    }
    public static double[][] clone ( final double[][] source ) {
        ParamChecks.nullNotPermitted ( source, "source" );
        final double[][] clone = new double[source.length][];
        for ( int i = 0; i < source.length; ++i ) {
            if ( source[i] != null ) {
                final double[] row = new double[source[i].length];
                System.arraycopy ( source[i], 0, row, 0, source[i].length );
                clone[i] = row;
            }
        }
        return clone;
    }
    public static double calculateColumnTotal ( final Values2D data, final int column ) {
        ParamChecks.nullNotPermitted ( data, "data" );
        double total = 0.0;
        for ( int rowCount = data.getRowCount(), r = 0; r < rowCount; ++r ) {
            final Number n = data.getValue ( r, column );
            if ( n != null ) {
                total += n.doubleValue();
            }
        }
        return total;
    }
    public static double calculateColumnTotal ( final Values2D data, final int column, final int[] validRows ) {
        ParamChecks.nullNotPermitted ( data, "data" );
        double total = 0.0;
        final int rowCount = data.getRowCount();
        for ( int v = 0; v < validRows.length; ++v ) {
            final int row = validRows[v];
            if ( row < rowCount ) {
                final Number n = data.getValue ( row, column );
                if ( n != null ) {
                    total += n.doubleValue();
                }
            }
        }
        return total;
    }
    public static double calculateRowTotal ( final Values2D data, final int row ) {
        ParamChecks.nullNotPermitted ( data, "data" );
        double total = 0.0;
        for ( int columnCount = data.getColumnCount(), c = 0; c < columnCount; ++c ) {
            final Number n = data.getValue ( row, c );
            if ( n != null ) {
                total += n.doubleValue();
            }
        }
        return total;
    }
    public static double calculateRowTotal ( final Values2D data, final int row, final int[] validCols ) {
        ParamChecks.nullNotPermitted ( data, "data" );
        double total = 0.0;
        final int colCount = data.getColumnCount();
        for ( int v = 0; v < validCols.length; ++v ) {
            final int col = validCols[v];
            if ( col < colCount ) {
                final Number n = data.getValue ( row, col );
                if ( n != null ) {
                    total += n.doubleValue();
                }
            }
        }
        return total;
    }
    public static Number[] createNumberArray ( final double[] data ) {
        ParamChecks.nullNotPermitted ( data, "data" );
        final Number[] result = new Number[data.length];
        for ( int i = 0; i < data.length; ++i ) {
            result[i] = new Double ( data[i] );
        }
        return result;
    }
    public static Number[][] createNumberArray2D ( final double[][] data ) {
        ParamChecks.nullNotPermitted ( data, "data" );
        final int l1 = data.length;
        final Number[][] result = new Number[l1][];
        for ( int i = 0; i < l1; ++i ) {
            result[i] = createNumberArray ( data[i] );
        }
        return result;
    }
    public static KeyedValues getCumulativePercentages ( final KeyedValues data ) {
        ParamChecks.nullNotPermitted ( data, "data" );
        final DefaultKeyedValues result = new DefaultKeyedValues();
        double total = 0.0;
        for ( int i = 0; i < data.getItemCount(); ++i ) {
            final Number v = data.getValue ( i );
            if ( v != null ) {
                total += v.doubleValue();
            }
        }
        double runningTotal = 0.0;
        for ( int j = 0; j < data.getItemCount(); ++j ) {
            final Number v2 = data.getValue ( j );
            if ( v2 != null ) {
                runningTotal += v2.doubleValue();
            }
            result.addValue ( data.getKey ( j ), new Double ( runningTotal / total ) );
        }
        return result;
    }
}
