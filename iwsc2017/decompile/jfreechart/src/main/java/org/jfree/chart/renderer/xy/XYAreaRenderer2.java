package org.jfree.chart.renderer.xy;
import java.io.ObjectOutputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectInputStream;
import org.jfree.util.ShapeUtilities;
import org.jfree.chart.entity.EntityCollection;
import java.awt.Stroke;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import java.awt.geom.Area;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.awt.Paint;
import org.jfree.chart.labels.XYSeriesLabelGenerator;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.Dataset;
import org.jfree.chart.LegendItem;
import org.jfree.chart.util.ParamChecks;
import java.awt.geom.GeneralPath;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import java.awt.Shape;
import org.jfree.util.PublicCloneable;
public class XYAreaRenderer2 extends AbstractXYItemRenderer implements XYItemRenderer, PublicCloneable {
    private static final long serialVersionUID = -7378069681579984133L;
    private boolean showOutline;
    private transient Shape legendArea;
    public XYAreaRenderer2() {
        this ( null, null );
    }
    public XYAreaRenderer2 ( final XYToolTipGenerator labelGenerator, final XYURLGenerator urlGenerator ) {
        this.showOutline = false;
        this.setBaseToolTipGenerator ( labelGenerator );
        this.setURLGenerator ( urlGenerator );
        final GeneralPath area = new GeneralPath();
        area.moveTo ( 0.0f, -4.0f );
        area.lineTo ( 3.0f, -2.0f );
        area.lineTo ( 4.0f, 4.0f );
        area.lineTo ( -4.0f, 4.0f );
        area.lineTo ( -3.0f, -2.0f );
        area.closePath();
        this.legendArea = area;
    }
    public boolean isOutline() {
        return this.showOutline;
    }
    public void setOutline ( final boolean show ) {
        this.showOutline = show;
        this.fireChangeEvent();
    }
    public boolean getPlotLines() {
        return false;
    }
    public Shape getLegendArea() {
        return this.legendArea;
    }
    public void setLegendArea ( final Shape area ) {
        ParamChecks.nullNotPermitted ( area, "area" );
        this.legendArea = area;
        this.fireChangeEvent();
    }
    @Override
    public LegendItem getLegendItem ( final int datasetIndex, final int series ) {
        LegendItem result = null;
        final XYPlot xyplot = this.getPlot();
        if ( xyplot != null ) {
            final XYDataset dataset = xyplot.getDataset ( datasetIndex );
            if ( dataset != null ) {
                final XYSeriesLabelGenerator lg = this.getLegendItemLabelGenerator();
                final String description;
                final String label = description = lg.generateLabel ( dataset, series );
                String toolTipText = null;
                if ( this.getLegendItemToolTipGenerator() != null ) {
                    toolTipText = this.getLegendItemToolTipGenerator().generateLabel ( dataset, series );
                }
                String urlText = null;
                if ( this.getLegendItemURLGenerator() != null ) {
                    urlText = this.getLegendItemURLGenerator().generateLabel ( dataset, series );
                }
                final Paint paint = this.lookupSeriesPaint ( series );
                result = new LegendItem ( label, description, toolTipText, urlText, this.legendArea, paint );
                result.setLabelFont ( this.lookupLegendTextFont ( series ) );
                final Paint labelPaint = this.lookupLegendTextPaint ( series );
                if ( labelPaint != null ) {
                    result.setLabelPaint ( labelPaint );
                }
                result.setDataset ( dataset );
                result.setDatasetIndex ( datasetIndex );
                result.setSeriesKey ( dataset.getSeriesKey ( series ) );
                result.setSeriesIndex ( series );
            }
        }
        return result;
    }
    @Override
    public void drawItem ( final Graphics2D g2, final XYItemRendererState state, final Rectangle2D dataArea, final PlotRenderingInfo info, final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis, final XYDataset dataset, final int series, final int item, final CrosshairState crosshairState, final int pass ) {
        if ( !this.getItemVisible ( series, item ) ) {
            return;
        }
        final double x1 = dataset.getXValue ( series, item );
        double y1 = dataset.getYValue ( series, item );
        if ( Double.isNaN ( y1 ) ) {
            y1 = 0.0;
        }
        final double transX1 = domainAxis.valueToJava2D ( x1, dataArea, plot.getDomainAxisEdge() );
        final double transY1 = rangeAxis.valueToJava2D ( y1, dataArea, plot.getRangeAxisEdge() );
        final double x2 = dataset.getXValue ( series, Math.max ( item - 1, 0 ) );
        double y2 = dataset.getYValue ( series, Math.max ( item - 1, 0 ) );
        if ( Double.isNaN ( y2 ) ) {
            y2 = 0.0;
        }
        final double transX2 = domainAxis.valueToJava2D ( x2, dataArea, plot.getDomainAxisEdge() );
        final double transY2 = rangeAxis.valueToJava2D ( y2, dataArea, plot.getRangeAxisEdge() );
        final int itemCount = dataset.getItemCount ( series );
        final double x3 = dataset.getXValue ( series, Math.min ( item + 1, itemCount - 1 ) );
        double y3 = dataset.getYValue ( series, Math.min ( item + 1, itemCount - 1 ) );
        if ( Double.isNaN ( y3 ) ) {
            y3 = 0.0;
        }
        final double transX3 = domainAxis.valueToJava2D ( x3, dataArea, plot.getDomainAxisEdge() );
        final double transY3 = rangeAxis.valueToJava2D ( y3, dataArea, plot.getRangeAxisEdge() );
        final double transZero = rangeAxis.valueToJava2D ( 0.0, dataArea, plot.getRangeAxisEdge() );
        final GeneralPath hotspot = new GeneralPath();
        if ( plot.getOrientation() == PlotOrientation.HORIZONTAL ) {
            AbstractXYItemRenderer.moveTo ( hotspot, transZero, ( transX2 + transX1 ) / 2.0 );
            AbstractXYItemRenderer.lineTo ( hotspot, ( transY2 + transY1 ) / 2.0, ( transX2 + transX1 ) / 2.0 );
            AbstractXYItemRenderer.lineTo ( hotspot, transY1, transX1 );
            AbstractXYItemRenderer.lineTo ( hotspot, ( transY1 + transY3 ) / 2.0, ( transX1 + transX3 ) / 2.0 );
            AbstractXYItemRenderer.lineTo ( hotspot, transZero, ( transX1 + transX3 ) / 2.0 );
        } else {
            AbstractXYItemRenderer.moveTo ( hotspot, ( transX2 + transX1 ) / 2.0, transZero );
            AbstractXYItemRenderer.lineTo ( hotspot, ( transX2 + transX1 ) / 2.0, ( transY2 + transY1 ) / 2.0 );
            AbstractXYItemRenderer.lineTo ( hotspot, transX1, transY1 );
            AbstractXYItemRenderer.lineTo ( hotspot, ( transX1 + transX3 ) / 2.0, ( transY1 + transY3 ) / 2.0 );
            AbstractXYItemRenderer.lineTo ( hotspot, ( transX1 + transX3 ) / 2.0, transZero );
        }
        hotspot.closePath();
        final PlotOrientation orientation = plot.getOrientation();
        final Paint paint = this.getItemPaint ( series, item );
        final Stroke stroke = this.getItemStroke ( series, item );
        g2.setPaint ( paint );
        g2.setStroke ( stroke );
        g2.fill ( hotspot );
        if ( this.isOutline() ) {
            g2.setStroke ( this.lookupSeriesOutlineStroke ( series ) );
            g2.setPaint ( this.lookupSeriesOutlinePaint ( series ) );
            g2.draw ( hotspot );
        }
        final int domainAxisIndex = plot.getDomainAxisIndex ( domainAxis );
        final int rangeAxisIndex = plot.getRangeAxisIndex ( rangeAxis );
        this.updateCrosshairValues ( crosshairState, x1, y1, domainAxisIndex, rangeAxisIndex, transX1, transY1, orientation );
        if ( state.getInfo() != null ) {
            final EntityCollection entities = state.getEntityCollection();
            if ( entities != null ) {
                final Area dataAreaHotspot = new Area ( hotspot );
                dataAreaHotspot.intersect ( new Area ( dataArea ) );
                if ( !dataAreaHotspot.isEmpty() ) {
                    String tip = null;
                    final XYToolTipGenerator generator = this.getToolTipGenerator ( series, item );
                    if ( generator != null ) {
                        tip = generator.generateToolTip ( dataset, series, item );
                    }
                    String url = null;
                    if ( this.getURLGenerator() != null ) {
                        url = this.getURLGenerator().generateURL ( dataset, series, item );
                    }
                    final XYItemEntity entity = new XYItemEntity ( dataAreaHotspot, dataset, series, item, tip, url );
                    entities.add ( entity );
                }
            }
        }
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof XYAreaRenderer2 ) ) {
            return false;
        }
        final XYAreaRenderer2 that = ( XYAreaRenderer2 ) obj;
        return this.showOutline == that.showOutline && ShapeUtilities.equal ( this.legendArea, that.legendArea ) && super.equals ( obj );
    }
    public Object clone() throws CloneNotSupportedException {
        final XYAreaRenderer2 clone = ( XYAreaRenderer2 ) super.clone();
        clone.legendArea = ShapeUtilities.clone ( this.legendArea );
        return clone;
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.legendArea = SerialUtilities.readShape ( stream );
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeShape ( this.legendArea, stream );
    }
}
