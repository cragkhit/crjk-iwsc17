package org.jfree.chart.renderer.xy;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.DomainOrder;
import org.jfree.data.xy.XYDataset;
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
