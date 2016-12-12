package org.junit.runners.model;
public interface RunnerScheduler {
    void schedule ( Runnable p0 );
    void finished();
}
