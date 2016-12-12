package org.junit.experimental;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import org.junit.runners.model.RunnerScheduler;
static final class ParallelComputer$1 implements RunnerScheduler {
    private final ExecutorService fService = Executors.newCachedThreadPool();
    public void schedule ( final Runnable childStatement ) {
        this.fService.submit ( childStatement );
    }
    public void finished() {
        try {
            this.fService.shutdown();
            this.fService.awaitTermination ( Long.MAX_VALUE, TimeUnit.NANOSECONDS );
        } catch ( InterruptedException e ) {
            e.printStackTrace ( System.err );
        }
    }
}
