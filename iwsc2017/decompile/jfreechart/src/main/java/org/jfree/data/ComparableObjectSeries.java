package org.jfree.data;
import org.jfree.util.ObjectUtilities;
import org.jfree.data.general.SeriesException;
import java.util.Collections;
import org.jfree.chart.util.ParamChecks;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
import org.jfree.data.general.Series;
public class ComparableObjectSeries extends Series implements Cloneable, Serializable {
    protected List data;
    private int maximumItemCount;
    private boolean autoSort;
    private boolean allowDuplicateXValues;
    public ComparableObjectSeries ( final Comparable key ) {
        this ( key, true, true );
    }
    public ComparableObjectSeries ( final Comparable key, final boolean autoSort, final boolean allowDuplicateXValues ) {
        super ( key );
        this.maximumItemCount = Integer.MAX_VALUE;
        this.data = new ArrayList();
        this.autoSort = autoSort;
        this.allowDuplicateXValues = allowDuplicateXValues;
    }
    public boolean getAutoSort() {
        return this.autoSort;
    }
    public boolean getAllowDuplicateXValues() {
        return this.allowDuplicateXValues;
    }
    @Override
    public int getItemCount() {
        return this.data.size();
    }
    public int getMaximumItemCount() {
        return this.maximumItemCount;
    }
    public void setMaximumItemCount ( final int maximum ) {
        this.maximumItemCount = maximum;
        boolean dataRemoved = false;
        while ( this.data.size() > maximum ) {
            this.data.remove ( 0 );
            dataRemoved = true;
        }
        if ( dataRemoved ) {
            this.fireSeriesChanged();
        }
    }
    protected void add ( final Comparable x, final Object y ) {
        this.add ( x, y, true );
    }
    protected void add ( final Comparable x, final Object y, final boolean notify ) {
        final ComparableObjectItem item = new ComparableObjectItem ( x, y );
        this.add ( item, notify );
    }
    protected void add ( final ComparableObjectItem item, final boolean notify ) {
        ParamChecks.nullNotPermitted ( item, "item" );
        if ( this.autoSort ) {
            int index = Collections.binarySearch ( this.data, item );
            if ( index < 0 ) {
                this.data.add ( -index - 1, item );
            } else {
                if ( !this.allowDuplicateXValues ) {
                    throw new SeriesException ( "X-value already exists." );
                }
                for ( int size = this.data.size(); index < size && item.compareTo ( this.data.get ( index ) ) == 0; ++index ) {}
                if ( index < this.data.size() ) {
                    this.data.add ( index, item );
                } else {
                    this.data.add ( item );
                }
            }
        } else {
            if ( !this.allowDuplicateXValues ) {
                final int index = this.indexOf ( item.getComparable() );
                if ( index >= 0 ) {
                    throw new SeriesException ( "X-value already exists." );
                }
            }
            this.data.add ( item );
        }
        if ( this.getItemCount() > this.maximumItemCount ) {
            this.data.remove ( 0 );
        }
        if ( notify ) {
            this.fireSeriesChanged();
        }
    }
    public int indexOf ( final Comparable x ) {
        if ( this.autoSort ) {
            return Collections.binarySearch ( this.data, new ComparableObjectItem ( x, null ) );
        }
        for ( int i = 0; i < this.data.size(); ++i ) {
            final ComparableObjectItem item = this.data.get ( i );
            if ( item.getComparable().equals ( x ) ) {
                return i;
            }
        }
        return -1;
    }
    protected void update ( final Comparable x, final Object y ) {
        final int index = this.indexOf ( x );
        if ( index < 0 ) {
            throw new SeriesException ( "No observation for x = " + x );
        }
        final ComparableObjectItem item = this.getDataItem ( index );
        item.setObject ( y );
        this.fireSeriesChanged();
    }
    protected void updateByIndex ( final int index, final Object y ) {
        final ComparableObjectItem item = this.getDataItem ( index );
        item.setObject ( y );
        this.fireSeriesChanged();
    }
    protected ComparableObjectItem getDataItem ( final int index ) {
        return this.data.get ( index );
    }
    protected void delete ( final int start, final int end ) {
        for ( int i = start; i <= end; ++i ) {
            this.data.remove ( start );
        }
        this.fireSeriesChanged();
    }
    public void clear() {
        if ( this.data.size() > 0 ) {
            this.data.clear();
            this.fireSeriesChanged();
        }
    }
    protected ComparableObjectItem remove ( final int index ) {
        final ComparableObjectItem result = this.data.remove ( index );
        this.fireSeriesChanged();
        return result;
    }
    public ComparableObjectItem remove ( final Comparable x ) {
        return this.remove ( this.indexOf ( x ) );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof ComparableObjectSeries ) ) {
            return false;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        final ComparableObjectSeries that = ( ComparableObjectSeries ) obj;
        return this.maximumItemCount == that.maximumItemCount && this.autoSort == that.autoSort && this.allowDuplicateXValues == that.allowDuplicateXValues && ObjectUtilities.equal ( ( Object ) this.data, ( Object ) that.data );
    }
    @Override
    public int hashCode() {
        int result = super.hashCode();
        final int count = this.getItemCount();
        if ( count > 0 ) {
            final ComparableObjectItem item = this.getDataItem ( 0 );
            result = 29 * result + item.hashCode();
        }
        if ( count > 1 ) {
            final ComparableObjectItem item = this.getDataItem ( count - 1 );
            result = 29 * result + item.hashCode();
        }
        if ( count > 2 ) {
            final ComparableObjectItem item = this.getDataItem ( count / 2 );
            result = 29 * result + item.hashCode();
        }
        result = 29 * result + this.maximumItemCount;
        result = 29 * result + ( this.autoSort ? 1 : 0 );
        result = 29 * result + ( this.allowDuplicateXValues ? 1 : 0 );
        return result;
    }
}
