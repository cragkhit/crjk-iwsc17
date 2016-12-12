package org.jfree.chart.renderer.xy;
import java.awt.geom.Line2D;
import java.io.ObjectOutputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectInputStream;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import java.awt.geom.Area;
import org.jfree.util.ShapeUtilities;
import java.awt.Shape;
import java.awt.Point;
import org.jfree.chart.plot.PlotOrientation;
import java.util.Stack;
import java.awt.Polygon;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.data.Range;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.plot.XYPlot;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import java.awt.Stroke;
import java.awt.Paint;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class StackedXYAreaRenderer extends XYAreaRenderer implements Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 5217394318178570889L;
    private transient Paint shapePaint;
    private transient Stroke shapeStroke;
    public StackedXYAreaRenderer() {
        this ( 4 );
    }
    public StackedXYAreaRenderer ( final int type ) {
        this ( type, null, null );
    }
    public StackedXYAreaRenderer ( final int type, final XYToolTipGenerator labelGenerator, final XYURLGenerator urlGenerator ) {
        super ( type, labelGenerator, urlGenerator );
        this.shapePaint = null;
        this.shapeStroke = null;
    }
    public Paint getShapePaint() {
        return this.shapePaint;
    }
    public void setShapePaint ( final Paint shapePaint ) {
        this.shapePaint = shapePaint;
        this.fireChangeEvent();
    }
    public Stroke getShapeStroke() {
        return this.shapeStroke;
    }
    public void setShapeStroke ( final Stroke shapeStroke ) {
        this.shapeStroke = shapeStroke;
        this.fireChangeEvent();
    }
    @Override
    public XYItemRendererState initialise ( final Graphics2D g2, final Rectangle2D dataArea, final XYPlot plot, final XYDataset data, final PlotRenderingInfo info ) {
        final XYItemRendererState state = new StackedXYAreaRendererState ( info );
        state.setProcessVisibleItemsOnly ( false );
        return state;
    }
    public int getPassCount() {
        return 2;
    }
    public Range findRangeBounds ( final XYDataset dataset ) {
        if ( dataset != null ) {
            return DatasetUtilities.findStackedRangeBounds ( ( TableXYDataset ) dataset );
        }
        return null;
    }
    @Override
    public void drawItem ( final Graphics2D g2, final XYItemRendererState state, final Rectangle2D dataArea, final PlotRenderingInfo info, final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis, final XYDataset dataset, final int series, final int item, final CrosshairState crosshairState, final int pass ) {
        final PlotOrientation orientation = plot.getOrientation();
        final StackedXYAreaRendererState areaState = ( StackedXYAreaRendererState ) state;
        final TableXYDataset tdataset = ( TableXYDataset ) dataset;
        final int itemCount = tdataset.getItemCount();
        final double x1 = dataset.getXValue ( series, item );
        double y1 = dataset.getYValue ( series, item );
        boolean nullPoint = false;
        if ( Double.isNaN ( y1 ) ) {
            y1 = 0.0;
            nullPoint = true;
        }
        final double ph1 = this.getPreviousHeight ( tdataset, series, item );
        final double transX1 = domainAxis.valueToJava2D ( x1, dataArea, plot.getDomainAxisEdge() );
        final double transY1 = rangeAxis.valueToJava2D ( y1 + ph1, dataArea, plot.getRangeAxisEdge() );
        Paint seriesFillPaint;
        final Paint seriesPaint = seriesFillPaint = this.getItemPaint ( series, item );
        if ( this.getUseFillPaint() ) {
            seriesFillPaint = this.getItemFillPaint ( series, item );
        }
        final Stroke seriesStroke = this.getItemStroke ( series, item );
        if ( pass == 0 ) {
            if ( item == 0 ) {
                areaState.setSeriesArea ( new Polygon() );
                areaState.setLastSeriesPoints ( areaState.getCurrentSeriesPoints() );
                areaState.setCurrentSeriesPoints ( new Stack() );
                final double transY2 = rangeAxis.valueToJava2D ( ph1, dataArea, plot.getRangeAxisEdge() );
                if ( orientation == PlotOrientation.VERTICAL ) {
                    areaState.getSeriesArea().addPoint ( ( int ) transX1, ( int ) transY2 );
                } else if ( orientation == PlotOrientation.HORIZONTAL ) {
                    areaState.getSeriesArea().addPoint ( ( int ) transY2, ( int ) transX1 );
                }
            }
            if ( orientation == PlotOrientation.VERTICAL ) {
                final Point point = new Point ( ( int ) transX1, ( int ) transY1 );
                areaState.getSeriesArea().addPoint ( ( int ) point.getX(), ( int ) point.getY() );
                areaState.getCurrentSeriesPoints().push ( point );
            } else if ( orientation == PlotOrientation.HORIZONTAL ) {
                areaState.getSeriesArea().addPoint ( ( int ) transY1, ( int ) transX1 );
            }
            if ( this.getPlotLines() && item > 0 ) {
                final double x2 = dataset.getXValue ( series, item - 1 );
                final double y2 = dataset.getYValue ( series, item - 1 );
                final double ph2 = this.getPreviousHeight ( tdataset, series, item - 1 );
                final double transX2 = domainAxis.valueToJava2D ( x2, dataArea, plot.getDomainAxisEdge() );
                final double transY3 = rangeAxis.valueToJava2D ( y2 + ph2, dataArea, plot.getRangeAxisEdge() );
                if ( orientation == PlotOrientation.VERTICAL ) {
                    areaState.getLine().setLine ( transX2, transY3, transX1, transY1 );
                } else if ( orientation == PlotOrientation.HORIZONTAL ) {
                    areaState.getLine().setLine ( transY3, transX2, transY1, transX1 );
                }
                g2.setPaint ( seriesPaint );
                g2.setStroke ( seriesStroke );
                g2.draw ( areaState.getLine() );
            }
            if ( this.getPlotArea() && item > 0 && item == itemCount - 1 ) {
                final double transY2 = rangeAxis.valueToJava2D ( ph1, dataArea, plot.getRangeAxisEdge() );
                if ( orientation == PlotOrientation.VERTICAL ) {
                    areaState.getSeriesArea().addPoint ( ( int ) transX1, ( int ) transY2 );
                } else if ( orientation == PlotOrientation.HORIZONTAL ) {
                    areaState.getSeriesArea().addPoint ( ( int ) transY2, ( int ) transX1 );
                }
                if ( series != 0 ) {
                    final Stack points = areaState.getLastSeriesPoints();
                    while ( !points.empty() ) {
                        final Point point2 = points.pop();
                        areaState.getSeriesArea().addPoint ( ( int ) point2.getX(), ( int ) point2.getY() );
                    }
                }
                g2.setPaint ( seriesFillPaint );
                g2.setStroke ( seriesStroke );
                g2.fill ( areaState.getSeriesArea() );
                if ( this.isOutline() ) {
                    g2.setStroke ( this.lookupSeriesOutlineStroke ( series ) );
                    g2.setPaint ( this.lookupSeriesOutlinePaint ( series ) );
                    g2.draw ( areaState.getSeriesArea() );
                }
            }
            final int domainAxisIndex = plot.getDomainAxisIndex ( domainAxis );
            final int rangeAxisIndex = plot.getRangeAxisIndex ( rangeAxis );
            this.updateCrosshairValues ( crosshairState, x1, ph1 + y1, domainAxisIndex, rangeAxisIndex, transX1, transY1, orientation );
        } else if ( pass == 1 ) {
            Shape shape = null;
            if ( this.getPlotShapes() ) {
                shape = this.getItemShape ( series, item );
                if ( plot.getOrientation() == PlotOrientation.VERTICAL ) {
                    shape = ShapeUtilities.createTranslatedShape ( shape, transX1, transY1 );
                } else if ( plot.getOrientation() == PlotOrientation.HORIZONTAL ) {
                    shape = ShapeUtilities.createTranslatedShape ( shape, transY1, transX1 );
                }
                if ( !nullPoint ) {
                    if ( this.getShapePaint() != null ) {
                        g2.setPaint ( this.getShapePaint() );
                    } else {
                        g2.setPaint ( seriesPaint );
                    }
                    if ( this.getShapeStroke() != null ) {
                        g2.setStroke ( this.getShapeStroke() );
                    } else {
                        g2.setStroke ( seriesStroke );
                    }
                    g2.draw ( shape );
                }
            } else if ( plot.getOrientation() == PlotOrientation.VERTICAL ) {
                shape = new Rectangle2D.Double ( transX1 - 3.0, transY1 - 3.0, 6.0, 6.0 );
            } else if ( plot.getOrientation() == PlotOrientation.HORIZONTAL ) {
                shape = new Rectangle2D.Double ( transY1 - 3.0, transX1 - 3.0, 6.0, 6.0 );
            }
            if ( state.getInfo() != null ) {
                final EntityCollection entities = state.getEntityCollection();
                if ( entities != null && shape != null && !nullPoint ) {
                    final Area dataAreaHotspot = new Area ( shape );
                    dataAreaHotspot.intersect ( new Area ( dataArea ) );
                    if ( !dataAreaHotspot.isEmpty() ) {
                        String tip = null;
                        final XYToolTipGenerator generator = this.getToolTipGenerator ( series, item );
                        if ( generator != null ) {
                            tip = generator.generateToolTip ( dataset, series, item );
                        }
                        String url = null;
                        if ( this.getURLGenerator() != null ) {
                            url = this.getURLGenerator().generateURL ( dataset, series, item );
                        }
                        final XYItemEntity entity = new XYItemEntity ( dataAreaHotspot, dataset, series, item, tip, url );
                        entities.add ( entity );
                    }
                }
            }
        }
    }
    protected double getPreviousHeight ( final TableXYDataset dataset, final int series, final int index ) {
        double result = 0.0;
        for ( int i = 0; i < series; ++i ) {
            final double value = dataset.getYValue ( i, index );
            if ( !Double.isNaN ( value ) ) {
                result += value;
            }
        }
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof StackedXYAreaRenderer ) || !super.equals ( obj ) ) {
            return false;
        }
        final StackedXYAreaRenderer that = ( StackedXYAreaRenderer ) obj;
        return PaintUtilities.equal ( this.shapePaint, that.shapePaint ) && ObjectUtilities.equal ( ( Object ) this.shapeStroke, ( Object ) that.shapeStroke );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.shapePaint = SerialUtilities.readPaint ( stream );
        this.shapeStroke = SerialUtilities.readStroke ( stream );
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.shapePaint, stream );
        SerialUtilities.writeStroke ( this.shapeStroke, stream );
    }
    static class StackedXYAreaRendererState extends XYItemRendererState {
        private Polygon seriesArea;
        private Line2D line;
        private Stack lastSeriesPoints;
        private Stack currentSeriesPoints;
        public StackedXYAreaRendererState ( final PlotRenderingInfo info ) {
            super ( info );
            this.seriesArea = null;
            this.line = new Line2D.Double();
            this.lastSeriesPoints = new Stack();
            this.currentSeriesPoints = new Stack();
        }
        public Polygon getSeriesArea() {
            return this.seriesArea;
        }
        public void setSeriesArea ( final Polygon area ) {
            this.seriesArea = area;
        }
        public Line2D getLine() {
            return this.line;
        }
        public Stack getCurrentSeriesPoints() {
            return this.currentSeriesPoints;
        }
        public void setCurrentSeriesPoints ( final Stack points ) {
            this.currentSeriesPoints = points;
        }
        public Stack getLastSeriesPoints() {
            return this.lastSeriesPoints;
        }
        public void setLastSeriesPoints ( final Stack points ) {
            this.lastSeriesPoints = points;
        }
    }
}
