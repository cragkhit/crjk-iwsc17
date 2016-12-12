package org.jfree.chart.plot;
import org.jfree.chart.util.ResourceBundleWrapper;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.chart.util.CloneUtils;
import org.jfree.util.PaintUtilities;
import org.jfree.util.ObjectUtilities;
import org.jfree.chart.LegendItem;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.event.ChartChangeEventType;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.AnnotationChangeEvent;
import org.jfree.chart.annotations.XYAnnotationBoundsInfo;
import org.jfree.data.Range;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.axis.TickType;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.RendererUtilities;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.chart.axis.AxisCollection;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.ValueTick;
import java.util.Collections;
import java.awt.image.ImageObserver;
import java.awt.Image;
import java.awt.image.BufferedImage;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisState;
import java.awt.Composite;
import java.awt.AlphaComposite;
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.chart.event.AnnotationChangeListener;
import org.jfree.chart.event.MarkerChangeListener;
import org.jfree.ui.Layer;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetChangeEvent;
import java.util.Iterator;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.util.ParamChecks;
import java.awt.BasicStroke;
import java.awt.Color;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.data.general.DatasetChangeListener;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.HashMap;
import org.jfree.chart.util.ShadowGenerator;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.annotations.XYAnnotation;
import java.awt.geom.Point2D;
import java.util.List;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.ValueAxis;
import java.util.Map;
import org.jfree.ui.RectangleInsets;
import java.util.ResourceBundle;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
import org.jfree.chart.event.RendererChangeListener;
public class XYPlot extends Plot implements ValueAxisPlot, Pannable, Zoomable, RendererChangeListener, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 7044148245716569264L;
    public static final Stroke DEFAULT_GRIDLINE_STROKE;
    public static final Paint DEFAULT_GRIDLINE_PAINT;
    public static final boolean DEFAULT_CROSSHAIR_VISIBLE = false;
    public static final Stroke DEFAULT_CROSSHAIR_STROKE;
    public static final Paint DEFAULT_CROSSHAIR_PAINT;
    protected static ResourceBundle localizationResources;
    private PlotOrientation orientation;
    private RectangleInsets axisOffset;
    private Map<Integer, ValueAxis> domainAxes;
    private Map<Integer, AxisLocation> domainAxisLocations;
    private Map<Integer, ValueAxis> rangeAxes;
    private Map<Integer, AxisLocation> rangeAxisLocations;
    private Map<Integer, XYDataset> datasets;
    private Map<Integer, XYItemRenderer> renderers;
    private Map<Integer, List<Integer>> datasetToDomainAxesMap;
    private Map<Integer, List<Integer>> datasetToRangeAxesMap;
    private transient Point2D quadrantOrigin;
    private transient Paint[] quadrantPaint;
    private boolean domainGridlinesVisible;
    private transient Stroke domainGridlineStroke;
    private transient Paint domainGridlinePaint;
    private boolean rangeGridlinesVisible;
    private transient Stroke rangeGridlineStroke;
    private transient Paint rangeGridlinePaint;
    private boolean domainMinorGridlinesVisible;
    private transient Stroke domainMinorGridlineStroke;
    private transient Paint domainMinorGridlinePaint;
    private boolean rangeMinorGridlinesVisible;
    private transient Stroke rangeMinorGridlineStroke;
    private transient Paint rangeMinorGridlinePaint;
    private boolean domainZeroBaselineVisible;
    private transient Stroke domainZeroBaselineStroke;
    private transient Paint domainZeroBaselinePaint;
    private boolean rangeZeroBaselineVisible;
    private transient Stroke rangeZeroBaselineStroke;
    private transient Paint rangeZeroBaselinePaint;
    private boolean domainCrosshairVisible;
    private double domainCrosshairValue;
    private transient Stroke domainCrosshairStroke;
    private transient Paint domainCrosshairPaint;
    private boolean domainCrosshairLockedOnData;
    private boolean rangeCrosshairVisible;
    private double rangeCrosshairValue;
    private transient Stroke rangeCrosshairStroke;
    private transient Paint rangeCrosshairPaint;
    private boolean rangeCrosshairLockedOnData;
    private Map foregroundDomainMarkers;
    private Map backgroundDomainMarkers;
    private Map foregroundRangeMarkers;
    private Map backgroundRangeMarkers;
    private List<XYAnnotation> annotations;
    private transient Paint domainTickBandPaint;
    private transient Paint rangeTickBandPaint;
    private AxisSpace fixedDomainAxisSpace;
    private AxisSpace fixedRangeAxisSpace;
    private DatasetRenderingOrder datasetRenderingOrder;
    private SeriesRenderingOrder seriesRenderingOrder;
    private int weight;
    private LegendItemCollection fixedLegendItems;
    private boolean domainPannable;
    private boolean rangePannable;
    private ShadowGenerator shadowGenerator;
    public XYPlot() {
        this ( null, null, null, null );
    }
    public XYPlot ( final XYDataset dataset, final ValueAxis domainAxis, final ValueAxis rangeAxis, final XYItemRenderer renderer ) {
        this.quadrantOrigin = new Point2D.Double ( 0.0, 0.0 );
        this.quadrantPaint = new Paint[] { null, null, null, null };
        this.domainCrosshairLockedOnData = true;
        this.rangeCrosshairLockedOnData = true;
        this.datasetRenderingOrder = DatasetRenderingOrder.REVERSE;
        this.seriesRenderingOrder = SeriesRenderingOrder.REVERSE;
        this.orientation = PlotOrientation.VERTICAL;
        this.weight = 1;
        this.axisOffset = RectangleInsets.ZERO_INSETS;
        this.domainAxes = new HashMap<Integer, ValueAxis>();
        this.domainAxisLocations = new HashMap<Integer, AxisLocation>();
        this.foregroundDomainMarkers = new HashMap();
        this.backgroundDomainMarkers = new HashMap();
        this.rangeAxes = new HashMap<Integer, ValueAxis>();
        this.rangeAxisLocations = new HashMap<Integer, AxisLocation>();
        this.foregroundRangeMarkers = new HashMap();
        this.backgroundRangeMarkers = new HashMap();
        this.datasets = new HashMap<Integer, XYDataset>();
        this.renderers = new HashMap<Integer, XYItemRenderer>();
        this.datasetToDomainAxesMap = new TreeMap<Integer, List<Integer>>();
        this.datasetToRangeAxesMap = new TreeMap<Integer, List<Integer>>();
        this.annotations = new ArrayList<XYAnnotation>();
        this.datasets.put ( 0, dataset );
        if ( dataset != null ) {
            dataset.addChangeListener ( this );
        }
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
        this.domainAxisLocations.put ( 0, AxisLocation.BOTTOM_OR_LEFT );
        this.rangeAxes.put ( 0, rangeAxis );
        this.mapDatasetToRangeAxis ( 0, 0 );
        if ( rangeAxis != null ) {
            rangeAxis.setPlot ( this );
            rangeAxis.addChangeListener ( this );
        }
        this.rangeAxisLocations.put ( 0, AxisLocation.BOTTOM_OR_LEFT );
        this.configureDomainAxes();
        this.configureRangeAxes();
        this.domainGridlinesVisible = true;
        this.domainGridlineStroke = XYPlot.DEFAULT_GRIDLINE_STROKE;
        this.domainGridlinePaint = XYPlot.DEFAULT_GRIDLINE_PAINT;
        this.domainMinorGridlinesVisible = false;
        this.domainMinorGridlineStroke = XYPlot.DEFAULT_GRIDLINE_STROKE;
        this.domainMinorGridlinePaint = Color.white;
        this.domainZeroBaselineVisible = false;
        this.domainZeroBaselinePaint = Color.black;
        this.domainZeroBaselineStroke = new BasicStroke ( 0.5f );
        this.rangeGridlinesVisible = true;
        this.rangeGridlineStroke = XYPlot.DEFAULT_GRIDLINE_STROKE;
        this.rangeGridlinePaint = XYPlot.DEFAULT_GRIDLINE_PAINT;
        this.rangeMinorGridlinesVisible = false;
        this.rangeMinorGridlineStroke = XYPlot.DEFAULT_GRIDLINE_STROKE;
        this.rangeMinorGridlinePaint = Color.white;
        this.rangeZeroBaselineVisible = false;
        this.rangeZeroBaselinePaint = Color.black;
        this.rangeZeroBaselineStroke = new BasicStroke ( 0.5f );
        this.domainCrosshairVisible = false;
        this.domainCrosshairValue = 0.0;
        this.domainCrosshairStroke = XYPlot.DEFAULT_CROSSHAIR_STROKE;
        this.domainCrosshairPaint = XYPlot.DEFAULT_CROSSHAIR_PAINT;
        this.rangeCrosshairVisible = false;
        this.rangeCrosshairValue = 0.0;
        this.rangeCrosshairStroke = XYPlot.DEFAULT_CROSSHAIR_STROKE;
        this.rangeCrosshairPaint = XYPlot.DEFAULT_CROSSHAIR_PAINT;
        this.shadowGenerator = null;
    }
    @Override
    public String getPlotType() {
        return XYPlot.localizationResources.getString ( "XY_Plot" );
    }
    @Override
    public PlotOrientation getOrientation() {
        return this.orientation;
    }
    public void setOrientation ( final PlotOrientation orientation ) {
        ParamChecks.nullNotPermitted ( orientation, "orientation" );
        if ( orientation != this.orientation ) {
            this.orientation = orientation;
            this.fireChangeEvent();
        }
    }
    public RectangleInsets getAxisOffset() {
        return this.axisOffset;
    }
    public void setAxisOffset ( final RectangleInsets offset ) {
        ParamChecks.nullNotPermitted ( offset, "offset" );
        this.axisOffset = offset;
        this.fireChangeEvent();
    }
    public ValueAxis getDomainAxis() {
        return this.getDomainAxis ( 0 );
    }
    public ValueAxis getDomainAxis ( final int index ) {
        ValueAxis result = this.domainAxes.get ( index );
        if ( result == null ) {
            final Plot parent = this.getParent();
            if ( parent instanceof XYPlot ) {
                final XYPlot xy = ( XYPlot ) parent;
                result = xy.getDomainAxis ( index );
            }
        }
        return result;
    }
    public void setDomainAxis ( final ValueAxis axis ) {
        this.setDomainAxis ( 0, axis );
    }
    public void setDomainAxis ( final int index, final ValueAxis axis ) {
        this.setDomainAxis ( index, axis, true );
    }
    public void setDomainAxis ( final int index, final ValueAxis axis, final boolean notify ) {
        final ValueAxis existing = this.getDomainAxis ( index );
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
    public void setDomainAxes ( final ValueAxis[] axes ) {
        for ( int i = 0; i < axes.length; ++i ) {
            this.setDomainAxis ( i, axes[i], false );
        }
        this.fireChangeEvent();
    }
    public AxisLocation getDomainAxisLocation() {
        return this.domainAxisLocations.get ( 0 );
    }
    public void setDomainAxisLocation ( final AxisLocation location ) {
        this.setDomainAxisLocation ( 0, location, true );
    }
    public void setDomainAxisLocation ( final AxisLocation location, final boolean notify ) {
        this.setDomainAxisLocation ( 0, location, notify );
    }
    public RectangleEdge getDomainAxisEdge() {
        return Plot.resolveDomainAxisLocation ( this.getDomainAxisLocation(), this.orientation );
    }
    public int getDomainAxisCount() {
        return this.domainAxes.size();
    }
    public void clearDomainAxes() {
        for ( final ValueAxis axis : this.domainAxes.values() ) {
            if ( axis != null ) {
                axis.removeChangeListener ( this );
            }
        }
        this.domainAxes.clear();
        this.fireChangeEvent();
    }
    public void configureDomainAxes() {
        for ( final ValueAxis axis : this.domainAxes.values() ) {
            if ( axis != null ) {
                axis.configure();
            }
        }
    }
    public AxisLocation getDomainAxisLocation ( final int index ) {
        AxisLocation result = this.domainAxisLocations.get ( index );
        if ( result == null ) {
            result = AxisLocation.getOpposite ( this.getDomainAxisLocation() );
        }
        return result;
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
    public RectangleEdge getDomainAxisEdge ( final int index ) {
        final AxisLocation location = this.getDomainAxisLocation ( index );
        return Plot.resolveDomainAxisLocation ( location, this.orientation );
    }
    public ValueAxis getRangeAxis() {
        return this.getRangeAxis ( 0 );
    }
    public void setRangeAxis ( final ValueAxis axis ) {
        if ( axis != null ) {
            axis.setPlot ( this );
        }
        final ValueAxis existing = this.getRangeAxis();
        if ( existing != null ) {
            existing.removeChangeListener ( this );
        }
        this.rangeAxes.put ( 0, axis );
        if ( axis != null ) {
            axis.configure();
            axis.addChangeListener ( this );
        }
        this.fireChangeEvent();
    }
    public AxisLocation getRangeAxisLocation() {
        return this.rangeAxisLocations.get ( 0 );
    }
    public void setRangeAxisLocation ( final AxisLocation location ) {
        this.setRangeAxisLocation ( 0, location, true );
    }
    public void setRangeAxisLocation ( final AxisLocation location, final boolean notify ) {
        this.setRangeAxisLocation ( 0, location, notify );
    }
    public RectangleEdge getRangeAxisEdge() {
        return Plot.resolveRangeAxisLocation ( this.getRangeAxisLocation(), this.orientation );
    }
    public ValueAxis getRangeAxis ( final int index ) {
        ValueAxis result = this.rangeAxes.get ( index );
        if ( result == null ) {
            final Plot parent = this.getParent();
            if ( parent instanceof XYPlot ) {
                final XYPlot xy = ( XYPlot ) parent;
                result = xy.getRangeAxis ( index );
            }
        }
        return result;
    }
    public void setRangeAxis ( final int index, final ValueAxis axis ) {
        this.setRangeAxis ( index, axis, true );
    }
    public void setRangeAxis ( final int index, final ValueAxis axis, final boolean notify ) {
        final ValueAxis existing = this.getRangeAxis ( index );
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
    public int getRangeAxisCount() {
        return this.rangeAxes.size();
    }
    public void clearRangeAxes() {
        for ( final ValueAxis axis : this.rangeAxes.values() ) {
            if ( axis != null ) {
                axis.removeChangeListener ( this );
            }
        }
        this.rangeAxes.clear();
        this.fireChangeEvent();
    }
    public void configureRangeAxes() {
        for ( final ValueAxis axis : this.rangeAxes.values() ) {
            if ( axis != null ) {
                axis.configure();
            }
        }
    }
    public AxisLocation getRangeAxisLocation ( final int index ) {
        AxisLocation result = this.rangeAxisLocations.get ( index );
        if ( result == null ) {
            result = AxisLocation.getOpposite ( this.getRangeAxisLocation() );
        }
        return result;
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
    public RectangleEdge getRangeAxisEdge ( final int index ) {
        final AxisLocation location = this.getRangeAxisLocation ( index );
        return Plot.resolveRangeAxisLocation ( location, this.orientation );
    }
    public XYDataset getDataset() {
        return this.getDataset ( 0 );
    }
    public XYDataset getDataset ( final int index ) {
        return this.datasets.get ( index );
    }
    public void setDataset ( final XYDataset dataset ) {
        this.setDataset ( 0, dataset );
    }
    public void setDataset ( final int index, final XYDataset dataset ) {
        final XYDataset existing = this.getDataset ( index );
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
    public int indexOf ( final XYDataset dataset ) {
        for ( final Map.Entry<Integer, XYDataset> entry : this.datasets.entrySet() ) {
            if ( dataset == entry.getValue() ) {
                return entry.getKey();
            }
        }
        return -1;
    }
    public void mapDatasetToDomainAxis ( final int index, final int axisIndex ) {
        final List axisIndices = new ArrayList ( 1 );
        axisIndices.add ( new Integer ( axisIndex ) );
        this.mapDatasetToDomainAxes ( index, axisIndices );
    }
    public void mapDatasetToDomainAxes ( final int index, final List axisIndices ) {
        ParamChecks.requireNonNegative ( index, "index" );
        this.checkAxisIndices ( axisIndices );
        final Integer key = new Integer ( index );
        this.datasetToDomainAxesMap.put ( key, new ArrayList<Integer> ( axisIndices ) );
        this.datasetChanged ( new DatasetChangeEvent ( this, this.getDataset ( index ) ) );
    }
    public void mapDatasetToRangeAxis ( final int index, final int axisIndex ) {
        final List axisIndices = new ArrayList ( 1 );
        axisIndices.add ( new Integer ( axisIndex ) );
        this.mapDatasetToRangeAxes ( index, axisIndices );
    }
    public void mapDatasetToRangeAxes ( final int index, final List axisIndices ) {
        ParamChecks.requireNonNegative ( index, "index" );
        this.checkAxisIndices ( axisIndices );
        final Integer key = new Integer ( index );
        this.datasetToRangeAxesMap.put ( key, new ArrayList<Integer> ( axisIndices ) );
        this.datasetChanged ( new DatasetChangeEvent ( this, this.getDataset ( index ) ) );
    }
    private void checkAxisIndices ( final List<Integer> indices ) {
        if ( indices == null ) {
            return;
        }
        final int count = indices.size();
        if ( count == 0 ) {
            throw new IllegalArgumentException ( "Empty list not permitted." );
        }
        final Set<Integer> set = new HashSet<Integer>();
        for ( final Integer item : indices ) {
            if ( set.contains ( item ) ) {
                throw new IllegalArgumentException ( "Indices must be unique." );
            }
            set.add ( item );
        }
    }
    public int getRendererCount() {
        return this.renderers.size();
    }
    public XYItemRenderer getRenderer() {
        return this.getRenderer ( 0 );
    }
    public XYItemRenderer getRenderer ( final int index ) {
        return this.renderers.get ( index );
    }
    public void setRenderer ( final XYItemRenderer renderer ) {
        this.setRenderer ( 0, renderer );
    }
    public void setRenderer ( final int index, final XYItemRenderer renderer ) {
        this.setRenderer ( index, renderer, true );
    }
    public void setRenderer ( final int index, final XYItemRenderer renderer, final boolean notify ) {
        final XYItemRenderer existing = this.getRenderer ( index );
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
    public void setRenderers ( final XYItemRenderer[] renderers ) {
        for ( int i = 0; i < renderers.length; ++i ) {
            this.setRenderer ( i, renderers[i], false );
        }
        this.fireChangeEvent();
    }
    public DatasetRenderingOrder getDatasetRenderingOrder() {
        return this.datasetRenderingOrder;
    }
    public void setDatasetRenderingOrder ( final DatasetRenderingOrder order ) {
        ParamChecks.nullNotPermitted ( order, "order" );
        this.datasetRenderingOrder = order;
        this.fireChangeEvent();
    }
    public SeriesRenderingOrder getSeriesRenderingOrder() {
        return this.seriesRenderingOrder;
    }
    public void setSeriesRenderingOrder ( final SeriesRenderingOrder order ) {
        ParamChecks.nullNotPermitted ( order, "order" );
        this.seriesRenderingOrder = order;
        this.fireChangeEvent();
    }
    public int getIndexOf ( final XYItemRenderer renderer ) {
        for ( final Map.Entry<Integer, XYItemRenderer> entry : this.renderers.entrySet() ) {
            if ( entry.getValue() == renderer ) {
                return entry.getKey();
            }
        }
        return -1;
    }
    public XYItemRenderer getRendererForDataset ( final XYDataset dataset ) {
        final int datasetIndex = this.indexOf ( dataset );
        if ( datasetIndex < 0 ) {
            return null;
        }
        XYItemRenderer result = this.renderers.get ( datasetIndex );
        if ( result == null ) {
            result = this.getRenderer();
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
    public boolean isDomainGridlinesVisible() {
        return this.domainGridlinesVisible;
    }
    public void setDomainGridlinesVisible ( final boolean visible ) {
        if ( this.domainGridlinesVisible != visible ) {
            this.domainGridlinesVisible = visible;
            this.fireChangeEvent();
        }
    }
    public boolean isDomainMinorGridlinesVisible() {
        return this.domainMinorGridlinesVisible;
    }
    public void setDomainMinorGridlinesVisible ( final boolean visible ) {
        if ( this.domainMinorGridlinesVisible != visible ) {
            this.domainMinorGridlinesVisible = visible;
            this.fireChangeEvent();
        }
    }
    public Stroke getDomainGridlineStroke() {
        return this.domainGridlineStroke;
    }
    public void setDomainGridlineStroke ( final Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.domainGridlineStroke = stroke;
        this.fireChangeEvent();
    }
    public Stroke getDomainMinorGridlineStroke() {
        return this.domainMinorGridlineStroke;
    }
    public void setDomainMinorGridlineStroke ( final Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.domainMinorGridlineStroke = stroke;
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
    public Paint getDomainMinorGridlinePaint() {
        return this.domainMinorGridlinePaint;
    }
    public void setDomainMinorGridlinePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.domainMinorGridlinePaint = paint;
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
    public boolean isDomainZeroBaselineVisible() {
        return this.domainZeroBaselineVisible;
    }
    public void setDomainZeroBaselineVisible ( final boolean visible ) {
        this.domainZeroBaselineVisible = visible;
        this.fireChangeEvent();
    }
    public Stroke getDomainZeroBaselineStroke() {
        return this.domainZeroBaselineStroke;
    }
    public void setDomainZeroBaselineStroke ( final Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.domainZeroBaselineStroke = stroke;
        this.fireChangeEvent();
    }
    public Paint getDomainZeroBaselinePaint() {
        return this.domainZeroBaselinePaint;
    }
    public void setDomainZeroBaselinePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.domainZeroBaselinePaint = paint;
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
    public Paint getDomainTickBandPaint() {
        return this.domainTickBandPaint;
    }
    public void setDomainTickBandPaint ( final Paint paint ) {
        this.domainTickBandPaint = paint;
        this.fireChangeEvent();
    }
    public Paint getRangeTickBandPaint() {
        return this.rangeTickBandPaint;
    }
    public void setRangeTickBandPaint ( final Paint paint ) {
        this.rangeTickBandPaint = paint;
        this.fireChangeEvent();
    }
    public Point2D getQuadrantOrigin() {
        return this.quadrantOrigin;
    }
    public void setQuadrantOrigin ( final Point2D origin ) {
        ParamChecks.nullNotPermitted ( origin, "origin" );
        this.quadrantOrigin = origin;
        this.fireChangeEvent();
    }
    public Paint getQuadrantPaint ( final int index ) {
        if ( index < 0 || index > 3 ) {
            throw new IllegalArgumentException ( "The index value (" + index + ") should be in the range 0 to 3." );
        }
        return this.quadrantPaint[index];
    }
    public void setQuadrantPaint ( final int index, final Paint paint ) {
        if ( index < 0 || index > 3 ) {
            throw new IllegalArgumentException ( "The index value (" + index + ") should be in the range 0 to 3." );
        }
        this.quadrantPaint[index] = paint;
        this.fireChangeEvent();
    }
    public void addDomainMarker ( final Marker marker ) {
        this.addDomainMarker ( marker, Layer.FOREGROUND );
    }
    public void addDomainMarker ( final Marker marker, final Layer layer ) {
        this.addDomainMarker ( 0, marker, layer );
    }
    public void clearDomainMarkers() {
        if ( this.backgroundDomainMarkers != null ) {
            final Set<Integer> keys = this.backgroundDomainMarkers.keySet();
            for ( final Integer key : keys ) {
                this.clearDomainMarkers ( key );
            }
            this.backgroundDomainMarkers.clear();
        }
        if ( this.foregroundDomainMarkers != null ) {
            final Set<Integer> keys = this.foregroundDomainMarkers.keySet();
            for ( final Integer key : keys ) {
                this.clearDomainMarkers ( key );
            }
            this.foregroundDomainMarkers.clear();
        }
        this.fireChangeEvent();
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
        if ( this.foregroundRangeMarkers != null ) {
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
    public void addDomainMarker ( final int index, final Marker marker, final Layer layer ) {
        this.addDomainMarker ( index, marker, layer, true );
    }
    public void addDomainMarker ( final int index, final Marker marker, final Layer layer, final boolean notify ) {
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
    public void clearRangeMarkers() {
        if ( this.backgroundRangeMarkers != null ) {
            final Set<Integer> keys = this.backgroundRangeMarkers.keySet();
            for ( final Integer key : keys ) {
                this.clearRangeMarkers ( key );
            }
            this.backgroundRangeMarkers.clear();
        }
        if ( this.foregroundRangeMarkers != null ) {
            final Set<Integer> keys = this.foregroundRangeMarkers.keySet();
            for ( final Integer key : keys ) {
                this.clearRangeMarkers ( key );
            }
            this.foregroundRangeMarkers.clear();
        }
        this.fireChangeEvent();
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
        ParamChecks.nullNotPermitted ( layer, "layer" );
        List markers;
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
    public void addAnnotation ( final XYAnnotation annotation ) {
        this.addAnnotation ( annotation, true );
    }
    public void addAnnotation ( final XYAnnotation annotation, final boolean notify ) {
        ParamChecks.nullNotPermitted ( annotation, "annotation" );
        this.annotations.add ( annotation );
        annotation.addChangeListener ( this );
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public boolean removeAnnotation ( final XYAnnotation annotation ) {
        return this.removeAnnotation ( annotation, true );
    }
    public boolean removeAnnotation ( final XYAnnotation annotation, final boolean notify ) {
        ParamChecks.nullNotPermitted ( annotation, "annotation" );
        final boolean removed = this.annotations.remove ( annotation );
        annotation.removeChangeListener ( this );
        if ( removed && notify ) {
            this.fireChangeEvent();
        }
        return removed;
    }
    public List getAnnotations() {
        return new ArrayList ( this.annotations );
    }
    public void clearAnnotations() {
        for ( final XYAnnotation annotation : this.annotations ) {
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
    protected AxisSpace calculateAxisSpace ( final Graphics2D g2, final Rectangle2D plotArea ) {
        AxisSpace space = new AxisSpace();
        space = this.calculateRangeAxisSpace ( g2, plotArea, space );
        final Rectangle2D revPlotArea = space.shrink ( plotArea, null );
        space = this.calculateDomainAxisSpace ( g2, revPlotArea, space );
        return space;
    }
    protected AxisSpace calculateDomainAxisSpace ( final Graphics2D g2, final Rectangle2D plotArea, AxisSpace space ) {
        if ( space == null ) {
            space = new AxisSpace();
        }
        if ( this.fixedDomainAxisSpace != null ) {
            if ( this.orientation == PlotOrientation.HORIZONTAL ) {
                space.ensureAtLeast ( this.fixedDomainAxisSpace.getLeft(), RectangleEdge.LEFT );
                space.ensureAtLeast ( this.fixedDomainAxisSpace.getRight(), RectangleEdge.RIGHT );
            } else if ( this.orientation == PlotOrientation.VERTICAL ) {
                space.ensureAtLeast ( this.fixedDomainAxisSpace.getTop(), RectangleEdge.TOP );
                space.ensureAtLeast ( this.fixedDomainAxisSpace.getBottom(), RectangleEdge.BOTTOM );
            }
        } else {
            for ( final ValueAxis axis : this.domainAxes.values() ) {
                if ( axis != null ) {
                    final RectangleEdge edge = this.getDomainAxisEdge ( this.findDomainAxisIndex ( axis ) );
                    space = axis.reserveSpace ( g2, this, plotArea, edge, space );
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
            if ( this.orientation == PlotOrientation.HORIZONTAL ) {
                space.ensureAtLeast ( this.fixedRangeAxisSpace.getTop(), RectangleEdge.TOP );
                space.ensureAtLeast ( this.fixedRangeAxisSpace.getBottom(), RectangleEdge.BOTTOM );
            } else if ( this.orientation == PlotOrientation.VERTICAL ) {
                space.ensureAtLeast ( this.fixedRangeAxisSpace.getLeft(), RectangleEdge.LEFT );
                space.ensureAtLeast ( this.fixedRangeAxisSpace.getRight(), RectangleEdge.RIGHT );
            }
        } else {
            for ( final ValueAxis axis : this.rangeAxes.values() ) {
                if ( axis != null ) {
                    final RectangleEdge edge = this.getRangeAxisEdge ( this.findRangeAxisIndex ( axis ) );
                    space = axis.reserveSpace ( g2, this, plotArea, edge, space );
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
    @Override
    public void draw ( Graphics2D g2, final Rectangle2D area, Point2D anchor, final PlotState parentState, final PlotRenderingInfo info ) {
        final boolean b1 = area.getWidth() <= 10.0;
        final boolean b2 = area.getHeight() <= 10.0;
        if ( b1 || b2 ) {
            return;
        }
        if ( info != null ) {
            info.setPlotArea ( area );
        }
        final RectangleInsets insets = this.getInsets();
        insets.trim ( area );
        final AxisSpace space = this.calculateAxisSpace ( g2, area );
        Rectangle2D dataArea = space.shrink ( area, null );
        this.axisOffset.trim ( dataArea );
        dataArea = this.integerise ( dataArea );
        if ( dataArea.isEmpty() ) {
            return;
        }
        this.createAndAddEntity ( ( Rectangle2D ) dataArea.clone(), info, null, null );
        if ( info != null ) {
            info.setDataArea ( dataArea );
        }
        this.drawBackground ( g2, dataArea );
        final Map axisStateMap = this.drawAxes ( g2, area, dataArea, info );
        final PlotOrientation orient = this.getOrientation();
        if ( anchor != null && !dataArea.contains ( anchor ) ) {
            anchor = null;
        }
        final CrosshairState crosshairState = new CrosshairState();
        crosshairState.setCrosshairDistance ( Double.POSITIVE_INFINITY );
        crosshairState.setAnchor ( anchor );
        crosshairState.setAnchorX ( Double.NaN );
        crosshairState.setAnchorY ( Double.NaN );
        if ( anchor != null ) {
            final ValueAxis domainAxis = this.getDomainAxis();
            if ( domainAxis != null ) {
                double x;
                if ( orient == PlotOrientation.VERTICAL ) {
                    x = domainAxis.java2DToValue ( anchor.getX(), dataArea, this.getDomainAxisEdge() );
                } else {
                    x = domainAxis.java2DToValue ( anchor.getY(), dataArea, this.getDomainAxisEdge() );
                }
                crosshairState.setAnchorX ( x );
            }
            final ValueAxis rangeAxis = this.getRangeAxis();
            if ( rangeAxis != null ) {
                double y;
                if ( orient == PlotOrientation.VERTICAL ) {
                    y = rangeAxis.java2DToValue ( anchor.getY(), dataArea, this.getRangeAxisEdge() );
                } else {
                    y = rangeAxis.java2DToValue ( anchor.getX(), dataArea, this.getRangeAxisEdge() );
                }
                crosshairState.setAnchorY ( y );
            }
        }
        crosshairState.setCrosshairX ( this.getDomainCrosshairValue() );
        crosshairState.setCrosshairY ( this.getRangeCrosshairValue() );
        final Shape originalClip = g2.getClip();
        final Composite originalComposite = g2.getComposite();
        g2.clip ( dataArea );
        g2.setComposite ( AlphaComposite.getInstance ( 3, this.getForegroundAlpha() ) );
        AxisState domainAxisState = axisStateMap.get ( this.getDomainAxis() );
        if ( domainAxisState == null && parentState != null ) {
            domainAxisState = parentState.getSharedAxisStates().get ( this.getDomainAxis() );
        }
        AxisState rangeAxisState = axisStateMap.get ( this.getRangeAxis() );
        if ( rangeAxisState == null && parentState != null ) {
            rangeAxisState = parentState.getSharedAxisStates().get ( this.getRangeAxis() );
        }
        if ( domainAxisState != null ) {
            this.drawDomainTickBands ( g2, dataArea, domainAxisState.getTicks() );
        }
        if ( rangeAxisState != null ) {
            this.drawRangeTickBands ( g2, dataArea, rangeAxisState.getTicks() );
        }
        if ( domainAxisState != null ) {
            this.drawDomainGridlines ( g2, dataArea, domainAxisState.getTicks() );
            this.drawZeroDomainBaseline ( g2, dataArea );
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
        for ( final XYDataset dataset : this.datasets.values() ) {
            final int datasetIndex = this.indexOf ( dataset );
            this.drawDomainMarkers ( g2, dataArea, datasetIndex, Layer.BACKGROUND );
        }
        for ( final XYDataset dataset : this.datasets.values() ) {
            final int datasetIndex = this.indexOf ( dataset );
            this.drawRangeMarkers ( g2, dataArea, datasetIndex, Layer.BACKGROUND );
        }
        boolean foundData = false;
        final DatasetRenderingOrder order = this.getDatasetRenderingOrder();
        final List<Integer> rendererIndices = this.getRendererIndices ( order );
        final List<Integer> datasetIndices = this.getDatasetIndices ( order );
        for ( final int i : rendererIndices ) {
            final XYItemRenderer renderer = this.getRenderer ( i );
            if ( renderer != null ) {
                final ValueAxis domainAxis2 = this.getDomainAxisForDataset ( i );
                final ValueAxis rangeAxis2 = this.getRangeAxisForDataset ( i );
                renderer.drawAnnotations ( g2, dataArea, domainAxis2, rangeAxis2, Layer.BACKGROUND, info );
            }
        }
        for ( final int datasetIndex2 : datasetIndices ) {
            final XYDataset dataset2 = this.getDataset ( datasetIndex2 );
            foundData = ( this.render ( g2, dataArea, datasetIndex2, info, crosshairState ) || foundData );
        }
        for ( final int i : rendererIndices ) {
            final XYItemRenderer renderer = this.getRenderer ( i );
            if ( renderer != null ) {
                final ValueAxis domainAxis2 = this.getDomainAxisForDataset ( i );
                final ValueAxis rangeAxis2 = this.getRangeAxisForDataset ( i );
                renderer.drawAnnotations ( g2, dataArea, domainAxis2, rangeAxis2, Layer.FOREGROUND, info );
            }
        }
        final int datasetIndex3 = crosshairState.getDatasetIndex();
        final ValueAxis xAxis = this.getDomainAxisForDataset ( datasetIndex3 );
        final RectangleEdge xAxisEdge = this.getDomainAxisEdge ( this.getDomainAxisIndex ( xAxis ) );
        if ( !this.domainCrosshairLockedOnData && anchor != null ) {
            double xx;
            if ( orient == PlotOrientation.VERTICAL ) {
                xx = xAxis.java2DToValue ( anchor.getX(), dataArea, xAxisEdge );
            } else {
                xx = xAxis.java2DToValue ( anchor.getY(), dataArea, xAxisEdge );
            }
            crosshairState.setCrosshairX ( xx );
        }
        this.setDomainCrosshairValue ( crosshairState.getCrosshairX(), false );
        if ( this.isDomainCrosshairVisible() ) {
            final double x2 = this.getDomainCrosshairValue();
            final Paint paint = this.getDomainCrosshairPaint();
            final Stroke stroke = this.getDomainCrosshairStroke();
            this.drawDomainCrosshair ( g2, dataArea, orient, x2, xAxis, stroke, paint );
        }
        final ValueAxis yAxis = this.getRangeAxisForDataset ( datasetIndex3 );
        final RectangleEdge yAxisEdge = this.getRangeAxisEdge ( this.getRangeAxisIndex ( yAxis ) );
        if ( !this.rangeCrosshairLockedOnData && anchor != null ) {
            double yy;
            if ( orient == PlotOrientation.VERTICAL ) {
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
            this.drawRangeCrosshair ( g2, dataArea, orient, y2, yAxis, stroke2, paint2 );
        }
        if ( !foundData ) {
            this.drawNoDataMessage ( g2, dataArea );
        }
        for ( final int j : rendererIndices ) {
            this.drawDomainMarkers ( g2, dataArea, j, Layer.FOREGROUND );
        }
        for ( final int j : rendererIndices ) {
            this.drawRangeMarkers ( g2, dataArea, j, Layer.FOREGROUND );
        }
        this.drawAnnotations ( g2, dataArea, info );
        if ( this.shadowGenerator != null && !suppressShadow ) {
            final BufferedImage shadowImage = this.shadowGenerator.createDropShadow ( dataImage );
            g2 = savedG2;
            g2.drawImage ( shadowImage, ( int ) dataArea.getX() + this.shadowGenerator.calculateOffsetX(), ( int ) dataArea.getY() + this.shadowGenerator.calculateOffsetY(), null );
            g2.drawImage ( dataImage, ( int ) dataArea.getX(), ( int ) dataArea.getY(), null );
        }
        g2.setClip ( originalClip );
        g2.setComposite ( originalComposite );
        this.drawOutline ( g2, dataArea );
    }
    private List<Integer> getDatasetIndices ( final DatasetRenderingOrder order ) {
        final List<Integer> result = new ArrayList<Integer>();
        for ( final Map.Entry<Integer, XYDataset> entry : this.datasets.entrySet() ) {
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
        for ( final Map.Entry<Integer, XYItemRenderer> entry : this.renderers.entrySet() ) {
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
        this.drawQuadrants ( g2, area );
        this.drawBackgroundImage ( g2, area );
    }
    protected void drawQuadrants ( final Graphics2D g2, final Rectangle2D area ) {
        boolean somethingToDraw = false;
        final ValueAxis xAxis = this.getDomainAxis();
        if ( xAxis == null ) {
            return;
        }
        final double x = xAxis.getRange().constrain ( this.quadrantOrigin.getX() );
        final double xx = xAxis.valueToJava2D ( x, area, this.getDomainAxisEdge() );
        final ValueAxis yAxis = this.getRangeAxis();
        if ( yAxis == null ) {
            return;
        }
        final double y = yAxis.getRange().constrain ( this.quadrantOrigin.getY() );
        final double yy = yAxis.valueToJava2D ( y, area, this.getRangeAxisEdge() );
        final double xmin = xAxis.getLowerBound();
        final double xxmin = xAxis.valueToJava2D ( xmin, area, this.getDomainAxisEdge() );
        final double xmax = xAxis.getUpperBound();
        final double xxmax = xAxis.valueToJava2D ( xmax, area, this.getDomainAxisEdge() );
        final double ymin = yAxis.getLowerBound();
        final double yymin = yAxis.valueToJava2D ( ymin, area, this.getRangeAxisEdge() );
        final double ymax = yAxis.getUpperBound();
        final double yymax = yAxis.valueToJava2D ( ymax, area, this.getRangeAxisEdge() );
        final Rectangle2D[] r = { null, null, null, null };
        if ( this.quadrantPaint[0] != null && x > xmin && y < ymax ) {
            if ( this.orientation == PlotOrientation.HORIZONTAL ) {
                r[0] = new Rectangle2D.Double ( Math.min ( yymax, yy ), Math.min ( xxmin, xx ), Math.abs ( yy - yymax ), Math.abs ( xx - xxmin ) );
            } else {
                r[0] = new Rectangle2D.Double ( Math.min ( xxmin, xx ), Math.min ( yymax, yy ), Math.abs ( xx - xxmin ), Math.abs ( yy - yymax ) );
            }
            somethingToDraw = true;
        }
        if ( this.quadrantPaint[1] != null && x < xmax && y < ymax ) {
            if ( this.orientation == PlotOrientation.HORIZONTAL ) {
                r[1] = new Rectangle2D.Double ( Math.min ( yymax, yy ), Math.min ( xxmax, xx ), Math.abs ( yy - yymax ), Math.abs ( xx - xxmax ) );
            } else {
                r[1] = new Rectangle2D.Double ( Math.min ( xx, xxmax ), Math.min ( yymax, yy ), Math.abs ( xx - xxmax ), Math.abs ( yy - yymax ) );
            }
            somethingToDraw = true;
        }
        if ( this.quadrantPaint[2] != null && x > xmin && y > ymin ) {
            if ( this.orientation == PlotOrientation.HORIZONTAL ) {
                r[2] = new Rectangle2D.Double ( Math.min ( yymin, yy ), Math.min ( xxmin, xx ), Math.abs ( yy - yymin ), Math.abs ( xx - xxmin ) );
            } else {
                r[2] = new Rectangle2D.Double ( Math.min ( xxmin, xx ), Math.min ( yymin, yy ), Math.abs ( xx - xxmin ), Math.abs ( yy - yymin ) );
            }
            somethingToDraw = true;
        }
        if ( this.quadrantPaint[3] != null && x < xmax && y > ymin ) {
            if ( this.orientation == PlotOrientation.HORIZONTAL ) {
                r[3] = new Rectangle2D.Double ( Math.min ( yymin, yy ), Math.min ( xxmax, xx ), Math.abs ( yy - yymin ), Math.abs ( xx - xxmax ) );
            } else {
                r[3] = new Rectangle2D.Double ( Math.min ( xx, xxmax ), Math.min ( yymin, yy ), Math.abs ( xx - xxmax ), Math.abs ( yy - yymin ) );
            }
            somethingToDraw = true;
        }
        if ( somethingToDraw ) {
            final Composite originalComposite = g2.getComposite();
            g2.setComposite ( AlphaComposite.getInstance ( 3, this.getBackgroundAlpha() ) );
            for ( int i = 0; i < 4; ++i ) {
                if ( this.quadrantPaint[i] != null && r[i] != null ) {
                    g2.setPaint ( this.quadrantPaint[i] );
                    g2.fill ( r[i] );
                }
            }
            g2.setComposite ( originalComposite );
        }
    }
    public void drawDomainTickBands ( final Graphics2D g2, final Rectangle2D dataArea, final List ticks ) {
        final Paint bandPaint = this.getDomainTickBandPaint();
        if ( bandPaint != null ) {
            boolean fillBand = false;
            final ValueAxis xAxis = this.getDomainAxis();
            double previous = xAxis.getLowerBound();
            for ( final ValueTick tick : ticks ) {
                final double current = tick.getValue();
                if ( fillBand ) {
                    this.getRenderer().fillDomainGridBand ( g2, this, xAxis, dataArea, previous, current );
                }
                previous = current;
                fillBand = !fillBand;
            }
            final double end = xAxis.getUpperBound();
            if ( fillBand ) {
                this.getRenderer().fillDomainGridBand ( g2, this, xAxis, dataArea, previous, end );
            }
        }
    }
    public void drawRangeTickBands ( final Graphics2D g2, final Rectangle2D dataArea, final List ticks ) {
        final Paint bandPaint = this.getRangeTickBandPaint();
        if ( bandPaint != null ) {
            boolean fillBand = false;
            final ValueAxis axis = this.getRangeAxis();
            double previous = axis.getLowerBound();
            for ( final ValueTick tick : ticks ) {
                final double current = tick.getValue();
                if ( fillBand ) {
                    this.getRenderer().fillRangeGridBand ( g2, this, axis, dataArea, previous, current );
                }
                previous = current;
                fillBand = !fillBand;
            }
            final double end = axis.getUpperBound();
            if ( fillBand ) {
                this.getRenderer().fillRangeGridBand ( g2, this, axis, dataArea, previous, end );
            }
        }
    }
    protected Map<Axis, AxisState> drawAxes ( final Graphics2D g2, final Rectangle2D plotArea, final Rectangle2D dataArea, final PlotRenderingInfo plotState ) {
        final AxisCollection axisCollection = new AxisCollection();
        for ( final ValueAxis axis : this.domainAxes.values() ) {
            if ( axis != null ) {
                final int axisIndex = this.findDomainAxisIndex ( axis );
                axisCollection.add ( axis, this.getDomainAxisEdge ( axisIndex ) );
            }
        }
        for ( final ValueAxis axis : this.rangeAxes.values() ) {
            if ( axis != null ) {
                final int axisIndex = this.findRangeAxisIndex ( axis );
                axisCollection.add ( axis, this.getRangeAxisEdge ( axisIndex ) );
            }
        }
        final Map axisStateMap = new HashMap();
        double cursor = dataArea.getMinY() - this.axisOffset.calculateTopOutset ( dataArea.getHeight() );
        for ( final ValueAxis axis2 : axisCollection.getAxesAtTop() ) {
            final AxisState info = axis2.draw ( g2, cursor, plotArea, dataArea, RectangleEdge.TOP, plotState );
            cursor = info.getCursor();
            axisStateMap.put ( axis2, info );
        }
        cursor = dataArea.getMaxY() + this.axisOffset.calculateBottomOutset ( dataArea.getHeight() );
        for ( final ValueAxis axis2 : axisCollection.getAxesAtBottom() ) {
            final AxisState info = axis2.draw ( g2, cursor, plotArea, dataArea, RectangleEdge.BOTTOM, plotState );
            cursor = info.getCursor();
            axisStateMap.put ( axis2, info );
        }
        cursor = dataArea.getMinX() - this.axisOffset.calculateLeftOutset ( dataArea.getWidth() );
        for ( final ValueAxis axis2 : axisCollection.getAxesAtLeft() ) {
            final AxisState info = axis2.draw ( g2, cursor, plotArea, dataArea, RectangleEdge.LEFT, plotState );
            cursor = info.getCursor();
            axisStateMap.put ( axis2, info );
        }
        cursor = dataArea.getMaxX() + this.axisOffset.calculateRightOutset ( dataArea.getWidth() );
        for ( final ValueAxis axis2 : axisCollection.getAxesAtRight() ) {
            final AxisState info = axis2.draw ( g2, cursor, plotArea, dataArea, RectangleEdge.RIGHT, plotState );
            cursor = info.getCursor();
            axisStateMap.put ( axis2, info );
        }
        return ( Map<Axis, AxisState> ) axisStateMap;
    }
    public boolean render ( final Graphics2D g2, final Rectangle2D dataArea, final int index, final PlotRenderingInfo info, final CrosshairState crosshairState ) {
        boolean foundData = false;
        final XYDataset dataset = this.getDataset ( index );
        if ( !DatasetUtilities.isEmptyOrNull ( dataset ) ) {
            foundData = true;
            final ValueAxis xAxis = this.getDomainAxisForDataset ( index );
            final ValueAxis yAxis = this.getRangeAxisForDataset ( index );
            if ( xAxis == null || yAxis == null ) {
                return foundData;
            }
            XYItemRenderer renderer = this.getRenderer ( index );
            if ( renderer == null ) {
                renderer = this.getRenderer();
                if ( renderer == null ) {
                    return foundData;
                }
            }
            final XYItemRendererState state = renderer.initialise ( g2, dataArea, this, dataset, info );
            final int passCount = renderer.getPassCount();
            final SeriesRenderingOrder seriesOrder = this.getSeriesRenderingOrder();
            if ( seriesOrder == SeriesRenderingOrder.REVERSE ) {
                for ( int pass = 0; pass < passCount; ++pass ) {
                    final int seriesCount = dataset.getSeriesCount();
                    for ( int series = seriesCount - 1; series >= 0; --series ) {
                        int firstItem = 0;
                        int lastItem = dataset.getItemCount ( series ) - 1;
                        if ( lastItem != -1 ) {
                            if ( state.getProcessVisibleItemsOnly() ) {
                                final int[] itemBounds = RendererUtilities.findLiveItems ( dataset, series, xAxis.getLowerBound(), xAxis.getUpperBound() );
                                firstItem = Math.max ( itemBounds[0] - 1, 0 );
                                lastItem = Math.min ( itemBounds[1] + 1, lastItem );
                            }
                            state.startSeriesPass ( dataset, series, firstItem, lastItem, pass, passCount );
                            for ( int item = firstItem; item <= lastItem; ++item ) {
                                renderer.drawItem ( g2, state, dataArea, info, this, xAxis, yAxis, dataset, series, item, crosshairState, pass );
                            }
                            state.endSeriesPass ( dataset, series, firstItem, lastItem, pass, passCount );
                        }
                    }
                }
            } else {
                for ( int pass = 0; pass < passCount; ++pass ) {
                    for ( int seriesCount = dataset.getSeriesCount(), series = 0; series < seriesCount; ++series ) {
                        int firstItem = 0;
                        int lastItem = dataset.getItemCount ( series ) - 1;
                        if ( state.getProcessVisibleItemsOnly() ) {
                            final int[] itemBounds = RendererUtilities.findLiveItems ( dataset, series, xAxis.getLowerBound(), xAxis.getUpperBound() );
                            firstItem = Math.max ( itemBounds[0] - 1, 0 );
                            lastItem = Math.min ( itemBounds[1] + 1, lastItem );
                        }
                        state.startSeriesPass ( dataset, series, firstItem, lastItem, pass, passCount );
                        for ( int item = firstItem; item <= lastItem; ++item ) {
                            renderer.drawItem ( g2, state, dataArea, info, this, xAxis, yAxis, dataset, series, item, crosshairState, pass );
                        }
                        state.endSeriesPass ( dataset, series, firstItem, lastItem, pass, passCount );
                    }
                }
            }
        }
        return foundData;
    }
    public ValueAxis getDomainAxisForDataset ( final int index ) {
        ParamChecks.requireNonNegative ( index, "index" );
        final List axisIndices = this.datasetToDomainAxesMap.get ( new Integer ( index ) );
        ValueAxis valueAxis;
        if ( axisIndices != null ) {
            final Integer axisIndex = axisIndices.get ( 0 );
            valueAxis = this.getDomainAxis ( axisIndex );
        } else {
            valueAxis = this.getDomainAxis ( 0 );
        }
        return valueAxis;
    }
    public ValueAxis getRangeAxisForDataset ( final int index ) {
        ParamChecks.requireNonNegative ( index, "index" );
        final List axisIndices = this.datasetToRangeAxesMap.get ( new Integer ( index ) );
        ValueAxis valueAxis;
        if ( axisIndices != null ) {
            final Integer axisIndex = axisIndices.get ( 0 );
            valueAxis = this.getRangeAxis ( axisIndex );
        } else {
            valueAxis = this.getRangeAxis ( 0 );
        }
        return valueAxis;
    }
    protected void drawDomainGridlines ( final Graphics2D g2, final Rectangle2D dataArea, final List ticks ) {
        if ( this.getRenderer() == null ) {
            return;
        }
        if ( this.isDomainGridlinesVisible() || this.isDomainMinorGridlinesVisible() ) {
            Stroke gridStroke = null;
            Paint gridPaint = null;
            final Iterator iterator = ticks.iterator();
            while ( iterator.hasNext() ) {
                boolean paintLine = false;
                final ValueTick tick = iterator.next();
                if ( tick.getTickType() == TickType.MINOR && this.isDomainMinorGridlinesVisible() ) {
                    gridStroke = this.getDomainMinorGridlineStroke();
                    gridPaint = this.getDomainMinorGridlinePaint();
                    paintLine = true;
                } else if ( tick.getTickType() == TickType.MAJOR && this.isDomainGridlinesVisible() ) {
                    gridStroke = this.getDomainGridlineStroke();
                    gridPaint = this.getDomainGridlinePaint();
                    paintLine = true;
                }
                final XYItemRenderer r = this.getRenderer();
                if ( r instanceof AbstractXYItemRenderer && paintLine ) {
                    ( ( AbstractXYItemRenderer ) r ).drawDomainLine ( g2, this, this.getDomainAxis(), dataArea, tick.getValue(), gridPaint, gridStroke );
                }
            }
        }
    }
    protected void drawRangeGridlines ( final Graphics2D g2, final Rectangle2D area, final List ticks ) {
        if ( this.getRenderer() == null ) {
            return;
        }
        if ( this.isRangeGridlinesVisible() || this.isRangeMinorGridlinesVisible() ) {
            Stroke gridStroke = null;
            Paint gridPaint = null;
            final ValueAxis axis = this.getRangeAxis();
            if ( axis != null ) {
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
                        this.getRenderer().drawRangeLine ( g2, this, this.getRangeAxis(), area, tick.getValue(), gridPaint, gridStroke );
                    }
                }
            }
        }
    }
    protected void drawZeroDomainBaseline ( final Graphics2D g2, final Rectangle2D area ) {
        if ( this.isDomainZeroBaselineVisible() ) {
            final XYItemRenderer r = this.getRenderer();
            if ( r instanceof AbstractXYItemRenderer ) {
                final AbstractXYItemRenderer renderer = ( AbstractXYItemRenderer ) r;
                renderer.drawDomainLine ( g2, this, this.getDomainAxis(), area, 0.0, this.domainZeroBaselinePaint, this.domainZeroBaselineStroke );
            }
        }
    }
    protected void drawZeroRangeBaseline ( final Graphics2D g2, final Rectangle2D area ) {
        if ( this.isRangeZeroBaselineVisible() ) {
            this.getRenderer().drawRangeLine ( g2, this, this.getRangeAxis(), area, 0.0, this.rangeZeroBaselinePaint, this.rangeZeroBaselineStroke );
        }
    }
    public void drawAnnotations ( final Graphics2D g2, final Rectangle2D dataArea, final PlotRenderingInfo info ) {
        for ( final XYAnnotation annotation : this.annotations ) {
            final ValueAxis xAxis = this.getDomainAxis();
            final ValueAxis yAxis = this.getRangeAxis();
            annotation.draw ( g2, this, dataArea, xAxis, yAxis, 0, info );
        }
    }
    protected void drawDomainMarkers ( final Graphics2D g2, final Rectangle2D dataArea, final int index, final Layer layer ) {
        final XYItemRenderer r = this.getRenderer ( index );
        if ( r == null ) {
            return;
        }
        if ( index >= this.getDatasetCount() ) {
            return;
        }
        final Collection markers = this.getDomainMarkers ( index, layer );
        final ValueAxis axis = this.getDomainAxisForDataset ( index );
        if ( markers != null && axis != null ) {
            for ( final Marker marker : markers ) {
                r.drawDomainMarker ( g2, this, axis, marker, dataArea );
            }
        }
    }
    protected void drawRangeMarkers ( final Graphics2D g2, final Rectangle2D dataArea, final int index, final Layer layer ) {
        final XYItemRenderer r = this.getRenderer ( index );
        if ( r == null ) {
            return;
        }
        if ( index >= this.getDatasetCount() ) {
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
    public Collection getDomainMarkers ( final Layer layer ) {
        return this.getDomainMarkers ( 0, layer );
    }
    public Collection getRangeMarkers ( final Layer layer ) {
        return this.getRangeMarkers ( 0, layer );
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
    protected void drawHorizontalLine ( final Graphics2D g2, final Rectangle2D dataArea, final double value, final Stroke stroke, final Paint paint ) {
        ValueAxis axis = this.getRangeAxis();
        if ( this.getOrientation() == PlotOrientation.HORIZONTAL ) {
            axis = this.getDomainAxis();
        }
        if ( axis.getRange().contains ( value ) ) {
            final double yy = axis.valueToJava2D ( value, dataArea, RectangleEdge.LEFT );
            final Line2D line = new Line2D.Double ( dataArea.getMinX(), yy, dataArea.getMaxX(), yy );
            g2.setStroke ( stroke );
            g2.setPaint ( paint );
            g2.draw ( line );
        }
    }
    protected void drawDomainCrosshair ( final Graphics2D g2, final Rectangle2D dataArea, final PlotOrientation orientation, final double value, final ValueAxis axis, final Stroke stroke, final Paint paint ) {
        if ( !axis.getRange().contains ( value ) ) {
            return;
        }
        Line2D line;
        if ( orientation == PlotOrientation.VERTICAL ) {
            final double xx = axis.valueToJava2D ( value, dataArea, RectangleEdge.BOTTOM );
            line = new Line2D.Double ( xx, dataArea.getMinY(), xx, dataArea.getMaxY() );
        } else {
            final double yy = axis.valueToJava2D ( value, dataArea, RectangleEdge.LEFT );
            line = new Line2D.Double ( dataArea.getMinX(), yy, dataArea.getMaxX(), yy );
        }
        final Object saved = g2.getRenderingHint ( RenderingHints.KEY_STROKE_CONTROL );
        g2.setRenderingHint ( RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE );
        g2.setStroke ( stroke );
        g2.setPaint ( paint );
        g2.draw ( line );
        g2.setRenderingHint ( RenderingHints.KEY_STROKE_CONTROL, saved );
    }
    protected void drawVerticalLine ( final Graphics2D g2, final Rectangle2D dataArea, final double value, final Stroke stroke, final Paint paint ) {
        ValueAxis axis = this.getDomainAxis();
        if ( this.getOrientation() == PlotOrientation.HORIZONTAL ) {
            axis = this.getRangeAxis();
        }
        if ( axis.getRange().contains ( value ) ) {
            final double xx = axis.valueToJava2D ( value, dataArea, RectangleEdge.BOTTOM );
            final Line2D line = new Line2D.Double ( xx, dataArea.getMinY(), xx, dataArea.getMaxY() );
            g2.setStroke ( stroke );
            g2.setPaint ( paint );
            g2.draw ( line );
        }
    }
    protected void drawRangeCrosshair ( final Graphics2D g2, final Rectangle2D dataArea, final PlotOrientation orientation, final double value, final ValueAxis axis, final Stroke stroke, final Paint paint ) {
        if ( !axis.getRange().contains ( value ) ) {
            return;
        }
        final Object saved = g2.getRenderingHint ( RenderingHints.KEY_STROKE_CONTROL );
        g2.setRenderingHint ( RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE );
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
        g2.setRenderingHint ( RenderingHints.KEY_STROKE_CONTROL, saved );
    }
    @Override
    public void handleClick ( final int x, final int y, final PlotRenderingInfo info ) {
        final Rectangle2D dataArea = info.getDataArea();
        if ( dataArea.contains ( x, y ) ) {
            final ValueAxis xaxis = this.getDomainAxis();
            if ( xaxis != null ) {
                final double hvalue = xaxis.java2DToValue ( x, info.getDataArea(), this.getDomainAxisEdge() );
                this.setDomainCrosshairValue ( hvalue );
            }
            final ValueAxis yaxis = this.getRangeAxis();
            if ( yaxis != null ) {
                final double vvalue = yaxis.java2DToValue ( y, info.getDataArea(), this.getRangeAxisEdge() );
                this.setRangeCrosshairValue ( vvalue );
            }
        }
    }
    private List<XYDataset> getDatasetsMappedToDomainAxis ( final Integer axisIndex ) {
        ParamChecks.nullNotPermitted ( axisIndex, "axisIndex" );
        final List<XYDataset> result = new ArrayList<XYDataset>();
        for ( final Map.Entry<Integer, XYDataset> entry : this.datasets.entrySet() ) {
            final int index = entry.getKey();
            final List<Integer> mappedAxes = this.datasetToDomainAxesMap.get ( index );
            if ( mappedAxes == null ) {
                if ( !axisIndex.equals ( XYPlot.ZERO ) ) {
                    continue;
                }
                result.add ( entry.getValue() );
            } else {
                if ( !mappedAxes.contains ( axisIndex ) ) {
                    continue;
                }
                result.add ( entry.getValue() );
            }
        }
        return result;
    }
    private List<XYDataset> getDatasetsMappedToRangeAxis ( final Integer axisIndex ) {
        ParamChecks.nullNotPermitted ( axisIndex, "axisIndex" );
        final List<XYDataset> result = new ArrayList<XYDataset>();
        for ( final Map.Entry<Integer, XYDataset> entry : this.datasets.entrySet() ) {
            final int index = entry.getKey();
            final List<Integer> mappedAxes = this.datasetToRangeAxesMap.get ( index );
            if ( mappedAxes == null ) {
                if ( !axisIndex.equals ( XYPlot.ZERO ) ) {
                    continue;
                }
                result.add ( entry.getValue() );
            } else {
                if ( !mappedAxes.contains ( axisIndex ) ) {
                    continue;
                }
                result.add ( entry.getValue() );
            }
        }
        return result;
    }
    public int getDomainAxisIndex ( final ValueAxis axis ) {
        int result = this.findDomainAxisIndex ( axis );
        if ( result < 0 ) {
            final Plot parent = this.getParent();
            if ( parent instanceof XYPlot ) {
                final XYPlot p = ( XYPlot ) parent;
                result = p.getDomainAxisIndex ( axis );
            }
        }
        return result;
    }
    private int findDomainAxisIndex ( final ValueAxis axis ) {
        for ( final Map.Entry<Integer, ValueAxis> entry : this.domainAxes.entrySet() ) {
            if ( entry.getValue() == axis ) {
                return entry.getKey();
            }
        }
        return -1;
    }
    public int getRangeAxisIndex ( final ValueAxis axis ) {
        int result = this.findRangeAxisIndex ( axis );
        if ( result < 0 ) {
            final Plot parent = this.getParent();
            if ( parent instanceof XYPlot ) {
                final XYPlot p = ( XYPlot ) parent;
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
    @Override
    public Range getDataRange ( final ValueAxis axis ) {
        Range result = null;
        final List<XYDataset> mappedDatasets = new ArrayList<XYDataset>();
        final List<XYAnnotation> includedAnnotations = new ArrayList<XYAnnotation>();
        boolean isDomainAxis = true;
        final int domainIndex = this.getDomainAxisIndex ( axis );
        if ( domainIndex >= 0 ) {
            isDomainAxis = true;
            mappedDatasets.addAll ( this.getDatasetsMappedToDomainAxis ( domainIndex ) );
            if ( domainIndex == 0 ) {
                for ( final XYAnnotation annotation : this.annotations ) {
                    if ( annotation instanceof XYAnnotationBoundsInfo ) {
                        includedAnnotations.add ( annotation );
                    }
                }
            }
        }
        final int rangeIndex = this.getRangeAxisIndex ( axis );
        if ( rangeIndex >= 0 ) {
            isDomainAxis = false;
            mappedDatasets.addAll ( this.getDatasetsMappedToRangeAxis ( rangeIndex ) );
            if ( rangeIndex == 0 ) {
                for ( final XYAnnotation annotation2 : this.annotations ) {
                    if ( annotation2 instanceof XYAnnotationBoundsInfo ) {
                        includedAnnotations.add ( annotation2 );
                    }
                }
            }
        }
        for ( final XYDataset d : mappedDatasets ) {
            if ( d != null ) {
                final XYItemRenderer r = this.getRendererForDataset ( d );
                if ( isDomainAxis ) {
                    if ( r != null ) {
                        result = Range.combine ( result, r.findDomainBounds ( d ) );
                    } else {
                        result = Range.combine ( result, DatasetUtilities.findDomainBounds ( d ) );
                    }
                } else if ( r != null ) {
                    result = Range.combine ( result, r.findRangeBounds ( d ) );
                } else {
                    result = Range.combine ( result, DatasetUtilities.findRangeBounds ( d ) );
                }
                if ( ! ( r instanceof AbstractXYItemRenderer ) ) {
                    continue;
                }
                final AbstractXYItemRenderer rr = ( AbstractXYItemRenderer ) r;
                final Collection c = rr.getAnnotations();
                for ( final XYAnnotation a : c ) {
                    if ( a instanceof XYAnnotationBoundsInfo ) {
                        includedAnnotations.add ( a );
                    }
                }
            }
        }
        for ( final XYAnnotationBoundsInfo xyabi : includedAnnotations ) {
            if ( xyabi.getIncludeInDataBounds() ) {
                if ( isDomainAxis ) {
                    result = Range.combine ( result, xyabi.getXRange() );
                } else {
                    result = Range.combine ( result, xyabi.getYRange() );
                }
            }
        }
        return result;
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
        this.configureDomainAxes();
        this.configureRangeAxes();
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
        if ( event.getSeriesVisibilityChanged() ) {
            this.configureDomainAxes();
            this.configureRangeAxes();
        }
        this.fireChangeEvent();
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
    public boolean isDomainCrosshairLockedOnData() {
        return this.domainCrosshairLockedOnData;
    }
    public void setDomainCrosshairLockedOnData ( final boolean flag ) {
        if ( this.domainCrosshairLockedOnData != flag ) {
            this.domainCrosshairLockedOnData = flag;
            this.fireChangeEvent();
        }
    }
    public double getDomainCrosshairValue() {
        return this.domainCrosshairValue;
    }
    public void setDomainCrosshairValue ( final double value ) {
        this.setDomainCrosshairValue ( value, true );
    }
    public void setDomainCrosshairValue ( final double value, final boolean notify ) {
        this.domainCrosshairValue = value;
        if ( this.isDomainCrosshairVisible() && notify ) {
            this.fireChangeEvent();
        }
    }
    public Stroke getDomainCrosshairStroke() {
        return this.domainCrosshairStroke;
    }
    public void setDomainCrosshairStroke ( final Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.domainCrosshairStroke = stroke;
        this.fireChangeEvent();
    }
    public Paint getDomainCrosshairPaint() {
        return this.domainCrosshairPaint;
    }
    public void setDomainCrosshairPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.domainCrosshairPaint = paint;
        this.fireChangeEvent();
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
    @Override
    public boolean isDomainPannable() {
        return this.domainPannable;
    }
    public void setDomainPannable ( final boolean pannable ) {
        this.domainPannable = pannable;
    }
    @Override
    public boolean isRangePannable() {
        return this.rangePannable;
    }
    public void setRangePannable ( final boolean pannable ) {
        this.rangePannable = pannable;
    }
    @Override
    public void panDomainAxes ( double percent, final PlotRenderingInfo info, final Point2D source ) {
        if ( !this.isDomainPannable() ) {
            return;
        }
        for ( int domainAxisCount = this.getDomainAxisCount(), i = 0; i < domainAxisCount; ++i ) {
            final ValueAxis axis = this.getDomainAxis ( i );
            if ( axis != null ) {
                if ( axis.isInverted() ) {
                    percent = -percent;
                }
                axis.pan ( percent );
            }
        }
    }
    @Override
    public void panRangeAxes ( double percent, final PlotRenderingInfo info, final Point2D source ) {
        if ( !this.isRangePannable() ) {
            return;
        }
        for ( int rangeAxisCount = this.getRangeAxisCount(), i = 0; i < rangeAxisCount; ++i ) {
            final ValueAxis axis = this.getRangeAxis ( i );
            if ( axis != null ) {
                if ( axis.isInverted() ) {
                    percent = -percent;
                }
                axis.pan ( percent );
            }
        }
    }
    @Override
    public void zoomDomainAxes ( final double factor, final PlotRenderingInfo info, final Point2D source ) {
        this.zoomDomainAxes ( factor, info, source, false );
    }
    @Override
    public void zoomDomainAxes ( final double factor, final PlotRenderingInfo info, final Point2D source, final boolean useAnchor ) {
        for ( final ValueAxis xAxis : this.domainAxes.values() ) {
            if ( xAxis == null ) {
                continue;
            }
            if ( useAnchor ) {
                double sourceX = source.getX();
                if ( this.orientation == PlotOrientation.HORIZONTAL ) {
                    sourceX = source.getY();
                }
                final double anchorX = xAxis.java2DToValue ( sourceX, info.getDataArea(), this.getDomainAxisEdge() );
                xAxis.resizeRange2 ( factor, anchorX );
            } else {
                xAxis.resizeRange ( factor );
            }
        }
    }
    @Override
    public void zoomDomainAxes ( final double lowerPercent, final double upperPercent, final PlotRenderingInfo info, final Point2D source ) {
        for ( final ValueAxis xAxis : this.domainAxes.values() ) {
            if ( xAxis != null ) {
                xAxis.zoomRange ( lowerPercent, upperPercent );
            }
        }
    }
    @Override
    public void zoomRangeAxes ( final double factor, final PlotRenderingInfo info, final Point2D source ) {
        this.zoomRangeAxes ( factor, info, source, false );
    }
    @Override
    public void zoomRangeAxes ( final double factor, final PlotRenderingInfo info, final Point2D source, final boolean useAnchor ) {
        for ( final ValueAxis yAxis : this.rangeAxes.values() ) {
            if ( yAxis == null ) {
                continue;
            }
            if ( useAnchor ) {
                double sourceY = source.getY();
                if ( this.orientation == PlotOrientation.HORIZONTAL ) {
                    sourceY = source.getX();
                }
                final double anchorY = yAxis.java2DToValue ( sourceY, info.getDataArea(), this.getRangeAxisEdge() );
                yAxis.resizeRange2 ( factor, anchorY );
            } else {
                yAxis.resizeRange ( factor );
            }
        }
    }
    @Override
    public void zoomRangeAxes ( final double lowerPercent, final double upperPercent, final PlotRenderingInfo info, final Point2D source ) {
        for ( final ValueAxis yAxis : this.rangeAxes.values() ) {
            if ( yAxis != null ) {
                yAxis.zoomRange ( lowerPercent, upperPercent );
            }
        }
    }
    @Override
    public boolean isDomainZoomable() {
        return true;
    }
    @Override
    public boolean isRangeZoomable() {
        return true;
    }
    public int getSeriesCount() {
        int result = 0;
        final XYDataset dataset = this.getDataset();
        if ( dataset != null ) {
            result = dataset.getSeriesCount();
        }
        return result;
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
        for ( final XYDataset dataset : this.datasets.values() ) {
            if ( dataset == null ) {
                continue;
            }
            final int datasetIndex = this.indexOf ( dataset );
            XYItemRenderer renderer = this.getRenderer ( datasetIndex );
            if ( renderer == null ) {
                renderer = this.getRenderer ( 0 );
            }
            if ( renderer == null ) {
                continue;
            }
            for ( int seriesCount = dataset.getSeriesCount(), i = 0; i < seriesCount; ++i ) {
                if ( renderer.isSeriesVisible ( i ) && renderer.isSeriesVisibleInLegend ( i ) ) {
                    final LegendItem item = renderer.getLegendItem ( datasetIndex, i );
                    if ( item != null ) {
                        result.add ( item );
                    }
                }
            }
        }
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof XYPlot ) ) {
            return false;
        }
        final XYPlot that = ( XYPlot ) obj;
        if ( this.weight != that.weight ) {
            return false;
        }
        if ( this.orientation != that.orientation ) {
            return false;
        }
        if ( !this.domainAxes.equals ( that.domainAxes ) ) {
            return false;
        }
        if ( !this.domainAxisLocations.equals ( that.domainAxisLocations ) ) {
            return false;
        }
        if ( this.rangeCrosshairLockedOnData != that.rangeCrosshairLockedOnData ) {
            return false;
        }
        if ( this.domainGridlinesVisible != that.domainGridlinesVisible ) {
            return false;
        }
        if ( this.rangeGridlinesVisible != that.rangeGridlinesVisible ) {
            return false;
        }
        if ( this.domainMinorGridlinesVisible != that.domainMinorGridlinesVisible ) {
            return false;
        }
        if ( this.rangeMinorGridlinesVisible != that.rangeMinorGridlinesVisible ) {
            return false;
        }
        if ( this.domainZeroBaselineVisible != that.domainZeroBaselineVisible ) {
            return false;
        }
        if ( this.rangeZeroBaselineVisible != that.rangeZeroBaselineVisible ) {
            return false;
        }
        if ( this.domainCrosshairVisible != that.domainCrosshairVisible ) {
            return false;
        }
        if ( this.domainCrosshairValue != that.domainCrosshairValue ) {
            return false;
        }
        if ( this.domainCrosshairLockedOnData != that.domainCrosshairLockedOnData ) {
            return false;
        }
        if ( this.rangeCrosshairVisible != that.rangeCrosshairVisible ) {
            return false;
        }
        if ( this.rangeCrosshairValue != that.rangeCrosshairValue ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.axisOffset, ( Object ) that.axisOffset ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.renderers, ( Object ) that.renderers ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.rangeAxes, ( Object ) that.rangeAxes ) ) {
            return false;
        }
        if ( !this.rangeAxisLocations.equals ( that.rangeAxisLocations ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.datasetToDomainAxesMap, ( Object ) that.datasetToDomainAxesMap ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.datasetToRangeAxesMap, ( Object ) that.datasetToRangeAxesMap ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.domainGridlineStroke, ( Object ) that.domainGridlineStroke ) ) {
            return false;
        }
        if ( !PaintUtilities.equal ( this.domainGridlinePaint, that.domainGridlinePaint ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.rangeGridlineStroke, ( Object ) that.rangeGridlineStroke ) ) {
            return false;
        }
        if ( !PaintUtilities.equal ( this.rangeGridlinePaint, that.rangeGridlinePaint ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.domainMinorGridlineStroke, ( Object ) that.domainMinorGridlineStroke ) ) {
            return false;
        }
        if ( !PaintUtilities.equal ( this.domainMinorGridlinePaint, that.domainMinorGridlinePaint ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.rangeMinorGridlineStroke, ( Object ) that.rangeMinorGridlineStroke ) ) {
            return false;
        }
        if ( !PaintUtilities.equal ( this.rangeMinorGridlinePaint, that.rangeMinorGridlinePaint ) ) {
            return false;
        }
        if ( !PaintUtilities.equal ( this.domainZeroBaselinePaint, that.domainZeroBaselinePaint ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.domainZeroBaselineStroke, ( Object ) that.domainZeroBaselineStroke ) ) {
            return false;
        }
        if ( !PaintUtilities.equal ( this.rangeZeroBaselinePaint, that.rangeZeroBaselinePaint ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.rangeZeroBaselineStroke, ( Object ) that.rangeZeroBaselineStroke ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.domainCrosshairStroke, ( Object ) that.domainCrosshairStroke ) ) {
            return false;
        }
        if ( !PaintUtilities.equal ( this.domainCrosshairPaint, that.domainCrosshairPaint ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.rangeCrosshairStroke, ( Object ) that.rangeCrosshairStroke ) ) {
            return false;
        }
        if ( !PaintUtilities.equal ( this.rangeCrosshairPaint, that.rangeCrosshairPaint ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.foregroundDomainMarkers, ( Object ) that.foregroundDomainMarkers ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.backgroundDomainMarkers, ( Object ) that.backgroundDomainMarkers ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.foregroundRangeMarkers, ( Object ) that.foregroundRangeMarkers ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.backgroundRangeMarkers, ( Object ) that.backgroundRangeMarkers ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.foregroundDomainMarkers, ( Object ) that.foregroundDomainMarkers ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.backgroundDomainMarkers, ( Object ) that.backgroundDomainMarkers ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.foregroundRangeMarkers, ( Object ) that.foregroundRangeMarkers ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.backgroundRangeMarkers, ( Object ) that.backgroundRangeMarkers ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.annotations, ( Object ) that.annotations ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( ( Object ) this.fixedLegendItems, ( Object ) that.fixedLegendItems ) ) {
            return false;
        }
        if ( !PaintUtilities.equal ( this.domainTickBandPaint, that.domainTickBandPaint ) ) {
            return false;
        }
        if ( !PaintUtilities.equal ( this.rangeTickBandPaint, that.rangeTickBandPaint ) ) {
            return false;
        }
        if ( !this.quadrantOrigin.equals ( that.quadrantOrigin ) ) {
            return false;
        }
        for ( int i = 0; i < 4; ++i ) {
            if ( !PaintUtilities.equal ( this.quadrantPaint[i], that.quadrantPaint[i] ) ) {
                return false;
            }
        }
        return ObjectUtilities.equal ( ( Object ) this.shadowGenerator, ( Object ) that.shadowGenerator ) && super.equals ( obj );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final XYPlot clone = ( XYPlot ) super.clone();
        clone.domainAxes = ( Map<Integer, ValueAxis> ) CloneUtils.cloneMapValues ( this.domainAxes );
        for ( final ValueAxis axis : clone.domainAxes.values() ) {
            if ( axis != null ) {
                axis.setPlot ( clone );
                axis.addChangeListener ( clone );
            }
        }
        clone.rangeAxes = ( Map<Integer, ValueAxis> ) CloneUtils.cloneMapValues ( this.rangeAxes );
        for ( final ValueAxis axis : clone.rangeAxes.values() ) {
            if ( axis != null ) {
                axis.setPlot ( clone );
                axis.addChangeListener ( clone );
            }
        }
        clone.domainAxisLocations = new HashMap<Integer, AxisLocation> ( this.domainAxisLocations );
        clone.rangeAxisLocations = new HashMap<Integer, AxisLocation> ( this.rangeAxisLocations );
        clone.datasets = new HashMap<Integer, XYDataset> ( this.datasets );
        for ( final XYDataset dataset : clone.datasets.values() ) {
            if ( dataset != null ) {
                dataset.addChangeListener ( clone );
            }
        }
        ( clone.datasetToDomainAxesMap = new TreeMap<Integer, List<Integer>>() ).putAll ( this.datasetToDomainAxesMap );
        ( clone.datasetToRangeAxesMap = new TreeMap<Integer, List<Integer>>() ).putAll ( this.datasetToRangeAxesMap );
        clone.renderers = ( Map<Integer, XYItemRenderer> ) CloneUtils.cloneMapValues ( this.renderers );
        for ( final XYItemRenderer renderer : clone.renderers.values() ) {
            if ( renderer != null ) {
                renderer.setPlot ( clone );
                renderer.addChangeListener ( clone );
            }
        }
        clone.foregroundDomainMarkers = ( Map ) ObjectUtilities.clone ( ( Object ) this.foregroundDomainMarkers );
        clone.backgroundDomainMarkers = ( Map ) ObjectUtilities.clone ( ( Object ) this.backgroundDomainMarkers );
        clone.foregroundRangeMarkers = ( Map ) ObjectUtilities.clone ( ( Object ) this.foregroundRangeMarkers );
        clone.backgroundRangeMarkers = ( Map ) ObjectUtilities.clone ( ( Object ) this.backgroundRangeMarkers );
        clone.annotations = ( List<XYAnnotation> ) ObjectUtilities.deepClone ( ( Collection ) this.annotations );
        if ( this.fixedDomainAxisSpace != null ) {
            clone.fixedDomainAxisSpace = ( AxisSpace ) ObjectUtilities.clone ( ( Object ) this.fixedDomainAxisSpace );
        }
        if ( this.fixedRangeAxisSpace != null ) {
            clone.fixedRangeAxisSpace = ( AxisSpace ) ObjectUtilities.clone ( ( Object ) this.fixedRangeAxisSpace );
        }
        if ( this.fixedLegendItems != null ) {
            clone.fixedLegendItems = ( LegendItemCollection ) this.fixedLegendItems.clone();
        }
        clone.quadrantOrigin = ( Point2D ) ObjectUtilities.clone ( ( Object ) this.quadrantOrigin );
        clone.quadrantPaint = this.quadrantPaint.clone();
        return clone;
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeStroke ( this.domainGridlineStroke, stream );
        SerialUtilities.writePaint ( this.domainGridlinePaint, stream );
        SerialUtilities.writeStroke ( this.rangeGridlineStroke, stream );
        SerialUtilities.writePaint ( this.rangeGridlinePaint, stream );
        SerialUtilities.writeStroke ( this.domainMinorGridlineStroke, stream );
        SerialUtilities.writePaint ( this.domainMinorGridlinePaint, stream );
        SerialUtilities.writeStroke ( this.rangeMinorGridlineStroke, stream );
        SerialUtilities.writePaint ( this.rangeMinorGridlinePaint, stream );
        SerialUtilities.writeStroke ( this.rangeZeroBaselineStroke, stream );
        SerialUtilities.writePaint ( this.rangeZeroBaselinePaint, stream );
        SerialUtilities.writeStroke ( this.domainCrosshairStroke, stream );
        SerialUtilities.writePaint ( this.domainCrosshairPaint, stream );
        SerialUtilities.writeStroke ( this.rangeCrosshairStroke, stream );
        SerialUtilities.writePaint ( this.rangeCrosshairPaint, stream );
        SerialUtilities.writePaint ( this.domainTickBandPaint, stream );
        SerialUtilities.writePaint ( this.rangeTickBandPaint, stream );
        SerialUtilities.writePoint2D ( this.quadrantOrigin, stream );
        for ( int i = 0; i < 4; ++i ) {
            SerialUtilities.writePaint ( this.quadrantPaint[i], stream );
        }
        SerialUtilities.writeStroke ( this.domainZeroBaselineStroke, stream );
        SerialUtilities.writePaint ( this.domainZeroBaselinePaint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.domainGridlineStroke = SerialUtilities.readStroke ( stream );
        this.domainGridlinePaint = SerialUtilities.readPaint ( stream );
        this.rangeGridlineStroke = SerialUtilities.readStroke ( stream );
        this.rangeGridlinePaint = SerialUtilities.readPaint ( stream );
        this.domainMinorGridlineStroke = SerialUtilities.readStroke ( stream );
        this.domainMinorGridlinePaint = SerialUtilities.readPaint ( stream );
        this.rangeMinorGridlineStroke = SerialUtilities.readStroke ( stream );
        this.rangeMinorGridlinePaint = SerialUtilities.readPaint ( stream );
        this.rangeZeroBaselineStroke = SerialUtilities.readStroke ( stream );
        this.rangeZeroBaselinePaint = SerialUtilities.readPaint ( stream );
        this.domainCrosshairStroke = SerialUtilities.readStroke ( stream );
        this.domainCrosshairPaint = SerialUtilities.readPaint ( stream );
        this.rangeCrosshairStroke = SerialUtilities.readStroke ( stream );
        this.rangeCrosshairPaint = SerialUtilities.readPaint ( stream );
        this.domainTickBandPaint = SerialUtilities.readPaint ( stream );
        this.rangeTickBandPaint = SerialUtilities.readPaint ( stream );
        this.quadrantOrigin = SerialUtilities.readPoint2D ( stream );
        this.quadrantPaint = new Paint[4];
        for ( int i = 0; i < 4; ++i ) {
            this.quadrantPaint[i] = SerialUtilities.readPaint ( stream );
        }
        this.domainZeroBaselineStroke = SerialUtilities.readStroke ( stream );
        this.domainZeroBaselinePaint = SerialUtilities.readPaint ( stream );
        for ( final ValueAxis axis : this.domainAxes.values() ) {
            if ( axis != null ) {
                axis.setPlot ( this );
                axis.addChangeListener ( this );
            }
        }
        for ( final ValueAxis axis : this.rangeAxes.values() ) {
            if ( axis != null ) {
                axis.setPlot ( this );
                axis.addChangeListener ( this );
            }
        }
        for ( final XYDataset dataset : this.datasets.values() ) {
            if ( dataset != null ) {
                dataset.addChangeListener ( this );
            }
        }
        for ( final XYItemRenderer renderer : this.renderers.values() ) {
            if ( renderer != null ) {
                renderer.addChangeListener ( this );
            }
        }
    }
    static {
        DEFAULT_GRIDLINE_STROKE = new BasicStroke ( 0.5f, 0, 2, 0.0f, new float[] { 2.0f, 2.0f }, 0.0f );
        DEFAULT_GRIDLINE_PAINT = Color.lightGray;
        DEFAULT_CROSSHAIR_STROKE = XYPlot.DEFAULT_GRIDLINE_STROKE;
        DEFAULT_CROSSHAIR_PAINT = Color.blue;
        XYPlot.localizationResources = ResourceBundleWrapper.getBundle ( "org.jfree.chart.plot.LocalizationBundle" );
    }
}
