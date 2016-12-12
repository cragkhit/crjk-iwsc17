package org.jfree.chart.entity;
import java.awt.Shape;
import org.jfree.data.xy.XYDataset;
public class XYItemEntity extends ChartEntity {
    private static final long serialVersionUID = -3870862224880283771L;
    private transient XYDataset dataset;
    private int series;
    private int item;
    public XYItemEntity ( final Shape area, final XYDataset dataset, final int series, final int item, final String toolTipText, final String urlText ) {
        super ( area, toolTipText, urlText );
        this.dataset = dataset;
        this.series = series;
        this.item = item;
    }
    public XYDataset getDataset() {
        return this.dataset;
    }
    public void setDataset ( final XYDataset dataset ) {
        this.dataset = dataset;
    }
    public int getSeriesIndex() {
        return this.series;
    }
    public void setSeriesIndex ( final int series ) {
        this.series = series;
    }
    public int getItem() {
        return this.item;
    }
    public void setItem ( final int item ) {
        this.item = item;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( obj instanceof XYItemEntity && super.equals ( obj ) ) {
            final XYItemEntity ie = ( XYItemEntity ) obj;
            return this.series == ie.series && this.item == ie.item;
        }
        return false;
    }
    @Override
    public String toString() {
        return "XYItemEntity: series = " + this.getSeriesIndex() + ", item = " + this.getItem() + ", dataset = " + this.getDataset();
    }
}
