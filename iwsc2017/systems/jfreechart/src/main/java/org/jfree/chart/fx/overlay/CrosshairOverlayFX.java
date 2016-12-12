package org.jfree.chart.fx.overlay;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.fx.ChartCanvas;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.RectangleEdge;
public class CrosshairOverlayFX extends CrosshairOverlay implements OverlayFX {
    @Override
    public void paintOverlay ( Graphics2D g2, ChartCanvas chartCanvas ) {
        Shape savedClip = g2.getClip();
        Rectangle2D dataArea = chartCanvas.getRenderingInfo().getPlotInfo().getDataArea();
        g2.clip ( dataArea );
        JFreeChart chart = chartCanvas.getChart();
        XYPlot plot = ( XYPlot ) chart.getPlot();
        ValueAxis xAxis = plot.getDomainAxis();
        RectangleEdge xAxisEdge = plot.getDomainAxisEdge();
        Iterator iterator = getDomainCrosshairs().iterator();
        while ( iterator.hasNext() ) {
            Crosshair ch = ( Crosshair ) iterator.next();
            if ( ch.isVisible() ) {
                double x = ch.getValue();
                double xx = xAxis.valueToJava2D ( x, dataArea, xAxisEdge );
                if ( plot.getOrientation() == PlotOrientation.VERTICAL ) {
                    drawVerticalCrosshair ( g2, dataArea, xx, ch );
                } else {
                    drawHorizontalCrosshair ( g2, dataArea, xx, ch );
                }
            }
        }
        ValueAxis yAxis = plot.getRangeAxis();
        RectangleEdge yAxisEdge = plot.getRangeAxisEdge();
        iterator = getRangeCrosshairs().iterator();
        while ( iterator.hasNext() ) {
            Crosshair ch = ( Crosshair ) iterator.next();
            if ( ch.isVisible() ) {
                double y = ch.getValue();
                double yy = yAxis.valueToJava2D ( y, dataArea, yAxisEdge );
                if ( plot.getOrientation() == PlotOrientation.VERTICAL ) {
                    drawHorizontalCrosshair ( g2, dataArea, yy, ch );
                } else {
                    drawVerticalCrosshair ( g2, dataArea, yy, ch );
                }
            }
        }
        g2.setClip ( savedClip );
    }
}
