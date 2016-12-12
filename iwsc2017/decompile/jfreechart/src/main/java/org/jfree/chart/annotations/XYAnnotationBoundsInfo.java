package org.jfree.chart.annotations;
import org.jfree.data.Range;
public interface XYAnnotationBoundsInfo {
    boolean getIncludeInDataBounds();
    Range getXRange();
    Range getYRange();
}
