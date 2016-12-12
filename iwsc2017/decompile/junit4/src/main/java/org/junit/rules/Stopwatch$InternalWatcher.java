package org.junit.rules;
import org.junit.AssumptionViolatedException;
import org.junit.runner.Description;
private class InternalWatcher extends TestWatcher {
    protected void starting ( final Description description ) {
        Stopwatch.access$100 ( Stopwatch.this );
    }
    protected void finished ( final Description description ) {
        Stopwatch.this.finished ( Stopwatch.access$200 ( Stopwatch.this ), description );
    }
    protected void succeeded ( final Description description ) {
        Stopwatch.access$300 ( Stopwatch.this );
        Stopwatch.this.succeeded ( Stopwatch.access$200 ( Stopwatch.this ), description );
    }
    protected void failed ( final Throwable e, final Description description ) {
        Stopwatch.access$300 ( Stopwatch.this );
        Stopwatch.this.failed ( Stopwatch.access$200 ( Stopwatch.this ), e, description );
    }
    protected void skipped ( final AssumptionViolatedException e, final Description description ) {
        Stopwatch.access$300 ( Stopwatch.this );
        Stopwatch.this.skipped ( Stopwatch.access$200 ( Stopwatch.this ), e, description );
    }
}
