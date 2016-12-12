package junit.framework;
import org.junit.internal.Throwables;
public class TestFailure {
    protected Test fFailedTest;
    protected Throwable fThrownException;
    public TestFailure ( final Test failedTest, final Throwable thrownException ) {
        this.fFailedTest = failedTest;
        this.fThrownException = thrownException;
    }
    public Test failedTest() {
        return this.fFailedTest;
    }
    public Throwable thrownException() {
        return this.fThrownException;
    }
    public String toString() {
        return this.fFailedTest + ": " + this.fThrownException.getMessage();
    }
    public String trace() {
        return Throwables.getStacktrace ( this.thrownException() );
    }
    public String exceptionMessage() {
        return this.thrownException().getMessage();
    }
    public boolean isFailure() {
        return this.thrownException() instanceof AssertionFailedError;
    }
}
