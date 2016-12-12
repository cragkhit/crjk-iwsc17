package org.jfree.chart.fx.interaction;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.fx.ChartViewer;
public interface ChartMouseListenerFX {
    void chartMouseClicked ( ChartMouseEventFX event );
    void chartMouseMoved ( ChartMouseEventFX event );
}
