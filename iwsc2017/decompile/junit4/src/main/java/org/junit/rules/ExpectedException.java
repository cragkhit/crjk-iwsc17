package org.junit.rules;
import org.hamcrest.SelfDescribing;
import org.hamcrest.StringDescription;
import org.junit.Assert;
import org.junit.internal.matchers.ThrowableCauseMatcher;
import org.junit.internal.matchers.ThrowableMessageMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
public class ExpectedException implements TestRule {
    private final ExpectedExceptionMatcherBuilder matcherBuilder;
    private String missingExceptionMessage;
    public static ExpectedException none() {
        return new ExpectedException();
    }
    private ExpectedException() {
        this.matcherBuilder = new ExpectedExceptionMatcherBuilder();
        this.missingExceptionMessage = "Expected test to throw %s";
    }
    @Deprecated
    public ExpectedException handleAssertionErrors() {
        return this;
    }
    @Deprecated
    public ExpectedException handleAssumptionViolatedExceptions() {
        return this;
    }
    public ExpectedException reportMissingExceptionWithMessage ( final String message ) {
        this.missingExceptionMessage = message;
        return this;
    }
    public Statement apply ( final Statement base, final Description description ) {
        return new ExpectedExceptionStatement ( base );
    }
    @Deprecated
    public ExpectedException expect ( final Matcher<?> matcher ) {
        this.matcherBuilder.add ( matcher );
        return this;
    }
    public ExpectedException expect ( final Class<? extends Throwable> type ) {
        this.expect ( ( Matcher<?> ) CoreMatchers.instanceOf ( ( Class ) type ) );
        return this;
    }
    public ExpectedException expectMessage ( final String substring ) {
        this.expectMessage ( ( Matcher<String> ) CoreMatchers.containsString ( substring ) );
        return this;
    }
    @Deprecated
    public ExpectedException expectMessage ( final Matcher<String> matcher ) {
        this.expect ( ThrowableMessageMatcher.hasMessage ( matcher ) );
        return this;
    }
    @Deprecated
    public ExpectedException expectCause ( final Matcher<? extends Throwable> expectedCause ) {
        this.expect ( ThrowableCauseMatcher.hasCause ( expectedCause ) );
        return this;
    }
    private void handleException ( final Throwable e ) throws Throwable {
        if ( this.isAnyExceptionExpected() ) {
            Assert.assertThat ( e, this.matcherBuilder.build() );
            return;
        }
        throw e;
    }
    private boolean isAnyExceptionExpected() {
        return this.matcherBuilder.expectsThrowable();
    }
    private void failDueToMissingException() throws AssertionError {
        Assert.fail ( this.missingExceptionMessage() );
    }
    private String missingExceptionMessage() {
        final String expectation = StringDescription.toString ( ( SelfDescribing ) this.matcherBuilder.build() );
        return String.format ( this.missingExceptionMessage, expectation );
    }
    private class ExpectedExceptionStatement extends Statement {
        private final Statement next;
        public ExpectedExceptionStatement ( final Statement base ) {
            this.next = base;
        }
        public void evaluate() throws Throwable {
            try {
                this.next.evaluate();
            } catch ( Throwable e ) {
                ExpectedException.this.handleException ( e );
                return;
            }
            if ( ExpectedException.this.isAnyExceptionExpected() ) {
                ExpectedException.this.failDueToMissingException();
            }
        }
    }
}
