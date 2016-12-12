package org.apache.juli;
import java.util.logging.LogRecord;
protected static class LogEntry {
    private final LogRecord record;
    private final AsyncFileHandler handler;
    public LogEntry ( final LogRecord record, final AsyncFileHandler handler ) {
        this.record = record;
        this.handler = handler;
    }
    public boolean flush() {
        if ( this.handler.closed ) {
            return false;
        }
        this.handler.publishInternal ( this.record );
        return true;
    }
}
