package junit.extensions;
import junit.framework.TestResult;
import junit.framework.Protectable;
class TestSetup$1 implements Protectable {
    final   TestResult val$result;
    public void protect() throws Exception {
        TestSetup.this.setUp();
        TestSetup.this.basicRun ( this.val$result );
        TestSetup.this.tearDown();
    }
}
