package org.jfree.chart.fx.interaction;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import org.jfree.chart.fx.ChartCanvas;
public interface MouseHandlerFX {
    String getID();
    boolean isEnabled();
    boolean hasMatchingModifiers ( MouseEvent e );
    void handleMouseMoved ( ChartCanvas canvas, MouseEvent e );
    void handleMouseClicked ( ChartCanvas canvas, MouseEvent e );
    void handleMousePressed ( ChartCanvas canvas, MouseEvent e );
    void handleMouseDragged ( ChartCanvas canvas, MouseEvent e );
    void handleMouseReleased ( ChartCanvas canvas, MouseEvent e );
    void handleScroll ( ChartCanvas canvas, ScrollEvent e );
}
