package org.jfree.chart;
import java.awt.geom.Point2D;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Zoomable;
import java.awt.event.MouseWheelEvent;
import java.io.Serializable;
import java.awt.event.MouseWheelListener;
class MouseWheelHandler implements MouseWheelListener, Serializable {
    private ChartPanel chartPanel;
    double zoomFactor;
    public MouseWheelHandler ( final ChartPanel chartPanel ) {
        this.chartPanel = chartPanel;
        this.zoomFactor = 0.1;
        this.chartPanel.addMouseWheelListener ( this );
    }
    public double getZoomFactor() {
        return this.zoomFactor;
    }
    public void setZoomFactor ( final double zoomFactor ) {
        this.zoomFactor = zoomFactor;
    }
    @Override
    public void mouseWheelMoved ( final MouseWheelEvent e ) {
        final JFreeChart chart = this.chartPanel.getChart();
        if ( chart == null ) {
            return;
        }
        final Plot plot = chart.getPlot();
        if ( plot instanceof Zoomable ) {
            final Zoomable zoomable = ( Zoomable ) plot;
            this.handleZoomable ( zoomable, e );
        } else if ( plot instanceof PiePlot ) {
            final PiePlot pp = ( PiePlot ) plot;
            pp.handleMouseWheelRotation ( e.getWheelRotation() );
        }
    }
    private void handleZoomable ( final Zoomable zoomable, final MouseWheelEvent e ) {
        final ChartRenderingInfo info = this.chartPanel.getChartRenderingInfo();
        final PlotRenderingInfo pinfo = info.getPlotInfo();
        final Point2D p = this.chartPanel.translateScreenToJava2D ( e.getPoint() );
        if ( !pinfo.getDataArea().contains ( p ) ) {
            return;
        }
        final Plot plot = ( Plot ) zoomable;
        final boolean notifyState = plot.isNotify();
        plot.setNotify ( false );
        final int clicks = e.getWheelRotation();
        double zf = 1.0 + this.zoomFactor;
        if ( clicks < 0 ) {
            zf = 1.0 / zf;
        }
        if ( this.chartPanel.isDomainZoomable() ) {
            zoomable.zoomDomainAxes ( zf, pinfo, p, true );
        }
        if ( this.chartPanel.isRangeZoomable() ) {
            zoomable.zoomRangeAxes ( zf, pinfo, p, true );
        }
        plot.setNotify ( notifyState );
    }
}
