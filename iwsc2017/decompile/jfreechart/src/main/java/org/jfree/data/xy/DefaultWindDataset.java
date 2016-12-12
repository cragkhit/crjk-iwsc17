package org.jfree.data.xy;
import java.util.Collections;
import java.util.Date;
import org.jfree.chart.util.ParamChecks;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import org.jfree.util.PublicCloneable;
public class DefaultWindDataset extends AbstractXYDataset implements WindDataset, PublicCloneable {
    private List seriesKeys;
    private List allSeriesData;
    public DefaultWindDataset() {
        this.seriesKeys = new ArrayList();
        this.allSeriesData = new ArrayList();
    }
    public DefaultWindDataset ( final Object[][][] data ) {
        this ( seriesNameListFromDataArray ( data ), data );
    }
    public DefaultWindDataset ( final String[] seriesNames, final Object[][][] data ) {
        this ( Arrays.asList ( seriesNames ), data );
    }
    public DefaultWindDataset ( final List seriesKeys, final Object[][][] data ) {
        ParamChecks.nullNotPermitted ( seriesKeys, "seriesKeys" );
        if ( seriesKeys.size() != data.length ) {
            throw new IllegalArgumentException ( "The number of series keys does not match the number of series in the data array." );
        }
        this.seriesKeys = seriesKeys;
        final int seriesCount = data.length;
        this.allSeriesData = new ArrayList ( seriesCount );
        for ( int seriesIndex = 0; seriesIndex < seriesCount; ++seriesIndex ) {
            final List oneSeriesData = new ArrayList();
            for ( int maxItemCount = data[seriesIndex].length, itemIndex = 0; itemIndex < maxItemCount; ++itemIndex ) {
                final Object xObject = data[seriesIndex][itemIndex][0];
                if ( xObject != null ) {
                    Number xNumber;
                    if ( xObject instanceof Number ) {
                        xNumber = ( Number ) xObject;
                    } else if ( xObject instanceof Date ) {
                        final Date xDate = ( Date ) xObject;
                        xNumber = new Long ( xDate.getTime() );
                    } else {
                        xNumber = new Integer ( 0 );
                    }
                    final Number windDir = ( Number ) data[seriesIndex][itemIndex][1];
                    final Number windForce = ( Number ) data[seriesIndex][itemIndex][2];
                    oneSeriesData.add ( new WindDataItem ( xNumber, windDir, windForce ) );
                }
            }
            Collections.sort ( ( List<Comparable> ) oneSeriesData );
            this.allSeriesData.add ( seriesIndex, oneSeriesData );
        }
    }
    public int getSeriesCount() {
        return this.allSeriesData.size();
    }
    public int getItemCount ( final int series ) {
        if ( series < 0 || series >= this.getSeriesCount() ) {
            throw new IllegalArgumentException ( "Invalid series index: " + series );
        }
        final List oneSeriesData = this.allSeriesData.get ( series );
        return oneSeriesData.size();
    }
    public Comparable getSeriesKey ( final int series ) {
        if ( series < 0 || series >= this.getSeriesCount() ) {
            throw new IllegalArgumentException ( "Invalid series index: " + series );
        }
        return this.seriesKeys.get ( series );
    }
    public Number getX ( final int series, final int item ) {
        final List oneSeriesData = this.allSeriesData.get ( series );
        final WindDataItem windItem = oneSeriesData.get ( item );
        return windItem.getX();
    }
    public Number getY ( final int series, final int item ) {
        return this.getWindForce ( series, item );
    }
    @Override
    public Number getWindDirection ( final int series, final int item ) {
        final List oneSeriesData = this.allSeriesData.get ( series );
        final WindDataItem windItem = oneSeriesData.get ( item );
        return windItem.getWindDirection();
    }
    @Override
    public Number getWindForce ( final int series, final int item ) {
        final List oneSeriesData = this.allSeriesData.get ( series );
        final WindDataItem windItem = oneSeriesData.get ( item );
        return windItem.getWindForce();
    }
    public static List seriesNameListFromDataArray ( final Object[][] data ) {
        final int seriesCount = data.length;
        final List seriesNameList = new ArrayList ( seriesCount );
        for ( int i = 0; i < seriesCount; ++i ) {
            seriesNameList.add ( "Series " + ( i + 1 ) );
        }
        return seriesNameList;
    }
    public boolean equals ( final Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( ! ( obj instanceof DefaultWindDataset ) ) {
            return false;
        }
        final DefaultWindDataset that = ( DefaultWindDataset ) obj;
        return this.seriesKeys.equals ( that.seriesKeys ) && this.allSeriesData.equals ( that.allSeriesData );
    }
}
