package org.junit.runners;
import org.junit.runner.notification.RunNotifier;
class ParentRunner$3 implements Runnable {
    final   Object val$each;
    final   RunNotifier val$notifier;
    public void run() {
        ParentRunner.this.runChild ( this.val$each, this.val$notifier );
    }
}
