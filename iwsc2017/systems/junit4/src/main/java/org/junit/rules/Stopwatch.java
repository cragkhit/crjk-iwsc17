package org.junit.rules;
import org.junit.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import java.util.concurrent.TimeUnit;
public class Stopwatch implements TestRule {
    private final Clock clock;
    private volatile long startNanos;
    private volatile long endNanos;
    public Stopwatch() {
        this ( new Clock() );
    }
    Stopwatch ( Clock clock ) {
        this.clock = clock;
    }
    public long runtime ( TimeUnit unit ) {
        return unit.convert ( getNanos(), TimeUnit.NANOSECONDS );
    }
    protected void succeeded ( long nanos, Description description ) {
    }
    protected void failed ( long nanos, Throwable e, Description description ) {
    }
    protected void skipped ( long nanos, AssumptionViolatedException e, Description description ) {
    }
    protected void finished ( long nanos, Description description ) {
    }
    private long getNanos() {
        if ( startNanos == 0 ) {
            throw new IllegalStateException ( "Test has not started" );
        }
        long currentEndNanos = endNanos;
        if ( currentEndNanos == 0 ) {
            currentEndNanos = clock.nanoTime();
        }
        return currentEndNanos - startNanos;
    }
    private void starting() {
        startNanos = clock.nanoTime();
        endNanos = 0;
    }
    private void stopping() {
        endNanos = clock.nanoTime();
    }
    public final Statement apply ( Statement base, Description description ) {
        return new InternalWatcher().apply ( base, description );
    }
    private class InternalWatcher extends TestWatcher {
        @Override protected void starting ( Description description ) {
            Stopwatch.this.starting();
        }
        @Override protected void finished ( Description description ) {
            Stopwatch.this.finished ( getNanos(), description );
        }
        @Override protected void succeeded ( Description description ) {
            Stopwatch.this.stopping();
            Stopwatch.this.succeeded ( getNanos(), description );
        }
        @Override protected void failed ( Throwable e, Description description ) {
            Stopwatch.this.stopping();
            Stopwatch.this.failed ( getNanos(), e, description );
        }
        @Override protected void skipped ( AssumptionViolatedException e, Description description ) {
            Stopwatch.this.stopping();
            Stopwatch.this.skipped ( getNanos(), e, description );
        }
    }
    static class Clock {
        public long nanoTime() {
            return System.nanoTime();
        }
    }
}
