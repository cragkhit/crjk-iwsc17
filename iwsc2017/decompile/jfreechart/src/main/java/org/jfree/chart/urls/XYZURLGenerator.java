package org.jfree.chart.urls;
import org.jfree.data.xy.XYZDataset;
public interface XYZURLGenerator extends XYURLGenerator {
    String generateURL ( XYZDataset p0, int p1, int p2 );
}
