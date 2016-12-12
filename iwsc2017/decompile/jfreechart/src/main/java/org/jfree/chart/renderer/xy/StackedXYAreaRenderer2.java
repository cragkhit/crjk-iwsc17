package org.jfree.chart.renderer.xy;
import java.awt.Paint;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.entity.EntityCollection;
import java.awt.geom.Area;
import java.awt.Shape;
import org.jfree.chart.plot.PlotOrientation;
import java.awt.geom.GeneralPath;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.PlotRenderingInfo;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class StackedXYAreaRenderer2 extends XYAreaRenderer2 implements Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 7752676509764539182L;
    private boolean roundXCoordinates;
    public StackedXYAreaRenderer2() {
        this ( null, null );
    }
    public StackedXYAreaRenderer2 ( final XYToolTipGenerator labelGenerator, final XYURLGenerator urlGenerator ) {
        super ( labelGenerator, urlGenerator );
        this.roundXCoordinates = true;
    }
    public boolean getRoundXCoordinates() {
        return this.roundXCoordinates;
    }
    public void setRoundXCoordinates ( final boolean round ) {
        this.roundXCoordinates = round;
        this.fireChangeEvent();
    }
    public Range findRangeBounds ( final XYDataset dataset ) {
        if ( dataset == null ) {
            return null;
        }
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        final TableXYDataset d = ( TableXYDataset ) dataset;
        for ( int itemCount = d.getItemCount(), i = 0; i < itemCount; ++i ) {
            final double[] stackValues = this.getStackValues ( ( TableXYDataset ) dataset, d.getSeriesCount(), i );
            min = Math.min ( min, stackValues[0] );
            max = Math.max ( max, stackValues[1] );
        }
        if ( min == Double.POSITIVE_INFINITY ) {
            return null;
        }
        return new Range ( min, max );
    }
    public int getPassCount() {
        return 1;
    }
    @Override
    public void drawItem ( final Graphics2D g2, final XYItemRendererState state, final Rectangle2D dataArea, final PlotRenderingInfo info, final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis, final XYDataset dataset, final int series, final int item, final CrosshairState crosshairState, final int pass ) {
        EntityCollection entities = null;
        if ( info != null ) {
            entities = info.getOwner().getEntityCollection();
        }
        final TableXYDataset tdataset = ( TableXYDataset ) dataset;
        final PlotOrientation orientation = plot.getOrientation();
        final double x1 = dataset.getXValue ( series, item );
        double y1 = dataset.getYValue ( series, item );
        if ( Double.isNaN ( y1 ) ) {
            y1 = 0.0;
        }
        final double[] stack1 = this.getStackValues ( tdataset, series, item );
        final double x2 = dataset.getXValue ( series, Math.max ( item - 1, 0 ) );
        double y2 = dataset.getYValue ( series, Math.max ( item - 1, 0 ) );
        if ( Double.isNaN ( y2 ) ) {
            y2 = 0.0;
        }
        final double[] stack2 = this.getStackValues ( tdataset, series, Math.max ( item - 1, 0 ) );
        final int itemCount = dataset.getItemCount ( series );
        final double x3 = dataset.getXValue ( series, Math.min ( item + 1, itemCount - 1 ) );
        double y3 = dataset.getYValue ( series, Math.min ( item + 1, itemCount - 1 ) );
        if ( Double.isNaN ( y3 ) ) {
            y3 = 0.0;
        }
        final double[] stack3 = this.getStackValues ( tdataset, series, Math.min ( item + 1, itemCount - 1 ) );
        final double xleft = ( x2 + x1 ) / 2.0;
        final double xright = ( x1 + x3 ) / 2.0;
        final double[] stackLeft = this.averageStackValues ( stack2, stack1 );
        final double[] stackRight = this.averageStackValues ( stack1, stack3 );
        final double[] adjStackLeft = this.adjustedStackValues ( stack2, stack1 );
        final double[] adjStackRight = this.adjustedStackValues ( stack1, stack3 );
        final RectangleEdge edge0 = plot.getDomainAxisEdge();
        float transX1 = ( float ) domainAxis.valueToJava2D ( x1, dataArea, edge0 );
        float transXLeft = ( float ) domainAxis.valueToJava2D ( xleft, dataArea, edge0 );
        float transXRight = ( float ) domainAxis.valueToJava2D ( xright, dataArea, edge0 );
        if ( this.roundXCoordinates ) {
            transX1 = Math.round ( transX1 );
            transXLeft = Math.round ( transXLeft );
            transXRight = Math.round ( transXRight );
        }
        final RectangleEdge edge = plot.getRangeAxisEdge();
        final GeneralPath left = new GeneralPath();
        final GeneralPath right = new GeneralPath();
        float transY1;
        if ( y1 >= 0.0 ) {
            transY1 = ( float ) rangeAxis.valueToJava2D ( y1 + stack1[1], dataArea, edge );
            final float transStack1 = ( float ) rangeAxis.valueToJava2D ( stack1[1], dataArea, edge );
            final float transStackLeft = ( float ) rangeAxis.valueToJava2D ( adjStackLeft[1], dataArea, edge );
            if ( y2 >= 0.0 ) {
                final double yleft = ( y2 + y1 ) / 2.0 + stackLeft[1];
                final float transYLeft = ( float ) rangeAxis.valueToJava2D ( yleft, dataArea, edge );
                if ( orientation == PlotOrientation.VERTICAL ) {
                    left.moveTo ( transX1, transY1 );
                    left.lineTo ( transX1, transStack1 );
                    left.lineTo ( transXLeft, transStackLeft );
                    left.lineTo ( transXLeft, transYLeft );
                } else {
                    left.moveTo ( transY1, transX1 );
                    left.lineTo ( transStack1, transX1 );
                    left.lineTo ( transStackLeft, transXLeft );
                    left.lineTo ( transYLeft, transXLeft );
                }
                left.closePath();
            } else {
                if ( orientation == PlotOrientation.VERTICAL ) {
                    left.moveTo ( transX1, transStack1 );
                    left.lineTo ( transX1, transY1 );
                    left.lineTo ( transXLeft, transStackLeft );
                } else {
                    left.moveTo ( transStack1, transX1 );
                    left.lineTo ( transY1, transX1 );
                    left.lineTo ( transStackLeft, transXLeft );
                }
                left.closePath();
            }
            final float transStackRight = ( float ) rangeAxis.valueToJava2D ( adjStackRight[1], dataArea, edge );
            if ( y3 >= 0.0 ) {
                final double yright = ( y1 + y3 ) / 2.0 + stackRight[1];
                final float transYRight = ( float ) rangeAxis.valueToJava2D ( yright, dataArea, edge );
                if ( orientation == PlotOrientation.VERTICAL ) {
                    right.moveTo ( transX1, transStack1 );
                    right.lineTo ( transX1, transY1 );
                    right.lineTo ( transXRight, transYRight );
                    right.lineTo ( transXRight, transStackRight );
                } else {
                    right.moveTo ( transStack1, transX1 );
                    right.lineTo ( transY1, transX1 );
                    right.lineTo ( transYRight, transXRight );
                    right.lineTo ( transStackRight, transXRight );
                }
                right.closePath();
            } else {
                if ( orientation == PlotOrientation.VERTICAL ) {
                    right.moveTo ( transX1, transStack1 );
                    right.lineTo ( transX1, transY1 );
                    right.lineTo ( transXRight, transStackRight );
                } else {
                    right.moveTo ( transStack1, transX1 );
                    right.lineTo ( transY1, transX1 );
                    right.lineTo ( transStackRight, transXRight );
                }
                right.closePath();
            }
        } else {
            transY1 = ( float ) rangeAxis.valueToJava2D ( y1 + stack1[0], dataArea, edge );
            final float transStack1 = ( float ) rangeAxis.valueToJava2D ( stack1[0], dataArea, edge );
            final float transStackLeft = ( float ) rangeAxis.valueToJava2D ( adjStackLeft[0], dataArea, edge );
            if ( y2 >= 0.0 ) {
                if ( orientation == PlotOrientation.VERTICAL ) {
                    left.moveTo ( transX1, transStack1 );
                    left.lineTo ( transX1, transY1 );
                    left.lineTo ( transXLeft, transStackLeft );
                } else {
                    left.moveTo ( transStack1, transX1 );
                    left.lineTo ( transY1, transX1 );
                    left.lineTo ( transStackLeft, transXLeft );
                }
                left.clone();
            } else {
                final double yleft = ( y2 + y1 ) / 2.0 + stackLeft[0];
                final float transYLeft = ( float ) rangeAxis.valueToJava2D ( yleft, dataArea, edge );
                if ( orientation == PlotOrientation.VERTICAL ) {
                    left.moveTo ( transX1, transY1 );
                    left.lineTo ( transX1, transStack1 );
                    left.lineTo ( transXLeft, transStackLeft );
                    left.lineTo ( transXLeft, transYLeft );
                } else {
                    left.moveTo ( transY1, transX1 );
                    left.lineTo ( transStack1, transX1 );
                    left.lineTo ( transStackLeft, transXLeft );
                    left.lineTo ( transYLeft, transXLeft );
                }
                left.closePath();
            }
            final float transStackRight = ( float ) rangeAxis.valueToJava2D ( adjStackRight[0], dataArea, edge );
            if ( y3 >= 0.0 ) {
                if ( orientation == PlotOrientation.VERTICAL ) {
                    right.moveTo ( transX1, transStack1 );
                    right.lineTo ( transX1, transY1 );
                    right.lineTo ( transXRight, transStackRight );
                } else {
                    right.moveTo ( transStack1, transX1 );
                    right.lineTo ( transY1, transX1 );
                    right.lineTo ( transStackRight, transXRight );
                }
                right.closePath();
            } else {
                final double yright = ( y1 + y3 ) / 2.0 + stackRight[0];
                final float transYRight = ( float ) rangeAxis.valueToJava2D ( yright, dataArea, edge );
                if ( orientation == PlotOrientation.VERTICAL ) {
                    right.moveTo ( transX1, transStack1 );
                    right.lineTo ( transX1, transY1 );
                    right.lineTo ( transXRight, transYRight );
                    right.lineTo ( transXRight, transStackRight );
                } else {
                    right.moveTo ( transStack1, transX1 );
                    right.lineTo ( transY1, transX1 );
                    right.lineTo ( transYRight, transXRight );
                    right.lineTo ( transStackRight, transXRight );
                }
                right.closePath();
            }
        }
        final Paint itemPaint = this.getItemPaint ( series, item );
        if ( pass == 0 ) {
            g2.setPaint ( itemPaint );
            g2.fill ( left );
            g2.fill ( right );
        }
        if ( entities != null ) {
            final Area dataAreaHotspot = new Area ( left );
            dataAreaHotspot.add ( new Area ( right ) );
            dataAreaHotspot.intersect ( new Area ( dataArea ) );
            if ( !dataAreaHotspot.isEmpty() ) {
                this.addEntity ( entities, dataAreaHotspot, dataset, series, item, transX1, transY1 );
            }
        }
    }
    private double[] getStackValues ( final TableXYDataset dataset, final int series, final int index ) {
        final double[] result = new double[2];
        for ( int i = 0; i < series; ++i ) {
            final double v = dataset.getYValue ( i, index );
            if ( !Double.isNaN ( v ) ) {
                if ( v >= 0.0 ) {
                    final double[] array = result;
                    final int n = 1;
                    array[n] += v;
                } else {
                    final double[] array2 = result;
                    final int n2 = 0;
                    array2[n2] += v;
                }
            }
        }
        return result;
    }
    private double[] averageStackValues ( final double[] stack1, final double[] stack2 ) {
        final double[] result = { ( stack1[0] + stack2[0] ) / 2.0, ( stack1[1] + stack2[1] ) / 2.0 };
        return result;
    }
    private double[] adjustedStackValues ( final double[] stack1, final double[] stack2 ) {
        final double[] result = new double[2];
        if ( stack1[0] == 0.0 || stack2[0] == 0.0 ) {
            result[0] = 0.0;
        } else {
            result[0] = ( stack1[0] + stack2[0] ) / 2.0;
        }
        if ( stack1[1] == 0.0 || stack2[1] == 0.0 ) {
            result[1] = 0.0;
        } else {
            result[1] = ( stack1[1] + stack2[1] ) / 2.0;
        }
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof StackedXYAreaRenderer2 ) ) {
            return false;
        }
        final StackedXYAreaRenderer2 that = ( StackedXYAreaRenderer2 ) obj;
        return this.roundXCoordinates == that.roundXCoordinates && super.equals ( obj );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
