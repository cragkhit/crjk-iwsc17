package org.junit.runner.notification;
import org.junit.runner.Result;
class RunNotifier$2 extends SafeNotifier {
    final   Result val$result;
    protected void notifyListener ( final RunListener each ) throws Exception {
        each.testRunFinished ( this.val$result );
    }
}
