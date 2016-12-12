package org.junit.runner.notification;
class RunNotifier$7 extends SafeNotifier {
    final   Failure val$failure;
    protected void notifyListener ( final RunListener each ) throws Exception {
        each.testAssumptionFailure ( this.val$failure );
    }
}
