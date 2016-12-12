package org.junit.internal.requests;
import org.junit.runner.Runner;
import org.junit.internal.builders.SuiteMethodBuilder;
private class CustomSuiteMethodBuilder extends SuiteMethodBuilder {
    public Runner runnerForClass ( final Class<?> testClass ) throws Throwable {
        if ( testClass == ClassRequest.access$200 ( ClassRequest.this ) && !ClassRequest.access$300 ( ClassRequest.this ) ) {
            return null;
        }
        return super.runnerForClass ( testClass );
    }
}
