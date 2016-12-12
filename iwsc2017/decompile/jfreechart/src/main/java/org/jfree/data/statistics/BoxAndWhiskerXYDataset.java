package org.jfree.data.statistics;
import java.util.List;
import org.jfree.data.xy.XYDataset;
public interface BoxAndWhiskerXYDataset extends XYDataset {
    Number getMeanValue ( int p0, int p1 );
    Number getMedianValue ( int p0, int p1 );
    Number getQ1Value ( int p0, int p1 );
    Number getQ3Value ( int p0, int p1 );
    Number getMinRegularValue ( int p0, int p1 );
    Number getMaxRegularValue ( int p0, int p1 );
    Number getMinOutlier ( int p0, int p1 );
    Number getMaxOutlier ( int p0, int p1 );
    List getOutliers ( int p0, int p1 );
    double getOutlierCoefficient();
    double getFaroutCoefficient();
}
