package org.junit.runner.notification;
import java.util.Iterator;
import org.junit.runner.Description;
import java.util.ArrayList;
import java.util.List;
private abstract class SafeNotifier {
    private final List<RunListener> currentListeners;
    SafeNotifier ( final RunNotifier x0 ) {
        this ( RunNotifier.access$000 ( x0 ) );
    }
    SafeNotifier ( final List<RunListener> currentListeners ) {
        this.currentListeners = currentListeners;
    }
    void run() {
        final int capacity = this.currentListeners.size();
        final List<RunListener> safeListeners = new ArrayList<RunListener> ( capacity );
        final List<Failure> failures = new ArrayList<Failure> ( capacity );
        for ( final RunListener listener : this.currentListeners ) {
            try {
                this.notifyListener ( listener );
                safeListeners.add ( listener );
            } catch ( Exception e ) {
                failures.add ( new Failure ( Description.TEST_MECHANISM, e ) );
            }
        }
        RunNotifier.access$100 ( RunNotifier.this, safeListeners, failures );
    }
    protected abstract void notifyListener ( final RunListener p0 ) throws Exception;
}
