package org.jfree.chart.plot;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.Annotation;
import org.jfree.chart.annotations.CategoryAnnotation;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.AxisCollection;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.CategoryAnchor;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.TickType;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.ValueTick;
import org.jfree.chart.event.AnnotationChangeEvent;
import org.jfree.chart.event.AnnotationChangeListener;
import org.jfree.chart.event.ChartChangeEventType;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.event.RendererChangeListener;
import org.jfree.chart.renderer.category.AbstractCategoryItemRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.chart.util.CloneUtils;
import org.jfree.chart.util.ParamChecks;
import org.jfree.chart.util.ResourceBundleWrapper;
import org.jfree.chart.util.ShadowGenerator;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.io.SerialUtilities;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;
import org.jfree.util.PublicCloneable;
import org.jfree.util.ShapeUtilities;
import org.jfree.util.SortOrder;
public class CategoryPlot extends Plot implements ValueAxisPlot, Pannable,
    Zoomable, AnnotationChangeListener, RendererChangeListener,
    Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = -3537691700434728188L;
    public static final boolean DEFAULT_DOMAIN_GRIDLINES_VISIBLE = false;
    public static final boolean DEFAULT_RANGE_GRIDLINES_VISIBLE = true;
    public static final Stroke DEFAULT_GRIDLINE_STROKE = new BasicStroke ( 0.5f,
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0.0f, new float[]
            {2.0f, 2.0f}, 0.0f );
    public static final Paint DEFAULT_GRIDLINE_PAINT = Color.lightGray;
    public static final Font DEFAULT_VALUE_LABEL_FONT = new Font ( "SansSerif",
            Font.PLAIN, 10 );
    public static final boolean DEFAULT_CROSSHAIR_VISIBLE = false;
    public static final Stroke DEFAULT_CROSSHAIR_STROKE
        = DEFAULT_GRIDLINE_STROKE;
    public static final Paint DEFAULT_CROSSHAIR_PAINT = Color.blue;
    protected static ResourceBundle localizationResources
        = ResourceBundleWrapper.getBundle (
              "org.jfree.chart.plot.LocalizationBundle" );
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
    private DatasetRenderingOrder renderingOrder
        = DatasetRenderingOrder.REVERSE;
    private SortOrder columnRenderingOrder = SortOrder.ASCENDING;
    private SortOrder rowRenderingOrder = SortOrder.ASCENDING;
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
    private boolean rangeCrosshairLockedOnData = true;
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
    public CategoryPlot ( CategoryDataset dataset, CategoryAxis domainAxis,
                          ValueAxis rangeAxis, CategoryItemRenderer renderer ) {
        super();
        this.orientation = PlotOrientation.VERTICAL;
        this.domainAxes = new HashMap<Integer, CategoryAxis>();
        this.domainAxisLocations = new HashMap<Integer, AxisLocation>();
        this.rangeAxes = new HashMap<Integer, ValueAxis>();
        this.rangeAxisLocations = new HashMap<Integer, AxisLocation>();
        this.datasetToDomainAxesMap = new TreeMap();
        this.datasetToRangeAxesMap = new TreeMap();
        this.renderers = new HashMap<Integer, CategoryItemRenderer>();
        this.datasets = new HashMap<Integer, CategoryDataset>();
        this.datasets.put ( 0, dataset );
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
        mapDatasetToDomainAxis ( 0, 0 );
        if ( domainAxis != null ) {
            domainAxis.setPlot ( this );
            domainAxis.addChangeListener ( this );
        }
        this.drawSharedDomainAxis = false;
        this.rangeAxes.put ( 0, rangeAxis );
        mapDatasetToRangeAxis ( 0, 0 );
        if ( rangeAxis != null ) {
            rangeAxis.setPlot ( this );
            rangeAxis.addChangeListener ( this );
        }
        configureDomainAxes();
        configureRangeAxes();
        this.domainGridlinesVisible = DEFAULT_DOMAIN_GRIDLINES_VISIBLE;
        this.domainGridlinePosition = CategoryAnchor.MIDDLE;
        this.domainGridlineStroke = DEFAULT_GRIDLINE_STROKE;
        this.domainGridlinePaint = DEFAULT_GRIDLINE_PAINT;
        this.rangeZeroBaselineVisible = false;
        this.rangeZeroBaselinePaint = Color.black;
        this.rangeZeroBaselineStroke = new BasicStroke ( 0.5f );
        this.rangeGridlinesVisible = DEFAULT_RANGE_GRIDLINES_VISIBLE;
        this.rangeGridlineStroke = DEFAULT_GRIDLINE_STROKE;
        this.rangeGridlinePaint = DEFAULT_GRIDLINE_PAINT;
        this.rangeMinorGridlinesVisible = false;
        this.rangeMinorGridlineStroke = DEFAULT_GRIDLINE_STROKE;
        this.rangeMinorGridlinePaint = Color.white;
        this.foregroundDomainMarkers = new HashMap();
        this.backgroundDomainMarkers = new HashMap();
        this.foregroundRangeMarkers = new HashMap();
        this.backgroundRangeMarkers = new HashMap();
        this.anchorValue = 0.0;
        this.domainCrosshairVisible = false;
        this.domainCrosshairStroke = DEFAULT_CROSSHAIR_STROKE;
        this.domainCrosshairPaint = DEFAULT_CROSSHAIR_PAINT;
        this.rangeCrosshairVisible = DEFAULT_CROSSHAIR_VISIBLE;
        this.rangeCrosshairValue = 0.0;
        this.rangeCrosshairStroke = DEFAULT_CROSSHAIR_STROKE;
        this.rangeCrosshairPaint = DEFAULT_CROSSHAIR_PAINT;
        this.annotations = new java.util.ArrayList();
        this.rangePannable = false;
        this.shadowGenerator = null;
    }
    @Override
    public String getPlotType() {
        return localizationResources.getString ( "Category_Plot" );
    }
    @Override
    public PlotOrientation getOrientation() {
        return this.orientation;
    }
    public void setOrientation ( PlotOrientation orientation ) {
        ParamChecks.nullNotPermitted ( orientation, "orientation" );
        this.orientation = orientation;
        fireChangeEvent();
    }
    public RectangleInsets getAxisOffset() {
        return this.axisOffset;
    }
    public void setAxisOffset ( RectangleInsets offset ) {
        ParamChecks.nullNotPermitted ( offset, "offset" );
        this.axisOffset = offset;
        fireChangeEvent();
    }
    public CategoryAxis getDomainAxis() {
        return getDomainAxis ( 0 );
    }
    public CategoryAxis getDomainAxis ( int index ) {
        CategoryAxis result = ( CategoryAxis ) this.domainAxes.get ( index );
        if ( result == null ) {
            Plot parent = getParent();
            if ( parent instanceof CategoryPlot ) {
                CategoryPlot cp = ( CategoryPlot ) parent;
                result = cp.getDomainAxis ( index );
            }
        }
        return result;
    }
    public void setDomainAxis ( CategoryAxis axis ) {
        setDomainAxis ( 0, axis );
    }
    public void setDomainAxis ( int index, CategoryAxis axis ) {
        setDomainAxis ( index, axis, true );
    }
    public void setDomainAxis ( int index, CategoryAxis axis, boolean notify ) {
        CategoryAxis existing = ( CategoryAxis ) this.domainAxes.get ( index );
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
            fireChangeEvent();
        }
    }
    public void setDomainAxes ( CategoryAxis[] axes ) {
        for ( int i = 0; i < axes.length; i++ ) {
            setDomainAxis ( i, axes[i], false );
        }
        fireChangeEvent();
    }
    public int getDomainAxisIndex ( CategoryAxis axis ) {
        ParamChecks.nullNotPermitted ( axis, "axis" );
        for ( Entry<Integer, CategoryAxis> entry : this.domainAxes.entrySet() ) {
            if ( entry.getValue() == axis ) {
                return entry.getKey();
            }
        }
        return -1;
    }
    public AxisLocation getDomainAxisLocation() {
        return getDomainAxisLocation ( 0 );
    }
    public AxisLocation getDomainAxisLocation ( int index ) {
        AxisLocation result = this.domainAxisLocations.get ( index );
        if ( result == null ) {
            result = AxisLocation.getOpposite ( getDomainAxisLocation ( 0 ) );
        }
        return result;
    }
    public void setDomainAxisLocation ( AxisLocation location ) {
        setDomainAxisLocation ( 0, location, true );
    }
    public void setDomainAxisLocation ( AxisLocation location, boolean notify ) {
        setDomainAxisLocation ( 0, location, notify );
    }
    public void setDomainAxisLocation ( int index, AxisLocation location ) {
        setDomainAxisLocation ( index, location, true );
    }
    public void setDomainAxisLocation ( int index, AxisLocation location,
                                        boolean notify ) {
        if ( index == 0 && location == null ) {
            throw new IllegalArgumentException (
                "Null 'location' for index 0 not permitted." );
        }
        this.domainAxisLocations.put ( index, location );
        if ( notify ) {
            fireChangeEvent();
        }
    }
    public RectangleEdge getDomainAxisEdge() {
        return getDomainAxisEdge ( 0 );
    }
    public RectangleEdge getDomainAxisEdge ( int index ) {
        RectangleEdge result;
        AxisLocation location = getDomainAxisLocation ( index );
        if ( location != null ) {
            result = Plot.resolveDomainAxisLocation ( location, this.orientation );
        } else {
            result = RectangleEdge.opposite ( getDomainAxisEdge ( 0 ) );
        }
        return result;
    }
    public int getDomainAxisCount() {
        return this.domainAxes.size();
    }
    public void clearDomainAxes() {
        for ( CategoryAxis xAxis : this.domainAxes.values() ) {
            if ( xAxis != null ) {
                xAxis.removeChangeListener ( this );
            }
        }
        this.domainAxes.clear();
        fireChangeEvent();
    }
    public void configureDomainAxes() {
        for ( CategoryAxis xAxis : this.domainAxes.values() ) {
            if ( xAxis != null ) {
                xAxis.configure();
            }
        }
    }
    public ValueAxis getRangeAxis() {
        return getRangeAxis ( 0 );
    }
    public ValueAxis getRangeAxis ( int index ) {
        ValueAxis result = this.rangeAxes.get ( index );
        if ( result == null ) {
            Plot parent = getParent();
            if ( parent instanceof CategoryPlot ) {
                CategoryPlot cp = ( CategoryPlot ) parent;
                result = cp.getRangeAxis ( index );
            }
        }
        return result;
    }
    public void setRangeAxis ( ValueAxis axis ) {
        setRangeAxis ( 0, axis );
    }
    public void setRangeAxis ( int index, ValueAxis axis ) {
        setRangeAxis ( index, axis, true );
    }
    public void setRangeAxis ( int index, ValueAxis axis, boolean notify ) {
        ValueAxis existing = this.rangeAxes.get ( index );
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
            fireChangeEvent();
        }
    }
    public void setRangeAxes ( ValueAxis[] axes ) {
        for ( int i = 0; i < axes.length; i++ ) {
            setRangeAxis ( i, axes[i], false );
        }
        fireChangeEvent();
    }
    public int getRangeAxisIndex ( ValueAxis axis ) {
        ParamChecks.nullNotPermitted ( axis, "axis" );
        int result = findRangeAxisIndex ( axis );
        if ( result < 0 ) {
            Plot parent = getParent();
            if ( parent instanceof CategoryPlot ) {
                CategoryPlot p = ( CategoryPlot ) parent;
                result = p.getRangeAxisIndex ( axis );
            }
        }
        return result;
    }
    private int findRangeAxisIndex ( ValueAxis axis ) {
        for ( Entry<Integer, ValueAxis> entry : this.rangeAxes.entrySet() ) {
            if ( entry.getValue() == axis ) {
                return entry.getKey();
            }
        }
        return -1;
    }
    public AxisLocation getRangeAxisLocation() {
        return getRangeAxisLocation ( 0 );
    }
    public AxisLocation getRangeAxisLocation ( int index ) {
        AxisLocation result = this.rangeAxisLocations.get ( index );
        if ( result == null ) {
            result = AxisLocation.getOpposite ( getRangeAxisLocation ( 0 ) );
        }
        return result;
    }
    public void setRangeAxisLocation ( AxisLocation location ) {
        setRangeAxisLocation ( location, true );
    }
    public void setRangeAxisLocation ( AxisLocation location, boolean notify ) {
        setRangeAxisLocation ( 0, location, notify );
    }
    public void setRangeAxisLocation ( int index, AxisLocation location ) {
        setRangeAxisLocation ( index, location, true );
    }
    public void setRangeAxisLocation ( int index, AxisLocation location,
                                       boolean notify ) {
        if ( index == 0 && location == null ) {
            throw new IllegalArgumentException (
                "Null 'location' for index 0 not permitted." );
        }
        this.rangeAxisLocations.put ( index, location );
        if ( notify ) {
            fireChangeEvent();
        }
    }
    public RectangleEdge getRangeAxisEdge() {
        return getRangeAxisEdge ( 0 );
    }
    public RectangleEdge getRangeAxisEdge ( int index ) {
        AxisLocation location = getRangeAxisLocation ( index );
        return Plot.resolveRangeAxisLocation ( location, this.orientation );
    }
    public int getRangeAxisCount() {
        return this.rangeAxes.size();
    }
    public void clearRangeAxes() {
        for ( ValueAxis yAxis : this.rangeAxes.values() ) {
            if ( yAxis != null ) {
                yAxis.removeChangeListener ( this );
            }
        }
        this.rangeAxes.clear();
        fireChangeEvent();
    }
    public void configureRangeAxes() {
        for ( ValueAxis yAxis : this.rangeAxes.values() ) {
            if ( yAxis != null ) {
                yAxis.configure();
            }
        }
    }
    public CategoryDataset getDataset() {
        return getDataset ( 0 );
    }
    public CategoryDataset getDataset ( int index ) {
        return this.datasets.get ( index );
    }
    public void setDataset ( CategoryDataset dataset ) {
        setDataset ( 0, dataset );
    }
    public void setDataset ( int index, CategoryDataset dataset ) {
        CategoryDataset existing = ( CategoryDataset ) this.datasets.get ( index );
        if ( existing != null ) {
            existing.removeChangeListener ( this );
        }
        this.datasets.put ( index, dataset );
        if ( dataset != null ) {
            dataset.addChangeListener ( this );
        }
        DatasetChangeEvent event = new DatasetChangeEvent ( this, dataset );
        datasetChanged ( event );
    }
    public int getDatasetCount() {
        return this.datasets.size();
    }
    public int indexOf ( CategoryDataset dataset ) {
        for ( Entry<Integer, CategoryDataset> entry : this.datasets.entrySet() ) {
            if ( entry.getValue() == dataset ) {
                return entry.getKey();
            }
        }
        return -1;
    }
    public void mapDatasetToDomainAxis ( int index, int axisIndex ) {
        List<Integer> axisIndices = new java.util.ArrayList<Integer> ( 1 );
        axisIndices.add ( axisIndex );
        mapDatasetToDomainAxes ( index, axisIndices );
    }
    public void mapDatasetToDomainAxes ( int index, List axisIndices ) {
        ParamChecks.requireNonNegative ( index, "index" );
        checkAxisIndices ( axisIndices );
        this.datasetToDomainAxesMap.put ( index, new ArrayList ( axisIndices ) );
        datasetChanged ( new DatasetChangeEvent ( this, getDataset ( index ) ) );
    }
    private void checkAxisIndices ( List indices ) {
        if ( indices == null ) {
            return;
        }
        int count = indices.size();
        if ( count == 0 ) {
            throw new IllegalArgumentException ( "Empty list not permitted." );
        }
        HashSet set = new HashSet();
        for ( int i = 0; i < count; i++ ) {
            Object item = indices.get ( i );
            if ( ! ( item instanceof Integer ) ) {
                throw new IllegalArgumentException (
                    "Indices must be Integer instances." );
            }
            if ( set.contains ( item ) ) {
                throw new IllegalArgumentException ( "Indices must be unique." );
            }
            set.add ( item );
        }
    }
    public CategoryAxis getDomainAxisForDataset ( int index ) {
        ParamChecks.requireNonNegative ( index, "index" );
        CategoryAxis axis;
        List axisIndices = ( List ) this.datasetToDomainAxesMap.get (
                               new Integer ( index ) );
        if ( axisIndices != null ) {
            Integer axisIndex = ( Integer ) axisIndices.get ( 0 );
            axis = getDomainAxis ( axisIndex.intValue() );
        } else {
            axis = getDomainAxis ( 0 );
        }
        return axis;
    }
    public void mapDatasetToRangeAxis ( int index, int axisIndex ) {
        List axisIndices = new java.util.ArrayList ( 1 );
        axisIndices.add ( new Integer ( axisIndex ) );
        mapDatasetToRangeAxes ( index, axisIndices );
    }
    public void mapDatasetToRangeAxes ( int index, List axisIndices ) {
        ParamChecks.requireNonNegative ( index, "index" );
        checkAxisIndices ( axisIndices );
        this.datasetToRangeAxesMap.put ( index, new ArrayList ( axisIndices ) );
        datasetChanged ( new DatasetChangeEvent ( this, getDataset ( index ) ) );
    }
    public ValueAxis getRangeAxisForDataset ( int index ) {
        ParamChecks.requireNonNegative ( index, "index" );
        ValueAxis axis;
        List axisIndices = ( List ) this.datasetToRangeAxesMap.get (
                               new Integer ( index ) );
        if ( axisIndices != null ) {
            Integer axisIndex = ( Integer ) axisIndices.get ( 0 );
            axis = getRangeAxis ( axisIndex.intValue() );
        } else {
            axis = getRangeAxis ( 0 );
        }
        return axis;
    }
    public int getRendererCount() {
        return this.renderers.size();
    }
    public CategoryItemRenderer getRenderer() {
        return getRenderer ( 0 );
    }
    public CategoryItemRenderer getRenderer ( int index ) {
        CategoryItemRenderer renderer = this.renderers.get ( index );
        if ( renderer == null ) {
            return this.renderers.get ( 0 );
        }
        return renderer;
    }
    public void setRenderer ( CategoryItemRenderer renderer ) {
        setRenderer ( 0, renderer, true );
    }
    public void setRenderer ( CategoryItemRenderer renderer, boolean notify ) {
        setRenderer ( 0, renderer, notify );
    }
    public void setRenderer ( int index, CategoryItemRenderer renderer ) {
        setRenderer ( index, renderer, true );
    }
    public void setRenderer ( int index, CategoryItemRenderer renderer,
                              boolean notify ) {
        CategoryItemRenderer existing = this.renderers.get ( index );
        if ( existing != null ) {
            existing.removeChangeListener ( this );
        }
        this.renderers.put ( index, renderer );
        if ( renderer != null ) {
            renderer.setPlot ( this );
            renderer.addChangeListener ( this );
        }
        configureDomainAxes();
        configureRangeAxes();
        if ( notify ) {
            fireChangeEvent();
        }
    }
    public void setRenderers ( CategoryItemRenderer[] renderers ) {
        for ( int i = 0; i < renderers.length; i++ ) {
            setRenderer ( i, renderers[i], false );
        }
        fireChangeEvent();
    }
    public CategoryItemRenderer getRendererForDataset ( CategoryDataset dataset ) {
        int datasetIndex = indexOf ( dataset );
        if ( datasetIndex < 0 ) {
            return null;
        }
        CategoryItemRenderer renderer = this.renderers.get ( datasetIndex );
        if ( renderer == null ) {
            return getRenderer();
        }
        return renderer;
    }
    public int getIndexOf ( CategoryItemRenderer renderer ) {
        for ( Entry<Integer, CategoryItemRenderer> entry
                : this.renderers.entrySet() ) {
            if ( entry.getValue() == renderer ) {
                return entry.getKey();
            }
        }
        return -1;
    }
    public DatasetRenderingOrder getDatasetRenderingOrder() {
        return this.renderingOrder;
    }
    public void setDatasetRenderingOrder ( DatasetRenderingOrder order ) {
        ParamChecks.nullNotPermitted ( order, "order" );
        this.renderingOrder = order;
        fireChangeEvent();
    }
    public SortOrder getColumnRenderingOrder() {
        return this.columnRenderingOrder;
    }
    public void setColumnRenderingOrder ( SortOrder order ) {
        ParamChecks.nullNotPermitted ( order, "order" );
        this.columnRenderingOrder = order;
        fireChangeEvent();
    }
    public SortOrder getRowRenderingOrder() {
        return this.rowRenderingOrder;
    }
    public void setRowRenderingOrder ( SortOrder order ) {
        ParamChecks.nullNotPermitted ( order, "order" );
        this.rowRenderingOrder = order;
        fireChangeEvent();
    }
    public boolean isDomainGridlinesVisible() {
        return this.domainGridlinesVisible;
    }
    public void setDomainGridlinesVisible ( boolean visible ) {
        if ( this.domainGridlinesVisible != visible ) {
            this.domainGridlinesVisible = visible;
            fireChangeEvent();
        }
    }
    public CategoryAnchor getDomainGridlinePosition() {
        return this.domainGridlinePosition;
    }
    public void setDomainGridlinePosition ( CategoryAnchor position ) {
        ParamChecks.nullNotPermitted ( position, "position" );
        this.domainGridlinePosition = position;
        fireChangeEvent();
    }
    public Stroke getDomainGridlineStroke() {
        return this.domainGridlineStroke;
    }
    public void setDomainGridlineStroke ( Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.domainGridlineStroke = stroke;
        fireChangeEvent();
    }
    public Paint getDomainGridlinePaint() {
        return this.domainGridlinePaint;
    }
    public void setDomainGridlinePaint ( Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.domainGridlinePaint = paint;
        fireChangeEvent();
    }
    public boolean isRangeZeroBaselineVisible() {
        return this.rangeZeroBaselineVisible;
    }
    public void setRangeZeroBaselineVisible ( boolean visible ) {
        this.rangeZeroBaselineVisible = visible;
        fireChangeEvent();
    }
    public Stroke getRangeZeroBaselineStroke() {
        return this.rangeZeroBaselineStroke;
    }
    public void setRangeZeroBaselineStroke ( Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.rangeZeroBaselineStroke = stroke;
        fireChangeEvent();
    }
    public Paint getRangeZeroBaselinePaint() {
        return this.rangeZeroBaselinePaint;
    }
    public void setRangeZeroBaselinePaint ( Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.rangeZeroBaselinePaint = paint;
        fireChangeEvent();
    }
    public boolean isRangeGridlinesVisible() {
        return this.rangeGridlinesVisible;
    }
    public void setRangeGridlinesVisible ( boolean visible ) {
        if ( this.rangeGridlinesVisible != visible ) {
            this.rangeGridlinesVisible = visible;
            fireChangeEvent();
        }
    }
    public Stroke getRangeGridlineStroke() {
        return this.rangeGridlineStroke;
    }
    public void setRangeGridlineStroke ( Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.rangeGridlineStroke = stroke;
        fireChangeEvent();
    }
    public Paint getRangeGridlinePaint() {
        return this.rangeGridlinePaint;
    }
    public void setRangeGridlinePaint ( Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.rangeGridlinePaint = paint;
        fireChangeEvent();
    }
    public boolean isRangeMinorGridlinesVisible() {
        return this.rangeMinorGridlinesVisible;
    }
    public void setRangeMinorGridlinesVisible ( boolean visible ) {
        if ( this.rangeMinorGridlinesVisible != visible ) {
            this.rangeMinorGridlinesVisible = visible;
            fireChangeEvent();
        }
    }
    public Stroke getRangeMinorGridlineStroke() {
        return this.rangeMinorGridlineStroke;
    }
    public void setRangeMinorGridlineStroke ( Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.rangeMinorGridlineStroke = stroke;
        fireChangeEvent();
    }
    public Paint getRangeMinorGridlinePaint() {
        return this.rangeMinorGridlinePaint;
    }
    public void setRangeMinorGridlinePaint ( Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.rangeMinorGridlinePaint = paint;
        fireChangeEvent();
    }
    public LegendItemCollection getFixedLegendItems() {
        return this.fixedLegendItems;
    }
    public void setFixedLegendItems ( LegendItemCollection items ) {
        this.fixedLegendItems = items;
        fireChangeEvent();
    }
    @Override
    public LegendItemCollection getLegendItems() {
        if ( this.fixedLegendItems != null ) {
            return this.fixedLegendItems;
        }
        LegendItemCollection result = new LegendItemCollection();
        for ( CategoryDataset dataset : this.datasets.values() ) {
            if ( dataset != null ) {
                int datasetIndex = indexOf ( dataset );
                CategoryItemRenderer renderer = getRenderer ( datasetIndex );
                if ( renderer != null ) {
                    result.addAll ( renderer.getLegendItems() );
                }
            }
        }
        return result;
    }
    @Override
    public void handleClick ( int x, int y, PlotRenderingInfo info ) {
        Rectangle2D dataArea = info.getDataArea();
        if ( dataArea.contains ( x, y ) ) {
            double java2D = 0.0;
            if ( this.orientation == PlotOrientation.HORIZONTAL ) {
                java2D = x;
            } else if ( this.orientation == PlotOrientation.VERTICAL ) {
                java2D = y;
            }
            RectangleEdge edge = Plot.resolveRangeAxisLocation (
                                     getRangeAxisLocation(), this.orientation );
            double value = getRangeAxis().java2DToValue (
                               java2D, info.getDataArea(), edge );
            setAnchorValue ( value );
            setRangeCrosshairValue ( value );
        }
    }
    @Override
    public void zoom ( double percent ) {
        if ( percent > 0.0 ) {
            double range = getRangeAxis().getRange().getLength();
            double scaledRange = range * percent;
            getRangeAxis().setRange ( this.anchorValue - scaledRange / 2.0,
                                      this.anchorValue + scaledRange / 2.0 );
        } else {
            getRangeAxis().setAutoRange ( true );
        }
    }
    @Override
    public void annotationChanged ( AnnotationChangeEvent event ) {
        if ( getParent() != null ) {
            getParent().annotationChanged ( event );
        } else {
            PlotChangeEvent e = new PlotChangeEvent ( this );
            notifyListeners ( e );
        }
    }
    @Override
    public void datasetChanged ( DatasetChangeEvent event ) {
        for ( ValueAxis yAxis : this.rangeAxes.values() ) {
            if ( yAxis != null ) {
                yAxis.configure();
            }
        }
        if ( getParent() != null ) {
            getParent().datasetChanged ( event );
        } else {
            PlotChangeEvent e = new PlotChangeEvent ( this );
            e.setType ( ChartChangeEventType.DATASET_UPDATED );
            notifyListeners ( e );
        }
    }
    @Override
    public void rendererChanged ( RendererChangeEvent event ) {
        Plot parent = getParent();
        if ( parent != null ) {
            if ( parent instanceof RendererChangeListener ) {
                RendererChangeListener rcl = ( RendererChangeListener ) parent;
                rcl.rendererChanged ( event );
            } else {
                throw new RuntimeException (
                    "The renderer has changed and I don't know what to do!" );
            }
        } else {
            configureRangeAxes();
            PlotChangeEvent e = new PlotChangeEvent ( this );
            notifyListeners ( e );
        }
    }
    public void addDomainMarker ( CategoryMarker marker ) {
        addDomainMarker ( marker, Layer.FOREGROUND );
    }
    public void addDomainMarker ( CategoryMarker marker, Layer layer ) {
        addDomainMarker ( 0, marker, layer );
    }
    public void addDomainMarker ( int index, CategoryMarker marker, Layer layer ) {
        addDomainMarker ( index, marker, layer, true );
    }
    public void addDomainMarker ( int index, CategoryMarker marker, Layer layer,
                                  boolean notify ) {
        ParamChecks.nullNotPermitted ( marker, "marker" );
        ParamChecks.nullNotPermitted ( layer, "layer" );
        Collection markers;
        if ( layer == Layer.FOREGROUND ) {
            markers = ( Collection ) this.foregroundDomainMarkers.get (
                          new Integer ( index ) );
            if ( markers == null ) {
                markers = new java.util.ArrayList();
                this.foregroundDomainMarkers.put ( new Integer ( index ), markers );
            }
            markers.add ( marker );
        } else if ( layer == Layer.BACKGROUND ) {
            markers = ( Collection ) this.backgroundDomainMarkers.get (
                          new Integer ( index ) );
            if ( markers == null ) {
                markers = new java.util.ArrayList();
                this.backgroundDomainMarkers.put ( new Integer ( index ), markers );
            }
            markers.add ( marker );
        }
        marker.addChangeListener ( this );
        if ( notify ) {
            fireChangeEvent();
        }
    }
    public void clearDomainMarkers() {
        if ( this.backgroundDomainMarkers != null ) {
            Set keys = this.backgroundDomainMarkers.keySet();
            Iterator iterator = keys.iterator();
            while ( iterator.hasNext() ) {
                Integer key = ( Integer ) iterator.next();
                clearDomainMarkers ( key.intValue() );
            }
            this.backgroundDomainMarkers.clear();
        }
        if ( this.foregroundDomainMarkers != null ) {
            Set keys = this.foregroundDomainMarkers.keySet();
            Iterator iterator = keys.iterator();
            while ( iterator.hasNext() ) {
                Integer key = ( Integer ) iterator.next();
                clearDomainMarkers ( key.intValue() );
            }
            this.foregroundDomainMarkers.clear();
        }
        fireChangeEvent();
    }
    public Collection getDomainMarkers ( Layer layer ) {
        return getDomainMarkers ( 0, layer );
    }
    public Collection getDomainMarkers ( int index, Layer layer ) {
        Collection result = null;
        Integer key = new Integer ( index );
        if ( layer == Layer.FOREGROUND ) {
            result = ( Collection ) this.foregroundDomainMarkers.get ( key );
        } else if ( layer == Layer.BACKGROUND ) {
            result = ( Collection ) this.backgroundDomainMarkers.get ( key );
        }
        if ( result != null ) {
            result = Collections.unmodifiableCollection ( result );
        }
        return result;
    }
    public void clearDomainMarkers ( int index ) {
        Integer key = new Integer ( index );
        if ( this.backgroundDomainMarkers != null ) {
            Collection markers
                = ( Collection ) this.backgroundDomainMarkers.get ( key );
            if ( markers != null ) {
                Iterator iterator = markers.iterator();
                while ( iterator.hasNext() ) {
                    Marker m = ( Marker ) iterator.next();
                    m.removeChangeListener ( this );
                }
                markers.clear();
            }
        }
        if ( this.foregroundDomainMarkers != null ) {
            Collection markers
                = ( Collection ) this.foregroundDomainMarkers.get ( key );
            if ( markers != null ) {
                Iterator iterator = markers.iterator();
                while ( iterator.hasNext() ) {
                    Marker m = ( Marker ) iterator.next();
                    m.removeChangeListener ( this );
                }
                markers.clear();
            }
        }
        fireChangeEvent();
    }
    public boolean removeDomainMarker ( Marker marker ) {
        return removeDomainMarker ( marker, Layer.FOREGROUND );
    }
    public boolean removeDomainMarker ( Marker marker, Layer layer ) {
        return removeDomainMarker ( 0, marker, layer );
    }
    public boolean removeDomainMarker ( int index, Marker marker, Layer layer ) {
        return removeDomainMarker ( index, marker, layer, true );
    }
    public boolean removeDomainMarker ( int index, Marker marker, Layer layer,
                                        boolean notify ) {
        ArrayList markers;
        if ( layer == Layer.FOREGROUND ) {
            markers = ( ArrayList ) this.foregroundDomainMarkers.get ( new Integer (
                          index ) );
        } else {
            markers = ( ArrayList ) this.backgroundDomainMarkers.get ( new Integer (
                          index ) );
        }
        if ( markers == null ) {
            return false;
        }
        boolean removed = markers.remove ( marker );
        if ( removed && notify ) {
            fireChangeEvent();
        }
        return removed;
    }
    public void addRangeMarker ( Marker marker ) {
        addRangeMarker ( marker, Layer.FOREGROUND );
    }
    public void addRangeMarker ( Marker marker, Layer layer ) {
        addRangeMarker ( 0, marker, layer );
    }
    public void addRangeMarker ( int index, Marker marker, Layer layer ) {
        addRangeMarker ( index, marker, layer, true );
    }
    public void addRangeMarker ( int index, Marker marker, Layer layer,
                                 boolean notify ) {
        Collection markers;
        if ( layer == Layer.FOREGROUND ) {
            markers = ( Collection ) this.foregroundRangeMarkers.get (
                          new Integer ( index ) );
            if ( markers == null ) {
                markers = new java.util.ArrayList();
                this.foregroundRangeMarkers.put ( new Integer ( index ), markers );
            }
            markers.add ( marker );
        } else if ( layer == Layer.BACKGROUND ) {
            markers = ( Collection ) this.backgroundRangeMarkers.get (
                          new Integer ( index ) );
            if ( markers == null ) {
                markers = new java.util.ArrayList();
                this.backgroundRangeMarkers.put ( new Integer ( index ), markers );
            }
            markers.add ( marker );
        }
        marker.addChangeListener ( this );
        if ( notify ) {
            fireChangeEvent();
        }
    }
    public void clearRangeMarkers() {
        if ( this.backgroundRangeMarkers != null ) {
            Set keys = this.backgroundRangeMarkers.keySet();
            Iterator iterator = keys.iterator();
            while ( iterator.hasNext() ) {
                Integer key = ( Integer ) iterator.next();
                clearRangeMarkers ( key.intValue() );
            }
            this.backgroundRangeMarkers.clear();
        }
        if ( this.foregroundRangeMarkers != null ) {
            Set keys = this.foregroundRangeMarkers.keySet();
            Iterator iterator = keys.iterator();
            while ( iterator.hasNext() ) {
                Integer key = ( Integer ) iterator.next();
                clearRangeMarkers ( key.intValue() );
            }
            this.foregroundRangeMarkers.clear();
        }
        fireChangeEvent();
    }
    public Collection getRangeMarkers ( Layer layer ) {
        return getRangeMarkers ( 0, layer );
    }
    public Collection getRangeMarkers ( int index, Layer layer ) {
        Collection result = null;
        Integer key = new Integer ( index );
        if ( layer == Layer.FOREGROUND ) {
            result = ( Collection ) this.foregroundRangeMarkers.get ( key );
        } else if ( layer == Layer.BACKGROUND ) {
            result = ( Collection ) this.backgroundRangeMarkers.get ( key );
        }
        if ( result != null ) {
            result = Collections.unmodifiableCollection ( result );
        }
        return result;
    }
    public void clearRangeMarkers ( int index ) {
        Integer key = new Integer ( index );
        if ( this.backgroundRangeMarkers != null ) {
            Collection markers
                = ( Collection ) this.backgroundRangeMarkers.get ( key );
            if ( markers != null ) {
                Iterator iterator = markers.iterator();
                while ( iterator.hasNext() ) {
                    Marker m = ( Marker ) iterator.next();
                    m.removeChangeListener ( this );
                }
                markers.clear();
            }
        }
        if ( this.foregroundRangeMarkers != null ) {
            Collection markers
                = ( Collection ) this.foregroundRangeMarkers.get ( key );
            if ( markers != null ) {
                Iterator iterator = markers.iterator();
                while ( iterator.hasNext() ) {
                    Marker m = ( Marker ) iterator.next();
                    m.removeChangeListener ( this );
                }
                markers.clear();
            }
        }
        fireChangeEvent();
    }
    public boolean removeRangeMarker ( Marker marker ) {
        return removeRangeMarker ( marker, Layer.FOREGROUND );
    }
    public boolean removeRangeMarker ( Marker marker, Layer layer ) {
        return removeRangeMarker ( 0, marker, layer );
    }
    public boolean removeRangeMarker ( int index, Marker marker, Layer layer ) {
        return removeRangeMarker ( index, marker, layer, true );
    }
    public boolean removeRangeMarker ( int index, Marker marker, Layer layer,
                                       boolean notify ) {
        ParamChecks.nullNotPermitted ( marker, "marker" );
        ArrayList markers;
        if ( layer == Layer.FOREGROUND ) {
            markers = ( ArrayList ) this.foregroundRangeMarkers.get ( new Integer (
                          index ) );
        } else {
            markers = ( ArrayList ) this.backgroundRangeMarkers.get ( new Integer (
                          index ) );
        }
        if ( markers == null ) {
            return false;
        }
        boolean removed = markers.remove ( marker );
        if ( removed && notify ) {
            fireChangeEvent();
        }
        return removed;
    }
    public boolean isDomainCrosshairVisible() {
        return this.domainCrosshairVisible;
    }
    public void setDomainCrosshairVisible ( boolean flag ) {
        if ( this.domainCrosshairVisible != flag ) {
            this.domainCrosshairVisible = flag;
            fireChangeEvent();
        }
    }
    public Comparable getDomainCrosshairRowKey() {
        return this.domainCrosshairRowKey;
    }
    public void setDomainCrosshairRowKey ( Comparable key ) {
        setDomainCrosshairRowKey ( key, true );
    }
    public void setDomainCrosshairRowKey ( Comparable key, boolean notify ) {
        this.domainCrosshairRowKey = key;
        if ( notify ) {
            fireChangeEvent();
        }
    }
    public Comparable getDomainCrosshairColumnKey() {
        return this.domainCrosshairColumnKey;
    }
    public void setDomainCrosshairColumnKey ( Comparable key ) {
        setDomainCrosshairColumnKey ( key, true );
    }
    public void setDomainCrosshairColumnKey ( Comparable key, boolean notify ) {
        this.domainCrosshairColumnKey = key;
        if ( notify ) {
            fireChangeEvent();
        }
    }
    public int getCrosshairDatasetIndex() {
        return this.crosshairDatasetIndex;
    }
    public void setCrosshairDatasetIndex ( int index ) {
        setCrosshairDatasetIndex ( index, true );
    }
    public void setCrosshairDatasetIndex ( int index, boolean notify ) {
        this.crosshairDatasetIndex = index;
        if ( notify ) {
            fireChangeEvent();
        }
    }
    public Paint getDomainCrosshairPaint() {
        return this.domainCrosshairPaint;
    }
    public void setDomainCrosshairPaint ( Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.domainCrosshairPaint = paint;
        fireChangeEvent();
    }
    public Stroke getDomainCrosshairStroke() {
        return this.domainCrosshairStroke;
    }
    public void setDomainCrosshairStroke ( Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.domainCrosshairStroke = stroke;
    }
    public boolean isRangeCrosshairVisible() {
        return this.rangeCrosshairVisible;
    }
    public void setRangeCrosshairVisible ( boolean flag ) {
        if ( this.rangeCrosshairVisible != flag ) {
            this.rangeCrosshairVisible = flag;
            fireChangeEvent();
        }
    }
    public boolean isRangeCrosshairLockedOnData() {
        return this.rangeCrosshairLockedOnData;
    }
    public void setRangeCrosshairLockedOnData ( boolean flag ) {
        if ( this.rangeCrosshairLockedOnData != flag ) {
            this.rangeCrosshairLockedOnData = flag;
            fireChangeEvent();
        }
    }
    public double getRangeCrosshairValue() {
        return this.rangeCrosshairValue;
    }
    public void setRangeCrosshairValue ( double value ) {
        setRangeCrosshairValue ( value, true );
    }
    public void setRangeCrosshairValue ( double value, boolean notify ) {
        this.rangeCrosshairValue = value;
        if ( isRangeCrosshairVisible() && notify ) {
            fireChangeEvent();
        }
    }
    public Stroke getRangeCrosshairStroke() {
        return this.rangeCrosshairStroke;
    }
    public void setRangeCrosshairStroke ( Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.rangeCrosshairStroke = stroke;
        fireChangeEvent();
    }
    public Paint getRangeCrosshairPaint() {
        return this.rangeCrosshairPaint;
    }
    public void setRangeCrosshairPaint ( Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.rangeCrosshairPaint = paint;
        fireChangeEvent();
    }
    public List getAnnotations() {
        return this.annotations;
    }
    public void addAnnotation ( CategoryAnnotation annotation ) {
        addAnnotation ( annotation, true );
    }
    public void addAnnotation ( CategoryAnnotation annotation, boolean notify ) {
        ParamChecks.nullNotPermitted ( annotation, "annotation" );
        this.annotations.add ( annotation );
        annotation.addChangeListener ( this );
        if ( notify ) {
            fireChangeEvent();
        }
    }
    public boolean removeAnnotation ( CategoryAnnotation annotation ) {
        return removeAnnotation ( annotation, true );
    }
    public boolean removeAnnotation ( CategoryAnnotation annotation,
                                      boolean notify ) {
        ParamChecks.nullNotPermitted ( annotation, "annotation" );
        boolean removed = this.annotations.remove ( annotation );
        annotation.removeChangeListener ( this );
        if ( removed && notify ) {
            fireChangeEvent();
        }
        return removed;
    }
    public void clearAnnotations() {
        for ( int i = 0; i < this.annotations.size(); i++ ) {
            CategoryAnnotation annotation
                = ( CategoryAnnotation ) this.annotations.get ( i );
            annotation.removeChangeListener ( this );
        }
        this.annotations.clear();
        fireChangeEvent();
    }
    public ShadowGenerator getShadowGenerator() {
        return this.shadowGenerator;
    }
    public void setShadowGenerator ( ShadowGenerator generator ) {
        this.shadowGenerator = generator;
        fireChangeEvent();
    }
    protected AxisSpace calculateDomainAxisSpace ( Graphics2D g2,
            Rectangle2D plotArea, AxisSpace space ) {
        if ( space == null ) {
            space = new AxisSpace();
        }
        if ( this.fixedDomainAxisSpace != null ) {
            if ( this.orientation.isHorizontal() ) {
                space.ensureAtLeast (
                    this.fixedDomainAxisSpace.getLeft(), RectangleEdge.LEFT );
                space.ensureAtLeast ( this.fixedDomainAxisSpace.getRight(),
                                      RectangleEdge.RIGHT );
            } else if ( this.orientation.isVertical() ) {
                space.ensureAtLeast ( this.fixedDomainAxisSpace.getTop(),
                                      RectangleEdge.TOP );
                space.ensureAtLeast ( this.fixedDomainAxisSpace.getBottom(),
                                      RectangleEdge.BOTTOM );
            }
        } else {
            RectangleEdge domainEdge = Plot.resolveDomainAxisLocation (
                                           getDomainAxisLocation(), this.orientation );
            if ( this.drawSharedDomainAxis ) {
                space = getDomainAxis().reserveSpace ( g2, this, plotArea,
                                                       domainEdge, space );
            }
            for ( CategoryAxis xAxis : this.domainAxes.values() ) {
                if ( xAxis != null ) {
                    int i = getDomainAxisIndex ( xAxis );
                    RectangleEdge edge = getDomainAxisEdge ( i );
                    space = xAxis.reserveSpace ( g2, this, plotArea, edge, space );
                }
            }
        }
        return space;
    }
    protected AxisSpace calculateRangeAxisSpace ( Graphics2D g2,
            Rectangle2D plotArea, AxisSpace space ) {
        if ( space == null ) {
            space = new AxisSpace();
        }
        if ( this.fixedRangeAxisSpace != null ) {
            if ( this.orientation.isHorizontal() ) {
                space.ensureAtLeast ( this.fixedRangeAxisSpace.getTop(),
                                      RectangleEdge.TOP );
                space.ensureAtLeast ( this.fixedRangeAxisSpace.getBottom(),
                                      RectangleEdge.BOTTOM );
            } else if ( this.orientation == PlotOrientation.VERTICAL ) {
                space.ensureAtLeast ( this.fixedRangeAxisSpace.getLeft(),
                                      RectangleEdge.LEFT );
                space.ensureAtLeast ( this.fixedRangeAxisSpace.getRight(),
                                      RectangleEdge.RIGHT );
            }
        } else {
            for ( ValueAxis yAxis : this.rangeAxes.values() ) {
                if ( yAxis != null ) {
                    int i = findRangeAxisIndex ( yAxis );
                    RectangleEdge edge = getRangeAxisEdge ( i );
                    space = yAxis.reserveSpace ( g2, this, plotArea, edge, space );
                }
            }
        }
        return space;
    }
    private Rectangle integerise ( Rectangle2D rect ) {
        int x0 = ( int ) Math.ceil ( rect.getMinX() );
        int y0 = ( int ) Math.ceil ( rect.getMinY() );
        int x1 = ( int ) Math.floor ( rect.getMaxX() );
        int y1 = ( int ) Math.floor ( rect.getMaxY() );
        return new Rectangle ( x0, y0, ( x1 - x0 ), ( y1 - y0 ) );
    }
    protected AxisSpace calculateAxisSpace ( Graphics2D g2,
            Rectangle2D plotArea ) {
        AxisSpace space = new AxisSpace();
        space = calculateRangeAxisSpace ( g2, plotArea, space );
        space = calculateDomainAxisSpace ( g2, plotArea, space );
        return space;
    }
    @Override
    public void draw ( Graphics2D g2, Rectangle2D area, Point2D anchor,
                       PlotState parentState, PlotRenderingInfo state ) {
        boolean b1 = ( area.getWidth() <= MINIMUM_WIDTH_TO_DRAW );
        boolean b2 = ( area.getHeight() <= MINIMUM_HEIGHT_TO_DRAW );
        if ( b1 || b2 ) {
            return;
        }
        if ( state == null ) {
            state = new PlotRenderingInfo ( null );
        }
        state.setPlotArea ( area );
        RectangleInsets insets = getInsets();
        insets.trim ( area );
        AxisSpace space = calculateAxisSpace ( g2, area );
        Rectangle2D dataArea = space.shrink ( area, null );
        this.axisOffset.trim ( dataArea );
        dataArea = integerise ( dataArea );
        if ( dataArea.isEmpty() ) {
            return;
        }
        state.setDataArea ( dataArea );
        createAndAddEntity ( ( Rectangle2D ) dataArea.clone(), state, null, null );
        if ( getRenderer() != null ) {
            getRenderer().drawBackground ( g2, this, dataArea );
        } else {
            drawBackground ( g2, dataArea );
        }
        Map axisStateMap = drawAxes ( g2, area, dataArea, state );
        if ( anchor != null && !dataArea.contains ( anchor ) ) {
            anchor = ShapeUtilities.getPointInRectangle ( anchor.getX(),
                     anchor.getY(), dataArea );
        }
        CategoryCrosshairState crosshairState = new CategoryCrosshairState();
        crosshairState.setCrosshairDistance ( Double.POSITIVE_INFINITY );
        crosshairState.setAnchor ( anchor );
        crosshairState.setAnchorX ( Double.NaN );
        crosshairState.setAnchorY ( Double.NaN );
        if ( anchor != null ) {
            ValueAxis rangeAxis = getRangeAxis();
            if ( rangeAxis != null ) {
                double y;
                if ( getOrientation() == PlotOrientation.VERTICAL ) {
                    y = rangeAxis.java2DToValue ( anchor.getY(), dataArea,
                                                  getRangeAxisEdge() );
                } else {
                    y = rangeAxis.java2DToValue ( anchor.getX(), dataArea,
                                                  getRangeAxisEdge() );
                }
                crosshairState.setAnchorY ( y );
            }
        }
        crosshairState.setRowKey ( getDomainCrosshairRowKey() );
        crosshairState.setColumnKey ( getDomainCrosshairColumnKey() );
        crosshairState.setCrosshairY ( getRangeCrosshairValue() );
        Shape savedClip = g2.getClip();
        g2.clip ( dataArea );
        drawDomainGridlines ( g2, dataArea );
        AxisState rangeAxisState = ( AxisState ) axisStateMap.get ( getRangeAxis() );
        if ( rangeAxisState == null ) {
            if ( parentState != null ) {
                rangeAxisState = ( AxisState ) parentState.getSharedAxisStates()
                                 .get ( getRangeAxis() );
            }
        }
        if ( rangeAxisState != null ) {
            drawRangeGridlines ( g2, dataArea, rangeAxisState.getTicks() );
            drawZeroRangeBaseline ( g2, dataArea );
        }
        Graphics2D savedG2 = g2;
        BufferedImage dataImage = null;
        boolean suppressShadow = Boolean.TRUE.equals ( g2.getRenderingHint (
                                     JFreeChart.KEY_SUPPRESS_SHADOW_GENERATION ) );
        if ( this.shadowGenerator != null && !suppressShadow ) {
            dataImage = new BufferedImage ( ( int ) dataArea.getWidth(),
                                            ( int ) dataArea.getHeight(), BufferedImage.TYPE_INT_ARGB );
            g2 = dataImage.createGraphics();
            g2.translate ( -dataArea.getX(), -dataArea.getY() );
            g2.setRenderingHints ( savedG2.getRenderingHints() );
        }
        for ( CategoryItemRenderer renderer : this.renderers.values() ) {
            int i = getIndexOf ( renderer );
            drawDomainMarkers ( g2, dataArea, i, Layer.BACKGROUND );
        }
        for ( CategoryItemRenderer renderer : this.renderers.values() ) {
            int i = getIndexOf ( renderer );
            drawRangeMarkers ( g2, dataArea, i, Layer.BACKGROUND );
        }
        boolean foundData = false;
        Composite originalComposite = g2.getComposite();
        g2.setComposite ( AlphaComposite.getInstance (
                              AlphaComposite.SRC_OVER, getForegroundAlpha() ) );
        DatasetRenderingOrder order = getDatasetRenderingOrder();
        List<Integer> datasetIndices = getDatasetIndices ( order );
        for ( int i : datasetIndices ) {
            foundData = render ( g2, dataArea, i, state, crosshairState )
                        || foundData;
        }
        List<Integer> rendererIndices = getRendererIndices ( order );
        for ( int i : rendererIndices ) {
            drawDomainMarkers ( g2, dataArea, i, Layer.FOREGROUND );
        }
        for ( int i : rendererIndices ) {
            drawRangeMarkers ( g2, dataArea, i, Layer.FOREGROUND );
        }
        drawAnnotations ( g2, dataArea );
        if ( this.shadowGenerator != null && !suppressShadow ) {
            BufferedImage shadowImage = this.shadowGenerator.createDropShadow (
                                            dataImage );
            g2 = savedG2;
            g2.drawImage ( shadowImage, ( int ) dataArea.getX()
                           + this.shadowGenerator.calculateOffsetX(),
                           ( int ) dataArea.getY()
                           + this.shadowGenerator.calculateOffsetY(), null );
            g2.drawImage ( dataImage, ( int ) dataArea.getX(),
                           ( int ) dataArea.getY(), null );
        }
        g2.setClip ( savedClip );
        g2.setComposite ( originalComposite );
        if ( !foundData ) {
            drawNoDataMessage ( g2, dataArea );
        }
        int datasetIndex = crosshairState.getDatasetIndex();
        setCrosshairDatasetIndex ( datasetIndex, false );
        Comparable rowKey = crosshairState.getRowKey();
        Comparable columnKey = crosshairState.getColumnKey();
        setDomainCrosshairRowKey ( rowKey, false );
        setDomainCrosshairColumnKey ( columnKey, false );
        if ( isDomainCrosshairVisible() && columnKey != null ) {
            Paint paint = getDomainCrosshairPaint();
            Stroke stroke = getDomainCrosshairStroke();
            drawDomainCrosshair ( g2, dataArea, this.orientation,
                                  datasetIndex, rowKey, columnKey, stroke, paint );
        }
        ValueAxis yAxis = getRangeAxisForDataset ( datasetIndex );
        RectangleEdge yAxisEdge = getRangeAxisEdge();
        if ( !this.rangeCrosshairLockedOnData && anchor != null ) {
            double yy;
            if ( getOrientation() == PlotOrientation.VERTICAL ) {
                yy = yAxis.java2DToValue ( anchor.getY(), dataArea, yAxisEdge );
            } else {
                yy = yAxis.java2DToValue ( anchor.getX(), dataArea, yAxisEdge );
            }
            crosshairState.setCrosshairY ( yy );
        }
        setRangeCrosshairValue ( crosshairState.getCrosshairY(), false );
        if ( isRangeCrosshairVisible() ) {
            double y = getRangeCrosshairValue();
            Paint paint = getRangeCrosshairPaint();
            Stroke stroke = getRangeCrosshairStroke();
            drawRangeCrosshair ( g2, dataArea, getOrientation(), y, yAxis,
                                 stroke, paint );
        }
        if ( isOutlineVisible() ) {
            if ( getRenderer() != null ) {
                getRenderer().drawOutline ( g2, this, dataArea );
            } else {
                drawOutline ( g2, dataArea );
            }
        }
    }
    private List<Integer> getDatasetIndices ( DatasetRenderingOrder order ) {
        List<Integer> result = new ArrayList<Integer>();
        for ( Map.Entry<Integer, CategoryDataset> entry :
                this.datasets.entrySet() ) {
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
    private List<Integer> getRendererIndices ( DatasetRenderingOrder order ) {
        List<Integer> result = new ArrayList<Integer>();
        for ( Map.Entry<Integer, CategoryItemRenderer> entry :
                this.renderers.entrySet() ) {
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
    public void drawBackground ( Graphics2D g2, Rectangle2D area ) {
        fillBackground ( g2, area, this.orientation );
        drawBackgroundImage ( g2, area );
    }
    protected Map drawAxes ( Graphics2D g2, Rectangle2D plotArea,
                             Rectangle2D dataArea, PlotRenderingInfo plotState ) {
        AxisCollection axisCollection = new AxisCollection();
        for ( CategoryAxis xAxis : this.domainAxes.values() ) {
            if ( xAxis != null ) {
                int index = getDomainAxisIndex ( xAxis );
                axisCollection.add ( xAxis, getDomainAxisEdge ( index ) );
            }
        }
        for ( ValueAxis yAxis : this.rangeAxes.values() ) {
            if ( yAxis != null ) {
                int index = findRangeAxisIndex ( yAxis );
                axisCollection.add ( yAxis, getRangeAxisEdge ( index ) );
            }
        }
        Map axisStateMap = new HashMap();
        double cursor = dataArea.getMinY() - this.axisOffset.calculateTopOutset (
                            dataArea.getHeight() );
        Iterator iterator = axisCollection.getAxesAtTop().iterator();
        while ( iterator.hasNext() ) {
            Axis axis = ( Axis ) iterator.next();
            if ( axis != null ) {
                AxisState axisState = axis.draw ( g2, cursor, plotArea, dataArea,
                                                  RectangleEdge.TOP, plotState );
                cursor = axisState.getCursor();
                axisStateMap.put ( axis, axisState );
            }
        }
        cursor = dataArea.getMaxY()
                 + this.axisOffset.calculateBottomOutset ( dataArea.getHeight() );
        iterator = axisCollection.getAxesAtBottom().iterator();
        while ( iterator.hasNext() ) {
            Axis axis = ( Axis ) iterator.next();
            if ( axis != null ) {
                AxisState axisState = axis.draw ( g2, cursor, plotArea, dataArea,
                                                  RectangleEdge.BOTTOM, plotState );
                cursor = axisState.getCursor();
                axisStateMap.put ( axis, axisState );
            }
        }
        cursor = dataArea.getMinX()
                 - this.axisOffset.calculateLeftOutset ( dataArea.getWidth() );
        iterator = axisCollection.getAxesAtLeft().iterator();
        while ( iterator.hasNext() ) {
            Axis axis = ( Axis ) iterator.next();
            if ( axis != null ) {
                AxisState axisState = axis.draw ( g2, cursor, plotArea, dataArea,
                                                  RectangleEdge.LEFT, plotState );
                cursor = axisState.getCursor();
                axisStateMap.put ( axis, axisState );
            }
        }
        cursor = dataArea.getMaxX()
                 + this.axisOffset.calculateRightOutset ( dataArea.getWidth() );
        iterator = axisCollection.getAxesAtRight().iterator();
        while ( iterator.hasNext() ) {
            Axis axis = ( Axis ) iterator.next();
            if ( axis != null ) {
                AxisState axisState = axis.draw ( g2, cursor, plotArea, dataArea,
                                                  RectangleEdge.RIGHT, plotState );
                cursor = axisState.getCursor();
                axisStateMap.put ( axis, axisState );
            }
        }
        return axisStateMap;
    }
    public boolean render ( Graphics2D g2, Rectangle2D dataArea, int index,
                            PlotRenderingInfo info, CategoryCrosshairState crosshairState ) {
        boolean foundData = false;
        CategoryDataset currentDataset = getDataset ( index );
        CategoryItemRenderer renderer = getRenderer ( index );
        CategoryAxis domainAxis = getDomainAxisForDataset ( index );
        ValueAxis rangeAxis = getRangeAxisForDataset ( index );
        boolean hasData = !DatasetUtilities.isEmptyOrNull ( currentDataset );
        if ( hasData && renderer != null ) {
            foundData = true;
            CategoryItemRendererState state = renderer.initialise ( g2, dataArea,
                                              this, index, info );
            state.setCrosshairState ( crosshairState );
            int columnCount = currentDataset.getColumnCount();
            int rowCount = currentDataset.getRowCount();
            int passCount = renderer.getPassCount();
            for ( int pass = 0; pass < passCount; pass++ ) {
                if ( this.columnRenderingOrder == SortOrder.ASCENDING ) {
                    for ( int column = 0; column < columnCount; column++ ) {
                        if ( this.rowRenderingOrder == SortOrder.ASCENDING ) {
                            for ( int row = 0; row < rowCount; row++ ) {
                                renderer.drawItem ( g2, state, dataArea, this,
                                                    domainAxis, rangeAxis, currentDataset,
                                                    row, column, pass );
                            }
                        } else {
                            for ( int row = rowCount - 1; row >= 0; row-- ) {
                                renderer.drawItem ( g2, state, dataArea, this,
                                                    domainAxis, rangeAxis, currentDataset,
                                                    row, column, pass );
                            }
                        }
                    }
                } else {
                    for ( int column = columnCount - 1; column >= 0; column-- ) {
                        if ( this.rowRenderingOrder == SortOrder.ASCENDING ) {
                            for ( int row = 0; row < rowCount; row++ ) {
                                renderer.drawItem ( g2, state, dataArea, this,
                                                    domainAxis, rangeAxis, currentDataset,
                                                    row, column, pass );
                            }
                        } else {
                            for ( int row = rowCount - 1; row >= 0; row-- ) {
                                renderer.drawItem ( g2, state, dataArea, this,
                                                    domainAxis, rangeAxis, currentDataset,
                                                    row, column, pass );
                            }
                        }
                    }
                }
            }
        }
        return foundData;
    }
    protected void drawDomainGridlines ( Graphics2D g2, Rectangle2D dataArea ) {
        if ( !isDomainGridlinesVisible() ) {
            return;
        }
        CategoryAnchor anchor = getDomainGridlinePosition();
        RectangleEdge domainAxisEdge = getDomainAxisEdge();
        CategoryDataset dataset = getDataset();
        if ( dataset == null ) {
            return;
        }
        CategoryAxis axis = getDomainAxis();
        if ( axis != null ) {
            int columnCount = dataset.getColumnCount();
            for ( int c = 0; c < columnCount; c++ ) {
                double xx = axis.getCategoryJava2DCoordinate ( anchor, c,
                            columnCount, dataArea, domainAxisEdge );
                CategoryItemRenderer renderer1 = getRenderer();
                if ( renderer1 != null ) {
                    renderer1.drawDomainGridline ( g2, this, dataArea, xx );
                }
            }
        }
    }
    protected void drawRangeGridlines ( Graphics2D g2, Rectangle2D dataArea,
                                        List ticks ) {
        if ( !isRangeGridlinesVisible() && !isRangeMinorGridlinesVisible() ) {
            return;
        }
        ValueAxis axis = getRangeAxis();
        if ( axis == null ) {
            return;
        }
        CategoryItemRenderer r = getRenderer();
        if ( r == null ) {
            return;
        }
        Stroke gridStroke = null;
        Paint gridPaint = null;
        boolean paintLine;
        Iterator iterator = ticks.iterator();
        while ( iterator.hasNext() ) {
            paintLine = false;
            ValueTick tick = ( ValueTick ) iterator.next();
            if ( ( tick.getTickType() == TickType.MINOR )
                    && isRangeMinorGridlinesVisible() ) {
                gridStroke = getRangeMinorGridlineStroke();
                gridPaint = getRangeMinorGridlinePaint();
                paintLine = true;
            } else if ( ( tick.getTickType() == TickType.MAJOR )
                        && isRangeGridlinesVisible() ) {
                gridStroke = getRangeGridlineStroke();
                gridPaint = getRangeGridlinePaint();
                paintLine = true;
            }
            if ( ( ( tick.getValue() != 0.0 )
                    || !isRangeZeroBaselineVisible() ) && paintLine ) {
                if ( r instanceof AbstractCategoryItemRenderer ) {
                    AbstractCategoryItemRenderer aci
                        = ( AbstractCategoryItemRenderer ) r;
                    aci.drawRangeLine ( g2, this, axis, dataArea,
                                        tick.getValue(), gridPaint, gridStroke );
                } else {
                    r.drawRangeGridline ( g2, this, axis, dataArea,
                                          tick.getValue() );
                }
            }
        }
    }
    protected void drawZeroRangeBaseline ( Graphics2D g2, Rectangle2D area ) {
        if ( !isRangeZeroBaselineVisible() ) {
            return;
        }
        CategoryItemRenderer r = getRenderer();
        if ( r instanceof AbstractCategoryItemRenderer ) {
            AbstractCategoryItemRenderer aci = ( AbstractCategoryItemRenderer ) r;
            aci.drawRangeLine ( g2, this, getRangeAxis(), area, 0.0,
                                this.rangeZeroBaselinePaint, this.rangeZeroBaselineStroke );
        } else {
            r.drawRangeGridline ( g2, this, getRangeAxis(), area, 0.0 );
        }
    }
    protected void drawAnnotations ( Graphics2D g2, Rectangle2D dataArea ) {
        if ( getAnnotations() != null ) {
            Iterator iterator = getAnnotations().iterator();
            while ( iterator.hasNext() ) {
                CategoryAnnotation annotation
                    = ( CategoryAnnotation ) iterator.next();
                annotation.draw ( g2, this, dataArea, getDomainAxis(),
                                  getRangeAxis() );
            }
        }
    }
    protected void drawDomainMarkers ( Graphics2D g2, Rectangle2D dataArea,
                                       int index, Layer layer ) {
        CategoryItemRenderer r = getRenderer ( index );
        if ( r == null ) {
            return;
        }
        Collection markers = getDomainMarkers ( index, layer );
        CategoryAxis axis = getDomainAxisForDataset ( index );
        if ( markers != null && axis != null ) {
            Iterator iterator = markers.iterator();
            while ( iterator.hasNext() ) {
                CategoryMarker marker = ( CategoryMarker ) iterator.next();
                r.drawDomainMarker ( g2, this, axis, marker, dataArea );
            }
        }
    }
    protected void drawRangeMarkers ( Graphics2D g2, Rectangle2D dataArea,
                                      int index, Layer layer ) {
        CategoryItemRenderer r = getRenderer ( index );
        if ( r == null ) {
            return;
        }
        Collection markers = getRangeMarkers ( index, layer );
        ValueAxis axis = getRangeAxisForDataset ( index );
        if ( markers != null && axis != null ) {
            Iterator iterator = markers.iterator();
            while ( iterator.hasNext() ) {
                Marker marker = ( Marker ) iterator.next();
                r.drawRangeMarker ( g2, this, axis, marker, dataArea );
            }
        }
    }
    protected void drawRangeLine ( Graphics2D g2, Rectangle2D dataArea,
                                   double value, Stroke stroke, Paint paint ) {
        double java2D = getRangeAxis().valueToJava2D ( value, dataArea,
                        getRangeAxisEdge() );
        Line2D line = null;
        if ( this.orientation == PlotOrientation.HORIZONTAL ) {
            line = new Line2D.Double ( java2D, dataArea.getMinY(), java2D,
                                       dataArea.getMaxY() );
        } else if ( this.orientation == PlotOrientation.VERTICAL ) {
            line = new Line2D.Double ( dataArea.getMinX(), java2D,
                                       dataArea.getMaxX(), java2D );
        }
        g2.setStroke ( stroke );
        g2.setPaint ( paint );
        g2.draw ( line );
    }
    protected void drawDomainCrosshair ( Graphics2D g2, Rectangle2D dataArea,
                                         PlotOrientation orientation, int datasetIndex,
                                         Comparable rowKey, Comparable columnKey, Stroke stroke,
                                         Paint paint ) {
        CategoryDataset dataset = getDataset ( datasetIndex );
        CategoryAxis axis = getDomainAxisForDataset ( datasetIndex );
        CategoryItemRenderer renderer = getRenderer ( datasetIndex );
        Line2D line;
        if ( orientation == PlotOrientation.VERTICAL ) {
            double xx = renderer.getItemMiddle ( rowKey, columnKey, dataset, axis,
                                                 dataArea, RectangleEdge.BOTTOM );
            line = new Line2D.Double ( xx, dataArea.getMinY(), xx,
                                       dataArea.getMaxY() );
        } else {
            double yy = renderer.getItemMiddle ( rowKey, columnKey, dataset, axis,
                                                 dataArea, RectangleEdge.LEFT );
            line = new Line2D.Double ( dataArea.getMinX(), yy,
                                       dataArea.getMaxX(), yy );
        }
        g2.setStroke ( stroke );
        g2.setPaint ( paint );
        g2.draw ( line );
    }
    protected void drawRangeCrosshair ( Graphics2D g2, Rectangle2D dataArea,
                                        PlotOrientation orientation, double value, ValueAxis axis,
                                        Stroke stroke, Paint paint ) {
        if ( !axis.getRange().contains ( value ) ) {
            return;
        }
        Line2D line;
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            double xx = axis.valueToJava2D ( value, dataArea,
                                             RectangleEdge.BOTTOM );
            line = new Line2D.Double ( xx, dataArea.getMinY(), xx,
                                       dataArea.getMaxY() );
        } else {
            double yy = axis.valueToJava2D ( value, dataArea,
                                             RectangleEdge.LEFT );
            line = new Line2D.Double ( dataArea.getMinX(), yy,
                                       dataArea.getMaxX(), yy );
        }
        g2.setStroke ( stroke );
        g2.setPaint ( paint );
        g2.draw ( line );
    }
    @Override
    public Range getDataRange ( ValueAxis axis ) {
        Range result = null;
        List<CategoryDataset> mappedDatasets = new ArrayList<CategoryDataset>();
        int rangeIndex = findRangeAxisIndex ( axis );
        if ( rangeIndex >= 0 ) {
            mappedDatasets.addAll ( datasetsMappedToRangeAxis ( rangeIndex ) );
        } else if ( axis == getRangeAxis() ) {
            mappedDatasets.addAll ( datasetsMappedToRangeAxis ( 0 ) );
        }
        for ( CategoryDataset d : mappedDatasets ) {
            CategoryItemRenderer r = getRendererForDataset ( d );
            if ( r != null ) {
                result = Range.combine ( result, r.findRangeBounds ( d ) );
            }
        }
        return result;
    }
    private List<CategoryDataset> datasetsMappedToDomainAxis ( int axisIndex ) {
        List<CategoryDataset> result = new ArrayList<CategoryDataset>();
        for ( Entry<Integer, CategoryDataset> entry : this.datasets.entrySet() ) {
            CategoryDataset dataset = entry.getValue();
            if ( dataset == null ) {
                continue;
            }
            Integer datasetIndex = entry.getKey();
            List mappedAxes = ( List ) this.datasetToDomainAxesMap.get (
                                  datasetIndex );
            if ( mappedAxes == null ) {
                if ( axisIndex == 0 ) {
                    result.add ( dataset );
                }
            } else {
                if ( mappedAxes.contains ( axisIndex ) ) {
                    result.add ( dataset );
                }
            }
        }
        return result;
    }
    private List<CategoryDataset> datasetsMappedToRangeAxis ( int axisIndex ) {
        List<CategoryDataset> result = new ArrayList<CategoryDataset>();
        for ( Entry<Integer, CategoryDataset> entry : this.datasets.entrySet() ) {
            Integer datasetIndex = entry.getKey();
            CategoryDataset dataset = entry.getValue();
            List mappedAxes = ( List ) this.datasetToRangeAxesMap.get (
                                  datasetIndex );
            if ( mappedAxes == null ) {
                if ( axisIndex == 0 ) {
                    result.add ( dataset );
                }
            } else {
                if ( mappedAxes.contains ( axisIndex ) ) {
                    result.add ( dataset );
                }
            }
        }
        return result;
    }
    public int getWeight() {
        return this.weight;
    }
    public void setWeight ( int weight ) {
        this.weight = weight;
        fireChangeEvent();
    }
    public AxisSpace getFixedDomainAxisSpace() {
        return this.fixedDomainAxisSpace;
    }
    public void setFixedDomainAxisSpace ( AxisSpace space ) {
        setFixedDomainAxisSpace ( space, true );
    }
    public void setFixedDomainAxisSpace ( AxisSpace space, boolean notify ) {
        this.fixedDomainAxisSpace = space;
        if ( notify ) {
            fireChangeEvent();
        }
    }
    public AxisSpace getFixedRangeAxisSpace() {
        return this.fixedRangeAxisSpace;
    }
    public void setFixedRangeAxisSpace ( AxisSpace space ) {
        setFixedRangeAxisSpace ( space, true );
    }
    public void setFixedRangeAxisSpace ( AxisSpace space, boolean notify ) {
        this.fixedRangeAxisSpace = space;
        if ( notify ) {
            fireChangeEvent();
        }
    }
    public List getCategories() {
        List result = null;
        if ( getDataset() != null ) {
            result = Collections.unmodifiableList ( getDataset().getColumnKeys() );
        }
        return result;
    }
    public List getCategoriesForAxis ( CategoryAxis axis ) {
        List result = new ArrayList();
        int axisIndex = getDomainAxisIndex ( axis );
        for ( CategoryDataset dataset : datasetsMappedToDomainAxis ( axisIndex ) ) {
            for ( int i = 0; i < dataset.getColumnCount(); i++ ) {
                Comparable category = dataset.getColumnKey ( i );
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
    public void setDrawSharedDomainAxis ( boolean draw ) {
        this.drawSharedDomainAxis = draw;
        fireChangeEvent();
    }
    @Override
    public boolean isDomainPannable() {
        return false;
    }
    @Override
    public boolean isRangePannable() {
        return this.rangePannable;
    }
    public void setRangePannable ( boolean pannable ) {
        this.rangePannable = pannable;
    }
    @Override
    public void panDomainAxes ( double percent, PlotRenderingInfo info,
                                Point2D source ) {
    }
    @Override
    public void panRangeAxes ( double percent, PlotRenderingInfo info,
                               Point2D source ) {
        if ( !isRangePannable() ) {
            return;
        }
        for ( ValueAxis axis : this.rangeAxes.values() ) {
            if ( axis == null ) {
                continue;
            }
            double length = axis.getRange().getLength();
            double adj = percent * length;
            if ( axis.isInverted() ) {
                adj = -adj;
            }
            axis.setRange ( axis.getLowerBound() + adj,
                            axis.getUpperBound() + adj );
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
    public void zoomDomainAxes ( double factor, PlotRenderingInfo state,
                                 Point2D source ) {
    }
    @Override
    public void zoomDomainAxes ( double lowerPercent, double upperPercent,
                                 PlotRenderingInfo state, Point2D source ) {
    }
    @Override
    public void zoomDomainAxes ( double factor, PlotRenderingInfo info,
                                 Point2D source, boolean useAnchor ) {
    }
    @Override
    public void zoomRangeAxes ( double factor, PlotRenderingInfo state,
                                Point2D source ) {
        zoomRangeAxes ( factor, state, source, false );
    }
    @Override
    public void zoomRangeAxes ( double factor, PlotRenderingInfo info,
                                Point2D source, boolean useAnchor ) {
        for ( ValueAxis rangeAxis : this.rangeAxes.values() ) {
            if ( rangeAxis == null ) {
                continue;
            }
            if ( useAnchor ) {
                double sourceY = source.getY();
                if ( this.orientation.isHorizontal() ) {
                    sourceY = source.getX();
                }
                double anchorY = rangeAxis.java2DToValue ( sourceY,
                                 info.getDataArea(), getRangeAxisEdge() );
                rangeAxis.resizeRange2 ( factor, anchorY );
            } else {
                rangeAxis.resizeRange ( factor );
            }
        }
    }
    @Override
    public void zoomRangeAxes ( double lowerPercent, double upperPercent,
                                PlotRenderingInfo state, Point2D source ) {
        for ( ValueAxis yAxis : this.rangeAxes.values() ) {
            if ( yAxis != null ) {
                yAxis.zoomRange ( lowerPercent, upperPercent );
            }
        }
    }
    public double getAnchorValue() {
        return this.anchorValue;
    }
    public void setAnchorValue ( double value ) {
        setAnchorValue ( value, true );
    }
    public void setAnchorValue ( double value, boolean notify ) {
        this.anchorValue = value;
        if ( notify ) {
            fireChangeEvent();
        }
    }
    @Override
    public boolean equals ( Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof CategoryPlot ) ) {
            return false;
        }
        CategoryPlot that = ( CategoryPlot ) obj;
        if ( this.orientation != that.orientation ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.axisOffset, that.axisOffset ) ) {
            return false;
        }
        if ( !this.domainAxes.equals ( that.domainAxes ) ) {
            return false;
        }
        if ( !this.domainAxisLocations.equals ( that.domainAxisLocations ) ) {
            return false;
        }
        if ( this.drawSharedDomainAxis != that.drawSharedDomainAxis ) {
            return false;
        }
        if ( !this.rangeAxes.equals ( that.rangeAxes ) ) {
            return false;
        }
        if ( !this.rangeAxisLocations.equals ( that.rangeAxisLocations ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.datasetToDomainAxesMap,
                                      that.datasetToDomainAxesMap ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.datasetToRangeAxesMap,
                                      that.datasetToRangeAxesMap ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.renderers, that.renderers ) ) {
            return false;
        }
        if ( this.renderingOrder != that.renderingOrder ) {
            return false;
        }
        if ( this.columnRenderingOrder != that.columnRenderingOrder ) {
            return false;
        }
        if ( this.rowRenderingOrder != that.rowRenderingOrder ) {
            return false;
        }
        if ( this.domainGridlinesVisible != that.domainGridlinesVisible ) {
            return false;
        }
        if ( this.domainGridlinePosition != that.domainGridlinePosition ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.domainGridlineStroke,
                                      that.domainGridlineStroke ) ) {
            return false;
        }
        if ( !PaintUtilities.equal ( this.domainGridlinePaint,
                                     that.domainGridlinePaint ) ) {
            return false;
        }
        if ( this.rangeGridlinesVisible != that.rangeGridlinesVisible ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.rangeGridlineStroke,
                                      that.rangeGridlineStroke ) ) {
            return false;
        }
        if ( !PaintUtilities.equal ( this.rangeGridlinePaint,
                                     that.rangeGridlinePaint ) ) {
            return false;
        }
        if ( this.anchorValue != that.anchorValue ) {
            return false;
        }
        if ( this.rangeCrosshairVisible != that.rangeCrosshairVisible ) {
            return false;
        }
        if ( this.rangeCrosshairValue != that.rangeCrosshairValue ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.rangeCrosshairStroke,
                                      that.rangeCrosshairStroke ) ) {
            return false;
        }
        if ( !PaintUtilities.equal ( this.rangeCrosshairPaint,
                                     that.rangeCrosshairPaint ) ) {
            return false;
        }
        if ( this.rangeCrosshairLockedOnData
                != that.rangeCrosshairLockedOnData ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.foregroundDomainMarkers,
                                      that.foregroundDomainMarkers ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.backgroundDomainMarkers,
                                      that.backgroundDomainMarkers ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.foregroundRangeMarkers,
                                      that.foregroundRangeMarkers ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.backgroundRangeMarkers,
                                      that.backgroundRangeMarkers ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.annotations, that.annotations ) ) {
            return false;
        }
        if ( this.weight != that.weight ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.fixedDomainAxisSpace,
                                      that.fixedDomainAxisSpace ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.fixedRangeAxisSpace,
                                      that.fixedRangeAxisSpace ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.fixedLegendItems,
                                      that.fixedLegendItems ) ) {
            return false;
        }
        if ( this.domainCrosshairVisible != that.domainCrosshairVisible ) {
            return false;
        }
        if ( this.crosshairDatasetIndex != that.crosshairDatasetIndex ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.domainCrosshairColumnKey,
                                      that.domainCrosshairColumnKey ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.domainCrosshairRowKey,
                                      that.domainCrosshairRowKey ) ) {
            return false;
        }
        if ( !PaintUtilities.equal ( this.domainCrosshairPaint,
                                     that.domainCrosshairPaint ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.domainCrosshairStroke,
                                      that.domainCrosshairStroke ) ) {
            return false;
        }
        if ( this.rangeMinorGridlinesVisible
                != that.rangeMinorGridlinesVisible ) {
            return false;
        }
        if ( !PaintUtilities.equal ( this.rangeMinorGridlinePaint,
                                     that.rangeMinorGridlinePaint ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.rangeMinorGridlineStroke,
                                      that.rangeMinorGridlineStroke ) ) {
            return false;
        }
        if ( this.rangeZeroBaselineVisible != that.rangeZeroBaselineVisible ) {
            return false;
        }
        if ( !PaintUtilities.equal ( this.rangeZeroBaselinePaint,
                                     that.rangeZeroBaselinePaint ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.rangeZeroBaselineStroke,
                                      that.rangeZeroBaselineStroke ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.shadowGenerator,
                                      that.shadowGenerator ) ) {
            return false;
        }
        return super.equals ( obj );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        CategoryPlot clone = ( CategoryPlot ) super.clone();
        clone.domainAxes = CloneUtils.cloneMapValues ( this.domainAxes );
        for ( CategoryAxis axis : clone.domainAxes.values() ) {
            if ( axis != null ) {
                axis.setPlot ( clone );
                axis.addChangeListener ( clone );
            }
        }
        clone.rangeAxes = CloneUtils.cloneMapValues ( this.rangeAxes );
        for ( ValueAxis axis : clone.rangeAxes.values() ) {
            if ( axis != null ) {
                axis.setPlot ( clone );
                axis.addChangeListener ( clone );
            }
        }
        clone.domainAxisLocations = new HashMap<Integer, AxisLocation> (
            this.domainAxisLocations );
        clone.rangeAxisLocations = new HashMap<Integer, AxisLocation> (
            this.rangeAxisLocations );
        clone.datasets = new HashMap<Integer, CategoryDataset> ( this.datasets );
        for ( CategoryDataset dataset : clone.datasets.values() ) {
            if ( dataset != null ) {
                dataset.addChangeListener ( clone );
            }
        }
        clone.datasetToDomainAxesMap = new TreeMap();
        clone.datasetToDomainAxesMap.putAll ( this.datasetToDomainAxesMap );
        clone.datasetToRangeAxesMap = new TreeMap();
        clone.datasetToRangeAxesMap.putAll ( this.datasetToRangeAxesMap );
        clone.renderers = CloneUtils.cloneMapValues ( this.renderers );
        for ( CategoryItemRenderer renderer : clone.renderers.values() ) {
            if ( renderer != null ) {
                renderer.setPlot ( clone );
                renderer.addChangeListener ( clone );
            }
        }
        if ( this.fixedDomainAxisSpace != null ) {
            clone.fixedDomainAxisSpace = ( AxisSpace ) ObjectUtilities.clone (
                                             this.fixedDomainAxisSpace );
        }
        if ( this.fixedRangeAxisSpace != null ) {
            clone.fixedRangeAxisSpace = ( AxisSpace ) ObjectUtilities.clone (
                                            this.fixedRangeAxisSpace );
        }
        clone.annotations = ( List ) ObjectUtilities.deepClone ( this.annotations );
        clone.foregroundDomainMarkers = cloneMarkerMap (
                                            this.foregroundDomainMarkers );
        clone.backgroundDomainMarkers = cloneMarkerMap (
                                            this.backgroundDomainMarkers );
        clone.foregroundRangeMarkers = cloneMarkerMap (
                                           this.foregroundRangeMarkers );
        clone.backgroundRangeMarkers = cloneMarkerMap (
                                           this.backgroundRangeMarkers );
        if ( this.fixedLegendItems != null ) {
            clone.fixedLegendItems
                = ( LegendItemCollection ) this.fixedLegendItems.clone();
        }
        return clone;
    }
    private Map cloneMarkerMap ( Map map ) throws CloneNotSupportedException {
        Map clone = new HashMap();
        Set keys = map.keySet();
        Iterator iterator = keys.iterator();
        while ( iterator.hasNext() ) {
            Object key = iterator.next();
            List entry = ( List ) map.get ( key );
            Object toAdd = ObjectUtilities.deepClone ( entry );
            clone.put ( key, toAdd );
        }
        return clone;
    }
    private void writeObject ( ObjectOutputStream stream ) throws IOException {
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
    private void readObject ( ObjectInputStream stream )
    throws IOException, ClassNotFoundException {
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
        for ( CategoryAxis xAxis : this.domainAxes.values() ) {
            if ( xAxis != null ) {
                xAxis.setPlot ( this );
                xAxis.addChangeListener ( this );
            }
        }
        for ( ValueAxis yAxis : this.rangeAxes.values() ) {
            if ( yAxis != null ) {
                yAxis.setPlot ( this );
                yAxis.addChangeListener ( this );
            }
        }
        for ( CategoryDataset dataset : this.datasets.values() ) {
            if ( dataset != null ) {
                dataset.addChangeListener ( this );
            }
        }
        for ( CategoryItemRenderer renderer : this.renderers.values() ) {
            if ( renderer != null ) {
                renderer.addChangeListener ( this );
            }
        }
    }
}
