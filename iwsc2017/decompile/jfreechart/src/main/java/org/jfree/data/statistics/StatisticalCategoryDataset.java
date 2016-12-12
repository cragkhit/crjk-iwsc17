package org.jfree.data.statistics;
import org.jfree.data.category.CategoryDataset;
public interface StatisticalCategoryDataset extends CategoryDataset {
    Number getMeanValue ( int p0, int p1 );
    Number getMeanValue ( Comparable p0, Comparable p1 );
    Number getStdDevValue ( int p0, int p1 );
    Number getStdDevValue ( Comparable p0, Comparable p1 );
}
