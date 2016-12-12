package org.junit.internal.builders;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;
public class JUnit4Builder extends RunnerBuilder {
    public Runner runnerForClass ( final Class<?> testClass ) throws Throwable {
        return new BlockJUnit4ClassRunner ( testClass );
    }
}
