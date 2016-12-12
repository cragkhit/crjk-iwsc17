package org.junit.runner;
import org.junit.runners.model.RunnerBuilder;
class Computer$1 extends RunnerBuilder {
    final   RunnerBuilder val$builder;
    public Runner runnerForClass ( final Class<?> testClass ) throws Throwable {
        return Computer.this.getRunner ( this.val$builder, testClass );
    }
}
