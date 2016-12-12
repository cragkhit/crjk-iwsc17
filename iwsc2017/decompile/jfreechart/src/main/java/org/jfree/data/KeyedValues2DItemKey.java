package org.jfree.data;
import org.jfree.util.ObjectUtilities;
import org.jfree.chart.util.ParamChecks;
import java.io.Serializable;
public class KeyedValues2DItemKey<R extends Comparable<R>, C extends Comparable<C>> implements ItemKey, Comparable<KeyedValues2DItemKey<R, C>>, Serializable {
    R rowKey;
    C columnKey;
    public KeyedValues2DItemKey ( final R rowKey, final C columnKey ) {
        ParamChecks.nullNotPermitted ( rowKey, "rowKey" );
        ParamChecks.nullNotPermitted ( columnKey, "columnKey" );
        this.rowKey = rowKey;
        this.columnKey = columnKey;
    }
    public R getRowKey() {
        return this.rowKey;
    }
    public C getColumnKey() {
        return this.columnKey;
    }
    @Override
    public int compareTo ( final KeyedValues2DItemKey<R, C> key ) {
        int result = this.rowKey.compareTo ( key.rowKey );
        if ( result == 0 ) {
            result = this.columnKey.compareTo ( key.columnKey );
        }
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof KeyedValues2DItemKey ) ) {
            return false;
        }
        final KeyedValues2DItemKey that = ( KeyedValues2DItemKey ) obj;
        return this.rowKey.equals ( that.rowKey ) && this.columnKey.equals ( that.columnKey );
    }
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + ObjectUtilities.hashCode ( ( Object ) this.rowKey );
        hash = 17 * hash + ObjectUtilities.hashCode ( ( Object ) this.columnKey );
        return hash;
    }
    @Override
    public String toJSONString() {
        final StringBuilder sb = new StringBuilder();
        sb.append ( "{\"rowKey\": \"" ).append ( this.rowKey.toString() );
        sb.append ( "\", " );
        sb.append ( "\"columnKey\": \"" ).append ( this.columnKey.toString() );
        sb.append ( "\"}" );
        return sb.toString();
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append ( "Values2DItemKey[row=" );
        sb.append ( this.rowKey.toString() ).append ( ",column=" );
        sb.append ( this.columnKey.toString() );
        sb.append ( "]" );
        return sb.toString();
    }
}
