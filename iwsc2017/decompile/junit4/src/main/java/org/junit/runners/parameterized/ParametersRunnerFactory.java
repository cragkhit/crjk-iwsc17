package org.junit.runners.parameterized;
import org.junit.runners.model.InitializationError;
import org.junit.runner.Runner;
public interface ParametersRunnerFactory {
    Runner createRunnerForTestWithParameters ( TestWithParameters p0 ) throws InitializationError;
}
