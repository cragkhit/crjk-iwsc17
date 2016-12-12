package org.jfree.data;
import java.util.Comparator;
import java.util.Arrays;
import org.jfree.util.SortOrder;
import java.util.List;
import org.jfree.chart.util.ParamChecks;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class DefaultKeyedValues implements KeyedValues, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 8468154364608194797L;
    private ArrayList keys;
    private ArrayList values;
    private HashMap indexMap;
    public DefaultKeyedValues() {
        this.keys = new ArrayList();
        this.values = new ArrayList();
        this.indexMap = new HashMap();
    }
    public int getItemCount() {
        return this.indexMap.size();
    }
    public Number getValue ( final int item ) {
        return this.values.get ( item );
    }
    @Override
    public Comparable getKey ( final int index ) {
        return this.keys.get ( index );
    }
    @Override
    public int getIndex ( final Comparable key ) {
        ParamChecks.nullNotPermitted ( key, "key" );
        final Integer i = this.indexMap.get ( key );
        if ( i == null ) {
            return -1;
        }
        return i;
    }
    @Override
    public List getKeys() {
        return ( List ) this.keys.clone();
    }
    @Override
    public Number getValue ( final Comparable key ) {
        final int index = this.getIndex ( key );
        if ( index < 0 ) {
            throw new UnknownKeyException ( "Key not found: " + key );
        }
        return this.getValue ( index );
    }
    public void addValue ( final Comparable key, final double value ) {
        this.addValue ( key, new Double ( value ) );
    }
    public void addValue ( final Comparable key, final Number value ) {
        this.setValue ( key, value );
    }
    public void setValue ( final Comparable key, final double value ) {
        this.setValue ( key, new Double ( value ) );
    }
    public void setValue ( final Comparable key, final Number value ) {
        ParamChecks.nullNotPermitted ( key, "key" );
        final int keyIndex = this.getIndex ( key );
        if ( keyIndex >= 0 ) {
            this.keys.set ( keyIndex, key );
            this.values.set ( keyIndex, value );
        } else {
            this.keys.add ( key );
            this.values.add ( value );
            this.indexMap.put ( key, new Integer ( this.keys.size() - 1 ) );
        }
    }
    public void insertValue ( final int position, final Comparable key, final double value ) {
        this.insertValue ( position, key, new Double ( value ) );
    }
    public void insertValue ( final int position, final Comparable key, final Number value ) {
        if ( position < 0 || position > this.getItemCount() ) {
            throw new IllegalArgumentException ( "'position' out of bounds." );
        }
        ParamChecks.nullNotPermitted ( key, "key" );
        final int pos = this.getIndex ( key );
        if ( pos == position ) {
            this.keys.set ( pos, key );
            this.values.set ( pos, value );
        } else {
            if ( pos >= 0 ) {
                this.keys.remove ( pos );
                this.values.remove ( pos );
            }
            this.keys.add ( position, key );
            this.values.add ( position, value );
            this.rebuildIndex();
        }
    }
    private void rebuildIndex() {
        this.indexMap.clear();
        for ( int i = 0; i < this.keys.size(); ++i ) {
            final Object key = this.keys.get ( i );
            this.indexMap.put ( key, new Integer ( i ) );
        }
    }
    public void removeValue ( final int index ) {
        this.keys.remove ( index );
        this.values.remove ( index );
        this.rebuildIndex();
    }
    public void removeValue ( final Comparable key ) {
        final int index = this.getIndex ( key );
        if ( index < 0 ) {
            throw new UnknownKeyException ( "The key (" + key + ") is not recognised." );
        }
        this.removeValue ( index );
    }
    public void clear() {
        this.keys.clear();
        this.values.clear();
        this.indexMap.clear();
    }
    public void sortByKeys ( final SortOrder order ) {
        final int size = this.keys.size();
        final DefaultKeyedValue[] data = new DefaultKeyedValue[size];
        for ( int i = 0; i < size; ++i ) {
            data[i] = new DefaultKeyedValue ( this.keys.get ( i ), this.values.get ( i ) );
        }
        final Comparator comparator = new KeyedValueComparator ( KeyedValueComparatorType.BY_KEY, order );
        Arrays.sort ( data, comparator );
        this.clear();
        for ( int j = 0; j < data.length; ++j ) {
            final DefaultKeyedValue value = data[j];
            this.addValue ( value.getKey(), value.getValue() );
        }
    }
    public void sortByValues ( final SortOrder order ) {
        final int size = this.keys.size();
        final DefaultKeyedValue[] data = new DefaultKeyedValue[size];
        for ( int i = 0; i < size; ++i ) {
            data[i] = new DefaultKeyedValue ( this.keys.get ( i ), this.values.get ( i ) );
        }
        final Comparator comparator = new KeyedValueComparator ( KeyedValueComparatorType.BY_VALUE, order );
        Arrays.sort ( data, comparator );
        this.clear();
        for ( int j = 0; j < data.length; ++j ) {
            final DefaultKeyedValue value = data[j];
            this.addValue ( value.getKey(), value.getValue() );
        }
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof KeyedValues ) ) {
            return false;
        }
        final KeyedValues that = ( KeyedValues ) obj;
        final int count = this.getItemCount();
        if ( count != that.getItemCount() ) {
            return false;
        }
        for ( int i = 0; i < count; ++i ) {
            final Comparable k1 = this.getKey ( i );
            final Comparable k2 = that.getKey ( i );
            if ( !k1.equals ( k2 ) ) {
                return false;
            }
            final Number v1 = this.getValue ( i );
            final Number v2 = that.getValue ( i );
            if ( v1 == null ) {
                if ( v2 != null ) {
                    return false;
                }
            } else if ( !v1.equals ( v2 ) ) {
                return false;
            }
        }
        return true;
    }
    @Override
    public int hashCode() {
        return ( this.keys != null ) ? this.keys.hashCode() : 0;
    }
    public Object clone() throws CloneNotSupportedException {
        final DefaultKeyedValues clone = ( DefaultKeyedValues ) super.clone();
        clone.keys = ( ArrayList ) this.keys.clone();
        clone.values = ( ArrayList ) this.values.clone();
        clone.indexMap = ( HashMap ) this.indexMap.clone();
        return clone;
    }
}
