package junit.framework;
import org.junit.internal.Throwables;
public class TestFailure {
    protected Test fFailedTest;
    protected Throwable fThrownException;
    public TestFailure ( Test failedTest, Throwable thrownException ) {
        fFailedTest = failedTest;
        fThrownException = thrownException;
    }
    public Test failedTest() {
        return fFailedTest;
    }
    public Throwable thrownException() {
        return fThrownException;
    }
    @Override
    public String toString() {
        return fFailedTest + ": " + fThrownException.getMessage();
    }
    public String trace() {
        return Throwables.getStacktrace ( thrownException() );
    }
    public String exceptionMessage() {
        return thrownException().getMessage();
    }
    public boolean isFailure() {
        return thrownException() instanceof AssertionFailedError;
    }
}
