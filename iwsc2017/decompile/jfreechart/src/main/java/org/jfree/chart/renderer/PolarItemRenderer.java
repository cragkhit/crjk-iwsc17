package org.jfree.chart.renderer;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.event.RendererChangeListener;
import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.ValueAxis;
import java.util.List;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.plot.PolarPlot;
import org.jfree.chart.plot.PlotRenderingInfo;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
public interface PolarItemRenderer {
    void drawSeries ( Graphics2D p0, Rectangle2D p1, PlotRenderingInfo p2, PolarPlot p3, XYDataset p4, int p5 );
    void drawAngularGridLines ( Graphics2D p0, PolarPlot p1, List p2, Rectangle2D p3 );
    void drawRadialGridLines ( Graphics2D p0, PolarPlot p1, ValueAxis p2, List p3, Rectangle2D p4 );
    LegendItem getLegendItem ( int p0 );
    PolarPlot getPlot();
    void setPlot ( PolarPlot p0 );
    void addChangeListener ( RendererChangeListener p0 );
    void removeChangeListener ( RendererChangeListener p0 );
    XYToolTipGenerator getToolTipGenerator ( int p0, int p1 );
    XYToolTipGenerator getSeriesToolTipGenerator ( int p0 );
    void setSeriesToolTipGenerator ( int p0, XYToolTipGenerator p1 );
    XYToolTipGenerator getBaseToolTipGenerator();
    void setBaseToolTipGenerator ( XYToolTipGenerator p0 );
    XYURLGenerator getURLGenerator();
    void setURLGenerator ( XYURLGenerator p0 );
}
