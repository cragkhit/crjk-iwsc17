package org.jfree.data;
import java.io.Serializable;
import org.jfree.chart.util.ParamChecks;
public class KeyedValuesItemKey implements ItemKey, Serializable {
    Comparable<? extends Object> key;
    public KeyedValuesItemKey ( Comparable<? extends Object> key ) {
        ParamChecks.nullNotPermitted ( key, "key" );
        this.key = key;
    }
    public Comparable<?> getKey() {
        return this.key;
    }
    @Override
    public boolean equals ( Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof KeyedValuesItemKey ) ) {
            return false;
        }
        KeyedValuesItemKey that = ( KeyedValuesItemKey ) obj;
        if ( !this.key.equals ( that.key ) ) {
            return false;
        }
        return true;
    }
    @Override
    public String toJSONString() {
        StringBuilder sb = new StringBuilder();
        sb.append ( "{\"key\": \"" ).append ( this.key.toString() ).append ( "\"}" );
        return sb.toString();
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append ( "KeyedValuesItemKey[" );
        sb.append ( this.key.toString() );
        sb.append ( "]" );
        return sb.toString();
    }
}
