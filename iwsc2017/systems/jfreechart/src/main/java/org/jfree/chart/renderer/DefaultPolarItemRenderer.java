package org.jfree.chart.renderer;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.List;
import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.XYSeriesLabelGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PolarPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.xy.XYDataset;
import org.jfree.io.SerialUtilities;
import org.jfree.text.TextUtilities;
import org.jfree.util.BooleanList;
import org.jfree.util.BooleanUtilities;
import org.jfree.util.ObjectList;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PublicCloneable;
import org.jfree.util.ShapeUtilities;
public class DefaultPolarItemRenderer extends AbstractRenderer
    implements PolarItemRenderer {
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
        this.fillComposite = AlphaComposite.getInstance (
                                 AlphaComposite.SRC_OVER, 0.3f );
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
    public void setPlot ( PolarPlot plot ) {
        this.plot = plot;
    }
    @Override
    public PolarPlot getPlot() {
        return this.plot;
    }
    public boolean getDrawOutlineWhenFilled() {
        return this.drawOutlineWhenFilled;
    }
    public void setDrawOutlineWhenFilled ( boolean drawOutlineWhenFilled ) {
        this.drawOutlineWhenFilled = drawOutlineWhenFilled;
        fireChangeEvent();
    }
    public Composite getFillComposite() {
        return this.fillComposite;
    }
    public void setFillComposite ( Composite composite ) {
        ParamChecks.nullNotPermitted ( composite, "composite" );
        this.fillComposite = composite;
        fireChangeEvent();
    }
    public boolean getShapesVisible() {
        return this.shapesVisible;
    }
    public void setShapesVisible ( boolean visible ) {
        this.shapesVisible = visible;
        fireChangeEvent();
    }
    public boolean getConnectFirstAndLastPoint() {
        return this.connectFirstAndLastPoint;
    }
    public void setConnectFirstAndLastPoint ( boolean connect ) {
        this.connectFirstAndLastPoint = connect;
        fireChangeEvent();
    }
    @Override
    public DrawingSupplier getDrawingSupplier() {
        DrawingSupplier result = null;
        PolarPlot p = getPlot();
        if ( p != null ) {
            result = p.getDrawingSupplier();
        }
        return result;
    }
    public boolean isSeriesFilled ( int series ) {
        boolean result = false;
        Boolean b = this.seriesFilled.getBoolean ( series );
        if ( b != null ) {
            result = b.booleanValue();
        }
        return result;
    }
    public void setSeriesFilled ( int series, boolean filled ) {
        this.seriesFilled.setBoolean ( series, BooleanUtilities.valueOf ( filled ) );
    }
    public boolean getUseFillPaint() {
        return this.useFillPaint;
    }
    public void setUseFillPaint ( boolean flag ) {
        this.useFillPaint = flag;
        fireChangeEvent();
    }
    public Shape getLegendLine() {
        return this.legendLine;
    }
    public void setLegendLine ( Shape line ) {
        ParamChecks.nullNotPermitted ( line, "line" );
        this.legendLine = line;
        fireChangeEvent();
    }
    protected void addEntity ( EntityCollection entities, Shape area,
                               XYDataset dataset, int series, int item,
                               double entityX, double entityY ) {
        if ( !getItemCreateEntity ( series, item ) ) {
            return;
        }
        Shape hotspot = area;
        if ( hotspot == null ) {
            double r = getDefaultEntityRadius();
            double w = r * 2;
            if ( getPlot().getOrientation() == PlotOrientation.VERTICAL ) {
                hotspot = new Ellipse2D.Double ( entityX - r, entityY - r, w, w );
            } else {
                hotspot = new Ellipse2D.Double ( entityY - r, entityX - r, w, w );
            }
        }
        String tip = null;
        XYToolTipGenerator generator = getToolTipGenerator ( series, item );
        if ( generator != null ) {
            tip = generator.generateToolTip ( dataset, series, item );
        }
        String url = null;
        if ( getURLGenerator() != null ) {
            url = getURLGenerator().generateURL ( dataset, series, item );
        }
        XYItemEntity entity = new XYItemEntity ( hotspot, dataset, series, item,
                tip, url );
        entities.add ( entity );
    }
    @Override
    public void drawSeries ( Graphics2D g2, Rectangle2D dataArea,
                             PlotRenderingInfo info, PolarPlot plot, XYDataset dataset,
                             int seriesIndex ) {
        final int numPoints = dataset.getItemCount ( seriesIndex );
        if ( numPoints == 0 ) {
            return;
        }
        GeneralPath poly = null;
        ValueAxis axis = plot.getAxisForDataset ( plot.indexOf ( dataset ) );
        for ( int i = 0; i < numPoints; i++ ) {
            double theta = dataset.getXValue ( seriesIndex, i );
            double radius = dataset.getYValue ( seriesIndex, i );
            Point p = plot.translateToJava2D ( theta, radius, axis, dataArea );
            if ( poly == null ) {
                poly = new GeneralPath();
                poly.moveTo ( p.x, p.y );
            } else {
                poly.lineTo ( p.x, p.y );
            }
        }
        assert poly != null;
        if ( getConnectFirstAndLastPoint() ) {
            poly.closePath();
        }
        g2.setPaint ( lookupSeriesPaint ( seriesIndex ) );
        g2.setStroke ( lookupSeriesStroke ( seriesIndex ) );
        if ( isSeriesFilled ( seriesIndex ) ) {
            Composite savedComposite = g2.getComposite();
            g2.setComposite ( this.fillComposite );
            g2.fill ( poly );
            g2.setComposite ( savedComposite );
            if ( this.drawOutlineWhenFilled ) {
                g2.setPaint ( lookupSeriesOutlinePaint ( seriesIndex ) );
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
            PathIterator pi = poly.getPathIterator ( null );
            int i = 0;
            while ( !pi.isDone() ) {
                final float[] coords = new float[6];
                final int segType = pi.currentSegment ( coords );
                pi.next();
                if ( segType != PathIterator.SEG_LINETO &&
                        segType != PathIterator.SEG_MOVETO ) {
                    continue;
                }
                final int x = Math.round ( coords[0] );
                final int y = Math.round ( coords[1] );
                final Shape shape = ShapeUtilities.createTranslatedShape (
                                        getItemShape ( seriesIndex, i++ ), x,  y );
                Paint paint;
                if ( useFillPaint ) {
                    paint = lookupSeriesFillPaint ( seriesIndex );
                } else {
                    paint = lookupSeriesPaint ( seriesIndex );
                }
                g2.setPaint ( paint );
                g2.fill ( shape );
                if ( isSeriesFilled ( seriesIndex ) && this.drawOutlineWhenFilled ) {
                    g2.setPaint ( lookupSeriesOutlinePaint ( seriesIndex ) );
                    g2.setStroke ( lookupSeriesOutlineStroke ( seriesIndex ) );
                    g2.draw ( shape );
                }
                if ( entities != null &&
                        AbstractXYItemRenderer.isPointInRect ( dataArea, x, y ) ) {
                    addEntity ( entities, shape, dataset, seriesIndex, i - 1, x, y );
                }
            }
        }
    }
    @Override
    public void drawAngularGridLines ( Graphics2D g2, PolarPlot plot,
                                       List ticks, Rectangle2D dataArea ) {
        g2.setFont ( plot.getAngleLabelFont() );
        g2.setStroke ( plot.getAngleGridlineStroke() );
        g2.setPaint ( plot.getAngleGridlinePaint() );
        ValueAxis axis = plot.getAxis();
        double centerValue, outerValue;
        if ( axis.isInverted() ) {
            outerValue = axis.getLowerBound();
            centerValue = axis.getUpperBound();
        } else {
            outerValue = axis.getUpperBound();
            centerValue = axis.getLowerBound();
        }
        Point center = plot.translateToJava2D ( 0, centerValue, axis, dataArea );
        Iterator iterator = ticks.iterator();
        while ( iterator.hasNext() ) {
            NumberTick tick = ( NumberTick ) iterator.next();
            double tickVal = tick.getNumber().doubleValue();
            Point p = plot.translateToJava2D ( tickVal, outerValue, axis,
                                               dataArea );
            g2.setPaint ( plot.getAngleGridlinePaint() );
            g2.drawLine ( center.x, center.y, p.x, p.y );
            if ( plot.isAngleLabelsVisible() ) {
                int x = p.x;
                int y = p.y;
                g2.setPaint ( plot.getAngleLabelPaint() );
                TextUtilities.drawAlignedString ( tick.getText(), g2, x, y,
                                                  tick.getTextAnchor() );
            }
        }
    }
    @Override
    public void drawRadialGridLines ( Graphics2D g2, PolarPlot plot,
                                      ValueAxis radialAxis, List ticks, Rectangle2D dataArea ) {
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
        Point center = plot.translateToJava2D ( 0, centerValue, radialAxis, dataArea );
        Iterator iterator = ticks.iterator();
        while ( iterator.hasNext() ) {
            NumberTick tick = ( NumberTick ) iterator.next();
            double angleDegrees = plot.isCounterClockwise()
                                  ? plot.getAngleOffset() : -plot.getAngleOffset();
            Point p = plot.translateToJava2D ( angleDegrees,
                                               tick.getNumber().doubleValue(), radialAxis, dataArea );
            int r = p.x - center.x;
            int upperLeftX = center.x - r;
            int upperLeftY = center.y - r;
            int d = 2 * r;
            Ellipse2D ring = new Ellipse2D.Double ( upperLeftX, upperLeftY, d, d );
            g2.setPaint ( plot.getRadiusGridlinePaint() );
            g2.draw ( ring );
        }
    }
    @Override
    public LegendItem getLegendItem ( int series ) {
        LegendItem result;
        PolarPlot plot = getPlot();
        if ( plot == null ) {
            return null;
        }
        XYDataset dataset = plot.getDataset ( plot.getIndexOf ( this ) );
        if ( dataset == null ) {
            return null;
        }
        String toolTipText = null;
        if ( getLegendItemToolTipGenerator() != null ) {
            toolTipText = getLegendItemToolTipGenerator().generateLabel (
                              dataset, series );
        }
        String urlText = null;
        if ( getLegendItemURLGenerator() != null ) {
            urlText = getLegendItemURLGenerator().generateLabel ( dataset,
                      series );
        }
        Comparable seriesKey = dataset.getSeriesKey ( series );
        String label = seriesKey.toString();
        String description = label;
        Shape shape = lookupSeriesShape ( series );
        Paint paint;
        if ( this.useFillPaint ) {
            paint = lookupSeriesFillPaint ( series );
        } else {
            paint = lookupSeriesPaint ( series );
        }
        Stroke stroke = lookupSeriesStroke ( series );
        Paint outlinePaint = lookupSeriesOutlinePaint ( series );
        Stroke outlineStroke = lookupSeriesOutlineStroke ( series );
        boolean shapeOutlined = isSeriesFilled ( series )
                                && this.drawOutlineWhenFilled;
        result = new LegendItem ( label, description, toolTipText, urlText,
                                  getShapesVisible(), shape,   true, paint,
                                  shapeOutlined, outlinePaint, outlineStroke,
                                  true, this.legendLine, stroke, paint );
        result.setToolTipText ( toolTipText );
        result.setURLText ( urlText );
        result.setDataset ( dataset );
        result.setSeriesKey ( seriesKey );
        result.setSeriesIndex ( series );
        return result;
    }
    @Override
    public XYToolTipGenerator getToolTipGenerator ( int series, int item ) {
        XYToolTipGenerator generator
            = ( XYToolTipGenerator ) this.toolTipGeneratorList.get ( series );
        if ( generator == null ) {
            generator = this.baseToolTipGenerator;
        }
        return generator;
    }
    @Override
    public XYToolTipGenerator getSeriesToolTipGenerator ( int series ) {
        return ( XYToolTipGenerator ) this.toolTipGeneratorList.get ( series );
    }
    @Override
    public void setSeriesToolTipGenerator ( int series,
                                            XYToolTipGenerator generator ) {
        this.toolTipGeneratorList.set ( series, generator );
        fireChangeEvent();
    }
    @Override
    public XYToolTipGenerator getBaseToolTipGenerator() {
        return this.baseToolTipGenerator;
    }
    @Override
    public void setBaseToolTipGenerator ( XYToolTipGenerator generator ) {
        this.baseToolTipGenerator = generator;
        fireChangeEvent();
    }
    @Override
    public XYURLGenerator getURLGenerator() {
        return this.urlGenerator;
    }
    @Override
    public void setURLGenerator ( XYURLGenerator urlGenerator ) {
        this.urlGenerator = urlGenerator;
        fireChangeEvent();
    }
    public XYSeriesLabelGenerator getLegendItemToolTipGenerator() {
        return this.legendItemToolTipGenerator;
    }
    public void setLegendItemToolTipGenerator (
        XYSeriesLabelGenerator generator ) {
        this.legendItemToolTipGenerator = generator;
        fireChangeEvent();
    }
    public XYSeriesLabelGenerator getLegendItemURLGenerator() {
        return this.legendItemURLGenerator;
    }
    public void setLegendItemURLGenerator ( XYSeriesLabelGenerator generator ) {
        this.legendItemURLGenerator = generator;
        fireChangeEvent();
    }
    @Override
    public boolean equals ( Object obj ) {
        if ( obj == null ) {
            return false;
        }
        if ( ! ( obj instanceof DefaultPolarItemRenderer ) ) {
            return false;
        }
        DefaultPolarItemRenderer that = ( DefaultPolarItemRenderer ) obj;
        if ( !this.seriesFilled.equals ( that.seriesFilled ) ) {
            return false;
        }
        if ( this.drawOutlineWhenFilled != that.drawOutlineWhenFilled ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.fillComposite, that.fillComposite ) ) {
            return false;
        }
        if ( this.useFillPaint != that.useFillPaint ) {
            return false;
        }
        if ( !ShapeUtilities.equal ( this.legendLine, that.legendLine ) ) {
            return false;
        }
        if ( this.shapesVisible != that.shapesVisible ) {
            return false;
        }
        if ( this.connectFirstAndLastPoint != that.connectFirstAndLastPoint ) {
            return false;
        }
        if ( !this.toolTipGeneratorList.equals ( that.toolTipGeneratorList ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.baseToolTipGenerator,
                                      that.baseToolTipGenerator ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.urlGenerator, that.urlGenerator ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.legendItemToolTipGenerator,
                                      that.legendItemToolTipGenerator ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.legendItemURLGenerator,
                                      that.legendItemURLGenerator ) ) {
            return false;
        }
        return super.equals ( obj );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        DefaultPolarItemRenderer clone
            = ( DefaultPolarItemRenderer ) super.clone();
        if ( this.legendLine != null ) {
            clone.legendLine = ShapeUtilities.clone ( this.legendLine );
        }
        clone.seriesFilled = ( BooleanList ) this.seriesFilled.clone();
        clone.toolTipGeneratorList
            = ( ObjectList ) this.toolTipGeneratorList.clone();
        if ( clone.baseToolTipGenerator instanceof PublicCloneable ) {
            clone.baseToolTipGenerator = ( XYToolTipGenerator )
                                         ObjectUtilities.clone ( this.baseToolTipGenerator );
        }
        if ( clone.urlGenerator instanceof PublicCloneable ) {
            clone.urlGenerator = ( XYURLGenerator )
                                 ObjectUtilities.clone ( this.urlGenerator );
        }
        if ( clone.legendItemToolTipGenerator instanceof PublicCloneable ) {
            clone.legendItemToolTipGenerator = ( XYSeriesLabelGenerator )
                                               ObjectUtilities.clone ( this.legendItemToolTipGenerator );
        }
        if ( clone.legendItemURLGenerator instanceof PublicCloneable ) {
            clone.legendItemURLGenerator = ( XYSeriesLabelGenerator )
                                           ObjectUtilities.clone ( this.legendItemURLGenerator );
        }
        return clone;
    }
    private void readObject ( ObjectInputStream stream )
    throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.legendLine = SerialUtilities.readShape ( stream );
        this.fillComposite = SerialUtilities.readComposite ( stream );
    }
    private void writeObject ( ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeShape ( this.legendLine, stream );
        SerialUtilities.writeComposite ( this.fillComposite, stream );
    }
}
