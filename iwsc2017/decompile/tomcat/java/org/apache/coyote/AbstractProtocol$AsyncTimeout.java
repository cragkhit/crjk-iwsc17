package org.apache.coyote;
import java.util.Iterator;
protected class AsyncTimeout implements Runnable {
    private volatile boolean asyncTimeoutRunning;
    protected AsyncTimeout() {
        this.asyncTimeoutRunning = true;
    }
    @Override
    public void run() {
        while ( this.asyncTimeoutRunning ) {
            try {
                Thread.sleep ( 1000L );
            } catch ( InterruptedException ex ) {}
            final long now = System.currentTimeMillis();
            for ( final Processor processor : AbstractProtocol.access$100 ( AbstractProtocol.this ) ) {
                processor.timeoutAsync ( now );
            }
            while ( AbstractProtocol.access$200 ( AbstractProtocol.this ).isPaused() && this.asyncTimeoutRunning ) {
                try {
                    Thread.sleep ( 1000L );
                } catch ( InterruptedException ex2 ) {}
            }
        }
    }
    protected void stop() {
        this.asyncTimeoutRunning = false;
        for ( final Processor processor : AbstractProtocol.access$100 ( AbstractProtocol.this ) ) {
            processor.timeoutAsync ( -1L );
        }
    }
}
