package org.jfree.chart.fx.interaction;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.JFreeChart;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.plot.Plot;
import javafx.scene.Cursor;
import org.jfree.chart.plot.Pannable;
import javafx.scene.input.MouseEvent;
import org.jfree.chart.fx.ChartCanvas;
import java.awt.geom.Point2D;
public class PanHandlerFX extends AbstractMouseHandlerFX {
    private Point2D panLast;
    private double panW;
    private double panH;
    public PanHandlerFX ( final String id ) {
        this ( id, false, false, false, false );
    }
    public PanHandlerFX ( final String id, final boolean altKey, final boolean ctrlKey, final boolean metaKey, final boolean shiftKey ) {
        super ( id, altKey, ctrlKey, metaKey, shiftKey );
    }
    @Override
    public void handleMousePressed ( final ChartCanvas canvas, final MouseEvent e ) {
        final Plot plot = canvas.getChart().getPlot();
        if ( ! ( plot instanceof Pannable ) ) {
            canvas.clearLiveHandler();
            return;
        }
        final Pannable pannable = ( Pannable ) plot;
        if ( pannable.isDomainPannable() || pannable.isRangePannable() ) {
            final Point2D point = new Point2D.Double ( e.getX(), e.getY() );
            final Rectangle2D dataArea = canvas.findDataArea ( point );
            if ( dataArea != null && dataArea.contains ( point ) ) {
                this.panW = dataArea.getWidth();
                this.panH = dataArea.getHeight();
                this.panLast = point;
                canvas.setCursor ( Cursor.MOVE );
            }
        }
    }
    @Override
    public void handleMouseDragged ( final ChartCanvas canvas, final MouseEvent e ) {
        if ( this.panLast == null ) {
            canvas.clearLiveHandler();
            return;
        }
        final JFreeChart chart = canvas.getChart();
        final double dx = e.getX() - this.panLast.getX();
        final double dy = e.getY() - this.panLast.getY();
        if ( dx == 0.0 && dy == 0.0 ) {
            return;
        }
        final double wPercent = -dx / this.panW;
        final double hPercent = dy / this.panH;
        final boolean old = chart.getPlot().isNotify();
        chart.getPlot().setNotify ( false );
        final Pannable p = ( Pannable ) chart.getPlot();
        final PlotRenderingInfo info = canvas.getRenderingInfo().getPlotInfo();
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
    @Override
    public void handleMouseReleased ( final ChartCanvas canvas, final MouseEvent e ) {
        if ( this.panLast != null ) {
            canvas.setCursor ( Cursor.DEFAULT );
        }
        this.panLast = null;
        canvas.clearLiveHandler();
    }
}
