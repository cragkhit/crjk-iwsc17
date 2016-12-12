package org.jfree.chart.title;
import java.awt.Color;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.PaintUtilities;
import org.jfree.chart.block.BlockFrame;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.block.BlockResult;
import org.jfree.chart.entity.ChartEntity;
import java.awt.Shape;
import org.jfree.chart.entity.TitleEntity;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.block.EntityBlockParams;
import java.awt.geom.Rectangle2D;
import org.jfree.ui.Size2D;
import org.jfree.chart.block.RectangleConstraint;
import java.awt.Graphics2D;
import org.jfree.chart.block.CenterArrangement;
import org.jfree.chart.block.LabelBlock;
import org.jfree.chart.block.BorderArrangement;
import org.jfree.chart.block.Block;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.event.TitleChangeEvent;
import org.jfree.chart.util.ParamChecks;
import org.jfree.chart.block.ColumnArrangement;
import org.jfree.chart.block.FlowArrangement;
import org.jfree.util.SortOrder;
import org.jfree.chart.block.Arrangement;
import org.jfree.chart.block.BlockContainer;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.LegendItemSource;
import java.awt.Paint;
import java.awt.Font;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class LegendTitle extends Title implements Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 2644010518533854633L;
    public static final Font DEFAULT_ITEM_FONT;
    public static final Paint DEFAULT_ITEM_PAINT;
    private LegendItemSource[] sources;
    private transient Paint backgroundPaint;
    private RectangleEdge legendItemGraphicEdge;
    private RectangleAnchor legendItemGraphicAnchor;
    private RectangleAnchor legendItemGraphicLocation;
    private RectangleInsets legendItemGraphicPadding;
    private Font itemFont;
    private transient Paint itemPaint;
    private RectangleInsets itemLabelPadding;
    private BlockContainer items;
    private Arrangement hLayout;
    private Arrangement vLayout;
    private BlockContainer wrapper;
    private SortOrder sortOrder;
    public LegendTitle ( final LegendItemSource source ) {
        this ( source, new FlowArrangement(), new ColumnArrangement() );
    }
    public LegendTitle ( final LegendItemSource source, final Arrangement hLayout, final Arrangement vLayout ) {
        this.sources = new LegendItemSource[] { source };
        this.items = new BlockContainer ( hLayout );
        this.hLayout = hLayout;
        this.vLayout = vLayout;
        this.backgroundPaint = null;
        this.legendItemGraphicEdge = RectangleEdge.LEFT;
        this.legendItemGraphicAnchor = RectangleAnchor.CENTER;
        this.legendItemGraphicLocation = RectangleAnchor.CENTER;
        this.legendItemGraphicPadding = new RectangleInsets ( 2.0, 2.0, 2.0, 2.0 );
        this.itemFont = LegendTitle.DEFAULT_ITEM_FONT;
        this.itemPaint = LegendTitle.DEFAULT_ITEM_PAINT;
        this.itemLabelPadding = new RectangleInsets ( 2.0, 2.0, 2.0, 2.0 );
        this.sortOrder = SortOrder.ASCENDING;
    }
    public LegendItemSource[] getSources() {
        return this.sources;
    }
    public void setSources ( final LegendItemSource[] sources ) {
        ParamChecks.nullNotPermitted ( sources, "sources" );
        this.sources = sources;
        this.notifyListeners ( new TitleChangeEvent ( this ) );
    }
    public Paint getBackgroundPaint() {
        return this.backgroundPaint;
    }
    public void setBackgroundPaint ( final Paint paint ) {
        this.backgroundPaint = paint;
        this.notifyListeners ( new TitleChangeEvent ( this ) );
    }
    public RectangleEdge getLegendItemGraphicEdge() {
        return this.legendItemGraphicEdge;
    }
    public void setLegendItemGraphicEdge ( final RectangleEdge edge ) {
        ParamChecks.nullNotPermitted ( edge, "edge" );
        this.legendItemGraphicEdge = edge;
        this.notifyListeners ( new TitleChangeEvent ( this ) );
    }
    public RectangleAnchor getLegendItemGraphicAnchor() {
        return this.legendItemGraphicAnchor;
    }
    public void setLegendItemGraphicAnchor ( final RectangleAnchor anchor ) {
        ParamChecks.nullNotPermitted ( anchor, "anchor" );
        this.legendItemGraphicAnchor = anchor;
    }
    public RectangleAnchor getLegendItemGraphicLocation() {
        return this.legendItemGraphicLocation;
    }
    public void setLegendItemGraphicLocation ( final RectangleAnchor anchor ) {
        this.legendItemGraphicLocation = anchor;
    }
    public RectangleInsets getLegendItemGraphicPadding() {
        return this.legendItemGraphicPadding;
    }
    public void setLegendItemGraphicPadding ( final RectangleInsets padding ) {
        ParamChecks.nullNotPermitted ( padding, "padding" );
        this.legendItemGraphicPadding = padding;
        this.notifyListeners ( new TitleChangeEvent ( this ) );
    }
    public Font getItemFont() {
        return this.itemFont;
    }
    public void setItemFont ( final Font font ) {
        ParamChecks.nullNotPermitted ( font, "font" );
        this.itemFont = font;
        this.notifyListeners ( new TitleChangeEvent ( this ) );
    }
    public Paint getItemPaint() {
        return this.itemPaint;
    }
    public void setItemPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.itemPaint = paint;
        this.notifyListeners ( new TitleChangeEvent ( this ) );
    }
    public RectangleInsets getItemLabelPadding() {
        return this.itemLabelPadding;
    }
    public void setItemLabelPadding ( final RectangleInsets padding ) {
        ParamChecks.nullNotPermitted ( padding, "padding" );
        this.itemLabelPadding = padding;
        this.notifyListeners ( new TitleChangeEvent ( this ) );
    }
    public SortOrder getSortOrder() {
        return this.sortOrder;
    }
    public void setSortOrder ( final SortOrder order ) {
        ParamChecks.nullNotPermitted ( order, "order" );
        this.sortOrder = order;
        this.notifyListeners ( new TitleChangeEvent ( this ) );
    }
    protected void fetchLegendItems() {
        this.items.clear();
        final RectangleEdge p = this.getPosition();
        if ( RectangleEdge.isTopOrBottom ( p ) ) {
            this.items.setArrangement ( this.hLayout );
        } else {
            this.items.setArrangement ( this.vLayout );
        }
        if ( this.sortOrder.equals ( ( Object ) SortOrder.ASCENDING ) ) {
            for ( int s = 0; s < this.sources.length; ++s ) {
                final LegendItemCollection legendItems = this.sources[s].getLegendItems();
                if ( legendItems != null ) {
                    for ( int i = 0; i < legendItems.getItemCount(); ++i ) {
                        this.addItemBlock ( legendItems.get ( i ) );
                    }
                }
            }
        } else {
            for ( int s = this.sources.length - 1; s >= 0; --s ) {
                final LegendItemCollection legendItems = this.sources[s].getLegendItems();
                if ( legendItems != null ) {
                    for ( int i = legendItems.getItemCount() - 1; i >= 0; --i ) {
                        this.addItemBlock ( legendItems.get ( i ) );
                    }
                }
            }
        }
    }
    private void addItemBlock ( final LegendItem item ) {
        final Block block = this.createLegendItemBlock ( item );
        this.items.add ( block );
    }
    protected Block createLegendItemBlock ( final LegendItem item ) {
        final LegendGraphic lg = new LegendGraphic ( item.getShape(), item.getFillPaint() );
        lg.setFillPaintTransformer ( item.getFillPaintTransformer() );
        lg.setShapeFilled ( item.isShapeFilled() );
        lg.setLine ( item.getLine() );
        lg.setLineStroke ( item.getLineStroke() );
        lg.setLinePaint ( item.getLinePaint() );
        lg.setLineVisible ( item.isLineVisible() );
        lg.setShapeVisible ( item.isShapeVisible() );
        lg.setShapeOutlineVisible ( item.isShapeOutlineVisible() );
        lg.setOutlinePaint ( item.getOutlinePaint() );
        lg.setOutlineStroke ( item.getOutlineStroke() );
        lg.setPadding ( this.legendItemGraphicPadding );
        final LegendItemBlockContainer legendItem = new LegendItemBlockContainer ( new BorderArrangement(), item.getDataset(), item.getSeriesKey() );
        lg.setShapeAnchor ( this.getLegendItemGraphicAnchor() );
        lg.setShapeLocation ( this.getLegendItemGraphicLocation() );
        legendItem.add ( lg, this.legendItemGraphicEdge );
        Font textFont = item.getLabelFont();
        if ( textFont == null ) {
            textFont = this.itemFont;
        }
        Paint textPaint = item.getLabelPaint();
        if ( textPaint == null ) {
            textPaint = this.itemPaint;
        }
        final LabelBlock labelBlock = new LabelBlock ( item.getLabel(), textFont, textPaint );
        labelBlock.setPadding ( this.itemLabelPadding );
        legendItem.add ( labelBlock );
        legendItem.setToolTipText ( item.getToolTipText() );
        legendItem.setURLText ( item.getURLText() );
        final BlockContainer result = new BlockContainer ( new CenterArrangement() );
        result.add ( legendItem );
        return result;
    }
    public BlockContainer getItemContainer() {
        return this.items;
    }
    public Size2D arrange ( final Graphics2D g2, final RectangleConstraint constraint ) {
        final Size2D result = new Size2D();
        this.fetchLegendItems();
        if ( this.items.isEmpty() ) {
            return result;
        }
        BlockContainer container = this.wrapper;
        if ( container == null ) {
            container = this.items;
        }
        final RectangleConstraint c = this.toContentConstraint ( constraint );
        final Size2D size = container.arrange ( g2, c );
        result.height = this.calculateTotalHeight ( size.height );
        result.width = this.calculateTotalWidth ( size.width );
        return result;
    }
    @Override
    public void draw ( final Graphics2D g2, final Rectangle2D area ) {
        this.draw ( g2, area, null );
    }
    public Object draw ( final Graphics2D g2, final Rectangle2D area, final Object params ) {
        Rectangle2D target = ( Rectangle2D ) area.clone();
        final Rectangle2D hotspot = ( Rectangle2D ) area.clone();
        StandardEntityCollection sec = null;
        if ( params instanceof EntityBlockParams && ( ( EntityBlockParams ) params ).getGenerateEntities() ) {
            sec = new StandardEntityCollection();
            sec.add ( new TitleEntity ( hotspot, this ) );
        }
        target = this.trimMargin ( target );
        if ( this.backgroundPaint != null ) {
            g2.setPaint ( this.backgroundPaint );
            g2.fill ( target );
        }
        final BlockFrame border = this.getFrame();
        border.draw ( g2, target );
        border.getInsets().trim ( target );
        BlockContainer container = this.wrapper;
        if ( container == null ) {
            container = this.items;
        }
        target = this.trimPadding ( target );
        final Object val = container.draw ( g2, target, params );
        if ( val instanceof BlockResult ) {
            final EntityCollection ec = ( ( BlockResult ) val ).getEntityCollection();
            if ( ec != null && sec != null ) {
                sec.addAll ( ec );
                ( ( BlockResult ) val ).setEntityCollection ( sec );
            }
        }
        return val;
    }
    public BlockContainer getWrapper() {
        return this.wrapper;
    }
    public void setWrapper ( final BlockContainer wrapper ) {
        this.wrapper = wrapper;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof LegendTitle ) ) {
            return false;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        final LegendTitle that = ( LegendTitle ) obj;
        return PaintUtilities.equal ( this.backgroundPaint, that.backgroundPaint ) && this.legendItemGraphicEdge == that.legendItemGraphicEdge && this.legendItemGraphicAnchor == that.legendItemGraphicAnchor && this.legendItemGraphicLocation == that.legendItemGraphicLocation && this.itemFont.equals ( that.itemFont ) && this.itemPaint.equals ( that.itemPaint ) && this.hLayout.equals ( that.hLayout ) && this.vLayout.equals ( that.vLayout ) && this.sortOrder.equals ( ( Object ) that.sortOrder );
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.backgroundPaint, stream );
        SerialUtilities.writePaint ( this.itemPaint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.backgroundPaint = SerialUtilities.readPaint ( stream );
        this.itemPaint = SerialUtilities.readPaint ( stream );
    }
    static {
        DEFAULT_ITEM_FONT = new Font ( "SansSerif", 0, 12 );
        DEFAULT_ITEM_PAINT = Color.black;
    }
}
