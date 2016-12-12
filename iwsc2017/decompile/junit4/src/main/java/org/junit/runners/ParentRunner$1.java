package org.junit.runners;
import org.junit.runners.model.RunnerScheduler;
class ParentRunner$1 implements RunnerScheduler {
    public void schedule ( final Runnable childStatement ) {
        childStatement.run();
    }
    public void finished() {
    }
}
