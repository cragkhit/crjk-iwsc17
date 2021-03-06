package org.jfree.chart.renderer.xy;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.axis.ValueAxis;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.ui.Layer;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.labels.ItemLabelPosition;
import java.awt.Font;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.labels.XYSeriesLabelGenerator;
import org.jfree.chart.LegendItem;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Paint;
import org.jfree.chart.event.RendererChangeListener;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.LegendItemSource;
public interface XYItemRenderer extends LegendItemSource {
    XYPlot getPlot();
    void setPlot ( XYPlot p0 );
    int getPassCount();
    Range findDomainBounds ( XYDataset p0 );
    Range findRangeBounds ( XYDataset p0 );
    void addChangeListener ( RendererChangeListener p0 );
    void removeChangeListener ( RendererChangeListener p0 );
    boolean getItemVisible ( int p0, int p1 );
    boolean isSeriesVisible ( int p0 );
    Boolean getSeriesVisible ( int p0 );
    void setSeriesVisible ( int p0, Boolean p1 );
    void setSeriesVisible ( int p0, Boolean p1, boolean p2 );
    boolean getBaseSeriesVisible();
    void setBaseSeriesVisible ( boolean p0 );
    void setBaseSeriesVisible ( boolean p0, boolean p1 );
    boolean isSeriesVisibleInLegend ( int p0 );
    Boolean getSeriesVisibleInLegend ( int p0 );
    void setSeriesVisibleInLegend ( int p0, Boolean p1 );
    void setSeriesVisibleInLegend ( int p0, Boolean p1, boolean p2 );
    boolean getBaseSeriesVisibleInLegend();
    void setBaseSeriesVisibleInLegend ( boolean p0 );
    void setBaseSeriesVisibleInLegend ( boolean p0, boolean p1 );
    Paint getItemPaint ( int p0, int p1 );
    Paint getSeriesPaint ( int p0 );
    void setSeriesPaint ( int p0, Paint p1 );
    Paint getBasePaint();
    void setBasePaint ( Paint p0 );
    Paint getItemOutlinePaint ( int p0, int p1 );
    Paint getSeriesOutlinePaint ( int p0 );
    void setSeriesOutlinePaint ( int p0, Paint p1 );
    Paint getBaseOutlinePaint();
    void setBaseOutlinePaint ( Paint p0 );
    Stroke getItemStroke ( int p0, int p1 );
    Stroke getSeriesStroke ( int p0 );
    void setSeriesStroke ( int p0, Stroke p1 );
    Stroke getBaseStroke();
    void setBaseStroke ( Stroke p0 );
    Stroke getItemOutlineStroke ( int p0, int p1 );
    Stroke getSeriesOutlineStroke ( int p0 );
    void setSeriesOutlineStroke ( int p0, Stroke p1 );
    Stroke getBaseOutlineStroke();
    void setBaseOutlineStroke ( Stroke p0 );
    Shape getItemShape ( int p0, int p1 );
    Shape getSeriesShape ( int p0 );
    void setSeriesShape ( int p0, Shape p1 );
    Shape getBaseShape();
    void setBaseShape ( Shape p0 );
    LegendItem getLegendItem ( int p0, int p1 );
    XYSeriesLabelGenerator getLegendItemLabelGenerator();
    void setLegendItemLabelGenerator ( XYSeriesLabelGenerator p0 );
    XYToolTipGenerator getToolTipGenerator ( int p0, int p1 );
    XYToolTipGenerator getSeriesToolTipGenerator ( int p0 );
    void setSeriesToolTipGenerator ( int p0, XYToolTipGenerator p1 );
    XYToolTipGenerator getBaseToolTipGenerator();
    void setBaseToolTipGenerator ( XYToolTipGenerator p0 );
    XYURLGenerator getURLGenerator();
    void setURLGenerator ( XYURLGenerator p0 );
    boolean isItemLabelVisible ( int p0, int p1 );
    boolean isSeriesItemLabelsVisible ( int p0 );
    void setSeriesItemLabelsVisible ( int p0, boolean p1 );
    void setSeriesItemLabelsVisible ( int p0, Boolean p1 );
    void setSeriesItemLabelsVisible ( int p0, Boolean p1, boolean p2 );
    Boolean getBaseItemLabelsVisible();
    void setBaseItemLabelsVisible ( boolean p0 );
    void setBaseItemLabelsVisible ( Boolean p0 );
    void setBaseItemLabelsVisible ( Boolean p0, boolean p1 );
    XYItemLabelGenerator getItemLabelGenerator ( int p0, int p1 );
    XYItemLabelGenerator getSeriesItemLabelGenerator ( int p0 );
    void setSeriesItemLabelGenerator ( int p0, XYItemLabelGenerator p1 );
    XYItemLabelGenerator getBaseItemLabelGenerator();
    void setBaseItemLabelGenerator ( XYItemLabelGenerator p0 );
    Font getItemLabelFont ( int p0, int p1 );
    Font getSeriesItemLabelFont ( int p0 );
    void setSeriesItemLabelFont ( int p0, Font p1 );
    Font getBaseItemLabelFont();
    void setBaseItemLabelFont ( Font p0 );
    Paint getItemLabelPaint ( int p0, int p1 );
    Paint getSeriesItemLabelPaint ( int p0 );
    void setSeriesItemLabelPaint ( int p0, Paint p1 );
    Paint getBaseItemLabelPaint();
    void setBaseItemLabelPaint ( Paint p0 );
    ItemLabelPosition getPositiveItemLabelPosition ( int p0, int p1 );
    ItemLabelPosition getSeriesPositiveItemLabelPosition ( int p0 );
    void setSeriesPositiveItemLabelPosition ( int p0, ItemLabelPosition p1 );
    void setSeriesPositiveItemLabelPosition ( int p0, ItemLabelPosition p1, boolean p2 );
    ItemLabelPosition getBasePositiveItemLabelPosition();
    void setBasePositiveItemLabelPosition ( ItemLabelPosition p0 );
    void setBasePositiveItemLabelPosition ( ItemLabelPosition p0, boolean p1 );
    ItemLabelPosition getNegativeItemLabelPosition ( int p0, int p1 );
    ItemLabelPosition getSeriesNegativeItemLabelPosition ( int p0 );
    void setSeriesNegativeItemLabelPosition ( int p0, ItemLabelPosition p1 );
    void setSeriesNegativeItemLabelPosition ( int p0, ItemLabelPosition p1, boolean p2 );
    ItemLabelPosition getBaseNegativeItemLabelPosition();
    void setBaseNegativeItemLabelPosition ( ItemLabelPosition p0 );
    void setBaseNegativeItemLabelPosition ( ItemLabelPosition p0, boolean p1 );
    void addAnnotation ( XYAnnotation p0 );
    void addAnnotation ( XYAnnotation p0, Layer p1 );
    boolean removeAnnotation ( XYAnnotation p0 );
    void removeAnnotations();
    void drawAnnotations ( Graphics2D p0, Rectangle2D p1, ValueAxis p2, ValueAxis p3, Layer p4, PlotRenderingInfo p5 );
    XYItemRendererState initialise ( Graphics2D p0, Rectangle2D p1, XYPlot p2, XYDataset p3, PlotRenderingInfo p4 );
    void drawItem ( Graphics2D p0, XYItemRendererState p1, Rectangle2D p2, PlotRenderingInfo p3, XYPlot p4, ValueAxis p5, ValueAxis p6, XYDataset p7, int p8, int p9, CrosshairState p10, int p11 );
    void fillDomainGridBand ( Graphics2D p0, XYPlot p1, ValueAxis p2, Rectangle2D p3, double p4, double p5 );
    void fillRangeGridBand ( Graphics2D p0, XYPlot p1, ValueAxis p2, Rectangle2D p3, double p4, double p5 );
    void drawDomainGridLine ( Graphics2D p0, XYPlot p1, ValueAxis p2, Rectangle2D p3, double p4 );
    void drawRangeLine ( Graphics2D p0, XYPlot p1, ValueAxis p2, Rectangle2D p3, double p4, Paint p5, Stroke p6 );
    void drawDomainMarker ( Graphics2D p0, XYPlot p1, ValueAxis p2, Marker p3, Rectangle2D p4 );
    void drawRangeMarker ( Graphics2D p0, XYPlot p1, ValueAxis p2, Marker p3, Rectangle2D p4 );
    Boolean getSeriesVisible();
    void setSeriesVisible ( Boolean p0 );
    void setSeriesVisible ( Boolean p0, boolean p1 );
    Boolean getSeriesVisibleInLegend();
    void setSeriesVisibleInLegend ( Boolean p0 );
    void setSeriesVisibleInLegend ( Boolean p0, boolean p1 );
    void setPaint ( Paint p0 );
    void setOutlinePaint ( Paint p0 );
    void setStroke ( Stroke p0 );
    void setOutlineStroke ( Stroke p0 );
    void setShape ( Shape p0 );
    void setItemLabelsVisible ( boolean p0 );
    void setItemLabelsVisible ( Boolean p0 );
    void setItemLabelsVisible ( Boolean p0, boolean p1 );
    void setItemLabelGenerator ( XYItemLabelGenerator p0 );
    void setToolTipGenerator ( XYToolTipGenerator p0 );
    Font getItemLabelFont();
    void setItemLabelFont ( Font p0 );
    Paint getItemLabelPaint();
    void setItemLabelPaint ( Paint p0 );
    ItemLabelPosition getPositiveItemLabelPosition();
    void setPositiveItemLabelPosition ( ItemLabelPosition p0 );
    void setPositiveItemLabelPosition ( ItemLabelPosition p0, boolean p1 );
    ItemLabelPosition getNegativeItemLabelPosition();
    void setNegativeItemLabelPosition ( ItemLabelPosition p0 );
    void setNegativeItemLabelPosition ( ItemLabelPosition p0, boolean p1 );
}
