package org.jfree.data.statistics;
import java.util.List;
import org.jfree.data.category.CategoryDataset;
public interface MultiValueCategoryDataset extends CategoryDataset {
    List getValues ( int p0, int p1 );
    List getValues ( Comparable p0, Comparable p1 );
}
