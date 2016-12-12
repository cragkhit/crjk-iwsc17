package org.jfree.chart.entity;
import org.jfree.util.ObjectUtilities;
import java.awt.Shape;
import org.jfree.data.general.Dataset;
import java.io.Serializable;
public class LegendItemEntity extends ChartEntity implements Cloneable, Serializable {
    private static final long serialVersionUID = -7435683933545666702L;
    private Dataset dataset;
    private Comparable seriesKey;
    private int seriesIndex;
    public LegendItemEntity ( final Shape area ) {
        super ( area );
    }
    public Dataset getDataset() {
        return this.dataset;
    }
    public void setDataset ( final Dataset dataset ) {
        this.dataset = dataset;
    }
    public Comparable getSeriesKey() {
        return this.seriesKey;
    }
    public void setSeriesKey ( final Comparable key ) {
        this.seriesKey = key;
    }
    public int getSeriesIndex() {
        return this.seriesIndex;
    }
    public void setSeriesIndex ( final int index ) {
        this.seriesIndex = index;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof LegendItemEntity ) ) {
            return false;
        }
        final LegendItemEntity that = ( LegendItemEntity ) obj;
        return ObjectUtilities.equal ( ( Object ) this.seriesKey, ( Object ) that.seriesKey ) && this.seriesIndex == that.seriesIndex && ObjectUtilities.equal ( ( Object ) this.dataset, ( Object ) that.dataset ) && super.equals ( obj );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    @Override
    public String toString() {
        return "LegendItemEntity: seriesKey=" + this.seriesKey + ", dataset=" + this.dataset;
    }
}
