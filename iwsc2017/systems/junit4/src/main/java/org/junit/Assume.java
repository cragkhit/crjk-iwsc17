package org.junit;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import org.hamcrest.Matcher;
public class Assume {
    @Deprecated
    public Assume() {
    }
    public static void assumeTrue ( boolean b ) {
        assumeThat ( b, is ( true ) );
    }
    public static void assumeFalse ( boolean b ) {
        assumeTrue ( !b );
    }
    public static void assumeTrue ( String message, boolean b ) {
        if ( !b ) {
            throw new AssumptionViolatedException ( message );
        }
    }
    public static void assumeFalse ( String message, boolean b ) {
        assumeTrue ( message, !b );
    }
    public static void assumeNotNull ( Object... objects ) {
        assumeThat ( objects, notNullValue() );
        assumeThat ( asList ( objects ), everyItem ( notNullValue() ) );
    }
    @Deprecated
    public static <T> void assumeThat ( T actual, Matcher<T> matcher ) {
        if ( !matcher.matches ( actual ) ) {
            throw new AssumptionViolatedException ( actual, matcher );
        }
    }
    @Deprecated
    public static <T> void assumeThat ( String message, T actual, Matcher<T> matcher ) {
        if ( !matcher.matches ( actual ) ) {
            throw new AssumptionViolatedException ( message, actual, matcher );
        }
    }
    public static void assumeNoException ( Throwable e ) {
        assumeThat ( e, nullValue() );
    }
    public static void assumeNoException ( String message, Throwable e ) {
        assumeThat ( message, e, nullValue() );
    }
}
