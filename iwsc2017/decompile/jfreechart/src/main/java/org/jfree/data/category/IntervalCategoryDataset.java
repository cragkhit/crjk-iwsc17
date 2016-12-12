package org.jfree.data.category;
public interface IntervalCategoryDataset extends CategoryDataset {
    Number getStartValue ( int p0, int p1 );
    Number getStartValue ( Comparable p0, Comparable p1 );
    Number getEndValue ( int p0, int p1 );
    Number getEndValue ( Comparable p0, Comparable p1 );
}
