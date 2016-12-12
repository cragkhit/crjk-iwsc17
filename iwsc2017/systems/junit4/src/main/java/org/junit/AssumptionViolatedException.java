package org.junit;
import org.hamcrest.Matcher;
@SuppressWarnings ( "deprecation" )
public class AssumptionViolatedException extends org.junit.internal.AssumptionViolatedException {
    private static final long serialVersionUID = 1L;
    @Deprecated
    public <T> AssumptionViolatedException ( T actual, Matcher<T> matcher ) {
        super ( actual, matcher );
    }
    @Deprecated
    public <T> AssumptionViolatedException ( String message, T actual, Matcher<T> matcher ) {
        super ( message, actual, matcher );
    }
    public AssumptionViolatedException ( String message ) {
        super ( message );
    }
    public AssumptionViolatedException ( String message, Throwable t ) {
        super ( message, t );
    }
}
