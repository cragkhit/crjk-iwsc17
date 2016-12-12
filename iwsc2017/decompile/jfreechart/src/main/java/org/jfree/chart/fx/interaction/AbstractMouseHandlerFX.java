package org.jfree.chart.fx.interaction;
import javafx.scene.input.ScrollEvent;
import org.jfree.chart.fx.ChartCanvas;
import javafx.scene.input.MouseEvent;
import org.jfree.chart.util.ParamChecks;
public class AbstractMouseHandlerFX implements MouseHandlerFX {
    private String id;
    private boolean enabled;
    private boolean altKey;
    private boolean ctrlKey;
    private boolean metaKey;
    private boolean shiftKey;
    public AbstractMouseHandlerFX ( final String id, final boolean altKey, final boolean ctrlKey, final boolean metaKey, final boolean shiftKey ) {
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
    public void setEnabled ( final boolean enabled ) {
        this.enabled = enabled;
    }
    @Override
    public boolean hasMatchingModifiers ( final MouseEvent e ) {
        boolean b = true;
        b = ( b && this.altKey == e.isAltDown() );
        b = ( b && this.ctrlKey == e.isControlDown() );
        b = ( b && this.metaKey == e.isMetaDown() );
        b = ( b && this.shiftKey == e.isShiftDown() );
        return b;
    }
    @Override
    public void handleMouseMoved ( final ChartCanvas canvas, final MouseEvent e ) {
    }
    @Override
    public void handleMouseClicked ( final ChartCanvas canvas, final MouseEvent e ) {
    }
    @Override
    public void handleMousePressed ( final ChartCanvas canvas, final MouseEvent e ) {
    }
    @Override
    public void handleMouseDragged ( final ChartCanvas canvas, final MouseEvent e ) {
    }
    @Override
    public void handleMouseReleased ( final ChartCanvas canvas, final MouseEvent e ) {
    }
    @Override
    public void handleScroll ( final ChartCanvas canvas, final ScrollEvent e ) {
    }
}
