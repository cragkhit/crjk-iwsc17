package org.jfree.data.general;
import org.jfree.util.SortOrder;
import java.util.Collections;
import java.util.List;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.KeyedValues;
import org.jfree.data.DefaultKeyedValues;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class DefaultPieDataset extends AbstractDataset implements PieDataset, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 2904745139106540618L;
    private DefaultKeyedValues data;
    public DefaultPieDataset() {
        this.data = new DefaultKeyedValues();
    }
    public DefaultPieDataset ( final KeyedValues data ) {
        ParamChecks.nullNotPermitted ( data, "data" );
        this.data = new DefaultKeyedValues();
        for ( int i = 0; i < data.getItemCount(); ++i ) {
            this.data.addValue ( data.getKey ( i ), data.getValue ( i ) );
        }
    }
    public int getItemCount() {
        return this.data.getItemCount();
    }
    public List getKeys() {
        return Collections.unmodifiableList ( ( List<?> ) this.data.getKeys() );
    }
    public Comparable getKey ( final int item ) {
        return this.data.getKey ( item );
    }
    public int getIndex ( final Comparable key ) {
        return this.data.getIndex ( key );
    }
    public Number getValue ( final int item ) {
        Number result = null;
        if ( this.getItemCount() > item ) {
            result = this.data.getValue ( item );
        }
        return result;
    }
    public Number getValue ( final Comparable key ) {
        ParamChecks.nullNotPermitted ( key, "key" );
        return this.data.getValue ( key );
    }
    public void setValue ( final Comparable key, final Number value ) {
        this.data.setValue ( key, value );
        this.fireDatasetChanged();
    }
    public void setValue ( final Comparable key, final double value ) {
        this.setValue ( key, new Double ( value ) );
    }
    public void insertValue ( final int position, final Comparable key, final double value ) {
        this.insertValue ( position, key, new Double ( value ) );
    }
    public void insertValue ( final int position, final Comparable key, final Number value ) {
        this.data.insertValue ( position, key, value );
        this.fireDatasetChanged();
    }
    public void remove ( final Comparable key ) {
        this.data.removeValue ( key );
        this.fireDatasetChanged();
    }
    public void clear() {
        if ( this.getItemCount() > 0 ) {
            this.data.clear();
            this.fireDatasetChanged();
        }
    }
    public void sortByKeys ( final SortOrder order ) {
        this.data.sortByKeys ( order );
        this.fireDatasetChanged();
    }
    public void sortByValues ( final SortOrder order ) {
        this.data.sortByValues ( order );
        this.fireDatasetChanged();
    }
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof PieDataset ) ) {
            return false;
        }
        final PieDataset that = ( PieDataset ) obj;
        final int count = this.getItemCount();
        if ( that.getItemCount() != count ) {
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
    public int hashCode() {
        return this.data.hashCode();
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final DefaultPieDataset clone = ( DefaultPieDataset ) super.clone();
        clone.data = ( DefaultKeyedValues ) this.data.clone();
        return clone;
    }
}
