package org.jfree.chart.fx.overlay;
import java.awt.Graphics2D;
import org.jfree.chart.fx.ChartCanvas;
import org.jfree.chart.panel.Overlay;
public interface OverlayFX extends Overlay {
    public void paintOverlay ( Graphics2D g2, ChartCanvas chartCanvas );
}
