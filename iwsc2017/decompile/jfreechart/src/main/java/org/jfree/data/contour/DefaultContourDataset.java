package org.jfree.data.contour;
import java.util.Arrays;
import org.jfree.data.Range;
import java.util.Date;
import java.util.Vector;
import org.jfree.data.xy.AbstractXYZDataset;
public class DefaultContourDataset extends AbstractXYZDataset implements ContourDataset {
    protected Comparable seriesKey;
    protected Number[] xValues;
    protected Number[] yValues;
    protected Number[] zValues;
    protected int[] xIndex;
    boolean[] dateAxis;
    public DefaultContourDataset() {
        this.seriesKey = null;
        this.xValues = null;
        this.yValues = null;
        this.zValues = null;
        this.xIndex = null;
        this.dateAxis = new boolean[3];
    }
    public DefaultContourDataset ( final Comparable seriesKey, final Object[] xData, final Object[] yData, final Object[] zData ) {
        this.seriesKey = null;
        this.xValues = null;
        this.yValues = null;
        this.zValues = null;
        this.xIndex = null;
        this.dateAxis = new boolean[3];
        this.seriesKey = seriesKey;
        this.initialize ( xData, yData, zData );
    }
    public void initialize ( final Object[] xData, final Object[] yData, final Object[] zData ) {
        this.xValues = new Double[xData.length];
        this.yValues = new Double[yData.length];
        this.zValues = new Double[zData.length];
        final Vector tmpVector = new Vector();
        double x = 1.123452E31;
        for ( int k = 0; k < this.xValues.length; ++k ) {
            if ( xData[k] != null ) {
                Number xNumber;
                if ( xData[k] instanceof Number ) {
                    xNumber = ( Number ) xData[k];
                } else if ( xData[k] instanceof Date ) {
                    this.dateAxis[0] = true;
                    final Date xDate = ( Date ) xData[k];
                    xNumber = new Long ( xDate.getTime() );
                } else {
                    xNumber = new Integer ( 0 );
                }
                this.xValues[k] = new Double ( xNumber.doubleValue() );
                if ( x != this.xValues[k].doubleValue() ) {
                    tmpVector.add ( new Integer ( k ) );
                    x = this.xValues[k].doubleValue();
                }
            }
        }
        final Object[] inttmp = tmpVector.toArray();
        this.xIndex = new int[inttmp.length];
        for ( int i = 0; i < inttmp.length; ++i ) {
            this.xIndex[i] = ( int ) inttmp[i];
        }
        for ( int j = 0; j < this.yValues.length; ++j ) {
            this.yValues[j] = ( Double ) yData[j];
            if ( zData[j] != null ) {
                this.zValues[j] = ( Double ) zData[j];
            }
        }
    }
    public static Object[][] formObjectArray ( final double[][] data ) {
        final Object[][] object = new Double[data.length][data[0].length];
        for ( int i = 0; i < object.length; ++i ) {
            for ( int j = 0; j < object[i].length; ++j ) {
                object[i][j] = new Double ( data[i][j] );
            }
        }
        return object;
    }
    public static Object[] formObjectArray ( final double[] data ) {
        final Object[] object = new Double[data.length];
        for ( int i = 0; i < object.length; ++i ) {
            object[i] = new Double ( data[i] );
        }
        return object;
    }
    @Override
    public int getItemCount ( final int series ) {
        if ( series > 0 ) {
            throw new IllegalArgumentException ( "Only one series for contour" );
        }
        return this.zValues.length;
    }
    @Override
    public double getMaxZValue() {
        double zMax = -1.0E20;
        for ( int k = 0; k < this.zValues.length; ++k ) {
            if ( this.zValues[k] != null ) {
                zMax = Math.max ( zMax, this.zValues[k].doubleValue() );
            }
        }
        return zMax;
    }
    @Override
    public double getMinZValue() {
        double zMin = 1.0E20;
        for ( int k = 0; k < this.zValues.length; ++k ) {
            if ( this.zValues[k] != null ) {
                zMin = Math.min ( zMin, this.zValues[k].doubleValue() );
            }
        }
        return zMin;
    }
    @Override
    public Range getZValueRange ( final Range x, final Range y ) {
        final double minX = x.getLowerBound();
        final double minY = y.getLowerBound();
        final double maxX = x.getUpperBound();
        final double maxY = y.getUpperBound();
        double zMin = 1.0E20;
        double zMax = -1.0E20;
        for ( int k = 0; k < this.zValues.length; ++k ) {
            if ( this.xValues[k].doubleValue() >= minX && this.xValues[k].doubleValue() <= maxX && this.yValues[k].doubleValue() >= minY && this.yValues[k].doubleValue() <= maxY && this.zValues[k] != null ) {
                zMin = Math.min ( zMin, this.zValues[k].doubleValue() );
                zMax = Math.max ( zMax, this.zValues[k].doubleValue() );
            }
        }
        return new Range ( zMin, zMax );
    }
    public double getMinZValue ( final double minX, final double minY, final double maxX, final double maxY ) {
        double zMin = 1.0E20;
        for ( int k = 0; k < this.zValues.length; ++k ) {
            if ( this.zValues[k] != null ) {
                zMin = Math.min ( zMin, this.zValues[k].doubleValue() );
            }
        }
        return zMin;
    }
    @Override
    public int getSeriesCount() {
        return 1;
    }
    @Override
    public Comparable getSeriesKey ( final int series ) {
        if ( series > 0 ) {
            throw new IllegalArgumentException ( "Only one series for contour" );
        }
        return this.seriesKey;
    }
    @Override
    public int[] getXIndices() {
        return this.xIndex;
    }
    @Override
    public Number[] getXValues() {
        return this.xValues;
    }
    @Override
    public Number getX ( final int series, final int item ) {
        if ( series > 0 ) {
            throw new IllegalArgumentException ( "Only one series for contour" );
        }
        return this.xValues[item];
    }
    public Number getXValue ( final int item ) {
        return this.xValues[item];
    }
    @Override
    public Number[] getYValues() {
        return this.yValues;
    }
    @Override
    public Number getY ( final int series, final int item ) {
        if ( series > 0 ) {
            throw new IllegalArgumentException ( "Only one series for contour" );
        }
        return this.yValues[item];
    }
    @Override
    public Number[] getZValues() {
        return this.zValues;
    }
    @Override
    public Number getZ ( final int series, final int item ) {
        if ( series > 0 ) {
            throw new IllegalArgumentException ( "Only one series for contour" );
        }
        return this.zValues[item];
    }
    @Override
    public int[] indexX() {
        final int[] index = new int[this.xValues.length];
        for ( int k = 0; k < index.length; ++k ) {
            index[k] = this.indexX ( k );
        }
        return index;
    }
    public int indexX ( final int k ) {
        final int i = Arrays.binarySearch ( this.xIndex, k );
        if ( i >= 0 ) {
            return i;
        }
        return -1 * i - 2;
    }
    public int indexY ( final int k ) {
        return k / this.xValues.length;
    }
    public int indexZ ( final int i, final int j ) {
        return this.xValues.length * j + i;
    }
    @Override
    public boolean isDateAxis ( final int axisNumber ) {
        return axisNumber >= 0 && axisNumber <= 2 && this.dateAxis[axisNumber];
    }
    public void setSeriesKeys ( final Comparable[] seriesKeys ) {
        if ( seriesKeys.length > 1 ) {
            throw new IllegalArgumentException ( "Contours only support one series" );
        }
        this.seriesKey = seriesKeys[0];
        this.fireDatasetChanged();
    }
}
