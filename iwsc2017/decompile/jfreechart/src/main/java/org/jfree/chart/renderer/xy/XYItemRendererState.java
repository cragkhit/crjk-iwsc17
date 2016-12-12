package org.jfree.chart.renderer.xy;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.plot.PlotRenderingInfo;
import java.awt.geom.Line2D;
import org.jfree.chart.renderer.RendererState;
public class XYItemRendererState extends RendererState {
    private int firstItemIndex;
    private int lastItemIndex;
    public Line2D workingLine;
    private boolean processVisibleItemsOnly;
    public XYItemRendererState ( final PlotRenderingInfo info ) {
        super ( info );
        this.workingLine = new Line2D.Double();
        this.processVisibleItemsOnly = true;
    }
    public boolean getProcessVisibleItemsOnly() {
        return this.processVisibleItemsOnly;
    }
    public void setProcessVisibleItemsOnly ( final boolean flag ) {
        this.processVisibleItemsOnly = flag;
    }
    public int getFirstItemIndex() {
        return this.firstItemIndex;
    }
    public int getLastItemIndex() {
        return this.lastItemIndex;
    }
    public void startSeriesPass ( final XYDataset dataset, final int series, final int firstItem, final int lastItem, final int pass, final int passCount ) {
        this.firstItemIndex = firstItem;
        this.lastItemIndex = lastItem;
    }
    public void endSeriesPass ( final XYDataset dataset, final int series, final int firstItem, final int lastItem, final int pass, final int passCount ) {
    }
}
