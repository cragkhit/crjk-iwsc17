package org.jfree.data.statistics;
import java.util.List;
import org.jfree.data.category.CategoryDataset;
public interface BoxAndWhiskerCategoryDataset extends CategoryDataset {
    Number getMeanValue ( int p0, int p1 );
    Number getMeanValue ( Comparable p0, Comparable p1 );
    Number getMedianValue ( int p0, int p1 );
    Number getMedianValue ( Comparable p0, Comparable p1 );
    Number getQ1Value ( int p0, int p1 );
    Number getQ1Value ( Comparable p0, Comparable p1 );
    Number getQ3Value ( int p0, int p1 );
    Number getQ3Value ( Comparable p0, Comparable p1 );
    Number getMinRegularValue ( int p0, int p1 );
    Number getMinRegularValue ( Comparable p0, Comparable p1 );
    Number getMaxRegularValue ( int p0, int p1 );
    Number getMaxRegularValue ( Comparable p0, Comparable p1 );
    Number getMinOutlier ( int p0, int p1 );
    Number getMinOutlier ( Comparable p0, Comparable p1 );
    Number getMaxOutlier ( int p0, int p1 );
    Number getMaxOutlier ( Comparable p0, Comparable p1 );
    List getOutliers ( int p0, int p1 );
    List getOutliers ( Comparable p0, Comparable p1 );
}
