package org.jfree.data.general;
import org.jfree.util.ObjectUtilities;
import org.jfree.data.DefaultKeyedValue;
import org.jfree.data.KeyedValue;
import java.io.Serializable;
public class DefaultKeyedValueDataset extends AbstractDataset implements KeyedValueDataset, Serializable {
    private static final long serialVersionUID = -8149484339560406750L;
    private KeyedValue data;
    public DefaultKeyedValueDataset() {
        this ( null );
    }
    public DefaultKeyedValueDataset ( final Comparable key, final Number value ) {
        this ( new DefaultKeyedValue ( key, value ) );
    }
    public DefaultKeyedValueDataset ( final KeyedValue data ) {
        this.data = data;
    }
    @Override
    public Comparable getKey() {
        Comparable result = null;
        if ( this.data != null ) {
            result = this.data.getKey();
        }
        return result;
    }
    @Override
    public Number getValue() {
        Number result = null;
        if ( this.data != null ) {
            result = this.data.getValue();
        }
        return result;
    }
    public void updateValue ( final Number value ) {
        if ( this.data == null ) {
            throw new RuntimeException ( "updateValue: can't update null." );
        }
        this.setValue ( this.data.getKey(), value );
    }
    public void setValue ( final Comparable key, final Number value ) {
        this.data = new DefaultKeyedValue ( key, value );
        this.notifyListeners ( new DatasetChangeEvent ( this, this ) );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof KeyedValueDataset ) ) {
            return false;
        }
        final KeyedValueDataset that = ( KeyedValueDataset ) obj;
        if ( this.data == null ) {
            return that.getKey() == null && that.getValue() == null;
        }
        return ObjectUtilities.equal ( ( Object ) this.data.getKey(), ( Object ) that.getKey() ) && ObjectUtilities.equal ( ( Object ) this.data.getValue(), ( Object ) that.getValue() );
    }
    @Override
    public int hashCode() {
        return ( this.data != null ) ? this.data.hashCode() : 0;
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final DefaultKeyedValueDataset clone = ( DefaultKeyedValueDataset ) super.clone();
        return clone;
    }
}
