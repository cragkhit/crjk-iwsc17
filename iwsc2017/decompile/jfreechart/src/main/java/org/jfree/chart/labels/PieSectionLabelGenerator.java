package org.jfree.chart.labels;
import java.text.AttributedString;
import org.jfree.data.general.PieDataset;
public interface PieSectionLabelGenerator {
    String generateSectionLabel ( PieDataset p0, Comparable p1 );
    AttributedString generateAttributedSectionLabel ( PieDataset p0, Comparable p1 );
}
