package org.jfree.data;
import org.jfree.chart.util.ParamChecks;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class DefaultKeyedValue implements KeyedValue, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = -7388924517460437712L;
    private Comparable key;
    private Number value;
    public DefaultKeyedValue ( final Comparable key, final Number value ) {
        ParamChecks.nullNotPermitted ( key, "key" );
        this.key = key;
        this.value = value;
    }
    @Override
    public Comparable getKey() {
        return this.key;
    }
    public Number getValue() {
        return this.value;
    }
    public synchronized void setValue ( final Number value ) {
        this.value = value;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof DefaultKeyedValue ) ) {
            return false;
        }
        final DefaultKeyedValue that = ( DefaultKeyedValue ) obj;
        if ( !this.key.equals ( that.key ) ) {
            return false;
        }
        if ( this.value != null ) {
            if ( this.value.equals ( that.value ) ) {
                return true;
            }
        } else if ( that.value == null ) {
            return true;
        }
        return false;
    }
    @Override
    public int hashCode() {
        int result = ( this.key != null ) ? this.key.hashCode() : 0;
        result = 29 * result + ( ( this.value != null ) ? this.value.hashCode() : 0 );
        return result;
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    @Override
    public String toString() {
        return "(" + this.key.toString() + ", " + this.value.toString() + ")";
    }
}
