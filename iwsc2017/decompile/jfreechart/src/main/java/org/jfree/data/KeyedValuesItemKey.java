package org.jfree.data;
import org.jfree.chart.util.ParamChecks;
import java.io.Serializable;
public class KeyedValuesItemKey implements ItemKey, Serializable {
    Comparable<?> key;
    public KeyedValuesItemKey ( final Comparable<?> key ) {
        ParamChecks.nullNotPermitted ( key, "key" );
        this.key = key;
    }
    public Comparable<?> getKey() {
        return this.key;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof KeyedValuesItemKey ) ) {
            return false;
        }
        final KeyedValuesItemKey that = ( KeyedValuesItemKey ) obj;
        return this.key.equals ( that.key );
    }
    @Override
    public String toJSONString() {
        final StringBuilder sb = new StringBuilder();
        sb.append ( "{\"key\": \"" ).append ( this.key.toString() ).append ( "\"}" );
        return sb.toString();
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append ( "KeyedValuesItemKey[" );
        sb.append ( this.key.toString() );
        sb.append ( "]" );
        return sb.toString();
    }
}
