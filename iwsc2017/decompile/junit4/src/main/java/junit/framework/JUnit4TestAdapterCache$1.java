package junit.framework;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
class JUnit4TestAdapterCache$1 extends RunListener {
    final   TestResult val$result;
    public void testFailure ( final Failure failure ) throws Exception {
        this.val$result.addError ( JUnit4TestAdapterCache.this.asTest ( failure.getDescription() ), failure.getException() );
    }
    public void testFinished ( final Description description ) throws Exception {
        this.val$result.endTest ( JUnit4TestAdapterCache.this.asTest ( description ) );
    }
    public void testStarted ( final Description description ) throws Exception {
        this.val$result.startTest ( JUnit4TestAdapterCache.this.asTest ( description ) );
    }
}
