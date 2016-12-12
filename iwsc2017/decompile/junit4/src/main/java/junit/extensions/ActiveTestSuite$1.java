package junit.extensions;
import junit.framework.TestResult;
import junit.framework.Test;
class ActiveTestSuite$1 extends Thread {
    final   Test val$test;
    final   TestResult val$result;
    public void run() {
        try {
            this.val$test.run ( this.val$result );
        } finally {
            ActiveTestSuite.this.runFinished();
        }
    }
}
