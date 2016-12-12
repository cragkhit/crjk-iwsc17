package org.jfree.chart.renderer.xy;
import org.jfree.chart.entity.EntityCollection;
import java.awt.geom.GeneralPath;
import java.awt.Shape;
import java.awt.geom.Line2D;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.PlotRenderingInfo;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.data.xy.VectorXYDataset;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class VectorRenderer extends AbstractXYItemRenderer implements XYItemRenderer, Cloneable, PublicCloneable, Serializable {
    private double baseLength;
    private double headLength;
    public VectorRenderer() {
        this.baseLength = 0.1;
        this.headLength = 0.14;
    }
    @Override
    public Range findDomainBounds ( final XYDataset dataset ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;
        final int seriesCount = dataset.getSeriesCount();
        if ( dataset instanceof VectorXYDataset ) {
            final VectorXYDataset vdataset = ( VectorXYDataset ) dataset;
            for ( int series = 0; series < seriesCount; ++series ) {
                for ( int itemCount = dataset.getItemCount ( series ), item = 0; item < itemCount; ++item ) {
                    final double delta = vdataset.getVectorXValue ( series, item );
                    double uvalue;
                    double lvalue;
                    if ( delta < 0.0 ) {
                        uvalue = vdataset.getXValue ( series, item );
                        lvalue = uvalue + delta;
                    } else {
                        lvalue = vdataset.getXValue ( series, item );
                        uvalue = lvalue + delta;
                    }
                    minimum = Math.min ( minimum, lvalue );
                    maximum = Math.max ( maximum, uvalue );
                }
            }
        } else {
            for ( int series2 = 0; series2 < seriesCount; ++series2 ) {
                for ( int itemCount2 = dataset.getItemCount ( series2 ), item2 = 0; item2 < itemCount2; ++item2 ) {
                    final double uvalue;
                    final double lvalue = uvalue = dataset.getXValue ( series2, item2 );
                    minimum = Math.min ( minimum, lvalue );
                    maximum = Math.max ( maximum, uvalue );
                }
            }
        }
        if ( minimum > maximum ) {
            return null;
        }
        return new Range ( minimum, maximum );
    }
    @Override
    public Range findRangeBounds ( final XYDataset dataset ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;
        final int seriesCount = dataset.getSeriesCount();
        if ( dataset instanceof VectorXYDataset ) {
            final VectorXYDataset vdataset = ( VectorXYDataset ) dataset;
            for ( int series = 0; series < seriesCount; ++series ) {
                for ( int itemCount = dataset.getItemCount ( series ), item = 0; item < itemCount; ++item ) {
                    final double delta = vdataset.getVectorYValue ( series, item );
                    double uvalue;
                    double lvalue;
                    if ( delta < 0.0 ) {
                        uvalue = vdataset.getYValue ( series, item );
                        lvalue = uvalue + delta;
                    } else {
                        lvalue = vdataset.getYValue ( series, item );
                        uvalue = lvalue + delta;
                    }
                    minimum = Math.min ( minimum, lvalue );
                    maximum = Math.max ( maximum, uvalue );
                }
            }
        } else {
            for ( int series2 = 0; series2 < seriesCount; ++series2 ) {
                for ( int itemCount2 = dataset.getItemCount ( series2 ), item2 = 0; item2 < itemCount2; ++item2 ) {
                    final double uvalue;
                    final double lvalue = uvalue = dataset.getYValue ( series2, item2 );
                    minimum = Math.min ( minimum, lvalue );
                    maximum = Math.max ( maximum, uvalue );
                }
            }
        }
        if ( minimum > maximum ) {
            return null;
        }
        return new Range ( minimum, maximum );
    }
    @Override
    public void drawItem ( final Graphics2D g2, final XYItemRendererState state, final Rectangle2D dataArea, final PlotRenderingInfo info, final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis, final XYDataset dataset, final int series, final int item, final CrosshairState crosshairState, final int pass ) {
        final double x = dataset.getXValue ( series, item );
        final double y = dataset.getYValue ( series, item );
        double dx = 0.0;
        double dy = 0.0;
        if ( dataset instanceof VectorXYDataset ) {
            dx = ( ( VectorXYDataset ) dataset ).getVectorXValue ( series, item );
            dy = ( ( VectorXYDataset ) dataset ).getVectorYValue ( series, item );
        }
        final double xx0 = domainAxis.valueToJava2D ( x, dataArea, plot.getDomainAxisEdge() );
        final double yy0 = rangeAxis.valueToJava2D ( y, dataArea, plot.getRangeAxisEdge() );
        final double xx = domainAxis.valueToJava2D ( x + dx, dataArea, plot.getDomainAxisEdge() );
        final double yy = rangeAxis.valueToJava2D ( y + dy, dataArea, plot.getRangeAxisEdge() );
        final PlotOrientation orientation = plot.getOrientation();
        Line2D line;
        if ( orientation.equals ( PlotOrientation.HORIZONTAL ) ) {
            line = new Line2D.Double ( yy0, xx0, yy, xx );
        } else {
            line = new Line2D.Double ( xx0, yy0, xx, yy );
        }
        g2.setPaint ( this.getItemPaint ( series, item ) );
        g2.setStroke ( this.getItemStroke ( series, item ) );
        g2.draw ( line );
        final double dxx = xx - xx0;
        final double dyy = yy - yy0;
        final double bx = xx0 + ( 1.0 - this.baseLength ) * dxx;
        final double by = yy0 + ( 1.0 - this.baseLength ) * dyy;
        final double cx = xx0 + ( 1.0 - this.headLength ) * dxx;
        final double cy = yy0 + ( 1.0 - this.headLength ) * dyy;
        double angle = 0.0;
        if ( dxx != 0.0 ) {
            angle = 1.5707963267948966 - Math.atan ( dyy / dxx );
        }
        final double deltaX = 2.0 * Math.cos ( angle );
        final double deltaY = 2.0 * Math.sin ( angle );
        final double leftx = cx + deltaX;
        final double lefty = cy - deltaY;
        final double rightx = cx - deltaX;
        final double righty = cy + deltaY;
        final GeneralPath p = new GeneralPath();
        if ( orientation == PlotOrientation.VERTICAL ) {
            p.moveTo ( ( float ) xx, ( float ) yy );
            p.lineTo ( ( float ) rightx, ( float ) righty );
            p.lineTo ( ( float ) bx, ( float ) by );
            p.lineTo ( ( float ) leftx, ( float ) lefty );
        } else {
            p.moveTo ( ( float ) yy, ( float ) xx );
            p.lineTo ( ( float ) righty, ( float ) rightx );
            p.lineTo ( ( float ) by, ( float ) bx );
            p.lineTo ( ( float ) lefty, ( float ) leftx );
        }
        p.closePath();
        g2.draw ( p );
        if ( info != null ) {
            final EntityCollection entities = info.getOwner().getEntityCollection();
            if ( entities != null ) {
                this.addEntity ( entities, line.getBounds(), dataset, series, item, 0.0, 0.0 );
            }
        }
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof VectorRenderer ) ) {
            return false;
        }
        final VectorRenderer that = ( VectorRenderer ) obj;
        return this.baseLength == that.baseLength && this.headLength == that.headLength && super.equals ( obj );
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
