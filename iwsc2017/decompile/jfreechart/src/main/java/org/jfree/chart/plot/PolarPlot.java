package org.jfree.chart.plot;
import org.jfree.chart.util.ResourceBundleWrapper;
import java.awt.BasicStroke;
import java.awt.Point;
import org.jfree.chart.axis.Axis;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.PublicCloneable;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;
import org.jfree.chart.LegendItem;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.data.Range;
import org.jfree.chart.axis.TickType;
import org.jfree.chart.axis.ValueTick;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.ui.RectangleEdge;
import java.awt.FontMetrics;
import java.util.Iterator;
import org.jfree.text.TextUtilities;
import org.jfree.chart.axis.AxisState;
import org.jfree.ui.RectangleInsets;
import java.awt.Composite;
import java.awt.AlphaComposite;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Collection;
import org.jfree.chart.axis.NumberTick;
import org.jfree.ui.TextAnchor;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.chart.util.ParamChecks;
import org.jfree.chart.event.AxisChangeListener;
import java.util.TreeMap;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.data.general.DatasetChangeListener;
import java.util.ArrayList;
import java.awt.Color;
import org.jfree.chart.renderer.PolarItemRenderer;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.xy.XYDataset;
import java.util.Map;
import org.jfree.chart.LegendItemCollection;
import java.awt.Font;
import org.jfree.chart.axis.TickUnit;
import org.jfree.util.ObjectList;
import java.util.List;
import java.util.ResourceBundle;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.Serializable;
import org.jfree.chart.event.RendererChangeListener;
public class PolarPlot extends Plot implements ValueAxisPlot, Zoomable, RendererChangeListener, Cloneable, Serializable {
    private static final long serialVersionUID = 3794383185924179525L;
    private static final int DEFAULT_MARGIN = 20;
    private static final double ANNOTATION_MARGIN = 7.0;
    public static final double DEFAULT_ANGLE_TICK_UNIT_SIZE = 45.0;
    public static final double DEFAULT_ANGLE_OFFSET = -90.0;
    public static final Stroke DEFAULT_GRIDLINE_STROKE;
    public static final Paint DEFAULT_GRIDLINE_PAINT;
    protected static ResourceBundle localizationResources;
    private List angleTicks;
    private ObjectList axes;
    private ObjectList axisLocations;
    private ObjectList datasets;
    private ObjectList renderers;
    private TickUnit angleTickUnit;
    private double angleOffset;
    private boolean counterClockwise;
    private boolean angleLabelsVisible;
    private Font angleLabelFont;
    private transient Paint angleLabelPaint;
    private boolean angleGridlinesVisible;
    private transient Stroke angleGridlineStroke;
    private transient Paint angleGridlinePaint;
    private boolean radiusGridlinesVisible;
    private transient Stroke radiusGridlineStroke;
    private transient Paint radiusGridlinePaint;
    private boolean radiusMinorGridlinesVisible;
    private List cornerTextItems;
    private int margin;
    private LegendItemCollection fixedLegendItems;
    private Map datasetToAxesMap;
    public PolarPlot() {
        this ( null, null, null );
    }
    public PolarPlot ( final XYDataset dataset, final ValueAxis radiusAxis, final PolarItemRenderer renderer ) {
        this.angleLabelsVisible = true;
        this.angleLabelFont = new Font ( "SansSerif", 0, 12 );
        this.angleLabelPaint = Color.black;
        this.cornerTextItems = new ArrayList();
        ( this.datasets = new ObjectList() ).set ( 0, ( Object ) dataset );
        if ( dataset != null ) {
            dataset.addChangeListener ( this );
        }
        this.angleTickUnit = new NumberTickUnit ( 45.0 );
        this.axes = new ObjectList();
        this.datasetToAxesMap = new TreeMap();
        this.axes.set ( 0, ( Object ) radiusAxis );
        if ( radiusAxis != null ) {
            radiusAxis.setPlot ( this );
            radiusAxis.addChangeListener ( this );
        }
        ( this.axisLocations = new ObjectList() ).set ( 0, ( Object ) PolarAxisLocation.EAST_ABOVE );
        this.axisLocations.set ( 1, ( Object ) PolarAxisLocation.NORTH_LEFT );
        this.axisLocations.set ( 2, ( Object ) PolarAxisLocation.WEST_BELOW );
        this.axisLocations.set ( 3, ( Object ) PolarAxisLocation.SOUTH_RIGHT );
        this.axisLocations.set ( 4, ( Object ) PolarAxisLocation.EAST_BELOW );
        this.axisLocations.set ( 5, ( Object ) PolarAxisLocation.NORTH_RIGHT );
        this.axisLocations.set ( 6, ( Object ) PolarAxisLocation.WEST_ABOVE );
        this.axisLocations.set ( 7, ( Object ) PolarAxisLocation.SOUTH_LEFT );
        ( this.renderers = new ObjectList() ).set ( 0, ( Object ) renderer );
        if ( renderer != null ) {
            renderer.setPlot ( this );
            renderer.addChangeListener ( this );
        }
        this.angleOffset = -90.0;
        this.counterClockwise = false;
        this.angleGridlinesVisible = true;
        this.angleGridlineStroke = PolarPlot.DEFAULT_GRIDLINE_STROKE;
        this.angleGridlinePaint = PolarPlot.DEFAULT_GRIDLINE_PAINT;
        this.radiusGridlinesVisible = true;
        this.radiusMinorGridlinesVisible = true;
        this.radiusGridlineStroke = PolarPlot.DEFAULT_GRIDLINE_STROKE;
        this.radiusGridlinePaint = PolarPlot.DEFAULT_GRIDLINE_PAINT;
        this.margin = 20;
    }
    @Override
    public String getPlotType() {
        return PolarPlot.localizationResources.getString ( "Polar_Plot" );
    }
    public ValueAxis getAxis() {
        return this.getAxis ( 0 );
    }
    public ValueAxis getAxis ( final int index ) {
        ValueAxis result = null;
        if ( index < this.axes.size() ) {
            result = ( ValueAxis ) this.axes.get ( index );
        }
        return result;
    }
    public void setAxis ( final ValueAxis axis ) {
        this.setAxis ( 0, axis );
    }
    public void setAxis ( final int index, final ValueAxis axis ) {
        this.setAxis ( index, axis, true );
    }
    public void setAxis ( final int index, final ValueAxis axis, final boolean notify ) {
        final ValueAxis existing = this.getAxis ( index );
        if ( existing != null ) {
            existing.removeChangeListener ( this );
        }
        if ( axis != null ) {
            axis.setPlot ( this );
        }
        this.axes.set ( index, ( Object ) axis );
        if ( axis != null ) {
            axis.configure();
            axis.addChangeListener ( this );
        }
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public PolarAxisLocation getAxisLocation() {
        return this.getAxisLocation ( 0 );
    }
    public PolarAxisLocation getAxisLocation ( final int index ) {
        PolarAxisLocation result = null;
        if ( index < this.axisLocations.size() ) {
            result = ( PolarAxisLocation ) this.axisLocations.get ( index );
        }
        return result;
    }
    public void setAxisLocation ( final PolarAxisLocation location ) {
        this.setAxisLocation ( 0, location, true );
    }
    public void setAxisLocation ( final PolarAxisLocation location, final boolean notify ) {
        this.setAxisLocation ( 0, location, notify );
    }
    public void setAxisLocation ( final int index, final PolarAxisLocation location ) {
        this.setAxisLocation ( index, location, true );
    }
    public void setAxisLocation ( final int index, final PolarAxisLocation location, final boolean notify ) {
        ParamChecks.nullNotPermitted ( location, "location" );
        this.axisLocations.set ( index, ( Object ) location );
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public int getAxisCount() {
        return this.axes.size();
    }
    public XYDataset getDataset() {
        return this.getDataset ( 0 );
    }
    public XYDataset getDataset ( final int index ) {
        XYDataset result = null;
        if ( index < this.datasets.size() ) {
            result = ( XYDataset ) this.datasets.get ( index );
        }
        return result;
    }
    public void setDataset ( final XYDataset dataset ) {
        this.setDataset ( 0, dataset );
    }
    public void setDataset ( final int index, final XYDataset dataset ) {
        final XYDataset existing = this.getDataset ( index );
        if ( existing != null ) {
            existing.removeChangeListener ( this );
        }
        this.datasets.set ( index, ( Object ) dataset );
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
        int result = -1;
        for ( int i = 0; i < this.datasets.size(); ++i ) {
            if ( dataset == this.datasets.get ( i ) ) {
                result = i;
                break;
            }
        }
        return result;
    }
    public PolarItemRenderer getRenderer() {
        return this.getRenderer ( 0 );
    }
    public PolarItemRenderer getRenderer ( final int index ) {
        PolarItemRenderer result = null;
        if ( index < this.renderers.size() ) {
            result = ( PolarItemRenderer ) this.renderers.get ( index );
        }
        return result;
    }
    public void setRenderer ( final PolarItemRenderer renderer ) {
        this.setRenderer ( 0, renderer );
    }
    public void setRenderer ( final int index, final PolarItemRenderer renderer ) {
        this.setRenderer ( index, renderer, true );
    }
    public void setRenderer ( final int index, final PolarItemRenderer renderer, final boolean notify ) {
        final PolarItemRenderer existing = this.getRenderer ( index );
        if ( existing != null ) {
            existing.removeChangeListener ( this );
        }
        this.renderers.set ( index, ( Object ) renderer );
        if ( renderer != null ) {
            renderer.setPlot ( this );
            renderer.addChangeListener ( this );
        }
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public TickUnit getAngleTickUnit() {
        return this.angleTickUnit;
    }
    public void setAngleTickUnit ( final TickUnit unit ) {
        ParamChecks.nullNotPermitted ( unit, "unit" );
        this.angleTickUnit = unit;
        this.fireChangeEvent();
    }
    public double getAngleOffset() {
        return this.angleOffset;
    }
    public void setAngleOffset ( final double offset ) {
        this.angleOffset = offset;
        this.fireChangeEvent();
    }
    public boolean isCounterClockwise() {
        return this.counterClockwise;
    }
    public void setCounterClockwise ( final boolean counterClockwise ) {
        this.counterClockwise = counterClockwise;
    }
    public boolean isAngleLabelsVisible() {
        return this.angleLabelsVisible;
    }
    public void setAngleLabelsVisible ( final boolean visible ) {
        if ( this.angleLabelsVisible != visible ) {
            this.angleLabelsVisible = visible;
            this.fireChangeEvent();
        }
    }
    public Font getAngleLabelFont() {
        return this.angleLabelFont;
    }
    public void setAngleLabelFont ( final Font font ) {
        ParamChecks.nullNotPermitted ( font, "font" );
        this.angleLabelFont = font;
        this.fireChangeEvent();
    }
    public Paint getAngleLabelPaint() {
        return this.angleLabelPaint;
    }
    public void setAngleLabelPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.angleLabelPaint = paint;
        this.fireChangeEvent();
    }
    public boolean isAngleGridlinesVisible() {
        return this.angleGridlinesVisible;
    }
    public void setAngleGridlinesVisible ( final boolean visible ) {
        if ( this.angleGridlinesVisible != visible ) {
            this.angleGridlinesVisible = visible;
            this.fireChangeEvent();
        }
    }
    public Stroke getAngleGridlineStroke() {
        return this.angleGridlineStroke;
    }
    public void setAngleGridlineStroke ( final Stroke stroke ) {
        this.angleGridlineStroke = stroke;
        this.fireChangeEvent();
    }
    public Paint getAngleGridlinePaint() {
        return this.angleGridlinePaint;
    }
    public void setAngleGridlinePaint ( final Paint paint ) {
        this.angleGridlinePaint = paint;
        this.fireChangeEvent();
    }
    public boolean isRadiusGridlinesVisible() {
        return this.radiusGridlinesVisible;
    }
    public void setRadiusGridlinesVisible ( final boolean visible ) {
        if ( this.radiusGridlinesVisible != visible ) {
            this.radiusGridlinesVisible = visible;
            this.fireChangeEvent();
        }
    }
    public Stroke getRadiusGridlineStroke() {
        return this.radiusGridlineStroke;
    }
    public void setRadiusGridlineStroke ( final Stroke stroke ) {
        this.radiusGridlineStroke = stroke;
        this.fireChangeEvent();
    }
    public Paint getRadiusGridlinePaint() {
        return this.radiusGridlinePaint;
    }
    public void setRadiusGridlinePaint ( final Paint paint ) {
        this.radiusGridlinePaint = paint;
        this.fireChangeEvent();
    }
    public boolean isRadiusMinorGridlinesVisible() {
        return this.radiusMinorGridlinesVisible;
    }
    public void setRadiusMinorGridlinesVisible ( final boolean flag ) {
        this.radiusMinorGridlinesVisible = flag;
        this.fireChangeEvent();
    }
    public int getMargin() {
        return this.margin;
    }
    public void setMargin ( final int margin ) {
        this.margin = margin;
        this.fireChangeEvent();
    }
    public LegendItemCollection getFixedLegendItems() {
        return this.fixedLegendItems;
    }
    public void setFixedLegendItems ( final LegendItemCollection items ) {
        this.fixedLegendItems = items;
        this.fireChangeEvent();
    }
    public void addCornerTextItem ( final String text ) {
        ParamChecks.nullNotPermitted ( text, "text" );
        this.cornerTextItems.add ( text );
        this.fireChangeEvent();
    }
    public void removeCornerTextItem ( final String text ) {
        final boolean removed = this.cornerTextItems.remove ( text );
        if ( removed ) {
            this.fireChangeEvent();
        }
    }
    public void clearCornerTextItems() {
        if ( this.cornerTextItems.size() > 0 ) {
            this.cornerTextItems.clear();
            this.fireChangeEvent();
        }
    }
    protected List refreshAngleTicks() {
        final List ticks = new ArrayList();
        for ( double currentTickVal = 0.0; currentTickVal < 360.0; currentTickVal += this.angleTickUnit.getSize() ) {
            final TextAnchor ta = this.calculateTextAnchor ( currentTickVal );
            final NumberTick tick = new NumberTick ( new Double ( currentTickVal ), this.angleTickUnit.valueToString ( currentTickVal ), ta, TextAnchor.CENTER, 0.0 );
            ticks.add ( tick );
        }
        return ticks;
    }
    protected TextAnchor calculateTextAnchor ( final double angleDegrees ) {
        TextAnchor ta = TextAnchor.CENTER;
        double offset;
        for ( offset = this.angleOffset; offset < 0.0; offset += 360.0 ) {}
        double normalizedAngle;
        for ( normalizedAngle = ( ( this.counterClockwise ? -1 : 1 ) * angleDegrees + offset ) % 360.0; this.counterClockwise && normalizedAngle < 0.0; normalizedAngle += 360.0 ) {}
        if ( normalizedAngle == 0.0 ) {
            ta = TextAnchor.CENTER_LEFT;
        } else if ( normalizedAngle > 0.0 && normalizedAngle < 90.0 ) {
            ta = TextAnchor.TOP_LEFT;
        } else if ( normalizedAngle == 90.0 ) {
            ta = TextAnchor.TOP_CENTER;
        } else if ( normalizedAngle > 90.0 && normalizedAngle < 180.0 ) {
            ta = TextAnchor.TOP_RIGHT;
        } else if ( normalizedAngle == 180.0 ) {
            ta = TextAnchor.CENTER_RIGHT;
        } else if ( normalizedAngle > 180.0 && normalizedAngle < 270.0 ) {
            ta = TextAnchor.BOTTOM_RIGHT;
        } else if ( normalizedAngle == 270.0 ) {
            ta = TextAnchor.BOTTOM_CENTER;
        } else if ( normalizedAngle > 270.0 && normalizedAngle < 360.0 ) {
            ta = TextAnchor.BOTTOM_LEFT;
        }
        return ta;
    }
    public void mapDatasetToAxis ( final int index, final int axisIndex ) {
        final List axisIndices = new ArrayList ( 1 );
        axisIndices.add ( new Integer ( axisIndex ) );
        this.mapDatasetToAxes ( index, axisIndices );
    }
    public void mapDatasetToAxes ( final int index, final List axisIndices ) {
        if ( index < 0 ) {
            throw new IllegalArgumentException ( "Requires 'index' >= 0." );
        }
        this.checkAxisIndices ( axisIndices );
        final Integer key = new Integer ( index );
        this.datasetToAxesMap.put ( key, new ArrayList ( axisIndices ) );
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
    public ValueAxis getAxisForDataset ( final int index ) {
        final List axisIndices = this.datasetToAxesMap.get ( new Integer ( index ) );
        ValueAxis valueAxis;
        if ( axisIndices != null ) {
            final Integer axisIndex = axisIndices.get ( 0 );
            valueAxis = this.getAxis ( axisIndex );
        } else {
            valueAxis = this.getAxis ( 0 );
        }
        return valueAxis;
    }
    public int getAxisIndex ( final ValueAxis axis ) {
        int result = this.axes.indexOf ( ( Object ) axis );
        if ( result < 0 ) {
            final Plot parent = this.getParent();
            if ( parent instanceof PolarPlot ) {
                final PolarPlot p = ( PolarPlot ) parent;
                result = p.getAxisIndex ( axis );
            }
        }
        return result;
    }
    public int getIndexOf ( final PolarItemRenderer renderer ) {
        return this.renderers.indexOf ( ( Object ) renderer );
    }
    @Override
    public void draw ( final Graphics2D g2, final Rectangle2D area, final Point2D anchor, final PlotState parentState, final PlotRenderingInfo info ) {
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
        if ( info != null ) {
            info.setDataArea ( area );
        }
        this.drawBackground ( g2, area );
        final int axisCount = this.axes.size();
        AxisState state = null;
        for ( int i = 0; i < axisCount; ++i ) {
            final ValueAxis axis = this.getAxis ( i );
            if ( axis != null ) {
                final PolarAxisLocation location = ( PolarAxisLocation ) this.axisLocations.get ( i );
                final AxisState s = this.drawAxis ( axis, location, g2, area );
                if ( i == 0 ) {
                    state = s;
                }
            }
        }
        final Shape originalClip = g2.getClip();
        final Composite originalComposite = g2.getComposite();
        g2.clip ( area );
        g2.setComposite ( AlphaComposite.getInstance ( 3, this.getForegroundAlpha() ) );
        this.drawGridlines ( g2, area, this.angleTicks = this.refreshAngleTicks(), state.getTicks() );
        this.render ( g2, area, info );
        g2.setClip ( originalClip );
        g2.setComposite ( originalComposite );
        this.drawOutline ( g2, area );
        this.drawCornerTextItems ( g2, area );
    }
    protected void drawCornerTextItems ( final Graphics2D g2, final Rectangle2D area ) {
        if ( this.cornerTextItems.isEmpty() ) {
            return;
        }
        g2.setColor ( Color.black );
        double width = 0.0;
        double height = 0.0;
        for ( final String msg : this.cornerTextItems ) {
            final FontMetrics fm = g2.getFontMetrics();
            final Rectangle2D bounds = TextUtilities.getTextBounds ( msg, g2, fm );
            width = Math.max ( width, bounds.getWidth() );
            height += bounds.getHeight();
        }
        final double xadj = 14.0;
        final double yadj = 7.0;
        width += xadj;
        height += yadj;
        double x = area.getMaxX() - width;
        double y = area.getMaxY() - height;
        g2.drawRect ( ( int ) x, ( int ) y, ( int ) width, ( int ) height );
        x += 7.0;
        for ( final String msg2 : this.cornerTextItems ) {
            final Rectangle2D bounds2 = TextUtilities.getTextBounds ( msg2, g2, g2.getFontMetrics() );
            y += bounds2.getHeight();
            g2.drawString ( msg2, ( int ) x, ( int ) y );
        }
    }
    protected AxisState drawAxis ( final ValueAxis axis, final PolarAxisLocation location, final Graphics2D g2, final Rectangle2D plotArea ) {
        final double centerX = plotArea.getCenterX();
        final double centerY = plotArea.getCenterY();
        final double r = Math.min ( plotArea.getWidth() / 2.0, plotArea.getHeight() / 2.0 ) - this.margin;
        final double x = centerX - r;
        final double y = centerY - r;
        Rectangle2D dataArea = null;
        AxisState result = null;
        if ( location == PolarAxisLocation.NORTH_RIGHT ) {
            dataArea = new Rectangle2D.Double ( x, y, r, r );
            result = axis.draw ( g2, centerX, plotArea, dataArea, RectangleEdge.RIGHT, null );
        } else if ( location == PolarAxisLocation.NORTH_LEFT ) {
            dataArea = new Rectangle2D.Double ( centerX, y, r, r );
            result = axis.draw ( g2, centerX, plotArea, dataArea, RectangleEdge.LEFT, null );
        } else if ( location == PolarAxisLocation.SOUTH_LEFT ) {
            dataArea = new Rectangle2D.Double ( centerX, centerY, r, r );
            result = axis.draw ( g2, centerX, plotArea, dataArea, RectangleEdge.LEFT, null );
        } else if ( location == PolarAxisLocation.SOUTH_RIGHT ) {
            dataArea = new Rectangle2D.Double ( x, centerY, r, r );
            result = axis.draw ( g2, centerX, plotArea, dataArea, RectangleEdge.RIGHT, null );
        } else if ( location == PolarAxisLocation.EAST_ABOVE ) {
            dataArea = new Rectangle2D.Double ( centerX, centerY, r, r );
            result = axis.draw ( g2, centerY, plotArea, dataArea, RectangleEdge.TOP, null );
        } else if ( location == PolarAxisLocation.EAST_BELOW ) {
            dataArea = new Rectangle2D.Double ( centerX, y, r, r );
            result = axis.draw ( g2, centerY, plotArea, dataArea, RectangleEdge.BOTTOM, null );
        } else if ( location == PolarAxisLocation.WEST_ABOVE ) {
            dataArea = new Rectangle2D.Double ( x, centerY, r, r );
            result = axis.draw ( g2, centerY, plotArea, dataArea, RectangleEdge.TOP, null );
        } else if ( location == PolarAxisLocation.WEST_BELOW ) {
            dataArea = new Rectangle2D.Double ( x, y, r, r );
            result = axis.draw ( g2, centerY, plotArea, dataArea, RectangleEdge.BOTTOM, null );
        }
        return result;
    }
    protected void render ( final Graphics2D g2, final Rectangle2D dataArea, final PlotRenderingInfo info ) {
        boolean hasData = false;
        final int datasetCount = this.datasets.size();
        for ( int i = datasetCount - 1; i >= 0; --i ) {
            final XYDataset dataset = this.getDataset ( i );
            if ( dataset != null ) {
                final PolarItemRenderer renderer = this.getRenderer ( i );
                if ( renderer != null ) {
                    if ( !DatasetUtilities.isEmptyOrNull ( dataset ) ) {
                        hasData = true;
                        for ( int seriesCount = dataset.getSeriesCount(), series = 0; series < seriesCount; ++series ) {
                            renderer.drawSeries ( g2, dataArea, info, this, dataset, series );
                        }
                    }
                }
            }
        }
        if ( !hasData ) {
            this.drawNoDataMessage ( g2, dataArea );
        }
    }
    protected void drawGridlines ( final Graphics2D g2, final Rectangle2D dataArea, final List angularTicks, final List radialTicks ) {
        final PolarItemRenderer renderer = this.getRenderer();
        if ( renderer == null ) {
            return;
        }
        if ( this.isAngleGridlinesVisible() ) {
            final Stroke gridStroke = this.getAngleGridlineStroke();
            final Paint gridPaint = this.getAngleGridlinePaint();
            if ( gridStroke != null && gridPaint != null ) {
                renderer.drawAngularGridLines ( g2, this, angularTicks, dataArea );
            }
        }
        if ( this.isRadiusGridlinesVisible() ) {
            final Stroke gridStroke = this.getRadiusGridlineStroke();
            final Paint gridPaint = this.getRadiusGridlinePaint();
            if ( gridStroke != null && gridPaint != null ) {
                final List ticks = this.buildRadialTicks ( radialTicks );
                renderer.drawRadialGridLines ( g2, this, this.getAxis(), ticks, dataArea );
            }
        }
    }
    protected List buildRadialTicks ( final List allTicks ) {
        final List ticks = new ArrayList();
        for ( final ValueTick tick : allTicks ) {
            if ( this.isRadiusMinorGridlinesVisible() || TickType.MAJOR.equals ( tick.getTickType() ) ) {
                ticks.add ( tick );
            }
        }
        return ticks;
    }
    @Override
    public void zoom ( final double percent ) {
        for ( int axisIdx = 0; axisIdx < this.getAxisCount(); ++axisIdx ) {
            final ValueAxis axis = this.getAxis ( axisIdx );
            if ( axis != null ) {
                if ( percent > 0.0 ) {
                    final double radius = axis.getUpperBound();
                    final double scaledRadius = radius * percent;
                    axis.setUpperBound ( scaledRadius );
                    axis.setAutoRange ( false );
                } else {
                    axis.setAutoRange ( true );
                }
            }
        }
    }
    private List getDatasetsMappedToAxis ( final Integer axisIndex ) {
        ParamChecks.nullNotPermitted ( axisIndex, "axisIndex" );
        final List result = new ArrayList();
        for ( int i = 0; i < this.datasets.size(); ++i ) {
            final List mappedAxes = this.datasetToAxesMap.get ( new Integer ( i ) );
            if ( mappedAxes == null ) {
                if ( axisIndex.equals ( PolarPlot.ZERO ) ) {
                    result.add ( this.datasets.get ( i ) );
                }
            } else if ( mappedAxes.contains ( axisIndex ) ) {
                result.add ( this.datasets.get ( i ) );
            }
        }
        return result;
    }
    @Override
    public Range getDataRange ( final ValueAxis axis ) {
        Range result = null;
        final int axisIdx = this.getAxisIndex ( axis );
        List mappedDatasets = new ArrayList();
        if ( axisIdx >= 0 ) {
            mappedDatasets = this.getDatasetsMappedToAxis ( new Integer ( axisIdx ) );
        }
        final Iterator iterator = mappedDatasets.iterator();
        int datasetIdx = -1;
        while ( iterator.hasNext() ) {
            ++datasetIdx;
            final XYDataset d = iterator.next();
            if ( d != null ) {
                result = Range.combine ( result, DatasetUtilities.findRangeBounds ( d ) );
            }
        }
        return result;
    }
    @Override
    public void datasetChanged ( final DatasetChangeEvent event ) {
        for ( int i = 0; i < this.axes.size(); ++i ) {
            final ValueAxis axis = ( ValueAxis ) this.axes.get ( i );
            if ( axis != null ) {
                axis.configure();
            }
        }
        if ( this.getParent() != null ) {
            this.getParent().datasetChanged ( event );
        } else {
            super.datasetChanged ( event );
        }
    }
    @Override
    public void rendererChanged ( final RendererChangeEvent event ) {
        this.fireChangeEvent();
    }
    @Override
    public LegendItemCollection getLegendItems() {
        if ( this.fixedLegendItems != null ) {
            return this.fixedLegendItems;
        }
        final LegendItemCollection result = new LegendItemCollection();
        for ( int count = this.datasets.size(), datasetIndex = 0; datasetIndex < count; ++datasetIndex ) {
            final XYDataset dataset = this.getDataset ( datasetIndex );
            final PolarItemRenderer renderer = this.getRenderer ( datasetIndex );
            if ( dataset != null && renderer != null ) {
                for ( int seriesCount = dataset.getSeriesCount(), i = 0; i < seriesCount; ++i ) {
                    final LegendItem item = renderer.getLegendItem ( i );
                    result.add ( item );
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
        if ( ! ( obj instanceof PolarPlot ) ) {
            return false;
        }
        final PolarPlot that = ( PolarPlot ) obj;
        return this.axes.equals ( ( Object ) that.axes ) && this.axisLocations.equals ( ( Object ) that.axisLocations ) && this.renderers.equals ( ( Object ) that.renderers ) && this.angleTickUnit.equals ( that.angleTickUnit ) && this.angleGridlinesVisible == that.angleGridlinesVisible && this.angleOffset == that.angleOffset && this.counterClockwise == that.counterClockwise && this.angleLabelsVisible == that.angleLabelsVisible && this.angleLabelFont.equals ( that.angleLabelFont ) && PaintUtilities.equal ( this.angleLabelPaint, that.angleLabelPaint ) && ObjectUtilities.equal ( ( Object ) this.angleGridlineStroke, ( Object ) that.angleGridlineStroke ) && PaintUtilities.equal ( this.angleGridlinePaint, that.angleGridlinePaint ) && this.radiusGridlinesVisible == that.radiusGridlinesVisible && ObjectUtilities.equal ( ( Object ) this.radiusGridlineStroke, ( Object ) that.radiusGridlineStroke ) && PaintUtilities.equal ( this.radiusGridlinePaint, that.radiusGridlinePaint ) && this.radiusMinorGridlinesVisible == that.radiusMinorGridlinesVisible && this.cornerTextItems.equals ( that.cornerTextItems ) && this.margin == that.margin && ObjectUtilities.equal ( ( Object ) this.fixedLegendItems, ( Object ) that.fixedLegendItems ) && super.equals ( obj );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final PolarPlot clone = ( PolarPlot ) super.clone();
        clone.axes = ( ObjectList ) ObjectUtilities.clone ( ( Object ) this.axes );
        for ( int i = 0; i < this.axes.size(); ++i ) {
            final ValueAxis axis = ( ValueAxis ) this.axes.get ( i );
            if ( axis != null ) {
                final ValueAxis clonedAxis = ( ValueAxis ) axis.clone();
                clone.axes.set ( i, ( Object ) clonedAxis );
                clonedAxis.setPlot ( clone );
                clonedAxis.addChangeListener ( clone );
            }
        }
        clone.datasets = ( ObjectList ) ObjectUtilities.clone ( ( Object ) this.datasets );
        for ( int i = 0; i < clone.datasets.size(); ++i ) {
            final XYDataset d = this.getDataset ( i );
            if ( d != null ) {
                d.addChangeListener ( clone );
            }
        }
        clone.renderers = ( ObjectList ) ObjectUtilities.clone ( ( Object ) this.renderers );
        for ( int i = 0; i < this.renderers.size(); ++i ) {
            final PolarItemRenderer renderer2 = ( PolarItemRenderer ) this.renderers.get ( i );
            if ( renderer2 instanceof PublicCloneable ) {
                final PublicCloneable pc = ( PublicCloneable ) renderer2;
                final PolarItemRenderer rc = ( PolarItemRenderer ) pc.clone();
                clone.renderers.set ( i, ( Object ) rc );
                rc.setPlot ( clone );
                rc.addChangeListener ( clone );
            }
        }
        clone.cornerTextItems = new ArrayList ( this.cornerTextItems );
        return clone;
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeStroke ( this.angleGridlineStroke, stream );
        SerialUtilities.writePaint ( this.angleGridlinePaint, stream );
        SerialUtilities.writeStroke ( this.radiusGridlineStroke, stream );
        SerialUtilities.writePaint ( this.radiusGridlinePaint, stream );
        SerialUtilities.writePaint ( this.angleLabelPaint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.angleGridlineStroke = SerialUtilities.readStroke ( stream );
        this.angleGridlinePaint = SerialUtilities.readPaint ( stream );
        this.radiusGridlineStroke = SerialUtilities.readStroke ( stream );
        this.radiusGridlinePaint = SerialUtilities.readPaint ( stream );
        this.angleLabelPaint = SerialUtilities.readPaint ( stream );
        for ( int rangeAxisCount = this.axes.size(), i = 0; i < rangeAxisCount; ++i ) {
            final Axis axis = ( Axis ) this.axes.get ( i );
            if ( axis != null ) {
                axis.setPlot ( this );
                axis.addChangeListener ( this );
            }
        }
        for ( int datasetCount = this.datasets.size(), j = 0; j < datasetCount; ++j ) {
            final Dataset dataset = ( Dataset ) this.datasets.get ( j );
            if ( dataset != null ) {
                dataset.addChangeListener ( this );
            }
        }
        for ( int rendererCount = this.renderers.size(), k = 0; k < rendererCount; ++k ) {
            final PolarItemRenderer renderer = ( PolarItemRenderer ) this.renderers.get ( k );
            if ( renderer != null ) {
                renderer.addChangeListener ( this );
            }
        }
    }
    @Override
    public void zoomDomainAxes ( final double factor, final PlotRenderingInfo state, final Point2D source ) {
    }
    @Override
    public void zoomDomainAxes ( final double factor, final PlotRenderingInfo state, final Point2D source, final boolean useAnchor ) {
    }
    @Override
    public void zoomDomainAxes ( final double lowerPercent, final double upperPercent, final PlotRenderingInfo state, final Point2D source ) {
    }
    @Override
    public void zoomRangeAxes ( final double factor, final PlotRenderingInfo state, final Point2D source ) {
        this.zoom ( factor );
    }
    @Override
    public void zoomRangeAxes ( final double factor, final PlotRenderingInfo info, final Point2D source, final boolean useAnchor ) {
        final double sourceX = source.getX();
        for ( int axisIdx = 0; axisIdx < this.getAxisCount(); ++axisIdx ) {
            final ValueAxis axis = this.getAxis ( axisIdx );
            if ( axis != null ) {
                if ( useAnchor ) {
                    final double anchorX = axis.java2DToValue ( sourceX, info.getDataArea(), RectangleEdge.BOTTOM );
                    axis.resizeRange ( factor, anchorX );
                } else {
                    axis.resizeRange ( factor );
                }
            }
        }
    }
    @Override
    public void zoomRangeAxes ( final double lowerPercent, final double upperPercent, final PlotRenderingInfo state, final Point2D source ) {
        this.zoom ( ( upperPercent + lowerPercent ) / 2.0 );
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
    public PlotOrientation getOrientation() {
        return PlotOrientation.HORIZONTAL;
    }
    public Point translateToJava2D ( double angleDegrees, final double radius, final ValueAxis axis, final Rectangle2D dataArea ) {
        if ( this.counterClockwise ) {
            angleDegrees = -angleDegrees;
        }
        final double radians = Math.toRadians ( angleDegrees + this.angleOffset );
        final double minx = dataArea.getMinX() + this.margin;
        final double maxx = dataArea.getMaxX() - this.margin;
        final double miny = dataArea.getMinY() + this.margin;
        final double maxy = dataArea.getMaxY() - this.margin;
        final double halfWidth = ( maxx - minx ) / 2.0;
        final double halfHeight = ( maxy - miny ) / 2.0;
        final double midX = minx + halfWidth;
        final double midY = miny + halfHeight;
        final double l = Math.min ( halfWidth, halfHeight );
        final Rectangle2D quadrant = new Rectangle2D.Double ( midX, midY, l, l );
        final double axisMin = axis.getLowerBound();
        final double adjustedRadius = Math.max ( radius, axisMin );
        final double length = axis.valueToJava2D ( adjustedRadius, quadrant, RectangleEdge.BOTTOM ) - midX;
        final float x = ( float ) ( midX + Math.cos ( radians ) * length );
        final float y = ( float ) ( midY + Math.sin ( radians ) * length );
        final int ix = Math.round ( x );
        final int iy = Math.round ( y );
        final Point p = new Point ( ix, iy );
        return p;
    }
    public Point translateValueThetaRadiusToJava2D ( final double angleDegrees, final double radius, final Rectangle2D dataArea ) {
        return this.translateToJava2D ( angleDegrees, radius, this.getAxis(), dataArea );
    }
    public double getMaxRadius() {
        return this.getAxis().getUpperBound();
    }
    public int getSeriesCount() {
        int result = 0;
        final XYDataset dataset = this.getDataset ( 0 );
        if ( dataset != null ) {
            result = dataset.getSeriesCount();
        }
        return result;
    }
    protected AxisState drawAxis ( final Graphics2D g2, final Rectangle2D plotArea, final Rectangle2D dataArea ) {
        return this.getAxis().draw ( g2, dataArea.getMinY(), plotArea, dataArea, RectangleEdge.TOP, null );
    }
    static {
        DEFAULT_GRIDLINE_STROKE = new BasicStroke ( 0.5f, 0, 2, 0.0f, new float[] { 2.0f, 2.0f }, 0.0f );
        DEFAULT_GRIDLINE_PAINT = Color.gray;
        PolarPlot.localizationResources = ResourceBundleWrapper.getBundle ( "org.jfree.chart.plot.LocalizationBundle" );
    }
}
