package org.jfree.chart.fx.overlay;
import org.jfree.chart.fx.ChartCanvas;
import java.awt.Graphics2D;
import org.jfree.chart.panel.Overlay;
public interface OverlayFX extends Overlay {
    void paintOverlay ( Graphics2D p0, ChartCanvas p1 );
}
