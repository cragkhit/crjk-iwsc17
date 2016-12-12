package org.junit.internal.builders;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.Runner;
public class IgnoredClassRunner extends Runner {
    private final Class<?> clazz;
    public IgnoredClassRunner ( final Class<?> testClass ) {
        this.clazz = testClass;
    }
    public void run ( final RunNotifier notifier ) {
        notifier.fireTestIgnored ( this.getDescription() );
    }
    public Description getDescription() {
        return Description.createSuiteDescription ( this.clazz );
    }
}
