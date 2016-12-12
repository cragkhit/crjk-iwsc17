package org.jfree.chart.renderer;
import java.io.ObjectOutputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectInputStream;
import org.jfree.util.PublicCloneable;
import org.jfree.util.ObjectUtilities;
import java.awt.Stroke;
import org.jfree.data.general.Dataset;
import org.jfree.chart.LegendItem;
import java.util.Iterator;
import org.jfree.text.TextUtilities;
import org.jfree.chart.axis.NumberTick;
import java.util.List;
import java.awt.Paint;
import java.awt.geom.PathIterator;
import java.awt.Point;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.util.ShapeUtilities;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import org.jfree.chart.plot.PlotRenderingInfo;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import java.awt.geom.Ellipse2D;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.util.BooleanUtilities;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.util.ParamChecks;
import java.awt.geom.Line2D;
import java.awt.AlphaComposite;
import org.jfree.chart.labels.XYSeriesLabelGenerator;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.util.ObjectList;
import java.awt.Shape;
import java.awt.Composite;
import org.jfree.util.BooleanList;
import org.jfree.chart.plot.PolarPlot;
public class DefaultPolarItemRenderer extends AbstractRenderer implements PolarItemRenderer {
    private PolarPlot plot;
    private BooleanList seriesFilled;
    private boolean drawOutlineWhenFilled;
    private transient Composite fillComposite;
    private boolean useFillPaint;
    private transient Shape legendLine;
    private boolean shapesVisible;
    private boolean connectFirstAndLastPoint;
    private ObjectList toolTipGeneratorList;
    private XYToolTipGenerator baseToolTipGenerator;
    private XYURLGenerator urlGenerator;
    private XYSeriesLabelGenerator legendItemToolTipGenerator;
    private XYSeriesLabelGenerator legendItemURLGenerator;
    public DefaultPolarItemRenderer() {
        this.seriesFilled = new BooleanList();
        this.drawOutlineWhenFilled = true;
        this.fillComposite = AlphaComposite.getInstance ( 3, 0.3f );
        this.useFillPaint = false;
        this.legendLine = new Line2D.Double ( -7.0, 0.0, 7.0, 0.0 );
        this.shapesVisible = true;
        this.connectFirstAndLastPoint = true;
        this.toolTipGeneratorList = new ObjectList();
        this.urlGenerator = null;
        this.legendItemToolTipGenerator = null;
        this.legendItemURLGenerator = null;
    }
    @Override
    public void setPlot ( final PolarPlot plot ) {
        this.plot = plot;
    }
    @Override
    public PolarPlot getPlot() {
        return this.plot;
    }
    public boolean getDrawOutlineWhenFilled() {
        return this.drawOutlineWhenFilled;
    }
    public void setDrawOutlineWhenFilled ( final boolean drawOutlineWhenFilled ) {
        this.drawOutlineWhenFilled = drawOutlineWhenFilled;
        this.fireChangeEvent();
    }
    public Composite getFillComposite() {
        return this.fillComposite;
    }
    public void setFillComposite ( final Composite composite ) {
        ParamChecks.nullNotPermitted ( composite, "composite" );
        this.fillComposite = composite;
        this.fireChangeEvent();
    }
    public boolean getShapesVisible() {
        return this.shapesVisible;
    }
    public void setShapesVisible ( final boolean visible ) {
        this.shapesVisible = visible;
        this.fireChangeEvent();
    }
    public boolean getConnectFirstAndLastPoint() {
        return this.connectFirstAndLastPoint;
    }
    public void setConnectFirstAndLastPoint ( final boolean connect ) {
        this.connectFirstAndLastPoint = connect;
        this.fireChangeEvent();
    }
    @Override
    public DrawingSupplier getDrawingSupplier() {
        DrawingSupplier result = null;
        final PolarPlot p = this.getPlot();
        if ( p != null ) {
            result = p.getDrawingSupplier();
        }
        return result;
    }
    public boolean isSeriesFilled ( final int series ) {
        boolean result = false;
        final Boolean b = this.seriesFilled.getBoolean ( series );
        if ( b != null ) {
            result = b;
        }
        return result;
    }
    public void setSeriesFilled ( final int series, final boolean filled ) {
        this.seriesFilled.setBoolean ( series, BooleanUtilities.valueOf ( filled ) );
    }
    public boolean getUseFillPaint() {
        return this.useFillPaint;
    }
    public void setUseFillPaint ( final boolean flag ) {
        this.useFillPaint = flag;
        this.fireChangeEvent();
    }
    public Shape getLegendLine() {
        return this.legendLine;
    }
    public void setLegendLine ( final Shape line ) {
        ParamChecks.nullNotPermitted ( line, "line" );
        this.legendLine = line;
        this.fireChangeEvent();
    }
    protected void addEntity ( final EntityCollection entities, final Shape area, final XYDataset dataset, final int series, final int item, final double entityX, final double entityY ) {
        if ( !this.getItemCreateEntity ( series, item ) ) {
            return;
        }
        Shape hotspot = area;
        if ( hotspot == null ) {
            final double r = this.getDefaultEntityRadius();
            final double w = r * 2.0;
            if ( this.getPlot().getOrientation() == PlotOrientation.VERTICAL ) {
                hotspot = new Ellipse2D.Double ( entityX - r, entityY - r, w, w );
            } else {
                hotspot = new Ellipse2D.Double ( entityY - r, entityX - r, w, w );
            }
        }
        String tip = null;
        final XYToolTipGenerator generator = this.getToolTipGenerator ( series, item );
        if ( generator != null ) {
            tip = generator.generateToolTip ( dataset, series, item );
        }
        String url = null;
        if ( this.getURLGenerator() != null ) {
            url = this.getURLGenerator().generateURL ( dataset, series, item );
        }
        final XYItemEntity entity = new XYItemEntity ( hotspot, dataset, series, item, tip, url );
        entities.add ( entity );
    }
    @Override
    public void drawSeries ( final Graphics2D g2, final Rectangle2D dataArea, final PlotRenderingInfo info, final PolarPlot plot, final XYDataset dataset, final int seriesIndex ) {
        final int numPoints = dataset.getItemCount ( seriesIndex );
        if ( numPoints == 0 ) {
            return;
        }
        GeneralPath poly = null;
        final ValueAxis axis = plot.getAxisForDataset ( plot.indexOf ( dataset ) );
        for ( int i = 0; i < numPoints; ++i ) {
            final double theta = dataset.getXValue ( seriesIndex, i );
            final double radius = dataset.getYValue ( seriesIndex, i );
            final Point p = plot.translateToJava2D ( theta, radius, axis, dataArea );
            if ( poly == null ) {
                poly = new GeneralPath();
                poly.moveTo ( p.x, p.y );
            } else {
                poly.lineTo ( p.x, p.y );
            }
        }
        assert poly != null;
        if ( this.getConnectFirstAndLastPoint() ) {
            poly.closePath();
        }
        g2.setPaint ( this.lookupSeriesPaint ( seriesIndex ) );
        g2.setStroke ( this.lookupSeriesStroke ( seriesIndex ) );
        if ( this.isSeriesFilled ( seriesIndex ) ) {
            final Composite savedComposite = g2.getComposite();
            g2.setComposite ( this.fillComposite );
            g2.fill ( poly );
            g2.setComposite ( savedComposite );
            if ( this.drawOutlineWhenFilled ) {
                g2.setPaint ( this.lookupSeriesOutlinePaint ( seriesIndex ) );
                g2.draw ( poly );
            }
        } else {
            g2.draw ( poly );
        }
        if ( this.shapesVisible ) {
            EntityCollection entities = null;
            if ( info != null ) {
                entities = info.getOwner().getEntityCollection();
            }
            final PathIterator pi = poly.getPathIterator ( null );
            int j = 0;
            while ( !pi.isDone() ) {
                final float[] coords = new float[6];
                final int segType = pi.currentSegment ( coords );
                pi.next();
                if ( segType != 1 && segType != 0 ) {
                    continue;
                }
                final int x = Math.round ( coords[0] );
                final int y = Math.round ( coords[1] );
                final Shape shape = ShapeUtilities.createTranslatedShape ( this.getItemShape ( seriesIndex, j++ ), ( double ) x, ( double ) y );
                Paint paint;
                if ( this.useFillPaint ) {
                    paint = this.lookupSeriesFillPaint ( seriesIndex );
                } else {
                    paint = this.lookupSeriesPaint ( seriesIndex );
                }
                g2.setPaint ( paint );
                g2.fill ( shape );
                if ( this.isSeriesFilled ( seriesIndex ) && this.drawOutlineWhenFilled ) {
                    g2.setPaint ( this.lookupSeriesOutlinePaint ( seriesIndex ) );
                    g2.setStroke ( this.lookupSeriesOutlineStroke ( seriesIndex ) );
                    g2.draw ( shape );
                }
                if ( entities == null || !AbstractXYItemRenderer.isPointInRect ( dataArea, x, y ) ) {
                    continue;
                }
                this.addEntity ( entities, shape, dataset, seriesIndex, j - 1, x, y );
            }
        }
    }
    @Override
    public void drawAngularGridLines ( final Graphics2D g2, final PolarPlot plot, final List ticks, final Rectangle2D dataArea ) {
        g2.setFont ( plot.getAngleLabelFont() );
        g2.setStroke ( plot.getAngleGridlineStroke() );
        g2.setPaint ( plot.getAngleGridlinePaint() );
        final ValueAxis axis = plot.getAxis();
        double outerValue;
        double centerValue;
        if ( axis.isInverted() ) {
            outerValue = axis.getLowerBound();
            centerValue = axis.getUpperBound();
        } else {
            outerValue = axis.getUpperBound();
            centerValue = axis.getLowerBound();
        }
        final Point center = plot.translateToJava2D ( 0.0, centerValue, axis, dataArea );
        for ( final NumberTick tick : ticks ) {
            final double tickVal = tick.getNumber().doubleValue();
            final Point p = plot.translateToJava2D ( tickVal, outerValue, axis, dataArea );
            g2.setPaint ( plot.getAngleGridlinePaint() );
            g2.drawLine ( center.x, center.y, p.x, p.y );
            if ( plot.isAngleLabelsVisible() ) {
                final int x = p.x;
                final int y = p.y;
                g2.setPaint ( plot.getAngleLabelPaint() );
                TextUtilities.drawAlignedString ( tick.getText(), g2, ( float ) x, ( float ) y, tick.getTextAnchor() );
            }
        }
    }
    @Override
    public void drawRadialGridLines ( final Graphics2D g2, final PolarPlot plot, final ValueAxis radialAxis, final List ticks, final Rectangle2D dataArea ) {
        ParamChecks.nullNotPermitted ( radialAxis, "radialAxis" );
        g2.setFont ( radialAxis.getTickLabelFont() );
        g2.setPaint ( plot.getRadiusGridlinePaint() );
        g2.setStroke ( plot.getRadiusGridlineStroke() );
        double centerValue;
        if ( radialAxis.isInverted() ) {
            centerValue = radialAxis.getUpperBound();
        } else {
            centerValue = radialAxis.getLowerBound();
        }
        final Point center = plot.translateToJava2D ( 0.0, centerValue, radialAxis, dataArea );
        for ( final NumberTick tick : ticks ) {
            final double angleDegrees = plot.isCounterClockwise() ? plot.getAngleOffset() : ( -plot.getAngleOffset() );
            final Point p = plot.translateToJava2D ( angleDegrees, tick.getNumber().doubleValue(), radialAxis, dataArea );
            final int r = p.x - center.x;
            final int upperLeftX = center.x - r;
            final int upperLeftY = center.y - r;
            final int d = 2 * r;
            final Ellipse2D ring = new Ellipse2D.Double ( upperLeftX, upperLeftY, d, d );
            g2.setPaint ( plot.getRadiusGridlinePaint() );
            g2.draw ( ring );
        }
    }
    @Override
    public LegendItem getLegendItem ( final int series ) {
        final PolarPlot plot = this.getPlot();
        if ( plot == null ) {
            return null;
        }
        final XYDataset dataset = plot.getDataset ( plot.getIndexOf ( this ) );
        if ( dataset == null ) {
            return null;
        }
        String toolTipText = null;
        if ( this.getLegendItemToolTipGenerator() != null ) {
            toolTipText = this.getLegendItemToolTipGenerator().generateLabel ( dataset, series );
        }
        String urlText = null;
        if ( this.getLegendItemURLGenerator() != null ) {
            urlText = this.getLegendItemURLGenerator().generateLabel ( dataset, series );
        }
        final Comparable seriesKey = dataset.getSeriesKey ( series );
        final String description;
        final String label = description = seriesKey.toString();
        final Shape shape = this.lookupSeriesShape ( series );
        Paint paint;
        if ( this.useFillPaint ) {
            paint = this.lookupSeriesFillPaint ( series );
        } else {
            paint = this.lookupSeriesPaint ( series );
        }
        final Stroke stroke = this.lookupSeriesStroke ( series );
        final Paint outlinePaint = this.lookupSeriesOutlinePaint ( series );
        final Stroke outlineStroke = this.lookupSeriesOutlineStroke ( series );
        final boolean shapeOutlined = this.isSeriesFilled ( series ) && this.drawOutlineWhenFilled;
        final LegendItem result = new LegendItem ( label, description, toolTipText, urlText, this.getShapesVisible(), shape, true, paint, shapeOutlined, outlinePaint, outlineStroke, true, this.legendLine, stroke, paint );
        result.setToolTipText ( toolTipText );
        result.setURLText ( urlText );
        result.setDataset ( dataset );
        result.setSeriesKey ( seriesKey );
        result.setSeriesIndex ( series );
        return result;
    }
    @Override
    public XYToolTipGenerator getToolTipGenerator ( final int series, final int item ) {
        XYToolTipGenerator generator = ( XYToolTipGenerator ) this.toolTipGeneratorList.get ( series );
        if ( generator == null ) {
            generator = this.baseToolTipGenerator;
        }
        return generator;
    }
    @Override
    public XYToolTipGenerator getSeriesToolTipGenerator ( final int series ) {
        return ( XYToolTipGenerator ) this.toolTipGeneratorList.get ( series );
    }
    @Override
    public void setSeriesToolTipGenerator ( final int series, final XYToolTipGenerator generator ) {
        this.toolTipGeneratorList.set ( series, ( Object ) generator );
        this.fireChangeEvent();
    }
    @Override
    public XYToolTipGenerator getBaseToolTipGenerator() {
        return this.baseToolTipGenerator;
    }
    @Override
    public void setBaseToolTipGenerator ( final XYToolTipGenerator generator ) {
        this.baseToolTipGenerator = generator;
        this.fireChangeEvent();
    }
    @Override
    public XYURLGenerator getURLGenerator() {
        return this.urlGenerator;
    }
    @Override
    public void setURLGenerator ( final XYURLGenerator urlGenerator ) {
        this.urlGenerator = urlGenerator;
        this.fireChangeEvent();
    }
    public XYSeriesLabelGenerator getLegendItemToolTipGenerator() {
        return this.legendItemToolTipGenerator;
    }
    public void setLegendItemToolTipGenerator ( final XYSeriesLabelGenerator generator ) {
        this.legendItemToolTipGenerator = generator;
        this.fireChangeEvent();
    }
    public XYSeriesLabelGenerator getLegendItemURLGenerator() {
        return this.legendItemURLGenerator;
    }
    public void setLegendItemURLGenerator ( final XYSeriesLabelGenerator generator ) {
        this.legendItemURLGenerator = generator;
        this.fireChangeEvent();
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == null ) {
            return false;
        }
        if ( ! ( obj instanceof DefaultPolarItemRenderer ) ) {
            return false;
        }
        final DefaultPolarItemRenderer that = ( DefaultPolarItemRenderer ) obj;
        return this.seriesFilled.equals ( ( Object ) that.seriesFilled ) && this.drawOutlineWhenFilled == that.drawOutlineWhenFilled && ObjectUtilities.equal ( ( Object ) this.fillComposite, ( Object ) that.fillComposite ) && this.useFillPaint == that.useFillPaint && ShapeUtilities.equal ( this.legendLine, that.legendLine ) && this.shapesVisible == that.shapesVisible && this.connectFirstAndLastPoint == that.connectFirstAndLastPoint && this.toolTipGeneratorList.equals ( ( Object ) that.toolTipGeneratorList ) && ObjectUtilities.equal ( ( Object ) this.baseToolTipGenerator, ( Object ) that.baseToolTipGenerator ) && ObjectUtilities.equal ( ( Object ) this.urlGenerator, ( Object ) that.urlGenerator ) && ObjectUtilities.equal ( ( Object ) this.legendItemToolTipGenerator, ( Object ) that.legendItemToolTipGenerator ) && ObjectUtilities.equal ( ( Object ) this.legendItemURLGenerator, ( Object ) that.legendItemURLGenerator ) && super.equals ( obj );
    }
    public Object clone() throws CloneNotSupportedException {
        final DefaultPolarItemRenderer clone = ( DefaultPolarItemRenderer ) super.clone();
        if ( this.legendLine != null ) {
            clone.legendLine = ShapeUtilities.clone ( this.legendLine );
        }
        clone.seriesFilled = ( BooleanList ) this.seriesFilled.clone();
        clone.toolTipGeneratorList = ( ObjectList ) this.toolTipGeneratorList.clone();
        if ( clone.baseToolTipGenerator instanceof PublicCloneable ) {
            clone.baseToolTipGenerator = ( XYToolTipGenerator ) ObjectUtilities.clone ( ( Object ) this.baseToolTipGenerator );
        }
        if ( clone.urlGenerator instanceof PublicCloneable ) {
            clone.urlGenerator = ( XYURLGenerator ) ObjectUtilities.clone ( ( Object ) this.urlGenerator );
        }
        if ( clone.legendItemToolTipGenerator instanceof PublicCloneable ) {
            clone.legendItemToolTipGenerator = ( XYSeriesLabelGenerator ) ObjectUtilities.clone ( ( Object ) this.legendItemToolTipGenerator );
        }
        if ( clone.legendItemURLGenerator instanceof PublicCloneable ) {
            clone.legendItemURLGenerator = ( XYSeriesLabelGenerator ) ObjectUtilities.clone ( ( Object ) this.legendItemURLGenerator );
        }
        return clone;
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.legendLine = SerialUtilities.readShape ( stream );
        this.fillComposite = SerialUtilities.readComposite ( stream );
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeShape ( this.legendLine, stream );
        SerialUtilities.writeComposite ( this.fillComposite, stream );
    }
}
