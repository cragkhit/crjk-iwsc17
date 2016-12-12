package org.jfree.data.general;
public interface SeriesDataset extends Dataset {
    int getSeriesCount();
    Comparable getSeriesKey ( int p0 );
    int indexOf ( Comparable p0 );
}
