package org.jfree.chart.plot;
import org.jfree.chart.util.ResourceBundleWrapper;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.chart.util.CloneUtils;
import org.jfree.util.PaintUtilities;
import org.jfree.util.ObjectUtilities;
import org.jfree.data.Range;
import java.awt.geom.Line2D;
import org.jfree.chart.renderer.category.AbstractCategoryItemRenderer;
import org.jfree.chart.axis.TickType;
import org.jfree.chart.axis.ValueTick;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.AxisCollection;
import java.awt.image.ImageObserver;
import java.awt.Image;
import java.awt.Composite;
import java.awt.AlphaComposite;
import java.awt.image.BufferedImage;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisState;
import java.awt.Shape;
import org.jfree.util.ShapeUtilities;
import org.jfree.chart.ChartRenderingInfo;
import java.awt.geom.Point2D;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import org.jfree.chart.annotations.CategoryAnnotation;
import java.util.Collections;
import java.util.Set;
import org.jfree.chart.event.MarkerChangeListener;
import org.jfree.ui.Layer;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.event.ChartChangeEventType;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.AnnotationChangeEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Collection;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.ui.RectangleEdge;
import java.util.Iterator;
import org.jfree.chart.util.ParamChecks;
import java.util.ArrayList;
import java.awt.BasicStroke;
import java.awt.Color;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.data.general.DatasetChangeListener;
import java.util.HashMap;
import org.jfree.chart.util.ShadowGenerator;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.CategoryAnchor;
import org.jfree.util.SortOrder;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import java.util.List;
import java.util.TreeMap;
import org.jfree.data.category.CategoryDataset;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import java.util.Map;
import org.jfree.ui.RectangleInsets;
import java.util.ResourceBundle;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
import org.jfree.chart.event.RendererChangeListener;
import org.jfree.chart.event.AnnotationChangeListener;
public class CategoryPlot extends Plot implements ValueAxisPlot, Pannable, Zoomable, AnnotationChangeListener, RendererChangeListener, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = -3537691700434728188L;
    public static final boolean DEFAULT_DOMAIN_GRIDLINES_VISIBLE = false;
    public static final boolean DEFAULT_RANGE_GRIDLINES_VISIBLE = true;
    public static final Stroke DEFAULT_GRIDLINE_STROKE;
    public static final Paint DEFAULT_GRIDLINE_PAINT;
    public static final Font DEFAULT_VALUE_LABEL_FONT;
    public static final boolean DEFAULT_CROSSHAIR_VISIBLE = false;
    public static final Stroke DEFAULT_CROSSHAIR_STROKE;
    public static final Paint DEFAULT_CROSSHAIR_PAINT;
    protected static ResourceBundle localizationResources;
    private PlotOrientation orientation;
    private RectangleInsets axisOffset;
    private Map<Integer, CategoryAxis> domainAxes;
    private Map<Integer, AxisLocation> domainAxisLocations;
    private boolean drawSharedDomainAxis;
    private Map<Integer, ValueAxis> rangeAxes;
    private Map<Integer, AxisLocation> rangeAxisLocations;
    private Map<Integer, CategoryDataset> datasets;
    private TreeMap<Integer, List<Integer>> datasetToDomainAxesMap;
    private TreeMap<Integer, List<Integer>> datasetToRangeAxesMap;
    private Map<Integer, CategoryItemRenderer> renderers;
    private DatasetRenderingOrder renderingOrder;
    private SortOrder columnRenderingOrder;
    private SortOrder rowRenderingOrder;
    private boolean domainGridlinesVisible;
    private CategoryAnchor domainGridlinePosition;
    private transient Stroke domainGridlineStroke;
    private transient Paint domainGridlinePaint;
    private boolean rangeZeroBaselineVisible;
    private transient Stroke rangeZeroBaselineStroke;
    private transient Paint rangeZeroBaselinePaint;
    private boolean rangeGridlinesVisible;
    private transient Stroke rangeGridlineStroke;
    private transient Paint rangeGridlinePaint;
    private boolean rangeMinorGridlinesVisible;
    private transient Stroke rangeMinorGridlineStroke;
    private transient Paint rangeMinorGridlinePaint;
    private double anchorValue;
    private int crosshairDatasetIndex;
    private boolean domainCrosshairVisible;
    private Comparable domainCrosshairRowKey;
    private Comparable domainCrosshairColumnKey;
    private transient Stroke domainCrosshairStroke;
    private transient Paint domainCrosshairPaint;
    private boolean rangeCrosshairVisible;
    private double rangeCrosshairValue;
    private transient Stroke rangeCrosshairStroke;
    private transient Paint rangeCrosshairPaint;
    private boolean rangeCrosshairLockedOnData;
    private Map foregroundDomainMarkers;
    private Map backgroundDomainMarkers;
    private Map foregroundRangeMarkers;
    private Map backgroundRangeMarkers;
    private List annotations;
    private int weight;
    private AxisSpace fixedDomainAxisSpace;
    private AxisSpace fixedRangeAxisSpace;
    private LegendItemCollection fixedLegendItems;
    private boolean rangePannable;
    private ShadowGenerator shadowGenerator;
    public CategoryPlot() {
        this ( null, null, null, null );
    }
    public CategoryPlot ( final CategoryDataset dataset, final CategoryAxis domainAxis, final ValueAxis rangeAxis, final CategoryItemRenderer renderer ) {
        this.renderingOrder = DatasetRenderingOrder.REVERSE;
        this.columnRenderingOrder = SortOrder.ASCENDING;
        this.rowRenderingOrder = SortOrder.ASCENDING;
        this.rangeCrosshairLockedOnData = true;
        this.orientation = PlotOrientation.VERTICAL;
        this.domainAxes = new HashMap<Integer, CategoryAxis>();
        this.domainAxisLocations = new HashMap<Integer, AxisLocation>();
        this.rangeAxes = new HashMap<Integer, ValueAxis>();
        this.rangeAxisLocations = new HashMap<Integer, AxisLocation>();
        this.datasetToDomainAxesMap = new TreeMap<Integer, List<Integer>>();
        this.datasetToRangeAxesMap = new TreeMap<Integer, List<Integer>>();
        this.renderers = new HashMap<Integer, CategoryItemRenderer>();
        ( this.datasets = new HashMap<Integer, CategoryDataset>() ).put ( 0, dataset );
        if ( dataset != null ) {
            dataset.addChangeListener ( this );
        }
        this.axisOffset = RectangleInsets.ZERO_INSETS;
        this.domainAxisLocations.put ( 0, AxisLocation.BOTTOM_OR_LEFT );
        this.rangeAxisLocations.put ( 0, AxisLocation.TOP_OR_LEFT );
        this.renderers.put ( 0, renderer );
        if ( renderer != null ) {
            renderer.setPlot ( this );
            renderer.addChangeListener ( this );
        }
        this.domainAxes.put ( 0, domainAxis );
        this.mapDatasetToDomainAxis ( 0, 0 );
        if ( domainAxis != null ) {
            domainAxis.setPlot ( this );
            domainAxis.addChangeListener ( this );
        }
        this.drawSharedDomainAxis = false;
        this.rangeAxes.put ( 0, rangeAxis );
        this.mapDatasetToRangeAxis ( 0, 0 );
        if ( rangeAxis != null ) {
            rangeAxis.setPlot ( this );
            rangeAxis.addChangeListener ( this );
        }
        this.configureDomainAxes();
        this.configureRangeAxes();
        this.domainGridlinesVisible = false;
        this.domainGridlinePosition = CategoryAnchor.MIDDLE;
        this.domainGridlineStroke = CategoryPlot.DEFAULT_GRIDLINE_STROKE;
        this.domainGridlinePaint = CategoryPlot.DEFAULT_GRIDLINE_PAINT;
        this.rangeZeroBaselineVisible = false;
        this.rangeZeroBaselinePaint = Color.black;
        this.rangeZeroBaselineStroke = new BasicStroke ( 0.5f );
        this.rangeGridlinesVisible = true;
        this.rangeGridlineStroke = CategoryPlot.DEFAULT_GRIDLINE_STROKE;
        this.rangeGridlinePaint = CategoryPlot.DEFAULT_GRIDLINE_PAINT;
        this.rangeMinorGridlinesVisible = false;
        this.rangeMinorGridlineStroke = CategoryPlot.DEFAULT_GRIDLINE_STROKE;
        this.rangeMinorGridlinePaint = Color.white;
        this.foregroundDomainMarkers = new HashMap();
        this.backgroundDomainMarkers = new HashMap();
        this.foregroundRangeMarkers = new HashMap();
        this.backgroundRangeMarkers = new HashMap();
        this.anchorValue = 0.0;
        this.domainCrosshairVisible = false;
        this.domainCrosshairStroke = CategoryPlot.DEFAULT_CROSSHAIR_STROKE;
        this.domainCrosshairPaint = CategoryPlot.DEFAULT_CROSSHAIR_PAINT;
        this.rangeCrosshairVisible = false;
        this.rangeCrosshairValue = 0.0;
        this.rangeCrosshairStroke = CategoryPlot.DEFAULT_CROSSHAIR_STROKE;
        this.rangeCrosshairPaint = CategoryPlot.DEFAULT_CROSSHAIR_PAINT;
        this.annotations = new ArrayList();
        this.rangePannable = false;
        this.shadowGenerator = null;
    }
    @Override
    public String getPlotType() {
        return CategoryPlot.localizationResources.getString ( "Category_Plot" );
    }
    @Override
    public PlotOrientation getOrientation() {
        return this.orientation;
    }
    public void setOrientation ( final PlotOrientation orientation ) {
        ParamChecks.nullNotPermitted ( orientation, "orientation" );
        this.orientation = orientation;
        this.fireChangeEvent();
    }
    public RectangleInsets getAxisOffset() {
        return this.axisOffset;
    }
    public void setAxisOffset ( final RectangleInsets offset ) {
        ParamChecks.nullNotPermitted ( offset, "offset" );
        this.axisOffset = offset;
        this.fireChangeEvent();
    }
    public CategoryAxis getDomainAxis() {
        return this.getDomainAxis ( 0 );
    }
    public CategoryAxis getDomainAxis ( final int index ) {
        CategoryAxis result = this.domainAxes.get ( index );
        if ( result == null ) {
            final Plot parent = this.getParent();
            if ( parent instanceof CategoryPlot ) {
                final CategoryPlot cp = ( CategoryPlot ) parent;
                result = cp.getDomainAxis ( index );
            }
        }
        return result;
    }
    public void setDomainAxis ( final CategoryAxis axis ) {
        this.setDomainAxis ( 0, axis );
    }
    public void setDomainAxis ( final int index, final CategoryAxis axis ) {
        this.setDomainAxis ( index, axis, true );
    }
    public void setDomainAxis ( final int index, final CategoryAxis axis, final boolean notify ) {
        final CategoryAxis existing = this.domainAxes.get ( index );
        if ( existing != null ) {
            existing.removeChangeListener ( this );
        }
        if ( axis != null ) {
            axis.setPlot ( this );
        }
        this.domainAxes.put ( index, axis );
        if ( axis != null ) {
            axis.configure();
            axis.addChangeListener ( this );
        }
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public void setDomainAxes ( final CategoryAxis[] axes ) {
        for ( int i = 0; i < axes.length; ++i ) {
            this.setDomainAxis ( i, axes[i], false );
        }
        this.fireChangeEvent();
    }
    public int getDomainAxisIndex ( final CategoryAxis axis ) {
        ParamChecks.nullNotPermitted ( axis, "axis" );
        for ( final Map.Entry<Integer, CategoryAxis> entry : this.domainAxes.entrySet() ) {
            if ( entry.getValue() == axis ) {
                return entry.getKey();
            }
        }
        return -1;
    }
    public AxisLocation getDomainAxisLocation() {
        return this.getDomainAxisLocation ( 0 );
    }
    public AxisLocation getDomainAxisLocation ( final int index ) {
        AxisLocation result = this.domainAxisLocations.get ( index );
        if ( result == null ) {
            result = AxisLocation.getOpposite ( this.getDomainAxisLocation ( 0 ) );
        }
        return result;
    }
    public void setDomainAxisLocation ( final AxisLocation location ) {
        this.setDomainAxisLocation ( 0, location, true );
    }
    public void setDomainAxisLocation ( final AxisLocation location, final boolean notify ) {
        this.setDomainAxisLocation ( 0, location, notify );
    }
    public void setDomainAxisLocation ( final int index, final AxisLocation location ) {
        this.setDomainAxisLocation ( index, location, true );
    }
    public void setDomainAxisLocation ( final int index, final AxisLocation location, final boolean notify ) {
        if ( index == 0 && location == null ) {
            throw new IllegalArgumentException ( "Null 'location' for index 0 not permitted." );
        }
        this.domainAxisLocations.put ( index, location );
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public RectangleEdge getDomainAxisEdge() {
        return this.getDomainAxisEdge ( 0 );
    }
    public RectangleEdge getDomainAxisEdge ( final int index ) {
        final AxisLocation location = this.getDomainAxisLocation ( index );
        RectangleEdge result;
        if ( location != null ) {
            result = Plot.resolveDomainAxisLocation ( location, this.orientation );
        } else {
            result = RectangleEdge.opposite ( this.getDomainAxisEdge ( 0 ) );
        }
        return result;
    }
    public int getDomainAxisCount() {
        return this.domainAxes.size();
    }
    public void clearDomainAxes() {
        for ( final CategoryAxis xAxis : this.domainAxes.values() ) {
            if ( xAxis != null ) {
                xAxis.removeChangeListener ( this );
            }
        }
        this.domainAxes.clear();
        this.fireChangeEvent();
    }
    public void configureDomainAxes() {
        for ( final CategoryAxis xAxis : this.domainAxes.values() ) {
            if ( xAxis != null ) {
                xAxis.configure();
            }
        }
    }
    public ValueAxis getRangeAxis() {
        return this.getRangeAxis ( 0 );
    }
    public ValueAxis getRangeAxis ( final int index ) {
        ValueAxis result = this.rangeAxes.get ( index );
        if ( result == null ) {
            final Plot parent = this.getParent();
            if ( parent instanceof CategoryPlot ) {
                final CategoryPlot cp = ( CategoryPlot ) parent;
                result = cp.getRangeAxis ( index );
            }
        }
        return result;
    }
    public void setRangeAxis ( final ValueAxis axis ) {
        this.setRangeAxis ( 0, axis );
    }
    public void setRangeAxis ( final int index, final ValueAxis axis ) {
        this.setRangeAxis ( index, axis, true );
    }
    public void setRangeAxis ( final int index, final ValueAxis axis, final boolean notify ) {
        final ValueAxis existing = this.rangeAxes.get ( index );
        if ( existing != null ) {
            existing.removeChangeListener ( this );
        }
        if ( axis != null ) {
            axis.setPlot ( this );
        }
        this.rangeAxes.put ( index, axis );
        if ( axis != null ) {
            axis.configure();
            axis.addChangeListener ( this );
        }
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public void setRangeAxes ( final ValueAxis[] axes ) {
        for ( int i = 0; i < axes.length; ++i ) {
            this.setRangeAxis ( i, axes[i], false );
        }
        this.fireChangeEvent();
    }
    public int getRangeAxisIndex ( final ValueAxis axis ) {
        ParamChecks.nullNotPermitted ( axis, "axis" );
        int result = this.findRangeAxisIndex ( axis );
        if ( result < 0 ) {
            final Plot parent = this.getParent();
            if ( parent instanceof CategoryPlot ) {
                final CategoryPlot p = ( CategoryPlot ) parent;
                result = p.getRangeAxisIndex ( axis );
            }
        }
        return result;
    }
    private int findRangeAxisIndex ( final ValueAxis axis ) {
        for ( final Map.Entry<Integer, ValueAxis> entry : this.rangeAxes.entrySet() ) {
            if ( entry.getValue() == axis ) {
                return entry.getKey();
            }
        }
        return -1;
    }
    public AxisLocation getRangeAxisLocation() {
        return this.getRangeAxisLocation ( 0 );
    }
    public AxisLocation getRangeAxisLocation ( final int index ) {
        AxisLocation result = this.rangeAxisLocations.get ( index );
        if ( result == null ) {
            result = AxisLocation.getOpposite ( this.getRangeAxisLocation ( 0 ) );
        }
        return result;
    }
    public void setRangeAxisLocation ( final AxisLocation location ) {
        this.setRangeAxisLocation ( location, true );
    }
    public void setRangeAxisLocation ( final AxisLocation location, final boolean notify ) {
        this.setRangeAxisLocation ( 0, location, notify );
    }
    public void setRangeAxisLocation ( final int index, final AxisLocation location ) {
        this.setRangeAxisLocation ( index, location, true );
    }
    public void setRangeAxisLocation ( final int index, final AxisLocation location, final boolean notify ) {
        if ( index == 0 && location == null ) {
            throw new IllegalArgumentException ( "Null 'location' for index 0 not permitted." );
        }
        this.rangeAxisLocations.put ( index, location );
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public RectangleEdge getRangeAxisEdge() {
        return this.getRangeAxisEdge ( 0 );
    }
    public RectangleEdge getRangeAxisEdge ( final int index ) {
        final AxisLocation location = this.getRangeAxisLocation ( index );
        return Plot.resolveRangeAxisLocation ( location, this.orientation );
    }
    public int getRangeAxisCount() {
        return this.rangeAxes.size();
    }
    public void clearRangeAxes() {
        for ( final ValueAxis yAxis : this.rangeAxes.values() ) {
            if ( yAxis != null ) {
                yAxis.removeChangeListener ( this );
            }
        }
        this.rangeAxes.clear();
        this.fireChangeEvent();
    }
    public void configureRangeAxes() {
        for ( final ValueAxis yAxis : this.rangeAxes.values() ) {
            if ( yAxis != null ) {
                yAxis.configure();
            }
        }
    }
    public CategoryDataset getDataset() {
        return this.getDataset ( 0 );
    }
    public CategoryDataset getDataset ( final int index ) {
        return this.datasets.get ( index );
    }
    public void setDataset ( final CategoryDataset dataset ) {
        this.setDataset ( 0, dataset );
    }
    public void setDataset ( final int index, final CategoryDataset dataset ) {
        final CategoryDataset existing = this.datasets.get ( index );
        if ( existing != null ) {
            existing.removeChangeListener ( this );
        }
        this.datasets.put ( index, dataset );
        if ( dataset != null ) {
            dataset.addChangeListener ( this );
        }
        final DatasetChangeEvent event = new DatasetChangeEvent ( this, dataset );
        this.datasetChanged ( event );
    }
    public int getDatasetCount() {
        return this.datasets.size();
    }
    public int indexOf ( final CategoryDataset dataset ) {
        for ( final Map.Entry<Integer, CategoryDataset> entry : this.datasets.entrySet() ) {
            if ( entry.getValue() == dataset ) {
                return entry.getKey();
            }
        }
        return -1;
    }
    public void mapDatasetToDomainAxis ( final int index, final int axisIndex ) {
        final List<Integer> axisIndices = new ArrayList<Integer> ( 1 );
        axisIndices.add ( axisIndex );
        this.mapDatasetToDomainAxes ( index, axisIndices );
    }
    public void mapDatasetToDomainAxes ( final int index, final List axisIndices ) {
        ParamChecks.requireNonNegative ( index, "index" );
        this.checkAxisIndices ( axisIndices );
        this.datasetToDomainAxesMap.put ( index, new ArrayList<Integer> ( axisIndices ) );
        this.datasetChanged ( new DatasetChangeEvent ( this, this.getDataset ( index ) ) );
    }
    private void checkAxisIndices ( final List indices ) {
        if ( indices == null ) {
            return;
        }
        final int count = indices.size();
        if ( count == 0 ) {
            throw new IllegalArgumentException ( "Empty list not permitted." );
        }
        final HashSet set = new HashSet();
        for ( int i = 0; i < count; ++i ) {
            final Object item = indices.get ( i );
            if ( ! ( item instanceof Integer ) ) {
                throw new IllegalArgumentException ( "Indices must be Integer instances." );
            }
            if ( set.contains ( item ) ) {
                throw new IllegalArgumentException ( "Indices must be unique." );
            }
            set.add ( item );
        }
    }
    public CategoryAxis getDomainAxisForDataset ( final int index ) {
        ParamChecks.requireNonNegative ( index, "index" );
        final List axisIndices = this.datasetToDomainAxesMap.get ( new Integer ( index ) );
        CategoryAxis axis;
        if ( axisIndices != null ) {
            final Integer axisIndex = axisIndices.get ( 0 );
            axis = this.getDomainAxis ( axisIndex );
        } else {
            axis = this.getDomainAxis ( 0 );
        }
        return axis;
    }
    public void mapDatasetToRangeAxis ( final int index, final int axisIndex ) {
        final List axisIndices = new ArrayList ( 1 );
        axisIndices.add ( new Integer ( axisIndex ) );
        this.mapDatasetToRangeAxes ( index, axisIndices );
    }
    public void mapDatasetToRangeAxes ( final int index, final List axisIndices ) {
        ParamChecks.requireNonNegative ( index, "index" );
        this.checkAxisIndices ( axisIndices );
        this.datasetToRangeAxesMap.put ( index, new ArrayList<Integer> ( axisIndices ) );
        this.datasetChanged ( new DatasetChangeEvent ( this, this.getDataset ( index ) ) );
    }
    public ValueAxis getRangeAxisForDataset ( final int index ) {
        ParamChecks.requireNonNegative ( index, "index" );
        final List axisIndices = this.datasetToRangeAxesMap.get ( new Integer ( index ) );
        ValueAxis axis;
        if ( axisIndices != null ) {
            final Integer axisIndex = axisIndices.get ( 0 );
            axis = this.getRangeAxis ( axisIndex );
        } else {
            axis = this.getRangeAxis ( 0 );
        }
        return axis;
    }
    public int getRendererCount() {
        return this.renderers.size();
    }
    public CategoryItemRenderer getRenderer() {
        return this.getRenderer ( 0 );
    }
    public CategoryItemRenderer getRenderer ( final int index ) {
        final CategoryItemRenderer renderer = this.renderers.get ( index );
        if ( renderer == null ) {
            return this.renderers.get ( 0 );
        }
        return renderer;
    }
    public void setRenderer ( final CategoryItemRenderer renderer ) {
        this.setRenderer ( 0, renderer, true );
    }
    public void setRenderer ( final CategoryItemRenderer renderer, final boolean notify ) {
        this.setRenderer ( 0, renderer, notify );
    }
    public void setRenderer ( final int index, final CategoryItemRenderer renderer ) {
        this.setRenderer ( index, renderer, true );
    }
    public void setRenderer ( final int index, final CategoryItemRenderer renderer, final boolean notify ) {
        final CategoryItemRenderer existing = this.renderers.get ( index );
        if ( existing != null ) {
            existing.removeChangeListener ( this );
        }
        this.renderers.put ( index, renderer );
        if ( renderer != null ) {
            renderer.setPlot ( this );
            renderer.addChangeListener ( this );
        }
        this.configureDomainAxes();
        this.configureRangeAxes();
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public void setRenderers ( final CategoryItemRenderer[] renderers ) {
        for ( int i = 0; i < renderers.length; ++i ) {
            this.setRenderer ( i, renderers[i], false );
        }
        this.fireChangeEvent();
    }
    public CategoryItemRenderer getRendererForDataset ( final CategoryDataset dataset ) {
        final int datasetIndex = this.indexOf ( dataset );
        if ( datasetIndex < 0 ) {
            return null;
        }
        final CategoryItemRenderer renderer = this.renderers.get ( datasetIndex );
        if ( renderer == null ) {
            return this.getRenderer();
        }
        return renderer;
    }
    public int getIndexOf ( final CategoryItemRenderer renderer ) {
        for ( final Map.Entry<Integer, CategoryItemRenderer> entry : this.renderers.entrySet() ) {
            if ( entry.getValue() == renderer ) {
                return entry.getKey();
            }
        }
        return -1;
    }
    public DatasetRenderingOrder getDatasetRenderingOrder() {
        return this.renderingOrder;
    }
    public void setDatasetRenderingOrder ( final DatasetRenderingOrder order ) {
        ParamChecks.nullNotPermitted ( order, "order" );
        this.renderingOrder = order;
        this.fireChangeEvent();
    }
    public SortOrder getColumnRenderingOrder() {
        return this.columnRenderingOrder;
    }
    public void setColumnRenderingOrder ( final SortOrder order ) {
        ParamChecks.nullNotPermitted ( order, "order" );
        this.columnRenderingOrder = order;
        this.fireChangeEvent();
    }
    public SortOrder getRowRenderingOrder() {
        return this.rowRenderingOrder;
    }
    public void setRowRenderingOrder ( final SortOrder order ) {
        ParamChecks.nullNotPermitted ( order, "order" );
        this.rowRenderingOrder = order;
        this.fireChangeEvent();
    }
    public boolean isDomainGridlinesVisible() {
        return this.domainGridlinesVisible;
    }
    public void setDomainGridlinesVisible ( final boolean visible ) {
        if ( this.domainGridlinesVisible != visible ) {
            this.domainGridlinesVisible = visible;
            this.fireChangeEvent();
        }
    }
    public CategoryAnchor getDomainGridlinePosition() {
        return this.domainGridlinePosition;
    }
    public void setDomainGridlinePosition ( final CategoryAnchor position ) {
        ParamChecks.nullNotPermitted ( position, "position" );
        this.domainGridlinePosition = position;
        this.fireChangeEvent();
    }
    public Stroke getDomainGridlineStroke() {
        return this.domainGridlineStroke;
    }
    public void setDomainGridlineStroke ( final Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.domainGridlineStroke = stroke;
        this.fireChangeEvent();
    }
    public Paint getDomainGridlinePaint() {
        return this.domainGridlinePaint;
    }
    public void setDomainGridlinePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.domainGridlinePaint = paint;
        this.fireChangeEvent();
    }
    public boolean isRangeZeroBaselineVisible() {
        return this.rangeZeroBaselineVisible;
    }
    public void setRangeZeroBaselineVisible ( final boolean visible ) {
        this.rangeZeroBaselineVisible = visible;
        this.fireChangeEvent();
    }
    public Stroke getRangeZeroBaselineStroke() {
        return this.rangeZeroBaselineStroke;
    }
    public void setRangeZeroBaselineStroke ( final Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.rangeZeroBaselineStroke = stroke;
        this.fireChangeEvent();
    }
    public Paint getRangeZeroBaselinePaint() {
        return this.rangeZeroBaselinePaint;
    }
    public void setRangeZeroBaselinePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.rangeZeroBaselinePaint = paint;
        this.fireChangeEvent();
    }
    public boolean isRangeGridlinesVisible() {
        return this.rangeGridlinesVisible;
    }
    public void setRangeGridlinesVisible ( final boolean visible ) {
        if ( this.rangeGridlinesVisible != visible ) {
            this.rangeGridlinesVisible = visible;
            this.fireChangeEvent();
        }
    }
    public Stroke getRangeGridlineStroke() {
        return this.rangeGridlineStroke;
    }
    public void setRangeGridlineStroke ( final Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.rangeGridlineStroke = stroke;
        this.fireChangeEvent();
    }
    public Paint getRangeGridlinePaint() {
        return this.rangeGridlinePaint;
    }
    public void setRangeGridlinePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.rangeGridlinePaint = paint;
        this.fireChangeEvent();
    }
    public boolean isRangeMinorGridlinesVisible() {
        return this.rangeMinorGridlinesVisible;
    }
    public void setRangeMinorGridlinesVisible ( final boolean visible ) {
        if ( this.rangeMinorGridlinesVisible != visible ) {
            this.rangeMinorGridlinesVisible = visible;
            this.fireChangeEvent();
        }
    }
    public Stroke getRangeMinorGridlineStroke() {
        return this.rangeMinorGridlineStroke;
    }
    public void setRangeMinorGridlineStroke ( final Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.rangeMinorGridlineStroke = stroke;
        this.fireChangeEvent();
    }
    public Paint getRangeMinorGridlinePaint() {
        return this.rangeMinorGridlinePaint;
    }
    public void setRangeMinorGridlinePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.rangeMinorGridlinePaint = paint;
        this.fireChangeEvent();
    }
    public LegendItemCollection getFixedLegendItems() {
        return this.fixedLegendItems;
    }
    public void setFixedLegendItems ( final LegendItemCollection items ) {
        this.fixedLegendItems = items;
        this.fireChangeEvent();
    }
    @Override
    public LegendItemCollection getLegendItems() {
        if ( this.fixedLegendItems != null ) {
            return this.fixedLegendItems;
        }
        final LegendItemCollection result = new LegendItemCollection();
        for ( final CategoryDataset dataset : this.datasets.values() ) {
            if ( dataset != null ) {
                final int datasetIndex = this.indexOf ( dataset );
                final CategoryItemRenderer renderer = this.getRenderer ( datasetIndex );
                if ( renderer == null ) {
                    continue;
                }
                result.addAll ( renderer.getLegendItems() );
            }
        }
        return result;
    }
    @Override
    public void handleClick ( final int x, final int y, final PlotRenderingInfo info ) {
        final Rectangle2D dataArea = info.getDataArea();
        if ( dataArea.contains ( x, y ) ) {
            double java2D = 0.0;
            if ( this.orientation == PlotOrientation.HORIZONTAL ) {
                java2D = x;
            } else if ( this.orientation == PlotOrientation.VERTICAL ) {
                java2D = y;
            }
            final RectangleEdge edge = Plot.resolveRangeAxisLocation ( this.getRangeAxisLocation(), this.orientation );
            final double value = this.getRangeAxis().java2DToValue ( java2D, info.getDataArea(), edge );
            this.setAnchorValue ( value );
            this.setRangeCrosshairValue ( value );
        }
    }
    @Override
    public void zoom ( final double percent ) {
        if ( percent > 0.0 ) {
            final double range = this.getRangeAxis().getRange().getLength();
            final double scaledRange = range * percent;
            this.getRangeAxis().setRange ( this.anchorValue - scaledRange / 2.0, this.anchorValue + scaledRange / 2.0 );
        } else {
            this.getRangeAxis().setAutoRange ( true );
        }
    }
    @Override
    public void annotationChanged ( final AnnotationChangeEvent event ) {
        if ( this.getParent() != null ) {
            this.getParent().annotationChanged ( event );
        } else {
            final PlotChangeEvent e = new PlotChangeEvent ( this );
            this.notifyListeners ( e );
        }
    }
    @Override
    public void datasetChanged ( final DatasetChangeEvent event ) {
        for ( final ValueAxis yAxis : this.rangeAxes.values() ) {
            if ( yAxis != null ) {
                yAxis.configure();
            }
        }
        if ( this.getParent() != null ) {
            this.getParent().datasetChanged ( event );
        } else {
            final PlotChangeEvent e = new PlotChangeEvent ( this );
            e.setType ( ChartChangeEventType.DATASET_UPDATED );
            this.notifyListeners ( e );
        }
    }
    @Override
    public void rendererChanged ( final RendererChangeEvent event ) {
        final Plot parent = this.getParent();
        if ( parent != null ) {
            if ( ! ( parent instanceof RendererChangeListener ) ) {
                throw new RuntimeException ( "The renderer has changed and I don't know what to do!" );
            }
            final RendererChangeListener rcl = ( RendererChangeListener ) parent;
            rcl.rendererChanged ( event );
        } else {
            this.configureRangeAxes();
            final PlotChangeEvent e = new PlotChangeEvent ( this );
            this.notifyListeners ( e );
        }
    }
    public void addDomainMarker ( final CategoryMarker marker ) {
        this.addDomainMarker ( marker, Layer.FOREGROUND );
    }
    public void addDomainMarker ( final CategoryMarker marker, final Layer layer ) {
        this.addDomainMarker ( 0, marker, layer );
    }
    public void addDomainMarker ( final int index, final CategoryMarker marker, final Layer layer ) {
        this.addDomainMarker ( index, marker, layer, true );
    }
    public void addDomainMarker ( final int index, final CategoryMarker marker, final Layer layer, final boolean notify ) {
        ParamChecks.nullNotPermitted ( marker, "marker" );
        ParamChecks.nullNotPermitted ( layer, "layer" );
        if ( layer == Layer.FOREGROUND ) {
            Collection markers = this.foregroundDomainMarkers.get ( new Integer ( index ) );
            if ( markers == null ) {
                markers = new ArrayList();
                this.foregroundDomainMarkers.put ( new Integer ( index ), markers );
            }
            markers.add ( marker );
        } else if ( layer == Layer.BACKGROUND ) {
            Collection markers = this.backgroundDomainMarkers.get ( new Integer ( index ) );
            if ( markers == null ) {
                markers = new ArrayList();
                this.backgroundDomainMarkers.put ( new Integer ( index ), markers );
            }
            markers.add ( marker );
        }
        marker.addChangeListener ( this );
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public void clearDomainMarkers() {
        if ( this.backgroundDomainMarkers != null ) {
            final Set keys = this.backgroundDomainMarkers.keySet();
            for ( final Integer key : keys ) {
                this.clearDomainMarkers ( key );
            }
            this.backgroundDomainMarkers.clear();
        }
        if ( this.foregroundDomainMarkers != null ) {
            final Set keys = this.foregroundDomainMarkers.keySet();
            for ( final Integer key : keys ) {
                this.clearDomainMarkers ( key );
            }
            this.foregroundDomainMarkers.clear();
        }
        this.fireChangeEvent();
    }
    public Collection getDomainMarkers ( final Layer layer ) {
        return this.getDomainMarkers ( 0, layer );
    }
    public Collection getDomainMarkers ( final int index, final Layer layer ) {
        Collection result = null;
        final Integer key = new Integer ( index );
        if ( layer == Layer.FOREGROUND ) {
            result = this.foregroundDomainMarkers.get ( key );
        } else if ( layer == Layer.BACKGROUND ) {
            result = this.backgroundDomainMarkers.get ( key );
        }
        if ( result != null ) {
            result = Collections.unmodifiableCollection ( ( Collection<?> ) result );
        }
        return result;
    }
    public void clearDomainMarkers ( final int index ) {
        final Integer key = new Integer ( index );
        if ( this.backgroundDomainMarkers != null ) {
            final Collection markers = this.backgroundDomainMarkers.get ( key );
            if ( markers != null ) {
                for ( final Marker m : markers ) {
                    m.removeChangeListener ( this );
                }
                markers.clear();
            }
        }
        if ( this.foregroundDomainMarkers != null ) {
            final Collection markers = this.foregroundDomainMarkers.get ( key );
            if ( markers != null ) {
                for ( final Marker m : markers ) {
                    m.removeChangeListener ( this );
                }
                markers.clear();
            }
        }
        this.fireChangeEvent();
    }
    public boolean removeDomainMarker ( final Marker marker ) {
        return this.removeDomainMarker ( marker, Layer.FOREGROUND );
    }
    public boolean removeDomainMarker ( final Marker marker, final Layer layer ) {
        return this.removeDomainMarker ( 0, marker, layer );
    }
    public boolean removeDomainMarker ( final int index, final Marker marker, final Layer layer ) {
        return this.removeDomainMarker ( index, marker, layer, true );
    }
    public boolean removeDomainMarker ( final int index, final Marker marker, final Layer layer, final boolean notify ) {
        ArrayList markers;
        if ( layer == Layer.FOREGROUND ) {
            markers = this.foregroundDomainMarkers.get ( new Integer ( index ) );
        } else {
            markers = this.backgroundDomainMarkers.get ( new Integer ( index ) );
        }
        if ( markers == null ) {
            return false;
        }
        final boolean removed = markers.remove ( marker );
        if ( removed && notify ) {
            this.fireChangeEvent();
        }
        return removed;
    }
    public void addRangeMarker ( final Marker marker ) {
        this.addRangeMarker ( marker, Layer.FOREGROUND );
    }
    public void addRangeMarker ( final Marker marker, final Layer layer ) {
        this.addRangeMarker ( 0, marker, layer );
    }
    public void addRangeMarker ( final int index, final Marker marker, final Layer layer ) {
        this.addRangeMarker ( index, marker, layer, true );
    }
    public void addRangeMarker ( final int index, final Marker marker, final Layer layer, final boolean notify ) {
        if ( layer == Layer.FOREGROUND ) {
            Collection markers = this.foregroundRangeMarkers.get ( new Integer ( index ) );
            if ( markers == null ) {
                markers = new ArrayList();
                this.foregroundRangeMarkers.put ( new Integer ( index ), markers );
            }
            markers.add ( marker );
        } else if ( layer == Layer.BACKGROUND ) {
            Collection markers = this.backgroundRangeMarkers.get ( new Integer ( index ) );
            if ( markers == null ) {
                markers = new ArrayList();
                this.backgroundRangeMarkers.put ( new Integer ( index ), markers );
            }
            markers.add ( marker );
        }
        marker.addChangeListener ( this );
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public void clearRangeMarkers() {
        if ( this.backgroundRangeMarkers != null ) {
            final Set keys = this.backgroundRangeMarkers.keySet();
            for ( final Integer key : keys ) {
                this.clearRangeMarkers ( key );
            }
            this.backgroundRangeMarkers.clear();
        }
        if ( this.foregroundRangeMarkers != null ) {
            final Set keys = this.foregroundRangeMarkers.keySet();
            for ( final Integer key : keys ) {
                this.clearRangeMarkers ( key );
            }
            this.foregroundRangeMarkers.clear();
        }
        this.fireChangeEvent();
    }
    public Collection getRangeMarkers ( final Layer layer ) {
        return this.getRangeMarkers ( 0, layer );
    }
    public Collection getRangeMarkers ( final int index, final Layer layer ) {
        Collection result = null;
        final Integer key = new Integer ( index );
        if ( layer == Layer.FOREGROUND ) {
            result = this.foregroundRangeMarkers.get ( key );
        } else if ( layer == Layer.BACKGROUND ) {
            result = this.backgroundRangeMarkers.get ( key );
        }
        if ( result != null ) {
            result = Collections.unmodifiableCollection ( ( Collection<?> ) result );
        }
        return result;
    }
    public void clearRangeMarkers ( final int index ) {
        final Integer key = new Integer ( index );
        if ( this.backgroundRangeMarkers != null ) {
            final Collection markers = this.backgroundRangeMarkers.get ( key );
            if ( markers != null ) {
                for ( final Marker m : markers ) {
                    m.removeChangeListener ( this );
                }
                markers.clear();
            }
        }
        if ( this.foregroundRangeMarkers != null ) {
            final Collection markers = this.foregroundRangeMarkers.get ( key );
            if ( markers != null ) {
                for ( final Marker m : markers ) {
                    m.removeChangeListener ( this );
                }
                markers.clear();
            }
        }
        this.fireChangeEvent();
    }
    public boolean removeRangeMarker ( final Marker marker ) {
        return this.removeRangeMarker ( marker, Layer.FOREGROUND );
    }
    public boolean removeRangeMarker ( final Marker marker, final Layer layer ) {
        return this.removeRangeMarker ( 0, marker, layer );
    }
    public boolean removeRangeMarker ( final int index, final Marker marker, final Layer layer ) {
        return this.removeRangeMarker ( index, marker, layer, true );
    }
    public boolean removeRangeMarker ( final int index, final Marker marker, final Layer layer, final boolean notify ) {
        ParamChecks.nullNotPermitted ( marker, "marker" );
        ArrayList markers;
        if ( layer == Layer.FOREGROUND ) {
            markers = this.foregroundRangeMarkers.get ( new Integer ( index ) );
        } else {
            markers = this.backgroundRangeMarkers.get ( new Integer ( index ) );
        }
        if ( markers == null ) {
            return false;
        }
        final boolean removed = markers.remove ( marker );
        if ( removed && notify ) {
            this.fireChangeEvent();
        }
        return removed;
    }
    public boolean isDomainCrosshairVisible() {
        return this.domainCrosshairVisible;
    }
    public void setDomainCrosshairVisible ( final boolean flag ) {
        if ( this.domainCrosshairVisible != flag ) {
            this.domainCrosshairVisible = flag;
            this.fireChangeEvent();
        }
    }
    public Comparable getDomainCrosshairRowKey() {
        return this.domainCrosshairRowKey;
    }
    public void setDomainCrosshairRowKey ( final Comparable key ) {
        this.setDomainCrosshairRowKey ( key, true );
    }
    public void setDomainCrosshairRowKey ( final Comparable key, final boolean notify ) {
        this.domainCrosshairRowKey = key;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public Comparable getDomainCrosshairColumnKey() {
        return this.domainCrosshairColumnKey;
    }
    public void setDomainCrosshairColumnKey ( final Comparable key ) {
        this.setDomainCrosshairColumnKey ( key, true );
    }
    public void setDomainCrosshairColumnKey ( final Comparable key, final boolean notify ) {
        this.domainCrosshairColumnKey = key;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public int getCrosshairDatasetIndex() {
        return this.crosshairDatasetIndex;
    }
    public void setCrosshairDatasetIndex ( final int index ) {
        this.setCrosshairDatasetIndex ( index, true );
    }
    public void setCrosshairDatasetIndex ( final int index, final boolean notify ) {
        this.crosshairDatasetIndex = index;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public Paint getDomainCrosshairPaint() {
        return this.domainCrosshairPaint;
    }
    public void setDomainCrosshairPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.domainCrosshairPaint = paint;
        this.fireChangeEvent();
    }
    public Stroke getDomainCrosshairStroke() {
        return this.domainCrosshairStroke;
    }
    public void setDomainCrosshairStroke ( final Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.domainCrosshairStroke = stroke;
    }
    public boolean isRangeCrosshairVisible() {
        return this.rangeCrosshairVisible;
    }
    public void setRangeCrosshairVisible ( final boolean flag ) {
        if ( this.rangeCrosshairVisible != flag ) {
            this.rangeCrosshairVisible = flag;
            this.fireChangeEvent();
        }
    }
    public boolean isRangeCrosshairLockedOnData() {
        return this.rangeCrosshairLockedOnData;
    }
    public void setRangeCrosshairLockedOnData ( final boolean flag ) {
        if ( this.rangeCrosshairLockedOnData != flag ) {
            this.rangeCrosshairLockedOnData = flag;
            this.fireChangeEvent();
        }
    }
    public double getRangeCrosshairValue() {
        return this.rangeCrosshairValue;
    }
    public void setRangeCrosshairValue ( final double value ) {
        this.setRangeCrosshairValue ( value, true );
    }
    public void setRangeCrosshairValue ( final double value, final boolean notify ) {
        this.rangeCrosshairValue = value;
        if ( this.isRangeCrosshairVisible() && notify ) {
            this.fireChangeEvent();
        }
    }
    public Stroke getRangeCrosshairStroke() {
        return this.rangeCrosshairStroke;
    }
    public void setRangeCrosshairStroke ( final Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.rangeCrosshairStroke = stroke;
        this.fireChangeEvent();
    }
    public Paint getRangeCrosshairPaint() {
        return this.rangeCrosshairPaint;
    }
    public void setRangeCrosshairPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.rangeCrosshairPaint = paint;
        this.fireChangeEvent();
    }
    public List getAnnotations() {
        return this.annotations;
    }
    public void addAnnotation ( final CategoryAnnotation annotation ) {
        this.addAnnotation ( annotation, true );
    }
    public void addAnnotation ( final CategoryAnnotation annotation, final boolean notify ) {
        ParamChecks.nullNotPermitted ( annotation, "annotation" );
        this.annotations.add ( annotation );
        annotation.addChangeListener ( this );
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public boolean removeAnnotation ( final CategoryAnnotation annotation ) {
        return this.removeAnnotation ( annotation, true );
    }
    public boolean removeAnnotation ( final CategoryAnnotation annotation, final boolean notify ) {
        ParamChecks.nullNotPermitted ( annotation, "annotation" );
        final boolean removed = this.annotations.remove ( annotation );
        annotation.removeChangeListener ( this );
        if ( removed && notify ) {
            this.fireChangeEvent();
        }
        return removed;
    }
    public void clearAnnotations() {
        for ( int i = 0; i < this.annotations.size(); ++i ) {
            final CategoryAnnotation annotation = this.annotations.get ( i );
            annotation.removeChangeListener ( this );
        }
        this.annotations.clear();
        this.fireChangeEvent();
    }
    public ShadowGenerator getShadowGenerator() {
        return this.shadowGenerator;
    }
    public void setShadowGenerator ( final ShadowGenerator generator ) {
        this.shadowGenerator = generator;
        this.fireChangeEvent();
    }
    protected AxisSpace calculateDomainAxisSpace ( final Graphics2D g2, final Rectangle2D plotArea, AxisSpace space ) {
        if ( space == null ) {
            space = new AxisSpace();
        }
        if ( this.fixedDomainAxisSpace != null ) {
            if ( this.orientation.isHorizontal() ) {
                space.ensureAtLeast ( this.fixedDomainAxisSpace.getLeft(), RectangleEdge.LEFT );
                space.ensureAtLeast ( this.fixedDomainAxisSpace.getRight(), RectangleEdge.RIGHT );
            } else if ( this.orientation.isVertical() ) {
                space.ensureAtLeast ( this.fixedDomainAxisSpace.getTop(), RectangleEdge.TOP );
                space.ensureAtLeast ( this.fixedDomainAxisSpace.getBottom(), RectangleEdge.BOTTOM );
            }
        } else {
            final RectangleEdge domainEdge = Plot.resolveDomainAxisLocation ( this.getDomainAxisLocation(), this.orientation );
            if ( this.drawSharedDomainAxis ) {
                space = this.getDomainAxis().reserveSpace ( g2, this, plotArea, domainEdge, space );
            }
            for ( final CategoryAxis xAxis : this.domainAxes.values() ) {
                if ( xAxis != null ) {
                    final int i = this.getDomainAxisIndex ( xAxis );
                    final RectangleEdge edge = this.getDomainAxisEdge ( i );
                    space = xAxis.reserveSpace ( g2, this, plotArea, edge, space );
                }
            }
        }
        return space;
    }
    protected AxisSpace calculateRangeAxisSpace ( final Graphics2D g2, final Rectangle2D plotArea, AxisSpace space ) {
        if ( space == null ) {
            space = new AxisSpace();
        }
        if ( this.fixedRangeAxisSpace != null ) {
            if ( this.orientation.isHorizontal() ) {
                space.ensureAtLeast ( this.fixedRangeAxisSpace.getTop(), RectangleEdge.TOP );
                space.ensureAtLeast ( this.fixedRangeAxisSpace.getBottom(), RectangleEdge.BOTTOM );
            } else if ( this.orientation == PlotOrientation.VERTICAL ) {
                space.ensureAtLeast ( this.fixedRangeAxisSpace.getLeft(), RectangleEdge.LEFT );
                space.ensureAtLeast ( this.fixedRangeAxisSpace.getRight(), RectangleEdge.RIGHT );
            }
        } else {
            for ( final ValueAxis yAxis : this.rangeAxes.values() ) {
                if ( yAxis != null ) {
                    final int i = this.findRangeAxisIndex ( yAxis );
                    final RectangleEdge edge = this.getRangeAxisEdge ( i );
                    space = yAxis.reserveSpace ( g2, this, plotArea, edge, space );
                }
            }
        }
        return space;
    }
    private Rectangle integerise ( final Rectangle2D rect ) {
        final int x0 = ( int ) Math.ceil ( rect.getMinX() );
        final int y0 = ( int ) Math.ceil ( rect.getMinY() );
        final int x = ( int ) Math.floor ( rect.getMaxX() );
        final int y = ( int ) Math.floor ( rect.getMaxY() );
        return new Rectangle ( x0, y0, x - x0, y - y0 );
    }
    protected AxisSpace calculateAxisSpace ( final Graphics2D g2, final Rectangle2D plotArea ) {
        AxisSpace space = new AxisSpace();
        space = this.calculateRangeAxisSpace ( g2, plotArea, space );
        space = this.calculateDomainAxisSpace ( g2, plotArea, space );
        return space;
    }
    @Override
    public void draw ( Graphics2D g2, final Rectangle2D area, Point2D anchor, final PlotState parentState, PlotRenderingInfo state ) {
        final boolean b1 = area.getWidth() <= 10.0;
        final boolean b2 = area.getHeight() <= 10.0;
        if ( b1 || b2 ) {
            return;
        }
        if ( state == null ) {
            state = new PlotRenderingInfo ( null );
        }
        state.setPlotArea ( area );
        final RectangleInsets insets = this.getInsets();
        insets.trim ( area );
        final AxisSpace space = this.calculateAxisSpace ( g2, area );
        Rectangle2D dataArea = space.shrink ( area, null );
        this.axisOffset.trim ( dataArea );
        dataArea = this.integerise ( dataArea );
        if ( dataArea.isEmpty() ) {
            return;
        }
        state.setDataArea ( dataArea );
        this.createAndAddEntity ( ( Rectangle2D ) dataArea.clone(), state, null, null );
        if ( this.getRenderer() != null ) {
            this.getRenderer().drawBackground ( g2, this, dataArea );
        } else {
            this.drawBackground ( g2, dataArea );
        }
        final Map axisStateMap = this.drawAxes ( g2, area, dataArea, state );
        if ( anchor != null && !dataArea.contains ( anchor ) ) {
            anchor = ShapeUtilities.getPointInRectangle ( anchor.getX(), anchor.getY(), dataArea );
        }
        final CategoryCrosshairState crosshairState = new CategoryCrosshairState();
        crosshairState.setCrosshairDistance ( Double.POSITIVE_INFINITY );
        crosshairState.setAnchor ( anchor );
        crosshairState.setAnchorX ( Double.NaN );
        crosshairState.setAnchorY ( Double.NaN );
        if ( anchor != null ) {
            final ValueAxis rangeAxis = this.getRangeAxis();
            if ( rangeAxis != null ) {
                double y;
                if ( this.getOrientation() == PlotOrientation.VERTICAL ) {
                    y = rangeAxis.java2DToValue ( anchor.getY(), dataArea, this.getRangeAxisEdge() );
                } else {
                    y = rangeAxis.java2DToValue ( anchor.getX(), dataArea, this.getRangeAxisEdge() );
                }
                crosshairState.setAnchorY ( y );
            }
        }
        crosshairState.setRowKey ( this.getDomainCrosshairRowKey() );
        crosshairState.setColumnKey ( this.getDomainCrosshairColumnKey() );
        crosshairState.setCrosshairY ( this.getRangeCrosshairValue() );
        final Shape savedClip = g2.getClip();
        g2.clip ( dataArea );
        this.drawDomainGridlines ( g2, dataArea );
        AxisState rangeAxisState = axisStateMap.get ( this.getRangeAxis() );
        if ( rangeAxisState == null && parentState != null ) {
            rangeAxisState = parentState.getSharedAxisStates().get ( this.getRangeAxis() );
        }
        if ( rangeAxisState != null ) {
            this.drawRangeGridlines ( g2, dataArea, rangeAxisState.getTicks() );
            this.drawZeroRangeBaseline ( g2, dataArea );
        }
        final Graphics2D savedG2 = g2;
        BufferedImage dataImage = null;
        final boolean suppressShadow = Boolean.TRUE.equals ( g2.getRenderingHint ( JFreeChart.KEY_SUPPRESS_SHADOW_GENERATION ) );
        if ( this.shadowGenerator != null && !suppressShadow ) {
            dataImage = new BufferedImage ( ( int ) dataArea.getWidth(), ( int ) dataArea.getHeight(), 2 );
            g2 = dataImage.createGraphics();
            g2.translate ( -dataArea.getX(), -dataArea.getY() );
            g2.setRenderingHints ( savedG2.getRenderingHints() );
        }
        for ( final CategoryItemRenderer renderer : this.renderers.values() ) {
            final int i = this.getIndexOf ( renderer );
            this.drawDomainMarkers ( g2, dataArea, i, Layer.BACKGROUND );
        }
        for ( final CategoryItemRenderer renderer : this.renderers.values() ) {
            final int i = this.getIndexOf ( renderer );
            this.drawRangeMarkers ( g2, dataArea, i, Layer.BACKGROUND );
        }
        boolean foundData = false;
        final Composite originalComposite = g2.getComposite();
        g2.setComposite ( AlphaComposite.getInstance ( 3, this.getForegroundAlpha() ) );
        final DatasetRenderingOrder order = this.getDatasetRenderingOrder();
        final List<Integer> datasetIndices = this.getDatasetIndices ( order );
        for ( final int j : datasetIndices ) {
            foundData = ( this.render ( g2, dataArea, j, state, crosshairState ) || foundData );
        }
        final List<Integer> rendererIndices = this.getRendererIndices ( order );
        for ( final int k : rendererIndices ) {
            this.drawDomainMarkers ( g2, dataArea, k, Layer.FOREGROUND );
        }
        for ( final int k : rendererIndices ) {
            this.drawRangeMarkers ( g2, dataArea, k, Layer.FOREGROUND );
        }
        this.drawAnnotations ( g2, dataArea );
        if ( this.shadowGenerator != null && !suppressShadow ) {
            final BufferedImage shadowImage = this.shadowGenerator.createDropShadow ( dataImage );
            g2 = savedG2;
            g2.drawImage ( shadowImage, ( int ) dataArea.getX() + this.shadowGenerator.calculateOffsetX(), ( int ) dataArea.getY() + this.shadowGenerator.calculateOffsetY(), null );
            g2.drawImage ( dataImage, ( int ) dataArea.getX(), ( int ) dataArea.getY(), null );
        }
        g2.setClip ( savedClip );
        g2.setComposite ( originalComposite );
        if ( !foundData ) {
            this.drawNoDataMessage ( g2, dataArea );
        }
        final int datasetIndex = crosshairState.getDatasetIndex();
        this.setCrosshairDatasetIndex ( datasetIndex, false );
        final Comparable rowKey = crosshairState.getRowKey();
        final Comparable columnKey = crosshairState.getColumnKey();
        this.setDomainCrosshairRowKey ( rowKey, false );
        this.setDomainCrosshairColumnKey ( columnKey, false );
        if ( this.isDomainCrosshairVisible() && columnKey != null ) {
            final Paint paint = this.getDomainCrosshairPaint();
            final Stroke stroke = this.getDomainCrosshairStroke();
            this.drawDomainCrosshair ( g2, dataArea, this.orientation, datasetIndex, rowKey, columnKey, stroke, paint );
        }
        final ValueAxis yAxis = this.getRangeAxisForDataset ( datasetIndex );
        final RectangleEdge yAxisEdge = this.getRangeAxisEdge();
        if ( !this.rangeCrosshairLockedOnData && anchor != null ) {
            double yy;
            if ( this.getOrientation() == PlotOrientation.VERTICAL ) {
                yy = yAxis.java2DToValue ( anchor.getY(), dataArea, yAxisEdge );
            } else {
                yy = yAxis.java2DToValue ( anchor.getX(), dataArea, yAxisEdge );
            }
            crosshairState.setCrosshairY ( yy );
        }
        this.setRangeCrosshairValue ( crosshairState.getCrosshairY(), false );
        if ( this.isRangeCrosshairVisible() ) {
            final double y2 = this.getRangeCrosshairValue();
            final Paint paint2 = this.getRangeCrosshairPaint();
            final Stroke stroke2 = this.getRangeCrosshairStroke();
            this.drawRangeCrosshair ( g2, dataArea, this.getOrientation(), y2, yAxis, stroke2, paint2 );
        }
        if ( this.isOutlineVisible() ) {
            if ( this.getRenderer() != null ) {
                this.getRenderer().drawOutline ( g2, this, dataArea );
            } else {
                this.drawOutline ( g2, dataArea );
            }
        }
    }
    private List<Integer> getDatasetIndices ( final DatasetRenderingOrder order ) {
        final List<Integer> result = new ArrayList<Integer>();
        for ( final Map.Entry<Integer, CategoryDataset> entry : this.datasets.entrySet() ) {
            if ( entry.getValue() != null ) {
                result.add ( entry.getKey() );
            }
        }
        Collections.sort ( result );
        if ( order == DatasetRenderingOrder.REVERSE ) {
            Collections.reverse ( result );
        }
        return result;
    }
    private List<Integer> getRendererIndices ( final DatasetRenderingOrder order ) {
        final List<Integer> result = new ArrayList<Integer>();
        for ( final Map.Entry<Integer, CategoryItemRenderer> entry : this.renderers.entrySet() ) {
            if ( entry.getValue() != null ) {
                result.add ( entry.getKey() );
            }
        }
        Collections.sort ( result );
        if ( order == DatasetRenderingOrder.REVERSE ) {
            Collections.reverse ( result );
        }
        return result;
    }
    @Override
    public void drawBackground ( final Graphics2D g2, final Rectangle2D area ) {
        this.fillBackground ( g2, area, this.orientation );
        this.drawBackgroundImage ( g2, area );
    }
    protected Map drawAxes ( final Graphics2D g2, final Rectangle2D plotArea, final Rectangle2D dataArea, final PlotRenderingInfo plotState ) {
        final AxisCollection axisCollection = new AxisCollection();
        for ( final CategoryAxis xAxis : this.domainAxes.values() ) {
            if ( xAxis != null ) {
                final int index = this.getDomainAxisIndex ( xAxis );
                axisCollection.add ( xAxis, this.getDomainAxisEdge ( index ) );
            }
        }
        for ( final ValueAxis yAxis : this.rangeAxes.values() ) {
            if ( yAxis != null ) {
                final int index = this.findRangeAxisIndex ( yAxis );
                axisCollection.add ( yAxis, this.getRangeAxisEdge ( index ) );
            }
        }
        final Map axisStateMap = new HashMap();
        double cursor = dataArea.getMinY() - this.axisOffset.calculateTopOutset ( dataArea.getHeight() );
        for ( final Axis axis : axisCollection.getAxesAtTop() ) {
            if ( axis != null ) {
                final AxisState axisState = axis.draw ( g2, cursor, plotArea, dataArea, RectangleEdge.TOP, plotState );
                cursor = axisState.getCursor();
                axisStateMap.put ( axis, axisState );
            }
        }
        cursor = dataArea.getMaxY() + this.axisOffset.calculateBottomOutset ( dataArea.getHeight() );
        for ( final Axis axis : axisCollection.getAxesAtBottom() ) {
            if ( axis != null ) {
                final AxisState axisState = axis.draw ( g2, cursor, plotArea, dataArea, RectangleEdge.BOTTOM, plotState );
                cursor = axisState.getCursor();
                axisStateMap.put ( axis, axisState );
            }
        }
        cursor = dataArea.getMinX() - this.axisOffset.calculateLeftOutset ( dataArea.getWidth() );
        for ( final Axis axis : axisCollection.getAxesAtLeft() ) {
            if ( axis != null ) {
                final AxisState axisState = axis.draw ( g2, cursor, plotArea, dataArea, RectangleEdge.LEFT, plotState );
                cursor = axisState.getCursor();
                axisStateMap.put ( axis, axisState );
            }
        }
        cursor = dataArea.getMaxX() + this.axisOffset.calculateRightOutset ( dataArea.getWidth() );
        for ( final Axis axis : axisCollection.getAxesAtRight() ) {
            if ( axis != null ) {
                final AxisState axisState = axis.draw ( g2, cursor, plotArea, dataArea, RectangleEdge.RIGHT, plotState );
                cursor = axisState.getCursor();
                axisStateMap.put ( axis, axisState );
            }
        }
        return axisStateMap;
    }
    public boolean render ( final Graphics2D g2, final Rectangle2D dataArea, final int index, final PlotRenderingInfo info, final CategoryCrosshairState crosshairState ) {
        boolean foundData = false;
        final CategoryDataset currentDataset = this.getDataset ( index );
        final CategoryItemRenderer renderer = this.getRenderer ( index );
        final CategoryAxis domainAxis = this.getDomainAxisForDataset ( index );
        final ValueAxis rangeAxis = this.getRangeAxisForDataset ( index );
        final boolean hasData = !DatasetUtilities.isEmptyOrNull ( currentDataset );
        if ( hasData && renderer != null ) {
            foundData = true;
            final CategoryItemRendererState state = renderer.initialise ( g2, dataArea, this, index, info );
            state.setCrosshairState ( crosshairState );
            final int columnCount = currentDataset.getColumnCount();
            final int rowCount = currentDataset.getRowCount();
            for ( int passCount = renderer.getPassCount(), pass = 0; pass < passCount; ++pass ) {
                if ( this.columnRenderingOrder == SortOrder.ASCENDING ) {
                    for ( int column = 0; column < columnCount; ++column ) {
                        if ( this.rowRenderingOrder == SortOrder.ASCENDING ) {
                            for ( int row = 0; row < rowCount; ++row ) {
                                renderer.drawItem ( g2, state, dataArea, this, domainAxis, rangeAxis, currentDataset, row, column, pass );
                            }
                        } else {
                            for ( int row = rowCount - 1; row >= 0; --row ) {
                                renderer.drawItem ( g2, state, dataArea, this, domainAxis, rangeAxis, currentDataset, row, column, pass );
                            }
                        }
                    }
                } else {
                    for ( int column = columnCount - 1; column >= 0; --column ) {
                        if ( this.rowRenderingOrder == SortOrder.ASCENDING ) {
                            for ( int row = 0; row < rowCount; ++row ) {
                                renderer.drawItem ( g2, state, dataArea, this, domainAxis, rangeAxis, currentDataset, row, column, pass );
                            }
                        } else {
                            for ( int row = rowCount - 1; row >= 0; --row ) {
                                renderer.drawItem ( g2, state, dataArea, this, domainAxis, rangeAxis, currentDataset, row, column, pass );
                            }
                        }
                    }
                }
            }
        }
        return foundData;
    }
    protected void drawDomainGridlines ( final Graphics2D g2, final Rectangle2D dataArea ) {
        if ( !this.isDomainGridlinesVisible() ) {
            return;
        }
        final CategoryAnchor anchor = this.getDomainGridlinePosition();
        final RectangleEdge domainAxisEdge = this.getDomainAxisEdge();
        final CategoryDataset dataset = this.getDataset();
        if ( dataset == null ) {
            return;
        }
        final CategoryAxis axis = this.getDomainAxis();
        if ( axis != null ) {
            for ( int columnCount = dataset.getColumnCount(), c = 0; c < columnCount; ++c ) {
                final double xx = axis.getCategoryJava2DCoordinate ( anchor, c, columnCount, dataArea, domainAxisEdge );
                final CategoryItemRenderer renderer1 = this.getRenderer();
                if ( renderer1 != null ) {
                    renderer1.drawDomainGridline ( g2, this, dataArea, xx );
                }
            }
        }
    }
    protected void drawRangeGridlines ( final Graphics2D g2, final Rectangle2D dataArea, final List ticks ) {
        if ( !this.isRangeGridlinesVisible() && !this.isRangeMinorGridlinesVisible() ) {
            return;
        }
        final ValueAxis axis = this.getRangeAxis();
        if ( axis == null ) {
            return;
        }
        final CategoryItemRenderer r = this.getRenderer();
        if ( r == null ) {
            return;
        }
        Stroke gridStroke = null;
        Paint gridPaint = null;
        final Iterator iterator = ticks.iterator();
        while ( iterator.hasNext() ) {
            boolean paintLine = false;
            final ValueTick tick = iterator.next();
            if ( tick.getTickType() == TickType.MINOR && this.isRangeMinorGridlinesVisible() ) {
                gridStroke = this.getRangeMinorGridlineStroke();
                gridPaint = this.getRangeMinorGridlinePaint();
                paintLine = true;
            } else if ( tick.getTickType() == TickType.MAJOR && this.isRangeGridlinesVisible() ) {
                gridStroke = this.getRangeGridlineStroke();
                gridPaint = this.getRangeGridlinePaint();
                paintLine = true;
            }
            if ( ( tick.getValue() != 0.0 || !this.isRangeZeroBaselineVisible() ) && paintLine ) {
                if ( r instanceof AbstractCategoryItemRenderer ) {
                    final AbstractCategoryItemRenderer aci = ( AbstractCategoryItemRenderer ) r;
                    aci.drawRangeLine ( g2, this, axis, dataArea, tick.getValue(), gridPaint, gridStroke );
                } else {
                    r.drawRangeGridline ( g2, this, axis, dataArea, tick.getValue() );
                }
            }
        }
    }
    protected void drawZeroRangeBaseline ( final Graphics2D g2, final Rectangle2D area ) {
        if ( !this.isRangeZeroBaselineVisible() ) {
            return;
        }
        final CategoryItemRenderer r = this.getRenderer();
        if ( r instanceof AbstractCategoryItemRenderer ) {
            final AbstractCategoryItemRenderer aci = ( AbstractCategoryItemRenderer ) r;
            aci.drawRangeLine ( g2, this, this.getRangeAxis(), area, 0.0, this.rangeZeroBaselinePaint, this.rangeZeroBaselineStroke );
        } else {
            r.drawRangeGridline ( g2, this, this.getRangeAxis(), area, 0.0 );
        }
    }
    protected void drawAnnotations ( final Graphics2D g2, final Rectangle2D dataArea ) {
        if ( this.getAnnotations() != null ) {
            for ( final CategoryAnnotation annotation : this.getAnnotations() ) {
                annotation.draw ( g2, this, dataArea, this.getDomainAxis(), this.getRangeAxis() );
            }
        }
    }
    protected void drawDomainMarkers ( final Graphics2D g2, final Rectangle2D dataArea, final int index, final Layer layer ) {
        final CategoryItemRenderer r = this.getRenderer ( index );
        if ( r == null ) {
            return;
        }
        final Collection markers = this.getDomainMarkers ( index, layer );
        final CategoryAxis axis = this.getDomainAxisForDataset ( index );
        if ( markers != null && axis != null ) {
            for ( final CategoryMarker marker : markers ) {
                r.drawDomainMarker ( g2, this, axis, marker, dataArea );
            }
        }
    }
    protected void drawRangeMarkers ( final Graphics2D g2, final Rectangle2D dataArea, final int index, final Layer layer ) {
        final CategoryItemRenderer r = this.getRenderer ( index );
        if ( r == null ) {
            return;
        }
        final Collection markers = this.getRangeMarkers ( index, layer );
        final ValueAxis axis = this.getRangeAxisForDataset ( index );
        if ( markers != null && axis != null ) {
            for ( final Marker marker : markers ) {
                r.drawRangeMarker ( g2, this, axis, marker, dataArea );
            }
        }
    }
    protected void drawRangeLine ( final Graphics2D g2, final Rectangle2D dataArea, final double value, final Stroke stroke, final Paint paint ) {
        final double java2D = this.getRangeAxis().valueToJava2D ( value, dataArea, this.getRangeAxisEdge() );
        Line2D line = null;
        if ( this.orientation == PlotOrientation.HORIZONTAL ) {
            line = new Line2D.Double ( java2D, dataArea.getMinY(), java2D, dataArea.getMaxY() );
        } else if ( this.orientation == PlotOrientation.VERTICAL ) {
            line = new Line2D.Double ( dataArea.getMinX(), java2D, dataArea.getMaxX(), java2D );
        }
        g2.setStroke ( stroke );
        g2.setPaint ( paint );
        g2.draw ( line );
    }
    protected void drawDomainCrosshair ( final Graphics2D g2, final Rectangle2D dataArea, final PlotOrientation orientation, final int datasetIndex, final Comparable rowKey, final Comparable columnKey, final Stroke stroke, final Paint paint ) {
        final CategoryDataset dataset = this.getDataset ( datasetIndex );
        final CategoryAxis axis = this.getDomainAxisForDataset ( datasetIndex );
        final CategoryItemRenderer renderer = this.getRenderer ( datasetIndex );
        Line2D line;
        if ( orientation == PlotOrientation.VERTICAL ) {
            final double xx = renderer.getItemMiddle ( rowKey, columnKey, dataset, axis, dataArea, RectangleEdge.BOTTOM );
            line = new Line2D.Double ( xx, dataArea.getMinY(), xx, dataArea.getMaxY() );
        } else {
            final double yy = renderer.getItemMiddle ( rowKey, columnKey, dataset, axis, dataArea, RectangleEdge.LEFT );
            line = new Line2D.Double ( dataArea.getMinX(), yy, dataArea.getMaxX(), yy );
        }
        g2.setStroke ( stroke );
        g2.setPaint ( paint );
        g2.draw ( line );
    }
    protected void drawRangeCrosshair ( final Graphics2D g2, final Rectangle2D dataArea, final PlotOrientation orientation, final double value, final ValueAxis axis, final Stroke stroke, final Paint paint ) {
        if ( !axis.getRange().contains ( value ) ) {
            return;
        }
        Line2D line;
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            final double xx = axis.valueToJava2D ( value, dataArea, RectangleEdge.BOTTOM );
            line = new Line2D.Double ( xx, dataArea.getMinY(), xx, dataArea.getMaxY() );
        } else {
            final double yy = axis.valueToJava2D ( value, dataArea, RectangleEdge.LEFT );
            line = new Line2D.Double ( dataArea.getMinX(), yy, dataArea.getMaxX(), yy );
        }
        g2.setStroke ( stroke );
        g2.setPaint ( paint );
        g2.draw ( line );
    }
    @Override
    public Range getDataRange ( final ValueAxis axis ) {
        Range result = null;
        final List<CategoryDataset> mappedDatasets = new ArrayList<CategoryDataset>();
        final int rangeIndex = this.findRangeAxisIndex ( axis );
        if ( rangeIndex >= 0 ) {
            mappedDatasets.addAll ( this.datasetsMappedToRangeAxis ( rangeIndex ) );
        } else if ( axis == this.getRangeAxis() ) {
            mappedDatasets.addAll ( this.datasetsMappedToRangeAxis ( 0 ) );
        }
        for ( final CategoryDataset d : mappedDatasets ) {
            final CategoryItemRenderer r = this.getRendererForDataset ( d );
            if ( r != null ) {
                result = Range.combine ( result, r.findRangeBounds ( d ) );
            }
        }
        return result;
    }
    private List<CategoryDataset> datasetsMappedToDomainAxis ( final int axisIndex ) {
        final List<CategoryDataset> result = new ArrayList<CategoryDataset>();
        for ( final Map.Entry<Integer, CategoryDataset> entry : this.datasets.entrySet() ) {
            final CategoryDataset dataset = entry.getValue();
            if ( dataset == null ) {
                continue;
            }
            final Integer datasetIndex = entry.getKey();
            final List mappedAxes = this.datasetToDomainAxesMap.get ( datasetIndex );
            if ( mappedAxes == null ) {
                if ( axisIndex != 0 ) {
                    continue;
                }
                result.add ( dataset );
            } else {
                if ( !mappedAxes.contains ( axisIndex ) ) {
                    continue;
                }
                result.add ( dataset );
            }
        }
        return result;
    }
    private List<CategoryDataset> datasetsMappedToRangeAxis ( final int axisIndex ) {
        final List<CategoryDataset> result = new ArrayList<CategoryDataset>();
        for ( final Map.Entry<Integer, CategoryDataset> entry : this.datasets.entrySet() ) {
            final Integer datasetIndex = entry.getKey();
            final CategoryDataset dataset = entry.getValue();
            final List mappedAxes = this.datasetToRangeAxesMap.get ( datasetIndex );
            if ( mappedAxes == null ) {
                if ( axisIndex != 0 ) {
                    continue;
                }
                result.add ( dataset );
            } else {
                if ( !mappedAxes.contains ( axisIndex ) ) {
                    continue;
                }
                result.add ( dataset );
            }
        }
        return result;
    }
    public int getWeight() {
        return this.weight;
    }
    public void setWeight ( final int weight ) {
        this.weight = weight;
        this.fireChangeEvent();
    }
    public AxisSpace getFixedDomainAxisSpace() {
        return this.fixedDomainAxisSpace;
    }
    public void setFixedDomainAxisSpace ( final AxisSpace space ) {
        this.setFixedDomainAxisSpace ( space, true );
    }
    public void setFixedDomainAxisSpace ( final AxisSpace space, final boolean notify ) {
        this.fixedDomainAxisSpace = space;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public AxisSpace getFixedRangeAxisSpace() {
        return this.fixedRangeAxisSpace;
    }
    public void setFixedRangeAxisSpace ( final AxisSpace space ) {
        this.setFixedRangeAxisSpace ( space, true );
    }
    public void setFixedRangeAxisSpace ( final AxisSpace space, final boolean notify ) {
        this.fixedRangeAxisSpace = space;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public List getCategories() {
        List result = null;
        if ( this.getDataset() != null ) {
            result = Collections.unmodifiableList ( ( List<?> ) this.getDataset().getColumnKeys() );
        }
        return result;
    }
    public List getCategoriesForAxis ( final CategoryAxis axis ) {
        final List result = new ArrayList();
        final int axisIndex = this.getDomainAxisIndex ( axis );
        for ( final CategoryDataset dataset : this.datasetsMappedToDomainAxis ( axisIndex ) ) {
            for ( int i = 0; i < dataset.getColumnCount(); ++i ) {
                final Comparable category = dataset.getColumnKey ( i );
                if ( !result.contains ( category ) ) {
                    result.add ( category );
                }
            }
        }
        return result;
    }
    public boolean getDrawSharedDomainAxis() {
        return this.drawSharedDomainAxis;
    }
    public void setDrawSharedDomainAxis ( final boolean draw ) {
        this.drawSharedDomainAxis = draw;
        this.fireChangeEvent();
    }
    @Override
    public boolean isDomainPannable() {
        return false;
    }
    @Override
    public boolean isRangePannable() {
        return this.rangePannable;
    }
    public void setRangePannable ( final boolean pannable ) {
        this.rangePannable = pannable;
    }
    @Override
    public void panDomainAxes ( final double percent, final PlotRenderingInfo info, final Point2D source ) {
    }
    @Override
    public void panRangeAxes ( final double percent, final PlotRenderingInfo info, final Point2D source ) {
        if ( !this.isRangePannable() ) {
            return;
        }
        for ( final ValueAxis axis : this.rangeAxes.values() ) {
            if ( axis == null ) {
                continue;
            }
            final double length = axis.getRange().getLength();
            double adj = percent * length;
            if ( axis.isInverted() ) {
                adj = -adj;
            }
            axis.setRange ( axis.getLowerBound() + adj, axis.getUpperBound() + adj );
        }
    }
    @Override
    public boolean isDomainZoomable() {
        return false;
    }
    @Override
    public boolean isRangeZoomable() {
        return true;
    }
    @Override
    public void zoomDomainAxes ( final double factor, final PlotRenderingInfo state, final Point2D source ) {
    }
    @Override
    public void zoomDomainAxes ( final double lowerPercent, final double upperPercent, final PlotRenderingInfo state, final Point2D source ) {
    }
    @Override
    public void zoomDomainAxes ( final double factor, final PlotRenderingInfo info, final Point2D source, final boolean useAnchor ) {
    }
    @Override
    public void zoomRangeAxes ( final double factor, final PlotRenderingInfo state, final Point2D source ) {
        this.zoomRangeAxes ( factor, state, source, false );
    }
    @Override
    public void zoomRangeAxes ( final double factor, final PlotRenderingInfo info, final Point2D source, final boolean useAnchor ) {
        for ( final ValueAxis rangeAxis : this.rangeAxes.values() ) {
            if ( rangeAxis == null ) {
                continue;
            }
            if ( useAnchor ) {
                double sourceY = source.getY();
                if ( this.orientation.isHorizontal() ) {
                    sourceY = source.getX();
                }
                final double anchorY = rangeAxis.java2DToValue ( sourceY, info.getDataArea(), this.getRangeAxisEdge() );
                rangeAxis.resizeRange2 ( factor, anchorY );
            } else {
                rangeAxis.resizeRange ( factor );
            }
        }
    }
    @Override
    public void zoomRangeAxes ( final double lowerPercent, final double upperPercent, final PlotRenderingInfo state, final Point2D source ) {
        for ( final ValueAxis yAxis : this.rangeAxes.values() ) {
            if ( yAxis != null ) {
                yAxis.zoomRange ( lowerPercent, upperPercent );
            }
        }
    }
    public double getAnchorValue() {
        return this.anchorValue;
    }
    public void setAnchorValue ( final double value ) {
        this.setAnchorValue ( value, true );
    }
    public void setAnchorValue ( final double value, final boolean notify ) {
        this.anchorValue = value;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof CategoryPlot ) ) {
            return false;
        }
        final CategoryPlot that = ( CategoryPlot ) obj;
        return this.orientation == that.orientation && ObjectUtilities.equal ( ( Object ) this.axisOffset, ( Object ) that.axisOffset ) && this.domainAxes.equals ( that.domainAxes ) && this.domainAxisLocations.equals ( that.domainAxisLocations ) && this.drawSharedDomainAxis == that.drawSharedDomainAxis && this.rangeAxes.equals ( that.rangeAxes ) && this.rangeAxisLocations.equals ( that.rangeAxisLocations ) && ObjectUtilities.equal ( ( Object ) this.datasetToDomainAxesMap, ( Object ) that.datasetToDomainAxesMap ) && ObjectUtilities.equal ( ( Object ) this.datasetToRangeAxesMap, ( Object ) that.datasetToRangeAxesMap ) && ObjectUtilities.equal ( ( Object ) this.renderers, ( Object ) that.renderers ) && this.renderingOrder == that.renderingOrder && this.columnRenderingOrder == that.columnRenderingOrder && this.rowRenderingOrder == that.rowRenderingOrder && this.domainGridlinesVisible == that.domainGridlinesVisible && this.domainGridlinePosition == that.domainGridlinePosition && ObjectUtilities.equal ( ( Object ) this.domainGridlineStroke, ( Object ) that.domainGridlineStroke ) && PaintUtilities.equal ( this.domainGridlinePaint, that.domainGridlinePaint ) && this.rangeGridlinesVisible == that.rangeGridlinesVisible && ObjectUtilities.equal ( ( Object ) this.rangeGridlineStroke, ( Object ) that.rangeGridlineStroke ) && PaintUtilities.equal ( this.rangeGridlinePaint, that.rangeGridlinePaint ) && this.anchorValue == that.anchorValue && this.rangeCrosshairVisible == that.rangeCrosshairVisible && this.rangeCrosshairValue == that.rangeCrosshairValue && ObjectUtilities.equal ( ( Object ) this.rangeCrosshairStroke, ( Object ) that.rangeCrosshairStroke ) && PaintUtilities.equal ( this.rangeCrosshairPaint, that.rangeCrosshairPaint ) && this.rangeCrosshairLockedOnData == that.rangeCrosshairLockedOnData && ObjectUtilities.equal ( ( Object ) this.foregroundDomainMarkers, ( Object ) that.foregroundDomainMarkers ) && ObjectUtilities.equal ( ( Object ) this.backgroundDomainMarkers, ( Object ) that.backgroundDomainMarkers ) && ObjectUtilities.equal ( ( Object ) this.foregroundRangeMarkers, ( Object ) that.foregroundRangeMarkers ) && ObjectUtilities.equal ( ( Object ) this.backgroundRangeMarkers, ( Object ) that.backgroundRangeMarkers ) && ObjectUtilities.equal ( ( Object ) this.annotations, ( Object ) that.annotations ) && this.weight == that.weight && ObjectUtilities.equal ( ( Object ) this.fixedDomainAxisSpace, ( Object ) that.fixedDomainAxisSpace ) && ObjectUtilities.equal ( ( Object ) this.fixedRangeAxisSpace, ( Object ) that.fixedRangeAxisSpace ) && ObjectUtilities.equal ( ( Object ) this.fixedLegendItems, ( Object ) that.fixedLegendItems ) && this.domainCrosshairVisible == that.domainCrosshairVisible && this.crosshairDatasetIndex == that.crosshairDatasetIndex && ObjectUtilities.equal ( ( Object ) this.domainCrosshairColumnKey, ( Object ) that.domainCrosshairColumnKey ) && ObjectUtilities.equal ( ( Object ) this.domainCrosshairRowKey, ( Object ) that.domainCrosshairRowKey ) && PaintUtilities.equal ( this.domainCrosshairPaint, that.domainCrosshairPaint ) && ObjectUtilities.equal ( ( Object ) this.domainCrosshairStroke, ( Object ) that.domainCrosshairStroke ) && this.rangeMinorGridlinesVisible == that.rangeMinorGridlinesVisible && PaintUtilities.equal ( this.rangeMinorGridlinePaint, that.rangeMinorGridlinePaint ) && ObjectUtilities.equal ( ( Object ) this.rangeMinorGridlineStroke, ( Object ) that.rangeMinorGridlineStroke ) && this.rangeZeroBaselineVisible == that.rangeZeroBaselineVisible && PaintUtilities.equal ( this.rangeZeroBaselinePaint, that.rangeZeroBaselinePaint ) && ObjectUtilities.equal ( ( Object ) this.rangeZeroBaselineStroke, ( Object ) that.rangeZeroBaselineStroke ) && ObjectUtilities.equal ( ( Object ) this.shadowGenerator, ( Object ) that.shadowGenerator ) && super.equals ( obj );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final CategoryPlot clone = ( CategoryPlot ) super.clone();
        clone.domainAxes = ( Map<Integer, CategoryAxis> ) CloneUtils.cloneMapValues ( this.domainAxes );
        for ( final CategoryAxis axis : clone.domainAxes.values() ) {
            if ( axis != null ) {
                axis.setPlot ( clone );
                axis.addChangeListener ( clone );
            }
        }
        clone.rangeAxes = ( Map<Integer, ValueAxis> ) CloneUtils.cloneMapValues ( this.rangeAxes );
        for ( final ValueAxis axis2 : clone.rangeAxes.values() ) {
            if ( axis2 != null ) {
                axis2.setPlot ( clone );
                axis2.addChangeListener ( clone );
            }
        }
        clone.domainAxisLocations = new HashMap<Integer, AxisLocation> ( this.domainAxisLocations );
        clone.rangeAxisLocations = new HashMap<Integer, AxisLocation> ( this.rangeAxisLocations );
        clone.datasets = new HashMap<Integer, CategoryDataset> ( this.datasets );
        for ( final CategoryDataset dataset : clone.datasets.values() ) {
            if ( dataset != null ) {
                dataset.addChangeListener ( clone );
            }
        }
        ( clone.datasetToDomainAxesMap = new TreeMap<Integer, List<Integer>>() ).putAll ( this.datasetToDomainAxesMap );
        ( clone.datasetToRangeAxesMap = new TreeMap<Integer, List<Integer>>() ).putAll ( this.datasetToRangeAxesMap );
        clone.renderers = ( Map<Integer, CategoryItemRenderer> ) CloneUtils.cloneMapValues ( this.renderers );
        for ( final CategoryItemRenderer renderer : clone.renderers.values() ) {
            if ( renderer != null ) {
                renderer.setPlot ( clone );
                renderer.addChangeListener ( clone );
            }
        }
        if ( this.fixedDomainAxisSpace != null ) {
            clone.fixedDomainAxisSpace = ( AxisSpace ) ObjectUtilities.clone ( ( Object ) this.fixedDomainAxisSpace );
        }
        if ( this.fixedRangeAxisSpace != null ) {
            clone.fixedRangeAxisSpace = ( AxisSpace ) ObjectUtilities.clone ( ( Object ) this.fixedRangeAxisSpace );
        }
        clone.annotations = ( List ) ObjectUtilities.deepClone ( ( Collection ) this.annotations );
        clone.foregroundDomainMarkers = this.cloneMarkerMap ( this.foregroundDomainMarkers );
        clone.backgroundDomainMarkers = this.cloneMarkerMap ( this.backgroundDomainMarkers );
        clone.foregroundRangeMarkers = this.cloneMarkerMap ( this.foregroundRangeMarkers );
        clone.backgroundRangeMarkers = this.cloneMarkerMap ( this.backgroundRangeMarkers );
        if ( this.fixedLegendItems != null ) {
            clone.fixedLegendItems = ( LegendItemCollection ) this.fixedLegendItems.clone();
        }
        return clone;
    }
    private Map cloneMarkerMap ( final Map map ) throws CloneNotSupportedException {
        final Map clone = new HashMap();
        final Set keys = map.keySet();
        for ( final Object key : keys ) {
            final List entry = map.get ( key );
            final Object toAdd = ObjectUtilities.deepClone ( ( Collection ) entry );
            clone.put ( key, toAdd );
        }
        return clone;
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeStroke ( this.domainGridlineStroke, stream );
        SerialUtilities.writePaint ( this.domainGridlinePaint, stream );
        SerialUtilities.writeStroke ( this.rangeGridlineStroke, stream );
        SerialUtilities.writePaint ( this.rangeGridlinePaint, stream );
        SerialUtilities.writeStroke ( this.rangeCrosshairStroke, stream );
        SerialUtilities.writePaint ( this.rangeCrosshairPaint, stream );
        SerialUtilities.writeStroke ( this.domainCrosshairStroke, stream );
        SerialUtilities.writePaint ( this.domainCrosshairPaint, stream );
        SerialUtilities.writeStroke ( this.rangeMinorGridlineStroke, stream );
        SerialUtilities.writePaint ( this.rangeMinorGridlinePaint, stream );
        SerialUtilities.writeStroke ( this.rangeZeroBaselineStroke, stream );
        SerialUtilities.writePaint ( this.rangeZeroBaselinePaint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.domainGridlineStroke = SerialUtilities.readStroke ( stream );
        this.domainGridlinePaint = SerialUtilities.readPaint ( stream );
        this.rangeGridlineStroke = SerialUtilities.readStroke ( stream );
        this.rangeGridlinePaint = SerialUtilities.readPaint ( stream );
        this.rangeCrosshairStroke = SerialUtilities.readStroke ( stream );
        this.rangeCrosshairPaint = SerialUtilities.readPaint ( stream );
        this.domainCrosshairStroke = SerialUtilities.readStroke ( stream );
        this.domainCrosshairPaint = SerialUtilities.readPaint ( stream );
        this.rangeMinorGridlineStroke = SerialUtilities.readStroke ( stream );
        this.rangeMinorGridlinePaint = SerialUtilities.readPaint ( stream );
        this.rangeZeroBaselineStroke = SerialUtilities.readStroke ( stream );
        this.rangeZeroBaselinePaint = SerialUtilities.readPaint ( stream );
        for ( final CategoryAxis xAxis : this.domainAxes.values() ) {
            if ( xAxis != null ) {
                xAxis.setPlot ( this );
                xAxis.addChangeListener ( this );
            }
        }
        for ( final ValueAxis yAxis : this.rangeAxes.values() ) {
            if ( yAxis != null ) {
                yAxis.setPlot ( this );
                yAxis.addChangeListener ( this );
            }
        }
        for ( final CategoryDataset dataset : this.datasets.values() ) {
            if ( dataset != null ) {
                dataset.addChangeListener ( this );
            }
        }
        for ( final CategoryItemRenderer renderer : this.renderers.values() ) {
            if ( renderer != null ) {
                renderer.addChangeListener ( this );
            }
        }
    }
    static {
        DEFAULT_GRIDLINE_STROKE = new BasicStroke ( 0.5f, 0, 2, 0.0f, new float[] { 2.0f, 2.0f }, 0.0f );
        DEFAULT_GRIDLINE_PAINT = Color.lightGray;
        DEFAULT_VALUE_LABEL_FONT = new Font ( "SansSerif", 0, 10 );
        DEFAULT_CROSSHAIR_STROKE = CategoryPlot.DEFAULT_GRIDLINE_STROKE;
        DEFAULT_CROSSHAIR_PAINT = Color.blue;
        CategoryPlot.localizationResources = ResourceBundleWrapper.getBundle ( "org.jfree.chart.plot.LocalizationBundle" );
    }
}
