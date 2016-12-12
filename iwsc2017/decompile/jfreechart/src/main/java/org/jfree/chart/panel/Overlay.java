package org.jfree.chart.panel;
import org.jfree.chart.event.OverlayChangeListener;
import org.jfree.chart.ChartPanel;
import java.awt.Graphics2D;
public interface Overlay {
    void paintOverlay ( Graphics2D p0, ChartPanel p1 );
    void addChangeListener ( OverlayChangeListener p0 );
    void removeChangeListener ( OverlayChangeListener p0 );
}
