package org.jfree.chart.fx.interaction;
import javafx.scene.input.MouseEvent;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.fx.ChartCanvas;
public class TooltipHandlerFX extends AbstractMouseHandlerFX
    implements MouseHandlerFX {
    public TooltipHandlerFX ( String id ) {
        super ( id, false, false, false, false );
    }
    @Override
    public void handleMouseMoved ( ChartCanvas canvas, MouseEvent e ) {
        if ( !canvas.isTooltipEnabled() ) {
            return;
        }
        String text = getTooltipText ( canvas, e.getX(), e.getY() );
        canvas.setTooltip ( text, e.getScreenX(), e.getScreenY() );
    }
    private String getTooltipText ( ChartCanvas canvas, double x, double y ) {
        ChartRenderingInfo info = canvas.getRenderingInfo();
        if ( info == null ) {
            return null;
        }
        EntityCollection entities = info.getEntityCollection();
        if ( entities == null ) {
            return null;
        }
        ChartEntity entity = entities.getEntity ( x, y );
        if ( entity == null ) {
            return null;
        }
        return entity.getToolTipText();
    }
}
