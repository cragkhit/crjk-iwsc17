package org.jfree.chart.fx.interaction;
import java.awt.geom.Point2D;
import javafx.scene.input.ScrollEvent;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.fx.ChartCanvas;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.Zoomable;
public class ScrollHandlerFX extends AbstractMouseHandlerFX
    implements MouseHandlerFX {
    private double zoomFactor = 0.1;
    public ScrollHandlerFX ( String id ) {
        super ( id, false, false, false, false );
        this.zoomFactor = 0.1;
    };
    public double getZoomFactor() {
        return this.zoomFactor;
    }
    public void setZoomFactor ( double zoomFactor ) {
        this.zoomFactor = zoomFactor;
    }
    @Override
    public void handleScroll ( ChartCanvas canvas, ScrollEvent e ) {
        JFreeChart chart = canvas.getChart();
        Plot plot = chart.getPlot();
        if ( plot instanceof Zoomable ) {
            Zoomable zoomable = ( Zoomable ) plot;
            handleZoomable ( canvas, zoomable, e );
        } else if ( plot instanceof PiePlot ) {
            PiePlot pp = ( PiePlot ) plot;
            pp.handleMouseWheelRotation ( ( int ) e.getDeltaY() );
        }
    }
    private void handleZoomable ( ChartCanvas canvas, Zoomable zoomable,
                                  ScrollEvent e ) {
        ChartRenderingInfo info = canvas.getRenderingInfo();
        PlotRenderingInfo pinfo = info.getPlotInfo();
        Point2D p = new Point2D.Double ( e.getX(), e.getY() );
        if ( pinfo.getDataArea().contains ( p ) ) {
            Plot plot = ( Plot ) zoomable;
            boolean notifyState = plot.isNotify();
            plot.setNotify ( false );
            int clicks = ( int ) e.getDeltaY();
            double zf = 1.0 + this.zoomFactor;
            if ( clicks < 0 ) {
                zf = 1.0 / zf;
            }
            if ( canvas.isDomainZoomable() ) {
                zoomable.zoomDomainAxes ( zf, pinfo, p, true );
            }
            if ( canvas.isRangeZoomable() ) {
                zoomable.zoomRangeAxes ( zf, pinfo, p, true );
            }
            plot.setNotify ( notifyState );
        }
    }
}
