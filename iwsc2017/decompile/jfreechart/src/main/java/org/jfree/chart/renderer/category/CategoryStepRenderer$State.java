package org.jfree.chart.renderer.category;
import org.jfree.chart.plot.PlotRenderingInfo;
import java.awt.geom.Line2D;
protected static class State extends CategoryItemRendererState {
    public Line2D line;
    public State ( final PlotRenderingInfo info ) {
        super ( info );
        this.line = new Line2D.Double();
    }
}
