package org.jfree.chart.util;
public class ParamChecks {
    public static void nullNotPermitted ( final Object param, final String name ) {
        if ( param == null ) {
            throw new IllegalArgumentException ( "Null '" + name + "' argument." );
        }
    }
    public static void requireNonNegative ( final int value, final String name ) {
        if ( value < 0 ) {
            throw new IllegalArgumentException ( "Require '" + name + "' (" + value + ") to be non-negative." );
        }
    }
    public static void requireInRange ( final int value, final String name, final int lowerBound, final int upperBound ) {
        if ( value < lowerBound ) {
            throw new IllegalArgumentException ( "Require '" + name + "' (" + value + ") to be in the range " + lowerBound + " to " + upperBound );
        }
    }
}
