package org.junit.internal.matchers;
import org.hamcrest.Factory;
import org.junit.internal.Throwables;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
@Deprecated
public class StacktracePrintingMatcher<T extends Throwable> extends TypeSafeMatcher<T> {
    private final Matcher<T> throwableMatcher;
    public StacktracePrintingMatcher ( final Matcher<T> throwableMatcher ) {
        this.throwableMatcher = throwableMatcher;
    }
    public void describeTo ( final Description description ) {
        this.throwableMatcher.describeTo ( description );
    }
    protected boolean matchesSafely ( final T item ) {
        return this.throwableMatcher.matches ( ( Object ) item );
    }
    protected void describeMismatchSafely ( final T item, final Description description ) {
        this.throwableMatcher.describeMismatch ( ( Object ) item, description );
        description.appendText ( "\nStacktrace was: " );
        description.appendText ( this.readStacktrace ( item ) );
    }
    private String readStacktrace ( final Throwable throwable ) {
        return Throwables.getStacktrace ( throwable );
    }
    @Factory
    public static <T extends Throwable> Matcher<T> isThrowable ( final Matcher<T> throwableMatcher ) {
        return ( Matcher<T> ) new StacktracePrintingMatcher ( ( org.hamcrest.Matcher<Throwable> ) throwableMatcher );
    }
    @Factory
    public static <T extends Exception> Matcher<T> isException ( final Matcher<T> exceptionMatcher ) {
        return ( Matcher<T> ) new StacktracePrintingMatcher ( ( org.hamcrest.Matcher<Throwable> ) exceptionMatcher );
    }
}
