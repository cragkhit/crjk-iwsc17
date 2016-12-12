package org.jfree.data.xy;
import org.jfree.util.ObjectUtilities;
import org.jfree.chart.util.ParamChecks;
import java.io.Serializable;
import org.jfree.data.ItemKey;
public class XYItemKey<S extends Comparable<S>> implements ItemKey, Comparable<XYItemKey<S>>, Serializable {
    private final S seriesKey;
    private final int itemIndex;
    public XYItemKey ( final S seriesKey, final int itemIndex ) {
        ParamChecks.nullNotPermitted ( seriesKey, "seriesKey" );
        this.seriesKey = seriesKey;
        this.itemIndex = itemIndex;
    }
    public S getSeriesKey() {
        return this.seriesKey;
    }
    public int getItemIndex() {
        return this.itemIndex;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof XYItemKey ) ) {
            return false;
        }
        final XYItemKey that = ( XYItemKey ) obj;
        return this.seriesKey.equals ( that.seriesKey ) && this.itemIndex == that.itemIndex;
    }
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + ObjectUtilities.hashCode ( ( Object ) this.seriesKey );
        hash = 41 * hash + this.itemIndex;
        return hash;
    }
    @Override
    public String toJSONString() {
        final StringBuilder sb = new StringBuilder();
        sb.append ( "{\"seriesKey\": \"" ).append ( this.seriesKey.toString() );
        sb.append ( "\", " );
        sb.append ( "\"itemIndex\": " ).append ( this.itemIndex ).append ( "}" );
        return sb.toString();
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append ( "XYItemKey[seriesKey=" );
        sb.append ( this.seriesKey.toString() ).append ( ",item=" );
        sb.append ( this.itemIndex );
        sb.append ( "]" );
        return sb.toString();
    }
    @Override
    public int compareTo ( final XYItemKey<S> key ) {
        int result = this.seriesKey.compareTo ( key.seriesKey );
        if ( result == 0 ) {
            result = this.itemIndex - key.itemIndex;
        }
        return result;
    }
}
