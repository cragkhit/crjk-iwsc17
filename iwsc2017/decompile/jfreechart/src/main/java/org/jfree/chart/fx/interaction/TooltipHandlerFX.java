package org.jfree.chart.fx.interaction;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.ChartRenderingInfo;
import javafx.scene.input.MouseEvent;
import org.jfree.chart.fx.ChartCanvas;
public class TooltipHandlerFX extends AbstractMouseHandlerFX implements MouseHandlerFX {
    public TooltipHandlerFX ( final String id ) {
        super ( id, false, false, false, false );
    }
    @Override
    public void handleMouseMoved ( final ChartCanvas canvas, final MouseEvent e ) {
        if ( !canvas.isTooltipEnabled() ) {
            return;
        }
        final String text = this.getTooltipText ( canvas, e.getX(), e.getY() );
        canvas.setTooltip ( text, e.getScreenX(), e.getScreenY() );
    }
    private String getTooltipText ( final ChartCanvas canvas, final double x, final double y ) {
        final ChartRenderingInfo info = canvas.getRenderingInfo();
        if ( info == null ) {
            return null;
        }
        final EntityCollection entities = info.getEntityCollection();
        if ( entities == null ) {
            return null;
        }
        final ChartEntity entity = entities.getEntity ( x, y );
        if ( entity == null ) {
            return null;
        }
        return entity.getToolTipText();
    }
}
