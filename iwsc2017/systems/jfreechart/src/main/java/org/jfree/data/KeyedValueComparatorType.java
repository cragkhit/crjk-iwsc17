

package org.jfree.data;

import java.io.Serializable;


public final class KeyedValueComparatorType implements Serializable {


    public static final KeyedValueComparatorType BY_KEY
        = new KeyedValueComparatorType ( "KeyedValueComparatorType.BY_KEY" );


    public static final KeyedValueComparatorType BY_VALUE
        = new KeyedValueComparatorType ( "KeyedValueComparatorType.BY_VALUE" );


    private String name;


    private KeyedValueComparatorType ( String name ) {
        this.name = name;
    }


    @Override
    public String toString() {
        return this.name;
    }


    @Override
    public boolean equals ( Object o ) {
        if ( this == o ) {
            return true;
        }
        if ( ! ( o instanceof KeyedValueComparatorType ) ) {
            return false;
        }

        KeyedValueComparatorType type = ( KeyedValueComparatorType ) o;
        if ( !this.name.equals ( type.name ) ) {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}

