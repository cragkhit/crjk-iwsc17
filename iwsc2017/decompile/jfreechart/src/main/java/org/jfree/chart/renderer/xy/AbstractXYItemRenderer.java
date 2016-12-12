package org.jfree.chart.renderer.xy;
import java.awt.geom.GeneralPath;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import java.awt.geom.Ellipse2D;
import org.jfree.chart.entity.EntityCollection;
import java.util.Iterator;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.util.ObjectUtilities;
import org.jfree.chart.util.CloneUtils;
import org.jfree.util.PublicCloneable;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.GradientPaintTransformer;
import java.awt.geom.Point2D;
import java.awt.Font;
import org.jfree.ui.RectangleAnchor;
import org.jfree.text.TextUtilities;
import java.awt.GradientPaint;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.util.TextUtils;
import org.jfree.ui.LengthAdjustmentType;
import java.awt.Composite;
import java.awt.AlphaComposite;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.Marker;
import java.awt.RenderingHints;
import org.jfree.chart.plot.Plot;
import java.awt.geom.Line2D;
import org.jfree.chart.plot.PlotOrientation;
import java.awt.Stroke;
import java.awt.Paint;
import java.awt.Shape;
import org.jfree.data.general.Dataset;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.Range;
import java.util.Collection;
import org.jfree.chart.event.AnnotationChangeEvent;
import org.jfree.chart.util.ParamChecks;
import org.jfree.ui.Layer;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.data.ItemKey;
import org.jfree.data.xy.XYItemKey;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.data.xy.XYDataset;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.chart.labels.StandardXYSeriesLabelGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import org.jfree.chart.labels.XYSeriesLabelGenerator;
import java.util.List;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.labels.XYItemLabelGenerator;
import java.util.Map;
import org.jfree.chart.plot.XYPlot;
import java.io.Serializable;
import org.jfree.chart.event.AnnotationChangeListener;
import org.jfree.chart.renderer.AbstractRenderer;
public abstract class AbstractXYItemRenderer extends AbstractRenderer implements XYItemRenderer, AnnotationChangeListener, Cloneable, Serializable {
    private static final long serialVersionUID = 8019124836026607990L;
    private XYPlot plot;
    private Map<Integer, XYItemLabelGenerator> itemLabelGeneratorMap;
    private XYItemLabelGenerator baseItemLabelGenerator;
    private Map<Integer, XYToolTipGenerator> toolTipGeneratorMap;
    private XYToolTipGenerator baseToolTipGenerator;
    private XYURLGenerator urlGenerator;
    private List backgroundAnnotations;
    private List foregroundAnnotations;
    private XYSeriesLabelGenerator legendItemLabelGenerator;
    private XYSeriesLabelGenerator legendItemToolTipGenerator;
    private XYSeriesLabelGenerator legendItemURLGenerator;
    private XYItemLabelGenerator itemLabelGenerator;
    private XYToolTipGenerator toolTipGenerator;
    protected AbstractXYItemRenderer() {
        this.itemLabelGenerator = null;
        this.itemLabelGeneratorMap = new HashMap<Integer, XYItemLabelGenerator>();
        this.toolTipGenerator = null;
        this.toolTipGeneratorMap = new HashMap<Integer, XYToolTipGenerator>();
        this.urlGenerator = null;
        this.backgroundAnnotations = new ArrayList();
        this.foregroundAnnotations = new ArrayList();
        this.legendItemLabelGenerator = new StandardXYSeriesLabelGenerator ( "{0}" );
    }
    @Override
    public int getPassCount() {
        return 1;
    }
    @Override
    public XYPlot getPlot() {
        return this.plot;
    }
    @Override
    public void setPlot ( final XYPlot plot ) {
        this.plot = plot;
    }
    @Override
    public XYItemRendererState initialise ( final Graphics2D g2, final Rectangle2D dataArea, final XYPlot plot, final XYDataset dataset, final PlotRenderingInfo info ) {
        return new XYItemRendererState ( info );
    }
    protected void beginElementGroup ( final Graphics2D g2, final Comparable seriesKey, final int itemIndex ) {
        this.beginElementGroup ( g2, new XYItemKey<Object> ( seriesKey, itemIndex ) );
    }
    @Override
    public XYItemLabelGenerator getItemLabelGenerator ( final int series, final int item ) {
        if ( this.itemLabelGenerator != null ) {
            return this.itemLabelGenerator;
        }
        XYItemLabelGenerator generator = this.itemLabelGeneratorMap.get ( series );
        if ( generator == null ) {
            generator = this.baseItemLabelGenerator;
        }
        return generator;
    }
    @Override
    public XYItemLabelGenerator getSeriesItemLabelGenerator ( final int series ) {
        return this.itemLabelGeneratorMap.get ( series );
    }
    @Override
    public void setSeriesItemLabelGenerator ( final int series, final XYItemLabelGenerator generator ) {
        this.itemLabelGeneratorMap.put ( series, generator );
        this.fireChangeEvent();
    }
    @Override
    public XYItemLabelGenerator getBaseItemLabelGenerator() {
        return this.baseItemLabelGenerator;
    }
    @Override
    public void setBaseItemLabelGenerator ( final XYItemLabelGenerator generator ) {
        this.baseItemLabelGenerator = generator;
        this.fireChangeEvent();
    }
    @Override
    public XYToolTipGenerator getToolTipGenerator ( final int series, final int item ) {
        if ( this.toolTipGenerator != null ) {
            return this.toolTipGenerator;
        }
        XYToolTipGenerator generator = this.toolTipGeneratorMap.get ( series );
        if ( generator == null ) {
            generator = this.baseToolTipGenerator;
        }
        return generator;
    }
    @Override
    public XYToolTipGenerator getSeriesToolTipGenerator ( final int series ) {
        return this.toolTipGeneratorMap.get ( series );
    }
    @Override
    public void setSeriesToolTipGenerator ( final int series, final XYToolTipGenerator generator ) {
        this.toolTipGeneratorMap.put ( series, generator );
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
    @Override
    public void addAnnotation ( final XYAnnotation annotation ) {
        this.addAnnotation ( annotation, Layer.FOREGROUND );
    }
    @Override
    public void addAnnotation ( final XYAnnotation annotation, final Layer layer ) {
        ParamChecks.nullNotPermitted ( annotation, "annotation" );
        if ( layer.equals ( ( Object ) Layer.FOREGROUND ) ) {
            this.foregroundAnnotations.add ( annotation );
            annotation.addChangeListener ( this );
            this.fireChangeEvent();
        } else {
            if ( !layer.equals ( ( Object ) Layer.BACKGROUND ) ) {
                throw new RuntimeException ( "Unknown layer." );
            }
            this.backgroundAnnotations.add ( annotation );
            annotation.addChangeListener ( this );
            this.fireChangeEvent();
        }
    }
    @Override
    public boolean removeAnnotation ( final XYAnnotation annotation ) {
        boolean removed = this.foregroundAnnotations.remove ( annotation );
        removed &= this.backgroundAnnotations.remove ( annotation );
        annotation.removeChangeListener ( this );
        this.fireChangeEvent();
        return removed;
    }
    @Override
    public void removeAnnotations() {
        for ( int i = 0; i < this.foregroundAnnotations.size(); ++i ) {
            final XYAnnotation annotation = this.foregroundAnnotations.get ( i );
            annotation.removeChangeListener ( this );
        }
        for ( int i = 0; i < this.backgroundAnnotations.size(); ++i ) {
            final XYAnnotation annotation = this.backgroundAnnotations.get ( i );
            annotation.removeChangeListener ( this );
        }
        this.foregroundAnnotations.clear();
        this.backgroundAnnotations.clear();
        this.fireChangeEvent();
    }
    @Override
    public void annotationChanged ( final AnnotationChangeEvent event ) {
        this.fireChangeEvent();
    }
    public Collection getAnnotations() {
        final List result = new ArrayList ( this.foregroundAnnotations );
        result.addAll ( this.backgroundAnnotations );
        return result;
    }
    @Override
    public XYSeriesLabelGenerator getLegendItemLabelGenerator() {
        return this.legendItemLabelGenerator;
    }
    @Override
    public void setLegendItemLabelGenerator ( final XYSeriesLabelGenerator generator ) {
        ParamChecks.nullNotPermitted ( generator, "generator" );
        this.legendItemLabelGenerator = generator;
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
    public Range findDomainBounds ( final XYDataset dataset ) {
        return this.findDomainBounds ( dataset, false );
    }
    protected Range findDomainBounds ( final XYDataset dataset, final boolean includeInterval ) {
        if ( dataset == null ) {
            return null;
        }
        if ( this.getDataBoundsIncludesVisibleSeriesOnly() ) {
            final List visibleSeriesKeys = new ArrayList();
            for ( int seriesCount = dataset.getSeriesCount(), s = 0; s < seriesCount; ++s ) {
                if ( this.isSeriesVisible ( s ) ) {
                    visibleSeriesKeys.add ( dataset.getSeriesKey ( s ) );
                }
            }
            return DatasetUtilities.findDomainBounds ( dataset, visibleSeriesKeys, includeInterval );
        }
        return DatasetUtilities.findDomainBounds ( dataset, includeInterval );
    }
    @Override
    public Range findRangeBounds ( final XYDataset dataset ) {
        return this.findRangeBounds ( dataset, false );
    }
    protected Range findRangeBounds ( final XYDataset dataset, final boolean includeInterval ) {
        if ( dataset == null ) {
            return null;
        }
        if ( this.getDataBoundsIncludesVisibleSeriesOnly() ) {
            final List visibleSeriesKeys = new ArrayList();
            for ( int seriesCount = dataset.getSeriesCount(), s = 0; s < seriesCount; ++s ) {
                if ( this.isSeriesVisible ( s ) ) {
                    visibleSeriesKeys.add ( dataset.getSeriesKey ( s ) );
                }
            }
            Range xRange = null;
            final XYPlot p = this.getPlot();
            if ( p != null ) {
                ValueAxis xAxis = null;
                final int index = p.getIndexOf ( this );
                if ( index >= 0 ) {
                    xAxis = this.plot.getDomainAxisForDataset ( index );
                }
                if ( xAxis != null ) {
                    xRange = xAxis.getRange();
                }
            }
            if ( xRange == null ) {
                xRange = new Range ( Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY );
            }
            return DatasetUtilities.findRangeBounds ( dataset, visibleSeriesKeys, xRange, includeInterval );
        }
        return DatasetUtilities.findRangeBounds ( dataset, includeInterval );
    }
    @Override
    public LegendItemCollection getLegendItems() {
        if ( this.plot == null ) {
            return new LegendItemCollection();
        }
        final LegendItemCollection result = new LegendItemCollection();
        final int index = this.plot.getIndexOf ( this );
        final XYDataset dataset = this.plot.getDataset ( index );
        if ( dataset != null ) {
            for ( int seriesCount = dataset.getSeriesCount(), i = 0; i < seriesCount; ++i ) {
                if ( this.isSeriesVisibleInLegend ( i ) ) {
                    final LegendItem item = this.getLegendItem ( index, i );
                    if ( item != null ) {
                        result.add ( item );
                    }
                }
            }
        }
        return result;
    }
    @Override
    public LegendItem getLegendItem ( final int datasetIndex, final int series ) {
        final XYPlot xyplot = this.getPlot();
        if ( xyplot == null ) {
            return null;
        }
        final XYDataset dataset = xyplot.getDataset ( datasetIndex );
        if ( dataset == null ) {
            return null;
        }
        final String description;
        final String label = description = this.legendItemLabelGenerator.generateLabel ( dataset, series );
        String toolTipText = null;
        if ( this.getLegendItemToolTipGenerator() != null ) {
            toolTipText = this.getLegendItemToolTipGenerator().generateLabel ( dataset, series );
        }
        String urlText = null;
        if ( this.getLegendItemURLGenerator() != null ) {
            urlText = this.getLegendItemURLGenerator().generateLabel ( dataset, series );
        }
        final Shape shape = this.lookupLegendShape ( series );
        final Paint paint = this.lookupSeriesPaint ( series );
        final LegendItem item = new LegendItem ( label, paint );
        item.setToolTipText ( toolTipText );
        item.setURLText ( urlText );
        item.setLabelFont ( this.lookupLegendTextFont ( series ) );
        final Paint labelPaint = this.lookupLegendTextPaint ( series );
        if ( labelPaint != null ) {
            item.setLabelPaint ( labelPaint );
        }
        item.setSeriesKey ( dataset.getSeriesKey ( series ) );
        item.setSeriesIndex ( series );
        item.setDataset ( dataset );
        item.setDatasetIndex ( datasetIndex );
        if ( this.getTreatLegendShapeAsLine() ) {
            item.setLineVisible ( true );
            item.setLine ( shape );
            item.setLinePaint ( paint );
            item.setShapeVisible ( false );
        } else {
            final Paint outlinePaint = this.lookupSeriesOutlinePaint ( series );
            final Stroke outlineStroke = this.lookupSeriesOutlineStroke ( series );
            item.setOutlinePaint ( outlinePaint );
            item.setOutlineStroke ( outlineStroke );
        }
        return item;
    }
    @Override
    public void fillDomainGridBand ( final Graphics2D g2, final XYPlot plot, final ValueAxis axis, final Rectangle2D dataArea, final double start, final double end ) {
        final double x1 = axis.valueToJava2D ( start, dataArea, plot.getDomainAxisEdge() );
        final double x2 = axis.valueToJava2D ( end, dataArea, plot.getDomainAxisEdge() );
        Rectangle2D band;
        if ( plot.getOrientation() == PlotOrientation.VERTICAL ) {
            band = new Rectangle2D.Double ( Math.min ( x1, x2 ), dataArea.getMinY(), Math.abs ( x2 - x1 ), dataArea.getHeight() );
        } else {
            band = new Rectangle2D.Double ( dataArea.getMinX(), Math.min ( x1, x2 ), dataArea.getWidth(), Math.abs ( x2 - x1 ) );
        }
        final Paint paint = plot.getDomainTickBandPaint();
        if ( paint != null ) {
            g2.setPaint ( paint );
            g2.fill ( band );
        }
    }
    @Override
    public void fillRangeGridBand ( final Graphics2D g2, final XYPlot plot, final ValueAxis axis, final Rectangle2D dataArea, final double start, final double end ) {
        final double y1 = axis.valueToJava2D ( start, dataArea, plot.getRangeAxisEdge() );
        final double y2 = axis.valueToJava2D ( end, dataArea, plot.getRangeAxisEdge() );
        Rectangle2D band;
        if ( plot.getOrientation() == PlotOrientation.VERTICAL ) {
            band = new Rectangle2D.Double ( dataArea.getMinX(), Math.min ( y1, y2 ), dataArea.getWidth(), Math.abs ( y2 - y1 ) );
        } else {
            band = new Rectangle2D.Double ( Math.min ( y1, y2 ), dataArea.getMinY(), Math.abs ( y2 - y1 ), dataArea.getHeight() );
        }
        final Paint paint = plot.getRangeTickBandPaint();
        if ( paint != null ) {
            g2.setPaint ( paint );
            g2.fill ( band );
        }
    }
    @Override
    public void drawDomainGridLine ( final Graphics2D g2, final XYPlot plot, final ValueAxis axis, final Rectangle2D dataArea, final double value ) {
        final Range range = axis.getRange();
        if ( !range.contains ( value ) ) {
            return;
        }
        final PlotOrientation orientation = plot.getOrientation();
        final double v = axis.valueToJava2D ( value, dataArea, plot.getDomainAxisEdge() );
        Line2D line = null;
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            line = new Line2D.Double ( dataArea.getMinX(), v, dataArea.getMaxX(), v );
        } else if ( orientation == PlotOrientation.VERTICAL ) {
            line = new Line2D.Double ( v, dataArea.getMinY(), v, dataArea.getMaxY() );
        }
        final Paint paint = plot.getDomainGridlinePaint();
        final Stroke stroke = plot.getDomainGridlineStroke();
        g2.setPaint ( ( paint != null ) ? paint : Plot.DEFAULT_OUTLINE_PAINT );
        g2.setStroke ( ( stroke != null ) ? stroke : Plot.DEFAULT_OUTLINE_STROKE );
        final Object saved = g2.getRenderingHint ( RenderingHints.KEY_STROKE_CONTROL );
        g2.setRenderingHint ( RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE );
        g2.draw ( line );
        g2.setRenderingHint ( RenderingHints.KEY_STROKE_CONTROL, saved );
    }
    public void drawDomainLine ( final Graphics2D g2, final XYPlot plot, final ValueAxis axis, final Rectangle2D dataArea, final double value, final Paint paint, final Stroke stroke ) {
        final Range range = axis.getRange();
        if ( !range.contains ( value ) ) {
            return;
        }
        final PlotOrientation orientation = plot.getOrientation();
        Line2D line = null;
        final double v = axis.valueToJava2D ( value, dataArea, plot.getDomainAxisEdge() );
        if ( orientation.isHorizontal() ) {
            line = new Line2D.Double ( dataArea.getMinX(), v, dataArea.getMaxX(), v );
        } else if ( orientation.isVertical() ) {
            line = new Line2D.Double ( v, dataArea.getMinY(), v, dataArea.getMaxY() );
        }
        g2.setPaint ( paint );
        g2.setStroke ( stroke );
        final Object saved = g2.getRenderingHint ( RenderingHints.KEY_STROKE_CONTROL );
        g2.setRenderingHint ( RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE );
        g2.draw ( line );
        g2.setRenderingHint ( RenderingHints.KEY_STROKE_CONTROL, saved );
    }
    @Override
    public void drawRangeLine ( final Graphics2D g2, final XYPlot plot, final ValueAxis axis, final Rectangle2D dataArea, final double value, final Paint paint, final Stroke stroke ) {
        final Range range = axis.getRange();
        if ( !range.contains ( value ) ) {
            return;
        }
        final PlotOrientation orientation = plot.getOrientation();
        Line2D line = null;
        final double v = axis.valueToJava2D ( value, dataArea, plot.getRangeAxisEdge() );
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            line = new Line2D.Double ( v, dataArea.getMinY(), v, dataArea.getMaxY() );
        } else if ( orientation == PlotOrientation.VERTICAL ) {
            line = new Line2D.Double ( dataArea.getMinX(), v, dataArea.getMaxX(), v );
        }
        g2.setPaint ( paint );
        g2.setStroke ( stroke );
        final Object saved = g2.getRenderingHint ( RenderingHints.KEY_STROKE_CONTROL );
        g2.setRenderingHint ( RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE );
        g2.draw ( line );
        g2.setRenderingHint ( RenderingHints.KEY_STROKE_CONTROL, saved );
    }
    @Override
    public void drawDomainMarker ( final Graphics2D g2, final XYPlot plot, final ValueAxis domainAxis, final Marker marker, final Rectangle2D dataArea ) {
        if ( marker instanceof ValueMarker ) {
            final ValueMarker vm = ( ValueMarker ) marker;
            final double value = vm.getValue();
            final Range range = domainAxis.getRange();
            if ( !range.contains ( value ) ) {
                return;
            }
            final double v = domainAxis.valueToJava2D ( value, dataArea, plot.getDomainAxisEdge() );
            final PlotOrientation orientation = plot.getOrientation();
            Line2D line = null;
            if ( orientation == PlotOrientation.HORIZONTAL ) {
                line = new Line2D.Double ( dataArea.getMinX(), v, dataArea.getMaxX(), v );
            } else {
                if ( orientation != PlotOrientation.VERTICAL ) {
                    throw new IllegalStateException ( "Unrecognised orientation." );
                }
                line = new Line2D.Double ( v, dataArea.getMinY(), v, dataArea.getMaxY() );
            }
            final Composite originalComposite = g2.getComposite();
            g2.setComposite ( AlphaComposite.getInstance ( 3, marker.getAlpha() ) );
            g2.setPaint ( marker.getPaint() );
            g2.setStroke ( marker.getStroke() );
            g2.draw ( line );
            final String label = marker.getLabel();
            final RectangleAnchor anchor = marker.getLabelAnchor();
            if ( label != null ) {
                final Font labelFont = marker.getLabelFont();
                g2.setFont ( labelFont );
                final Point2D coords = this.calculateDomainMarkerTextAnchorPoint ( g2, orientation, dataArea, line.getBounds2D(), marker.getLabelOffset(), LengthAdjustmentType.EXPAND, anchor );
                final Rectangle2D r = TextUtils.calcAlignedStringBounds ( label, g2, ( float ) coords.getX(), ( float ) coords.getY(), marker.getLabelTextAnchor() );
                g2.setPaint ( marker.getLabelBackgroundColor() );
                g2.fill ( r );
                g2.setPaint ( marker.getLabelPaint() );
                TextUtils.drawAlignedString ( label, g2, ( float ) coords.getX(), ( float ) coords.getY(), marker.getLabelTextAnchor() );
            }
            g2.setComposite ( originalComposite );
        } else if ( marker instanceof IntervalMarker ) {
            final IntervalMarker im = ( IntervalMarker ) marker;
            final double start = im.getStartValue();
            final double end = im.getEndValue();
            final Range range2 = domainAxis.getRange();
            if ( !range2.intersects ( start, end ) ) {
                return;
            }
            final double start2d = domainAxis.valueToJava2D ( start, dataArea, plot.getDomainAxisEdge() );
            final double end2d = domainAxis.valueToJava2D ( end, dataArea, plot.getDomainAxisEdge() );
            double low = Math.min ( start2d, end2d );
            double high = Math.max ( start2d, end2d );
            final PlotOrientation orientation2 = plot.getOrientation();
            Rectangle2D rect = null;
            if ( orientation2 == PlotOrientation.HORIZONTAL ) {
                low = Math.max ( low, dataArea.getMinY() );
                high = Math.min ( high, dataArea.getMaxY() );
                rect = new Rectangle2D.Double ( dataArea.getMinX(), low, dataArea.getWidth(), high - low );
            } else if ( orientation2 == PlotOrientation.VERTICAL ) {
                low = Math.max ( low, dataArea.getMinX() );
                high = Math.min ( high, dataArea.getMaxX() );
                rect = new Rectangle2D.Double ( low, dataArea.getMinY(), high - low, dataArea.getHeight() );
            }
            final Composite originalComposite2 = g2.getComposite();
            g2.setComposite ( AlphaComposite.getInstance ( 3, marker.getAlpha() ) );
            final Paint p = marker.getPaint();
            if ( p instanceof GradientPaint ) {
                GradientPaint gp = ( GradientPaint ) p;
                final GradientPaintTransformer t = im.getGradientPaintTransformer();
                if ( t != null ) {
                    gp = t.transform ( gp, ( Shape ) rect );
                }
                g2.setPaint ( gp );
            } else {
                g2.setPaint ( p );
            }
            g2.fill ( rect );
            if ( im.getOutlinePaint() != null && im.getOutlineStroke() != null ) {
                if ( orientation2 == PlotOrientation.VERTICAL ) {
                    final Line2D line2 = new Line2D.Double();
                    final double y0 = dataArea.getMinY();
                    final double y = dataArea.getMaxY();
                    g2.setPaint ( im.getOutlinePaint() );
                    g2.setStroke ( im.getOutlineStroke() );
                    if ( range2.contains ( start ) ) {
                        line2.setLine ( start2d, y0, start2d, y );
                        g2.draw ( line2 );
                    }
                    if ( range2.contains ( end ) ) {
                        line2.setLine ( end2d, y0, end2d, y );
                        g2.draw ( line2 );
                    }
                } else {
                    final Line2D line2 = new Line2D.Double();
                    final double x0 = dataArea.getMinX();
                    final double x = dataArea.getMaxX();
                    g2.setPaint ( im.getOutlinePaint() );
                    g2.setStroke ( im.getOutlineStroke() );
                    if ( range2.contains ( start ) ) {
                        line2.setLine ( x0, start2d, x, start2d );
                        g2.draw ( line2 );
                    }
                    if ( range2.contains ( end ) ) {
                        line2.setLine ( x0, end2d, x, end2d );
                        g2.draw ( line2 );
                    }
                }
            }
            final String label2 = marker.getLabel();
            final RectangleAnchor anchor2 = marker.getLabelAnchor();
            if ( label2 != null ) {
                final Font labelFont2 = marker.getLabelFont();
                g2.setFont ( labelFont2 );
                final Point2D coords2 = this.calculateDomainMarkerTextAnchorPoint ( g2, orientation2, dataArea, rect, marker.getLabelOffset(), marker.getLabelOffsetType(), anchor2 );
                final Rectangle2D r2 = TextUtils.calcAlignedStringBounds ( label2, g2, ( float ) coords2.getX(), ( float ) coords2.getY(), marker.getLabelTextAnchor() );
                g2.setPaint ( marker.getLabelBackgroundColor() );
                g2.fill ( r2 );
                g2.setPaint ( marker.getLabelPaint() );
                TextUtilities.drawAlignedString ( label2, g2, ( float ) coords2.getX(), ( float ) coords2.getY(), marker.getLabelTextAnchor() );
            }
            g2.setComposite ( originalComposite2 );
        }
    }
    protected Point2D calculateDomainMarkerTextAnchorPoint ( final Graphics2D g2, final PlotOrientation orientation, final Rectangle2D dataArea, final Rectangle2D markerArea, final RectangleInsets markerOffset, final LengthAdjustmentType labelOffsetType, final RectangleAnchor anchor ) {
        Rectangle2D anchorRect = null;
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            anchorRect = markerOffset.createAdjustedRectangle ( markerArea, LengthAdjustmentType.CONTRACT, labelOffsetType );
        } else if ( orientation == PlotOrientation.VERTICAL ) {
            anchorRect = markerOffset.createAdjustedRectangle ( markerArea, labelOffsetType, LengthAdjustmentType.CONTRACT );
        }
        return RectangleAnchor.coordinates ( anchorRect, anchor );
    }
    @Override
    public void drawRangeMarker ( final Graphics2D g2, final XYPlot plot, final ValueAxis rangeAxis, final Marker marker, final Rectangle2D dataArea ) {
        if ( marker instanceof ValueMarker ) {
            final ValueMarker vm = ( ValueMarker ) marker;
            final double value = vm.getValue();
            final Range range = rangeAxis.getRange();
            if ( !range.contains ( value ) ) {
                return;
            }
            final double v = rangeAxis.valueToJava2D ( value, dataArea, plot.getRangeAxisEdge() );
            final PlotOrientation orientation = plot.getOrientation();
            Line2D line = null;
            if ( orientation == PlotOrientation.HORIZONTAL ) {
                line = new Line2D.Double ( v, dataArea.getMinY(), v, dataArea.getMaxY() );
            } else {
                if ( orientation != PlotOrientation.VERTICAL ) {
                    throw new IllegalStateException ( "Unrecognised orientation." );
                }
                line = new Line2D.Double ( dataArea.getMinX(), v, dataArea.getMaxX(), v );
            }
            final Composite originalComposite = g2.getComposite();
            g2.setComposite ( AlphaComposite.getInstance ( 3, marker.getAlpha() ) );
            g2.setPaint ( marker.getPaint() );
            g2.setStroke ( marker.getStroke() );
            g2.draw ( line );
            final String label = marker.getLabel();
            final RectangleAnchor anchor = marker.getLabelAnchor();
            if ( label != null ) {
                final Font labelFont = marker.getLabelFont();
                g2.setFont ( labelFont );
                final Point2D coords = this.calculateRangeMarkerTextAnchorPoint ( g2, orientation, dataArea, line.getBounds2D(), marker.getLabelOffset(), LengthAdjustmentType.EXPAND, anchor );
                final Rectangle2D r = TextUtils.calcAlignedStringBounds ( label, g2, ( float ) coords.getX(), ( float ) coords.getY(), marker.getLabelTextAnchor() );
                g2.setPaint ( marker.getLabelBackgroundColor() );
                g2.fill ( r );
                g2.setPaint ( marker.getLabelPaint() );
                TextUtilities.drawAlignedString ( label, g2, ( float ) coords.getX(), ( float ) coords.getY(), marker.getLabelTextAnchor() );
            }
            g2.setComposite ( originalComposite );
        } else if ( marker instanceof IntervalMarker ) {
            final IntervalMarker im = ( IntervalMarker ) marker;
            final double start = im.getStartValue();
            final double end = im.getEndValue();
            final Range range2 = rangeAxis.getRange();
            if ( !range2.intersects ( start, end ) ) {
                return;
            }
            final double start2d = rangeAxis.valueToJava2D ( start, dataArea, plot.getRangeAxisEdge() );
            final double end2d = rangeAxis.valueToJava2D ( end, dataArea, plot.getRangeAxisEdge() );
            double low = Math.min ( start2d, end2d );
            double high = Math.max ( start2d, end2d );
            final PlotOrientation orientation2 = plot.getOrientation();
            Rectangle2D rect = null;
            if ( orientation2 == PlotOrientation.HORIZONTAL ) {
                low = Math.max ( low, dataArea.getMinX() );
                high = Math.min ( high, dataArea.getMaxX() );
                rect = new Rectangle2D.Double ( low, dataArea.getMinY(), high - low, dataArea.getHeight() );
            } else if ( orientation2 == PlotOrientation.VERTICAL ) {
                low = Math.max ( low, dataArea.getMinY() );
                high = Math.min ( high, dataArea.getMaxY() );
                rect = new Rectangle2D.Double ( dataArea.getMinX(), low, dataArea.getWidth(), high - low );
            }
            final Composite originalComposite2 = g2.getComposite();
            g2.setComposite ( AlphaComposite.getInstance ( 3, marker.getAlpha() ) );
            final Paint p = marker.getPaint();
            if ( p instanceof GradientPaint ) {
                GradientPaint gp = ( GradientPaint ) p;
                final GradientPaintTransformer t = im.getGradientPaintTransformer();
                if ( t != null ) {
                    gp = t.transform ( gp, ( Shape ) rect );
                }
                g2.setPaint ( gp );
            } else {
                g2.setPaint ( p );
            }
            g2.fill ( rect );
            if ( im.getOutlinePaint() != null && im.getOutlineStroke() != null ) {
                if ( orientation2 == PlotOrientation.VERTICAL ) {
                    final Line2D line2 = new Line2D.Double();
                    final double x0 = dataArea.getMinX();
                    final double x = dataArea.getMaxX();
                    g2.setPaint ( im.getOutlinePaint() );
                    g2.setStroke ( im.getOutlineStroke() );
                    if ( range2.contains ( start ) ) {
                        line2.setLine ( x0, start2d, x, start2d );
                        g2.draw ( line2 );
                    }
                    if ( range2.contains ( end ) ) {
                        line2.setLine ( x0, end2d, x, end2d );
                        g2.draw ( line2 );
                    }
                } else {
                    final Line2D line2 = new Line2D.Double();
                    final double y0 = dataArea.getMinY();
                    final double y = dataArea.getMaxY();
                    g2.setPaint ( im.getOutlinePaint() );
                    g2.setStroke ( im.getOutlineStroke() );
                    if ( range2.contains ( start ) ) {
                        line2.setLine ( start2d, y0, start2d, y );
                        g2.draw ( line2 );
                    }
                    if ( range2.contains ( end ) ) {
                        line2.setLine ( end2d, y0, end2d, y );
                        g2.draw ( line2 );
                    }
                }
            }
            final String label2 = marker.getLabel();
            final RectangleAnchor anchor2 = marker.getLabelAnchor();
            if ( label2 != null ) {
                final Font labelFont2 = marker.getLabelFont();
                g2.setFont ( labelFont2 );
                final Point2D coords2 = this.calculateRangeMarkerTextAnchorPoint ( g2, orientation2, dataArea, rect, marker.getLabelOffset(), marker.getLabelOffsetType(), anchor2 );
                final Rectangle2D r2 = TextUtils.calcAlignedStringBounds ( label2, g2, ( float ) coords2.getX(), ( float ) coords2.getY(), marker.getLabelTextAnchor() );
                g2.setPaint ( marker.getLabelBackgroundColor() );
                g2.fill ( r2 );
                g2.setPaint ( marker.getLabelPaint() );
                TextUtilities.drawAlignedString ( label2, g2, ( float ) coords2.getX(), ( float ) coords2.getY(), marker.getLabelTextAnchor() );
            }
            g2.setComposite ( originalComposite2 );
        }
    }
    private Point2D calculateRangeMarkerTextAnchorPoint ( final Graphics2D g2, final PlotOrientation orientation, final Rectangle2D dataArea, final Rectangle2D markerArea, final RectangleInsets markerOffset, final LengthAdjustmentType labelOffsetForRange, final RectangleAnchor anchor ) {
        Rectangle2D anchorRect = null;
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            anchorRect = markerOffset.createAdjustedRectangle ( markerArea, labelOffsetForRange, LengthAdjustmentType.CONTRACT );
        } else if ( orientation == PlotOrientation.VERTICAL ) {
            anchorRect = markerOffset.createAdjustedRectangle ( markerArea, LengthAdjustmentType.CONTRACT, labelOffsetForRange );
        }
        return RectangleAnchor.coordinates ( anchorRect, anchor );
    }
    @Override
    protected Object clone() throws CloneNotSupportedException {
        final AbstractXYItemRenderer clone = ( AbstractXYItemRenderer ) super.clone();
        if ( this.itemLabelGenerator != null && this.itemLabelGenerator instanceof PublicCloneable ) {
            final PublicCloneable pc = ( PublicCloneable ) this.itemLabelGenerator;
            clone.itemLabelGenerator = ( XYItemLabelGenerator ) pc.clone();
        }
        clone.itemLabelGeneratorMap = ( Map<Integer, XYItemLabelGenerator> ) CloneUtils.cloneMapValues ( this.itemLabelGeneratorMap );
        if ( this.baseItemLabelGenerator != null && this.baseItemLabelGenerator instanceof PublicCloneable ) {
            final PublicCloneable pc = ( PublicCloneable ) this.baseItemLabelGenerator;
            clone.baseItemLabelGenerator = ( XYItemLabelGenerator ) pc.clone();
        }
        if ( this.toolTipGenerator != null && this.toolTipGenerator instanceof PublicCloneable ) {
            final PublicCloneable pc = ( PublicCloneable ) this.toolTipGenerator;
            clone.toolTipGenerator = ( XYToolTipGenerator ) pc.clone();
        }
        clone.toolTipGeneratorMap = ( Map<Integer, XYToolTipGenerator> ) CloneUtils.cloneMapValues ( this.toolTipGeneratorMap );
        if ( this.baseToolTipGenerator != null && this.baseToolTipGenerator instanceof PublicCloneable ) {
            final PublicCloneable pc = ( PublicCloneable ) this.baseToolTipGenerator;
            clone.baseToolTipGenerator = ( XYToolTipGenerator ) pc.clone();
        }
        if ( this.legendItemLabelGenerator instanceof PublicCloneable ) {
            clone.legendItemLabelGenerator = ( XYSeriesLabelGenerator ) ObjectUtilities.clone ( ( Object ) this.legendItemLabelGenerator );
        }
        if ( this.legendItemToolTipGenerator instanceof PublicCloneable ) {
            clone.legendItemToolTipGenerator = ( XYSeriesLabelGenerator ) ObjectUtilities.clone ( ( Object ) this.legendItemToolTipGenerator );
        }
        if ( this.legendItemURLGenerator instanceof PublicCloneable ) {
            clone.legendItemURLGenerator = ( XYSeriesLabelGenerator ) ObjectUtilities.clone ( ( Object ) this.legendItemURLGenerator );
        }
        clone.foregroundAnnotations = ( List ) ObjectUtilities.deepClone ( ( Collection ) this.foregroundAnnotations );
        clone.backgroundAnnotations = ( List ) ObjectUtilities.deepClone ( ( Collection ) this.backgroundAnnotations );
        return clone;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof AbstractXYItemRenderer ) ) {
            return false;
        }
        final AbstractXYItemRenderer that = ( AbstractXYItemRenderer ) obj;
        return ObjectUtilities.equal ( ( Object ) this.itemLabelGenerator, ( Object ) that.itemLabelGenerator ) && this.itemLabelGeneratorMap.equals ( that.itemLabelGeneratorMap ) && ObjectUtilities.equal ( ( Object ) this.baseItemLabelGenerator, ( Object ) that.baseItemLabelGenerator ) && ObjectUtilities.equal ( ( Object ) this.toolTipGenerator, ( Object ) that.toolTipGenerator ) && this.toolTipGeneratorMap.equals ( that.toolTipGeneratorMap ) && ObjectUtilities.equal ( ( Object ) this.baseToolTipGenerator, ( Object ) that.baseToolTipGenerator ) && ObjectUtilities.equal ( ( Object ) this.urlGenerator, ( Object ) that.urlGenerator ) && this.foregroundAnnotations.equals ( that.foregroundAnnotations ) && this.backgroundAnnotations.equals ( that.backgroundAnnotations ) && ObjectUtilities.equal ( ( Object ) this.legendItemLabelGenerator, ( Object ) that.legendItemLabelGenerator ) && ObjectUtilities.equal ( ( Object ) this.legendItemToolTipGenerator, ( Object ) that.legendItemToolTipGenerator ) && ObjectUtilities.equal ( ( Object ) this.legendItemURLGenerator, ( Object ) that.legendItemURLGenerator ) && super.equals ( obj );
    }
    @Override
    public DrawingSupplier getDrawingSupplier() {
        DrawingSupplier result = null;
        final XYPlot p = this.getPlot();
        if ( p != null ) {
            result = p.getDrawingSupplier();
        }
        return result;
    }
    protected void updateCrosshairValues ( final CrosshairState crosshairState, final double x, final double y, final int domainAxisIndex, final int rangeAxisIndex, final double transX, final double transY, final PlotOrientation orientation ) {
        ParamChecks.nullNotPermitted ( orientation, "orientation" );
        if ( crosshairState != null ) {
            if ( this.plot.isDomainCrosshairLockedOnData() ) {
                if ( this.plot.isRangeCrosshairLockedOnData() ) {
                    crosshairState.updateCrosshairPoint ( x, y, domainAxisIndex, rangeAxisIndex, transX, transY, orientation );
                } else {
                    crosshairState.updateCrosshairX ( x, domainAxisIndex );
                }
            } else if ( this.plot.isRangeCrosshairLockedOnData() ) {
                crosshairState.updateCrosshairY ( y, rangeAxisIndex );
            }
        }
    }
    protected void drawItemLabel ( final Graphics2D g2, final PlotOrientation orientation, final XYDataset dataset, final int series, final int item, final double x, final double y, final boolean negative ) {
        final XYItemLabelGenerator generator = this.getItemLabelGenerator ( series, item );
        if ( generator != null ) {
            final Font labelFont = this.getItemLabelFont ( series, item );
            final Paint paint = this.getItemLabelPaint ( series, item );
            g2.setFont ( labelFont );
            g2.setPaint ( paint );
            final String label = generator.generateLabel ( dataset, series, item );
            ItemLabelPosition position;
            if ( !negative ) {
                position = this.getPositiveItemLabelPosition ( series, item );
            } else {
                position = this.getNegativeItemLabelPosition ( series, item );
            }
            final Point2D anchorPoint = this.calculateLabelAnchorPoint ( position.getItemLabelAnchor(), x, y, orientation );
            TextUtilities.drawRotatedString ( label, g2, ( float ) anchorPoint.getX(), ( float ) anchorPoint.getY(), position.getTextAnchor(), position.getAngle(), position.getRotationAnchor() );
        }
    }
    @Override
    public void drawAnnotations ( final Graphics2D g2, final Rectangle2D dataArea, final ValueAxis domainAxis, final ValueAxis rangeAxis, final Layer layer, final PlotRenderingInfo info ) {
        Iterator iterator = null;
        if ( layer.equals ( ( Object ) Layer.FOREGROUND ) ) {
            iterator = this.foregroundAnnotations.iterator();
        } else {
            if ( !layer.equals ( ( Object ) Layer.BACKGROUND ) ) {
                throw new RuntimeException ( "Unknown layer." );
            }
            iterator = this.backgroundAnnotations.iterator();
        }
        while ( iterator.hasNext() ) {
            final XYAnnotation annotation = iterator.next();
            final int index = this.plot.getIndexOf ( this );
            annotation.draw ( g2, this.plot, dataArea, domainAxis, rangeAxis, index, info );
        }
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
    public static boolean isPointInRect ( final Rectangle2D rect, final double x, final double y ) {
        return x >= rect.getMinX() && x <= rect.getMaxX() && y >= rect.getMinY() && y <= rect.getMaxY();
    }
    protected static void moveTo ( final GeneralPath hotspot, final double x, final double y ) {
        hotspot.moveTo ( ( float ) x, ( float ) y );
    }
    protected static void lineTo ( final GeneralPath hotspot, final double x, final double y ) {
        hotspot.lineTo ( ( float ) x, ( float ) y );
    }
    public XYItemLabelGenerator getItemLabelGenerator() {
        return this.itemLabelGenerator;
    }
    @Override
    public void setItemLabelGenerator ( final XYItemLabelGenerator generator ) {
        this.itemLabelGenerator = generator;
        this.fireChangeEvent();
    }
    public XYToolTipGenerator getToolTipGenerator() {
        return this.toolTipGenerator;
    }
    @Override
    public void setToolTipGenerator ( final XYToolTipGenerator generator ) {
        this.toolTipGenerator = generator;
        this.fireChangeEvent();
    }
    protected void updateCrosshairValues ( final CrosshairState crosshairState, final double x, final double y, final double transX, final double transY, final PlotOrientation orientation ) {
        this.updateCrosshairValues ( crosshairState, x, y, 0, 0, transX, transY, orientation );
    }
}
