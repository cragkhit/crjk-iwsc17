package org.junit.internal.requests;
import org.junit.internal.builders.SuiteMethodBuilder;
import org.junit.runners.model.RunnerBuilder;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.runner.Runner;
import org.junit.runner.Request;
public class ClassRequest extends Request {
    private final Object runnerLock;
    private final Class<?> fTestClass;
    private final boolean canUseSuiteMethod;
    private volatile Runner runner;
    public ClassRequest ( final Class<?> testClass, final boolean canUseSuiteMethod ) {
        this.runnerLock = new Object();
        this.fTestClass = testClass;
        this.canUseSuiteMethod = canUseSuiteMethod;
    }
    public ClassRequest ( final Class<?> testClass ) {
        this ( testClass, true );
    }
    public Runner getRunner() {
        if ( this.runner == null ) {
            synchronized ( this.runnerLock ) {
                if ( this.runner == null ) {
                    this.runner = new CustomAllDefaultPossibilitiesBuilder().safeRunnerForClass ( this.fTestClass );
                }
            }
        }
        return this.runner;
    }
    private class CustomAllDefaultPossibilitiesBuilder extends AllDefaultPossibilitiesBuilder {
        protected RunnerBuilder suiteMethodBuilder() {
            return new CustomSuiteMethodBuilder();
        }
    }
    private class CustomSuiteMethodBuilder extends SuiteMethodBuilder {
        public Runner runnerForClass ( final Class<?> testClass ) throws Throwable {
            if ( testClass == ClassRequest.this.fTestClass && !ClassRequest.this.canUseSuiteMethod ) {
                return null;
            }
            return super.runnerForClass ( testClass );
        }
    }
}
