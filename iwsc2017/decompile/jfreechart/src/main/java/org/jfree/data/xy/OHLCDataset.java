package org.jfree.data.xy;
public interface OHLCDataset extends XYDataset {
    Number getHigh ( int p0, int p1 );
    double getHighValue ( int p0, int p1 );
    Number getLow ( int p0, int p1 );
    double getLowValue ( int p0, int p1 );
    Number getOpen ( int p0, int p1 );
    double getOpenValue ( int p0, int p1 );
    Number getClose ( int p0, int p1 );
    double getCloseValue ( int p0, int p1 );
    Number getVolume ( int p0, int p1 );
    double getVolumeValue ( int p0, int p1 );
}
