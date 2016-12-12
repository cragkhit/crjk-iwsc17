package org.junit.experimental.max;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import java.util.HashMap;
import org.junit.runner.Description;
import java.util.Map;
import org.junit.runner.notification.RunListener;
private final class RememberingListener extends RunListener {
    private long overallStart;
    private Map<Description, Long> starts;
    private RememberingListener() {
        this.overallStart = System.currentTimeMillis();
        this.starts = new HashMap<Description, Long>();
    }
    public void testStarted ( final Description description ) throws Exception {
        this.starts.put ( description, System.nanoTime() );
    }
    public void testFinished ( final Description description ) throws Exception {
        final long end = System.nanoTime();
        final long start = this.starts.get ( description );
        MaxHistory.this.putTestDuration ( description, end - start );
    }
    public void testFailure ( final Failure failure ) throws Exception {
        MaxHistory.this.putTestFailureTimestamp ( failure.getDescription(), this.overallStart );
    }
    public void testRunFinished ( final Result result ) throws Exception {
        MaxHistory.access$000 ( MaxHistory.this );
    }
}
