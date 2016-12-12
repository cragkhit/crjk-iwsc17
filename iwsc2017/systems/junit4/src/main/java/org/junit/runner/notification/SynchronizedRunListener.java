package org.junit.runner.notification;
import org.junit.runner.Description;
import org.junit.runner.Result;
@RunListener.ThreadSafe
final class SynchronizedRunListener extends RunListener {
    private final RunListener listener;
    private final Object monitor;
    SynchronizedRunListener ( RunListener listener, Object monitor ) {
        this.listener = listener;
        this.monitor = monitor;
    }
    @Override
    public void testRunStarted ( Description description ) throws Exception {
        synchronized ( monitor ) {
            listener.testRunStarted ( description );
        }
    }
    @Override
    public void testRunFinished ( Result result ) throws Exception {
        synchronized ( monitor ) {
            listener.testRunFinished ( result );
        }
    }
    @Override
    public void testSuiteStarted ( Description description ) throws Exception {
        synchronized ( monitor ) {
            listener.testSuiteStarted ( description );
        }
    }
    @Override
    public void testSuiteFinished ( Description description ) throws Exception {
        synchronized ( monitor ) {
            listener.testSuiteFinished ( description );
        }
    }
    @Override
    public void testStarted ( Description description ) throws Exception {
        synchronized ( monitor ) {
            listener.testStarted ( description );
        }
    }
    @Override
    public void testFinished ( Description description ) throws Exception {
        synchronized ( monitor ) {
            listener.testFinished ( description );
        }
    }
    @Override
    public void testFailure ( Failure failure ) throws Exception {
        synchronized ( monitor ) {
            listener.testFailure ( failure );
        }
    }
    @Override
    public void testAssumptionFailure ( Failure failure ) {
        synchronized ( monitor ) {
            listener.testAssumptionFailure ( failure );
        }
    }
    @Override
    public void testIgnored ( Description description ) throws Exception {
        synchronized ( monitor ) {
            listener.testIgnored ( description );
        }
    }
    @Override
    public int hashCode() {
        return listener.hashCode();
    }
    @Override
    public boolean equals ( Object other ) {
        if ( this == other ) {
            return true;
        }
        if ( ! ( other instanceof SynchronizedRunListener ) ) {
            return false;
        }
        SynchronizedRunListener that = ( SynchronizedRunListener ) other;
        return listener.equals ( that.listener );
    }
    @Override
    public String toString() {
        return listener.toString() + " (with synchronization wrapper)";
    }
}
