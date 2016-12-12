package org.jfree.chart.renderer.xy;
import org.jfree.chart.entity.EntityCollection;
import java.awt.Stroke;
import java.awt.Paint;
import java.awt.Shape;
import org.jfree.util.ShapeUtilities;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.plot.XYPlot;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import java.awt.Polygon;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class XYStepAreaRenderer extends AbstractXYItemRenderer implements XYItemRenderer, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = -7311560779702649635L;
    public static final int SHAPES = 1;
    public static final int AREA = 2;
    public static final int AREA_AND_SHAPES = 3;
    private boolean shapesVisible;
    private boolean shapesFilled;
    private boolean plotArea;
    private boolean showOutline;
    protected transient Polygon pArea;
    private double rangeBase;
    private double stepPoint;
    public XYStepAreaRenderer() {
        this ( 2 );
    }
    public XYStepAreaRenderer ( final int type ) {
        this ( type, null, null );
    }
    public XYStepAreaRenderer ( final int type, final XYToolTipGenerator toolTipGenerator, final XYURLGenerator urlGenerator ) {
        this.pArea = null;
        this.setBaseToolTipGenerator ( toolTipGenerator );
        this.setURLGenerator ( urlGenerator );
        if ( type == 2 ) {
            this.plotArea = true;
        } else if ( type == 1 ) {
            this.shapesVisible = true;
        } else if ( type == 3 ) {
            this.plotArea = true;
            this.shapesVisible = true;
        }
        this.showOutline = false;
        this.stepPoint = 1.0;
    }
    public boolean isOutline() {
        return this.showOutline;
    }
    public void setOutline ( final boolean show ) {
        this.showOutline = show;
        this.fireChangeEvent();
    }
    public boolean getShapesVisible() {
        return this.shapesVisible;
    }
    public void setShapesVisible ( final boolean flag ) {
        this.shapesVisible = flag;
        this.fireChangeEvent();
    }
    public boolean isShapesFilled() {
        return this.shapesFilled;
    }
    public void setShapesFilled ( final boolean filled ) {
        this.shapesFilled = filled;
        this.fireChangeEvent();
    }
    public boolean getPlotArea() {
        return this.plotArea;
    }
    public void setPlotArea ( final boolean flag ) {
        this.plotArea = flag;
        this.fireChangeEvent();
    }
    public double getRangeBase() {
        return this.rangeBase;
    }
    public void setRangeBase ( final double val ) {
        this.rangeBase = val;
        this.fireChangeEvent();
    }
    public double getStepPoint() {
        return this.stepPoint;
    }
    public void setStepPoint ( final double stepPoint ) {
        if ( stepPoint < 0.0 || stepPoint > 1.0 ) {
            throw new IllegalArgumentException ( "Requires stepPoint in [0.0;1.0]" );
        }
        this.stepPoint = stepPoint;
        this.fireChangeEvent();
    }
    @Override
    public XYItemRendererState initialise ( final Graphics2D g2, final Rectangle2D dataArea, final XYPlot plot, final XYDataset data, final PlotRenderingInfo info ) {
        final XYItemRendererState state = super.initialise ( g2, dataArea, plot, data, info );
        state.setProcessVisibleItemsOnly ( false );
        return state;
    }
    @Override
    public void drawItem ( final Graphics2D g2, final XYItemRendererState state, final Rectangle2D dataArea, final PlotRenderingInfo info, final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis, final XYDataset dataset, final int series, final int item, final CrosshairState crosshairState, final int pass ) {
        final PlotOrientation orientation = plot.getOrientation();
        final int itemCount = dataset.getItemCount ( series );
        final Paint paint = this.getItemPaint ( series, item );
        final Stroke seriesStroke = this.getItemStroke ( series, item );
        g2.setPaint ( paint );
        g2.setStroke ( seriesStroke );
        final double x1 = dataset.getXValue ( series, item );
        final double y1 = dataset.getYValue ( series, item );
        double x2 = x1;
        double y2 = Double.isNaN ( y1 ) ? this.getRangeBase() : y1;
        double transX1 = domainAxis.valueToJava2D ( x2, dataArea, plot.getDomainAxisEdge() );
        double transY1 = rangeAxis.valueToJava2D ( y2, dataArea, plot.getRangeAxisEdge() );
        transY1 = restrictValueToDataArea ( transY1, plot, dataArea );
        if ( this.pArea == null && !Double.isNaN ( y1 ) ) {
            this.pArea = new Polygon();
            double transY2 = rangeAxis.valueToJava2D ( this.getRangeBase(), dataArea, plot.getRangeAxisEdge() );
            transY2 = restrictValueToDataArea ( transY2, plot, dataArea );
            if ( orientation == PlotOrientation.VERTICAL ) {
                this.pArea.addPoint ( ( int ) transX1, ( int ) transY2 );
            } else if ( orientation == PlotOrientation.HORIZONTAL ) {
                this.pArea.addPoint ( ( int ) transY2, ( int ) transX1 );
            }
        }
        if ( item > 0 ) {
            final double x3 = dataset.getXValue ( series, item - 1 );
            final double y3 = Double.isNaN ( y1 ) ? y1 : dataset.getYValue ( series, item - 1 );
            x2 = x3;
            y2 = ( Double.isNaN ( y3 ) ? this.getRangeBase() : y3 );
            final double transX2 = domainAxis.valueToJava2D ( x2, dataArea, plot.getDomainAxisEdge() );
            double transY3 = rangeAxis.valueToJava2D ( y2, dataArea, plot.getRangeAxisEdge() );
            transY3 = restrictValueToDataArea ( transY3, plot, dataArea );
            if ( Double.isNaN ( y1 ) ) {
                transX1 = transX2;
                transY3 = transY1;
            }
            if ( transY3 != transY1 ) {
                final double transXs = transX2 + this.getStepPoint() * ( transX1 - transX2 );
                if ( orientation == PlotOrientation.VERTICAL ) {
                    this.pArea.addPoint ( ( int ) transXs, ( int ) transY3 );
                    this.pArea.addPoint ( ( int ) transXs, ( int ) transY1 );
                } else if ( orientation == PlotOrientation.HORIZONTAL ) {
                    this.pArea.addPoint ( ( int ) transY3, ( int ) transXs );
                    this.pArea.addPoint ( ( int ) transY1, ( int ) transXs );
                }
            }
        }
        Shape shape = null;
        if ( !Double.isNaN ( y1 ) ) {
            if ( orientation == PlotOrientation.VERTICAL ) {
                this.pArea.addPoint ( ( int ) transX1, ( int ) transY1 );
            } else if ( orientation == PlotOrientation.HORIZONTAL ) {
                this.pArea.addPoint ( ( int ) transY1, ( int ) transX1 );
            }
            if ( this.getShapesVisible() ) {
                shape = this.getItemShape ( series, item );
                if ( orientation == PlotOrientation.VERTICAL ) {
                    shape = ShapeUtilities.createTranslatedShape ( shape, transX1, transY1 );
                } else if ( orientation == PlotOrientation.HORIZONTAL ) {
                    shape = ShapeUtilities.createTranslatedShape ( shape, transY1, transX1 );
                }
                if ( this.isShapesFilled() ) {
                    g2.fill ( shape );
                } else {
                    g2.draw ( shape );
                }
            } else if ( orientation == PlotOrientation.VERTICAL ) {
                shape = new Rectangle2D.Double ( transX1 - 2.0, transY1 - 2.0, 4.0, 4.0 );
            } else if ( orientation == PlotOrientation.HORIZONTAL ) {
                shape = new Rectangle2D.Double ( transY1 - 2.0, transX1 - 2.0, 4.0, 4.0 );
            }
        }
        if ( this.getPlotArea() && item > 0 && this.pArea != null && ( item == itemCount - 1 || Double.isNaN ( y1 ) ) ) {
            double transY4 = rangeAxis.valueToJava2D ( this.getRangeBase(), dataArea, plot.getRangeAxisEdge() );
            transY4 = restrictValueToDataArea ( transY4, plot, dataArea );
            if ( orientation == PlotOrientation.VERTICAL ) {
                this.pArea.addPoint ( ( int ) transX1, ( int ) transY4 );
            } else if ( orientation == PlotOrientation.HORIZONTAL ) {
                this.pArea.addPoint ( ( int ) transY4, ( int ) transX1 );
            }
            g2.fill ( this.pArea );
            if ( this.isOutline() ) {
                g2.setStroke ( plot.getOutlineStroke() );
                g2.setPaint ( plot.getOutlinePaint() );
                g2.draw ( this.pArea );
            }
            this.pArea = null;
        }
        if ( !Double.isNaN ( y1 ) ) {
            final int domainAxisIndex = plot.getDomainAxisIndex ( domainAxis );
            final int rangeAxisIndex = plot.getRangeAxisIndex ( rangeAxis );
            this.updateCrosshairValues ( crosshairState, x1, y1, domainAxisIndex, rangeAxisIndex, transX1, transY1, orientation );
        }
        final EntityCollection entities = state.getEntityCollection();
        if ( entities != null ) {
            this.addEntity ( entities, shape, dataset, series, item, transX1, transY1 );
        }
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof XYStepAreaRenderer ) ) {
            return false;
        }
        final XYStepAreaRenderer that = ( XYStepAreaRenderer ) obj;
        return this.showOutline == that.showOutline && this.shapesVisible == that.shapesVisible && this.shapesFilled == that.shapesFilled && this.plotArea == that.plotArea && this.rangeBase == that.rangeBase && this.stepPoint == that.stepPoint && super.equals ( obj );
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    protected static double restrictValueToDataArea ( double value, final XYPlot plot, final Rectangle2D dataArea ) {
        double min = 0.0;
        double max = 0.0;
        if ( plot.getOrientation() == PlotOrientation.VERTICAL ) {
            min = dataArea.getMinY();
            max = dataArea.getMaxY();
        } else if ( plot.getOrientation() == PlotOrientation.HORIZONTAL ) {
            min = dataArea.getMinX();
            max = dataArea.getMaxX();
        }
        if ( value < min ) {
            value = min;
        } else if ( value > max ) {
            value = max;
        }
        return value;
    }
}
