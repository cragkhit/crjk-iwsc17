package org.jfree.data.xy;
public interface VectorXYDataset extends XYDataset {
    double getVectorXValue ( int p0, int p1 );
    double getVectorYValue ( int p0, int p1 );
    Vector getVector ( int p0, int p1 );
}
