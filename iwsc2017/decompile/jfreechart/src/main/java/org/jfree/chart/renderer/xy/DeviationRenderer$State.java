package org.jfree.chart.renderer.xy;
import java.util.ArrayList;
import org.jfree.chart.plot.PlotRenderingInfo;
import java.util.List;
public static class State extends XYLineAndShapeRenderer.State {
    public List upperCoordinates;
    public List lowerCoordinates;
    public State ( final PlotRenderingInfo info ) {
        super ( info );
        this.lowerCoordinates = new ArrayList();
        this.upperCoordinates = new ArrayList();
    }
}
