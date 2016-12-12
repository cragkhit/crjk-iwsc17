package org.junit.internal;
import org.hamcrest.Description;
import org.hamcrest.StringDescription;
import org.hamcrest.Matcher;
import org.hamcrest.SelfDescribing;
public class AssumptionViolatedException extends RuntimeException implements SelfDescribing {
    private static final long serialVersionUID = 2L;
    private final String fAssumption;
    private final boolean fValueMatcher;
    private final Object fValue;
    private final Matcher<?> fMatcher;
    public AssumptionViolatedException ( final String assumption, final boolean hasValue, final Object value, final Matcher<?> matcher ) {
        this.fAssumption = assumption;
        this.fValue = value;
        this.fMatcher = matcher;
        this.fValueMatcher = hasValue;
        if ( value instanceof Throwable ) {
            this.initCause ( ( Throwable ) value );
        }
    }
    public AssumptionViolatedException ( final Object value, final Matcher<?> matcher ) {
        this ( null, true, value, matcher );
    }
    public AssumptionViolatedException ( final String assumption, final Object value, final Matcher<?> matcher ) {
        this ( assumption, true, value, matcher );
    }
    public AssumptionViolatedException ( final String assumption ) {
        this ( assumption, false, null, null );
    }
    public AssumptionViolatedException ( final String assumption, final Throwable e ) {
        this ( assumption, false, null, null );
        this.initCause ( e );
    }
    public String getMessage() {
        return StringDescription.asString ( ( SelfDescribing ) this );
    }
    public void describeTo ( final Description description ) {
        if ( this.fAssumption != null ) {
            description.appendText ( this.fAssumption );
        }
        if ( this.fValueMatcher ) {
            if ( this.fAssumption != null ) {
                description.appendText ( ": " );
            }
            description.appendText ( "got: " );
            description.appendValue ( this.fValue );
            if ( this.fMatcher != null ) {
                description.appendText ( ", expected: " );
                description.appendDescriptionOf ( ( SelfDescribing ) this.fMatcher );
            }
        }
    }
}
