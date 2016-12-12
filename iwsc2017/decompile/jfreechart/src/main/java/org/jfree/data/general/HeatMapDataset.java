package org.jfree.data.general;
public interface HeatMapDataset {
    int getXSampleCount();
    int getYSampleCount();
    double getMinimumXValue();
    double getMaximumXValue();
    double getMinimumYValue();
    double getMaximumYValue();
    double getXValue ( int p0 );
    double getYValue ( int p0 );
    double getZValue ( int p0, int p1 );
    Number getZ ( int p0, int p1 );
}
