package org.jfree.data.general;
import java.io.Serializable;
public abstract class AbstractSeriesDataset extends AbstractDataset implements SeriesDataset, SeriesChangeListener, Serializable {
    private static final long serialVersionUID = -6074996219705033171L;
    @Override
    public abstract int getSeriesCount();
    @Override
    public abstract Comparable getSeriesKey ( final int p0 );
    @Override
    public int indexOf ( final Comparable seriesKey ) {
        for ( int seriesCount = this.getSeriesCount(), s = 0; s < seriesCount; ++s ) {
            if ( this.getSeriesKey ( s ).equals ( seriesKey ) ) {
                return s;
            }
        }
        return -1;
    }
    @Override
    public void seriesChanged ( final SeriesChangeEvent event ) {
        this.fireDatasetChanged();
    }
}
