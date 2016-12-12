package junit.framework;
public interface TestListener {
    void addError ( Test p0, Throwable p1 );
    void addFailure ( Test p0, AssertionFailedError p1 );
    void endTest ( Test p0 );
    void startTest ( Test p0 );
}
