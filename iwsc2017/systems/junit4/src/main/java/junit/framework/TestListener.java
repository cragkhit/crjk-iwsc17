package junit.framework;
public interface TestListener {
    public void addError ( Test test, Throwable e );
    public void addFailure ( Test test, AssertionFailedError e );
    public void endTest ( Test test );
    public void startTest ( Test test );
}
