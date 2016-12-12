package org.jfree.chart.renderer.xy;
import org.jfree.chart.plot.PlotRenderingInfo;
import java.awt.geom.Line2D;
import java.awt.geom.GeneralPath;
static class XYAreaRendererState extends XYItemRendererState {
    public GeneralPath area;
    public Line2D line;
    public XYAreaRendererState ( final PlotRenderingInfo info ) {
        super ( info );
        this.area = new GeneralPath();
        this.line = new Line2D.Double();
    }
}
