package org.jfree.chart.fx.interaction;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javafx.scene.input.MouseEvent;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.fx.ChartCanvas;
import org.jfree.chart.plot.Pannable;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
public class PanHandlerFX extends AbstractMouseHandlerFX {
    private Point2D panLast;
    private double panW;
    private double panH;
    public PanHandlerFX ( String id ) {
        this ( id, false, false, false, false );
    }
    public PanHandlerFX ( String id, boolean altKey, boolean ctrlKey,
                          boolean metaKey, boolean shiftKey ) {
        super ( id, altKey, ctrlKey, metaKey, shiftKey );
    }
    @Override
    public void handleMousePressed ( ChartCanvas canvas, MouseEvent e ) {
        Plot plot = canvas.getChart().getPlot();
        if ( ! ( plot instanceof Pannable ) ) {
            canvas.clearLiveHandler();
            return;
        }
        Pannable pannable = ( Pannable ) plot;
        if ( pannable.isDomainPannable() || pannable.isRangePannable() ) {
            Point2D point = new Point2D.Double ( e.getX(), e.getY() );
            Rectangle2D dataArea = canvas.findDataArea ( point );
            if ( dataArea != null && dataArea.contains ( point ) ) {
                this.panW = dataArea.getWidth();
                this.panH = dataArea.getHeight();
                this.panLast = point;
                canvas.setCursor ( javafx.scene.Cursor.MOVE );
            }
        }
    }
    public void handleMouseDragged ( ChartCanvas canvas, MouseEvent e ) {
        if ( this.panLast == null ) {
            canvas.clearLiveHandler();
            return;
        }
        JFreeChart chart = canvas.getChart();
        double dx = e.getX() - this.panLast.getX();
        double dy = e.getY() - this.panLast.getY();
        if ( dx == 0.0 && dy == 0.0 ) {
            return;
        }
        double wPercent = -dx / this.panW;
        double hPercent = dy / this.panH;
        boolean old = chart.getPlot().isNotify();
        chart.getPlot().setNotify ( false );
        Pannable p = ( Pannable ) chart.getPlot();
        PlotRenderingInfo info = canvas.getRenderingInfo().getPlotInfo();
        if ( p.getOrientation().isVertical() ) {
            p.panDomainAxes ( wPercent, info, this.panLast );
            p.panRangeAxes ( hPercent, info, this.panLast );
        } else {
            p.panDomainAxes ( hPercent, info, this.panLast );
            p.panRangeAxes ( wPercent, info, this.panLast );
        }
        this.panLast = new Point2D.Double ( e.getX(), e.getY() );
        chart.getPlot().setNotify ( old );
    }
    public void handleMouseReleased ( ChartCanvas canvas, MouseEvent e ) {
        if ( this.panLast != null ) {
            canvas.setCursor ( javafx.scene.Cursor.DEFAULT );
        }
        this.panLast = null;
        canvas.clearLiveHandler();
    }
}
