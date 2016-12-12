package org.jfree.chart.renderer.category;
import java.awt.geom.Ellipse2D;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.util.SortOrder;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.util.CloneUtils;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryCrosshairState;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.util.ObjectUtilities;
import org.jfree.data.general.Dataset;
import org.jfree.chart.LegendItem;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.GradientPaintTransformer;
import java.awt.GradientPaint;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.util.TextUtils;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.Marker;
import java.awt.geom.Point2D;
import java.awt.Font;
import org.jfree.ui.RectangleAnchor;
import org.jfree.text.TextUtilities;
import java.awt.Composite;
import java.awt.AlphaComposite;
import org.jfree.chart.plot.CategoryMarker;
import java.awt.RenderingHints;
import org.jfree.chart.axis.ValueAxis;
import java.awt.Stroke;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Line2D;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.axis.CategoryAxis;
import java.util.List;
import org.jfree.data.general.DatasetUtilities;
import java.util.ArrayList;
import org.jfree.data.Range;
import org.jfree.data.ItemKey;
import org.jfree.data.KeyedValues2DItemKey;
import org.jfree.data.category.CategoryDataset;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.util.ParamChecks;
import org.jfree.chart.labels.StandardCategorySeriesLabelGenerator;
import java.util.HashMap;
import org.jfree.chart.labels.CategorySeriesLabelGenerator;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import java.util.Map;
import org.jfree.chart.plot.CategoryPlot;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
import org.jfree.chart.renderer.AbstractRenderer;
public abstract class AbstractCategoryItemRenderer extends AbstractRenderer implements CategoryItemRenderer, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 1247553218442497391L;
    private CategoryPlot plot;
    private Map<Integer, CategoryItemLabelGenerator> itemLabelGeneratorMap;
    private CategoryItemLabelGenerator baseItemLabelGenerator;
    private Map<Integer, CategoryToolTipGenerator> toolTipGeneratorMap;
    private CategoryToolTipGenerator baseToolTipGenerator;
    private Map<Integer, CategoryURLGenerator> itemURLGeneratorMap;
    private CategoryURLGenerator baseItemURLGenerator;
    private CategorySeriesLabelGenerator legendItemLabelGenerator;
    private CategorySeriesLabelGenerator legendItemToolTipGenerator;
    private CategorySeriesLabelGenerator legendItemURLGenerator;
    private transient int rowCount;
    private transient int columnCount;
    private CategoryItemLabelGenerator itemLabelGenerator;
    private CategoryToolTipGenerator toolTipGenerator;
    private CategoryURLGenerator itemURLGenerator;
    protected AbstractCategoryItemRenderer() {
        this.itemLabelGenerator = null;
        this.itemLabelGeneratorMap = new HashMap<Integer, CategoryItemLabelGenerator>();
        this.toolTipGenerator = null;
        this.toolTipGeneratorMap = new HashMap<Integer, CategoryToolTipGenerator>();
        this.itemURLGenerator = null;
        this.itemURLGeneratorMap = new HashMap<Integer, CategoryURLGenerator>();
        this.legendItemLabelGenerator = new StandardCategorySeriesLabelGenerator();
    }
    @Override
    public int getPassCount() {
        return 1;
    }
    @Override
    public CategoryPlot getPlot() {
        return this.plot;
    }
    @Override
    public void setPlot ( final CategoryPlot plot ) {
        ParamChecks.nullNotPermitted ( plot, "plot" );
        this.plot = plot;
    }
    @Override
    public CategoryItemLabelGenerator getItemLabelGenerator ( final int row, final int column ) {
        return this.getSeriesItemLabelGenerator ( row );
    }
    @Override
    public CategoryItemLabelGenerator getSeriesItemLabelGenerator ( final int series ) {
        if ( this.itemLabelGenerator != null ) {
            return this.itemLabelGenerator;
        }
        CategoryItemLabelGenerator generator = this.itemLabelGeneratorMap.get ( series );
        if ( generator == null ) {
            generator = this.baseItemLabelGenerator;
        }
        return generator;
    }
    @Override
    public void setSeriesItemLabelGenerator ( final int series, final CategoryItemLabelGenerator generator ) {
        this.itemLabelGeneratorMap.put ( series, generator );
        this.fireChangeEvent();
    }
    @Override
    public CategoryItemLabelGenerator getBaseItemLabelGenerator() {
        return this.baseItemLabelGenerator;
    }
    @Override
    public void setBaseItemLabelGenerator ( final CategoryItemLabelGenerator generator ) {
        this.baseItemLabelGenerator = generator;
        this.fireChangeEvent();
    }
    @Override
    public CategoryToolTipGenerator getToolTipGenerator ( final int row, final int column ) {
        CategoryToolTipGenerator result;
        if ( this.toolTipGenerator != null ) {
            result = this.toolTipGenerator;
        } else {
            result = this.getSeriesToolTipGenerator ( row );
            if ( result == null ) {
                result = this.baseToolTipGenerator;
            }
        }
        return result;
    }
    @Override
    public CategoryToolTipGenerator getSeriesToolTipGenerator ( final int series ) {
        return this.toolTipGeneratorMap.get ( series );
    }
    @Override
    public void setSeriesToolTipGenerator ( final int series, final CategoryToolTipGenerator generator ) {
        this.toolTipGeneratorMap.put ( series, generator );
        this.fireChangeEvent();
    }
    @Override
    public CategoryToolTipGenerator getBaseToolTipGenerator() {
        return this.baseToolTipGenerator;
    }
    @Override
    public void setBaseToolTipGenerator ( final CategoryToolTipGenerator generator ) {
        this.baseToolTipGenerator = generator;
        this.fireChangeEvent();
    }
    @Override
    public CategoryURLGenerator getItemURLGenerator ( final int row, final int column ) {
        return this.getSeriesItemURLGenerator ( row );
    }
    @Override
    public CategoryURLGenerator getSeriesItemURLGenerator ( final int series ) {
        if ( this.itemURLGenerator != null ) {
            return this.itemURLGenerator;
        }
        CategoryURLGenerator generator = this.itemURLGeneratorMap.get ( series );
        if ( generator == null ) {
            generator = this.baseItemURLGenerator;
        }
        return generator;
    }
    @Override
    public void setSeriesItemURLGenerator ( final int series, final CategoryURLGenerator generator ) {
        this.itemURLGeneratorMap.put ( series, generator );
        this.fireChangeEvent();
    }
    @Override
    public CategoryURLGenerator getBaseItemURLGenerator() {
        return this.baseItemURLGenerator;
    }
    @Override
    public void setBaseItemURLGenerator ( final CategoryURLGenerator generator ) {
        this.baseItemURLGenerator = generator;
        this.fireChangeEvent();
    }
    public int getRowCount() {
        return this.rowCount;
    }
    public int getColumnCount() {
        return this.columnCount;
    }
    protected CategoryItemRendererState createState ( final PlotRenderingInfo info ) {
        return new CategoryItemRendererState ( info );
    }
    @Override
    public CategoryItemRendererState initialise ( final Graphics2D g2, final Rectangle2D dataArea, final CategoryPlot plot, final int rendererIndex, final PlotRenderingInfo info ) {
        this.setPlot ( plot );
        final CategoryDataset data = plot.getDataset ( rendererIndex );
        if ( data != null ) {
            this.rowCount = data.getRowCount();
            this.columnCount = data.getColumnCount();
        } else {
            this.rowCount = 0;
            this.columnCount = 0;
        }
        final CategoryItemRendererState state = this.createState ( info );
        state.setElementHinting ( plot.fetchElementHintingFlag() );
        final int[] visibleSeriesTemp = new int[this.rowCount];
        int visibleSeriesCount = 0;
        for ( int row = 0; row < this.rowCount; ++row ) {
            if ( this.isSeriesVisible ( row ) ) {
                visibleSeriesTemp[visibleSeriesCount] = row;
                ++visibleSeriesCount;
            }
        }
        final int[] visibleSeries = new int[visibleSeriesCount];
        System.arraycopy ( visibleSeriesTemp, 0, visibleSeries, 0, visibleSeriesCount );
        state.setVisibleSeriesArray ( visibleSeries );
        return state;
    }
    protected void beginElementGroup ( final Graphics2D g2, final Comparable rowKey, final Comparable columnKey ) {
        this.beginElementGroup ( g2, new KeyedValues2DItemKey<Object, Object> ( rowKey, columnKey ) );
    }
    @Override
    public Range findRangeBounds ( final CategoryDataset dataset ) {
        return this.findRangeBounds ( dataset, false );
    }
    protected Range findRangeBounds ( final CategoryDataset dataset, final boolean includeInterval ) {
        if ( dataset == null ) {
            return null;
        }
        if ( this.getDataBoundsIncludesVisibleSeriesOnly() ) {
            final List visibleSeriesKeys = new ArrayList();
            for ( int seriesCount = dataset.getRowCount(), s = 0; s < seriesCount; ++s ) {
                if ( this.isSeriesVisible ( s ) ) {
                    visibleSeriesKeys.add ( dataset.getRowKey ( s ) );
                }
            }
            return DatasetUtilities.findRangeBounds ( dataset, visibleSeriesKeys, includeInterval );
        }
        return DatasetUtilities.findRangeBounds ( dataset, includeInterval );
    }
    @Override
    public double getItemMiddle ( final Comparable rowKey, final Comparable columnKey, final CategoryDataset dataset, final CategoryAxis axis, final Rectangle2D area, final RectangleEdge edge ) {
        return axis.getCategoryMiddle ( columnKey, dataset.getColumnKeys(), area, edge );
    }
    @Override
    public void drawBackground ( final Graphics2D g2, final CategoryPlot plot, final Rectangle2D dataArea ) {
        plot.drawBackground ( g2, dataArea );
    }
    @Override
    public void drawOutline ( final Graphics2D g2, final CategoryPlot plot, final Rectangle2D dataArea ) {
        plot.drawOutline ( g2, dataArea );
    }
    @Override
    public void drawDomainGridline ( final Graphics2D g2, final CategoryPlot plot, final Rectangle2D dataArea, final double value ) {
        Line2D line = null;
        final PlotOrientation orientation = plot.getOrientation();
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            line = new Line2D.Double ( dataArea.getMinX(), value, dataArea.getMaxX(), value );
        } else if ( orientation == PlotOrientation.VERTICAL ) {
            line = new Line2D.Double ( value, dataArea.getMinY(), value, dataArea.getMaxY() );
        }
        Paint paint = plot.getDomainGridlinePaint();
        if ( paint == null ) {
            paint = CategoryPlot.DEFAULT_GRIDLINE_PAINT;
        }
        g2.setPaint ( paint );
        Stroke stroke = plot.getDomainGridlineStroke();
        if ( stroke == null ) {
            stroke = CategoryPlot.DEFAULT_GRIDLINE_STROKE;
        }
        g2.setStroke ( stroke );
        g2.draw ( line );
    }
    @Override
    public void drawRangeGridline ( final Graphics2D g2, final CategoryPlot plot, final ValueAxis axis, final Rectangle2D dataArea, final double value ) {
        final Range range = axis.getRange();
        if ( !range.contains ( value ) ) {
            return;
        }
        final PlotOrientation orientation = plot.getOrientation();
        final double v = axis.valueToJava2D ( value, dataArea, plot.getRangeAxisEdge() );
        Line2D line = null;
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            line = new Line2D.Double ( v, dataArea.getMinY(), v, dataArea.getMaxY() );
        } else if ( orientation == PlotOrientation.VERTICAL ) {
            line = new Line2D.Double ( dataArea.getMinX(), v, dataArea.getMaxX(), v );
        }
        Paint paint = plot.getRangeGridlinePaint();
        if ( paint == null ) {
            paint = CategoryPlot.DEFAULT_GRIDLINE_PAINT;
        }
        g2.setPaint ( paint );
        Stroke stroke = plot.getRangeGridlineStroke();
        if ( stroke == null ) {
            stroke = CategoryPlot.DEFAULT_GRIDLINE_STROKE;
        }
        g2.setStroke ( stroke );
        g2.draw ( line );
    }
    public void drawRangeLine ( final Graphics2D g2, final CategoryPlot plot, final ValueAxis axis, final Rectangle2D dataArea, final double value, final Paint paint, final Stroke stroke ) {
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
    public void drawDomainMarker ( final Graphics2D g2, final CategoryPlot plot, final CategoryAxis axis, final CategoryMarker marker, final Rectangle2D dataArea ) {
        final Comparable category = marker.getKey();
        final CategoryDataset dataset = plot.getDataset ( plot.getIndexOf ( this ) );
        final int columnIndex = dataset.getColumnIndex ( category );
        if ( columnIndex < 0 ) {
            return;
        }
        final Composite savedComposite = g2.getComposite();
        g2.setComposite ( AlphaComposite.getInstance ( 3, marker.getAlpha() ) );
        final PlotOrientation orientation = plot.getOrientation();
        Rectangle2D bounds;
        if ( marker.getDrawAsLine() ) {
            final double v = axis.getCategoryMiddle ( columnIndex, dataset.getColumnCount(), dataArea, plot.getDomainAxisEdge() );
            Line2D line = null;
            if ( orientation == PlotOrientation.HORIZONTAL ) {
                line = new Line2D.Double ( dataArea.getMinX(), v, dataArea.getMaxX(), v );
            } else {
                if ( orientation != PlotOrientation.VERTICAL ) {
                    throw new IllegalStateException();
                }
                line = new Line2D.Double ( v, dataArea.getMinY(), v, dataArea.getMaxY() );
            }
            g2.setPaint ( marker.getPaint() );
            g2.setStroke ( marker.getStroke() );
            g2.draw ( line );
            bounds = line.getBounds2D();
        } else {
            final double v2 = axis.getCategoryStart ( columnIndex, dataset.getColumnCount(), dataArea, plot.getDomainAxisEdge() );
            final double v3 = axis.getCategoryEnd ( columnIndex, dataset.getColumnCount(), dataArea, plot.getDomainAxisEdge() );
            Rectangle2D area = null;
            if ( orientation == PlotOrientation.HORIZONTAL ) {
                area = new Rectangle2D.Double ( dataArea.getMinX(), v2, dataArea.getWidth(), v3 - v2 );
            } else if ( orientation == PlotOrientation.VERTICAL ) {
                area = new Rectangle2D.Double ( v2, dataArea.getMinY(), v3 - v2, dataArea.getHeight() );
            }
            g2.setPaint ( marker.getPaint() );
            g2.fill ( area );
            bounds = area;
        }
        final String label = marker.getLabel();
        final RectangleAnchor anchor = marker.getLabelAnchor();
        if ( label != null ) {
            final Font labelFont = marker.getLabelFont();
            g2.setFont ( labelFont );
            g2.setPaint ( marker.getLabelPaint() );
            final Point2D coordinates = this.calculateDomainMarkerTextAnchorPoint ( g2, orientation, dataArea, bounds, marker.getLabelOffset(), marker.getLabelOffsetType(), anchor );
            TextUtilities.drawAlignedString ( label, g2, ( float ) coordinates.getX(), ( float ) coordinates.getY(), marker.getLabelTextAnchor() );
        }
        g2.setComposite ( savedComposite );
    }
    @Override
    public void drawRangeMarker ( final Graphics2D g2, final CategoryPlot plot, final ValueAxis axis, final Marker marker, final Rectangle2D dataArea ) {
        if ( marker instanceof ValueMarker ) {
            final ValueMarker vm = ( ValueMarker ) marker;
            final double value = vm.getValue();
            final Range range = axis.getRange();
            if ( !range.contains ( value ) ) {
                return;
            }
            final Composite savedComposite = g2.getComposite();
            g2.setComposite ( AlphaComposite.getInstance ( 3, marker.getAlpha() ) );
            final PlotOrientation orientation = plot.getOrientation();
            final double v = axis.valueToJava2D ( value, dataArea, plot.getRangeAxisEdge() );
            Line2D line = null;
            if ( orientation == PlotOrientation.HORIZONTAL ) {
                line = new Line2D.Double ( v, dataArea.getMinY(), v, dataArea.getMaxY() );
            } else {
                if ( orientation != PlotOrientation.VERTICAL ) {
                    throw new IllegalStateException();
                }
                line = new Line2D.Double ( dataArea.getMinX(), v, dataArea.getMaxX(), v );
            }
            g2.setPaint ( marker.getPaint() );
            g2.setStroke ( marker.getStroke() );
            g2.draw ( line );
            final String label = marker.getLabel();
            final RectangleAnchor anchor = marker.getLabelAnchor();
            if ( label != null ) {
                final Font labelFont = marker.getLabelFont();
                g2.setFont ( labelFont );
                final Point2D coordinates = this.calculateRangeMarkerTextAnchorPoint ( g2, orientation, dataArea, line.getBounds2D(), marker.getLabelOffset(), LengthAdjustmentType.EXPAND, anchor );
                final Rectangle2D rect = TextUtils.calcAlignedStringBounds ( label, g2, ( float ) coordinates.getX(), ( float ) coordinates.getY(), marker.getLabelTextAnchor() );
                g2.setPaint ( marker.getLabelBackgroundColor() );
                g2.fill ( rect );
                g2.setPaint ( marker.getLabelPaint() );
                TextUtils.drawAlignedString ( label, g2, ( float ) coordinates.getX(), ( float ) coordinates.getY(), marker.getLabelTextAnchor() );
            }
            g2.setComposite ( savedComposite );
        } else if ( marker instanceof IntervalMarker ) {
            final IntervalMarker im = ( IntervalMarker ) marker;
            final double start = im.getStartValue();
            final double end = im.getEndValue();
            final Range range2 = axis.getRange();
            if ( !range2.intersects ( start, end ) ) {
                return;
            }
            final Composite savedComposite2 = g2.getComposite();
            g2.setComposite ( AlphaComposite.getInstance ( 3, marker.getAlpha() ) );
            final double start2d = axis.valueToJava2D ( start, dataArea, plot.getRangeAxisEdge() );
            final double end2d = axis.valueToJava2D ( end, dataArea, plot.getRangeAxisEdge() );
            double low = Math.min ( start2d, end2d );
            double high = Math.max ( start2d, end2d );
            final PlotOrientation orientation2 = plot.getOrientation();
            Rectangle2D rect2 = null;
            if ( orientation2 == PlotOrientation.HORIZONTAL ) {
                low = Math.max ( low, dataArea.getMinX() );
                high = Math.min ( high, dataArea.getMaxX() );
                rect2 = new Rectangle2D.Double ( low, dataArea.getMinY(), high - low, dataArea.getHeight() );
            } else if ( orientation2 == PlotOrientation.VERTICAL ) {
                low = Math.max ( low, dataArea.getMinY() );
                high = Math.min ( high, dataArea.getMaxY() );
                rect2 = new Rectangle2D.Double ( dataArea.getMinX(), low, dataArea.getWidth(), high - low );
            }
            final Paint p = marker.getPaint();
            if ( p instanceof GradientPaint ) {
                GradientPaint gp = ( GradientPaint ) p;
                final GradientPaintTransformer t = im.getGradientPaintTransformer();
                if ( t != null ) {
                    gp = t.transform ( gp, ( Shape ) rect2 );
                }
                g2.setPaint ( gp );
            } else {
                g2.setPaint ( p );
            }
            g2.fill ( rect2 );
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
                final Point2D coords = this.calculateRangeMarkerTextAnchorPoint ( g2, orientation2, dataArea, rect2, marker.getLabelOffset(), marker.getLabelOffsetType(), anchor2 );
                final Rectangle2D r = TextUtils.calcAlignedStringBounds ( label2, g2, ( float ) coords.getX(), ( float ) coords.getY(), marker.getLabelTextAnchor() );
                g2.setPaint ( marker.getLabelBackgroundColor() );
                g2.fill ( r );
                g2.setPaint ( marker.getLabelPaint() );
                TextUtilities.drawAlignedString ( label2, g2, ( float ) coords.getX(), ( float ) coords.getY(), marker.getLabelTextAnchor() );
            }
            g2.setComposite ( savedComposite2 );
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
    protected Point2D calculateRangeMarkerTextAnchorPoint ( final Graphics2D g2, final PlotOrientation orientation, final Rectangle2D dataArea, final Rectangle2D markerArea, final RectangleInsets markerOffset, final LengthAdjustmentType labelOffsetType, final RectangleAnchor anchor ) {
        Rectangle2D anchorRect = null;
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            anchorRect = markerOffset.createAdjustedRectangle ( markerArea, labelOffsetType, LengthAdjustmentType.CONTRACT );
        } else if ( orientation == PlotOrientation.VERTICAL ) {
            anchorRect = markerOffset.createAdjustedRectangle ( markerArea, LengthAdjustmentType.CONTRACT, labelOffsetType );
        }
        return RectangleAnchor.coordinates ( anchorRect, anchor );
    }
    @Override
    public LegendItem getLegendItem ( final int datasetIndex, final int series ) {
        final CategoryPlot p = this.getPlot();
        if ( p == null ) {
            return null;
        }
        if ( !this.isSeriesVisible ( series ) || !this.isSeriesVisibleInLegend ( series ) ) {
            return null;
        }
        final CategoryDataset dataset = p.getDataset ( datasetIndex );
        final String description;
        final String label = description = this.legendItemLabelGenerator.generateLabel ( dataset, series );
        String toolTipText = null;
        if ( this.legendItemToolTipGenerator != null ) {
            toolTipText = this.legendItemToolTipGenerator.generateLabel ( dataset, series );
        }
        String urlText = null;
        if ( this.legendItemURLGenerator != null ) {
            urlText = this.legendItemURLGenerator.generateLabel ( dataset, series );
        }
        final Shape shape = this.lookupLegendShape ( series );
        final Paint paint = this.lookupSeriesPaint ( series );
        final Paint outlinePaint = this.lookupSeriesOutlinePaint ( series );
        final Stroke outlineStroke = this.lookupSeriesOutlineStroke ( series );
        final LegendItem item = new LegendItem ( label, description, toolTipText, urlText, shape, paint, outlineStroke, outlinePaint );
        item.setLabelFont ( this.lookupLegendTextFont ( series ) );
        final Paint labelPaint = this.lookupLegendTextPaint ( series );
        if ( labelPaint != null ) {
            item.setLabelPaint ( labelPaint );
        }
        item.setSeriesKey ( dataset.getRowKey ( series ) );
        item.setSeriesIndex ( series );
        item.setDataset ( dataset );
        item.setDatasetIndex ( datasetIndex );
        return item;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof AbstractCategoryItemRenderer ) ) {
            return false;
        }
        final AbstractCategoryItemRenderer that = ( AbstractCategoryItemRenderer ) obj;
        return ObjectUtilities.equal ( ( Object ) this.itemLabelGenerator, ( Object ) that.itemLabelGenerator ) && ObjectUtilities.equal ( ( Object ) this.itemLabelGeneratorMap, ( Object ) that.itemLabelGeneratorMap ) && ObjectUtilities.equal ( ( Object ) this.baseItemLabelGenerator, ( Object ) that.baseItemLabelGenerator ) && ObjectUtilities.equal ( ( Object ) this.toolTipGenerator, ( Object ) that.toolTipGenerator ) && ObjectUtilities.equal ( ( Object ) this.toolTipGeneratorMap, ( Object ) that.toolTipGeneratorMap ) && ObjectUtilities.equal ( ( Object ) this.baseToolTipGenerator, ( Object ) that.baseToolTipGenerator ) && ObjectUtilities.equal ( ( Object ) this.itemURLGenerator, ( Object ) that.itemURLGenerator ) && ObjectUtilities.equal ( ( Object ) this.itemURLGeneratorMap, ( Object ) that.itemURLGeneratorMap ) && ObjectUtilities.equal ( ( Object ) this.baseItemURLGenerator, ( Object ) that.baseItemURLGenerator ) && ObjectUtilities.equal ( ( Object ) this.legendItemLabelGenerator, ( Object ) that.legendItemLabelGenerator ) && ObjectUtilities.equal ( ( Object ) this.legendItemToolTipGenerator, ( Object ) that.legendItemToolTipGenerator ) && ObjectUtilities.equal ( ( Object ) this.legendItemURLGenerator, ( Object ) that.legendItemURLGenerator ) && super.equals ( obj );
    }
    @Override
    public int hashCode() {
        final int result = super.hashCode();
        return result;
    }
    @Override
    public DrawingSupplier getDrawingSupplier() {
        DrawingSupplier result = null;
        final CategoryPlot cp = this.getPlot();
        if ( cp != null ) {
            result = cp.getDrawingSupplier();
        }
        return result;
    }
    protected void updateCrosshairValues ( final CategoryCrosshairState crosshairState, final Comparable rowKey, final Comparable columnKey, final double value, final int datasetIndex, final double transX, final double transY, final PlotOrientation orientation ) {
        ParamChecks.nullNotPermitted ( orientation, "orientation" );
        if ( crosshairState != null ) {
            if ( this.plot.isRangeCrosshairLockedOnData() ) {
                crosshairState.updateCrosshairPoint ( rowKey, columnKey, value, datasetIndex, transX, transY, orientation );
            } else {
                crosshairState.updateCrosshairX ( rowKey, columnKey, datasetIndex, transX, orientation );
            }
        }
    }
    protected void drawItemLabel ( final Graphics2D g2, final PlotOrientation orientation, final CategoryDataset dataset, final int row, final int column, final double x, final double y, final boolean negative ) {
        final CategoryItemLabelGenerator generator = this.getItemLabelGenerator ( row, column );
        if ( generator != null ) {
            final Font labelFont = this.getItemLabelFont ( row, column );
            final Paint paint = this.getItemLabelPaint ( row, column );
            g2.setFont ( labelFont );
            g2.setPaint ( paint );
            final String label = generator.generateLabel ( dataset, row, column );
            ItemLabelPosition position;
            if ( !negative ) {
                position = this.getPositiveItemLabelPosition ( row, column );
            } else {
                position = this.getNegativeItemLabelPosition ( row, column );
            }
            final Point2D anchorPoint = this.calculateLabelAnchorPoint ( position.getItemLabelAnchor(), x, y, orientation );
            TextUtilities.drawRotatedString ( label, g2, ( float ) anchorPoint.getX(), ( float ) anchorPoint.getY(), position.getTextAnchor(), position.getAngle(), position.getRotationAnchor() );
        }
    }
    public Object clone() throws CloneNotSupportedException {
        final AbstractCategoryItemRenderer clone = ( AbstractCategoryItemRenderer ) super.clone();
        if ( this.itemLabelGenerator != null ) {
            if ( ! ( this.itemLabelGenerator instanceof PublicCloneable ) ) {
                throw new CloneNotSupportedException ( "ItemLabelGenerator not cloneable." );
            }
            final PublicCloneable pc = ( PublicCloneable ) this.itemLabelGenerator;
            clone.itemLabelGenerator = ( CategoryItemLabelGenerator ) pc.clone();
        }
        if ( this.itemLabelGeneratorMap != null ) {
            clone.itemLabelGeneratorMap = ( Map<Integer, CategoryItemLabelGenerator> ) CloneUtils.cloneMapValues ( this.itemLabelGeneratorMap );
        }
        if ( this.baseItemLabelGenerator != null ) {
            if ( ! ( this.baseItemLabelGenerator instanceof PublicCloneable ) ) {
                throw new CloneNotSupportedException ( "ItemLabelGenerator not cloneable." );
            }
            final PublicCloneable pc = ( PublicCloneable ) this.baseItemLabelGenerator;
            clone.baseItemLabelGenerator = ( CategoryItemLabelGenerator ) pc.clone();
        }
        if ( this.toolTipGenerator != null ) {
            if ( ! ( this.toolTipGenerator instanceof PublicCloneable ) ) {
                throw new CloneNotSupportedException ( "Tool tip generator not cloneable." );
            }
            final PublicCloneable pc = ( PublicCloneable ) this.toolTipGenerator;
            clone.toolTipGenerator = ( CategoryToolTipGenerator ) pc.clone();
        }
        if ( this.toolTipGeneratorMap != null ) {
            clone.toolTipGeneratorMap = ( Map<Integer, CategoryToolTipGenerator> ) CloneUtils.cloneMapValues ( this.toolTipGeneratorMap );
        }
        if ( this.baseToolTipGenerator != null ) {
            if ( ! ( this.baseToolTipGenerator instanceof PublicCloneable ) ) {
                throw new CloneNotSupportedException ( "Base tool tip generator not cloneable." );
            }
            final PublicCloneable pc = ( PublicCloneable ) this.baseToolTipGenerator;
            clone.baseToolTipGenerator = ( CategoryToolTipGenerator ) pc.clone();
        }
        if ( this.itemURLGenerator != null ) {
            if ( ! ( this.itemURLGenerator instanceof PublicCloneable ) ) {
                throw new CloneNotSupportedException ( "Item URL generator not cloneable." );
            }
            final PublicCloneable pc = ( PublicCloneable ) this.itemURLGenerator;
            clone.itemURLGenerator = ( CategoryURLGenerator ) pc.clone();
        }
        if ( this.itemURLGeneratorMap != null ) {
            clone.itemURLGeneratorMap = ( Map<Integer, CategoryURLGenerator> ) CloneUtils.cloneMapValues ( this.itemURLGeneratorMap );
        }
        if ( this.baseItemURLGenerator != null ) {
            if ( ! ( this.baseItemURLGenerator instanceof PublicCloneable ) ) {
                throw new CloneNotSupportedException ( "Base item URL generator not cloneable." );
            }
            final PublicCloneable pc = ( PublicCloneable ) this.baseItemURLGenerator;
            clone.baseItemURLGenerator = ( CategoryURLGenerator ) pc.clone();
        }
        if ( this.legendItemLabelGenerator instanceof PublicCloneable ) {
            clone.legendItemLabelGenerator = ( CategorySeriesLabelGenerator ) ObjectUtilities.clone ( ( Object ) this.legendItemLabelGenerator );
        }
        if ( this.legendItemToolTipGenerator instanceof PublicCloneable ) {
            clone.legendItemToolTipGenerator = ( CategorySeriesLabelGenerator ) ObjectUtilities.clone ( ( Object ) this.legendItemToolTipGenerator );
        }
        if ( this.legendItemURLGenerator instanceof PublicCloneable ) {
            clone.legendItemURLGenerator = ( CategorySeriesLabelGenerator ) ObjectUtilities.clone ( ( Object ) this.legendItemURLGenerator );
        }
        return clone;
    }
    protected CategoryAxis getDomainAxis ( final CategoryPlot plot, final int index ) {
        CategoryAxis result = plot.getDomainAxis ( index );
        if ( result == null ) {
            result = plot.getDomainAxis();
        }
        return result;
    }
    protected ValueAxis getRangeAxis ( final CategoryPlot plot, final int index ) {
        ValueAxis result = plot.getRangeAxis ( index );
        if ( result == null ) {
            result = plot.getRangeAxis();
        }
        return result;
    }
    public LegendItemCollection getLegendItems() {
        final LegendItemCollection result = new LegendItemCollection();
        if ( this.plot == null ) {
            return result;
        }
        final int index = this.plot.getIndexOf ( this );
        final CategoryDataset dataset = this.plot.getDataset ( index );
        if ( dataset == null ) {
            return result;
        }
        final int seriesCount = dataset.getRowCount();
        if ( this.plot.getRowRenderingOrder().equals ( ( Object ) SortOrder.ASCENDING ) ) {
            for ( int i = 0; i < seriesCount; ++i ) {
                if ( this.isSeriesVisibleInLegend ( i ) ) {
                    final LegendItem item = this.getLegendItem ( index, i );
                    if ( item != null ) {
                        result.add ( item );
                    }
                }
            }
        } else {
            for ( int i = seriesCount - 1; i >= 0; --i ) {
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
    public CategorySeriesLabelGenerator getLegendItemLabelGenerator() {
        return this.legendItemLabelGenerator;
    }
    public void setLegendItemLabelGenerator ( final CategorySeriesLabelGenerator generator ) {
        ParamChecks.nullNotPermitted ( generator, "generator" );
        this.legendItemLabelGenerator = generator;
        this.fireChangeEvent();
    }
    public CategorySeriesLabelGenerator getLegendItemToolTipGenerator() {
        return this.legendItemToolTipGenerator;
    }
    public void setLegendItemToolTipGenerator ( final CategorySeriesLabelGenerator generator ) {
        this.legendItemToolTipGenerator = generator;
        this.fireChangeEvent();
    }
    public CategorySeriesLabelGenerator getLegendItemURLGenerator() {
        return this.legendItemURLGenerator;
    }
    public void setLegendItemURLGenerator ( final CategorySeriesLabelGenerator generator ) {
        this.legendItemURLGenerator = generator;
        this.fireChangeEvent();
    }
    protected void addItemEntity ( final EntityCollection entities, final CategoryDataset dataset, final int row, final int column, final Shape hotspot ) {
        ParamChecks.nullNotPermitted ( hotspot, "hotspot" );
        if ( !this.getItemCreateEntity ( row, column ) ) {
            return;
        }
        String tip = null;
        final CategoryToolTipGenerator tipster = this.getToolTipGenerator ( row, column );
        if ( tipster != null ) {
            tip = tipster.generateToolTip ( dataset, row, column );
        }
        String url = null;
        final CategoryURLGenerator urlster = this.getItemURLGenerator ( row, column );
        if ( urlster != null ) {
            url = urlster.generateURL ( dataset, row, column );
        }
        final CategoryItemEntity entity = new CategoryItemEntity ( hotspot, tip, url, dataset, dataset.getRowKey ( row ), dataset.getColumnKey ( column ) );
        entities.add ( entity );
    }
    protected void addEntity ( final EntityCollection entities, final Shape hotspot, final CategoryDataset dataset, final int row, final int column, final double entityX, final double entityY ) {
        if ( !this.getItemCreateEntity ( row, column ) ) {
            return;
        }
        Shape s;
        if ( ( s = hotspot ) == null ) {
            final double r = this.getDefaultEntityRadius();
            final double w = r * 2.0;
            if ( this.getPlot().getOrientation() == PlotOrientation.VERTICAL ) {
                s = new Ellipse2D.Double ( entityX - r, entityY - r, w, w );
            } else {
                s = new Ellipse2D.Double ( entityY - r, entityX - r, w, w );
            }
        }
        String tip = null;
        final CategoryToolTipGenerator generator = this.getToolTipGenerator ( row, column );
        if ( generator != null ) {
            tip = generator.generateToolTip ( dataset, row, column );
        }
        String url = null;
        final CategoryURLGenerator urlster = this.getItemURLGenerator ( row, column );
        if ( urlster != null ) {
            url = urlster.generateURL ( dataset, row, column );
        }
        final CategoryItemEntity entity = new CategoryItemEntity ( s, tip, url, dataset, dataset.getRowKey ( row ), dataset.getColumnKey ( column ) );
        entities.add ( entity );
    }
    @Override
    public void setItemLabelGenerator ( final CategoryItemLabelGenerator generator ) {
        this.itemLabelGenerator = generator;
        this.fireChangeEvent();
    }
    @Override
    public CategoryToolTipGenerator getToolTipGenerator() {
        return this.toolTipGenerator;
    }
    @Override
    public void setToolTipGenerator ( final CategoryToolTipGenerator generator ) {
        this.toolTipGenerator = generator;
        this.fireChangeEvent();
    }
    @Override
    public void setItemURLGenerator ( final CategoryURLGenerator generator ) {
        this.itemURLGenerator = generator;
        this.fireChangeEvent();
    }
}
