package org.jfree.data.general;
import org.jfree.util.ObjectUtilities;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class DefaultValueDataset extends AbstractDataset implements ValueDataset, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 8137521217249294891L;
    private Number value;
    public DefaultValueDataset() {
        this ( null );
    }
    public DefaultValueDataset ( final double value ) {
        this ( new Double ( value ) );
    }
    public DefaultValueDataset ( final Number value ) {
        this.value = value;
    }
    public Number getValue() {
        return this.value;
    }
    public void setValue ( final Number value ) {
        this.value = value;
        this.notifyListeners ( new DatasetChangeEvent ( this, this ) );
    }
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( obj instanceof ValueDataset ) {
            final ValueDataset vd = ( ValueDataset ) obj;
            return ObjectUtilities.equal ( ( Object ) this.value, ( Object ) vd.getValue() );
        }
        return false;
    }
    public int hashCode() {
        return ( this.value != null ) ? this.value.hashCode() : 0;
    }
}
