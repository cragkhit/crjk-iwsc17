package org.jfree.chart.fx.interaction;
import javafx.scene.input.ScrollEvent;
import org.jfree.chart.fx.ChartCanvas;
import javafx.scene.input.MouseEvent;
public interface MouseHandlerFX {
    String getID();
    boolean isEnabled();
    boolean hasMatchingModifiers ( MouseEvent p0 );
    void handleMouseMoved ( ChartCanvas p0, MouseEvent p1 );
    void handleMouseClicked ( ChartCanvas p0, MouseEvent p1 );
    void handleMousePressed ( ChartCanvas p0, MouseEvent p1 );
    void handleMouseDragged ( ChartCanvas p0, MouseEvent p1 );
    void handleMouseReleased ( ChartCanvas p0, MouseEvent p1 );
    void handleScroll ( ChartCanvas p0, ScrollEvent p1 );
}
