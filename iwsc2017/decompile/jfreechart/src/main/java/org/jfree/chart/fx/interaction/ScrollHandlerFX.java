package org.jfree.chart.fx.interaction;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.ChartRenderingInfo;
import java.awt.geom.Point2D;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Zoomable;
import javafx.scene.input.ScrollEvent;
import org.jfree.chart.fx.ChartCanvas;
public class ScrollHandlerFX extends AbstractMouseHandlerFX implements MouseHandlerFX {
    private double zoomFactor;
    public ScrollHandlerFX ( final String id ) {
        super ( id, false, false, false, false );
        this.zoomFactor = 0.1;
        this.zoomFactor = 0.1;
    }
    public double getZoomFactor() {
        return this.zoomFactor;
    }
    public void setZoomFactor ( final double zoomFactor ) {
        this.zoomFactor = zoomFactor;
    }
    @Override
    public void handleScroll ( final ChartCanvas canvas, final ScrollEvent e ) {
        final JFreeChart chart = canvas.getChart();
        final Plot plot = chart.getPlot();
        if ( plot instanceof Zoomable ) {
            final Zoomable zoomable = ( Zoomable ) plot;
            this.handleZoomable ( canvas, zoomable, e );
        } else if ( plot instanceof PiePlot ) {
            final PiePlot pp = ( PiePlot ) plot;
            pp.handleMouseWheelRotation ( ( int ) e.getDeltaY() );
        }
    }
    private void handleZoomable ( final ChartCanvas canvas, final Zoomable zoomable, final ScrollEvent e ) {
        final ChartRenderingInfo info = canvas.getRenderingInfo();
        final PlotRenderingInfo pinfo = info.getPlotInfo();
        final Point2D p = new Point2D.Double ( e.getX(), e.getY() );
        if ( pinfo.getDataArea().contains ( p ) ) {
            final Plot plot = ( Plot ) zoomable;
            final boolean notifyState = plot.isNotify();
            plot.setNotify ( false );
            final int clicks = ( int ) e.getDeltaY();
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
