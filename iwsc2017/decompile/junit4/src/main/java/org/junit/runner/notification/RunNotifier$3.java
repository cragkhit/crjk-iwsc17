package org.junit.runner.notification;
import org.junit.runner.Description;
class RunNotifier$3 extends SafeNotifier {
    final   Description val$description;
    protected void notifyListener ( final RunListener each ) throws Exception {
        each.testSuiteStarted ( this.val$description );
    }
}
