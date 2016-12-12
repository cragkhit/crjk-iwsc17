package org.jfree.chart.renderer.xy;
import org.jfree.chart.plot.PlotRenderingInfo;
import java.awt.geom.GeneralPath;
public static class State extends XYItemRendererState {
    public GeneralPath seriesPath;
    private int seriesIndex;
    private boolean lastPointGood;
    public State ( final PlotRenderingInfo info ) {
        super ( info );
    }
    public boolean isLastPointGood() {
        return this.lastPointGood;
    }
    public void setLastPointGood ( final boolean good ) {
        this.lastPointGood = good;
    }
    public int getSeriesIndex() {
        return this.seriesIndex;
    }
    public void setSeriesIndex ( final int index ) {
        this.seriesIndex = index;
    }
}
