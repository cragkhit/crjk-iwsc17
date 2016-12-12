package org.apache.juli;
import java.util.concurrent.TimeUnit;
protected static class LoggerThread extends Thread {
    protected final boolean run = true;
    public LoggerThread() {
        this.setDaemon ( this.run );
        this.setName ( "AsyncFileHandlerWriter-" + System.identityHashCode ( this ) );
    }
    @Override
    public void run() {
        while ( true ) {
            try {
                while ( true ) {
                    final LogEntry entry = AsyncFileHandler.queue.poll ( AsyncFileHandler.LOGGER_SLEEP_TIME, TimeUnit.MILLISECONDS );
                    if ( entry != null ) {
                        entry.flush();
                    }
                }
            } catch ( InterruptedException ex ) {
                continue;
            } catch ( Exception x ) {
                x.printStackTrace();
                continue;
            }
            break;
        }
    }
}
