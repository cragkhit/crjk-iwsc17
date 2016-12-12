package org.jfree.data.xy;
public interface IntervalXYDataset extends XYDataset {
    Number getStartX ( int p0, int p1 );
    double getStartXValue ( int p0, int p1 );
    Number getEndX ( int p0, int p1 );
    double getEndXValue ( int p0, int p1 );
    Number getStartY ( int p0, int p1 );
    double getStartYValue ( int p0, int p1 );
    Number getEndY ( int p0, int p1 );
    double getEndYValue ( int p0, int p1 );
}
