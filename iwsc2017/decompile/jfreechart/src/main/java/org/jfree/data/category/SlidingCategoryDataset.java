package org.jfree.data.category;
import org.jfree.util.PublicCloneable;
import org.jfree.data.UnknownKeyException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import org.jfree.data.general.AbstractDataset;
public class SlidingCategoryDataset extends AbstractDataset implements CategoryDataset {
    private CategoryDataset underlying;
    private int firstCategoryIndex;
    private int maximumCategoryCount;
    public SlidingCategoryDataset ( final CategoryDataset underlying, final int firstColumn, final int maxColumns ) {
        this.underlying = underlying;
        this.firstCategoryIndex = firstColumn;
        this.maximumCategoryCount = maxColumns;
    }
    public CategoryDataset getUnderlyingDataset() {
        return this.underlying;
    }
    public int getFirstCategoryIndex() {
        return this.firstCategoryIndex;
    }
    public void setFirstCategoryIndex ( final int first ) {
        if ( first < 0 || first >= this.underlying.getColumnCount() ) {
            throw new IllegalArgumentException ( "Invalid index." );
        }
        this.firstCategoryIndex = first;
        this.fireDatasetChanged();
    }
    public int getMaximumCategoryCount() {
        return this.maximumCategoryCount;
    }
    public void setMaximumCategoryCount ( final int max ) {
        if ( max < 0 ) {
            throw new IllegalArgumentException ( "Requires 'max' >= 0." );
        }
        this.maximumCategoryCount = max;
        this.fireDatasetChanged();
    }
    private int lastCategoryIndex() {
        if ( this.maximumCategoryCount == 0 ) {
            return -1;
        }
        return Math.min ( this.firstCategoryIndex + this.maximumCategoryCount, this.underlying.getColumnCount() ) - 1;
    }
    @Override
    public int getColumnIndex ( final Comparable key ) {
        final int index = this.underlying.getColumnIndex ( key );
        if ( index >= this.firstCategoryIndex && index <= this.lastCategoryIndex() ) {
            return index - this.firstCategoryIndex;
        }
        return -1;
    }
    @Override
    public Comparable getColumnKey ( final int column ) {
        return this.underlying.getColumnKey ( column + this.firstCategoryIndex );
    }
    @Override
    public List getColumnKeys() {
        final List result = new ArrayList();
        for ( int last = this.lastCategoryIndex(), i = this.firstCategoryIndex; i <= last; ++i ) {
            result.add ( this.underlying.getColumnKey ( i ) );
        }
        return Collections.unmodifiableList ( ( List<?> ) result );
    }
    @Override
    public int getRowIndex ( final Comparable key ) {
        return this.underlying.getRowIndex ( key );
    }
    @Override
    public Comparable getRowKey ( final int row ) {
        return this.underlying.getRowKey ( row );
    }
    @Override
    public List getRowKeys() {
        return this.underlying.getRowKeys();
    }
    @Override
    public Number getValue ( final Comparable rowKey, final Comparable columnKey ) {
        final int r = this.getRowIndex ( rowKey );
        final int c = this.getColumnIndex ( columnKey );
        if ( c != -1 ) {
            return this.underlying.getValue ( r, c + this.firstCategoryIndex );
        }
        throw new UnknownKeyException ( "Unknown columnKey: " + columnKey );
    }
    @Override
    public int getColumnCount() {
        final int last = this.lastCategoryIndex();
        if ( last == -1 ) {
            return 0;
        }
        return Math.max ( last - this.firstCategoryIndex + 1, 0 );
    }
    @Override
    public int getRowCount() {
        return this.underlying.getRowCount();
    }
    @Override
    public Number getValue ( final int row, final int column ) {
        return this.underlying.getValue ( row, column + this.firstCategoryIndex );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof SlidingCategoryDataset ) ) {
            return false;
        }
        final SlidingCategoryDataset that = ( SlidingCategoryDataset ) obj;
        return this.firstCategoryIndex == that.firstCategoryIndex && this.maximumCategoryCount == that.maximumCategoryCount && this.underlying.equals ( that.underlying );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final SlidingCategoryDataset clone = ( SlidingCategoryDataset ) super.clone();
        if ( this.underlying instanceof PublicCloneable ) {
            final PublicCloneable pc = ( PublicCloneable ) this.underlying;
            clone.underlying = ( CategoryDataset ) pc.clone();
        }
        return clone;
    }
}
