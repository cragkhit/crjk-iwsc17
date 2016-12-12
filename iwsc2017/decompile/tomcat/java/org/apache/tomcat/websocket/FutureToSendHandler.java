package org.apache.tomcat.websocket;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;
import javax.websocket.SendResult;
import java.util.concurrent.CountDownLatch;
import org.apache.tomcat.util.res.StringManager;
import javax.websocket.SendHandler;
import java.util.concurrent.Future;
class FutureToSendHandler implements Future<Void>, SendHandler {
    private static final StringManager sm;
    private final CountDownLatch latch;
    private final WsSession wsSession;
    private volatile SendResult result;
    public FutureToSendHandler ( final WsSession wsSession ) {
        this.latch = new CountDownLatch ( 1 );
        this.result = null;
        this.wsSession = wsSession;
    }
    public void onResult ( final SendResult result ) {
        this.result = result;
        this.latch.countDown();
    }
    @Override
    public boolean cancel ( final boolean mayInterruptIfRunning ) {
        return false;
    }
    @Override
    public boolean isCancelled() {
        return false;
    }
    @Override
    public boolean isDone() {
        return this.latch.getCount() == 0L;
    }
    @Override
    public Void get() throws InterruptedException, ExecutionException {
        try {
            this.wsSession.registerFuture ( this );
            this.latch.await();
        } finally {
            this.wsSession.unregisterFuture ( this );
        }
        if ( this.result.getException() != null ) {
            throw new ExecutionException ( this.result.getException() );
        }
        return null;
    }
    @Override
    public Void get ( final long timeout, final TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException {
        boolean retval = false;
        try {
            this.wsSession.registerFuture ( this );
            retval = this.latch.await ( timeout, unit );
        } finally {
            this.wsSession.unregisterFuture ( this );
        }
        if ( !retval ) {
            throw new TimeoutException ( FutureToSendHandler.sm.getString ( "futureToSendHandler.timeout", timeout, unit.toString().toLowerCase() ) );
        }
        if ( this.result.getException() != null ) {
            throw new ExecutionException ( this.result.getException() );
        }
        return null;
    }
    static {
        sm = StringManager.getManager ( FutureToSendHandler.class );
    }
}
