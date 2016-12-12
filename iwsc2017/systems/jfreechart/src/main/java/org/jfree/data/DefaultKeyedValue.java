

package org.jfree.data;

import java.io.Serializable;
import org.jfree.chart.util.ParamChecks;

import org.jfree.util.PublicCloneable;


public class DefaultKeyedValue implements KeyedValue, Cloneable,
    PublicCloneable, Serializable {


    private static final long serialVersionUID = -7388924517460437712L;


    private Comparable key;


    private Number value;


    public DefaultKeyedValue ( Comparable key, Number value ) {
        ParamChecks.nullNotPermitted ( key, "key" );
        this.key = key;
        this.value = value;
    }


    @Override
    public Comparable getKey() {
        return this.key;
    }


    @Override
    public Number getValue() {
        return this.value;
    }


    public synchronized void setValue ( Number value ) {
        this.value = value;
    }


    @Override
    public boolean equals ( Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof DefaultKeyedValue ) ) {
            return false;
        }
        DefaultKeyedValue that = ( DefaultKeyedValue ) obj;

        if ( !this.key.equals ( that.key ) ) {
            return false;
        }
        if ( this.value != null
                ? !this.value.equals ( that.value ) : that.value != null ) {
            return false;
        }
        return true;
    }


    @Override
    public int hashCode() {
        int result;
        result = ( this.key != null ? this.key.hashCode() : 0 );
        result = 29 * result + ( this.value != null ? this.value.hashCode() : 0 );
        return result;
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        return ( DefaultKeyedValue ) super.clone();
    }


    @Override
    public String toString() {
        return "(" + this.key.toString() + ", " + this.value.toString() + ")";
    }

}
