package org.jfree.chart;
import java.util.EventListener;
public interface ChartMouseListener extends EventListener {
    void chartMouseClicked ( ChartMouseEvent p0 );
    void chartMouseMoved ( ChartMouseEvent p0 );
}
