package org.jfree.chart.renderer.xy;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.plot.PlotRenderingInfo;
import java.awt.geom.GeneralPath;
public static class State extends XYItemRendererState {
    public GeneralPath seriesPath;
    private boolean lastPointGood;
    public State ( final PlotRenderingInfo info ) {
        super ( info );
        this.seriesPath = new GeneralPath();
    }
    public boolean isLastPointGood() {
        return this.lastPointGood;
    }
    public void setLastPointGood ( final boolean good ) {
        this.lastPointGood = good;
    }
    @Override
    public void startSeriesPass ( final XYDataset dataset, final int series, final int firstItem, final int lastItem, final int pass, final int passCount ) {
        this.seriesPath.reset();
        this.lastPointGood = false;
        super.startSeriesPass ( dataset, series, firstItem, lastItem, pass, passCount );
    }
}
