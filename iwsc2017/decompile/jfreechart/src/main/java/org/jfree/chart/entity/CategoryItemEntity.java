package org.jfree.chart.entity;
import org.jfree.util.ObjectUtilities;
import org.jfree.chart.util.ParamChecks;
import java.awt.Shape;
import org.jfree.data.category.CategoryDataset;
import java.io.Serializable;
public class CategoryItemEntity extends ChartEntity implements Cloneable, Serializable {
    private static final long serialVersionUID = -8657249457902337349L;
    private CategoryDataset dataset;
    private int series;
    private Object category;
    private int categoryIndex;
    private Comparable rowKey;
    private Comparable columnKey;
    public CategoryItemEntity ( final Shape area, final String toolTipText, final String urlText, final CategoryDataset dataset, final int series, final Object category, final int categoryIndex ) {
        super ( area, toolTipText, urlText );
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        this.dataset = dataset;
        this.series = series;
        this.category = category;
        this.categoryIndex = categoryIndex;
        this.rowKey = dataset.getRowKey ( series );
        this.columnKey = dataset.getColumnKey ( categoryIndex );
    }
    public CategoryItemEntity ( final Shape area, final String toolTipText, final String urlText, final CategoryDataset dataset, final Comparable rowKey, final Comparable columnKey ) {
        super ( area, toolTipText, urlText );
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        this.dataset = dataset;
        this.rowKey = rowKey;
        this.columnKey = columnKey;
        this.series = dataset.getRowIndex ( rowKey );
        this.category = columnKey;
        this.categoryIndex = dataset.getColumnIndex ( columnKey );
    }
    public CategoryDataset getDataset() {
        return this.dataset;
    }
    public void setDataset ( final CategoryDataset dataset ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        this.dataset = dataset;
    }
    public Comparable getRowKey() {
        return this.rowKey;
    }
    public void setRowKey ( final Comparable rowKey ) {
        this.rowKey = rowKey;
        this.series = this.dataset.getRowIndex ( rowKey );
    }
    public Comparable getColumnKey() {
        return this.columnKey;
    }
    public void setColumnKey ( final Comparable columnKey ) {
        this.columnKey = columnKey;
        this.category = columnKey;
        this.categoryIndex = this.dataset.getColumnIndex ( columnKey );
    }
    public int getSeries() {
        return this.series;
    }
    public void setSeries ( final int series ) {
        this.series = series;
    }
    public Object getCategory() {
        return this.category;
    }
    public void setCategory ( final Object category ) {
        this.category = category;
    }
    public int getCategoryIndex() {
        return this.categoryIndex;
    }
    public void setCategoryIndex ( final int index ) {
        this.categoryIndex = index;
    }
    @Override
    public String toString() {
        return "CategoryItemEntity: rowKey=" + this.rowKey + ", columnKey=" + this.columnKey + ", dataset=" + this.dataset;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof CategoryItemEntity ) ) {
            return false;
        }
        final CategoryItemEntity that = ( CategoryItemEntity ) obj;
        return this.rowKey.equals ( that.rowKey ) && this.columnKey.equals ( that.columnKey ) && ObjectUtilities.equal ( ( Object ) this.dataset, ( Object ) that.dataset ) && this.categoryIndex == that.categoryIndex && this.series == that.series && ObjectUtilities.equal ( this.category, that.category ) && super.equals ( obj );
    }
}
