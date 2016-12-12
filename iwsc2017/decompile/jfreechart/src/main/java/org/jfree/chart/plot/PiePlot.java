package org.jfree.chart.plot;
import org.jfree.chart.util.ResourceBundleWrapper;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.PublicCloneable;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;
import java.awt.RadialGradientPaint;
import org.jfree.ui.RectangleAnchor;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.QuadCurve2D;
import org.jfree.chart.LegendItem;
import java.awt.geom.Line2D;
import org.jfree.chart.LegendItemCollection;
import org.jfree.text.TextBlock;
import org.jfree.text.TextBox;
import org.jfree.text.TextMeasurer;
import org.jfree.text.G2TextMeasurer;
import org.jfree.data.KeyedValues;
import org.jfree.data.DefaultKeyedValues;
import java.awt.FontMetrics;
import org.jfree.ui.TextAnchor;
import org.jfree.text.TextUtilities;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.util.ShapeUtilities;
import java.awt.geom.Arc2D;
import java.util.List;
import java.awt.image.ImageObserver;
import java.awt.Image;
import java.awt.image.BufferedImage;
import org.jfree.chart.JFreeChart;
import java.awt.Composite;
import java.awt.AlphaComposite;
import java.awt.geom.Point2D;
import org.jfree.data.general.DatasetUtilities;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.util.Iterator;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.util.UnitType;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import java.util.TreeMap;
import org.jfree.data.general.DatasetChangeListener;
import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ResourceBundle;
import org.jfree.chart.util.ShadowGenerator;
import java.awt.Shape;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.ui.RectangleInsets;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import java.util.Map;
import org.jfree.chart.StrokeMap;
import org.jfree.chart.PaintMap;
import org.jfree.util.Rotation;
import org.jfree.data.general.PieDataset;
import java.awt.Stroke;
import java.awt.Paint;
import java.awt.Font;
import java.io.Serializable;
public class PiePlot extends Plot implements Cloneable, Serializable {
    private static final long serialVersionUID = -795612466005590431L;
    public static final double DEFAULT_INTERIOR_GAP = 0.08;
    public static final double MAX_INTERIOR_GAP = 0.4;
    public static final double DEFAULT_START_ANGLE = 90.0;
    public static final Font DEFAULT_LABEL_FONT;
    public static final Paint DEFAULT_LABEL_PAINT;
    public static final Paint DEFAULT_LABEL_BACKGROUND_PAINT;
    public static final Paint DEFAULT_LABEL_OUTLINE_PAINT;
    public static final Stroke DEFAULT_LABEL_OUTLINE_STROKE;
    public static final Paint DEFAULT_LABEL_SHADOW_PAINT;
    public static final double DEFAULT_MINIMUM_ARC_ANGLE_TO_DRAW = 1.0E-5;
    private PieDataset dataset;
    private int pieIndex;
    private double interiorGap;
    private boolean circular;
    private double startAngle;
    private Rotation direction;
    private PaintMap sectionPaintMap;
    private transient Paint baseSectionPaint;
    private boolean autoPopulateSectionPaint;
    private boolean sectionOutlinesVisible;
    private PaintMap sectionOutlinePaintMap;
    private transient Paint baseSectionOutlinePaint;
    private boolean autoPopulateSectionOutlinePaint;
    private StrokeMap sectionOutlineStrokeMap;
    private transient Stroke baseSectionOutlineStroke;
    private boolean autoPopulateSectionOutlineStroke;
    private transient Paint shadowPaint;
    private double shadowXOffset;
    private double shadowYOffset;
    private Map explodePercentages;
    private PieSectionLabelGenerator labelGenerator;
    private Font labelFont;
    private transient Paint labelPaint;
    private transient Paint labelBackgroundPaint;
    private transient Paint labelOutlinePaint;
    private transient Stroke labelOutlineStroke;
    private transient Paint labelShadowPaint;
    private boolean simpleLabels;
    private RectangleInsets labelPadding;
    private RectangleInsets simpleLabelOffset;
    private double maximumLabelWidth;
    private double labelGap;
    private boolean labelLinksVisible;
    private PieLabelLinkStyle labelLinkStyle;
    private double labelLinkMargin;
    private transient Paint labelLinkPaint;
    private transient Stroke labelLinkStroke;
    private AbstractPieLabelDistributor labelDistributor;
    private PieToolTipGenerator toolTipGenerator;
    private PieURLGenerator urlGenerator;
    private PieSectionLabelGenerator legendLabelGenerator;
    private PieSectionLabelGenerator legendLabelToolTipGenerator;
    private PieURLGenerator legendLabelURLGenerator;
    private boolean ignoreNullValues;
    private boolean ignoreZeroValues;
    private transient Shape legendItemShape;
    private double minimumArcAngleToDraw;
    private ShadowGenerator shadowGenerator;
    protected static ResourceBundle localizationResources;
    static final boolean DEBUG_DRAW_INTERIOR = false;
    static final boolean DEBUG_DRAW_LINK_AREA = false;
    static final boolean DEBUG_DRAW_PIE_AREA = false;
    private transient Paint sectionPaint;
    private transient Paint sectionOutlinePaint;
    private transient Stroke sectionOutlineStroke;
    public PiePlot() {
        this ( null );
    }
    public PiePlot ( final PieDataset dataset ) {
        this.shadowPaint = Color.gray;
        this.shadowXOffset = 4.0;
        this.shadowYOffset = 4.0;
        this.simpleLabels = true;
        this.maximumLabelWidth = 0.14;
        this.labelGap = 0.025;
        this.labelLinkStyle = PieLabelLinkStyle.STANDARD;
        this.labelLinkMargin = 0.025;
        this.labelLinkPaint = Color.black;
        this.labelLinkStroke = new BasicStroke ( 0.5f );
        this.dataset = dataset;
        if ( dataset != null ) {
            dataset.addChangeListener ( this );
        }
        this.pieIndex = 0;
        this.interiorGap = 0.08;
        this.circular = true;
        this.startAngle = 90.0;
        this.direction = Rotation.CLOCKWISE;
        this.minimumArcAngleToDraw = 1.0E-5;
        this.sectionPaint = null;
        this.sectionPaintMap = new PaintMap();
        this.baseSectionPaint = Color.gray;
        this.autoPopulateSectionPaint = true;
        this.sectionOutlinesVisible = true;
        this.sectionOutlinePaint = null;
        this.sectionOutlinePaintMap = new PaintMap();
        this.baseSectionOutlinePaint = PiePlot.DEFAULT_OUTLINE_PAINT;
        this.autoPopulateSectionOutlinePaint = false;
        this.sectionOutlineStroke = null;
        this.sectionOutlineStrokeMap = new StrokeMap();
        this.baseSectionOutlineStroke = PiePlot.DEFAULT_OUTLINE_STROKE;
        this.autoPopulateSectionOutlineStroke = false;
        this.explodePercentages = new TreeMap();
        this.labelGenerator = new StandardPieSectionLabelGenerator();
        this.labelFont = PiePlot.DEFAULT_LABEL_FONT;
        this.labelPaint = PiePlot.DEFAULT_LABEL_PAINT;
        this.labelBackgroundPaint = PiePlot.DEFAULT_LABEL_BACKGROUND_PAINT;
        this.labelOutlinePaint = PiePlot.DEFAULT_LABEL_OUTLINE_PAINT;
        this.labelOutlineStroke = PiePlot.DEFAULT_LABEL_OUTLINE_STROKE;
        this.labelShadowPaint = PiePlot.DEFAULT_LABEL_SHADOW_PAINT;
        this.labelLinksVisible = true;
        this.labelDistributor = new PieLabelDistributor ( 0 );
        this.simpleLabels = false;
        this.simpleLabelOffset = new RectangleInsets ( UnitType.RELATIVE, 0.18, 0.18, 0.18, 0.18 );
        this.labelPadding = new RectangleInsets ( 2.0, 2.0, 2.0, 2.0 );
        this.toolTipGenerator = null;
        this.urlGenerator = null;
        this.legendLabelGenerator = new StandardPieSectionLabelGenerator();
        this.legendLabelToolTipGenerator = null;
        this.legendLabelURLGenerator = null;
        this.legendItemShape = Plot.DEFAULT_LEGEND_ITEM_CIRCLE;
        this.ignoreNullValues = false;
        this.ignoreZeroValues = false;
        this.shadowGenerator = null;
    }
    public PieDataset getDataset() {
        return this.dataset;
    }
    public void setDataset ( final PieDataset dataset ) {
        final PieDataset existing = this.dataset;
        if ( existing != null ) {
            existing.removeChangeListener ( this );
        }
        if ( ( this.dataset = dataset ) != null ) {
            this.setDatasetGroup ( dataset.getGroup() );
            dataset.addChangeListener ( this );
        }
        final DatasetChangeEvent event = new DatasetChangeEvent ( this, dataset );
        this.datasetChanged ( event );
    }
    public int getPieIndex() {
        return this.pieIndex;
    }
    public void setPieIndex ( final int index ) {
        this.pieIndex = index;
    }
    public double getStartAngle() {
        return this.startAngle;
    }
    public void setStartAngle ( final double angle ) {
        this.startAngle = angle;
        this.fireChangeEvent();
    }
    public Rotation getDirection() {
        return this.direction;
    }
    public void setDirection ( final Rotation direction ) {
        ParamChecks.nullNotPermitted ( direction, "direction" );
        this.direction = direction;
        this.fireChangeEvent();
    }
    public double getInteriorGap() {
        return this.interiorGap;
    }
    public void setInteriorGap ( final double percent ) {
        if ( percent < 0.0 || percent > 0.4 ) {
            throw new IllegalArgumentException ( "Invalid 'percent' (" + percent + ") argument." );
        }
        if ( this.interiorGap != percent ) {
            this.interiorGap = percent;
            this.fireChangeEvent();
        }
    }
    public boolean isCircular() {
        return this.circular;
    }
    public void setCircular ( final boolean flag ) {
        this.setCircular ( flag, true );
    }
    public void setCircular ( final boolean circular, final boolean notify ) {
        this.circular = circular;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public boolean getIgnoreNullValues() {
        return this.ignoreNullValues;
    }
    public void setIgnoreNullValues ( final boolean flag ) {
        this.ignoreNullValues = flag;
        this.fireChangeEvent();
    }
    public boolean getIgnoreZeroValues() {
        return this.ignoreZeroValues;
    }
    public void setIgnoreZeroValues ( final boolean flag ) {
        this.ignoreZeroValues = flag;
        this.fireChangeEvent();
    }
    protected Paint lookupSectionPaint ( final Comparable key ) {
        return this.lookupSectionPaint ( key, this.getAutoPopulateSectionPaint() );
    }
    protected Paint lookupSectionPaint ( final Comparable key, final boolean autoPopulate ) {
        Paint result = this.getSectionPaint();
        if ( result != null ) {
            return result;
        }
        result = this.sectionPaintMap.getPaint ( key );
        if ( result != null ) {
            return result;
        }
        if ( autoPopulate ) {
            final DrawingSupplier ds = this.getDrawingSupplier();
            if ( ds != null ) {
                result = ds.getNextPaint();
                this.sectionPaintMap.put ( key, result );
            } else {
                result = this.baseSectionPaint;
            }
        } else {
            result = this.baseSectionPaint;
        }
        return result;
    }
    public Paint getSectionPaint() {
        return this.sectionPaint;
    }
    public void setSectionPaint ( final Paint paint ) {
        this.sectionPaint = paint;
        this.fireChangeEvent();
    }
    protected Comparable getSectionKey ( final int section ) {
        Comparable key = null;
        if ( this.dataset != null && section >= 0 && section < this.dataset.getItemCount() ) {
            key = this.dataset.getKey ( section );
        }
        if ( key == null ) {
            key = new Integer ( section );
        }
        return key;
    }
    public Paint getSectionPaint ( final Comparable key ) {
        return this.sectionPaintMap.getPaint ( key );
    }
    public void setSectionPaint ( final Comparable key, final Paint paint ) {
        this.sectionPaintMap.put ( key, paint );
        this.fireChangeEvent();
    }
    public void clearSectionPaints ( final boolean notify ) {
        this.sectionPaintMap.clear();
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public Paint getBaseSectionPaint() {
        return this.baseSectionPaint;
    }
    public void setBaseSectionPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.baseSectionPaint = paint;
        this.fireChangeEvent();
    }
    public boolean getAutoPopulateSectionPaint() {
        return this.autoPopulateSectionPaint;
    }
    public void setAutoPopulateSectionPaint ( final boolean auto ) {
        this.autoPopulateSectionPaint = auto;
        this.fireChangeEvent();
    }
    public boolean getSectionOutlinesVisible() {
        return this.sectionOutlinesVisible;
    }
    public void setSectionOutlinesVisible ( final boolean visible ) {
        this.sectionOutlinesVisible = visible;
        this.fireChangeEvent();
    }
    protected Paint lookupSectionOutlinePaint ( final Comparable key ) {
        return this.lookupSectionOutlinePaint ( key, this.getAutoPopulateSectionOutlinePaint() );
    }
    protected Paint lookupSectionOutlinePaint ( final Comparable key, final boolean autoPopulate ) {
        Paint result = this.getSectionOutlinePaint();
        if ( result != null ) {
            return result;
        }
        result = this.sectionOutlinePaintMap.getPaint ( key );
        if ( result != null ) {
            return result;
        }
        if ( autoPopulate ) {
            final DrawingSupplier ds = this.getDrawingSupplier();
            if ( ds != null ) {
                result = ds.getNextOutlinePaint();
                this.sectionOutlinePaintMap.put ( key, result );
            } else {
                result = this.baseSectionOutlinePaint;
            }
        } else {
            result = this.baseSectionOutlinePaint;
        }
        return result;
    }
    public Paint getSectionOutlinePaint ( final Comparable key ) {
        return this.sectionOutlinePaintMap.getPaint ( key );
    }
    public void setSectionOutlinePaint ( final Comparable key, final Paint paint ) {
        this.sectionOutlinePaintMap.put ( key, paint );
        this.fireChangeEvent();
    }
    public void clearSectionOutlinePaints ( final boolean notify ) {
        this.sectionOutlinePaintMap.clear();
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public Paint getBaseSectionOutlinePaint() {
        return this.baseSectionOutlinePaint;
    }
    public void setBaseSectionOutlinePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.baseSectionOutlinePaint = paint;
        this.fireChangeEvent();
    }
    public boolean getAutoPopulateSectionOutlinePaint() {
        return this.autoPopulateSectionOutlinePaint;
    }
    public void setAutoPopulateSectionOutlinePaint ( final boolean auto ) {
        this.autoPopulateSectionOutlinePaint = auto;
        this.fireChangeEvent();
    }
    protected Stroke lookupSectionOutlineStroke ( final Comparable key ) {
        return this.lookupSectionOutlineStroke ( key, this.getAutoPopulateSectionOutlineStroke() );
    }
    protected Stroke lookupSectionOutlineStroke ( final Comparable key, final boolean autoPopulate ) {
        Stroke result = this.getSectionOutlineStroke();
        if ( result != null ) {
            return result;
        }
        result = this.sectionOutlineStrokeMap.getStroke ( key );
        if ( result != null ) {
            return result;
        }
        if ( autoPopulate ) {
            final DrawingSupplier ds = this.getDrawingSupplier();
            if ( ds != null ) {
                result = ds.getNextOutlineStroke();
                this.sectionOutlineStrokeMap.put ( key, result );
            } else {
                result = this.baseSectionOutlineStroke;
            }
        } else {
            result = this.baseSectionOutlineStroke;
        }
        return result;
    }
    public Stroke getSectionOutlineStroke ( final Comparable key ) {
        return this.sectionOutlineStrokeMap.getStroke ( key );
    }
    public void setSectionOutlineStroke ( final Comparable key, final Stroke stroke ) {
        this.sectionOutlineStrokeMap.put ( key, stroke );
        this.fireChangeEvent();
    }
    public void clearSectionOutlineStrokes ( final boolean notify ) {
        this.sectionOutlineStrokeMap.clear();
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public Stroke getBaseSectionOutlineStroke() {
        return this.baseSectionOutlineStroke;
    }
    public void setBaseSectionOutlineStroke ( final Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.baseSectionOutlineStroke = stroke;
        this.fireChangeEvent();
    }
    public boolean getAutoPopulateSectionOutlineStroke() {
        return this.autoPopulateSectionOutlineStroke;
    }
    public void setAutoPopulateSectionOutlineStroke ( final boolean auto ) {
        this.autoPopulateSectionOutlineStroke = auto;
        this.fireChangeEvent();
    }
    public Paint getShadowPaint() {
        return this.shadowPaint;
    }
    public void setShadowPaint ( final Paint paint ) {
        this.shadowPaint = paint;
        this.fireChangeEvent();
    }
    public double getShadowXOffset() {
        return this.shadowXOffset;
    }
    public void setShadowXOffset ( final double offset ) {
        this.shadowXOffset = offset;
        this.fireChangeEvent();
    }
    public double getShadowYOffset() {
        return this.shadowYOffset;
    }
    public void setShadowYOffset ( final double offset ) {
        this.shadowYOffset = offset;
        this.fireChangeEvent();
    }
    public double getExplodePercent ( final Comparable key ) {
        double result = 0.0;
        if ( this.explodePercentages != null ) {
            final Number percent = this.explodePercentages.get ( key );
            if ( percent != null ) {
                result = percent.doubleValue();
            }
        }
        return result;
    }
    public void setExplodePercent ( final Comparable key, final double percent ) {
        ParamChecks.nullNotPermitted ( key, "key" );
        if ( this.explodePercentages == null ) {
            this.explodePercentages = new TreeMap();
        }
        this.explodePercentages.put ( key, new Double ( percent ) );
        this.fireChangeEvent();
    }
    public double getMaximumExplodePercent() {
        if ( this.dataset == null ) {
            return 0.0;
        }
        double result = 0.0;
        for ( final Comparable key : this.dataset.getKeys() ) {
            final Number explode = this.explodePercentages.get ( key );
            if ( explode != null ) {
                result = Math.max ( result, explode.doubleValue() );
            }
        }
        return result;
    }
    public PieSectionLabelGenerator getLabelGenerator() {
        return this.labelGenerator;
    }
    public void setLabelGenerator ( final PieSectionLabelGenerator generator ) {
        this.labelGenerator = generator;
        this.fireChangeEvent();
    }
    public double getLabelGap() {
        return this.labelGap;
    }
    public void setLabelGap ( final double gap ) {
        this.labelGap = gap;
        this.fireChangeEvent();
    }
    public double getMaximumLabelWidth() {
        return this.maximumLabelWidth;
    }
    public void setMaximumLabelWidth ( final double width ) {
        this.maximumLabelWidth = width;
        this.fireChangeEvent();
    }
    public boolean getLabelLinksVisible() {
        return this.labelLinksVisible;
    }
    public void setLabelLinksVisible ( final boolean visible ) {
        this.labelLinksVisible = visible;
        this.fireChangeEvent();
    }
    public PieLabelLinkStyle getLabelLinkStyle() {
        return this.labelLinkStyle;
    }
    public void setLabelLinkStyle ( final PieLabelLinkStyle style ) {
        ParamChecks.nullNotPermitted ( style, "style" );
        this.labelLinkStyle = style;
        this.fireChangeEvent();
    }
    public double getLabelLinkMargin() {
        return this.labelLinkMargin;
    }
    public void setLabelLinkMargin ( final double margin ) {
        this.labelLinkMargin = margin;
        this.fireChangeEvent();
    }
    public Paint getLabelLinkPaint() {
        return this.labelLinkPaint;
    }
    public void setLabelLinkPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.labelLinkPaint = paint;
        this.fireChangeEvent();
    }
    public Stroke getLabelLinkStroke() {
        return this.labelLinkStroke;
    }
    public void setLabelLinkStroke ( final Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.labelLinkStroke = stroke;
        this.fireChangeEvent();
    }
    protected double getLabelLinkDepth() {
        return 0.1;
    }
    public Font getLabelFont() {
        return this.labelFont;
    }
    public void setLabelFont ( final Font font ) {
        ParamChecks.nullNotPermitted ( font, "font" );
        this.labelFont = font;
        this.fireChangeEvent();
    }
    public Paint getLabelPaint() {
        return this.labelPaint;
    }
    public void setLabelPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.labelPaint = paint;
        this.fireChangeEvent();
    }
    public Paint getLabelBackgroundPaint() {
        return this.labelBackgroundPaint;
    }
    public void setLabelBackgroundPaint ( final Paint paint ) {
        this.labelBackgroundPaint = paint;
        this.fireChangeEvent();
    }
    public Paint getLabelOutlinePaint() {
        return this.labelOutlinePaint;
    }
    public void setLabelOutlinePaint ( final Paint paint ) {
        this.labelOutlinePaint = paint;
        this.fireChangeEvent();
    }
    public Stroke getLabelOutlineStroke() {
        return this.labelOutlineStroke;
    }
    public void setLabelOutlineStroke ( final Stroke stroke ) {
        this.labelOutlineStroke = stroke;
        this.fireChangeEvent();
    }
    public Paint getLabelShadowPaint() {
        return this.labelShadowPaint;
    }
    public void setLabelShadowPaint ( final Paint paint ) {
        this.labelShadowPaint = paint;
        this.fireChangeEvent();
    }
    public RectangleInsets getLabelPadding() {
        return this.labelPadding;
    }
    public void setLabelPadding ( final RectangleInsets padding ) {
        ParamChecks.nullNotPermitted ( padding, "padding" );
        this.labelPadding = padding;
        this.fireChangeEvent();
    }
    public boolean getSimpleLabels() {
        return this.simpleLabels;
    }
    public void setSimpleLabels ( final boolean simple ) {
        this.simpleLabels = simple;
        this.fireChangeEvent();
    }
    public RectangleInsets getSimpleLabelOffset() {
        return this.simpleLabelOffset;
    }
    public void setSimpleLabelOffset ( final RectangleInsets offset ) {
        ParamChecks.nullNotPermitted ( offset, "offset" );
        this.simpleLabelOffset = offset;
        this.fireChangeEvent();
    }
    public AbstractPieLabelDistributor getLabelDistributor() {
        return this.labelDistributor;
    }
    public void setLabelDistributor ( final AbstractPieLabelDistributor distributor ) {
        ParamChecks.nullNotPermitted ( distributor, "distributor" );
        this.labelDistributor = distributor;
        this.fireChangeEvent();
    }
    public PieToolTipGenerator getToolTipGenerator() {
        return this.toolTipGenerator;
    }
    public void setToolTipGenerator ( final PieToolTipGenerator generator ) {
        this.toolTipGenerator = generator;
        this.fireChangeEvent();
    }
    public PieURLGenerator getURLGenerator() {
        return this.urlGenerator;
    }
    public void setURLGenerator ( final PieURLGenerator generator ) {
        this.urlGenerator = generator;
        this.fireChangeEvent();
    }
    public double getMinimumArcAngleToDraw() {
        return this.minimumArcAngleToDraw;
    }
    public void setMinimumArcAngleToDraw ( final double angle ) {
        this.minimumArcAngleToDraw = angle;
    }
    public Shape getLegendItemShape() {
        return this.legendItemShape;
    }
    public void setLegendItemShape ( final Shape shape ) {
        ParamChecks.nullNotPermitted ( shape, "shape" );
        this.legendItemShape = shape;
        this.fireChangeEvent();
    }
    public PieSectionLabelGenerator getLegendLabelGenerator() {
        return this.legendLabelGenerator;
    }
    public void setLegendLabelGenerator ( final PieSectionLabelGenerator generator ) {
        ParamChecks.nullNotPermitted ( generator, "generator" );
        this.legendLabelGenerator = generator;
        this.fireChangeEvent();
    }
    public PieSectionLabelGenerator getLegendLabelToolTipGenerator() {
        return this.legendLabelToolTipGenerator;
    }
    public void setLegendLabelToolTipGenerator ( final PieSectionLabelGenerator generator ) {
        this.legendLabelToolTipGenerator = generator;
        this.fireChangeEvent();
    }
    public PieURLGenerator getLegendLabelURLGenerator() {
        return this.legendLabelURLGenerator;
    }
    public void setLegendLabelURLGenerator ( final PieURLGenerator generator ) {
        this.legendLabelURLGenerator = generator;
        this.fireChangeEvent();
    }
    public ShadowGenerator getShadowGenerator() {
        return this.shadowGenerator;
    }
    public void setShadowGenerator ( final ShadowGenerator generator ) {
        this.shadowGenerator = generator;
        this.fireChangeEvent();
    }
    public void handleMouseWheelRotation ( final int rotateClicks ) {
        this.setStartAngle ( this.startAngle + rotateClicks * 4.0 );
    }
    public PiePlotState initialise ( final Graphics2D g2, final Rectangle2D plotArea, final PiePlot plot, final Integer index, final PlotRenderingInfo info ) {
        final PiePlotState state = new PiePlotState ( info );
        state.setPassesRequired ( 2 );
        if ( this.dataset != null ) {
            state.setTotal ( DatasetUtilities.calculatePieDatasetTotal ( plot.getDataset() ) );
        }
        state.setLatestAngle ( plot.getStartAngle() );
        return state;
    }
    @Override
    public void draw ( Graphics2D g2, final Rectangle2D area, final Point2D anchor, final PlotState parentState, final PlotRenderingInfo info ) {
        final RectangleInsets insets = this.getInsets();
        insets.trim ( area );
        if ( info != null ) {
            info.setPlotArea ( area );
            info.setDataArea ( area );
        }
        this.drawBackground ( g2, area );
        this.drawOutline ( g2, area );
        final Shape savedClip = g2.getClip();
        g2.clip ( area );
        final Composite originalComposite = g2.getComposite();
        g2.setComposite ( AlphaComposite.getInstance ( 3, this.getForegroundAlpha() ) );
        if ( !DatasetUtilities.isEmptyOrNull ( this.dataset ) ) {
            final Graphics2D savedG2 = g2;
            final boolean suppressShadow = Boolean.TRUE.equals ( g2.getRenderingHint ( JFreeChart.KEY_SUPPRESS_SHADOW_GENERATION ) );
            BufferedImage dataImage = null;
            if ( this.shadowGenerator != null && !suppressShadow ) {
                dataImage = new BufferedImage ( ( int ) area.getWidth(), ( int ) area.getHeight(), 2 );
                g2 = dataImage.createGraphics();
                g2.translate ( -area.getX(), -area.getY() );
                g2.setRenderingHints ( savedG2.getRenderingHints() );
            }
            this.drawPie ( g2, area, info );
            if ( this.shadowGenerator != null && !suppressShadow ) {
                final BufferedImage shadowImage = this.shadowGenerator.createDropShadow ( dataImage );
                g2 = savedG2;
                g2.drawImage ( shadowImage, ( int ) area.getX() + this.shadowGenerator.calculateOffsetX(), ( int ) area.getY() + this.shadowGenerator.calculateOffsetY(), null );
                g2.drawImage ( dataImage, ( int ) area.getX(), ( int ) area.getY(), null );
            }
        } else {
            this.drawNoDataMessage ( g2, area );
        }
        g2.setClip ( savedClip );
        g2.setComposite ( originalComposite );
        this.drawOutline ( g2, area );
    }
    protected void drawPie ( final Graphics2D g2, final Rectangle2D plotArea, final PlotRenderingInfo info ) {
        final PiePlotState state = this.initialise ( g2, plotArea, this, null, info );
        double labelReserve = 0.0;
        if ( this.labelGenerator != null && !this.simpleLabels ) {
            labelReserve = this.labelGap + this.maximumLabelWidth;
        }
        final double gapHorizontal = plotArea.getWidth() * labelReserve * 2.0;
        final double gapVertical = plotArea.getHeight() * this.interiorGap * 2.0;
        double linkX = plotArea.getX() + gapHorizontal / 2.0;
        double linkY = plotArea.getY() + gapVertical / 2.0;
        double linkW = plotArea.getWidth() - gapHorizontal;
        double linkH = plotArea.getHeight() - gapVertical;
        if ( this.circular ) {
            final double min = Math.min ( linkW, linkH ) / 2.0;
            linkX = ( linkX + linkX + linkW ) / 2.0 - min;
            linkY = ( linkY + linkY + linkH ) / 2.0 - min;
            linkW = 2.0 * min;
            linkH = 2.0 * min;
        }
        final Rectangle2D linkArea = new Rectangle2D.Double ( linkX, linkY, linkW, linkH );
        state.setLinkArea ( linkArea );
        double lm = 0.0;
        if ( !this.simpleLabels ) {
            lm = this.labelLinkMargin;
        }
        final double hh = linkArea.getWidth() * lm * 2.0;
        final double vv = linkArea.getHeight() * lm * 2.0;
        final Rectangle2D explodeArea = new Rectangle2D.Double ( linkX + hh / 2.0, linkY + vv / 2.0, linkW - hh, linkH - vv );
        state.setExplodedPieArea ( explodeArea );
        final double maximumExplodePercent = this.getMaximumExplodePercent();
        final double percent = maximumExplodePercent / ( 1.0 + maximumExplodePercent );
        final double h1 = explodeArea.getWidth() * percent;
        final double v1 = explodeArea.getHeight() * percent;
        final Rectangle2D pieArea = new Rectangle2D.Double ( explodeArea.getX() + h1 / 2.0, explodeArea.getY() + v1 / 2.0, explodeArea.getWidth() - h1, explodeArea.getHeight() - v1 );
        state.setPieArea ( pieArea );
        state.setPieCenterX ( pieArea.getCenterX() );
        state.setPieCenterY ( pieArea.getCenterY() );
        state.setPieWRadius ( pieArea.getWidth() / 2.0 );
        state.setPieHRadius ( pieArea.getHeight() / 2.0 );
        if ( this.dataset != null && this.dataset.getKeys().size() > 0 ) {
            final List keys = this.dataset.getKeys();
            final double totalValue = DatasetUtilities.calculatePieDatasetTotal ( this.dataset );
            for ( int passesRequired = state.getPassesRequired(), pass = 0; pass < passesRequired; ++pass ) {
                double runningTotal = 0.0;
                for ( int section = 0; section < keys.size(); ++section ) {
                    final Number n = this.dataset.getValue ( section );
                    if ( n != null ) {
                        final double value = n.doubleValue();
                        if ( value > 0.0 ) {
                            runningTotal += value;
                            this.drawItem ( g2, section, explodeArea, state, pass );
                        }
                    }
                }
            }
            if ( this.simpleLabels ) {
                this.drawSimpleLabels ( g2, keys, totalValue, plotArea, linkArea, state );
            } else {
                this.drawLabels ( g2, keys, totalValue, plotArea, linkArea, state );
            }
        } else {
            this.drawNoDataMessage ( g2, plotArea );
        }
    }
    protected void drawItem ( final Graphics2D g2, final int section, final Rectangle2D dataArea, final PiePlotState state, final int currentPass ) {
        final Number n = this.dataset.getValue ( section );
        if ( n == null ) {
            return;
        }
        final double value = n.doubleValue();
        double angle1 = 0.0;
        double angle2 = 0.0;
        if ( this.direction == Rotation.CLOCKWISE ) {
            angle1 = state.getLatestAngle();
            angle2 = angle1 - value / state.getTotal() * 360.0;
        } else {
            if ( this.direction != Rotation.ANTICLOCKWISE ) {
                throw new IllegalStateException ( "Rotation type not recognised." );
            }
            angle1 = state.getLatestAngle();
            angle2 = angle1 + value / state.getTotal() * 360.0;
        }
        final double angle3 = angle2 - angle1;
        if ( Math.abs ( angle3 ) > this.getMinimumArcAngleToDraw() ) {
            double ep = 0.0;
            final double mep = this.getMaximumExplodePercent();
            if ( mep > 0.0 ) {
                ep = this.getExplodePercent ( section ) / mep;
            }
            final Rectangle2D arcBounds = this.getArcBounds ( state.getPieArea(), state.getExplodedPieArea(), angle1, angle3, ep );
            final Arc2D.Double arc = new Arc2D.Double ( arcBounds, angle1, angle3, 2 );
            if ( currentPass == 0 ) {
                if ( this.shadowPaint != null && this.shadowGenerator == null ) {
                    final Shape shadowArc = ShapeUtilities.createTranslatedShape ( ( Shape ) arc, ( double ) ( float ) this.shadowXOffset, ( double ) ( float ) this.shadowYOffset );
                    g2.setPaint ( this.shadowPaint );
                    g2.fill ( shadowArc );
                }
            } else if ( currentPass == 1 ) {
                final Comparable key = this.getSectionKey ( section );
                final Paint paint = this.lookupSectionPaint ( key, state );
                g2.setPaint ( paint );
                g2.fill ( arc );
                final Paint outlinePaint = this.lookupSectionOutlinePaint ( key );
                final Stroke outlineStroke = this.lookupSectionOutlineStroke ( key );
                if ( this.sectionOutlinesVisible ) {
                    g2.setPaint ( outlinePaint );
                    g2.setStroke ( outlineStroke );
                    g2.draw ( arc );
                }
                if ( state.getInfo() != null ) {
                    final EntityCollection entities = state.getEntityCollection();
                    if ( entities != null ) {
                        String tip = null;
                        if ( this.toolTipGenerator != null ) {
                            tip = this.toolTipGenerator.generateToolTip ( this.dataset, key );
                        }
                        String url = null;
                        if ( this.urlGenerator != null ) {
                            url = this.urlGenerator.generateURL ( this.dataset, key, this.pieIndex );
                        }
                        final PieSectionEntity entity = new PieSectionEntity ( arc, this.dataset, this.pieIndex, section, key, tip, url );
                        entities.add ( entity );
                    }
                }
            }
        }
        state.setLatestAngle ( angle2 );
    }
    protected void drawSimpleLabels ( final Graphics2D g2, final List keys, final double totalValue, final Rectangle2D plotArea, final Rectangle2D pieArea, final PiePlotState state ) {
        final Composite originalComposite = g2.getComposite();
        g2.setComposite ( AlphaComposite.getInstance ( 3, 1.0f ) );
        final Rectangle2D labelsArea = this.simpleLabelOffset.createInsetRectangle ( pieArea );
        double runningTotal = 0.0;
        for ( final Comparable key : keys ) {
            double v = 0.0;
            final Number n = this.getDataset().getValue ( key );
            boolean include;
            if ( n == null ) {
                include = !this.getIgnoreNullValues();
            } else {
                v = n.doubleValue();
                include = ( this.getIgnoreZeroValues() ? ( v > 0.0 ) : ( v >= 0.0 ) );
            }
            if ( include ) {
                runningTotal += v;
                final double mid = this.getStartAngle() + this.getDirection().getFactor() * ( ( runningTotal - v / 2.0 ) * 360.0 ) / totalValue;
                final Arc2D arc = new Arc2D.Double ( labelsArea, this.getStartAngle(), mid - this.getStartAngle(), 0 );
                final int x = ( int ) arc.getEndPoint().getX();
                final int y = ( int ) arc.getEndPoint().getY();
                final PieSectionLabelGenerator myLabelGenerator = this.getLabelGenerator();
                if ( myLabelGenerator == null ) {
                    continue;
                }
                final String label = myLabelGenerator.generateSectionLabel ( this.dataset, key );
                if ( label == null ) {
                    continue;
                }
                g2.setFont ( this.labelFont );
                final FontMetrics fm = g2.getFontMetrics();
                final Rectangle2D bounds = TextUtilities.getTextBounds ( label, g2, fm );
                final Rectangle2D out = this.labelPadding.createOutsetRectangle ( bounds );
                final Shape bg = ShapeUtilities.createTranslatedShape ( ( Shape ) out, x - bounds.getCenterX(), y - bounds.getCenterY() );
                if ( this.labelShadowPaint != null && this.shadowGenerator == null ) {
                    final Shape shadow = ShapeUtilities.createTranslatedShape ( bg, this.shadowXOffset, this.shadowYOffset );
                    g2.setPaint ( this.labelShadowPaint );
                    g2.fill ( shadow );
                }
                if ( this.labelBackgroundPaint != null ) {
                    g2.setPaint ( this.labelBackgroundPaint );
                    g2.fill ( bg );
                }
                if ( this.labelOutlinePaint != null && this.labelOutlineStroke != null ) {
                    g2.setPaint ( this.labelOutlinePaint );
                    g2.setStroke ( this.labelOutlineStroke );
                    g2.draw ( bg );
                }
                g2.setPaint ( this.labelPaint );
                g2.setFont ( this.labelFont );
                TextUtilities.drawAlignedString ( label, g2, ( float ) x, ( float ) y, TextAnchor.CENTER );
            }
        }
        g2.setComposite ( originalComposite );
    }
    protected void drawLabels ( final Graphics2D g2, final List keys, final double totalValue, final Rectangle2D plotArea, final Rectangle2D linkArea, final PiePlotState state ) {
        final Composite originalComposite = g2.getComposite();
        g2.setComposite ( AlphaComposite.getInstance ( 3, 1.0f ) );
        final DefaultKeyedValues leftKeys = new DefaultKeyedValues();
        final DefaultKeyedValues rightKeys = new DefaultKeyedValues();
        double runningTotal = 0.0;
        for ( final Comparable key : keys ) {
            double v = 0.0;
            final Number n = this.dataset.getValue ( key );
            boolean include;
            if ( n == null ) {
                include = !this.ignoreNullValues;
            } else {
                v = n.doubleValue();
                include = ( this.ignoreZeroValues ? ( v > 0.0 ) : ( v >= 0.0 ) );
            }
            if ( include ) {
                runningTotal += v;
                final double mid = this.startAngle + this.direction.getFactor() * ( ( runningTotal - v / 2.0 ) * 360.0 ) / totalValue;
                if ( Math.cos ( Math.toRadians ( mid ) ) < 0.0 ) {
                    leftKeys.addValue ( key, new Double ( mid ) );
                } else {
                    rightKeys.addValue ( key, new Double ( mid ) );
                }
            }
        }
        g2.setFont ( this.getLabelFont() );
        final double marginX = plotArea.getX();
        final double gap = plotArea.getWidth() * this.labelGap;
        final double ww = linkArea.getX() - gap - marginX;
        final float labelWidth = ( float ) this.labelPadding.trimWidth ( ww );
        if ( this.labelGenerator != null ) {
            this.drawLeftLabels ( leftKeys, g2, plotArea, linkArea, labelWidth, state );
            this.drawRightLabels ( rightKeys, g2, plotArea, linkArea, labelWidth, state );
        }
        g2.setComposite ( originalComposite );
    }
    protected void drawLeftLabels ( final KeyedValues leftKeys, final Graphics2D g2, final Rectangle2D plotArea, final Rectangle2D linkArea, final float maxLabelWidth, final PiePlotState state ) {
        this.labelDistributor.clear();
        final double lGap = plotArea.getWidth() * this.labelGap;
        final double verticalLinkRadius = state.getLinkArea().getHeight() / 2.0;
        for ( int i = 0; i < leftKeys.getItemCount(); ++i ) {
            final String label = this.labelGenerator.generateSectionLabel ( this.dataset, leftKeys.getKey ( i ) );
            if ( label != null ) {
                final TextBlock block = TextUtilities.createTextBlock ( label, this.labelFont, this.labelPaint, maxLabelWidth, ( TextMeasurer ) new G2TextMeasurer ( g2 ) );
                final TextBox labelBox = new TextBox ( block );
                labelBox.setBackgroundPaint ( this.labelBackgroundPaint );
                labelBox.setOutlinePaint ( this.labelOutlinePaint );
                labelBox.setOutlineStroke ( this.labelOutlineStroke );
                if ( this.shadowGenerator == null ) {
                    labelBox.setShadowPaint ( this.labelShadowPaint );
                } else {
                    labelBox.setShadowPaint ( ( Paint ) null );
                }
                labelBox.setInteriorGap ( this.labelPadding );
                final double theta = Math.toRadians ( leftKeys.getValue ( i ).doubleValue() );
                final double baseY = state.getPieCenterY() - Math.sin ( theta ) * verticalLinkRadius;
                final double hh = labelBox.getHeight ( g2 );
                this.labelDistributor.addPieLabelRecord ( new PieLabelRecord ( leftKeys.getKey ( i ), theta, baseY, labelBox, hh, lGap / 2.0 + lGap / 2.0 * -Math.cos ( theta ), 1.0 - this.getLabelLinkDepth() + this.getExplodePercent ( leftKeys.getKey ( i ) ) ) );
            }
        }
        final double hh2 = plotArea.getHeight();
        final double gap = hh2 * this.getInteriorGap();
        this.labelDistributor.distributeLabels ( plotArea.getMinY() + gap, hh2 - 2.0 * gap );
        for ( int j = 0; j < this.labelDistributor.getItemCount(); ++j ) {
            this.drawLeftLabel ( g2, state, this.labelDistributor.getPieLabelRecord ( j ) );
        }
    }
    protected void drawRightLabels ( final KeyedValues keys, final Graphics2D g2, final Rectangle2D plotArea, final Rectangle2D linkArea, final float maxLabelWidth, final PiePlotState state ) {
        this.labelDistributor.clear();
        final double lGap = plotArea.getWidth() * this.labelGap;
        final double verticalLinkRadius = state.getLinkArea().getHeight() / 2.0;
        for ( int i = 0; i < keys.getItemCount(); ++i ) {
            final String label = this.labelGenerator.generateSectionLabel ( this.dataset, keys.getKey ( i ) );
            if ( label != null ) {
                final TextBlock block = TextUtilities.createTextBlock ( label, this.labelFont, this.labelPaint, maxLabelWidth, ( TextMeasurer ) new G2TextMeasurer ( g2 ) );
                final TextBox labelBox = new TextBox ( block );
                labelBox.setBackgroundPaint ( this.labelBackgroundPaint );
                labelBox.setOutlinePaint ( this.labelOutlinePaint );
                labelBox.setOutlineStroke ( this.labelOutlineStroke );
                if ( this.shadowGenerator == null ) {
                    labelBox.setShadowPaint ( this.labelShadowPaint );
                } else {
                    labelBox.setShadowPaint ( ( Paint ) null );
                }
                labelBox.setInteriorGap ( this.labelPadding );
                final double theta = Math.toRadians ( keys.getValue ( i ).doubleValue() );
                final double baseY = state.getPieCenterY() - Math.sin ( theta ) * verticalLinkRadius;
                final double hh = labelBox.getHeight ( g2 );
                this.labelDistributor.addPieLabelRecord ( new PieLabelRecord ( keys.getKey ( i ), theta, baseY, labelBox, hh, lGap / 2.0 + lGap / 2.0 * Math.cos ( theta ), 1.0 - this.getLabelLinkDepth() + this.getExplodePercent ( keys.getKey ( i ) ) ) );
            }
        }
        final double hh2 = plotArea.getHeight();
        final double gap = 0.0;
        this.labelDistributor.distributeLabels ( plotArea.getMinY() + gap, hh2 - 2.0 * gap );
        for ( int j = 0; j < this.labelDistributor.getItemCount(); ++j ) {
            this.drawRightLabel ( g2, state, this.labelDistributor.getPieLabelRecord ( j ) );
        }
    }
    @Override
    public LegendItemCollection getLegendItems() {
        final LegendItemCollection result = new LegendItemCollection();
        if ( this.dataset == null ) {
            return result;
        }
        final List keys = this.dataset.getKeys();
        int section = 0;
        final Shape shape = this.getLegendItemShape();
        for ( final Comparable key : keys ) {
            final Number n = this.dataset.getValue ( key );
            boolean include;
            if ( n == null ) {
                include = !this.ignoreNullValues;
            } else {
                final double v = n.doubleValue();
                if ( v == 0.0 ) {
                    include = !this.ignoreZeroValues;
                } else {
                    include = ( v > 0.0 );
                }
            }
            if ( include ) {
                final String label = this.legendLabelGenerator.generateSectionLabel ( this.dataset, key );
                if ( label != null ) {
                    final String description = label;
                    String toolTipText = null;
                    if ( this.legendLabelToolTipGenerator != null ) {
                        toolTipText = this.legendLabelToolTipGenerator.generateSectionLabel ( this.dataset, key );
                    }
                    String urlText = null;
                    if ( this.legendLabelURLGenerator != null ) {
                        urlText = this.legendLabelURLGenerator.generateURL ( this.dataset, key, this.pieIndex );
                    }
                    final Paint paint = this.lookupSectionPaint ( key );
                    final Paint outlinePaint = this.lookupSectionOutlinePaint ( key );
                    final Stroke outlineStroke = this.lookupSectionOutlineStroke ( key );
                    final LegendItem item = new LegendItem ( label, description, toolTipText, urlText, true, shape, true, paint, true, outlinePaint, outlineStroke, false, new Line2D.Float(), new BasicStroke(), Color.black );
                    item.setDataset ( this.getDataset() );
                    item.setSeriesIndex ( this.dataset.getIndex ( key ) );
                    item.setSeriesKey ( key );
                    result.add ( item );
                }
                ++section;
            } else {
                ++section;
            }
        }
        return result;
    }
    @Override
    public String getPlotType() {
        return PiePlot.localizationResources.getString ( "Pie_Plot" );
    }
    protected Rectangle2D getArcBounds ( final Rectangle2D unexploded, final Rectangle2D exploded, final double angle, final double extent, final double explodePercent ) {
        if ( explodePercent == 0.0 ) {
            return unexploded;
        }
        final Arc2D arc1 = new Arc2D.Double ( unexploded, angle, extent / 2.0, 0 );
        final Point2D point1 = arc1.getEndPoint();
        final Arc2D.Double arc2 = new Arc2D.Double ( exploded, angle, extent / 2.0, 0 );
        final Point2D point2 = arc2.getEndPoint();
        final double deltaX = ( point1.getX() - point2.getX() ) * explodePercent;
        final double deltaY = ( point1.getY() - point2.getY() ) * explodePercent;
        return new Rectangle2D.Double ( unexploded.getX() - deltaX, unexploded.getY() - deltaY, unexploded.getWidth(), unexploded.getHeight() );
    }
    protected void drawLeftLabel ( final Graphics2D g2, final PiePlotState state, final PieLabelRecord record ) {
        final double anchorX = state.getLinkArea().getMinX();
        final double targetX = anchorX - record.getGap();
        final double targetY = record.getAllocatedY();
        if ( this.labelLinksVisible ) {
            final double theta = record.getAngle();
            final double linkX = state.getPieCenterX() + Math.cos ( theta ) * state.getPieWRadius() * record.getLinkPercent();
            final double linkY = state.getPieCenterY() - Math.sin ( theta ) * state.getPieHRadius() * record.getLinkPercent();
            final double elbowX = state.getPieCenterX() + Math.cos ( theta ) * state.getLinkArea().getWidth() / 2.0;
            final double anchorY;
            final double elbowY = anchorY = state.getPieCenterY() - Math.sin ( theta ) * state.getLinkArea().getHeight() / 2.0;
            g2.setPaint ( this.labelLinkPaint );
            g2.setStroke ( this.labelLinkStroke );
            final PieLabelLinkStyle style = this.getLabelLinkStyle();
            if ( style.equals ( PieLabelLinkStyle.STANDARD ) ) {
                g2.draw ( new Line2D.Double ( linkX, linkY, elbowX, elbowY ) );
                g2.draw ( new Line2D.Double ( anchorX, anchorY, elbowX, elbowY ) );
                g2.draw ( new Line2D.Double ( anchorX, anchorY, targetX, targetY ) );
            } else if ( style.equals ( PieLabelLinkStyle.QUAD_CURVE ) ) {
                final QuadCurve2D q = new QuadCurve2D.Float();
                q.setCurve ( targetX, targetY, anchorX, anchorY, elbowX, elbowY );
                g2.draw ( q );
                g2.draw ( new Line2D.Double ( elbowX, elbowY, linkX, linkY ) );
            } else if ( style.equals ( PieLabelLinkStyle.CUBIC_CURVE ) ) {
                final CubicCurve2D c = new CubicCurve2D.Float();
                c.setCurve ( targetX, targetY, anchorX, anchorY, elbowX, elbowY, linkX, linkY );
                g2.draw ( c );
            }
        }
        final TextBox tb = record.getLabel();
        tb.draw ( g2, ( float ) targetX, ( float ) targetY, RectangleAnchor.RIGHT );
    }
    protected void drawRightLabel ( final Graphics2D g2, final PiePlotState state, final PieLabelRecord record ) {
        final double anchorX = state.getLinkArea().getMaxX();
        final double targetX = anchorX + record.getGap();
        final double targetY = record.getAllocatedY();
        if ( this.labelLinksVisible ) {
            final double theta = record.getAngle();
            final double linkX = state.getPieCenterX() + Math.cos ( theta ) * state.getPieWRadius() * record.getLinkPercent();
            final double linkY = state.getPieCenterY() - Math.sin ( theta ) * state.getPieHRadius() * record.getLinkPercent();
            final double elbowX = state.getPieCenterX() + Math.cos ( theta ) * state.getLinkArea().getWidth() / 2.0;
            final double anchorY;
            final double elbowY = anchorY = state.getPieCenterY() - Math.sin ( theta ) * state.getLinkArea().getHeight() / 2.0;
            g2.setPaint ( this.labelLinkPaint );
            g2.setStroke ( this.labelLinkStroke );
            final PieLabelLinkStyle style = this.getLabelLinkStyle();
            if ( style.equals ( PieLabelLinkStyle.STANDARD ) ) {
                g2.draw ( new Line2D.Double ( linkX, linkY, elbowX, elbowY ) );
                g2.draw ( new Line2D.Double ( anchorX, anchorY, elbowX, elbowY ) );
                g2.draw ( new Line2D.Double ( anchorX, anchorY, targetX, targetY ) );
            } else if ( style.equals ( PieLabelLinkStyle.QUAD_CURVE ) ) {
                final QuadCurve2D q = new QuadCurve2D.Float();
                q.setCurve ( targetX, targetY, anchorX, anchorY, elbowX, elbowY );
                g2.draw ( q );
                g2.draw ( new Line2D.Double ( elbowX, elbowY, linkX, linkY ) );
            } else if ( style.equals ( PieLabelLinkStyle.CUBIC_CURVE ) ) {
                final CubicCurve2D c = new CubicCurve2D.Float();
                c.setCurve ( targetX, targetY, anchorX, anchorY, elbowX, elbowY, linkX, linkY );
                g2.draw ( c );
            }
        }
        final TextBox tb = record.getLabel();
        tb.draw ( g2, ( float ) targetX, ( float ) targetY, RectangleAnchor.LEFT );
    }
    protected Point2D getArcCenter ( final PiePlotState state, final Comparable key ) {
        Point2D center = new Point2D.Double ( state.getPieCenterX(), state.getPieCenterY() );
        double ep = this.getExplodePercent ( key );
        final double mep = this.getMaximumExplodePercent();
        if ( mep > 0.0 ) {
            ep /= mep;
        }
        if ( ep != 0.0 ) {
            final Rectangle2D pieArea = state.getPieArea();
            final Rectangle2D expPieArea = state.getExplodedPieArea();
            final Number n = this.dataset.getValue ( key );
            final double value = n.doubleValue();
            double angle1;
            double angle2;
            if ( this.direction == Rotation.CLOCKWISE ) {
                angle1 = state.getLatestAngle();
                angle2 = angle1 - value / state.getTotal() * 360.0;
            } else {
                if ( this.direction != Rotation.ANTICLOCKWISE ) {
                    throw new IllegalStateException ( "Rotation type not recognised." );
                }
                angle1 = state.getLatestAngle();
                angle2 = angle1 + value / state.getTotal() * 360.0;
            }
            final double angle3 = angle2 - angle1;
            final Arc2D arc1 = new Arc2D.Double ( pieArea, angle1, angle3 / 2.0, 0 );
            final Point2D point1 = arc1.getEndPoint();
            final Arc2D.Double arc2 = new Arc2D.Double ( expPieArea, angle1, angle3 / 2.0, 0 );
            final Point2D point2 = arc2.getEndPoint();
            final double deltaX = ( point1.getX() - point2.getX() ) * ep;
            final double deltaY = ( point1.getY() - point2.getY() ) * ep;
            center = new Point2D.Double ( state.getPieCenterX() - deltaX, state.getPieCenterY() - deltaY );
        }
        return center;
    }
    protected Paint lookupSectionPaint ( final Comparable key, final PiePlotState state ) {
        Paint paint = this.lookupSectionPaint ( key, this.getAutoPopulateSectionPaint() );
        if ( paint instanceof RadialGradientPaint ) {
            final RadialGradientPaint rgp = ( RadialGradientPaint ) paint;
            final Point2D center = this.getArcCenter ( state, key );
            final float radius = ( float ) Math.max ( state.getPieHRadius(), state.getPieWRadius() );
            final float[] fractions = rgp.getFractions();
            final Color[] colors = rgp.getColors();
            paint = new RadialGradientPaint ( center, radius, fractions, colors );
        }
        return paint;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof PiePlot ) ) {
            return false;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        final PiePlot that = ( PiePlot ) obj;
        return this.pieIndex == that.pieIndex && this.interiorGap == that.interiorGap && this.circular == that.circular && this.startAngle == that.startAngle && this.direction == that.direction && this.ignoreZeroValues == that.ignoreZeroValues && this.ignoreNullValues == that.ignoreNullValues && PaintUtilities.equal ( this.sectionPaint, that.sectionPaint ) && ObjectUtilities.equal ( ( Object ) this.sectionPaintMap, ( Object ) that.sectionPaintMap ) && PaintUtilities.equal ( this.baseSectionPaint, that.baseSectionPaint ) && this.sectionOutlinesVisible == that.sectionOutlinesVisible && PaintUtilities.equal ( this.sectionOutlinePaint, that.sectionOutlinePaint ) && ObjectUtilities.equal ( ( Object ) this.sectionOutlinePaintMap, ( Object ) that.sectionOutlinePaintMap ) && PaintUtilities.equal ( this.baseSectionOutlinePaint, that.baseSectionOutlinePaint ) && ObjectUtilities.equal ( ( Object ) this.sectionOutlineStroke, ( Object ) that.sectionOutlineStroke ) && ObjectUtilities.equal ( ( Object ) this.sectionOutlineStrokeMap, ( Object ) that.sectionOutlineStrokeMap ) && ObjectUtilities.equal ( ( Object ) this.baseSectionOutlineStroke, ( Object ) that.baseSectionOutlineStroke ) && PaintUtilities.equal ( this.shadowPaint, that.shadowPaint ) && this.shadowXOffset == that.shadowXOffset && this.shadowYOffset == that.shadowYOffset && ObjectUtilities.equal ( ( Object ) this.explodePercentages, ( Object ) that.explodePercentages ) && ObjectUtilities.equal ( ( Object ) this.labelGenerator, ( Object ) that.labelGenerator ) && ObjectUtilities.equal ( ( Object ) this.labelFont, ( Object ) that.labelFont ) && PaintUtilities.equal ( this.labelPaint, that.labelPaint ) && PaintUtilities.equal ( this.labelBackgroundPaint, that.labelBackgroundPaint ) && PaintUtilities.equal ( this.labelOutlinePaint, that.labelOutlinePaint ) && ObjectUtilities.equal ( ( Object ) this.labelOutlineStroke, ( Object ) that.labelOutlineStroke ) && PaintUtilities.equal ( this.labelShadowPaint, that.labelShadowPaint ) && this.simpleLabels == that.simpleLabels && this.simpleLabelOffset.equals ( ( Object ) that.simpleLabelOffset ) && this.labelPadding.equals ( ( Object ) that.labelPadding ) && this.maximumLabelWidth == that.maximumLabelWidth && this.labelGap == that.labelGap && this.labelLinkMargin == that.labelLinkMargin && this.labelLinksVisible == that.labelLinksVisible && this.labelLinkStyle.equals ( that.labelLinkStyle ) && PaintUtilities.equal ( this.labelLinkPaint, that.labelLinkPaint ) && ObjectUtilities.equal ( ( Object ) this.labelLinkStroke, ( Object ) that.labelLinkStroke ) && ObjectUtilities.equal ( ( Object ) this.toolTipGenerator, ( Object ) that.toolTipGenerator ) && ObjectUtilities.equal ( ( Object ) this.urlGenerator, ( Object ) that.urlGenerator ) && this.minimumArcAngleToDraw == that.minimumArcAngleToDraw && ShapeUtilities.equal ( this.legendItemShape, that.legendItemShape ) && ObjectUtilities.equal ( ( Object ) this.legendLabelGenerator, ( Object ) that.legendLabelGenerator ) && ObjectUtilities.equal ( ( Object ) this.legendLabelToolTipGenerator, ( Object ) that.legendLabelToolTipGenerator ) && ObjectUtilities.equal ( ( Object ) this.legendLabelURLGenerator, ( Object ) that.legendLabelURLGenerator ) && this.autoPopulateSectionPaint == that.autoPopulateSectionPaint && this.autoPopulateSectionOutlinePaint == that.autoPopulateSectionOutlinePaint && this.autoPopulateSectionOutlineStroke == that.autoPopulateSectionOutlineStroke && ObjectUtilities.equal ( ( Object ) this.shadowGenerator, ( Object ) that.shadowGenerator );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final PiePlot clone = ( PiePlot ) super.clone();
        clone.sectionPaintMap = ( PaintMap ) this.sectionPaintMap.clone();
        clone.sectionOutlinePaintMap = ( PaintMap ) this.sectionOutlinePaintMap.clone();
        clone.sectionOutlineStrokeMap = ( StrokeMap ) this.sectionOutlineStrokeMap.clone();
        clone.explodePercentages = new TreeMap ( this.explodePercentages );
        if ( this.labelGenerator != null ) {
            clone.labelGenerator = ( PieSectionLabelGenerator ) ObjectUtilities.clone ( ( Object ) this.labelGenerator );
        }
        if ( clone.dataset != null ) {
            clone.dataset.addChangeListener ( clone );
        }
        if ( this.urlGenerator instanceof PublicCloneable ) {
            clone.urlGenerator = ( PieURLGenerator ) ObjectUtilities.clone ( ( Object ) this.urlGenerator );
        }
        clone.legendItemShape = ShapeUtilities.clone ( this.legendItemShape );
        if ( this.legendLabelGenerator != null ) {
            clone.legendLabelGenerator = ( PieSectionLabelGenerator ) ObjectUtilities.clone ( ( Object ) this.legendLabelGenerator );
        }
        if ( this.legendLabelToolTipGenerator != null ) {
            clone.legendLabelToolTipGenerator = ( PieSectionLabelGenerator ) ObjectUtilities.clone ( ( Object ) this.legendLabelToolTipGenerator );
        }
        if ( this.legendLabelURLGenerator instanceof PublicCloneable ) {
            clone.legendLabelURLGenerator = ( PieURLGenerator ) ObjectUtilities.clone ( ( Object ) this.legendLabelURLGenerator );
        }
        return clone;
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.sectionPaint, stream );
        SerialUtilities.writePaint ( this.baseSectionPaint, stream );
        SerialUtilities.writePaint ( this.sectionOutlinePaint, stream );
        SerialUtilities.writePaint ( this.baseSectionOutlinePaint, stream );
        SerialUtilities.writeStroke ( this.sectionOutlineStroke, stream );
        SerialUtilities.writeStroke ( this.baseSectionOutlineStroke, stream );
        SerialUtilities.writePaint ( this.shadowPaint, stream );
        SerialUtilities.writePaint ( this.labelPaint, stream );
        SerialUtilities.writePaint ( this.labelBackgroundPaint, stream );
        SerialUtilities.writePaint ( this.labelOutlinePaint, stream );
        SerialUtilities.writeStroke ( this.labelOutlineStroke, stream );
        SerialUtilities.writePaint ( this.labelShadowPaint, stream );
        SerialUtilities.writePaint ( this.labelLinkPaint, stream );
        SerialUtilities.writeStroke ( this.labelLinkStroke, stream );
        SerialUtilities.writeShape ( this.legendItemShape, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.sectionPaint = SerialUtilities.readPaint ( stream );
        this.baseSectionPaint = SerialUtilities.readPaint ( stream );
        this.sectionOutlinePaint = SerialUtilities.readPaint ( stream );
        this.baseSectionOutlinePaint = SerialUtilities.readPaint ( stream );
        this.sectionOutlineStroke = SerialUtilities.readStroke ( stream );
        this.baseSectionOutlineStroke = SerialUtilities.readStroke ( stream );
        this.shadowPaint = SerialUtilities.readPaint ( stream );
        this.labelPaint = SerialUtilities.readPaint ( stream );
        this.labelBackgroundPaint = SerialUtilities.readPaint ( stream );
        this.labelOutlinePaint = SerialUtilities.readPaint ( stream );
        this.labelOutlineStroke = SerialUtilities.readStroke ( stream );
        this.labelShadowPaint = SerialUtilities.readPaint ( stream );
        this.labelLinkPaint = SerialUtilities.readPaint ( stream );
        this.labelLinkStroke = SerialUtilities.readStroke ( stream );
        this.legendItemShape = SerialUtilities.readShape ( stream );
    }
    public Paint getSectionPaint ( final int section ) {
        final Comparable key = this.getSectionKey ( section );
        return this.getSectionPaint ( key );
    }
    public void setSectionPaint ( final int section, final Paint paint ) {
        final Comparable key = this.getSectionKey ( section );
        this.setSectionPaint ( key, paint );
    }
    public Paint getSectionOutlinePaint() {
        return this.sectionOutlinePaint;
    }
    public void setSectionOutlinePaint ( final Paint paint ) {
        this.sectionOutlinePaint = paint;
        this.fireChangeEvent();
    }
    public Paint getSectionOutlinePaint ( final int section ) {
        final Comparable key = this.getSectionKey ( section );
        return this.getSectionOutlinePaint ( key );
    }
    public void setSectionOutlinePaint ( final int section, final Paint paint ) {
        final Comparable key = this.getSectionKey ( section );
        this.setSectionOutlinePaint ( key, paint );
    }
    public Stroke getSectionOutlineStroke() {
        return this.sectionOutlineStroke;
    }
    public void setSectionOutlineStroke ( final Stroke stroke ) {
        this.sectionOutlineStroke = stroke;
        this.fireChangeEvent();
    }
    public Stroke getSectionOutlineStroke ( final int section ) {
        final Comparable key = this.getSectionKey ( section );
        return this.getSectionOutlineStroke ( key );
    }
    public void setSectionOutlineStroke ( final int section, final Stroke stroke ) {
        final Comparable key = this.getSectionKey ( section );
        this.setSectionOutlineStroke ( key, stroke );
    }
    public double getExplodePercent ( final int section ) {
        final Comparable key = this.getSectionKey ( section );
        return this.getExplodePercent ( key );
    }
    public void setExplodePercent ( final int section, final double percent ) {
        final Comparable key = this.getSectionKey ( section );
        this.setExplodePercent ( key, percent );
    }
    static {
        DEFAULT_LABEL_FONT = new Font ( "SansSerif", 0, 10 );
        DEFAULT_LABEL_PAINT = Color.black;
        DEFAULT_LABEL_BACKGROUND_PAINT = new Color ( 255, 255, 192 );
        DEFAULT_LABEL_OUTLINE_PAINT = Color.black;
        DEFAULT_LABEL_OUTLINE_STROKE = new BasicStroke ( 0.5f );
        DEFAULT_LABEL_SHADOW_PAINT = new Color ( 151, 151, 151, 128 );
        PiePlot.localizationResources = ResourceBundleWrapper.getBundle ( "org.jfree.chart.plot.LocalizationBundle" );
    }
}
