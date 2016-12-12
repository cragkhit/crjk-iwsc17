package org.jfree.chart.fx.interaction;
import java.awt.geom.Point2D;
import javafx.scene.input.MouseEvent;
import org.jfree.chart.fx.ChartCanvas;
import org.jfree.chart.fx.ChartViewer;
public class DispatchHandlerFX extends AbstractMouseHandlerFX {
    private Point2D mousePressedPoint;
    public DispatchHandlerFX ( String id ) {
        super ( id, false, false, false, false );
    }
    @Override
    public void handleMousePressed ( ChartCanvas canvas, MouseEvent e ) {
        this.mousePressedPoint = new Point2D.Double ( e.getX(), e.getY() );
    }
    @Override
    public void handleMouseMoved ( ChartCanvas canvas, MouseEvent e ) {
        Point2D currPt = new Point2D.Double ( e.getX(), e.getY() );
        canvas.dispatchMouseMovedEvent ( currPt, e );
    }
    @Override
    public void handleMouseClicked ( ChartCanvas canvas, MouseEvent e ) {
        if ( this.mousePressedPoint == null ) {
            return;
        }
        Point2D currPt = new Point2D.Double ( e.getX(), e.getY() );
        if ( this.mousePressedPoint.distance ( currPt ) < 2 ) {
            canvas.dispatchMouseClickedEvent ( currPt, e );
        }
        this.mousePressedPoint = null;
    }
}
