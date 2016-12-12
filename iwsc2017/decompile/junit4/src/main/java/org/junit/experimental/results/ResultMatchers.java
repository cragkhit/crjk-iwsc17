package org.junit.experimental.results;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.Matcher;
public class ResultMatchers {
    public static Matcher<PrintableResult> isSuccessful() {
        return failureCountIs ( 0 );
    }
    public static Matcher<PrintableResult> failureCountIs ( final int count ) {
        return ( Matcher<PrintableResult> ) new TypeSafeMatcher<PrintableResult>() {
            public void describeTo ( final Description description ) {
                description.appendText ( "has " + count + " failures" );
            }
            public boolean matchesSafely ( final PrintableResult item ) {
                return item.failureCount() == count;
            }
        };
    }
    public static Matcher<Object> hasSingleFailureContaining ( final String string ) {
        return ( Matcher<Object> ) new BaseMatcher<Object>() {
            public boolean matches ( final Object item ) {
                return item.toString().contains ( string ) && ResultMatchers.failureCountIs ( 1 ).matches ( item );
            }
            public void describeTo ( final Description description ) {
                description.appendText ( "has single failure containing " + string );
            }
        };
    }
    public static Matcher<PrintableResult> hasFailureContaining ( final String string ) {
        return ( Matcher<PrintableResult> ) new TypeSafeMatcher<PrintableResult>() {
            public boolean matchesSafely ( final PrintableResult item ) {
                return item.failureCount() > 0 && item.toString().contains ( string );
            }
            public void describeTo ( final Description description ) {
                description.appendText ( "has failure containing " + string );
            }
        };
    }
}
