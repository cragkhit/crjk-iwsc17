package org.jfree.chart.renderer.xy;
import java.awt.geom.Line2D;
import java.io.ObjectOutputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectInputStream;
import org.jfree.chart.HashUtilities;
import org.jfree.chart.entity.EntityCollection;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.BasicStroke;
import java.awt.GradientPaint;
import org.jfree.util.ShapeUtilities;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.axis.ValueAxis;
import java.awt.Paint;
import org.jfree.chart.labels.XYSeriesLabelGenerator;
import org.jfree.data.general.Dataset;
import org.jfree.chart.LegendItem;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.plot.XYPlot;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.chart.util.ParamChecks;
import org.jfree.ui.StandardGradientPaintTransformer;
import java.awt.geom.GeneralPath;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.ui.GradientPaintTransformer;
import java.awt.Shape;
import org.jfree.util.PublicCloneable;
public class XYAreaRenderer extends AbstractXYItemRenderer implements XYItemRenderer, PublicCloneable {
    private static final long serialVersionUID = -4481971353973876747L;
    public static final int SHAPES = 1;
    public static final int LINES = 2;
    public static final int SHAPES_AND_LINES = 3;
    public static final int AREA = 4;
    public static final int AREA_AND_SHAPES = 5;
    private boolean plotShapes;
    private boolean plotLines;
    private boolean plotArea;
    private boolean showOutline;
    private transient Shape legendArea;
    private boolean useFillPaint;
    private GradientPaintTransformer gradientTransformer;
    public XYAreaRenderer() {
        this ( 4 );
    }
    public XYAreaRenderer ( final int type ) {
        this ( type, null, null );
    }
    public XYAreaRenderer ( final int type, final XYToolTipGenerator toolTipGenerator, final XYURLGenerator urlGenerator ) {
        this.setBaseToolTipGenerator ( toolTipGenerator );
        this.setURLGenerator ( urlGenerator );
        if ( type == 1 ) {
            this.plotShapes = true;
        }
        if ( type == 2 ) {
            this.plotLines = true;
        }
        if ( type == 3 ) {
            this.plotShapes = true;
            this.plotLines = true;
        }
        if ( type == 4 ) {
            this.plotArea = true;
        }
        if ( type == 5 ) {
            this.plotArea = true;
            this.plotShapes = true;
        }
        this.showOutline = false;
        final GeneralPath area = new GeneralPath();
        area.moveTo ( 0.0f, -4.0f );
        area.lineTo ( 3.0f, -2.0f );
        area.lineTo ( 4.0f, 4.0f );
        area.lineTo ( -4.0f, 4.0f );
        area.lineTo ( -3.0f, -2.0f );
        area.closePath();
        this.legendArea = area;
        this.useFillPaint = false;
        this.gradientTransformer = ( GradientPaintTransformer ) new StandardGradientPaintTransformer();
    }
    public boolean getPlotShapes() {
        return this.plotShapes;
    }
    public boolean getPlotLines() {
        return this.plotLines;
    }
    public boolean getPlotArea() {
        return this.plotArea;
    }
    public boolean isOutline() {
        return this.showOutline;
    }
    public void setOutline ( final boolean show ) {
        this.showOutline = show;
        this.fireChangeEvent();
    }
    public Shape getLegendArea() {
        return this.legendArea;
    }
    public void setLegendArea ( final Shape area ) {
        ParamChecks.nullNotPermitted ( area, "area" );
        this.legendArea = area;
        this.fireChangeEvent();
    }
    public boolean getUseFillPaint() {
        return this.useFillPaint;
    }
    public void setUseFillPaint ( final boolean use ) {
        this.useFillPaint = use;
        this.fireChangeEvent();
    }
    public GradientPaintTransformer getGradientTransformer() {
        return this.gradientTransformer;
    }
    public void setGradientTransformer ( final GradientPaintTransformer transformer ) {
        ParamChecks.nullNotPermitted ( transformer, "transformer" );
        this.gradientTransformer = transformer;
        this.fireChangeEvent();
    }
    @Override
    public XYItemRendererState initialise ( final Graphics2D g2, final Rectangle2D dataArea, final XYPlot plot, final XYDataset data, final PlotRenderingInfo info ) {
        final XYAreaRendererState state = new XYAreaRendererState ( info );
        state.setProcessVisibleItemsOnly ( false );
        return state;
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
        final XYAreaRendererState areaState = ( XYAreaRendererState ) state;
        final double x1 = dataset.getXValue ( series, item );
        double y1 = dataset.getYValue ( series, item );
        if ( Double.isNaN ( y1 ) ) {
            y1 = 0.0;
        }
        final double transX1 = domainAxis.valueToJava2D ( x1, dataArea, plot.getDomainAxisEdge() );
        final double transY1 = rangeAxis.valueToJava2D ( y1, dataArea, plot.getRangeAxisEdge() );
        final int itemCount = dataset.getItemCount ( series );
        final double x2 = dataset.getXValue ( series, Math.max ( item - 1, 0 ) );
        double y2 = dataset.getYValue ( series, Math.max ( item - 1, 0 ) );
        if ( Double.isNaN ( y2 ) ) {
            y2 = 0.0;
        }
        final double transX2 = domainAxis.valueToJava2D ( x2, dataArea, plot.getDomainAxisEdge() );
        final double transY2 = rangeAxis.valueToJava2D ( y2, dataArea, plot.getRangeAxisEdge() );
        final double x3 = dataset.getXValue ( series, Math.min ( item + 1, itemCount - 1 ) );
        double y3 = dataset.getYValue ( series, Math.min ( item + 1, itemCount - 1 ) );
        if ( Double.isNaN ( y3 ) ) {
            y3 = 0.0;
        }
        final double transX3 = domainAxis.valueToJava2D ( x3, dataArea, plot.getDomainAxisEdge() );
        final double transY3 = rangeAxis.valueToJava2D ( y3, dataArea, plot.getRangeAxisEdge() );
        final double transZero = rangeAxis.valueToJava2D ( 0.0, dataArea, plot.getRangeAxisEdge() );
        if ( item == 0 ) {
            areaState.area = new GeneralPath();
            final double zero = rangeAxis.valueToJava2D ( 0.0, dataArea, plot.getRangeAxisEdge() );
            if ( plot.getOrientation().isVertical() ) {
                AbstractXYItemRenderer.moveTo ( areaState.area, transX1, zero );
            } else if ( plot.getOrientation().isHorizontal() ) {
                AbstractXYItemRenderer.moveTo ( areaState.area, zero, transX1 );
            }
        }
        if ( plot.getOrientation().isVertical() ) {
            AbstractXYItemRenderer.lineTo ( areaState.area, transX1, transY1 );
        } else if ( plot.getOrientation().isHorizontal() ) {
            AbstractXYItemRenderer.lineTo ( areaState.area, transY1, transX1 );
        }
        final PlotOrientation orientation = plot.getOrientation();
        Paint paint = this.getItemPaint ( series, item );
        final Stroke stroke = this.getItemStroke ( series, item );
        g2.setPaint ( paint );
        g2.setStroke ( stroke );
        if ( this.getPlotShapes() ) {
            Shape shape = this.getItemShape ( series, item );
            if ( orientation == PlotOrientation.VERTICAL ) {
                shape = ShapeUtilities.createTranslatedShape ( shape, transX1, transY1 );
            } else if ( orientation == PlotOrientation.HORIZONTAL ) {
                shape = ShapeUtilities.createTranslatedShape ( shape, transY1, transX1 );
            }
            g2.draw ( shape );
        }
        if ( this.getPlotLines() && item > 0 ) {
            if ( plot.getOrientation() == PlotOrientation.VERTICAL ) {
                areaState.line.setLine ( transX2, transY2, transX1, transY1 );
            } else if ( plot.getOrientation() == PlotOrientation.HORIZONTAL ) {
                areaState.line.setLine ( transY2, transX2, transY1, transX1 );
            }
            g2.draw ( areaState.line );
        }
        if ( this.getPlotArea() && item > 0 && item == itemCount - 1 ) {
            if ( orientation == PlotOrientation.VERTICAL ) {
                AbstractXYItemRenderer.lineTo ( areaState.area, transX1, transZero );
                areaState.area.closePath();
            } else if ( orientation == PlotOrientation.HORIZONTAL ) {
                AbstractXYItemRenderer.lineTo ( areaState.area, transZero, transX1 );
                areaState.area.closePath();
            }
            if ( this.useFillPaint ) {
                paint = this.lookupSeriesFillPaint ( series );
            }
            if ( paint instanceof GradientPaint ) {
                final GradientPaint gp = ( GradientPaint ) paint;
                final GradientPaint adjGP = this.gradientTransformer.transform ( gp, ( Shape ) dataArea );
                g2.setPaint ( adjGP );
            }
            g2.fill ( areaState.area );
            if ( this.isOutline() ) {
                Shape area = areaState.area;
                final Stroke outlineStroke = this.lookupSeriesOutlineStroke ( series );
                if ( outlineStroke instanceof BasicStroke ) {
                    final BasicStroke bs = ( BasicStroke ) outlineStroke;
                    if ( bs.getDashArray() != null ) {
                        final Area poly = new Area ( areaState.area );
                        final Area clip = new Area ( new Rectangle2D.Double ( dataArea.getX() - 5.0, dataArea.getY() - 5.0, dataArea.getWidth() + 10.0, dataArea.getHeight() + 10.0 ) );
                        poly.intersect ( clip );
                        area = poly;
                    }
                }
                g2.setStroke ( outlineStroke );
                g2.setPaint ( this.lookupSeriesOutlinePaint ( series ) );
                g2.draw ( area );
            }
        }
        final int domainAxisIndex = plot.getDomainAxisIndex ( domainAxis );
        final int rangeAxisIndex = plot.getRangeAxisIndex ( rangeAxis );
        this.updateCrosshairValues ( crosshairState, x1, y1, domainAxisIndex, rangeAxisIndex, transX1, transY1, orientation );
        final EntityCollection entities = state.getEntityCollection();
        if ( entities != null ) {
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
            final Area dataAreaHotspot = new Area ( hotspot );
            dataAreaHotspot.intersect ( new Area ( dataArea ) );
            if ( !dataAreaHotspot.isEmpty() ) {
                this.addEntity ( entities, dataAreaHotspot, dataset, series, item, 0.0, 0.0 );
            }
        }
    }
    public Object clone() throws CloneNotSupportedException {
        final XYAreaRenderer clone = ( XYAreaRenderer ) super.clone();
        clone.legendArea = ShapeUtilities.clone ( this.legendArea );
        return clone;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof XYAreaRenderer ) ) {
            return false;
        }
        final XYAreaRenderer that = ( XYAreaRenderer ) obj;
        return this.plotArea == that.plotArea && this.plotLines == that.plotLines && this.plotShapes == that.plotShapes && this.showOutline == that.showOutline && this.useFillPaint == that.useFillPaint && this.gradientTransformer.equals ( that.gradientTransformer ) && ShapeUtilities.equal ( this.legendArea, that.legendArea );
    }
    public int hashCode() {
        int result = super.hashCode();
        result = HashUtilities.hashCode ( result, this.plotArea );
        result = HashUtilities.hashCode ( result, this.plotLines );
        result = HashUtilities.hashCode ( result, this.plotShapes );
        result = HashUtilities.hashCode ( result, this.useFillPaint );
        return result;
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.legendArea = SerialUtilities.readShape ( stream );
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeShape ( this.legendArea, stream );
    }
    static class XYAreaRendererState extends XYItemRendererState {
        public GeneralPath area;
        public Line2D line;
        public XYAreaRendererState ( final PlotRenderingInfo info ) {
            super ( info );
            this.area = new GeneralPath();
            this.line = new Line2D.Double();
        }
    }
}
