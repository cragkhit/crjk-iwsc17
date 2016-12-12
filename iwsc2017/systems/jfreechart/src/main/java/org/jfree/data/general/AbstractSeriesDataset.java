

package org.jfree.data.general;

import java.io.Serializable;


public abstract class AbstractSeriesDataset extends AbstractDataset
    implements SeriesDataset, SeriesChangeListener, Serializable {


    private static final long serialVersionUID = -6074996219705033171L;


    protected AbstractSeriesDataset() {
        super();
    }


    @Override
    public abstract int getSeriesCount();


    @Override
    public abstract Comparable getSeriesKey ( int series );


    @Override
    public int indexOf ( Comparable seriesKey ) {
        int seriesCount = getSeriesCount();
        for ( int s = 0; s < seriesCount; s++ ) {
            if ( getSeriesKey ( s ).equals ( seriesKey ) ) {
                return s;
            }
        }
        return -1;
    }


    @Override
    public void seriesChanged ( SeriesChangeEvent event ) {
        fireDatasetChanged();
    }

}
