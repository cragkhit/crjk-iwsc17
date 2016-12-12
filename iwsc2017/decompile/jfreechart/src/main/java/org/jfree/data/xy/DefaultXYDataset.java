package org.jfree.data.xy;
import java.util.Collection;
import java.util.Arrays;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.DomainOrder;
import java.util.ArrayList;
import java.util.List;
import org.jfree.util.PublicCloneable;
public class DefaultXYDataset extends AbstractXYDataset implements XYDataset, PublicCloneable {
    private List seriesKeys;
    private List seriesList;
    public DefaultXYDataset() {
        this.seriesKeys = new ArrayList();
        this.seriesList = new ArrayList();
    }
    public int getSeriesCount() {
        return this.seriesList.size();
    }
    public Comparable getSeriesKey ( final int series ) {
        if ( series < 0 || series >= this.getSeriesCount() ) {
            throw new IllegalArgumentException ( "Series index out of bounds" );
        }
        return this.seriesKeys.get ( series );
    }
    public int indexOf ( final Comparable seriesKey ) {
        return this.seriesKeys.indexOf ( seriesKey );
    }
    @Override
    public DomainOrder getDomainOrder() {
        return DomainOrder.NONE;
    }
    @Override
    public int getItemCount ( final int series ) {
        if ( series < 0 || series >= this.getSeriesCount() ) {
            throw new IllegalArgumentException ( "Series index out of bounds" );
        }
        final double[][] seriesArray = this.seriesList.get ( series );
        return seriesArray[0].length;
    }
    @Override
    public double getXValue ( final int series, final int item ) {
        final double[][] seriesData = this.seriesList.get ( series );
        return seriesData[0][item];
    }
    @Override
    public Number getX ( final int series, final int item ) {
        return new Double ( this.getXValue ( series, item ) );
    }
    @Override
    public double getYValue ( final int series, final int item ) {
        final double[][] seriesData = this.seriesList.get ( series );
        return seriesData[1][item];
    }
    @Override
    public Number getY ( final int series, final int item ) {
        return new Double ( this.getYValue ( series, item ) );
    }
    public void addSeries ( final Comparable seriesKey, final double[][] data ) {
        if ( seriesKey == null ) {
            throw new IllegalArgumentException ( "The 'seriesKey' cannot be null." );
        }
        if ( data == null ) {
            throw new IllegalArgumentException ( "The 'data' is null." );
        }
        if ( data.length != 2 ) {
            throw new IllegalArgumentException ( "The 'data' array must have length == 2." );
        }
        if ( data[0].length != data[1].length ) {
            throw new IllegalArgumentException ( "The 'data' array must contain two arrays with equal length." );
        }
        final int seriesIndex = this.indexOf ( seriesKey );
        if ( seriesIndex == -1 ) {
            this.seriesKeys.add ( seriesKey );
            this.seriesList.add ( data );
        } else {
            this.seriesList.remove ( seriesIndex );
            this.seriesList.add ( seriesIndex, data );
        }
        this.notifyListeners ( new DatasetChangeEvent ( this, this ) );
    }
    public void removeSeries ( final Comparable seriesKey ) {
        final int seriesIndex = this.indexOf ( seriesKey );
        if ( seriesIndex >= 0 ) {
            this.seriesKeys.remove ( seriesIndex );
            this.seriesList.remove ( seriesIndex );
            this.notifyListeners ( new DatasetChangeEvent ( this, this ) );
        }
    }
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof DefaultXYDataset ) ) {
            return false;
        }
        final DefaultXYDataset that = ( DefaultXYDataset ) obj;
        if ( !this.seriesKeys.equals ( that.seriesKeys ) ) {
            return false;
        }
        for ( int i = 0; i < this.seriesList.size(); ++i ) {
            final double[][] d1 = this.seriesList.get ( i );
            final double[][] d2 = that.seriesList.get ( i );
            final double[] d1x = d1[0];
            final double[] d2x = d2[0];
            if ( !Arrays.equals ( d1x, d2x ) ) {
                return false;
            }
            final double[] d1y = d1[1];
            final double[] d2y = d2[1];
            if ( !Arrays.equals ( d1y, d2y ) ) {
                return false;
            }
        }
        return true;
    }
    public int hashCode() {
        int result = this.seriesKeys.hashCode();
        result = 29 * result + this.seriesList.hashCode();
        return result;
    }
    public Object clone() throws CloneNotSupportedException {
        final DefaultXYDataset clone = ( DefaultXYDataset ) super.clone();
        clone.seriesKeys = new ArrayList ( this.seriesKeys );
        clone.seriesList = new ArrayList ( this.seriesList.size() );
        for ( int i = 0; i < this.seriesList.size(); ++i ) {
            final double[][] data = this.seriesList.get ( i );
            final double[] x = data[0];
            final double[] y = data[1];
            final double[] xx = new double[x.length];
            final double[] yy = new double[y.length];
            System.arraycopy ( x, 0, xx, 0, x.length );
            System.arraycopy ( y, 0, yy, 0, y.length );
            clone.seriesList.add ( i, new double[][] { xx, yy } );
        }
        return clone;
    }
}
