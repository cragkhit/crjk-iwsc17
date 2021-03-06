package junit.framework;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
public class TestResult {
    protected List<TestFailure> fFailures;
    protected List<TestFailure> fErrors;
    protected List<TestListener> fListeners;
    protected int fRunTests;
    private boolean fStop;
    public TestResult() {
        fFailures = new ArrayList<TestFailure>();
        fErrors = new ArrayList<TestFailure>();
        fListeners = new ArrayList<TestListener>();
        fRunTests = 0;
        fStop = false;
    }
    public synchronized void addError ( Test test, Throwable e ) {
        fErrors.add ( new TestFailure ( test, e ) );
        for ( TestListener each : cloneListeners() ) {
            each.addError ( test, e );
        }
    }
    public synchronized void addFailure ( Test test, AssertionFailedError e ) {
        fFailures.add ( new TestFailure ( test, e ) );
        for ( TestListener each : cloneListeners() ) {
            each.addFailure ( test, e );
        }
    }
    public synchronized void addListener ( TestListener listener ) {
        fListeners.add ( listener );
    }
    public synchronized void removeListener ( TestListener listener ) {
        fListeners.remove ( listener );
    }
    private synchronized List<TestListener> cloneListeners() {
        List<TestListener> result = new ArrayList<TestListener>();
        result.addAll ( fListeners );
        return result;
    }
    public void endTest ( Test test ) {
        for ( TestListener each : cloneListeners() ) {
            each.endTest ( test );
        }
    }
    public synchronized int errorCount() {
        return fErrors.size();
    }
    public synchronized Enumeration<TestFailure> errors() {
        return Collections.enumeration ( fErrors );
    }
    public synchronized int failureCount() {
        return fFailures.size();
    }
    public synchronized Enumeration<TestFailure> failures() {
        return Collections.enumeration ( fFailures );
    }
    protected void run ( final TestCase test ) {
        startTest ( test );
        Protectable p = new Protectable() {
            public void protect() throws Throwable {
                test.runBare();
            }
        };
        runProtected ( test, p );
        endTest ( test );
    }
    public synchronized int runCount() {
        return fRunTests;
    }
    public void runProtected ( final Test test, Protectable p ) {
        try {
            p.protect();
        } catch ( AssertionFailedError e ) {
            addFailure ( test, e );
        } catch ( ThreadDeath e ) {
            throw e;
        } catch ( Throwable e ) {
            addError ( test, e );
        }
    }
    public synchronized boolean shouldStop() {
        return fStop;
    }
    public void startTest ( Test test ) {
        final int count = test.countTestCases();
        synchronized ( this ) {
            fRunTests += count;
        }
        for ( TestListener each : cloneListeners() ) {
            each.startTest ( test );
        }
    }
    public synchronized void stop() {
        fStop = true;
    }
    public synchronized boolean wasSuccessful() {
        return failureCount() == 0 && errorCount() == 0;
    }
}
