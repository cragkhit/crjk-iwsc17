package org.jfree.data;
import org.jfree.chart.util.ParamChecks;
import org.jfree.util.SortOrder;
import java.io.Serializable;
import java.util.Comparator;
public class KeyedValueComparator implements Comparator, Serializable {
    private KeyedValueComparatorType type;
    private SortOrder order;
    public KeyedValueComparator ( final KeyedValueComparatorType type, final SortOrder order ) {
        ParamChecks.nullNotPermitted ( type, "type" );
        ParamChecks.nullNotPermitted ( order, "order" );
        this.type = type;
        this.order = order;
    }
    public KeyedValueComparatorType getType() {
        return this.type;
    }
    public SortOrder getOrder() {
        return this.order;
    }
    @Override
    public int compare ( final Object o1, final Object o2 ) {
        if ( o2 == null ) {
            return -1;
        }
        if ( o1 == null ) {
            return 1;
        }
        final KeyedValue kv1 = ( KeyedValue ) o1;
        final KeyedValue kv2 = ( KeyedValue ) o2;
        int result;
        if ( this.type == KeyedValueComparatorType.BY_KEY ) {
            if ( this.order.equals ( ( Object ) SortOrder.ASCENDING ) ) {
                result = kv1.getKey().compareTo ( kv2.getKey() );
            } else {
                if ( !this.order.equals ( ( Object ) SortOrder.DESCENDING ) ) {
                    throw new IllegalArgumentException ( "Unrecognised sort order." );
                }
                result = kv2.getKey().compareTo ( kv1.getKey() );
            }
        } else {
            if ( this.type != KeyedValueComparatorType.BY_VALUE ) {
                throw new IllegalArgumentException ( "Unrecognised type." );
            }
            final Number n1 = kv1.getValue();
            final Number n2 = kv2.getValue();
            if ( n2 == null ) {
                return -1;
            }
            if ( n1 == null ) {
                return 1;
            }
            final double d1 = n1.doubleValue();
            final double d2 = n2.doubleValue();
            if ( this.order.equals ( ( Object ) SortOrder.ASCENDING ) ) {
                if ( d1 > d2 ) {
                    result = 1;
                } else if ( d1 < d2 ) {
                    result = -1;
                } else {
                    result = 0;
                }
            } else {
                if ( !this.order.equals ( ( Object ) SortOrder.DESCENDING ) ) {
                    throw new IllegalArgumentException ( "Unrecognised sort order." );
                }
                if ( d1 > d2 ) {
                    result = -1;
                } else if ( d1 < d2 ) {
                    result = 1;
                } else {
                    result = 0;
                }
            }
        }
        return result;
    }
}
