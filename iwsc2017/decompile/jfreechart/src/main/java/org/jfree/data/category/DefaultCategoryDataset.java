package org.jfree.data.category;
import java.util.List;
import org.jfree.data.DefaultKeyedValues2D;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
import org.jfree.data.general.AbstractDataset;
public class DefaultCategoryDataset extends AbstractDataset implements CategoryDataset, PublicCloneable, Serializable {
    private static final long serialVersionUID = -8168173757291644622L;
    private DefaultKeyedValues2D data;
    public DefaultCategoryDataset() {
        this.data = new DefaultKeyedValues2D();
    }
    public int getRowCount() {
        return this.data.getRowCount();
    }
    public int getColumnCount() {
        return this.data.getColumnCount();
    }
    public Number getValue ( final int row, final int column ) {
        return this.data.getValue ( row, column );
    }
    public Comparable getRowKey ( final int row ) {
        return this.data.getRowKey ( row );
    }
    public int getRowIndex ( final Comparable key ) {
        return this.data.getRowIndex ( key );
    }
    public List getRowKeys() {
        return this.data.getRowKeys();
    }
    public Comparable getColumnKey ( final int column ) {
        return this.data.getColumnKey ( column );
    }
    public int getColumnIndex ( final Comparable key ) {
        return this.data.getColumnIndex ( key );
    }
    public List getColumnKeys() {
        return this.data.getColumnKeys();
    }
    public Number getValue ( final Comparable rowKey, final Comparable columnKey ) {
        return this.data.getValue ( rowKey, columnKey );
    }
    public void addValue ( final Number value, final Comparable rowKey, final Comparable columnKey ) {
        this.data.addValue ( value, rowKey, columnKey );
        this.fireDatasetChanged();
    }
    public void addValue ( final double value, final Comparable rowKey, final Comparable columnKey ) {
        this.addValue ( new Double ( value ), rowKey, columnKey );
    }
    public void setValue ( final Number value, final Comparable rowKey, final Comparable columnKey ) {
        this.data.setValue ( value, rowKey, columnKey );
        this.fireDatasetChanged();
    }
    public void setValue ( final double value, final Comparable rowKey, final Comparable columnKey ) {
        this.setValue ( new Double ( value ), rowKey, columnKey );
    }
    public void incrementValue ( final double value, final Comparable rowKey, final Comparable columnKey ) {
        double existing = 0.0;
        final Number n = this.getValue ( rowKey, columnKey );
        if ( n != null ) {
            existing = n.doubleValue();
        }
        this.setValue ( existing + value, rowKey, columnKey );
    }
    public void removeValue ( final Comparable rowKey, final Comparable columnKey ) {
        this.data.removeValue ( rowKey, columnKey );
        this.fireDatasetChanged();
    }
    public void removeRow ( final int rowIndex ) {
        this.data.removeRow ( rowIndex );
        this.fireDatasetChanged();
    }
    public void removeRow ( final Comparable rowKey ) {
        this.data.removeRow ( rowKey );
        this.fireDatasetChanged();
    }
    public void removeColumn ( final int columnIndex ) {
        this.data.removeColumn ( columnIndex );
        this.fireDatasetChanged();
    }
    public void removeColumn ( final Comparable columnKey ) {
        this.data.removeColumn ( columnKey );
        this.fireDatasetChanged();
    }
    public void clear() {
        this.data.clear();
        this.fireDatasetChanged();
    }
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof CategoryDataset ) ) {
            return false;
        }
        final CategoryDataset that = ( CategoryDataset ) obj;
        if ( !this.getRowKeys().equals ( that.getRowKeys() ) ) {
            return false;
        }
        if ( !this.getColumnKeys().equals ( that.getColumnKeys() ) ) {
            return false;
        }
        final int rowCount = this.getRowCount();
        final int colCount = this.getColumnCount();
        for ( int r = 0; r < rowCount; ++r ) {
            for ( int c = 0; c < colCount; ++c ) {
                final Number v1 = this.getValue ( r, c );
                final Number v2 = that.getValue ( r, c );
                if ( v1 == null ) {
                    if ( v2 != null ) {
                        return false;
                    }
                } else if ( !v1.equals ( v2 ) ) {
                    return false;
                }
            }
        }
        return true;
    }
    public int hashCode() {
        return this.data.hashCode();
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final DefaultCategoryDataset clone = ( DefaultCategoryDataset ) super.clone();
        clone.data = ( DefaultKeyedValues2D ) this.data.clone();
        return clone;
    }
}
