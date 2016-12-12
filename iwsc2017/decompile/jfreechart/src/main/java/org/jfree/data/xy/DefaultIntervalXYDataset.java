package org.jfree.data.xy;
import java.util.Collection;
import java.util.Arrays;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetChangeEvent;
import java.util.ArrayList;
import java.util.List;
import org.jfree.util.PublicCloneable;
public class DefaultIntervalXYDataset extends AbstractIntervalXYDataset implements PublicCloneable {
    private List seriesKeys;
    private List seriesList;
    public DefaultIntervalXYDataset() {
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
    public int getItemCount ( final int series ) {
        if ( series < 0 || series >= this.getSeriesCount() ) {
            throw new IllegalArgumentException ( "Series index out of bounds" );
        }
        final double[][] seriesArray = this.seriesList.get ( series );
        return seriesArray[0].length;
    }
    public double getXValue ( final int series, final int item ) {
        final double[][] seriesData = this.seriesList.get ( series );
        return seriesData[0][item];
    }
    public double getYValue ( final int series, final int item ) {
        final double[][] seriesData = this.seriesList.get ( series );
        return seriesData[3][item];
    }
    @Override
    public double getStartXValue ( final int series, final int item ) {
        final double[][] seriesData = this.seriesList.get ( series );
        return seriesData[1][item];
    }
    @Override
    public double getEndXValue ( final int series, final int item ) {
        final double[][] seriesData = this.seriesList.get ( series );
        return seriesData[2][item];
    }
    @Override
    public double getStartYValue ( final int series, final int item ) {
        final double[][] seriesData = this.seriesList.get ( series );
        return seriesData[4][item];
    }
    @Override
    public double getEndYValue ( final int series, final int item ) {
        final double[][] seriesData = this.seriesList.get ( series );
        return seriesData[5][item];
    }
    public Number getEndX ( final int series, final int item ) {
        return new Double ( this.getEndXValue ( series, item ) );
    }
    public Number getEndY ( final int series, final int item ) {
        return new Double ( this.getEndYValue ( series, item ) );
    }
    public Number getStartX ( final int series, final int item ) {
        return new Double ( this.getStartXValue ( series, item ) );
    }
    public Number getStartY ( final int series, final int item ) {
        return new Double ( this.getStartYValue ( series, item ) );
    }
    public Number getX ( final int series, final int item ) {
        return new Double ( this.getXValue ( series, item ) );
    }
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
        if ( data.length != 6 ) {
            throw new IllegalArgumentException ( "The 'data' array must have length == 6." );
        }
        final int length = data[0].length;
        if ( length != data[1].length || length != data[2].length || length != data[3].length || length != data[4].length || length != data[5].length ) {
            throw new IllegalArgumentException ( "The 'data' array must contain six arrays with equal length." );
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
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof DefaultIntervalXYDataset ) ) {
            return false;
        }
        final DefaultIntervalXYDataset that = ( DefaultIntervalXYDataset ) obj;
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
            final double[] d1xs = d1[1];
            final double[] d2xs = d2[1];
            if ( !Arrays.equals ( d1xs, d2xs ) ) {
                return false;
            }
            final double[] d1xe = d1[2];
            final double[] d2xe = d2[2];
            if ( !Arrays.equals ( d1xe, d2xe ) ) {
                return false;
            }
            final double[] d1y = d1[3];
            final double[] d2y = d2[3];
            if ( !Arrays.equals ( d1y, d2y ) ) {
                return false;
            }
            final double[] d1ys = d1[4];
            final double[] d2ys = d2[4];
            if ( !Arrays.equals ( d1ys, d2ys ) ) {
                return false;
            }
            final double[] d1ye = d1[5];
            final double[] d2ye = d2[5];
            if ( !Arrays.equals ( d1ye, d2ye ) ) {
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
        final DefaultIntervalXYDataset clone = ( DefaultIntervalXYDataset ) super.clone();
        clone.seriesKeys = new ArrayList ( this.seriesKeys );
        clone.seriesList = new ArrayList ( this.seriesList.size() );
        for ( int i = 0; i < this.seriesList.size(); ++i ) {
            final double[][] data = this.seriesList.get ( i );
            final double[] x = data[0];
            final double[] xStart = data[1];
            final double[] xEnd = data[2];
            final double[] y = data[3];
            final double[] yStart = data[4];
            final double[] yEnd = data[5];
            final double[] xx = new double[x.length];
            final double[] xxStart = new double[xStart.length];
            final double[] xxEnd = new double[xEnd.length];
            final double[] yy = new double[y.length];
            final double[] yyStart = new double[yStart.length];
            final double[] yyEnd = new double[yEnd.length];
            System.arraycopy ( x, 0, xx, 0, x.length );
            System.arraycopy ( xStart, 0, xxStart, 0, xStart.length );
            System.arraycopy ( xEnd, 0, xxEnd, 0, xEnd.length );
            System.arraycopy ( y, 0, yy, 0, y.length );
            System.arraycopy ( yStart, 0, yyStart, 0, yStart.length );
            System.arraycopy ( yEnd, 0, yyEnd, 0, yEnd.length );
            clone.seriesList.add ( i, new double[][] { xx, xxStart, xxEnd, yy, yyStart, yyEnd } );
        }
        return clone;
    }
}
