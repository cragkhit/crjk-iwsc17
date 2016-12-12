package org.jfree.chart.fx.interaction;
import javafx.scene.input.MouseEvent;
import org.jfree.chart.fx.ChartCanvas;
import java.awt.geom.Point2D;
public class DispatchHandlerFX extends AbstractMouseHandlerFX {
    private Point2D mousePressedPoint;
    public DispatchHandlerFX ( final String id ) {
        super ( id, false, false, false, false );
    }
    @Override
    public void handleMousePressed ( final ChartCanvas canvas, final MouseEvent e ) {
        this.mousePressedPoint = new Point2D.Double ( e.getX(), e.getY() );
    }
    @Override
    public void handleMouseMoved ( final ChartCanvas canvas, final MouseEvent e ) {
        final Point2D currPt = new Point2D.Double ( e.getX(), e.getY() );
        canvas.dispatchMouseMovedEvent ( currPt, e );
    }
    @Override
    public void handleMouseClicked ( final ChartCanvas canvas, final MouseEvent e ) {
        if ( this.mousePressedPoint == null ) {
            return;
        }
        final Point2D currPt = new Point2D.Double ( e.getX(), e.getY() );
        if ( this.mousePressedPoint.distance ( currPt ) < 2.0 ) {
            canvas.dispatchMouseClickedEvent ( currPt, e );
        }
        this.mousePressedPoint = null;
    }
}
