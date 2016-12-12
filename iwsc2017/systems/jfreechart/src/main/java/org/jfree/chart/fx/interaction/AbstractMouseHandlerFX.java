package org.jfree.chart.fx.interaction;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import org.jfree.chart.fx.ChartCanvas;
import org.jfree.chart.util.ParamChecks;
public class AbstractMouseHandlerFX implements MouseHandlerFX {
    private String id;
    private boolean enabled;
    private boolean altKey;
    private boolean ctrlKey;
    private boolean metaKey;
    private boolean shiftKey;
    public AbstractMouseHandlerFX ( String id, boolean altKey, boolean ctrlKey,
                                    boolean metaKey, boolean shiftKey ) {
        ParamChecks.nullNotPermitted ( id, "id" );
        this.id = id;
        this.enabled = true;
        this.altKey = altKey;
        this.ctrlKey = ctrlKey;
        this.metaKey = metaKey;
        this.shiftKey = shiftKey;
    }
    @Override
    public String getID() {
        return this.id;
    }
    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
    public void setEnabled ( boolean enabled ) {
        this.enabled = enabled;
    }
    @Override
    public boolean hasMatchingModifiers ( MouseEvent e ) {
        boolean b = true;
        b = b && ( this.altKey == e.isAltDown() );
        b = b && ( this.ctrlKey == e.isControlDown() );
        b = b && ( this.metaKey == e.isMetaDown() );
        b = b && ( this.shiftKey == e.isShiftDown() );
        return b;
    }
    @Override
    public void handleMouseMoved ( ChartCanvas canvas, MouseEvent e ) {
    }
    @Override
    public void handleMouseClicked ( ChartCanvas canvas, MouseEvent e ) {
    }
    @Override
    public void handleMousePressed ( ChartCanvas canvas, MouseEvent e ) {
    }
    @Override
    public void handleMouseDragged ( ChartCanvas canvas, MouseEvent e ) {
    }
    @Override
    public void handleMouseReleased ( ChartCanvas canvas, MouseEvent e ) {
    }
    @Override
    public void handleScroll ( ChartCanvas canvas, ScrollEvent e ) {
    }
}
