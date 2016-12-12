package org.jfree.data;
import java.io.Serializable;
public final class KeyedValueComparatorType implements Serializable {
    public static final KeyedValueComparatorType BY_KEY;
    public static final KeyedValueComparatorType BY_VALUE;
    private String name;
    private KeyedValueComparatorType ( final String name ) {
        this.name = name;
    }
    @Override
    public String toString() {
        return this.name;
    }
    @Override
    public boolean equals ( final Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( ! ( o instanceof KeyedValueComparatorType ) ) {
            return false;
        }
        final KeyedValueComparatorType type = ( KeyedValueComparatorType ) o;
        return this.name.equals ( type.name );
    }
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
    static {
        BY_KEY = new KeyedValueComparatorType ( "KeyedValueComparatorType.BY_KEY" );
        BY_VALUE = new KeyedValueComparatorType ( "KeyedValueComparatorType.BY_VALUE" );
    }
}
