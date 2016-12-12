package org.junit.experimental.results;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
static final class ResultMatchers$3 extends TypeSafeMatcher<PrintableResult> {
    final   String val$string;
    public boolean matchesSafely ( final PrintableResult item ) {
        return item.failureCount() > 0 && item.toString().contains ( this.val$string );
    }
    public void describeTo ( final Description description ) {
        description.appendText ( "has failure containing " + this.val$string );
    }
}
