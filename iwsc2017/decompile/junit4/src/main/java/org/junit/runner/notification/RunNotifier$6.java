package org.junit.runner.notification;
import java.util.Iterator;
import java.util.List;
class RunNotifier$6 extends SafeNotifier {
    final   List val$failures;
    protected void notifyListener ( final RunListener listener ) throws Exception {
        for ( final Failure each : this.val$failures ) {
            listener.testFailure ( each );
        }
    }
}
