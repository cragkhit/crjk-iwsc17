package org.jfree.chart.urls;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;
public class StandardXYZURLGenerator extends StandardXYURLGenerator implements XYZURLGenerator {
    @Override
    public String generateURL ( final XYZDataset dataset, final int series, final int item ) {
        return super.generateURL ( dataset, series, item );
    }
}
