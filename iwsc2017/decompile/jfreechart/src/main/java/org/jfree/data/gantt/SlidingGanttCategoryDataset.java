package org.jfree.data.gantt;
import org.jfree.util.PublicCloneable;
import org.jfree.data.UnknownKeyException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import org.jfree.data.general.AbstractDataset;
public class SlidingGanttCategoryDataset extends AbstractDataset implements GanttCategoryDataset {
    private GanttCategoryDataset underlying;
    private int firstCategoryIndex;
    private int maximumCategoryCount;
    public SlidingGanttCategoryDataset ( final GanttCategoryDataset underlying, final int firstColumn, final int maxColumns ) {
        this.underlying = underlying;
        this.firstCategoryIndex = firstColumn;
        this.maximumCategoryCount = maxColumns;
    }
    public GanttCategoryDataset getUnderlyingDataset() {
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
        for ( int last = this.lastCategoryIndex(), i = this.firstCategoryIndex; i < last; ++i ) {
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
    public Number getPercentComplete ( final Comparable rowKey, final Comparable columnKey ) {
        final int r = this.getRowIndex ( rowKey );
        final int c = this.getColumnIndex ( columnKey );
        if ( c != -1 ) {
            return this.underlying.getPercentComplete ( r, c + this.firstCategoryIndex );
        }
        throw new UnknownKeyException ( "Unknown columnKey: " + columnKey );
    }
    @Override
    public Number getPercentComplete ( final Comparable rowKey, final Comparable columnKey, final int subinterval ) {
        final int r = this.getRowIndex ( rowKey );
        final int c = this.getColumnIndex ( columnKey );
        if ( c != -1 ) {
            return this.underlying.getPercentComplete ( r, c + this.firstCategoryIndex, subinterval );
        }
        throw new UnknownKeyException ( "Unknown columnKey: " + columnKey );
    }
    @Override
    public Number getEndValue ( final Comparable rowKey, final Comparable columnKey, final int subinterval ) {
        final int r = this.getRowIndex ( rowKey );
        final int c = this.getColumnIndex ( columnKey );
        if ( c != -1 ) {
            return this.underlying.getEndValue ( r, c + this.firstCategoryIndex, subinterval );
        }
        throw new UnknownKeyException ( "Unknown columnKey: " + columnKey );
    }
    @Override
    public Number getEndValue ( final int row, final int column, final int subinterval ) {
        return this.underlying.getEndValue ( row, column + this.firstCategoryIndex, subinterval );
    }
    @Override
    public Number getPercentComplete ( final int series, final int category ) {
        return this.underlying.getPercentComplete ( series, category + this.firstCategoryIndex );
    }
    @Override
    public Number getPercentComplete ( final int row, final int column, final int subinterval ) {
        return this.underlying.getPercentComplete ( row, column + this.firstCategoryIndex, subinterval );
    }
    @Override
    public Number getStartValue ( final Comparable rowKey, final Comparable columnKey, final int subinterval ) {
        final int r = this.getRowIndex ( rowKey );
        final int c = this.getColumnIndex ( columnKey );
        if ( c != -1 ) {
            return this.underlying.getStartValue ( r, c + this.firstCategoryIndex, subinterval );
        }
        throw new UnknownKeyException ( "Unknown columnKey: " + columnKey );
    }
    @Override
    public Number getStartValue ( final int row, final int column, final int subinterval ) {
        return this.underlying.getStartValue ( row, column + this.firstCategoryIndex, subinterval );
    }
    @Override
    public int getSubIntervalCount ( final Comparable rowKey, final Comparable columnKey ) {
        final int r = this.getRowIndex ( rowKey );
        final int c = this.getColumnIndex ( columnKey );
        if ( c != -1 ) {
            return this.underlying.getSubIntervalCount ( r, c + this.firstCategoryIndex );
        }
        throw new UnknownKeyException ( "Unknown columnKey: " + columnKey );
    }
    @Override
    public int getSubIntervalCount ( final int row, final int column ) {
        return this.underlying.getSubIntervalCount ( row, column + this.firstCategoryIndex );
    }
    @Override
    public Number getStartValue ( final Comparable rowKey, final Comparable columnKey ) {
        final int r = this.getRowIndex ( rowKey );
        final int c = this.getColumnIndex ( columnKey );
        if ( c != -1 ) {
            return this.underlying.getStartValue ( r, c + this.firstCategoryIndex );
        }
        throw new UnknownKeyException ( "Unknown columnKey: " + columnKey );
    }
    @Override
    public Number getStartValue ( final int row, final int column ) {
        return this.underlying.getStartValue ( row, column + this.firstCategoryIndex );
    }
    @Override
    public Number getEndValue ( final Comparable rowKey, final Comparable columnKey ) {
        final int r = this.getRowIndex ( rowKey );
        final int c = this.getColumnIndex ( columnKey );
        if ( c != -1 ) {
            return this.underlying.getEndValue ( r, c + this.firstCategoryIndex );
        }
        throw new UnknownKeyException ( "Unknown columnKey: " + columnKey );
    }
    @Override
    public Number getEndValue ( final int series, final int category ) {
        return this.underlying.getEndValue ( series, category + this.firstCategoryIndex );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof SlidingGanttCategoryDataset ) ) {
            return false;
        }
        final SlidingGanttCategoryDataset that = ( SlidingGanttCategoryDataset ) obj;
        return this.firstCategoryIndex == that.firstCategoryIndex && this.maximumCategoryCount == that.maximumCategoryCount && this.underlying.equals ( that.underlying );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final SlidingGanttCategoryDataset clone = ( SlidingGanttCategoryDataset ) super.clone();
        if ( this.underlying instanceof PublicCloneable ) {
            final PublicCloneable pc = ( PublicCloneable ) this.underlying;
            clone.underlying = ( GanttCategoryDataset ) pc.clone();
        }
        return clone;
    }
}
