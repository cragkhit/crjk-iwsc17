package org.junit.internal.runners;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.junit.runner.notification.Failure;
import org.junit.runners.model.InitializationError;
import java.util.Collections;
import org.junit.runners.model.InvalidTestClassError;
import java.lang.reflect.InvocationTargetException;
import org.junit.runner.notification.RunNotifier;
import java.util.Iterator;
import java.lang.annotation.Annotation;
import org.junit.runner.Description;
import java.util.List;
import org.junit.runner.Runner;
public class ErrorReportingRunner extends Runner {
    private final List<Throwable> causes;
    private final String classNames;
    public ErrorReportingRunner ( final Class<?> testClass, final Throwable cause ) {
        this ( cause, ( Class<?>[] ) new Class[] { testClass } );
    }
    public ErrorReportingRunner ( final Throwable cause, final Class<?>... testClasses ) {
        if ( testClasses == null || testClasses.length == 0 ) {
            throw new NullPointerException ( "Test classes cannot be null or empty" );
        }
        for ( final Class<?> testClass : testClasses ) {
            if ( testClass == null ) {
                throw new NullPointerException ( "Test class cannot be null" );
            }
        }
        this.classNames = this.getClassNames ( testClasses );
        this.causes = this.getCauses ( cause );
    }
    public Description getDescription() {
        final Description description = Description.createSuiteDescription ( this.classNames, new Annotation[0] );
        for ( final Throwable each : this.causes ) {
            description.addChild ( this.describeCause() );
        }
        return description;
    }
    public void run ( final RunNotifier notifier ) {
        for ( final Throwable each : this.causes ) {
            this.runCause ( each, notifier );
        }
    }
    private String getClassNames ( final Class<?>... testClasses ) {
        final StringBuilder builder = new StringBuilder();
        for ( final Class<?> testClass : testClasses ) {
            if ( builder.length() != 0 ) {
                builder.append ( ", " );
            }
            builder.append ( testClass.getName() );
        }
        return builder.toString();
    }
    private List<Throwable> getCauses ( final Throwable cause ) {
        if ( cause instanceof InvocationTargetException ) {
            return this.getCauses ( cause.getCause() );
        }
        if ( cause instanceof InvalidTestClassError ) {
            return Collections.singletonList ( cause );
        }
        if ( cause instanceof InitializationError ) {
            return ( ( InitializationError ) cause ).getCauses();
        }
        if ( cause instanceof org.junit.internal.runners.InitializationError ) {
            return ( ( org.junit.internal.runners.InitializationError ) cause ).getCauses();
        }
        return Collections.singletonList ( cause );
    }
    private Description describeCause() {
        return Description.createTestDescription ( this.classNames, "initializationError", new Annotation[0] );
    }
    private void runCause ( final Throwable child, final RunNotifier notifier ) {
        final Description description = this.describeCause();
        notifier.fireTestStarted ( description );
        notifier.fireTestFailure ( new Failure ( description, child ) );
        notifier.fireTestFinished ( description );
    }
}
