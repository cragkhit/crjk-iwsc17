package org.jfree.chart.fx.overlay;
import java.util.Iterator;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.JFreeChart;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.XYPlot;
import java.awt.Shape;
import org.jfree.chart.fx.ChartCanvas;
import java.awt.Graphics2D;
import org.jfree.chart.panel.CrosshairOverlay;
public class CrosshairOverlayFX extends CrosshairOverlay implements OverlayFX {
    @Override
    public void paintOverlay ( final Graphics2D g2, final ChartCanvas chartCanvas ) {
        final Shape savedClip = g2.getClip();
        final Rectangle2D dataArea = chartCanvas.getRenderingInfo().getPlotInfo().getDataArea();
        g2.clip ( dataArea );
        final JFreeChart chart = chartCanvas.getChart();
        final XYPlot plot = ( XYPlot ) chart.getPlot();
        final ValueAxis xAxis = plot.getDomainAxis();
        final RectangleEdge xAxisEdge = plot.getDomainAxisEdge();
        for ( final Crosshair ch : this.getDomainCrosshairs() ) {
            if ( ch.isVisible() ) {
                final double x = ch.getValue();
                final double xx = xAxis.valueToJava2D ( x, dataArea, xAxisEdge );
                if ( plot.getOrientation() == PlotOrientation.VERTICAL ) {
                    this.drawVerticalCrosshair ( g2, dataArea, xx, ch );
                } else {
                    this.drawHorizontalCrosshair ( g2, dataArea, xx, ch );
                }
            }
        }
        final ValueAxis yAxis = plot.getRangeAxis();
        final RectangleEdge yAxisEdge = plot.getRangeAxisEdge();
        for ( final Crosshair ch2 : this.getRangeCrosshairs() ) {
            if ( ch2.isVisible() ) {
                final double y = ch2.getValue();
                final double yy = yAxis.valueToJava2D ( y, dataArea, yAxisEdge );
                if ( plot.getOrientation() == PlotOrientation.VERTICAL ) {
                    this.drawHorizontalCrosshair ( g2, dataArea, yy, ch2 );
                } else {
                    this.drawVerticalCrosshair ( g2, dataArea, yy, ch2 );
                }
            }
        }
        g2.setClip ( savedClip );
    }
}
