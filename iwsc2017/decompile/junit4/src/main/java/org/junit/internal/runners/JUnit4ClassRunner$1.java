package org.junit.internal.runners;
import org.junit.runner.notification.RunNotifier;
class JUnit4ClassRunner$1 implements Runnable {
    final   RunNotifier val$notifier;
    public void run() {
        JUnit4ClassRunner.this.runMethods ( this.val$notifier );
    }
}
