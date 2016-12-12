package org.junit.runners.parameterized;
import org.junit.runners.model.InitializationError;
import org.junit.runner.Runner;
public class BlockJUnit4ClassRunnerWithParametersFactory implements ParametersRunnerFactory {
    public Runner createRunnerForTestWithParameters ( final TestWithParameters test ) throws InitializationError {
        return new BlockJUnit4ClassRunnerWithParameters ( test );
    }
}
