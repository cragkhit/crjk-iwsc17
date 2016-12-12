package org.jfree.data.contour;
import org.jfree.data.Range;
public class NonGridContourDataset extends DefaultContourDataset {
    static final int DEFAULT_NUM_X = 50;
    static final int DEFAULT_NUM_Y = 50;
    static final int DEFAULT_POWER = 4;
    public NonGridContourDataset() {
    }
    public NonGridContourDataset ( final String seriesName, final Object[] xData, final Object[] yData, final Object[] zData ) {
        super ( seriesName, xData, yData, zData );
        this.buildGrid ( 50, 50, 4 );
    }
    public NonGridContourDataset ( final String seriesName, final Object[] xData, final Object[] yData, final Object[] zData, final int numX, final int numY, final int power ) {
        super ( seriesName, xData, yData, zData );
        this.buildGrid ( numX, numY, power );
    }
    protected void buildGrid ( final int numX, final int numY, final int power ) {
        final int numValues = numX * numY;
        final double[] xGrid = new double[numValues];
        final double[] yGrid = new double[numValues];
        final double[] zGrid = new double[numValues];
        double xMin = 1.0E20;
        for ( int k = 0; k < this.xValues.length; ++k ) {
            xMin = Math.min ( xMin, this.xValues[k].doubleValue() );
        }
        double xMax = -1.0E20;
        for ( int i = 0; i < this.xValues.length; ++i ) {
            xMax = Math.max ( xMax, this.xValues[i].doubleValue() );
        }
        double yMin = 1.0E20;
        for ( int j = 0; j < this.yValues.length; ++j ) {
            yMin = Math.min ( yMin, this.yValues[j].doubleValue() );
        }
        double yMax = -1.0E20;
        for ( int l = 0; l < this.yValues.length; ++l ) {
            yMax = Math.max ( yMax, this.yValues[l].doubleValue() );
        }
        final Range xRange = new Range ( xMin, xMax );
        final Range yRange = new Range ( yMin, yMax );
        xRange.getLength();
        yRange.getLength();
        final double dxGrid = xRange.getLength() / ( numX - 1 );
        final double dyGrid = yRange.getLength() / ( numY - 1 );
        double x = 0.0;
        for ( int m = 0; m < numX; ++m ) {
            if ( m == 0 ) {
                x = xMin;
            } else {
                x += dxGrid;
            }
            double y = 0.0;
            for ( int j2 = 0; j2 < numY; ++j2 ) {
                final int k2 = numY * m + j2;
                xGrid[k2] = x;
                if ( j2 == 0 ) {
                    y = yMin;
                } else {
                    y += dyGrid;
                }
                yGrid[k2] = y;
            }
        }
        for ( int kGrid = 0; kGrid < xGrid.length; ++kGrid ) {
            double dTotal = 0.0;
            zGrid[kGrid] = 0.0;
            for ( int k3 = 0; k3 < this.xValues.length; ++k3 ) {
                final double xPt = this.xValues[k3].doubleValue();
                final double yPt = this.yValues[k3].doubleValue();
                double d = this.distance ( xPt, yPt, xGrid[kGrid], yGrid[kGrid] );
                if ( power != 1 ) {
                    d = Math.pow ( d, power );
                }
                d = Math.sqrt ( d );
                if ( d > 0.0 ) {
                    d = 1.0 / d;
                } else {
                    d = 1.0E20;
                }
                if ( this.zValues[k3] != null ) {
                    final double[] array = zGrid;
                    final int n = kGrid;
                    array[n] += this.zValues[k3].doubleValue() * d;
                }
                dTotal += d;
            }
            zGrid[kGrid] /= dTotal;
        }
        this.initialize ( DefaultContourDataset.formObjectArray ( xGrid ), DefaultContourDataset.formObjectArray ( yGrid ), DefaultContourDataset.formObjectArray ( zGrid ) );
    }
    protected double distance ( final double xDataPt, final double yDataPt, final double xGrdPt, final double yGrdPt ) {
        final double dx = xDataPt - xGrdPt;
        final double dy = yDataPt - yGrdPt;
        return Math.sqrt ( dx * dx + dy * dy );
    }
}
