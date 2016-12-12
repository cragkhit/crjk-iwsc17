package org.jfree.chart.renderer.category;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.PaintUtilities;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.util.PaintAlpha;
import org.jfree.data.category.CategoryDataset;
import org.jfree.chart.axis.CategoryAxis;
import java.awt.geom.Point2D;
import java.awt.Font;
import org.jfree.ui.RectangleAnchor;
import org.jfree.text.TextUtilities;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.data.Range;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import java.awt.Stroke;
import java.awt.Image;
import java.awt.geom.Line2D;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Composite;
import java.awt.AlphaComposite;
import java.awt.geom.GeneralPath;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.CategoryPlot;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.chart.util.ParamChecks;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.ui.TextAnchor;
import org.jfree.chart.labels.ItemLabelAnchor;
import java.awt.Paint;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
import org.jfree.chart.Effect3D;
public class BarRenderer3D extends BarRenderer implements Effect3D, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 7686976503536003636L;
    public static final double DEFAULT_X_OFFSET = 12.0;
    public static final double DEFAULT_Y_OFFSET = 8.0;
    public static final Paint DEFAULT_WALL_PAINT;
    private double xOffset;
    private double yOffset;
    private transient Paint wallPaint;
    public BarRenderer3D() {
        this ( 12.0, 8.0 );
    }
    public BarRenderer3D ( final double xOffset, final double yOffset ) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.wallPaint = BarRenderer3D.DEFAULT_WALL_PAINT;
        final ItemLabelPosition p1 = new ItemLabelPosition ( ItemLabelAnchor.INSIDE12, TextAnchor.TOP_CENTER );
        this.setBasePositiveItemLabelPosition ( p1 );
        final ItemLabelPosition p2 = new ItemLabelPosition ( ItemLabelAnchor.INSIDE12, TextAnchor.TOP_CENTER );
        this.setBaseNegativeItemLabelPosition ( p2 );
    }
    @Override
    public double getXOffset() {
        return this.xOffset;
    }
    @Override
    public double getYOffset() {
        return this.yOffset;
    }
    public Paint getWallPaint() {
        return this.wallPaint;
    }
    public void setWallPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.wallPaint = paint;
        this.fireChangeEvent();
    }
    @Override
    public CategoryItemRendererState initialise ( final Graphics2D g2, final Rectangle2D dataArea, final CategoryPlot plot, final int rendererIndex, final PlotRenderingInfo info ) {
        final Rectangle2D adjusted = new Rectangle2D.Double ( dataArea.getX(), dataArea.getY() + this.getYOffset(), dataArea.getWidth() - this.getXOffset(), dataArea.getHeight() - this.getYOffset() );
        final CategoryItemRendererState state = super.initialise ( g2, adjusted, plot, rendererIndex, info );
        return state;
    }
    public void drawBackground ( final Graphics2D g2, final CategoryPlot plot, final Rectangle2D dataArea ) {
        final float x0 = ( float ) dataArea.getX();
        final float x = x0 + ( float ) Math.abs ( this.xOffset );
        final float x2 = ( float ) dataArea.getMaxX();
        final float x3 = x2 - ( float ) Math.abs ( this.xOffset );
        final float y0 = ( float ) dataArea.getMaxY();
        final float y = y0 - ( float ) Math.abs ( this.yOffset );
        final float y2 = ( float ) dataArea.getMinY();
        final float y3 = y2 + ( float ) Math.abs ( this.yOffset );
        final GeneralPath clip = new GeneralPath();
        clip.moveTo ( x0, y0 );
        clip.lineTo ( x0, y3 );
        clip.lineTo ( x, y2 );
        clip.lineTo ( x2, y2 );
        clip.lineTo ( x2, y );
        clip.lineTo ( x3, y0 );
        clip.closePath();
        final Composite originalComposite = g2.getComposite();
        g2.setComposite ( AlphaComposite.getInstance ( 3, plot.getBackgroundAlpha() ) );
        final Paint backgroundPaint = plot.getBackgroundPaint();
        if ( backgroundPaint != null ) {
            g2.setPaint ( backgroundPaint );
            g2.fill ( clip );
        }
        final GeneralPath leftWall = new GeneralPath();
        leftWall.moveTo ( x0, y0 );
        leftWall.lineTo ( x0, y3 );
        leftWall.lineTo ( x, y2 );
        leftWall.lineTo ( x, y );
        leftWall.closePath();
        g2.setPaint ( this.getWallPaint() );
        g2.fill ( leftWall );
        final GeneralPath bottomWall = new GeneralPath();
        bottomWall.moveTo ( x0, y0 );
        bottomWall.lineTo ( x, y );
        bottomWall.lineTo ( x2, y );
        bottomWall.lineTo ( x3, y0 );
        bottomWall.closePath();
        g2.setPaint ( this.getWallPaint() );
        g2.fill ( bottomWall );
        g2.setPaint ( Color.lightGray );
        final Line2D corner = new Line2D.Double ( x0, y0, x, y );
        g2.draw ( corner );
        corner.setLine ( x, y, x, y2 );
        g2.draw ( corner );
        corner.setLine ( x, y, x2, y );
        g2.draw ( corner );
        final Image backgroundImage = plot.getBackgroundImage();
        if ( backgroundImage != null ) {
            final Rectangle2D adjusted = new Rectangle2D.Double ( dataArea.getX() + this.getXOffset(), dataArea.getY(), dataArea.getWidth() - this.getXOffset(), dataArea.getHeight() - this.getYOffset() );
            plot.drawBackgroundImage ( g2, adjusted );
        }
        g2.setComposite ( originalComposite );
    }
    public void drawOutline ( final Graphics2D g2, final CategoryPlot plot, final Rectangle2D dataArea ) {
        final float x0 = ( float ) dataArea.getX();
        final float x = x0 + ( float ) Math.abs ( this.xOffset );
        final float x2 = ( float ) dataArea.getMaxX();
        final float x3 = x2 - ( float ) Math.abs ( this.xOffset );
        final float y0 = ( float ) dataArea.getMaxY();
        final float y = y0 - ( float ) Math.abs ( this.yOffset );
        final float y2 = ( float ) dataArea.getMinY();
        final float y3 = y2 + ( float ) Math.abs ( this.yOffset );
        final GeneralPath clip = new GeneralPath();
        clip.moveTo ( x0, y0 );
        clip.lineTo ( x0, y3 );
        clip.lineTo ( x, y2 );
        clip.lineTo ( x2, y2 );
        clip.lineTo ( x2, y );
        clip.lineTo ( x3, y0 );
        clip.closePath();
        final Stroke outlineStroke = plot.getOutlineStroke();
        final Paint outlinePaint = plot.getOutlinePaint();
        if ( outlineStroke != null && outlinePaint != null ) {
            g2.setStroke ( outlineStroke );
            g2.setPaint ( outlinePaint );
            g2.draw ( clip );
        }
    }
    public void drawDomainGridline ( final Graphics2D g2, final CategoryPlot plot, final Rectangle2D dataArea, final double value ) {
        Line2D line1 = null;
        Line2D line2 = null;
        final PlotOrientation orientation = plot.getOrientation();
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            final double y1 = value - this.getYOffset();
            final double x0 = dataArea.getMinX();
            final double x = x0 + this.getXOffset();
            final double x2 = dataArea.getMaxX();
            line1 = new Line2D.Double ( x0, value, x, y1 );
            line2 = new Line2D.Double ( x, y1, x2, y1 );
        } else if ( orientation == PlotOrientation.VERTICAL ) {
            final double x3 = value + this.getXOffset();
            final double y2 = dataArea.getMaxY();
            final double y3 = y2 - this.getYOffset();
            final double y4 = dataArea.getMinY();
            line1 = new Line2D.Double ( value, y2, x3, y3 );
            line2 = new Line2D.Double ( x3, y3, x3, y4 );
        }
        final Paint paint = plot.getDomainGridlinePaint();
        final Stroke stroke = plot.getDomainGridlineStroke();
        g2.setPaint ( ( paint != null ) ? paint : Plot.DEFAULT_OUTLINE_PAINT );
        g2.setStroke ( ( stroke != null ) ? stroke : Plot.DEFAULT_OUTLINE_STROKE );
        g2.draw ( line1 );
        g2.draw ( line2 );
    }
    public void drawRangeGridline ( final Graphics2D g2, final CategoryPlot plot, final ValueAxis axis, final Rectangle2D dataArea, final double value ) {
        final Range range = axis.getRange();
        if ( !range.contains ( value ) ) {
            return;
        }
        final Rectangle2D adjusted = new Rectangle2D.Double ( dataArea.getX(), dataArea.getY() + this.getYOffset(), dataArea.getWidth() - this.getXOffset(), dataArea.getHeight() - this.getYOffset() );
        Line2D line1 = null;
        Line2D line2 = null;
        final PlotOrientation orientation = plot.getOrientation();
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            final double x0 = axis.valueToJava2D ( value, adjusted, plot.getRangeAxisEdge() );
            final double x = x0 + this.getXOffset();
            final double y0 = dataArea.getMaxY();
            final double y = y0 - this.getYOffset();
            final double y2 = dataArea.getMinY();
            line1 = new Line2D.Double ( x0, y0, x, y );
            line2 = new Line2D.Double ( x, y, x, y2 );
        } else if ( orientation == PlotOrientation.VERTICAL ) {
            final double y3 = axis.valueToJava2D ( value, adjusted, plot.getRangeAxisEdge() );
            final double y4 = y3 - this.getYOffset();
            final double x2 = dataArea.getMinX();
            final double x3 = x2 + this.getXOffset();
            final double x4 = dataArea.getMaxX();
            line1 = new Line2D.Double ( x2, y3, x3, y4 );
            line2 = new Line2D.Double ( x3, y4, x4, y4 );
        }
        final Paint paint = plot.getRangeGridlinePaint();
        final Stroke stroke = plot.getRangeGridlineStroke();
        g2.setPaint ( ( paint != null ) ? paint : Plot.DEFAULT_OUTLINE_PAINT );
        g2.setStroke ( ( stroke != null ) ? stroke : Plot.DEFAULT_OUTLINE_STROKE );
        g2.draw ( line1 );
        g2.draw ( line2 );
    }
    public void drawRangeLine ( final Graphics2D g2, final CategoryPlot plot, final ValueAxis axis, final Rectangle2D dataArea, final double value, final Paint paint, final Stroke stroke ) {
        final Range range = axis.getRange();
        if ( !range.contains ( value ) ) {
            return;
        }
        final Rectangle2D adjusted = new Rectangle2D.Double ( dataArea.getX(), dataArea.getY() + this.getYOffset(), dataArea.getWidth() - this.getXOffset(), dataArea.getHeight() - this.getYOffset() );
        Line2D line1 = null;
        Line2D line2 = null;
        final PlotOrientation orientation = plot.getOrientation();
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            final double x0 = axis.valueToJava2D ( value, adjusted, plot.getRangeAxisEdge() );
            final double x = x0 + this.getXOffset();
            final double y0 = dataArea.getMaxY();
            final double y = y0 - this.getYOffset();
            final double y2 = dataArea.getMinY();
            line1 = new Line2D.Double ( x0, y0, x, y );
            line2 = new Line2D.Double ( x, y, x, y2 );
        } else if ( orientation == PlotOrientation.VERTICAL ) {
            final double y3 = axis.valueToJava2D ( value, adjusted, plot.getRangeAxisEdge() );
            final double y4 = y3 - this.getYOffset();
            final double x2 = dataArea.getMinX();
            final double x3 = x2 + this.getXOffset();
            final double x4 = dataArea.getMaxX();
            line1 = new Line2D.Double ( x2, y3, x3, y4 );
            line2 = new Line2D.Double ( x3, y4, x4, y4 );
        }
        g2.setPaint ( paint );
        g2.setStroke ( stroke );
        g2.draw ( line1 );
        g2.draw ( line2 );
    }
    public void drawRangeMarker ( final Graphics2D g2, final CategoryPlot plot, final ValueAxis axis, final Marker marker, final Rectangle2D dataArea ) {
        final Rectangle2D adjusted = new Rectangle2D.Double ( dataArea.getX(), dataArea.getY() + this.getYOffset(), dataArea.getWidth() - this.getXOffset(), dataArea.getHeight() - this.getYOffset() );
        if ( marker instanceof ValueMarker ) {
            final ValueMarker vm = ( ValueMarker ) marker;
            final double value = vm.getValue();
            final Range range = axis.getRange();
            if ( !range.contains ( value ) ) {
                return;
            }
            GeneralPath path = null;
            final PlotOrientation orientation = plot.getOrientation();
            if ( orientation == PlotOrientation.HORIZONTAL ) {
                final float x = ( float ) axis.valueToJava2D ( value, adjusted, plot.getRangeAxisEdge() );
                final float y = ( float ) adjusted.getMaxY();
                path = new GeneralPath();
                path.moveTo ( x, y );
                path.lineTo ( ( float ) ( x + this.getXOffset() ), y - ( float ) this.getYOffset() );
                path.lineTo ( ( float ) ( x + this.getXOffset() ), ( float ) ( adjusted.getMinY() - this.getYOffset() ) );
                path.lineTo ( x, ( float ) adjusted.getMinY() );
                path.closePath();
            } else {
                if ( orientation != PlotOrientation.VERTICAL ) {
                    throw new IllegalStateException();
                }
                final float y2 = ( float ) axis.valueToJava2D ( value, adjusted, plot.getRangeAxisEdge() );
                final float x2 = ( float ) dataArea.getX();
                path = new GeneralPath();
                path.moveTo ( x2, y2 );
                path.lineTo ( x2 + ( float ) this.xOffset, y2 - ( float ) this.yOffset );
                path.lineTo ( ( float ) ( adjusted.getMaxX() + this.xOffset ), y2 - ( float ) this.yOffset );
                path.lineTo ( ( float ) adjusted.getMaxX(), y2 );
                path.closePath();
            }
            g2.setPaint ( marker.getPaint() );
            g2.fill ( path );
            g2.setPaint ( marker.getOutlinePaint() );
            g2.draw ( path );
            final String label = marker.getLabel();
            final RectangleAnchor anchor = marker.getLabelAnchor();
            if ( label != null ) {
                final Font labelFont = marker.getLabelFont();
                g2.setFont ( labelFont );
                g2.setPaint ( marker.getLabelPaint() );
                final Point2D coordinates = this.calculateRangeMarkerTextAnchorPoint ( g2, orientation, dataArea, path.getBounds2D(), marker.getLabelOffset(), LengthAdjustmentType.EXPAND, anchor );
                TextUtilities.drawAlignedString ( label, g2, ( float ) coordinates.getX(), ( float ) coordinates.getY(), marker.getLabelTextAnchor() );
            }
        } else {
            super.drawRangeMarker ( g2, plot, axis, marker, adjusted );
        }
    }
    @Override
    public void drawItem ( final Graphics2D g2, final CategoryItemRendererState state, final Rectangle2D dataArea, final CategoryPlot plot, final CategoryAxis domainAxis, final ValueAxis rangeAxis, final CategoryDataset dataset, final int row, final int column, final int pass ) {
        final int visibleRow = state.getVisibleSeriesIndex ( row );
        if ( visibleRow < 0 ) {
            return;
        }
        final Number dataValue = dataset.getValue ( row, column );
        if ( dataValue == null ) {
            return;
        }
        final double value = dataValue.doubleValue();
        final Rectangle2D adjusted = new Rectangle2D.Double ( dataArea.getX(), dataArea.getY() + this.getYOffset(), dataArea.getWidth() - this.getXOffset(), dataArea.getHeight() - this.getYOffset() );
        final PlotOrientation orientation = plot.getOrientation();
        final double barW0 = this.calculateBarW0 ( plot, orientation, adjusted, domainAxis, state, visibleRow, column );
        final double[] barL0L1 = this.calculateBarL0L1 ( value );
        if ( barL0L1 == null ) {
            return;
        }
        final RectangleEdge edge = plot.getRangeAxisEdge();
        final double transL0 = rangeAxis.valueToJava2D ( barL0L1[0], adjusted, edge );
        final double transL = rangeAxis.valueToJava2D ( barL0L1[1], adjusted, edge );
        final double barL0 = Math.min ( transL0, transL );
        final double barLength = Math.abs ( transL - transL0 );
        Rectangle2D bar;
        if ( orientation.isHorizontal() ) {
            bar = new Rectangle2D.Double ( barL0, barW0, barLength, state.getBarWidth() );
        } else {
            bar = new Rectangle2D.Double ( barW0, barL0, state.getBarWidth(), barLength );
        }
        if ( state.getElementHinting() ) {
            this.beginElementGroup ( g2, dataset.getRowKey ( row ), dataset.getColumnKey ( column ) );
        }
        final Paint itemPaint = this.getItemPaint ( row, column );
        g2.setPaint ( itemPaint );
        g2.fill ( bar );
        final double x0 = bar.getMinX();
        final double x = x0 + this.getXOffset();
        final double x2 = bar.getMaxX();
        final double x3 = x2 + this.getXOffset();
        final double y0 = bar.getMinY() - this.getYOffset();
        final double y = bar.getMinY();
        final double y2 = bar.getMaxY() - this.getYOffset();
        final double y3 = bar.getMaxY();
        GeneralPath bar3dRight = null;
        if ( barLength > 0.0 ) {
            bar3dRight = new GeneralPath();
            bar3dRight.moveTo ( ( float ) x2, ( float ) y3 );
            bar3dRight.lineTo ( ( float ) x2, ( float ) y );
            bar3dRight.lineTo ( ( float ) x3, ( float ) y0 );
            bar3dRight.lineTo ( ( float ) x3, ( float ) y2 );
            bar3dRight.closePath();
            g2.setPaint ( PaintAlpha.darker ( itemPaint ) );
            g2.fill ( bar3dRight );
        }
        final GeneralPath bar3dTop = new GeneralPath();
        bar3dTop.moveTo ( ( float ) x0, ( float ) y );
        bar3dTop.lineTo ( ( float ) x, ( float ) y0 );
        bar3dTop.lineTo ( ( float ) x3, ( float ) y0 );
        bar3dTop.lineTo ( ( float ) x2, ( float ) y );
        bar3dTop.closePath();
        g2.fill ( bar3dTop );
        if ( this.isDrawBarOutline() && state.getBarWidth() > 3.0 ) {
            g2.setStroke ( this.getItemOutlineStroke ( row, column ) );
            g2.setPaint ( this.getItemOutlinePaint ( row, column ) );
            g2.draw ( bar );
            if ( bar3dRight != null ) {
                g2.draw ( bar3dRight );
            }
            g2.draw ( bar3dTop );
        }
        if ( state.getElementHinting() ) {
            this.endElementGroup ( g2 );
        }
        final CategoryItemLabelGenerator generator = this.getItemLabelGenerator ( row, column );
        if ( generator != null && this.isItemLabelVisible ( row, column ) ) {
            this.drawItemLabel ( g2, dataset, row, column, plot, generator, bar, value < 0.0 );
        }
        final EntityCollection entities = state.getEntityCollection();
        if ( entities != null ) {
            final GeneralPath barOutline = new GeneralPath();
            barOutline.moveTo ( ( float ) x0, ( float ) y3 );
            barOutline.lineTo ( ( float ) x0, ( float ) y );
            barOutline.lineTo ( ( float ) x, ( float ) y0 );
            barOutline.lineTo ( ( float ) x3, ( float ) y0 );
            barOutline.lineTo ( ( float ) x3, ( float ) y2 );
            barOutline.lineTo ( ( float ) x2, ( float ) y3 );
            barOutline.closePath();
            this.addItemEntity ( entities, dataset, row, column, barOutline );
        }
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof BarRenderer3D ) ) {
            return false;
        }
        final BarRenderer3D that = ( BarRenderer3D ) obj;
        return this.xOffset == that.xOffset && this.yOffset == that.yOffset && PaintUtilities.equal ( this.wallPaint, that.wallPaint ) && super.equals ( obj );
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.wallPaint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.wallPaint = SerialUtilities.readPaint ( stream );
    }
    static {
        DEFAULT_WALL_PAINT = new Color ( 221, 221, 221 );
    }
}
