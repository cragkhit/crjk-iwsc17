package org.junit.rules;
import static java.lang.String.format;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.internal.matchers.ThrowableCauseMatcher.hasCause;
import static org.junit.internal.matchers.ThrowableMessageMatcher.hasMessage;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.AssumptionViolatedException;
import org.junit.runners.model.Statement;
public class ExpectedException implements TestRule {
    public static ExpectedException none() {
        return new ExpectedException();
    }
    private final ExpectedExceptionMatcherBuilder matcherBuilder = new ExpectedExceptionMatcherBuilder();
    private String missingExceptionMessage = "Expected test to throw %s";
    private ExpectedException() {
    }
    @Deprecated
    public ExpectedException handleAssertionErrors() {
        return this;
    }
    @Deprecated
    public ExpectedException handleAssumptionViolatedExceptions() {
        return this;
    }
    public ExpectedException reportMissingExceptionWithMessage ( String message ) {
        missingExceptionMessage = message;
        return this;
    }
    public Statement apply ( Statement base,
                             org.junit.runner.Description description ) {
        return new ExpectedExceptionStatement ( base );
    }
    @Deprecated
    public ExpectedException expect ( Matcher<?> matcher ) {
        matcherBuilder.add ( matcher );
        return this;
    }
    public ExpectedException expect ( Class<? extends Throwable> type ) {
        expect ( instanceOf ( type ) );
        return this;
    }
    public ExpectedException expectMessage ( String substring ) {
        expectMessage ( containsString ( substring ) );
        return this;
    }
    @Deprecated
    public ExpectedException expectMessage ( Matcher<String> matcher ) {
        expect ( hasMessage ( matcher ) );
        return this;
    }
    @Deprecated
    public ExpectedException expectCause ( Matcher<? extends Throwable> expectedCause ) {
        expect ( hasCause ( expectedCause ) );
        return this;
    }
    private class ExpectedExceptionStatement extends Statement {
        private final Statement next;
        public ExpectedExceptionStatement ( Statement base ) {
            next = base;
        }
        @Override
        public void evaluate() throws Throwable {
            try {
                next.evaluate();
            } catch ( Throwable e ) {
                handleException ( e );
                return;
            }
            if ( isAnyExceptionExpected() ) {
                failDueToMissingException();
            }
        }
    }
    private void handleException ( Throwable e ) throws Throwable {
        if ( isAnyExceptionExpected() ) {
            assertThat ( e, matcherBuilder.build() );
        } else {
            throw e;
        }
    }
    private boolean isAnyExceptionExpected() {
        return matcherBuilder.expectsThrowable();
    }
    private void failDueToMissingException() throws AssertionError {
        fail ( missingExceptionMessage() );
    }
    private String missingExceptionMessage() {
        String expectation = StringDescription.toString ( matcherBuilder.build() );
        return format ( missingExceptionMessage, expectation );
    }
}
