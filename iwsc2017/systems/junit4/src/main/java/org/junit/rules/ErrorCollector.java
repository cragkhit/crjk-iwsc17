package org.junit.rules;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertThrows;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.junit.function.ThrowingRunnable;
import org.hamcrest.Matcher;
import org.junit.runners.model.MultipleFailureException;
public class ErrorCollector extends Verifier {
    private List<Throwable> errors = new ArrayList<Throwable>();
    @Override
    protected void verify() throws Throwable {
        MultipleFailureException.assertEmpty ( errors );
    }
    public void addError ( Throwable error ) {
        errors.add ( error );
    }
    @Deprecated
    public <T> void checkThat ( final T value, final Matcher<T> matcher ) {
        checkThat ( "", value, matcher );
    }
    @Deprecated
    public <T> void checkThat ( final String reason, final T value, final Matcher<T> matcher ) {
        checkSucceeds ( new Callable<Object>() {
            public Object call() throws Exception {
                assertThat ( reason, value, matcher );
                return value;
            }
        } );
    }
    public <T> T checkSucceeds ( Callable<T> callable ) {
        try {
            return callable.call();
        } catch ( Throwable e ) {
            addError ( e );
            return null;
        }
    }
    public void checkThrows ( Class<? extends Throwable> expectedThrowable, ThrowingRunnable runnable ) {
        try {
            assertThrows ( expectedThrowable, runnable );
        } catch ( AssertionError e ) {
            addError ( e );
        }
    }
}
