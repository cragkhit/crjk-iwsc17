package org.junit.runners;
import org.junit.internal.runners.SuiteMethod;
public class AllTests extends SuiteMethod {
    public AllTests ( final Class<?> klass ) throws Throwable {
        super ( klass );
    }
}
