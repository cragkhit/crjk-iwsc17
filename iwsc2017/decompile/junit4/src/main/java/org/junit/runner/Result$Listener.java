package org.junit.runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
@ThreadSafe
private class Listener extends RunListener {
    public void testRunStarted ( final Description description ) throws Exception {
        Result.access$500 ( Result.this ).set ( System.currentTimeMillis() );
    }
    public void testRunFinished ( final Result result ) throws Exception {
        final long endTime = System.currentTimeMillis();
        Result.access$600 ( Result.this ).addAndGet ( endTime - Result.access$500 ( Result.this ).get() );
    }
    public void testFinished ( final Description description ) throws Exception {
        Result.access$700 ( Result.this ).getAndIncrement();
    }
    public void testFailure ( final Failure failure ) throws Exception {
        Result.access$800 ( Result.this ).add ( failure );
    }
    public void testIgnored ( final Description description ) throws Exception {
        Result.access$900 ( Result.this ).getAndIncrement();
    }
    public void testAssumptionFailure ( final Failure failure ) {
    }
}
