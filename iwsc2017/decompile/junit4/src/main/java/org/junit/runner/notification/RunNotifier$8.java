package org.junit.runner.notification;
import org.junit.runner.Description;
class RunNotifier$8 extends SafeNotifier {
    final   Description val$description;
    protected void notifyListener ( final RunListener each ) throws Exception {
        each.testIgnored ( this.val$description );
    }
}
