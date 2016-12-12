package org.junit.experimental.results;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
static final class ResultMatchers$1 extends TypeSafeMatcher<PrintableResult> {
    final   int val$count;
    public void describeTo ( final Description description ) {
        description.appendText ( "has " + this.val$count + " failures" );
    }
    public boolean matchesSafely ( final PrintableResult item ) {
        return item.failureCount() == this.val$count;
    }
}
