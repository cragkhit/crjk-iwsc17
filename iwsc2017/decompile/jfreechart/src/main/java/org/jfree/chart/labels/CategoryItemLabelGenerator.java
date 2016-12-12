package org.jfree.chart.labels;
import org.jfree.data.category.CategoryDataset;
public interface CategoryItemLabelGenerator {
    String generateRowLabel ( CategoryDataset p0, int p1 );
    String generateColumnLabel ( CategoryDataset p0, int p1 );
    String generateLabel ( CategoryDataset p0, int p1, int p2 );
}
