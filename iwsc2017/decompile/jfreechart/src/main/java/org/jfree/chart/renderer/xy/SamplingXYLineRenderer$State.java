package org.jfree.chart.renderer.xy;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.plot.PlotRenderingInfo;
import java.awt.geom.GeneralPath;
public static class State extends XYItemRendererState {
    GeneralPath seriesPath;
    GeneralPath intervalPath;
    double dX;
    double lastX;
    double openY;
    double highY;
    double lowY;
    double closeY;
    boolean lastPointGood;
    public State ( final PlotRenderingInfo info ) {
        super ( info );
        this.dX = 1.0;
        this.openY = 0.0;
        this.highY = 0.0;
        this.lowY = 0.0;
        this.closeY = 0.0;
    }
    @Override
    public void startSeriesPass ( final XYDataset dataset, final int series, final int firstItem, final int lastItem, final int pass, final int passCount ) {
        this.seriesPath.reset();
        this.intervalPath.reset();
        this.lastPointGood = false;
        super.startSeriesPass ( dataset, series, firstItem, lastItem, pass, passCount );
    }
}
