package org.jfree.chart.renderer.xy;
import org.jfree.chart.plot.PlotRenderingInfo;
import java.util.Stack;
import java.awt.geom.Line2D;
import java.awt.Polygon;
static class StackedXYAreaRendererState extends XYItemRendererState {
    private Polygon seriesArea;
    private Line2D line;
    private Stack lastSeriesPoints;
    private Stack currentSeriesPoints;
    public StackedXYAreaRendererState ( final PlotRenderingInfo info ) {
        super ( info );
        this.seriesArea = null;
        this.line = new Line2D.Double();
        this.lastSeriesPoints = new Stack();
        this.currentSeriesPoints = new Stack();
    }
    public Polygon getSeriesArea() {
        return this.seriesArea;
    }
    public void setSeriesArea ( final Polygon area ) {
        this.seriesArea = area;
    }
    public Line2D getLine() {
        return this.line;
    }
    public Stack getCurrentSeriesPoints() {
        return this.currentSeriesPoints;
    }
    public void setCurrentSeriesPoints ( final Stack points ) {
        this.currentSeriesPoints = points;
    }
    public Stack getLastSeriesPoints() {
        return this.lastSeriesPoints;
    }
    public void setLastSeriesPoints ( final Stack points ) {
        this.lastSeriesPoints = points;
    }
}
