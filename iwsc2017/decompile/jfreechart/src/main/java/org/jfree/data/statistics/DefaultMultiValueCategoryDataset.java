package org.jfree.data.statistics;
import java.util.Iterator;
import java.util.Collections;
import java.util.ArrayList;
import org.jfree.chart.util.ParamChecks;
import java.util.List;
import org.jfree.data.Range;
import org.jfree.data.KeyedObjects2D;
import org.jfree.util.PublicCloneable;
import org.jfree.data.RangeInfo;
import org.jfree.data.general.AbstractDataset;
public class DefaultMultiValueCategoryDataset extends AbstractDataset implements MultiValueCategoryDataset, RangeInfo, PublicCloneable {
    protected KeyedObjects2D data;
    private Number minimumRangeValue;
    private Number maximumRangeValue;
    private Range rangeBounds;
    public DefaultMultiValueCategoryDataset() {
        this.data = new KeyedObjects2D();
        this.minimumRangeValue = null;
        this.maximumRangeValue = null;
        this.rangeBounds = new Range ( 0.0, 0.0 );
    }
    public void add ( final List values, final Comparable rowKey, final Comparable columnKey ) {
        ParamChecks.nullNotPermitted ( values, "values" );
        ParamChecks.nullNotPermitted ( rowKey, "rowKey" );
        ParamChecks.nullNotPermitted ( columnKey, "columnKey" );
        final List vlist = new ArrayList ( values.size() );
        final Iterator iterator = values.listIterator();
        while ( iterator.hasNext() ) {
            final Object obj = iterator.next();
            if ( obj instanceof Number ) {
                final Number n = ( Number ) obj;
                final double v = n.doubleValue();
                if ( Double.isNaN ( v ) ) {
                    continue;
                }
                vlist.add ( n );
            }
        }
        Collections.sort ( ( List<Comparable> ) vlist );
        this.data.addObject ( vlist, rowKey, columnKey );
        if ( vlist.size() > 0 ) {
            double maxval = Double.NEGATIVE_INFINITY;
            double minval = Double.POSITIVE_INFINITY;
            for ( int i = 0; i < vlist.size(); ++i ) {
                final Number n2 = vlist.get ( i );
                final double v2 = n2.doubleValue();
                minval = Math.min ( minval, v2 );
                maxval = Math.max ( maxval, v2 );
            }
            if ( this.maximumRangeValue == null ) {
                this.maximumRangeValue = new Double ( maxval );
            } else if ( maxval > this.maximumRangeValue.doubleValue() ) {
                this.maximumRangeValue = new Double ( maxval );
            }
            if ( this.minimumRangeValue == null ) {
                this.minimumRangeValue = new Double ( minval );
            } else if ( minval < this.minimumRangeValue.doubleValue() ) {
                this.minimumRangeValue = new Double ( minval );
            }
            this.rangeBounds = new Range ( this.minimumRangeValue.doubleValue(), this.maximumRangeValue.doubleValue() );
        }
        this.fireDatasetChanged();
    }
    @Override
    public List getValues ( final int row, final int column ) {
        final List values = ( List ) this.data.getObject ( row, column );
        if ( values != null ) {
            return Collections.unmodifiableList ( ( List<?> ) values );
        }
        return Collections.EMPTY_LIST;
    }
    @Override
    public List getValues ( final Comparable rowKey, final Comparable columnKey ) {
        return Collections.unmodifiableList ( ( List<?> ) this.data.getObject ( rowKey, columnKey ) );
    }
    public Number getValue ( final Comparable row, final Comparable column ) {
        final List l = ( List ) this.data.getObject ( row, column );
        double average = 0.0;
        int count = 0;
        if ( l != null && l.size() > 0 ) {
            for ( int i = 0; i < l.size(); ++i ) {
                final Number n = l.get ( i );
                average += n.doubleValue();
                ++count;
            }
            if ( count > 0 ) {
                average /= count;
            }
        }
        if ( count == 0 ) {
            return null;
        }
        return new Double ( average );
    }
    public Number getValue ( final int row, final int column ) {
        final List l = ( List ) this.data.getObject ( row, column );
        double average = 0.0;
        int count = 0;
        if ( l != null && l.size() > 0 ) {
            for ( int i = 0; i < l.size(); ++i ) {
                final Number n = l.get ( i );
                average += n.doubleValue();
                ++count;
            }
            if ( count > 0 ) {
                average /= count;
            }
        }
        if ( count == 0 ) {
            return null;
        }
        return new Double ( average );
    }
    public int getColumnIndex ( final Comparable key ) {
        return this.data.getColumnIndex ( key );
    }
    public Comparable getColumnKey ( final int column ) {
        return this.data.getColumnKey ( column );
    }
    public List getColumnKeys() {
        return this.data.getColumnKeys();
    }
    public int getRowIndex ( final Comparable key ) {
        return this.data.getRowIndex ( key );
    }
    public Comparable getRowKey ( final int row ) {
        return this.data.getRowKey ( row );
    }
    public List getRowKeys() {
        return this.data.getRowKeys();
    }
    public int getRowCount() {
        return this.data.getRowCount();
    }
    public int getColumnCount() {
        return this.data.getColumnCount();
    }
    @Override
    public double getRangeLowerBound ( final boolean includeInterval ) {
        double result = Double.NaN;
        if ( this.minimumRangeValue != null ) {
            result = this.minimumRangeValue.doubleValue();
        }
        return result;
    }
    @Override
    public double getRangeUpperBound ( final boolean includeInterval ) {
        double result = Double.NaN;
        if ( this.maximumRangeValue != null ) {
            result = this.maximumRangeValue.doubleValue();
        }
        return result;
    }
    @Override
    public Range getRangeBounds ( final boolean includeInterval ) {
        return this.rangeBounds;
    }
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof DefaultMultiValueCategoryDataset ) ) {
            return false;
        }
        final DefaultMultiValueCategoryDataset that = ( DefaultMultiValueCategoryDataset ) obj;
        return this.data.equals ( that.data );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final DefaultMultiValueCategoryDataset clone = ( DefaultMultiValueCategoryDataset ) super.clone();
        clone.data = ( KeyedObjects2D ) this.data.clone();
        return clone;
    }
}
