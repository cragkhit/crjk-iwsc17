package org.junit.experimental.results;
import org.hamcrest.Description;
import org.hamcrest.BaseMatcher;
static final class ResultMatchers$2 extends BaseMatcher<Object> {
    final   String val$string;
    public boolean matches ( final Object item ) {
        return item.toString().contains ( this.val$string ) && ResultMatchers.failureCountIs ( 1 ).matches ( item );
    }
    public void describeTo ( final Description description ) {
        description.appendText ( "has single failure containing " + this.val$string );
    }
}
