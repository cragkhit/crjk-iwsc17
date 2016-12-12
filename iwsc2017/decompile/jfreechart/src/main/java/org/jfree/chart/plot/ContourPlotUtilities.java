package org.jfree.chart.plot;
import org.jfree.data.contour.DefaultContourDataset;
import org.jfree.data.Range;
import org.jfree.data.contour.ContourDataset;
public abstract class ContourPlotUtilities {
    public static Range visibleRange ( final ContourDataset data, final Range x, final Range y ) {
        Range range = null;
        range = ( ( DefaultContourDataset ) data ).getZValueRange ( x, y );
        return range;
    }
}
