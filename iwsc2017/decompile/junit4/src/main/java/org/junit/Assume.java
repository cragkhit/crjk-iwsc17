package org.junit;
import java.util.List;
import java.util.Arrays;
import org.hamcrest.Matcher;
import org.hamcrest.CoreMatchers;
public class Assume {
    public static void assumeTrue ( final boolean b ) {
        assumeThat ( b, ( org.hamcrest.Matcher<Boolean> ) CoreMatchers.is ( ( Object ) true ) );
    }
    public static void assumeFalse ( final boolean b ) {
        assumeTrue ( !b );
    }
    public static void assumeTrue ( final String message, final boolean b ) {
        if ( !b ) {
            throw new AssumptionViolatedException ( message );
        }
    }
    public static void assumeFalse ( final String message, final boolean b ) {
        assumeTrue ( message, !b );
    }
    public static void assumeNotNull ( final Object... objects ) {
        assumeThat ( objects, ( org.hamcrest.Matcher<Object[]> ) CoreMatchers.notNullValue() );
        assumeThat ( Arrays.asList ( objects ), ( org.hamcrest.Matcher<List<Object>> ) CoreMatchers.everyItem ( CoreMatchers.notNullValue() ) );
    }
    @Deprecated
    public static <T> void assumeThat ( final T actual, final Matcher<T> matcher ) {
        if ( !matcher.matches ( ( Object ) actual ) ) {
            throw new AssumptionViolatedException ( ( T ) actual, ( Matcher<T> ) matcher );
        }
    }
    @Deprecated
    public static <T> void assumeThat ( final String message, final T actual, final Matcher<T> matcher ) {
        if ( !matcher.matches ( ( Object ) actual ) ) {
            throw new AssumptionViolatedException ( message, ( T ) actual, ( Matcher<T> ) matcher );
        }
    }
    public static void assumeNoException ( final Throwable e ) {
        assumeThat ( e, ( org.hamcrest.Matcher<Throwable> ) CoreMatchers.nullValue() );
    }
    public static void assumeNoException ( final String message, final Throwable e ) {
        assumeThat ( message, e, ( org.hamcrest.Matcher<Throwable> ) CoreMatchers.nullValue() );
    }
}
