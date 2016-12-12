package org.junit.internal.runners.statements;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Callable;
private class CallableStatement implements Callable<Throwable> {
    private final CountDownLatch startLatch;
    private CallableStatement() {
        this.startLatch = new CountDownLatch ( 1 );
    }
    public Throwable call() throws Exception {
        try {
            this.startLatch.countDown();
            FailOnTimeout.access$600 ( FailOnTimeout.this ).evaluate();
        } catch ( Exception e ) {
            throw e;
        } catch ( Throwable e2 ) {
            return e2;
        }
        return null;
    }
    public void awaitStarted() throws InterruptedException {
        this.startLatch.await();
    }
}
