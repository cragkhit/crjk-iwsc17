package org.junit.internal.runners;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.junit.runner.Describable;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import junit.framework.Test;
import org.junit.runner.notification.RunNotifier;
import junit.framework.TestListener;
private static final class OldTestClassAdaptingListener implements TestListener {
    private final RunNotifier notifier;
    private OldTestClassAdaptingListener ( final RunNotifier notifier ) {
        this.notifier = notifier;
    }
    public void endTest ( final Test test ) {
        this.notifier.fireTestFinished ( this.asDescription ( test ) );
    }
    public void startTest ( final Test test ) {
        this.notifier.fireTestStarted ( this.asDescription ( test ) );
    }
    public void addError ( final Test test, final Throwable e ) {
        final Failure failure = new Failure ( this.asDescription ( test ), e );
        this.notifier.fireTestFailure ( failure );
    }
    private Description asDescription ( final Test test ) {
        if ( test instanceof Describable ) {
            final Describable facade = ( Describable ) test;
            return facade.getDescription();
        }
        return Description.createTestDescription ( this.getEffectiveClass ( test ), this.getName ( test ) );
    }
    private Class<? extends Test> getEffectiveClass ( final Test test ) {
        return test.getClass();
    }
    private String getName ( final Test test ) {
        if ( test instanceof TestCase ) {
            return ( ( TestCase ) test ).getName();
        }
        return test.toString();
    }
    public void addFailure ( final Test test, final AssertionFailedError t ) {
        this.addError ( test, ( Throwable ) t );
    }
}
