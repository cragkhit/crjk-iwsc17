package org.jfree.data.xy;
public interface IntervalXYZDataset extends XYZDataset {
    Number getStartXValue ( int p0, int p1 );
    Number getEndXValue ( int p0, int p1 );
    Number getStartYValue ( int p0, int p1 );
    Number getEndYValue ( int p0, int p1 );
    Number getStartZValue ( int p0, int p1 );
    Number getEndZValue ( int p0, int p1 );
}
