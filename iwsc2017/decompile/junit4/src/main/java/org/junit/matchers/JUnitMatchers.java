package org.junit.matchers;
import org.junit.internal.matchers.StacktracePrintingMatcher;
import org.hamcrest.core.CombinableMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
@Deprecated
public class JUnitMatchers {
    @Deprecated
    public static <T> Matcher<Iterable<? super T>> hasItem ( final T element ) {
        return ( Matcher<Iterable<? super T>> ) CoreMatchers.hasItem ( ( Object ) element );
    }
    @Deprecated
    public static <T> Matcher<Iterable<? super T>> hasItem ( final Matcher<? super T> elementMatcher ) {
        return ( Matcher<Iterable<? super T>> ) CoreMatchers.hasItem ( ( Matcher ) elementMatcher );
    }
    @Deprecated
    public static <T> Matcher<Iterable<T>> hasItems ( final T... elements ) {
        return ( Matcher<Iterable<T>> ) CoreMatchers.hasItems ( ( Object[] ) elements );
    }
    @Deprecated
    public static <T> Matcher<Iterable<T>> hasItems ( final Matcher<? super T>... elementMatchers ) {
        return ( Matcher<Iterable<T>> ) CoreMatchers.hasItems ( ( Matcher[] ) elementMatchers );
    }
    @Deprecated
    public static <T> Matcher<Iterable<T>> everyItem ( final Matcher<T> elementMatcher ) {
        return ( Matcher<Iterable<T>> ) CoreMatchers.everyItem ( ( Matcher ) elementMatcher );
    }
    @Deprecated
    public static Matcher<String> containsString ( final String substring ) {
        return ( Matcher<String> ) CoreMatchers.containsString ( substring );
    }
    @Deprecated
    public static <T> CombinableMatcher.CombinableBothMatcher<T> both ( final Matcher<? super T> matcher ) {
        return ( CombinableMatcher.CombinableBothMatcher<T> ) CoreMatchers.both ( ( Matcher ) matcher );
    }
    @Deprecated
    public static <T> CombinableMatcher.CombinableEitherMatcher<T> either ( final Matcher<? super T> matcher ) {
        return ( CombinableMatcher.CombinableEitherMatcher<T> ) CoreMatchers.either ( ( Matcher ) matcher );
    }
    public static <T extends Throwable> Matcher<T> isThrowable ( final Matcher<T> throwableMatcher ) {
        return StacktracePrintingMatcher.isThrowable ( throwableMatcher );
    }
    public static <T extends Exception> Matcher<T> isException ( final Matcher<T> exceptionMatcher ) {
        return StacktracePrintingMatcher.isException ( exceptionMatcher );
    }
}
