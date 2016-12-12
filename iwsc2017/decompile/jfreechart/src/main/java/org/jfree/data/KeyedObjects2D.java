package org.jfree.data;
import java.util.Collection;
import java.util.Iterator;
import java.util.Collections;
import org.jfree.chart.util.ParamChecks;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
public class KeyedObjects2D implements Cloneable, Serializable {
    private static final long serialVersionUID = -1015873563138522374L;
    private List rowKeys;
    private List columnKeys;
    private List rows;
    public KeyedObjects2D() {
        this.rowKeys = new ArrayList();
        this.columnKeys = new ArrayList();
        this.rows = new ArrayList();
    }
    public int getRowCount() {
        return this.rowKeys.size();
    }
    public int getColumnCount() {
        return this.columnKeys.size();
    }
    public Object getObject ( final int row, final int column ) {
        Object result = null;
        final KeyedObjects rowData = this.rows.get ( row );
        if ( rowData != null ) {
            final Comparable columnKey = this.columnKeys.get ( column );
            if ( columnKey != null ) {
                final int index = rowData.getIndex ( columnKey );
                if ( index >= 0 ) {
                    result = rowData.getObject ( columnKey );
                }
            }
        }
        return result;
    }
    public Comparable getRowKey ( final int row ) {
        return this.rowKeys.get ( row );
    }
    public int getRowIndex ( final Comparable key ) {
        ParamChecks.nullNotPermitted ( key, "key" );
        return this.rowKeys.indexOf ( key );
    }
    public List getRowKeys() {
        return Collections.unmodifiableList ( ( List<?> ) this.rowKeys );
    }
    public Comparable getColumnKey ( final int column ) {
        return this.columnKeys.get ( column );
    }
    public int getColumnIndex ( final Comparable key ) {
        ParamChecks.nullNotPermitted ( key, "key" );
        return this.columnKeys.indexOf ( key );
    }
    public List getColumnKeys() {
        return Collections.unmodifiableList ( ( List<?> ) this.columnKeys );
    }
    public Object getObject ( final Comparable rowKey, final Comparable columnKey ) {
        ParamChecks.nullNotPermitted ( rowKey, "rowKey" );
        ParamChecks.nullNotPermitted ( columnKey, "columnKey" );
        final int row = this.rowKeys.indexOf ( rowKey );
        if ( row < 0 ) {
            throw new UnknownKeyException ( "Row key (" + rowKey + ") not recognised." );
        }
        final int column = this.columnKeys.indexOf ( columnKey );
        if ( column < 0 ) {
            throw new UnknownKeyException ( "Column key (" + columnKey + ") not recognised." );
        }
        final KeyedObjects rowData = this.rows.get ( row );
        final int index = rowData.getIndex ( columnKey );
        if ( index >= 0 ) {
            return rowData.getObject ( index );
        }
        return null;
    }
    public void addObject ( final Object object, final Comparable rowKey, final Comparable columnKey ) {
        this.setObject ( object, rowKey, columnKey );
    }
    public void setObject ( final Object object, final Comparable rowKey, final Comparable columnKey ) {
        ParamChecks.nullNotPermitted ( rowKey, "rowKey" );
        ParamChecks.nullNotPermitted ( columnKey, "columnKey" );
        final int rowIndex = this.rowKeys.indexOf ( rowKey );
        KeyedObjects row;
        if ( rowIndex >= 0 ) {
            row = this.rows.get ( rowIndex );
        } else {
            this.rowKeys.add ( rowKey );
            row = new KeyedObjects();
            this.rows.add ( row );
        }
        row.setObject ( columnKey, object );
        final int columnIndex = this.columnKeys.indexOf ( columnKey );
        if ( columnIndex < 0 ) {
            this.columnKeys.add ( columnKey );
        }
    }
    public void removeObject ( final Comparable rowKey, final Comparable columnKey ) {
        final int rowIndex = this.getRowIndex ( rowKey );
        if ( rowIndex < 0 ) {
            throw new UnknownKeyException ( "Row key (" + rowKey + ") not recognised." );
        }
        final int columnIndex = this.getColumnIndex ( columnKey );
        if ( columnIndex < 0 ) {
            throw new UnknownKeyException ( "Column key (" + columnKey + ") not recognised." );
        }
        this.setObject ( null, rowKey, columnKey );
        boolean allNull = true;
        KeyedObjects row = this.rows.get ( rowIndex );
        for ( int item = 0, itemCount = row.getItemCount(); item < itemCount; ++item ) {
            if ( row.getObject ( item ) != null ) {
                allNull = false;
                break;
            }
        }
        if ( allNull ) {
            this.rowKeys.remove ( rowIndex );
            this.rows.remove ( rowIndex );
        }
        allNull = true;
        for ( int item = 0, itemCount = this.rows.size(); item < itemCount; ++item ) {
            row = this.rows.get ( item );
            final int colIndex = row.getIndex ( columnKey );
            if ( colIndex >= 0 && row.getObject ( colIndex ) != null ) {
                allNull = false;
                break;
            }
        }
        if ( allNull ) {
            for ( int item = 0, itemCount = this.rows.size(); item < itemCount; ++item ) {
                row = this.rows.get ( item );
                final int colIndex = row.getIndex ( columnKey );
                if ( colIndex >= 0 ) {
                    row.removeValue ( colIndex );
                }
            }
            this.columnKeys.remove ( columnKey );
        }
    }
    public void removeRow ( final int rowIndex ) {
        this.rowKeys.remove ( rowIndex );
        this.rows.remove ( rowIndex );
    }
    public void removeRow ( final Comparable rowKey ) {
        final int index = this.getRowIndex ( rowKey );
        if ( index < 0 ) {
            throw new UnknownKeyException ( "Row key (" + rowKey + ") not recognised." );
        }
        this.removeRow ( index );
    }
    public void removeColumn ( final int columnIndex ) {
        final Comparable columnKey = this.getColumnKey ( columnIndex );
        this.removeColumn ( columnKey );
    }
    public void removeColumn ( final Comparable columnKey ) {
        final int index = this.getColumnIndex ( columnKey );
        if ( index < 0 ) {
            throw new UnknownKeyException ( "Column key (" + columnKey + ") not recognised." );
        }
        for ( final KeyedObjects rowData : this.rows ) {
            final int i = rowData.getIndex ( columnKey );
            if ( i >= 0 ) {
                rowData.removeValue ( i );
            }
        }
        this.columnKeys.remove ( columnKey );
    }
    public void clear() {
        this.rowKeys.clear();
        this.columnKeys.clear();
        this.rows.clear();
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof KeyedObjects2D ) ) {
            return false;
        }
        final KeyedObjects2D that = ( KeyedObjects2D ) obj;
        if ( !this.getRowKeys().equals ( that.getRowKeys() ) ) {
            return false;
        }
        if ( !this.getColumnKeys().equals ( that.getColumnKeys() ) ) {
            return false;
        }
        final int rowCount = this.getRowCount();
        if ( rowCount != that.getRowCount() ) {
            return false;
        }
        final int colCount = this.getColumnCount();
        if ( colCount != that.getColumnCount() ) {
            return false;
        }
        for ( int r = 0; r < rowCount; ++r ) {
            for ( int c = 0; c < colCount; ++c ) {
                final Object v1 = this.getObject ( r, c );
                final Object v2 = that.getObject ( r, c );
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
    @Override
    public int hashCode() {
        int result = this.rowKeys.hashCode();
        result = 29 * result + this.columnKeys.hashCode();
        result = 29 * result + this.rows.hashCode();
        return result;
    }
    public Object clone() throws CloneNotSupportedException {
        final KeyedObjects2D clone = ( KeyedObjects2D ) super.clone();
        clone.columnKeys = new ArrayList ( this.columnKeys );
        clone.rowKeys = new ArrayList ( this.rowKeys );
        clone.rows = new ArrayList ( this.rows.size() );
        for ( final KeyedObjects row : this.rows ) {
            clone.rows.add ( row.clone() );
        }
        return clone;
    }
}
