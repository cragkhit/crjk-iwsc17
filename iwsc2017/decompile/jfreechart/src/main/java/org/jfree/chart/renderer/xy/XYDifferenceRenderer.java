package org.jfree.chart.renderer.xy;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.PaintUtilities;
import org.jfree.data.general.Dataset;
import org.jfree.chart.LegendItem;
import java.awt.geom.GeneralPath;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.ui.RectangleEdge;
import java.awt.Stroke;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.util.ShapeUtilities;
import org.jfree.chart.plot.PlotOrientation;
import java.util.List;
import java.util.Collections;
import java.util.Collection;
import java.util.LinkedList;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.plot.XYPlot;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.chart.event.RendererChangeEvent;
import java.awt.geom.Line2D;
import org.jfree.chart.util.ParamChecks;
import java.awt.Color;
import java.awt.Shape;
import java.awt.Paint;
import org.jfree.util.PublicCloneable;
public class XYDifferenceRenderer extends AbstractXYItemRenderer implements XYItemRenderer, PublicCloneable {
    private static final long serialVersionUID = -8447915602375584857L;
    private transient Paint positivePaint;
    private transient Paint negativePaint;
    private boolean shapesVisible;
    private transient Shape legendLine;
    private boolean roundXCoordinates;
    public XYDifferenceRenderer() {
        this ( Color.green, Color.red, false );
    }
    public XYDifferenceRenderer ( final Paint positivePaint, final Paint negativePaint, final boolean shapes ) {
        ParamChecks.nullNotPermitted ( positivePaint, "positivePaint" );
        ParamChecks.nullNotPermitted ( negativePaint, "negativePaint" );
        this.positivePaint = positivePaint;
        this.negativePaint = negativePaint;
        this.shapesVisible = shapes;
        this.legendLine = new Line2D.Double ( -7.0, 0.0, 7.0, 0.0 );
        this.roundXCoordinates = false;
    }
    public Paint getPositivePaint() {
        return this.positivePaint;
    }
    public void setPositivePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.positivePaint = paint;
        this.fireChangeEvent();
    }
    public Paint getNegativePaint() {
        return this.negativePaint;
    }
    public void setNegativePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.negativePaint = paint;
        this.notifyListeners ( new RendererChangeEvent ( this ) );
    }
    public boolean getShapesVisible() {
        return this.shapesVisible;
    }
    public void setShapesVisible ( final boolean flag ) {
        this.shapesVisible = flag;
        this.fireChangeEvent();
    }
    public Shape getLegendLine() {
        return this.legendLine;
    }
    public void setLegendLine ( final Shape line ) {
        ParamChecks.nullNotPermitted ( line, "line" );
        this.legendLine = line;
        this.fireChangeEvent();
    }
    public boolean getRoundXCoordinates() {
        return this.roundXCoordinates;
    }
    public void setRoundXCoordinates ( final boolean round ) {
        this.roundXCoordinates = round;
        this.fireChangeEvent();
    }
    @Override
    public XYItemRendererState initialise ( final Graphics2D g2, final Rectangle2D dataArea, final XYPlot plot, final XYDataset data, final PlotRenderingInfo info ) {
        final XYItemRendererState state = super.initialise ( g2, dataArea, plot, data, info );
        state.setProcessVisibleItemsOnly ( false );
        return state;
    }
    @Override
    public int getPassCount() {
        return 2;
    }
    @Override
    public void drawItem ( final Graphics2D g2, final XYItemRendererState state, final Rectangle2D dataArea, final PlotRenderingInfo info, final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis, final XYDataset dataset, final int series, final int item, final CrosshairState crosshairState, final int pass ) {
        if ( pass == 0 ) {
            this.drawItemPass0 ( g2, dataArea, info, plot, domainAxis, rangeAxis, dataset, series, item, crosshairState );
        } else if ( pass == 1 ) {
            this.drawItemPass1 ( g2, dataArea, info, plot, domainAxis, rangeAxis, dataset, series, item, crosshairState );
        }
    }
    protected void drawItemPass0 ( final Graphics2D x_graphics, final Rectangle2D x_dataArea, final PlotRenderingInfo x_info, final XYPlot x_plot, final ValueAxis x_domainAxis, final ValueAxis x_rangeAxis, final XYDataset x_dataset, final int x_series, final int x_item, final CrosshairState x_crosshairState ) {
        if ( 0 != x_series || 0 != x_item ) {
            return;
        }
        final boolean b_impliedZeroSubtrahend = 1 == x_dataset.getSeriesCount();
        if ( this.isEitherSeriesDegenerate ( x_dataset, b_impliedZeroSubtrahend ) ) {
            return;
        }
        if ( !b_impliedZeroSubtrahend && this.areSeriesDisjoint ( x_dataset ) ) {
            return;
        }
        final LinkedList l_minuendXs = new LinkedList();
        final LinkedList l_minuendYs = new LinkedList();
        final LinkedList l_subtrahendXs = new LinkedList();
        final LinkedList l_subtrahendYs = new LinkedList();
        final LinkedList l_polygonXs = new LinkedList();
        final LinkedList l_polygonYs = new LinkedList();
        int l_minuendItem = 0;
        final int l_minuendItemCount = x_dataset.getItemCount ( 0 );
        Double l_minuendCurX = null;
        Double l_minuendNextX = null;
        Double l_minuendCurY = null;
        Double l_minuendNextY = null;
        double l_minuendMaxY = Double.NEGATIVE_INFINITY;
        double l_minuendMinY = Double.POSITIVE_INFINITY;
        int l_subtrahendItem = 0;
        int l_subtrahendItemCount = 0;
        Double l_subtrahendCurX = null;
        Double l_subtrahendNextX = null;
        Double l_subtrahendCurY = null;
        Double l_subtrahendNextY = null;
        double l_subtrahendMaxY = Double.NEGATIVE_INFINITY;
        double l_subtrahendMinY = Double.POSITIVE_INFINITY;
        if ( b_impliedZeroSubtrahend ) {
            l_subtrahendItem = 0;
            l_subtrahendItemCount = 2;
            l_subtrahendCurX = new Double ( x_dataset.getXValue ( 0, 0 ) );
            l_subtrahendNextX = new Double ( x_dataset.getXValue ( 0, l_minuendItemCount - 1 ) );
            l_subtrahendCurY = new Double ( 0.0 );
            l_subtrahendNextY = new Double ( 0.0 );
            l_subtrahendMaxY = 0.0;
            l_subtrahendMinY = 0.0;
            l_subtrahendXs.add ( l_subtrahendCurX );
            l_subtrahendYs.add ( l_subtrahendCurY );
        } else {
            l_subtrahendItemCount = x_dataset.getItemCount ( 1 );
        }
        boolean b_minuendDone = false;
        boolean b_minuendAdvanced = true;
        boolean b_minuendAtIntersect = false;
        boolean b_minuendFastForward = false;
        boolean b_subtrahendDone = false;
        boolean b_subtrahendAdvanced = true;
        boolean b_subtrahendAtIntersect = false;
        boolean b_subtrahendFastForward = false;
        boolean b_colinear = false;
        double l_x1 = 0.0;
        double l_y1 = 0.0;
        double l_x2 = 0.0;
        double l_y2 = 0.0;
        double l_x3 = 0.0;
        double l_y3 = 0.0;
        double l_x4 = 0.0;
        double l_y4 = 0.0;
        boolean b_fastForwardDone = false;
        while ( !b_fastForwardDone ) {
            l_x1 = x_dataset.getXValue ( 0, l_minuendItem );
            l_y1 = x_dataset.getYValue ( 0, l_minuendItem );
            l_x2 = x_dataset.getXValue ( 0, l_minuendItem + 1 );
            l_y2 = x_dataset.getYValue ( 0, l_minuendItem + 1 );
            l_minuendCurX = new Double ( l_x1 );
            l_minuendCurY = new Double ( l_y1 );
            l_minuendNextX = new Double ( l_x2 );
            l_minuendNextY = new Double ( l_y2 );
            if ( b_impliedZeroSubtrahend ) {
                l_x3 = l_subtrahendCurX;
                l_y3 = l_subtrahendCurY;
                l_x4 = l_subtrahendNextX;
                l_y4 = l_subtrahendNextY;
            } else {
                l_x3 = x_dataset.getXValue ( 1, l_subtrahendItem );
                l_y3 = x_dataset.getYValue ( 1, l_subtrahendItem );
                l_x4 = x_dataset.getXValue ( 1, l_subtrahendItem + 1 );
                l_y4 = x_dataset.getYValue ( 1, l_subtrahendItem + 1 );
                l_subtrahendCurX = new Double ( l_x3 );
                l_subtrahendCurY = new Double ( l_y3 );
                l_subtrahendNextX = new Double ( l_x4 );
                l_subtrahendNextY = new Double ( l_y4 );
            }
            if ( l_x2 <= l_x3 ) {
                ++l_minuendItem;
                b_minuendFastForward = true;
            } else if ( l_x4 <= l_x1 ) {
                ++l_subtrahendItem;
                b_subtrahendFastForward = true;
            } else {
                if ( l_x3 < l_x1 && l_x1 < l_x4 ) {
                    final double l_slope = ( l_y4 - l_y3 ) / ( l_x4 - l_x3 );
                    l_subtrahendCurX = l_minuendCurX;
                    l_subtrahendCurY = new Double ( l_slope * l_x1 + ( l_y3 - l_slope * l_x3 ) );
                    l_subtrahendXs.add ( l_subtrahendCurX );
                    l_subtrahendYs.add ( l_subtrahendCurY );
                }
                if ( l_x1 < l_x3 && l_x3 < l_x2 ) {
                    final double l_slope = ( l_y2 - l_y1 ) / ( l_x2 - l_x1 );
                    l_minuendCurX = l_subtrahendCurX;
                    l_minuendCurY = new Double ( l_slope * l_x3 + ( l_y1 - l_slope * l_x1 ) );
                    l_minuendXs.add ( l_minuendCurX );
                    l_minuendYs.add ( l_minuendCurY );
                }
                l_minuendMaxY = l_minuendCurY;
                l_minuendMinY = l_minuendCurY;
                l_subtrahendMaxY = l_subtrahendCurY;
                l_subtrahendMinY = l_subtrahendCurY;
                b_fastForwardDone = true;
            }
        }
        while ( !b_minuendDone && !b_subtrahendDone ) {
            if ( !b_minuendDone && !b_minuendFastForward && b_minuendAdvanced ) {
                l_x1 = x_dataset.getXValue ( 0, l_minuendItem );
                l_y1 = x_dataset.getYValue ( 0, l_minuendItem );
                l_minuendCurX = new Double ( l_x1 );
                l_minuendCurY = new Double ( l_y1 );
                if ( !b_minuendAtIntersect ) {
                    l_minuendXs.add ( l_minuendCurX );
                    l_minuendYs.add ( l_minuendCurY );
                }
                l_minuendMaxY = Math.max ( l_minuendMaxY, l_y1 );
                l_minuendMinY = Math.min ( l_minuendMinY, l_y1 );
                l_x2 = x_dataset.getXValue ( 0, l_minuendItem + 1 );
                l_y2 = x_dataset.getYValue ( 0, l_minuendItem + 1 );
                l_minuendNextX = new Double ( l_x2 );
                l_minuendNextY = new Double ( l_y2 );
            }
            if ( !b_impliedZeroSubtrahend && !b_subtrahendDone && !b_subtrahendFastForward && b_subtrahendAdvanced ) {
                l_x3 = x_dataset.getXValue ( 1, l_subtrahendItem );
                l_y3 = x_dataset.getYValue ( 1, l_subtrahendItem );
                l_subtrahendCurX = new Double ( l_x3 );
                l_subtrahendCurY = new Double ( l_y3 );
                if ( !b_subtrahendAtIntersect ) {
                    l_subtrahendXs.add ( l_subtrahendCurX );
                    l_subtrahendYs.add ( l_subtrahendCurY );
                }
                l_subtrahendMaxY = Math.max ( l_subtrahendMaxY, l_y3 );
                l_subtrahendMinY = Math.min ( l_subtrahendMinY, l_y3 );
                l_x4 = x_dataset.getXValue ( 1, l_subtrahendItem + 1 );
                l_y4 = x_dataset.getYValue ( 1, l_subtrahendItem + 1 );
                l_subtrahendNextX = new Double ( l_x4 );
                l_subtrahendNextY = new Double ( l_y4 );
            }
            b_minuendFastForward = false;
            b_subtrahendFastForward = false;
            Double l_intersectX = null;
            Double l_intersectY = null;
            boolean b_intersect = false;
            b_minuendAtIntersect = false;
            b_subtrahendAtIntersect = false;
            if ( l_x2 == l_x4 && l_y2 == l_y4 ) {
                if ( l_x1 == l_x3 && l_y1 == l_y3 ) {
                    b_colinear = true;
                } else {
                    l_intersectX = new Double ( l_x2 );
                    l_intersectY = new Double ( l_y2 );
                    b_intersect = true;
                    b_minuendAtIntersect = true;
                    b_subtrahendAtIntersect = true;
                }
            } else {
                final double l_denominator = ( l_y4 - l_y3 ) * ( l_x2 - l_x1 ) - ( l_x4 - l_x3 ) * ( l_y2 - l_y1 );
                final double l_deltaY = l_y1 - l_y3;
                final double l_deltaX = l_x1 - l_x3;
                final double l_numeratorA = ( l_x4 - l_x3 ) * l_deltaY - ( l_y4 - l_y3 ) * l_deltaX;
                final double l_numeratorB = ( l_x2 - l_x1 ) * l_deltaY - ( l_y2 - l_y1 ) * l_deltaX;
                if ( 0.0 == l_numeratorA && 0.0 == l_numeratorB && 0.0 == l_denominator ) {
                    b_colinear = true;
                } else if ( b_colinear ) {
                    l_minuendXs.clear();
                    l_minuendYs.clear();
                    l_subtrahendXs.clear();
                    l_subtrahendYs.clear();
                    l_polygonXs.clear();
                    l_polygonYs.clear();
                    b_colinear = false;
                    final boolean b_useMinuend = l_x3 <= l_x1 && l_x1 <= l_x4;
                    l_polygonXs.add ( b_useMinuend ? l_minuendCurX : l_subtrahendCurX );
                    l_polygonYs.add ( b_useMinuend ? l_minuendCurY : l_subtrahendCurY );
                }
                final double l_slopeA = l_numeratorA / l_denominator;
                final double l_slopeB = l_numeratorB / l_denominator;
                final boolean b_vertical = l_x1 == l_x2 && l_x3 == l_x4 && l_x2 == l_x4;
                if ( ( 0.0 < l_slopeA && l_slopeA <= 1.0 && 0.0 < l_slopeB && l_slopeB <= 1.0 ) || b_vertical ) {
                    double l_xi;
                    double l_yi;
                    if ( b_vertical ) {
                        b_colinear = false;
                        l_xi = l_x2;
                        l_yi = l_x4;
                    } else {
                        l_xi = l_x1 + l_slopeA * ( l_x2 - l_x1 );
                        l_yi = l_y1 + l_slopeA * ( l_y2 - l_y1 );
                    }
                    l_intersectX = new Double ( l_xi );
                    l_intersectY = new Double ( l_yi );
                    b_intersect = true;
                    b_minuendAtIntersect = ( l_xi == l_x2 && l_yi == l_y2 );
                    b_subtrahendAtIntersect = ( l_xi == l_x4 && l_yi == l_y4 );
                    l_minuendCurX = l_intersectX;
                    l_minuendCurY = l_intersectY;
                    l_subtrahendCurX = l_intersectX;
                    l_subtrahendCurY = l_intersectY;
                }
            }
            if ( b_intersect ) {
                l_polygonXs.addAll ( l_minuendXs );
                l_polygonYs.addAll ( l_minuendYs );
                l_polygonXs.add ( l_intersectX );
                l_polygonYs.add ( l_intersectY );
                Collections.reverse ( l_subtrahendXs );
                Collections.reverse ( l_subtrahendYs );
                l_polygonXs.addAll ( l_subtrahendXs );
                l_polygonYs.addAll ( l_subtrahendYs );
                final boolean b_positive = l_subtrahendMaxY <= l_minuendMaxY && l_subtrahendMinY <= l_minuendMinY;
                this.createPolygon ( x_graphics, x_dataArea, x_plot, x_domainAxis, x_rangeAxis, b_positive, l_polygonXs, l_polygonYs );
                l_minuendXs.clear();
                l_minuendYs.clear();
                l_subtrahendXs.clear();
                l_subtrahendYs.clear();
                l_polygonXs.clear();
                l_polygonYs.clear();
                l_minuendMinY = ( l_subtrahendMinY = ( l_subtrahendMaxY = ( l_minuendMaxY = l_intersectY ) ) );
                l_polygonXs.add ( l_intersectX );
                l_polygonYs.add ( l_intersectY );
            }
            if ( l_x2 <= l_x4 ) {
                ++l_minuendItem;
                b_minuendAdvanced = true;
            } else {
                b_minuendAdvanced = false;
            }
            if ( l_x4 <= l_x2 ) {
                ++l_subtrahendItem;
                b_subtrahendAdvanced = true;
            } else {
                b_subtrahendAdvanced = false;
            }
            b_minuendDone = ( l_minuendItem == l_minuendItemCount - 1 );
            b_subtrahendDone = ( l_subtrahendItem == l_subtrahendItemCount - 1 );
        }
        if ( b_minuendDone && l_x3 < l_x2 && l_x2 < l_x4 ) {
            final double l_slope = ( l_y4 - l_y3 ) / ( l_x4 - l_x3 );
            l_subtrahendNextX = l_minuendNextX;
            l_subtrahendNextY = new Double ( l_slope * l_x2 + ( l_y3 - l_slope * l_x3 ) );
        }
        if ( b_subtrahendDone && l_x1 < l_x4 && l_x4 < l_x2 ) {
            final double l_slope = ( l_y2 - l_y1 ) / ( l_x2 - l_x1 );
            l_minuendNextX = l_subtrahendNextX;
            l_minuendNextY = new Double ( l_slope * l_x4 + ( l_y1 - l_slope * l_x1 ) );
        }
        l_minuendMaxY = Math.max ( l_minuendMaxY, l_minuendNextY );
        l_subtrahendMaxY = Math.max ( l_subtrahendMaxY, l_subtrahendNextY );
        l_minuendMinY = Math.min ( l_minuendMinY, l_minuendNextY );
        l_subtrahendMinY = Math.min ( l_subtrahendMinY, l_subtrahendNextY );
        l_minuendXs.add ( l_minuendNextX );
        l_minuendYs.add ( l_minuendNextY );
        l_subtrahendXs.add ( l_subtrahendNextX );
        l_subtrahendYs.add ( l_subtrahendNextY );
        l_polygonXs.addAll ( l_minuendXs );
        l_polygonYs.addAll ( l_minuendYs );
        Collections.reverse ( l_subtrahendXs );
        Collections.reverse ( l_subtrahendYs );
        l_polygonXs.addAll ( l_subtrahendXs );
        l_polygonYs.addAll ( l_subtrahendYs );
        final boolean b_positive = l_subtrahendMaxY <= l_minuendMaxY && l_subtrahendMinY <= l_minuendMinY;
        this.createPolygon ( x_graphics, x_dataArea, x_plot, x_domainAxis, x_rangeAxis, b_positive, l_polygonXs, l_polygonYs );
    }
    protected void drawItemPass1 ( final Graphics2D x_graphics, final Rectangle2D x_dataArea, final PlotRenderingInfo x_info, final XYPlot x_plot, final ValueAxis x_domainAxis, final ValueAxis x_rangeAxis, final XYDataset x_dataset, final int x_series, final int x_item, final CrosshairState x_crosshairState ) {
        Shape l_entityArea = null;
        EntityCollection l_entities = null;
        if ( null != x_info ) {
            l_entities = x_info.getOwner().getEntityCollection();
        }
        final Paint l_seriesPaint = this.getItemPaint ( x_series, x_item );
        final Stroke l_seriesStroke = this.getItemStroke ( x_series, x_item );
        x_graphics.setPaint ( l_seriesPaint );
        x_graphics.setStroke ( l_seriesStroke );
        final PlotOrientation l_orientation = x_plot.getOrientation();
        final RectangleEdge l_domainAxisLocation = x_plot.getDomainAxisEdge();
        final RectangleEdge l_rangeAxisLocation = x_plot.getRangeAxisEdge();
        final double l_x0 = x_dataset.getXValue ( x_series, x_item );
        final double l_y0 = x_dataset.getYValue ( x_series, x_item );
        final double l_x = x_domainAxis.valueToJava2D ( l_x0, x_dataArea, l_domainAxisLocation );
        final double l_y = x_rangeAxis.valueToJava2D ( l_y0, x_dataArea, l_rangeAxisLocation );
        if ( this.getShapesVisible() ) {
            Shape l_shape = this.getItemShape ( x_series, x_item );
            if ( l_orientation == PlotOrientation.HORIZONTAL ) {
                l_shape = ShapeUtilities.createTranslatedShape ( l_shape, l_y, l_x );
            } else {
                l_shape = ShapeUtilities.createTranslatedShape ( l_shape, l_x, l_y );
            }
            if ( l_shape.intersects ( x_dataArea ) ) {
                x_graphics.setPaint ( this.getItemPaint ( x_series, x_item ) );
                x_graphics.fill ( l_shape );
            }
            l_entityArea = l_shape;
        }
        if ( null != l_entities ) {
            if ( null == l_entityArea ) {
                l_entityArea = new Rectangle2D.Double ( l_x - 2.0, l_y - 2.0, 4.0, 4.0 );
            }
            String l_tip = null;
            final XYToolTipGenerator l_tipGenerator = this.getToolTipGenerator ( x_series, x_item );
            if ( null != l_tipGenerator ) {
                l_tip = l_tipGenerator.generateToolTip ( x_dataset, x_series, x_item );
            }
            String l_url = null;
            final XYURLGenerator l_urlGenerator = this.getURLGenerator();
            if ( null != l_urlGenerator ) {
                l_url = l_urlGenerator.generateURL ( x_dataset, x_series, x_item );
            }
            final XYItemEntity l_entity = new XYItemEntity ( l_entityArea, x_dataset, x_series, x_item, l_tip, l_url );
            l_entities.add ( l_entity );
        }
        if ( this.isItemLabelVisible ( x_series, x_item ) ) {
            this.drawItemLabel ( x_graphics, l_orientation, x_dataset, x_series, x_item, l_x, l_y, l_y < 0.0 );
        }
        final int l_domainAxisIndex = x_plot.getDomainAxisIndex ( x_domainAxis );
        final int l_rangeAxisIndex = x_plot.getRangeAxisIndex ( x_rangeAxis );
        this.updateCrosshairValues ( x_crosshairState, l_x0, l_y0, l_domainAxisIndex, l_rangeAxisIndex, l_x, l_y, l_orientation );
        if ( 0 == x_item ) {
            return;
        }
        final double l_x2 = x_domainAxis.valueToJava2D ( x_dataset.getXValue ( x_series, x_item - 1 ), x_dataArea, l_domainAxisLocation );
        final double l_y2 = x_rangeAxis.valueToJava2D ( x_dataset.getYValue ( x_series, x_item - 1 ), x_dataArea, l_rangeAxisLocation );
        Line2D l_line = null;
        if ( PlotOrientation.HORIZONTAL == l_orientation ) {
            l_line = new Line2D.Double ( l_y, l_x, l_y2, l_x2 );
        } else if ( PlotOrientation.VERTICAL == l_orientation ) {
            l_line = new Line2D.Double ( l_x, l_y, l_x2, l_y2 );
        }
        if ( null != l_line && l_line.intersects ( x_dataArea ) ) {
            x_graphics.setPaint ( this.getItemPaint ( x_series, x_item ) );
            x_graphics.setStroke ( this.getItemStroke ( x_series, x_item ) );
            x_graphics.draw ( l_line );
        }
    }
    private boolean isEitherSeriesDegenerate ( final XYDataset x_dataset, final boolean x_impliedZeroSubtrahend ) {
        if ( x_impliedZeroSubtrahend ) {
            return x_dataset.getItemCount ( 0 ) < 2;
        }
        return x_dataset.getItemCount ( 0 ) < 2 || x_dataset.getItemCount ( 1 ) < 2;
    }
    private boolean areSeriesDisjoint ( final XYDataset x_dataset ) {
        final int l_minuendItemCount = x_dataset.getItemCount ( 0 );
        final double l_minuendFirst = x_dataset.getXValue ( 0, 0 );
        final double l_minuendLast = x_dataset.getXValue ( 0, l_minuendItemCount - 1 );
        final int l_subtrahendItemCount = x_dataset.getItemCount ( 1 );
        final double l_subtrahendFirst = x_dataset.getXValue ( 1, 0 );
        final double l_subtrahendLast = x_dataset.getXValue ( 1, l_subtrahendItemCount - 1 );
        return l_minuendLast < l_subtrahendFirst || l_subtrahendLast < l_minuendFirst;
    }
    private void createPolygon ( final Graphics2D x_graphics, final Rectangle2D x_dataArea, final XYPlot x_plot, final ValueAxis x_domainAxis, final ValueAxis x_rangeAxis, final boolean x_positive, final LinkedList x_xValues, final LinkedList x_yValues ) {
        final PlotOrientation l_orientation = x_plot.getOrientation();
        final RectangleEdge l_domainAxisLocation = x_plot.getDomainAxisEdge();
        final RectangleEdge l_rangeAxisLocation = x_plot.getRangeAxisEdge();
        final Object[] l_xValues = x_xValues.toArray();
        final Object[] l_yValues = x_yValues.toArray();
        final GeneralPath l_path = new GeneralPath();
        if ( PlotOrientation.VERTICAL == l_orientation ) {
            double l_x = x_domainAxis.valueToJava2D ( ( double ) l_xValues[0], x_dataArea, l_domainAxisLocation );
            if ( this.roundXCoordinates ) {
                l_x = Math.rint ( l_x );
            }
            double l_y = x_rangeAxis.valueToJava2D ( ( double ) l_yValues[0], x_dataArea, l_rangeAxisLocation );
            l_path.moveTo ( ( float ) l_x, ( float ) l_y );
            for ( int i = 1; i < l_xValues.length; ++i ) {
                l_x = x_domainAxis.valueToJava2D ( ( double ) l_xValues[i], x_dataArea, l_domainAxisLocation );
                if ( this.roundXCoordinates ) {
                    l_x = Math.rint ( l_x );
                }
                l_y = x_rangeAxis.valueToJava2D ( ( double ) l_yValues[i], x_dataArea, l_rangeAxisLocation );
                l_path.lineTo ( ( float ) l_x, ( float ) l_y );
            }
            l_path.closePath();
        } else {
            double l_x = x_domainAxis.valueToJava2D ( ( double ) l_xValues[0], x_dataArea, l_domainAxisLocation );
            if ( this.roundXCoordinates ) {
                l_x = Math.rint ( l_x );
            }
            double l_y = x_rangeAxis.valueToJava2D ( ( double ) l_yValues[0], x_dataArea, l_rangeAxisLocation );
            l_path.moveTo ( ( float ) l_y, ( float ) l_x );
            for ( int i = 1; i < l_xValues.length; ++i ) {
                l_x = x_domainAxis.valueToJava2D ( ( double ) l_xValues[i], x_dataArea, l_domainAxisLocation );
                if ( this.roundXCoordinates ) {
                    l_x = Math.rint ( l_x );
                }
                l_y = x_rangeAxis.valueToJava2D ( ( double ) l_yValues[i], x_dataArea, l_rangeAxisLocation );
                l_path.lineTo ( ( float ) l_y, ( float ) l_x );
            }
            l_path.closePath();
        }
        if ( l_path.intersects ( x_dataArea ) ) {
            x_graphics.setPaint ( x_positive ? this.getPositivePaint() : this.getNegativePaint() );
            x_graphics.fill ( l_path );
        }
    }
    @Override
    public LegendItem getLegendItem ( final int datasetIndex, final int series ) {
        LegendItem result = null;
        final XYPlot p = this.getPlot();
        if ( p != null ) {
            final XYDataset dataset = p.getDataset ( datasetIndex );
            if ( dataset != null && this.getItemVisible ( series, 0 ) ) {
                final String description;
                final String label = description = this.getLegendItemLabelGenerator().generateLabel ( dataset, series );
                String toolTipText = null;
                if ( this.getLegendItemToolTipGenerator() != null ) {
                    toolTipText = this.getLegendItemToolTipGenerator().generateLabel ( dataset, series );
                }
                String urlText = null;
                if ( this.getLegendItemURLGenerator() != null ) {
                    urlText = this.getLegendItemURLGenerator().generateLabel ( dataset, series );
                }
                final Paint paint = this.lookupSeriesPaint ( series );
                final Stroke stroke = this.lookupSeriesStroke ( series );
                final Shape line = this.getLegendLine();
                result = new LegendItem ( label, description, toolTipText, urlText, line, stroke, paint );
                result.setLabelFont ( this.lookupLegendTextFont ( series ) );
                final Paint labelPaint = this.lookupLegendTextPaint ( series );
                if ( labelPaint != null ) {
                    result.setLabelPaint ( labelPaint );
                }
                result.setDataset ( dataset );
                result.setDatasetIndex ( datasetIndex );
                result.setSeriesKey ( dataset.getSeriesKey ( series ) );
                result.setSeriesIndex ( series );
            }
        }
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof XYDifferenceRenderer ) ) {
            return false;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        final XYDifferenceRenderer that = ( XYDifferenceRenderer ) obj;
        return PaintUtilities.equal ( this.positivePaint, that.positivePaint ) && PaintUtilities.equal ( this.negativePaint, that.negativePaint ) && this.shapesVisible == that.shapesVisible && ShapeUtilities.equal ( this.legendLine, that.legendLine ) && this.roundXCoordinates == that.roundXCoordinates;
    }
    public Object clone() throws CloneNotSupportedException {
        final XYDifferenceRenderer clone = ( XYDifferenceRenderer ) super.clone();
        clone.legendLine = ShapeUtilities.clone ( this.legendLine );
        return clone;
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.positivePaint, stream );
        SerialUtilities.writePaint ( this.negativePaint, stream );
        SerialUtilities.writeShape ( this.legendLine, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.positivePaint = SerialUtilities.readPaint ( stream );
        this.negativePaint = SerialUtilities.readPaint ( stream );
        this.legendLine = SerialUtilities.readShape ( stream );
    }
}
