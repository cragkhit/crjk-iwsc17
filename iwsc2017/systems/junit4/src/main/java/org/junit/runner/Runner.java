package org.junit.runner;
import org.junit.runner.notification.RunNotifier;
public abstract class Runner implements Describable {
    public abstract Description getDescription();
    public abstract void run ( RunNotifier notifier );
    public int testCount() {
        return getDescription().testCount();
    }
}
