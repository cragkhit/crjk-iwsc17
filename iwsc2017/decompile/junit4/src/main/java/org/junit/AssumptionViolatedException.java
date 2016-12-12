package org.junit;
import org.hamcrest.Matcher;
import org.junit.internal.AssumptionViolatedException;
public class AssumptionViolatedException extends org.junit.internal.AssumptionViolatedException {
    private static final long serialVersionUID = 1L;
    public AssumptionViolatedException ( final T actual, final Matcher<T> matcher ) {
        super ( actual, matcher );
    }
    public AssumptionViolatedException ( final String message, final T actual, final Matcher<T> matcher ) {
        super ( message, actual, matcher );
    }
    public AssumptionViolatedException ( final String message ) {
        super ( message );
    }
    public AssumptionViolatedException ( final String message, final Throwable t ) {
        super ( message, t );
    }
}
