package org.jfree.chart.renderer;
import java.awt.geom.Rectangle2D;
import java.awt.BasicStroke;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.chart.HashUtilities;
import org.jfree.util.ShapeUtilities;
import org.jfree.util.PaintUtilities;
import org.jfree.util.ObjectUtilities;
import java.util.List;
import java.util.Arrays;
import java.util.EventListener;
import org.jfree.chart.event.RendererChangeListener;
import java.awt.geom.Point2D;
import org.jfree.chart.plot.PlotOrientation;
import java.awt.RenderingHints;
import org.jfree.chart.ChartHints;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.ItemKey;
import java.awt.Graphics2D;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.ui.TextAnchor;
import org.jfree.chart.labels.ItemLabelAnchor;
import java.util.HashMap;
import java.awt.Color;
import org.jfree.chart.event.RendererChangeEvent;
import javax.swing.event.EventListenerList;
import org.jfree.chart.labels.ItemLabelPosition;
import java.util.Map;
import org.jfree.util.ShapeList;
import org.jfree.util.StrokeList;
import org.jfree.util.PaintList;
import org.jfree.util.BooleanList;
import java.awt.Font;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Paint;
import java.io.Serializable;
public abstract class AbstractRenderer implements Cloneable, Serializable {
    private static final long serialVersionUID = -828267569428206075L;
    public static final Double ZERO;
    public static final Paint DEFAULT_PAINT;
    public static final Paint DEFAULT_OUTLINE_PAINT;
    public static final Stroke DEFAULT_STROKE;
    public static final Stroke DEFAULT_OUTLINE_STROKE;
    public static final Shape DEFAULT_SHAPE;
    public static final Font DEFAULT_VALUE_LABEL_FONT;
    public static final Paint DEFAULT_VALUE_LABEL_PAINT;
    private BooleanList seriesVisibleList;
    private boolean baseSeriesVisible;
    private BooleanList seriesVisibleInLegendList;
    private boolean baseSeriesVisibleInLegend;
    private PaintList paintList;
    private boolean autoPopulateSeriesPaint;
    private transient Paint basePaint;
    private PaintList fillPaintList;
    private boolean autoPopulateSeriesFillPaint;
    private transient Paint baseFillPaint;
    private PaintList outlinePaintList;
    private boolean autoPopulateSeriesOutlinePaint;
    private transient Paint baseOutlinePaint;
    private StrokeList strokeList;
    private boolean autoPopulateSeriesStroke;
    private transient Stroke baseStroke;
    private StrokeList outlineStrokeList;
    private transient Stroke baseOutlineStroke;
    private boolean autoPopulateSeriesOutlineStroke;
    private ShapeList shapeList;
    private boolean autoPopulateSeriesShape;
    private transient Shape baseShape;
    private BooleanList itemLabelsVisibleList;
    private Boolean baseItemLabelsVisible;
    private Map<Integer, Font> itemLabelFontMap;
    private Font baseItemLabelFont;
    private PaintList itemLabelPaintList;
    private transient Paint baseItemLabelPaint;
    private Map<Integer, ItemLabelPosition> positiveItemLabelPositionMap;
    private ItemLabelPosition basePositiveItemLabelPosition;
    private Map<Integer, ItemLabelPosition> negativeItemLabelPositionMap;
    private ItemLabelPosition baseNegativeItemLabelPosition;
    private double itemLabelAnchorOffset;
    private BooleanList createEntitiesList;
    private boolean baseCreateEntities;
    private ShapeList legendShapeList;
    private transient Shape baseLegendShape;
    private boolean treatLegendShapeAsLine;
    private Map<Integer, Font> legendTextFontMap;
    private Font baseLegendTextFont;
    private PaintList legendTextPaint;
    private transient Paint baseLegendTextPaint;
    private boolean dataBoundsIncludesVisibleSeriesOnly;
    private int defaultEntityRadius;
    private transient EventListenerList listenerList;
    private transient RendererChangeEvent event;
    private static final double ADJ;
    private static final double OPP;
    private Boolean seriesVisible;
    private Boolean seriesVisibleInLegend;
    private transient Paint paint;
    private transient Paint fillPaint;
    private transient Paint outlinePaint;
    private transient Stroke stroke;
    private transient Stroke outlineStroke;
    private transient Shape shape;
    private Boolean itemLabelsVisible;
    private Font itemLabelFont;
    private transient Paint itemLabelPaint;
    private ItemLabelPosition positiveItemLabelPosition;
    private ItemLabelPosition negativeItemLabelPosition;
    private Boolean createEntities;
    public AbstractRenderer() {
        this.itemLabelAnchorOffset = 2.0;
        this.dataBoundsIncludesVisibleSeriesOnly = true;
        this.seriesVisible = null;
        this.seriesVisibleList = new BooleanList();
        this.baseSeriesVisible = true;
        this.seriesVisibleInLegend = null;
        this.seriesVisibleInLegendList = new BooleanList();
        this.baseSeriesVisibleInLegend = true;
        this.paint = null;
        this.paintList = new PaintList();
        this.basePaint = AbstractRenderer.DEFAULT_PAINT;
        this.autoPopulateSeriesPaint = true;
        this.fillPaint = null;
        this.fillPaintList = new PaintList();
        this.baseFillPaint = Color.white;
        this.autoPopulateSeriesFillPaint = false;
        this.outlinePaint = null;
        this.outlinePaintList = new PaintList();
        this.baseOutlinePaint = AbstractRenderer.DEFAULT_OUTLINE_PAINT;
        this.autoPopulateSeriesOutlinePaint = false;
        this.stroke = null;
        this.strokeList = new StrokeList();
        this.baseStroke = AbstractRenderer.DEFAULT_STROKE;
        this.autoPopulateSeriesStroke = true;
        this.outlineStroke = null;
        this.outlineStrokeList = new StrokeList();
        this.baseOutlineStroke = AbstractRenderer.DEFAULT_OUTLINE_STROKE;
        this.autoPopulateSeriesOutlineStroke = false;
        this.shape = null;
        this.shapeList = new ShapeList();
        this.baseShape = AbstractRenderer.DEFAULT_SHAPE;
        this.autoPopulateSeriesShape = true;
        this.itemLabelsVisible = null;
        this.itemLabelsVisibleList = new BooleanList();
        this.baseItemLabelsVisible = Boolean.FALSE;
        this.itemLabelFont = null;
        this.itemLabelFontMap = new HashMap<Integer, Font>();
        this.baseItemLabelFont = new Font ( "SansSerif", 0, 10 );
        this.itemLabelPaint = null;
        this.itemLabelPaintList = new PaintList();
        this.baseItemLabelPaint = Color.black;
        this.positiveItemLabelPosition = null;
        this.positiveItemLabelPositionMap = new HashMap<Integer, ItemLabelPosition>();
        this.basePositiveItemLabelPosition = new ItemLabelPosition ( ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER );
        this.negativeItemLabelPosition = null;
        this.negativeItemLabelPositionMap = new HashMap<Integer, ItemLabelPosition>();
        this.baseNegativeItemLabelPosition = new ItemLabelPosition ( ItemLabelAnchor.OUTSIDE6, TextAnchor.TOP_CENTER );
        this.createEntities = null;
        this.createEntitiesList = new BooleanList();
        this.baseCreateEntities = true;
        this.defaultEntityRadius = 3;
        this.legendShapeList = new ShapeList();
        this.baseLegendShape = null;
        this.treatLegendShapeAsLine = false;
        this.legendTextFontMap = new HashMap<Integer, Font>();
        this.baseLegendTextFont = null;
        this.legendTextPaint = new PaintList();
        this.baseLegendTextPaint = null;
        this.listenerList = new EventListenerList();
    }
    public abstract DrawingSupplier getDrawingSupplier();
    protected void beginElementGroup ( final Graphics2D g2, final ItemKey key ) {
        ParamChecks.nullNotPermitted ( key, "key" );
        final Map m = new HashMap ( 1 );
        m.put ( "ref", key.toJSONString() );
        g2.setRenderingHint ( ChartHints.KEY_BEGIN_ELEMENT, m );
    }
    protected void endElementGroup ( final Graphics2D g2 ) {
        g2.setRenderingHint ( ChartHints.KEY_END_ELEMENT, Boolean.TRUE );
    }
    public boolean getItemVisible ( final int series, final int item ) {
        return this.isSeriesVisible ( series );
    }
    public boolean isSeriesVisible ( final int series ) {
        boolean result = this.baseSeriesVisible;
        if ( this.seriesVisible != null ) {
            result = this.seriesVisible;
        } else {
            final Boolean b = this.seriesVisibleList.getBoolean ( series );
            if ( b != null ) {
                result = b;
            }
        }
        return result;
    }
    public Boolean getSeriesVisible ( final int series ) {
        return this.seriesVisibleList.getBoolean ( series );
    }
    public void setSeriesVisible ( final int series, final Boolean visible ) {
        this.setSeriesVisible ( series, visible, true );
    }
    public void setSeriesVisible ( final int series, final Boolean visible, final boolean notify ) {
        this.seriesVisibleList.setBoolean ( series, visible );
        if ( notify ) {
            final RendererChangeEvent e = new RendererChangeEvent ( this, true );
            this.notifyListeners ( e );
        }
    }
    public boolean getBaseSeriesVisible() {
        return this.baseSeriesVisible;
    }
    public void setBaseSeriesVisible ( final boolean visible ) {
        this.setBaseSeriesVisible ( visible, true );
    }
    public void setBaseSeriesVisible ( final boolean visible, final boolean notify ) {
        this.baseSeriesVisible = visible;
        if ( notify ) {
            final RendererChangeEvent e = new RendererChangeEvent ( this, true );
            this.notifyListeners ( e );
        }
    }
    public boolean isSeriesVisibleInLegend ( final int series ) {
        boolean result = this.baseSeriesVisibleInLegend;
        if ( this.seriesVisibleInLegend != null ) {
            result = this.seriesVisibleInLegend;
        } else {
            final Boolean b = this.seriesVisibleInLegendList.getBoolean ( series );
            if ( b != null ) {
                result = b;
            }
        }
        return result;
    }
    public Boolean getSeriesVisibleInLegend ( final int series ) {
        return this.seriesVisibleInLegendList.getBoolean ( series );
    }
    public void setSeriesVisibleInLegend ( final int series, final Boolean visible ) {
        this.setSeriesVisibleInLegend ( series, visible, true );
    }
    public void setSeriesVisibleInLegend ( final int series, final Boolean visible, final boolean notify ) {
        this.seriesVisibleInLegendList.setBoolean ( series, visible );
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public boolean getBaseSeriesVisibleInLegend() {
        return this.baseSeriesVisibleInLegend;
    }
    public void setBaseSeriesVisibleInLegend ( final boolean visible ) {
        this.setBaseSeriesVisibleInLegend ( visible, true );
    }
    public void setBaseSeriesVisibleInLegend ( final boolean visible, final boolean notify ) {
        this.baseSeriesVisibleInLegend = visible;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public Paint getItemPaint ( final int row, final int column ) {
        return this.lookupSeriesPaint ( row );
    }
    public Paint lookupSeriesPaint ( final int series ) {
        if ( this.paint != null ) {
            return this.paint;
        }
        Paint seriesPaint = this.getSeriesPaint ( series );
        if ( seriesPaint == null && this.autoPopulateSeriesPaint ) {
            final DrawingSupplier supplier = this.getDrawingSupplier();
            if ( supplier != null ) {
                seriesPaint = supplier.getNextPaint();
                this.setSeriesPaint ( series, seriesPaint, false );
            }
        }
        if ( seriesPaint == null ) {
            seriesPaint = this.basePaint;
        }
        return seriesPaint;
    }
    public Paint getSeriesPaint ( final int series ) {
        return this.paintList.getPaint ( series );
    }
    public void setSeriesPaint ( final int series, final Paint paint ) {
        this.setSeriesPaint ( series, paint, true );
    }
    public void setSeriesPaint ( final int series, final Paint paint, final boolean notify ) {
        this.paintList.setPaint ( series, paint );
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public void clearSeriesPaints ( final boolean notify ) {
        this.paintList.clear();
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public Paint getBasePaint() {
        return this.basePaint;
    }
    public void setBasePaint ( final Paint paint ) {
        this.setBasePaint ( paint, true );
    }
    public void setBasePaint ( final Paint paint, final boolean notify ) {
        this.basePaint = paint;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public boolean getAutoPopulateSeriesPaint() {
        return this.autoPopulateSeriesPaint;
    }
    public void setAutoPopulateSeriesPaint ( final boolean auto ) {
        this.autoPopulateSeriesPaint = auto;
    }
    public Paint getItemFillPaint ( final int row, final int column ) {
        return this.lookupSeriesFillPaint ( row );
    }
    public Paint lookupSeriesFillPaint ( final int series ) {
        if ( this.fillPaint != null ) {
            return this.fillPaint;
        }
        Paint seriesFillPaint = this.getSeriesFillPaint ( series );
        if ( seriesFillPaint == null && this.autoPopulateSeriesFillPaint ) {
            final DrawingSupplier supplier = this.getDrawingSupplier();
            if ( supplier != null ) {
                seriesFillPaint = supplier.getNextFillPaint();
                this.setSeriesFillPaint ( series, seriesFillPaint, false );
            }
        }
        if ( seriesFillPaint == null ) {
            seriesFillPaint = this.baseFillPaint;
        }
        return seriesFillPaint;
    }
    public Paint getSeriesFillPaint ( final int series ) {
        return this.fillPaintList.getPaint ( series );
    }
    public void setSeriesFillPaint ( final int series, final Paint paint ) {
        this.setSeriesFillPaint ( series, paint, true );
    }
    public void setSeriesFillPaint ( final int series, final Paint paint, final boolean notify ) {
        this.fillPaintList.setPaint ( series, paint );
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public Paint getBaseFillPaint() {
        return this.baseFillPaint;
    }
    public void setBaseFillPaint ( final Paint paint ) {
        this.setBaseFillPaint ( paint, true );
    }
    public void setBaseFillPaint ( final Paint paint, final boolean notify ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.baseFillPaint = paint;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public boolean getAutoPopulateSeriesFillPaint() {
        return this.autoPopulateSeriesFillPaint;
    }
    public void setAutoPopulateSeriesFillPaint ( final boolean auto ) {
        this.autoPopulateSeriesFillPaint = auto;
    }
    public Paint getItemOutlinePaint ( final int row, final int column ) {
        return this.lookupSeriesOutlinePaint ( row );
    }
    public Paint lookupSeriesOutlinePaint ( final int series ) {
        if ( this.outlinePaint != null ) {
            return this.outlinePaint;
        }
        Paint seriesOutlinePaint = this.getSeriesOutlinePaint ( series );
        if ( seriesOutlinePaint == null && this.autoPopulateSeriesOutlinePaint ) {
            final DrawingSupplier supplier = this.getDrawingSupplier();
            if ( supplier != null ) {
                seriesOutlinePaint = supplier.getNextOutlinePaint();
                this.setSeriesOutlinePaint ( series, seriesOutlinePaint, false );
            }
        }
        if ( seriesOutlinePaint == null ) {
            seriesOutlinePaint = this.baseOutlinePaint;
        }
        return seriesOutlinePaint;
    }
    public Paint getSeriesOutlinePaint ( final int series ) {
        return this.outlinePaintList.getPaint ( series );
    }
    public void setSeriesOutlinePaint ( final int series, final Paint paint ) {
        this.setSeriesOutlinePaint ( series, paint, true );
    }
    public void setSeriesOutlinePaint ( final int series, final Paint paint, final boolean notify ) {
        this.outlinePaintList.setPaint ( series, paint );
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public Paint getBaseOutlinePaint() {
        return this.baseOutlinePaint;
    }
    public void setBaseOutlinePaint ( final Paint paint ) {
        this.setBaseOutlinePaint ( paint, true );
    }
    public void setBaseOutlinePaint ( final Paint paint, final boolean notify ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.baseOutlinePaint = paint;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public boolean getAutoPopulateSeriesOutlinePaint() {
        return this.autoPopulateSeriesOutlinePaint;
    }
    public void setAutoPopulateSeriesOutlinePaint ( final boolean auto ) {
        this.autoPopulateSeriesOutlinePaint = auto;
    }
    public Stroke getItemStroke ( final int row, final int column ) {
        return this.lookupSeriesStroke ( row );
    }
    public Stroke lookupSeriesStroke ( final int series ) {
        if ( this.stroke != null ) {
            return this.stroke;
        }
        Stroke result = this.getSeriesStroke ( series );
        if ( result == null && this.autoPopulateSeriesStroke ) {
            final DrawingSupplier supplier = this.getDrawingSupplier();
            if ( supplier != null ) {
                result = supplier.getNextStroke();
                this.setSeriesStroke ( series, result, false );
            }
        }
        if ( result == null ) {
            result = this.baseStroke;
        }
        return result;
    }
    public Stroke getSeriesStroke ( final int series ) {
        return this.strokeList.getStroke ( series );
    }
    public void setSeriesStroke ( final int series, final Stroke stroke ) {
        this.setSeriesStroke ( series, stroke, true );
    }
    public void setSeriesStroke ( final int series, final Stroke stroke, final boolean notify ) {
        this.strokeList.setStroke ( series, stroke );
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public void clearSeriesStrokes ( final boolean notify ) {
        this.strokeList.clear();
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public Stroke getBaseStroke() {
        return this.baseStroke;
    }
    public void setBaseStroke ( final Stroke stroke ) {
        this.setBaseStroke ( stroke, true );
    }
    public void setBaseStroke ( final Stroke stroke, final boolean notify ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.baseStroke = stroke;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public boolean getAutoPopulateSeriesStroke() {
        return this.autoPopulateSeriesStroke;
    }
    public void setAutoPopulateSeriesStroke ( final boolean auto ) {
        this.autoPopulateSeriesStroke = auto;
    }
    public Stroke getItemOutlineStroke ( final int row, final int column ) {
        return this.lookupSeriesOutlineStroke ( row );
    }
    public Stroke lookupSeriesOutlineStroke ( final int series ) {
        if ( this.outlineStroke != null ) {
            return this.outlineStroke;
        }
        Stroke result = this.getSeriesOutlineStroke ( series );
        if ( result == null && this.autoPopulateSeriesOutlineStroke ) {
            final DrawingSupplier supplier = this.getDrawingSupplier();
            if ( supplier != null ) {
                result = supplier.getNextOutlineStroke();
                this.setSeriesOutlineStroke ( series, result, false );
            }
        }
        if ( result == null ) {
            result = this.baseOutlineStroke;
        }
        return result;
    }
    public Stroke getSeriesOutlineStroke ( final int series ) {
        return this.outlineStrokeList.getStroke ( series );
    }
    public void setSeriesOutlineStroke ( final int series, final Stroke stroke ) {
        this.setSeriesOutlineStroke ( series, stroke, true );
    }
    public void setSeriesOutlineStroke ( final int series, final Stroke stroke, final boolean notify ) {
        this.outlineStrokeList.setStroke ( series, stroke );
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public Stroke getBaseOutlineStroke() {
        return this.baseOutlineStroke;
    }
    public void setBaseOutlineStroke ( final Stroke stroke ) {
        this.setBaseOutlineStroke ( stroke, true );
    }
    public void setBaseOutlineStroke ( final Stroke stroke, final boolean notify ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.baseOutlineStroke = stroke;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public boolean getAutoPopulateSeriesOutlineStroke() {
        return this.autoPopulateSeriesOutlineStroke;
    }
    public void setAutoPopulateSeriesOutlineStroke ( final boolean auto ) {
        this.autoPopulateSeriesOutlineStroke = auto;
    }
    public Shape getItemShape ( final int row, final int column ) {
        return this.lookupSeriesShape ( row );
    }
    public Shape lookupSeriesShape ( final int series ) {
        if ( this.shape != null ) {
            return this.shape;
        }
        Shape result = this.getSeriesShape ( series );
        if ( result == null && this.autoPopulateSeriesShape ) {
            final DrawingSupplier supplier = this.getDrawingSupplier();
            if ( supplier != null ) {
                result = supplier.getNextShape();
                this.setSeriesShape ( series, result, false );
            }
        }
        if ( result == null ) {
            result = this.baseShape;
        }
        return result;
    }
    public Shape getSeriesShape ( final int series ) {
        return this.shapeList.getShape ( series );
    }
    public void setSeriesShape ( final int series, final Shape shape ) {
        this.setSeriesShape ( series, shape, true );
    }
    public void setSeriesShape ( final int series, final Shape shape, final boolean notify ) {
        this.shapeList.setShape ( series, shape );
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public Shape getBaseShape() {
        return this.baseShape;
    }
    public void setBaseShape ( final Shape shape ) {
        this.setBaseShape ( shape, true );
    }
    public void setBaseShape ( final Shape shape, final boolean notify ) {
        ParamChecks.nullNotPermitted ( shape, "shape" );
        this.baseShape = shape;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public boolean getAutoPopulateSeriesShape() {
        return this.autoPopulateSeriesShape;
    }
    public void setAutoPopulateSeriesShape ( final boolean auto ) {
        this.autoPopulateSeriesShape = auto;
    }
    public boolean isItemLabelVisible ( final int row, final int column ) {
        return this.isSeriesItemLabelsVisible ( row );
    }
    public boolean isSeriesItemLabelsVisible ( final int series ) {
        if ( this.itemLabelsVisible != null ) {
            return this.itemLabelsVisible;
        }
        Boolean b = this.itemLabelsVisibleList.getBoolean ( series );
        if ( b == null ) {
            b = this.baseItemLabelsVisible;
        }
        if ( b == null ) {
            b = Boolean.FALSE;
        }
        return b;
    }
    public void setSeriesItemLabelsVisible ( final int series, final boolean visible ) {
        this.setSeriesItemLabelsVisible ( series, Boolean.valueOf ( visible ) );
    }
    public void setSeriesItemLabelsVisible ( final int series, final Boolean visible ) {
        this.setSeriesItemLabelsVisible ( series, visible, true );
    }
    public void setSeriesItemLabelsVisible ( final int series, final Boolean visible, final boolean notify ) {
        this.itemLabelsVisibleList.setBoolean ( series, visible );
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public Boolean getBaseItemLabelsVisible() {
        return this.baseItemLabelsVisible;
    }
    public void setBaseItemLabelsVisible ( final boolean visible ) {
        this.setBaseItemLabelsVisible ( Boolean.valueOf ( visible ) );
    }
    public void setBaseItemLabelsVisible ( final Boolean visible ) {
        this.setBaseItemLabelsVisible ( visible, true );
    }
    public void setBaseItemLabelsVisible ( final Boolean visible, final boolean notify ) {
        this.baseItemLabelsVisible = visible;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public Font getItemLabelFont ( final int row, final int column ) {
        Font result = this.itemLabelFont;
        if ( result == null ) {
            result = this.getSeriesItemLabelFont ( row );
            if ( result == null ) {
                result = this.baseItemLabelFont;
            }
        }
        return result;
    }
    public Font getSeriesItemLabelFont ( final int series ) {
        return this.itemLabelFontMap.get ( series );
    }
    public void setSeriesItemLabelFont ( final int series, final Font font ) {
        this.setSeriesItemLabelFont ( series, font, true );
    }
    public void setSeriesItemLabelFont ( final int series, final Font font, final boolean notify ) {
        this.itemLabelFontMap.put ( series, font );
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public Font getBaseItemLabelFont() {
        return this.baseItemLabelFont;
    }
    public void setBaseItemLabelFont ( final Font font ) {
        ParamChecks.nullNotPermitted ( font, "font" );
        this.setBaseItemLabelFont ( font, true );
    }
    public void setBaseItemLabelFont ( final Font font, final boolean notify ) {
        this.baseItemLabelFont = font;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public Paint getItemLabelPaint ( final int row, final int column ) {
        Paint result = this.itemLabelPaint;
        if ( result == null ) {
            result = this.getSeriesItemLabelPaint ( row );
            if ( result == null ) {
                result = this.baseItemLabelPaint;
            }
        }
        return result;
    }
    public Paint getSeriesItemLabelPaint ( final int series ) {
        return this.itemLabelPaintList.getPaint ( series );
    }
    public void setSeriesItemLabelPaint ( final int series, final Paint paint ) {
        this.setSeriesItemLabelPaint ( series, paint, true );
    }
    public void setSeriesItemLabelPaint ( final int series, final Paint paint, final boolean notify ) {
        this.itemLabelPaintList.setPaint ( series, paint );
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public Paint getBaseItemLabelPaint() {
        return this.baseItemLabelPaint;
    }
    public void setBaseItemLabelPaint ( final Paint paint ) {
        this.setBaseItemLabelPaint ( paint, true );
    }
    public void setBaseItemLabelPaint ( final Paint paint, final boolean notify ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.baseItemLabelPaint = paint;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public ItemLabelPosition getPositiveItemLabelPosition ( final int row, final int column ) {
        return this.getSeriesPositiveItemLabelPosition ( row );
    }
    public ItemLabelPosition getSeriesPositiveItemLabelPosition ( final int series ) {
        if ( this.positiveItemLabelPosition != null ) {
            return this.positiveItemLabelPosition;
        }
        ItemLabelPosition position = this.positiveItemLabelPositionMap.get ( series );
        if ( position == null ) {
            position = this.basePositiveItemLabelPosition;
        }
        return position;
    }
    public void setSeriesPositiveItemLabelPosition ( final int series, final ItemLabelPosition position ) {
        this.setSeriesPositiveItemLabelPosition ( series, position, true );
    }
    public void setSeriesPositiveItemLabelPosition ( final int series, final ItemLabelPosition position, final boolean notify ) {
        this.positiveItemLabelPositionMap.put ( series, position );
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public ItemLabelPosition getBasePositiveItemLabelPosition() {
        return this.basePositiveItemLabelPosition;
    }
    public void setBasePositiveItemLabelPosition ( final ItemLabelPosition position ) {
        this.setBasePositiveItemLabelPosition ( position, true );
    }
    public void setBasePositiveItemLabelPosition ( final ItemLabelPosition position, final boolean notify ) {
        ParamChecks.nullNotPermitted ( position, "position" );
        this.basePositiveItemLabelPosition = position;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public ItemLabelPosition getNegativeItemLabelPosition ( final int row, final int column ) {
        return this.getSeriesNegativeItemLabelPosition ( row );
    }
    public ItemLabelPosition getSeriesNegativeItemLabelPosition ( final int series ) {
        if ( this.negativeItemLabelPosition != null ) {
            return this.negativeItemLabelPosition;
        }
        ItemLabelPosition position = this.negativeItemLabelPositionMap.get ( series );
        if ( position == null ) {
            position = this.baseNegativeItemLabelPosition;
        }
        return position;
    }
    public void setSeriesNegativeItemLabelPosition ( final int series, final ItemLabelPosition position ) {
        this.setSeriesNegativeItemLabelPosition ( series, position, true );
    }
    public void setSeriesNegativeItemLabelPosition ( final int series, final ItemLabelPosition position, final boolean notify ) {
        this.negativeItemLabelPositionMap.put ( series, position );
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public ItemLabelPosition getBaseNegativeItemLabelPosition() {
        return this.baseNegativeItemLabelPosition;
    }
    public void setBaseNegativeItemLabelPosition ( final ItemLabelPosition position ) {
        this.setBaseNegativeItemLabelPosition ( position, true );
    }
    public void setBaseNegativeItemLabelPosition ( final ItemLabelPosition position, final boolean notify ) {
        ParamChecks.nullNotPermitted ( position, "position" );
        this.baseNegativeItemLabelPosition = position;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public double getItemLabelAnchorOffset() {
        return this.itemLabelAnchorOffset;
    }
    public void setItemLabelAnchorOffset ( final double offset ) {
        this.itemLabelAnchorOffset = offset;
        this.fireChangeEvent();
    }
    public boolean getItemCreateEntity ( final int series, final int item ) {
        if ( this.createEntities != null ) {
            return this.createEntities;
        }
        final Boolean b = this.getSeriesCreateEntities ( series );
        if ( b != null ) {
            return b;
        }
        return this.baseCreateEntities;
    }
    public Boolean getSeriesCreateEntities ( final int series ) {
        return this.createEntitiesList.getBoolean ( series );
    }
    public void setSeriesCreateEntities ( final int series, final Boolean create ) {
        this.setSeriesCreateEntities ( series, create, true );
    }
    public void setSeriesCreateEntities ( final int series, final Boolean create, final boolean notify ) {
        this.createEntitiesList.setBoolean ( series, create );
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public boolean getBaseCreateEntities() {
        return this.baseCreateEntities;
    }
    public void setBaseCreateEntities ( final boolean create ) {
        this.setBaseCreateEntities ( create, true );
    }
    public void setBaseCreateEntities ( final boolean create, final boolean notify ) {
        this.baseCreateEntities = create;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public int getDefaultEntityRadius() {
        return this.defaultEntityRadius;
    }
    public void setDefaultEntityRadius ( final int radius ) {
        this.defaultEntityRadius = radius;
    }
    public Shape lookupLegendShape ( final int series ) {
        Shape result = this.getLegendShape ( series );
        if ( result == null ) {
            result = this.baseLegendShape;
        }
        if ( result == null ) {
            result = this.lookupSeriesShape ( series );
        }
        return result;
    }
    public Shape getLegendShape ( final int series ) {
        return this.legendShapeList.getShape ( series );
    }
    public void setLegendShape ( final int series, final Shape shape ) {
        this.legendShapeList.setShape ( series, shape );
        this.fireChangeEvent();
    }
    public Shape getBaseLegendShape() {
        return this.baseLegendShape;
    }
    public void setBaseLegendShape ( final Shape shape ) {
        this.baseLegendShape = shape;
        this.fireChangeEvent();
    }
    protected boolean getTreatLegendShapeAsLine() {
        return this.treatLegendShapeAsLine;
    }
    protected void setTreatLegendShapeAsLine ( final boolean treatAsLine ) {
        if ( this.treatLegendShapeAsLine != treatAsLine ) {
            this.treatLegendShapeAsLine = treatAsLine;
            this.fireChangeEvent();
        }
    }
    public Font lookupLegendTextFont ( final int series ) {
        Font result = this.getLegendTextFont ( series );
        if ( result == null ) {
            result = this.baseLegendTextFont;
        }
        return result;
    }
    public Font getLegendTextFont ( final int series ) {
        return this.legendTextFontMap.get ( series );
    }
    public void setLegendTextFont ( final int series, final Font font ) {
        this.legendTextFontMap.put ( series, font );
        this.fireChangeEvent();
    }
    public Font getBaseLegendTextFont() {
        return this.baseLegendTextFont;
    }
    public void setBaseLegendTextFont ( final Font font ) {
        this.baseLegendTextFont = font;
        this.fireChangeEvent();
    }
    public Paint lookupLegendTextPaint ( final int series ) {
        Paint result = this.getLegendTextPaint ( series );
        if ( result == null ) {
            result = this.baseLegendTextPaint;
        }
        return result;
    }
    public Paint getLegendTextPaint ( final int series ) {
        return this.legendTextPaint.getPaint ( series );
    }
    public void setLegendTextPaint ( final int series, final Paint paint ) {
        this.legendTextPaint.setPaint ( series, paint );
        this.fireChangeEvent();
    }
    public Paint getBaseLegendTextPaint() {
        return this.baseLegendTextPaint;
    }
    public void setBaseLegendTextPaint ( final Paint paint ) {
        this.baseLegendTextPaint = paint;
        this.fireChangeEvent();
    }
    public boolean getDataBoundsIncludesVisibleSeriesOnly() {
        return this.dataBoundsIncludesVisibleSeriesOnly;
    }
    public void setDataBoundsIncludesVisibleSeriesOnly ( final boolean visibleOnly ) {
        this.dataBoundsIncludesVisibleSeriesOnly = visibleOnly;
        this.notifyListeners ( new RendererChangeEvent ( this, true ) );
    }
    protected Point2D calculateLabelAnchorPoint ( final ItemLabelAnchor anchor, final double x, final double y, final PlotOrientation orientation ) {
        Point2D result = null;
        if ( anchor == ItemLabelAnchor.CENTER ) {
            result = new Point2D.Double ( x, y );
        } else if ( anchor == ItemLabelAnchor.INSIDE1 ) {
            result = new Point2D.Double ( x + AbstractRenderer.OPP * this.itemLabelAnchorOffset, y - AbstractRenderer.ADJ * this.itemLabelAnchorOffset );
        } else if ( anchor == ItemLabelAnchor.INSIDE2 ) {
            result = new Point2D.Double ( x + AbstractRenderer.ADJ * this.itemLabelAnchorOffset, y - AbstractRenderer.OPP * this.itemLabelAnchorOffset );
        } else if ( anchor == ItemLabelAnchor.INSIDE3 ) {
            result = new Point2D.Double ( x + this.itemLabelAnchorOffset, y );
        } else if ( anchor == ItemLabelAnchor.INSIDE4 ) {
            result = new Point2D.Double ( x + AbstractRenderer.ADJ * this.itemLabelAnchorOffset, y + AbstractRenderer.OPP * this.itemLabelAnchorOffset );
        } else if ( anchor == ItemLabelAnchor.INSIDE5 ) {
            result = new Point2D.Double ( x + AbstractRenderer.OPP * this.itemLabelAnchorOffset, y + AbstractRenderer.ADJ * this.itemLabelAnchorOffset );
        } else if ( anchor == ItemLabelAnchor.INSIDE6 ) {
            result = new Point2D.Double ( x, y + this.itemLabelAnchorOffset );
        } else if ( anchor == ItemLabelAnchor.INSIDE7 ) {
            result = new Point2D.Double ( x - AbstractRenderer.OPP * this.itemLabelAnchorOffset, y + AbstractRenderer.ADJ * this.itemLabelAnchorOffset );
        } else if ( anchor == ItemLabelAnchor.INSIDE8 ) {
            result = new Point2D.Double ( x - AbstractRenderer.ADJ * this.itemLabelAnchorOffset, y + AbstractRenderer.OPP * this.itemLabelAnchorOffset );
        } else if ( anchor == ItemLabelAnchor.INSIDE9 ) {
            result = new Point2D.Double ( x - this.itemLabelAnchorOffset, y );
        } else if ( anchor == ItemLabelAnchor.INSIDE10 ) {
            result = new Point2D.Double ( x - AbstractRenderer.ADJ * this.itemLabelAnchorOffset, y - AbstractRenderer.OPP * this.itemLabelAnchorOffset );
        } else if ( anchor == ItemLabelAnchor.INSIDE11 ) {
            result = new Point2D.Double ( x - AbstractRenderer.OPP * this.itemLabelAnchorOffset, y - AbstractRenderer.ADJ * this.itemLabelAnchorOffset );
        } else if ( anchor == ItemLabelAnchor.INSIDE12 ) {
            result = new Point2D.Double ( x, y - this.itemLabelAnchorOffset );
        } else if ( anchor == ItemLabelAnchor.OUTSIDE1 ) {
            result = new Point2D.Double ( x + 2.0 * AbstractRenderer.OPP * this.itemLabelAnchorOffset, y - 2.0 * AbstractRenderer.ADJ * this.itemLabelAnchorOffset );
        } else if ( anchor == ItemLabelAnchor.OUTSIDE2 ) {
            result = new Point2D.Double ( x + 2.0 * AbstractRenderer.ADJ * this.itemLabelAnchorOffset, y - 2.0 * AbstractRenderer.OPP * this.itemLabelAnchorOffset );
        } else if ( anchor == ItemLabelAnchor.OUTSIDE3 ) {
            result = new Point2D.Double ( x + 2.0 * this.itemLabelAnchorOffset, y );
        } else if ( anchor == ItemLabelAnchor.OUTSIDE4 ) {
            result = new Point2D.Double ( x + 2.0 * AbstractRenderer.ADJ * this.itemLabelAnchorOffset, y + 2.0 * AbstractRenderer.OPP * this.itemLabelAnchorOffset );
        } else if ( anchor == ItemLabelAnchor.OUTSIDE5 ) {
            result = new Point2D.Double ( x + 2.0 * AbstractRenderer.OPP * this.itemLabelAnchorOffset, y + 2.0 * AbstractRenderer.ADJ * this.itemLabelAnchorOffset );
        } else if ( anchor == ItemLabelAnchor.OUTSIDE6 ) {
            result = new Point2D.Double ( x, y + 2.0 * this.itemLabelAnchorOffset );
        } else if ( anchor == ItemLabelAnchor.OUTSIDE7 ) {
            result = new Point2D.Double ( x - 2.0 * AbstractRenderer.OPP * this.itemLabelAnchorOffset, y + 2.0 * AbstractRenderer.ADJ * this.itemLabelAnchorOffset );
        } else if ( anchor == ItemLabelAnchor.OUTSIDE8 ) {
            result = new Point2D.Double ( x - 2.0 * AbstractRenderer.ADJ * this.itemLabelAnchorOffset, y + 2.0 * AbstractRenderer.OPP * this.itemLabelAnchorOffset );
        } else if ( anchor == ItemLabelAnchor.OUTSIDE9 ) {
            result = new Point2D.Double ( x - 2.0 * this.itemLabelAnchorOffset, y );
        } else if ( anchor == ItemLabelAnchor.OUTSIDE10 ) {
            result = new Point2D.Double ( x - 2.0 * AbstractRenderer.ADJ * this.itemLabelAnchorOffset, y - 2.0 * AbstractRenderer.OPP * this.itemLabelAnchorOffset );
        } else if ( anchor == ItemLabelAnchor.OUTSIDE11 ) {
            result = new Point2D.Double ( x - 2.0 * AbstractRenderer.OPP * this.itemLabelAnchorOffset, y - 2.0 * AbstractRenderer.ADJ * this.itemLabelAnchorOffset );
        } else if ( anchor == ItemLabelAnchor.OUTSIDE12 ) {
            result = new Point2D.Double ( x, y - 2.0 * this.itemLabelAnchorOffset );
        }
        return result;
    }
    public void addChangeListener ( final RendererChangeListener listener ) {
        ParamChecks.nullNotPermitted ( listener, "listener" );
        this.listenerList.add ( RendererChangeListener.class, listener );
    }
    public void removeChangeListener ( final RendererChangeListener listener ) {
        ParamChecks.nullNotPermitted ( listener, "listener" );
        this.listenerList.remove ( RendererChangeListener.class, listener );
    }
    public boolean hasListener ( final EventListener listener ) {
        final List list = Arrays.asList ( this.listenerList.getListenerList() );
        return list.contains ( listener );
    }
    protected void fireChangeEvent() {
        this.notifyListeners ( new RendererChangeEvent ( this ) );
    }
    public void notifyListeners ( final RendererChangeEvent event ) {
        final Object[] ls = this.listenerList.getListenerList();
        for ( int i = ls.length - 2; i >= 0; i -= 2 ) {
            if ( ls[i] == RendererChangeListener.class ) {
                ( ( RendererChangeListener ) ls[i + 1] ).rendererChanged ( event );
            }
        }
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof AbstractRenderer ) ) {
            return false;
        }
        final AbstractRenderer that = ( AbstractRenderer ) obj;
        return this.dataBoundsIncludesVisibleSeriesOnly == that.dataBoundsIncludesVisibleSeriesOnly && this.treatLegendShapeAsLine == that.treatLegendShapeAsLine && this.defaultEntityRadius == that.defaultEntityRadius && ObjectUtilities.equal ( ( Object ) this.seriesVisible, ( Object ) that.seriesVisible ) && this.seriesVisibleList.equals ( ( Object ) that.seriesVisibleList ) && this.baseSeriesVisible == that.baseSeriesVisible && ObjectUtilities.equal ( ( Object ) this.seriesVisibleInLegend, ( Object ) that.seriesVisibleInLegend ) && this.seriesVisibleInLegendList.equals ( ( Object ) that.seriesVisibleInLegendList ) && this.baseSeriesVisibleInLegend == that.baseSeriesVisibleInLegend && PaintUtilities.equal ( this.paint, that.paint ) && ObjectUtilities.equal ( ( Object ) this.paintList, ( Object ) that.paintList ) && PaintUtilities.equal ( this.basePaint, that.basePaint ) && PaintUtilities.equal ( this.fillPaint, that.fillPaint ) && ObjectUtilities.equal ( ( Object ) this.fillPaintList, ( Object ) that.fillPaintList ) && PaintUtilities.equal ( this.baseFillPaint, that.baseFillPaint ) && PaintUtilities.equal ( this.outlinePaint, that.outlinePaint ) && ObjectUtilities.equal ( ( Object ) this.outlinePaintList, ( Object ) that.outlinePaintList ) && PaintUtilities.equal ( this.baseOutlinePaint, that.baseOutlinePaint ) && ObjectUtilities.equal ( ( Object ) this.stroke, ( Object ) that.stroke ) && ObjectUtilities.equal ( ( Object ) this.strokeList, ( Object ) that.strokeList ) && ObjectUtilities.equal ( ( Object ) this.baseStroke, ( Object ) that.baseStroke ) && ObjectUtilities.equal ( ( Object ) this.outlineStroke, ( Object ) that.outlineStroke ) && ObjectUtilities.equal ( ( Object ) this.outlineStrokeList, ( Object ) that.outlineStrokeList ) && ObjectUtilities.equal ( ( Object ) this.baseOutlineStroke, ( Object ) that.baseOutlineStroke ) && ShapeUtilities.equal ( this.shape, that.shape ) && ObjectUtilities.equal ( ( Object ) this.shapeList, ( Object ) that.shapeList ) && ShapeUtilities.equal ( this.baseShape, that.baseShape ) && ObjectUtilities.equal ( ( Object ) this.itemLabelsVisible, ( Object ) that.itemLabelsVisible ) && ObjectUtilities.equal ( ( Object ) this.itemLabelsVisibleList, ( Object ) that.itemLabelsVisibleList ) && ObjectUtilities.equal ( ( Object ) this.baseItemLabelsVisible, ( Object ) that.baseItemLabelsVisible ) && ObjectUtilities.equal ( ( Object ) this.itemLabelFont, ( Object ) that.itemLabelFont ) && ObjectUtilities.equal ( ( Object ) this.itemLabelFontMap, ( Object ) that.itemLabelFontMap ) && ObjectUtilities.equal ( ( Object ) this.baseItemLabelFont, ( Object ) that.baseItemLabelFont ) && PaintUtilities.equal ( this.itemLabelPaint, that.itemLabelPaint ) && ObjectUtilities.equal ( ( Object ) this.itemLabelPaintList, ( Object ) that.itemLabelPaintList ) && PaintUtilities.equal ( this.baseItemLabelPaint, that.baseItemLabelPaint ) && ObjectUtilities.equal ( ( Object ) this.positiveItemLabelPosition, ( Object ) that.positiveItemLabelPosition ) && ObjectUtilities.equal ( ( Object ) this.positiveItemLabelPositionMap, ( Object ) that.positiveItemLabelPositionMap ) && ObjectUtilities.equal ( ( Object ) this.basePositiveItemLabelPosition, ( Object ) that.basePositiveItemLabelPosition ) && ObjectUtilities.equal ( ( Object ) this.negativeItemLabelPosition, ( Object ) that.negativeItemLabelPosition ) && ObjectUtilities.equal ( ( Object ) this.negativeItemLabelPositionMap, ( Object ) that.negativeItemLabelPositionMap ) && ObjectUtilities.equal ( ( Object ) this.baseNegativeItemLabelPosition, ( Object ) that.baseNegativeItemLabelPosition ) && this.itemLabelAnchorOffset == that.itemLabelAnchorOffset && ObjectUtilities.equal ( ( Object ) this.createEntities, ( Object ) that.createEntities ) && ObjectUtilities.equal ( ( Object ) this.createEntitiesList, ( Object ) that.createEntitiesList ) && this.baseCreateEntities == that.baseCreateEntities && ObjectUtilities.equal ( ( Object ) this.legendShapeList, ( Object ) that.legendShapeList ) && ShapeUtilities.equal ( this.baseLegendShape, that.baseLegendShape ) && ObjectUtilities.equal ( ( Object ) this.legendTextFontMap, ( Object ) that.legendTextFontMap ) && ObjectUtilities.equal ( ( Object ) this.baseLegendTextFont, ( Object ) that.baseLegendTextFont ) && ObjectUtilities.equal ( ( Object ) this.legendTextPaint, ( Object ) that.legendTextPaint ) && PaintUtilities.equal ( this.baseLegendTextPaint, that.baseLegendTextPaint );
    }
    @Override
    public int hashCode() {
        int result = 193;
        result = HashUtilities.hashCode ( result, this.seriesVisibleList );
        result = HashUtilities.hashCode ( result, this.baseSeriesVisible );
        result = HashUtilities.hashCode ( result, this.seriesVisibleInLegendList );
        result = HashUtilities.hashCode ( result, this.baseSeriesVisibleInLegend );
        result = HashUtilities.hashCode ( result, this.paintList );
        result = HashUtilities.hashCode ( result, this.basePaint );
        result = HashUtilities.hashCode ( result, this.fillPaintList );
        result = HashUtilities.hashCode ( result, this.baseFillPaint );
        result = HashUtilities.hashCode ( result, this.outlinePaintList );
        result = HashUtilities.hashCode ( result, this.baseOutlinePaint );
        result = HashUtilities.hashCode ( result, this.strokeList );
        result = HashUtilities.hashCode ( result, this.baseStroke );
        result = HashUtilities.hashCode ( result, this.outlineStrokeList );
        result = HashUtilities.hashCode ( result, this.baseOutlineStroke );
        result = HashUtilities.hashCode ( result, this.itemLabelsVisibleList );
        result = HashUtilities.hashCode ( result, this.baseItemLabelsVisible );
        return result;
    }
    @Override
    protected Object clone() throws CloneNotSupportedException {
        final AbstractRenderer clone = ( AbstractRenderer ) super.clone();
        if ( this.seriesVisibleList != null ) {
            clone.seriesVisibleList = ( BooleanList ) this.seriesVisibleList.clone();
        }
        if ( this.seriesVisibleInLegendList != null ) {
            clone.seriesVisibleInLegendList = ( BooleanList ) this.seriesVisibleInLegendList.clone();
        }
        if ( this.paintList != null ) {
            clone.paintList = ( PaintList ) this.paintList.clone();
        }
        if ( this.fillPaintList != null ) {
            clone.fillPaintList = ( PaintList ) this.fillPaintList.clone();
        }
        if ( this.outlinePaintList != null ) {
            clone.outlinePaintList = ( PaintList ) this.outlinePaintList.clone();
        }
        if ( this.strokeList != null ) {
            clone.strokeList = ( StrokeList ) this.strokeList.clone();
        }
        if ( this.outlineStrokeList != null ) {
            clone.outlineStrokeList = ( StrokeList ) this.outlineStrokeList.clone();
        }
        if ( this.shape != null ) {
            clone.shape = ShapeUtilities.clone ( this.shape );
        }
        if ( this.shapeList != null ) {
            clone.shapeList = ( ShapeList ) this.shapeList.clone();
        }
        if ( this.baseShape != null ) {
            clone.baseShape = ShapeUtilities.clone ( this.baseShape );
        }
        if ( this.itemLabelsVisibleList != null ) {
            clone.itemLabelsVisibleList = ( BooleanList ) this.itemLabelsVisibleList.clone();
        }
        if ( this.itemLabelFontMap != null ) {
            clone.itemLabelFontMap = new HashMap<Integer, Font> ( this.itemLabelFontMap );
        }
        if ( this.itemLabelPaintList != null ) {
            clone.itemLabelPaintList = ( PaintList ) this.itemLabelPaintList.clone();
        }
        if ( this.positiveItemLabelPositionMap != null ) {
            clone.positiveItemLabelPositionMap = new HashMap<Integer, ItemLabelPosition> ( this.positiveItemLabelPositionMap );
        }
        if ( this.negativeItemLabelPositionMap != null ) {
            clone.negativeItemLabelPositionMap = new HashMap<Integer, ItemLabelPosition> ( this.negativeItemLabelPositionMap );
        }
        if ( this.createEntitiesList != null ) {
            clone.createEntitiesList = ( BooleanList ) this.createEntitiesList.clone();
        }
        if ( this.legendShapeList != null ) {
            clone.legendShapeList = ( ShapeList ) this.legendShapeList.clone();
        }
        if ( this.legendTextFontMap != null ) {
            clone.legendTextFontMap = new HashMap<Integer, Font> ( this.legendTextFontMap );
        }
        if ( this.legendTextPaint != null ) {
            clone.legendTextPaint = ( PaintList ) this.legendTextPaint.clone();
        }
        clone.listenerList = new EventListenerList();
        clone.event = null;
        return clone;
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.paint, stream );
        SerialUtilities.writePaint ( this.basePaint, stream );
        SerialUtilities.writePaint ( this.fillPaint, stream );
        SerialUtilities.writePaint ( this.baseFillPaint, stream );
        SerialUtilities.writePaint ( this.outlinePaint, stream );
        SerialUtilities.writePaint ( this.baseOutlinePaint, stream );
        SerialUtilities.writeStroke ( this.stroke, stream );
        SerialUtilities.writeStroke ( this.baseStroke, stream );
        SerialUtilities.writeStroke ( this.outlineStroke, stream );
        SerialUtilities.writeStroke ( this.baseOutlineStroke, stream );
        SerialUtilities.writeShape ( this.shape, stream );
        SerialUtilities.writeShape ( this.baseShape, stream );
        SerialUtilities.writePaint ( this.itemLabelPaint, stream );
        SerialUtilities.writePaint ( this.baseItemLabelPaint, stream );
        SerialUtilities.writeShape ( this.baseLegendShape, stream );
        SerialUtilities.writePaint ( this.baseLegendTextPaint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.paint = SerialUtilities.readPaint ( stream );
        this.basePaint = SerialUtilities.readPaint ( stream );
        this.fillPaint = SerialUtilities.readPaint ( stream );
        this.baseFillPaint = SerialUtilities.readPaint ( stream );
        this.outlinePaint = SerialUtilities.readPaint ( stream );
        this.baseOutlinePaint = SerialUtilities.readPaint ( stream );
        this.stroke = SerialUtilities.readStroke ( stream );
        this.baseStroke = SerialUtilities.readStroke ( stream );
        this.outlineStroke = SerialUtilities.readStroke ( stream );
        this.baseOutlineStroke = SerialUtilities.readStroke ( stream );
        this.shape = SerialUtilities.readShape ( stream );
        this.baseShape = SerialUtilities.readShape ( stream );
        this.itemLabelPaint = SerialUtilities.readPaint ( stream );
        this.baseItemLabelPaint = SerialUtilities.readPaint ( stream );
        this.baseLegendShape = SerialUtilities.readShape ( stream );
        this.baseLegendTextPaint = SerialUtilities.readPaint ( stream );
        this.listenerList = new EventListenerList();
    }
    public Boolean getSeriesVisible() {
        return this.seriesVisible;
    }
    public void setSeriesVisible ( final Boolean visible ) {
        this.setSeriesVisible ( visible, true );
    }
    public void setSeriesVisible ( final Boolean visible, final boolean notify ) {
        this.seriesVisible = visible;
        if ( notify ) {
            final RendererChangeEvent e = new RendererChangeEvent ( this, true );
            this.notifyListeners ( e );
        }
    }
    public Boolean getSeriesVisibleInLegend() {
        return this.seriesVisibleInLegend;
    }
    public void setSeriesVisibleInLegend ( final Boolean visible ) {
        this.setSeriesVisibleInLegend ( visible, true );
    }
    public void setSeriesVisibleInLegend ( final Boolean visible, final boolean notify ) {
        this.seriesVisibleInLegend = visible;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public void setPaint ( final Paint paint ) {
        this.setPaint ( paint, true );
    }
    public void setPaint ( final Paint paint, final boolean notify ) {
        this.paint = paint;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public void setFillPaint ( final Paint paint ) {
        this.setFillPaint ( paint, true );
    }
    public void setFillPaint ( final Paint paint, final boolean notify ) {
        this.fillPaint = paint;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public void setOutlinePaint ( final Paint paint ) {
        this.setOutlinePaint ( paint, true );
    }
    public void setOutlinePaint ( final Paint paint, final boolean notify ) {
        this.outlinePaint = paint;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public void setStroke ( final Stroke stroke ) {
        this.setStroke ( stroke, true );
    }
    public void setStroke ( final Stroke stroke, final boolean notify ) {
        this.stroke = stroke;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public void setOutlineStroke ( final Stroke stroke ) {
        this.setOutlineStroke ( stroke, true );
    }
    public void setOutlineStroke ( final Stroke stroke, final boolean notify ) {
        this.outlineStroke = stroke;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public void setShape ( final Shape shape ) {
        this.setShape ( shape, true );
    }
    public void setShape ( final Shape shape, final boolean notify ) {
        this.shape = shape;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public void setItemLabelsVisible ( final boolean visible ) {
        this.setItemLabelsVisible ( Boolean.valueOf ( visible ) );
    }
    public void setItemLabelsVisible ( final Boolean visible ) {
        this.setItemLabelsVisible ( visible, true );
    }
    public void setItemLabelsVisible ( final Boolean visible, final boolean notify ) {
        this.itemLabelsVisible = visible;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public Font getItemLabelFont() {
        return this.itemLabelFont;
    }
    public void setItemLabelFont ( final Font font ) {
        this.setItemLabelFont ( font, true );
    }
    public void setItemLabelFont ( final Font font, final boolean notify ) {
        this.itemLabelFont = font;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public Paint getItemLabelPaint() {
        return this.itemLabelPaint;
    }
    public void setItemLabelPaint ( final Paint paint ) {
        this.setItemLabelPaint ( paint, true );
    }
    public void setItemLabelPaint ( final Paint paint, final boolean notify ) {
        this.itemLabelPaint = paint;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public ItemLabelPosition getPositiveItemLabelPosition() {
        return this.positiveItemLabelPosition;
    }
    public void setPositiveItemLabelPosition ( final ItemLabelPosition position ) {
        this.setPositiveItemLabelPosition ( position, true );
    }
    public void setPositiveItemLabelPosition ( final ItemLabelPosition position, final boolean notify ) {
        this.positiveItemLabelPosition = position;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public ItemLabelPosition getNegativeItemLabelPosition() {
        return this.negativeItemLabelPosition;
    }
    public void setNegativeItemLabelPosition ( final ItemLabelPosition position ) {
        this.setNegativeItemLabelPosition ( position, true );
    }
    public void setNegativeItemLabelPosition ( final ItemLabelPosition position, final boolean notify ) {
        this.negativeItemLabelPosition = position;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    public Boolean getCreateEntities() {
        return this.createEntities;
    }
    public void setCreateEntities ( final Boolean create ) {
        this.setCreateEntities ( create, true );
    }
    public void setCreateEntities ( final Boolean create, final boolean notify ) {
        this.createEntities = create;
        if ( notify ) {
            this.fireChangeEvent();
        }
    }
    static {
        ZERO = new Double ( 0.0 );
        DEFAULT_PAINT = Color.blue;
        DEFAULT_OUTLINE_PAINT = Color.gray;
        DEFAULT_STROKE = new BasicStroke ( 1.0f );
        DEFAULT_OUTLINE_STROKE = new BasicStroke ( 1.0f );
        DEFAULT_SHAPE = new Rectangle2D.Double ( -3.0, -3.0, 6.0, 6.0 );
        DEFAULT_VALUE_LABEL_FONT = new Font ( "SansSerif", 0, 10 );
        DEFAULT_VALUE_LABEL_PAINT = Color.black;
        ADJ = Math.cos ( 0.5235987755982988 );
        OPP = Math.sin ( 0.5235987755982988 );
    }
}
