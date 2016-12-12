package org.jfree.chart.labels;
import org.jfree.data.xy.XYZDataset;
public interface XYZToolTipGenerator extends XYToolTipGenerator {
    String generateToolTip ( XYZDataset p0, int p1, int p2 );
}
