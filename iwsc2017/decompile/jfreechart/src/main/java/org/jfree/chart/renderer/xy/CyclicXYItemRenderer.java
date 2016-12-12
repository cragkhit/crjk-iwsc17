package org.jfree.chart.renderer.xy;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.DomainOrder;
import org.jfree.chart.axis.CyclicNumberAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.PlotRenderingInfo;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import java.io.Serializable;
public class CyclicXYItemRenderer extends StandardXYItemRenderer implements Serializable {
    private static final long serialVersionUID = 4035912243303764892L;
    public CyclicXYItemRenderer() {
    }
    public CyclicXYItemRenderer ( final int type ) {
        super ( type );
    }
    public CyclicXYItemRenderer ( final int type, final XYToolTipGenerator labelGenerator ) {
        super ( type, labelGenerator );
    }
    public CyclicXYItemRenderer ( final int type, final XYToolTipGenerator labelGenerator, final XYURLGenerator urlGenerator ) {
        super ( type, labelGenerator, urlGenerator );
    }
    @Override
    public void drawItem ( final Graphics2D g2, final XYItemRendererState state, final Rectangle2D dataArea, final PlotRenderingInfo info, final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis, final XYDataset dataset, final int series, final int item, final CrosshairState crosshairState, final int pass ) {
        if ( !this.getPlotLines() || ( ! ( domainAxis instanceof CyclicNumberAxis ) && ! ( rangeAxis instanceof CyclicNumberAxis ) ) || item <= 0 ) {
            super.drawItem ( g2, state, dataArea, info, plot, domainAxis, rangeAxis, dataset, series, item, crosshairState, pass );
            return;
        }
        double xn = dataset.getXValue ( series, item - 1 );
        double yn = dataset.getYValue ( series, item - 1 );
        if ( Double.isNaN ( yn ) ) {
            super.drawItem ( g2, state, dataArea, info, plot, domainAxis, rangeAxis, dataset, series, item, crosshairState, pass );
            return;
        }
        double[] x = new double[2];
        double[] y = new double[2];
        x[0] = xn;
        y[0] = yn;
        xn = dataset.getXValue ( series, item );
        yn = dataset.getYValue ( series, item );
        if ( Double.isNaN ( yn ) ) {
            return;
        }
        x[1] = xn;
        y[1] = yn;
        double xcycleBound = Double.NaN;
        double ycycleBound = Double.NaN;
        boolean xBoundMapping = false;
        boolean yBoundMapping = false;
        CyclicNumberAxis cnax = null;
        CyclicNumberAxis cnay = null;
        if ( domainAxis instanceof CyclicNumberAxis ) {
            cnax = ( CyclicNumberAxis ) domainAxis;
            xcycleBound = cnax.getCycleBound();
            xBoundMapping = cnax.isBoundMappedToLastCycle();
            if ( x[0] != x[1] && ( ( xcycleBound >= x[0] && xcycleBound <= x[1] ) || ( xcycleBound >= x[1] && xcycleBound <= x[0] ) ) ) {
                final double[] nx = new double[3];
                final double[] ny = new double[3];
                nx[0] = x[0];
                nx[2] = x[1];
                ny[0] = y[0];
                ny[2] = y[1];
                nx[1] = xcycleBound;
                ny[1] = ( y[1] - y[0] ) * ( xcycleBound - x[0] ) / ( x[1] - x[0] ) + y[0];
                x = nx;
                y = ny;
            }
        }
        if ( rangeAxis instanceof CyclicNumberAxis ) {
            cnay = ( CyclicNumberAxis ) rangeAxis;
            ycycleBound = cnay.getCycleBound();
            yBoundMapping = cnay.isBoundMappedToLastCycle();
            if ( y[0] != y[1] && ( ( ycycleBound >= y[0] && ycycleBound <= y[1] ) || ( ycycleBound >= y[1] && ycycleBound <= y[0] ) ) ) {
                final double[] nx = new double[x.length + 1];
                final double[] ny = new double[y.length + 1];
                nx[0] = x[0];
                nx[2] = x[1];
                ny[0] = y[0];
                ny[2] = y[1];
                ny[1] = ycycleBound;
                nx[1] = ( x[1] - x[0] ) * ( ycycleBound - y[0] ) / ( y[1] - y[0] ) + x[0];
                if ( x.length == 3 ) {
                    nx[3] = x[2];
                    ny[3] = y[2];
                }
                x = nx;
                y = ny;
            } else if ( x.length == 3 && y[1] != y[2] && ( ( ycycleBound >= y[1] && ycycleBound <= y[2] ) || ( ycycleBound >= y[2] && ycycleBound <= y[1] ) ) ) {
                final double[] nx = new double[4];
                final double[] ny = new double[4];
                nx[0] = x[0];
                nx[1] = x[1];
                nx[3] = x[2];
                ny[0] = y[0];
                ny[1] = y[1];
                ny[3] = y[2];
                ny[2] = ycycleBound;
                nx[2] = ( x[2] - x[1] ) * ( ycycleBound - y[1] ) / ( y[2] - y[1] ) + x[1];
                x = nx;
                y = ny;
            }
        }
        if ( x.length == 2 ) {
            super.drawItem ( g2, state, dataArea, info, plot, domainAxis, rangeAxis, dataset, series, item, crosshairState, pass );
            return;
        }
        final OverwriteDataSet newset = new OverwriteDataSet ( x, y, dataset );
        if ( cnax != null ) {
            if ( xcycleBound == x[0] ) {
                cnax.setBoundMappedToLastCycle ( x[1] <= xcycleBound );
            }
            if ( xcycleBound == x[1] ) {
                cnax.setBoundMappedToLastCycle ( x[0] <= xcycleBound );
            }
        }
        if ( cnay != null ) {
            if ( ycycleBound == y[0] ) {
                cnay.setBoundMappedToLastCycle ( y[1] <= ycycleBound );
            }
            if ( ycycleBound == y[1] ) {
                cnay.setBoundMappedToLastCycle ( y[0] <= ycycleBound );
            }
        }
        super.drawItem ( g2, state, dataArea, info, plot, domainAxis, rangeAxis, newset, series, 1, crosshairState, pass );
        if ( cnax != null ) {
            if ( xcycleBound == x[1] ) {
                cnax.setBoundMappedToLastCycle ( x[2] <= xcycleBound );
            }
            if ( xcycleBound == x[2] ) {
                cnax.setBoundMappedToLastCycle ( x[1] <= xcycleBound );
            }
        }
        if ( cnay != null ) {
            if ( ycycleBound == y[1] ) {
                cnay.setBoundMappedToLastCycle ( y[2] <= ycycleBound );
            }
            if ( ycycleBound == y[2] ) {
                cnay.setBoundMappedToLastCycle ( y[1] <= ycycleBound );
            }
        }
        super.drawItem ( g2, state, dataArea, info, plot, domainAxis, rangeAxis, newset, series, 2, crosshairState, pass );
        if ( x.length == 4 ) {
            if ( cnax != null ) {
                if ( xcycleBound == x[2] ) {
                    cnax.setBoundMappedToLastCycle ( x[3] <= xcycleBound );
                }
                if ( xcycleBound == x[3] ) {
                    cnax.setBoundMappedToLastCycle ( x[2] <= xcycleBound );
                }
            }
            if ( cnay != null ) {
                if ( ycycleBound == y[2] ) {
                    cnay.setBoundMappedToLastCycle ( y[3] <= ycycleBound );
                }
                if ( ycycleBound == y[3] ) {
                    cnay.setBoundMappedToLastCycle ( y[2] <= ycycleBound );
                }
            }
            super.drawItem ( g2, state, dataArea, info, plot, domainAxis, rangeAxis, newset, series, 3, crosshairState, pass );
        }
        if ( cnax != null ) {
            cnax.setBoundMappedToLastCycle ( xBoundMapping );
        }
        if ( cnay != null ) {
            cnay.setBoundMappedToLastCycle ( yBoundMapping );
        }
    }
    protected static class OverwriteDataSet implements XYDataset {
        protected XYDataset delegateSet;
        Double[] x;
        Double[] y;
        public OverwriteDataSet ( final double[] x, final double[] y, final XYDataset delegateSet ) {
            this.delegateSet = delegateSet;
            this.x = new Double[x.length];
            this.y = new Double[y.length];
            for ( int i = 0; i < x.length; ++i ) {
                this.x[i] = new Double ( x[i] );
                this.y[i] = new Double ( y[i] );
            }
        }
        @Override
        public DomainOrder getDomainOrder() {
            return DomainOrder.NONE;
        }
        @Override
        public int getItemCount ( final int series ) {
            return this.x.length;
        }
        @Override
        public Number getX ( final int series, final int item ) {
            return this.x[item];
        }
        @Override
        public double getXValue ( final int series, final int item ) {
            double result = Double.NaN;
            final Number xx = this.getX ( series, item );
            if ( xx != null ) {
                result = xx.doubleValue();
            }
            return result;
        }
        @Override
        public Number getY ( final int series, final int item ) {
            return this.y[item];
        }
        @Override
        public double getYValue ( final int series, final int item ) {
            double result = Double.NaN;
            final Number yy = this.getY ( series, item );
            if ( yy != null ) {
                result = yy.doubleValue();
            }
            return result;
        }
        @Override
        public int getSeriesCount() {
            return this.delegateSet.getSeriesCount();
        }
        @Override
        public Comparable getSeriesKey ( final int series ) {
            return this.delegateSet.getSeriesKey ( series );
        }
        @Override
        public int indexOf ( final Comparable seriesName ) {
            return this.delegateSet.indexOf ( seriesName );
        }
        @Override
        public void addChangeListener ( final DatasetChangeListener listener ) {
        }
        @Override
        public void removeChangeListener ( final DatasetChangeListener listener ) {
        }
        @Override
        public DatasetGroup getGroup() {
            return this.delegateSet.getGroup();
        }
        @Override
        public void setGroup ( final DatasetGroup group ) {
        }
    }
}
