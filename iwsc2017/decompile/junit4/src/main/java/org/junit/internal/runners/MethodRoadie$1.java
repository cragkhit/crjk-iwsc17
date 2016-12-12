package org.junit.internal.runners;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;
import org.junit.runners.model.TestTimedOutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
class MethodRoadie$1 implements Runnable {
    final   long val$timeout;
    public void run() {
        final ExecutorService service = Executors.newSingleThreadExecutor();
        final Callable<Object> callable = new Callable<Object>() {
            public Object call() throws Exception {
                MethodRoadie.this.runTestMethod();
                return null;
            }
        };
        final Future<Object> result = service.submit ( callable );
        service.shutdown();
        try {
            final boolean terminated = service.awaitTermination ( this.val$timeout, TimeUnit.MILLISECONDS );
            if ( !terminated ) {
                service.shutdownNow();
            }
            result.get ( 0L, TimeUnit.MILLISECONDS );
        } catch ( TimeoutException e2 ) {
            MethodRoadie.this.addFailure ( new TestTimedOutException ( this.val$timeout, TimeUnit.MILLISECONDS ) );
        } catch ( Exception e ) {
            MethodRoadie.this.addFailure ( e );
        }
    }
}
