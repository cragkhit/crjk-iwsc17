package org.junit.internal.matchers;
import org.hamcrest.Factory;
import org.hamcrest.SelfDescribing;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
public class ThrowableMessageMatcher<T extends Throwable> extends TypeSafeMatcher<T> {
    private final Matcher<String> matcher;
    public ThrowableMessageMatcher ( final Matcher<String> matcher ) {
        this.matcher = matcher;
    }
    public void describeTo ( final Description description ) {
        description.appendText ( "exception with message " );
        description.appendDescriptionOf ( ( SelfDescribing ) this.matcher );
    }
    protected boolean matchesSafely ( final T item ) {
        return this.matcher.matches ( ( Object ) item.getMessage() );
    }
    protected void describeMismatchSafely ( final T item, final Description description ) {
        description.appendText ( "message " );
        this.matcher.describeMismatch ( ( Object ) item.getMessage(), description );
    }
    @Factory
    public static <T extends Throwable> Matcher<T> hasMessage ( final Matcher<String> matcher ) {
        return ( Matcher<T> ) new ThrowableMessageMatcher ( matcher );
    }
}
