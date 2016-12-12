package org.jfree.data.gantt;
import org.jfree.data.category.IntervalCategoryDataset;
public interface GanttCategoryDataset extends IntervalCategoryDataset {
    Number getPercentComplete ( int p0, int p1 );
    Number getPercentComplete ( Comparable p0, Comparable p1 );
    int getSubIntervalCount ( int p0, int p1 );
    int getSubIntervalCount ( Comparable p0, Comparable p1 );
    Number getStartValue ( int p0, int p1, int p2 );
    Number getStartValue ( Comparable p0, Comparable p1, int p2 );
    Number getEndValue ( int p0, int p1, int p2 );
    Number getEndValue ( Comparable p0, Comparable p1, int p2 );
    Number getPercentComplete ( int p0, int p1, int p2 );
    Number getPercentComplete ( Comparable p0, Comparable p1, int p2 );
}
