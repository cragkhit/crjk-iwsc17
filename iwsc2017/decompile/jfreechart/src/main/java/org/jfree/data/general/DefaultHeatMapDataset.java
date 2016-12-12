package org.jfree.data.general;
import org.jfree.data.DataUtilities;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class DefaultHeatMapDataset extends AbstractDataset implements HeatMapDataset, Cloneable, PublicCloneable, Serializable {
    private int xSamples;
    private int ySamples;
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;
    private double[][] zValues;
    public DefaultHeatMapDataset ( final int xSamples, final int ySamples, final double minX, final double maxX, final double minY, final double maxY ) {
        if ( xSamples < 1 ) {
            throw new IllegalArgumentException ( "Requires 'xSamples' > 0" );
        }
        if ( ySamples < 1 ) {
            throw new IllegalArgumentException ( "Requires 'ySamples' > 0" );
        }
        if ( Double.isInfinite ( minX ) || Double.isNaN ( minX ) ) {
            throw new IllegalArgumentException ( "'minX' cannot be INF or NaN." );
        }
        if ( Double.isInfinite ( maxX ) || Double.isNaN ( maxX ) ) {
            throw new IllegalArgumentException ( "'maxX' cannot be INF or NaN." );
        }
        if ( Double.isInfinite ( minY ) || Double.isNaN ( minY ) ) {
            throw new IllegalArgumentException ( "'minY' cannot be INF or NaN." );
        }
        if ( Double.isInfinite ( maxY ) || Double.isNaN ( maxY ) ) {
            throw new IllegalArgumentException ( "'maxY' cannot be INF or NaN." );
        }
        this.xSamples = xSamples;
        this.ySamples = ySamples;
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.zValues = new double[xSamples][];
        for ( int x = 0; x < xSamples; ++x ) {
            this.zValues[x] = new double[ySamples];
        }
    }
    @Override
    public int getXSampleCount() {
        return this.xSamples;
    }
    @Override
    public int getYSampleCount() {
        return this.ySamples;
    }
    @Override
    public double getMinimumXValue() {
        return this.minX;
    }
    @Override
    public double getMaximumXValue() {
        return this.maxX;
    }
    @Override
    public double getMinimumYValue() {
        return this.minY;
    }
    @Override
    public double getMaximumYValue() {
        return this.maxY;
    }
    @Override
    public double getXValue ( final int xIndex ) {
        final double x = this.minX + ( this.maxX - this.minX ) * ( xIndex / this.xSamples );
        return x;
    }
    @Override
    public double getYValue ( final int yIndex ) {
        final double y = this.minY + ( this.maxY - this.minY ) * ( yIndex / this.ySamples );
        return y;
    }
    @Override
    public double getZValue ( final int xIndex, final int yIndex ) {
        return this.zValues[xIndex][yIndex];
    }
    @Override
    public Number getZ ( final int xIndex, final int yIndex ) {
        return new Double ( this.getZValue ( xIndex, yIndex ) );
    }
    public void setZValue ( final int xIndex, final int yIndex, final double z ) {
        this.setZValue ( xIndex, yIndex, z, true );
    }
    public void setZValue ( final int xIndex, final int yIndex, final double z, final boolean notify ) {
        this.zValues[xIndex][yIndex] = z;
        if ( notify ) {
            this.fireDatasetChanged();
        }
    }
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof DefaultHeatMapDataset ) ) {
            return false;
        }
        final DefaultHeatMapDataset that = ( DefaultHeatMapDataset ) obj;
        return this.xSamples == that.xSamples && this.ySamples == that.ySamples && this.minX == that.minX && this.maxX == that.maxX && this.minY == that.minY && this.maxY == that.maxY && DataUtilities.equal ( this.zValues, that.zValues );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final DefaultHeatMapDataset clone = ( DefaultHeatMapDataset ) super.clone();
        clone.zValues = DataUtilities.clone ( this.zValues );
        return clone;
    }
}
